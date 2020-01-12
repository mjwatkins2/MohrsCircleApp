package com.engineercalc.elasticity;

import com.engineercalc.elasticity.TensorHelper.Type;

public class StressTensor2D extends Tensor2D {

	public StressTensor2D() {
		super();
	}
	
	public StressTensor2D(Tensor tensor) {
		super(tensor);
	}

	@Override
	public Type getType() {return Type.STRESS;}
	
	@Override
	public StressTensor3D convertTo3D() {
		StressTensor3D tensor = new StressTensor3D();
		tensor.copyFrom(this);
		return tensor;
	}

	@Override
	public StressTensor2D convertTo2D() {
		return this;
	}

	@Override
	public StrainTensor3D convertToStrain(ElasticMaterial material) {
		StrainTensor3D out = material.convertToStrain(convertTo3D());
		out.copyExtraData(this);
		return out;
	}

	@Override
	public StressTensor2D convertToStress(ElasticMaterial material) {
		return this;
	}
	
	@Override
	public StrainRosette convertToRosette(ElasticMaterial material) {
		// Convert directly to rosette assuming out of plane terms are all zero
		StrainRosette SR = new StrainRosette();
		SR.copyFrom(material.convertToStrain(this));
		return SR;
	}
}
