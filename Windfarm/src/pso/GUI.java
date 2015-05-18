package pso;

import java.awt.Color;
import java.awt.Graphics;

import javax.swing.JFrame;
import javax.swing.JPanel;

import windfarmapi.WindScenario;

public class GUI extends JPanel{
	
	private int width;
	private int height;
	private static double factor = 15; 
	private JFrame mainframe;
	private double[][] layout;
	private static int radius = 8; 
	
	
	public GUI(WindScenario scenario)
	{
		super();
		this.width = (int) (scenario.width/factor); 
		this.height = (int) (scenario.height/factor);
		super.setSize(width, height);
		super.setBackground(Color.WHITE);
		mainframe = new JFrame();
		mainframe.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		mainframe.setBounds(0, 0, width, height);
		mainframe.getContentPane().add(this);
		mainframe.setVisible(true);
		
	}
	
	public void update(double[][] layout){
		this.layout = layout;
		repaint();
	}
	
	
	@Override
	public void paint(Graphics g){
		super.paintComponent(g);
		if(layout!= null)
			for(double[] windmill : layout)
			{
				g.setColor(Color.red);
				int x = (int)(windmill[0]/factor);
				int y = (int)(windmill[1]/factor);
				g.fillOval(x-radius, y-radius, radius, radius);
			}
	}
	

}
