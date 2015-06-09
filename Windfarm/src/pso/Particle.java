package pso;

import java.awt.Color;
import java.awt.Graphics2D;

import org.dyn4j.dynamics.Body;
import org.dyn4j.geometry.Vector2;

public class Particle extends Body{

	private double[] bestPos;
	private double bestScore;
	private double score;
	
	public double getX(){
		return this.transform.getTranslationX();
	}
	
	public double getY(){
		return this.transform.getTranslationY();
	}
	
	public void newEval(double score){
		if(this.bestScore < score){
			this.bestScore = score;
			this.bestPos = new double[]{this.getX(), this.getY()};
		}
		this.score = score;
	}
	
	public void draw(Graphics2D g2, double factor, int borderWidth, int radius, int radiusS, double highest, double lowest){
		g2.setColor(setColor(highest, lowest));
		int x = (int)(this.getX()/factor + borderWidth/2);
		int y = (int)(this.getY()/factor + borderWidth/2);
		g2.drawOval(x-radius, y-radius, 2*radius, 2*radius);
		g2.fillOval(x-radiusS, y-radiusS, radiusS*2, radiusS*2);

	}
	
	private Color setColor(double highest, double lowest){
		double h = (score-lowest)/(highest-lowest) * 0.4;
		double s = 0.9;
		double b = 0.9;
		
		return Color.getHSBColor((float)h, (float)s, (float)b);
				
	}
	
	public double distanceTo(Particle party){
		return Math.sqrt(Math.pow(this.getX()-party.getX(), 2) + Math.pow(this.getY()-party.getY(), 2));
	}
	
	public Vector2 getPosition(){
		return new Vector2(this.getX(), this.getY());
	}
	
	public double getScore(){
		return score;
	}
	
	
	
}
