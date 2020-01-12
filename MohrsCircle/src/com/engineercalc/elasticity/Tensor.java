package com.engineercalc.elasticity;

import com.engineercalc.elasticity.TensorHelper.Dimension;
import com.engineercalc.elasticity.TensorHelper.Type;

public abstract class Tensor {
	
	public enum Component {
		XX(0), YY(1), ZZ(2), XY(3), YZ(4), ZX(5);
		protected final int value;
		private Component(int value) { this.value = value;}
	};
	
	protected static final double HYDROSTATIC_TOL = 1e-8;
	
	// Tensor input properties
	private double[] components = new double[6];
	
	// Computed values
	protected boolean hydrostatic = false;
	protected boolean dirty = true;
	
	/* Extra data that is be saved and reloaded
		Reserved:
		0 alpha
		1 beta
	 	2 gamma
	*/
	protected double[] extraData = {0, 45, 45};
	
	protected Tensor() {
	}
	
	/**
	 * Copies any extra data from the tensor
	 * @param tensor
	 */
	protected Tensor(Tensor tensor) {
		copyExtraData(tensor);
	}
	
	public void copyFrom(Tensor tensor) {
		// Can't use components[] array here so as to prevent nonzero values from slipping out of 3D-only components.
		setXX(tensor.getXX());
		setYY(tensor.getYY());
		setZZ(tensor.getZZ());
		setXY(tensor.getXY());
		setYZ(tensor.getYZ());
		setZX(tensor.getZX());
		copyExtraData(tensor);
		dirty = true;
	}
	
	protected void copyExtraData(Tensor tensor) {
		for (int i = 0; i < Math.min(extraData.length, tensor.extraData.length); i++) {
			extraData[i] = tensor.extraData[i];
		}
	}

	public double getXX() {return getComponent(Component.XX);}
	public double getYY() {return getComponent(Component.YY);}
	public double getZZ() {return getComponent(Component.ZZ);}
	public double getXY() {return getComponent(Component.XY);}
	public double getYZ() {return getComponent(Component.YZ);}
	public double getZX() {return getComponent(Component.ZX);}
	
	protected double getComponent(Component c) {
		return components[c.value];
	}

	public double getTensorShear(Component c) {
		switch (c) {
			case XY:
			case YZ:
			case ZX:
				return components[c.value];
			default:
				return Double.NaN;
		}
	}
	
	public void setXX(double val) {setComponent(Component.XX, val);}
	public void setYY(double val) {setComponent(Component.YY, val);}
	public void setZZ(double val) {setComponent(Component.ZZ, val);}
	public void setXY(double val) {setComponent(Component.XY, val);}
	public void setYZ(double val) {setComponent(Component.YZ, val);}
	public void setZX(double val) {setComponent(Component.ZX, val);}
	
	/**
	 * Set a component. If shear strain, use engineering shear strain.
	 * @param c
	 * @param val
	 */
	public void setComponent(Component c, double val) {
		components[c.value] = val;
		dirty = true;
	}
	
	public abstract Dimension getDimension();
	public abstract Type getType();
	public abstract Tensor convertTo3D();
	public abstract Tensor convertTo2D();
	public abstract Tensor convertToStrain(ElasticMaterial material);
	public abstract Tensor convertToStress(ElasticMaterial material);
	public StrainRosette convertToRosette(ElasticMaterial material) {
		// Convert to strain, then to 2D, then to rosette.
		StrainRosette SR = new StrainRosette();
		SR.copyFrom(convertToStrain(material).convertTo2D());
		return SR;
	}
	
	protected abstract void doComputedProperties();
	
	public boolean isHydrostatic() {
		if (dirty)
			doComputedProperties();
		return hydrostatic;
	}
	
	/**
	 * Can be used to construct a new Tensor(double[] arr)
	 * @return An array that is readable by Tensor(double[] arr)
	 */
	public double[] getArray() {
		double[] arr = new double[components.length + extraData.length];
		for (int i = 0; i < components.length; i++) {
			arr[i] = components[i];
		}
		for (int i = 0; i < extraData.length; i++) {
			arr[i + components.length] = extraData[i];
		}
		
		return arr;
	}

	public void setArray(double[] arr) {
		int maxI1 = Math.min(arr.length, components.length);		// Be sure no out-of-bounds
		for (int i = 0; i < maxI1; i++) {
			 components[i] = arr[i];
		}
		int maxI2 = Math.min(arr.length - maxI1, extraData.length);
		for (int i = 0; i < maxI2; i++) {
			 extraData[i] = arr[i + maxI1];
		}
		dirty = true;
	}
}
