/**
 * 
 */
package com.lpi.simpleclock;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;

/**
 * @author lucien
 * Gestion des preferences du widget
 */
public class Preferences
{
	private boolean _dirty ;
	private String _typeFace = null ;
	private int	   	_couleurTexte = Color.WHITE ;
	private int	   	_couleurFond = Color.argb(0,  0,  0,  0);
	private boolean  _Ombre = true ;
	
	public static final String PREFERENCES				= "com.lpi.simpleclock" ; //$NON-NLS-1$
	public static final String PREF_TYPEFACE			= "typeface" ; //$NON-NLS-1$
	public static final String PREF_COULEUR_TEXTE		= "couleurtexte" ; //$NON-NLS-1$
	public static final String PREF_COULEUR_FOND		= "couleurfond" ; //$NON-NLS-1$
	public static final String PREF_OMBRE				= "ombre" ; //$NON-NLS-1$
	
	public Preferences( Context c )
	{
		SharedPreferences settings 				= c.getSharedPreferences( PREFERENCES, Context.MODE_PRIVATE);
		_typeFace								= settings.getString( PREF_TYPEFACE, ""); //$NON-NLS-1$
		_couleurTexte							= settings.getInt( PREF_COULEUR_TEXTE, _couleurTexte);
		_couleurFond							= settings.getInt( PREF_COULEUR_FOND, _couleurFond);
		_Ombre									= settings.getBoolean( PREF_OMBRE, _Ombre);
		_dirty = false ;
	}
	
	/**
	 * @return the _typeFace
	 */
	public String get_typeFace()
	{
		return _typeFace;
	}

	/**
	 * @param _typeFace the _typeFace to set
	 */
	public void set_typeFace(String typeFace)
	{
		_typeFace = typeFace;
		_dirty = true ;
	}
	
	/**
	 * S'assurer que les preferences sont bien enregistrees
	 * @param context
	 */
	public void flush(Context c)
	{
		if ( ! _dirty)
			return ;
		
		SharedPreferences settings = c.getSharedPreferences( PREFERENCES, Context.MODE_PRIVATE );
		SharedPreferences.Editor editor = settings.edit();

		editor.putString( PREF_TYPEFACE, _typeFace);
		editor.putInt( PREF_COULEUR_TEXTE, _couleurTexte );
		editor.putInt( PREF_COULEUR_FOND, _couleurFond);
		editor.putBoolean( PREF_OMBRE, _Ombre);
		
		editor.commit();
		_dirty = false ;
	}

	/**
	 * @return the _couleurTexte
	 */
	public int get_couleurTexte()
	{
		return _couleurTexte;
	}

	/**
	 * @param _couleurTexte the _couleurTexte to set
	 */
	public void set_couleurTexte(int couleur)
	{
		_couleurTexte = couleur;
		_dirty = true ;
	}
	/**
	 * @return the _couleurTFond
	 */
	public int get_couleurFond()
	{
		return _couleurFond;
	}

	/**
	 * @param _couleurTexte the _couleurTexte to set
	 */
	public void set_couleurFond(int couleur)
	{
		_couleurFond = couleur;
		_dirty = true ;
	}

	/**
	 * @return the _Ombre
	 */
	public boolean is_Ombre()
	{
		return _Ombre;
	}

	/**
	 * @param _Ombre the _Ombre to set
	 */
	public void set_Ombre(boolean Ombre)
	{
		_Ombre = Ombre;
		_dirty = true ;
	}
}
