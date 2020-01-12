package com.engineercalc.mohrscircle;

import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.support.v4.app.FragmentManager;
import android.view.View;
import android.widget.Button;

import com.engineercalc.elasticity.Tensor;
import com.engineercalc.mohrscircle.MohrsCircleActivity.MohrsCircleInterface;

public class MohrsCircleTablet implements MohrsCircleInterface {
	
	private MohrsCircleActivity mActivity;
	
	public MohrsCircleTablet(MohrsCircleActivity activity) {
		mActivity = activity;
	}

	@Override
	public void onCreate() {
		
		mActivity.setContentView(R.layout.main_tablet);
		
		Button moreButton = (Button)mActivity.findViewById(R.id.btn_more);
		moreButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				PropertiesDialogFragment propDialog = new PropertiesDialogFragment();
				propDialog.show(mActivity.getSupportFragmentManager(), "propertiesdialog");
			}
		});
	}

	@Override
	public void pushInputChange() {
		
		final FragmentManager FM = mActivity.getSupportFragmentManager();
		
		final InputFragment inputFragment = (InputFragment)FM.findFragmentById(R.id.input_fragment);
		final PlotFragment plotFragment = (PlotFragment)FM.findFragmentById(R.id.plot_fragment);
		
		final Tensor tensor = mActivity.getTensor();
		inputFragment.setTensor(tensor);
		plotFragment.update(tensor);
	}

	@Override
	public void addPrefsToEditorForSaving(Editor editor) {
	}

	@Override
	public void loadSavedPrefs(SharedPreferences settings) {
	}

}
