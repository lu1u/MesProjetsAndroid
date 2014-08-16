/**
 * 
 */
package com.lpi.compagnondumotard.tts;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

import com.lpi.compagnondumotard.Constants;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;
import android.speech.tts.TextToSpeech.OnUtteranceCompletedListener;
import android.widget.Toast;

/**
 * @author lucien
 *
 */
public class TTSManager implements OnInitListener, OnUtteranceCompletedListener{
	private TextToSpeech _Tts;
	private boolean _ttsInitialise = false ;
	private ArrayList<String> _pendingMessages ;
	private Service _service;
	private boolean _shutdownAsked = false ;
	private boolean _telephoneDecroche = false ;
	private TTSListener _ttsListener ;
	HashMap<String, String> params = new HashMap<String, String>();

	

	public boolean is_telephoneDecroche() {
		return _telephoneDecroche;
	}

	public void set_telephoneDecroche(boolean _telephoneDecroche) {
		this._telephoneDecroche = _telephoneDecroche;
		//if ( ! _telephoneDecroche )
		//	flushAnnonces() ;
	}

	
	public TTSManager(Service s, TTSListener l )
	{
		params.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID,"stringId");
		_ttsListener = l ;
		_Tts = new TextToSpeech(s, this);
		_ttsInitialise = false ;
		_service = s ;
		_pendingMessages = new ArrayList<String>() ;
	}

	/***
	 * Resultat de l'initialisation de TTS
	 */
	@Override
	public void onInit(int status) {
		if (status == TextToSpeech.SUCCESS) {
			if ( _Tts.isLanguageAvailable(Locale.FRANCE) == TextToSpeech.LANG_COUNTRY_AVAILABLE)
			{
				if ( _Tts.setLanguage(Locale.FRENCH) == TextToSpeech.LANG_MISSING_DATA)
					Erreur( "Langue française non supportée") ;
				else
				{
					_ttsInitialise = true ;
					// Solder les messages en attente
					for (String m : _pendingMessages ) 
						_Tts.speak(m, TextToSpeech.QUEUE_ADD, params) ;

					_pendingMessages.clear() ;
					
					_Tts.setOnUtteranceCompletedListener(this) ;
				}
			}

		} else
			Erreur("Initialisation TTS échouée, status = " + status ) ;

	}

	private void AnnonceVocale(Context ctx, String message ) {
		try
		{
			if ( _telephoneDecroche )
				// Mettre le message en attente si le telephone est decroche
				_pendingMessages.add(message) ;
			else
				if ( _ttsInitialise )
					_Tts.speak(message, TextToSpeech.QUEUE_ADD, params) ;
					else
					_pendingMessages.add( message ) ;
			
			Intent broadcastIntent = new Intent(); 
			broadcastIntent.setAction(Constants.MESSAGE);
			broadcastIntent.putExtra(Constants.MESSAGE, message);
			ctx.sendBroadcast(broadcastIntent);
		}
		catch( Exception e )
		{
			e.printStackTrace() ;
			//Log.e(Constants.TAG, e.getLocalizedMessage());
		}
	}
	

	public void AnnonceVocale(Context ctx, int resId, Object ...args )
	{
		String format = ctx.getResources().getString(resId) ;
		AnnonceVocale( ctx, String.format(format, args));
	}

	private void Erreur(String text) {
		Toast t = Toast.makeText(_service, text, Toast.LENGTH_LONG) ;
		t.show() ;
	}

	public void shutdown()
	{
		if ( _Tts != null)
		{
			if (_Tts.isSpeaking())
				_shutdownAsked = true ;
			else
				_Tts.shutdown() ;
		}
		_ttsInitialise = false ;
	}

	@Override
	public void onUtteranceCompleted(String arg0) {
		if (_shutdownAsked)
			_Tts.shutdown() ;
		
		if ( _ttsListener != null )
			_ttsListener.onUtteranceCompleted() ;
	}

	/***
	 * Dire toutes les annonces qui avaient ete mises en attente
	 */
	public void flushAnnonces() {
		// Solder les messages en attente
		for (String m : _pendingMessages ) 
			_Tts.speak(m, TextToSpeech.QUEUE_ADD, params) ;

		_pendingMessages.clear() ;
	}

}
