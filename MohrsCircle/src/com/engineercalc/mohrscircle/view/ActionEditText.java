package com.engineercalc.mohrscircle.view;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;

import com.engineercalc.mohrscircle.MohrsCircleActivity;

import android.content.Context;
import android.content.res.Configuration;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;


/**
 * An extension of EditText that performs some action when either the focus is lost
 * or the ACTION_DONE button is pressed. Designed for formatted numeric input.
 *
 */
public class ActionEditText extends EditText {

	private NumberFormat displayFormat;
	private NumberFormat editFormatLTone;
	private NumberFormat editFormatGTone;
	char decimalSeparator;
	private String textBeforeEditing = "";
	private double mValue = 0.0;
	private boolean isFocused = false;
	
	private MohrsCircleActivity mActivity;
	private OnEditActionListener onEditActionListener = null;

	public ActionEditText(Context context) {
		super(context);
		init();
	}

	public ActionEditText(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public ActionEditText(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}
	
	private void init() {
		/**
		 * When editing very small numbers, the edit pattern can be identical to "0", so during a string before/after
		 * comparison the ActionEditText won't register user typing "0" as a change to actual zero. So need two 
		 * patterns for editing.
		 */
		final String displayPattern = "0.0###E+0";
		final String editPatternGTone = "0.#####";
		final String editPatternLTone = "0.########";
		if (!isInEditMode()) {
			displayFormat = NumberFormat.getInstance();
			if (displayFormat instanceof DecimalFormat) {
				((DecimalFormat)displayFormat).applyPattern(displayPattern);
			} else {
				// This should never happen
				Log.w("ActionEditText", "NumberFormat is not DecimalFormat");
				displayFormat = new DecimalFormat(displayPattern);
			}
			editFormatLTone = NumberFormat.getInstance();
			editFormatGTone = NumberFormat.getInstance();
			if (editFormatGTone instanceof DecimalFormat) {
				((DecimalFormat)editFormatLTone).applyPattern(editPatternLTone);
				((DecimalFormat)editFormatGTone).applyPattern(editPatternGTone);
			} else {
				// This should never happen
				Log.w("ActionEditText", "NumberFormat is not DecimalFormat");
				editFormatLTone = new DecimalFormat(editPatternLTone);
				editFormatGTone = new DecimalFormat(editPatternGTone);
			}
		}
		
		if (isInEditMode()) {
			decimalSeparator = '.';
		} else {
			decimalSeparator = ((DecimalFormat)editFormatGTone).getDecimalFormatSymbols().getDecimalSeparator();
		}
		
		setOnFocusChangeListener(new OnFocusChangeListener() {
			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				if (hasFocus) {
					doSelect();
				} else {
					doUnselect();
				}
			}
		});
		
		setOnEditorActionListener(new OnEditorActionListener() {
			@Override
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
	            if (actionId == EditorInfo.IME_ACTION_DONE) {
	            	mActivity.hideKeyboard();
	            	doUnselect();	// For some reason, the text is NOT unselected in this pop up, so the focuschangelistener isn't hit.
	            }
				return false;
			}
		});
		
	}
	
	public abstract class OnEditActionListener {
		public OnEditActionListener(MohrsCircleActivity activity) {
			mActivity = activity;
		}
		
		public abstract void onEditAction(ActionEditText actionEditText);
	}
	
	public void setOnEditActionListener(OnEditActionListener listener) {
		onEditActionListener = listener;
	}
	
	private void doSelect() {
		if (Math.abs(mValue) > 1) {
			textBeforeEditing = editFormatGTone.format(mValue);
		} else {
			textBeforeEditing = editFormatLTone.format(mValue);
		}
		setText(textBeforeEditing);
		setSelection(textBeforeEditing.length());
		final boolean portrait = getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT;
		if (portrait)
			selectAll();
		
		isFocused = true;
	}
	
	private void doUnselect() {
		if (isFocused) {
			isFocused = false;
		} else {
			return;
		}
		
		if (!getText().toString().equalsIgnoreCase(textBeforeEditing)) {
			mValue = textToDouble();
		}
		setText(displayFormat.format(mValue));
		doAction();
	}
	
	private void doAction() {
		if (onEditActionListener != null)
			onEditActionListener.onEditAction(this);
	}
	
	private double textToDouble() {
    	String S = getText().toString();
    	// Allow both '.' and ',' to be the decimal separator
    	S = S.replace(',', decimalSeparator);	// TODO see if this is fixed yet and then (1) remove digits tag from view (2) remove '.' ',' replacement
    	S = S.replace('.', decimalSeparator);
		double val;
		try {
			if (Math.abs(mValue) > 1) {
				val = editFormatGTone.parse(S).doubleValue();
			} else {
				val = editFormatLTone.parse(S).doubleValue();
			}
		} catch (ParseException e) {
			val = 0.0;
			setNumber(val);
		}
		return val;
	}
	
	public double getDoubleValue() {
		return mValue;
	}
	
	public void setNumber(double value) {
		if (Math.abs(value) < 1e-10) {
			value = 0.0;
		}
		mValue = value;
		setText(displayFormat.format(value));
	}
}
