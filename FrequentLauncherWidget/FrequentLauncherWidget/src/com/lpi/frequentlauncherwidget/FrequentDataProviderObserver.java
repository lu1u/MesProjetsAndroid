/**
 * 
 */
package com.lpi.frequentlauncherwidget;

import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.database.ContentObserver;
import android.os.Handler;
import android.util.Log;

/**
 * @author lucien
 *
 */
/**
 * Our data observer just notifies an update for all weather widgets when it detects a change.
 */
public class FrequentDataProviderObserver extends ContentObserver
{
	private AppWidgetManager mAppWidgetManager;
	private ComponentName mComponentName;
	public final static String TAG = "FrequentDataProviderObserver" ; //$NON-NLS-1$
	public FrequentDataProviderObserver(AppWidgetManager mgr, ComponentName cn, Handler h)
	{
		super(h);
		mAppWidgetManager = mgr;
		mComponentName = cn;
	}

	@Override
	public void onChange(boolean selfChange)
	{
		// The data has changed, so notify the widget that the collection view needs to be updated.
		// In response, the factory's onDataSetChanged() will be called which will requery the
		// cursor for the new data.
		Log.d(TAG, "*****Observer onchange" ); //$NON-NLS-1$
		mAppWidgetManager.notifyAppWidgetViewDataChanged(mAppWidgetManager.getAppWidgetIds(mComponentName),
				R.id.application_list);
	}
}
