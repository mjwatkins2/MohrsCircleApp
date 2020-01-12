package com.engineercalc.mohrscircle;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockDialogFragment;
import com.engineercalc.elasticity.Tensor;
import com.engineercalc.elasticity.Tensor2D;
import com.engineercalc.elasticity.Tensor3D;
import com.engineercalc.mohrscircle.view.FormattedTextView;

public class PropertiesDialogFragment extends SherlockDialogFragment {

	private MohrsCircleActivity mActivity;
	private TextView S1Lbl, S2Lbl, S3Lbl, I1Lbl, I2Lbl, I3Lbl, SeqLbl, TmaxLbl, n1Lbl, n2Lbl, n3Lbl, invariantsLbl;
	private FormattedTextView S1Val, S2Val, S3Val, I1Val, I2Val, I3Val, SeqVal, TmaxVal, n1Val, n2Val, n3Val;
	private View S3Row, I1Row, I2Row, I3Row;
	private boolean isDialog = false;
	
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		isDialog = true;
		
		AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
		
		final LayoutInflater lf1 = mActivity.getLayoutInflater();
		final View view = lf1.inflate(R.layout.properties_fragment, null);
		
		builder.setTitle(R.string.propertiestab)
		       .setView(view)
		       .setCancelable(true)
			   .setPositiveButton(R.string.OK, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					// Nothing to do for OK button
				}
			});
		
		prepareViews(view, savedInstanceState);
		
		AlertDialog d = builder.create();
		
		
		return d;
	}
	
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);

		try {
			mActivity = (MohrsCircleActivity)activity;
		} catch (ClassCastException e) {
			throw new ClassCastException(activity.toString() + " is not a MohrsCircleActivity.");
		}
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
	
		View view = super.onCreateView(inflater, container, savedInstanceState);

		if(!isDialog) {
			// This is included so that this class can also function as a regular view.
			// This has not been tested!
			view = inflater.inflate(R.layout.properties_fragment, container, false);
			prepareViews(view, savedInstanceState);
		}
    	
		return view;
	}

	private void prepareViews(View view, Bundle savedInstanceState) {

    	S1Lbl = (TextView)view.findViewById(R.id.S1_lbl);
    	S1Val = (FormattedTextView)view.findViewById(R.id.S1_val);
    	
    	S2Lbl = (TextView)view.findViewById(R.id.S2_lbl);
    	S2Val = (FormattedTextView)view.findViewById(R.id.S2_val);

		S3Row = view.findViewById(R.id.S3_row);
    	S3Lbl = (TextView)view.findViewById(R.id.S3_lbl);
    	S3Val = (FormattedTextView)view.findViewById(R.id.S3_val);

    	n1Lbl = (TextView)view.findViewById(R.id.n1_lbl);
    	n1Val = (FormattedTextView)view.findViewById(R.id.n1_val);
    	n1Val.setDecimalFormat("0.00");
    	
    	n2Lbl = (TextView)view.findViewById(R.id.n2_lbl);
    	n2Val = (FormattedTextView)view.findViewById(R.id.n2_val);
    	n2Val.setDecimalFormat("0.00");
    	
    	n3Lbl = (TextView)view.findViewById(R.id.n3_lbl);
    	n3Val = (FormattedTextView)view.findViewById(R.id.n3_val);
    	n3Val.setDecimalFormat("0.00");
    	
    	SeqLbl = (TextView)view.findViewById(R.id.Seq_lbl);
    	SeqVal = (FormattedTextView)view.findViewById(R.id.Seq_val);
    	
    	TmaxLbl = (TextView)view.findViewById(R.id.Tmax_lbl);
    	TmaxVal = (FormattedTextView)view.findViewById(R.id.Tmax_val);
    	
    	invariantsLbl = (TextView)view.findViewById(R.id.invariants_lbl);
    	
    	I1Row = view.findViewById(R.id.I1_row);
    	I1Lbl = (TextView)view.findViewById(R.id.I1_lbl);
    	I1Val = (FormattedTextView)view.findViewById(R.id.I1_val);

    	I2Row = view.findViewById(R.id.I2_row);
    	I2Lbl = (TextView)view.findViewById(R.id.I2_lbl);
    	I2Val = (FormattedTextView)view.findViewById(R.id.I2_val);

    	I3Row = view.findViewById(R.id.I3_row);
    	I3Lbl = (TextView)view.findViewById(R.id.I3_lbl);
    	I3Val = (FormattedTextView)view.findViewById(R.id.I3_val);
    	
	}
	
    @Override
    public void onResume() {
    	super.onResume();
    	
    	update(mActivity.getTensor());
    }
    
    
    public void update(Tensor tensor) {
    	
    	final String equals = " = ";

    	I1Lbl.setText(getString(R.string.I1) + equals);
    	I2Lbl.setText(getString(R.string.I2) + equals);
    	I3Lbl.setText(getString(R.string.I3) + equals);
    	n1Lbl.setText(getString(R.string.n1) + equals);
    	n2Lbl.setText(getString(R.string.n2) + equals);
    	
    	switch (tensor.getType()) {
	    	case STRESS:
	        	S1Lbl.setText(getString(R.string.S1) + equals);
	        	S2Lbl.setText(getString(R.string.S2) + equals);
	        	S3Lbl.setText(getString(R.string.S3) + equals);
	        	SeqLbl.setText(getString(R.string.Seq) + equals);
	        	TmaxLbl.setText(getString(R.string.Tmax) + equals);
	    		break;
	    	case STRAIN:
	    	case STRAINROSETTE:
	        	S1Lbl.setText(getString(R.string.E1) + equals);
	        	S2Lbl.setText(getString(R.string.E2) + equals);
	        	S3Lbl.setText(getString(R.string.E3) + equals);
	        	SeqLbl.setText(getString(R.string.Eeq) + equals);
	        	TmaxLbl.setText(getString(R.string.Gmax) + equals);
	    		break;
    	}
    	
    	switch (tensor.getDimension()) {
	    	case DIM2D:
	    		S3Row.setVisibility(View.GONE);
	    		invariantsLbl.setVisibility(View.GONE);
	    		I1Row.setVisibility(View.GONE);
	    		I2Row.setVisibility(View.GONE);
	    		I3Row.setVisibility(View.GONE);
	    		
	    		Tensor2D tensor2D = (Tensor2D) tensor;
	    		S1Val.setNumber(tensor2D.getFirstPrincipal());
	    		S2Val.setNumber(tensor2D.getSecondPrincipal());
	    		SeqVal.setNumber(tensor2D.getVonMises());
	    		TmaxVal.setNumber(tensor2D.getMaxShear());

	    		n3Lbl.setText(getString(R.string.thetap) + equals);
	    		
	    		n1Val.setVector(tensor2D.getFirstPrincipalDirection());
	    		n2Val.setVector(tensor2D.getSecondPrincipalDirection());
	    		n3Val.setNumber(tensor2D.getPrincipalAngle2D());
	    		break;
	    	case DIM3D:
	    		S3Row.setVisibility(View.VISIBLE);
	    		invariantsLbl.setVisibility(View.VISIBLE);
	    		I1Row.setVisibility(View.VISIBLE);
	    		I2Row.setVisibility(View.VISIBLE);
	    		I3Row.setVisibility(View.VISIBLE);
	    		
	    		Tensor3D tensor3D = (Tensor3D) tensor;
	    		S1Val.setNumber(tensor3D.getFirstPrincipal());
	    		S2Val.setNumber(tensor3D.getSecondPrincipal());
	    		S3Val.setNumber(tensor3D.getThirdPrincipal());
	    		SeqVal.setNumber(tensor3D.getVonMises());
	    		TmaxVal.setNumber(tensor3D.getMaxShear());
	    		
	    		n3Lbl.setText(getString(R.string.n3) + equals);
	    		
	    		n1Val.setVector(tensor3D.getFirstPrincipalDirection());
	    		n2Val.setVector(tensor3D.getSecondPrincipalDirection());
	    		n3Val.setVector(tensor3D.getThirdPrincipalDirection());
	    		
	    		I1Val.setNumber(tensor3D.getFirstInvariant());
	    		I2Val.setNumber(tensor3D.getSecondInvariant());
	    		I3Val.setNumber(tensor3D.getThirdInvariant());
	    		
	    		break;
    	}
    }
}
