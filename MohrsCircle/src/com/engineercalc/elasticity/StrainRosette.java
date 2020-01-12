package com.engineercalc.elasticity;

import Jama.Matrix;

import com.engineercalc.elasticity.TensorHelper.Type;

/**
 * When first created, shows what the strain gauges would read given the angles.
 * When an angle is changed, the strain gauges remain the same value (changing the underlying tensor).
 */
public class StrainRosette extends StrainTensor2D {
	
	// Used for storing input until a valid input is given.
	// Then the valid input is passed to the underlying tensor,
	// at which point this should mirror the underlying data.
	private double[] tempAngles = {0,0,0};
	private double[] tempEs = {0,0,0};
	private boolean isValid = false;
	
	public StrainRosette() {
		super();
	}
	
	public StrainRosette(Tensor tensor) {
		super(tensor);
	}
	
	@Override
	public Type getType() {return Type.STRAINROSETTE;}
	
	@Override
	public void copyFrom(Tensor tensor) {
		super.copyFrom(tensor);
		setTempInputFromTensor();
	}
	
	@Override
	protected void copyExtraData(Tensor tensor) {
		super.copyExtraData(tensor);
		setTempInputFromTensor();
	}
	
	@Override
	public void setArray(double[] arr) {
		super.setArray(arr);
		setTempInputFromTensor();
	}
	
	/*
	 * Set the temp input angles and Es from the saved angles
	 * and the underlying tensor as if the user had inputted them.
	 * Then recompute the tensor to see if the input (the angles, 
	 * really) is a valid set of input.
	 */
	private void setTempInputFromTensor() {
		for (int i = 0; i < 3; i++) {
			tempAngles[i] = extraData[i];
		}
		tempEs[0] = getEAtAnyAngle(extraData[0]);
		tempEs[1] = getEAtAnyAngle(extraData[0]+extraData[1]);
		tempEs[2] = getEAtAnyAngle(extraData[0]+extraData[1]+extraData[2]);
		tryComputingTensorFromTempInput();
	}
	
	public void setEa(double Ea) {
		tempEs[0] = Ea;
		tryComputingTensorFromTempInput();
	}
	
	public void setEb(double Eb) {
		tempEs[1] = Eb;
		tryComputingTensorFromTempInput();
	}
	
	public void setEc(double Ec) {
		tempEs[2] = Ec;
		tryComputingTensorFromTempInput();
	}
	
	public void setAlpha(double angle) {
		tempAngles[0] = angle;
		tryComputingTensorFromTempInput();
	}
	
	public void setBeta(double angle) {
		tempAngles[1] = angle;
		tryComputingTensorFromTempInput();
	}
	
	public void setGamma(double angle) {
		tempAngles[2] = angle;
		tryComputingTensorFromTempInput();
	}
	
	private double getEAtAnyAngle(double angle) {
		final double Ex = getComponent(Component.XX);
		final double Ey = getComponent(Component.YY);
		final double Exy = getTensorShear(Component.XY);
		final double ca = Math.cos(2*rad(angle));
		final double sa = Math.sin(2*rad(angle));
		
		return (Ex+Ey)/2 + (Ex-Ey)/2 * ca + Exy*sa;
	}
	
	public double getEa() {
		if (isValid) {
			return getEAtAnyAngle(getAlpha());
		} else {
			return tempEs[0];
		}
	}
	
	public double getEb() {
		if (isValid) {
			return getEAtAnyAngle(getAlpha()+getBeta());
		} else {
			return tempEs[1];
		}
	}
	
	public double getEc() {
		if (isValid) {
			return getEAtAnyAngle(getAlpha()+getBeta()+getGamma());
		} else {
			return tempEs[2];
		}
	}
	
	public double getAlpha() {
		return tempAngles[0];
	}
	
	public double getBeta() {
		return tempAngles[1];
	}
	
	public double getGamma() {
		return tempAngles[2];
	}
	
	private double rad(double degrees) {
		return degrees*Math.PI/180;
	}
	
	private void tryComputingTensorFromTempInput() {
		final double alpha = tempAngles[0];
		final double beta = tempAngles[1];
		final double gamma = tempAngles[2];
		final double ang1 = alpha;
		final double ang2 = alpha + beta;
		final double ang3 = alpha + beta + gamma;

		// If two gauges overlap or are 180 degrees off, invalid
		final double TOL2 = 1e-3;
		if (Math.abs(ang1 - ang2) < TOL2 ||
				Math.abs(ang1 - ang3) < TOL2 ||
				Math.abs(ang2 - ang3) < TOL2 ||
				Math.abs(180 - Math.abs(ang1 - ang2)) < TOL2 ||
				Math.abs(180 - Math.abs(ang1 - ang3)) < TOL2 ||
				Math.abs(180 - Math.abs(ang2 - ang3)) < TOL2 || 
				Math.abs(360 - Math.abs(ang1 - ang2)) < TOL2 ||	// there should be a better way to do this...
				Math.abs(360 - Math.abs(ang1 - ang3)) < TOL2 ||
				Math.abs(360 - Math.abs(ang2 - ang3)) < TOL2) {
			isValid = false;
			return;
		}
		
		// The all-zeros case is valid
		final double TOL = 1e-10;
		if (Math.abs(tempEs[0]) + Math.abs(tempEs[1]) + Math.abs(tempEs[2]) < TOL) {
			isValid = true;
			extraData[0] = alpha;
			extraData[1] = beta;
			extraData[2] = gamma;
			super.setComponent(Component.XX, 0);
			super.setComponent(Component.YY, 0);
			super.setComponent(Component.XY, 0);
			return;
		}
		
		// System of equations: [A]*{Ex Ey Ez}' = {Ea Eb Ec}'
		final double ca = Math.cos(2*rad(alpha));
		final double cb = Math.cos(2*rad(alpha+beta));
		final double cg = Math.cos(2*rad(alpha+beta+gamma));
		final double sa = Math.sin(2*rad(alpha));
		final double sb = Math.sin(2*rad(alpha+beta));
		final double sg = Math.sin(2*rad(alpha+beta+gamma));
		
		final double[][] Aarr = {{0.5*(1+ca), 0.5*(1-ca), sa},
				  			  {0.5*(1+cb), 0.5*(1-cb), sb},
				  			  {0.5*(1+cg), 0.5*(1-cg), sg}};
		Matrix A = new Matrix(Aarr);
		
		final double[][] EabcArr = {{tempEs[0]},{tempEs[1]},{tempEs[2]}};
		Matrix Eabc = new Matrix(EabcArr);
		
		try {
			Matrix Exyz = A.solve(Eabc);
			
			// If we made it this far, then the system was valid for solving.
			isValid = true;
			extraData[0] = alpha;
			extraData[1] = beta;
			extraData[2] = gamma;
			
			super.setComponent(Component.XX, Exyz.get(0, 0));
			super.setComponent(Component.YY, Exyz.get(1, 0));
			super.setComponent(Component.XY, 2*Exyz.get(2, 0));
		} catch (Exception e) {
			isValid = false;
			// Leave the temp input values the same so that the user can edit additional fields
			// and make the input rosette valid without losing data.
		}
	}
	
	public boolean isValid() {return isValid;}

	@Override
	public StrainTensor2D convertTo2D() {
		StrainTensor2D tensor = new StrainTensor2D();
		tensor.copyFrom(this);
		return tensor;
	}

	@Override
	public StrainTensor2D convertToStrain(ElasticMaterial material) {
		StrainTensor2D tensor = new StrainTensor2D();
		tensor.copyFrom(this);
		return tensor;
	}

	@Override
	public StressTensor2D convertToStress(ElasticMaterial material) {
		StressTensor2D out = material.convertToStress(this);
		out.copyExtraData(this);
		return out;
	}

	@Override
	public StrainRosette convertToRosette(ElasticMaterial material) {
		return this;
	}
}
