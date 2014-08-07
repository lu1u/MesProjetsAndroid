/*
 * Copyright (C) 2011 The Android Open Source Project Licensed under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with the License. You may obtain
 * a copy of the License at http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable
 * law or agreed to in writing, software distributed under the License is distributed on an "AS IS"
 * BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 * for the specific language governing permissions and limitations under the License.
 */

package com.lpi.frequentlauncherwidget.widgetprovider;

import java.util.Calendar;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.Toast;

import com.lpi.frequentlauncherwidget.FrequentDataProvider;
import com.lpi.frequentlauncherwidget.FrequentDataProviderObserver;
import com.lpi.frequentlauncherwidget.R;
import com.lpi.frequentlauncherwidget.bdd.MySQLiteDatabase;
import com.lpi.frequentlauncherwidget.configuration.Configuration;

public abstract class FrequentWidgetProvider extends AppWidgetProvider
{
	public static final String CLICK_ACTION = "com.lpi.frequentlauncher.CLICK"; //$NON-NLS-1$
	public static final String REFRESH_ACTION = "com.lpi.frequentlauncher.REFRESH"; //$NON-NLS-1$
	public static final String CONFIG_ACTION = "com.lpi.frequentlauncher.CONFIG"; //$NON-NLS-1$
	public static final String SORT_ACTION = "com.lpi.frequentlauncher.SORT"; //$NON-NLS-1$
	public static final String EXTRA_WIDGETID = "com.lpi.frequentlauncher.widgetid"; //$NON-NLS-1$
	public static final String EXTRA_COMPONENT_NAME = "com.lpi.frequentlauncher.componentname"; //$NON-NLS-1$
	public static final String EXTRA_APPLICATION_NAME = "com.lpi.frequentlauncher.applicationname"; //$NON-NLS-1$
	public static final String TAG = "FrequentWidgetProvider"; //$NON-NLS-1$

	private static HandlerThread sWorkerThread;
	private static Handler sWorkerQueue;
	private static FrequentDataProviderObserver sDataObserver;

	public FrequentWidgetProvider()
	{
		// Start the worker thread
		sWorkerThread = new HandlerThread("FrequentWidgetProvider-worker"); //$NON-NLS-1$
		sWorkerThread.start();
		sWorkerQueue = new Handler(sWorkerThread.getLooper());
	}

	/***
	 * Register for external updates to the data to trigger an update of the widget. When using
	 * content providers, the data is often updated via a background service, or in response to user
	 * interaction in the main app. To ensure that the widget always reflects the current state of
	 * the data, we must listen for changes and update ourselves accordingly.
	 */
	@Override
	public void onEnabled(Context context)
	{
		try
		{
			// Log.d(TAG, "OnEnabled") ;
			final ContentResolver r = context.getContentResolver();
			if (sDataObserver == null)
			{
				final AppWidgetManager mgr = AppWidgetManager.getInstance(context);
				final ComponentName cn = new ComponentName(context, FrequentWidgetProvider.class);
				sDataObserver = new FrequentDataProviderObserver(mgr, cn, sWorkerQueue);
				// Log.d(TAG, "OnEnabled: register " + FrequentDataProvider.CONTENT_URI) ;
				r.registerContentObserver(FrequentDataProvider.CONTENT_URI, true, sDataObserver);
			}
		} catch (Exception e)
		{
			Erreur(context, e);

			e.printStackTrace();
		}
	}

	@Override
	public void onDeleted(Context context, int[] appWidgetIds)
	{
		try
		{
			// Log.d(TAG, "OnDeleted") ;
			super.onDeleted(context, appWidgetIds);
		} catch (Exception e)
		{
			Erreur(context, e);

			e.printStackTrace();
		}
	}

	@Override
	public void onReceive(Context ctx, Intent intent)
	{
		final String action = intent.getAction();
		final int widgetId = intent.getIntExtra(EXTRA_WIDGETID, -1);
		Log.d(TAG, "OnReceive " + action + ", widgetid: " + widgetId);
		try
		{
			if (action.equals(REFRESH_ACTION))
				HandleRefresh(ctx, intent, widgetId);
			else if (action.equals(CONFIG_ACTION))
				HandleConfig(ctx, intent, widgetId);
			else if (action.equals(CLICK_ACTION))
				HandleClickAction(ctx, intent, widgetId);
			else if (action.equals(SORT_ACTION))
				HandleSortAction(ctx, intent, widgetId);
			super.onReceive(ctx, intent);
		} catch (Exception e)
		{
			Erreur(ctx, e);

			e.printStackTrace();
		}
	}

	/**
	 * Changer l'option SORT
	 * 
	 * @param ctx
	 * @param intent
	 */
	private void HandleSortAction(Context ctx, Intent intent, int widgetId)
	{
		// Changer l'option
		MySQLiteDatabase bd = new MySQLiteDatabase(ctx);
		int Tri = bd.ChangeSortOption(widgetId);

		Toast t = Toast.makeText(
				ctx,
				ctx.getResources().getString(
						Tri == MySQLiteDatabase.TRI_DATELANCEMENT ? R.string.sortdate : R.string.sortfrequency),
				Toast.LENGTH_SHORT);
		t.show();

		// Raffraichir
		HandleRefresh(ctx, intent, widgetId);
	}

	/***
	 * Click sur un element: lancer l'application correspondante
	 * 
	 * @param ctx
	 * @param intent
	 */
	private void HandleClickAction(Context ctx, Intent intent, int widgetId)
	{
		final String componentname = intent.getStringExtra(EXTRA_COMPONENT_NAME);
		final String applicationtname = intent.getStringExtra(EXTRA_APPLICATION_NAME);

		try
		{
			String format = ctx.getResources().getString(R.string.launching);
			String message = String.format(format, applicationtname);
			Toast t = Toast.makeText(ctx, message, Toast.LENGTH_SHORT);
			t.show();

			Intent i = new Intent();
			PackageManager manager = ctx.getPackageManager();
			i = manager.getLaunchIntentForPackage(componentname);
			i.addCategory(Intent.CATEGORY_LAUNCHER);
			ctx.startActivity(i);

			// Incremente le compteur de lancement de cette application
			MySQLiteDatabase bd = new MySQLiteDatabase(ctx);
			int time = (int) (Calendar.getInstance().getTimeInMillis() / 1000L);
			int id = 1;
			bd.IncrementeCompteur(componentname, time, id);
			HandleRefresh(ctx, intent, widgetId);

		} catch (Exception e)
		{
			Erreur(ctx, e);
		}
	}

	/**
	 * Bouton refresh: rafraichir la liste
	 * 
	 * @param ctx
	 * @param intent
	 */
	private void HandleRefresh(Context ctx, Intent intent, int widgetId)
	{
		final Context context = ctx;
		final ContentResolver r = context.getContentResolver();
		final Cursor c = r.query(FrequentDataProvider.CONTENT_URI, null, null, null, null);

		// Forcer la mise a jour du DataProvider
		final Uri uri = ContentUris.withAppendedId(FrequentDataProvider.CONTENT_URI, 0);
		ContentValues values = new ContentValues();
		r.update(uri, values, null, null);

		// Avertir les widgets
		final AppWidgetManager mgr = AppWidgetManager.getInstance(context);
		ComponentName cn = new ComponentName(context, FrequentWidgetProviderList.class);
		mgr.notifyAppWidgetViewDataChanged(mgr.getAppWidgetIds(cn), R.id.application_list);
		cn = new ComponentName(context, FrequentWidgetProviderStack.class);
		mgr.notifyAppWidgetViewDataChanged(mgr.getAppWidgetIds(cn), R.id.application_list);
	}

	/**
	 * Bouton Config: afficher la fenetre de configuration
	 * 
	 * @param ctx
	 * @param intent
	 */
	private void HandleConfig(Context ctx, Intent intent, int widgetId)
	{
		int WidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
		Intent myIntent = new Intent(ctx, Configuration.class);
		myIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		myIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, WidgetId);
		ctx.startActivity(myIntent);
	}

	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds)
	{
		try
		{
			// Update each of the widgets with the remote adapter
			for (int i = 0; i < appWidgetIds.length; ++i)
			{
				final RemoteViews rv = new RemoteViews(context.getPackageName(), getLayout());
				SetDataProviderService(rv, context, appWidgetIds[i]);
				rv.setEmptyView(R.id.application_list, R.id.empty_view);

				SetOnClickListeners(rv, context, appWidgetIds[i]);
				if (sDataObserver != null)
					sDataObserver.dispatchChange(true, FrequentDataProvider.CONTENT_URI);
				appWidgetManager.updateAppWidget(appWidgetIds[i], rv);
			}
			super.onUpdate(context, appWidgetManager, appWidgetIds);
		} catch (Exception e)
		{
			Erreur(context, e);
			e.printStackTrace();
		}
	}

	protected abstract int getLayout();

	/**
	 * Installe les recepteurs de clic sur les boutons
	 * 
	 * @param rv
	 * @param context
	 * @param appWidgetId
	 */
	protected abstract void SetOnClickListeners(RemoteViews rv, Context context, int appWidgetId);

	// Specify the service to provide data for the collection widget. Note that we need to
	// embed the appWidgetId via the data otherwise it will be ignored.
	// Set the empty view to be displayed if the collection is empty. It must be a sibling
	// view of the collection view.
	private void SetDataProviderService(RemoteViews rv, Context context, int appWidgetId)
	{
		final Intent intent = new Intent(context, getServiceClass() );
		intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
		intent.setData(Uri.parse(intent.toUri(Intent.URI_INTENT_SCHEME)));
		rv.setRemoteAdapter(R.id.application_list, intent);
	}

	protected abstract Class<?> getServiceClass() ;
	/***
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
				String line = st[i].getMethodName() + ":" + st[i].getLineNumber();
				stack.append(line);
				stack.append("\n");
			}
			message = "Erreur " + e.getLocalizedMessage() + "\n" + stack.toString();
		}
		Log.e(TAG, message);

		Toast t = Toast.makeText(context, "Erreur\n" + message + "\n", Toast.LENGTH_LONG);
		t.show();

	}

	/***
	 * Envoyer un Intent forcant le rafraichissement
	 * 
	 * @param context
	 */
	public static void SendRefresh(Context context)
	{
		Intent i = new Intent(REFRESH_ACTION);
		i.setAction(REFRESH_ACTION);
		context.sendBroadcast(i);
	}

}