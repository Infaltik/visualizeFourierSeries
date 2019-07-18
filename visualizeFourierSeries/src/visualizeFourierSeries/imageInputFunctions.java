package visualizeFourierSeries;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.text.DecimalFormat;
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
	
	public static void test1() {
		Collections.addAll(test, new Point(194,440), new Point(194,439), new Point(195,439), new Point(195,438), new Point(196,438), new Point(196,437), new Point(197,437), new Point(197,436), new Point(198,436), new Point(199,436), 
				new Point(200,436), new Point(201,436), new Point(202,436), new Point(203,436), new Point(204,436), new Point(205,436), new Point(206,436), new Point(207,436), new Point(208,436), new Point(208,435), 
				new Point(209,435), new Point(209,434), new Point(210,434), new Point(210,433), new Point(210,432), new Point(211,432), new Point(211,431), new Point(212,431), new Point(212,430), new Point(213,430), 
				new Point(213,429), new Point(214,429), new Point(215,428), new Point(216,428), new Point(216,427), new Point(217,427), new Point(218,427), new Point(219,427), new Point(219,426), new Point(220,426), 
				new Point(221,426), new Point(221,425), new Point(222,425), new Point(223,425), new Point(223,424), new Point(224,424), new Point(225,423), new Point(226,423), new Point(226,422), new Point(227,422), 
				new Point(228,421), new Point(229,421), new Point(229,420), new Point(230,420), new Point(230,419), new Point(231,419), new Point(231,418), new Point(232,418), new Point(232,417), new Point(233,417), 
				new Point(233,416), new Point(234,415), new Point(234,414), new Point(235,414), new Point(235,413), new Point(236,413), new Point(236,412), new Point(236,411), new Point(237,411), new Point(237,410), 
				new Point(237,409), new Point(238,409), new Point(238,408), new Point(238,407), new Point(239,407), new Point(239,406), new Point(239,405), new Point(240,405), new Point(240,404), new Point(240,403), 
				new Point(240,402), new Point(241,401), new Point(241,400), new Point(241,399), new Point(241,398), new Point(241,397), new Point(242,397), new Point(242,396), new Point(242,395), new Point(242,394), 
				new Point(242,393), new Point(242,392), new Point(242,391), new Point(242,390), new Point(242,389), new Point(243,389), new Point(243,388), new Point(243,387), new Point(243,386), new Point(243,385), 
				new Point(243,384), new Point(244,384), new Point(244,383), new Point(244,382), new Point(244,381), new Point(244,380), new Point(244,379), new Point(245,379), new Point(245,378), new Point(245,377), 
				new Point(245,376), new Point(245,375), new Point(245,374), new Point(245,373), new Point(246,373), new Point(246,372), new Point(246,371), new Point(246,370), new Point(246,369), new Point(246,368), 
				new Point(246,367), new Point(246,366), new Point(247,366), new Point(247,365), new Point(247,364), new Point(247,363), new Point(247,362), new Point(247,361), new Point(247,360), new Point(247,359), 
				new Point(247,358), new Point(247,357), new Point(247,356), new Point(248,356), new Point(248,355), new Point(248,354), new Point(248,353), new Point(248,352), new Point(248,351), new Point(248,350), 
				new Point(248,349), new Point(248,348), new Point(248,347), new Point(248,346), new Point(248,345), new Point(248,344), new Point(248,343), new Point(248,342), new Point(248,341), new Point(248,340), 
				new Point(248,339), new Point(248,338), new Point(248,337), new Point(248,336), new Point(248,335), new Point(248,334), new Point(248,333), new Point(248,332), new Point(248,331), new Point(248,330), 
				new Point(247,329), new Point(247,328), new Point(247,327), new Point(247,326), new Point(247,325), new Point(247,324), new Point(247,323), new Point(247,322), new Point(247,321), new Point(247,320), 
				new Point(247,319), new Point(247,318), new Point(247,317), new Point(247,316), new Point(247,315), new Point(247,314), new Point(246,314), new Point(246,313), new Point(246,312), new Point(246,311), 
				new Point(246,310), new Point(246,309), new Point(246,308), new Point(246,307), new Point(246,306), new Point(246,305), new Point(245,305), new Point(245,304), new Point(245,303), new Point(245,302), 
				new Point(245,301), new Point(245,300), new Point(245,299), new Point(245,298), new Point(244,298), new Point(244,297), new Point(244,296), new Point(244,295), new Point(244,294), new Point(244,293), 
				new Point(244,292), new Point(244,291), new Point(243,291), new Point(243,290), new Point(243,289), new Point(243,288), new Point(243,287), new Point(243,286), new Point(243,285), new Point(243,284), 
				new Point(243,285), new Point(243,286), new Point(243,287), new Point(242,287), new Point(242,286), new Point(242,285), new Point(242,284), new Point(242,283), new Point(242,282), new Point(242,281), 
				new Point(241,281), new Point(241,280), new Point(241,279), new Point(241,278), new Point(241,277), new Point(241,276), new Point(240,276), new Point(240,275), new Point(240,274), new Point(240,273), 
				new Point(240,272), new Point(240,271), new Point(240,272), new Point(239,272), new Point(239,271), new Point(239,270), new Point(239,269), new Point(239,268), new Point(239,267), new Point(239,268), 
				new Point(238,268), new Point(238,267), new Point(238,266), new Point(238,265), new Point(238,264), new Point(238,263), new Point(238,264), new Point(237,264), new Point(237,263), new Point(237,262), 
				new Point(237,261), new Point(237,260), new Point(236,260), new Point(236,259), new Point(236,258), new Point(236,257), new Point(236,256), new Point(236,255), new Point(235,255), new Point(235,254), 
				new Point(235,253), new Point(236,253), new Point(236,252), new Point(236,251), new Point(237,250), new Point(238,250), new Point(239,250), new Point(240,250), new Point(241,250), new Point(241,251), 
				new Point(242,251), new Point(243,251), new Point(244,251), new Point(244,252), new Point(245,252), new Point(246,252), new Point(247,252), new Point(248,252), new Point(249,252), new Point(250,252), 
				new Point(251,252), new Point(252,252), new Point(253,252), new Point(253,251), new Point(254,251), new Point(255,251), new Point(255,250), new Point(256,250), new Point(257,250), new Point(258,250), 
				new Point(258,249), new Point(259,249), new Point(260,249), new Point(260,250), new Point(261,250), new Point(261,251), new Point(260,252), new Point(259,253), new Point(259,254), new Point(258,254), 
				new Point(258,255), new Point(257,255), new Point(256,256), new Point(255,257), new Point(254,257), new Point(254,258), new Point(253,258), new Point(252,259), new Point(251,259), new Point(250,259), 
				new Point(250,260), new Point(249,260), new Point(248,260), new Point(247,260), new Point(246,260), new Point(247,260), new Point(247,261), new Point(246,261), new Point(245,261), new Point(244,261), 
				new Point(243,261), new Point(242,261), new Point(241,261), new Point(240,261), new Point(239,261), new Point(238,261), new Point(237,261), new Point(237,260), new Point(236,260), new Point(235,260), 
				new Point(234,260), new Point(234,259), new Point(233,259), new Point(232,259), new Point(231,258), new Point(230,258), new Point(230,257), new Point(229,257), new Point(228,257), new Point(228,256), 
				new Point(227,256), new Point(226,256), new Point(226,255), new Point(225,255), new Point(225,254), new Point(224,254), new Point(223,253), new Point(222,253), new Point(222,252), new Point(221,252), 
				new Point(221,251), new Point(220,251), new Point(220,250), new Point(219,250), new Point(219,249), new Point(218,249), new Point(218,248), new Point(218,247), new Point(217,247), new Point(217,246), 
				new Point(216,246), new Point(216,245), new Point(215,245), new Point(215,244), new Point(215,243), new Point(215,242), new Point(214,242), new Point(214,241), new Point(214,240), new Point(214,239), 
				new Point(214,238), new Point(214,237), new Point(215,236), new Point(215,235), new Point(216,235), new Point(216,234), new Point(217,234), new Point(217,233), new Point(218,233), new Point(219,233), 
				new Point(219,232), new Point(220,232), new Point(221,232), new Point(222,232), new Point(223,232), new Point(224,232), new Point(225,232), new Point(225,233), new Point(226,233), new Point(227,233), 
				new Point(228,233), new Point(228,234), new Point(229,234), new Point(230,234), new Point(230,235), new Point(231,235), new Point(231,236), new Point(232,236), new Point(232,237), new Point(232,238), 
				new Point(232,239), new Point(232,240), new Point(232,241), new Point(232,240), new Point(232,239), new Point(232,238), new Point(233,238), new Point(233,239), new Point(233,240), new Point(233,241), 
				new Point(233,242), new Point(233,243), new Point(233,244), new Point(234,244), new Point(234,245), new Point(235,246), new Point(235,247), new Point(236,247), new Point(237,248), new Point(238,248), 
				new Point(239,247), new Point(240,247), new Point(240,246), new Point(241,246), new Point(241,245), new Point(242,245), new Point(242,244), new Point(242,243), new Point(243,243), new Point(243,242), 
				new Point(243,241), new Point(244,241), new Point(244,240), new Point(244,239), new Point(244,238), new Point(245,238), new Point(245,237), new Point(245,236), new Point(245,235), new Point(246,235), 
				new Point(246,234), new Point(246,233), new Point(246,232), new Point(246,231), new Point(246,230), new Point(246,229), new Point(246,228), new Point(246,229), new Point(246,230), new Point(247,230), 
				new Point(247,229), new Point(247,228), new Point(246,227), new Point(246,226), new Point(246,225), new Point(246,224), new Point(245,223), new Point(245,222), new Point(245,221), new Point(244,221), 
				new Point(244,220), new Point(244,219), new Point(243,219), new Point(243,218), new Point(242,218), new Point(242,217), new Point(242,216), new Point(241,216), new Point(241,215), new Point(240,215), 
				new Point(240,214), new Point(239,214), new Point(239,213), new Point(238,212), new Point(238,211), new Point(237,211), new Point(237,210), new Point(236,210), new Point(236,209), new Point(235,209), 
				new Point(235,208), new Point(234,207), new Point(234,206), new Point(233,206), new Point(233,205), new Point(233,204), new Point(232,204), new Point(232,203), new Point(232,202), new Point(232,201), 
				new Point(231,201), new Point(231,200), new Point(231,199), new Point(231,198), new Point(231,197), new Point(230,197), new Point(230,196), new Point(230,195), new Point(230,194), new Point(230,193), 
				new Point(230,192), new Point(230,191), new Point(230,190), new Point(230,189), new Point(230,190), new Point(230,191), new Point(229,191), new Point(229,190), new Point(229,189), new Point(229,188), 
				new Point(229,187), new Point(229,186), new Point(229,185), new Point(229,184), new Point(229,183), new Point(229,182), new Point(229,181), new Point(229,180), new Point(229,181), new Point(228,181), 
				new Point(228,180), new Point(228,179), new Point(228,178), new Point(228,177), new Point(228,176), new Point(228,175), new Point(228,174), new Point(227,174), new Point(227,173), new Point(227,172), 
				new Point(227,171), new Point(227,170), new Point(226,170), new Point(226,169), new Point(225,169), new Point(224,169), new Point(223,169), new Point(222,170), new Point(221,170), new Point(220,171), 
				new Point(219,171), new Point(219,172), new Point(218,172), new Point(217,172), new Point(216,172), new Point(215,172), new Point(214,172), new Point(214,171), new Point(213,171), new Point(213,170), 
				new Point(213,169), new Point(212,168), new Point(213,168), new Point(213,167), new Point(214,167), new Point(214,168), new Point(215,168), new Point(216,168), new Point(217,168), new Point(218,168), 
				new Point(219,168), new Point(220,168), new Point(221,167), new Point(222,166), new Point(222,165), new Point(221,164), new Point(221,163), new Point(220,162), new Point(220,161), new Point(219,161), 
				new Point(219,160), new Point(218,160), new Point(218,159), new Point(217,159), new Point(216,158), new Point(215,158), new Point(214,158), new Point(213,158), new Point(212,158), new Point(211,158), 
				new Point(211,159), new Point(211,160), new Point(210,160), new Point(210,161), new Point(211,161), new Point(212,161), new Point(212,162), new Point(213,162), new Point(213,161), new Point(214,161), 
				new Point(214,162), new Point(215,162), new Point(216,162), new Point(217,162), new Point(217,163), new Point(218,163), new Point(219,164), new Point(219,165), new Point(219,166), new Point(218,166), 
				new Point(217,166), new Point(216,165), new Point(215,165), new Point(214,165), new Point(214,164), new Point(213,164), new Point(212,164), new Point(212,163), new Point(211,163), new Point(211,162), 
				new Point(211,161), new Point(210,161), new Point(210,162), new Point(209,162), new Point(208,162), new Point(207,162), new Point(207,163), new Point(206,163), new Point(205,163), new Point(205,164), 
				new Point(204,164), new Point(204,165), new Point(203,165), new Point(203,166), new Point(203,167), new Point(203,166), new Point(203,165), new Point(204,165), new Point(204,164), new Point(205,164), 
				new Point(205,163), new Point(206,163), new Point(206,162), new Point(207,162), new Point(207,161), new Point(208,161), new Point(208,160), new Point(209,160), new Point(209,159), new Point(210,159), 
				new Point(211,159), new Point(211,158), new Point(212,158), new Point(212,157), new Point(213,157), new Point(213,158), new Point(214,158), new Point(214,157), new Point(214,156), new Point(215,156), 
				new Point(216,155), new Point(217,155), new Point(218,155), new Point(219,155), new Point(219,154), new Point(220,154), new Point(220,155), new Point(221,155), new Point(222,155), new Point(222,156), 
				new Point(223,156), new Point(223,157), new Point(223,158), new Point(224,158), new Point(224,159), new Point(224,160), new Point(224,161), new Point(224,162), new Point(224,163), new Point(224,164), 
				new Point(224,165), new Point(224,166), new Point(224,167), new Point(224,168), new Point(223,168), new Point(223,169), new Point(223,170), new Point(223,171), new Point(223,172), new Point(223,173), 
				new Point(222,173), new Point(222,174), new Point(222,175), new Point(222,176), new Point(221,176), new Point(221,177), new Point(221,178), new Point(220,178), new Point(220,179), new Point(219,180), 
				new Point(218,180), new Point(218,181), new Point(217,181), new Point(216,181), new Point(215,181), new Point(216,181), new Point(216,182), new Point(215,182), new Point(214,182), new Point(213,182), 
				new Point(212,182), new Point(211,182), new Point(210,182), new Point(209,182), new Point(210,182), new Point(210,181), new Point(209,181), new Point(208,181), new Point(207,181), new Point(206,181), 
				new Point(205,181), new Point(205,180), new Point(204,180), new Point(203,180), new Point(202,180), new Point(202,179), new Point(201,179), new Point(200,179), new Point(199,179), new Point(199,178), 
				new Point(198,178), new Point(197,178), new Point(197,177), new Point(196,177), new Point(195,177), new Point(194,177), new Point(194,176), new Point(193,176), new Point(192,176), new Point(192,175), 
				new Point(191,175), new Point(190,175), new Point(190,174), new Point(189,174), new Point(188,174), new Point(188,173), new Point(187,173), new Point(186,173), new Point(186,172), new Point(185,172), 
				new Point(184,172), new Point(183,172), new Point(183,171), new Point(182,171), new Point(182,170), new Point(181,170), new Point(181,169), new Point(180,169), new Point(179,168), new Point(178,167), 
				new Point(178,166), new Point(177,166), new Point(177,165), new Point(176,164), new Point(176,163), new Point(176,162), new Point(175,162), new Point(175,161), new Point(175,160), new Point(175,159), 
				new Point(175,158), new Point(175,157), new Point(175,156), new Point(175,155), new Point(175,154), new Point(175,153), new Point(175,152), new Point(175,151), new Point(175,150), new Point(175,149), 
				new Point(175,148), new Point(175,147), new Point(175,148), new Point(176,148), new Point(176,147), new Point(176,146), new Point(176,145), new Point(176,144), new Point(176,143), new Point(176,142), 
				new Point(176,141), new Point(176,142), new Point(177,142), new Point(177,141), new Point(177,140), new Point(177,139), new Point(177,138), new Point(177,137), new Point(177,136), new Point(178,136), 
				new Point(178,135), new Point(178,134), new Point(178,133), new Point(178,132), new Point(178,131), new Point(178,130), new Point(178,129), new Point(178,128), new Point(178,127), new Point(178,126), 
				new Point(178,127), new Point(177,127), new Point(177,126), new Point(177,125), new Point(177,124), new Point(177,123), new Point(177,122), new Point(176,122), new Point(176,121), new Point(176,120), 
				new Point(176,119), new Point(175,119), new Point(175,118), new Point(175,117), new Point(175,116), new Point(174,116), new Point(174,115), new Point(174,114), new Point(174,113), new Point(173,113), 
				new Point(173,112), new Point(173,111), new Point(173,110), new Point(172,110), new Point(172,109), new Point(172,108), new Point(172,107), new Point(172,108), new Point(171,108), new Point(171,107), 
				new Point(171,106), new Point(171,105), new Point(170,105), new Point(170,104), new Point(170,103), new Point(169,103), new Point(169,102), new Point(169,101), new Point(169,100), new Point(168,100), 
				new Point(168,99), new Point(168,98), new Point(167,98), new Point(167,97), new Point(167,96), new Point(166,96), new Point(166,95), new Point(166,94), new Point(165,94), new Point(165,93), 
				new Point(165,92), new Point(164,92), new Point(164,91), new Point(163,90), new Point(163,89), new Point(162,89), new Point(162,88), new Point(162,87), new Point(161,87), new Point(161,86), 
				new Point(160,86), new Point(160,85), new Point(159,85), new Point(159,84), new Point(158,84), new Point(158,83), new Point(157,84), new Point(157,85), new Point(157,86), new Point(157,87), 
				new Point(157,86), new Point(157,85), new Point(156,85), new Point(156,86), new Point(156,87), new Point(156,88), new Point(156,89), new Point(156,90), new Point(156,89), new Point(157,89), 
				new Point(157,90), new Point(157,91), new Point(157,92), new Point(157,93), new Point(157,94), new Point(157,95), new Point(157,96), new Point(158,96), new Point(158,97), new Point(158,98), 
				new Point(158,99), new Point(159,100), new Point(159,101), new Point(159,102), new Point(160,102), new Point(160,103), new Point(161,104), new Point(161,105), new Point(162,105), new Point(162,106), 
				new Point(163,107), new Point(164,108), new Point(164,109), new Point(165,109), new Point(165,110), new Point(165,111), new Point(166,111), new Point(166,110), new Point(167,110), new Point(167,111), 
				new Point(168,112), new Point(169,112), new Point(169,113), new Point(170,113), new Point(170,114), new Point(171,114), new Point(172,115), new Point(173,115), new Point(174,115), new Point(174,116), 
				new Point(175,116), new Point(176,117), new Point(177,117), new Point(178,117), new Point(178,118), new Point(179,118), new Point(180,118), new Point(180,119), new Point(181,119), new Point(182,119), 
				new Point(183,119), new Point(183,120), new Point(184,120), new Point(185,120), new Point(186,120), new Point(187,120), new Point(186,120), new Point(186,121), new Point(187,121), new Point(188,121), 
				new Point(189,121), new Point(190,121), new Point(191,121), new Point(191,122), new Point(192,122), new Point(193,122), new Point(194,122), new Point(195,122), new Point(196,122), new Point(197,122), 
				new Point(198,122), new Point(199,122), new Point(200,122), new Point(199,122), new Point(198,122), new Point(197,122), new Point(197,123), new Point(198,123), new Point(199,123), new Point(200,123), 
				new Point(201,123), new Point(202,123), new Point(203,123), new Point(204,123), new Point(205,123), new Point(206,123), new Point(207,123), new Point(208,123), new Point(209,123), new Point(210,123), 
				new Point(211,123), new Point(212,123), new Point(213,123), new Point(214,123), new Point(213,123), new Point(212,123), new Point(212,122), new Point(213,122), new Point(214,122), new Point(215,122), 
				new Point(216,122), new Point(217,122), new Point(218,122), new Point(219,122), new Point(220,122), new Point(221,122), new Point(220,122), new Point(220,121), new Point(221,121), new Point(222,121), 
				new Point(223,121), new Point(224,121), new Point(225,121), new Point(225,120), new Point(226,120), new Point(227,120), new Point(228,119), new Point(229,119), new Point(230,119), new Point(231,118), 
				new Point(232,118), new Point(233,118), new Point(233,117), new Point(234,117), new Point(234,116), new Point(235,116), new Point(235,115), new Point(236,115), new Point(236,114), new Point(237,114), 
				new Point(237,113), new Point(238,113), new Point(238,112), new Point(239,112), new Point(239,111), new Point(240,111), new Point(240,110), new Point(240,109), new Point(240,108), new Point(240,107), 
				new Point(240,106), new Point(240,105), new Point(239,105), new Point(239,104), new Point(239,103), new Point(238,103), new Point(238,102), new Point(237,102), new Point(237,101), new Point(236,101), 
				new Point(235,100), new Point(234,99), new Point(233,99), new Point(233,98), new Point(232,98), new Point(231,98), new Point(231,97), new Point(230,97), new Point(229,97), new Point(229,96), 
				new Point(228,96), new Point(227,96), new Point(226,95), new Point(225,95), new Point(224,95), new Point(225,95), new Point(226,95), new Point(226,94), new Point(225,94), new Point(224,94), 
				new Point(223,94), new Point(222,94), new Point(221,94), new Point(220,94), new Point(219,94), new Point(218,94), new Point(219,94), new Point(220,94), new Point(220,93), new Point(219,93), 
				new Point(218,93), new Point(218,92), new Point(217,92), new Point(216,92), new Point(215,92), new Point(214,92), new Point(213,92), new Point(214,92), new Point(214,91), new Point(213,91), 
				new Point(212,91), new Point(211,91), new Point(210,91), new Point(209,91), new Point(210,91), new Point(210,90), new Point(209,90), new Point(208,90), new Point(207,90), new Point(206,90), 
				new Point(206,91), new Point(205,91), new Point(204,91), new Point(204,92), new Point(203,92), new Point(202,92), new Point(201,92), new Point(200,92), new Point(201,92), new Point(201,91), 
				new Point(200,91), new Point(199,91), new Point(198,91), new Point(197,91), new Point(197,90), new Point(196,90), new Point(195,90), new Point(194,90), new Point(194,89), new Point(193,89), 
				new Point(192,89), new Point(192,88), new Point(191,88), new Point(190,88), new Point(190,87), new Point(189,87), new Point(188,86), new Point(187,86), new Point(187,85), new Point(186,85), 
				new Point(185,85), new Point(185,84), new Point(184,84), new Point(183,83), new Point(182,83), new Point(182,82), new Point(181,82), new Point(180,81), new Point(179,81), new Point(179,80), 
				new Point(178,80), new Point(177,80), new Point(177,79), new Point(176,79), new Point(176,78), new Point(175,78), new Point(174,78), new Point(174,77), new Point(173,77), new Point(172,77), 
				new Point(172,76), new Point(171,76), new Point(170,76), new Point(170,75), new Point(169,75), new Point(168,75), new Point(168,74), new Point(167,74), new Point(166,74), new Point(165,74), 
				new Point(165,73), new Point(164,73), new Point(163,73), new Point(162,73), new Point(162,72), new Point(161,72), new Point(160,72), new Point(159,72), new Point(158,72), new Point(157,72), 
				new Point(157,71), new Point(156,71), new Point(155,71), new Point(154,71), new Point(153,71), new Point(152,71), new Point(151,71), new Point(150,71), new Point(149,71), new Point(149,72), 
				new Point(148,72), new Point(147,72), new Point(146,72), new Point(145,72), new Point(144,72), new Point(143,72), new Point(143,73), new Point(142,73), new Point(141,73), new Point(140,73), 
				new Point(139,73), new Point(139,74), new Point(138,74), new Point(137,74), new Point(136,74), new Point(136,75), new Point(135,75), new Point(134,75), new Point(134,76), new Point(133,76), 
				new Point(132,76), new Point(131,76), new Point(131,77), new Point(130,77), new Point(129,77), new Point(129,78), new Point(128,78), new Point(127,78), new Point(127,79), new Point(126,79), 
				new Point(125,79), new Point(125,80), new Point(124,80), new Point(123,80), new Point(123,81), new Point(122,81), new Point(121,81), new Point(121,82), new Point(120,82), new Point(119,83), 
				new Point(118,83), new Point(118,84), new Point(117,84), new Point(116,84), new Point(116,85), new Point(115,85), new Point(114,86), new Point(113,86), new Point(113,87), new Point(112,87), 
				new Point(111,88), new Point(110,88), new Point(110,89), new Point(109,89), new Point(108,90), new Point(107,90), new Point(107,91), new Point(106,91), new Point(105,92), new Point(104,92), 
				new Point(104,93), new Point(103,93), new Point(102,94), new Point(101,94), new Point(101,95), new Point(100,95), new Point(100,96), new Point(99,96), new Point(98,97), new Point(97,97), 
				new Point(97,98), new Point(96,98), new Point(96,99), new Point(95,99), new Point(95,100), new Point(94,100), new Point(94,101), new Point(93,101), new Point(93,102), new Point(92,102), 
				new Point(92,103), new Point(91,103), new Point(91,104), new Point(90,104), new Point(90,105), new Point(89,105), new Point(89,106), new Point(88,106), new Point(88,107), new Point(87,107), 
				new Point(87,108), new Point(86,108), new Point(86,109), new Point(86,110), new Point(85,110), new Point(85,111), new Point(84,111), new Point(84,112), new Point(84,113), new Point(83,113), 
				new Point(83,114), new Point(82,114), new Point(82,115), new Point(82,116), new Point(81,116), new Point(81,117), new Point(81,118), new Point(80,118), new Point(80,119), new Point(80,120), 
				new Point(79,120), new Point(79,121), new Point(79,122), new Point(78,122), new Point(78,123), new Point(78,124), new Point(78,125), new Point(77,125), new Point(77,126), new Point(77,127), 
				new Point(77,128), new Point(76,128), new Point(76,129), new Point(76,130), new Point(76,131), new Point(76,132), new Point(75,132), new Point(75,133), new Point(75,134), new Point(75,135), 
				new Point(75,136), new Point(75,137), new Point(75,138), new Point(75,137), new Point(74,137), new Point(74,138), new Point(74,139), new Point(74,140), new Point(74,141), new Point(74,142), 
				new Point(74,143), new Point(74,144), new Point(74,145), new Point(74,146), new Point(74,147), new Point(74,148), new Point(74,149), new Point(74,150), new Point(74,151), new Point(74,152), 
				new Point(74,153), new Point(74,152), new Point(75,152), new Point(75,153), new Point(75,154), new Point(75,155), new Point(75,156), new Point(75,157), new Point(75,158), new Point(75,159), 
				new Point(76,159), new Point(76,160), new Point(76,161), new Point(76,162), new Point(76,163), new Point(77,163), new Point(77,164), new Point(77,165), new Point(77,166), new Point(78,166), 
				new Point(78,167), new Point(78,168), new Point(79,168), new Point(79,169), new Point(79,170), new Point(80,171), new Point(80,172), new Point(81,172), new Point(81,173), new Point(81,174), 
				new Point(82,174), new Point(82,175), new Point(83,176), new Point(83,177), new Point(84,177), new Point(84,178), new Point(85,178), new Point(85,179), new Point(86,179), new Point(86,180), 
				new Point(87,180), new Point(88,180), new Point(88,181), new Point(89,181), new Point(90,182), new Point(91,182), new Point(91,183), new Point(92,183), new Point(93,183), new Point(93,184), 
				new Point(94,184), new Point(94,185), new Point(95,185), new Point(96,185), new Point(96,186), new Point(97,186), new Point(97,187), new Point(98,187), new Point(99,188), new Point(100,188), 
				new Point(100,189), new Point(101,189), new Point(101,190), new Point(102,190), new Point(103,191), new Point(104,192), new Point(105,192), new Point(105,193), new Point(106,193), new Point(106,194), 
				new Point(107,194), new Point(107,195), new Point(108,195), new Point(108,196), new Point(109,196), new Point(110,197), new Point(111,198), new Point(112,198), new Point(112,199), new Point(113,199), 
				new Point(113,200), new Point(114,200), new Point(114,201), new Point(115,201), new Point(115,202), new Point(116,202), new Point(117,203), new Point(118,204), new Point(119,205), new Point(120,205), 
				new Point(120,206), new Point(121,206), new Point(121,207), new Point(122,207), new Point(122,208), new Point(123,208), new Point(123,209), new Point(124,209), new Point(124,210), new Point(125,210), 
				new Point(125,211), new Point(126,211), new Point(126,212), new Point(127,212), new Point(127,213), new Point(128,213), new Point(129,214), new Point(130,215), new Point(131,216), new Point(132,216), 
				new Point(132,217), new Point(133,217), new Point(133,218), new Point(134,218), new Point(134,219), new Point(135,219), new Point(135,220), new Point(136,220), new Point(136,221), new Point(137,221), 
				new Point(137,222), new Point(138,222), new Point(138,223), new Point(139,223), new Point(139,224), new Point(140,224), new Point(141,225), new Point(142,226), new Point(143,227), new Point(144,228), 
				new Point(145,229), new Point(146,229), new Point(146,230), new Point(147,230), new Point(147,231), new Point(148,231), new Point(148,232), new Point(149,232), new Point(149,233), new Point(150,233), 
				new Point(150,234), new Point(151,234), new Point(151,235), new Point(152,235), new Point(153,236), new Point(154,236), new Point(154,237), new Point(155,237), new Point(155,238), new Point(156,238), 
				new Point(157,238), new Point(157,239), new Point(158,239), new Point(159,240), new Point(160,240), new Point(160,241), new Point(161,241), new Point(162,241), new Point(163,242), new Point(164,242), 
				new Point(165,243), new Point(166,243), new Point(167,243), new Point(167,244), new Point(168,244), new Point(169,244), new Point(170,244), new Point(170,245), new Point(171,245), new Point(172,245), 
				new Point(173,245), new Point(173,246), new Point(174,246), new Point(175,246), new Point(176,246), new Point(177,246), new Point(178,246), new Point(179,246), new Point(180,245), new Point(181,244), 
				new Point(181,243), new Point(182,243), new Point(182,242), new Point(182,241), new Point(182,240), new Point(183,240), new Point(183,239), new Point(183,238), new Point(183,237), new Point(183,236), 
				new Point(183,235), new Point(184,235), new Point(184,234), new Point(184,233), new Point(184,232), new Point(184,231), new Point(184,230), new Point(184,229), new Point(184,230), new Point(185,230), 
				new Point(185,229), new Point(185,228), new Point(185,227), new Point(185,226), new Point(185,225), new Point(185,224), new Point(185,223), new Point(185,222), new Point(185,221), new Point(186,221), 
				new Point(185,221), new Point(185,220), new Point(185,219), new Point(185,218), new Point(185,217), new Point(185,216), new Point(185,215), new Point(185,214), new Point(185,213), new Point(185,212), 
				new Point(185,211), new Point(185,210), new Point(185,209), new Point(185,208), new Point(185,207), new Point(185,208), new Point(185,209), new Point(184,209), new Point(184,208), new Point(184,207), 
				new Point(184,206), new Point(184,205), new Point(184,204), new Point(184,203), new Point(184,202), new Point(184,201), new Point(184,200), new Point(184,199), new Point(184,200), new Point(184,201), 
				new Point(183,201), new Point(183,200), new Point(183,199), new Point(183,198), new Point(183,197), new Point(183,196), new Point(183,195), new Point(183,194), new Point(183,193), new Point(183,192), 
				new Point(183,191), new Point(183,190), new Point(183,191), new Point(183,192), new Point(182,192), new Point(182,191), new Point(182,190), new Point(182,189), new Point(182,188), new Point(182,187), 
				new Point(182,186), new Point(182,185), new Point(182,184), new Point(182,183), new Point(182,182), new Point(182,181), new Point(182,180), new Point(182,179), new Point(183,179), new Point(183,178), 
				new Point(182,178), new Point(182,177), new Point(183,177), new Point(183,176), new Point(182,176), new Point(183,176), new Point(183,175), new Point(183,174), new Point(183,173), new Point(183,172), 
				new Point(183,171), new Point(184,171), new Point(184,170), new Point(185,170), new Point(185,169), new Point(186,168), new Point(186,167), new Point(187,167), new Point(188,166), new Point(189,166), 
				new Point(190,166), new Point(191,166), new Point(190,166), new Point(189,166), new Point(189,165), new Point(190,165), new Point(191,165), new Point(191,164), new Point(192,164), new Point(193,164), 
				new Point(194,164), new Point(193,164), new Point(192,164), new Point(192,163), new Point(193,163), new Point(194,163), new Point(195,163), new Point(196,163), new Point(197,163), new Point(198,163), 
				new Point(199,163), new Point(198,163), new Point(198,162), new Point(199,162), new Point(200,162), new Point(201,161), new Point(202,161), new Point(202,160), new Point(203,160), new Point(203,159), 
				new Point(204,159), new Point(204,158), new Point(205,158), new Point(205,157), new Point(206,157), new Point(206,156), new Point(207,156), new Point(207,155), new Point(208,155), new Point(208,154), 
				new Point(209,154), new Point(209,153), new Point(210,153), new Point(210,152), new Point(211,151), new Point(212,150), new Point(213,149), new Point(214,148), new Point(215,148), new Point(215,147), 
				new Point(216,147), new Point(217,146), new Point(218,146), new Point(219,146), new Point(220,146), new Point(220,147), new Point(221,147), new Point(222,147), new Point(222,148), new Point(223,148), 
				new Point(224,149), new Point(225,149), new Point(225,150), new Point(226,150), new Point(227,151), new Point(228,151), new Point(228,152), new Point(229,152), new Point(229,153), new Point(230,154), 
				new Point(230,155), new Point(230,156), new Point(230,157), new Point(230,158), new Point(230,159), new Point(230,160), new Point(230,161), new Point(229,161), new Point(229,162), new Point(229,163), 
				new Point(229,164), new Point(229,165), new Point(229,166), new Point(229,167), new Point(229,168), new Point(229,167), new Point(229,166), new Point(229,165), new Point(228,165), new Point(228,166), 
				new Point(228,167), new Point(228,168), new Point(229,169), new Point(229,170), new Point(230,170), new Point(230,171), new Point(231,171), new Point(231,172), new Point(232,172), new Point(232,173), 
				new Point(233,174), new Point(233,175), new Point(234,175), new Point(234,176), new Point(235,176), new Point(235,177), new Point(236,178), new Point(236,179), new Point(237,179), new Point(237,180), 
				new Point(238,180), new Point(238,181), new Point(239,182), new Point(239,183), new Point(240,183), new Point(240,184), new Point(241,184), new Point(241,185), new Point(242,186), new Point(243,187), 
				new Point(243,188), new Point(244,188), new Point(244,189), new Point(245,189), new Point(245,190), new Point(246,191), new Point(246,192), new Point(247,192), new Point(247,193), new Point(247,194), 
				new Point(248,194), new Point(248,195), new Point(248,196), new Point(248,197), new Point(248,198), new Point(248,197), new Point(249,197), new Point(249,198), new Point(249,199), new Point(249,200), 
				new Point(249,201), new Point(249,202), new Point(249,203), new Point(249,204), new Point(249,203), new Point(250,203), new Point(250,204), new Point(250,205), new Point(250,206), new Point(250,207), 
				new Point(250,208), new Point(250,209), new Point(250,210), new Point(250,211), new Point(250,212), new Point(250,213), new Point(250,212), new Point(250,211), new Point(251,211), new Point(251,212), 
				new Point(251,213), new Point(251,214), new Point(251,215), new Point(251,216), new Point(251,217), new Point(251,218), new Point(251,219), new Point(251,220), new Point(251,221), new Point(251,222), 
				new Point(251,223), new Point(251,224), new Point(251,225), new Point(251,226), new Point(251,227), new Point(251,228), new Point(251,229), new Point(251,230), new Point(251,231), new Point(252,231), 
				new Point(253,231), new Point(254,231), new Point(255,231), new Point(254,231), new Point(254,230), new Point(255,230), new Point(256,230), new Point(257,230), new Point(258,230), new Point(259,230), 
				new Point(258,230), new Point(258,229), new Point(259,229), new Point(260,229), new Point(261,229), new Point(262,229), new Point(262,228), new Point(263,228), new Point(264,228), new Point(265,228), 
				new Point(266,228), new Point(267,228), new Point(267,227), new Point(268,227), new Point(269,227), new Point(270,227), new Point(271,227), new Point(272,227), new Point(273,227), new Point(272,227), 
				new Point(272,226), new Point(273,226), new Point(274,226), new Point(275,226), new Point(276,226), new Point(277,226), new Point(278,226), new Point(279,226), new Point(280,226), new Point(281,226), 
				new Point(282,226), new Point(283,226), new Point(284,226), new Point(285,226), new Point(284,226), new Point(284,227), new Point(285,227), new Point(286,227), new Point(287,227), new Point(288,227), 
				new Point(288,228), new Point(289,228), new Point(290,228), new Point(290,229), new Point(291,229), new Point(292,229), new Point(292,230), new Point(293,230), new Point(293,231), new Point(294,231), 
				new Point(295,231), new Point(295,232), new Point(295,233), new Point(295,234), new Point(295,235), new Point(295,234), new Point(295,233), new Point(295,232), new Point(295,231), new Point(294,231), 
				new Point(294,232), new Point(294,233), new Point(294,234), new Point(294,235), new Point(293,235), new Point(293,234), new Point(292,234), new Point(291,234), new Point(290,234), new Point(290,233), 
				new Point(289,233), new Point(288,233), new Point(287,233), new Point(287,232), new Point(286,232), new Point(285,232), new Point(284,232), new Point(283,232), new Point(283,231), new Point(282,231), 
				new Point(281,231), new Point(280,231), new Point(279,231), new Point(278,231), new Point(277,231), new Point(277,230), new Point(276,230), new Point(275,230), new Point(274,230), new Point(273,230), 
				new Point(272,230), new Point(271,230), new Point(270,230), new Point(271,230), new Point(272,230), new Point(273,230), new Point(274,230), new Point(275,230), new Point(276,230), new Point(277,230), 
				new Point(276,231), new Point(275,231), new Point(274,231), new Point(273,231), new Point(272,231), new Point(271,231), new Point(270,231), new Point(269,231), new Point(268,231), new Point(267,231), 
				new Point(266,231), new Point(265,231), new Point(265,232), new Point(264,232), new Point(263,232), new Point(262,232), new Point(262,233), new Point(261,233), new Point(260,233), new Point(259,234), 
				new Point(258,234), new Point(258,235), new Point(257,235), new Point(256,235), new Point(256,236), new Point(255,236), new Point(255,237), new Point(254,237), new Point(253,238), new Point(252,238), 
				new Point(252,239), new Point(251,239), new Point(251,240), new Point(250,240), new Point(250,241), new Point(249,242), new Point(250,242), new Point(251,242), new Point(252,242), new Point(253,242), 
				new Point(254,242), new Point(253,242), new Point(253,241), new Point(254,241), new Point(255,241), new Point(256,241), new Point(257,241), new Point(258,241), new Point(259,241), new Point(260,241), 
				new Point(259,241), new Point(259,240), new Point(260,240), new Point(261,240), new Point(262,240), new Point(263,240), new Point(264,240), new Point(265,240), new Point(266,240), new Point(267,240), 
				new Point(268,240), new Point(269,240), new Point(270,240), new Point(271,240), new Point(270,240), new Point(269,240), new Point(268,240), new Point(268,239), new Point(269,239), new Point(270,239));
		
	}
	
	public static void test2() {
		Collections.addAll(test, new Point(271,239), new Point(272,239), new Point(273,239), new Point(274,239), new Point(275,239), new Point(276,239), new Point(277,239), new Point(278,239), new Point(279,239), new Point(280,239), 
		new Point(281,239), new Point(282,239), new Point(281,239), new Point(280,239), new Point(280,240), new Point(281,240), new Point(282,240), new Point(283,240), new Point(284,240), new Point(285,240), 
		new Point(286,240), new Point(287,240), new Point(288,240), new Point(288,241), new Point(289,241), new Point(290,241), new Point(291,241), new Point(291,242), new Point(292,242), new Point(293,242), 
		new Point(294,242), new Point(295,242), new Point(295,243), new Point(296,243), new Point(296,244), new Point(297,244), new Point(297,245), new Point(298,245), new Point(299,245), new Point(298,245), 
		new Point(298,246), new Point(299,246), new Point(299,247), new Point(300,247), new Point(300,248), new Point(301,248), new Point(301,249), new Point(301,250), new Point(302,250), new Point(302,251), 
		new Point(303,252), new Point(303,253), new Point(304,253), new Point(304,254), new Point(305,254), new Point(305,255), new Point(305,256), new Point(306,256), new Point(306,257), new Point(307,257), 
		new Point(307,258), new Point(308,258), new Point(308,259), new Point(309,259), new Point(309,260), new Point(310,260), new Point(310,261), new Point(311,261), new Point(311,262), new Point(312,262), 
		new Point(312,263), new Point(313,263), new Point(313,264), new Point(314,264), new Point(315,265), new Point(316,265), new Point(316,266), new Point(317,266), new Point(318,267), new Point(319,267), 
		new Point(319,268), new Point(320,268), new Point(321,269), new Point(322,269), new Point(323,270), new Point(324,270), new Point(324,271), new Point(325,271), new Point(326,271), new Point(327,271), 
		new Point(327,272), new Point(328,272), new Point(329,272), new Point(329,273), new Point(330,273), new Point(331,273), new Point(332,273), new Point(332,274), new Point(333,274), new Point(334,274), 
		new Point(335,274), new Point(335,275), new Point(336,275), new Point(337,275), new Point(338,275), new Point(339,275), new Point(339,276), new Point(340,276), new Point(341,276), new Point(342,276), 
		new Point(343,276), new Point(344,276), new Point(345,276), new Point(344,276), new Point(344,277), new Point(345,277), new Point(346,277), new Point(347,277), new Point(348,277), new Point(349,277), 
		new Point(350,277), new Point(351,277), new Point(352,277), new Point(353,277), new Point(354,277), new Point(355,277), new Point(356,277), new Point(357,277), new Point(358,277), new Point(359,277), 
		new Point(360,277), new Point(361,277), new Point(362,277), new Point(363,277), new Point(364,277), new Point(363,277), new Point(362,277), new Point(362,278), new Point(363,278), new Point(364,278), 
		new Point(365,278), new Point(366,278), new Point(367,278), new Point(367,279), new Point(366,280), new Point(365,281), new Point(364,281), new Point(364,282), new Point(363,282), new Point(362,282), 
		new Point(362,283), new Point(361,283), new Point(360,283), new Point(360,284), new Point(359,284), new Point(358,284), new Point(357,284), new Point(356,284), new Point(357,284), new Point(357,285), 
		new Point(356,285), new Point(355,285), new Point(354,285), new Point(353,285), new Point(352,285), new Point(351,285), new Point(350,285), new Point(349,285), new Point(349,286), new Point(348,286), 
		new Point(348,285), new Point(347,285), new Point(346,285), new Point(345,285), new Point(344,285), new Point(343,285), new Point(342,285), new Point(341,285), new Point(340,285), new Point(339,285), 
		new Point(339,284), new Point(338,284), new Point(337,284), new Point(336,284), new Point(335,284), new Point(334,284), new Point(334,283), new Point(333,283), new Point(332,283), new Point(331,283), 
		new Point(330,283), new Point(331,283), new Point(331,282), new Point(330,282), new Point(329,282), new Point(328,282), new Point(327,282), new Point(327,281), new Point(326,281), new Point(325,281), 
		new Point(324,281), new Point(324,280), new Point(323,280), new Point(322,280), new Point(322,279), new Point(321,279), new Point(320,279), new Point(319,279), new Point(319,278), new Point(318,278), 
		new Point(317,278), new Point(317,277), new Point(316,277), new Point(315,277), new Point(315,276), new Point(314,276), new Point(313,276), new Point(313,275), new Point(312,275), new Point(311,275), 
		new Point(310,274), new Point(309,274), new Point(309,273), new Point(308,273), new Point(307,273), new Point(307,272), new Point(306,272), new Point(306,271), new Point(305,271), new Point(304,271), 
		new Point(304,270), new Point(303,270), new Point(303,269), new Point(302,269), new Point(301,268), new Point(300,267), new Point(299,266), new Point(299,265), new Point(298,265), new Point(298,264), 
		new Point(298,263), new Point(298,262), new Point(297,262), new Point(297,261), new Point(297,260), new Point(297,259), new Point(297,258), new Point(296,258), new Point(296,257), new Point(296,256), 
		new Point(296,255), new Point(296,254), new Point(296,253), new Point(296,252), new Point(296,251), new Point(296,250), new Point(296,249), new Point(296,248), new Point(296,249), new Point(296,250), 
		new Point(296,251), new Point(296,252), new Point(295,252), new Point(295,251), new Point(295,250), new Point(295,249), new Point(295,248), new Point(295,247), new Point(295,246), new Point(295,245), 
		new Point(295,244), new Point(295,243), new Point(295,242), new Point(295,241), new Point(295,240), new Point(295,239), new Point(295,238), new Point(295,237), new Point(295,236), new Point(295,235), 
		new Point(295,234), new Point(295,233), new Point(294,233), new Point(294,232), new Point(294,231), new Point(294,230), new Point(293,230), new Point(293,229), new Point(293,228), new Point(293,227), 
		new Point(293,226), new Point(293,225), new Point(293,226), new Point(292,226), new Point(292,225), new Point(292,224), new Point(292,223), new Point(292,222), new Point(292,221), new Point(292,220), 
		new Point(292,219), new Point(292,218), new Point(292,217), new Point(292,216), new Point(292,217), new Point(292,218), new Point(292,219), new Point(292,220), new Point(291,220), new Point(291,219), 
		new Point(291,218), new Point(291,217), new Point(291,216), new Point(291,215), new Point(291,214), new Point(291,213), new Point(291,212), new Point(291,211), new Point(291,210), new Point(291,209), 
		new Point(291,208), new Point(291,207), new Point(291,206), new Point(291,205), new Point(291,204), new Point(291,203), new Point(291,202), new Point(291,201), new Point(291,200), new Point(291,199), 
		new Point(291,198), new Point(291,199), new Point(291,200), new Point(291,201), new Point(291,202), new Point(291,203), new Point(291,204), new Point(290,204), new Point(290,203), new Point(290,202), 
		new Point(290,201), new Point(290,200), new Point(290,199), new Point(290,198), new Point(290,197), new Point(290,196), new Point(290,195), new Point(290,194), new Point(290,193), new Point(290,192), 
		new Point(290,191), new Point(290,190), new Point(290,189), new Point(290,188), new Point(290,187), new Point(290,186), new Point(290,187), new Point(290,188), new Point(290,189), new Point(290,190), 
		new Point(290,191), new Point(291,191), new Point(291,190), new Point(291,189), new Point(291,188), new Point(291,187), new Point(291,186), new Point(291,185), new Point(291,184), new Point(291,183), 
		new Point(291,182), new Point(291,181), new Point(291,180), new Point(292,180), new Point(292,179), new Point(292,178), new Point(292,177), new Point(292,176), new Point(293,176), new Point(293,175), 
		new Point(293,174), new Point(293,173), new Point(294,173), new Point(294,172), new Point(294,171), new Point(295,170), new Point(295,169), new Point(295,168), new Point(296,168), new Point(296,167), 
		new Point(296,166), new Point(297,166), new Point(297,165), new Point(297,164), new Point(297,163), new Point(297,162), new Point(297,161), new Point(297,160), new Point(297,159), new Point(297,158), 
		new Point(297,157), new Point(297,156), new Point(297,155), new Point(296,155), new Point(296,154), new Point(296,153), new Point(296,152), new Point(295,152), new Point(295,151), new Point(295,150), 
		new Point(294,150), new Point(294,149), new Point(294,148), new Point(293,148), new Point(293,147), new Point(293,146), new Point(292,146), new Point(292,145), new Point(292,144), new Point(291,144), 
		new Point(291,143), new Point(291,142), new Point(290,142), new Point(290,141), new Point(290,140), new Point(289,140), new Point(289,139), new Point(288,138), new Point(288,137), new Point(287,136), 
		new Point(287,135), new Point(286,134), new Point(286,133), new Point(286,132), new Point(285,132), new Point(285,131), new Point(285,130), new Point(284,130), new Point(284,129), new Point(284,128), 
		new Point(284,127), new Point(284,126), new Point(284,127), new Point(283,127), new Point(283,126), new Point(283,125), new Point(283,124), new Point(283,123), new Point(282,123), new Point(282,122), 
		new Point(282,121), new Point(282,120), new Point(282,119), new Point(282,118), new Point(281,118), new Point(281,117), new Point(281,116), new Point(281,115), new Point(281,114), new Point(280,114), 
		new Point(280,113), new Point(280,112), new Point(280,111), new Point(279,111), new Point(279,110), new Point(279,109), new Point(279,108), new Point(278,108), new Point(278,107), new Point(278,106), 
		new Point(277,106), new Point(277,105), new Point(276,104), new Point(276,103), new Point(275,103), new Point(275,102), new Point(274,101), new Point(273,100), new Point(272,99), new Point(271,98), 
		new Point(270,98), new Point(270,97), new Point(269,97), new Point(268,96), new Point(267,96), new Point(267,95), new Point(266,95), new Point(265,95), new Point(265,94), new Point(264,94), 
		new Point(263,94), new Point(262,94), new Point(261,93), new Point(260,93), new Point(259,93), new Point(258,93), new Point(257,93), new Point(256,93), new Point(255,93), new Point(254,93), 
		new Point(253,93), new Point(252,93), new Point(251,93), new Point(250,93), new Point(249,93), new Point(248,93), new Point(247,93), new Point(246,93), new Point(245,93), new Point(244,93), 
		new Point(243,93), new Point(242,93), new Point(241,93), new Point(240,93), new Point(239,93), new Point(238,93), new Point(237,93), new Point(236,93), new Point(235,93), new Point(234,93), 
		new Point(233,93), new Point(232,93), new Point(231,93), new Point(230,93), new Point(229,93), new Point(228,93), new Point(229,93), new Point(230,93), new Point(230,94), new Point(229,94), 
		new Point(228,94), new Point(227,94), new Point(226,94), new Point(226,95), new Point(225,95), new Point(224,95), new Point(225,95), new Point(226,95), new Point(226,94), new Point(225,94), 
		new Point(224,94), new Point(223,94), new Point(222,94), new Point(221,94), new Point(220,94), new Point(219,94), new Point(218,94), new Point(219,94), new Point(219,95), new Point(218,95), 
		new Point(217,95), new Point(216,95), new Point(215,95), new Point(214,95), new Point(213,95), new Point(212,95), new Point(211,95), new Point(210,95), new Point(209,95), new Point(210,95), 
		new Point(210,96), new Point(209,96), new Point(208,96), new Point(207,96), new Point(206,96), new Point(205,96), new Point(204,96), new Point(203,96), new Point(202,96), new Point(203,96), 
		new Point(203,97), new Point(202,97), new Point(201,97), new Point(200,97), new Point(199,97), new Point(198,97), new Point(198,98), new Point(197,98), new Point(196,98), new Point(195,98), 
		new Point(195,99), new Point(194,99), new Point(193,99), new Point(192,99), new Point(192,100), new Point(191,100), new Point(190,100), new Point(190,101), new Point(189,101), new Point(188,101), 
		new Point(187,101), new Point(187,102), new Point(186,102), new Point(185,102), new Point(185,103), new Point(184,103), new Point(183,103), new Point(182,103), new Point(182,104), new Point(181,104), 
		new Point(180,104), new Point(180,105), new Point(179,105), new Point(178,105), new Point(178,106), new Point(177,106), new Point(176,106), new Point(175,106), new Point(175,107), new Point(174,107), 
		new Point(173,107), new Point(173,108), new Point(172,108), new Point(172,109), new Point(171,109), new Point(170,109), new Point(169,109), new Point(168,110), new Point(167,110), new Point(167,111), 
		new Point(166,111), new Point(165,111), new Point(164,111), new Point(164,112), new Point(163,112), new Point(162,112), new Point(162,113), new Point(161,113), new Point(160,113), new Point(160,114), 
		new Point(159,114), new Point(158,114), new Point(158,115), new Point(157,115), new Point(156,115), new Point(156,116), new Point(155,116), new Point(154,116), new Point(154,117), new Point(153,117), 
		new Point(152,117), new Point(152,118), new Point(151,118), new Point(150,118), new Point(150,119), new Point(149,119), new Point(148,119), new Point(148,120), new Point(147,120), new Point(146,120), 
		new Point(146,121), new Point(145,121), new Point(144,121), new Point(144,122), new Point(143,122), new Point(142,123), new Point(141,123), new Point(141,124), new Point(140,124), new Point(139,124), 
		new Point(139,125), new Point(138,125), new Point(137,125), new Point(137,126), new Point(136,126), new Point(135,126), new Point(135,127), new Point(134,127), new Point(133,128), new Point(132,128), 
		new Point(132,129), new Point(131,129), new Point(130,129), new Point(130,130), new Point(129,130), new Point(128,131), new Point(127,131), new Point(127,132), new Point(126,132), new Point(125,133), 
		new Point(124,133), new Point(124,134), new Point(123,134), new Point(122,135), new Point(121,135), new Point(121,136), new Point(120,136), new Point(119,137), new Point(118,137), new Point(118,138), 
		new Point(117,138), new Point(116,139), new Point(115,139), new Point(115,140), new Point(114,140), new Point(113,141), new Point(112,141), new Point(112,142), new Point(111,142), new Point(111,143), 
		new Point(110,143), new Point(109,144), new Point(108,144), new Point(108,145), new Point(107,145), new Point(107,146), new Point(106,146), new Point(106,147), new Point(105,147), new Point(104,148), 
		new Point(103,149), new Point(102,149), new Point(102,150), new Point(101,150), new Point(101,151), new Point(100,151), new Point(100,152), new Point(99,152), new Point(98,153), new Point(97,154), 
		new Point(96,155), new Point(95,156), new Point(94,157), new Point(93,158), new Point(92,159), new Point(91,160), new Point(90,161), new Point(90,162), new Point(89,162), new Point(89,163), 
		new Point(88,163), new Point(88,164), new Point(87,164), new Point(87,165), new Point(86,166), new Point(85,167), new Point(85,168), new Point(84,168), new Point(84,169), new Point(83,170), 
		new Point(83,171), new Point(82,171), new Point(82,172), new Point(81,172), new Point(81,173), new Point(81,174), new Point(80,174), new Point(80,175), new Point(80,176), new Point(79,176), 
		new Point(79,177), new Point(79,178), new Point(78,178), new Point(78,179), new Point(78,180), new Point(77,180), new Point(77,181), new Point(77,182), new Point(76,183), new Point(76,184), 
		new Point(76,185), new Point(75,186), new Point(75,187), new Point(75,188), new Point(74,189), new Point(74,190), new Point(74,191), new Point(74,192), new Point(74,193), new Point(73,194), 
		new Point(73,195), new Point(73,196), new Point(73,197), new Point(73,198), new Point(72,199), new Point(72,200), new Point(72,201), new Point(72,202), new Point(72,203), new Point(72,204), 
		new Point(71,205), new Point(71,206), new Point(71,207), new Point(71,208), new Point(71,209), new Point(71,210), new Point(71,211), new Point(71,212), new Point(71,213), new Point(71,214), 
		new Point(71,215), new Point(70,216), new Point(70,217), new Point(70,218), new Point(70,219), new Point(70,220), new Point(70,221), new Point(70,222), new Point(70,223), new Point(69,223), 
		new Point(69,224), new Point(69,225), new Point(69,226), new Point(69,227), new Point(69,228), new Point(69,229), new Point(69,230), new Point(69,231), new Point(69,232), new Point(69,233), 
		new Point(69,234), new Point(69,235), new Point(69,236), new Point(69,237), new Point(68,238), new Point(68,239), new Point(68,240), new Point(68,241), new Point(68,242), new Point(68,243), 
		new Point(68,244), new Point(68,245), new Point(68,246), new Point(68,247), new Point(68,248), new Point(68,249), new Point(68,250), new Point(68,251), new Point(68,252), new Point(68,253), 
		new Point(68,254), new Point(68,255), new Point(68,256), new Point(68,257), new Point(68,258), new Point(68,259), new Point(69,260), new Point(69,261), new Point(69,262), new Point(69,263), 
		new Point(69,264), new Point(69,265), new Point(69,266), new Point(69,267), new Point(69,268), new Point(69,269), new Point(69,270), new Point(69,271), new Point(69,272), new Point(69,273), 
		new Point(69,274), new Point(69,275), new Point(69,276), new Point(69,277), new Point(69,278), new Point(70,279), new Point(70,280), new Point(70,281), new Point(70,282), new Point(70,283), 
		new Point(70,284), new Point(70,285), new Point(70,286), new Point(70,287), new Point(70,288), new Point(71,289), new Point(71,290), new Point(71,291), new Point(71,292), new Point(71,293), 
		new Point(71,294), new Point(72,295), new Point(72,296), new Point(72,297), new Point(72,298), new Point(72,299), new Point(72,300), new Point(73,301), new Point(73,302), new Point(73,303), 
		new Point(73,304), new Point(73,305), new Point(74,306), new Point(74,307), new Point(74,308), new Point(75,309), new Point(75,310), new Point(75,311), new Point(75,312), new Point(76,313), 
		new Point(76,314), new Point(76,315), new Point(76,316), new Point(77,317), new Point(77,318), new Point(77,319), new Point(78,320), new Point(78,321), new Point(78,322), new Point(78,323), 
		new Point(79,324), new Point(79,325), new Point(79,326), new Point(80,327), new Point(80,328), new Point(80,329), new Point(80,330), new Point(80,331), new Point(81,332), new Point(81,333), 
		new Point(81,334), new Point(82,335), new Point(82,336), new Point(83,337), new Point(83,338), new Point(84,339), new Point(84,340), new Point(85,341), new Point(86,342), new Point(87,343), 
		new Point(88,344), new Point(89,344), new Point(90,345), new Point(91,345), new Point(92,346), new Point(93,346), new Point(94,347), new Point(95,347), new Point(96,348), new Point(97,348), 
		new Point(98,349), new Point(99,349), new Point(100,350), new Point(101,350), new Point(102,351), new Point(103,351), new Point(104,352), new Point(105,352), new Point(106,353), new Point(107,353), 
		new Point(108,354), new Point(109,354), new Point(110,355), new Point(111,355), new Point(112,356), new Point(113,356), new Point(114,356), new Point(115,357), new Point(116,357), new Point(117,358), 
		new Point(118,358), new Point(119,358), new Point(120,359), new Point(121,359), new Point(122,360), new Point(123,360), new Point(124,361), new Point(125,361), new Point(126,361), new Point(127,362), 
		new Point(128,362), new Point(129,362), new Point(130,363), new Point(131,363), new Point(132,363), new Point(133,364), new Point(134,364), new Point(135,365), new Point(136,365), new Point(137,365), 
		new Point(138,366), new Point(139,366), new Point(140,366), new Point(141,366), new Point(142,367), new Point(143,367), new Point(144,367), new Point(145,368), new Point(146,368), new Point(147,368), 
		new Point(148,368), new Point(149,369), new Point(150,369), new Point(151,369), new Point(152,369), new Point(153,370), new Point(154,370), new Point(155,370), new Point(156,370), new Point(157,370), 
		new Point(158,371), new Point(159,371), new Point(160,371), new Point(161,371), new Point(162,371), new Point(163,372), new Point(164,372), new Point(165,372), new Point(166,372), new Point(167,372), 
		new Point(168,372), new Point(169,372), new Point(170,372), new Point(171,373), new Point(172,373), new Point(173,373), new Point(174,373), new Point(175,373), new Point(176,373), new Point(177,373), 
		new Point(178,373), new Point(179,373), new Point(180,373), new Point(181,373), new Point(182,373), new Point(183,373), new Point(184,373), new Point(185,373), new Point(186,372), new Point(187,372), 
		new Point(188,372), new Point(189,372), new Point(190,372), new Point(191,372), new Point(192,372), new Point(193,371), new Point(194,371), new Point(195,371), new Point(196,371), new Point(197,371), 
		new Point(198,370), new Point(199,370), new Point(200,370), new Point(201,369), new Point(202,369), new Point(203,369), new Point(204,368), new Point(205,368), new Point(206,367), new Point(207,367), 
		new Point(208,366), new Point(209,366), new Point(210,365), new Point(211,365), new Point(212,364), new Point(213,363), new Point(214,362), new Point(215,362), new Point(216,361), new Point(217,360), 
		new Point(218,359), new Point(219,358), new Point(220,357), new Point(221,356), new Point(221,355), new Point(222,354), new Point(223,353), new Point(223,352), new Point(224,351), new Point(225,350), 
		new Point(225,349), new Point(226,348), new Point(226,347), new Point(227,346), new Point(227,345), new Point(228,344), new Point(228,343), new Point(229,342), new Point(229,341), new Point(229,340), 
		new Point(230,339), new Point(230,338), new Point(230,337), new Point(230,336), new Point(231,335), new Point(231,334), new Point(231,333), new Point(231,332), new Point(232,331), new Point(232,330), 
		new Point(232,329), new Point(233,328), new Point(233,327), new Point(233,326), new Point(233,325), new Point(234,324), new Point(234,323), new Point(234,322), new Point(234,321), new Point(235,320), 
		new Point(235,319), new Point(235,318), new Point(235,317), new Point(236,316), new Point(236,315), new Point(236,314), new Point(236,313), new Point(236,312), new Point(237,311), new Point(237,310), 
		new Point(237,309), new Point(237,308), new Point(237,307), new Point(237,306), new Point(237,305), new Point(237,304), new Point(238,303), new Point(238,302), new Point(238,301), new Point(238,300), 
		new Point(238,299), new Point(238,298), new Point(238,297), new Point(239,296), new Point(239,295), new Point(239,294), new Point(239,293), new Point(240,292), new Point(240,291), new Point(240,290), 
		new Point(241,289), new Point(241,288), new Point(242,287), new Point(242,286), new Point(242,285), new Point(243,284), new Point(244,283), new Point(245,282), new Point(246,281), new Point(246,280), 
		new Point(247,279), new Point(248,278), new Point(249,277), new Point(250,276), new Point(251,275), new Point(251,274), new Point(252,273), new Point(252,272), new Point(252,271), new Point(252,270), 
		new Point(252,269), new Point(251,268), new Point(250,267), new Point(249,266), new Point(248,265), new Point(247,264), new Point(246,263), new Point(245,262), new Point(244,262), new Point(244,261), 
		new Point(243,261), new Point(242,261), new Point(242,260), new Point(241,260), new Point(240,259), new Point(239,258), new Point(238,258), new Point(237,257), new Point(236,257), new Point(236,256), 
		new Point(235,256), new Point(235,255), new Point(234,255), new Point(233,255), new Point(232,254), new Point(231,254), new Point(230,253), new Point(229,253), new Point(228,252), new Point(227,251), 
		new Point(226,251), new Point(225,250), new Point(224,250), new Point(223,249), new Point(222,249), new Point(221,248), new Point(220,248), new Point(219,247), new Point(218,247), new Point(217,247), 
		new Point(217,246), new Point(216,246), new Point(216,245), new Point(215,245), new Point(214,245), new Point(214,244), new Point(213,244), new Point(212,244), new Point(212,243), new Point(211,243), 
		new Point(210,243), new Point(210,242), new Point(209,242), new Point(208,241), new Point(207,241), new Point(206,240), new Point(205,240), new Point(204,239), new Point(203,239), new Point(202,238), 
		new Point(201,238), new Point(200,237), new Point(199,237), new Point(198,236), new Point(197,236), new Point(197,235), new Point(196,235), new Point(195,234), new Point(194,234), new Point(194,233), 
		new Point(193,233), new Point(192,232), new Point(191,231), new Point(190,231), new Point(190,230), new Point(189,230), new Point(189,229), new Point(188,229), new Point(188,228), new Point(187,228), 
		new Point(187,227), new Point(186,227), new Point(186,226), new Point(185,226), new Point(185,225), new Point(185,224), new Point(184,224), new Point(183,223), new Point(183,222), new Point(182,222), 
		new Point(182,221), new Point(182,220), new Point(181,220), new Point(181,219), new Point(180,218), new Point(180,217), new Point(180,216), new Point(179,216), new Point(179,215), new Point(179,214), 
		new Point(179,213), new Point(178,213), new Point(178,212), new Point(178,211), new Point(178,210), new Point(178,209), new Point(178,208), new Point(177,208), new Point(177,207), new Point(177,206), 
		new Point(177,205), new Point(177,204), new Point(177,203), new Point(177,202), new Point(177,201), new Point(177,200), new Point(177,199), new Point(178,199), new Point(178,198), new Point(178,197), 
		new Point(178,196), new Point(178,195), new Point(178,194), new Point(179,193), new Point(179,192), new Point(179,191), new Point(179,190), new Point(179,189), new Point(180,189), new Point(180,188), 
		new Point(180,187), new Point(180,186), new Point(181,186), new Point(181,185), new Point(181,184), new Point(182,184), new Point(182,183), new Point(182,182), new Point(182,181), new Point(182,180), 
		new Point(182,179), new Point(182,178), new Point(182,177), new Point(182,176), new Point(182,177), new Point(182,178), new Point(182,179), new Point(182,180), new Point(182,181), new Point(183,181), 
		new Point(183,180), new Point(183,179), new Point(183,178), new Point(183,177), new Point(183,176), new Point(183,175), new Point(183,174), new Point(183,173), new Point(183,172), new Point(183,171), 
		new Point(184,171), new Point(184,170), new Point(185,170), new Point(185,169), new Point(186,168), new Point(186,167), new Point(187,167), new Point(188,166), new Point(189,166), new Point(190,166), 
		new Point(191,166), new Point(190,166), new Point(189,166), new Point(189,165), new Point(190,165), new Point(191,165), new Point(191,164), new Point(192,164), new Point(193,164), new Point(194,164), 
		new Point(193,164), new Point(192,164), new Point(192,163), new Point(193,163), new Point(193,162), new Point(194,161), new Point(194,160), new Point(195,160), new Point(195,159), new Point(196,158), 
		new Point(197,157), new Point(197,156), new Point(198,156), new Point(198,155), new Point(199,154), new Point(199,153), new Point(200,153), new Point(200,152), new Point(201,152), new Point(201,151), 
		new Point(202,151), new Point(202,150), new Point(203,149), new Point(203,148), new Point(204,148), new Point(204,147), new Point(205,147), new Point(205,146), new Point(206,146), new Point(206,145), 
		new Point(207,144), new Point(208,143), new Point(209,142), new Point(209,141), new Point(210,141), new Point(210,140), new Point(211,140), new Point(211,139), new Point(212,139), new Point(212,138), 
		new Point(213,138), new Point(213,137), new Point(214,137), new Point(214,136), new Point(215,135), new Point(216,134), new Point(217,133), new Point(218,132), new Point(219,131), new Point(220,130), 
		new Point(221,129), new Point(222,128), new Point(223,127), new Point(224,126), new Point(225,125), new Point(226,124), new Point(227,123), new Point(228,122), new Point(229,121), new Point(229,120), 
		new Point(230,120), new Point(230,119), new Point(231,119), new Point(231,118), new Point(232,118), new Point(233,118), new Point(233,117), new Point(234,117), new Point(234,116), new Point(235,116), 
		new Point(235,115), new Point(236,115), new Point(237,115), new Point(237,114), new Point(238,114), new Point(238,113), new Point(238,112), new Point(239,112), new Point(239,111), new Point(240,111), 
		new Point(240,110), new Point(241,110), new Point(241,109), new Point(242,109), new Point(243,109), new Point(243,108), new Point(244,108), new Point(244,107), new Point(245,107), new Point(245,106), 
		new Point(246,106), new Point(246,105), new Point(247,105), new Point(248,104), new Point(249,103), new Point(250,103), new Point(250,102), new Point(251,102), new Point(251,101), new Point(252,101), 
		new Point(252,100), new Point(253,100), new Point(253,99), new Point(254,99), new Point(255,98), new Point(256,98), new Point(256,97), new Point(257,97), new Point(257,96), new Point(258,96), 
		new Point(258,95), new Point(259,95), new Point(260,94), new Point(260,93), new Point(261,93), new Point(262,93), new Point(262,92), new Point(263,92), new Point(263,91), new Point(264,91), 
		new Point(265,90), new Point(266,90), new Point(266,89), new Point(267,89), new Point(267,88), new Point(268,88), new Point(269,87), new Point(270,87), new Point(270,86), new Point(271,86), 
		new Point(271,85), new Point(272,85), new Point(273,84), new Point(274,84), new Point(274,83), new Point(275,83), new Point(276,82), new Point(277,82), new Point(277,81), new Point(278,81), 
		new Point(278,80), new Point(279,80), new Point(280,79), new Point(281,79), new Point(281,78), new Point(282,78), new Point(283,78), new Point(283,77), new Point(284,77), new Point(285,77), 
		new Point(286,77), new Point(287,77), new Point(288,77), new Point(289,77), new Point(289,78), new Point(290,78), new Point(291,78), new Point(291,79), new Point(292,79), new Point(292,80), 
		new Point(293,80), new Point(293,81), new Point(294,81), new Point(294,82), new Point(295,83), new Point(295,84), new Point(296,84), new Point(296,85), new Point(296,86), new Point(297,86), 
		new Point(297,87), new Point(297,88), new Point(298,88), new Point(298,89), new Point(298,90), new Point(299,91), new Point(299,92), new Point(299,93), new Point(300,93), new Point(300,94), 
		new Point(300,95), new Point(300,96), new Point(301,96), new Point(301,97), new Point(301,98), new Point(301,99), new Point(302,99), new Point(302,100), new Point(302,101), new Point(302,102), 
		new Point(303,102), new Point(303,103), new Point(303,104), new Point(303,105), new Point(304,105), new Point(304,106), new Point(304,107), new Point(304,108), new Point(305,108), new Point(305,109), 
		new Point(305,110), new Point(305,111), new Point(306,111), new Point(306,112), new Point(306,113), new Point(306,114), new Point(306,115), new Point(307,115), new Point(307,116), new Point(307,117), 
		new Point(307,118), new Point(308,118), new Point(308,119), new Point(308,120), new Point(308,121), new Point(309,121), new Point(309,122), new Point(309,123), new Point(310,124), new Point(310,125), 
		new Point(310,126), new Point(311,126), new Point(311,127), new Point(311,128), new Point(312,129), new Point(312,130), new Point(312,131), new Point(313,131), new Point(313,132), new Point(313,133), 
		new Point(314,133), new Point(314,134), new Point(314,135), new Point(315,135), new Point(315,136), new Point(315,137), new Point(315,138), new Point(316,138), new Point(316,139), new Point(316,140), 
		new Point(316,141), new Point(316,142), new Point(316,143), new Point(316,144), new Point(316,143), new Point(317,143), new Point(317,144), new Point(317,145), new Point(317,146), new Point(317,147), 
		new Point(317,148), new Point(317,149), new Point(317,150), new Point(317,151), new Point(317,152), new Point(317,153), new Point(317,154), new Point(317,155), new Point(317,156), new Point(317,157), 
		new Point(317,156), new Point(316,156), new Point(316,157), new Point(316,158), new Point(316,159), new Point(316,160), new Point(316,161), new Point(316,162), new Point(316,163), new Point(315,163), 
		new Point(315,164), new Point(315,165), new Point(315,166), new Point(315,167), new Point(315,168), new Point(314,168), new Point(314,169), new Point(314,170), new Point(314,171), new Point(314,172), 
		new Point(313,172), new Point(313,173), new Point(313,174), new Point(313,175), new Point(313,176), new Point(312,176), new Point(312,177), new Point(312,178), new Point(312,179), new Point(311,179), 
		new Point(311,180), new Point(311,181), new Point(311,182), new Point(310,182), new Point(310,183), new Point(310,184), new Point(310,185), new Point(309,185), new Point(309,186), new Point(309,187), 
		new Point(309,188), new Point(308,188), new Point(308,189), new Point(308,190), new Point(308,191), new Point(307,191), new Point(307,192), new Point(307,193), new Point(306,193), new Point(306,194), 
		new Point(306,195), new Point(306,196), new Point(305,196), new Point(305,197), new Point(305,198), new Point(304,198), new Point(304,199), new Point(304,200), new Point(303,200), new Point(303,201), 
		new Point(303,202), new Point(303,203), new Point(302,203), new Point(302,204), new Point(302,205), new Point(301,205), new Point(301,206), new Point(301,207), new Point(301,208), new Point(300,208), 
		new Point(300,209), new Point(300,210), new Point(300,211), new Point(299,211), new Point(299,212), new Point(299,213), new Point(299,214), new Point(298,214), new Point(298,215), new Point(298,216), 
		new Point(298,217), new Point(298,218), new Point(297,218), new Point(297,219), new Point(297,220), new Point(297,221), new Point(297,222), new Point(297,223), new Point(297,224), new Point(297,225), 
		new Point(296,225), new Point(296,226), new Point(296,227), new Point(296,228), new Point(296,229), new Point(296,230), new Point(296,231), new Point(296,232), new Point(296,233), new Point(296,234), 
		new Point(297,234), new Point(297,235), new Point(298,235), new Point(298,236), new Point(298,237), new Point(298,236), new Point(297,236), new Point(297,237), new Point(296,237), new Point(296,238), 
		new Point(296,239), new Point(296,240), new Point(297,240), new Point(297,241), new Point(297,242), new Point(298,242), new Point(298,243), new Point(298,244), new Point(298,245), new Point(298,246), 
		new Point(298,245), new Point(299,245), new Point(299,246), new Point(299,247), new Point(299,248), new Point(298,248), new Point(298,249), new Point(299,249), new Point(299,250), new Point(299,251), 
		new Point(299,252), new Point(298,252), new Point(299,253), new Point(299,254), new Point(300,254), new Point(301,254), new Point(302,254), new Point(303,254), new Point(304,254), new Point(305,254), 
		new Point(304,254), new Point(303,254), new Point(303,253), new Point(304,253), new Point(305,253), new Point(305,252), new Point(306,252), new Point(306,251), new Point(306,250), new Point(307,250), 
		new Point(307,249), new Point(307,248), new Point(307,247), new Point(307,246), new Point(307,247), new Point(307,248), new Point(307,249), new Point(307,250), new Point(306,250), new Point(306,249), 
		new Point(306,248), new Point(306,247), new Point(306,246), new Point(306,245), new Point(306,244), new Point(306,243), new Point(306,242), new Point(305,242), new Point(305,241), new Point(305,240), 
		new Point(305,239), new Point(304,239), new Point(304,238), new Point(304,237), new Point(304,236), new Point(303,236), new Point(303,235), new Point(303,234), new Point(302,234), new Point(302,233), 
		new Point(302,232), new Point(301,232), new Point(301,231), new Point(301,230), new Point(300,230), new Point(300,229), new Point(300,228), new Point(299,228), new Point(299,227), new Point(299,226), 
		new Point(298,226), new Point(298,225), new Point(297,225), new Point(297,224), new Point(296,224), new Point(296,225), new Point(296,226), new Point(296,227), new Point(296,228), new Point(296,229), 
		new Point(295,229), new Point(295,230), new Point(295,231), new Point(295,232), new Point(295,233), new Point(295,234), new Point(295,235), new Point(295,236), new Point(295,237), new Point(295,238), 
		new Point(295,239), new Point(295,240), new Point(295,241), new Point(295,242), new Point(295,243), new Point(294,243), new Point(294,244), new Point(295,244), new Point(295,245), new Point(294,245), 
		new Point(294,246), new Point(295,246), new Point(295,247), new Point(295,248), new Point(294,248), new Point(294,249), new Point(295,249), new Point(295,250), new Point(294,250), new Point(294,251), 
		new Point(295,251), new Point(295,252), new Point(294,252), new Point(294,253), new Point(294,254), new Point(294,255), new Point(294,256), new Point(294,257), new Point(294,258), new Point(294,259), 
		new Point(294,260), new Point(294,261), new Point(294,262), new Point(294,263), new Point(294,264), new Point(294,265), new Point(294,266), new Point(294,267), new Point(294,268), new Point(294,269), 
		new Point(294,270), new Point(294,271), new Point(294,272), new Point(294,273), new Point(294,274), new Point(294,275), new Point(294,276), new Point(293,277), new Point(293,278), new Point(293,279), 
		new Point(293,280), new Point(293,281), new Point(293,282), new Point(293,283), new Point(293,284), new Point(293,285), new Point(293,286), new Point(292,287), new Point(292,288), new Point(292,289), 
		new Point(292,290), new Point(292,291), new Point(292,292), new Point(292,293), new Point(292,294), new Point(292,295), new Point(292,296), new Point(292,297), new Point(291,297), new Point(291,298), 
		new Point(291,299), new Point(291,300), new Point(291,301), new Point(291,302), new Point(291,303), new Point(291,304), new Point(291,305), new Point(290,306), new Point(290,307), new Point(290,308), 
		new Point(290,309), new Point(290,310), new Point(290,311), new Point(290,312), new Point(290,313), new Point(289,314), new Point(289,315), new Point(289,316), new Point(289,317), new Point(289,318), 
		new Point(289,319), new Point(289,320), new Point(288,321), new Point(288,322), new Point(288,323), new Point(288,324), new Point(288,325), new Point(288,326), new Point(288,327), new Point(288,328), 
		new Point(288,329), new Point(287,329), new Point(287,330), new Point(287,331), new Point(287,332), new Point(287,333), new Point(287,334), new Point(287,335), new Point(287,334), new Point(286,334), 
		new Point(286,335), new Point(286,336), new Point(286,337), new Point(286,338), new Point(286,339), new Point(286,340), new Point(286,341), new Point(286,340), new Point(285,340), new Point(285,341), 
		new Point(285,342), new Point(285,343), new Point(285,344), new Point(285,345), new Point(285,346), new Point(285,345), new Point(284,345), new Point(284,346), new Point(284,347), new Point(284,348), 
		new Point(284,349), new Point(284,350), new Point(284,351), new Point(284,350), new Point(283,350), new Point(283,351), new Point(283,352), new Point(283,353), new Point(283,354), new Point(283,355), 
		new Point(282,355), new Point(282,356), new Point(282,357), new Point(282,358), new Point(282,359), new Point(281,359), new Point(281,360), new Point(281,361), new Point(281,362), new Point(281,363), 
		new Point(281,364), new Point(281,363), new Point(280,363), new Point(280,364), new Point(280,365), new Point(280,366), new Point(280,367), new Point(279,367), new Point(279,368), new Point(279,369), 
		new Point(279,370), new Point(279,371), new Point(278,371), new Point(278,372), new Point(278,373), new Point(278,374), new Point(278,375), new Point(278,374), new Point(277,374), new Point(277,375), 
		new Point(277,376), new Point(277,377), new Point(277,378), new Point(276,378), new Point(276,379), new Point(276,380), new Point(276,381), new Point(275,381), new Point(275,382), new Point(275,383), 
		new Point(275,384), new Point(274,384), new Point(274,385), new Point(274,386), new Point(274,387), new Point(273,387), new Point(273,388), new Point(273,389), new Point(273,390), new Point(272,390), 
		new Point(272,391), new Point(272,392), new Point(271,392), new Point(271,393), new Point(271,394), new Point(271,395), new Point(270,395), new Point(270,396), new Point(270,397), new Point(269,397), 
		new Point(269,398), new Point(269,399), new Point(268,399), new Point(268,400), new Point(268,401), new Point(267,401), new Point(267,402), new Point(267,403), new Point(266,404), new Point(266,405), 
		new Point(265,405), new Point(265,406), new Point(265,407), new Point(264,407), new Point(264,408), new Point(264,409), new Point(263,409), new Point(263,410), new Point(262,411), new Point(262,412), 
		new Point(261,412), new Point(261,413), new Point(261,414), new Point(260,414), new Point(260,415), new Point(259,415), new Point(259,416), new Point(258,417), new Point(258,418), new Point(257,418), 
		new Point(257,419), new Point(256,419), new Point(256,420), new Point(255,420), new Point(255,421), new Point(254,422), new Point(253,423), new Point(252,424), new Point(251,425), new Point(250,426), 
		new Point(249,427), new Point(248,427), new Point(248,428), new Point(247,428), new Point(247,429), new Point(246,429), new Point(246,430), new Point(245,430), new Point(244,431), new Point(243,431), 
		new Point(243,432), new Point(242,432), new Point(242,433), new Point(241,433), new Point(240,434), new Point(239,434), new Point(239,435), new Point(238,435), new Point(237,436), new Point(236,436), 
		new Point(236,437), new Point(235,437), new Point(234,437), new Point(234,438), new Point(233,438), new Point(232,438), new Point(232,439), new Point(231,439), new Point(230,439), new Point(230,440), 
		new Point(229,440), new Point(228,440), new Point(227,440), new Point(227,441), new Point(226,441), new Point(225,441), new Point(225,442), new Point(224,442), new Point(223,442), new Point(222,442), 
		new Point(222,443), new Point(221,443), new Point(220,443), new Point(219,443), new Point(218,443), new Point(218,444), new Point(217,444), new Point(216,444), new Point(215,444), new Point(214,444), 
		new Point(213,444), new Point(212,444), new Point(212,445), new Point(211,445), new Point(210,445), new Point(209,445), new Point(208,445), new Point(207,445), new Point(206,445), new Point(205,445), 
		new Point(204,445), new Point(203,445), new Point(202,445), new Point(202,444), new Point(201,444), new Point(200,444), new Point(199,444), new Point(198,444), new Point(198,443), new Point(197,443), 
		new Point(196,443), new Point(196,442), new Point(195,442), new Point(195,441), new Point(194,441), new Point(194,440));
	}
	

}
