/**
 * Activity de configuration du widget
 */
package com.lpi.simpleclock;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CheckedTextView;
import android.widget.ListView;

/**
 * @author lucien
 * 
 */
public class Configuration extends Activity
{
	// Keeps the font file paths and names in separate arrays
	private List<String> m_fontPaths;
	private List<String> m_fontNames;
	private int _selectedItem = -1;

	int AppWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;

	public static final String COULEUR_FOND = "couleurfond"; //$NON-NLS-1$
	public static final String COULEUR_TEXTE = "couleurtexte"; //$NON-NLS-1$

	private Preferences prefs;

	// private static final String TAG = "SimpleClock";

	/***
	 * Creation et initialisation de l'Activity
	 */
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		setResult(RESULT_CANCELED);
		setContentView(R.layout.configure);
		Intent intent = getIntent();
		Bundle extras = intent.getExtras();
		if (extras != null)
			AppWidgetId = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
		else
			AppWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;

		prefs = new Preferences(this);
		fillFontList();

		((CheckBox) findViewById(R.id.checkBoxOmbre)).setChecked(prefs.is_Ombre());
		findViewById(R.id.btnFond).setBackgroundColor(prefs.get_couleurFond());
		findViewById(R.id.btnTexte).setBackgroundColor(prefs.get_couleurTexte());
	}

	/**
	 * Appele en cas de clic sur le bouton "cancel"
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

	/***
	 * Remplissage de la listView des fontes
	 */
	private void fillFontList()
	{
		ListView lv = (ListView) findViewById(R.id.listViewFontes);
		// Get the fonts on the device
		HashMap<String, String> fonts = FontManager.enumerateFonts();
		m_fontPaths = new ArrayList<String>();
		m_fontNames = new ArrayList<String>();

		// Get the current value to find the checked item
		String selectedFontPath = prefs.get_typeFace();

		int idx = 0;
		_selectedItem = 0;

		for (String path : fonts.keySet())
		{
			if (path.equals(selectedFontPath))
				_selectedItem = idx;

			m_fontPaths.add(path);
			m_fontNames.add(fonts.get(path));
			idx++;
		}

		FontAdapter adapter = new FontAdapter();
		lv.setAdapter(adapter);
		lv.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> adapter, View v, int position, long id)
			{
				_selectedItem = position;
			}
		});
		lv.setSelection(_selectedItem);
		lv.setItemChecked(_selectedItem, true);
	}

	/**
	 * Appele quand on clique sur le bouton "Ok"
	 * 
	 * @param v
	 */
	public void OnOK(View v)
	{
		if (_selectedItem > -1)
		{
			prefs.set_typeFace(m_fontPaths.get(_selectedItem));
		}

		prefs.set_Ombre(((CheckBox) findViewById(R.id.checkBoxOmbre)).isChecked());
		prefs.flush(this);

		Intent resultValue = new Intent();
		resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetId);
		setResult(RESULT_OK, resultValue);

		SimpleClockWidgetProvider.updateWidgets(this);
		finish();
	}

	// Font adaptor responsible for redrawing the item TextView with the appropriate font.
	// We use BaseAdapter since we need both arrays, and the effort is quite small.
	public class FontAdapter extends BaseAdapter
	{
		@Override
		public int getCount()
		{
			return m_fontNames.size();
		}

		@Override
		public Object getItem(int position)
		{
			return m_fontNames.get(position);
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

				// And inflate the view android.R.layout.select_dialog_singlechoice
				// Why? See com.android.internal.app.AlertController method createListView()
				view = inflater.inflate(android.R.layout.select_dialog_singlechoice, parent, false);
			}

			if (view != null)
			{
				// Find the text view from our interface
				CheckedTextView tv = (CheckedTextView) view.findViewById(android.R.id.text1);

				Typeface tface = Typeface.createFromFile(m_fontPaths.get(position));
				if (tface != null)
					tv.setTypeface(tface);
				tv.setText(m_fontNames.get(position));
			}

			return view;
		}
	}

	/**
	 * Appele quand on clique sur le bouton "Couleur du fond"
	 * 
	 * @param v
	 */
	public void OnClickCouleurFond(View v)
	{
		Intent it = new Intent(this, ColorPickerActivity.class);
		it.putExtra(ColorPickerActivity.TYPE_COULEUR, COULEUR_FOND);
		it.putExtra(ColorPickerActivity.COULEUR, prefs.get_couleurFond());

		startActivityForResult(it, ColorPickerActivity.RESULT_CODE_COULEUR);
	}

	/**
	 * Appele quand on clique sur le bouton "Couleur du texte"
	 * 
	 * @param v
	 */
	public void OnClickCouleurTexte(View v)
	{
		Intent it = new Intent(this, ColorPickerActivity.class);
		it.putExtra(ColorPickerActivity.TYPE_COULEUR, COULEUR_TEXTE);
		it.putExtra(ColorPickerActivity.COULEUR, prefs.get_couleurTexte());

		startActivityForResult(it, ColorPickerActivity.RESULT_CODE_COULEUR);
	}

	/***
	 * Resultat de l'activity "ColorPicker"
	 */
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		switch (requestCode)
		{
		case ColorPickerActivity.RESULT_CODE_COULEUR:
			if (resultCode == RESULT_OK)
				if (data != null)
				{
					String typeCouleur = data.getStringExtra(ColorPickerActivity.TYPE_COULEUR);
					if (COULEUR_FOND.equals(typeCouleur))
					{
						int Couleur = data.getIntExtra(ColorPickerActivity.COULEUR, prefs.get_couleurFond());
						prefs.set_couleurFond(Couleur);
						findViewById(R.id.btnFond).setBackgroundColor(Couleur);
					} else if (COULEUR_TEXTE.equals(typeCouleur))
					{
						int Couleur = data.getIntExtra(ColorPickerActivity.COULEUR, prefs.get_couleurTexte());
						prefs.set_couleurTexte(Couleur);
						findViewById(R.id.btnTexte).setBackgroundColor(Couleur);
					}
				}
			break;
			
		default:
			break;
		}
	}
}
