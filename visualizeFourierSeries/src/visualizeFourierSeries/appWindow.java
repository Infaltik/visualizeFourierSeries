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
import java.awt.geom.Point2D;
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
	ArrayList<Point> fourier_series_drawn_image_array = new ArrayList<Point>();
	ArrayList<arrowAndCircleRenderData> arrow_circle_render_data_array = new ArrayList<arrowAndCircleRenderData>();
	int current_app_status = 1;
	
	// Different application status values
	public static final int DRAWING_IMAGE = 1;
	public static final int RENDERING_FOURIER_ANIMATION = 2;
	public static double testAngle = 0;
	int testCount = 0;
	boolean arrow_calculations_done = false;

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
						this.setBackground(Color.black);
						
						g2.setColor(Color.red);
						for(int i = 0; i < arrow_circle_render_data_array.size(); i++){
							drawArrow(g2, arrow_circle_render_data_array.get(i), true);
						}
						
						// could be put in a function since similar code is used in the other switch-case
						for(int i = 0; i < drawn_image_array.size()-1; i++) {
							
							int current_x = (int) drawn_image_array.get(i).getX();
							int current_y = (int) drawn_image_array.get(i).getY();
							int next_x = (int) drawn_image_array.get(i+1).getX();
							int next_y = (int) drawn_image_array.get(i+1).getY();
							
							g2.setStroke(new BasicStroke(5));
							Color color1 = new Color(0, (float) 1, 0, (float) 0.4);
							g2.setColor(color1);
							g2.drawLine(current_x, current_y, next_x, next_y);
						}
						
						if(fourier_series_drawn_image_array.size() > 1){
							for(int i = 0; i < fourier_series_drawn_image_array.size()-1; i++) {
								
								int current_x = (int) fourier_series_drawn_image_array.get(i).getX();
								int current_y = (int) fourier_series_drawn_image_array.get(i).getY();
								int next_x = (int) fourier_series_drawn_image_array.get(i+1).getX();
								int next_y = (int) fourier_series_drawn_image_array.get(i+1).getY();
								
								g2.setStroke(new BasicStroke(5));
								g2.setColor(Color.red);
								g2.drawLine(current_x, current_y, next_x, next_y);
							}
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
				
				Color color = new Color(0, 0, 0, (float) 0.5); 
				g2.setColor(color);
				g2.setStroke(new BasicStroke(1));
				g2.drawLine(mathematics.originPixelX, mathematics.originPixelY-5, mathematics.originPixelX, mathematics.originPixelY+5);
				g2.drawLine(mathematics.originPixelX-5, mathematics.originPixelY, mathematics.originPixelX+5, mathematics.originPixelY);
				
				
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
					for(int i = 1; i <= 1; i++) {
						mathematics.addMoreSamplesToFunction();
						System.out.println("done" + i);
					}
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
	
	public void arrowPreRenderCalculations(double angle_in_radians){

		arrow_circle_render_data_array.clear();
		ArrayList<arrowAndCircleRenderData> current_arrows_array = new ArrayList<arrowAndCircleRenderData>();
		for(int i = 0; i < mathematics.nbr_of_fourier_terms; i++){
			arrowAndCircleRenderData current_arrow = calculateArrow(mathematics.fourier_series_terms[i], mathematics.shifting_indices_array[i]);
			arrow_circle_render_data_array.add(current_arrow);
		}
		
	/*	if(testCount == 0){
			arrowAndCircleRenderData arrow = calculateArrow(mathematics.pixelToComplexNumber(drawn_image_array.get(testCount)), 0);
			arrow_circle_render_data_array.add(arrow);
			arrowAndCircleRenderData arrow2 = calculateArrow(new complexNumber(0.8,0), 1);
			arrow_circle_render_data_array.add(arrow2);
		}
		if(testCount < drawn_image_array.size()-1) {
			testCount++;
		}
		*/
		//ArrayList<arrowAndCircleRenderData> test = new ArrayList<arrowAndCircleRenderData>();
		//test.add(arrow);
		//arrow_circle_render_data_array = current_arrows_array;
		arrow_calculations_done = true;
	}
	
	public arrowAndCircleRenderData calculateArrow(complexNumber complex_value, int shift_index){
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
		double head_arrow_x_length = Math.max(pixel_magnitude/3, 0);
		double head_arrow_half_y_length = 0.8*head_arrow_x_length; // Could calculate these once and save them as to not calculate them over and over???
		double body_arrow_x_length = Math.max((2*pixel_magnitude)/3, 0);
		double body_arrow_half_y_length = Math.max(0.2*head_arrow_half_y_length, 0);
		
		// Arrowhead calculations
		Point2D.Double point1 = mathematics.pointDouble2x2MatrixMult(mathematics.rotationMatrix(angle_in_radians), new Point2D.Double( body_arrow_x_length, -head_arrow_half_y_length ));
		Point2D.Double point2 = mathematics.pointDouble2x2MatrixMult(mathematics.rotationMatrix(angle_in_radians), new Point2D.Double( (body_arrow_x_length+head_arrow_x_length), 0 ));
		Point2D.Double point3 = mathematics.pointDouble2x2MatrixMult(mathematics.rotationMatrix(angle_in_radians), new Point2D.Double( body_arrow_x_length, head_arrow_half_y_length ));
		Point2D.Double point4 = mathematics.pointDouble2x2MatrixMult(mathematics.rotationMatrix(angle_in_radians), new Point2D.Double( body_arrow_x_length, body_arrow_half_y_length ));
		Point2D.Double point5 = mathematics.pointDouble2x2MatrixMult(mathematics.rotationMatrix(angle_in_radians), new Point2D.Double( 0, body_arrow_half_y_length ));
		Point2D.Double point6 = mathematics.pointDouble2x2MatrixMult(mathematics.rotationMatrix(angle_in_radians), new Point2D.Double( 0, -body_arrow_half_y_length ));
		Point2D.Double point7 = mathematics.pointDouble2x2MatrixMult(mathematics.rotationMatrix(angle_in_radians), new Point2D.Double( body_arrow_x_length, -body_arrow_half_y_length ));
		
		point1 = new Point2D.Double(point1.x+x_translation, -point1.y+y_translation);
		point2 = new Point2D.Double(point2.x+x_translation, -point2.y+y_translation);
		point3 = new Point2D.Double(point3.x+x_translation, -point3.y+y_translation);
		point4 = new Point2D.Double(point4.x+x_translation, -point4.y+y_translation);
		point5 = new Point2D.Double(point5.x+x_translation, -point5.y+y_translation);
		point6 = new Point2D.Double(point6.x+x_translation, -point6.y+y_translation);
		point7 = new Point2D.Double(point7.x+x_translation, -point7.y+y_translation);
						
		int[] xPoints = {(int) Math.round(point1.x), (int) Math.round(point2.x), (int) Math.round(point3.x), 
				(int) Math.round(point4.x), (int) Math.round(point5.x), (int) Math.round(point6.x), (int) Math.round(point7.x)};
		int[] yPoints = {(int) Math.round(point1.y), (int) Math.round(point2.y), (int) Math.round(point3.y), 
				(int) Math.round(point4.y), (int) Math.round(point5.y), (int) Math.round(point6.y), (int) Math.round(point7.y)};
		Polygon arrow_polygon = new Polygon(xPoints, yPoints, 7);
				
		// Circle radius calculations
		int circle_radius = (int) pixel_magnitude;
		
		Point arrow_end_point = new Point( (int) Math.round(point2.x), (int) Math.round(point2.y));
		arrowAndCircleRenderData data = new arrowAndCircleRenderData(x_translation, y_translation, arrow_end_point, arrow_polygon, circle_radius);
		
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
