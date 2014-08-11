package com.lpi.compagnondumotard.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.BatteryManager;

import com.lpi.compagnondumotard.service.CompanionService;

public class BatteryChangeReceiver extends BroadcastReceiver {
	CompanionService service ;
	public BatteryChangeReceiver( CompanionService s )
	{
		super();
		service = s ;
	}

    /***
     * Reception d'un changement d'etat de la batterie
     * @param context
     * @param intent
     */
	@Override
	public void onReceive(Context context, Intent intent)
	{
		//Log.debug( "BatteryChangeReceiver:" +  intent.getAction()) ;
		String action = intent.getAction();
		if (action.equals(Intent.ACTION_BATTERY_CHANGED))
		{
			int rawlevel = intent.getIntExtra("level", -1);
			int scale = intent.getIntExtra("scale", -1);
			int level = -1;
			if (rawlevel >= 0 && scale > 0) {
				level = (rawlevel * 100) / scale;
			}
			
			int status = intent.getIntExtra("status", BatteryManager.BATTERY_STATUS_UNKNOWN);
			service.batteryChange( level, status ) ;
		}
	}


}