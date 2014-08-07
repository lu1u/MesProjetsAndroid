package com.lpi.frequentlauncherwidget.service;

import java.text.SimpleDateFormat;
import java.util.Date;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.util.Log;
import android.widget.RemoteViews;

import com.lpi.frequentlauncherwidget.R;
import com.lpi.frequentlauncherwidget.bdd.BDDOpenHelper;
import com.lpi.frequentlauncherwidget.widgetprovider.FrequentWidgetProvider;

public class FrequentRemoteViewsFactoryStack extends FrequentRemoteViewsFactory
{
	//public static final String TAG = "FrequentRemoteViewsFactoryStack" ;
	
	public FrequentRemoteViewsFactoryStack(Context context, Intent intent)
	{
		super(context, intent);
	}

	@Override
	public int getViewTypeCount()
	{
		return 1;
	}

	@Override
	public RemoteViews getViewAt(int position)
	{
		//Log.d(TAG, "getViewAt") ;
		// Get the data for this position from the content provider
		String application = "";
		Drawable icon = null;
		String componentname = null;
		int nbLancements = 0;
		long dernierLancement = 0;
		final int nbLancementColIndex = mCursor.getColumnIndex(BDDOpenHelper.COLONNE_NB_LANCEMENTS);
		final int dernierLancementColIndex = mCursor.getColumnIndex(BDDOpenHelper.COLONNE_DERNIER_LANCEMENT);

		if (mCursor.moveToPosition(position))
		{
			final PackageManager packagemanager = mContext.getPackageManager();
			final int cmpNameColIndex = mCursor.getColumnIndex(BDDOpenHelper.COLONNE_COMPONENT_NAME);
			componentname = mCursor.getString(cmpNameColIndex);
			nbLancements = mCursor.getInt(nbLancementColIndex);
			dernierLancement = mCursor.getLong(dernierLancementColIndex);

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

		final int layoutId = R.layout.dark_widget_item_stack;
		RemoteViews rv = new RemoteViews(mContext.getPackageName(), layoutId);

		// Set the click intent so that we can handle it
		final Intent fillInIntent = new Intent();
		final Bundle extras = new Bundle();
		extras.putString(FrequentWidgetProvider.EXTRA_COMPONENT_NAME, componentname);
		extras.putString(FrequentWidgetProvider.EXTRA_APPLICATION_NAME, application);
		fillInIntent.putExtras(extras);

		rv.setTextViewText(R.id.widget_item, application);
		rv.setTextViewText(R.id.nb_lancements, String.format( mContext.getResources().getString(R.string.nblancementformat), nbLancements));
		rv.setTextViewText(R.id.datedernierlancement, getTime(mContext, dernierLancement));
		if (icon != null)
			rv.setImageViewBitmap(R.id.imageView1, drawableToBitmap(icon));
		
		// Placer les listeners pour reagir en cas de clic
		rv.setOnClickFillInIntent(R.id.widget_item, fillInIntent);
		rv.setOnClickFillInIntent(R.id.nb_lancements, fillInIntent);
		rv.setOnClickFillInIntent(R.id.datedernierlancement, fillInIntent);
		rv.setOnClickFillInIntent(R.id.imageView1, fillInIntent);

		return rv;
	}

	/**
	 * Retourne une chaine de caracteres representant la date et l'heure
	 * @param ctx
	 * @param time
	 * @return
	 */
	public static String getTime(Context ctx, long time)
	{
		Date date = new Date(time) ;
		final String datePattern = ((SimpleDateFormat) DateFormat.getDateFormat(ctx)).toLocalizedPattern();
		final String dateSt = new SimpleDateFormat(datePattern).format(date) ;
		
		final String timePattern = ((SimpleDateFormat) DateFormat.getTimeFormat(ctx)) .toLocalizedPattern();
		final String timeSt = new SimpleDateFormat(timePattern).format(date) ;
		
		String format = ctx.getResources().getString(R.string.dateformat) ;
		return String.format(format,  dateSt, timeSt) ;
	}
}
