package pso;

import java.util.ArrayList;

import windfarmapi.WindFarmLayoutEvaluator;
import windfarmapi.WindScenario;

public class KusiakParticleEvaluator extends WindFarmLayoutEvaluator {
	protected double tspe[][];
	protected ArrayList<Particle> tpositions;
	protected double energyCapture;
	protected double wakeFreeRatio;
	protected double energyCost;
	
	public static final double fac=Math.PI/180;

	@Override
	public void initialize(WindScenario scenario) {
		tspe=null;
		tpositions=null;
		energyCapture=0;
		wakeFreeRatio=0;
		this.scenario=scenario;
        energyCost=Double.MAX_VALUE;
        super.nEvals = 0;
	}

        @Override
	public double evaluate(ArrayList<Particle> layout) {
	    final double ct  = 750000;
	    final double cs  = 8000000;
	    final double m   = 30;
	    final double r   = 0.03;
	    final double y   = 20;
	    final double com = 20000;

	    double wfr = evaluate_2014(layout);
	    if (wfr<=0) return Double.MAX_VALUE; 
	    int n = layout.size();
	    
	    energyCost = ((ct*n+cs*Math.floor(n/m)*(0.666667+0.333333*Math.exp(-0.00174*n*n))+com*n)/
		    ((1-Math.pow(1+r, -y))/r)/(8760.0*scenario.wakeFreeEnergy*wfr*n))+0.1/n;

	    //	    System.out.println(n+"\t"+wfr+"\t"+fit);

	    return energyCost;
	}

	@Override
	public double evaluate_2014(ArrayList<Particle> layout) {
		super.nEvals++;
		// Copying the layout
		tpositions=layout;

		energyCapture=0;
		if (checkConstraint()) {
			tspe=new double[scenario.thetas.length][tpositions.size()];
			// wind resource per turbine => stored temporaly in tspe
			for (int turb=0; turb<tpositions.size(); turb++) {
				// for each turbine
				double turb_energy = 0;
				for (int thets=0; thets<scenario.thetas.length; thets++) {
					// for each direction
					// calculate the wake
					double totalVdef=calculateWakeTurbine(turb, thets);
					double cTurb=scenario.c[thets]*(1.0-totalVdef);
					// annual power output per turbine and per direction
					double tint=scenario.thetas[thets][1]-scenario.thetas[thets][0];
					double w=scenario.omegas[thets];
					double ki=scenario.ks[thets];
					double totalPow=0;
					for (int ghh=1; ghh<scenario.vints.length; ghh++) {
						double v=(scenario.vints[ghh]+scenario.vints[ghh-1])/2.0;
						double P=powOutput(v);
						double prV=WindScenario.wblcdf(scenario.vints[ghh], cTurb, ki)-WindScenario.wblcdf(scenario.vints[ghh-1],cTurb,ki);
						totalPow+=prV*P;
					}
					totalPow+=scenario.PRated*(1.0-WindScenario.wblcdf(scenario.vRated, cTurb, ki));
					totalPow*=tint*w;
					tspe[thets][turb]=totalPow;
					turb_energy+=totalPow;
					energyCapture+=totalPow;
				}
				tpositions.get(turb).newEval(turb_energy);
				
			}
			wakeFreeRatio=energyCapture/(scenario.wakeFreeEnergy*tpositions.size());
			return wakeFreeRatio;
		} else {
			energyCapture=0;
			wakeFreeRatio=0;
			tspe=null;
			return 0;
		}
	}

	@Override
	public double[][] getEnergyOutputs() {
		return tspe;
	}

	@Override
	public double[] getTurbineFitnesses() {
		double res[]=new double[tspe[0].length];
		for (int i=0; i<res.length; i++) {
			res[i]=0;
			for (int j=0; j<tspe.length; j++) {
				res[i]+=tspe[j][i];
			}
			res[i]=res[i]/scenario.wakeFreeEnergy;
		}
		return res;
	}

	protected boolean checkConstraint() {
	    for (int i=0; i<tpositions.size(); i++) {
		// checking obstacle constraints
		for (int j=0; j<scenario.obstacles.length; j++) {
		    if (tpositions.get(i).getX() > scenario.obstacles[j][0] &&
			tpositions.get(i).getX() < scenario.obstacles[j][2] &&
			tpositions.get(i).getY() > scenario.obstacles[j][1] &&
			tpositions.get(i).getY() < scenario.obstacles[j][3]) {
			System.out.println("Turbine "+i+"("+tpositions.get(i).getX()+", "+tpositions.get(i).getY()+") is in the obstacle "+j+" ["+scenario.obstacles[j][0]+", "+scenario.obstacles[j][1]+", "+scenario.obstacles[j][2]+", "+scenario.obstacles[j][3]+"].");
			return false;
		    }
		}
		// checking the security constraints
	        for (int j=0; j<tpositions.size(); j++) {
	            if (i!=j) {
	                // calculate the sqared distance between both turb
	                double dist=(tpositions.get(i).getX()-tpositions.get(j).getX())*(tpositions.get(i).getX()-tpositions.get(j).getX())+
	                (tpositions.get(i).getY()-tpositions.get(j).getY())*(tpositions.get(i).getY()-tpositions.get(j).getY());
	                if (dist<scenario.minDist) {
			    System.out.println("Security distance contraint violated between turbines "+i+" ("+tpositions.get(i).getX()+", "+tpositions.get(i).getY()+") and "+j+" ("+tpositions.get(j).getX()+", "+tpositions.get(j).getY()+"): "+Math.sqrt(dist)+" > "+Math.sqrt(scenario.minDist));
	                    return false;
	                }
	            }
	        }
	    }
	    return true;
	}

	protected double calculateWakeTurbine(int turb, int thetIndex) {
	    double x=tpositions.get(turb).getX();
	    double y=tpositions.get(turb).getY();
	    double velDef=0;
	    for (int oturb=0; oturb<tpositions.size(); oturb++) {
	        if (oturb!=turb) {
	            double xo=tpositions.get(oturb).getX();
	            double yo=tpositions.get(oturb).getY();
	            double beta=calculateBeta(x, y, xo, yo, thetIndex);
	            if (beta<scenario.atan_k) {
	                double dij=calculateProjectedDistance(x, y, xo, yo, thetIndex);
	                double curDef=calculateVelocityDeficit(dij);
	                velDef+=curDef*curDef;
	            }
	        }
	    }
	    return Math.sqrt(velDef);
	}
	
	protected double calculateBeta(double xi, double yi, double xj, double yj, int thetIndex) {
	    double num=((xi-xj)*scenario.getCosMidThetas(thetIndex)+(yi-yj)*scenario.getSinMidThetas(thetIndex)+scenario.rkRatio);
	    double a=xi-xj+scenario.rkRatio*scenario.getCosMidThetas(thetIndex);
	    double b=yi-yj+scenario.rkRatio*scenario.getSinMidThetas(thetIndex);
	    double denom=Math.sqrt(a*a+b*b);
	    return Math.acos(num/denom);
	}
	
	protected double powOutput(double v) {
	    if (v<scenario.vCin) {
	        return 0;
	    } else if (v>=scenario.vCin && v<=scenario.vRated) {
	        return scenario.lambda*v+scenario.eta;
	    } else if (scenario.vCout>v && v>scenario.vRated) {
	        return scenario.PRated;
	    } else {
	        return 0;
	    }
	}
	
	protected double calculateProjectedDistance(double xi, double yi, double xj, double yj, int thetIndex) {
		return Math.abs((xi-xj)*scenario.getCosMidThetas(thetIndex)+(yi-yj)*scenario.getSinMidThetas(thetIndex));
	}

	double calculateVelocityDeficit(double dij) {
	    return scenario.trans_CT/((1.0+scenario.krRatio*dij)*(1.0+scenario.krRatio*dij));
	}

	@Override
	public double getEnergyOutput() {
		
		return energyCapture;
	}

	@Override
	public double getWakeFreeRatio() {
		return wakeFreeRatio;
	}
	
	@Override
	public double getEnergyCost() {
		return energyCost;
	}

	@Override
	public WindScenario getScenario() {
		return scenario;
	}

	@Override
	public double evaluate_2014(double[][] layout) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public double evaluate(double[][] layout) {
		// TODO Auto-generated method stub
		return 0;
	}
}
