/**
 * Classe utilitaire pour la creation de la base de donnees Trois tables: LANCEMENTS: enregistre
 * tous les lancements detectes EXCLUES: enregistre les applications qu'on ne veut pas voir
 * apparaitre dans le widget ² PREFERENCES:enregistre les preferences
 */
package com.lpi.frequentlauncherwidget.bdd;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;

import com.lpi.frequentlauncherwidget.R;

/**
 * @author lucien
 * 
 */
@SuppressWarnings("nls")
public class BDDOpenHelper extends SQLiteOpenHelper
{
	public static final String TAG = "BDDOpenHelper";
	// Version de la base de données
	private static final int DATABASE_VERSION = 5;

	// Nom de la base
	private static final String LANCEMENTS_BASE_NAME = "lancements.db";

	// Nom des tables
	public static final String TABLE_LANCEMENTS = "LANCEMENTS";
	public static final String TABLE_EXCLUES = "EXCLUES";
	public static final String TABLE_PREFERENCES = "PREFERENCES";

	// /////////////////////////////////////////////////////////////////////////////////////////////
	// Description de la colonne LANCEMENTS
	public static final String COLONNE_ID = "ID";
	public static final String COLONNE_COMPONENT_NAME = "COMPONENT_NAME";
	public static final String COLONNE_NB_LANCEMENTS = "NBLANCEMENTS";
	public static final String COLONNE_DERNIER_ID = "LAST_ID";
	public static final String COLONNE_DERNIER_LANCEMENT = "LAST_LAUNCH";

	// Table PREFERENCES
	public static final String COLONNE_PREFERENCE_NAME = "NAME";
	public static final String COLONNE_PREFERENCE_VALUE = "VALUE";

	// Description de la requete EXCLUES
	// public static final String COLUMN_COMPONENTNAME = "COMPONENT_NAME" ;

	// Requêtes SQL pour la création de la base
	private static final String REQUETE_CREATION_BDD_1 = "CREATE TABLE " + TABLE_LANCEMENTS + " (" + COLONNE_ID
			+ " INTEGER PRIMARY KEY AUTOINCREMENT, " + COLONNE_COMPONENT_NAME + " TEXT NOT NULL, "
			+ COLONNE_NB_LANCEMENTS + " INTEGER NOT NULL, " + COLONNE_DERNIER_ID + " INTEGER NOT NULL,"
			+ COLONNE_DERNIER_LANCEMENT + " INTEGER NOT NULL );";

	private static final String REQUETE_CREATION_BDD_2 = "CREATE TABLE " + TABLE_EXCLUES + " ("
			+ COLONNE_COMPONENT_NAME + " TEXT NOT NULL );";

	private static final String REQUETE_CREATION_BDD_3 = "CREATE TABLE " + TABLE_PREFERENCES + " ("
			+ COLONNE_PREFERENCE_NAME + " TEXT NOT NULL UNIQUE," + COLONNE_PREFERENCE_VALUE + " TEXT NOT NULL);";

	private Context mContext;

	public BDDOpenHelper(Context context, CursorFactory factory)
	{
		super(context, LANCEMENTS_BASE_NAME, factory, DATABASE_VERSION);
		mContext = context;
	}

	/**
	 * Création de la base
	 */
	@Override
	public void onCreate(SQLiteDatabase db)
	{
		db.execSQL(REQUETE_CREATION_BDD_1);
		db.execSQL(REQUETE_CREATION_BDD_2);
		db.execSQL(REQUETE_CREATION_BDD_3);
		
		// Initialisation de la table des exclues a partir des ressources
		// A faire: initialiser la base a partir d'un asset
		String[] exclues = mContext.getResources().getStringArray(R.array.exclues);
		ContentValues record = new ContentValues();
		for (String ex : exclues)
		{
			record.put(COLONNE_COMPONENT_NAME, ex);
			db.insert(TABLE_EXCLUES, null, record);
		}
	}

	/**
	 * Mise à jour de la base
	 */
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
	{
		// Lorsque l'on change le numéro de version de la base on supprime la
		// table puis on la recrée
		if (newVersion > DATABASE_VERSION)
		{
			db.execSQL("DROP TABLE " + TABLE_LANCEMENTS + ";");
			db.execSQL("DROP TABLE " + TABLE_EXCLUES + ";");
			db.execSQL("DROP TABLE " + TABLE_PREFERENCES + ";");
			onCreate(db);
		}
	}

}
