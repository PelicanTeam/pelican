/**
 * 
 */
package fr.unistra.pelican.gui.MultiViews;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.LayoutManager;

/**
 * All components in the layout are superposed...
 * 
 * @author Benjamin Perret
 *
 */
public class OverlappingLayout implements LayoutManager {

	
	
	private int minWidth = 0, minHeight = 0;
    private int preferredWidth = 0, preferredHeight = 0;
    private boolean sizeUnknown = true;

	
	/**
	 * 
	 */
	public OverlappingLayout() {
		
	}

	private void setSizes(Container parent) {
        int nComps = parent.getComponentCount();
        Dimension d = null;

        //Reset preferred/minimum width and height.
        preferredWidth = 0;
        preferredHeight = 0;
        minWidth = 0;
        minHeight = 0;

        for (int i = 0; i < nComps; i++) {
            Component c = parent.getComponent(i);
            if (c.isVisible()) {
                d = c.getPreferredSize();
                preferredWidth=Math.max(preferredWidth, d.width);
                preferredHeight =Math.max(preferredWidth, d.height);
                d=c.getMinimumSize();
                minWidth=Math.max(minWidth,d.width);
                minHeight=Math.max(minHeight,d.height);
                

            }
        }
        sizeUnknown=false;
    }

	
	/* (non-Javadoc)
	 * @see java.awt.LayoutManager#addLayoutComponent(java.lang.String, java.awt.Component)
	 */
	
	public void addLayoutComponent(String name, Component comp) {
	

	}

	/* (non-Javadoc)
	 * @see java.awt.LayoutManager#layoutContainer(java.awt.Container)
	 */
	
	public void layoutContainer(Container parent) {
		 Insets insets = parent.getInsets();
	        int maxWidth = parent.getWidth()
	                       - (insets.left + insets.right);
	        int maxHeight = parent.getHeight()
	                        - (insets.top + insets.bottom);
	        int nComps = parent.getComponentCount();
	        // Go through the components' sizes, if neither
	        // preferredLayoutSize nor minimumLayoutSize has
	        // been called.
	        if (sizeUnknown) {
	            setSizes(parent);
	        }
	        for (int i = 0; i < nComps; i++) {
	            Component c = parent.getComponent(i);
	            if (c.isVisible()) {
	               c.setBounds(insets.left, insets.top, maxWidth, maxHeight);
	                

	            }
	        }

	}

	/* (non-Javadoc)
	 * @see java.awt.LayoutManager#minimumLayoutSize(java.awt.Container)
	 */
	
	public Dimension minimumLayoutSize(Container parent) {
		 Dimension dim = new Dimension(0, 0);
	        

	        //Always add the container's insets!
	        Insets insets = parent.getInsets();
	        dim.width = minWidth
	                    + insets.left + insets.right;
	        dim.height = minHeight
	                     + insets.top + insets.bottom;

	        sizeUnknown = false;

	        return dim;

	}

	/* (non-Javadoc)
	 * @see java.awt.LayoutManager#preferredLayoutSize(java.awt.Container)
	 */
	
	public Dimension preferredLayoutSize(Container parent) {
		Dimension dim = new Dimension(0, 0);
      

        setSizes(parent);

        //Always add the container's insets!
        Insets insets = parent.getInsets();
        dim.width = preferredWidth
                    + insets.left + insets.right;
        dim.height = preferredHeight
                     + insets.top + insets.bottom;

        sizeUnknown = false;

        return dim;

	}

	/* (non-Javadoc)
	 * @see java.awt.LayoutManager#removeLayoutComponent(java.awt.Component)
	 */
	
	public void removeLayoutComponent(Component comp) {
		// TODO Auto-generated method stub

	}

	

}
