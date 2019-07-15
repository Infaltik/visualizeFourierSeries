package visualizeFourierSeries;

import java.awt.Point;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javax.imageio.ImageIO;

public class imageInputFunctions {
	
	public static BufferedImage input_image;
	public static double[][] normalized_image_array;
	public static BufferedImage output_image;
	
	public static void readImage(String filePath){
		File file = new File(filePath);
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
			
			thresholdImage(normalized_image_array, 0.5);
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

}
