package visualizeFourierSeries;

import java.awt.Point;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javax.imageio.ImageIO;

public class imageInputFunctions {
	
	public static ArrayList<Point> test_image_array = new ArrayList<Point>();
	public static BufferedImage image;
	
	public static void readImage(String filePath){
		File file = new File(filePath);
		image = null;
		
		try {
			image = ImageIO.read(file);
			BufferedImage test_image = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_BYTE_GRAY);
			for(int y = 0; y < test_image.getHeight(); y++){
				for(int x = 0; x < test_image.getWidth(); x++){
					test_image.setRGB(x, y, 0);
				}
			}
			
			
			for(int y = 0; y < image.getHeight(); y++){
				for(int x = 0; x < image.getWidth(); x++){
					//System.out.println(image.getRGB(x, y));
					int pixel = image.getRGB(x, y);
					if(Math.abs(pixel) > 2000000){
						System.out.println(pixel + "   " + x + ", " + y);
						test_image.setRGB(x, y, -200);
						test_image_array.add(new Point(x, y));
					}
				}
			}
			ImageIO.write(test_image, "png", new File("output.png"));
			
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
