package fr.unistra.pelican.util.detection;

import java.awt.Point;

import fr.unistra.pelican.BooleanImage;
import fr.unistra.pelican.util.morphology.FlatStructuringElement2D;
import fr.unistra.pelican.util.morphology.ValuedMonoBandFlatStructuringElement;


/**
*Parameters needed for all the detection methods based on MHMT. It defines the ValuedMonoBandFlatStructuringElement to use. 
* 
* @author : Jonathan Weber
* TODO: check if coordinates are correct after removing FlatStructuringElement
*/

public class MHMTDetectionParameters implements Comparable{
	
	/**
	 * The band assigned to the SE
	 */
	private int band;
	
	/**
	 * The thresh to use with the SE in the MHMT
	 */
	private double thresh;
	
	/**
	 * If SE type is erosion (false means that SE type is dilation)
	 */
	private boolean erosion;
	
	/**
	 * Shift of the segment by report to the center of the SE. Positive value means segment is at the right of the center, negative means at left.
	 */
	private int segmentShift;
	
	/**
	 * Length of the segment. Positive value means is right to the segmentShift, negative means left.
	 */
	private int segmentLength;
	
	/**
	 * Constructor of MHMTDetectionParameters
	 * 
	 * @param band The band assigned to the SE
	 * @param thresh The thresh to use with the SE in the MHMT
	 * @param erosion If SE type is erosion (false means that SE type is dilation)
	 * @param segmentShift Shift of the segment by report to the center of the SE. Positive value means segment is at the right of the center, negative means at left.
	 * @param segmentLength Length of the segment. Positive value means is right to the segmentShift, negative means left.
	 */
	public MHMTDetectionParameters(int band,double thresh,boolean erosion,int segmentShift, int segmentLength)
	{
		this.band=band;
		this.thresh=thresh;
		this.erosion=erosion;
		this.segmentShift=segmentShift;
		this.segmentLength=segmentLength;
	}

	/**
	 * 
	 * @return The band assigned to the SE
	 */
	public int getBand() {
		return band;
	}

	/**
	 * 
	 * @param band The band assigned to the SE
	 */
	public void setBand(int band) {
		this.band = band;
	}

	/**
	 * 
	 * @return If SE type is erosion (false means that SE type is dilation)
	 */
	public boolean isErosion() {
		return erosion;
	}

	/**
	 * 
	 * @param erosion If SE type is erosion (false means that SE type is dilation)
	 */
	public void setErosion(boolean erosion) {
		this.erosion = erosion;
	}

	/**
	 * 
	 * @return The thresh to use with the SE in the MHMT
	 */
	public double getThresh() {
		return thresh;
	}

	/**
	 * 
	 * @param thresh The thresh to use with the SE in the MHMT
	 */
	public void setThresh(double thresh) {
		this.thresh = thresh;
	}

	/**
	 * 
	 * @return Shift of the segment by report to the center of the SE. Positive value means segment is at the right of the center, negative means at left.
	 */
	public int getSegmentShift() {
		return segmentShift;
	}

	/**
	 * 
	 * @param segmentShift Shift of the segment by report to the center of the SE. Positive value means segment is at the right of the center, negative means at left.
	 */
	public void setSegmentShift(int segmentShift) {
		this.segmentShift = segmentShift;
	}

	/**
	 * 
	 * @return Length of the segment. Positive value means is right to the segmentShift, negative means left.
	 */
	public int getSegmentLength() {
		return segmentLength;
	}

	/**
	 * 
	 * @param segmentLength Length of the segment. Positive value means is right to the segmentShift, negative means left.
	 */
	public void setSegmentLength(int segmentLength) {
		this.segmentLength = segmentLength;
	}

	
	/**
	 * 
	 * @return the ValuedMonoBandFlatStructuringElement defined by the MHMTDetectionParameters
	 */
	public ValuedMonoBandFlatStructuringElement getValuedMonoBandFlatStructuringElement()
	{
		BooleanImage fse;
		
		//case where the shift and the length are in the same direction
		if(((segmentShift>=0)&&(segmentLength>=0))||((segmentShift<0)&&(segmentLength<0)))
		{
			int length = segmentShift+segmentLength;
			fse = FlatStructuringElement2D.createHorizontalLineFlatStructuringElement(Math.abs(length));
			fse.fill(false);
			if(length<0)
			{
				fse.setCenter(new Point(Math.abs(length)-1,0));
				for(int i=0;i<Math.abs(segmentLength);i++)
					fse.setPixelXYBoolean(i, 0,true);
				
			}
			else
			{
				fse.setCenter(new Point(0,0));
				for(int i=segmentShift;i<segmentShift+segmentLength;i++)
					fse.setPixelXYBoolean(i,0, true);
			}
		}
		else
		{
			//case where segmentShift is higher than segmentLength
			if(Math.abs(segmentShift)>Math.abs(segmentLength))
			{
				fse=FlatStructuringElement2D.createHorizontalLineFlatStructuringElement(Math.abs(segmentShift));
				fse.fill(false);
				if(segmentShift>0)
				{
					fse.setCenter(new Point(0,0));
					for(int i=segmentShift+segmentLength;i<segmentShift;i++)
						fse.setPixelXYBoolean(i,0,true);
				}
				else
				{
					fse.setCenter(new Point(0,Math.abs(segmentShift)-1));
					for(int i=0;i<segmentLength;i++)
						fse.setPixelXYBoolean(i,0,true);
				}				
			}
			//case where segmentShift is smaller than segmentLength
			else
			{
				fse=FlatStructuringElement2D.createHorizontalLineFlatStructuringElement(Math.abs(segmentLength));

				if(segmentLength>0)
				{
					fse.setCenter(new Point(0,0));
					fse.setPixelXYBoolean(0, 0,true);
				}
				else
				{
					fse.setCenter(new Point(0,Math.abs(segmentLength)-1));
					fse.setPixelXYBoolean(Math.abs(segmentLength)-1,0,true);
				}			
			}
		}
		
		
		return new ValuedMonoBandFlatStructuringElement(fse, band, thresh, erosion);
	}

	/**
	 * Comparison method based on ES length, useful for optimizing MHMT
	 */
	public int compareTo(Object o) {
		return Math.abs(segmentLength)-Math.abs(((MHMTDetectionParameters)o).segmentLength);
	}
	
	/**
	 * toString method
	 */
	public String toString() {
		StringBuffer s=new StringBuffer();
		s.append("shape:");
		s.append(segmentLength<0?"":"+");
		s.append(segmentLength);
		s.append(segmentShift<0?"":"+");
		s.append(segmentShift);
		s.append(" band:");
		s.append(band+1);
		s.append( " thr: ");
		s.append(erosion?">":"<");
		s.append(thresh*255);
		return s.toString();
	}


}
