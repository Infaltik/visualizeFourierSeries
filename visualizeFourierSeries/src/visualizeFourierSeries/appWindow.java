package visualizeFourierSeries;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.RenderingHints;
import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.text.DecimalFormat;
import java.util.ArrayList;

import javax.swing.JFrame;
import javax.swing.JPanel;


public class appWindow extends JFrame{
	
	JPanel rendering_panel;
	public static int rendering_panel_width = 1400;
	public static int rendering_panel_height = 1000; // atm the frame and rendering panel have the same size, need to add other containers
									   // to have other size
	int x = 0, y = 0;
	int prev_x = 0, prev_y = 0;
	ArrayList<Point> drawn_image_array = new ArrayList<Point>();
	ArrayList<arrowAndCircleRenderData> arrow_circle_render_data_array = new ArrayList<arrowAndCircleRenderData>();
	int current_app_status = 1;
	
	// Different application status values
	public static final int DRAWING_IMAGE = 1;
	public static final int RENDERING_FOURIER_ANIMATION = 2;
	public static double testAngle = 0;
	int testCount = 0;

	public appWindow(String window_title) {
		
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.setSize(rendering_panel_width, rendering_panel_height); // 900 750
		this.setTitle(window_title);
		this.setResizable(false);
		this.setLocationRelativeTo(null);
		
		rendering_panel = new JPanel() {
			protected void paintComponent(Graphics g) {
				Graphics2D g2 = (Graphics2D) g;
				super.paintComponent(g2);
				
				g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
				
				switch(current_app_status){
					case DRAWING_IMAGE:
						
						g2.drawString("Mouse position: " + x + ", " + y, 40, 40);
						
						complexNumber complex_value = mathematics.pixelToComplexNumber(new Point(x, y));
						DecimalFormat numberFormat = new DecimalFormat("0.0000");
						if(complex_value.getImagPart() < 0) {
							g2.drawString("Complex value: " + numberFormat.format(complex_value.getRealPart()) + "-" + numberFormat.format(Math.abs(complex_value.getImagPart())) + "i", 40, 80);
						}
						else {
							g2.drawString("Complex value: " + numberFormat.format(complex_value.getRealPart()) + "+" + numberFormat.format(Math.abs(complex_value.getImagPart())) + "i", 40, 80);
						}
						
						for(int i = 0; i < drawn_image_array.size()-1; i++) {
							
							int current_x = (int) drawn_image_array.get(i).getX();
							int current_y = (int) drawn_image_array.get(i).getY();
							int next_x = (int) drawn_image_array.get(i+1).getX();
							int next_y = (int) drawn_image_array.get(i+1).getY();
							
							g2.setStroke(new BasicStroke(5));
							g2.drawLine(current_x, current_y, next_x, next_y);
						}
						break;
						
					case RENDERING_FOURIER_ANIMATION:
						this.setBackground(new Color(Math.max(x/10, 0), Math.max(y/10, 0), 0));
						g2.setColor(Color.red);
						
						for(int i = 0; i < arrow_circle_render_data_array.size(); i++){
							drawArrow(g2, arrow_circle_render_data_array.get(i), true);
						}
					//	drawArrow(g2, testAngle, 0.4, 650, 500, false);
					//	drawArrow(g2, testAngle+0.5, 0.2, 700, 500, false);
						
					//	g2.setColor(Color.BLUE);
					//	g2.fillRect(625, 475, 50, 50);
						
						break;
					default:
						this.setBackground(Color.BLACK);
						break;
				}
				
				
			}
		};
		
		
		rendering_panel.addMouseMotionListener(new MouseMotionListener() {

			public void mouseDragged(MouseEvent e) {

				prev_x = x;
				prev_y = y;
				
				x = e.getX();
				y = e.getY();
				
				if(current_app_status == DRAWING_IMAGE){
					drawn_image_array.add(new Point(x, y));
					rendering_panel.repaint();
				}
				
			}

			public void mouseMoved(MouseEvent e) {
				prev_x = x;
				prev_y = y;
				
				x = e.getX();
				y = e.getY();
				
				if(current_app_status == DRAWING_IMAGE){
					rendering_panel.repaint();
				}
				
			}
		});
		
		rendering_panel.addMouseListener(new MouseListener(){

			public void mouseClicked(MouseEvent arg0) {}
			public void mouseEntered(MouseEvent arg0) {}
			public void mouseExited(MouseEvent arg0) {}
			public void mousePressed(MouseEvent arg0) {}

			public void mouseReleased(MouseEvent arg0) {
				
				if(current_app_status == DRAWING_IMAGE){
					System.out.println("Released mouse button");
					current_app_status = 2;
					mathematics.convertToComplexAndStoreFunction(drawn_image_array);
					System.out.println(mathematics.complexFunctionToApproximate.length);
					mathematics.calculateFourierSeriesCoefficients();
					
					Thread renderThread = new Thread(new renderLoop());
					renderThread.start();
				}
			}
			
		});
		
		rendering_panel.setBackground(Color.WHITE);
		this.add(rendering_panel);
		
		
		this.setVisible(true);
	}
	
	public void arrowPreRenderCalculations(double angle_in_radians, double scale, int x, int y){
		
	/*	// Arrowhead calculations
		Point point1 = mathematics.point2x2MatrixMult(mathematics.similarityTransformationMatrix(angle_in_radians, scale), new Point(50, -20));
		Point point2 = mathematics.point2x2MatrixMult(mathematics.similarityTransformationMatrix(angle_in_radians, scale), new Point(75, 0));
		Point point3 = mathematics.point2x2MatrixMult(mathematics.similarityTransformationMatrix(angle_in_radians, scale), new Point(50, 20));
		point1 = new Point(point1.x+x, point1.y+y);
		point2 = new Point(point2.x+x, point2.y+y);
		point3 = new Point(point3.x+x, point3.y+y);
		
		int[] xPoints = {point1.x, point2.x, point3.x};
		int[] yPoints = {point1.y, point2.y, point3.y};
		Polygon arrowHead = new Polygon(xPoints, yPoints, 3);
		
		// Arrow body calculations
		Point body_arrow_connection = mathematics.point2x2MatrixMult(mathematics.similarityTransformationMatrix(angle_in_radians, scale), new Point(50, 0));
		body_arrow_connection = new Point(body_arrow_connection.x+x, body_arrow_connection.y+y);
		
		// Circle radius calculations
		int circleRadius = (int) (scale*(50 + 25));
		
		arrowAndCircleRenderData data = new arrowAndCircleRenderData(x, y, arrowHead, body_arrow_connection, (int) scale*8, circleRadius); 
		
		
		arrow_circle_render_data_array.add(data);
	*/
		//arrowAndCircleRenderData arrow = calculateArrow(new complexNumber(Math.sqrt(this.x*this.x + this.y+this.y)/2000, testAngle, false), 0, this.x, this.y);
		
		if(testCount == 0){
			arrowAndCircleRenderData arrow = calculateArrow2(mathematics.pixelToComplexNumber(drawn_image_array.get(testCount)), 0);
			arrow_circle_render_data_array.add(arrow);
			arrowAndCircleRenderData arrow2 = calculateArrow2(new complexNumber(0.8,0), 1);
			arrow_circle_render_data_array.add(arrow2);
		}
		if(testCount < drawn_image_array.size()-1) {
			testCount++;
		}
		
		//ArrayList<arrowAndCircleRenderData> test = new ArrayList<arrowAndCircleRenderData>();
		//test.add(arrow);
		//arrow_circle_render_data_array = test;
	}
	
	public arrowAndCircleRenderData calculateArrow(complexNumber complex_value, int shift_index, int x, int y){
		// The offset the arrow should be translated
		int x_translation = x;
		int y_translation = y;
		if(shift_index != 0){
			int vector_sum_index = mathematics.shiftIndexToVectorSumIndex(shift_index);
			Point previous_end_point = arrow_circle_render_data_array.get(vector_sum_index-1).getArrowEndPoint();
			x_translation = previous_end_point.x;
			y_translation = previous_end_point.y;
		}
		
		// Calculate the magnitude of the arrow in unit of pixels
		double complex_magnitude = complex_value.getMagnitude();
		double pixel_magnitude = mathematics.complexMagnitudeToPixelMagnitude(complex_magnitude);
		
		// Calculate the angle of the arrow
		double angle_in_radians = complex_value.getArgument();
		
		// Arrow proportions
		double body_arrow_length = (2*pixel_magnitude)/3; // not getX, should be magnitude of complex number???
		double head_arrow_x_length = pixel_magnitude/3;
		double half_head_arrow_y_length = 0.8*head_arrow_x_length; // Could calculate these once and save them as to not calculate them over and over???
		
		// Arrowhead calculations
		Point point1 = mathematics.point2x2MatrixMult(mathematics.rotationMatrix(angle_in_radians), new Point((int) body_arrow_length, (int) -half_head_arrow_y_length));
		Point point2 = mathematics.point2x2MatrixMult(mathematics.rotationMatrix(angle_in_radians), new Point((int) (body_arrow_length+head_arrow_x_length), 0));
		Point point3 = mathematics.point2x2MatrixMult(mathematics.rotationMatrix(angle_in_radians), new Point((int) body_arrow_length, (int) half_head_arrow_y_length));
		point1 = new Point(point1.x+x_translation, -point1.y+y_translation);
		point2 = new Point(point2.x+x_translation, -point2.y+y_translation);
		point3 = new Point(point3.x+x_translation, -point3.y+y_translation);
				
		int[] xPoints = {point1.x, point2.x, point3.x};
		int[] yPoints = {point1.y, point2.y, point3.y};
		Polygon arrowHead = new Polygon(xPoints, yPoints, 3);
		
		// Arrow body calculations
		Point body_arrow_connection = mathematics.point2x2MatrixMult(mathematics.rotationMatrix(angle_in_radians), new Point((int) body_arrow_length, 0));
		body_arrow_connection = new Point(body_arrow_connection.x+x_translation, -body_arrow_connection.y+y_translation);
		float arrow_body_stroke = (float) (0.16*body_arrow_length);
		
		// Circle radius calculations
		int circleRadius = (int) (body_arrow_length + head_arrow_x_length);
		
		arrowAndCircleRenderData data = new arrowAndCircleRenderData(x_translation, y_translation, point2, arrowHead, body_arrow_connection, arrow_body_stroke, circleRadius); 
		
		return data;
	}
	public arrowAndCircleRenderData calculateArrow2(complexNumber complex_value, int shift_index){
		// The offset the arrow should be translated
		int x_translation = mathematics.originPixelX;
		int y_translation = mathematics.originPixelY;
		if(shift_index != 0){
			int vector_sum_index = mathematics.shiftIndexToVectorSumIndex(shift_index);
			Point previous_end_point = arrow_circle_render_data_array.get(vector_sum_index-1).getArrowEndPoint();
			x_translation = previous_end_point.x;
			y_translation = previous_end_point.y;
		}
		
		// Calculate the magnitude of the arrow in unit of pixels
		double complex_magnitude = complex_value.getMagnitude();
		double pixel_magnitude = mathematics.complexMagnitudeToPixelMagnitude(complex_magnitude);
				
		// Calculate the angle of the arrow
		double angle_in_radians = complex_value.getArgument();
				
		// Arrow proportions
		double head_arrow_x_length = pixel_magnitude/3;
		double head_arrow_half_y_length = 0.8*head_arrow_x_length; // Could calculate these once and save them as to not calculate them over and over???
		double body_arrow_x_length = (2*pixel_magnitude)/3;
		double body_arrow_half_y_length = 0.2*head_arrow_half_y_length;
		
		// Arrowhead calculations
		Point point1 = mathematics.point2x2MatrixMult(mathematics.rotationMatrix(angle_in_radians), new Point( (int) body_arrow_x_length, (int) -head_arrow_half_y_length ));
		Point point2 = mathematics.point2x2MatrixMult(mathematics.rotationMatrix(angle_in_radians), new Point( (int) (body_arrow_x_length+head_arrow_x_length), 0 ));
		Point point3 = mathematics.point2x2MatrixMult(mathematics.rotationMatrix(angle_in_radians), new Point( (int) body_arrow_x_length, (int) head_arrow_half_y_length ));
		Point point4 = mathematics.point2x2MatrixMult(mathematics.rotationMatrix(angle_in_radians), new Point( (int) body_arrow_x_length, (int) body_arrow_half_y_length ));
		Point point5 = mathematics.point2x2MatrixMult(mathematics.rotationMatrix(angle_in_radians), new Point( 0, (int) body_arrow_half_y_length ));
		Point point6 = mathematics.point2x2MatrixMult(mathematics.rotationMatrix(angle_in_radians), new Point( 0, (int) -body_arrow_half_y_length ));
		Point point7 = mathematics.point2x2MatrixMult(mathematics.rotationMatrix(angle_in_radians), new Point( (int) body_arrow_x_length, (int) -body_arrow_half_y_length ));
		
		point1 = new Point(point1.x+x_translation, -point1.y+y_translation);
		point2 = new Point(point2.x+x_translation, -point2.y+y_translation);
		point3 = new Point(point3.x+x_translation, -point3.y+y_translation);
		point4 = new Point(point4.x+x_translation, -point4.y+y_translation);
		point5 = new Point(point5.x+x_translation, -point5.y+y_translation);
		point6 = new Point(point6.x+x_translation, -point6.y+y_translation);
		point7 = new Point(point7.x+x_translation, -point7.y+y_translation);
						
		int[] xPoints = {point1.x, point2.x, point3.x, point4.x, point5.x, point6.x, point7.x};
		int[] yPoints = {point1.y, point2.y, point3.y, point4.y, point5.y, point6.y, point7.y};
		Polygon arrow_polygon = new Polygon(xPoints, yPoints, 7);
				
		// Circle radius calculations
		int circle_radius = (int) pixel_magnitude;
				
		arrowAndCircleRenderData data = new arrowAndCircleRenderData(x_translation, y_translation, point2, arrow_polygon, circle_radius);
		
		return data;
	}
	
	public void drawArrow(Graphics2D g2, arrowAndCircleRenderData renderData, boolean showRotationCircle) {
		
		int circleRadius = renderData.getCircleRadius();
		int x_pos = renderData.getX();
		int y_pos = renderData.getY();
//		Point body_arrow_connection = renderData.getBodyArrowConnection();
		
		if(showRotationCircle){
			g2.drawOval(x_pos-circleRadius, y_pos-circleRadius, circleRadius*2, circleRadius*2);
		}
		
		g2.fill(renderData.getArrowHeadPolygon());
	//	g2.setStroke(new BasicStroke(renderData.getArrowBodyStroke()));
	//	g2.drawLine(x_pos, y_pos, body_arrow_connection.x, body_arrow_connection.y);
	//	g2.setStroke(new BasicStroke(1)); // Reset the stroke to default
	}
	
	public void render(){
		rendering_panel.repaint();
	}
	
}
