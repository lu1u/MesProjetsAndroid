/**
 * 
 */
package com.lpi.simpleclock;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.os.Bundle;
import android.widget.RemoteViews;
import android.widget.Toast;

/**
 * @author lucien
 * 
 */
public class SimpleClockWidgetProvider extends AppWidgetProvider
{
	public static String CLICK_ACTION = "com.lpi.simpleclock.CLICK"; //$NON-NLS-1$
	public static String REFRESH_ACTION = "com.lpi.simpleclock.REFRESH"; //$NON-NLS-1$
	public static final long MILLISECONDES_DANS_MINUTE = 60 * 1000;

	// public static final String TAG = "SimpleClockProvider";

	/***
	 * Envoyer un Intent pour rafraichir tous les widgets
	 * 
	 * @param context
	 */
	static void updateWidgets(Context context)
	{
		AppWidgetManager widgetManager = AppWidgetManager.getInstance(context);
		ComponentName widgetComponent = new ComponentName(context, SimpleClockWidgetProvider.class);
		int[] widgetIds = widgetManager.getAppWidgetIds(widgetComponent);

		Intent update = new Intent(context, SimpleClockWidgetProvider.class);
		update.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, widgetIds);
		update.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
		context.sendBroadcast(update);
	}


	/***
	 * Afficher l'Activity de configuration du widget
	 * 
	 * @param context
	 * @param intent
	 */
	private void displayConfiguration(Context context, Intent intent)
	{
		int WidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
		Intent myIntent = new Intent(context, Configuration.class);
		myIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		myIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, WidgetId);
		context.startActivity(myIntent);
	}

	/***
	 * Rapporte une erreur detectee par le programme
	 * 
	 * @param context
	 * @param e
	 */
	@SuppressWarnings("nls")
	public void displayError(Context context, Exception e)
	{
		String message = "";
		if (e == null)
		{
			message = "null exception ?!?"; //$NON-NLS-1$
		} else
		{
			StringBuffer stack = new StringBuffer();

			StackTraceElement[] st = e.getStackTrace();
			for (int i = 0; i < st.length; i++)
			{
				String line = st[i].getMethodName() + ":" + st[i].getLineNumber();
				// Log.e(TAG, line);
				stack.append(line);
				stack.append("\n");
			}
			message = "Erreur " + e.getLocalizedMessage() + "\n" + stack.toString();
		}
		// Log.e(TAG, message);
		Toast t = Toast.makeText(context, "Erreur\n" + message + "\n", Toast.LENGTH_LONG);
		t.show();
	}

	/***
	 * Gerer l'evenement "changement de date"
	 * 
	 * @param context
	 * @param intent
	 */
	private void handleDateChanged(Context context, Intent intent)
	{
		updateWidgets(context);
	}

	/***
	 * Gerer l'evenement "redimensionnement"
	 * 
	 * @param context
	 * @param intent
	 */
	private void handleResize(Context context, Intent intent)
	{
		AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
		ComponentName widgetComponent = new ComponentName(context, getClass());
		int[] widgetIds = appWidgetManager.getAppWidgetIds(widgetComponent);
		appWidgetManager.updateAppWidget(widgetIds, new RemoteViews(context.getPackageName(), R.layout.widgetlayout));
	}

	/*
	 * (non-Javadoc)
	 * @see android.appwidget.AppWidgetProvider#onAppWidgetOptionsChanged(android .content.Context,
	 * android.appwidget.AppWidgetManager, int, android.os.Bundle)
	 */
	@Override
	public void onAppWidgetOptionsChanged(Context context, AppWidgetManager appWidgetManager, int appWidgetId,
			Bundle newOptions)
	{
		updateWidgets(context);
		super.onAppWidgetOptionsChanged(context, appWidgetManager, appWidgetId, newOptions);
	}

	/***
	 * Reception des evenements
	 */
	@Override
	public void onReceive(Context context, Intent intent)
	{
		final String action = intent.getAction();
		// Log.d(TAG, "OnReceive " + action );
		if (action.equals(Intent.ACTION_DATE_CHANGED))
			handleDateChanged(context, intent);
		else if (action.equals(AppWidgetManager.ACTION_APPWIDGET_CONFIGURE))
			displayConfiguration(context, intent);
		// else if (action.contentEquals("com.motorola.blur.home.ACTION_SET_WIDGET_SIZE"))
		// HandleResize(context, intent);

		super.onReceive(context, intent);
	}

	/***
	 * Configurer une alarme pour le prochain changement de minute
	 * 
	 * @param context
	 * @param appWidgetIds
	 */
	private void configureNextAlarm(Context context, int[] appWidgetIds)
	{
		Calendar now = Calendar.getInstance();
		long ProchaineAlarme = (now.getTimeInMillis() + MILLISECONDES_DANS_MINUTE + 1);
		ProchaineAlarme -= ProchaineAlarme % MILLISECONDES_DANS_MINUTE;

		Intent update = new Intent(context, SimpleClockWidgetProvider.class);
		update.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, appWidgetIds);
		update.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);

		PendingIntent pendingIntentAlarme = PendingIntent.getBroadcast(context, 0, update, 0);
		AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
		alarmManager.set(AlarmManager.RTC, ProchaineAlarme, pendingIntentAlarme);
	}

	/***
	 * Appelé pour mettre les widgets a jour
	 */
	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds)
	{
		// Log.d(TAG, "onUpdate");

		try
		{
			for (int i = 0; i < appWidgetIds.length; ++i)
			{
				final int appWidgetId = appWidgetIds[i];
				Bundle options = appWidgetManager.getAppWidgetOptions(appWidgetId);
				final int minWidth = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH);
				final int minHeight = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT);
				final RemoteViews rv = new RemoteViews(context.getPackageName(), R.layout.widgetlayout);
				registerOnClickListener(context, rv, appWidgetIds, appWidgetId);

				if (minWidth > 0 && minHeight > 0)
				{
					rv.setImageViewBitmap(R.id.imageView1, createWidgetBitmep(context, minWidth, minHeight));
				}

				appWidgetManager.updateAppWidget(appWidgetId, rv);

			}
			configureNextAlarm(context, appWidgetIds);
			super.onUpdate(context, appWidgetManager, appWidgetIds);
		} catch (Exception e)
		{
			displayError(context, e);
		}
	}

	/***
	 * Cree l'image a afficher
	 * 
	 * @param context
	 * @param Largeur
	 * @param Hauteur
	 * @return
	 */
	private Bitmap createWidgetBitmep(Context context, int Largeur, int Hauteur)
	{
		Bitmap bm = Bitmap.createBitmap(Largeur, Hauteur, Bitmap.Config.ARGB_8888);
		Canvas canvas = new Canvas(bm);

		if (Largeur > Hauteur)
		{
			drawBitmap(context, canvas, Largeur, Hauteur);
		} else
		{
			// Si la largeur < hauteur: affichage vertical
			int C = Math.min(Largeur, Hauteur) / 2;
			Matrix m = new Matrix();
			m.setRotate(90, C, C);
			canvas.setMatrix(m);
			drawBitmap(context, canvas, Hauteur, Largeur);
		}

		return bm;
	}

	/**
	 * Dessine l'image du widget: heure+date
	 * 
	 * @param context
	 * @param canvas
	 * @param largeur
	 * @param hauteur
	 */
	private void drawBitmap(Context context, Canvas canvas, int largeur, int hauteur)
	{
		Preferences prefs = new Preferences(context);
		canvas.drawColor(prefs.get_couleurFond());
		Paint paint = new Paint();

		if (prefs.is_Ombre())
			paint.setShadowLayer(1, 1, 1, Color.BLACK);

		Typeface tf;
		try
		{
			tf = Typeface.createFromFile(prefs.get_typeFace());
		} catch (Exception e)
		{
			tf = null;
		}
		if (tf != null)
			paint.setTypeface(tf);
		paint.setTextAlign(Align.RIGHT);
		Date maintenant = new Date();

		// Heure
		String texte = DateFormat.getTimeInstance(DateFormat.SHORT).format(maintenant);
		// Log.d(TAG, "heure " + texte ) ;
		final int tailleTexte = getTextSize(largeur, hauteur, texte, paint);

		Rect rBounds = new Rect();
		paint.getTextBounds(texte, 0, texte.length(), rBounds);

		float X = largeur;
		float Y = hauteur - rBounds.bottom - 2;
		paint.setColor(prefs.get_couleurTexte());
		canvas.drawText(texte, X, Y, paint);

		// Date
		texte = DateFormat.getDateInstance(DateFormat.SHORT).format(maintenant);
		paint.setTextSize(tailleTexte / 3);
		paint.setTextAlign(Align.LEFT);
		X = 0;
		Y = -(paint.ascent());

		canvas.drawText(texte, X, Y, paint);
	}

	/***
	 * Calcule une taille de texte pour que celui ci tienne dans le rectangle donne
	 * 
	 * @param r
	 * @param s
	 * @return
	 */
	public static int getTextSize(int Largeur, int Hauteur, String s, Paint paint)
	{
		int texteSize = Hauteur;
		paint.setTextSize(texteSize);

		final int longueur = s.length();
		Rect rBounds = new Rect();
		paint.getTextBounds(s, 0, longueur, rBounds);

		int LargeurTexte = rBounds.width();
		int HauteurTexte = rBounds.height();
		boolean tropGrand = (LargeurTexte >= Largeur) || (HauteurTexte >= Hauteur);

		// Diminuer progressivement la taille jusqu'a ce que le texte tienne
		while ((texteSize > 3) && tropGrand)
		{
			texteSize--;
			paint.setTextSize(texteSize);
			paint.getTextBounds(s, 0, longueur, rBounds);

			tropGrand = (rBounds.width() >= Largeur) || (rBounds.height() >= Hauteur);
		}

		return texteSize;
	}

	/***
	 * Enregistre le Listener qui reagira si on clique sur le widget
	 * @param context
	 * @param rv
	 * @param appWidgetIds
	 * @param appWidgetId
	 */
	protected void registerOnClickListener(Context context, RemoteViews rv, int[] appWidgetIds, int appWidgetId)
	{
		Intent intent = new Intent(context, getClass());
		intent.setAction(AppWidgetManager.ACTION_APPWIDGET_CONFIGURE);
		// intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, appWidgetIds);
		intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);

		PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
		rv.setOnClickPendingIntent(R.id.imageView1, pendingIntent);
	}

}
