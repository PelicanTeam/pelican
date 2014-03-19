package fr.unistra.pelican;

import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.IOException;
import java.io.NotSerializableException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.URL;
import java.util.HashMap;

import javax.media.Buffer;
import javax.media.Duration;
import javax.media.Manager;
import javax.media.Player;
import javax.media.Time;
import javax.media.control.FrameGrabbingControl;
import javax.media.control.FramePositioningControl;
import javax.media.format.VideoFormat;
import javax.media.util.BufferToImage;

import fr.unistra.pelican.util.largeImages.ByteUnit;
import fr.unistra.pelican.util.largeImages.LargeImageInterface;
import fr.unistra.pelican.util.largeImages.LargeImageMemoryManager;
import fr.unistra.pelican.util.largeImages.LargeImageUtil;
import fr.unistra.pelican.util.largeImages.Unit;

/**
 * JMFVideos are used to open and read from videos supported by JMF (yes, such
 * video exists ...)
 */
public class JMFVideo extends LargeByteImage implements LargeImageInterface {

	/**
	 * Serial
	 */
	private static final long serialVersionUID = -7585583132719664934L;

	/**
	 * Player used to read the video
	 */
	private transient Player p;

	/**
	 * BufferedImage to access to the frames of the video
	 */
	private transient BufferedImage bim;

	/**
	 * VideoFormat
	 */
	private VideoFormat vf;

	/**
	 * Frame positioner
	 */
	private transient FramePositioningControl fpc;

	/**
	 * Frame grabber
	 */
	private transient FrameGrabbingControl fg;

	/**
	 * BufferToImage
	 */
	private transient BufferToImage bufferToImage;

	/**
	 * Buffer
	 */
	private transient Buffer buf;

	/**
	 * Number of pixels in each frames
	 */
	public int frameSize;

	/**
	 * Constructs a new JMF Video with the file at the given address.
	 * 
	 * @param filename
	 *            path to the Video
	 */
	public JMFVideo(String filename) {
		this(filename, 0);
	}

	/**
	 * Constructs a new JMF Video with the file at the given address and with
	 * the specified unitSize.
	 * 
	 * @param filename
	 *            path to the Video
	 * @param unitSize
	 *            Max unit size in bytes
	 */
	public JMFVideo(String filename, int unitSize) {
		super();
		this.setFile(new File(filename));

		int bands;
		initializePlayer();

		// convert the buffer to an image
		bim = (BufferedImage) bufferToImage.createImage(buf);

		int width = bim.getWidth(null);
		int height = bim.getHeight(null);

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
			throw new AlgorithmException("Unsupported pixel organization");
		}

		if (bands == 3) {
			this.setColor(true);
		}

		this.setDim(width, height, 1, this.getTDim(), bands);
		this.setUnitLength(-1);
		this.computeUnitSize(unitSize);
		this.calculate();
	}

	@Override
	public void calculate() {
		this.frameSize = this.getXDim() * this.getYDim() * this.getBDim();
		this.computeUnitDim();
		this.setSize((long) this.getXDim() * (long) this.getYDim()
				* (long) this.getZDim() * (long) this.getTDim()
				* (long) this.getBDim());
	}

	@Override
	public void createFile() {
		throw new PelicanException("A JMFVideo should not create a file");
	}

	@Override
	public void setUnit(Unit currentUnit, int currentId, boolean modified) {
		if (modified) {
			throw new PelicanException("A JMFVideo should not be modified");
		}
		LargeImageUtil.setUnit(this, currentUnit, currentId, modified);
	}

	@Override
	public ByteUnit loadAnUnit(int id) {

		if (p == null) {
			this.initializePlayer();
		}

		ByteUnit currentUnit = new ByteUnit(this.getUnitSize());
		if (id >= this.getUnitDim()) {
			throw new AlgorithmException("there is no " + id
					+ "th unit in this video");
		}

		byte[] newPixels = new byte[this.getUnitSize()];

		int firstFrame = (int) ((((long) id) << this.getUnitPowerSize()) / ((long) this.frameSize));
		int lastFrame = (int) ((((long) (id + 1)) << this.getUnitPowerSize()) / ((long) this.frameSize));
		int nbFrame = lastFrame - firstFrame + 1;
		int firstPixelInFirstFrame = (int) ((((long) id) << this
				.getUnitPowerSize()) % ((long) this.frameSize));
		int arrayOffset = -firstPixelInFirstFrame;

		for (int i = 0; i < nbFrame; i++) {
			fpc.seek(i + firstFrame + 1);
			buf = fg.grabFrame();
			bim = (BufferedImage) bufferToImage.createImage(buf);
			WritableRaster r = bim.getRaster();

			for (int y = 0; y < this.getYDim(); y++) {
				for (int x = 0; x < this.getXDim(); x++) {
					for (int b = 0; b < this.getBDim(); b++) {
						int pos = arrayOffset
								+ b
								+ this.getBDim()
								* (x + this.getXDim()
										* (y + this.getYDim() * (i)));
						if ((pos >= 0) && (pos < newPixels.length)) {
							newPixels[arrayOffset
									+ b
									+ this.getBDim()
									* (x + this.getXDim()
											* (y + this.getYDim() * (i)))] = (byte) ((byte) r
									.getSample(x, y, b) + Byte.MIN_VALUE);
						}
					}
				}
			}
		}

		currentUnit.setPixels(newPixels);
		this.setUnit(currentUnit, id, false);

		return currentUnit;
	}

	@Override
	public void fillFile() {
		throw new PelicanException("A JMFVideo should not be modified");
	}

	@Override
	public void close() {
		p.close();
	}

	@Override
	public long getUnitLength() {
		if (this.unitLength == -1) {
			this.computeUnitLength();
		}
		return this.unitLength;
	}

	@Override
	public String getWorkingFileSuffix() {
		return ".avi";
	}

	/**
	 * Initialize the player so you can read in the file.
	 */
	private void initializePlayer() {
		p = null;
		try {
			URL url = new URL("file:" + this.getFile().getAbsolutePath());
			p = Manager.createRealizedPlayer(url);
		} catch (Exception e) {
			e.printStackTrace();
		}

		// create a frame positioner
		fpc = (FramePositioningControl) p
				.getControl("javax.media.control.FramePositioningControl");

		// create a frame grabber
		fg = (FrameGrabbingControl) p
				.getControl("javax.media.control.FrameGrabbingControl");

		// request that the player changes to a 'prefetched' state
		p.prefetch();

		// wait until the player is in that state...

		Time duration = p.getDuration();
		if (duration != Duration.DURATION_UNKNOWN) {
			this.setTDim(fpc.mapTimeToFrame(duration));
		} else {
			throw new AlgorithmException("Duration unknown");
		}

		// move to the first frame
		fpc.seek(1);

		// take a snap of the current frame
		buf = fg.grabFrame();

		// get its video format details
		vf = (VideoFormat) buf.getFormat();

		// initialize BufferToImage with video format
		bufferToImage = new BufferToImage(vf);

	}

	/**
	 * You already have a copy of the file don't need a new since there was no modifications
	 * @param out
	 * @throws NotSerializableException
	 */
	private void writeObject(ObjectOutputStream out) throws NotSerializableException{
		throw new NotSerializableException("JMFVideo can not be serialized");
	}
	
	private void readObject(ObjectInputStream in) throws IOException,
			ClassNotFoundException {
		in.defaultReadObject();
		this.unitMap = new HashMap<Integer, Unit>();
		this.memoryId = LargeImageMemoryManager.getInstance().addImage(this);
	}
}
