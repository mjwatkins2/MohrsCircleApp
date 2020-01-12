package com.engineercalc.mohrscircle.view;

import java.text.DecimalFormat;
import java.text.NumberFormat;

import com.engineercalc.mohrscircle.R;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.TextView;

public class FormattedTextView extends TextView {

	private NumberFormat formatter;

	public FormattedTextView(Context context, AttributeSet attrs) {
		super(context, attrs);
		getAttrs(context, attrs);
	}

	public FormattedTextView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		getAttrs(context, attrs);
	}

	private void getAttrs(Context context, AttributeSet attrs) {
		TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.FormattedTextView, 0, 0);
		
		String format;
		try {
			format = a.getString(R.styleable.FormattedTextView_decimalFormat);
		} finally {
			a.recycle();
		}
		
		if (format != null && !isInEditMode()) {
			setDecimalFormat(format);
		}
	}
	
	public void setNumber(double value) {
		if (Math.abs(value) < 1e-10) {
			value = 0.0;
		}
		
		if (formatter != null) {
			setText(formatter.format(value));
		} else {
			setText(String.valueOf(value));
		}
	}
	
	public void setVector(double[] vals) {
		String txt = "[";
		
		for (int i = 0; i < vals.length; i++) {
			if (formatter != null) {
				txt = txt + formatter.format(vals[i]);
			} else {
				txt = txt + String.valueOf(vals[i]);
			}
			if (i < vals.length - 1) {
				txt = txt + "; ";
			}
		}
		txt = txt + "]";
		
		setText(txt);
	}
	
	public void setDecimalFormat(String format) {
		formatter = NumberFormat.getInstance();
		if (formatter instanceof DecimalFormat) {
			((DecimalFormat)formatter).applyPattern(format);
		} else {
			// This should never happen
			Log.w("FormattedTextView", "NumberFormat is not DecimalFormat");
			formatter = new DecimalFormat(format);
		}
	}
}
