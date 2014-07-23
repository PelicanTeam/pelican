package fr.unistra.pelican.algorithms.draw;

import java.awt.Color;
import java.awt.Point;

import fr.unistra.pelican.Algorithm;
import fr.unistra.pelican.AlgorithmException;
import fr.unistra.pelican.Image;
/**
 * This class draws a line on a 2D-picture. Colored or not. 
 * The line is draw according to the Bresenham method.
 *
 */
public class DrawLine extends Algorithm {


	/**
	 * Image to be processed
	 */
	public Image inputImage;

	/**
	 * Extremities
	 */
	public Point p1;
	public Point p2;

	/**
	 * Color of the line
	 */
	public Color color=Color.WHITE;

	/**
	 * Resulting picture
	 */
	public Image outputImage;


	/**
	 * Constructor
	 */

	public DrawLine(){
		super();
		super.inputs="inputImage,p1,p2";
		super.options="color";
		super.outputs="outputImage";
	}
	@Override
	public void launch() throws AlgorithmException {
		if (inputImage.getZDim()!=1) throw new AlgorithmException("This is not a 2D picture");
		outputImage=inputImage.copyImage(true);
		int dx,dy;
		int x1,x2,y1,y2;
		x1 = p1.x;
		y1 = p1.y;
		x2 = p2.x;
		y2 = p2.y;
		dx = x2-x1;
		if(dx != 0){
			if(dx>0){
				dy = y2 - y1;
				if(dy != 0){
					if(dy>0){
						if(dx >= dy){
							int e = dx;
							dx *= 2;
							dy *= 2;
							while(x1 != x2){
								outputImage.setPixelXYZTBByte(x1,y1,0,0,0,color.getRed());
								outputImage.setPixelXYZTBByte(x1,y1,0,0,1,color.getGreen());
								outputImage.setPixelXYZTBByte(x1,y1,0,0,2,color.getBlue());
								x1++;
								e -= dy;
								if(e<0){
									y1++;
									e += dx;
								}
							}
						}
						else{
							int e = dy;
							dy *= 2;
							dx *= 2;
							while(y1 != y2){
								outputImage.setPixelXYZTBByte(x1,y1,0,0,0,color.getRed());
								outputImage.setPixelXYZTBByte(x1,y1,0,0,1,color.getGreen());
								outputImage.setPixelXYZTBByte(x1,y1,0,0,2,color.getBlue());
								y1++;
								e -= dx;
								if(e<0){
									x1++;
									e += dy;
								}
							}
						}
					}
					else{
						if(dx >= -dy){
							int e = dx;
							dx *= 2;
							dy *= 2;
							while(x1 != x2){
								outputImage.setPixelXYZTBByte(x1,y1,0,0,0,color.getRed());
								outputImage.setPixelXYZTBByte(x1,y1,0,0,1,color.getGreen());
								outputImage.setPixelXYZTBByte(x1,y1,0,0,2,color.getBlue());
								x1++;
								e += dy;
								if(e < 0){
									y1 -= 1;
									e += dx;
								}
							}
						}
						else{
							int e = dy;
							dy *= 2;
							dx *= 2;
							while(y1 != y2){
								outputImage.setPixelXYZTBByte(x1,y1,0,0,0,color.getRed());
								outputImage.setPixelXYZTBByte(x1,y1,0,0,1,color.getGreen());
								outputImage.setPixelXYZTBByte(x1,y1,0,0,2,color.getBlue());
								y1--;
								e += dx;
								if(e > 0){
									x1 ++;
									e += dy;
								}
							}
						}
					}
				}
				else{
					while(x1 != x2){
						outputImage.setPixelXYZTBByte(x1,y1,0,0,0,color.getRed());
						outputImage.setPixelXYZTBByte(x1,y1,0,0,1,color.getGreen());
						outputImage.setPixelXYZTBByte(x1,y1,0,0,2,color.getBlue());
						x1++;
					}
				}
				dy = y2 - y1;
				if(dy != 0){
					if(dy > 0){
						if(-dx >= dy){
							int e = dx;
							dy *=2;
							dx *= 2;
							while(x1 != x2){
								outputImage.setPixelXYZTBByte(x1,y1,0,0,0,color.getRed());
								outputImage.setPixelXYZTBByte(x1,y1,0,0,1,color.getGreen());
								outputImage.setPixelXYZTBByte(x1,y1,0,0,2,color.getBlue());
								x1--;
								e += dy;
								if(e >= 0){
									y1++;
									e += dx;
								}
							}
						}
						else{
							int e = dy;
							dy *= 2;
							dx *= 2;
							while(y1 != y2){
								outputImage.setPixelXYZTBByte(x1,y1,0,0,0,color.getRed());
								outputImage.setPixelXYZTBByte(x1,y1,0,0,1,color.getGreen());
								outputImage.setPixelXYZTBByte(x1,y1,0,0,2,color.getBlue());
								y1++;
								e += dx;
								if(e <= 0){
									x1--;
									e += dy;
								}
							}
						}
					}
					else{
						if(dx <= dy){
							int e = dx;
							dx *= 2;
							dy *= 2;
							while(x1 != x2){
								outputImage.setPixelXYZTBByte(x1,y1,0,0,0,color.getRed());
								outputImage.setPixelXYZTBByte(x1,y1,0,0,1,color.getGreen());
								outputImage.setPixelXYZTBByte(x1,y1,0,0,2,color.getBlue());
								x1--;
								e -= dy;
								if(e >= 0){
									y1--;
									e += dx;
								}
							}
						}
						else{
							int e = dy;
							dy *= 2;
							dx *= 2;
							while(y1 != y2){
								outputImage.setPixelXYZTBByte(x1,y1,0,0,0,color.getRed());
								outputImage.setPixelXYZTBByte(x1,y1,0,0,1,color.getGreen());
								outputImage.setPixelXYZTBByte(x1,y1,0,0,2,color.getBlue());
								y1--;
								e -= dx;
								if(e >= 0){
									x1--;
									e += dy;
								}
							}
						}
					}
				}
				else{
					while(x1 != x2){
						outputImage.setPixelXYZTBByte(x1,y1,0,0,0,color.getRed());
						outputImage.setPixelXYZTBByte(x1,y1,0,0,1,color.getGreen());
						outputImage.setPixelXYZTBByte(x1,y1,0,0,2,color.getBlue());
						x1--;
					}
				}
			}
		}
		else{
			dy = y2 -y1;
			if(dy != 0){
				if(dy > 0){
					while(y1 != y2){
						outputImage.setPixelXYZTBByte(x1,y1,0,0,0,color.getRed());
						outputImage.setPixelXYZTBByte(x1,y1,0,0,1,color.getGreen());
						outputImage.setPixelXYZTBByte(x1,y1,0,0,2,color.getBlue());
						y1++;
					}
				}
				else{
					while(y1 != y2){
						outputImage.setPixelXYZTBByte(x1,y1,0,0,0,color.getRed());
						outputImage.setPixelXYZTBByte(x1,y1,0,0,1,color.getGreen());
						outputImage.setPixelXYZTBByte(x1,y1,0,0,2,color.getBlue());
						y1--;
					}
				}
			}
		}
	}

	/**
	 * Draw a colored line on a 2D Picture
	 * @param inputImage image to be processed
	 * @param p1 First extremity of the line
	 * @param p2 Second extremity of the line
	 * @param color Color of the line
	 * @return
	 */
	public static Image exec(Image inputImage,Point p1,Point p2, Color color){
		return (Image) new DrawLine().process(inputImage,p1,p2,color);
	}
	
	/**
	 * Draw a line on a 2D Picture
	* @param inputImage image to be processed
	 * @param p1 First extremity of the line
	 * @param p2 Second extremity of the line
	 * @return
	 */
	public static Image exec(Image inputImage,Point p1,Point p2){
		return (Image) new DrawLine().process(inputImage,p1,p2);
	}
}
