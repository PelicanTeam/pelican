package fr.unistra.pelican.demos;

import fr.unistra.pelican.Image;
import fr.unistra.pelican.PelicanException;
import fr.unistra.pelican.algorithms.io.MultipleImageLoadAndProcess;
import fr.unistra.pelican.algorithms.io.PelicanImageSave;

public class DirectoryToPelicanDemo
{
	public static void main(String[] args)
	{
		try{
			String path="/home/lefevre/data/video/ina/jt2";
			if (args.length!=0)
				path=args[0];
			
			
			Image video = (Image)new MultipleImageLoadAndProcess().process(path,Image.T,MultipleImageLoadAndProcess.REDUCE);
			//Image video=PelicanImageLoad.process(path+".pelican");

			video.setColor(true);
			//Viewer2D.exec(video,path);
			
			new PelicanImageSave().process(video,path+".pelican");
			//MultipleImageSave.process(video,path+"tmp",".jpg");
						
		}
		catch(PelicanException ex){
			ex.printStackTrace();
		}

	}
} 