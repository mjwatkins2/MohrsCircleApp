package com.engineercalc.mohrscircle.view;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Paint.Align;
import android.util.FloatMath;

public class ScaledCanvas {

	private final RectF arcRect = new RectF();
	private final Matrix scaleMatrix = new Matrix();
	private final Rect textBounds = new Rect();
	private final Path scaledPath = new Path();

	private Canvas canvas;
	private float scale;
	private float minScaledX, maxScaledX, minScaledY, maxScaledY;
	//private float viewWidth, viewHeight;
	private float ahLen;
	private float ahW;

	Paint legendInnerPaint = new Paint();
	Paint legendBorderPaint = new Paint();
	Paint legendPaint = new Paint();
	
	public enum HALIGN {LEFT, RIGHT, CENTER};
	public enum VALIGN {TOP, BOTTOM, CENTER};

	/**
	 * Canvas wrapper that scales object drawing operations to non-scaled canvas.
	 * Also flips the y-direction so positive is up.
	 * @param canvas
	 * @param scale
	 * @param arrowHeadLength 
	 * @param arrowHeadHalfWidth
	 * @param arrowPaint
	 * @param textPaint
	 */
	public ScaledCanvas() {
		legendInnerPaint.setStyle(Style.FILL);
		legendInnerPaint.setColor(Color.WHITE);
		legendBorderPaint.setColor(Color.BLACK);
		legendBorderPaint.setStyle(Style.STROKE);
		legendBorderPaint.setStrokeWidth(0.0f);
		legendPaint.setStyle(Style.FILL);
	}
	
	/**
	 * Fits the desired drawing region into the view. Note that the actual min and max values
	 * of X and Y may be larger than the values specified here.  
	 * @param viewWidth
	 * @param viewHeight
	 * @param minX
	 * @param maxX
	 * @param minY
	 * @param maxY
	 */
	public void setMinimumDrawRegion(float viewWidth, float viewHeight, float minX, float maxX, float minY, float maxY) {
		
		if ((viewHeight/viewWidth) > (maxY-minY)/(maxX-minX)) {
			scale = viewWidth/(maxX-minX);
			minScaledX = minX;
			maxScaledX = maxX;
			final float targetHeight = (maxX-minX)*(viewHeight/viewWidth);
			minScaledY = (maxY + minY)/2.0f - targetHeight/2.0f;
			maxScaledY = (maxY + minY)/2.0f + targetHeight/2.0f;
		} else {
			scale = viewHeight/(maxY-minY);
			final float targetWidth = (maxY-minY)*(viewWidth/viewHeight);
			minScaledX = (maxX + minX)/2.0f - targetWidth/2.0f;
			maxScaledX = (maxX + minX)/2.0f + targetWidth/2.0f;
			minScaledY = minY;
			maxScaledY = maxY;
		}
		canvas.translate(viewWidth/2.0f, viewHeight/2.0f);
		canvas.translate(-(viewWidth/2.0f + minScaledX*scale), -(viewHeight/2.0f + minScaledY*scale));
		canvas.scale(1, -1);
		
		scaleMatrix.setScale(scale, scale);
	}
	
	public void setArrowHeadSize(float arrowHeadLength, float arrowHeadHalfWidth) {
		ahLen = arrowHeadLength;
		ahW = arrowHeadHalfWidth;
	}
	
	public void lockCanvas(Canvas canvas) {
		this.canvas = canvas;
	}
	
	public void unlockCanvas() {
		canvas = null;
	}
	
	public float getMinX() {
		return minScaledX;
	}
	public float getMaxX() {
		return maxScaledX;
	}
	public float getMinY() {
		return minScaledY;
	}
	public float getMaxY() {
		return maxScaledY;
	}
	public float getScale() {
		return scale;
	}
	public float getMinXUnscaled() {
		return minScaledX*scale;
	}
	public float getMaxXUnscaled() {
		return maxScaledX*scale;
	}
	public float getMinYUnscaled() {
		return minScaledY*scale;
	}
	public float getMaxYUnscaled() {
		return maxScaledY*scale;
	}
	
	/* ---------------------------- */
	/* Pass-through drawing methods */
	/* ---------------------------- */


	public void drawColor(int color) {
		canvas.drawColor(color);
	}
	
	public void drawCircle(float cx, float cy, float radius, Paint paint) {
		canvas.drawCircle(cx*scale, cy*scale, radius*scale, paint);
	}

	public void drawLine(float startX, float startY, float stopX, float stopY, Paint paint) {
		canvas.drawLine(startX*scale, startY*scale, stopX*scale, stopY*scale, paint);
	}
	

	public void drawRect(float left, float top, float right, float bottom, Paint paint) {
		canvas.drawRect(left*scale, top*scale, right*scale, bottom*scale, paint);
	}

	public void drawPath(Path path, Paint paint) {
		path.transform(scaleMatrix, scaledPath);
		canvas.drawPath(scaledPath, paint);
	}
	
	public void translate(float dx, float dy) {
		canvas.translate(dx*scale, dy*scale);
	}
	
	public void scale(float sx, float sy) {
		canvas.scale(sx, sy);
	}

	/* ------------- */
	/* Custom shapes */
	/* ------------- */

	public void drawPoint(float cx, float cy, float radius, Paint paint) {
		canvas.drawCircle(cx*scale, cy*scale, radius, paint);
	}
	
	public void drawArrow(float tailX, float tailY, float headX, float headY, boolean leftHead, boolean rightHead,
			Paint arrowPaint, boolean scaled) {
		
		float arrowLength = ahLen;
		float arrowWidth = ahW;
		if (scaled) {
			arrowLength = ahLen*scale;
			arrowWidth = ahW*scale;
		}
		
		final float theta = (float)Math.atan2(headY-tailY, headX-tailX);
		final float length = (float)Math.hypot(headX-tailX, headY-tailY)*scale;
		final float startX = tailX*scale;
		final float endX = headX*scale;
		final float startY = tailY*scale;
		final float endY = headY*scale;
		final float xCos = (length-arrowLength)*FloatMath.cos(theta);
		final float xSin = (length-arrowLength)*FloatMath.sin(theta);
		final float yCos = (arrowWidth)*FloatMath.cos(theta);
		final float ySin = (arrowWidth)*FloatMath.sin(theta);
		
		canvas.drawLine(startX, startY, endX, endY, arrowPaint);
		if (rightHead) {
			canvas.drawLine(startX + xCos - (-ySin), startY + xSin + (-yCos), endX, endY, arrowPaint);
		}
		if (leftHead) {
			canvas.drawLine(startX + xCos - ySin, startY + xSin + yCos, endX, endY, arrowPaint);
		}
	}
	
	public void drawCircleArc(float xCenter, float yCenter, float radius, float startAngle, float sweep, boolean arrowHead,
			Paint arcPaint, boolean scaled) {
		
		float arrowLength = ahLen;
		float arrowWidth = ahW;
		if (scaled) {
			arrowLength = ahLen*scale;
			arrowWidth = ahW*scale;
		}
		
		arcRect.set((xCenter-radius)*scale, (yCenter-radius)*scale, (xCenter+radius)*scale, (yCenter+radius)*scale);
		canvas.drawArc(arcRect, startAngle, sweep, false, arcPaint);
		
		if (arrowHead && sweep != 0.0f) {
			float inv = 1;
			if (sweep < 0) inv = -1;
			startAngle *= (float)Math.PI/180.0f;
			float endAngle = startAngle + sweep*(float)Math.PI/180.0f;
			final float endX = (xCenter + radius*FloatMath.cos(endAngle))*scale;
			final float endY = (yCenter + radius*FloatMath.sin(endAngle))*scale;
			endAngle += Math.PI/2.0f;
			final float xCos = (-arrowLength)*FloatMath.cos(endAngle)*inv;
			final float xSin = (-arrowLength)*FloatMath.sin(endAngle)*inv;
			final float yCos = (arrowWidth)*FloatMath.cos(endAngle)*inv;
			final float ySin = (arrowWidth)*FloatMath.sin(endAngle)*inv;
			canvas.drawLine(endX + xCos - (-ySin), endY + xSin + (-yCos), endX, endY, arcPaint);
			canvas.drawLine(endX + xCos - ySin, endY + xSin + yCos, endX, endY, arcPaint);
		}
	}
	
	public void drawAngledRectangle(float w, float h, float distFromOrigin, float angle, Paint paint) {
		Rect r = new Rect();
		r.set((int)(scale*(-w/2.0f)), (int)(scale*(-h/2.0f)), (int)(scale*(w/2.0f)), (int)(scale*(h/2.0f)));
		canvas.save();
		canvas.rotate(angle);
		canvas.translate(distFromOrigin*scale,0);
		canvas.drawRect(r, paint);
		canvas.restore();
	}
	
	public void drawLegend(String[] labels, int[] colors, Paint textPaint) {
		final float outerPadding = 5;
		final float innerPadding = 5;
		float maxLen = 0;
		float maxH = 0;
		for (String label : labels) {
			textPaint.getTextBounds(label, 0, label.length(), textBounds);
			if (textBounds.width() > maxLen) {
				maxLen = textBounds.width();
			}
		}
		maxH = textPaint.getTextSize();

		final float circleR = maxH*0.2f;
		final float rectW = maxLen + innerPadding*2 + circleR*2;

		canvas.save();
		canvas.translate(getMaxXUnscaled(), getMaxYUnscaled());

		textPaint.setTextAlign(Align.LEFT);
		for (int i = 0; i < labels.length; i++) {
			legendPaint.setColor(colors[i]);
			canvas.drawCircle(-rectW-innerPadding+circleR, -outerPadding-innerPadding-maxH*(i+0.5f), circleR, legendPaint);
			drawAlignedTextUnscaled(labels[i], HALIGN.LEFT, VALIGN.BOTTOM, -rectW-innerPadding+circleR*2, -outerPadding-innerPadding-maxH*(i+1), textPaint);
		}
		
		canvas.restore();
	}

	/* ---------------------------- */
	/* Aligned text drawing methods */
	/* ---------------------------- */
	
	private float computeTextXPosition(String txt, HALIGN ha, float x, Paint textPaint) {
		
		textPaint.getTextBounds(txt, 0, txt.length(), textBounds);
		switch (ha) {
			case LEFT:
				x += 3f;
				break;
			case RIGHT:
				x -= textBounds.width()+3f;
				break;
			default:
				x -= textBounds.width()*0.5f;
				break;
		}
		return x;
	}
	
	private float computeTextYPosition(VALIGN va, float y, Paint textPaint) {
		
		switch (va) {
			case TOP:
				y -= textPaint.getTextSize()+0f;
				break;
			case BOTTOM:
				y += 3f;
				break;
			default:
				y -= textPaint.getTextSize()*0.5f;
				break;
		}
		return -y;
	}
	
	public void drawAlignedTextUnscaled(String txt, HALIGN ha, VALIGN va,
			float x, float y, Paint textPaint) {
		
		textPaint.setTextAlign(Align.LEFT);
		x = computeTextXPosition(txt, ha, x, textPaint);
		y = computeTextYPosition(va, y, textPaint);
		canvas.scale(1, -1);
		canvas.drawText(txt, x, y, textPaint);
		canvas.scale(1, -1);
	}
	
	public void drawAlignedText(String txt, HALIGN ha, VALIGN va,
			float x, float y, Paint textPaint, boolean scaled) {
		
		float originalSize = textPaint.getTextSize();
		if (scaled)
			textPaint.setTextSize(originalSize * scale);
		
		textPaint.setTextAlign(Align.LEFT);
		x = computeTextXPosition(txt, ha, x*scale, textPaint);
		y = computeTextYPosition(va, y*scale, textPaint);
		canvas.scale(1, -1);
		canvas.drawText(txt, x, y, textPaint);
		canvas.scale(1, -1);
		
		textPaint.setTextSize(originalSize);
	}
	
	public void drawAlignedSubscriptedText(String txt, HALIGN ha, VALIGN va, int[] subIndeces,
			float x, float y, Paint textPaint, boolean scaled) {
		
		drawAlignedSubscriptedTextFraction(txt, null, ha, va, subIndeces, x, y, textPaint, null, scaled);
	}
	
	public void drawAlignedSubscriptedTextFraction(String txt, String denominator, HALIGN ha, VALIGN va, int[] subIndeces,
			float x, float y, Paint textPaint, Paint fractionPaint, boolean scaled) {
		
		float originalSize = textPaint.getTextSize();
		if (scaled)
			textPaint.setTextSize(originalSize * scale);
		
		textPaint.setTextAlign(Align.LEFT);
		x = computeTextXPosition(txt, ha, x*scale, textPaint);
		y = computeTextYPosition(va, y*scale, textPaint);
		final float startX = x;

		canvas.scale(1, -1);
		if (subIndeces != null) {
			final int len = txt.length();
			final int subILen = subIndeces.length;
			for (int i = 0; i < len; i++) {
				boolean subscriptChar = false;
				for (int j = 0; j < subILen; j++) {
					if (i == subIndeces[j]) {
						subscriptChar = true;
						break;
					}
				}
				if (subscriptChar) {
					canvas.drawText(txt, i, i+1, x, y+textPaint.getTextSize() * 0.3f, textPaint);
				} else {
					canvas.drawText(txt, i, i+1, x, y, textPaint);
				}
				textPaint.getTextBounds(txt, i, i+1, textBounds);
				x += textBounds.width();
			}
		} else {
			canvas.drawText(txt, x, y, textPaint);
		}
		canvas.scale(1, -1);

		textPaint.setTextSize(originalSize);
		
		if (denominator != null) {
			float yPos;
			if (scaled) {
				yPos = y+textPaint.getTextSize()*0.7f*scale;
			} else {
				yPos = y+textPaint.getTextSize()*0.7f;
			}
			canvas.drawLine(startX, -yPos, x, -yPos, fractionPaint);
			drawAlignedText(denominator, HALIGN.CENTER, VALIGN.TOP, (x+startX)*0.5f/scale, -yPos/scale, textPaint, scaled);
		}
	}
}