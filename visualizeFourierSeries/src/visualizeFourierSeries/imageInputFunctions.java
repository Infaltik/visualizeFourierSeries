package visualizeFourierSeries;

import java.awt.Point;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javax.imageio.ImageIO;

public class imageInputFunctions {
	
	public static ArrayList<Point> test_image_array = new ArrayList<Point>();
	public static BufferedImage input_image;
	public static BufferedImage image;
	
	public static void readImage(String filePath){
		File file = new File(filePath);
		input_image = null;
		
		try {
			input_image = ImageIO.read(file);
			BufferedImage test_image = new BufferedImage(input_image.getWidth(), input_image.getHeight(), BufferedImage.TYPE_BYTE_GRAY);
			for(int y = 0; y < test_image.getHeight(); y++){
				for(int x = 0; x < test_image.getWidth(); x++){
					test_image.setRGB(x, y, -1);
				}
			}
			
			
			for(int y = 0; y < input_image.getHeight(); y++){
				for(int x = 0; x < input_image.getWidth(); x++){
					//System.out.println(image.getRGB(x, y));
					int pixel = input_image.getRGB(x, y);
					if(Math.abs(pixel) > 3000000){
						System.out.println(pixel + "   " + x + ", " + y);
						test_image.setRGB(x, y, 0);
						test_image_array.add(new Point(x, y));
					}
				}
			}
			ImageIO.write(test_image, "png", new File("src/visualizeFourierSeries/output.png"));
			image = test_image;
			
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
