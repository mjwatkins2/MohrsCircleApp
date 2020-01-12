package com.engineercalc.elasticity;

import com.engineercalc.elasticity.TensorHelper.Type;

public class StrainTensor2D extends Tensor2D {
	
	public StrainTensor2D() {
		super();
	}
	
	public StrainTensor2D(Tensor tensor) {
		super(tensor);
	}
	
	@Override
	public double getComponent(Component c) {
		double val = super.getComponent(c);
		if (c == Component.XY) {
			val *= 2.0;
		}
		return val;
	}
	
	@Override
	public void setComponent(Component c, double val) {
		if (c == Component.XY) {
			val = val/2.0;
		}
		super.setComponent(c, val);
	}
	
	@Override
	public double getMaxShear() {
		return super.getMaxShear()*2;
	}
	
	@Override
	public Type getType() {return Type.STRAIN;}

	@Override
	public StrainTensor3D convertTo3D() {
		StrainTensor3D tensor = new StrainTensor3D();
		tensor.copyFrom(this);
		return tensor;
	}

	@Override
	public StrainTensor2D convertTo2D() {
		return this;
	}

	@Override
	public StrainTensor2D convertToStrain(ElasticMaterial material) {
		return this;
	}

	@Override
	public Tensor convertToStress(ElasticMaterial material) {
		StressTensor3D out = material.convertToStress(convertTo3D());
		out.copyExtraData(this);
		return out;
	}
}
