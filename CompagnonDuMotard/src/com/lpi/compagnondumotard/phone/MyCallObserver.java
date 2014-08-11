package com.lpi.compagnondumotard.phone;


import android.telephony.PhoneStateListener;

import com.lpi.compagnondumotard.service.CompanionService;

public class MyCallObserver extends PhoneStateListener {
	CompanionService service ;

	public MyCallObserver(CompanionService c)
	{
		service = c ;
	}
	public void onCallStateChanged(int state,String incomingNumber){
		service.setPhoneState( state, incomingNumber ) ;
	}
}