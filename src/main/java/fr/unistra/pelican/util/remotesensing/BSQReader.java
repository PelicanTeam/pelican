package fr.unistra.pelican.util.remotesensing;

import java.io.BufferedInputStream;
import java.io.DataInput;
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
 * Loads an image (in BSQ format) into an Image
 * @author Clément Hengy, Jonathan Weber
 */
public final class BSQReader extends BinReader {

	public BSQReader(HdrReader h, String p){
		this.hr = h;
		this.path = new File(p).getAbsoluteFile();
	}
	
	public Image getPelicanImage() throws Throwable{
		int xi, yi, bi, bytesNumber;
		File[] subFiles;
		byte[] tab;
		File binaryFile = null;
		String hdrPathRadical;
		FileInputStream fis;
		
		xi = hr.getCols();
		yi = hr.getLines();
		bi = hr.getBands();
		bytesNumber = hr.getBytesNumber();
		hdrPathRadical = this.path.getName().substring(0, this.path.getName().length()-4);
		subFiles = this.path.getParentFile().listFiles();
		
		for(int i = 0; i < subFiles.length && (null == binaryFile); ++i){
			String fileName = subFiles[i].getName();
			if(fileName.equals(hdrPathRadical) || fileName.equals(hdrPathRadical+".img") || fileName.endsWith(hdrPathRadical+".IMG") || fileName.endsWith(hdrPathRadical+".bsq") || fileName.endsWith(hdrPathRadical+".BSQ"))
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

		
		BufferedInputStream bif = new BufferedInputStream(fis);
		DataInput bf = null;
		if(!hr.getByteOrder())
			bf = DataInputFactory.createDataInputLittleEndian(fis);
		else
			bf = DataInputFactory.createDataInputBigEndian(fis);

		
		switch(hr.getDataType()){
			case 1:
				ByteImage imgcase1 = new ByteImage(xi, yi, 1, 1, bi);
				for(int b = 0; b < bi; ++b){
					for(int y = 0; y < yi; ++y){
						for(int x = 0; x < xi; ++x){
							imgcase1.setPixelXYBByte(x, y, b, bf.readByte());
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
							imgcase2.setPixelXYBInt(x, y, b, bf.readShort());
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
							imgcase3.setPixelXYBInt(x, y, b,  bf.readInt());
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
							imgcase4.setPixelXYBDouble(x, y, b, bf.readFloat());
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
							imgcase5.setPixelXYBDouble(x, y, b, bf.readDouble());
						}
					}
				}
				img=imgcase5;
				break;
			case 12:
				double _2pow11 = 1./Math.pow(2, 11);
				DoubleImage imgcase12 = new DoubleImage(xi, yi, 1, 1, bi);
				for(int b = 0; b < bi; ++b){
					for(int y = 0; y < yi; ++y){
						for(int x = 0; x < xi; ++x){
							// case 12 => pixel 16 bits mais seulement 11 bits de codé
							imgcase12.setPixelXYBDouble(x, y, b, bf.readShort()*_2pow11);
						}
					}
				}
				img=imgcase12;
				break;				
		}
					
		super.setProperties();
		
		return img;
	}

	@Override
	public Image getPelicanImage(int sx, int sy, int ex, int ey) throws Throwable {
		int xi, yi, bi, bytesNumber;
		File[] subFiles;
		byte[] tab;
		File binaryFile = null;
		String hdrPathRadical;
		FileInputStream fis;
		
		xi = hr.getCols();
		yi = hr.getLines();
		bi = hr.getBands();
		bytesNumber = hr.getBytesNumber();
		hdrPathRadical = this.path.getName().substring(0, this.path.getName().length()-4);
		subFiles = this.path.getParentFile().listFiles();
		
		for(int i = 0; i < subFiles.length && (null == binaryFile); ++i){
			String fileName = subFiles[i].getName();
			if(fileName.equals(hdrPathRadical) || fileName.equals(hdrPathRadical+".img") || fileName.endsWith(hdrPathRadical+".IMG") || fileName.endsWith(hdrPathRadical+".bsq") || fileName.endsWith(hdrPathRadical+".BSQ"))
				binaryFile = subFiles[i];
		}
		
		if(null == binaryFile){
			System.err.println("getPelicanImage() : Unable to find the associated binary file");
			return null;
		}
		
		try{
			fis = new FileInputStream(binaryFile);
		}
		catch(FileNotFoundException eex){
			System.err.println("getPelicanImage() : Unable to open the associated binary file");
			return null;
		}

		// buffer de 32Mo
		BufferedInputStream bif = new BufferedInputStream(fis, /*Math.min(bytesNumber,*/32*1024*1024/*)*/);
		DataInput bf = null;
		if(!hr.getByteOrder())
			bf = DataInputFactory.createDataInputLittleEndian(bif);
		else
			bf = DataInputFactory.createDataInputBigEndian(bif);

		
		switch(hr.getDataType()){
			case 1:
				ByteImage imgcase1 = new ByteImage(ex-sx+1, ey-sy+1, 1, 1, bi);
				for(int b = 0; b < bi; ++b){
					for(int y = 0; y < yi; ++y){
						for(int x = 0; x < xi; ++x){
							byte value = bf.readByte();
							if(x <= ex && x >= sx && y <= ey && y >= sy)
								imgcase1.setPixelXYBByte(x, y, b, value);
						}
					}
				}
				img=imgcase1;
				break;
			case 2:
				IntegerImage imgcase2 = new IntegerImage(ex-sx+1, ey-sy+1, 1, 1, bi);
				for(int b = 0; b < bi; ++b){
					for(int y = 0; y < yi; ++y){
						for(int x = 0; x < xi; ++x){
							short value = bf.readShort();
							if(x <= ex && x >= sx && y <= ey && y >= sy)
								imgcase2.setPixelXYBInt(x, y, b, value);
						}
					}
				}
				img=imgcase2;
				break;
			case 3:
				IntegerImage imgcase3 = new IntegerImage(ex-sx+1, ey-sy+1, 1, 1, bi);
				for(int b = 0; b < bi; ++b){
					for(int y = 0; y < yi; ++y){
						for(int x = 0; x < xi; ++x){
							int value = bf.readInt();
							if(x <= ex && x >= sx && y <= ey && y >= sy)
								imgcase3.setPixelXYBInt(x, y, b, value);
						}
					}
				}
				img=imgcase3;
				break;
			case 4:
				DoubleImage imgcase4 = new DoubleImage(ex-sx+1, ey-sy+1, 1, 1, bi);
				for(int b = 0; b < bi; ++b){
					for(int y = 0; y < yi; ++y){
						for(int x = 0; x < xi; ++x){
							float value = bf.readFloat();
							if(x <= ex && x >= sx && y <= ey && y >= sy)
								imgcase4.setPixelXYBDouble(x, y, b, value);
						}
					}
				}
				img=imgcase4;
				break;
			case 5:
				DoubleImage imgcase5 = new DoubleImage(ex-sx+1, ey-sy+1, 1, 1, bi);
				for(int b = 0; b < bi; ++b){
					for(int y = 0; y < yi; ++y){
						for(int x = 0; x < xi; ++x){
							double value = bf.readDouble();
							if(x <= ex && x >= sx && y <= ey && y >= sy)
								imgcase5.setPixelXYBDouble(x, y, b, value);
						}
					}
				}
				img=imgcase5;
				break;
			case 12:
				DoubleImage imgcase12 = new DoubleImage(ex-sx+1, ey-sy+1, 1, 1, bi);
				for(int b = 0; b < bi; ++b){
					for(int y = 0; y < yi; ++y){
						for(int x = 0; x < xi; ++x)	{		
							short value = bf.readShort();
							// case 12 => pixel 16 bits mais seulement 11 bits de codé
							if(x <= ex && x >= sx && y <= ey && y >= sy)
								imgcase12.setPixelXYBDouble(x-sx, y-sy, b, value/Math.pow(2, 11));
						}
					}
				}
				img=imgcase12;
				break;				
		}
					
		super.setProperties();
		
		return img;
	}
}
