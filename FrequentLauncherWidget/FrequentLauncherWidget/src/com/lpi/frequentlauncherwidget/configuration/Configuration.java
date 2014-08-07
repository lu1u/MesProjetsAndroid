/**
 * 
 */
package com.lpi.frequentlauncherwidget.configuration;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import android.app.Activity;
import android.app.ProgressDialog;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ListView;
import com.lpi.frequentlauncherwidget.R;
import com.lpi.frequentlauncherwidget.bdd.MySQLiteDatabase;
import com.lpi.frequentlauncherwidget.widgetprovider.FrequentWidgetProvider;

/**
 * @author lucien
 * 
 */
public class Configuration extends Activity
{
	private MySQLiteDatabase _bdd;
	private ApplicationAdapter _adapter;

	// Keeps the font file paths and names in separate arrays
	int AppWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;
	// private Preferences prefs;
	private static final String TAG = "Configuration";
	Handler _handler = new Handler() {
		@Override
		public void handleMessage(Message msg)
		{
			ListView lv = (ListView) findViewById(R.id.listViewApplications);
			lv.setAdapter(_adapter);
		}
	};

	/**
	 * 
	 * @param v
	 */
	public void OnCancel(View v)
	{
		Intent resultValue = new Intent();
		resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetId);
		setResult(RESULT_CANCELED, resultValue);
		finish();
	}

	/** Called with the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		Log.d(TAG, "onCreate");

		setResult(RESULT_CANCELED);
		// Inflate our UI from its XML layout description.
		setContentView(R.layout.excluded);
		Intent intent = getIntent();
		Bundle extras = intent.getExtras();
		Log.d(TAG, "extras");
		if (extras != null)
			AppWidgetId = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
		else
			AppWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;

		RemplitListeApplications();
	}

	/***
	 * Remplit la listeview avec les applications installee Va chercher dans la base de donnees les
	 * applications qui sont exclues
	 */
	private void RemplitListeApplications()
	{
		_bdd = new MySQLiteDatabase(this);
		Log.d(TAG, "remplitlistefontes");

		ListView lv = (ListView) findViewById(R.id.listViewApplications);
		ApplicationAdapter tmpadapter = new ApplicationAdapter();
		lv.setAdapter(tmpadapter);

		_adapter = new ApplicationAdapter();

		// Chargement des applications dans un thread separe, car ca peut etre long...
		final ProgressDialog progressDialog = ProgressDialog.show(this,
				getResources().getString(R.string.chargement_applications), getResources()
						.getString(R.string.patientez));

		new Thread() {
			@Override
			public void run()
			{
				try
				{
					PackageManager pm = getPackageManager();
					for (ApplicationInfo appInfo : pm.getInstalledApplications(0))
					{
						_adapter.add(new LigneConfig(appInfo, pm, _bdd));
					}

					_adapter.sort();

				} catch (Exception e)
				{
					Log.e("tag", e.getMessage());
				}
				// dismiss the progress dialog
				progressDialog.dismiss();
				_handler.sendEmptyMessage(0);
			}
		}.start();

	}

	/**
	 * Validation de la configuration
	 * @param v
	 */
	public void OnOK(View v)
	{
		_bdd.setExclues(_adapter.getExclues());
		Intent resultValue = new Intent();
		setResult(RESULT_OK);
		resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetId);
		FrequentWidgetProvider.SendRefresh(this); 
		finish();
	}

	public class ApplicationAdapter extends BaseAdapter
	{
		private List<LigneConfig> _applications;

		public ApplicationAdapter()
		{
			super();
			_applications = new ArrayList<LigneConfig>();

		}

		public ArrayList<String> getExclues()
		{
			ArrayList<String> res = new ArrayList<String>();

			for (LigneConfig l : _applications)
				if (l.exclue)
				{
					//Log.d(TAG, "Exclue " + l.componentName);
					res.add(l.componentName);
				} //else
					//Log.d(TAG, "Non exclue " + l.componentName);
			return res;
		}

		public void handleClick(View view, int position)
		{
			Log.d(TAG, "handleClick " + position);
			LigneConfig l = _applications.get(position);

			CheckBox tv = (CheckBox) view.findViewById(R.id.checkBox1);
			l.exclue = tv.isChecked();
		}

		public void sort()
		{
			Collections.sort(_applications, new Comparator<LigneConfig>() {
				@Override
				public int compare(LigneConfig l1, LigneConfig l2)
				{

					return l1.nomApplication.compareTo(l2.nomApplication);
				}
			});
		}

		public void add(LigneConfig l)
		{
			_applications.add(l);
		}

		@Override
		public int getCount()
		{
			return _applications.size();
		}

		@Override
		public Object getItem(int position)
		{
			return _applications.get(position).componentName;
		}

		@Override
		public long getItemId(int position)
		{
			// We use the position as ID
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent)
		{
			View view = convertView;

			// This function may be called in two cases: a new view needs to be created,
			// or an existing view needs to be reused
			if (view == null)
			{
				// Since we're using the system list for the layout, use the system inflater
				final LayoutInflater inflater = (LayoutInflater) getApplicationContext().getSystemService(
						Context.LAYOUT_INFLATER_SERVICE);
				view = inflater.inflate(R.layout.excludedview, parent, false);
			}

			if (view != null)
			{
				LigneConfig ligne = _applications.get(position);
				CheckBox cb = (CheckBox) view.findViewById(R.id.checkBox1);
				cb.setText(ligne.nomApplication + " (" + ligne.componentName + ")");
				cb.setChecked(ligne.exclue);
				cb.setTag(ligne);
				cb.setOnCheckedChangeListener(new OnCheckedChangeListener() {
					@Override
					public void onCheckedChanged(CompoundButton arg0, boolean arg1)
					{
						LigneConfig ligne = (LigneConfig) arg0.getTag();
						ligne.exclue = arg1;
					}
				});
			}

			return view;
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		switch (requestCode)
		{
		/*
		 * case ColorPickerActivity.RESULT_CODE_COULEUR: if (resultCode == RESULT_OK ) if (data !=
		 * null) { String typeCouleur = data.getStringExtra(ColorPickerActivity.TYPE_COULEUR); if
		 * (COULEUR_FOND.equals(typeCouleur)) { int Couleur =
		 * data.getIntExtra(ColorPickerActivity.COULEUR, prefs.get_couleurFond());
		 * prefs.set_couleurFond(Couleur); findViewById(R.id.btnFond).setBackgroundColor(Couleur); }
		 * else if (COULEUR_TEXTE.equals(typeCouleur)) { int Couleur =
		 * data.getIntExtra(ColorPickerActivity.COULEUR, prefs.get_couleurTexte());
		 * prefs.set_couleurTexte(Couleur); findViewById(R.id.btnTexte).setBackgroundColor(Couleur);
		 * } } break;
		 */
		}
	}
}
