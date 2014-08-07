package com.lpi.frequentlauncherwidget.service;

import android.content.Intent;

public class FrequentWidgetServiceList extends FrequentWidgetService
{
	@Override
	public RemoteViewsFactory onGetViewFactory(Intent intent)
	{
		return new FrequentRemoteViewsFactoryListe(this.getApplicationContext(), intent);
	}
}
