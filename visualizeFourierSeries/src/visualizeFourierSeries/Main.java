package visualizeFourierSeries;

import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

public class Main {
	
	public static appWindow app_window;
	
	public static void main(String args[]) {
		initialSetup();
		app_window = new appWindow("Fourier Series Visualization");
		
	}
	
	public static void initialSetup(){
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (UnsupportedLookAndFeelException e) {
			e.printStackTrace();
		}
		//imageInputFunctions.readImage("src\\visualizeFourierSeries\\output_manually_modified.png");
		mathematics.createShiftIndices();
	}

}
