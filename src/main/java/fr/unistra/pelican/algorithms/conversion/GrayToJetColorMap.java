/**
 * 
 */
package fr.unistra.pelican.algorithms.conversion;

import fr.unistra.pelican.Algorithm;
import fr.unistra.pelican.AlgorithmException;
import fr.unistra.pelican.Image;
import fr.unistra.pelican.algorithms.arithmetic.Inversion;
import fr.unistra.pelican.algorithms.io.ImageLoader;
import fr.unistra.pelican.algorithms.visualisation.MViewer;
import fr.unistra.pelican.util.Tools;

/**
 * @author Benjamin Perret
 *
 */
public class GrayToJetColorMap extends Algorithm {

	public Image inputImage;
	
	public boolean ignoreBackground=false;
	
	public Image outputImage;
	
	
	public GrayToJetColorMap(){
		super.inputs="inputImage";
		super.options="ignoreBackground";
		super.outputs="outputImage";
	}
	
	/* (non-Javadoc)
	 * @see fr.unistra.pelican.Algorithm#launch()
	 */
	@Override
	public void launch() throws AlgorithmException {
		outputImage=inputImage.newInstance(inputImage.xdim, inputImage.ydim, inputImage.zdim, inputImage.tdim, 3);
		
		double max=Double.NEGATIVE_INFINITY;
		double min=Double.POSITIVE_INFINITY;
		if(ignoreBackground)
		{
			for(int i=0;i<inputImage.size();i++)
			{
				double v=inputImage.getPixelDouble(i);
				if(!Tools.relativeDoubleEquality(v, 0.0))
				{
					max=Math.max(max, v);
					min=Math.min(min,v);
				}
			}
		}
		else{
			min=inputImage.minimumDouble();
			max=inputImage.maximumDouble();
		}
		
		double hmin=0;
		double hmax=360;
		
		
		System.out.println(min + "-" + max);
		for(int t=0;t<inputImage.tdim;t++)
			for(int z=0;z<inputImage.zdim;z++)
				for(int y=0;y<inputImage.ydim;y++)
					for(int x=0;x<inputImage.xdim;x++)
					{
						double v=inputImage.getPixelXYZTDouble(x, y, z, t);
						if(!ignoreBackground || !Tools.relativeDoubleEquality(v,0.0)){
							double h=(hmin+(hmax-hmin)*(v-min)/(max-min))/360.0;
						//	if(x%50==0)System.out.println(v +"-> " + (h*360));
							convertHSY(h,1.0,1.0);
							outputImage.setPixelXYZTBDouble(x, y, z, t, 0, r);
							outputImage.setPixelXYZTBDouble(x, y, z, t, 1, g);
							outputImage.setPixelXYZTBDouble(x, y, z, t, 2, b);
						}
					}
	}

	int r,g,b;
	
	private void convertHSV(double H, double S, double V)
	{
		double R, G, B;
		
		if (S >= 0.0 && S <= 0.0) { // doubles are tricky in
			// equality tests..
			R = G = B = V; // hue undefined

		} else if (V >= 0.0 && V <= 0.0) {
			R = G = B = 0.0; // hue and saturation undefined

		} else {
			H = H * 6; // H *= 360 / 60

			int i = (int) Math.floor(H);
			double f = H - i;
			double p = V * (1 - S);
			double q = V * (1 - S * f);
			double w = V * (1 - S * (1 - f));

			switch (i) {
			case 0:
				R = V;
				G = w;
				B = p;
				break;
			case 1:
				R = q;
				G = V;
				B = p;
				break;
			case 2:
				R = p;
				G = V;
				B = w;
				break;
			case 3:
				R = p;
				G = q;
				B = V;
				break;
			case 4:
				R = w;
				G = p;
				B = V;
				break;
			default:
				R = V;
			G = p;
			B = q;
			break;
			}
			
		}
		r=(int) Math.round(R * 255);
		g=(int) Math.round(G * 255);
		b=(int) Math.round(B * 255);
	}
	
	public  int[] convertHSY(double H, double S, double Y) {
		int[] rgb = new int[3];

		double R, G, B;
		R = G = B = 0.0;

		// H to radians
		H = H * Math.PI * 2.0;

		// chroma
		int k = (int) Math.floor(H / (Math.PI / 3.0));
		double Hstar = H - k * (Math.PI / 3.0);
		double C = (Math.sqrt(3.0) * S)
				/ (2.0 * Math.sin(2.0 * Math.PI / 3.0 - Hstar));

		double C1 = C * Math.cos(H);
		double C2 = -1 * C * Math.sin(H);

		R = 1.0 * Y + 0.701 * C1 + 0.27308667732669306 * C2;
		G = 1.0 * Y - 0.299 * C1 - 0.3042635918629327 * C2;
		B = 1.0 * Y - 0.299 * C1 + 0.8504369465163188 * C2;

		rgb[0] = (int) Math.round(R * 255);
		if (rgb[0] > 255)
			rgb[0] = 255;
		else if (rgb[0] < 0)
			rgb[0] = 0;

		rgb[1] = (int) Math.round(G * 255);
		if (rgb[1] > 255)
			rgb[1] = 255;
		else if (rgb[1] < 0)
			rgb[1] = 0;

		rgb[2] = (int) Math.round(B * 255);
		if (rgb[2] > 255)
			rgb[2] = 255;
		else if (rgb[2] < 0)
			rgb[2] = 0;

		r=rgb[0];
		g=rgb[1];
		b=rgb[2];
		return rgb;
	}
	
	public static <T extends Image> T exec(T inputImage){
		return (T)new GrayToJetColorMap().process(inputImage);
	}
	
	public static <T extends Image> T exec(T inputImage, boolean ignoreBackground){
		return (T)new GrayToJetColorMap().process(inputImage, ignoreBackground);
	}
	
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Image im=ImageLoader.exec("samples/blobs-ndg.png");
		im=Inversion.exec(im);
		Image res=GrayToJetColorMap.exec(im);
		MViewer.exec(im,res);

	}

}
