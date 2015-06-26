package pso;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.ArrayList;

import org.dyn4j.dynamics.Body;
import org.dyn4j.geometry.Vector2;

/**
 * Represents a windmill.
 *
 */
public class Particle extends Body{

	private Vector2 bestPos;
	private Vector2 globalBestPos;
	private double bestScore;
	private double score = -1;
	private double distanceTreshold;
	private double maxPossibleDistance;
	private double personalConfidence = 0.1;//0.1
	private double globalConfidence = 0.5;//0.5
	
	
	public Particle(double distanceTreshold, double maxPossibleDistance) {
		this.distanceTreshold = distanceTreshold;
		this.maxPossibleDistance = maxPossibleDistance;
		this.globalBestPos = getPosition();
	}

	public double getX(){
		return this.transform.getTranslationX();
	}
	
	public double getY(){
		return this.transform.getTranslationY();
	}
	
	public void newEval(double score){
		if(this.bestScore < score){
			this.bestScore = score;
			this.bestPos = new Vector2(this.getX(), this.getY());
		}
		this.score = score;
	}
	
	/**
	 * Draw particle to given graphics context
	 */
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
	
	
	public void updateVelocity(ArrayList<Particle> particles, int index, Vector2 globalBest)
	{

		Vector2 repulsiveForce = new Vector2(0.0,0.0);
		Vector2 resultingForce = new Vector2(0.0,0.0);
		Vector2 globalBestForce = new Vector2(0.0,0.0);
		Vector2 bestPosForce = new Vector2(0.0,0.0);
		for(int i = 0 ; i < particles.size() ; i++) 
		{
			if(i!=index)
			{
				Particle part2 = particles.get(i);
				Vector2 delta = getPosition().subtract(part2.getPosition());
				double distance = delta.getMagnitude();
				if (distance > distanceTreshold) {
					continue;
				}
				
				//Power to make closer particles weigh much higher
				double forceScalar = Math.pow(1.0 - distance/this.maxPossibleDistance, 1.5) * 2500;
				
				delta.normalize();
				
				repulsiveForce.add(delta.multiply(forceScalar));
						
			}
				
		}
		
		//personal best
		if(score!=-1)
		{
			Vector2 delta = this.getPosition().subtract(this.bestPos);
			resultingForce.add(delta.multiply(personalConfidence));
//			System.out.println("Personal force: " + resultingForce.toString() + " repulsiveForce: " + repulsiveForce.toString());
		}
		
		//best layout
		bestPosForce = getPosition().subtract(globalBestPos).multiply(globalConfidence); 
		resultingForce.add(bestPosForce);
				
		//global best
//		globalBestForce = this.getPosition().subtract(globalBest).multiply(socialConfidence);
//		resultingForce.add(globalBestForce);
//		
		
		
		
		
			//System.out.println(resultingForce);
			
			resultingForce.add(repulsiveForce);
			this.applyForce(resultingForce);
	
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

	public void newGlobalBest() {
		this.globalBestPos = getPosition();
		
	}
	
	
	
}
