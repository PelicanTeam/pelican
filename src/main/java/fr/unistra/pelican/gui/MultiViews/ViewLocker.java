/**
 * 
 */
package fr.unistra.pelican.gui.MultiViews;

import java.awt.Image;
import java.awt.Toolkit;
import java.net.URL;
import java.util.ArrayList;
import java.util.Map;

import javax.swing.ImageIcon;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import fr.unistra.pelican.util.Wrapper;

/**
 * Lock the properties of several views together.
 * 
 * @author Benjamin Perret
 *
 */
public class ViewLocker implements ChangeListener{

	public static final String LOCKER_PROPERTY_NAME = "VIEW_LOCKER";
	
	public static final String LOCKER_PROPERTY_DO_NOT_PERFORM_EVENT = "VIEW_LOCKER_DNPE";
	
	public static final Image LOCK_ICON  ;//=Toolkit.getDefaultToolkit().createImage((MultiView.RESSOURCES_PATH + "icon_lock.png"));
	
	static{
		Wrapper w=new Wrapper();
		ClassLoader cl = w.getClass().getClassLoader();
		   // Create icons
		 //  Icon saveIcon  = new ImageIcon(cl.getResource("images/save.gif")); 
		URL url=cl.getResource(MultiView.RESSOURCES_PATH + "icon_lock.png");
		//InputStream in=cl.getResourceAsStream("model/modelDemoVisu.aim");
		//File f;
		Image i=null;
		if(url!=null)
		{
			i=Toolkit.getDefaultToolkit().createImage(url);
			
			
		}else {
			
			i=Toolkit.getDefaultToolkit().createImage(MultiView.RESSOURCES_PATH + "icon_lock.png");
		}
		LOCK_ICON=i;
	}
	
	//private static int globalLockNumber=1;
	
	private static ArrayList<Integer> usedNumbers=new ArrayList<Integer>();
	
	private int lockNumber=0;
	
	private ArrayList<View> list= new ArrayList<View>();
	
	public ViewLocker()
	{
		lockNumber=findLockNumber();
		usedNumbers.add(lockNumber);
	}
	
	private int findLockNumber(){
		boolean flag=true;
		int n=0;
		do
		{
			n++;
			flag=usedNumbers.contains(n);
		}while(flag);
		return n;
	}
	public void add(View v)
	{
		v.addChangeListener(this);
		v.properties.put(LOCKER_PROPERTY_NAME,this);
		if(list.size()>0)
			v.copyAttribute(list.get(0));
		list.add(v);
	}
	
	public void remove(View v)
	{
		v.properties.remove(LOCKER_PROPERTY_NAME);
		v.removeChangeListener(this);
		list.remove(v);
		if(list.size()==0)
		{
			usedNumbers.remove((Integer)lockNumber);
			
		}
			
	}
	
	public void clear()
	{
		for(View v:list)
		{
			v.properties.remove(LOCKER_PROPERTY_NAME);
			v.removeChangeListener(this);
		}
		usedNumbers.remove((Integer)lockNumber);	
		
		list.clear();
	}
	
	
	public void stateChanged(ChangeEvent e) {
		
		View ori=(View)e.getSource();
		Map<String,Object> prop=((View)(e.getSource())).properties;
		if(prop.containsKey(LOCKER_PROPERTY_DO_NOT_PERFORM_EVENT )) // a good (not really in fact) old hack to avoid infinite loops...
		{
			prop.remove(LOCKER_PROPERTY_DO_NOT_PERFORM_EVENT );
		}
		else for (View v : list) {
			if(v!=ori)
			{
				v.properties.put(LOCKER_PROPERTY_DO_NOT_PERFORM_EVENT , null);
				v.copyAttribute(ori);
			}
		}
		
	}


	public int getLockNumber() {
		return lockNumber;
	}
	
	public int size(){
		return list.size();
	}
	
}
