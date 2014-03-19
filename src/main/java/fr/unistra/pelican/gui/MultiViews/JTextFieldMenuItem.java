/**
 * 
 */
package fr.unistra.pelican.gui.MultiViews;

import java.awt.Component;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.MenuElement;
import javax.swing.MenuSelectionManager;

/**
 * @author Benjamin Perret
 *
 */
public class JTextFieldMenuItem extends JPanel implements MenuElement {


	JTextField field;
	JLabel label;
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * @param brm
	 */
	public JTextFieldMenuItem(String text, String init) {
		
		
		field= new JTextField(5);
		field.setText(init);
		field.setEnabled(true);
		field.setEditable(true);
		label=new JLabel(text);
		this.add(label);
		this.add(field);
		
	}










	/**
	 * @return
	 * @see javax.swing.text.JTextComponent#getText()
	 */
	public String getText() {
		return field.getText();
	}










	/**
	 * @param t
	 * @see javax.swing.text.JTextComponent#setText(java.lang.String)
	 */
	public void setText(String t) {
		field.setText(t);
	}










	public void setEnabled(boolean f)
	{
		super.setEnabled(f);
		field.setEnabled(f);
		label.setEnabled(f);
	}
	
	
	
	public void addActionListener(ActionListener cl)
	{
		field.addActionListener(cl);
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
    	field.setSelectionStart(0);
    	field.setSelectionEnd(field.getText().length());
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
