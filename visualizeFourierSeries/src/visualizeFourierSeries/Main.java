package visualizeFourierSeries;

public class Main {
	
	public static appWindow app_window;
	
	public static void main(String args[]) {
		initialSetup();
		app_window = new appWindow("Fourier Series Visualization");
		
	}
	
	public static void initialSetup(){
		imageInputFunctions.readImage("src/visualizeFourierSeries/fourierSeriesTestImage.jpg");
		mathematics.createShiftIndices();
	}

}
