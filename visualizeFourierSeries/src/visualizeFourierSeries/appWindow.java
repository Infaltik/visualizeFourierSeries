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
						
						complexNumber complex_value = mathematics.pixel_to_complex_value(new Point(x, y));
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
						
						drawArrow(g2, testAngle, 0.5, 650, 500);
						
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
					Thread renderThread = new Thread(new renderLoop());
					renderThread.start();
				}
			}
			
		});
		
		rendering_panel.setBackground(Color.WHITE);
		this.add(rendering_panel);
		
		
		this.setVisible(true);
	}
	
	public void drawArrow(Graphics2D g2, double angle_in_radians, double scale, int x, int y) {
		Point point1 = mathematics.point2x2MatrixMult(mathematics.similarityTransformationMatrix(angle_in_radians, scale), new Point(25, -75));
		Point point2 = mathematics.point2x2MatrixMult(mathematics.similarityTransformationMatrix(angle_in_radians, scale), new Point(75, -75));
		Point point3 = mathematics.point2x2MatrixMult(mathematics.similarityTransformationMatrix(angle_in_radians, scale), new Point(75, -25));
		point1 = new Point(point1.x+x, point1.y+y);
		point2 = new Point(point2.x+x, point2.y+y);
		point3 = new Point(point3.x+x, point3.y+y);
		
		Point point4 = new Point(x, y);
		Point point5 = mathematics.point2x2MatrixMult(mathematics.similarityTransformationMatrix(angle_in_radians, scale), new Point(50, -50));
		point5 = new Point(point5.x+x, point5.y+y);
		
		int circleRadius = (int) (Math.sqrt(Math.pow(scale*50, 2) + Math.pow(scale*50, 2)) + (scale*50)/(Math.cos(Math.PI/4)*2));
		g2.drawOval(x-circleRadius, y-circleRadius, circleRadius*2, circleRadius*2);
		
		
		int[] xPoints = {point1.x, point2.x, point3.x};
		int[] yPoints = {point1.y, point2.y, point3.y};
		Polygon arrowHead = new Polygon(xPoints, yPoints, 3);
		g2.fill(arrowHead);
		g2.setStroke(new BasicStroke((int) (scale*12)));
		g2.drawLine(point4.x, point4.y, point5.x, point5.y);
		g2.setStroke(new BasicStroke(1));
	}
	
	public void render(){
		rendering_panel.repaint();
	}
	
}
