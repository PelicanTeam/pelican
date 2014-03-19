package fr.unistra.pelican.algorithms.io;

import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;

import javax.media.Buffer;
import javax.media.CannotRealizeException;
import javax.media.Duration;
import javax.media.Manager;
import javax.media.NoPlayerException;
import javax.media.Player;
import javax.media.Time;
import javax.media.control.FrameGrabbingControl;
import javax.media.control.FramePositioningControl;
import javax.media.format.VideoFormat;
import javax.media.util.BufferToImage;

import fr.unistra.pelican.Algorithm;
import fr.unistra.pelican.AlgorithmException;
import fr.unistra.pelican.ByteImage;

/**
 * Experimental video loader (works only the AVI format, for further detail
 * please contact the author). 
 * It allows to choice a ratio of frame which will be discarded and to select an interval for the selection or to load some frames listed in an ArrayList.
 * 
 * @author Erhan Aptoula, Jonathan Weber, Vincent Danner
 */

public class VideoLoader extends Algorithm {

	/**
	 * Input image.
	 */
	public String filename;

	/**
	 * Output image.
	 */
	public ByteImage outputImage;
	
	//optional parameters	
	/**
	 * Ratio of frames
	 */
	public Integer ratio = 1;
	
	/**
	 * Beginning of the selection
	 */
	public Integer firstFrame = 0;
	
	/**
	 * End of the selection
	 */
	public Integer lastFrame = null;


	/**
	 * Constructor
	 * 
	 */
	public VideoLoader() {

		super();
		super.inputs = "filename";
		super.options= "ratio,firstFrame,lastFrame";
		super.outputs = "outputImage";
		
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.unistra.pelican.Algorithm#launch()
	 */
	public void launch() throws AlgorithmException {
		// create a movie player, in a 'realized' state
		Player p = null;
		int totalFrames = 0;
		int bands = 1;
		
		try{
			URL url = new URL("file:" + filename);			
			try {				
				p = Manager.createRealizedPlayer(url);
				
				// create a frame positioner
				FramePositioningControl fpc = (FramePositioningControl) p
						.getControl("javax.media.control.FramePositioningControl");
		
				// create a frame grabber
				FrameGrabbingControl fg = (FrameGrabbingControl) p
						.getControl("javax.media.control.FrameGrabbingControl");
		
				// request that the player changes to a 'prefetched' state
				p.prefetch();
		
				// wait until the player is in that state...
		
				Time duration = p.getDuration();
				if (duration != Duration.DURATION_UNKNOWN)
					totalFrames = fpc.mapTimeToFrame(duration);
				else
					throw new AlgorithmException("Duration unknown");
					
				// check if the arguments are valid ones
				if(ratio<=0) throw new AlgorithmException("The ratio must be a positive integer");
				if(firstFrame<0||firstFrame>=totalFrames) throw new AlgorithmException("The firstFrame must be choosen within the frames of the input (between 0 and "+(totalFrames-1)+")");
				if(lastFrame==null){ lastFrame = totalFrames-1;}
				if(lastFrame >=totalFrames ||lastFrame<firstFrame) throw new AlgorithmException("The lastFrame must be choosen within the frames of the input and after the firstFrame (last frame : "+(totalFrames-1)+")");
				
				Integer framesUsed = (lastFrame-firstFrame+ratio)/ratio;
				
				for (int i = 0; i <framesUsed; i++) {
					// move to a particular frame
					fpc.seek(i*ratio+firstFrame+1);
		
					// take a snap of the current frame
					Buffer buf = fg.grabFrame();
		
					// get its video format details
					VideoFormat vf = (VideoFormat) buf.getFormat();
		
					// initialize BufferToImage with video format
					BufferToImage bufferToImage = new BufferToImage(vf);
		
					// convert the buffer to an image
					BufferedImage bim = (BufferedImage) bufferToImage.createImage(buf);
		
					int width = bim.getWidth(null);
					int height = bim.getHeight(null);
		
					if (outputImage == null) {
						switch (bim.getType()) {
						case BufferedImage.TYPE_BYTE_GRAY:
						case BufferedImage.TYPE_USHORT_GRAY:
							bands = 1;
							break;
						case BufferedImage.TYPE_INT_RGB:
						case BufferedImage.TYPE_INT_BGR:
							bands = 3;
							break;
						default:
							throw new AlgorithmException(
									"Unsupported pixel organization");
						}
						outputImage = new ByteImage(width,height,1,framesUsed,bands);
						if(outputImage.getBDim()==3) outputImage.setColor(true);
					}
		
					WritableRaster r = bim.getRaster();
					int loc=(i)*outputImage.getBDim()*outputImage.getXDim()*outputImage.getYDim();
					for (int y = 0; y < height; y++)
						for (int x = 0; x < width; x++)
							for (int b = 0; b < bands; b++)
							{
								outputImage.setPixelByte(loc,(byte)r.getSample(x, y, b));
								loc++;
								//outputImage.setPixelXYZTBByte(x,y,0,i,b,(byte)r.getSample(x, y, b));
							}						
				}			
			}finally{
				p.close();
			}
		} catch (NoPlayerException e) {
			throw new AlgorithmException("Unable to read file "+ this.filename);
		} catch (CannotRealizeException e) {
			throw new AlgorithmException("Unable to read file "+ this.filename);
		} catch (IOException e) {
			throw new AlgorithmException("Unable to read file "+ this.filename);
		}
	}

	/**
	 * Experimental video loader.
	 * 
	 * @param filename
	 *            Directory of the video
	 * @return the loaded video
	 */
	public static ByteImage exec(String filename) {
		return (ByteImage) new VideoLoader().process(filename);
	}
	
	/**
	 * Experimental video loader.
	 * 
	 * @param filename
	 *            Directory of the video
	 * @param ratio
	 * 			  number of frames divided by the number of selected frames
	 * @return the loaded video
	 */
	public static ByteImage exec(String filename,Integer ratio) {
		return (ByteImage) new VideoLoader().process(filename,ratio);
	}

	/**
	 * Experimental video loader.
	 * 
	 * @param filename
	 *            Directory of the video
	 * @param ratio
	 * 			  number of frames divided by the number of selected frames
	 * @param firstFrame 
	 * 			  number of the first frame which will be selected
	 * @return the loaded video
	 */
	public static ByteImage exec(String filename,Integer ratio,Integer firstFrame) {
		return (ByteImage) new VideoLoader().process(filename,ratio,firstFrame);
	}
	
	/**
	 * Experimental video loader.
	 * 
	 * @param filename
	 *            Directory of the video
	 * @param ratio
	 * 			  number of frames divided by the number of selected frames
	 * @param firstFrame 
	 * 			  number of the first frame which will be selected
	 * @param lastFrame
	 * 			  number of the last frame of the selection
	 * @return the loaded video
	 */
	public static ByteImage exec(String filename,Integer ratio,Integer firstFrame,Integer lastFrame) {
		return (ByteImage) new VideoLoader().process(filename,ratio,firstFrame,lastFrame);
	}
	
	/**
	 * Experimental video loader.
	 * 
	 * @param filename
	 *            Directory of the video
	 * @param frameNumbers
	 * 			  ArrayList which contains the index of the frames selected
	 * @return the loaded video
	 */
	public static ByteImage exec(String filename,ArrayList<Integer> frameNumbers) {
		return (ByteImage) new FrameLoader().process(filename,frameNumbers);
	}
}
