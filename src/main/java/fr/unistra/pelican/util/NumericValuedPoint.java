package fr.unistra.pelican.util;

public class NumericValuedPoint extends Point implements Comparable<NumericValuedPoint>{
	
	private static final long serialVersionUID = 3324106333572556597L;
	private Number value=0;

	
	public NumericValuedPoint(int x, int y, Number value) {
		super(x, y);
		this.value=value;
		// TODO Auto-generated constructor stub
	}

	public NumericValuedPoint(java.awt.Point p, Number value) {
		super(p);
		this.value=value;
	}
	
	public Number getValue()
	{
		return value;
	}
	
	public void setValue(Number value)
	{
		this.value=value;
	}
	

	@Override
	public int compareTo(NumericValuedPoint o) {		
		if (value.doubleValue() == o.getValue().doubleValue())
			return 0;
		else if(value.doubleValue() < o.getValue().doubleValue())
			return -1;
		else
			return 1;
	}

}
