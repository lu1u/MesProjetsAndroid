/**
 * 
 */
package com.lpi.simpleclock;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.lpi.simpleclock.colorpicker.ColorPicker;
import com.lpi.simpleclock.colorpicker.OpacityBar;
import com.lpi.simpleclock.colorpicker.SaturationBar;
import com.lpi.simpleclock.colorpicker.ValueBar;

/**
 * @author lucien
 * 
 */
public class ColorPickerActivity extends Activity
{

	public static final String COULEUR = "couleur";
	static final int RESULT_CODE_COULEUR = 1;
	public static final String TYPE_COULEUR = "typecouleur";
	private String _typeCouleur = "inconnu" ;

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		setResult(RESULT_CANCELED);
		// Inflate our UI from its XML layout description.
		setContentView(R.layout.colorpicker);

		ColorPicker picker = (ColorPicker) findViewById(R.id.colorPicker);
		OpacityBar opacityBar = (OpacityBar) findViewById(R.id.opacityBar);
		SaturationBar saturationBar = (SaturationBar) findViewById(R.id.saturationBar);
		ValueBar valueBar = (ValueBar) findViewById(R.id.valueBar);

		picker.addOpacityBar(opacityBar);
		picker.addSaturationBar(saturationBar);
		picker.addValueBar(valueBar);

		Bundle b = getIntent().getExtras() ;
		if (b != null)
		{
			int Couleur = b.getInt(COULEUR);
			picker.setColor(Couleur);
			_typeCouleur = b.getString(TYPE_COULEUR);
		}
	}

	public void OnOK(View v)
	{
		ColorPicker picker = (ColorPicker) findViewById(R.id.colorPicker);
		Intent resultValue = new Intent();
		
		resultValue.putExtra(TYPE_COULEUR, _typeCouleur);
		resultValue.putExtra(COULEUR, picker.getColor());
		setResult(RESULT_OK, resultValue);
		finish();
	}

	public void OnAnnuler(View v)
	{
		setResult(RESULT_CANCELED, new Intent());
		finish();
	}
}
