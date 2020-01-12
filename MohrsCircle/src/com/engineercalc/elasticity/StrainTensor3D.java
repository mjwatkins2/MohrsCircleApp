package com.engineercalc.elasticity;

import com.engineercalc.elasticity.TensorHelper.Type;

public class StrainTensor3D extends Tensor3D {

	public StrainTensor3D() {
		super();
	}
	
	public StrainTensor3D(Tensor tensor) {
		super(tensor);
	}

	@Override
	public double getComponent(Component c) {
		double val = super.getComponent(c);
		if (c == Component.XY || c == Component.YZ || c == Component.ZX) {
			val *= 2.0;
		}
		return val;
	}

	@Override
	public void setComponent(Component c, double val) {
		if (c == Component.XY || c == Component.YZ || c == Component.ZX) {
			val = val/2.0;
		}
		super.setComponent(c, val);
	}
	
	@Override
	public double getMaxShear() {
		return super.getMaxShear()*2.0;
	}
	
	@Override
	public Type getType() {return Type.STRAIN;}

	@Override
	public StrainTensor3D convertTo3D() {
		return this;
	}

	@Override
	public StrainTensor2D convertTo2D() {
		StrainTensor2D tensor = new StrainTensor2D();
		tensor.copyFrom(this);
		return tensor;
	}

	@Override
	public StrainTensor3D convertToStrain(ElasticMaterial material) {
		return this;
	}

	@Override
	public StressTensor3D convertToStress(ElasticMaterial material) {
		StressTensor3D out = material.convertToStress(this);
		out.copyExtraData(this);
		return out;
	}
	
	@Override
	protected double[][] getTensor() {
		final double[][] array = {{getXX(),     getXY()/2.0, getZX()/2.0},
								  {getXY()/2.0, getYY(),     getYZ()/2.0},
								  {getZX()/2.0, getYZ()/2.0, getZZ()}};
		return array;
	}
}
