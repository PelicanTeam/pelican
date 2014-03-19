package fr.unistra.pelican.demos.applied.video;


import fr.unistra.pelican.Image;
import fr.unistra.pelican.PelicanException;
import fr.unistra.pelican.algorithms.applied.video.shot.AdaptiveShotChangeDetection;
import fr.unistra.pelican.algorithms.io.PelicanImageLoad;


public class ShotChangeDetectionDemo {

	/**
	 * @param args
	 */
	public static void main(String[] args) throws PelicanException {
	
		// Lancement du timer
		long t1=System.currentTimeMillis();
		
		// Chargement
		String path="/home/lefevre/data/foot";
		if (args.length!=0)
			path=args[0];
		Image video = PelicanImageLoad.exec(path+".pelican");
		//Image video = MultipleImageLoad.process(path);
		//Image video = PartialMultipleImageLoad.process(path,0,10);

		// Affichage optionnel
		//video.setColor(true);
		//Viewer2D.exec(video,"Sequence");
		
		// Calcul des mesures entre trames
		/*
		Double[] values=PixelBasedInterframeDifference.process(video);
		Double[] values=HistogramBasedInterframeDifference.process(video);
		Double[] values=HSVBasedInterframeDifference.process(video,0.25,0.5,true);
		system.out.println(java.util.Arrays.deepToString(values));
		*/
		
		// Appel de la méthode
		Integer[] res=AdaptiveShotChangeDetection.exec(video,8);
		//Integer[] res=ClassicalShotChangeDetection.process(video,ClassicalShotChangeDetection.HISTO,50);
		//Integer[] res=ClassicalShotChangeDetection.process(video,ClassicalShotChangeDetection.PIXEL,50);
		
		// Affichage des résultats
		for(int i=0;i<res.length-1;i++) {
			if(res[i]==1) {
				System.out.print("Effet : "+(i+2));
				while(res[i]==1 && i<res.length-1) i++;
				System.out.println(" a "+(i+1));
				}
			if(res[i]==2) {
				System.out.print("Cut : "+(i+2));
				while(res[i]==2 && i<res.length-1) i++;
				System.out.println(" a "+(i+1));
			}
		}
		
		// Arrêt du timer
		long t2=System.currentTimeMillis()-t1;
		System.out.println("Demo terminee : "+ (t2/1000) + " secondes");
		
	}

}