package visualizeFourierSeries;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Polygon;
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
						g2.drawOval(x+20, y+20, 40, 40);
						
						
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
		
		// Arrowhead calculations
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
	}
	
	public void drawArrow(Graphics2D g2, arrowAndCircleRenderData renderData, boolean showRotationCircle) {
		
		int circleRadius = renderData.getCircleRadius();
		int x_pos = renderData.getX();
		int y_pos = renderData.getY();
		Point body_arrow_connection = renderData.getBodyArrowConnection();
		
		if(showRotationCircle){
			g2.drawOval(x_pos-circleRadius, y_pos-circleRadius, circleRadius*2, circleRadius*2);
		}
		
		g2.fill(renderData.getArrowHeadPolygon());
		g2.setStroke(new BasicStroke(renderData.getArrowBodyStroke()));
		g2.drawLine(x_pos, y_pos, body_arrow_connection.x, body_arrow_connection.y);
		g2.setStroke(new BasicStroke(1)); // Reset the stroke to default
	}
	
	public void render(){
		rendering_panel.repaint();
	}
	
}
