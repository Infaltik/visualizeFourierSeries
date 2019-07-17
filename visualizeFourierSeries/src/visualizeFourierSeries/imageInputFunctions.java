package visualizeFourierSeries;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.io.File;
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
		
		
		int preview_top_left_corner_x = Main.app_window.rendering_panel_width-output_image.getWidth()-18;
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
	
	public static void printTracedArrayInSavableFormat() {
		
		for(int i = 0; i < traced_image_array.size(); i++) {
			System.out.print("new Point(" + traced_image_array.get(i).x + "," + traced_image_array.get(i).y + "), ");
			if((i+1)%10 == 0) {
				System.out.print("\n");
			}
		}
		System.out.print("\n");
		
		
		ArrayList<Point> test = new ArrayList<Point>();
		Collections.addAll(test, new Point(207,204), new Point(208,204), new Point(209,204), new Point(210,204), new Point(211,204), new Point(212,204), new Point(213,204), new Point(214,204), new Point(215,204), new Point(216,204), 
				new Point(217,204), new Point(218,204), new Point(219,204), new Point(220,204), new Point(221,204), new Point(222,204), new Point(223,204), new Point(224,204), new Point(225,204), new Point(226,204), 
				new Point(227,204), new Point(228,204), new Point(229,204), new Point(230,204), new Point(231,204), new Point(232,204), new Point(233,204), new Point(234,204), new Point(235,204), new Point(236,204), 
				new Point(237,204), new Point(238,204), new Point(239,204), new Point(240,204), new Point(241,204), new Point(242,204), new Point(243,204), new Point(244,204), new Point(245,204), new Point(246,204), 
				new Point(247,204), new Point(248,204), new Point(249,204), new Point(250,204), new Point(251,204), new Point(252,204), new Point(253,204), new Point(254,204), new Point(255,204), new Point(256,204), 
				new Point(257,204), new Point(258,204), new Point(259,204), new Point(260,204), new Point(261,204), new Point(262,204), new Point(263,204), new Point(264,204), new Point(265,204), new Point(266,204), 
				new Point(267,204), new Point(268,204), new Point(269,204), new Point(270,204), new Point(271,204), new Point(272,204), new Point(273,204), new Point(274,204), new Point(275,204), new Point(276,204), 
				new Point(277,204), new Point(278,204), new Point(279,204), new Point(280,204), new Point(281,204), new Point(282,204), new Point(283,204), new Point(284,204), new Point(285,204), new Point(286,204), 
				new Point(287,204), new Point(288,204), new Point(289,204), new Point(290,204), new Point(291,204), new Point(292,204), new Point(293,204), new Point(294,204), new Point(295,204), new Point(296,204), 
				new Point(297,204), new Point(298,204), new Point(299,204), new Point(300,204), new Point(301,204), new Point(302,204), new Point(303,204), new Point(304,204), new Point(305,204), new Point(306,204), 
				new Point(307,204), new Point(308,204), new Point(309,204), new Point(310,204), new Point(311,204), new Point(312,204), new Point(313,204), new Point(314,204), new Point(315,204), new Point(316,204), 
				new Point(317,204), new Point(318,204), new Point(319,204), new Point(320,204), new Point(321,204), new Point(322,204), new Point(323,204), new Point(324,204), new Point(325,204), new Point(326,204), 
				new Point(327,204), new Point(328,204), new Point(329,204), new Point(330,204), new Point(331,204), new Point(332,204), new Point(333,204), new Point(334,204), new Point(335,204), new Point(336,204), 
				new Point(337,204), new Point(338,204), new Point(339,204), new Point(340,204), new Point(341,204), new Point(342,204), new Point(343,204), new Point(344,204), new Point(345,204), new Point(346,204), 
				new Point(347,204), new Point(348,204), new Point(349,204), new Point(350,204), new Point(351,204), new Point(352,204), new Point(353,204), new Point(354,204), new Point(355,204), new Point(356,204), 
				new Point(357,204), new Point(358,204), new Point(359,204), new Point(360,204));
		
	}

}
