package visualizeFourierSeries;

public class ComplexNumber {
	
	private double real_part, imag_part;
	
	public ComplexNumber(double real, double imag) {
		real_part = real;
		imag_part = imag;
	}
	
	public ComplexNumber(double cmplxExpFcnInput){
		// Complex number from complex exponential function
		real_part = Math.cos(cmplxExpFcnInput);
		imag_part = Math.sin(cmplxExpFcnInput);
	}
	
	public ComplexNumber(double magnitude, double argument_in_radians, boolean dummy_input){
		// Complex number given from polar form. (Dummy input used to be able to overload constructor)
		real_part = magnitude*Math.cos(argument_in_radians);
		imag_part = magnitude*Math.sin(argument_in_radians);
	}
	
	public double getMagnitude(){
		return Math.sqrt(real_part*real_part + imag_part*imag_part);
	}
	
	public double getArgument(){
		return Math.atan2(imag_part, real_part);
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
	
	public String toString() {
		String s = "complexNumber[" + real_part + ", " + imag_part + "i]";
		return s;
	}

}
