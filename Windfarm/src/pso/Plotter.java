package pso;

import java.io.File;
import java.io.IOException;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

public class Plotter {

	
	/**
	 * Save after every n datapoints
	 */
	private int saveAfterN = 10;
	
	private XYSeries series = new XYSeries("Fitness");
	private String plotUID;
	
	public Plotter() {
		plotUID = System.currentTimeMillis()+"";
		new File("plots").mkdirs();
	}
	
	
	public void addDataPoint(int x, double y) {
		series.add(x,y);
		if(x%saveAfterN == 0) {
			saveToImage();
		}
	}
	
	
	private void saveToImage() {
		
		System.out.println("Writing plot to file!");
		
		// Add the series to your data set
		XYSeriesCollection dataset = new XYSeriesCollection();
		dataset.addSeries(series);
		// Generate the graph
		JFreeChart chart = ChartFactory.createXYLineChart(
			"", // Title
			"time", // x-axis Label
			"fitness*1000", // y-axis Label
			dataset, // Dataset
			PlotOrientation.VERTICAL, // Plot Orientation
			false, // Show Legend
			true, // Use tooltips
			false // Configure chart to generate URLs?
		);
		XYPlot xyPlot = (XYPlot) chart.getPlot();
		((NumberAxis)xyPlot.getRangeAxis()).setAutoRangeIncludesZero(false);
		
		
		try {
			ChartUtilities.saveChartAsJPEG(new File("plots/chart"+plotUID+".jpg"), chart, 800, 600);
		} catch (IOException e) {
			System.err.println("Problem occurred creating chart.");
		}
	}
	
		
	
	
}
