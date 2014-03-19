package fr.unistra.pelican.util;


/**
 * 
 * Class containing various static tools..
 * 
 *
 */
public class BasinTools
{
	private double hueA;
	private double hueB;
	
	private double lum;
	private double sat;
	
	/**
	 *
	 * This constructor exists with the sole purpose of making it possible
	 * to compute 
	 *
	 */
	public BasinTools()
	{
		hueA = 0.0; 
		hueB = 0.0;
		lum = 0.0;
		sat = 0.0;
	}
	
	public static final double epsilon = 10e-6;
	
	/**
	 * simple metric to be used during basin clustering
	 * 
	 * the descriptors are assumed to contain: avg luminance, 
	 * avg saturation and avg (weighted?) hue.
	 * 
	 * @param p1 descriptor of basin1
	 * @param p2 descriptor of basin2
	 * @param w component weights
	 * @return
	 */
	public static double distance(double[] p1,double[] p2,double[] w)
	{
		double esik = 0.11;
		
		double dist = 0.0;
		
		if(p1.length != p2.length){
			System.err.println("basinDistance: Incompatible vector lengths");
			return -1.0;
		}

		double hueDiff = Tools.hueDistance(p1[0],p2[0]) * 2.0 * w[0];

		double satDiff = Math.abs(p1[1] - p2[1]) * w[1];
		double lumDiff = Math.abs(p1[2] - p2[2]) * w[2];
		
		double coeff = 1 / ((1 + Math.exp(-10 * (p1[1] - 0.5))) * (1 + Math.exp(-10 * (p2[1] - 0.5))));
		
		// renkozu
		dist += hueDiff * hueDiff * coeff;
		//dist += hueDiff * hueDiff;
		
		// doygunluk
		dist += satDiff * satDiff;
		
		// aydinlik
		//dist += lumDiff * lumDiff * (1-coeff);
		dist += lumDiff * lumDiff;
		
		return Math.sqrt(dist);
	}

	/**
	 * simple metric to be used during basin clustering
	 * 
	 * the descriptors are assumed to contain: avg luminance, 
	 * avg saturation and avg (weighted?) hue.
	 * 
	 * @param p1 descriptor of basin1
	 * @param p2 descriptor of basin2
	 * @param width component weights
	 * @return
	 */
	public static double discreteDistance(double[] p1,double[] p2,double esik)
	{	
		
		double lumDiff =  Math.abs(p1[2]-p2[2]);
		double satDiff =  Math.abs(p1[1]-p2[1]);
		double hueDiff = hueDiffBoost(Tools.hueDistance(p1[0],p2[0]));
		/*
		// once bakalim, renk onemli mi.
		if (Math.min(p1[1],p2[1]) >= esik){
			// renk ozu
			//return Tools.hueDistance(p1[0],p2[0]) * 2.0 * 0.75 + Math.abs(p1[1] - p2[1]) * 0.25;
			
			return Math.sqrt((hueDiff * hueDiff + satDiff * satDiff * 0.5) / 1.5);
			
		}else if (Math.max(p1[1],p2[1]) <= esik){
			// aydinlik
			//return Math.abs(p1[2] - p2[2]) * 0.75 + Math.abs(p1[1] - p2[1]) * 0.25;
			
			return Math.sqrt(lumDiff * lumDiff);

		}else{// durum ortada
			// aydinlik
			//return Math.abs(p1[2] - p2[2]) * 0.75 + Math.abs(p1[1] - p2[1]) * 0.25;
			
			return Math.sqrt((lumDiff * lumDiff + 0.5 * satDiff * satDiff) / 1.5);
		}
		*/
		
		// \in [5,50]
		//double slope = -5 - 45 * (1 - esik);
		double slope = -10;
		
		double coeff = 1 / ((1 + Math.exp(slope * (p1[1] - esik))) * (1 + Math.exp(slope * (p2[1] - esik))));
		double coeffSat = (Math.exp(2 * satDiff) - 1)/(Math.exp(2 * 1.0) - 1.0);
		double coeffLum = (Math.exp(2 * lumDiff) - 1)/(Math.exp(2 * 1.0) - 1.0);
		
		return ((hueDiff * coeff + lumDiff * (1-coeff)) * (1-coeffSat) + coeffSat * satDiff) * (1-coeffLum) + coeffLum * lumDiff;
	}
	
	// tekrar ele alalim...olagan sartlar altinda, islev : [1/3;0.5] -> [1/3;1.0]
	// bunun icin de, islevi [0,2/3] arasi tasarlayip kicina 1/3 baglayalim
	public static double hueDiffBoost(double delta)
	{
		//if (delta <= 1/3) return delta;
		//else return 1/3 + (Math.exp(6 * (delta - 1/3)) - 1)/(Math.exp(1) - 1);
		if (delta <= 0.25) return Math.exp(4 * delta * Math.log(2)) - 1; 
		else return 1.0;
	}
	
	/**
	 * simple metric to be used during basin clustering with the RGB colour space
	 * 
	 * @param p1 descriptor of basin1
	 * @param p2 descriptor of basin2
	 * @param width component weights
	 * @return
	 */
	public static double RGBMaxDistance(double[] p1,double[] p2)
	{	
		double red = Math.abs(p1[0] - p2[0]);
		double green = Math.abs(p1[1] - p2[1]);
		double blue = Math.abs(p1[2] - p2[2]);
		
		//return Math.max(red,Math.max(green,blue));
		return Math.sqrt(red * red + green * green + blue * blue) / Math.sqrt(2.0);
	}
	
	/**
	 * basin descriptor sum..see basinDistance
	 * 
	 * @param p vector
	 */
	public void addUp(double[] p)
	{
		// renk ozu..bu asamada sadece ara toplamlari hazirla...
		// bolme icin cagirinca da degeri geri cevir
		// nasil olsa bu evrede.."d"nin ilk ogesi veya kalani hic kullanilmiyor
		hueA += p[1] * Math.cos(p[0] * 2.0 * Math.PI);
		hueB += p[1] * Math.sin(p[0] * 2.0 * Math.PI);
		
		// doygunluk
		sat += p[1];
		
		// aydinlik
		lum += p[2];
	}
	
	/**
	 * see basinDistance
	 * 
	 * @param s
	 * @return
	 */
	public double[] mean(double s)
	{	
		double[] ort = new double[3];

		ort[0] = Math.atan2(hueB,hueA);
		
		ort[0] = 0.5 * ort[0] / Math.PI;
		
		if (ort[0] < 0.0) ort[0] = 1.0 + ort[0];

		ort[1] = sat / s;
		ort[2] = lum / s;
		
		return ort;
	}
	
	public static double[] mean(double[] p1,double[] p2)
	{	
		double[] ort = new double[3];
		
		// renk ozu
		double A = p1[1] * Math.cos(p1[0] * 2.0 * Math.PI) + p2[1] * Math.cos(p2[0] * 2.0 * Math.PI);
		double B = p1[1] * Math.sin(p1[0] * 2.0 * Math.PI) + p2[1] * Math.sin(p2[0] * 2.0 * Math.PI);

		ort[0] = Math.atan2(B,A);
		
		ort[0] = 0.5 * ort[0] / Math.PI;
		
		if (ort[0] < 0.0) ort[0] = 1.0 + ort[0];

		// aydinlik ile doygunluk
		ort[1] = (p1[1] + p2[1]) / 2.0;
		ort[2] = (p1[2] + p2[2]) / 2.0;
		
		return ort;
	}
	
	public static double norm(double[] p,double[] w)
	{
		double norm = 0.0;
		
		for(int i = 0; i < p.length; i++)
			norm += p[i] * p[i] * w[i];
		
		return Math.sqrt(norm);
	}
	
	/**
	 * see distance
	 * 
	 * @param p1
	 * @param p2
	 * @return
	 */
	public static double[] difference(double[] p1,double[] p2)
	{
		if(p1.length != p2.length){
			System.err.println("Incompatible vector lengths");
			return null;
		}
		
		double[] fark = new double[p1.length];
		
		fark[2] = p1[2]-p2[2];
		fark[1] = p1[1]-p2[1];
		//fark[0] = p1[0]-p2[0];
		fark[0] = Tools.hueDistance(p1[0],p2[0]) * 2.0;
		
		return fark;
	}
}
