package fr.unistra.pelican.util;

import java.awt.Point;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.PixelInterleavedSampleModel;
import java.awt.image.Raster;
import java.awt.image.SampleModel;
import java.util.Vector;

import javax.media.jai.PlanarImage;
import javax.media.jai.RasterFactory;
import javax.media.jai.TiledImage;

import fr.unistra.pelican.ByteImage;
import fr.unistra.pelican.Image;
import fr.unistra.pelican.algorithms.conversion.AverageChannels;
import fr.unistra.pelican.algorithms.conversion.GrayToRGB;

/**
 * 
 * Class containing various memory-related static tools. Warning: these methods
 * are accurate only if JVM is launch with -Xmx -Xms options
 * 
 * 
 * @author Lefevre
 */
public class Memory {

	public static long maxMemory() {
		return Runtime.getRuntime().maxMemory();
	}

	public static long totalMemory() {
		return Runtime.getRuntime().totalMemory();
	}

	public static long freeMemory() {
		return Runtime.getRuntime().freeMemory();
	}

	public static long totalFreeMemory() {
		Runtime runtime = Runtime.getRuntime();
		long maxMemory = runtime.maxMemory();
		long allocatedMemory = runtime.totalMemory();
		long freeMemory = runtime.freeMemory();
		return (freeMemory + (maxMemory - allocatedMemory));
	}

	public static long totalUsedMemory() {
		Runtime runtime = Runtime.getRuntime();
		long maxMemory = runtime.maxMemory();
		long freeMemory = runtime.freeMemory();
		return (maxMemory - freeMemory);
	}

	public static double totalFreeMemoryKB() {
		return totalFreeMemory() / 1024.0;
	}

	public static double totalFreeMemoryMB() {
		return totalFreeMemoryKB() / 1024.0;
	}

	public static double totalFreeMemoryGB() {
		return totalFreeMemoryMB() / 1024.0;
	}

	public static double totalUsedMemoryKB() {
		return totalUsedMemory() / 1024.0;
	}

	public static double totalUsedMemoryMB() {
		return totalUsedMemoryKB() / 1024.0;
	}

	public static double totalUsedMemoryGB() {
		return totalUsedMemoryMB() / 1024.0;
	}

}
