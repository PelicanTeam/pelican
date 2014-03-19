/**
 * 
 */
package fr.unistra.pelican.gui;

import java.awt.FileDialog;
import java.io.File;
import java.io.FilenameFilter;

import javax.swing.JFileChooser;
import javax.swing.JFrame;

/**
 * A generic FileChooser
 * 
 * TODO: add options for extensions filtering...
 * 
 * @author Benjamin Perret
 *
 */
public abstract class FileChooserToolBox {

	private static FileDialog fileChooser=null;
	
	private static JFileChooser jfileChooser=null;
	
	private static void prepareChooser(JFrame parent)
	{
			
		fileChooser = new FileDialog(parent);	
		//fileChooser.setFilenameFilter(new AimFilter());		
	}
	
	private static void prepareChooser2()
	{
			
		jfileChooser = new JFileChooser();	
		//fileChooser.setFilenameFilter(new AimFilter());		
	}
	
	public static File openSaveFileChooser(JFrame parent)
	{
		File f=null;
		if (fileChooser==null)
			prepareChooser(parent);
		fileChooser.setMode(FileDialog.SAVE);
		fileChooser.setTitle("Save file here...");
		fileChooser.setVisible(true); //fileChooser.showSaveDialog(parent);
		//if(returnVal == JFileChooser.APPROVE_OPTION) 
		//      f=fileChooser.getSelectedFile();
		
		String fname=fileChooser.getFile();
		if(fname!=null)
			f=new File(fileChooser.getDirectory() + File.separatorChar + fname);
		
		/*if (f!=null && f.exists())
		{
			int n = JOptionPane.showConfirmDialog(
				    null,
				    "Warning file: " + f.getName() + " already exists. Do you want to overwrite it?",
				    "Saving or not saving",
				    JOptionPane.YES_NO_OPTION);
			if (n!=0)
				f=null;
		}*/
		return f;
	}
	
	public static File openOpenFileChooser(JFrame parent)
	{
		File f=null;
		if (fileChooser==null)
			prepareChooser(parent);
		
		fileChooser.setMode(FileDialog.LOAD);
		fileChooser.setVisible(true);
		fileChooser.setTitle("Choose file to load...");
		String fname=fileChooser.getFile();
		if(fname!=null)
			f=new File(fileChooser.getDirectory() + File.separatorChar + fname);
		
		return f;
	}
	
	public static File openOpenDirectoryChooser(JFrame parent){
		File f=null;
		if (jfileChooser==null)
			prepareChooser2();
		jfileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		
		int res=jfileChooser.showDialog(parent, "Open");
		if(res==JFileChooser.APPROVE_OPTION)
		{
			f=jfileChooser.getSelectedFile();
		}else{
			System.out.println("Action cancelled by user!");
		}
		
		return f;
	}
}

class AimFilter implements FilenameFilter
{


	
	public boolean accept(File f) {

		if(f.isDirectory())
			return true;
		String extension = getExtension(f);
		System.out.println(extension);
		if (extension != null && extension.equals("aim") )
			return true;

		return false;

	}

	private String getExtension(File f) {
       return getExtension(f.getName());
    }
	
	private String getExtension(String s ) {
        String ext = null;
        int i = s.lastIndexOf('.');

        if (i > 0 &&  i < s.length() - 1) {
            ext = s.substring(i+1).toLowerCase();
        }
        return ext;
    }

	/*public String getDescription() {
		
		return "Astronomical Image Model files";
	}*/
	
	
	public boolean accept(File arg0, String arg1) {
		String extension = getExtension(arg1);
		if (extension != null && extension.equals("aim") )
			return true;

		return false;
	}
	
}
