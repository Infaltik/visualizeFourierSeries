package visualizeFourierSeries;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.util.ArrayList;

import javax.swing.JFrame;
import javax.swing.JPanel;

public class appWindow extends JFrame{
	
	int x = 0, y = 0;
	int prev_x = 0, prev_y = 0;
	ArrayList<complexNumber> drawn_image_array = new ArrayList<complexNumber>();

	public appWindow(String window_title) {
		
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.setSize(1400, 1000); // 900 750
		this.setTitle(window_title);
		this.setResizable(false);
		this.setLocationRelativeTo(null);
		
		JPanel rendering_panel = new JPanel() {
			protected void paintComponent(Graphics g) {
				Graphics2D g2 = (Graphics2D) g;
				super.paintComponent(g2);
				g2.drawString("Mouse position: " + x + ", " + y, 40, 40);
				//g.drawLine(prev_x, prev_y, x, y);
				for(int i = 0; i < drawn_image_array.size()-1; i++) {
					
					int current_x = drawn_image_array.get(i).getRealPart();
					int current_y = drawn_image_array.get(i).getImagPart();
					int next_x = drawn_image_array.get(i+1).getRealPart();
					int next_y = drawn_image_array.get(i+1).getImagPart();
					
					g2.setStroke(new BasicStroke(5));
					g2.drawLine(current_x, current_y, next_x, next_y);
				}
			}
		};
		
		
		rendering_panel.addMouseMotionListener(new MouseMotionListener() {

			public void mouseDragged(MouseEvent e) {

				prev_x = x;
				prev_y = y;
				
				x = e.getX();
				y = e.getY();
				drawn_image_array.add(new complexNumber(x, y));
				
				rendering_panel.repaint();
			}

			public void mouseMoved(MouseEvent e) {
				prev_x = x;
				prev_y = y;
				
				x = e.getX();
				y = e.getY();
				
				rendering_panel.repaint();
			}
		});
		
		rendering_panel.setBackground(Color.WHITE);
		this.add(rendering_panel);
		
		
		this.setVisible(true);
	}
	
}
