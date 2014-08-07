package com.lpi.frequentlauncherwidget.service;

import android.content.Intent;

public class FrequentWidgetServiceStack extends FrequentWidgetService
{
	@Override
	public RemoteViewsFactory onGetViewFactory(Intent intent)
	{
		return new FrequentRemoteViewsFactoryStack(this.getApplicationContext(), intent);
	}
	
	
}
