package com.engineercalc.mohrscircle;

import com.actionbarsherlock.app.SherlockFragment;
import com.engineercalc.elasticity.StrainRosette;
import com.engineercalc.elasticity.Tensor;
import com.engineercalc.elasticity.Tensor.Component;
import com.engineercalc.elasticity.TensorHelper.Dimension;
import com.engineercalc.elasticity.TensorHelper.Type;
import com.engineercalc.mohrscircle.R;
import com.engineercalc.mohrscircle.view.ActionEditText;
import com.engineercalc.mohrscircle.view.UnitSquareView;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TableRow;
import android.widget.TextView;

public class InputFragment extends SherlockFragment {

	private MohrsCircleActivity mActivity;
	private TextView labelSx, labelSy, labelSz, labelSxy, labelSyz, labelSzx;
	private ActionEditText editTextSx, editTextSy, editTextSz, editTextSxy, editTextSyz, editTextSzx;
	private TableRow inputRowSz, inputRowTyz, inputRowTzx;
	private UnitSquareView unitSquare;
	private TextView labelError;

    @Override
    public void onAttach(Activity activity) {
    	super.onAttach(activity);
        
        try {
            mActivity= (MohrsCircleActivity) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " is not a MohrsCircleActivity.");
        }
    }
    
    private class OnEditActionListener extends ActionEditText.OnEditActionListener {
    	private Component c;

		OnEditActionListener(MohrsCircleActivity activity, ActionEditText actionEditText, Component c) {
			actionEditText.super(activity);
    		this.c = c;
		}

		@Override
		public void onEditAction(ActionEditText actionEditText) {
			// Seems like there should be better way to do this
			// This has to match up with the GUI input labels
			Tensor t = mActivity.getTensor();
			double val = actionEditText.getDoubleValue();
			if (t.getType() == Type.STRAINROSETTE) {
				StrainRosette sr = (StrainRosette)t;
				switch (c) {
				case XX:
					sr.setAlpha(val);
					break;
				case YY:
					sr.setBeta(val);
					break;
				case ZZ:
					sr.setGamma(val);
					break;
				case XY:
					sr.setEa(val);
					break;
				case YZ:
					sr.setEb(val);
					break;
				case ZX:
					sr.setEc(val);
					break;
				}
		    	unitSquare.setImageType(t);	// Update rosette image
		    	mActivity.setTensorComponent(Component.XX, sr.getXX());
		    	mActivity.setTensorComponent(Component.YY, sr.getYY());
		    	mActivity.setTensorComponent(Component.XY, sr.getXY());
			} else {
				mActivity.setTensorComponent(c, val);
			}
	    	checkInputError(t);
		}
    }
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    	super.onCreateView(inflater, container, savedInstanceState);
    	
    	int layout = R.layout.input_fragment_phone;
    	if(getResources().getBoolean(R.bool.two_panes))
    		layout = R.layout.input_fragment_tablet;
    	View view =  inflater.inflate(layout, container, false);
    	
    	labelSx = (TextView)view.findViewById(R.id.inputXX_lbl);
    	labelSy = (TextView)view.findViewById(R.id.inputYY_lbl);
    	labelSz = (TextView)view.findViewById(R.id.inputZZ_lbl);
    	labelSxy = (TextView)view.findViewById(R.id.inputXY_lbl);
    	labelSyz = (TextView)view.findViewById(R.id.inputYZ_lbl);
    	labelSzx = (TextView)view.findViewById(R.id.inputZX_lbl);
    	editTextSx = (ActionEditText)view.findViewById(R.id.inputXX_val);
    	editTextSy = (ActionEditText)view.findViewById(R.id.inputYY_val);
    	editTextSz = (ActionEditText)view.findViewById(R.id.inputZZ_val);
    	editTextSxy = (ActionEditText)view.findViewById(R.id.inputXY_val);
    	editTextSyz = (ActionEditText)view.findViewById(R.id.inputYZ_val);
    	editTextSzx = (ActionEditText)view.findViewById(R.id.inputZX_val);
    	inputRowSz = (TableRow)view.findViewById(R.id.inputRowSz);
    	inputRowTyz = (TableRow)view.findViewById(R.id.inputRowTyz);
    	inputRowTzx = (TableRow)view.findViewById(R.id.inputRowTzx);

		editTextSx.setOnEditActionListener(new OnEditActionListener(mActivity, editTextSx, Component.XX));
		editTextSy.setOnEditActionListener(new OnEditActionListener(mActivity, editTextSy, Component.YY));
		editTextSz.setOnEditActionListener(new OnEditActionListener(mActivity, editTextSz, Component.ZZ));
		editTextSxy.setOnEditActionListener(new OnEditActionListener(mActivity, editTextSxy, Component.XY));
		editTextSyz.setOnEditActionListener(new OnEditActionListener(mActivity, editTextSyz, Component.YZ));
		editTextSzx.setOnEditActionListener(new OnEditActionListener(mActivity, editTextSzx, Component.ZX));
    	
		unitSquare = (UnitSquareView)view.findViewById(R.id.unit_square_view);

		labelError = (TextView)view.findViewById(R.id.input_error);
		
    	return view;
    }
    
    @Override
    public void onResume() {
    	super.onResume();

    	updateAllViewsFromTensor(mActivity.getTensor());
    }
    
    public void setTensor(Tensor tensor) {
    	updateAllViewsFromTensor(tensor);
    }
    
    private void updateAllViewsFromTensor(Tensor tensor) {
    	
    	View view =  getView();
    	if (view == null)
    		return;
    	
    	labelError.setVisibility(View.GONE);
    	    	
    	if (tensor.getType() == Type.STRESS) {
        	labelSx.setText(R.string.Sx);
        	labelSy.setText(R.string.Sy);
        	labelSz.setText(R.string.Sz);
        	labelSxy.setText(R.string.Txy);
        	labelSyz.setText(R.string.Tyz);
        	labelSzx.setText(R.string.Tzx);
        	editTextSx.setHint(R.string.Sx);
        	editTextSy.setHint(R.string.Sy);
        	editTextSz.setHint(R.string.Sz);
        	editTextSxy.setHint(R.string.Txy);
        	editTextSyz.setHint(R.string.Tyz);
        	editTextSzx.setHint(R.string.Tzx);
    	} else if (tensor.getType() == Type.STRAIN) {
        	labelSx.setText(R.string.Ex);
        	labelSy.setText(R.string.Ey);
        	labelSz.setText(R.string.Ez);
        	labelSxy.setText(R.string.Gxy);
        	labelSyz.setText(R.string.Gyz);
        	labelSzx.setText(R.string.Gzx);
        	editTextSx.setHint(R.string.Ex);
        	editTextSy.setHint(R.string.Ey);
        	editTextSz.setHint(R.string.Ez);
        	editTextSxy.setHint(R.string.Gxy);
        	editTextSyz.setHint(R.string.Gyz);
        	editTextSzx.setHint(R.string.Gzx);
    	} else if (tensor.getType() == Type.STRAINROSETTE) {
        	labelSx.setText(R.string.alpha);
        	labelSy.setText(R.string.beta);
        	labelSz.setText(R.string.gamma);
        	labelSxy.setText(R.string.Ea);
        	labelSyz.setText(R.string.Eb);
        	labelSzx.setText(R.string.Ec);
        	editTextSx.setHint(R.string.alpha);
        	editTextSy.setHint(R.string.beta);
        	editTextSz.setHint(R.string.gamma);
        	editTextSxy.setHint(R.string.Ea);
        	editTextSyz.setHint(R.string.Eb);
        	editTextSzx.setHint(R.string.Ec);
    	}
    	
    	if (tensor.getType() == Type.STRAINROSETTE) {
    		inputRowSz.setVisibility(View.VISIBLE);
    		inputRowTyz.setVisibility(View.VISIBLE);
    		inputRowTzx.setVisibility(View.VISIBLE);
    		StrainRosette SR = (StrainRosette)tensor;
    		editTextSx.setNumber(SR.getAlpha());
        	editTextSy.setNumber(SR.getBeta());
        	editTextSz.setNumber(SR.getGamma());
        	editTextSxy.setNumber(SR.getEa());
        	editTextSyz.setNumber(SR.getEb());
        	editTextSzx.setNumber(SR.getEc());
    	} else if (tensor.getDimension() == Dimension.DIM3D) {
    		inputRowSz.setVisibility(View.VISIBLE);
    		inputRowTyz.setVisibility(View.VISIBLE);
    		inputRowTzx.setVisibility(View.VISIBLE);
    		editTextSx.setNumber(tensor.getXX());
        	editTextSy.setNumber(tensor.getYY());
        	editTextSz.setNumber(tensor.getZZ());
        	editTextSxy.setNumber(tensor.getXY());
        	editTextSyz.setNumber(tensor.getYZ());
        	editTextSzx.setNumber(tensor.getZX());
    	} else if (tensor.getDimension() == Dimension.DIM2D) {
    		inputRowSz.setVisibility(View.GONE);
    		inputRowTyz.setVisibility(View.GONE);
    		inputRowTzx.setVisibility(View.GONE);
    		editTextSx.setNumber(tensor.getXX());
        	editTextSy.setNumber(tensor.getYY());
        	editTextSxy.setNumber(tensor.getXY());
    	} 
    	
    	unitSquare.setImageType(tensor);
    	checkInputError(tensor);
    }
    
    @Override
	public void onPause() {
    	super.onPause();

    	// Any focused input value is saved when focus is cleared.
    	mActivity.clearFocus();
    }
    
    private void checkInputError(Tensor tensor) {
    	if (tensor.getType() == Type.STRAINROSETTE) {
    		if (!((StrainRosette)tensor).isValid()) {
    			labelError.setVisibility(View.VISIBLE);
    			return;
    		}
    	}
		labelError.setVisibility(View.GONE);
    }
}