package com.lpi.compagnondumotard.phone;

import android.content.Context;
import android.database.ContentObserver;
import android.os.Handler;

import com.lpi.compagnondumotard.service.CompanionService;

public class SMSObserver extends ContentObserver {
	Context context ;
	CompanionService service ;

	public SMSObserver( Context c, CompanionService s) {
		super(new Handler());
		context = c ;
		service = s ;
	}



	/***
	 * Envoi ou reception d'un SMS
	 */
	@Override
	public void onChange(boolean selfChange) {
		if (service != null)
			service.onSMS() ;
	}



	@Override
	public boolean deliverSelfNotifications() {
		return false;
	}
}