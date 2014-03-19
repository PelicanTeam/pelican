/**
 * 
 */
package fr.unistra.pelican.algorithms.io;

import java.io.IOException;

import fr.unistra.pelican.Algorithm;
import fr.unistra.pelican.AlgorithmException;
import fr.unistra.pelican.BooleanImage;
import fr.unistra.pelican.ByteImage;
import fr.unistra.pelican.Image;
import fr.unistra.pelican.IntegerImage;
import fr.unistra.pelican.util.jFits.Fits;
import fr.unistra.pelican.util.jFits.FitsException;
import fr.unistra.pelican.util.jFits.FitsFile;
import fr.unistra.pelican.util.jFits.FitsHDUnit;
import fr.unistra.pelican.util.jFits.FitsHeader;
import fr.unistra.pelican.util.jFits.FitsKeyword;
import fr.unistra.pelican.util.jFits.FitsMatrix;

/**
 * Save an image in fits file format.
 * Can handle x,y and b dim.
 * In case of multiband image, each band is saved in a standard fits image extension.
 * 
 * If a header is provided, its correctness is NOT verified.
 * If no header is provided, a suitable header is searched in image properties.
 * If no header is found at all, a minimal header is generated automatically.
 * 
 * 
 * @author Benjamin Perret
 *
 */
public class FitsImageSave extends Algorithm {

	/**
	 * File path, destination of saved image
	 */
	public String filename;
	
	/**
	 * Image to save
	 */
	public Image inputImage;
	
	/**
	 * Optional header
	 */
	public FitsHeader header;
	
	/**
	 * Force to use a special bit per pixel as specified in {@link Fits}
	 */
	public int bitPix=-1;
	/**
	 * 
	 */
	public FitsImageSave() {
		super.inputs="filename,inputImage";
		super.options="bitPix,header";
		super.outputs="";
	}

	/* (non-Javadoc)
	 * @see fr.unistra.pelican.Algorithm#launch()
	 */
	@Override
	public void launch() throws AlgorithmException {
		
		try{
			
			FitsHDUnit [] hdus=new FitsHDUnit[inputImage.bdim];
			
			for(int b=0;b<inputImage.bdim;b++)
			{
				FitsMatrix data=null;
				switch (bitPix)
				{
					case Fits.BYTE:
						data=getMatrixByte(inputImage,b);
						break;
					case Fits.SHORT:
						data=getMatrixShort(inputImage,b);
						break;
					case Fits.INT:
						data=getMatrixInt(inputImage,b);
						break;
					case Fits.FLOAT:
						data=getMatrixFloat(inputImage,b);
						break;
					case Fits.DOUBLE:
						data=getMatrixDouble(inputImage,b);
						break;
					default:
						if(inputImage instanceof BooleanImage || inputImage instanceof ByteImage)
							data=getMatrixByte(inputImage,b);
						else if(inputImage instanceof IntegerImage)
							data=getMatrixInt(inputImage,b);
						else data=getMatrixDouble(inputImage,b);
						
				}
			
				FitsHeader head;
				if(b==0)
				{
					if(header == null)
					{
						Object o = inputImage.properties.get(LoadFitsWithExtensions.HEADER_KEYWORD + b);
						if (o!= null  && o instanceof FitsHeader)
						{
							head=mergeHeader(data.getHeader(),(FitsHeader)o);
							//System.out.println("Head found in properties for band " +b);
						} else {
							//System.out.println("Headder generated for " +b + " " +o);
							head=data.getHeader();
						}
					} else {
						head=header;
					}
					
					if(inputImage.bdim!=1)
						 head.insertKeywordAt(new FitsKeyword("EXTEND", true,"File may contain standard extension"), 5);
					head.addKeyword(new FitsKeyword("COMMENT","Generated from PELICAN"));
				}
				else {
					
					if(header == null)
					{
						Object o = inputImage.properties.get(LoadFitsWithExtensions.HEADER_KEYWORD + b);
						if (o!= null  && o instanceof FitsHeader)
						{
							head=mergeHeader(data.getHeader(),(FitsHeader)o);
							//System.out.println("Head found in properties for band " +b);
						} else {
							//System.out.println("Headder generated for " +b + " " +o);
							head=data.getHeader();
						}
						
					} else {
						head=header;
					}
					
					head.setExtension(Fits.IMAGE);
				}
				if(b==0)
					filterHeader(head);
				hdus[b]=new FitsHDUnit(head,data);
				
			}
			
			
			
			FitsFile file = new FitsFile(); 
			for(int b=0;b<inputImage.bdim;b++)
				file.addHDUnit(hdus[b]);

			file.writeFile(filename);
			file.closeFile();}
		catch (FitsException e)
		{
			throw new AlgorithmException("Fits Save error: " +e);
		} catch (IOException e)
		{
			throw new AlgorithmException("Fits Save error: " +e);
		}
		

	}
	
	private FitsHeader filterHeader(FitsHeader header)
	{
		String [] remove={"PCOUNT","GCOUNT"};
		for(int i=0;i < header.getNoKeywords(); i++)
		{
			FitsKeyword kw= header.getKeyword(i);
			boolean flag=true;
			String name=kw.getName();
			for(int j=0;j<remove.length;j++)
				if(name.compareTo(remove[j])==0)
				{
					header.removeKeywordAt(i);
					i--;
					continue;
				}
		}
		return header;
	}
	
	private FitsHeader mergeHeader(FitsHeader minimalHeader, FitsHeader complete)
	{
		String [] forbidden={"NAXIS","BITPIX","SIMPLE","EXTEND","NAXIS1","NAXIS2","NAXIS3"};
		for(int i=0;i < complete.getNoKeywords(); i++)
		{
			FitsKeyword kw= complete.getKeyword(i);
			boolean flag=true;
			String name=kw.getName();
			for(int j=0;j<forbidden.length;j++)
				if(name.compareTo(forbidden[j])==0)
					flag=false;
			if(flag){
				minimalHeader.addKeyword(kw);
				//System.out.println("accept " + kw + " name : '"+kw.getName()+"'");
			}
		}
		return minimalHeader;
	}
	
	private FitsMatrix getMatrixShort(Image im, int b) throws FitsException, IOException
	{

		int xdim=im.xdim;
		int ydim=im.ydim;
		
		int nax[]={xdim,ydim}; // dimensions
		short dat [] =new short[xdim*ydim];  //donnï¿œes
		

		for(int j=0;j<ydim;j++) // conversion des donnees et mise sous forme lineaire
			for (int i=0;i<xdim;i++)
				dat[j*im.xdim + i]=(short)im.getPixelXYBInt(i, ydim-j-1, b);
		
		FitsMatrix data=new FitsMatrix(Fits.SHORT,nax); // fits matrix des bonnes dims
		data.setShortValues(0, dat); // remplissage sans offset
		return data;
			
	}
	
	private FitsMatrix getMatrixDouble(Image im, int b) throws FitsException, IOException
	{

		int xdim=im.xdim;
		int ydim=im.ydim;
		
		int nax[]={xdim,ydim}; // dimensions
		double dat [] =new double[xdim*ydim];  //donnï¿œes
		

		for(int j=0;j<ydim;j++) // conversion des donnees et mise sous forme lineaire
			for (int i=0;i<xdim;i++)
				dat[j*im.xdim + i]=im.getPixelXYBDouble(i, ydim-j-1, b);
		
		FitsMatrix data=new FitsMatrix(Fits.DOUBLE,nax); // fits matrix des bonnes dims
		data.setDoubleValues(0, dat); // remplissage sans offset
		return data;
			
	}
	
	private FitsMatrix getMatrixFloat(Image im, int b) throws FitsException, IOException
	{

		int xdim=im.xdim;
		int ydim=im.ydim;
		
		int nax[]={xdim,ydim}; // dimensions
		float dat [] =new float[xdim*ydim];  //donnï¿œes
		

		for(int j=0;j<ydim;j++) // conversion des donnees et mise sous forme lineaire
			for (int i=0;i<xdim;i++)
				dat[j*im.xdim + i]=(float)im.getPixelXYBDouble(i, ydim-j-1, b);
		
		FitsMatrix data=new FitsMatrix(Fits.FLOAT,nax); // fits matrix des bonnes dims
		data.setFloatValues(0, dat); // remplissage sans offset
		return data;
			
	}
	
	private FitsMatrix getMatrixByte(Image im, int b) throws FitsException, IOException
	{

		int xdim=im.xdim;
		int ydim=im.ydim;
		
		int nax[]={xdim,ydim}; // dimensions
		short dat [] =new short[xdim*ydim];  //donnees
		

			for(int j=0;j<ydim;j++) // conversion des donnees et mise sous forme lineaire
				for (int i=0;i<xdim;i++)
					dat[ j*im.xdim + i]=(short)im.getPixelXYBByte(i, ydim-j-1, b);
		
		FitsMatrix data=new FitsMatrix(Fits.SHORT,nax); // fits matrix des bonnes dims
		data.setShortValues(0, dat); // remplissage sans offset
		return data;
	}
	
	private FitsMatrix getMatrixInt(Image im, int b) throws FitsException, IOException
	{
	
		int xdim=im.xdim;
		int ydim=im.ydim;
		
		int nax[]={xdim,ydim}; // dimensions
		int dat [] =new int[xdim*ydim];  //donnees
		

		for(int j=0;j<ydim;j++) // conversion des donnees et mise sous forme lineaire
			for (int i=0;i<xdim;i++)
				dat[j*im.xdim + i]=(short)im.getPixelXYBInt(i, ydim-j-1, b);
		
		FitsMatrix data=new FitsMatrix(Fits.SHORT,nax); // fits matrix des bonnes dims
		data.setIntValues(0, dat); // remplissage sans offset
		return data;
		
		
		
	}
	
	public static void exec(String filename, Image inputImage)
	{
		(new FitsImageSave()).process(filename,inputImage); 
	}
	
	public static void exec(String filename, Image inputImage, int bitPix)
	{
		(new FitsImageSave()).process(filename,inputImage,bitPix); 
	}
	
	public static void exec(String filename, Image inputImage, FitsHeader header)
	{
		(new FitsImageSave()).process(filename,inputImage,-1,header) ;
	}
	
	public static void exec(String filename, Image inputImage, int bitPix,FitsHeader header)
	{
		(new FitsImageSave()).process(filename,inputImage,bitPix,header) ;
	}
	
	
	
}
