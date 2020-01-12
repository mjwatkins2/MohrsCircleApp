package com.engineercalc.elasticity;

import com.engineercalc.elasticity.TensorHelper.Dimension;

public abstract class Tensor2D extends Tensor {

	private double S1, S2, Tmax, Seq, theta;

	public Tensor2D() {
		super();
	}
	
	public Tensor2D(Tensor tensor) {
		super(tensor);
	}
	
	@Override
	public double getZZ() {return 0.0;}
	@Override
	public double getYZ() {return 0.0;}
	@Override
	public double getZX() {return 0.0;}

	@Override
	public double getTensorShear(Component c) {
		double val = 0;
		if (c == Component.XY) {
			val = super.getTensorShear(c);
		}
		return val;
	}
	
	@Override
	public void setComponent(Component c, double val) {
		if (c == Component.ZX || c == Component.YZ || c == Component.ZZ) {
			return;
		}
		super.setComponent(c, val);
	}
	
	@Override
	public void setZZ(double val) {return;}
	@Override
	public void setYZ(double val) {return;}
	@Override
	public void setZX(double val) {return;}

	@Override
	public Dimension getDimension() {return Dimension.DIM2D;}
	
	@Override
	protected void doComputedProperties() {
		if (!dirty)
			return;
		
		hydrostatic = testHydrostatic();

		final double Sxx = getXX();
		final double Syy = getYY();
		final double Sxy = getTensorShear(Component.XY);
		final double avg = (Sxx + Syy) / 2.0;
		final double diff = (Sxx - Syy) / 2.0;
		final double R = Math.sqrt(diff*diff + Sxy*Sxy);
		
		S1 = avg+R;
		S2 = avg-R;
		Tmax = R;
		Seq = Math.sqrt(S1*S1 - S1*S2 + S2*S2);

		theta = 180.0/2.0/Math.PI * Math.atan2(2*Sxy, Sxx - Syy);
		
		dirty = false;
	}
	
	private boolean testHydrostatic() {
		final double Sxx = getXX();
		final double Syy = getYY();
		final double Sxy = getXY();
		final double avg = (Sxx + Syy) / 2.0;
		final double min = Math.abs(avg * HYDROSTATIC_TOL);
		
		if (Math.abs(Sxx - Syy) <= HYDROSTATIC_TOL && Math.abs(Sxy) <= min) {
			return true;
		}
		
		return false;
	}
	
	public double getFirstPrincipal() {
		doComputedProperties();
		return S1;
	}
	
	public double getSecondPrincipal() {
		doComputedProperties();
		return S2;
	}
	
	public double getMaxShear() {
		doComputedProperties();
		return Tmax;
	}

	final public double getMaxTensorShear() {
		doComputedProperties();
		return Tmax;
	}
	
	public double getVonMises() {
		doComputedProperties();
		return Seq;
	}
	
	public double getPrincipalAngle2D() {
		doComputedProperties();
		return theta;
	}
	
	public double[] getFirstPrincipalDirection() {
		doComputedProperties();
		if (hydrostatic) {
			final double[] nullVector = {0.0, 0.0};
			return nullVector;
		} else {
			final double[] dir = {Math.cos(theta*Math.PI/180.0), Math.sin(theta*Math.PI/180.0)};
			return dir;
		}
	}
	
	public double[] getSecondPrincipalDirection() {
		doComputedProperties();
		if (hydrostatic) {
			final double[] nullVector = {0.0, 0.0};
			return nullVector;
		} else {
			final double[] dir = {Math.cos(theta*Math.PI/180.0 + Math.PI/2), Math.sin(theta*Math.PI/180.0 + Math.PI/2)};
			return dir;
		}
	}
}