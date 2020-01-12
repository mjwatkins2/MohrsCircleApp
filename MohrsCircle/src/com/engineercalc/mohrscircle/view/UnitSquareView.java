package com.engineercalc.mohrscircle.view;

import com.engineercalc.mohrscircle.view.ScaledCanvas.HALIGN;
import com.engineercalc.mohrscircle.view.ScaledCanvas.VALIGN;
import com.engineercalc.elasticity.StrainRosette;
import com.engineercalc.elasticity.Tensor;
import com.engineercalc.elasticity.TensorHelper.Dimension;
import com.engineercalc.elasticity.TensorHelper.Type;
import com.engineercalc.mohrscircle.R;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.View;
import android.util.FloatMath;

public class UnitSquareView  extends View {
	/*
	 *	light
			blue	33B5E5	51 181 229
			purple	AA66CC	170 102 204
			green	99CC00	153 204   0
			orange	FFBB33	255 187  51
			red		FF4444	255  68  68
	 	dark
			blue	0099CC	0 153 204
			purple	9933CC	53  51 204
			green	669900	102 153   0
			orange	FF8800	255 136   0
			red		CC0000	204   0   0
	 */
	private final Paint squareOutlinePaint = new Paint();
	private final Paint squareOutlineDashedPaint = new Paint();
	private final Paint squareFillPaint = new Paint();
	private final Paint textPaint = new Paint();
	private final Paint axesPaint = new Paint();
	private final Paint arrowPaint = new Paint();
	private final int lightBlue = Color.argb(100, 51, 181, 229);
	private final int lightGreen = Color.argb(100, 153, 204, 0);
	private final int lightRed = Color.argb(100, 255, 68, 68);
	private final int darkBlue = Color.rgb(0, 153, 204);
	private final int darkGreen = Color.rgb(102, 153, 0);
	private final int darkRed = Color.rgb(204, 0, 0);
	private final Path xFace = new Path();
	private final Path yFace = new Path();
	private final Path zFace = new Path();
	private final Path shearedSquare = new Path();
	
	private final static float sqWS = 5.0f;		// stress square half-width
	private final static float sqWE = 5.0f;		// stress square half-width
	private final static float ahLen = 1f;		// arrow head length
	private final static float ahW = 0.5f;		// arrow head width
	
	private Dimension dimension = Dimension.DIM2D;
	private Type type = Type.STRAINROSETTE;
	
	private float alpha = 45;
	private float beta = 45;
	private float gamma = 45;
	
	ScaledCanvas canvas = new ScaledCanvas();
	
	public UnitSquareView(Context context) {
		super(context);
		init();
	}
	
	public UnitSquareView(Context context, AttributeSet attrs){
		super(context,attrs);
		init();
	}
	
	public UnitSquareView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}
	
	private void init() {
		canvas.setArrowHeadSize(ahLen, ahW);
		
		final boolean aa = true;
		final float w = getResources().getInteger(R.integer.linewidth);
		
		squareOutlinePaint.setStyle(Style.STROKE);
		squareOutlinePaint.setColor(Color.BLACK);
		squareOutlinePaint.setStrokeWidth(w);
		squareOutlinePaint.setAntiAlias(aa);
		
		arrowPaint.setStyle(Style.STROKE);
		arrowPaint.setColor(Color.BLACK);
		arrowPaint.setStrokeWidth(w*0.75f);
		arrowPaint.setAntiAlias(aa);
		
		textPaint.setColor(Color.BLACK);
		textPaint.setAntiAlias(true);
		
		squareOutlineDashedPaint.setStyle(Style.STROKE);
		squareOutlineDashedPaint.setColor(Color.BLACK);
		squareOutlineDashedPaint.setStrokeWidth(w*0.7f);
		squareOutlineDashedPaint.setAntiAlias(aa);
		squareOutlineDashedPaint.setPathEffect(new DashPathEffect(new float[] {10,10}, 0));

		squareFillPaint.setStyle(Style.FILL);
				
		axesPaint.setStyle(Style.STROKE);
		axesPaint.setStrokeWidth(w);
		axesPaint.setAntiAlias(aa);
		
		xFace.moveTo(10, 5);
		xFace.lineTo(10, -5);
		xFace.lineTo(0, -10);
		xFace.lineTo(0, 0);
		xFace.close();
		yFace.moveTo(-10, 5);
		yFace.lineTo(0, 10);
		yFace.lineTo(10, 5);
		yFace.lineTo(0, 0);
		yFace.close();
		zFace.moveTo(0, 0);
		zFace.lineTo(0, -10);
		zFace.lineTo(-10, -5);
		zFace.lineTo(-10, 5);
		zFace.close();
		
		shearedSquare.moveTo(-sqWE, -sqWE);
		shearedSquare.lineTo(-sqWE*0.4f, sqWE*1.4f);
		shearedSquare.lineTo(sqWE*2.0f, sqWE*2.0f);
		shearedSquare.lineTo(sqWE*1.4f, -sqWE*0.4f);
		shearedSquare.close();
	}
	
	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);
		invalidate();
	}
	
	public void setImageType(Tensor tensor) {
		this.dimension = tensor.getDimension();
		this.type = tensor.getType();
		if (type == Type.STRAINROSETTE) {
			StrainRosette SR = (StrainRosette)tensor;
			alpha = (float)SR.getAlpha();
			beta = (float)SR.getBeta();
			gamma = (float)SR.getGamma();
		}
		invalidate();
	}
	
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		final float desiredAR = 1.2f; //desiredMinWidth/desiredMinHeight;
		
		final int widthMSMode = MeasureSpec.getMode(widthMeasureSpec);
		final int widthMSSize = MeasureSpec.getSize(widthMeasureSpec);
		final int heightMSMode = MeasureSpec.getMode(heightMeasureSpec);
		final int heightMSSize = MeasureSpec.getSize(heightMeasureSpec);
		
		if (heightMSMode == MeasureSpec.UNSPECIFIED) {
			setMeasuredDimension(widthMSSize, (int) (widthMSSize/desiredAR));
			return;
		}
		
		if (widthMSMode == MeasureSpec.UNSPECIFIED) {
			setMeasuredDimension((int)(heightMSSize*desiredAR), heightMSSize);
			return;
		}
		
		int heightIfWidthConstrained = (int)(widthMSSize/desiredAR);
		int widthIfHeightConstrained = (int)(heightMSSize*desiredAR);
		
		// Is height bigger than necessary?
		if (widthMSSize < widthIfHeightConstrained) {
			setMeasuredDimension(widthMSSize, heightIfWidthConstrained);
			return;
		}
		
		// Is width bigger than necessary?
		if (heightMSSize < heightIfWidthConstrained) {
			setMeasuredDimension(widthIfHeightConstrained, heightMSSize);
			return;
		}
		
		setMeasuredDimension(widthMSSize, heightMSSize);
	}
	
	@Override
	protected void onDraw (Canvas canvasIn) {
		super.onDraw(canvasIn);

		canvas.lockCanvas(canvasIn);
		canvas.setMinimumDrawRegion(getWidth(), getHeight(), -13, 13, -13, 13);

		canvas.drawColor(Color.WHITE);
		
		final Resources res = getResources();
		
		if (dimension == Dimension.DIM2D) {
			if (type == Type.STRESS) {
				drawStress2D(canvas, res);
			} else if (type == Type.STRAINROSETTE) {
				drawStrainRosette(canvas, res);
			} else {
				drawStrain2D(canvas, res);
			}
		} else {
			if (type == Type.STRESS) {
				drawStress3D(canvas, res);
			} else if (type == Type.STRAINROSETTE) {
				drawStrainRosette(canvas, res);
			} else {
				drawStrain3D(canvas, res);
			}
		}
		canvas.unlockCanvas();
	}
	
	private void drawAxes2D(ScaledCanvas canvas, String xLabel, String yLabel) {
		
		final float minX = canvas.getMinX();
		final float minY = canvas.getMinY();
		
		textPaint.setTextSize(1.25f);
		
		axesPaint.setColor(darkBlue);
		textPaint.setColor(darkBlue);
		canvas.drawLine(minX+1.0f, minY+1.0f, minX+3.0f, minY+1.0f, axesPaint);
		canvas.drawAlignedText(xLabel, HALIGN.LEFT, VALIGN.BOTTOM, minX+3.2f, minY+1.0f, textPaint, true);
		axesPaint.setColor(darkGreen);
		textPaint.setColor(darkGreen);
		canvas.drawLine(minX+1.0f, minY+1.0f, minX+1.0f, minY+3.0f, axesPaint);
		canvas.drawAlignedText(yLabel, HALIGN.LEFT, VALIGN.BOTTOM, minX+1.1f, minY+3.2f, textPaint, true);
	}
	
	private void drawAxes3D(ScaledCanvas canvas, String xLabel, String yLabel, String zLabel) {

		final float minX = canvas.getMinX();
		final float minY = canvas.getMinY();

		textPaint.setTextSize(1.25f);
		
		axesPaint.setColor(darkBlue);
		textPaint.setColor(darkBlue);
		canvas.drawLine(minX+2.5f, minY+3f, minX+4.5f, minY+2f, axesPaint);
		canvas.drawAlignedText(xLabel, HALIGN.LEFT, VALIGN.TOP, minX+4.5f, minY+2f, textPaint, true);
		axesPaint.setColor(darkGreen);
		textPaint.setColor(darkGreen);
		canvas.drawLine(minX+2.5f, minY+3, minX+2.5f, minY+5f, axesPaint);
		canvas.drawAlignedText(yLabel, HALIGN.LEFT, VALIGN.CENTER, minX+2.6f, minY+4.9f, textPaint, true);
		axesPaint.setColor(darkRed);
		textPaint.setColor(darkRed);
		canvas.drawLine(minX+2.5f, minY+3, minX+0.5f, minY+2f, axesPaint);
		canvas.drawAlignedText(zLabel, HALIGN.LEFT, VALIGN.TOP, minX+0.5f, minY+2f, textPaint, true);
	}

	private void drawStress2D(ScaledCanvas canvas, Resources res) {
		final float sqW = sqWS;
		final float axLen = 4.0f;			// axial arrow length
		final float shLen = 4.0f;			// shear arrow half-length
		final float shOffs = 0.5f;			// shear arrow offset from square
		final float axEnd = sqW+axLen;		// axial arrow endpoint
		final float shPos = sqW + shOffs;	// shear arrow position
		final float ahWS = ahW;
		
		drawAxes2D(canvas, res.getString(R.string.x), res.getString(R.string.y));
		
		textPaint.setTextSize(1.5f);
		textPaint.setColor(Color.BLACK);
		
		// square
		squareFillPaint.setColor(lightBlue);
		canvas.drawRect(-sqW, -sqW, sqW, sqW, squareFillPaint);
		canvas.drawRect(-sqW, -sqW, sqW, sqW, squareOutlinePaint);
		
		// Sx
		canvas.drawArrow(sqW, 0.0f, axEnd, 0.0f, true, true, arrowPaint, true);
		canvas.drawAlignedSubscriptedText(res.getString(R.string.Sx), HALIGN.CENTER, VALIGN.TOP, new int[]{1}, axEnd, -ahWS*1.2f, textPaint, true);
		// -Sx
		canvas.drawArrow(-sqW, 0.0f, -axEnd, 0.0f, true, true, arrowPaint, true);
		canvas.drawAlignedSubscriptedText(res.getString(R.string.Sx), HALIGN.CENTER, VALIGN.TOP, new int[]{1}, -axEnd, -ahWS*1.2f, textPaint, true);
		// Sxy right
		canvas.drawArrow(shPos, -shLen, shPos, shLen, false, true, arrowPaint, true);
		// -Sxy left
		canvas.drawArrow(-shPos, shLen, -shPos, -shLen, false, true, arrowPaint, true);
		canvas.drawAlignedSubscriptedText(res.getString(R.string.Txy), HALIGN.LEFT, VALIGN.BOTTOM, new int[]{1, 2}, sqW*1.1f, sqW*1.1f, textPaint, true);

		// Sy
		canvas.drawArrow(0.0f, sqW, 0.0f, axEnd, true, true, arrowPaint, true);
		canvas.drawAlignedSubscriptedText(res.getString(R.string.Sy), HALIGN.LEFT, VALIGN.CENTER, new int[]{1}, ahWS*1.5f, axEnd, textPaint, true);
		// -Sy
		canvas.drawArrow(0.0f, -sqW, 0.0f, -axEnd, true, true, arrowPaint, true);
		canvas.drawAlignedSubscriptedText(res.getString(R.string.Sy), HALIGN.LEFT, VALIGN.CENTER, new int[]{1}, ahWS*1.5f, -axEnd, textPaint, true);
		// Sxy	bottom
		canvas.drawArrow(shLen, -shPos, -shLen, -shPos, true, false, arrowPaint, true);
		// -Sxy top
		canvas.drawArrow(-shLen, shPos, shLen, shPos, true, false, arrowPaint, true);
		canvas.drawAlignedSubscriptedText(res.getString(R.string.Txy), HALIGN.RIGHT, VALIGN.TOP, new int[]{1, 2}, -sqW, -sqW, textPaint, true);
	}
	
	private void drawStrain2D(ScaledCanvas canvas, Resources res) {
		final float sqW = sqWE;				// square half-width
		final float ahLenS = ahLen;

		drawAxes2D(canvas, res.getString(R.string.x), res.getString(R.string.y));
		
		textPaint.setTextSize(1.5f);
		textPaint.setColor(Color.BLACK);

		// square
		canvas.drawRect(-sqW, -sqW, sqW, sqW, squareOutlineDashedPaint);
		squareFillPaint.setColor(lightBlue);
		canvas.drawPath(shearedSquare, squareFillPaint);
		canvas.drawPath(shearedSquare, squareOutlinePaint);
		canvas.drawAlignedText(res.getString(R.string.dx), HALIGN.CENTER, VALIGN.TOP, 0, sqW*0.95f, textPaint, true);
		canvas.drawAlignedText(res.getString(R.string.dy), HALIGN.RIGHT, VALIGN.CENTER, sqW*0.95f, 0, textPaint, true);
		
		// ex
		canvas.drawLine(sqW, -sqW*1.1f, sqW, -sqW*1.5f, arrowPaint);
		canvas.drawLine(sqW*1.4f, -sqW*0.5f, sqW*1.4f, -sqW*1.5f, arrowPaint);
		canvas.drawArrow(sqW-2.1f*ahLenS, -sqW*1.4f, sqW, -sqW*1.4f, true, true, arrowPaint, true);
		canvas.drawArrow(sqW*1.4f+2.1f*ahLenS, -sqW*1.4f, sqW*1.4f, -sqW*1.4f, true, true, arrowPaint, true);
		canvas.drawAlignedSubscriptedText(res.getString(R.string.Ex) + res.getString(R.string.dx), HALIGN.CENTER, VALIGN.TOP, new int[]{1}, sqW*1.2f, -sqW*1.5f, textPaint, true);	
		// ey
		canvas.drawLine(-sqW*1.1f, sqW, -sqW*1.5f, sqW, arrowPaint);
		canvas.drawLine(-sqW*0.5f, sqW*1.4f, -sqW*1.5f, sqW*1.4f, arrowPaint);
		canvas.drawArrow(-sqW*1.4f, sqW-2.1f*ahLenS, -sqW*1.4f, sqW, true, true, arrowPaint, true);
		canvas.drawArrow(-sqW*1.4f, sqW*1.4f+2.1f*ahLenS, -sqW*1.4f, sqW*1.4f, true, true, arrowPaint, true);
		canvas.drawAlignedSubscriptedText(res.getString(R.string.Ey) + res.getString(R.string.dy), HALIGN.RIGHT, VALIGN.CENTER, new int[]{1}, -sqW*1.5f, sqW*1.2f, textPaint, true);
		// gxy
		final float arcAngle = (float)(Math.atan2(3, 12)*180.0/Math.PI);
		canvas.drawCircleArc(-sqW, -sqW, sqW*1.3f, 0, arcAngle, false, arrowPaint, true);
		canvas.drawAlignedSubscriptedTextFraction(res.getString(R.string.Gxy), "2", HALIGN.RIGHT, VALIGN.TOP, new int[]{1, 2}, sqW*0.3f, -sqW, textPaint, arrowPaint, true);
		canvas.drawCircleArc(-sqW, -sqW, sqW*1.3f, 90, -arcAngle, false, arrowPaint, true);
		canvas.drawAlignedSubscriptedTextFraction(res.getString(R.string.Gxy), "2", HALIGN.RIGHT, VALIGN.TOP, new int[]{1, 2}, -sqW*1.1f, sqW*0.3f, textPaint, arrowPaint, true);
	}


	private void drawStrainRosette(ScaledCanvas canvas, Resources res) {
		final float dist = 9, h = 1.5f, w = 3, lblDist = dist+w/2.0f+1.3f, lblOff = 8*(float)Math.PI/180.0f;
		final float lineLen = dist-w/2;
		final float axesLen = 11;
		textPaint.setTextSize(1.5f);
		
		final float alphaf = (alpha)*(float)Math.PI/180.0f;
		final float betaf = (beta)*(float)Math.PI/180.0f;
		final float gammaf = (gamma)*(float)Math.PI/180.0f;
		
		final float centroidX = dist*(FloatMath.cos(alphaf) + FloatMath.cos(alphaf+betaf) + FloatMath.cos(alphaf+betaf+gammaf))/3.0f;
		final float centroidY = dist*(FloatMath.sin(alphaf) + FloatMath.sin(alphaf+betaf) + FloatMath.sin(alphaf+betaf+gammaf))/3.0f;
		final float centroidD = FloatMath.sqrt(centroidX*centroidX+centroidY*centroidY);
		
		final float scaler = 1+(centroidD)/dist/10;
		canvas.translate(-centroidX, -centroidY);
		canvas.scale(scaler,scaler);

		// Axes
		canvas.drawLine(0, 0, axesLen, 0, squareOutlinePaint);
		canvas.drawLine(0, 0, 0, axesLen, squareOutlinePaint);
		
		textPaint.setColor(Color.BLACK);
		textPaint.setTextSize(1.5f);
		canvas.drawAlignedText(res.getString(R.string.x), HALIGN.LEFT, VALIGN.BOTTOM,
				Math.min(axesLen, canvas.getMaxX()+centroidX-2), 0.5f, textPaint, true);
		canvas.drawAlignedText(res.getString(R.string.y), HALIGN.RIGHT, VALIGN.BOTTOM, -0.5f,
				Math.min(axesLen, canvas.getMaxY()+centroidY-2), textPaint, true);
		
		// Gauge Ea
		squareFillPaint.setColor(lightBlue);
		canvas.drawAngledRectangle(w, h, dist, alpha, squareFillPaint);
		canvas.drawAngledRectangle(w, h, dist, alpha, squareOutlinePaint);
		float angle = alphaf;
		canvas.drawLine(0, 0, lineLen*FloatMath.cos(angle),  lineLen*FloatMath.sin(angle), arrowPaint);
		canvas.drawAlignedSubscriptedText(res.getString(R.string.Ea), HALIGN.CENTER, VALIGN.CENTER, new int[]{1}, 
				lblDist*FloatMath.cos(angle-lblOff), lblDist*FloatMath.sin(angle-lblOff), textPaint, true);

		// Gauge Eb
		squareFillPaint.setColor(lightGreen);
		canvas.drawAngledRectangle(w, h, dist, alpha+beta, squareFillPaint);
		canvas.drawAngledRectangle(w, h, dist, alpha+beta, squareOutlinePaint);
		angle = (alphaf+betaf);
		canvas.drawLine(0, 0, lineLen*FloatMath.cos(angle),  lineLen*FloatMath.sin(angle), arrowPaint);
		canvas.drawAlignedSubscriptedText(res.getString(R.string.Eb), HALIGN.CENTER, VALIGN.CENTER, new int[]{1}, 
				lblDist*FloatMath.cos(angle-lblOff), lblDist*FloatMath.sin(angle-lblOff), textPaint, true);

		// Gauge Ec
		squareFillPaint.setColor(lightRed);
		canvas.drawAngledRectangle(w, h, dist, alpha+beta+gamma, squareFillPaint);
		canvas.drawAngledRectangle(w, h, dist, alpha+beta+gamma, squareOutlinePaint);
		angle = (alphaf+betaf+gammaf);
		canvas.drawLine(0, 0, lineLen*FloatMath.cos(angle),  lineLen*FloatMath.sin(angle), arrowPaint);
		canvas.drawAlignedSubscriptedText(res.getString(R.string.Ec), HALIGN.CENTER, VALIGN.CENTER, new int[]{1}, 
				lblDist*FloatMath.cos(angle-lblOff), lblDist*FloatMath.sin(angle-lblOff), textPaint, true);
		
		final float arcR = 5, arcLbl = arcR + 1;
		if (Math.abs(alpha) > 1) {
			canvas.drawCircleArc(0, 0, arcR-0.5f, 0, alpha, true, arrowPaint, true);
		}
		angle = (alpha/2-3)*(float)Math.PI/180.0f;
		textPaint.setColor(darkBlue);
		canvas.drawAlignedText(res.getString(R.string.alpha), HALIGN.CENTER, VALIGN.CENTER, 
				(arcLbl-0.5f)*FloatMath.cos(angle), (arcLbl-0.5f)*FloatMath.sin(angle), textPaint, true);
		
		angle = (alpha+beta/2-3)*(float)Math.PI/180.0f;
		textPaint.setColor(darkGreen);
		canvas.drawAlignedText(res.getString(R.string.beta), HALIGN.CENTER, VALIGN.CENTER, 
				arcLbl*FloatMath.cos(angle), arcLbl*FloatMath.sin(angle), textPaint, true);
		canvas.drawCircleArc(0, 0, arcR, alpha, beta, true, arrowPaint, true);
		
		angle = (alpha+beta+gamma/2-3)*(float)Math.PI/180.0f;
		textPaint.setColor(darkRed);
		canvas.drawAlignedText(res.getString(R.string.gamma), HALIGN.CENTER, VALIGN.CENTER, 
				(arcLbl+0.5f)*FloatMath.cos(angle), (arcLbl+0.5f)*FloatMath.sin(angle), textPaint, true);
		canvas.drawCircleArc(0, 0, arcR+0.5f, alpha+beta, gamma, true, arrowPaint, true);
	}
	
	private void drawStress3D(ScaledCanvas canvas, Resources res) {

		drawAxes3D(canvas, res.getString(R.string.x), res.getString(R.string.y), res.getString(R.string.z));
		
		textPaint.setTextSize(1.75f);
		textPaint.setColor(Color.BLACK);
		
		canvas.scale(FloatMath.sqrt(3)/1.85f, 1);
		
		// cube
		squareFillPaint.setColor(lightBlue);
		canvas.drawPath(xFace, squareFillPaint);
		canvas.drawPath(xFace, squareOutlinePaint);
		squareFillPaint.setColor(lightGreen);
		canvas.drawPath(yFace, squareFillPaint);
		canvas.drawPath(yFace, squareOutlinePaint);
		squareFillPaint.setColor(lightRed);
		canvas.drawPath(zFace, squareFillPaint);
		canvas.drawPath(zFace, squareOutlinePaint);
		
		// Sx
		canvas.drawArrow(5.0f, -2.5f, 9.0f, -4.5f, true, true, arrowPaint, true);
		canvas.drawAlignedSubscriptedText(res.getString(R.string.Sx), HALIGN.LEFT, VALIGN.BOTTOM, new int[]{1}, 10.5f, -5, textPaint, true);
		// Txy
		canvas.drawArrow(5.0f, -6.5f, 5.0f, 1.5f, false, true, arrowPaint, true);
		canvas.drawAlignedSubscriptedText(res.getString(R.string.Txy), HALIGN.LEFT, VALIGN.TOP, new int[]{1, 2}, 5.5f, 3, textPaint, true);
		// Tzx
		canvas.drawArrow(9.0f, -0.5f, 1.0f, -4.5f, false, true, arrowPaint, true);
		
		// Sy
		canvas.drawArrow(0.0f, 5.0f, 0.0f, 9.0f, true, true, arrowPaint, true);
		canvas.drawAlignedSubscriptedText(res.getString(R.string.Sy), HALIGN.CENTER, VALIGN.BOTTOM, new int[]{1}, 0, 10.8f, textPaint, true);
		// Txy
		canvas.drawArrow(-4.0f, 7.0f, 4.0f, 3.0f, true, false, arrowPaint, true);
		// Tyz
		canvas.drawArrow(4.0f, 7.0f, -4.0f, 3.0f, false, true, arrowPaint, true);
		canvas.drawAlignedSubscriptedText(res.getString(R.string.Tyz), HALIGN.RIGHT, VALIGN.TOP, new int[]{1, 2}, -5.8f, 3, textPaint, true);
		
		// Sz
		canvas.drawArrow(-5.0f, -2.5f, -9.0f, -4.5f, true, true, arrowPaint, true);
		canvas.drawAlignedSubscriptedText(res.getString(R.string.Sz), HALIGN.RIGHT, VALIGN.BOTTOM, new int[]{1}, -10.5f, -5, textPaint, true);
		// Tyz
		canvas.drawArrow(-5.0f, -6.5f, -5.0f, 1.5f, true, false, arrowPaint, true);
		// Tzx
		canvas.drawArrow(-9.0f, -0.5f, -1.0f, -4.5f, true, false, arrowPaint, true);
		canvas.drawAlignedSubscriptedText(res.getString(R.string.Tzx), HALIGN.LEFT, VALIGN.TOP, new int[]{1, 2}, 0.5f, -4.5f, textPaint, true);
	}
	
	private void drawStrain3D(ScaledCanvas canvas, Resources res) {
		
		final float sqW = sqWE;				// square half-width
		final float ahLenS = ahLen;

		drawAxes2D(canvas, res.getString(R.string.i), res.getString(R.string.j));
		
		textPaint.setTextSize(1.5f);
		textPaint.setColor(Color.BLACK);
		
		canvas.drawAlignedText(res.getString(R.string.ijxyyzzx), HALIGN.CENTER, VALIGN.TOP, 0, canvas.getMaxY(), textPaint, true);

		// square
		canvas.drawRect(-sqW, -sqW, sqW, sqW, squareOutlineDashedPaint);
		squareFillPaint.setColor(lightBlue);
		canvas.drawPath(shearedSquare, squareFillPaint);
		canvas.drawPath(shearedSquare, squareOutlinePaint);
		canvas.drawAlignedText(res.getString(R.string.di), HALIGN.CENTER, VALIGN.TOP, 0, sqW*0.95f, textPaint, true);
		canvas.drawAlignedText(res.getString(R.string.dj), HALIGN.RIGHT, VALIGN.CENTER, sqW*0.95f, 0, textPaint, true);
		
		// ex
		canvas.drawLine(sqW, -sqW*1.1f, sqW, -sqW*1.5f, arrowPaint);
		canvas.drawLine(sqW*1.4f, -sqW*0.5f, sqW*1.4f, -sqW*1.5f, arrowPaint);
		canvas.drawArrow(sqW-2.1f*ahLenS, -sqW*1.4f, sqW, -sqW*1.4f, true, true, arrowPaint, true);
		canvas.drawArrow(sqW*1.4f+2.1f*ahLenS, -sqW*1.4f, sqW*1.4f, -sqW*1.4f, true, true, arrowPaint, true);
		canvas.drawAlignedSubscriptedText(res.getString(R.string.Ei) + res.getString(R.string.di), HALIGN.CENTER, VALIGN.TOP, new int[]{1}, sqW*1.2f, -sqW*1.5f, textPaint, true);	
		// ey
		canvas.drawLine(-sqW*1.1f, sqW, -sqW*1.5f, sqW, arrowPaint);
		canvas.drawLine(-sqW*0.5f, sqW*1.4f, -sqW*1.5f, sqW*1.4f, arrowPaint);
		canvas.drawArrow(-sqW*1.4f, sqW-2.1f*ahLenS, -sqW*1.4f, sqW, true, true, arrowPaint, true);
		canvas.drawArrow(-sqW*1.4f, sqW*1.4f+2.1f*ahLenS, -sqW*1.4f, sqW*1.4f, true, true, arrowPaint, true);
		canvas.drawAlignedSubscriptedText(res.getString(R.string.Ej) + res.getString(R.string.dj), HALIGN.RIGHT, VALIGN.CENTER, new int[]{1}, -sqW*1.5f, sqW*1.2f, textPaint, true);
		// gxy
		final float arcAngle = (float)(Math.atan2(3, 12)*180.0/Math.PI);
		canvas.drawCircleArc(-sqW, -sqW, sqW*1.3f, 0, arcAngle, false, arrowPaint, true);
		canvas.drawAlignedSubscriptedTextFraction(res.getString(R.string.Gij), "2", HALIGN.RIGHT, VALIGN.TOP, new int[]{1, 2}, sqW*0.3f, -sqW, textPaint, arrowPaint, true);
		canvas.drawCircleArc(-sqW, -sqW, sqW*1.3f, 90, -arcAngle, false, arrowPaint, true);
		canvas.drawAlignedSubscriptedTextFraction(res.getString(R.string.Gij), "2", HALIGN.RIGHT, VALIGN.TOP, new int[]{1, 2}, -sqW*1.1f, sqW*0.3f, textPaint, arrowPaint, true);
	}
}
