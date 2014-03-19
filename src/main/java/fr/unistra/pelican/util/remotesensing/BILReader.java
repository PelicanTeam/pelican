package fr.unistra.pelican.util.remotesensing;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import fr.unistra.pelican.ByteImage;
import fr.unistra.pelican.DoubleImage;
import fr.unistra.pelican.Image;
import fr.unistra.pelican.IntegerImage;


/**
 * Loads an image (in BIL format) into an Image
 * @author Clément Hengy
 */
public final class BILReader extends BinReader {

	public BILReader(HdrReader h, String p){
		this.hr = h;
		this.path = new File(p).getAbsoluteFile();
	}
	
	public Image getPelicanImage() {
		int xi, yi, bi, bytesNumber, blockLenght;
		File[] subFiles;
		byte[] tab;
		File binaryFile = null;
		String hdrPathRadical;
		FileInputStream fis;
		ByteBuffer bf;
		
		xi = hr.getCols();
		yi = hr.getLines();
		bi = hr.getBands();
		bytesNumber = hr.getBytesNumber();
		blockLenght = xi*bi;
		hdrPathRadical = this.path.getName().substring(0, this.path.getName().length()-4);
		subFiles = this.path.getParentFile().listFiles();
		
		for(int i = 0; i < subFiles.length && (null == binaryFile); ++i){
			String fileName = subFiles[i].getName();
			if(fileName.equals(hdrPathRadical) || fileName.equals(hdrPathRadical+".img") || fileName.endsWith(hdrPathRadical+".IMG") || fileName.endsWith(hdrPathRadical+".bil") || fileName.endsWith(hdrPathRadical+".BIL"))
				binaryFile = subFiles[i];
		}
		
		if(null == binaryFile){
			System.err.println("getPelicanImage() : Unable to find the associated binary file");
			return null;
		}
		
		try{
			fis = new FileInputStream(binaryFile);
		}
		catch(FileNotFoundException ex){
			System.err.println("getPelicanImage() : Unable to open the associated binary file");
			return null;
		}
		
		tab = new byte[xi*yi*bi*bytesNumber];
		
		
		try{
			if(fis.read(tab) != tab.length){
				System.err.println("getPelicanImage() : Error while reading file, unexpected length");
				return null;
			}
		}
		catch(IOException ex){
			System.err.println("getPelicanImage() : I/O error");
			return null;
		}
		
		bf = ByteBuffer.wrap(tab);
		
		if(!hr.getByteOrder())
			bf.order(ByteOrder.LITTLE_ENDIAN);
		
		switch(hr.getDataType()){
			case 1:
				ByteImage imgcase1 = new ByteImage(xi, yi, 1, 1, bi);
				for(int b = 0; b < bi; ++b){
					for(int y = 0; y < yi; ++y){
						for(int x = 0; x < xi; ++x){
							byte value = bf.get((b*xi + y*blockLenght + x)*bytesNumber);
							imgcase1.setPixelXYBByte(x, y, b, value);
						}
					}
				}
				img=imgcase1;
				break;
			case 2:		
				IntegerImage imgcase2 = new IntegerImage(xi, yi, 1, 1, bi);
				for(int b = 0; b < bi; ++b){
					for(int y = 0; y < yi; ++y){
						for(int x = 0; x < xi; ++x){
							short value = bf.getShort((b*xi + y*blockLenght + x)*bytesNumber);
							imgcase2.setPixelXYBInt(x, y, b, value);
						}
					}
				}
				img=imgcase2;
				break;
			case 3:
				IntegerImage imgcase3 = new IntegerImage(xi, yi, 1, 1, bi);
				for(int b = 0; b < bi; ++b){
					for(int y = 0; y < yi; ++y){
						for(int x = 0; x < xi; ++x){
							int value = bf.getInt((b*xi + y*blockLenght + x)*bytesNumber);
							imgcase3.setPixelXYBInt(x, y, b, value);
						}
					}
				}
				img=imgcase3;
				break;
			case 4:
				DoubleImage imgcase4 = new DoubleImage(xi, yi, 1, 1, bi);
				for(int b = 0; b < bi; ++b){
					for(int y = 0; y < yi; ++y){
						for(int x = 0; x < xi; ++x){
							float value = bf.getFloat((b*xi + y*blockLenght + x)*bytesNumber);
							imgcase4.setPixelXYBDouble(x, y, b, value);
						}
					}
				}
				img=imgcase4;
				break;
			case 5:
				DoubleImage imgcase5 = new DoubleImage(xi, yi, 1, 1, bi);
				for(int b = 0; b < bi; ++b){
					for(int y = 0; y < yi; ++y){
						for(int x = 0; x < xi; ++x){
							double value = bf.getDouble((b*xi + y*blockLenght + x)*bytesNumber);
							imgcase5.setPixelXYBDouble(x, y, b, value);
						}
					}
				}
				img=imgcase5;
				break;
			case 12:
				IntegerImage imgcase12 = new IntegerImage(xi, yi, 1, 1, bi);
				for(int b = 0; b < bi; ++b){
					for(int y = 0; y < yi; ++y){
						for(int x = 0; x < xi; ++x){
							char value = bf.getChar((b*xi + y*blockLenght + x)*bytesNumber);
							imgcase12.setPixelXYBInt(x, y, b, value);
						}
					}
				}
				img=imgcase12;
				break;
		}
		
		super.setProperties();
					
		return img;
	}

	public Image getPelicanImage(int sx, int sy, int ex, int ey) {
		throw new RuntimeException("Not implemented");
	}

}
