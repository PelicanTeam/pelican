/**
 * 
 */
package fr.unistra.pelican.gui.MultiViews;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.Rectangle2D;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import net.miginfocom.swing.MigLayout;
import fr.unistra.pelican.Image;

/**
 * A Render for a mini-overview of a View.... 
 * Use it in JList or other graphic container and set the cellRenderer and the cellEditor to use the object itself
 * 
 * @author Benjamin Perret
 *
 */
public class MiniViewPanel extends JPanel implements ChangeListener, MouseMotionListener, MouseListener{

	/**
	 * the  serialVersionID....
	 */
	private static final long serialVersionUID = 6837769718326710070L;

	private View view;
	private int margy=20;
	boolean selected=false;

	private MultiView parent;
	private int px,py;
	private boolean drag=false;
	
	private Color background;
	private Color foreground;
	private JLabel label=new JLabel("Band:");
	private JSlider slider;
	private JPanel sliderP=new JPanel(new MigLayout());;
	int w;
	int h;
	Rectangle miniView=new Rectangle();
	boolean miniViewed=false;
	int bx;
	int by;
	double f;
	
	
	public double getFactor(){
		int bxO=12;
		int byO=12;
		java.awt.Image im=view.getDisplay();
		int ox= im.getWidth(null);
		int oy=im.getHeight(null);
		int sx=w-2*bxO;
		int sy=h-margy-2*byO;
		
		double f1=(double)(sx)/(double)(ox);
		double f2=(double)(sy)/(double)(oy);
		
		return  Math.min(f1,f2);
	}
	
	public boolean getCursor(int x, int y)
	{
		
		int bxO=12;
		int byO=12;
		if (view.getViewPort() != null) {
			java.awt.Image im=view.getDisplay();
			int ox= im.getWidth(null);
			int oy=im.getHeight(null);
			int sx=w-2*bxO;
			int sy=h-margy-2*byO;
			
			double f1=(double)(sx)/(double)(ox);
			double f2=(double)(sy)/(double)(oy);
			
			f= Math.min(f1,f2);
			
			sx=(int)((double)ox*f);
			sy=(int)((double)oy*f);
			
			bx = (w-sx)/2;
			by = (h-margy-sy)/2;
			Rectangle r = view.areaCovered();
			if (r.x != 0 || r.y != 0 || r.width != ox || r.height != oy) {
			
				miniView.x=bx + (int) (f * r.x);
				miniView.y=by + (int) (f * r.y);
				miniView.width=(int) (f * r.width);
				miniView.height=(int) (f * r.height);
				miniViewed=true;
				//System.out.println(x + " " +y + "  " +miniView);
				if(miniView.contains(x, y))
					return true;
			}
			else{ miniViewed=false;}
		}else {miniViewed=false;}
		return false;
	}
	
	public MiniViewPanel(MultiView parent,View v){
		
		super(new BorderLayout());
		
		this.parent=parent;
		
	   
	   
	    
		this.addMouseListener(this);
		this.addMouseMotionListener(this);
		slider=new JSlider(SwingConstants.HORIZONTAL,0,0,0);
		
		slider.setMajorTickSpacing(1);
		slider.setMinorTickSpacing(1);
		//slider.setPaintTicks(true);
		
		slider.setMaximumSize(new Dimension(200,20));
		sliderP.add(label);
		sliderP.add(slider);
		slider.addChangeListener(this);
		slider.setSnapToTicks(true);
		//slider.setPaintLabels(true);
		setView(v);
		//this.add(slider,BorderLayout.SOUTH);
		//img=new JPanel();
		//img.setSize(100,100);
		//this.add(img,BorderLayout.CENTER);
		
		//name=new JLabel("No Name");
		//name.setMinimumSize(new Dimension(2,margy));
		//name.setVerticalAlignment(JLabel.TOP);
		//name.setHorizontalAlignment(JLabel.CENTER);
		//this.add(name,BorderLayout.SOUTH);
		//System.out.println("new");
	}
	
	

    // This is the only method defined by ListCellRenderer.
    // We just reconfigure the JLabel each time we're called.

	private boolean added=false;
	
	
	private void toSlideOrNot()
	{
		if (view != null) {
			Image pim = view.getPersistentImage();
			int bdim = pim.bdim;
			if (bdim == 1) {
				if (added) {
					this.remove(sliderP);

					added = false;
				}
				// System.out.println("remove "+bdim + " " +pim);
			} else {
				if (!added) {
					this.add(sliderP, BorderLayout.SOUTH);

					added = true;

					// System.out.println("add " +bdim + " " +pim);
				}
				slider.setMaximum(pim.bdim - 1);
				slider.setValue(view.getDisplayedBand());
				if (view.isColoured()) {
					// System.out.println("enabled false");
					slider.setEnabled(false);
				} else {
					// System.out.println("enabled true");
					slider.setEnabled(true);

				}
			}
		}
	}
	
    /* (non-Javadoc)
	 * @see javax.swing.JComponent#paint(java.awt.Graphics)
	 */
	
	public void paint(Graphics g) {
		//System.out.println("paint " + this);
		super.paint(g);
		 final Color bg=new Color(210,210,210);
		 final Color bg1=new Color(230,230,230);
		 final Color tr=new Color(0,0,255,60);
		 final Color tr2=new Color(0,0,255,120);
		int bxO=12;
		int byO=12;
		
		//
		 
		
		if(view!=null)
			{
			toSlideOrNot();
			
			
			
			
			w=getWidth();
			h=getHeight()-((added)?sliderP.getHeight():0);
			g.setColor((selected)?bg:bg1);
			g.fillRoundRect(2, 2, w-4, h-4, 5, 5);
			java.awt.Image im=view.getDisplay();
			
			int ox= im.getWidth(null);
			int oy=im.getHeight(null);
			int sx=w-2*bxO;
			int sy=h-margy-2*byO;
			
			double f1=(double)(sx)/(double)(ox);
			double f2=(double)(sy)/(double)(oy);
			
			f= Math.min(f1,f2);
			
			sx=(int)((double)ox*f);
			sy=(int)((double)oy*f);
			
			bx = (w-sx)/2;
			by = (h-margy-sy)/2;
			g.setColor((selected)?bg1:bg);
			g.fill3DRect(bx-5, by-5, sx+10, sy+10,!selected);
			g.setColor(Color.black);
			g.drawRect(bx-1, by-1, sx+2, sy+2);
			
			g.drawImage(view.getDisplay(), bx, by, sx, sy, this);
			String name = view.getImage().getName();
			
			Object o=view.properties.get(ViewLocker.LOCKER_PROPERTY_NAME);
			if(o!=null && o instanceof ViewLocker)
			{
				String num= "" + ((ViewLocker)o).getLockNumber();
				g.drawImage(ViewLocker.LOCK_ICON, 5, 5, 15,15, this);
				g.drawString(num, 2, 12);
			}
			
			if (view.getViewPort() != null) {
				Rectangle r = view.areaCovered();
				if (r.x != 0 || r.y != 0 || r.width != ox || r.height != oy) {
					//System.out.println(r);
					g.setColor(tr);
					miniView.x=bx + (int) (f * r.x);
					miniView.y=by + (int) (f * r.y);
					miniView.width=(int) (f * r.width);
					miniView.height=(int) (f * r.height);
					g.fillRect(miniView.x, miniView.y,
							miniView.width, miniView.height);
					g.setColor(tr2);
					g.drawRect(miniView.x, miniView.y,
							miniView.width, miniView.height);
					g.setColor(Color.black);
					miniViewed=true;
					//System.out.println("pas else " +this);
				}
				else{ miniViewed=false;}
			}else {miniViewed=false;}
			//int lefth = h-sy-by-10;
			
			if(name==null)
				name="No Name";
			FontMetrics fm = g.getFontMetrics();
			Rectangle2D sn=fm.getStringBounds(name, g);
			int tx = (int)(w-sn.getWidth())/2;
			int ty = (int)(margy-10-sn.getHeight())/2;
			//System.out.println("lh " + lefth +"  sy " + sy + " by " +by+ " ty " +ty);
			g.drawString(name, tx, h-margy+ty+10);
			}
		
		//name.paint(g);
	}




	/*public Component getListCellRendererComponent(
      JList list,
      Object value,            // value to display
      int index,               // cell index
      boolean isSelected,      // is the cell selected
      boolean cellHasFocus)    // the list and the cell have the focus
    {
		// System.out.println("create");
		
		
		
       
		
	  this.view=(View)value;
       
      if(!view.isRegistredChangeListener(this) )
    		  view.addChangeListener(this);
		//name.setText(view.getImage().getName());
       selected=isSelected;
          if (isSelected) {
            setBackground(list.getSelectionBackground());
              setForeground(list.getSelectionForeground());
              
          }
        else {
              setBackground(list.getBackground());
              setForeground(list.getForeground());
          }
          setEnabled(list.isEnabled());
          setFont(list.getFont());
        setOpaque(true);
        return this;
    }*/




	
	public void stateChanged(ChangeEvent arg0) {
		Object source =arg0.getSource();
		if(source instanceof View)
		{
			parent.list.revalidate();
			parent.list.repaint();
		}
		else if(source == slider)
		{
			view.setDisplayedBand(slider.getValue());
			if(parent.imProfile.contains(view))
				parent.imProfile.refresh();
		}
		
		
	}




	
	public void mouseClicked(MouseEvent e) {
		
		
	}

	
	public void mouseEntered(MouseEvent e) {
		
		
	}

	
	public void mouseExited(MouseEvent e) {
	
		
	}
	
	boolean draggingMiniWindow=false;
	boolean draggingHold=false;
	Point draggingPos=new Point();
	//View draggingView;
	
	
	public void mousePressed(MouseEvent e) {
		parent.maybeShowPopup(e,e.getX()+this.getX(),e.getY()+this.getY());
		if(e.getModifiers() == MouseEvent.BUTTON1_MASK && getCursor( e.getX(), e.getY()))
		{

			draggingPos.x=e.getX();
			draggingPos.y=e.getY();
			draggingHold=true;
			setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR) );
		}
		
	}

	
	public void mouseReleased(MouseEvent e) {
		parent.maybeShowPopup(e,e.getX()+this.getX(),e.getY()+this.getY());
		setCursor(Cursor.getDefaultCursor());
			draggingHold=false;
			
		
			
	}

	
	
	public void mouseDragged(MouseEvent e) {
		if(draggingHold)
		{
			
			int ppx,ppy;
			ppx=e.getX();
			ppy=e.getY();
			double f=1.0/getFactor()*view.getZoom();
			view.shiftX((int)(f*(double)(draggingPos.x-ppx)));
			view.shiftY((int)(f*(double)(draggingPos.y-ppy)));
			draggingPos.x=ppx;
			draggingPos.y=ppy;
			
		}
		
	}

	
	
	public void mouseMoved(MouseEvent e) {
		
		//int ind=list.locationToIndex(e.getPoint());
		//Rectangle r=list.getCellBounds(ind, ind);
		//draggingView=(View)viewList.get(ind);
		
		/*int x=e.getX();
		int y=e.getY();
		
		
		if(getCursor( x, y))
		{
			//System.out.println("in");
			draggingMiniWindow=true;
			parent.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		}
		else {
			//System.out.println("out");
			parent.setCursor(Cursor.getDefaultCursor());
			draggingMiniWindow=false;
		}*/
	}

	public boolean isSelected() {
		return selected;
	}

	public void setSelected(boolean selected) {
		this.selected = selected;
		
	}

	public void setBackground(Color background) {
		this.background = background;
		super.setBackground(background);
		//if(sliderP!=null)sliderP.setBackground(background);
	}

	public void setForeground(Color foreground) {
		this.foreground = foreground;
		super.setForeground(foreground);
		//if(sliderP!=null)sliderP.setForeground(foreground);
	}

	public View getView() {
		return view;
	}

	public void setView(View view) {
		this.view = view;
		 if(!view.isRegistredChangeListener(this) )
		    	view.addChangeListener(this);
		 toSlideOrNot();
	}
	
}
