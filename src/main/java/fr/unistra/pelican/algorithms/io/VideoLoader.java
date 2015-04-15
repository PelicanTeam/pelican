package fr.unistra.pelican.algorithms.io;


import java.awt.image.BufferedImage;
import java.io.File;

import com.xuggle.mediatool.IMediaReader;
import com.xuggle.mediatool.MediaListenerAdapter;
import com.xuggle.mediatool.ToolFactory;
import com.xuggle.mediatool.event.IVideoPictureEvent;
import com.xuggle.xuggler.IContainer;
import com.xuggle.xuggler.IStream;
import com.xuggle.xuggler.IStreamCoder;
import com.xuggle.xuggler.ICodec;

import fr.unistra.pelican.Algorithm;
import fr.unistra.pelican.AlgorithmException;
import fr.unistra.pelican.ByteImage;
import fr.unistra.pelican.Image;
import fr.unistra.pelican.PelicanException;
import fr.unistra.pelican.algorithms.io.ImageLoader;
import fr.unistra.pelican.algorithms.visualisation.ViewerVideo;


/**
 * Video loader using Xuggle library 
 * It load the whole video uncompressed, it isn't memory-wise.
 * 
 * Possibility to reduce frame number by selecting one frame every ratio frames.
 * 
 * You can also select first frame and last frame
 * 
 * TODO : Improves efficiency of loading. Too slow for now.
 * TODO : Deal with timestamp ?
 * 
 * @author Jonathan Weber
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
		long t= System.currentTimeMillis();
		// Get info about the video (size and # of frames)
		IContainer container = IContainer.make();
		if (container.open(filename, IContainer.Type.READ, null) < 0)
			throw new PelicanException("Could not open file: " + filename);
		int numStreams = container.getNumStreams();
		int xDim=-1;
		int yDim=-1;
		int tDim=-1;
		for(int i = 0; i < numStreams; i++)
		{
			IStream stream = container.getStream(i);
			IStreamCoder coder = stream.getStreamCoder();
			if (coder.getCodecType() == ICodec.Type.CODEC_TYPE_VIDEO)
			{
				xDim = coder.getWidth();
				yDim = coder.getHeight();
				tDim = (int) stream.getNumFrames();
				System.out.println(new File(filename).getName()+" => Height: "+yDim+" | Width: "+xDim+" | # Frames: "+tDim+" || "+(xDim*yDim*tDim)+" pixels => "+(xDim*yDim*tDim*3)+" bytes");
				if(lastFrame==null)
					lastFrame=tDim-1;
				if(ratio!=1||firstFrame!=0||lastFrame!=tDim-1)
					System.out.println("With ratio="+ratio+";firstFrame="+firstFrame+";lastFrame="+lastFrame+" => # Frames: "+(((lastFrame-firstFrame+1)/ratio)+1)+" || "+(xDim*yDim*(((lastFrame-firstFrame+1)/ratio)+1))+" pixels => "+(xDim*yDim*(((lastFrame-firstFrame+1)/ratio)+1)*3)+" bytes");
				break;
			}
		}
		
		// Instantiate video output
		outputImage = new ByteImage(xDim,yDim, 1, ((lastFrame-firstFrame+1)/ratio)+1,3);
		
		// Read video and write it to outputImage via the listener
		IMediaReader reader = ToolFactory.makeReader(filename);
		reader.setBufferedImageTypeToGenerate(BufferedImage.TYPE_3BYTE_BGR);
		reader.addListener(new VideoListener());
		
		// Read the video file
		while (reader.readPacket() == null)
			do {} while(false);
		outputImage.setColor(true);
		System.out.println("Loaded in "+(System.currentTimeMillis()-t)+" ms."); 
	}
	
	private class VideoListener extends MediaListenerAdapter
	{

		int currentOutputFrame=0;
		int currentInputFrame=0;
		
		public VideoListener()
		{
			currentInputFrame = firstFrame.intValue();
		}
		
		
		public void onVideoPicture(IVideoPictureEvent event)
		{
			if(currentInputFrame>=firstFrame&&currentInputFrame<=lastFrame&&(currentInputFrame-firstFrame)%ratio==0)
			{
				Image tmp = ImageLoader.convertFromJAI(event.getImage(),false);
				outputImage.setImage4D(tmp, currentOutputFrame, Image.T);
				currentOutputFrame++;
			}
			currentInputFrame++;			
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
	
}
