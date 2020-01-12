package com.engineercalc.elasticity;

import java.util.Arrays;
import java.util.Comparator;

import com.engineercalc.elasticity.TensorHelper.Dimension;

import Jama.EigenvalueDecomposition;
import Jama.Matrix;

public abstract class Tensor3D extends Tensor {

	private double S1, S2, S3, Tmax, Seq, I1, I2, I3;
	private double[] princDir1, princDir2, princDir3;
	
	public Tensor3D() {
		super();
	}
	
	public Tensor3D(Tensor tensor) {
		super(tensor);
	}
	
	@Override
	public Dimension getDimension() {return Dimension.DIM3D;}
	
	protected abstract double[][] getTensor();
	
	@Override
	protected void doComputedProperties() {
		if (!dirty)
			return;
		
		hydrostatic = testHydrostatic();
		
		if (hydrostatic) {
			final double Sxx = getXX();
			S1 = Sxx;
			S2 = Sxx;
			S3 = Sxx;
			Tmax = 0.0;
			Seq = 0.0;
			final double[] nullVector = {0.0, 0.0, 0.0};
			princDir1 = nullVector;
			princDir2 = nullVector;
			princDir3 = nullVector;
		} else {
			final EigenvalueDecomposition eig = new Matrix(getTensor()).eig();
			final double[] eigenValues = eig.getRealEigenvalues();
			final Matrix eigenVectors = eig.getV();
			
			// Columns are the eigenvectors
			double[][] princDirs = new double[3][];
			
			Matrix eigV1 = eigenVectors.getMatrix(0, 2, 0, 0);
			eigV1 = eigV1.times(1.0/eigV1.normF());
			princDirs[0] = eigV1.getColumnPackedCopy();
			
			Matrix eigV2 = eigenVectors.getMatrix(0, 2, 1, 1);
			eigV2 = eigV2.times(1.0/eigV2.normF());
			princDirs[1] = eigV2.getColumnPackedCopy();
			
			Matrix eigV3 = eigenVectors.getMatrix(0, 2, 2, 2);
			eigV3 = eigV3.times(1.0/eigV3.normF());
			princDirs[2] = eigV3.getColumnPackedCopy();

			Integer[] idx = {0, 1, 2};
			if (!((eigenValues[2] >= eigenValues[1]) && (eigenValues[1] >= eigenValues[0]))) {	
				//Arrays.sort(eigenValues);
				Arrays.sort(idx, new Comparator<Integer>() {
					@Override
					public int compare(Integer o1, Integer o2) {
						return Double.compare(eigenValues[o1], eigenValues[o2]);
					}
				});
			}
				
			S1 = eigenValues[idx[2]];
			S2 = eigenValues[idx[1]];
			S3 = eigenValues[idx[0]];
			princDir1 = princDirs[idx[2]];
			princDir2 = princDirs[idx[1]];
			princDir3 = princDirs[idx[0]];
			
			Tmax = (S1 - S3)/2.0;
			Seq = Math.sqrt(0.5 * ((S1-S2)*(S1-S2) + (S2-S3)*(S2-S3) + (S3-S1)*(S3-S1)));
		}
		
		I1 = S1 + S2 + S3;
		I2 = S1*S2 + S2*S3 + S3*S1;
		I3 = S1*S2*S3;
		
		dirty = false;
	}
	
	private boolean testHydrostatic() {
		final double Sxx = getXX();
		final double Syy = getYY();
		final double Szz = getZZ();
		final double Sxy = getXY();
		final double Syz = getYZ();
		final double Szx = getZX();
		final double avg = (Sxx + Syy + Szz) / 3.0;
		final double min = Math.abs(avg * HYDROSTATIC_TOL);
		
		// Check: axials do not vary from average and shears are zero
		if (Math.abs(Sxx - avg) <= min && Math.abs(Syy - avg) <= min && Math.abs(Szz - avg) <= min &&
				Math.abs(Sxy) <= min && Math.abs(Syz) <= min && Math.abs(Szx) <= min) {
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
	
	public double getThirdPrincipal() {
		doComputedProperties();
		return S3;
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
	
	public double[] getFirstPrincipalDirection() {
		doComputedProperties();
		return princDir1;
	}
	
	public double[] getSecondPrincipalDirection() {
		doComputedProperties();
		return princDir2;
	}
	
	public double[] getThirdPrincipalDirection() {
		doComputedProperties();
		return princDir3;
	}
	
	public double getFirstInvariant() {
		doComputedProperties();
		return I1;
	}
	
	public double getSecondInvariant() {
		doComputedProperties();
		return I2;
	}
	
	public double getThirdInvariant() {
		doComputedProperties();
		return I3;
	}
}
