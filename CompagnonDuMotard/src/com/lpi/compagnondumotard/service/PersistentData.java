package com.lpi.compagnondumotard.service;

import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationManager;
import android.preference.PreferenceManager;
import android.util.Log;

/**
 * Created by lucien on 10/08/2014.
 */
@SuppressWarnings("nls")
public class PersistentData {
    public static final String HEURE_ALARME = "heureAlarme"; 
    static final int NB_POSITIONS_VITESSE = 3;
    final static String CACHE_NAME = "Lpi.BikersCompanion.cache"; 
    final static String DATA_NBPOSITIONS = "NbPositions"; 
    final static String DATA_ACCURACY = "Accuracy"; 
    final static String DATA_ALTITUDE = "Altitude"; 
    final static String DATA_BEARING = "Bearing"; 
    final static String DATA_LATITUDE = "Latitude"; 
    final static String DATA_LONGITUDE = "Longitude"; 
    final static String DATA_PROVIDER = "Provider"; 
    final static String DATA_SPEED = "Speed"; 
    final static String DATA_TIME = "Time"; 
    
    
    final static String DATA_DERNIERE_ALERTE_VITESSE = "DerniereAlerteVitesse"; 
    final static String DATA_DERNIEREHEUREPAUSE = "DerniereHeurePause"; 
    final static String DATA_DISTANCEPARCOURUE = "DistanceParcourue"; 
    final static String DATA_AUTONOMIE = "Autonomie"; 
    final static String DATA_DERNIEREALARME = "DerniereAlarme"; 
    final static String DATA_HEUREDEPART = "HeureDepart"; 
    final static String DATA_ANNONCEBATTERIEFAIBLE = "AnnonceBatterieFaible"; 
    final static String DATA_ANNONCEDEMIRESERVOIR = "DemiReservoirAnnonce"; 
    final static String DATA_ANNONCEQUARTRESERVOIR = "QuartReservoirAnnonce"; 
    final static String DATA_DERNIERSMS = "dernier_sms";

    ///////////////////////////////////////////////////////////////////////////
    // Donnees en acces publique pour raison de performance
    // mettre _modifie a true des qu'on modifie une donnee pour que Flush enregistre les modifs
    public long _DernierSMS ;
    public long _DerniereHeurePause;
    public long _DistanceParcourue ;
    public long _DerniereAlerteVitesse;
    public long _HeureDepart;
    public long _DerniereHeureAlarme;
    public long _DernierChiffreKilometre ;
    public boolean _AnnonceQuartReservoir;
    public boolean _AnnonceDemiReservoir;
    public boolean _AnnonceBatterieFaible;
    public boolean _modifie ;           //
	public long _Autonomie;

    public static final String TAG = "PersistentData" ;

    public PersistentData( Context context )
    {
        SharedPreferences settings 			= GetPreferences(context) ;
       
        _DernierSMS             =   settings.getLong( DATA_DERNIERSMS, 0 ) ;
        _DerniereHeurePause 	=   settings.getLong( DATA_DERNIEREHEUREPAUSE, 0 ) ;
        _DistanceParcourue      =   settings.getLong(DATA_DISTANCEPARCOURUE, 0) ;
        _Autonomie				=   settings.getLong(DATA_AUTONOMIE, 0) ;
        _DerniereAlerteVitesse  =   settings.getLong(DATA_DERNIERE_ALERTE_VITESSE, 0) ;
        _HeureDepart            =   settings.getLong(DATA_HEUREDEPART, 0) ;
        _DerniereHeureAlarme    =   settings.getLong(DATA_DERNIEREALARME, 0) ;
        _AnnonceDemiReservoir   =   settings.getBoolean(DATA_ANNONCEDEMIRESERVOIR, false) ;
        _AnnonceQuartReservoir  =   settings.getBoolean(DATA_ANNONCEQUARTRESERVOIR, false) ;
        _AnnonceBatterieFaible  =   settings.getBoolean(DATA_ANNONCEBATTERIEFAIBLE, false) ;
        _modifie = false ;
        /*
        Log.d(TAG, "Read persistent data" ) ;
        Log.d(TAG, "DernierSMS " + CompanionService.toHourString(_DernierSMS)) ;
        Log.d(TAG, "Dernier pause " + CompanionService.toHourString(_DerniereHeurePause)) ;
        Log.d(TAG, "Dernier alerte vitesse " + CompanionService.toHourString(_DerniereAlerteVitesse)) ;
        Log.d(TAG, "Heure depart " + CompanionService.toHourString(_HeureDepart)) ;
        Log.d(TAG, "Distance parcourue " + _DistanceParcourue) ;
        Log.d(TAG, "Annonce Demi Reservoir" + _AnnonceDemiReservoir ) ; 
        Log.d(TAG, "Annonce Quart Reservoir" + _AnnonceQuartReservoir ) ; 
        Log.d(TAG, "Annonce Batterie faible" + _AnnonceBatterieFaible) ; 
        */
    }

/***
 * Retourne un objet qui permettra d'acceder aux donnees persistantes
 * @param context
 * @return
 */
    private SharedPreferences GetPreferences( Context context )
	{
		return context.getSharedPreferences( CACHE_NAME, Context.MODE_PRIVATE );
	}


	public void Flush( Context context )
    {
        if ( ! _modifie )
            return ;
        
        SharedPreferences settings = GetPreferences( context ) ;
        SharedPreferences.Editor editor = settings.edit();
/*
        Log.d(TAG, "Save persistent data" ) ;
        Log.d(TAG, "DernierSMS " + CompanionService.toHourString(_DernierSMS)) ;
        Log.d(TAG, "Dernier pause " + CompanionService.toHourString(_DerniereHeurePause)) ;
        Log.d(TAG, "Dernier alerte vitesse " + CompanionService.toHourString(_DerniereAlerteVitesse)) ;
        Log.d(TAG, "Heure depart " + CompanionService.toHourString(_HeureDepart)) ;
        Log.d(TAG, "Distance parcourue " + _DistanceParcourue) ;
        Log.d(TAG, "Annonce Demi Reservoir" + _AnnonceDemiReservoir ) ; 
        Log.d(TAG, "Annonce Quart Reservoir" + _AnnonceQuartReservoir ) ; 
        Log.d(TAG, "Annonce Batterie faible" + _AnnonceBatterieFaible) ; 
    */
        editor.putLong( DATA_DERNIERSMS, _DernierSMS ) ;
        editor.putLong( DATA_DERNIEREHEUREPAUSE, _DerniereHeurePause) ;
        editor.putLong( DATA_DISTANCEPARCOURUE, _DistanceParcourue) ;
        editor.putLong( DATA_AUTONOMIE, _Autonomie) ;
        editor.putLong( DATA_DERNIERE_ALERTE_VITESSE, _DerniereAlerteVitesse) ;
        editor.putLong( DATA_HEUREDEPART, _HeureDepart) ;
        editor.putLong( DATA_DERNIEREALARME, _DerniereHeureAlarme) ;
        editor.putBoolean(DATA_ANNONCEDEMIRESERVOIR, _AnnonceDemiReservoir) ;
        editor.putBoolean(DATA_ANNONCEQUARTRESERVOIR, _AnnonceQuartReservoir) ;
        editor.putBoolean(DATA_ANNONCEBATTERIEFAIBLE, _AnnonceBatterieFaible) ;
        editor.commit();
        _modifie = false ;
    }


    public int LitDernieresPositions(Context context, Location[] locations)
    {
        SharedPreferences settings = GetPreferences(context) ;
        int NbPositions = settings.getInt(PersistentData.DATA_NBPOSITIONS, 0);

        for (int i = 0; i < NbPositions; i++)
        {
            locations[i] = new Location(LocationManager.GPS_PROVIDER) ;
            locations[i].setAccuracy(settings.getFloat(PersistentData.DATA_ACCURACY + i, 0));
            locations[i].setAltitude(settings.getFloat(PersistentData.DATA_ALTITUDE + i, 0));
            locations[i].setBearing(settings.getFloat(PersistentData.DATA_BEARING + i, 0));
            locations[i].setLatitude(settings.getFloat(PersistentData.DATA_LATITUDE + i, 0));
            locations[i].setLongitude(settings.getFloat(PersistentData.DATA_LONGITUDE + i, 0));
            locations[i].setProvider(settings.getString(PersistentData.DATA_PROVIDER + i, "")); 
            locations[i].setSpeed(settings.getFloat(PersistentData.DATA_SPEED + i, 0));
            locations[i].setTime(settings.getLong(PersistentData.DATA_TIME + i, 0));

        }
        return NbPositions;
    }

    /***
     * Memorise les dernieres positions
     *
     * @param locations
     * @param NbPositions
     */
    public void EcritDernieresPositions(Context context, Location[] locations, int NbPositions)
    {
        SharedPreferences settings = GetPreferences(context) ;
        SharedPreferences.Editor editor = settings.edit();

        editor.putInt(PersistentData.DATA_NBPOSITIONS, NbPositions);

        for (int i = 0; i < NbPositions; i++)
        {
            editor.putFloat(PersistentData.DATA_ACCURACY + i, locations[i].getAccuracy());
            editor.putFloat(PersistentData.DATA_ALTITUDE + i, (float) locations[i].getAltitude());
            editor.putFloat(PersistentData.DATA_BEARING + i, locations[i].getBearing());
            editor.putFloat(PersistentData.DATA_LATITUDE + i, (float) locations[i].getLatitude());
            editor.putFloat(PersistentData.DATA_LONGITUDE + i, (float) locations[i].getLongitude());
            editor.putString(PersistentData.DATA_PROVIDER + i, locations[i].getProvider());
            editor.putFloat(PersistentData.DATA_SPEED + i, locations[i].getSpeed());
            editor.putLong(PersistentData.DATA_TIME + i, locations[i].getTime());
        }

        // Commit the edits!
        editor.commit();
    }
}
