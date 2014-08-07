/**
 * 
 */
package com.lpi.frequentlauncherwidget.configuration;

import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;

import com.lpi.frequentlauncherwidget.bdd.MySQLiteDatabase;

/**
 * @author lucien
 *
 */
class LigneConfig
{
	String componentName ;
	String nomApplication ;
	boolean exclue ;
	
	public LigneConfig( ApplicationInfo appInfo, PackageManager pm, MySQLiteDatabase bdd )
	{
		componentName = appInfo.packageName ;
		String app ;
		String application ;
		try
		{
			nomApplication = pm.getApplicationLabel(appInfo).toString();
		} catch (final Exception e)
		{
			application = "inconnue";
		}
			exclue = bdd.InExcluded(componentName) ;
	}

	private boolean IsInExcluded(String componentName2)
	{
		// TODO Auto-generated method stub
		return false;
	}
}