package com.engineercalc.mohrscircle.view;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.util.AttributeSet;
import android.util.FloatMath;
import android.view.View;

import com.engineercalc.elasticity.Tensor;
import com.engineercalc.mohrscircle.view.ScaledCanvas.HALIGN;
import com.engineercalc.mohrscircle.view.ScaledCanvas.VALIGN;
import com.engineercalc.elasticity.StressTensor2D;
import com.engineercalc.elasticity.Tensor2D;
import com.engineercalc.elasticity.Tensor3D;
import com.engineercalc.elasticity.Tensor.Component;
import com.engineercalc.elasticity.TensorHelper.Dimension;
import com.engineercalc.elasticity.TensorHelper.Type;
import com.engineercalc.mohrscircle.R;

public class PlotView extends View {
	
	private final Paint axesPaint = new Paint();
	private final Paint gridPaint = new Paint();
	private final Paint circlePaint = new Paint();
	private final Paint circleFillPaint = new Paint();
	private final Paint arrowPaint = new Paint();
	private final Paint bgPaint = new Paint();
	private final Paint textPaint = new Paint();
	private final Paint pointPaint = new Paint();
	private final int lightBlue = Color.argb(225, 51, 181, 229);
	private final int lightGreen = Color.argb(225, 153, 204, 0);
	private final int lightRed = Color.argb(225, 255, 68, 68);
	private final static float circlePaddingPercent = 0.15f;
	private final static double targetTickNums = 10;
	private final static float ahLen = 15.0f;	// arrow head length
	private final static float ahW = 10.0f;		// arrow head width
	private static float pointRadius = 5f;
	private boolean flipAxisLabels = false;

	private Tensor tensor = new StressTensor2D();
	private float yAxisLoc, tick;
	ScaledCanvas canvas = new ScaledCanvas();
	
	public PlotView(Context context) {
		super(context);
		init();
	}
	
	public PlotView(Context context, AttributeSet attrs){
		super(context,attrs);
		init();
	}
	
	public PlotView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}

	private void init() {
		//tensor.setXX(20);	// debug only
		canvas.setArrowHeadSize(ahLen, ahW);
		
		final boolean aa = true;
		float w = 3;
		pointRadius = w*w;
		w = getResources().getInteger(R.integer.linewidth);
		pointRadius = w*2;
		
		bgPaint.setColor(Color.WHITE);

		axesPaint.setStyle(Style.STROKE);
		axesPaint.setColor(Color.GRAY);
		axesPaint.setAntiAlias(aa);
		axesPaint.setStrokeWidth(w);
		
		gridPaint.setStyle(Style.STROKE);
		gridPaint.setColor(Color.GRAY);
		gridPaint.setAlpha(110);
		gridPaint.setStrokeWidth(0.0f);
		gridPaint.setAntiAlias(aa);
		
		circlePaint.setStyle(Style.STROKE);
		circlePaint.setColor(Color.BLACK);
		circlePaint.setAntiAlias(aa);
		circlePaint.setStrokeWidth(w);

		circleFillPaint.setStyle(Style.FILL);
		circleFillPaint.setColor(Color.parseColor("#ffdddddd"));
		circleFillPaint.setAntiAlias(aa);
		
		pointPaint.setStyle(Style.FILL);
		pointPaint.setAntiAlias(aa);
		pointPaint.setStrokeWidth(0.0f);
		
		textPaint.setColor(Color.DKGRAY);
		textPaint.setAntiAlias(true);
		textPaint.setTextSize(24);
		textPaint.setTextSize(getResources().getInteger(R.integer.plot_text_size));

		arrowPaint.setStyle(Style.STROKE);
		arrowPaint.setColor(Color.BLACK);
		arrowPaint.setStrokeWidth(w*0.75f);
		arrowPaint.setAntiAlias(aa);
	}
	
	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);
		invalidate();
	}
	
	@Override
	protected void onDraw (Canvas canvasIn) {
		super.onDraw(canvasIn);

		canvas.lockCanvas(canvasIn);
		
		float minX, maxX, minY, maxY;

		if (tensor.isHydrostatic()) {
			final float stress = (float)tensor.getXX();
			final float hydroPadding = 0.5f;
			if (stress > 0.0f) {
				minX = -stress*hydroPadding/2;
				maxX = stress*(1.0f+hydroPadding);
			} else if (stress < 0.0f) {
				minX = stress*(1.0f+hydroPadding);
				maxX = -stress*hydroPadding/2;
			} else {
				minX = -10.0f;
				maxX = 10.0f;
			}
			minY = -10; maxY = 10;
		} else {
			float Smax, Smin, Tmax;
			if (tensor.getDimension() == Dimension.DIM2D) {
				Smax = (float)((Tensor2D)tensor).getFirstPrincipal();
				Smin = (float)((Tensor2D)tensor).getSecondPrincipal();
				Tmax = (float)((Tensor2D)tensor).getMaxTensorShear();
			} else {
				Smax = (float)((Tensor3D)tensor).getFirstPrincipal();
				Smin = (float)((Tensor3D)tensor).getThirdPrincipal();
				Tmax = (float)((Tensor3D)tensor).getMaxTensorShear();
			}
			final float pad = (Smax-Smin)*circlePaddingPercent;
			minX = Smin-pad;
			maxX = Smax+pad;
			minY = -Tmax-pad;
			maxY = Tmax+pad;
		}
		
		canvas.setMinimumDrawRegion(getWidth(), getHeight(), minX, maxX, minY, maxY);
		
		// Set where y-axis is drawn
		flipAxisLabels = false;
		if (canvas.getMinX() > 0.0f) {
			yAxisLoc = canvas.getMinX() + 2.0f/canvas.getScale();	// 1/scale is equal to one pixel. Move the axis two pixels right.
		} else if (canvas.getMaxX() < 0.0f) {
			yAxisLoc = canvas.getMaxX() - 2.0f/canvas.getScale();
			flipAxisLabels = true;
		} else {
			yAxisLoc = 0.0f;
		}
		tick = (float)findTickIncr(canvas.getMinX(), canvas.getMaxX());
		
		canvas.drawColor(bgPaint.getColor());
		
		if (tensor.getDimension() == Dimension.DIM2D) {
			draw2D(canvas, (Tensor2D)tensor);
		} else {
			draw3D(canvas, (Tensor3D)tensor);
		}
		
		canvas.unlockCanvas();
	}
	
	private void draw2D(ScaledCanvas canvas, Tensor2D tensor) {
		drawAxes(canvas);
		draw(canvas, tensor);
		drawInput(canvas, tensor);
	}
	
	private void draw3D(ScaledCanvas canvas, Tensor3D tensor) {
		drawShadedRegion(canvas, tensor);
		drawAxes(canvas);
		draw(canvas, tensor);
		drawInput(canvas, tensor);
	}
	
	public void setTensorData(Tensor tensor) {
		this.tensor = tensor;
		invalidate();
	}

	private float findTickIncr(float minX, float maxX) {
		final float guess = (maxX-minX)/(float)targetTickNums;
		
		final float pow = FloatMath.floor((float)Math.log10(guess));
		final float guess1 = (float)Math.pow(10, pow);
		final float guess2 = 5*guess1;
		if (Math.abs(targetTickNums - (maxX-minX)/guess1) < Math.abs(targetTickNums - (maxX-minX)/guess2)) {
			return guess1;
		} else {
			return guess2;
		}
	}
	
	private void drawAxes(ScaledCanvas canvas) {
		final Resources res = getResources();
		final float minX = canvas.getMinX();
		final float maxX = canvas.getMaxX();
		final float minY = canvas.getMinY();
		final float maxY = canvas.getMaxY();
		
		canvas.drawLine(minX, 0.0f, maxX, 0.0f, axesPaint);
		canvas.drawLine(yAxisLoc, minY, yAxisLoc, maxY, axesPaint);
		
		for (float x = 0; x <= maxX; x+=tick) {
			canvas.drawLine(x, minY, x, maxY, gridPaint);
		}
		for (float x = -tick; x >= minX; x-=tick) {
			canvas.drawLine(x, minY, x, maxY, gridPaint);
		}
		
		for (float y = 0; y <= maxY; y+= tick) {
			canvas.drawLine(minX, y, maxX, y, gridPaint);
		}
		for (float y = -tick; y >= minY; y-= tick) {
			canvas.drawLine(minX, y, maxX, y, gridPaint);
		}

		HALIGN verticalAxisLabelHAlign = HALIGN.LEFT;
		if (flipAxisLabels) {
			verticalAxisLabelHAlign = HALIGN.RIGHT;
		}
		if (tensor.getType() == Type.STRESS) {
			canvas.drawAlignedText(res.getString(R.string.S), HALIGN.RIGHT, VALIGN.BOTTOM, maxX, 0, textPaint, false);
			canvas.drawAlignedText(res.getString(R.string.T), verticalAxisLabelHAlign, VALIGN.TOP, yAxisLoc, maxY, textPaint, false);
		} else {
			canvas.drawAlignedText(res.getString(R.string.E), HALIGN.RIGHT, VALIGN.BOTTOM, maxX, 0, textPaint, false);
			canvas.drawAlignedText(res.getString(R.string.G) + "/2", verticalAxisLabelHAlign, VALIGN.TOP, yAxisLoc, maxY, textPaint, false);
		}
	}
	
	private void draw(ScaledCanvas canvas, Tensor2D tensor) {
		final Resources res = getResources();
		final String lblS1, lblS2, lblTmax;
		if (tensor.getType() == Type.STRESS) {
			lblS1 = res.getString(R.string.S1);
			lblS2 = res.getString(R.string.S2);
			lblTmax = res.getString(R.string.Tmax);
		} else {
			lblS1 = res.getString(R.string.E1);
			lblS2 = res.getString(R.string.E2);
			lblTmax = res.getString(R.string.Gmax) + "/2";
		}
		
		if (tensor.isHydrostatic()) {
			final float stress = (float)tensor.getXX();
			pointPaint.setColor(Color.BLACK);
			canvas.drawPoint(stress, 0.0f, pointRadius, pointPaint);
			String label = lblS1 + "=" + lblS2;
			
			canvas.drawAlignedText(label, HALIGN.LEFT, VALIGN.TOP, stress, 0, textPaint, false);
			return;
		}
		
		final float S1 = (float)tensor.getFirstPrincipal();
		final float S2 = (float)tensor.getSecondPrincipal();
		final float Tmax = (float)tensor.getMaxTensorShear();
		
		canvas.drawCircle((S1+S2)/2.0f, 0.0f, (S1-S2)/2.0f, circlePaint);
		
		canvas.drawAlignedText(lblS1, HALIGN.LEFT, VALIGN.TOP, S1, 0, textPaint, false);
		canvas.drawAlignedText(lblS2, HALIGN.LEFT, VALIGN.TOP, S2, 0, textPaint, false);
		canvas.drawAlignedText(lblTmax, HALIGN.LEFT, VALIGN.BOTTOM, (S2+S1)/2.0f, Tmax, textPaint, false);
	}
	
	private void drawShadedRegion(ScaledCanvas canvas, Tensor3D tensor) {
		final float S1 = (float)tensor.getFirstPrincipal();
		final float S2 = (float)tensor.getSecondPrincipal();
		final float S3 = (float)tensor.getThirdPrincipal();
		
		canvas.drawCircle((S1+S3)/2.0f, 0.0f, (S1-S3)/2.0f, circleFillPaint);
		canvas.drawCircle((S2+S3)/2.0f, 0.0f, (S2-S3)/2.0f, bgPaint);
		canvas.drawCircle((S1+S2)/2.0f, 0.0f, (S1-S2)/2.0f, bgPaint);
	}
	
	private void draw(ScaledCanvas canvas, Tensor3D tensor) {
		final Resources res = getResources();
		final String lblS1, lblS2, lblS3, lblTmax;
		if (tensor.getType() == Type.STRESS) {
			lblS1 = res.getString(R.string.S1);
			lblS2 = res.getString(R.string.S2);
			lblS3 = res.getString(R.string.S3);
			lblTmax = res.getString(R.string.Tmax);
		} else {
			lblS1 = res.getString(R.string.E1);
			lblS2 = res.getString(R.string.E2);
			lblS3 = res.getString(R.string.E3);
			lblTmax = res.getString(R.string.Gmax) + "/2";
		}
		
		if (tensor.isHydrostatic()) {
			final float stress = (float)tensor.getXX();
			pointPaint.setColor(Color.BLACK);
			canvas.drawPoint(stress, 0.0f, pointRadius, pointPaint);
			String label = lblS1 + "=" + lblS2 + "=" + lblS3;
			
			canvas.drawAlignedText(label, HALIGN.LEFT, VALIGN.TOP, stress, 0, textPaint, false);
			return;
		}
		
		final float S1 = (float)tensor.getFirstPrincipal();
		final float S2 = (float)tensor.getSecondPrincipal();
		final float S3 = (float)tensor.getThirdPrincipal();
		final float Tmax = (float)tensor.getMaxTensorShear();
		
		canvas.drawCircle((S1+S3)/2.0f, 0.0f, (S1-S3)/2.0f, circlePaint);
		canvas.drawCircle((S2+S3)/2.0f, 0.0f, (S2-S3)/2.0f, circlePaint);
		canvas.drawCircle((S1+S2)/2.0f, 0.0f, (S1-S2)/2.0f, circlePaint);
		
		canvas.drawAlignedText(lblS1, HALIGN.LEFT, VALIGN.TOP, S1, 0, textPaint, false);
		canvas.drawAlignedText(lblS2, HALIGN.LEFT, VALIGN.TOP, S2, 0, textPaint, false);
		canvas.drawAlignedText(lblS3, HALIGN.LEFT, VALIGN.TOP, S3, 0, textPaint, false);
		canvas.drawAlignedText(lblTmax, HALIGN.LEFT, VALIGN.BOTTOM, (S3+S1)/2.0f, Tmax, textPaint, false);
	}
	
	private void drawInput(ScaledCanvas canvas, Tensor2D tensor) {
		if (tensor.isHydrostatic())
			return;
		
		final Resources res = getResources();
		
		final float S1 = (float)tensor.getFirstPrincipal();
		final float S2 = (float)tensor.getSecondPrincipal();
		final float Tmax = (float)tensor.getMaxTensorShear();
		final float avg = (S1 + S2) * 0.5f;
		final float radius = Tmax;
		final float Sxx = (float)tensor.getXX();
		final float Syy = (float)tensor.getYY();
		final float Sxy = (float)tensor.getTensorShear(Component.XY);
		final float thetaP = (float)tensor.getPrincipalAngle2D();

		// connect the dots
		canvas.drawLine(Sxx, -Sxy, Syy, Sxy, circlePaint);

		// principal angle label
		if (Math.abs(thetaP) > 0.1f) {
			float thetaLabelOffset;
			if (thetaP <= 0.0f) {
				canvas.drawCircleArc(avg, 0, radius*0.6f, 0, -thetaP*2, true, arrowPaint, false);
				thetaLabelOffset = 18;
			} else {
				canvas.drawCircleArc(avg, 0, radius*0.6f, -thetaP*2, thetaP*2, true, arrowPaint, false);
				thetaLabelOffset = -18;
			}
			final float thetaLabelX = avg + radius*0.6f*FloatMath.cos((-2f*thetaP+thetaLabelOffset)*(float)Math.PI/180.0f);
			final float thetaLabelY = radius*0.6f*FloatMath.sin((-2f*thetaP+thetaLabelOffset)*(float)Math.PI/180.0f);
			canvas.drawAlignedText("2" + res.getString(R.string.thetap), HALIGN.CENTER, VALIGN.CENTER, thetaLabelX, thetaLabelY, textPaint, false);
		}

		// colored circle to indicate input values
		pointPaint.setColor(lightBlue);
		canvas.drawPoint(Sxx, -Sxy, pointRadius, pointPaint);		// Note this shear term is plotted negative
		pointPaint.setColor(lightGreen);
		canvas.drawPoint(Syy, Sxy, pointRadius, pointPaint);

		String[] labels = {res.getString(R.string.x),
				res.getString(R.string.y)};
		int[] colors = {lightBlue, lightGreen};
		canvas.drawLegend(labels, colors, textPaint);
	}
	
	private void drawInput(ScaledCanvas canvas, Tensor3D tensor) {
		if (tensor.isHydrostatic())
			return;
		
		final Resources res = getResources();
		
		final float Sxx = (float)tensor.getXX();
		final float Syy = (float)tensor.getYY();
		final float Szz = (float)tensor.getZZ();
		final float Sxy = (float)tensor.getTensorShear(Component.XY);
		final float Syz = (float)tensor.getTensorShear(Component.YZ);
		final float Szx = (float)tensor.getTensorShear(Component.ZX);
		
		final float SxTmax = FloatMath.sqrt(Sxy*Sxy + Szx*Szx);		// Applied shear on the plane with x-normal
		final float SyTmax = FloatMath.sqrt(Sxy*Sxy + Syz*Syz);
		final float SzTmax = FloatMath.sqrt(Szx*Szx + Syz*Syz);
		
		pointPaint.setColor(lightBlue);
		canvas.drawPoint(Sxx, SxTmax, pointRadius, pointPaint);
		pointPaint.setColor(lightGreen);
		canvas.drawPoint(Syy, SyTmax, pointRadius, pointPaint);
		pointPaint.setColor(lightRed);
		canvas.drawPoint(Szz, SzTmax, pointRadius, pointPaint);
		
		String[] labels = {res.getString(R.string.x),
				res.getString(R.string.y),
				res.getString(R.string.z)};
		int[] colors = {lightBlue, lightGreen, lightRed};
		canvas.drawLegend(labels, colors, textPaint);
	}
}
