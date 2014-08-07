/*
 * Copyright (C) 2011 The Android Open Source Project Licensed under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with the License. You may obtain
 * a copy of the License at http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable
 * law or agreed to in writing, software distributed under the License is distributed on an "AS IS"
 * BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 * for the specific language governing permissions and limitations under the License.
 */

package com.lpi.frequentlauncherwidget;

import java.util.List;

import android.app.ActivityManager;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

import com.lpi.frequentlauncherwidget.bdd.BDDOpenHelper;
import com.lpi.frequentlauncherwidget.bdd.MySQLiteDatabase;
import com.lpi.frequentlauncherwidget.service.FrequentRemoteViewsFactoryStack;
import com.lpi.frequentlauncherwidget.widgetprovider.FrequentWidgetProvider;

/**
 * The AppWidgetProvider for our sample weather widget.
 */
public class FrequentDataProvider extends ContentProvider
{
	@SuppressWarnings("nls")
	public static final String TAG = "FrequentDataProvider";
	@SuppressWarnings("nls")
	public static final Uri CONTENT_URI = Uri.parse("content://com.lpi.frequentlauncherwidget.provider");
	protected MySQLiteDatabase _BDD;
	private boolean _run;
	private String _lastComponentName;

	/**
	 * Generally, this data will be stored in an external and persistent location (ie. File,
	 * Database, SharedPreferences) so that the data can persist if the process is ever killed. For
	 * simplicity, in this sample the data will only be stored in memory.
	 */

	@Override
	public boolean onCreate()
	{
		RefreshDatabase();
		if (_BDD == null)
			_BDD = new MySQLiteDatabase(getContext());
		final int Delai = _BDD.GetDelaiScrutation();
		Log.d(TAG, "Delai de scrutation " + Delai);
		_run = true;
		new Thread(new Runnable() {
			@Override
			public void run()
			{
				while (_run)
				{
					// Log.d(TAG, "Boucle du dataprovider") ;
					try
					{
						Thread.sleep(1000 * Delai); // Verifier toutes les 10 secondes
						RefreshDatabase();
					} catch (InterruptedException e)
					{
						_run = false;
					}
				}
			}
		}).start();
		return true;
	}

	/***
	 * Scruter les applications recentes et les mettre dans la base
	 */
	public synchronized void RefreshDatabase()
	{
		ActivityManager activityManager = (ActivityManager) getContext().getSystemService(Context.ACTIVITY_SERVICE);

		List<ActivityManager.RunningTaskInfo> taskInfo = activityManager.getRunningTasks(1);
		if (taskInfo != null)
		{
			String cm = taskInfo.get(0).topActivity.getPackageName();

			// Nouvelle applications ?
			if (!cm.equals(_lastComponentName))
			{
				if (_BDD == null)
					_BDD = new MySQLiteDatabase(getContext());
				long time = System.currentTimeMillis() ;
				Log.d(TAG, "Nlle app:" + cm + ", lancement " + FrequentRemoteViewsFactoryStack.getTime(getContext(), time));
				Log.d(TAG, "time:" + time);
				_BDD.IncrementeCompteur(cm, time, 1);
				getContext().getContentResolver().notifyChange(CONTENT_URI, null);
				FrequentWidgetProvider.SendRefresh(getContext());
				_lastComponentName = cm;
			}
		}
	}

	@Override
	public void shutdown()
	{
		_run = false;
		if (_BDD != null)
			_BDD.close();
		super.shutdown();
	}

	private long getId(Uri uri)
	{
		String lastPathSegment = uri.getLastPathSegment();
		if (lastPathSegment != null)
		{
			try
			{
				return Long.parseLong(lastPathSegment);
			} catch (NumberFormatException e)
			{
				Log.e(TAG, e.getLocalizedMessage());
			}
		}
		return -1;
	}

	@Override
	@SuppressWarnings("nls")
	public synchronized Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
			String sortOrder)
	{
		Log.d(TAG, "DataProvider Query ");
		long id = getId(uri);

		if (id < 0)
			return _BDD.getFrequentApps(-1);
		else
			return _BDD._bdd.query(BDDOpenHelper.TABLE_LANCEMENTS, projection, BDDOpenHelper.COLONNE_ID + "=" + id,
					null, null, null, null);
	}

	@Override
	@SuppressWarnings("nls")
	public String getType(Uri uri)
	{
		return "vnd.android.cursor.dir/vnd.frequentlauncherwidget.componentname";
	}

	@Override
	public Uri insert(Uri uri, ContentValues values)
	{
		return null;
	}

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs)
	{
		return 0;
	}

	@Override
	public synchronized int update(Uri uri, ContentValues values, String selection, String[] selectionArgs)
	{
		Log.d(TAG, "DataProvider update ");
		assert (uri.getPathSegments().size() == 1);
		getContext().getContentResolver().notifyChange(uri, null);
		return 1;
	}

}