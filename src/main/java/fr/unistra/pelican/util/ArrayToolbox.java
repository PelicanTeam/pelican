/**
 * 
 */
package fr.unistra.pelican.util;

import java.io.PrintStream;
import java.lang.reflect.Array;
import java.text.Format;
import java.util.ArrayList;

import org.jdom.Document;
import org.jdom.Element;

/**
 * Some useful function when working with tabs with unknown dimensions.
 * All arrays are supposed to be regular, i.e. all tabs in a given dimension have the same size
 * e.g. well regular tab
 * 	[[ 0 , 0 , 0 ],
 *   [ 0 , 0 , 0 ],
 *   [ 0 , 0 , 0 ]]
 *   
 *   not regular
 *  [[ 0 , 0 , 0 ],
 *   [ 0 , 0 ],
 *   [ 0 ]]
 *
 * 
 * @author Benjamin Perret
 *
 */
public abstract class ArrayToolbox {

	/**
	 * 
	 * @param o
	 * @return
	 */
	public static int [] size(Object o)
	{
		ArrayList<Integer> size = new ArrayList<Integer>();
		boolean flag=true;
		
		while(flag)
		{
			try 
			{
				size.add(Array.getLength(o));
				o=Array.get(o, 0);
			} catch (java.lang.IllegalArgumentException e)
			{
				flag=false;
			}
			
		}
		
		int [] s = new int[size.size()];
		int j=0;
		for(int i: size)
			s[j++]=i;
		
		return s;
	}
	
	
	private static Object getLastDim(Object o, int ... index)
	{
		for(int i=0;i<index.length-1;i++)
		{
			o=Array.get(o, index[i]);
		}
		
		return o;
	}
	
	public static double getDouble(Object o, int ... index )
	{
		o=getLastDim(o,index);
		return Array.getDouble(o, index[index.length-1]);
	}
	
	public static float getFloat(Object o, int ... index )
	{
		o=getLastDim(o,index);
		return Array.getFloat(o, index[index.length-1]);
	}
	
	public static long getLong(Object o, int ... index )
	{
		o=getLastDim(o,index);
		return Array.getLong(o, index[index.length-1]);
	}
	
	public static int getInt(Object o, int ... index )
	{
		o=getLastDim(o,index);
		return Array.getInt(o, index[index.length-1]);
	}
	
	public static short getShort(Object o, int ... index )
	{
		o=getLastDim(o,index);
		return Array.getShort(o, index[index.length-1]);
	}
	
	public static char getChar(Object o, int ... index )
	{
		o=getLastDim(o,index);
		return Array.getChar(o, index[index.length-1]);
	}
	
	public static boolean getBoolean(Object o, int ... index )
	{
		o=getLastDim(o,index);
		return Array.getBoolean(o, index[index.length-1]);
	}
	
	public static Object get(Object o, int ... index )
	{
		o=getLastDim(o,index);
		return Array.get(o, index[index.length-1]);
	}
	
	public static void setDouble(Object o,double v, int ... index )
	{
		o=getLastDim(o,index);
		Array.setDouble(o, index[index.length-1],v);
	}
	
	public static void setFloat(Object o,float v, int ... index )
	{
		o=getLastDim(o,index);
		Array.setFloat(o, index[index.length-1],v);
	}
	
	public static void setLong(Object o,long v, int ... index )
	{
		o=getLastDim(o,index);
		Array.setLong(o, index[index.length-1],v);
	}
	
	public static void setInt(Object o,int v, int ... index )
	{
		o=getLastDim(o,index);
		Array.setInt(o, index[index.length-1],v);
	}
	
	public static void setShort(Object o,short v, int ... index )
	{
		o=getLastDim(o,index);
		Array.setShort(o, index[index.length-1],v);
	}
	
	public static void setChar(Object o,char v, int ... index )
	{
		o=getLastDim(o,index);
		Array.setChar(o, index[index.length-1],v);
	}
	
	public static void setBoolean(Object o,boolean v, int ... index )
	{
		o=getLastDim(o,index);
		Array.setBoolean(o, index[index.length-1],v);
	}
	
	public static void set(Object o,Object v, int ... index )
	{
		o=getLastDim(o,index);
		Array.set(o, index[index.length-1],v);
	}
	
	public static String printString( Object o)
	{
		String op="";
		int [] size = size(o);
		if(size.length==0)
		{
			op+=("" + o);
		}
		else if (size.length==1)
		{
			op+=("[");
			for(int i=0;i<size[0];i++)
				op+=("" + Array.get(o,i) + ((i!=size[0]-1)?";":"]"));
			
		}
		else if(size.length==2)
		{
			for(int i=0;i<size[0];i++)
			{
				op+=printString(Array.get(o,i)) + "\n";
			}
			//op+="\n";
		}
		else {
			for(int i=0;i<size[0];i++)
			{
				op+=(i + "->");
				if(size.length==3)
					op+="\n";
				printString(Array.get(o,i));
				//ps.out.println();
			}
				
			
		}
		return op;
	}
	
	public static String printString( Object o,Format formater)
	{
		String op="";
		int [] size = size(o);
		if(size.length==0)
			op+=("" + o);
		else if (size.length==1)
		{
			op+=("[");
			for(int i=0;i<size[0];i++)
				op+=("" + formater.format(Array.get(o,i)) + ((i!=size[0]-1)?";":"]"));
			
		}
		else if(size.length==2)
		{
			for(int i=0;i<size[0];i++)
			{
				op+=printString(Array.get(o,i)) + "\n";
			}
			//op+="\n";
		}
		else {
			for(int i=0;i<size[0];i++)
			{
				op+=(i + "->");
				if(size.length==3)
					op+="\n";
				printString(Array.get(o,i));
				//ps.out.println();
			}
				
			
		}
		return op;
	}
	
	
	public static void println(PrintStream ps, Object o)
	
	{
		print(ps,o);
		ps.println();
	}
	
	public static void print(PrintStream ps, Object o)
	{
		int [] size = size(o);
		if(size.length==0)
			ps.println("" + o);
		else if (size.length==1)
		{
			ps.print("[");
			for(int i=0;i<size[0];i++)
				ps.print("" + Array.get(o,i) + ((i!=size[0]-1)?";":"]"));
			ps.println();
		}
		else if(size.length==2)
		{
			for(int i=0;i<size[0];i++)
				print(ps,Array.get(o,i));
			ps.println();
		}
		else {
			for(int i=0;i<size[0];i++)
			{
				ps.print(i + "->");
				if(size.length==3)
					ps.println();
				print(ps,Array.get(o,i));
				//ps.out.println();
			}
				
			
		}
		
	}
	
	public static <T> T mult(T a, double k)
	{
		T b;
		if(a instanceof double [])
		{
			b = (T)mult((double [])a,k);
		}
		else
		{
			int length=Array.getLength(a);
			if(length>0)
			{
			b =(T)Array.newInstance(Array.get(a, 0).getClass(), length);
			
			for(int i=0;i<length;i++)
			{
				Object t=mult(Array.get(a, i),k);
				Array.set(b, i, t);
			}
			} else b=a;
			
		}
		return b;
	}
	
	public static <T> T add(T a, double k)
	{
		T b;
		if(a instanceof double [])
		{
			b = (T)add((double [])a,k);
		}
		else
		{
			int length=Array.getLength(a);
			if(length>0)
			{
			b =(T)Array.newInstance(Array.get(a, 0).getClass(), length);
			
			for(int i=0;i<length;i++)
			{
				Object t=add(Array.get(a, i),k);
				Array.set(b, i, t);
			}
			} else b=a;
			
		}
		return b;
	}
	
	public static <T> T add(T a, T b)
	{
		T c;
		if(a instanceof double [])
		{
			c = (T)add((double [])a,(double [])b);
		}
		else
		{
			int length=Array.getLength(a);
			if(length>0)
			{
			c =(T)Array.newInstance(Array.get(a, 0).getClass(), length);
			
			for(int i=0;i<length;i++)
			{
				Object t=add(Array.get(a, i),Array.get(b, i));
				Array.set(c, i, t);
			}
			} else c=a;
			
		}
		return c;
	}
	
	
	public static void print( Object o)
	{
		print(System.out,o);
	}
	
	
	public static double [] add(double [] a, double [] b)
	{
		double [] r=a.clone();
		for(int i=0;i<a.length;i++)
			r[i]+=b[i];
		return r;
	}
	
	public static double [] log(double [] a)
	{
		double [] r=new double[a.length];
		for(int i=0;i<a.length;i++)
			r[i]=Math.log(a[i]);
		return r;
	}
	
	public static double [] sub(double [] a, double [] b)
	{
		double [] r=a.clone();
		for(int i=0;i<a.length;i++)
			r[i]-=b[i];
		return r;
	}
	
	public static double [] mult(double [] a, double [] b)
	{
		double [] r=a.clone();
		for(int i=0;i<a.length;i++)
			r[i]*=b[i];
		return r;
	}
	
	public static double [] mult(double [] a, double k)
	{
		double [] r=a.clone();
		for(int i=0;i<a.length;i++)
			r[i]*=k;
		return r;
	}
	
	public static void multNS(double [] a, double k)
	{
		for(int i=0;i<a.length;i++)
			a[i]*=k;
	}
	
	public static double [] add(double [] a, double k)
	{
		double [] r=a.clone();
		for(int i=0;i<a.length;i++)
			r[i]+=k;
		return r;
	}
	
	public static double [] div(double [] a, double [] b)
	{
		double [] r=a.clone();
		for(int i=0;i<a.length;i++)
			r[i]/=b[i];
		return r;
	}
	
	public static double cross(double [] a, double [] b)
	{
		double  c=0.0;
		for(int i=0;i<a.length;i++)
			c+=a[i]*b[i];
		
		return c;
	}
	

}
