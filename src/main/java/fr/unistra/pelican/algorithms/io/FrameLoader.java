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
 * Experimental Frame loader (works only the AVI format, for further detail
 * please contact the author).
 * It loads only the frames given in the ArrayList selectedFrames
 * @author Erhan Aptoula, Jonathan Weber,Vincent Danner
 */

public class FrameLoader extends Algorithm {

	/**
	 * Input parameter.
	 */
	public String filename;
	
	/**
	 * List of the frames to be loaded
	 */
	public ArrayList<Integer> selectedFrames=new ArrayList<Integer>();
	

	/**
	 * Output parameter.
	 */
	public ByteImage outputImage;
	

	/**
	 * Constructor
	 * 
	 */
	public FrameLoader() {

		super();
		super.inputs = "filename,selectedFrames";
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
				
				Integer framesUsed=selectedFrames.size();
		
				for (int i = 0; i <framesUsed; i++) {
					Integer currentFrame = selectedFrames.get(i);
					if(currentFrame>=totalFrames||currentFrame<0) throw new AlgorithmException("there is no frame n° "+currentFrame+ " in this video" );
					// move to a particular frame
					fpc.seek(currentFrame+1);
		
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
					if(outputImage.getBDim()==3)
						outputImage.setColor(true);
				}
			}finally{
				p.close();
			}
		} catch (NoPlayerException e) {
			throw new AlgorithmException("Unable to read file "+this.filename);
		} catch (CannotRealizeException e){
			throw new AlgorithmException("Unable to read file "+this.filename);
		} catch (IOException e) {
			throw new AlgorithmException("Unable to read file "+this.filename);
		}
	}

	/**
	 * Experimental frame loader.
	 * 
	 * @param filename
	 *            Directory of the video
	 * @param selectedFrames
	 *            ArrayList of the frames that must be loaded
	 * @return the loaded frames as a video
	 */
	public static ByteImage exec(String filename,ArrayList<Integer> selectedFrames) {
		return (ByteImage) new FrameLoader().process(filename, selectedFrames);
	}
	
}

