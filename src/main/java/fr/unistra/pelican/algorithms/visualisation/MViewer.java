/**
 * 
 */
package fr.unistra.pelican.algorithms.visualisation;

import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;

import javax.swing.JFrame;

import fr.unistra.pelican.Algorithm;
import fr.unistra.pelican.AlgorithmException;
import fr.unistra.pelican.Image;
import fr.unistra.pelican.algorithms.io.ImageLoader;
import fr.unistra.pelican.gui.MultiViews.MultiView;

/**
 * Let your dreams become true and display several Pelican images in the SAME window!
 * To do so use this algorithm to create a MultiView object then add the different images to this object. 
 * 
 * @author Benjamin Perret
 *
 */
public class MViewer extends Algorithm {

	public Image [] inputImage=null;
	
	public MultiView multiView;
	
	/**
	 * 
	 */
	public MViewer() {
		this.inputs="";
		this.options="inputImage";
		this.outputs="multiView";
	}

	/* (non-Javadoc)
	 * @see fr.unistra.pelican.Algorithm#launch()
	 */
	@Override
	public void launch() throws AlgorithmException {
		JFrame frame=new JFrame();
		multiView=new MultiView();
		frame.add(multiView);
		frame.setTitle("Multi View System");
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		frame.setSize(600, 800);
		frame.setVisible(true);
		frame.addWindowListener(new WindowListener(){

			@Override
			public void windowActivated(WindowEvent e) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void windowClosed(WindowEvent e) {
				//System.out.println("closed");
				// TODO Auto-generated method stub
				
			}

			@Override
			public void windowClosing(WindowEvent e) {
				//System.out.println("closing");
				multiView.dispose();
				
			}

			@Override
			public void windowDeactivated(WindowEvent e) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void windowDeiconified(WindowEvent e) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void windowIconified(WindowEvent e) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void windowOpened(WindowEvent e) {
				// TODO Auto-generated method stub
				
			}
			
		});
		
		if(inputImage!=null)
		{
			for(Image i:inputImage)
			multiView.add(i);
		}
		
	}
	
	public static MultiView exec(Image ... inputImage)
	{
		return (MultiView)(new MViewer()).process((Object)inputImage);
	}
	
	public static MultiView exec()
	{
		return (MultiView)(new MViewer()).process();
	}
	
	public static void main(String [] args)
	{
		MultiView mv=MViewer.exec();
		for(int i=0; i<args.length;i++)
		{
			File f = new File(args[i]);
			if(!(f.exists() && f.canRead()) )
			{
				System.err.println("File " + f + " does not exist or cannot be read.");
			}else{
				try{			
					Image im=ImageLoader.exec(f.getAbsolutePath());
					mv.add(im);
				} catch (Exception e){
					System.err.println("File " + f + " contains error or is not a regular image file or file format is not supported.");
					System.err.println("Internal error was: "+e);
				}
			}
		}
	}

}
