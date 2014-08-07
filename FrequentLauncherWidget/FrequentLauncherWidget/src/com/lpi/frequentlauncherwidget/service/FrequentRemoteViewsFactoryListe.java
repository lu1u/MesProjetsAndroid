/**
 * 
 */
package com.lpi.frequentlauncherwidget.service;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.widget.RemoteViews;

import com.lpi.frequentlauncherwidget.R;
import com.lpi.frequentlauncherwidget.bdd.BDDOpenHelper;
import com.lpi.frequentlauncherwidget.widgetprovider.FrequentWidgetProvider;

/**
 * @author lucien
 *
 */
public class FrequentRemoteViewsFactoryListe extends FrequentRemoteViewsFactory
{
	//public static final String TAG = "FrequentRemoteViewsFactoryListe" ;
	public FrequentRemoteViewsFactoryListe(Context context, Intent intent)
	{
		super(context, intent);
	}

	@Override
	public int getViewTypeCount()
	{
		//Log.d(TAG, "getViewTypeCount") ;
		return 2;
	}

	@Override
	public RemoteViews getViewAt(int position)
	{
		//Log.d(TAG, "getViewAt") ;
		// Get the data for this position from the content provider
		String application = ""; //$NON-NLS-1$
		Drawable icon = null;
		String componentname = null;
		
		if (mCursor.moveToPosition(position))
		{
			final PackageManager packagemanager = mContext.getPackageManager();
			final int cmpNameColIndex = mCursor.getColumnIndex(BDDOpenHelper.COLONNE_COMPONENT_NAME);
			componentname = mCursor.getString(cmpNameColIndex);
			
			try
			{
				ApplicationInfo app = packagemanager.getApplicationInfo(componentname, 0);
				application = packagemanager.getApplicationLabel(app).toString();
				icon = packagemanager.getApplicationIcon(componentname);
			} catch (final Exception e)
			{
				application = componentname;
			}
		}

		final int layoutId = (position % 2 == 0 ? R.layout.light_widget_item_list : R.layout.dark_widget_item_list);
		RemoteViews rv = new RemoteViews(mContext.getPackageName(), layoutId);

		// Set the click intent so that we can handle it
		final Intent fillInIntent = new Intent();
		final Bundle extras = new Bundle();
		extras.putString(FrequentWidgetProvider.EXTRA_COMPONENT_NAME, componentname);
		extras.putString(FrequentWidgetProvider.EXTRA_APPLICATION_NAME, application);
		fillInIntent.putExtras(extras);

		rv.setTextViewText(R.id.widget_item, application); 
		if (icon != null)
			rv.setImageViewBitmap(R.id.imageView1, drawableToBitmap(icon));
		rv.setOnClickFillInIntent(R.id.widget_item, fillInIntent);
		rv.setOnClickFillInIntent(R.id.imageView1, fillInIntent);
		
		return rv;
	}
}
