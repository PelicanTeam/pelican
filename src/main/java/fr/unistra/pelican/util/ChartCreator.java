/**
 * 
 */
package fr.unistra.pelican.util;

import java.awt.Color;
import java.awt.RenderingHints;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;

import javax.swing.JFrame;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartRenderingInfo;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.StandardXYItemRenderer;
import org.jfree.chart.renderer.xy.XYDotRenderer;
import org.jfree.chart.renderer.xy.XYErrorRenderer;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.data.statistics.HistogramDataset;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYIntervalSeries;
import org.jfree.data.xy.XYIntervalSeriesCollection;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.io.IOUtils;


import com.sun.xml.internal.txw2.Document;

import fr.unistra.pelican.Image;
import fr.unistra.pelican.algorithms.visualisation.MViewer;
import fr.unistra.pelican.gui.MultiViews.ImagePanel;

/**
 * Ever wanted to plot nice charts in a few lines!
 * 
 * @author Benjamin Perret
 *
 */
public abstract class ChartCreator {

	public static BufferedImage createXYChart(String title,String [] seriesName, String xAxis, String yAxis,double [][] x, double [][] y, int width, int height)
	{
		
		return getXYChart(title, seriesName, xAxis, yAxis, x, y).createBufferedImage(width, height);

	}
	
	public static JFreeChart getHistogramChart(String title,String  serieName, String xAxis, String yAxis,double [] value, int bins)
	{
		HistogramDataset dataset = new HistogramDataset();
    
		dataset.addSeries(serieName, value, bins);
		JFreeChart chart = ChartFactory.createHistogram(title,xAxis,yAxis,dataset,PlotOrientation.VERTICAL,false,false,false);
		return chart;
	}
	
	public static JFreeChart getHistogramChart(String title,String  [] seriesName, String xAxis, String yAxis,double [][] value, int bins)
	{
		HistogramDataset dataset = new HistogramDataset();
    
		for(int i=0;i<seriesName.length;i++)
		{
			dataset.addSeries(seriesName[i], value[i], bins);
		}
		JFreeChart chart = ChartFactory.createHistogram(title,xAxis,yAxis,dataset,PlotOrientation.VERTICAL,true,false,false);
		XYPlot xyPlot = (XYPlot)chart.getPlot();

		xyPlot.setForegroundAlpha(0.6F);
		return chart;
	}
	
	public static BufferedImage createHistogramChart(String title,String  serieName, String xAxis, String yAxis,double [] value, int bins, int width, int height)
	{
		return getHistogramChart(title, serieName, xAxis, yAxis, value, bins).createBufferedImage(width, height);
	}
	public static JFreeChart getXYChart(String title,String [] seriesName, String xAxis, String yAxis,double [][] x, double [][] y)
	{
		
		if(x.length != y.length || seriesName.length!=x.length)
		{
			System.err.println("Error creating chart, seriesName, x and y must have same dimensions");
			return null;
		}
		XYSeriesCollection xyDataset = new XYSeriesCollection();
		for(int i=0;i<seriesName.length;i++)
		{
			if(x[i].length != y[i].length)
			{
				System.err.println("Error in serie " + (i +1) + " x and y must have same dimensions!");
				return null;
			}
			XYSeries series = new XYSeries(seriesName[i]);
			
			for(int j=0;j<x[i].length;j++)
				series.add(x[i][j], y[i][j]);
			xyDataset.addSeries(series);
		}
		
		JFreeChart chart = ChartFactory.createXYLineChart
		                     (title,  // Title
		                    	xAxis,           // X-Axis label
		                    	yAxis,           // Y-Axis label
		                      xyDataset,          // Dataset
		                      PlotOrientation.VERTICAL, //orientation
		                      true,                // Show legend
		                      false,
		                      false
		                     );
		
		return chart;

	}
	
	public static JFreeChart getXYPointsChartWithErrorBars(String title,String seriesName, String xAxis, String yAxis,double [] x, double [] y,double [] yErrorDown,double [] yErrorUp)
	{
		if(x.length != y.length || y.length!=yErrorDown.length || yErrorDown.length!=yErrorUp.length)
		{
				System.err.println("Error in serie x, y and y up and down errors must have same dimensions!");
				return null;
		}
		
		XYIntervalSeriesCollection seriesCollection=new XYIntervalSeriesCollection();
		XYIntervalSeries intervalSerie=new XYIntervalSeries(seriesName);
		
			
		for(int i=0;i<x.length;i++)
			intervalSerie.add(x[i], x[i], x[i], y[i], yErrorDown[i], yErrorUp[i]);
			
		seriesCollection.addSeries(intervalSerie);
			
		
		XYItemRenderer renderer = new XYErrorRenderer();
		NumberAxis domainAxis=new NumberAxis(xAxis);
		NumberAxis valueAxis=new NumberAxis(yAxis);
		XYPlot plot = new XYPlot(seriesCollection, domainAxis, valueAxis, renderer);
		//renderer0.setSeriesPaint(0,new Color(0,0,255));
		//renderer0.setSeriesPaint(1,new Color(255,0,0));
		JFreeChart chart=new JFreeChart(title,null,plot,true);
		
		
		return chart;
		

	}
	
	public static JFreeChart getXYPointsChart(String title,String seriesName, String xAxis, String yAxis,double [] x, double [] y)
	{
		if(x.length != y.length )
		{
				System.err.println("Error in series x, y  must have same dimensions!");
				return null;
		}
		
		XYSeriesCollection seriesCollection=new XYSeriesCollection();
		XYSeries intervalSerie=new XYSeries(seriesName);
		
			
		for(int i=0;i<x.length;i++)
			intervalSerie.add(x[i], y[i]);
			
		seriesCollection.addSeries(intervalSerie);
		
		int psize=3;
		
		XYDotRenderer renderer = new XYDotRenderer();
		renderer.setDotHeight(psize);
		renderer.setDotWidth(psize);
		NumberAxis domainAxis=new NumberAxis(xAxis);
		NumberAxis valueAxis=new NumberAxis(yAxis);
		XYPlot plot = new XYPlot(seriesCollection, domainAxis, valueAxis, renderer);
		//renderer0.setSeriesPaint(0,new Color(0,0,255));
		//renderer0.setSeriesPaint(1,new Color(255,0,0));
		JFreeChart chart=new JFreeChart(title,null,plot,true);
		
		
		return chart;
		

	}
	
	public static BufferedImage createXYPointsChartWithErrorBars(String title,String seriesName, String xAxis, String yAxis,double [] x, double [] y,double [] yErrorDown,double [] yErrorUp, int width, int height)
	{
		return getXYPointsChartWithErrorBars(title, seriesName, xAxis, yAxis, x, y, yErrorDown, yErrorUp).createBufferedImage(width, height);
	}
	
	
	public static BufferedImage createXYPointsChart(String title,String seriesName, String xAxis, String yAxis,double [] x, double [] y, int width, int height)
	{
		return getXYPointsChart(title, seriesName, xAxis, yAxis, x, y).createBufferedImage(width, height);
	}
	
	public static BufferedImage createXYChart(String title,String serieName, String xAxis, String yAxis,double [] x, double [] y, int width, int height)
	{
		XYSeries series = new XYSeries(serieName);
		for(int i=0;i<x.length;i++)
			series.add(x[i], y[i]);
		
		XYDataset xyDataset = new XYSeriesCollection(series);
		JFreeChart chart = ChartFactory.createXYLineChart
		                     (title,  // Title
		                    	xAxis,           // X-Axis label
		                    	yAxis,           // Y-Axis label
		                      xyDataset,          // Dataset
		                      PlotOrientation.VERTICAL, //orientation
		                      true,                // Show legend
		                      false,
		                      false
		                     );
		
		return chart.createBufferedImage(width, height);

	}
	
	public static BufferedImage createXYChart(String title,double [] y)
	{
		double [] x=new double[y.length];
		for(int i=0;i<x.length;i++)
			x[i]=i;
		return createXYChart(title, "data1", "x", "y", x, y, 800, 600);

	}

	public static BufferedImage createXYChart(String title,double [] y,int width, int height)
	{
		double [] x=new double[y.length];
		for(int i=0;i<x.length;i++)
			x[i]=i;
		return createXYChart(title, "data1", "x", "y", x, y, width,height);

	}

	public static BufferedImage createDoubleAxisXYChart(String title,String serieName1,String serieName2, String xAxis, String yAxis1, String yAxis2,double [] x,double [] y1,double [] y2,int width, int height)
	{
		// final String chartTitle = "Dual Axis Demo 2";
		XYSeriesCollection dataset = new XYSeriesCollection();
		XYSeries series =  new XYSeries(serieName1);
		for(int i=0;i<x.length;i++)
			series.add(x[i], y1[i]);
		dataset.addSeries(series);
		
		XYSeriesCollection dataset2 = new XYSeriesCollection();
		XYSeries series2 =  new XYSeries(serieName2);
		for(int i=0;i<x.length;i++)
			series2.add(x[i], y2[i]);
		dataset2.addSeries(series2);
		
	        final JFreeChart chart = ChartFactory.createXYLineChart		                     (title,  // Title
                	xAxis,           // X-Axis label
                	yAxis1,           // Y-Axis label
                  dataset,          // Dataset
                  PlotOrientation.VERTICAL, //orientation
                  true,                // Show legend
                  false,
                  false
                 );

	  //      final StandardLegend legend = (StandardLegend) chart.getLegend();
	    //    legend.setDisplaySeriesShapes(true);
	        
	        XYPlot plot = chart.getXYPlot();
	        NumberAxis axis2 = new NumberAxis(yAxis2);
	        axis2.setAutoRangeIncludesZero(false);
	        plot.setRangeAxis(1, axis2);
	        plot.setDataset(1, dataset2);
	        plot.mapDatasetToRangeAxis(1, 1);
	        //final XYItemRenderer renderer = plot.getRenderer();
	       // renderer.setToolTipGenerator(StandardXYToolTipGenerator.getTimeSeriesInstance());
	       /* if (renderer instanceof StandardXYItemRenderer) {
	            final StandardXYItemRenderer rr = (StandardXYItemRenderer) renderer;
	            rr.setPlotShapes(true);
	            rr.setShapesFilled(true);
	        }*/
	        
	        final StandardXYItemRenderer renderer2 = new StandardXYItemRenderer();
	        renderer2.setSeriesPaint(0, Color.black);
	        plot.setRenderer(1, renderer2);
	      /* renderer2.setPlotShapes(true);
	        renderer.setToolTipGenerator(StandardXYToolTipGenerator.getTimeSeriesInstance());
	    
	        plot.setRenderer(1, renderer2);
	        */
	       /* final DateAxis axis = (DateAxis) plot.getDomainAxis();
	        axis.setDateFormatOverride(new SimpleDateFormat("MMM-yyyy"));
	        
	        final ChartPanel chartPanel = new ChartPanel(chart);
	        chartPanel.setPreferredSize(new java.awt.Dimension(500, 270));
	        setContentPane(chartPanel);*/
	        return chart.createBufferedImage(width, height);
	}
	
	public static BufferedImage createDoubleAxisXYChart(String title,String [] serieName1,String [] serieName2, String xAxis, String yAxis1, String yAxis2,double [][] x,double [][] y1,double [][] y2,int width, int height)
	{
		// final String chartTitle = "Dual Axis Demo 2";
		XYSeriesCollection dataset = new XYSeriesCollection();
		for(int j=0;j<serieName1.length;j++)
		{
			XYSeries series =  new XYSeries(serieName1[j]);
			for(int i=0;i<x[j].length;i++)
				series.add(x[j][i], y1[j][i]);
			dataset.addSeries(series);
		}
		
		XYSeriesCollection dataset2 = new XYSeriesCollection();
		for(int j=0;j<serieName2.length;j++)
		{
			XYSeries series =  new XYSeries(serieName2[j]);
			for(int i=0;i<x[j].length;i++)
				series.add(x[j][i], y2[j][i]);
			dataset2.addSeries(series);
		}
		
	        final JFreeChart chart = ChartFactory.createXYLineChart		                     (title,  // Title
                	xAxis,           // X-Axis label
                	yAxis1,           // Y-Axis label
                  dataset,          // Dataset
                  PlotOrientation.VERTICAL, //orientation
                  true,                // Show legend
                  false,
                  false
                 );

	  //      final StandardLegend legend = (StandardLegend) chart.getLegend();
	    //    legend.setDisplaySeriesShapes(true);
	        
	        XYPlot plot = chart.getXYPlot();
	        NumberAxis axis2 = new NumberAxis(yAxis2);
	        axis2.setAutoRangeIncludesZero(false);
	        plot.setRangeAxis(1, axis2);
	        plot.setDataset(1, dataset2);
	        plot.mapDatasetToRangeAxis(1, 1);
	        //final XYItemRenderer renderer = plot.getRenderer();
	       // renderer.setToolTipGenerator(StandardXYToolTipGenerator.getTimeSeriesInstance());
	       /* if (renderer instanceof StandardXYItemRenderer) {
	            final StandardXYItemRenderer rr = (StandardXYItemRenderer) renderer;
	            rr.setPlotShapes(true);
	            rr.setShapesFilled(true);
	        }*/
	        
	        final StandardXYItemRenderer renderer2 = new StandardXYItemRenderer();
	        renderer2.setSeriesPaint(0, Color.black);
	        plot.setRenderer(1, renderer2);
	      /* renderer2.setPlotShapes(true);
	        renderer.setToolTipGenerator(StandardXYToolTipGenerator.getTimeSeriesInstance());
	    
	        plot.setRenderer(1, renderer2);
	        */
	       /* final DateAxis axis = (DateAxis) plot.getDomainAxis();
	        axis.setDateFormatOverride(new SimpleDateFormat("MMM-yyyy"));
	        
	        final ChartPanel chartPanel = new ChartPanel(chart);
	        chartPanel.setPreferredSize(new java.awt.Dimension(500, 270));
	        setContentPane(chartPanel);*/
	        return chart.createBufferedImage(width, height);
	}
	
	public static void displayChart(String title,String [] seriesName, String xAxis, String yAxis,double [][] x, double [][] y, int width, int height)
	{
		JFrame frame=new JFrame();
		frame.setTitle(title);
		ImagePanel imp=new ImagePanel();
		imp.setImage(createXYChart(title, seriesName, xAxis, yAxis, x, y, width, height));
		frame.add(imp);
		frame.setSize(width,height);
		frame.setVisible(true);
	}
	
	public static void displayChart(String title,String serieName, String xAxis, String yAxis,double [] x, double [] y, int width, int height)
	{
		JFrame frame=new JFrame();
		frame.setTitle(title);
		ImagePanel imp=new ImagePanel();
		imp.setImage(createXYChart(title, serieName, xAxis, yAxis, x, y, width, height));
		frame.add(imp);
		frame.setSize(width,height);
		frame.setVisible(true);
	}
	
	public static void displayChart(String title,double [] y)
	{
		JFrame frame=new JFrame();
		frame.setTitle(title);
		ImagePanel imp=new ImagePanel();
		imp.setImage(createXYChart(title,y));
		frame.add(imp);
		frame.setSize(600,800);
		frame.setVisible(true);
	}

	public static void displayChart(String title,double [] y, int width, int height)
	{
		JFrame frame=new JFrame();
		frame.setTitle(title);
		ImagePanel imp=new ImagePanel();
		imp.setImage(createXYChart(title,y,width,height));
		frame.add(imp);
		frame.setSize(600,800);
		frame.setVisible(true);
	}
	
   
	
	public static void main(String [] args){
		double [] x={0.1,1.0};
		double [] y={0.5,0.7};
		double [] yd={0.4,0.5};
		double [] yu={0.65,0.75};
		JFreeChart chart=getXYPointsChartWithErrorBars("test", "serie1", "des x", "des y", x, y, yd, yu);
		MViewer.exec().add(chart.createBufferedImage(600, 600));
		
	}
	
}
