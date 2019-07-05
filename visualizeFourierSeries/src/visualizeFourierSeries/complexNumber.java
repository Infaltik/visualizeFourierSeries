package visualizeFourierSeries;

public class complexNumber {
	
	private double real_part, imag_part;
	
	public complexNumber(double real, double imag) {
		real_part = real;
		imag_part = imag;
	}
	
	public double getRealPart() {
		return real_part;
	}
	
	public double getImagPart() {
		return imag_part;
	}
	
	public void setRealPart(double real) {
		real_part = real;
	}
	
	public void setImagPart(double imag) {
		imag_part = imag;
	}

}
