package com.engineercalc.mohrscircle;

import com.engineercalc.mohrscircle.R;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.MenuInflater;
import com.engineercalc.elasticity.ElasticMaterial;
import com.engineercalc.elasticity.ElasticMaterial.Constant;
import com.engineercalc.elasticity.ElasticMaterial.InvalidConstantException;
import com.engineercalc.elasticity.StressTensor2D;
import com.engineercalc.elasticity.StressTensor3D;
import com.engineercalc.elasticity.Tensor;
import com.engineercalc.elasticity.Tensor.Component;
import com.engineercalc.elasticity.TensorHelper.Dimension;
import com.engineercalc.elasticity.TensorHelper.Type;
import com.engineercalc.elasticity.TensorHelper;

import android.support.v4.app.FragmentTransaction;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

public class MohrsCircleActivity extends SherlockFragmentActivity {
	
	private Tensor tensor = new StressTensor3D();
	private ElasticMaterial material = new ElasticMaterial();
	private boolean tablet = false;
	private boolean suppressDimSwitchMessageBar = false;
	private static final int VERSION = 4;
	private boolean firstRun = false;
	private MohrsCircleInterface interf;
	private final boolean isFreeVersion = false;

	
	protected interface MohrsCircleInterface {
		void onCreate();
		void pushInputChange();
		void addPrefsToEditorForSaving(SharedPreferences.Editor editor);
		void loadSavedPrefs(SharedPreferences settings);
	}
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        tablet = getResources().getBoolean(R.bool.two_panes);
        
        final ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setDisplayShowHomeEnabled(true);
        
        if (tablet) {
        	interf = new MohrsCircleTablet(this);
        } else {
        	interf = new MohrsCircleTabs(this);
        }
        
        interf.onCreate();
        
        loadPrefs();
        
        pushInputChange();

    	if (firstRun && !tablet) {
			FragmentTransaction FT = getSupportFragmentManager().beginTransaction();
			MessageBarFragment MBF = new MessageBarFragment();
			MBF.setOptions(R.string.SlideRight).setHideDelay(10000);
			FT.replace(R.id.messagebar, MBF).setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
			FT.commit();
    	}
    	
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
    	super.onCreateOptionsMenu(menu);
    	MenuInflater inflater = getSupportMenuInflater();
        inflater.inflate(R.menu.actionbar, menu);
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
    	super.onOptionsItemSelected(item);

    	// If an input is focused when a MenuItem is selected, it doesn't lose focus so any changes are lost.
    	// Fix this by hiding the keyboard, which also forces a clearfocus.
    	hideKeyboard();
    	
    	boolean showDimSwitchMessage = false;
    	
    	switch (item.getItemId()) {
	    	case R.id.menu_2D:
	    		tensor = tensor.convertTo2D();
	    		break;
	    	case R.id.menu_3D: 
	    		if (isFreeVersion) {
	    			advertisePaidVersion();
	    		} else {
	    			tensor = tensor.convertTo3D();
	    		}
	    		break;
	    	case R.id.menu_stress:
	    		showDimSwitchMessage = (tensor.getType() == Type.STRAIN) && (tensor.getDimension() == Dimension.DIM2D);
    			tensor = tensor.convertToStress(material);
	    		break;
	    	case R.id.menu_strain:
	    		if (isFreeVersion) {
	    			advertisePaidVersion();
	    		} else {
	    			showDimSwitchMessage = (tensor.getType() == Type.STRESS) && (tensor.getDimension() == Dimension.DIM2D);
	    			tensor = tensor.convertToStrain(material);
	    		}
	    		break;
	    	case R.id.menu_rosette:
	    		if (isFreeVersion) {
	    			advertisePaidVersion();
	    		} else {
	    			tensor = tensor.convertToRosette(material);
	    		}
	    		break;
	    	case R.id.menu_mat:
	    		MaterialDialogFragment md = new MaterialDialogFragment();
	    		md.show(getSupportFragmentManager(), "materialdialog");
	    		break;
	    	default:
	    		return false;
    	}

    	if (!suppressDimSwitchMessageBar && showDimSwitchMessage) {
			FragmentTransaction FT = getSupportFragmentManager().beginTransaction();
			MessageBarFragment MBF = new MessageBarFragment();
			MBF.setOptions(R.string.DimensionSwitchTitle, R.string.DimensionSwitchMsg).setHideDelay(6000);
			FT.replace(R.id.messagebar, MBF).setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
			FT.commit();
    	}
    	
    	pushInputChange();
    	
    	return false;
    }
    
    private void advertisePaidVersion() {
    	Toast.makeText(this, "Feature available in paid version",  Toast.LENGTH_SHORT).show();

		FragmentTransaction FT = getSupportFragmentManager().beginTransaction();
		MessageBarFragment MBF = new MessageBarFragment();
		MBF.setOptions(R.string.BuyPaidTitle, true).setHideDelay(6000);
		FT.replace(R.id.messagebar, MBF).setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
		FT.commit();
    }
	
	private void pushInputChange() {
		interf.pushInputChange();
	}
	
	public Tensor getTensor() {
		return tensor;
	}
	
	public void setTensorComponent(Component c, double val) {
		tensor.setComponent(c, val);
		pushInputChange();
	}
	
	public void setMaterial(ElasticMaterial mat) {
		material = mat;
		pushInputChange();
	}
	
	public ElasticMaterial getMaterial() {
		return material;
	}
	
	/*
	 * Used by MessageBarFragment to control whether a given message is shown, or suppressed (no longer shown).
	 */
	public void setSuppressMessageBar(int messageId, boolean suppress) {
		if (messageId == R.string.DimensionSwitchTitle) {
			suppressDimSwitchMessageBar = suppress;
		}
	}
	
	public void clearFocus() {
    	View focused = findViewById(R.id.main).findFocus();
    	if (focused != null)
    		focused.clearFocus();
	}
	
	/**
	 * Clears current focus and hides the soft keyboard
	 */
	public void hideKeyboard() {
		clearFocus();
    	InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
    	imm.hideSoftInputFromWindow(findViewById(R.id.main).getApplicationWindowToken(), 0);
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		savePrefs();
	}
	
	@Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        savePrefs();
	}

	private static final String PREFS = "MohrsCirclePrefs",
			TENSORLEN = "tensorlen", TENSOR = "tensor",
			MATE = "matE", MATV = "matV",
			SUPPRESSDIMSWITCHMESSAGEBAR = "suppressDimSwitchMessageBar",
			CURRENTVER = "currentVersion";
	
	private void savePrefs() {
		SharedPreferences.Editor editor = getSharedPreferences(PREFS, MODE_PRIVATE).edit();

		interf.addPrefsToEditorForSaving(editor);

		final double[] arr = TensorHelper.getSaveData(tensor);
		editor.putInt(TENSORLEN, arr.length);
		for (int i = 0; i < arr.length; i++) {
			editor.putFloat(TENSOR + i, (float)arr[i]);
		}
		editor.putFloat(MATE, (float)material.getE());
		editor.putFloat(MATV, (float)material.getV());
		editor.putBoolean(SUPPRESSDIMSWITCHMESSAGEBAR, suppressDimSwitchMessageBar);
		editor.putInt(CURRENTVER, VERSION);
		editor.commit();
	}
	
	private void loadPrefs() {
        SharedPreferences settings = getSharedPreferences(PREFS, MODE_PRIVATE);
        
        interf.loadSavedPrefs(settings);
        
        final int arrlen = settings.getInt(TENSORLEN, -1);
        if (arrlen != -1) {
        	final double arr[] = new double[arrlen];
    		for (int i = 0; i < arrlen; i++) {
                arr[i] = settings.getFloat(TENSOR + i, 0.0f);
    		}
    		tensor = TensorHelper.tensorFromSaveData(arr);
    		if (tensor == null) {
    			tensor = new StressTensor2D();
    		}
        }
        
        final double E = settings.getFloat(MATE, 10000000f);
        final double v = settings.getFloat(MATV, 0.3f);
        try {
			material.setConstants(Constant.E, E, Constant.V, v);
		} catch (InvalidConstantException e) {
		}
        suppressDimSwitchMessageBar = settings.getBoolean(SUPPRESSDIMSWITCHMESSAGEBAR, false);
        
        final int lastOpenedVersion = settings.getInt(CURRENTVER, -1);
        if (lastOpenedVersion == -1) {
        	firstRun = true;
        }
        if (lastOpenedVersion != VERSION) {
        	//showReleaseNotes = true;
        }

        
        if (isFreeVersion) {
        	tensor = tensor.convertTo2D().convertToStress(material);
        }
	}
}