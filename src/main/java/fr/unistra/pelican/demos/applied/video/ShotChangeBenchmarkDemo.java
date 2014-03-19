package fr.unistra.pelican.demos.applied.video;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;

import fr.unistra.pelican.Image;
import fr.unistra.pelican.PelicanException;
import fr.unistra.pelican.algorithms.applied.video.shot.AdaptiveShotChangeDetection;
import fr.unistra.pelican.algorithms.applied.video.shot.ClassicalShotChangeDetection;
import fr.unistra.pelican.algorithms.geometric.BlockResampling2D;
import fr.unistra.pelican.algorithms.io.PelicanImageLoad;
import fr.unistra.pelican.algorithms.io.PelicanImageSave;
import fr.unistra.pelican.algorithms.visualisation.Viewer2D;



public class ShotChangeBenchmarkDemo {

	private static PrintStream out = null;
	private static boolean isReduced = true;
	private static String origin="/home/lefevre/data/global/video/jt1";
	//private static String origin="/home/lefevre/data/global/video/foot1";
	private static boolean ourBench=false;
	
	
	/**
	 * @param args
	 * @throws FileNotFoundException 
	 */
	public static void main(String[] args) throws PelicanException, FileNotFoundException {
	
		// Lancement du timer
		long t1=System.currentTimeMillis();
		
		// Chargement
		String path=origin;
		//String path="/home/lefevre/data/global/video/foot3";
		if (args.length!=0)
			path=args[0];
		
		if(isReduced)
			path=path+"_reduced";
		Image video = PelicanImageLoad.exec(path+".pelican");
		
		if(origin.endsWith("jt1"))
			video=reduce(video,6000);
		

		File f = new File(path+".csv");
		out = new PrintStream(new FileOutputStream(f));
		
		boolean reduceOnly=false;
		if(reduceOnly) {
			video= BlockResampling2D.exec(video,8,8,false);
			PelicanImageSave.exec(video,path+"_reduced.pelican");
			return;
		}

		
		// Affichage optionnel
		boolean viewOnly=false;
		if (viewOnly) {
			video.setColor(true);
			Viewer2D.exec(video,"Sequence");
		}
		
		// Benchmark
		bench(video);

		// Fermeture du fichier
		out.close();
		
		// Arrêt du timer
		long t2=System.currentTimeMillis()-t1;
		System.out.println("Démo terminée : "+ (t2/1000) + " secondes");
		
	}
		
	public static void bench(Image video) throws PelicanException {
		// Gestion des paramètres
		double lthr, lthrS, lthrC, lthrT, lthrO;
		double hthr, hthrS, hthrC, hthrT, hthrO;
		int nthr, nthrS, nthrC, nthrT, nthrO;
		// thr : seuil fixe pour la comparaison avec la mesure dérivée
		lthr=0;
		hthr=20;
		nthr=6;
		// thrS : seuil de saturation pour considérer la teinte comme valable
		lthrS=0;
		hthrS=1;
		nthrS=6;
		// thrC : influence de la teinte et de la saturation
		lthrC=0;
		hthrC=1;
		nthrC=6;
		// thrT : inertie du seuil
		lthrT=0;
		hthrT=1;
		nthrT=6;
		// thrO : autres méthodes
		lthrO=0;
		hthrO=1;
		nthrO=1001;
		// Génération des différents paramètres
		double athr []=new double[nthr];
		double athrS []=new double [nthrS];
		double athrC []=new double [nthrC];
		double athrT []=new double [nthrT];
		double athrO []=new double [nthrO];
		int thr, thrS, thrC, thrT, thrO;
		for (thr=0;thr<nthr;thr++)
			athr[thr]=lthr+thr*(hthr-lthr)/(nthr-1);
		for (thrS=0;thrS<nthrS;thrS++)
			athrS[thrS]=lthrS+thrS*(hthrS-lthrS)/(nthrS-1);
		for (thrC=0;thrC<nthrC;thrC++)
			athrC[thrC]=lthrC+thrC*(hthrC-lthrC)/(nthrC-1);
		for (thrT=0;thrT<nthrT;thrT++)
			athrT[thrT]=lthrT+thrT*(hthrT-lthrT)/(nthrT-1);
		for (thrO=0;thrO<nthrO;thrO++)
			athrO[thrO]=lthrO+thrO*(hthrO-lthrO)/(nthrO-1);
		// Lancement de la méthode avec les différents paramètres;
		Integer[] res=null;
		if (ourBench)
		for (thr=0;thr<nthr;thr++)
			for (thrS=0;thrS<nthrS;thrS++)
				for (thrC=0;thrC<nthrC;thrC++)
					for (thrT=0;thrT<nthrT;thrT++) {
						// Appel de la méthode
						res=null;
						System.gc();
						res=(Integer[]) new AdaptiveShotChangeDetection().process(video,athr[thr],athrS[thrS],athrT[thrT],athrC[thrC],!isReduced);
						//res=AdaptiveShotChangeDetection.process(video,8,0.25,0.5,0.75,false);
						// Affichage des paramètres
						System.out.print("thr="+athr[thr]);
						System.out.print("\t thrS="+athrS[thrS]);
						System.out.print("\t thrC="+athrC[thrC]);
						System.out.print("\t thrT="+athrT[thrT]);
						System.out.println("");
						out.print(athr[thr]+"\t"+athrS[thrS]+"\t"+athrC[thrC]+"\t"+athrT[thrT]+"\t");
						
						// Affichage des résultats
						results(res);
						//System.out.println("Résultats terminés");
						// Calcul des statistiques
						int delta=0;
						stats(res,delta);
						//System.out.println("Statistiques terminées");
					}
		else
			for (thrO=0;thrO<nthrO;thrO++) {
				//double val=100*athrO[thrO];
				//res=ClassicalShotChangeDetection.process(video,ClassicalShotChangeDetection.HISTO,val);
				double val=256*athrO[thrO];
				res=ClassicalShotChangeDetection.exec(video,ClassicalShotChangeDetection.PIXEL,val);
				System.out.print("thrO="+val);
				System.out.println("");
				out.print(val+"\t");
				// Affichage des résultats
				results(res);
				//System.out.println("Résultats terminés");
				// Calcul des statistiques
				int delta=0;
				stats(res,delta);
				//System.out.println("Statistiques terminées");
			}

		}

	public static void stats(Integer [] res, int delta) {
		// Décompte du nombre de transitions
		int nbEffects=0;
		int nbCuts=0;
		int nb=0;
		for(int i=0;i<res.length-1;i++) {
			if(res[i]==1) {
				nbEffects++;
				while(res[i]==1 && i<res.length-1) i++;
				}
			if(res[i]==2) {
				nbCuts++;
				while(res[i]==2 && i<res.length-1) i++;
			}
		}
		nb=nbEffects+nbCuts;
		// Enregistrement des transitions
		int[][] data=new int[nb][3];
		nb=0;
		for(int i=0;i<res.length-1;i++) {
			if(res[i]==1) {
				data[nb][0]=1;
				data[nb][1]=i+2;
				while(res[i]==1 && i<res.length-1) i++;
				data[nb][2]=i+1;
				nb++;
				}
			if(res[i]==2) {
				data[nb][0]=2;
				data[nb][1]=i+2;
				while(res[i]==2 && i<res.length-1) i++;
				data[nb][2]=i+1;
				nb++;
			}
		}
		// Chargement du résultat de référence
		int[][] ref=loadReference();
		int refCuts=0;
		int refEffects=0;
		for(int i=0;i<ref.length;i++) {
			if (ref[i][0]==1)
				refEffects++;
			if (ref[i][0]==2)
				refCuts++;
		}
		// Comparaison itérative avec la référence
		boolean ref2 []=new boolean[ref.length];
		boolean data2 []=new boolean[data.length];
		boolean incrData,incrRef;
		int cptRef=0;
		int cptData=0;
		while (cptRef<ref.length && cptData<data.length) {
			int cases=0;
			boolean ok=false,ko=false;
			// Cas 1 : fin data < début ref => data < ref
			// Data XXXX
			// Ref        XXXX
			if (data[cptData][2]+delta<ref[cptRef][1]) {
				cases++;
				ko=true;
			}
			// Cas 2 : fin data > début ref et fin data < fin ref => data <= ref
			// Data XXXXX
			// Ref      XXXXX
			if (data[cptData][2]+delta>=ref[cptRef][1] && data[cptData][2]<=ref[cptRef][2]) {
				cases++;
				ok=true;
				data2[cptData]=true;
				ref2[cptRef]=true;
			}
			// Cas 3 : début data < début ref et fin data > fin ref => ref in data
			// Data XXXXXXXX
			// Ref    XXXX
			if (data[cptData][1]<=ref[cptRef][1] && data[cptData][2]>=ref[cptRef][2]) {
				cases++;
				ok=true;
				data2[cptData]=true;
				ref2[cptRef]=true;
			}
			// Cas 4 : début data > début ref et fin data < fin ref => data in ref
			// Data    XXXX
			// Ref  XXXXXXXXX
			if (data[cptData][1]>=ref[cptRef][1] && data[cptData][2]<=ref[cptRef][2]) {
				cases++;
				ok=true;
				data2[cptData]=true;
				ref2[cptRef]=true;
			}
			// Cas 5 : début data > début ref et début data < fin ref => data >= ref
			// Data     XXXXXX
			// Ref  XXXXXXXX
			if (data[cptData][1]>=ref[cptRef][1] && data[cptData][1]<=ref[cptRef][2]+delta) {
				cases++;
				ok=true;
				data2[cptData]=true;
				ref2[cptRef]=true;
			}
			// Cas 6 : début data > fin ref => data > ref
			// Data       XXXX
			// Ref  XXXX
			if (data[cptData][1]>ref[cptRef][2]+delta) {
				ko=true;
				cases++;
			}
			// Vérification de l'unicité des cas
			if (cases==0)
				System.out.println("cases==0 : cptData="+cptData+" cptRef="+cptRef);
			if(ok && ko)
				System.out.println("ok && ko : cptData="+cptData+" cptRef="+cptRef);
			// Incrémentation des compteurs
			if (data[cptData][2]<=ref[cptRef][2])
				incrData=true;
			else
				incrData=false;
			if (data[cptData][2]>=ref[cptRef][2])
				incrRef=true;
			else 
				incrRef=false;
			if (incrData)
				cptData++;
			if (incrRef)
				cptRef++;
		}
		// Calcul des mesures de qualité
		int fpEffects=0,fnEffects=0;
		int fpCuts=0,fnCuts=0;
		for (cptData=0;cptData<data2.length;cptData++)
			if (data2[cptData]==false) { 
				if (data[cptData][0]==1) {
					fpEffects++;
					//System.out.println("Effet FP : "+data[cptData][1]+" à "+data[cptData][2]);
				}
				if (data[cptData][0]==2) {
					fpCuts++;
					//System.out.println("Cut FP : "+data[cptData][1]+" à "+data[cptData][2]);
				}
			}
		int fp=fpEffects+fpCuts;
		for (cptRef=0;cptRef<ref2.length;cptRef++)
			if (ref2[cptRef]==false) { 
				if (ref[cptRef][0]==1) { 
					fnEffects++;
					//System.out.println("Effet FN : "+ref[cptRef][1]+" à "+ref[cptRef][2]);
				}
				if (ref[cptRef][0]==2) {
					fnCuts++;
					//System.out.println("Cut FN : "+ref[cptRef][1]+" à "+ref[cptRef][2]);
				}
			}
		int fncEffects=refEffects-nbEffects+fpEffects;
		int fncCuts=refCuts-nbCuts+fpCuts;
		int fn=fnEffects+fnCuts;
		int fnc=fncEffects+fncCuts;
		// Affichage des statistiques
		System.out.print("Effects : "+"NB="+nbEffects+"\t FP="+fpEffects+"\t FN="+fnEffects+"\t FNC="+fncEffects);
		System.out.println("\t Recall="+(nbEffects)/((double)(nbEffects+fnEffects))+"\t Recall cor="+((nbEffects)/((double)(nbEffects+fncEffects)))+"\t Precision="+(nbEffects)/((double)(nbEffects+fpEffects)));
		System.out.print("Cuts : "+"NB="+nbCuts+"\t FP="+fpCuts+"\t FN="+fnCuts+"\t FNC="+fncCuts);
		System.out.println("\t Recall="+(nbCuts)/((double)(nbCuts+fnCuts))+"\t Recall cor="+((nbCuts)/((double)(nbCuts+fncCuts)))+"\t Precision="+(nbCuts)/((double)(nbCuts+fpCuts)));
		System.out.print("General : "+"NB="+nb+"\t FP="+fp+"\t FN="+fn+"\t FNC="+(refCuts+refEffects-nb+fp));
		System.out.println("\t Recall="+(nb)/((double)(nb+fn))+"\t Recall cor="+((nb)/((double)(nb+fnc)))+"\t Precision="+(nb)/((double)(nb+fp)));
		
		out.print("\t"+nbEffects+"\t"+fpEffects+"\t"+fnEffects+"\t"+(refEffects-nbEffects+fpEffects));
		out.print("\t"+(nbEffects)/((double)(nbEffects+fnEffects))+"\t"+((nbEffects)/((double)(refEffects+fpEffects)))+"\t"+(nbEffects)/((double)(nbEffects+fpEffects)));
		out.print("\t"+nbCuts+"\t"+fpCuts+"\t"+fnCuts+"\t"+(refCuts-nbCuts+fpCuts));
		out.print("\t"+(nbCuts)/((double)(nbCuts+fnCuts))+"\t"+((nbCuts)/((double)(refCuts+fpCuts)))+"\t"+(nbCuts)/((double)(nbCuts+fpCuts)));
		out.print("\t"+nb+"\t"+fp+"\t"+fn+"\t"+(refCuts+refEffects-nb+fp));
		out.print("\t"+(nb)/((double)(nb+fn))+"\t"+((nb)/((double)(refCuts+refEffects+fp)))+"\t"+(nb)/((double)(nb+fp)));
		out.println();
		
	}

	public static void results(Integer [] res) {
		// Affichage des résultats
		for(int i=0;i<res.length-1;i++) {
			if(res[i]==1) {
				//System.out.print("Effet : "+(i+2));
				while(res[i]==1 && i<res.length-1) i++;
				//System.out.println(" à "+(i+1));
				}
			if(res[i]==2) {
				//System.out.print("Cut : "+(i+2));
				while(res[i]==2 && i<res.length-1) i++;
				//System.out.println(" à "+(i+1));
			}
		}
		
	}
	
	public static int[][] loadReference() {
		int [][] ref=null;
		int cptRef=0;
		String file=origin+"-ref.txt";
		String line="";
		try {
			// Lecture du fichier
			BufferedReader f = new BufferedReader(new FileReader(file));
			// Nombre de transitions
			line=f.readLine();
			int nb=Integer.parseInt(line);
			ref=new int[nb][3];
			// Parcours des transitions
			line=f.readLine();
			while (line!=null) {
				// Détermination du type de transition
				if (line.charAt(0)=='E')
					ref[cptRef][0]=1;
				if (line.charAt(0)=='C')
					ref[cptRef][0]=2;
				if (line.charAt(0)=='/') {
					line=f.readLine();
					continue;	
				}
				// Détermination des frontières de la transition
				ref[cptRef][1]=Integer.parseInt(line.substring(line.indexOf(':')+1,line.indexOf('-')));
				ref[cptRef][2]=Integer.parseInt(line.substring(line.lastIndexOf('-')+1));
				//System.out.println(ref[cptRef][0]+":"+ref[cptRef][1]+" à "+ref[cptRef][2]+" => "+line);
				cptRef++;
				line=f.readLine();
			}
			f.close();
		} 
		catch (FileNotFoundException e) { e.printStackTrace(); } 
		catch (IOException e) { e.printStackTrace(); }
		
		return ref;
		
	}
	/*
	public static int[][] loadResults() {
		int [][] ref=null;
		int cptRef=0;
		String file=origin;
		if (isReduced)
			file=file+"_reduced";
		file=file+".csv";
		String line="";
		try {
			// Nombre de résultats
			BufferedReader f = new BufferedReader(new FileReader(file));
			line=f.readLine();
			while (line!=null) {
				cptRef++;
				line=f.readLine();
			}
			int nb=cptRef;
			f.close();
			f = new BufferedReader(new FileReader(file));
			// Lecture du fichier
			line=f.readLine();
			int largeur=30;
			ref=new int[nb][largeur];
			cptRef=0;
			while (line!=null) {
				int indice1=0;
				int indice2=line.indexOf('\t');
				ref[cptRef][i]=Integer.parseInt(line.substring(0,indice));
				for(i=1;i<largeur;i++) {
					indice1=indice2+1;
					indice2=line.indexOf('\t',indice1);
					
					ref[cptRef][i]=Integer.parseInt(line.substring(indice1,indice2));
					    
				// Détermination des frontières de la transition
				ref[cptRef][1]=Integer.parseInt(line.substring(line.indexOf(':')+1,line.indexOf('-')));
				ref[cptRef][2]=Integer.parseInt(line.substring(line.lastIndexOf('-')+1));
				//System.out.println(ref[cptRef][0]+":"+ref[cptRef][1]+" à "+ref[cptRef][2]+" => "+line);
				cptRef++;
				line=f.readLine();
			}
			f.close();
		} 
		catch (FileNotFoundException e) { e.printStackTrace(); } 
		catch (IOException e) { e.printStackTrace(); }
		
		return ref;
		
	}
	*/
	
	public static Image reduce(Image im, int size) {
		Image result=im.newInstance(im.getXDim(),im.getYDim(),im.getZDim(),size,im.getBDim());
		for(int t=0;t<size;t++)
			for(int x=0;x<im.getXDim();x++)
				for(int y=0;y<im.getYDim();y++)
					for(int z=0;z<im.getZDim();z++)
						for(int b=0;b<im.getBDim();b++)
							result.setPixelByte(x,y,z,t,b,im.getPixelByte(x,y,z,t,b));
		return result;
	}
	
}