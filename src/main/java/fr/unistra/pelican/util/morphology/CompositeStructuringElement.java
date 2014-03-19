package fr.unistra.pelican.util.morphology;

import java.awt.Point;

import fr.unistra.pelican.BooleanImage;

/** A structuring element for hit-and-miss. Each pixel may be
 * for the foreground, the background, or nothing.
 * @deprecated
 */
public class CompositeStructuringElement implements StructuringElement {

	private int rows; //nbre de lignes

	private int cols; //nbre de colonnes

	public static int FOREGROUND = 1;

	public static int BACKGROUND = 0;

	public static int UNDEFINED = -1;

	int[] values; //valeurs de l'element structurant

	Point center;

	public CompositeStructuringElement(CompositeStructuringElement e) {
		this.rows = e.getRows();
		this.cols = e.getColumns();
		values = new int[rows * cols];
		this.center = (Point) e.getCenter().clone();
	}

	public CompositeStructuringElement(int rows, int cols) {
		this.rows = rows;
		this.cols = cols;
		values = new int[rows * cols];
	}

	public CompositeStructuringElement(int rows, int cols, Point center) {
		this.rows = rows;
		this.cols = cols;
		this.center = (Point) center.clone();
		values = new int[rows * cols];
	}

	public CompositeStructuringElement(int rows, int cols, Point center,
			int[] values) {
		this(rows, cols, center);
		this.setValues(values);
	}

	public CompositeStructuringElement(BooleanImage foreground, BooleanImage background) {
		this(Math.max(foreground.getXDim()-foreground.getCenter().x, background.getXDim()-background.getCenter().x) - Math.min(-foreground.getCenter().x, -background.getCenter().x),
			Math.max(foreground.getYDim()-foreground.getCenter().y, background.getYDim()-background.getCenter().y) - Math.min(-foreground.getCenter().y, -background.getCenter().y),
		 new Point(Math.min(foreground.getCenter().x, background.getCenter().x),Math.min(foreground.getCenter().y, background.getCenter().y)));
		
		//TODO finir
		
	}
	
	public void setValues(int[] values) {
		if (this.values == null)
			this.values = (int[]) values.clone();
		else
			for (int i = 0; i < values.length; i++)
				this.values[i] = values[i];
	}

	public boolean isBackground(int col, int row) {
		return values[row * cols + col] == BACKGROUND;
	}

	public boolean isForeground(int col, int row) {
		return values[row * cols + col] == FOREGROUND;
	}

	public void setcenter(Point center) {
		this.center = center;
	}

	public BooleanImage getBackgroundStructuringElement() {
		BooleanImage se = new BooleanImage(this.rows,
				this.cols,1,1,1);
		se.setCenter(this.center);
		se.fill(false);

		for (int i = 0; i < rows * cols; i++)
			if (this.values[i] == this.BACKGROUND)
				se.setPixelBoolean(i, true);

		return se;
	}

	public BooleanImage getForegroundStructuringElement() {
		BooleanImage se = new BooleanImage(this.rows,
				this.cols,1,1,1);
		se.setCenter(this.center);
		se.fill(false);

		for (int i = 0; i < rows * cols; i++)
			if (this.values[i] == this.FOREGROUND)
				se.setPixelBoolean(i, true);

		return se;
	}
	
	public CompositeStructuringElement rotate(double degree)
	{
		double angleradian = Math.toRadians(degree);
		
		double xinput = this.cols;
		double yinput = this.rows;
		
		double tcos = Math.cos(-angleradian);
		double tsin = Math.sin(-angleradian);
		
		
		int xoutput = (int)Math.ceil(xinput*Math.abs(tcos)+yinput*Math.abs(tsin));
		int youtput = (int)Math.ceil(xinput*Math.abs(tsin)+yinput*Math.abs(tcos));
		
		CompositeStructuringElement cse = new CompositeStructuringElement(youtput, xoutput,new Point(xoutput/2, youtput / 2));
		
		BooleanImage seF = FlatStructuringElement2D.rotate(this.getForegroundStructuringElement(),degree);
		BooleanImage seB = FlatStructuringElement2D.rotate(this.getBackgroundStructuringElement(),degree);
		
		for(int x=0;x<xoutput;x++)
			for(int y=0;y<youtput;y++)
			{
				if(seF.getPixelXYBoolean(x,y))
				{
					cse.setValue(y, x, 1);
				}
				else if(seB.getPixelXYBoolean(x,y))
				{
					cse.setValue(y, x, 0);
				}
				else
				{
					cse.setValue(y, x, -1);
				}
					
			}
		
		return cse;
	}

	public Point getCenter() {
		return center;
	}

	public int getRows() {
		return this.rows;
	}

	public int getColumns() {
		return this.cols;
	}

	public void fill(int b) {
		java.util.Arrays.fill(values, b);
	}

	public void setValue(int row, int col, int b) {
		values[row * cols + col] = b;
	}

	public int getValue(int col, int row) {
		return values[row * cols + col];
	}
	
	public void print(){
		
		for(int i=0;i<this.cols;i++)
		{
			for(int j=0;j<this.rows;j++)
			{
				if(this.getValue(j, i)==0)
				{
					System.out.print("0 ");
				}
				else if(this.getValue(j, i)==1)
				{
					System.out.print("1 ");
				}
				else
				{
					System.out.print("  ");
				}
			}
			System.out.println();
		}
	}
	
}
	
