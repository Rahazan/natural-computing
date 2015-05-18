package pso;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;

import javax.swing.JFrame;
import javax.swing.JPanel;

import windfarmapi.WindScenario;

public class GUI extends JPanel{
	
	private int width;
	private int height;
	private static double factor = 15; 
	private JFrame mainframe;
	private double[][] layout;
	private int radius;
	private int radiusS = 5;
	private double[][] obstacles;
	private static int borderWidth = 10;
	
	
	public GUI(WindScenario scenario)
	{
		super();
		this.width = (int) (scenario.width/factor) + borderWidth; 
		this.height = (int) (scenario.height/factor) + borderWidth;
		this.obstacles = scenario.obstacles;
		radius = (int)((scenario.R*8)/factor);
		super.setPreferredSize(new Dimension(width,height));
		super.setBackground(Color.WHITE);
		mainframe = new JFrame();
		mainframe.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		mainframe.getContentPane().add(this);
		mainframe.pack();
		mainframe.setVisible(true);
		
	}
	
	public void update(double[][] layout){
		this.layout = layout;
		repaint();
	}
	
	
	@Override
	public void paint(Graphics g){
		Graphics2D g2 = (Graphics2D)g;
		super.paintComponent(g2);
		//draw border
		g2.setColor(Color.black);
		g2.setStroke(new BasicStroke(borderWidth));
		g2.drawRect(0, 0, width, height);
		
		g2.setStroke(new BasicStroke(1));
		
		if(obstacles != null)
		{
			for(double[] obs : obstacles)
			{
				int xmin = (int) (obs[0]/factor)+borderWidth;
				int ymin = (int) (obs[1]/factor)+borderWidth;
				int xmax = (int) (obs[2]/factor)+borderWidth;
				int ymax= (int) (obs[3]/factor)+borderWidth;
				g2.fillRect(xmin, ymin, xmax-xmin, ymax-ymin);
			}
		}
		if(layout!= null)
		{
			g2.setColor(Color.red);
			for(double[] windmill : layout)
			{
				
				int x = (int)(windmill[0]/factor + borderWidth/2);
				int y = (int)(windmill[1]/factor + borderWidth/2);
				g2.drawOval(x-radius, y-radius, 2*radius, 2*radius);
				g2.fillOval(x-radiusS, y-radiusS, radiusS*2, radiusS*2);
			}
		}
	}
	

}
