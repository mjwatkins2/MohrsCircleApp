package com.engineercalc.elasticity;

import com.engineercalc.elasticity.TensorHelper.Type;

public class StressTensor3D extends Tensor3D {

	public StressTensor3D() {
		super();
	}
	
	public StressTensor3D(Tensor tensor) {
		super(tensor);
	}

	@Override
	protected double[][] getTensor() {
		final double[][] array = {{getXX(), getXY(), getZX()},
		  {getXY(), getYY(), getYZ()},
		  {getZX(), getYZ(), getZZ()}};
		return array;
	}
	
	@Override
	public Type getType() {return Type.STRESS;}
	
	@Override
	public StressTensor3D convertTo3D() {
		return this;
	}

	@Override
	public StressTensor2D convertTo2D() {
		StressTensor2D tensor = new StressTensor2D();
		tensor.copyFrom(this);
		return tensor;
	}

	@Override
	public StrainTensor3D convertToStrain(ElasticMaterial material) {
		StrainTensor3D out = material.convertToStrain(this);
		out.copyExtraData(this);
		return out;
	}

	@Override
	public StressTensor3D convertToStress(ElasticMaterial material) {
		return this;
	}
}
