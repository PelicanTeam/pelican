/**
 * 
 */
package fr.unistra.pelican.algorithms.io;

import java.io.File;
import java.io.IOException;

import fr.unistra.pelican.Algorithm;
import fr.unistra.pelican.AlgorithmException;
import fr.unistra.pelican.DoubleImage;
import fr.unistra.pelican.algorithms.visualisation.MViewer;
import fr.unistra.pelican.gui.MultiViews.MultiView;
import fr.unistra.pelican.util.jFits.Fits;
import fr.unistra.pelican.util.jFits.FitsException;
import fr.unistra.pelican.util.jFits.FitsFile;
import fr.unistra.pelican.util.jFits.FitsHDUnit;
import fr.unistra.pelican.util.jFits.FitsHeader;
import fr.unistra.pelican.util.jFits.FitsMatrix;

/**
 * 
 * Read a fits file with possible  extensions, each image extension correspond to a band in the final image.
 * Image extensions must have 2 dims all equals.
 * 
 * Result is always in double precision
 * 
 * @author Benjamin Perret
 *
 */
public class LoadFitsWithExtensions extends Algorithm {

	public static final String HEADER_KEYWORD= "FITS_HEADER_";
	
	/**
	 * Path to fits file
	 */
	public String filename;
	
	/**
	 * Result
	 */
	public DoubleImage outputImage;
	
	private boolean debug=false;
	
	public LoadFitsWithExtensions(){
		super.inputs="filename";
		super.outputs="outputImage";
	}
	
	/* (non-Javadoc)
	 * @see fr.unistra.pelican.Algorithm#launch()
	 */
	@Override
	public void launch() throws AlgorithmException {
		File f= new File(filename);
		try {
			FitsFile ff = new FitsFile(f);
			int nbHdu=0;
			if(debug)
			{
				System.out.println("Fits Reader: " + ff.getNoHDUnits() +" data units found");
			}
			for(int i=0;i<ff.getNoHDUnits();i++)
			{
				if(ff.getHDUnit(i).getType() == Fits.IMAGE)
					nbHdu++;
				else{
					System.out.println("Fits Reader: Skipping HDU " +i + " unmanaged data type: " + ff.getHDUnit(i).getType());
				}
			}
				
			
			int dimx=0;
			int dimy=0;
			int b=0;
			for(int i=0;i<ff.getNoHDUnits();i++)
				if(ff.getHDUnit(i).getType() == Fits.IMAGE)
				{
					FitsHDUnit hdu =ff.getHDUnit(i);
					FitsHeader header=hdu.getHeader();
					FitsMatrix matrix=(FitsMatrix)hdu.getData();
					if(outputImage==null)
					{
						int [] naxis =matrix.getNaxis();
						if(naxis.length != 2)
						{
							boolean flag=true; // some fits image have more axis but with only one element per axis so we can just ignore them
							for(int bi=2;flag && bi<naxis.length;bi++)
								if(naxis[bi]!=1)
									flag=false;
							if(!flag)
							throw new AlgorithmException("Fits reading exception, image in data unit " + i +" must have two dimensions! " +filename);
						}
						dimx=naxis[0];
						dimy=naxis[1];
						outputImage=new DoubleImage(dimx,dimy,1,1,nbHdu);
					} else {
						int [] naxis =matrix.getNaxis();
						if(naxis.length != 2)
						{
							System.out.println("Fits HDU " +i + " ignored :  incorrect number of dimensions");
								continue;
							//throw new AlgorithmException("Fits reading exception, image in data unit " + i +" must have two dimensions! " +filename);
						}
						if(dimx!=naxis[0] || dimy!=naxis[1])
						{
							System.out.println("Fits HDU " +i + " ignored :  incorrect dimension sizes");
							continue;
						}
							//throw new AlgorithmException("Fits reading excpetion, all image extensions must have same dimensions! " +filename);
					}
					
					double [] tmp = new double[dimx];
					outputImage.properties.put(HEADER_KEYWORD + b, header);
					for(int y=0;y<dimy;y++)
					{
						tmp = matrix.getDoubleValues(y*dimx, dimx, tmp);
						for(int x=0;x<dimx;x++)
						{
							
							outputImage.setPixelXYBDouble(x, dimy-y-1, b, tmp[x]);
						}
					}
					b++;
				}
			ff.closeFile();
			
		} catch (IOException e) {
			throw new AlgorithmException("IO exception: ",e);
		} catch (FitsException e) {
			throw new AlgorithmException("Fits File exception: ",e);
		}
		

	}
	
	public static DoubleImage exec(String filename)  throws AlgorithmException
	{
		return (DoubleImage)(new LoadFitsWithExtensions()).process(filename);
	}
	/*
	public static void main(String [] args)
	{
		DoubleImage im= LoadFitsWithExtensions.exec("D:\\perret\\Articles\\2009 - SCIA\\fig\\pgc2182-3b-ori.fits");
		DoubleImage im0 = (DoubleImage)im.getImage2D(0, 0, 0);
		DoubleImage im1 = (DoubleImage)im.getImage2D(0, 0, 1);
		DoubleImage im2 = (DoubleImage)im.getImage2D(0, 0, 2);
		
		DoubleImage imr= LoadFitsWithExtensions.exec("D:\\perret\\Articles\\2009 - SCIA\\fig\\pgc2182-3b-residual.fits");
		DoubleImage imr0 = (DoubleImage)imr.getImage2D(0, 0, 0);
		DoubleImage imr1 = (DoubleImage)imr.getImage2D(0, 0, 1);
		DoubleImage imr2 = (DoubleImage)imr.getImage2D(0, 0, 2);
		
		DoubleImage ims= LoadFitsWithExtensions.exec("D:\\perret\\Articles\\2009 - SCIA\\fig\\pgc2182-3b-simu.fits");
		DoubleImage ims0 = (DoubleImage)ims.getImage2D(0, 0, 0);
		DoubleImage ims1 = (DoubleImage)ims.getImage2D(0, 0, 1);
		DoubleImage ims2 = (DoubleImage)ims.getImage2D(0, 0, 2);
		MultiView mv = MViewer.exec(im0);
		mv.add(im1);
		mv.add(im2);
		
		mv.add(imr0);
		mv.add(imr1);
		mv.add(imr2);
		
		mv.add(ims0);
		mv.add(ims1);
		mv.add(ims2);
		
		double vo = im.volume();
		double vr = imr.volume();
		double p=im.size();
		System.out.println("vo " + (vo/p) + "   vr " + (vr/p));
	}*/

}
