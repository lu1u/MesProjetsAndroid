/**
 * 
 */
package com.lpi.frequentlauncherwidget.bdd;

import java.util.ArrayList;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

/**
 * @author lucien
 *
 */
public class MySQLiteDatabase
{
	@SuppressWarnings("nls")
	public static final String TAG = "MySQLiteDatabase" ;
	private BDDOpenHelper _openHelper ;
	public SQLiteDatabase _bdd ;
	public static final int TRI_NBLANCEMENTS = 0 ;
	public static final int TRI_DATELANCEMENT = 1 ;
	@SuppressWarnings("nls")
	public static final String PREFERENCE_TRI = "Tri" ;
	public static final String PREFERENCE_DELAI = "Delai" ;
	
	/***
	 * Ouverture et creation de la base de donnees
	 */
	public MySQLiteDatabase( Context ctx )
	{
		_openHelper = new BDDOpenHelper(ctx, null);
		_bdd = _openHelper.getWritableDatabase();
	}
	
	public void close()
	{
		_bdd.close();
	}
	
	/**
	 * Incremente le compteur de lancement d'une application, creer l'enregistrement si il n'existe pas
	 */
	@SuppressWarnings("nls")
	public void IncrementeCompteur( String componentName, int time, int id )
	{
		try
		{
			// D'abord essayer de trouver l'enregistrement
			String[] tableColumns = new String[]{ BDDOpenHelper.COLONNE_COMPONENT_NAME, BDDOpenHelper.COLONNE_NB_LANCEMENTS };
			String whereClause = BDDOpenHelper.COLONNE_COMPONENT_NAME + " =?";
			String[] whereArgs = new String[] {componentName };
			Cursor c = _bdd.query(BDDOpenHelper.TABLE_LANCEMENTS, tableColumns, whereClause, whereArgs, null, null, null);
			//Log.d(TAG, "apres query, cursor " + c.getCount() ) ;
			
			if ( c.getCount() == 0 )
			{
				if ( ! InExcluded(componentName))
				{
				// Creer l'enregistrement
				ContentValues insertValues = new ContentValues();
				insertValues.put(BDDOpenHelper.COLONNE_COMPONENT_NAME, componentName);
				insertValues.put(BDDOpenHelper.COLONNE_NB_LANCEMENTS, Integer.valueOf(1));
				insertValues.put(BDDOpenHelper.COLONNE_DERNIER_LANCEMENT, Integer.valueOf(time));
				insertValues.put(BDDOpenHelper.COLONNE_DERNIER_ID, Integer.valueOf(id));
				_bdd.insert(BDDOpenHelper.TABLE_LANCEMENTS, null, insertValues);
				}
			}
			else
			{
				final int col = c.getColumnIndex(BDDOpenHelper.COLONNE_NB_LANCEMENTS) ;
				c.moveToFirst() ;
				int Compteur = c.getInt(col) ;
				
				// Incrementer le compteur
				String strFilter = BDDOpenHelper.COLONNE_COMPONENT_NAME + "= '" + componentName + "'" ;
				ContentValues args = new ContentValues();
				args.put(BDDOpenHelper.COLONNE_NB_LANCEMENTS, Integer.valueOf(Compteur + 1));
				_bdd.update(BDDOpenHelper.TABLE_LANCEMENTS, args, strFilter, null);
			}
			
			c.close();
		} catch (Exception e)
		{
			Log.e(TAG, e.getLocalizedMessage() );
		}
	}

	/***
	 * Retourne true si l'application fait partie de la table des exclues
	 * @param componentName
	 * @return
	 */
	public boolean InExcluded(String componentName)
	{
		String[] tableColumns = new String[]{ BDDOpenHelper.COLONNE_COMPONENT_NAME };
		@SuppressWarnings("nls")
		String whereClause = BDDOpenHelper.COLONNE_COMPONENT_NAME + " =?";
		String[] whereArgs = new String[] {componentName };
		Cursor c = _bdd.query(BDDOpenHelper.TABLE_EXCLUES, tableColumns, whereClause, whereArgs, null, null, null);
		
		boolean res = false ;
		if ( c != null)
			if ( c.getCount() > 0 )
				res = true ;
		c.close();
		return res;
	}

	/***
	 * Vide la table des applications exclues et la remplit avec celle qui est donnee en parametre
	 * @param exclues
	 */
	public void setExclues(ArrayList<String> exclues)
	{
		// Vider la table EXCLUES
		_bdd.delete(BDDOpenHelper.TABLE_EXCLUES, null, null) ;
		
		// La remplir a nouveau
		ContentValues record = new ContentValues();
		for (String ex : exclues)
		{
			record.put(BDDOpenHelper.COLONNE_COMPONENT_NAME, ex);
			_bdd.insert(BDDOpenHelper.TABLE_EXCLUES, null, record);
		}
	}

	@SuppressWarnings("nls")
	/**
	 * Retourne un curseur permettant d'acceder a la liste des applications frequentes
	 * @return
	 */
	public Cursor getFrequentApps( int WidgetId )
	{
		String query = null ;
		int Tri = getSortOption( WidgetId) ;
		switch( Tri )
		{
		case TRI_NBLANCEMENTS:
			query = "SELECT * FROM " + BDDOpenHelper.TABLE_LANCEMENTS 
				+ " WHERE " + BDDOpenHelper.COLONNE_COMPONENT_NAME + " NOT IN ( SELECT " + BDDOpenHelper.COLONNE_COMPONENT_NAME + " FROM " + BDDOpenHelper.TABLE_EXCLUES + ")"
				+ " ORDER BY " +BDDOpenHelper.COLONNE_NB_LANCEMENTS + " DESC "; //LIMIT 10" ;
			break ;
			
		case TRI_DATELANCEMENT :
			query = "SELECT * FROM " + BDDOpenHelper.TABLE_LANCEMENTS 
			+ " WHERE " + BDDOpenHelper.COLONNE_COMPONENT_NAME + " NOT IN ( SELECT " + BDDOpenHelper.COLONNE_COMPONENT_NAME + " FROM " + BDDOpenHelper.TABLE_EXCLUES + ")"
			+ " ORDER BY " +BDDOpenHelper.COLONNE_DERNIER_LANCEMENT + " DESC " ; //LIMIT 10" ;
			break ;
			
		default: 
			assert false: "Valeur de tri incorrecte " + Tri ;
		}
		assert query != null ;
		
		return _bdd.rawQuery(query, null ) ;
	}

	/**
	 * Change l'option de tri des applications
	 */
	public int ChangeSortOption( int WidgetId )
	{
		int Tri = getSortOption( WidgetId ) ;
		switch( Tri )
		{
		case TRI_DATELANCEMENT : Tri = TRI_NBLANCEMENTS ; break ;
		case TRI_NBLANCEMENTS : Tri = TRI_DATELANCEMENT ; break ;
		default: 
			assert false : "Mauvaise valeur de tri dans la base" ; //$NON-NLS-1$
		}
		
		SetPreference( PREFERENCE_TRI, WidgetId, Integer.toString(Tri) ) ;
		return Tri ;
	}
	
	public int GetDelaiScrutation()
	{
		return GetIntPreference( PREFERENCE_DELAI, 3 ) ;
	}
	public int getSortOption(int WidgetId )
	{
		return GetIntPreference( PREFERENCE_TRI, WidgetId, TRI_NBLANCEMENTS ) ;
	}

	@SuppressWarnings("nls")
	private void SetPreference(String preference, int WidgetId, String Val )
	{
		final String preferenceName = preference + WidgetId ;
		
		final String requete =  "INSERT OR REPLACE INTO " + BDDOpenHelper.TABLE_PREFERENCES 
				+ "( " + BDDOpenHelper.COLONNE_PREFERENCE_NAME + "," +  BDDOpenHelper.COLONNE_PREFERENCE_VALUE + " ) "
				+ "VALUES ( '" + preferenceName + "', '" + Val + "' )  ;";
		_bdd.execSQL(requete);
	}

	private int GetIntPreference(String preference, int WidgetId, int defaut)
	{
		return GetIntPreference( preference + WidgetId, defaut );
	}
	
	private int GetIntPreference( String preference, int defaut )
	{
		String pref = GetPreference(preference) ;
		
		try
		{
			return Integer.parseInt(pref);
		} catch (NumberFormatException e)
		{
			return defaut ;
		}
	}

	@SuppressWarnings("nls")
	/***
	 * Retourne une des preferences nommees dans la table des references
	 * @param preference
	 * @return
	 */
	private String GetPreference(String preference )
	{
		String[] tableColumns = new String[]{ BDDOpenHelper.COLONNE_PREFERENCE_VALUE };
		String whereClause = BDDOpenHelper.COLONNE_PREFERENCE_NAME + " =?";
		String[] whereArgs = new String[] {preference };
		Cursor c = _bdd.query(BDDOpenHelper.TABLE_PREFERENCES, tableColumns, whereClause, whereArgs, null, null, null);
		assert c == null : "requete incorrecte dans GetPreference" ;
		if ( c == null)
			return null ;
		
		String res = null ;
		if ( c.moveToFirst())
			res = c.getString(0) ;
		
		c.close();
		return res ;
	}
	
	/***
	 * Retourne une des preferences nommees dans la table des references
	 * @param preference
	 * @return
	 */
	private String GetPreference(String preference, int WidgetId )
	{
		return GetPreference( preference + WidgetId ) ;
	}
	
		
}

