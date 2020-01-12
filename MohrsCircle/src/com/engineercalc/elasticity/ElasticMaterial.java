package com.engineercalc.elasticity;

public class ElasticMaterial {

	public enum Constant {
		E(0), V(1), G(2), K(3), L(4);
		private final int value;
		private Constant(int value) { this.value = value;}
	};
	
	private double E, v, G, K, L;

	public ElasticMaterial() {
		// set up some defaults
		E = 1e7;
		v = 0.3;
	}
	
	public double getE() { return E;}
	public double getV() { return v;}
	public double getG() { return G;}
	public double getK() { return K;}
	public double getL() { return L;}
	
	public double getConstant(Constant constant) {
		switch (constant) {
			case E:
				return E;
			case V:
				return v;
			case G:
				return G;
			case K:
				return K;
			case L:
				return L;
			default:
				return Double.NEGATIVE_INFINITY;
		}
	}

	public void setConstants(Constant constant1, double value1, Constant constant2, double value2) throws InvalidConstantException {
		// Make the switch statements a little easier by reducing permutations
		if (constant1 == constant2) {
			throw new InvalidConstantException("The two inputs must be different constants.");
		} else if (constant2.value < constant1.value) {
			final Constant tempC = constant1;
			final double tempV = value1;
			constant1 = constant2;
			value1 = value2;
			constant2 = tempC;
			value2 = tempV;
		}
		
		try {
			switch (constant1) {
				case E:
					switch (constant2) {
						case V:
							setConstantsEV(value1, value2);
							break;
						case G:
							setConstantsEG(value1, value2);
							break;
						case K:
							setConstantsEK(value1, value2);
							break;
						case L:
							setConstantsEL(value1, value2);
							break;
						default:
							throw new InvalidConstantException("Unrecognized input constant.");
					}
					break;
				case V:
					switch (constant2) {
						case G:
							setConstantsVG(value1, value2);
							break;
						case K:
							setConstantsVK(value1, value2);
							break;
						case L:
							setConstantsVL(value1, value2);
							break;
						default:
							throw new InvalidConstantException("Unrecognized input constant.");
					}
					break;
				case G:
					switch (constant2) {
						case K:
							setConstantsGK(value1, value2);
							break;
						case L:
							setConstantsGL(value1, value2);
							break;
						default:
							throw new InvalidConstantException("Unrecognized input constant.");
					}
					break;
				case K:
					switch (constant2) {
						case L:
							setConstantsKL(value1, value2);
							break;
						default:
							throw new InvalidConstantException("Unrecognized input constant.");
					}
					break;
				default:
					throw new InvalidConstantException("Unrecognized input constant.");
			}
		} catch (InvalidConstantException e) {
			throw e;
		} catch (Exception e) {
			throw new InvalidConstantException("Invalid constants.");
		}
	}
	
	private void setConstantsEG(double E, double G) throws InvalidConstantException {
		if (G <= 0) {
			throw new InvalidConstantException("G must be greater than 0");
		}
		this.E = E;
		v = (E-2*G)/(2*G);
		setConstantsEV(E, v);
		this.G = G;
	}
	
	private void setConstantsEK(double E, double K) throws InvalidConstantException {
		if (K <= 0) {
			throw new InvalidConstantException("K must be greater than 0");
		}
		this.E = E;
		v = (3*K-E)/(6*K);
		setConstantsEV(E, v);
		this.K = K;
	}
	
	private void setConstantsEL(double E, double L) throws InvalidConstantException {
		if (E == 0 && L == 0) {
			throw new InvalidConstantException("E and L cannot both be 0");
		}
		this.E = E;
		double R = Math.sqrt(E*E + 9*L*L +2*E*L);
		v = 2*L/(E+L+R);
		setConstantsEV(E, v);
		this.L = L;
	}
	
	private void setConstantsVG(double v, double G) throws InvalidConstantException {
		E = 2*G*(1+v);
		this.v = v;
		setConstantsEV(E, v);
		this.G = G;
	}
	
	private void setConstantsVK(double v, double K) throws InvalidConstantException {
		E = 3*K*(1-2*v);
		this.v = v;
		setConstantsEV(E, v);
		this.K = K;
	}
	
	private void setConstantsVL(double v, double L) throws InvalidConstantException {
		if (v <= 0) {
			throw new InvalidConstantException("v must be greater than 0");
		}
		E = L*(1+v)*(1-2*v)/v;
		this.v = v;
		setConstantsEV(E, v);
		this.L = L;
	}
	
	private void setConstantsGK(double G, double K) throws InvalidConstantException {
		if (3*K+G == 0) {
			throw new InvalidConstantException("3*K+G cannot equal 0");
		} else if (6*K+2*G == 0) {
			throw new InvalidConstantException("6*K+2*G cannot equal 0");
		}
		E = 9*K*G/(3*K+G);
		v = (3*K-2*G)/(6*K+2*G);
		setConstantsEV(E, v);
		this.G = G;
		this.K = K;
	}
	
	private void setConstantsGL(double G, double L) throws InvalidConstantException {
		if (L+G == 0) {
			throw new InvalidConstantException("L+G cannot equal 0");
		}
		E = G*(3*L+2*G)/(L+G);
		v = L/2/(L+G);
		setConstantsEV(E, v);
		this.G = G;
		this.L = L;
	}
	
	private void setConstantsKL(double K, double L) throws InvalidConstantException {
		if (3*K-L == 0) {
			throw new InvalidConstantException("3*K-L cannot equal 0");
		}
		E = 9*K*(K-L)/(3*K-L);
		v = L/(3*K-L);
		setConstantsEV(E, v);
		this.K = K;
		this.L = L;
	}
	
	private void setConstantsEV(double E, double v) throws InvalidConstantException {
		
		if (E <= 0) {
			throw new InvalidConstantException("E must be greater than 0");
		} else if (v < 0) {
			throw new InvalidConstantException("v must be greater than 0");
		} else if (v >= 0.5) {
			throw new InvalidConstantException("v must be less than 1/2");
		}
		this.E = E;
		this.v = v;
		G = E/2/(1+v);
		K = E/3/(1-2*v);
		L = E*v/(1+v)/(1-2*v);
	}
	
	public class InvalidConstantException extends Exception {
		private static final long serialVersionUID = 3108783131858659516L;

		public InvalidConstantException(String err) {
			super(err);
		}
	}
	
	/**
	 * Assumes plane stress
	 */
	public StressTensor2D convertToStress(StrainTensor2D strain) {

		StressTensor2D stress = new StressTensor2D(strain);
		final double coeff = E/(1-v*v);
		stress.setXX(coeff * (strain.getXX() + v*strain.getYY()));
		stress.setYY(coeff * (strain.getYY() + v*strain.getXX()));
		stress.setXY(G*strain.getXY());
		return stress;
	}
	
	public StressTensor3D convertToStress(StrainTensor3D strain) {
		
		StressTensor3D stress = new StressTensor3D(strain);
		final double coeff = E/(1+v)/(1-2*v);
		stress.setXX(coeff * ((1-v)*strain.getXX() + v*(strain.getYY() + strain.getZZ())));
		stress.setYY(coeff * ((1-v)*strain.getYY() + v*(strain.getXX() + strain.getZZ())));
		stress.setZZ(coeff * ((1-v)*strain.getZZ() + v*(strain.getXX() + strain.getYY())));
		stress.setXY(G*strain.getXY());
		stress.setYZ(G*strain.getYZ());
		stress.setZX(G*strain.getZX());
		return stress;
	}
	
	/**
	 * Assumes plane stress
	 */
	public StrainTensor2D convertToStrain(StressTensor2D stress) {
		
		StrainTensor2D strain = new StrainTensor2D(stress);
		final double coeff = 1/E;
		strain.setXX(coeff * (stress.getXX() - v*stress.getYY()));
		strain.setYY(coeff * (stress.getYY() - v*stress.getXX()));
		strain.setXY(stress.getXY()/G);
		return strain;
	}
	
	public StrainTensor3D convertToStrain(StressTensor3D stress) {
		
		StrainTensor3D strain = new StrainTensor3D(stress);
		final double coeff = 1/E;
		strain.setXX(coeff * (stress.getXX() - v*(stress.getYY() + stress.getZZ())));
		strain.setYY(coeff * (stress.getYY() - v*(stress.getXX() + stress.getZZ())));
		strain.setZZ(coeff * (stress.getZZ() - v*(stress.getXX() + stress.getYY())));
		strain.setXY(stress.getXY()/G);
		strain.setYZ(stress.getYZ()/G);
		strain.setZX(stress.getZX()/G);
		return strain;
	}
}
