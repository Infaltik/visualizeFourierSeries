package visualizeFourierSeries;

import java.awt.Point;
import java.util.Collections;

public class renderLoop implements Runnable{
	
	public static int TARGET_FPS = 200;
	public static long RENDER_WAIT_TIME = 1000000000/TARGET_FPS;
	public boolean should_stop_thread = false;
	
	public void run(){
		if(Main.app_window.should_calculate_fourier_coefficients) {
			mathematics.calculateFourierSeriesCoefficients();
			Main.app_window.should_calculate_fourier_coefficients = false;
		}
		
		
		// The render loop
		long last_time_rendered = System.nanoTime();
		
		// Run the animation for one loop of the complex function
		while(mathematics.independent_variable <= 1.05 && !should_stop_thread){
			long current_time = System.nanoTime();
			long delta_render = current_time - last_time_rendered;
			
			// Do the calculations for the fourier series and rendering so that
			// the only thing left to do is to render the screen
			if(!Main.app_window.arrow_calculations_done){
				// Calculations for determining the fourier series
				mathematics.calculateFourierSeriesTerms(mathematics.independent_variable);
				
				// Add the current function value to the fourier series drawing array
				Point end_point_pixel = mathematics.calculateEndPoint();
				Main.app_window.fourier_series_drawn_image_array.add(end_point_pixel);
				
				// Rendering calculations
				Main.app_window.arrowPreRenderCalculations();
				
				// Increase the function input value
				mathematics.independent_variable += Main.app_window.animation_drawing_speed/Main.app_window.initial_drawn_image_array_size;
			}
			
			try {
				Thread.sleep(1);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			
			
			
			if(delta_render >= RENDER_WAIT_TIME) {
				last_time_rendered = System.nanoTime();
				
				if(Main.app_window.arrow_calculations_done){
					Main.app_window.render(); // Render one frame
				}
			}
			
		}
	}

}
