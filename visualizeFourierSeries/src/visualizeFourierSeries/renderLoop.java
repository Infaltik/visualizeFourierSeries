package visualizeFourierSeries;

import java.awt.Point;

public class renderLoop implements Runnable{
	
	final int TARGET_FPS = 144;
	final long RENDER_WAIT_TIME = 1000000000/TARGET_FPS;
	double independent_variable = 0;
	
	public void run(){
		long last_time_rendered = System.nanoTime();
		
		
		while(independent_variable < 1){
			long current_time = System.nanoTime();
			long delta_render = current_time - last_time_rendered;
			
			// Do the calculations for the fourier series and rendering so that
			// the only thing left to do is to render the screen
			
			if(!Main.app_window.arrow_calculations_done){
				mathematics.calculateFourierSeriesTerms(independent_variable);
				Main.app_window.arrowPreRenderCalculations(appWindow.testAngle);
				int array_size = Main.app_window.arrow_circle_render_data_array.size();
				Point current_end_point = Main.app_window.arrow_circle_render_data_array.get(array_size-1	).getArrowEndPoint();
				Main.app_window.fourier_series_drawn_image_array.add(current_end_point);
			}
			
			try {
				Thread.sleep(1);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			
			
			
			if(delta_render >= RENDER_WAIT_TIME) {
				last_time_rendered = System.nanoTime();
				appWindow.testAngle += 0.01;
				independent_variable += 0.001;
				
				if(Main.app_window.arrow_calculations_done){
					Main.app_window.render(); // Render one frame
					Main.app_window.arrow_calculations_done = false;
				}
			}
			
		}
		
	}

}
