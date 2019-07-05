package visualizeFourierSeries;

public class renderLoop implements Runnable{
	
	final int TARGET_FPS = 60;
	final long RENDER_WAIT_TIME = 1000000000/TARGET_FPS;
	
	public void run(){
		long last_time_rendered = System.nanoTime();
		
		
		while(true){
			long current_time = System.nanoTime();
			long delta_render = current_time - last_time_rendered;
			
			try {
				Thread.sleep(1);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			
			
			
			if(delta_render >= RENDER_WAIT_TIME) {
				last_time_rendered = System.nanoTime();
				appWindow.testAngle += 0.1;
				
				Main.app_window.render(); // Render one frame
			}
			
		}
		
	}

}
