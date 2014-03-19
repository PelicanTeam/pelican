package fr.unistra.pelican.demos.applied.video;

import java.awt.Point;

import fr.unistra.pelican.Image;
import fr.unistra.pelican.PelicanException;
import fr.unistra.pelican.algorithms.applied.video.tracking.SnakeMultipleTracking;
import fr.unistra.pelican.algorithms.applied.video.tracking.SnakeTracking;
import fr.unistra.pelican.algorithms.io.MultipleImageLoad;
import fr.unistra.pelican.algorithms.io.MultipleImageSave;
import fr.unistra.pelican.algorithms.visualisation.Viewer2D;


public class SnakeTrackingDemo {

	public static boolean multiple=true;
	public static boolean background=false;
	
	public static void main (String args[]) throws PelicanException {
		String path="/home/lefevre/data/snake1";
		Image image=MultipleImageLoad.exec(path+"/",Image.T);
		image.setColor(true);


		if (background) {
			Image average=image.newInstance(image.getXDim(),image.getYDim(),image.getZDim(),1,image.getBDim());
			double sum;
			for (int b=0;b<image.getBDim();b++)
				for (int z=0;z<image.getZDim();z++)
					for (int y=0;y<image.getYDim();y++)
						for (int x=0;x<image.getXDim();x++) {
							sum=0;
							for (int t=0;t<image.getTDim();t++)
								sum+=image.getPixelDouble(x,y,z,t,b);
							sum/=image.getTDim();
							average.setPixelDouble(x,y,z,0,b,sum);
						}
			fr.unistra.pelican.algorithms.io.ImageSave.exec(average,"/home/lefevre/data/bgmodel.bmp");
			/*
			snake.setReference(ImageLoader.exec("/home/lefevre/data/bgmodel.bmp"));
			snake.setBackgroundModel(Snake.BACKGROUND_REFERENCE);
			*/
		}
		
		if(args.length>1) {
		//FIXME: take into account the parameters
		int splitStrategy=Integer.parseInt(args[0]);//Snake.SPLIT_EXTERN;
		int mergeStrategy=Integer.parseInt(args[1]);//Snake.MERGE_CENTERS;
		double mergeParameter=Double.parseDouble(args[2]);//10;
		int checkSizeMin=Integer.parseInt(args[3]);//3;
		int checkWidthMin=Integer.parseInt(args[4]);//3;
		int checkHeightMin=Integer.parseInt(args[5]);//3;
		int checkAreaMin=Integer.parseInt(args[6]);//5;
		}
		else {
			System.out.println("splitStrategy mergeStrategy mergeParameter checkSizeMin checkWidthMin checkHeightMin checkAreaMin");
			System.out.println("splitStrategy : SPLIT_EXTERN=1, SPLIT_INTERN=2");
			System.out.println("mergeStrategy : MERGE_CENTERS=0, MERGE_EXTREMA=1, MERGE_BOTH=2");
		}
		Point p1=new Point(image.getXDim()*8/10,image.getYDim()*4/10);
		Point p2=new Point(image.getXDim()*95/100,image.getYDim()*7/10);
		Image output;
		if (multiple==false)
			output=SnakeTracking.exec(image,p1,p2);
		else {
			Point[] t1={p1};
			Point[] t2={p2};
			output=SnakeMultipleTracking.exec(image,t1,t2);
		}
		if(args.length==0)
			Viewer2D.exec(output,"snake");
		else
			MultipleImageSave.exec(output,args[args.length-1],".png");
	}
	
}
