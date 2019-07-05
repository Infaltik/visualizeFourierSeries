package visualizeFourierSeries;

public class Main {
	
	public static appWindow app_window;
	
	public static void main(String args[]) {
		
		double[][] testMatrix1 = {{7, 13},
								 {3, 15}};
		double[][] testMatrix2 = {{2, 5},
				 				  {19, 11}};
		double[][] resultMatrix = mathematics.matrixMultiplication2x2(testMatrix2, testMatrix1);
		
		//mathematics.print2x2MatrixToConsole(rot);
		
		app_window = new appWindow("Fourier Series Visualization");
		
	}

}
