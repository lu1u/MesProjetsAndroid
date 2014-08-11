/**
 *
 */
package com.lpi.compagnondumotard;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * @author lucien
 */
public class Preferences {
    //public static final String PREFERENCES = "com.lpi.bikercompanion.preferences";
    static private String PREFS_MINUTESENTREPAUSES = "delai_pauses"; //$NON-NLS-1$
    static private String PREFS_MINUTESENTREANNONCEHEURE = "delai_minutes"; //$NON-NLS-1$
    static private String PREFS_LIRESMS = "LireSMS"; //$NON-NLS-1$
    static private String PREFS_SETAIRETELEPHONEDECROCHE = "se_taire_telephone"; //$NON-NLS-1$
    static private String PREFS_AUTONOMIERESERVOIR = "autonomie"; //$NON-NLS-1$
    static private String PREFS_ALERTEDEMIRESERVOIR = "alerte_mireservoir"; //$NON-NLS-1$
    static private String PREFS_ALERTEQUARTRESERVOIR = "alerte_quartreservoir"; //$NON-NLS-1$
    static private String PREFS_VITESSEMAX = "vitesse_max"; //$NON-NLS-1$

    private boolean _dirty;
    private int minutesEntrePauses = 120;
    private int minutesEntreAnnonceHeure = 15;
    private boolean lireSMS = true;
    private boolean seTaireTelephoneDecroche = true;
    private long autonomieReservoir = 300;
    private boolean alerteDemiReservoir = true;
    private boolean alerteQuartReservoir = true;
    private long vitesseMax = (long) (130.0 / 3.6);

    /**
     * Constructeur
     *
     * @param context
     */
    public Preferences(Context context) {
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
        _dirty = false;
        minutesEntrePauses = Integer.valueOf(settings.getString(PREFS_MINUTESENTREPAUSES, Integer.toString(minutesEntrePauses)));
        minutesEntreAnnonceHeure = Integer.valueOf(settings.getString(PREFS_MINUTESENTREANNONCEHEURE, Integer.toString(minutesEntreAnnonceHeure)));
        lireSMS = settings.getBoolean(PREFS_LIRESMS, lireSMS);
        seTaireTelephoneDecroche = settings.getBoolean(PREFS_SETAIRETELEPHONEDECROCHE, seTaireTelephoneDecroche);
        autonomieReservoir = Long.valueOf(settings.getString(PREFS_AUTONOMIERESERVOIR, Long.toString(autonomieReservoir)));
        alerteDemiReservoir = settings.getBoolean(PREFS_ALERTEDEMIRESERVOIR, alerteDemiReservoir);
        alerteQuartReservoir = settings.getBoolean(PREFS_ALERTEQUARTRESERVOIR, alerteQuartReservoir);
        vitesseMax = Long.valueOf(settings.getString(PREFS_VITESSEMAX, Long.toString(vitesseMax)));
    }


    /**
     * Ecrire les modifications
     *
     * @param context
     */
    public void Flush(Context context) {
        if (!_dirty)
            return;

        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = settings.edit();
        editor.putInt(PREFS_MINUTESENTREPAUSES, minutesEntrePauses);
        editor.putInt(PREFS_MINUTESENTREANNONCEHEURE, minutesEntreAnnonceHeure);
        editor.putBoolean(PREFS_LIRESMS, lireSMS);
        editor.putBoolean(PREFS_SETAIRETELEPHONEDECROCHE, seTaireTelephoneDecroche);
        editor.putLong(PREFS_AUTONOMIERESERVOIR, autonomieReservoir);
        editor.putBoolean(PREFS_ALERTEDEMIRESERVOIR, alerteDemiReservoir);
        editor.putBoolean(PREFS_ALERTEQUARTRESERVOIR, alerteQuartReservoir);
        editor.putLong(PREFS_VITESSEMAX, vitesseMax);
        editor.commit();
        _dirty = false;
    }


    /**
     * @return the minutesEntrePauses
     */
    public int getMinutesEntrePauses() {
        return minutesEntrePauses;
    }


    /**
     * @param minutesEntrePauses the minutesEntrePauses to set
     */
    public void setMinutesEntrePauses(int minutesEntrePauses) {
        this.minutesEntrePauses = minutesEntrePauses;
        _dirty = true;
    }


    /**
     * @return the minutesEntreAnnonceHeure
     */
    public int getMinutesEntreAnnonceHeure() {
        return minutesEntreAnnonceHeure;
    }


    /**
     * @param minutesEntreAnnonceHeure the minutesEntreAnnonceHeure to set
     */
    public void setMinutesEntreAnnonceHeure(int minutesEntreAnnonceHeure) {
        this.minutesEntreAnnonceHeure = minutesEntreAnnonceHeure;
        _dirty = true;
    }


    /**
     * @return the lireSMS
     */
    public boolean isLireSMS() {
        return lireSMS;
    }


    /**
     * @param lireSMS the lireSMS to set
     */
    public void setLireSMS(boolean lireSMS) {
        this.lireSMS = lireSMS;
        _dirty = true;
    }


    /**
     * @return the seTaireTelephoneDecroche
     */
    public boolean isSeTaireTelephoneDecroche() {
        return seTaireTelephoneDecroche;
    }


    /**
     * @param seTaireTelephoneDecroche the seTaireTelephoneDecroche to set
     */
    public void setSeTaireTelephoneDecroche(boolean seTaireTelephoneDecroche) {
        this.seTaireTelephoneDecroche = seTaireTelephoneDecroche;
        _dirty = true;
    }


    /**
     * @return the autonomieReservoir
     */
    public long getAutonomieReservoir() {
        return autonomieReservoir;
    }


    /**
     * @param autonomieReservoir the autonomieReservoir to set
     */
    public void setAutonomieReservoir(long autonomieReservoir) {
        this.autonomieReservoir = autonomieReservoir;
        _dirty = true;
    }


    /**
     * @return the alerteDemiReservoir
     */
    public boolean isAlerteDemiReservoir() {
        return alerteDemiReservoir;
    }


    /**
     * @param alerteDemiReservoir the alerteDemiReservoir to set
     */
    public void setAlerteDemiReservoir(boolean alerteDemiReservoir) {
        this.alerteDemiReservoir = alerteDemiReservoir;
        _dirty = true;
    }


    /**
     * @return the alerteQuartReservoir
     */
    public boolean isAlerteQuartReservoir() {
        return alerteQuartReservoir;
    }


    /**
     * @param alerteQuartReservoir the alerteQuartReservoir to set
     */
    public void setAlerteQuartReservoir(boolean alerteQuartReservoir) {
        this.alerteQuartReservoir = alerteQuartReservoir;
        _dirty = true;
    }


    /**
     * @return the vitesseMax
     */
    public long getVitesseMax() {
        return vitesseMax;
    }


    /**
     * @param vitesseMax the vitesseMax to set
     */
    public void setVitesseMax(long vitesseMax) {
        this.vitesseMax = vitesseMax;
        _dirty = true;
    }
}
