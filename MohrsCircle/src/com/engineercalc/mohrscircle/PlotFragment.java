package com.engineercalc.mohrscircle;

import com.actionbarsherlock.app.SherlockFragment;
import com.engineercalc.elasticity.Tensor;
import com.engineercalc.elasticity.Tensor2D;
import com.engineercalc.elasticity.Tensor3D;
import com.engineercalc.elasticity.TensorHelper.Dimension;
import com.engineercalc.elasticity.TensorHelper.Type;
import com.engineercalc.mohrscircle.R;
import com.engineercalc.mohrscircle.view.FormattedTextView;
import com.engineercalc.mohrscircle.view.PlotView;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class PlotFragment extends SherlockFragment {

	private MohrsCircleActivity mActivity;
	private TextView labelS1, labelS2, labelS3, labelTmax;
	private FormattedTextView textS1Val, textS2Val, textS3Val, textTmaxVal;
	
    @Override
    public void onAttach(Activity activity) {
    	super.onAttach(activity);
        
        try {
            mActivity = (MohrsCircleActivity) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " is not a MohrsCircleActivity.");
        }
    }
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    	super.onCreateView(inflater, container, savedInstanceState);
    	
    	int layout = R.layout.plot_fragment_phone;
    	if(getResources().getBoolean(R.bool.two_panes))
    		layout = R.layout.plot_fragment_tablet;
    	View view = inflater.inflate(layout, container, false);

    	labelS1 = ((TextView)view.findViewById(R.id.S1_lbl));
    	labelS2 = ((TextView)view.findViewById(R.id.S2_lbl));
    	labelS3 = ((TextView)view.findViewById(R.id.S3_lbl));
    	labelTmax = ((TextView)view.findViewById(R.id.Tmax_lbl));
    	textS1Val = ((FormattedTextView)view.findViewById(R.id.S1_val));
    	textS2Val = ((FormattedTextView)view.findViewById(R.id.S2_val));
    	textS3Val = ((FormattedTextView)view.findViewById(R.id.S3_val));
    	textTmaxVal = ((FormattedTextView)view.findViewById(R.id.Tmax_val));
    	
        return view;
    }
    
    @Override
    public void onResume() {
    	super.onResume();
    	
    	update(mActivity.getTensor());
    }
    
    public void update(Tensor tensor) {    	
    	View view = getView();
    	if (view == null)
    		return;
    	
    	final String equals = " = ";
    	
    	if (tensor.getType() == Type.STRESS) {
    		labelS1.setText(getString(R.string.S1) + equals);
    		labelS2.setText(getString(R.string.S2) + equals);
    		labelS3.setText(getString(R.string.S3) + equals);
    		labelTmax.setText(getString(R.string.Tmax) + equals);
    	} else {
    		labelS1.setText(getString(R.string.E1) + equals);
    		labelS2.setText(getString(R.string.E2) + equals);
    		labelS3.setText(getString(R.string.E3) + equals);
    		labelTmax.setText(getString(R.string.Gmax) + equals);
    	}
    	
    	if (tensor.getDimension() == Dimension.DIM2D) {
    		labelS3.setText(getString(R.string.thetap) + equals);
    		
    		Tensor2D tensor2D = (Tensor2D) tensor;
        	textS1Val.setNumber(tensor2D.getFirstPrincipal());
        	textS2Val.setNumber(tensor2D.getSecondPrincipal());
        	textTmaxVal.setNumber(tensor2D.getMaxShear());
    		textS3Val.setNumber(tensor2D.getPrincipalAngle2D());
    		
        	((PlotView)view.findViewById(R.id.plot_view)).setTensorData(tensor2D);
    	} else {
    		Tensor3D tensor3D = (Tensor3D) tensor;
        	textS1Val.setNumber(tensor3D.getFirstPrincipal());
        	textS2Val.setNumber(tensor3D.getSecondPrincipal());
        	textS3Val.setNumber(tensor3D.getThirdPrincipal());
        	textTmaxVal.setNumber(tensor3D.getMaxShear());
        	
        	((PlotView)view.findViewById(R.id.plot_view)).setTensorData(tensor3D);
    	}
    }
}