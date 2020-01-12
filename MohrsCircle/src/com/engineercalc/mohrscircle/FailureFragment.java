package com.engineercalc.mohrscircle;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.actionbarsherlock.app.SherlockFragment;

public class FailureFragment extends SherlockFragment {

	
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);
		
		View view = inflater.inflate(/*R.layout.failure_fragment*/1, container, false);
		
		
		return view;
	}

    @Override
    public void onResume() {
    	super.onResume();
    	
    	// update properties
    }
}
