package pso;

import org.dyn4j.dynamics.Body;

public class Particle extends Body{

	private double[] bestPos;
	private double bestVal;
	
	public double getX(){
		return this.transform.getTranslationX();
	}
	
	public double getY(){
		return this.transform.getTranslationY();
	}
	
	public void newEval(double score){
		if(this.bestVal < score){
			this.bestVal = score;
			this.bestPos = new double[]{this.getX(), this.getY()};
		}
	}
	
}
