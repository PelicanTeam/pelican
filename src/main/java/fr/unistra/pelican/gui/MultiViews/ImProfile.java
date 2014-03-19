/**
 * 
 */
package fr.unistra.pelican.gui.MultiViews;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFrame;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.jfree.chart.JFreeChart;
import org.jfree.chart.renderer.xy.XYItemRenderer;

import fr.unistra.pelican.Image;
import fr.unistra.pelican.util.ChartCreator;
import fr.unistra.pelican.util.Line;

/**
 * <p>To display cut through a set of images and show the cut line over another Panel (transparency and event dispatching). 
 * <p>Chart can be customized (change color, add extra informations,...) by implementing the {@link ImageProfileChartCustomizer} interface and adding it to the properties of the image with keyword profileCustomizer
 * 
 * @author Benjamin Perret
 *
 */
public class ImProfile extends JPanel implements MouseMotionListener, MouseListener, MouseWheelListener,ChangeListener, ActionListener{

	
	private Color color [] = {Color.black, Color.red, Color.blue, Color.green,Color.orange,Color.gray, Color.cyan, Color.pink, Color.yellow};
	
	/**
	 * Image property keyword to store your own {@link ImageProfileChartCustomizer}
	 */
	public static final String profileCustomizer = "IMAGE_PROFILE_CUSTOMIZER";
	
	private int x1;
	private int x2;
	private int y1;
	private int y2;
	private Point p1=null;
	private View view;
	private Point p2=null;
	
	private Point selected=null;
	boolean change=true;
	public JFrame frame;
	private ArrayList<View> views = new ArrayList<View>();
	private BufferedImage im ;
	private ImagePanel profile;
	
	private boolean log=false;
	
	private JFreeChart chart;
	private JCheckBoxMenuItem logScale;
	private JMenuItem dropAsMatlabPlot;
	
	public void setLine(Point p1, Point p2)
	{
		if(this.p1!=null)
		{
			this.p1.x=p1.x;
			this.p1.y=p1.y;
		}else {
			this.p1=new Point(p1);
		}
		
		if(this.p2!=null)
		{
			this.p2.x=p2.x;
			this.p2.y=p2.y;
		}else {
			this.p2=new Point(p2);
		}
	}
	
	public ImProfile()
	{
		
		
		this.addMouseListener(this);
		this.addMouseMotionListener(this);
		this.addMouseWheelListener(this);
		
		profile=new ImagePanel();
		buildPopUp();
		frame=new JFrame("Profile");
		frame.setSize(400, 400);
		frame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
		frame.addComponentListener(new ComponentListener(){

			@Override
			public void componentHidden(ComponentEvent e) {
				
				
			}

			@Override
			public void componentMoved(ComponentEvent e) {
				
				
			}

			@Override
			public void componentResized(ComponentEvent e) {
				refreshGraph();
				
			}

			@Override
			public void componentShown(ComponentEvent e) {
				
				
			}
			
		});
		frame.add(profile);
		//frame.setLocationRelativeTo(null);
		
	}
	
	private void buildPopUp(){
		//popup=new JPopupMenu();
		
		logScale=new JCheckBoxMenuItem("log scale");
		logScale.setSelected(log);
		logScale.addActionListener(this);
		
		dropAsMatlabPlot = new JMenuItem("Drop for Matlab Plot");
		dropAsMatlabPlot.addActionListener(this);
		
		profile.addPopUpOption("Profile view", logScale,dropAsMatlabPlot);
	}
	
	@Override
	public void paint(Graphics g) {
		if (frame.isVisible()) {
			Component c = this.getParent().getParent();
			if (c != null && c instanceof MagicPanel) {
				View v = ((MagicPanel) c).getBg().getView();
				if(v==null)
					return;
				if(v!=view)
				{
					change=true;
					view=v;
				}
				if (p1 == null || p2 == null) {
					int w = getWidth();
					int h = getHeight();
					int x1 = view.getAbsoluteXCoord(w / 4);
					int x2 = view.getAbsoluteXCoord(3 * w / 4);
					int y1 = view.getAbsoluteYCoord(h / 2);
					int y2 = view.getAbsoluteYCoord(h / 2);
					p1 = new Point(x1, y1);
					p2 = new Point(x2, y2);
				}
				x1 = view.getRelativeXCoord(p1.x);
				x2 = view.getRelativeXCoord(p2.x);
				y1 = view.getRelativeYCoord(p1.y);
				y2 = view.getRelativeYCoord(p2.y);
				g.setColor(Color.red);

				g.drawLine(x1, y1, x2, y2);
				g.drawRect(x1 - 2, y1 - 2, 4, 4);
				g.drawRect(x2 - 2, y2 - 2, 4, 4);
			}
			
			extractProfiles();
		}
	}
	double [][] xSeries;
	double [][] ySeries;
	private void extractProfiles(){
		if(change)
		{
			//System.out.println("extract");
		int l=views.size();
		String [] names = new String[l];
		xSeries=new double[l][];
		ySeries=new double[l][];
		int i=0;
		
		if (views.size() == 0) {
			//System.out.println("nullin");
				profile.setImage((Image) null);
			} else {
				for (View v : views) {
					double[][] op = null;
					names[i] = v.getImage().getName();
					if (names[i] == null)
						names[i] = "No Name";
					Object o = null;// v.properties.get(PROFILE_SAVE);
					if (o != null && o instanceof double[][]) {
						op = (double[][]) o;
					} else {
						change = true;
						op = extractLine(v, v.getDisplayedBand());
					}

					ySeries[i] = op[0];
					xSeries[i] = op[1];
					i++;
				}

				chart = ChartCreator.getXYChart("Profile view", names,"Distance", "Intensity", xSeries, ySeries);
				XYItemRenderer renderer = chart.getXYPlot().getRenderer();
				for(int j=0;j<Math.min(names.length, color.length);j++)
				{
					renderer.setSeriesPaint(j, color[j]);
				}
				int j=0;
				for (View v : views) {
					Image image=v.getImage();
					Object o= image.getProperty(profileCustomizer);
					if(o!=null && o instanceof ImageProfileChartCustomizer)
					{
						((ImageProfileChartCustomizer)o).customizeChart(chart, p1, p2,v,j);
					}
					j++;
				}
				
				
				
				refreshGraph();
				
				
				
				change = false;
			}
		}
		
		

	}
	
	
	
	/**
	 * Implements this and add you own customizer in the image properties
	 * @author Benjamin Perret
	 *
	 */
	public abstract interface ImageProfileChartCustomizer{
		
		/**
		 * Customize given chart knowing the position of the cut, the underlying view and the index of the serie
		 * @param chart the chart
		 * @param p1 origin of the cut
		 * @param p2 end of the cut
		 * @param v view of the image
		 * @param serieIndex index of the serie representing the cut
		 */
		public abstract void customizeChart(JFreeChart chart, Point p1, Point p2, View v, int serieIndex);
		
	}
	
	
	private void refreshGraph(){
		if(chart!=null){
		im=chart.createBufferedImage(profile.getWidth(), profile.getHeight());
		profile.setImage(im);
		View v=profile.setImage(im);
		v.setAutoFitWindow(true);}
	}
	

	
	private double [][] extractLine(View v, int band)
	{
		double [][] res= new double [2][];
		Image im=v.getPersistentImage();
		int x1 = p1.x;//v.getAbsoluteXCoord(p1.x);
		int x2 = p2.x;//v.getAbsoluteXCoord(p2.x);
		int y1 = p1.y;//v.getAbsoluteYCoord(p1.y);
		int y2 = p2.y;//v.getAbsoluteYCoord(p2.y);
		
		Line l =new Line(new Point(x1,y1), new Point(x2,y2));
		res[0]=l.imProfileDouble(im,band);
		//System.out.println(l);
		
		res[1] = new double[res[0].length];
		double c=0.0;
		double length=Math.sqrt((p1.x-p2.x)*(p1.x-p2.x)+(p1.y-p2.y)*(p1.y-p2.y));
		double step=length/(double)res[0].length;//not perfect but seems ok
		if (log) {
			for (int i = 0; i < res[0].length; i++) {
				res[0][i] = Math.max(Math.log(res[0][i]), -2);
				res[1][i] = c;
				c += step;
			}
		}else{
			for (int i = 0; i < res[0].length; i++) {
				res[1][i] = c;
				c += step;
			}
		}
		
		return res;
	}
	
	/**
	 * @param e
	 * @return
	 * @see java.util.ArrayList#add(java.lang.Object)
	 */
	public boolean add(View e) {

		boolean b=false;
		if(!views.contains(e))
			b= views.add(e);
		change=true;
		revalidate();
		return b;
	}

	/**
	 * 
	 * @see java.util.ArrayList#clear()
	 */
	public void clear() {
		for (View v: views)
			remove(v);
		
	}

	public void refresh()
	{
		change=true;
		revalidate();
	}
	/**
	 * @param o
	 * @return
	 * @see java.util.ArrayList#remove(java.lang.Object)
	 */
	public boolean remove(Object o) {
		boolean res=views.remove(o);
		refresh();
		repaint();
		return res;
	}
	
	public void setVisible(boolean f)
	{
		frame.setVisible(f);
	}
	
	public void setVisible(boolean f, Point location)
	{
		frame.setLocation(location);
		frame.setVisible(f);
	}
	
	public boolean isVisible()
	{
		return frame.isVisible();
	}

	
	public void mouseDragged(MouseEvent e) {
		if (selected != null) {
			selected.x = view.getAbsoluteXCoord(e.getX());
			selected.y = view.getAbsoluteYCoord(e.getY());
			change = true;
		} else
			redispatchMouseEvent(e);
	}

	
	public void mouseMoved(MouseEvent e) {
		
		redispatchMouseEvent(e) ;
	}

	
	public void mouseClicked(MouseEvent e) {
		redispatchMouseEvent(e) ;
	}

	
	public void mouseEntered(MouseEvent e) {
		
		redispatchMouseEvent(e) ;
	}

	
	public void mouseExited(MouseEvent e) {
		
		redispatchMouseEvent(e) ;
	}

	
	public void mousePressed(MouseEvent e) {
		int x=e.getX();
		int y=e.getY();
		int dx=x-x1;
		int dy=y-y1;
		int dx2=x-x2;
		int dy2=y-y2;
		if(dx*dx+dy*dy<=25)
		{
			selected=p1;
		}
		else if(dx2*dx2+dy2*dy2<=25)
		{
			selected=p2;
		}
		else redispatchMouseEvent(e) ;
		
	}

	
	public void mouseReleased(MouseEvent e) {
		selected=null;
		redispatchMouseEvent(e) ;
	}
	
	private void redispatchMouseEvent(MouseEvent e) {
		Point glassPanePoint = e.getPoint();
		Container container = this.getParent();
		Point containerPoint = SwingUtilities.convertPoint(this,
				glassPanePoint, container);

		if (containerPoint.y < 0) { // we're not in the content pane
		// Could have special code to handle mouse events over
		// the menu bar or non-system window decorations, such as
		// the ones provided by the Java look and feel.
		} else {
			// The mouse event is probably over the content pane.
			// Find out exactly which component it's over.
			Component component = null;
			/*
			 * SwingUtilities.getDeepestComponentAt( container,
			 * containerPoint.x, containerPoint.y);
			 */

			int nbcom = container.getComponentCount();

			for (int i = 0; i < nbcom; i++) {
				Component c = container.getComponent(i);

				if (c != this) {
					component = SwingUtilities.getDeepestComponentAt(c,
							containerPoint.x, containerPoint.y);
					break;
				}
			}

			if ((component != null)) {

				Point componentPoint = SwingUtilities.convertPoint(this,
						glassPanePoint, component);
				component
						.dispatchEvent(new MouseEvent(component, e.getID(), e
								.getWhen(), e.getModifiers(), componentPoint.x,
								componentPoint.y, e.getClickCount(), e
										.isPopupTrigger()));
			}
		}

		// Update the glass pane if requested.

	}

	private void redispatchMouseWheelEvent(MouseWheelEvent e) {
		Point glassPanePoint = e.getPoint();
		Container container = this.getParent();
		Point containerPoint = SwingUtilities.convertPoint(
		              this,
		              glassPanePoint,
		              container);

		if (containerPoint.y < 0) { //we're not in the content pane
		//Could have special code to handle mouse events over
		//the menu bar or non-system window decorations, such as
		//the ones provided by the Java look and feel.
		} else {
		//The mouse event is probably over the content pane.
		//Find out exactly which component it's over.
		Component component =null;
		/*SwingUtilities.getDeepestComponentAt(
		              container,
		              containerPoint.x,
		              containerPoint.y);*/



		int nbcom=container.getComponentCount();

		for(int i=0;i<nbcom;i++)
		{
			Component c = container.getComponent(i);
			
			if(c!=this)
			{
				component =SwingUtilities.getDeepestComponentAt(
			              c,
			              containerPoint.x,
			              containerPoint.y);
				break;
			}
		}
		
		if ((component != null)
		) {

		Point componentPoint = SwingUtilities.convertPoint(
		                  this,
		                  glassPanePoint,
		                  component);
		component.dispatchEvent(new MouseWheelEvent(component,
		                           e.getID(),
		                           e.getWhen(),
		                           e.getModifiers(),
		                           componentPoint.x,
		                           componentPoint.y,
		                           e.getClickCount(),
		                           e.isPopupTrigger(),e.getScrollType(),e.getScrollAmount(),e.getWheelRotation()));
		}
		}

		//Update the glass pane if requested.

		}
	
	
	
	public void mouseWheelMoved(MouseWheelEvent e) {
		redispatchMouseWheelEvent(e);
		
	}

	/**
	 * @param o
	 * @return
	 * @see java.util.ArrayList#contains(java.lang.Object)
	 */
	public boolean contains(Object o) {
		return views.contains(o);
	}

	
	public void stateChanged(ChangeEvent e) {
		Object o=e.getSource();
		if(o instanceof View)
		{
			change=true;
		}
		
	}

	private void dropAsMatlabPlot(){
		StringBuffer buf=new StringBuffer();
		for(int i=0;i < xSeries.length;i++)
		{
			double [] xs=xSeries[i];
			double [] ys=ySeries[i];
			buf.append("X"+i+"=[");
			for(int j=0;j<xs.length;j++)
			{
				buf.append(xs[j]);
				if(j!=xs.length-1)
					buf.append(",");
			}
			buf.append("];\n");
			buf.append("Y"+i+"=[");
			for(int j=0;j<ys.length;j++)
			{
				buf.append(ys[j]);
				if(j!=ys.length-1)
					buf.append(",");
			}
			buf.append("];\n");
		}
		
		
		buf.append("figure(2);\nhold on;\n");
		for(int i=0;i < xSeries.length;i++)
			buf.append("plot(X"+i+",Y"+i+");\n");
		buf.append("hold off;\n");
		System.out.println(buf);
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		Object o=e.getSource();
		if(o==logScale)
		{
			log=logScale.isSelected();
			refresh();
			repaint();
		}else if(o==dropAsMatlabPlot)
		{
			dropAsMatlabPlot();
		}
		
	}
	
	

}
