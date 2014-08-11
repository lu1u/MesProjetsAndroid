/**
 * 
 */
package com.lpi.compagnondumotard;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;



/**
 * @author lucien
 *
 */
public class Log {
	private static final String FILE_NAME = "/mnt/sdcard/BikerCompanion.log" ;
	public static final String TAG = "BikerCompanion";
	public static void debug( String s )
	{
		try {
			android.util.Log.d(TAG, s) ;
			
			FileWriter fstream = new FileWriter(FILE_NAME,true);
			BufferedWriter out = new BufferedWriter(fstream, 8192);
			out.write("D ") ;
			out.write(new Date().toString()) ;
			out.write(" " ) ;
			out.write(s) ;
			out.newLine() ;
			out.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@SuppressWarnings("nls")
	public static void erreur( String s )
	{
		try {
			android.util.Log.e(TAG, s) ;
			
			FileWriter fstream = new FileWriter(FILE_NAME,true);
			BufferedWriter out = new BufferedWriter(fstream, 8192);
			out.write("E ") ;
			out.write(new Date().toString()) ;
			out.write(" " ) ;
			out.write(s) ;
			out.newLine() ;
			out.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
