/*
 * Copyright (C) 2007 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.lpi.compagnondumotard;

import java.util.Locale;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.lpi.compagnondumotard.service.CompanionService;

/**
 * This class provides a basic demonstration of how to write an Android
 * activity. Inside of its window, it places a single view: an EditText that
 * displays and edits some internal text.
 */
public class TableauDeBordActivity extends Activity implements OnInitListener   {
	static public final String DEBUG_INTENT = "lpi.BikersCompanion.Debug" ;
	static public final String DEBUG_STRING = "lpi.BikersCompanion.debug" ;
	boolean	GPSEnabled = false ;
	boolean	TTSEnabled = false ;
	boolean pauseEnCours = false ;
	
	private Button btnDemmarrage;
	private Button btnPause ;
	private Button btnPlein ;
	private Button btnConfiguration ;
	private int etat = Constants.ETAT_ARRETE ;

	private TextToSpeech mTts;

	/** Called with the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		try
		{
			super.onCreate(savedInstanceState);

			// Inflate our UI from its XML layout description.
			setContentView(R.layout.activity_tableau_de_bord);
			btnDemmarrage = (Button)findViewById(R.id.buttonDemarrage) ;
			btnPause = (Button)findViewById(R.id.buttonPause) ;
			btnPlein = (Button)findViewById(R.id.buttonPlein) ;
			btnConfiguration = (Button)findViewById(R.id.buttonPreferences) ;
			assert btnDemmarrage != null ;
            assert btnPause != null ;
            assert btnPlein != null ;
            assert btnConfiguration != null ;

			ChangeStartButtonLook(false) ;
			btnPause.setEnabled(false) ;
			btnPlein.setEnabled(false) ;

			//btnRaz = (Button)findViewById(R.id.buttonReset) ;
			//dbv = (DebugView)findViewById(R.id.debugView1 ) ;

			// Reception des messages de debug
			IntentFilter intentFilter = new IntentFilter();
			intentFilter.addAction(DEBUG_INTENT);
			intentFilter.addAction(Constants.SERVICE_ANSWER);
			intentFilter.addAction(Constants.KILOMETER_CHANGE);
			registerReceiver(new BroadcastReceiver(){
				@Override
				public void onReceive(Context context, Intent intent) {
					OnMessageReceive( context, intent ) ;
				}


			}
			, intentFilter);
		} catch (Exception e)
		{
			Log.erreur(e.getLocalizedMessage());
		}

}

    /***
     * Change l'apparence du bouton Demarrer/Arret
     * @param started
     */
    private void ChangeStartButtonLook(boolean started)
    {
        if (btnDemmarrage== null)
            btnDemmarrage = (Button)findViewById(R.id.buttonDemarrage) ;
        assert btnDemmarrage != null ;

        if ( started)
        {
            btnDemmarrage.setText(R.string.btnstoptrip);
            btnDemmarrage.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_av_stop, 0, 0, 0);
        }
        else
        {
            btnDemmarrage.setText(R.string.btnstarttrip);
            btnDemmarrage.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_av_start, 0, 0, 0);

        }
    }


    /***
     * Change l'apparence du bouton Demarrer/Arret
     * @param enPause
     */
    private void ChangePauseButtonLook(boolean enPause )
    {
        if (btnPause== null)
            btnPause = (Button)findViewById(R.id.buttonPause) ;
        assert btnPause != null ;

        if ( enPause)
        {
            btnPause.setText(R.string.btnpause);
            //btnPause.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, R.drawable.ic_av_stop);
        }
        else
        {
            btnPause.setText(R.string.btnenpause);
            //btnPause.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, R.drawable.ic_av_start);

        }
    }



	/**
	 * reception d'un message du service
	 * @param context
	 * @param intent
	 */
	protected void OnMessageReceive(Context context, Intent intent) {
		try
		{
			String action = intent.getAction();
			Log.debug("OnMessageReceive " + action );
			if ( action.equals( DEBUG_INTENT ))
			{
				//String debug = intent.getExtras().getString(DEBUG_STRING) ;
				//dbv.addDebug(debug) ;
			}
			else
				if ( action.equals(Constants.SERVICE_ANSWER ))
				{
					final int	Etat = intent.getIntExtra(Constants.COMMANDE_STATE, Constants.ETAT_ARRETE);
                    ChangeStartButtonLook(Etat == Constants.ETAT_ENROUTE) ;
				}
				else
					if ( action.equals(Constants.KILOMETER_CHANGE))
						changeKilometrage( intent ) ;
		} catch (Exception e)
		{
			Log.erreur(e.getLocalizedMessage());
		}
	}
	
	
	/***
	 * Change le kilometrage affiche
	 * @param intent
	 */
	private void changeKilometrage(Intent intent)
	{
		long kilometre = intent.getLongExtra(Constants.KILOMETRE, 0 ) ;
		Preferences pref = new Preferences(this) ;
		
		String format = getResources().getString(R.string.odometer) ;
		TextView tv = (TextView)findViewById(R.id.textViewOdometer) ;
		tv.setText( String.format( format, Long.toString(kilometre), Long.toString(pref.getAutonomieReservoir()-kilometre)));
	}

	public void OnClicDemarrageArret(View v)
	{
		try
			{
                Log.debug("OnClicDemarrageArret");
				if ( etat == Constants.ETAT_ARRETE )
					DemarreParcours() ;
				else
					ArreteParcours() ;
			} catch (Exception e) {
				//Erreur(context, e.getLocalizedMessage()) ;
				e.printStackTrace();
			Log.erreur(e.getLocalizedMessage());
		}
	}


	///////////////////////////////////////////////////////////////////////////
	/***
	 * Arrete le service
	 */
	///////////////////////////////////////////////////////////////////////////
	private void ArreteParcours() {
        Log.debug("arreteparcours");
        etat = Constants.ETAT_ARRETE ;
        pauseEnCours = false ;
        ChangeStartButtonLook(false);
		btnPause.setEnabled(false) ;
		btnPlein.setEnabled(false) ;
		btnConfiguration.setEnabled(true) ;
        ChangePauseButtonLook(false) ;


		// Dire au service de s'arreter
		Intent intent = new Intent() ;
		intent.setAction(Constants.SERVICE_COMMAND) ;
		intent.putExtra(Constants.COMMAND, Constants.COMMAND_STOP);
		sendBroadcast(intent) ;			
}

	///////////////////////////////////////////////////////////////////////////
	/**
	 * Demarre le parcours
	 * Demarre prealablement le service si besoin
	 */
	///////////////////////////////////////////////////////////////////////////
	private void DemarreParcours() {
        Log.debug("DemarreParcours");
        etat = Constants.ETAT_ENROUTE ;
        pauseEnCours = false ;
        ChangeStartButtonLook(true);
        ChangePauseButtonLook(false);
		btnPause.setEnabled(true) ;
		btnPlein.setEnabled(true) ;
		btnConfiguration.setEnabled(false) ;

		Intent intent = new Intent(this, CompanionService.class ) ;
		startService(intent) ;
	}	

	///////////////////////////////////////////////////////////////////////////
	/***
	 * Reouverture de la fenetre
	 */
	@Override
	///////////////////////////////////////////////////////////////////////////
	protected void onResume() {
		try
		{
            Log.debug("onresume");
			super.onResume();
            ChangeStartButtonLook(false);
			GPSEnabled = false ;
			TTSEnabled = false ;

			CheckGPS() ;
			CheckTTS() ;
			CheckService() ;
		} catch (Exception e)
		{
			Log.erreur(e.getLocalizedMessage());
		}		
	}

	/***
	 * Envoi un message au service pour savoir dans quel etat il est
	 */
	private void CheckService() {
		if ( serviceStarted())
		{
			Intent intent = new Intent(  ) ;
			intent.setAction(Constants.SERVICE_COMMAND);
			intent.putExtra(Constants.COMMAND, Constants.COMMAND_GETSTATE ) ;
			sendBroadcast(intent);
		}
	}
	/***
	 * Verifier que le TTS est activé
	 */
	private void CheckTTS() {
		mTts = new TextToSpeech(this, this);
	}

	/**
	 * Verifier que le GPS est activé, proposer de l'activer sinon
	 */
	private void CheckGPS() {
		if (! isGPSEnabled())
			buildAlertMessageNoGps() ;
		else
		{
			GPSEnabled = true ;
			if ( TTSEnabled )
			{
				// GPS et TTS disponibles: on peut degriser les boutons
				btnDemmarrage.setEnabled(true) ;
				//btnRaz.setEnabled(true) ;
			}
		}
	}

	/***
	 * Retourne true si le GPS est activé
	 * @return
	 */
	private boolean isGPSEnabled()
	{
		final LocationManager manager = (LocationManager) getSystemService( Context.LOCATION_SERVICE );
		return manager.isProviderEnabled( LocationManager.GPS_PROVIDER );
	}

	private void buildAlertMessageNoGps() {
		final AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage("Votre GPS semble être désactivé. Il est indispensable au bon fonctionnement de cette application. Voulez-vous l'activer ?")
		.setCancelable(false)
		.setPositiveButton("Oui", new DialogInterface.OnClickListener() {
			public void onClick(@SuppressWarnings("unused") final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
				startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
			}
		})
		.setNegativeButton("Non", new DialogInterface.OnClickListener() {
			public void onClick(final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
				dialog.cancel();
			}
		});
		final AlertDialog alert = builder.create();
		alert.show();

	}

	private void buildAlertMessageNoTTS() {
		final Activity a = this ;
		final AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage("La fonction de synthèse vocale semble être désactivée. Elle est indispensable au bon fonctionnement de cette application. Voulez-vous l'activer ?")
		.setCancelable(false)
		.setPositiveButton("Oui", new DialogInterface.OnClickListener() {
			public void onClick(@SuppressWarnings("unused") final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
				Intent intent = new Intent();
				intent.setAction("com.android.settings.TTS_SETTINGS");
				intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				a.startActivity(intent);
			}
		})
		.setNegativeButton("Non", new DialogInterface.OnClickListener() {
			public void onClick(final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
				dialog.cancel();
			}
		});
		final AlertDialog alert = builder.create();
		alert.show();

	}
	private boolean serviceStarted() {
		ActivityManager manager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
		final String name = CompanionService.class.getName();
		for (RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
			if (name.equals(service.service.getClassName())) {

				return true;
			}
		}
		return false;
	}

	///////////////////////////////////////////////////////////////////////////
	/***
	 * Envoyer au service l'ordre de se remettre a zero
	 * @param v
	 */
	public void OnClickPause( View v)
	{
		try
		{
			pauseEnCours =! pauseEnCours;
			Intent intent = new Intent(  ) ;
			intent.setAction(Constants.SERVICE_COMMAND);
			intent.putExtra(Constants.COMMAND, Constants.COMMAND_PAUSE ) ;
			intent.putExtra(Constants.COMMAND_PAUSE_STATE, pauseEnCours ) ;
			sendBroadcast(intent);
		} catch (Exception e)
		{
			Log.erreur(e.getLocalizedMessage());
		}
	}

	///////////////////////////////////////////////////////////////////////////
	/***
	 * Plein de carburant refait
	 * @param v
	 */
	public void OnClickPlein( View v)
	{
		try
		{
			Intent intent = new Intent(  ) ;
			intent.setAction(Constants.SERVICE_COMMAND);
			intent.putExtra(Constants.COMMAND, Constants.COMMAND_PLEIN ) ;
			sendBroadcast(intent);
		} catch (Exception e)
		{
			Log.erreur(e.getLocalizedMessage());
		}
	}


    /***
     * Click sur le bouton des preferences
     * @param v
     */
    public void OnClickPreferences( View v)
    {
        try
        {
            Intent intent = new Intent(this, ConfigurationActivity.class);
            startActivity(intent);
        } catch (Exception e)
        {
            Log.erreur(e.getLocalizedMessage());
        }
    }

    /***
     * Bouton configuration de la synthese vocale
     * @param v
     */
    public void OnClickVocal(View v)
    {
        try
        {
            Intent intent = new Intent() ;
            intent.setAction("com.android.settings.TTS_SETTINGS");
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            getApplication().startActivity(intent);
        } catch (Exception e)
        {
            Log.erreur(e.getLocalizedMessage());
        }
    }

    /***
     * Bouton configuration audio
     * @param v
     */
    public void OnClickAudio(View v)
    {
        try
        {
            Intent intent = new Intent() ;
            intent.setAction(android.provider.Settings.ACTION_SOUND_SETTINGS) ;
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            getApplication().startActivity(intent);
        } catch (Exception e)
        {
            Log.erreur(e.getLocalizedMessage());
        }
    }

	@Override
	public void onInit(int status) {
		try
		{
			TTSEnabled = false ;
			if (status == TextToSpeech.SUCCESS) {
				if ( mTts.isLanguageAvailable(Locale.FRANCE) == TextToSpeech.LANG_COUNTRY_AVAILABLE)
					if ( mTts.setLanguage(Locale.FRENCH) != TextToSpeech.LANG_MISSING_DATA)
						TTSEnabled = true ;

				if (! TTSEnabled)
					buildAlertMessageNoTTS() ;
				else
					if ( GPSEnabled )
					{
						// TTS et GPS disponible: on peut degriser les boutons
						btnDemmarrage.setEnabled(true) ;
						//btnRaz.setEnabled(true) ;
					}
			}

			mTts.shutdown() ;
			mTts = null ;
		} catch (Exception e)
		{
			Log.erreur(e.getLocalizedMessage());
		}
	}
}
