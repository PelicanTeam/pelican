package fr.unistra.pelican.gui.MultiViews;

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.Vector;

import javax.swing.AbstractButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.event.ChangeEvent;


/**
 * If you want to ensure that no more than a given number of buttons (CheckBox, RadioButtons...) are checked in a group.
 * 
 * @author Benjamin Perret
 *
 */
public class MultiButtonGroup implements ItemListener
{
	private int maxSelect=2;
	
	private Vector<AbstractButton> queue;
	
	ArrayList<AbstractButton> list =new ArrayList<AbstractButton>();
	
	public MultiButtonGroup()
	{
		this(2);
	}
	
	public MultiButtonGroup(int max)
	{
		this.maxSelect=Math.max(1, max);
		queue = new Vector<AbstractButton>(maxSelect);
	}
	
	
	public int getNumberOfSelectedButtons()
	{
		return queue.size();
	}
	
	public void add(AbstractButton button)
	{
		list.add(button);
		if(button.isSelected())
			humSelected(button);
		button.addItemListener(this);
		
		
	}
	
	public void remove(AbstractButton button)
	{
		queue.remove(button);
		if(list.remove(button))
			button.removeItemListener(this);;
	}
	
	public void clear()
	{
		queue.clear();
		list.clear();
	}
	
	public int size(){
		return list.size();
	}

	
	private void humSelected(AbstractButton b)
	{
		queue.add(b);
		if (queue.size() > maxSelect) {

			
			queue.firstElement().setSelected(false);
			
		}
	}
	
	
	public void itemStateChanged(ItemEvent e) {
		
		if (e.getStateChange() == e.SELECTED
				|| e.getStateChange() == e.DESELECTED) {
			AbstractButton b = (AbstractButton) e.getSource();
			if (b.isSelected()) {
				 humSelected(b);
				

			} else {
				queue.remove(b);
			}
		}

	}
	
	public void stateChanged(ChangeEvent e) {
		
		
	}
	
	public static void main(String [] args)
	{
		int nb=10;
		JFrame frame = new JFrame();
		JPanel pan = new JPanel();
		frame.add(pan);
		MultiButtonGroup mbg = new MultiButtonGroup(2);
		for(int i=0;i<nb;i++)
		{
			JCheckBox j = new JCheckBox("button " + i);
			mbg.add(j);
			pan.add(j);
			
		}
		frame.pack();
		frame.setVisible(true);
	}

	
	
	
}

