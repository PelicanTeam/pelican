/**
 * 
 */
package fr.unistra.pelican.gui.MultiViews;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;

import javax.swing.BoundedRangeModel;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.MenuElement;
import javax.swing.MenuSelectionManager;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeListener;

import com.sun.media.ui.Slider;

/**
 * A JSlider that can be used as menu item ...
 * 
 * @author Benjamin Perret
 *
 */
class SliderMenuItem extends JPanel implements MenuElement {


	JSlider slider;
	
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * @param brm
	 */
	public SliderMenuItem(String text, BoundedRangeModel brm) {
		slider=new JSlider(brm);
		init(text);
	}




	/**
	 * @param min
	 * @param max
	 * @param value
	 */
	public SliderMenuItem(String text,int min, int max, int value) {
		slider=new JSlider(min,max,value);
		init(text);
	}



	/**
	 * @param min
	 * @param max
	 */
	public SliderMenuItem(String text,int min, int max) {
		slider=new JSlider(min,max);
		init(text);
	}


	private void init(String text)
	{
		this.add(new JLabel(text));
		Dimension d=slider.getPreferredSize();
		d.height=45;
		d.width=150;
		slider.setPreferredSize(d);
		slider.setMajorTickSpacing(5);
		slider.setMinorTickSpacing(1);
		slider.setPaintTicks(true);
		slider.setPaintLabels(true);
		
		this.add(slider);
	}

	
	
	public JSlider getSlider(){
		return slider;
	}
	
	public void addChangeListener(ChangeListener cl)
	{
		slider.addChangeListener(cl);
	}

	/* (non-Javadoc)
     * @see javax.swing.MenuElement#processMouseEvent(java.awt.event.MouseEvent, javax.swing.MenuElement[], javax.swing.MenuSelectionManager)
     */
    public void processMouseEvent(MouseEvent e, MenuElement path[], MenuSelectionManager manager) {
    }

    /* (non-Javadoc)
     * @see javax.swing.MenuElement#processKeyEvent(java.awt.event.KeyEvent, javax.swing.MenuElement[], javax.swing.MenuSelectionManager)
     */
    public void processKeyEvent(KeyEvent e, MenuElement path[], MenuSelectionManager manager) {
    }

    /* (non-Javadoc)
     * @see javax.swing.MenuElement#menuSelectionChanged(boolean)
     */
    public void menuSelectionChanged(boolean isIncluded) {
    }

    /* (non-Javadoc)
     * @see javax.swing.MenuElement#getSubElements()
     */
    public MenuElement[] getSubElements() {
      return new MenuElement[0];
    }

    /* (non-Javadoc)
     * @see javax.swing.MenuElement#getComponent()
     */
    public Component getComponent() {
      return this;
    }
  }
