package visualizeFourierSeries;

import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

public class Main {
	
	public static AppWindow app_window;
	
	public static void main(String args[]) {
		initialSetup();
		app_window = new AppWindow("Fourier Series Visualization");
		
	}
	
	public static void initialSetup(){
		ImageInputFunctions.lookForSavedImageData();
		
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
		//mathematics.createShiftIndices();
	}

}
