package visualizeFourierSeries;

public class Main {
	
	public static appWindow app_window;
	
	public static void main(String args[]) {
		
		mathematics.createShiftIndices();
		app_window = new appWindow("Fourier Series Visualization");
		
	}

}
