package com.lpi.frequentlauncherwidget.service;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.lpi.frequentlauncherwidget.bdd.MySQLiteDatabase;


/**
 * This is the factory that will provide data to the collection widget.
 */
abstract class  FrequentRemoteViewsFactory implements RemoteViewsService.RemoteViewsFactory
{
	protected Context mContext;
	protected Cursor mCursor;
	protected int mAppWidgetId;
	protected MySQLiteDatabase _bdd;

	//public static final String TAG = "StackRemoteViewsFactory";

	public FrequentRemoteViewsFactory(Context context, Intent intent)
	{
		_bdd = new MySQLiteDatabase(context);
		mContext = context;
		mAppWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
	}

	@Override
	public void onCreate()
	{
		// Since we reload the cursor in onDataSetChanged() which gets called immediately after
		// onCreate(), we do nothing here.
	}

	@Override
	public void onDestroy()
	{
		if (mCursor != null)
		{
			mCursor.close();
		}
	}

	@Override
	public int getCount()
	{
		//Log.d(TAG, "getCount " + mCursor.getCount());
		return mCursor.getCount();
	}

	

	/***
	 * Convert a Drawable object to a Bitmap object
	 * @param drawable
	 * @return
	 */
	public Bitmap drawableToBitmap(Drawable drawable)
	{
		if (drawable instanceof BitmapDrawable)
		{
			return ((BitmapDrawable) drawable).getBitmap();
		}

		int width = drawable.getIntrinsicWidth();
		width = width > 0 ? width : 10 ;
		int height = drawable.getIntrinsicHeight();
		height = height > 0 ? height : 10 ;

		Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
		Canvas canvas = new Canvas(bitmap);
		drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
		drawable.draw(canvas);

		return bitmap;
	}

	@Override
	public RemoteViews getLoadingView()
	{
		// We aren't going to return a default loading view in this sample
		return null;
	}

	
	@Override
	public long getItemId(int position)
	{
		return position;
	}

	@Override
	public boolean hasStableIds()
	{
		return true;
	}

	@Override
	public void onDataSetChanged()
	{
		// Refresh the cursor
		if (mCursor != null)
		{
			mCursor.close();
		}
		mCursor = _bdd.getFrequentApps(mAppWidgetId);
	}
}
