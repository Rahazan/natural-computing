package pso;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.util.ArrayList;

import javax.swing.JFrame;
import javax.swing.JPanel;

import windfarmapi.WindScenario;

public class GUI extends JPanel{
	
	private int width;
	private int height;
	private static double factor = 15; 
	private JFrame mainframe;
	private ArrayList<Particle> layout;
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
		radius = (int)((scenario.R*8/2)/factor);
		super.setPreferredSize(new Dimension(width,height));
		super.setBackground(Color.WHITE);
		mainframe = new JFrame("Windfarm visualization");
		mainframe.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		mainframe.getContentPane().add(this);
		mainframe.pack();
		mainframe.setVisible(true);
		
	}
	
	public void update(ArrayList<Particle> layout){
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
				int xmin = (int) (obs[0]/factor)+borderWidth/2;
				int ymin = (int) (obs[1]/factor)+borderWidth/2;
				int xmax = (int) (obs[2]/factor)+borderWidth/2;
				int ymax= (int) (obs[3]/factor)+borderWidth/2;
				g2.fillRect(xmin, ymin, xmax-xmin, ymax-ymin);
			}
		}
		if(layout!= null)
		{
			double highest = 0;
			double lowest = Double.MAX_VALUE;
			double score;
			for(Particle windmill : layout)
			{
				score = windmill.getScore();
				
				if(score >= highest)
					highest = score;
				else if(score < lowest)
					lowest = score;
			}
			for(Particle windmill : layout)
			{
				windmill.draw(g2, factor, borderWidth, radius, radiusS, highest, lowest);
			}
		}
	}
	

}
