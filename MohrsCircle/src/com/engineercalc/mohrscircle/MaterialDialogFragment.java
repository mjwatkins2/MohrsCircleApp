package com.engineercalc.mohrscircle;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockDialogFragment;
import com.engineercalc.elasticity.ElasticMaterial;
import com.engineercalc.elasticity.ElasticMaterial.Constant;
import com.engineercalc.elasticity.ElasticMaterial.InvalidConstantException;
import com.engineercalc.mohrscircle.view.ActionEditText;
import com.engineercalc.mohrscircle.view.FormattedTextView;

public class MaterialDialogFragment extends SherlockDialogFragment implements OnItemSelectedListener {

	private MohrsCircleActivity mActivity;
	private final List<Constant> defaultList = Arrays.asList(Constant.E, Constant.V, Constant.G, Constant.K, Constant.L);
    private EnumMap<Constant, String> constantsMap = new EnumMap<Constant, String>(Constant.class);
	private List<Constant> constant1List = new ArrayList<Constant>(defaultList);
	private List<Constant> constant2List = new ArrayList<Constant>(defaultList);
	private Spinner constant1Spinner, constant2Spinner;
	private ActionEditText constant1Val, constant2Val;
	private FormattedTextView elastE, elastV, elastG, elastK, elastL;
	private TextView elastErr, elastELbl, elastVLbl, elastGLbl, elastKLbl, elastLLbl;
	private ElasticMaterial material = new ElasticMaterial();
	private Constant lastSelectedConstant1 = Constant.E;		// keep this around because onResume() doesn't have a savedInstanceState
	private Constant lastSelectedConstant2 = Constant.V;
	private boolean isDialog = false;
	
	
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		isDialog = true;
		
		AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
		
		final LayoutInflater lf1 = mActivity.getLayoutInflater();
		final View view = lf1.inflate(R.layout.material_fragment, null);
		
		builder.setTitle(R.string.materialtab)
		       .setView(view)
		       .setCancelable(true)
			   .setPositiveButton(R.string.OK, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					mActivity.setMaterial(material);
				}
			});
		
		prepareViews(view, savedInstanceState);
		
		AlertDialog d = builder.create();
		return d;
	}
	
	/**
	 * Note this occurs first, even before onCreateDialog
	 */
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		
		try {
			mActivity = (MohrsCircleActivity)activity;
		} catch (ClassCastException e) {
			throw new ClassCastException(activity.toString() + " is not a MohrsCircleActivity.");
		}
    	
    	constantsMap.put(Constant.E, getString(R.string.elast_E));
    	constantsMap.put(Constant.V, getString(R.string.elast_v));
    	constantsMap.put(Constant.G, getString(R.string.elast_G));
    	constantsMap.put(Constant.K, getString(R.string.elast_K));
    	constantsMap.put(Constant.L, getString(R.string.elast_l));
	}
	

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = super.onCreateView(inflater, container, savedInstanceState);

		if(!isDialog) {
			// This is included so that this class can also function as a regular view.
			// This has not been tested!
			view = inflater.inflate(R.layout.material_fragment, container, false);
			prepareViews(view, savedInstanceState);
		}
		
		return view;
	}
	
	private void prepareViews(View view, Bundle savedInstanceState) {
		
		constant1Spinner = (Spinner) view.findViewById(R.id.constant1_spinner);
		constant2Spinner = (Spinner) view.findViewById(R.id.constant2_spinner);
		
		constant1Val = (ActionEditText) view.findViewById(R.id.constant1_val);
		constant1Val.setOnEditActionListener(constant1Val.new OnEditActionListener(mActivity) {
			@Override
			public void onEditAction(ActionEditText actionEditText) {
				updateElasticConstants();
			}
		});
    	
		constant2Val = (ActionEditText) view.findViewById(R.id.constant2_val);
		constant2Val.setOnEditActionListener(constant2Val.new OnEditActionListener(mActivity) {
			@Override
			public void onEditAction(ActionEditText actionEditText) {
				updateElasticConstants();
			}
		});
		
		elastE = (FormattedTextView) view.findViewById(R.id.elast_E_val);
		elastV = (FormattedTextView) view.findViewById(R.id.elast_v_val);
		elastV.setDecimalFormat("0.00##");
		elastG = (FormattedTextView) view.findViewById(R.id.elast_G_val);
		elastK = (FormattedTextView) view.findViewById(R.id.elast_K_val);
		elastL = (FormattedTextView) view.findViewById(R.id.elast_l_val);
		
		elastErr = (TextView) view.findViewById(R.id.elast_error);
		elastELbl = (TextView) view.findViewById(R.id.elast_E_lbl);
		elastVLbl = (TextView) view.findViewById(R.id.elast_v_lbl);
		elastGLbl = (TextView) view.findViewById(R.id.elast_G_lbl);
		elastKLbl = (TextView) view.findViewById(R.id.elast_K_lbl);
		elastLLbl = (TextView) view.findViewById(R.id.elast_l_lbl);
    	
    	constant1Spinner.setOnItemSelectedListener(this);
    	constant2Spinner.setOnItemSelectedListener(this);
    	
    	if (savedInstanceState != null) {
    		int savedConst1 = savedInstanceState.getInt("const1", 0);
    		int savedConst2 = savedInstanceState.getInt("const2", 1);

    		Constant const1 = defaultList.get(savedConst1);
    		Constant const2 = defaultList.get(savedConst2);
    		
    		lastSelectedConstant1 = const1;
    		lastSelectedConstant2 = const2;
    	}
	}
	
    @Override
    public void onResume() {
    	super.onResume();
    	material = mActivity.getMaterial();
    	forceUpdateSpinnersAndEditors(lastSelectedConstant1, lastSelectedConstant2);
		updateElasticConstants();
		mActivity.clearFocus();
    }
	
	@Override
    public void onSaveInstanceState(Bundle outState) {
    	super.onSaveInstanceState(outState);
    	if (constant1Spinner == null || constant2Spinner == null) return;
    	int const1 = defaultList.lastIndexOf(constant1List.get(constant1Spinner.getSelectedItemPosition()));
    	int const2 = defaultList.lastIndexOf(constant1List.get(constant1Spinner.getSelectedItemPosition()));
    	outState.putInt("const1", const1);
    	outState.putInt("const2", const2);
    }
    
	@Override
	public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {

		Constant const1 = constant1List.get(constant1Spinner.getSelectedItemPosition());
		Constant const2 = constant2List.get(constant2Spinner.getSelectedItemPosition());
		
		switch (parent.getId()) {
			case R.id.constant1_spinner:
				// update spinner 2 by removing const1
				updateSpinner(constant2Spinner, constant2List, const1, const2);
				updateEditText(constant1Val, const1);
				lastSelectedConstant1 = const1;
				break;
			case R.id.constant2_spinner:
				// update spinner 1 by removing const2
				updateSpinner(constant1Spinner, constant1List, const2, const1);
				updateEditText(constant2Val, const2);
				lastSelectedConstant2 = const2;
				break;
		}
		updateElasticConstants();
	}

	@Override
	public void onNothingSelected(AdapterView<?> arg0) { }
	
	
	/**
	 * Updates the selection items for both spinners and the corresponding EditTexts
	 */
	private void forceUpdateSpinnersAndEditors(Constant const1, Constant const2) {
		if (const1 == const2) {
			int const1DefaultList = defaultList.lastIndexOf(const1);
			int const2DefaultList = const1DefaultList+1;
			if (const2DefaultList >= defaultList.size())
				const2DefaultList = 0;
			const2 = defaultList.get(const2DefaultList);
		}

		forceUpdateSpinner(constant1Spinner, constant1List, const2, const1);
		updateEditText(constant1Val, const1);
		forceUpdateSpinner(constant2Spinner, constant2List, const1, const2);
		updateEditText(constant2Val, const2);
	}
	
	/**
	 * Only updates the spinner selection items if the item to remove is in the list of spinner items (prevents infinite loops)
	 */
	private void updateSpinner(Spinner spinner, List<Constant> constantList, Constant toRemove, Constant currentSelection) {
		if (constantList.contains(toRemove)) {
			forceUpdateSpinner(spinner, constantList, toRemove, currentSelection);
		}
	}
	
	/**
	 * Updates the spinner selection options
	 */
	private void forceUpdateSpinner(Spinner spinner, List<Constant> constantList, Constant toRemove, Constant currentSelection) {
		
		// Remove one item from the default list (whatever has been picked in the other spinner)
		// and build the list of string spinner options
		List<String> stringList = new ArrayList<String>();
		constantList.clear();
		for (Constant c : defaultList) {
			if (c != toRemove) {
				constantList.add(c);
				stringList.add(constantsMap.get(c));
			}
		}
		
		// Send the spinner options to the spinner
    	ArrayAdapter<String> adapter = new ArrayAdapter<String>(mActivity, android.R.layout.simple_spinner_item, stringList);
    	adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
    	spinner.setAdapter(adapter);
    	adapter.notifyDataSetChanged();
    	
    	// Reset spinner selection to the previously selected value
    	spinner.setSelection(constantList.lastIndexOf(currentSelection));
	}
	
	private void updateEditText(ActionEditText editor, Constant constant) {
		editor.setNumber(material.getConstant(constant));
	}
	
	private void updateElasticConstants() {
		int pos1 = constant1Spinner.getSelectedItemPosition();
		int pos2 = constant2Spinner.getSelectedItemPosition();
		Constant const1Selection = constant1List.get(pos1);
		Constant const2Selection = constant2List.get(pos2);

		double const1InputVal = constant1Val.getDoubleValue();
		double const2InputVal = constant2Val.getDoubleValue();
    	
		try {
			material.setConstants(const1Selection, const1InputVal, const2Selection, const2InputVal);
			
			elastE.setNumber(material.getE());
			elastV.setNumber(material.getV());
			elastG.setNumber(material.getG());
			elastK.setNumber(material.getK());
			elastL.setNumber(material.getL());
			
			elastE.setTextColor(Color.BLACK);
			elastV.setTextColor(Color.BLACK);
			elastG.setTextColor(Color.BLACK);
			elastK.setTextColor(Color.BLACK);
			elastL.setTextColor(Color.BLACK);
			elastELbl.setTextColor(Color.BLACK);
			elastVLbl.setTextColor(Color.BLACK);
			elastGLbl.setTextColor(Color.BLACK);
			elastKLbl.setTextColor(Color.BLACK);
			elastLLbl.setTextColor(Color.BLACK);
			
			elastErr.setText("");
			elastErr.setVisibility(View.GONE);
			
		} catch (InvalidConstantException e) {
			
			elastE.setTextColor(Color.LTGRAY);
			elastV.setTextColor(Color.LTGRAY);
			elastG.setTextColor(Color.LTGRAY);
			elastK.setTextColor(Color.LTGRAY);
			elastL.setTextColor(Color.LTGRAY);
			elastELbl.setTextColor(Color.LTGRAY);
			elastVLbl.setTextColor(Color.LTGRAY);
			elastGLbl.setTextColor(Color.LTGRAY);
			elastKLbl.setTextColor(Color.LTGRAY);
			elastLLbl.setTextColor(Color.LTGRAY);

			elastErr.setVisibility(View.VISIBLE);
			String err = e.getMessage();
			elastErr.setText(err);
		}
		
	}
}
