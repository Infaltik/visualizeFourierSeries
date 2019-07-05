package visualizeFourierSeries;

import java.awt.Point;
import java.util.ArrayList;

public class mathematics {
	
	int nbr_of_fourier_terms = 101;
	
	public complexNumber[] image_pixels_to_coordinate_values(ArrayList<Point> image_pixels){
		
		complexNumber[] complex_values_array = new complexNumber[image_pixels.size()];
		
		// Take in the array of image pixels and return a coordinate value for each pixel
		return null; // Temporary
	}
	
	public static complexNumber pixel_to_complex_value(Point pixel){
		int originPixelX = appWindow.rendering_panel_width/2;
		int originPixelY = appWindow.rendering_panel_height/2;
		
		double real_part = ( (double)((int) pixel.getX()) - originPixelX ) / originPixelX;
		double imag_part = -( (double)((int) pixel.getY()) - originPixelY ) / originPixelY;
		
		return new complexNumber(real_part, imag_part);
	}
	
	

}
