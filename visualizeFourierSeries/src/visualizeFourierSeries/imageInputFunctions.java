package visualizeFourierSeries;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.geom.Point2D;
import java.awt.geom.Point2D.Double;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;

import javax.imageio.ImageIO;

public class imageInputFunctions {
	
	public static BufferedImage input_image;
	public static double[][] normalized_image_array;
	public static BufferedImage output_image;
	public static ArrayList<Point> traced_image_array = new ArrayList<Point>();
	public static int zoomInFactor = 1;
	public static int zoom_x_pos = 0;
	public static int zoom_y_pos = 0;
	public static int preview_rectangle_width;
	public static int preview_rectangle_height;
	
	public static ArrayList<Point> test = new ArrayList<Point>();
	
	public static void readImage(File file){
		input_image = null;
		
		try {
			input_image = ImageIO.read(file);
			normalized_image_array = new double[input_image.getWidth()][input_image.getHeight()];
			
			for(int y = 0; y < input_image.getHeight(); y++){
				for(int x = 0; x < input_image.getWidth(); x++){
					int pixel = input_image.getRGB(x, y);
					int a = (pixel>>24)&0xff;
					int r = (pixel>>16)&0xff;
					int g = (pixel>>8)&0xff;
					int b = pixel&0xff;
					
				    double normalized_pixel = (r + g + b)/765.0;
				    normalized_image_array[x][y] = normalized_pixel;
				    
				}
			}
			
			thresholdImage(normalized_image_array, 0.2);
			output_image = thresholdImageToBufferedImage(normalized_image_array);
			
			
			ImageIO.write(output_image, "png", new File("src/visualizeFourierSeries/output.png"));
			
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void thresholdImage(double[][] image_array, double threshold_value){
		
		for(int y = 0; y < image_array[0].length; y++){
			for(int x = 0; x < image_array.length; x++){
				double pixel = image_array[x][y];
				
				if(pixel < threshold_value){
					image_array[x][y] = 0.0;
				}
				else{
					image_array[x][y] = 1.0;
				}
			}
		}
	}
	
	public static BufferedImage thresholdImageToBufferedImage(double[][] image_array){
		int width = image_array.length;
		int height = image_array[0].length;
		BufferedImage result = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);
		
		for(int y = 0; y < height; y++){
			for(int x = 0; x < width; x++){
				if(image_array[x][y] == 0.0){
					result.setRGB(x, y, 0);
				}
				else{
					result.setRGB(x, y, 0xFFFFFF);
				}
			}
		}
		
		return result;
	}
	
	public static void zoomIn(){
		zoomInFactor++;
	}
	
	public static void zoomOut(){
		zoomInFactor = Math.max(1, imageInputFunctions.zoomInFactor-1);
	}
	
	public static BufferedImage getZoomedInImage(int x_pos, int y_pos){
		int old_width = appWindow.rendering_panel_width;
		int old_height = appWindow.rendering_panel_height;
		BufferedImage result = new BufferedImage(old_width, old_height, BufferedImage.TYPE_BYTE_GRAY);
		
		// Initialize the result image by a white background
		for(int y = 0; y < old_height; y++){
			for(int x = 0; x < old_width; x++){
				result.setRGB(x, y, 0xFFFFFF);
			}
		}
		
		double new_width = old_width/((double) zoomInFactor);
		double new_height = old_height/((double) zoomInFactor);
		
		// Draw a portion of the previewed image with large pixels
		for(int y = 0; y < new_height-1; y++){
			for(int x = 0; x < new_width-1; x++){
				if( (x+x_pos < output_image.getWidth()) && (y+y_pos < output_image.getHeight()) ){
					if(output_image.getRGB(x+x_pos, y+y_pos) == -16777216){
						drawLargerPixel(result, x, y);
					}
				}
			}
		}
		
		return result;
	}
	
	public static void drawLargerPixel(BufferedImage image, int x_pos, int y_pos){
		for(int y = 0; y < zoomInFactor; y++){
			for(int x = 0; x < zoomInFactor; x++){
				image.setRGB(x_pos*zoomInFactor + x, y_pos*zoomInFactor + y, 0);
			}
		}
	}
	
	
	public static void drawImageTracingPreview(Graphics2D g2) {
		// Should be moved from here ??? 
		BufferedImage zoomed_in_image = getZoomedInImage(zoom_x_pos, zoom_y_pos);
		g2.drawImage(zoomed_in_image, 0, 0, zoomed_in_image.getWidth(), zoomed_in_image.getHeight(), null);
		// -----------------------
		
		
		int preview_top_left_corner_x = appWindow.rendering_panel_width-output_image.getWidth()-18;
		int preview_top_left_corner_y = 2;
		
		double image_to_panel_ratio = appWindow.rendering_panel_width / ((double) output_image.getWidth()*zoomInFactor);
		
		
		g2.setStroke(new BasicStroke(5));
		g2.drawRect(preview_top_left_corner_x, preview_top_left_corner_y, output_image.getWidth(), output_image.getHeight());
		g2.drawImage(output_image, preview_top_left_corner_x, preview_top_left_corner_y,
				output_image.getWidth(), output_image.getHeight(), null);
		
		// Red rectangle indicator
		if(image_to_panel_ratio < 1){
			g2.setColor(Color.red);
			g2.setStroke(new BasicStroke(2));
			double proportion = ((double) Main.app_window.getHeight())/Main.app_window.getWidth();
			preview_rectangle_width = (int) (output_image.getWidth()*image_to_panel_ratio);
			preview_rectangle_height = (int) (preview_rectangle_width*proportion);
			g2.drawRect(preview_top_left_corner_x + zoom_x_pos, preview_top_left_corner_y + zoom_y_pos,
					preview_rectangle_width, preview_rectangle_height);
		}
		
	}
	
	public static void drawTracingMarker(Graphics2D g2){
		Point current_point = traced_image_array.get(traced_image_array.size()-1);
		g2.setColor(Color.blue);
		
		// Translate the image coordinate to the coordinate in the zoomed in image
		int x = (current_point.x - zoom_x_pos)*zoomInFactor;
		int y = (current_point.y - zoom_y_pos)*zoomInFactor;
		
		g2.fillRect(x, y, zoomInFactor, zoomInFactor);
		
		g2.setColor(Color.red);
		g2.drawString("Tracing marker coordinates: x=" + current_point.x + ", y=" + current_point.y, 
				50, 50);
		
		g2.drawString("Distance from mouse pointer to marker x=: " + (appWindow.x-x)+
				", y=" + (appWindow.y-y), 50, 75);
		
	}
	
	public static void printTracedArrayInSavableFormat() {
		for(int i = 0; i < traced_image_array.size(); i++) {
			System.out.println(traced_image_array.get(i).x + "," + traced_image_array.get(i).y);
		}
	}
	
	public static void loadElephantImage(){
		File file = new File("src\\visualizeFourierSeries\\elephant_drawing_data.txt");
		
		try {
			BufferedReader br = new BufferedReader(new FileReader(file));
			ArrayList<Point> result_array = new ArrayList<Point>();
			
			String current_string;
			
			while( (current_string = br.readLine()) != null ){
				if(current_string.contains(",")){
					String[] string_parts = current_string.split("\\,");
					result_array.add(new Point(2*Integer.parseInt(string_parts[0]), 2*Integer.parseInt(string_parts[1])));
				}
				else{
					throw new IllegalArgumentException("String " + current_string + " does not contain ,");
				}
			}
			br.close();
			Main.app_window.drawn_image_array = result_array;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	public static void loadEighthNoteImage(){
		File file = new File("src\\visualizeFourierSeries\\eighth_note_data.txt");
		
		try {
			BufferedReader br = new BufferedReader(new FileReader(file));
			ArrayList<Point> result_array = new ArrayList<Point>();
			
			String current_string;
			
			while( (current_string = br.readLine()) != null ){
				if(current_string.contains(",")){
					String[] string_parts = current_string.split("\\,");
					result_array.add(new Point(2*Integer.parseInt(string_parts[0]), 2*Integer.parseInt(string_parts[1])));
				}
				else{
					throw new IllegalArgumentException("String " + current_string + " does not contain ,");
				}
			}
			br.close();
			Main.app_window.drawn_image_array = result_array;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	

}
