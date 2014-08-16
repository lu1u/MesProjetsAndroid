/**
 * 
 */
package com.lpi.compagnondumotard;

/**
 * @author lucien
 *
 */
public class Constants {

	//static public final String	TAG 				= "BikersCompanion" ;
	public static final String	TIME_UPDATE 			= "lpi.BikersCompanion.Alarme" ;
	public static final String	PAUSE_UPDATE 			= "lpi.BikersCompanion.Pause" ;
	public static final String	SERVICE_COMMAND 		= "lpi.BikersCompanion.Commande" ;
	public static final String	SERVICE_ANSWER	 		= "lpi.BikersCompanion.Reponse" ;
	public static final String	KILOMETER_CHANGE 		= "lpi.BikersCompanion.Kilometre" ;
	public static final String	MESSAGE			 		= "lpi.BikersCompanion.Message" ;

	// Parametres de SERVICE_COMMAND
	public static final String 	COMMAND					= "commande" ;
	public static final int 	COMMAND_START			= 0 ;
	public static final int 	COMMAND_STOP			= 1 ;
	public static final int 	COMMAND_RAZ				= 2 ;
	public static final int		COMMAND_PAUSE			= 3 ;
	public static final int		COMMAND_PLEIN			= 4 ;
	public static final int 	COMMAND_GETSTATE		= 5 ;
	
	// COMMANDE_PAUSE
	public static final String	COMMAND_PAUSE_STATE		=	"pause" ;
	
	// Etats du service
	public static final String  COMMANDE_STATE			= "etat" ;
	public static final int		ETAT_ARRETE				=	0 ;
	public static final int		ETAT_ENROUTE			=	1 ;

	// Changement de kilometre
	public static final String	PARCOURU				=	"parcouru" ;
	public static final String	AUTONOMIE				=	"autonomie" ;
}
