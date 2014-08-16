/**
 * Service qui receptionne les differents evenements interessant l'application: - GPS - SMS -
 * Telephone - Batterie - Horloge (annonce de l'heure)
 */
package com.lpi.compagnondumotard.service;

import java.util.Calendar;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.BatteryManager;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.ContactsContract.PhoneLookup;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.widget.Toast;

import com.lpi.compagnondumotard.Constants;
import com.lpi.compagnondumotard.Log;
import com.lpi.compagnondumotard.Preferences;
import com.lpi.compagnondumotard.R;
import com.lpi.compagnondumotard.TableauDeBordActivity;
import com.lpi.compagnondumotard.phone.MyCallObserver;
import com.lpi.compagnondumotard.phone.SMSObserver;
import com.lpi.compagnondumotard.tts.TTSListener;
import com.lpi.compagnondumotard.tts.TTSManager;

/**
 * @author lucien
 */
public class CompanionService extends Service implements LocationListener, TTSListener
{
	private static final String TAG = "CompanionService"; //$NON-NLS-1$
	private static final int NOTIFICATION_ID = 1;

	public static final int TYPEALARME_RIEN = 0;
	public static final int TYPEALARME_PAUSE = 1;
	public static final int TYPEALARME_HEURE = 2;
	private static final String[] COLONNES_NUMERO = new String[]
	{ PhoneLookup.DISPLAY_NAME };
	private int etatService = Constants.ETAT_ARRETE;

	// Preferences
	LocationManager locationManager;

	private TTSManager tts;

	private PendingIntent pendingIntentAlarme;
	private BatteryChangeReceiver batteryReceiver;
	private SMSObserver mSMSObserver;

	private boolean _StopEnCours = false;

	@Override
	public int onStartCommand(Intent intent, int flags, int startId)
	{
		Log.debug("OnCreate"); //$NON-NLS-1$
		super.onStartCommand(intent, flags, startId);
		PersistentData data = new PersistentData(this);
		try
		{
			tts = new TTSManager(this, this);

			IntentFilter intentFilter = new IntentFilter();
			intentFilter.addAction(Constants.TIME_UPDATE);
			intentFilter.addAction(Constants.PAUSE_UPDATE);
			intentFilter.addAction(Constants.SERVICE_COMMAND);
			registerReceiver(new BroadcastReceiver() {
				@Override
				public void onReceive(Context context, Intent intent)
				{
					onServiceReceive(context, intent);
				}
			}, intentFilter);

			// Affiche une icone de notification pour montrer qu'on est la
			notificationIcon(true);
			// LoadState() ;
			if (etatService == Constants.ETAT_ENROUTE)
				restart(getApplicationContext(), data);
			else
				start(data);
		} catch (Exception e)
		{
			Erreur(this, e);
		}
		data.Flush(this);
		return START_STICKY;
	}

	/*
	 * Affiche une icone et un message dans la barre de notification
	 */
	private void notificationIcon(boolean trajetDemarre)
	{
		NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		String message = getString(trajetDemarre ? R.string.TrajetDemarre : R.string.TrajetArrete);

		Notification notification = new Notification(R.drawable.ic_stat_notification, message,
				System.currentTimeMillis());
		Intent notificationIntent = new Intent(this, TableauDeBordActivity.class);
		PendingIntent contentIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);

		notification.setLatestEventInfo(getApplicationContext(), getResources().getString(R.string.app_name), message,
				contentIntent);
		mNotificationManager.notify(NOTIFICATION_ID, notification);
	}

	/**
	 * INitialisation des differents services
	 */
	private void InitServices(PersistentData data)
	{
		Log.debug("InitServices"); //$NON-NLS-1$
		Preferences pref = new Preferences(this);

		try
		{
			if ((pref.getVitesseMax() > 0) || pref.isAlerteDemiReservoir() || pref.isAlerteQuartReservoir())
			{
				if (locationManager == null)
				{
					// GPS
					locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
					locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 10, this);
				}
			} else
				locationManager = null;

			if ((pref.getMinutesEntrePauses() > 0) || pref.getMinutesEntreAnnonceHeure() > 0)
			{
				// Annonce de l'heure
				setNextAlarm(data);
			}

			if (pref.isLireSMS())
			{
				if (mSMSObserver == null)
				{
					// Lire les SMS
					Log.debug("InitSMS"); //$NON-NLS-1$
					mSMSObserver = new SMSObserver(this, this);
					getContentResolver().registerContentObserver(Uri.parse("content://sms"), true, mSMSObserver); //$NON-NLS-1$
				}
			}

			if (pref.isSeTaireTelephoneDecroche())
			{
				MyCallObserver phoneListener = new MyCallObserver(this);
				TelephonyManager telephony = (TelephonyManager) getApplicationContext().getSystemService(
						Context.TELEPHONY_SERVICE);
				telephony.listen(phoneListener, PhoneStateListener.LISTEN_CALL_STATE);
			}

			// Changement d etat de la batterie
			IntentFilter intentFilter = new IntentFilter();
			intentFilter.addAction(Intent.ACTION_BATTERY_CHANGED);
			batteryReceiver = new BatteryChangeReceiver(this);
			registerReceiver(batteryReceiver, intentFilter);

		} catch (Exception e)
		{
			Erreur(e.getLocalizedMessage());
		}

	}

	/**
	 * Arrete les services
	 */
	private void StopServices()
	{
		Log.debug("StopServices"); //$NON-NLS-1$

		// GPS
		if (locationManager != null)
		{
			locationManager.removeUpdates(this);
			locationManager = null;
		}

		// Alarme : pause, heure...
		if (pendingIntentAlarme != null)
		{
			AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
			alarmManager.cancel(pendingIntentAlarme);
			pendingIntentAlarme = null;
		}

		// Lecture SMS
		if (mSMSObserver != null)
		{
			getContentResolver().unregisterContentObserver(mSMSObserver);
			mSMSObserver = null;
		}

		if (batteryReceiver != null)
		{
			try
			{
				unregisterReceiver(batteryReceiver);
			} catch (Exception e)
			{
				e.printStackTrace();
			}
		}
	}

	/***
	 * Reception d'un SMS
	 */
	public void onSMS()
	{
		Log.debug("OnSMS"); //$NON-NLS-1$
		PersistentData data = new PersistentData(this);
		try
		{
			// Lire le dernier SMS non lu
			@SuppressWarnings("nls")
			Cursor cur = getApplicationContext().getContentResolver().query(Uri.parse("content://sms//inbox"),
					new String[]
					{ "address", "body", "date " }, "read = 0", null, android.provider.CallLog.Calls.DATE + " DESC");
			if (cur == null)
				return;

			cur.moveToFirst();
			long date = cur.getLong(2);

			if (date != data._DernierSMS)
			{
				String adresse = cur.getString(0);
				String body = cur.getString(1);
				String contact = getContactFromNumber(adresse);
				tts.AnnonceVocale(this, R.string.formatOnSMS, contact, body);
				data._DernierSMS = date;
				data._modifie = true;
			}

			cur.close();
		} catch (Exception e)
		{
			e.printStackTrace();
			Erreur(e.getLocalizedMessage());
		}

		data.Flush(this);
	}

	/**
	 * Essaie de retrouver le nom d'un contact a partir de son numero de telephone
	 *
	 * @param numero
	 *            : numero appelant
	 * @return le nom du contact ou "numero inconnu "+numero
	 */
	public String getContactFromNumber(String numero)
	{
		String res;

		try
		{
			Cursor c = getContentResolver().query(
					Uri.withAppendedPath(PhoneLookup.CONTENT_FILTER_URI, Uri.encode(numero)), COLONNES_NUMERO, null,
					null, null);
			c.moveToFirst();
			res = c.getString(c.getColumnIndexOrThrow(PhoneLookup.DISPLAY_NAME));
			c.close();

		} catch (Exception e)
		{
			if (numero.startsWith("+33")) //$NON-NLS-1$
			{
				numero = "0" + numero.substring(3); //$NON-NLS-1$
				return getContactFromNumber(numero);
			} else
				res = null;
		}

		if (res != null)
			return res;

		String strFormat = getResources().getString(R.string.unknownContact);
		return String.format(strFormat, numero);
	}

	/**
	 * Reception d'un message a destination de ce service
	 *
	 * @param context
	 *            : Context
	 * @param intent
	 *            : Intent recu
	 */
	protected void onServiceReceive(Context context, Intent intent)
	{
		String action = intent.getAction();
		Log.debug("OnServiceReceive " + action); //$NON-NLS-1$
		PersistentData data = new PersistentData(this);

		if (action.equals(Constants.TIME_UPDATE))
			announceHour(context, data);
		else if (action.equals(Constants.PAUSE_UPDATE))
			announcePause(context, data);
		else if (action.equals(Constants.SERVICE_COMMAND))
			executeCommand(intent, data);

		setNextAlarm(data);
		data.Flush(this);
	}

	/**
	 * Execute une des commandes envoyees a ce service
	 *
	 * @param intent
	 * @param data
	 */
	private void executeCommand(Intent intent, PersistentData data)
	{
		final int Command = intent.getIntExtra(Constants.COMMAND, -1);
		Log.debug("ExecuteCommand " + Command); //$NON-NLS-1$
		switch (Command)
		{
		case Constants.COMMAND_START:
			start(data);
			break;

		case Constants.COMMAND_STOP:
			Stop(data);
			break;

		case Constants.COMMAND_RAZ:
			Raz(data);
			break;

		case Constants.COMMAND_GETSTATE:
			getState(data);
			break;

		case Constants.COMMAND_PLEIN:
			refillTank(data);
			break;

		case Constants.COMMAND_PAUSE:
			pause(intent, data);
			break;

		default:
			Log.debug("action inconnue" + Command); //$NON-NLS-1$
		}
	}

	/**
	 * Signale qu'on est en pause
	 *
	 * @param intent
	 */
	private void pause(Intent intent, PersistentData data)
	{
		final boolean enPause = intent.getBooleanExtra(Constants.COMMAND_PAUSE_STATE, false);
		if (enPause)
		{
			tts.AnnonceVocale(this, R.string.pause);
			if (pendingIntentAlarme != null)
			{
				AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
				alarmManager.cancel(pendingIntentAlarme);
			}
		} else
		{
			tts.AnnonceVocale(this, R.string.pauseFinished);
			Calendar maintenant = Calendar.getInstance();
			data._DerniereHeurePause = maintenant.getTimeInMillis();
			data._modifie = true;
			setNextAlarm(data);
		}
	}

	/**
	 * Bouton "plein d'essence"
	 * 
	 * @param data
	 */
	private void refillTank(PersistentData data)
	{
		Preferences pref = new Preferences(this);
		data._Autonomie = pref.getAutonomieReservoir();
		data._modifie = true;
		tts.AnnonceVocale(this, R.string.tankFixed, Long.valueOf(pref.getAutonomieReservoir()));

		// Mettre a jour l'UI
		Intent broadcastIntent = new Intent();
		broadcastIntent.setAction(Constants.KILOMETER_CHANGE);
		broadcastIntent.putExtra(Constants.PARCOURU, data._DistanceParcourue);
		broadcastIntent.putExtra(Constants.AUTONOMIE, data._Autonomie);
		sendBroadcast(broadcastIntent);
	}

	/**
	 * L'activite demande dans quel etat on se trouve
	 *
	 * @param data
	 */
	private void getState(PersistentData data)
	{
		// Log.d(Constants.TAG, "CommandGetState " + etatService) ;
		Intent broadcastIntent = new Intent(); // , TableauDeBordActivity.class);
		broadcastIntent.setAction(Constants.SERVICE_ANSWER);
		broadcastIntent.putExtra(Constants.COMMANDE_STATE, etatService);
		sendBroadcast(broadcastIntent);
	}

	/**
	 * Remise a zero du trajet
	 *
	 * @param data
	 */
	private static void Raz(PersistentData data)
	{
		// TODO Auto-generated method stub
		Log.debug("Raz"); //$NON-NLS-1$

	}

	/**
	 * Arreter le trajet
	 *
	 * @param data
	 */
	private void Stop(PersistentData data)
	{
		etatService = Constants.ETAT_ARRETE;

		// Log.d(Constants.TAG, "Stop") ;
		_StopEnCours = true;
		tts.AnnonceVocale(this, R.string.stopTrip);
		tts.flushAnnonces();
		StopServices();
		notificationIcon(false);

		// Fermer le service, puisqu'il ne sert plus a rien
		// stopSelf() ;
	}

	/**
	 * Demarrer le trajet
	 * 
	 * @param data
	 */
	private void start(PersistentData data)
	{
		etatService = Constants.ETAT_ENROUTE;
		Preferences pref = new Preferences(this);

		tts.AnnonceVocale(this, R.string.startTrip, Long.valueOf(pref.getAutonomieReservoir()));
		resetTrip(data);
		InitServices(data);
	}

	/**
	 * Demarrer le trajet
	 *
	 * @param context
	 */
	private void restart(Context context, PersistentData data)
	{
		InitServices(data);

		// LitPreferences(getApplicationContext()) ;
		// tts.AnnonceVocale( "Demarrage du service deja en route") ;
		InitServices(data);
	}

	/***
	 * Annonce qu'il est temps de faire une pause
	 * 
	 * @param context
	 * @param data
	 */
	private void announcePause(Context context, PersistentData data)
	{
		Log.debug("!!AnnoncePause"); //$NON-NLS-1$
		pendingIntentAlarme = null;
		data._DerniereHeurePause = System.currentTimeMillis();
		data._modifie = true;
		tts.AnnonceVocale(this, R.string.timeforpause);
	}

	/**
	 * Fait l'annonce de l'heure
	 *
	 * @param context
	 * @param data
	 *
	 */
	private void announceHour(Context context, PersistentData data)
	{
		Log.debug("!!Annonce heure");
		pendingIntentAlarme = null;

		Calendar c = Calendar.getInstance();
		tts.AnnonceVocale(this, R.string.announceHour, Integer.valueOf(c.get(Calendar.HOUR_OF_DAY)),
				Integer.valueOf(c.get(Calendar.MINUTE)));

		c.set(Calendar.SECOND, 0);
		data._DerniereHeureAlarme = c.getTimeInMillis();
		data._modifie = true;
	}

	/**
	 * Programme le prochain evenement a annoncer: pause ou quart d'heure
	 */
	private void setNextAlarm(PersistentData data)
	{
		Log.debug("setNextAlarm");
		Preferences pref = new Preferences(this);

		long ProchaineAlarme = Long.MAX_VALUE;
		int type = TYPEALARME_RIEN;

		// Annonce des pauses
		final long minutesEntrePauses = pref.getMinutesEntrePauses();
		if (minutesEntrePauses > 0)
		{
			Log.debug("derniere pause" + toHourString(data._DerniereHeurePause));
			Log.debug("minutes entre pauses" + minutesEntrePauses);
			type = TYPEALARME_PAUSE;
			ProchaineAlarme = data._DerniereHeurePause + (minutesEntrePauses * 60 * 1000);
			Log.debug("Prochaine pause " + toHourString(ProchaineAlarme));
		}

		// Annonce de l'heure
		if (pref.getMinutesEntreAnnonceHeure() > 0)
		{
			long prochainCarillon = getNextHourAlarm(pref.getMinutesEntreAnnonceHeure(), data);
			Log.debug("Prochain carillon " + toHourString(prochainCarillon));
			if (prochainCarillon < ProchaineAlarme)
			{
				type = TYPEALARME_HEURE;
				ProchaineAlarme = prochainCarillon;
			}
		}

		// Configurer l'alarme
		if (type != TYPEALARME_RIEN)
		{
			Intent intent = new Intent(type == TYPEALARME_HEURE ? Constants.TIME_UPDATE : Constants.PAUSE_UPDATE);

			LogHeure("Prochaine alarme ", ProchaineAlarme); //$NON-NLS-1$
			Context context = getApplicationContext();
			pendingIntentAlarme = PendingIntent.getBroadcast(context, 0, intent, 0);
			AlarmManager alarmManager = (AlarmManager) context.getSystemService(ALARM_SERVICE);
			alarmManager.set(AlarmManager.RTC_WAKEUP, ProchaineAlarme, pendingIntentAlarme);
		}
	}

	/**
	 * Calcule quand on doit annoncer la prochaine heure pleine, demi heure ou quart d'heure
	 *
	 * @return
	 */
	private long getNextHourAlarm(int minutesEntreAnnonceHeures, PersistentData data)
	{
		Log.debug("Prochain Carillon, minutes entre annonce heures " + minutesEntreAnnonceHeures);

		Calendar depart = Calendar.getInstance();
		Log.debug("Heure derniere alarme " + toHourString(data._DerniereHeureAlarme));
		depart.setTimeInMillis(data._DerniereHeureAlarme);
		depart.set(Calendar.SECOND, 0);

		int Minutes = depart.get(Calendar.MINUTE);
		Log.debug("Minutes " + Minutes);

		Minutes -= (Minutes % minutesEntreAnnonceHeures);
		Minutes += minutesEntreAnnonceHeures;
		if (Minutes > 60)
		{
			Minutes -= 60;
			depart.roll(Calendar.HOUR_OF_DAY, 1);
		}
		Log.debug("Prochaine minute " + Minutes);
		depart.set(Calendar.MINUTE, Minutes);

		Log.debug("Prochaine " + toHourString(depart.getTimeInMillis()));
		return depart.getTimeInMillis();
	}

	private static void LogHeure(String string, long prochaineAlarme)
	{
		Log.debug(string + toHourString(prochaineAlarme));
	}

	/**
	 * Calcule une representation textuel de l'heure
	 *
	 * @param prochaineAlarme
	 * @return
	 */
	@SuppressWarnings("nls")
	public static String toHourString(long prochaineAlarme)
	{
		Calendar c = Calendar.getInstance();
		c.setTimeInMillis(prochaineAlarme);
		return String.valueOf(c.get(Calendar.HOUR_OF_DAY)) + ":" + c.get(Calendar.MINUTE) + ":"
				+ c.get(Calendar.SECOND);
	}

	/***
	 * Retourne l'heure de la prochaine fois ou une certaine 'minute' arrivera
	 *
	 * @param Minute
	 * @param maintenant
	 * @return
	 */
	/*
	 * private long GetProchaineHeure(int Minute, Calendar maintenant) { Calendar heure =
	 * (Calendar)maintenant.clone() ; if ( heure.get(Calendar.MINUTE) == Minute &&
	 * heure.get(Calendar.SECOND) < 5 ) return maintenant.getTimeInMillis() ;
	 * heure.set(Calendar.SECOND, 0 ) ; if (heure.get(Calendar.MINUTE) > Minute )
	 * heure.roll(Calendar.HOUR_OF_DAY, 1) ; heure.set(Calendar.MINUTE, Minute) ;
	 * LogHeure("Prochaine heure + " + Minute + "=", heure.getTimeInMillis()) ; return
	 * heure.getTimeInMillis() ; }
	 */

	/**
	 * Remise à zero du parcours
	 */
	private void resetTrip(PersistentData data)
	{

		long Now = System.currentTimeMillis();
		data._DerniereAlerteVitesse = Now;
		data._DistanceParcourue = 0;
		data._HeureDepart = Now;
		data._DerniereHeurePause = Now;
		data._DerniereHeureAlarme = Now;
		data._AnnonceQuartReservoir = false;
		data._AnnonceDemiReservoir = false;
		data._modifie = true;

		data.EcritDernieresPositions(this, null, 0);
	}

	/*
	 * (non-Javadoc)
	 * @see android.app.Service#onBind(android.content.Intent)
	 */
	@Override
	public IBinder onBind(Intent arg0)
	{
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * Reception de la nouvelle position par le GPS
	 */
	@Override
	public void onLocationChanged(Location ici)
	{
		Location dernieresPositions[] = new Location[PersistentData.NB_POSITIONS_VITESSE];
		PersistentData data = new PersistentData(this);
		int iDernieresPositions = data.LitDernieresPositions(this, dernieresPositions);

		// On travaille sur une moyenne mobile de plusieurs positions
		if (iDernieresPositions < PersistentData.NB_POSITIONS_VITESSE)
		{
			dernieresPositions[iDernieresPositions] = ici;
			iDernieresPositions++;
			data.EcritDernieresPositions(this, dernieresPositions, iDernieresPositions);
			return;
		} else
		{
			System.arraycopy(dernieresPositions, 1, dernieresPositions, 0, iDernieresPositions - 1);
			dernieresPositions[iDernieresPositions - 1] = ici;
			data.EcritDernieresPositions(this, dernieresPositions, iDernieresPositions);
		}

		Preferences pref = new Preferences(this);

		calcTripLength(dernieresPositions, iDernieresPositions, pref, data);

		final long vitesseMax = pref.getVitesseMax();
		if (vitesseMax > 0)
		{
			speedTest(vitesseMax, data);
		}

		data.Flush(this);
	}

	/***
	 * Lance une alerte si la vitesse est excessive
	 * 
	 * @param vitesseMax
	 * @param data
	 */
	private void speedTest(long vitesseMax, PersistentData data)
	{
		Location dernieresPositions[] = new Location[PersistentData.NB_POSITIONS_VITESSE];
		int iDernieresPositions = data.LitDernieresPositions(this, dernieresPositions);

		Location ici = dernieresPositions[iDernieresPositions - 1];

		final float temps = Math.abs(ici.getTime() - dernieresPositions[0].getTime()) / 1000.f;

		// Calcule de la distance totale entre les n dernieres positions
		float distance = 0;
		for (int i = 0; i < iDernieresPositions - 1; i++)
		{
			distance += dernieresPositions[i].distanceTo(dernieresPositions[i + 1]);
		}

		final float vitesse = (distance / temps);
		final int vitesseKmH = Math.round(vitesse * 3.6f);
		// Log.debug("Vitesse " + vitesseKmH + " km/h");

		if (vitesse > vitesseMax)
			if ((ici.getTime() - data._DerniereAlerteVitesse) > 5000)
			{
				tts.AnnonceVocale(this, R.string.toofast, Long.toString((long) vitesseKmH));
				data._DerniereAlerteVitesse = ici.getTime();
				data._modifie = true;
			}
	}

	/**
	 * Destruction du service par le systeme
	 */
	@Override
	public void onDestroy()
	{
		StopServices();

		tts.shutdown();
	}

	private void calcTripLength(Location[] dernieresPositions, int iDernieresPositions, Preferences pref,
			PersistentData data)
	{
		final long derniereDistance = (long) dernieresPositions[iDernieresPositions - 1]
				.distanceTo(dernieresPositions[iDernieresPositions - 2]);
		final long distanceParcourue = data._DistanceParcourue + derniereDistance;
		data._Autonomie -= derniereDistance;
		data._modifie = true;

		final long autonomieReservoir = pref.getAutonomieReservoir() * 1000;

		// Demi reservoir
		if (data._Autonomie <= (autonomieReservoir / 2))
			if (pref.isAlerteDemiReservoir())
				if (!data._AnnonceDemiReservoir)
				{
					tts.AnnonceVocale(this, R.string.halftank);
					data._AnnonceDemiReservoir = true;
				}

		// Quart de reservoir
		if (data._Autonomie <= (autonomieReservoir / 4L))
			if (pref.isAlerteQuartReservoir())
				if (!data._AnnonceQuartReservoir)
				{
					tts.AnnonceVocale(this, R.string.quartertank);
					data._AnnonceQuartReservoir = true;
				}

		// Mettre a jour l'affichage tous les 100m
		if ((distanceParcourue / 100) != (data._DistanceParcourue / 100))
		{
			// Changer l'UI tous les 100m
			Intent broadcastIntent = new Intent();
			broadcastIntent.setAction(Constants.KILOMETER_CHANGE);
			broadcastIntent.putExtra(Constants.PARCOURU, data._DistanceParcourue);
			broadcastIntent.putExtra(Constants.AUTONOMIE, data._Autonomie);
			sendBroadcast(broadcastIntent);
		}

		data._DistanceParcourue = distanceParcourue;
	}

	@Override
	public void onProviderDisabled(String arg0)
	{
		tts.AnnonceVocale(this, R.string.gpsdeactivated);
	}

	@Override
	public void onProviderEnabled(String arg0)
	{
		Log.debug("OnProviderEnabled"); //$NON-NLS-1$
	}

	@Override
	public void onStatusChanged(String arg0, int arg1, Bundle arg2)
	{
		Log.debug("OnStatusChanged"); //$NON-NLS-1$
	}

	private void Erreur(String text)
	{
		Toast t = Toast.makeText(getApplicationContext(), text, Toast.LENGTH_LONG);
		t.show();
	}

	public void setPhoneState(int state, String incomingNumber)
	{
		boolean telephoneDecroche = false;
		switch (state)
		{
		case TelephonyManager.CALL_STATE_IDLE:
			telephoneDecroche = false;
			break;

		case TelephonyManager.CALL_STATE_OFFHOOK:
			telephoneDecroche = true;
			break;

		case TelephonyManager.CALL_STATE_RINGING:
			telephoneDecroche = true;
			handleTelephonCall(incomingNumber);
			break;
		default:
			assert false;
		}

		tts.set_telephoneDecroche(telephoneDecroche);
	}

	/**
	 * Annonce un appel telephonique
	 *
	 * @param incomingNumber
	 */
	private void handleTelephonCall(String incomingNumber)
	{
		String contact = getContactFromNumber(incomingNumber);
		// String format = getResources().getString(R.string.incoming_call) ;
		// tts.AnnonceVocale( String.format(format, contact) );
		tts.AnnonceVocale(this, R.string.incoming_call, contact);

		// TODO: trouver un moyen de refuser l'appel
	}

	public void batteryChange(int level, int status)
	{
		PersistentData data = new PersistentData(this);
		if (level > 15)
		{
			// Rien a faire
			if (data._AnnonceBatterieFaible)
			{
				data._AnnonceBatterieFaible = false;
				data._modifie = true;
			}
			data.Flush(this);
			return;
		}

		switch (status)
		{
		case BatteryManager.BATTERY_STATUS_FULL:
		case BatteryManager.BATTERY_STATUS_NOT_CHARGING:
		case BatteryManager.BATTERY_STATUS_CHARGING:
			return; // La batterie est en train de se charger, pas la peine de prevenir
		case BatteryManager.BATTERY_STATUS_DISCHARGING:
			if (!data._AnnonceBatterieFaible) // Deja annonce ?
			{
				data._AnnonceBatterieFaible = true;
				data._modifie = true;
				tts.AnnonceVocale(this, R.string.lowbattery, level);
			}
			break;
		default:
			assert false;
		}

		data.Flush(this);
	}

	@Override
	public void onUtteranceCompleted()
	{
		if (_StopEnCours)
		{
			stopSelf();
			_StopEnCours = false;
		}
	}

	/**
	 * Rapporte une erreur detectee par le programme
	 *
	 * @param context
	 * @param e
	 */
	@SuppressWarnings("nls")
	public static void Erreur(Context context, Exception e)
	{
		String message = ""; //$NON-NLS-1$
		if (e == null)
		{
			message = "null exception ?!?"; //$NON-NLS-1$
		} else
		{
			StringBuffer stack = new StringBuffer();

			StackTraceElement[] st = e.getStackTrace();
			for (int i = 0; i < st.length; i++)
			{
				String line = st[i].getMethodName() + ":" + st[i].getLineNumber(); //$NON-NLS-1$
				// Log.e(TAG, line);
				stack.append(line);
				stack.append("\n");
			}
			message = e.getLocalizedMessage() + "\n" + stack.toString(); //$NON-NLS-1$
		}
		Log.erreur(message);
		Toast t = Toast.makeText(context, "Erreur\n" + message + "\n", Toast.LENGTH_LONG); //$NON-NLS-1$
		t.show();

	}

}
