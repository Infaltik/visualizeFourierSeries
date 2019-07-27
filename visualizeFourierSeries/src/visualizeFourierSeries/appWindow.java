package visualizeFourierSeries;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.Point2D;
import java.io.File;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;


public class appWindow extends JFrame{
	
	private int drawing_brush_size = 2;
	public double animation_drawing_speed = 2.0;
	boolean show_target_function_in_animation = false;
	boolean show_arrow_circles_in_animation = true;
	boolean show_arrows_in_animation = true;
	JPanel rendering_panel;
	public static int rendering_panel_width = 1400;
	public static int rendering_panel_height = 1000; // atm the frame and rendering panel have the same size, need to add other containers
									   // to have other size
	public static int settings_panel_width = 400;
	public static int settings_panel_height = rendering_panel_height;
	public static int x = 0, y = 0;
	private int prev_x = 0, prev_y = 0;
	public static ArrayList<Point> drawn_image_array = new ArrayList<Point>();
	ArrayList<Point> fourier_series_drawn_image_array = new ArrayList<Point>();
	ArrayList<arrowAndCircleRenderData> arrow_circle_render_data_array = new ArrayList<arrowAndCircleRenderData>();
	public static int initial_drawn_image_array_size;
	
	JButton draw_image_button;
	JButton trace_input_image_button;
	JButton elephant_image_demo_button;
	
	// Different application status values
	public static final int SELECTION_SCREEN = 1;
	public static final int DRAWING_IMAGE = 2;
	public static final int RENDERING_FOURIER_ANIMATION = 3;
	public static final int SELECTING_TRACING_INPUT_IMAGE_START = 4;
	public static final int TRACING_INPUT_IMAGE = 5;
	
	private int current_app_status = SELECTION_SCREEN;
	
	boolean arrow_calculations_done = false;

	public appWindow(String window_title) {
		
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.setSize(rendering_panel_width + settings_panel_width,
				rendering_panel_height); // 900 750
		this.setTitle(window_title);
		this.setResizable(false);
		this.setLocationRelativeTo(null);
		
		rendering_panel = new JPanel() {
			protected void paintComponent(Graphics g) {
				Graphics2D g2 = (Graphics2D) g;
				super.paintComponent(g2);
				
				g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
				
				switch(current_app_status){
					case SELECTION_SCREEN:
						this.setBackground(Color.black);
						break;
					
					case DRAWING_IMAGE:
						this.setBackground(Color.white);
						
						g2.drawString("Mouse position: " + x + ", " + y, 40, 40);
						
						complexNumber complex_value = mathematics.pixelToComplexNumber(new Point(x, y));
						DecimalFormat numberFormat = new DecimalFormat("0.0000");
						if(complex_value.getImagPart() < 0) {
							g2.drawString("Complex value: " + numberFormat.format(complex_value.getRealPart()) + "-" + numberFormat.format(Math.abs(complex_value.getImagPart())) + "i", 40, 80);
						}
						else {
							g2.drawString("Complex value: " + numberFormat.format(complex_value.getRealPart()) + "+" + numberFormat.format(Math.abs(complex_value.getImagPart())) + "i", 40, 80);
						}
						
						drawImageArray(g2, drawn_image_array, Color.black);
						drawOriginMarker(g2);
						break;
						
					case RENDERING_FOURIER_ANIMATION:
						this.setBackground(Color.black);
						
						if(show_target_function_in_animation) {
							// could be put in a function since similar code is used in the other switch-case
							Color color1 = new Color(0, (float) 1, 0, (float) 0.4);
							drawImageArray(g2, drawn_image_array, color1);
						}
						
						
						g2.setColor(Color.white);
						for(int i = 0; i < arrow_circle_render_data_array.size(); i++){
							drawArrow(g2, arrow_circle_render_data_array.get(i), show_arrow_circles_in_animation);
						}
						arrow_calculations_done = false;
						
						
												
						if(fourier_series_drawn_image_array.size() > 1){
							drawImageArray(g2, fourier_series_drawn_image_array, Color.red);
						}
						
						drawOriginMarker(g2);
						break;
					case TRACING_INPUT_IMAGE:
						imageInputFunctions.drawImageTracingPreview(g2);
						imageInputFunctions.drawTracingMarker(g2);
						break;
					case SELECTING_TRACING_INPUT_IMAGE_START:
						imageInputFunctions.drawImageTracingPreview(g2);
						break;
					default:
						this.setBackground(Color.BLACK);
						break;
				}
				
				
			}
		};
		rendering_panel.setPreferredSize(new Dimension(rendering_panel_width, rendering_panel_height)); // 900 750
		
		
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
				
				if(current_app_status == DRAWING_IMAGE || current_app_status == TRACING_INPUT_IMAGE){
					rendering_panel.repaint();
				}
				
			}
		});
		
		rendering_panel.addMouseListener(new MouseListener(){

			public void mouseClicked(MouseEvent e) {
				if(current_app_status == SELECTING_TRACING_INPUT_IMAGE_START) {
					
					// Find the actual pixel value for the zoomed in selected pixel
					int x_residual = e.getX()%imageInputFunctions.zoomInFactor;
					int y_residual = e.getY()%imageInputFunctions.zoomInFactor;
					
					imageInputFunctions.traced_image_array.add(new Point((e.getX()-x_residual)/imageInputFunctions.zoomInFactor + imageInputFunctions.zoom_x_pos
							, (e.getY()-y_residual)/imageInputFunctions.zoomInFactor + imageInputFunctions.zoom_y_pos));
					current_app_status = TRACING_INPUT_IMAGE;
					render();
				}
			}
			public void mouseEntered(MouseEvent arg0) {}
			public void mouseExited(MouseEvent arg0) {}
			public void mousePressed(MouseEvent arg0) {}

			public void mouseReleased(MouseEvent arg0) {
				
				if(current_app_status == DRAWING_IMAGE){
					System.out.println("Released mouse button");
					current_app_status = RENDERING_FOURIER_ANIMATION;
					
					calculateAndstartFourierAnimation();
				}
			}
			
		});
		
		this.addKeyListener(new KeyListener() {

			public void keyTyped(KeyEvent e) {}

			public void keyPressed(KeyEvent e) {
				System.out.println("Key pressed");
				
				switch(current_app_status){
				case TRACING_INPUT_IMAGE:
					Point previous_point = imageInputFunctions.traced_image_array.get(imageInputFunctions.traced_image_array.size()-1);
					Point current_point = null;
					
					switch(e.getKeyCode()) {
					case 81: // Q key
						current_point = new Point(previous_point.x-1, previous_point.y-1);
						imageInputFunctions.traced_image_array.add(current_point);
						render();
						break;
					case 87: // W key
						current_point = new Point(previous_point.x, previous_point.y-1);
						imageInputFunctions.traced_image_array.add(current_point);
						render();
						break;
					case 69: // E key
						current_point = new Point(previous_point.x+1, previous_point.y-1);
						imageInputFunctions.traced_image_array.add(current_point);
						render();
						break;
					case 68: // D key
						current_point = new Point(previous_point.x+1, previous_point.y);
						imageInputFunctions.traced_image_array.add(current_point);
						render();
						break;
					case 67: // C key
						current_point = new Point(previous_point.x+1, previous_point.y+1);
						imageInputFunctions.traced_image_array.add(current_point);
						render();
						break;
					case 88: // X key
						current_point = new Point(previous_point.x, previous_point.y+1);
						imageInputFunctions.traced_image_array.add(current_point);
						render();
						break;
					case 90: // Z key
						current_point = new Point(previous_point.x-1, previous_point.y+1);
						imageInputFunctions.traced_image_array.add(current_point);
						render();
						break;
					case 65: // A key
						current_point = new Point(previous_point.x-1, previous_point.y);
						imageInputFunctions.traced_image_array.add(current_point);
						render();
						break;
					case 10: // Enter
						imageInputFunctions.printTracedArrayInSavableFormat();
						break;
					case 109: // right "-"-key
						imageInputFunctions.zoomOut();
						render();
						break;
					case 107: // right "+"-key
						imageInputFunctions.zoomIn();
						render();
						break;
					case 37: // left arrow
						imageInputFunctions.zoom_x_pos = Math.max(0, imageInputFunctions.zoom_x_pos-1);
						render();
						break;
					case 38: // up arrow
						imageInputFunctions.zoom_y_pos = Math.max(0, imageInputFunctions.zoom_y_pos-1);
						render();
						break;
					case 39: // right arrow
						int max_x_pos = imageInputFunctions.output_image.getWidth()-imageInputFunctions.preview_rectangle_width;
						imageInputFunctions.zoom_x_pos = Math.min(max_x_pos, imageInputFunctions.zoom_x_pos+1);
						render();
						break;
					case 40: // down arrow
						int max_y_pos = imageInputFunctions.output_image.getHeight()-imageInputFunctions.preview_rectangle_height;
						imageInputFunctions.zoom_y_pos = Math.min(max_y_pos, imageInputFunctions.zoom_y_pos+1);
						render();
						break;
					case 27: // esc
						if(imageInputFunctions.traced_image_array.size() > 1) {
							imageInputFunctions.traced_image_array.remove(imageInputFunctions.traced_image_array.size()-1);
						}
						render();
						break;
					}
					break;
				case SELECTING_TRACING_INPUT_IMAGE_START:
					switch(e.getKeyCode()) {
					case 109:
						imageInputFunctions.zoomOut();
						render();
						break;
					case 107:
						imageInputFunctions.zoomIn();
						render();
						break;
					case 37:
						imageInputFunctions.zoom_x_pos = Math.max(0, imageInputFunctions.zoom_x_pos-1);
						render();
						break;
					case 38:
						imageInputFunctions.zoom_y_pos = Math.max(0, imageInputFunctions.zoom_y_pos-1);
						render();
						break;
					case 39:
						int max_x_pos = imageInputFunctions.output_image.getWidth()-imageInputFunctions.preview_rectangle_width;
						imageInputFunctions.zoom_x_pos = Math.min(max_x_pos, imageInputFunctions.zoom_x_pos+1);
						render();
						break;
					case 40:
						int max_y_pos = imageInputFunctions.output_image.getHeight()-imageInputFunctions.preview_rectangle_height;
						imageInputFunctions.zoom_y_pos = Math.min(max_y_pos, imageInputFunctions.zoom_y_pos+1);
						render();
						break;
					}
					break;
				}
				
			}
			public void keyReleased(KeyEvent e) {}
			
		});
		
		draw_image_button = new JButton("Draw image");
		draw_image_button.setFocusable(false);
		draw_image_button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				System.out.println("Draw image button pressed");
				showSelectionButtons(false);
				current_app_status = DRAWING_IMAGE;
				render();
			}
		});
		
		trace_input_image_button = new JButton("Trace input image");
		trace_input_image_button.setFocusable(false);
		trace_input_image_button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JFileChooser fc = new JFileChooser();
				int returned_value = fc.showOpenDialog(null);
				
				if(returned_value == JFileChooser.APPROVE_OPTION) {
					File file = fc.getSelectedFile();
					imageInputFunctions.readImage(file);
					
					showSelectionButtons(false);
					current_app_status = SELECTING_TRACING_INPUT_IMAGE_START;
					render();
				}
				else if(returned_value == JFileChooser.CANCEL_OPTION) {
					System.out.println("File selection canceled");
				}
				else if(returned_value == JFileChooser.ERROR_OPTION) {
					System.err.println("An error occured while selecting a file");
				}
				
				System.out.println("Trace input image button pressed");
			}
		});
		
		elephant_image_demo_button = new JButton("Show demo");
		elephant_image_demo_button.setFocusable(false);
		elephant_image_demo_button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				System.out.println("Elephant image demo button pressed");
				showSelectionButtons(false);
				imageInputFunctions.loadElephantImage();
				calculateAndstartFourierAnimation();
				
				current_app_status = RENDERING_FOURIER_ANIMATION;
				render();
			}
		});
		
		
		if(current_app_status == SELECTION_SCREEN){
			showSelectionButtons(true);
		}
		
		this.add(rendering_panel, BorderLayout.WEST);
		
		JPanel settings_panel = new JPanel();
		settings_panel.setBackground(Color.white);
		settings_panel.setPreferredSize(new Dimension(settings_panel_width,
				settings_panel_height));
		
		JCheckBox show_arrow_circles_check_box = new JCheckBox("Show circles", show_arrow_circles_in_animation);
		show_arrow_circles_check_box.addItemListener(new ItemListener(){
			public void itemStateChanged(ItemEvent e) {
				if(e.getStateChange() == 1){
					show_arrow_circles_in_animation = true;
					render();
				}
				else{
					show_arrow_circles_in_animation = false;
					render();
				}
			}
		});
		show_arrow_circles_check_box.setFocusable(false);
		
		JCheckBox show_arrows_check_box = new JCheckBox("Show arrows", true);
		show_arrows_check_box.addItemListener(new ItemListener(){
			public void itemStateChanged(ItemEvent e) {
				if(e.getStateChange() == 1){
					show_arrows_in_animation = true;
					render();
				}
				else{
					show_arrows_in_animation = false;
					render();
				}
			}
		});
		show_arrows_check_box.setFocusable(false);
		
		JCheckBox show_target_image_check_box = new JCheckBox("Show target image", show_target_function_in_animation);
		show_target_image_check_box.addItemListener(new ItemListener(){
			public void itemStateChanged(ItemEvent e) {
				if(e.getStateChange() == 1){
					show_target_function_in_animation = true;
					render();
				}
				else{
					show_target_function_in_animation = false;
					render();
				}
			}
		});
		show_target_image_check_box.setFocusable(false);
		
		JPanel animation_speed_slider_panel = new JPanel(new BorderLayout());
		animation_speed_slider_panel.add(new JLabel("Function evaluation resolution (Sort of the animation speed)"), BorderLayout.NORTH);
		JSlider animation_speed_slider = new JSlider(JSlider.HORIZONTAL, 0, 400, (int) (animation_drawing_speed*100) );
		animation_speed_slider.addChangeListener(new ChangeListener(){
			public void stateChanged(ChangeEvent e) {
				animation_drawing_speed = animation_speed_slider.getValue()/100.0;
			}
		});
		animation_speed_slider_panel.add(animation_speed_slider);
		animation_speed_slider.setFocusable(false);
		animation_speed_slider.setMajorTickSpacing(100);
		animation_speed_slider.setMinorTickSpacing(1);
		animation_speed_slider.setPaintTicks(true);
		animation_speed_slider.setPaintLabels(true);
		
		JPanel FPS_slider_panel = new JPanel(new BorderLayout());
		FPS_slider_panel.add(new JLabel("Frames per second"), BorderLayout.NORTH);
		JSlider FPS_slider = new JSlider(JSlider.HORIZONTAL, 0, 200, renderLoop.TARGET_FPS);
		FPS_slider.addChangeListener(new ChangeListener(){
			public void stateChanged(ChangeEvent e) {
				renderLoop.TARGET_FPS = FPS_slider.getValue();
				if(renderLoop.TARGET_FPS != 0) {
					renderLoop.RENDER_WAIT_TIME = 1000000000/renderLoop.TARGET_FPS;
				}
				else {
					renderLoop.RENDER_WAIT_TIME = Long.MAX_VALUE;
				}
			}
		});
		FPS_slider_panel.add(FPS_slider);
		FPS_slider.setFocusable(false);
		FPS_slider.setMajorTickSpacing(50);
		FPS_slider.setMinorTickSpacing(1);
		FPS_slider.setPaintTicks(true);
		FPS_slider.setPaintLabels(true);
		
		
		settings_panel.add(show_arrow_circles_check_box);
		settings_panel.add(show_arrows_check_box);
		settings_panel.add(show_target_image_check_box);
		settings_panel.add(animation_speed_slider_panel);
		settings_panel.add(FPS_slider_panel);
		
		this.add(settings_panel, BorderLayout.EAST);
		
		this.setVisible(true);
	}
	
	public void arrowPreRenderCalculations(){
		arrow_circle_render_data_array.clear();
		for(int i = 0; i < mathematics.nbr_of_fourier_terms; i++){
			arrowAndCircleRenderData current_arrow = calculateArrow(mathematics.fourier_series_terms[i], mathematics.shifting_indices_array[i]);
			arrow_circle_render_data_array.add(current_arrow);
		}
		
		arrow_calculations_done = true;
	}
	
	public arrowAndCircleRenderData calculateArrow(complexNumber complex_value, int shift_index){
		
		
		int vector_sum_index = mathematics.shiftIndexToVectorSumIndex(shift_index);
		Point2D.Double end_point = mathematics.calculateEndPointDouble(vector_sum_index);
		Point2D.Double previous_end_point = mathematics.calculateEndPointDouble(vector_sum_index-1);
		
		// Calculate the magnitude of the arrow in unit of pixels
		double pixel_magnitude = Math.sqrt( (end_point.x - previous_end_point.x)*
				(end_point.x - previous_end_point.x) + (end_point.y - previous_end_point.y)*
				(end_point.y - previous_end_point.y) );	
		
		// If the arrow is shorter than a pixel, don't draw it. Return empty data
		if(pixel_magnitude < 1){
			return null;
		}
		
		// The offset the arrow should be translated
		double x_translation = previous_end_point.x;
		double y_translation = previous_end_point.y;
		
		// Calculate the angle of the arrow
		double angle_in_radians = complex_value.getArgument();
						
		// Arrow proportions
		double head_arrow_x_length = Math.min(pixel_magnitude/4, 30);
		double head_arrow_half_y_length = 0.55*head_arrow_x_length; // Could calculate these once and save them as to not calculate them over and over???
		double body_arrow_x_length = pixel_magnitude - head_arrow_x_length;
		double body_arrow_half_y_length = 0.1*head_arrow_half_y_length;
		
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
		arrowAndCircleRenderData data = new arrowAndCircleRenderData((int) Math.round(x_translation), 
				(int) Math.round(y_translation), arrow_end_point, arrow_polygon, circle_radius);
		
		return data;
	}
	
	public arrowAndCircleRenderData calculateArrow2(complexNumber complex_value, int shift_index){
		// TEMPORARY TEST
		Point exact_point = mathematics.calculateEndPoint(
				mathematics.shiftIndexToVectorSumIndex(shift_index));
		
		
		// Calculate the magnitude of the arrow in unit of pixels
		double complex_magnitude = complex_value.getMagnitude();
		double pixel_magnitude = mathematics.complexMagnitudeToPixelMagnitude(complex_magnitude);
						
		// If the arrow is shorter than a pixel, don't draw it
		if(pixel_magnitude < 1){
			//return null;
		}
		
		// The offset the arrow should be translated
		int x_translation = mathematics.originPixelX;
		int y_translation = mathematics.originPixelY;
		if(shift_index != 0){
			int vector_sum_index = mathematics.shiftIndexToVectorSumIndex(shift_index);
			Point previous_end_point = arrow_circle_render_data_array.get(vector_sum_index-1).getArrowEndPoint();
			
			if(Math.abs(previous_end_point.x - exact_point.x) > 1 || Math.abs(previous_end_point.y - exact_point.y) > 1 ){
				//System.out.println(Math.abs(previous_end_point.x - exact_point.x) + ", " + 
				//		Math.abs(previous_end_point.y - exact_point.y));
			}
			
			x_translation = previous_end_point.x;
			y_translation = previous_end_point.y;
		}
		
		// Calculate the angle of the arrow
		double angle_in_radians = complex_value.getArgument();
				
		// Arrow proportions
		double head_arrow_x_length = pixel_magnitude/3;
		double head_arrow_half_y_length = 0.8*head_arrow_x_length; // Could calculate these once and save them as to not calculate them over and over???
		double body_arrow_x_length = (2*pixel_magnitude)/3;
		double body_arrow_half_y_length = 0.15*head_arrow_half_y_length;
		
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
		
		// TEMPORARY TEST
		if(mathematics.shiftIndexToVectorSumIndex(shift_index) == 20){
			System.out.println(Math.abs(point2.x - exact_point.x) + ", " + 
					Math.abs(point2.y - exact_point.y) + ", " + mathematics.shiftIndexToVectorSumIndex(shift_index));
		}
		
		Point arrow_end_point = new Point( (int) Math.round(point2.x), (int) Math.round(point2.y));
		arrowAndCircleRenderData data = new arrowAndCircleRenderData(x_translation, y_translation, arrow_end_point, arrow_polygon, circle_radius);
		
		return data;
	}
	
	public void drawArrow(Graphics2D g2, arrowAndCircleRenderData renderData, boolean showRotationCircle) {
		
		// If the arrow data is not available (because the arrow has length shorter than a pixel) then skip drawing
		// that arrow
		if(renderData == null){
			return;
		}
		
		if(showRotationCircle){
			int circleRadius = renderData.getCircleRadius();
			int x_pos = renderData.getX();
			int y_pos = renderData.getY();
			
			g2.setStroke(new BasicStroke(1));
			Color circle_color = new Color(1, 1, 1, (float) 0.4);
			g2.setColor(circle_color);
			g2.drawOval(x_pos-circleRadius, y_pos-circleRadius, circleRadius*2, circleRadius*2);
		}
		
		if(show_arrows_in_animation) {
			g2.setColor(Color.white);
			g2.fill(renderData.getArrowHeadPolygon());
		//	g2.setStroke(new BasicStroke(1)); // Reset the stroke to default
		}
	}
	
	public void drawImageArray(Graphics2D g2, ArrayList<Point> image_array, Color color){
		for(int i = 0; i < image_array.size()-1; i++) {
			
			int current_x = (int) image_array.get(i).getX();
			int current_y = (int) image_array.get(i).getY();
			int next_x = (int) image_array.get(i+1).getX();
			int next_y = (int) image_array.get(i+1).getY();
			
			g2.setStroke(new BasicStroke(drawing_brush_size));
			g2.setColor(color);
			g2.drawLine(current_x, current_y, next_x, next_y);
			
			// Reset the stroke to default
			g2.setStroke(new BasicStroke(1));
		}
	}
	
	public void showSelectionButtons(boolean show_buttons){
		if(show_buttons){
			rendering_panel.add(draw_image_button);
			rendering_panel.add(trace_input_image_button);
			rendering_panel.add(elephant_image_demo_button);
		}
		else{
			rendering_panel.remove(draw_image_button);
			rendering_panel.remove(trace_input_image_button);
			rendering_panel.remove(elephant_image_demo_button);
		}
	}
	
	public void drawOriginMarker(Graphics2D g2) {
		Color color = new Color(0, 0, 0, (float) 0.5);
		g2.setColor(color);
		g2.setStroke(new BasicStroke(1));
		g2.drawLine(mathematics.originPixelX, mathematics.originPixelY-5, mathematics.originPixelX, mathematics.originPixelY+5);
		g2.drawLine(mathematics.originPixelX-5, mathematics.originPixelY, mathematics.originPixelX+5, mathematics.originPixelY);
	}
	
	public void calculateAndstartFourierAnimation() {
		// Flip array so that the fourier series animation draws in the same
		// direction as the drawer
		Collections.reverse(drawn_image_array);
		
		mathematics.convertToComplexAndStoreFunction(drawn_image_array);
		mathematics.iterateAddingMoreSamplesToFunction(5); // Make this input dependant on number of fourier terms ???
		mathematics.calculateFourierSeriesCoefficients();
		
		Thread render_thread = new Thread(new renderLoop());
		render_thread.start();
	}
	
	public void render(){
		rendering_panel.repaint();
	}
	
}
