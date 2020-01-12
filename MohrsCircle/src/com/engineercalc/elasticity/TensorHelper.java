package com.engineercalc.elasticity;

public final class TensorHelper {

	public enum Type {
		STRESS(0), STRAIN(1), STRAINROSETTE(2);
		protected final int value;
		private Type(int value) { this.value = value;}
	};
	
	public enum Dimension {
		DIM2D(0), DIM3D(1);
		protected final int value;
		private Dimension(int value) { this.value = value;}
	};
	
	public static double[] getSaveData(Tensor tensor) {
		double[] arr = tensor.getArray();
		
		double[] out = new double[arr.length+2];
		for (int i = 0; i < arr.length; i++) {
			out[i] = arr[i];
		}
		
		out[out.length-2] = tensor.getType().value;
		out[out.length-1] = tensor.getDimension().value;
		return out;
	}

	public static Tensor tensorFromSaveData(double[] in) {
		Tensor tensor;
		Dimension dim;
		Type type;
		
		try {
			int typeInt = (int)in[in.length-2];
			if (typeInt == Type.STRESS.value) {
				type = Type.STRESS;
			} else if (typeInt == Type.STRAIN.value) {
				type = Type.STRAIN;
			} else if (typeInt == Type.STRAINROSETTE.value) {
				type = Type.STRAINROSETTE;
			} else {
				return null;
			}
			int dimInt = (int)in[in.length-1];
			if (dimInt == Dimension.DIM2D.value) {
				dim = Dimension.DIM2D;
			} else if (dimInt == Dimension.DIM3D.value) {
				dim = Dimension.DIM3D;
			} else {
				return null;
			}
		} catch (Exception e) {
			dim = Dimension.DIM2D;
			type = Type.STRESS;
		}

		tensor = fromDimType(dim, type);
		
		double[] arr = new double[in.length-2];
		for (int i = 0; i < in.length-2; i++) {
			arr[i] = in[i];
		}
		
		tensor.setArray(arr);
		
		return tensor;
	}
	
	public static Tensor fromDimType(Dimension dim, Type type) {
		switch (dim) {
			case DIM2D:
				switch (type) {
					case STRAIN:
						return new StrainTensor2D();
					case STRESS:
						return new StressTensor2D();
					case STRAINROSETTE:
						return new StrainRosette();
				}
				break;
			case DIM3D:
				switch (type) {
					case STRAIN:
						return new StrainTensor3D();
					case STRESS:
						return new StressTensor3D();
					case STRAINROSETTE:
						return new StrainRosette();
				}
				break;
		}
		return null;
	}
	
}
