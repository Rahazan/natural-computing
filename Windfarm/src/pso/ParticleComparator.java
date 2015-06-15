package pso;

import java.util.Comparator;

public class ParticleComparator implements Comparator<Particle> {

	@Override
	public int compare(Particle arg0, Particle arg1) {
		return Double.compare(arg0.getScore(), arg1.getScore());
	}

}
