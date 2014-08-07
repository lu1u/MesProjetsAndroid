/**
 * 
 */
package com.lpi.frequentlauncherwidget.widgetprovider;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.widget.RemoteViews;

import com.lpi.frequentlauncherwidget.R;
import com.lpi.frequentlauncherwidget.service.FrequentWidgetServiceStack;

/**
 * @author lucien
 *
 */
public class FrequentWidgetProviderStack extends FrequentWidgetProvider
{
	/**
	 * Installe les recepteurs de clic sur les boutons
	 * 
	 * @param rv
	 * @param context
	 * @param appWidgetId
	 */
	@Override
	protected void SetOnClickListeners(RemoteViews rv, Context context, int appWidgetId)
	{
		// liste des applications
		final Intent onClickIntent = new Intent(context, FrequentWidgetProviderStack.class);
		onClickIntent.setAction(FrequentWidgetProvider.CLICK_ACTION);
		onClickIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
		onClickIntent.setData(Uri.parse(onClickIntent.toUri(Intent.URI_INTENT_SCHEME)));
		final PendingIntent onClickPendingIntent = PendingIntent.getBroadcast(context, 0, onClickIntent,
				PendingIntent.FLAG_UPDATE_CURRENT);
		rv.setPendingIntentTemplate(R.id.application_list, onClickPendingIntent);

		// Bouton refresh
		{
			final Intent refreshIntent = new Intent(context, FrequentWidgetProviderStack.class);
			refreshIntent.setAction(FrequentWidgetProvider.REFRESH_ACTION);
			refreshIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
			final PendingIntent refreshPendingIntent = PendingIntent.getBroadcast(context, 0, refreshIntent, PendingIntent.FLAG_UPDATE_CURRENT);
			rv.setOnClickPendingIntent(R.id.refresh, refreshPendingIntent);
		}

		// Bouton configuration
		{
			final Intent configIntent = new Intent(context, FrequentWidgetProviderStack.class);
			configIntent.setAction(FrequentWidgetProvider.CONFIG_ACTION);
			configIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
			final PendingIntent configPendingIntent = PendingIntent.getBroadcast(context, 0, configIntent, PendingIntent.FLAG_UPDATE_CURRENT);
			rv.setOnClickPendingIntent(R.id.settings, configPendingIntent);
		}

		// Bouton tri
		{
			final Intent sortIntent = new Intent(context, FrequentWidgetProviderStack.class);
			sortIntent.setAction(FrequentWidgetProvider.SORT_ACTION);
			sortIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
			final PendingIntent configPendingIntent = PendingIntent.getBroadcast(context, 0, sortIntent, PendingIntent.FLAG_UPDATE_CURRENT);
			rv.setOnClickPendingIntent(R.id.sort, configPendingIntent);
		}

	}

	/* (non-Javadoc)
	 * @see com.lpi.frequentlauncherwidget.FrequentWidgetProvider#getLayout()
	 */
	@Override
	protected int getLayout()
	{
		return R.layout.widget_layout_stack ;
	}
	
	@Override
	protected  Class<?> getServiceClass()
	{
		return FrequentWidgetServiceStack.class ;
	}

}
