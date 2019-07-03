package visualizeFourierSeries;

public class complexNumber {
	
	private int real_part, imag_part; // Should be changed to double later but need to implement coordinate system first
	
	public complexNumber(int real, int imag) {
		real_part = real;
		imag_part = imag;
	}
	
	public int getRealPart() {
		return real_part;
	}
	
	public int getImagPart() {
		return imag_part;
	}
	
	public void setRealPart(int real) {
		real_part = real;
	}
	
	public void setImagPart(int imag) {
		imag_part = imag;
	}

}
