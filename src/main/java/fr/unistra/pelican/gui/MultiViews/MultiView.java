/**
 * 
 */
package fr.unistra.pelican.gui.MultiViews;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.AbstractCellEditor;
import javax.swing.DebugGraphics;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFrame;
import javax.swing.JLayeredPane;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.ScrollPaneConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;

import fr.unistra.pelican.AlgorithmException;
import fr.unistra.pelican.Image;
import fr.unistra.pelican.InvalidParameterException;
import fr.unistra.pelican.algorithms.io.ImageLoader;

/**
 * A JPanel with all needed for the multiview sytem, yahou!
 * 
 * @author Benjamin Perret
 *
 */
public class MultiView extends JPanel implements ChangeListener, ListSelectionListener, ActionListener, MouseListener, MouseMotionListener, DropTargetListener{

	public static final String RESSOURCES_PATH="java/fr/unistra/pelican/gui/MultiViews/ressources/";
	
	/**
	 * View Locker to make some views follow each other
	 */
	private ArrayList<ViewLocker> lockerList=new ArrayList<ViewLocker>();
	
	/**
	 * List of views
	 */
	private ArrayList<MiniViewPanel> viewList=new ArrayList<MiniViewPanel>();
	
	/**
	 * The panel to display image
	 */
	private ImagePanel ipanel= new ImagePanel();
	
	/**
	 * The left table containing miniviews of view
	 */
	protected JTable list;//=new JTable(viewList);
	
	/**
	 * The popup for the table
	 */
	private JPopupMenu listPopup=new JPopupMenu();
	private JMenuItem remove = new JMenuItem("Remove"); 
	private JCheckBoxMenuItem lock = new JCheckBoxMenuItem("Lock Views"); 
	JMenu profileSub;
	JCheckBoxMenuItem showProfileView;
	JCheckBoxMenuItem showImageInProfile;
	
	/**
	 * The magic panel to display things over the image panel
	 */
	private MagicPanel mPanel;
	
	/**
	 * The special renderer for the table
	 */
	ViewListCellRenderer vcr;
	
	/**
	 * The tool to display profiles view
	 */
	protected ImProfile imProfile=new ImProfile();
	private DropTarget dropTarget;
	public MultiView()
	{
		super(new BorderLayout());
		vcr=new ViewListCellRenderer();
		list = new JTable(new ViewTableModel());
		list.setDefaultRenderer(MiniViewPanel.class, vcr);
		list.setSize(160, this.getHeight());
		list.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		//list.setDebugGraphicsOptions(DebugGraphics.LOG_OPTION);
		JScrollPane listScroller = new JScrollPane(list);
		listScroller.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		listScroller.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		listScroller.setPreferredSize(new Dimension(150, 80));
		//list.setFixedCellWidth(130);
		//list.setFixedCellHeight(140);
		//list.addListSelectionListener(this);
		TableColumn col = list.getColumnModel().getColumn(0);
	    col.setCellEditor(new MyTableCellEditor());

		list.getSelectionModel().addListSelectionListener(this);
		list.setRowHeight(140);
		//list.add
		//ipanel.setForeground(Color.red);
		list.addMouseListener(this);
		//list.addMouseMotionListener(this);
		mPanel = new MagicPanel(ipanel,null);
		this.add(mPanel,BorderLayout.CENTER);
		this.add(listScroller,BorderLayout.EAST);
		dropTarget=new DropTarget(this, DnDConstants.ACTION_COPY_OR_MOVE,
		        this, true, null);
		createListPopup();
	}

	private void createListPopup(){
		listPopup.add(lock);
		//listPopup.add(unlock);
		//listPopup.add(unlockAll);
		
		listPopup.add(new JPopupMenu.Separator());
		 profileSub=new JMenu("Profile View");
		 listPopup.add(profileSub);
		 showProfileView=new JCheckBoxMenuItem("Show Profile View");
		 showProfileView.setSelected(false);
		 showProfileView.addActionListener(this);
		 profileSub.add(showProfileView);
		 
		showImageInProfile=new JCheckBoxMenuItem("Image profile");
		showImageInProfile.setSelected(false);
		showImageInProfile.addActionListener(this);
		profileSub.add(showImageInProfile);
		listPopup.add(new JPopupMenu.Separator());
		listPopup.add(remove);
		
		lock.addActionListener(this);
		//unlock.addActionListener(this);
		//unlockAll.addActionListener(this);
		remove.addActionListener(this);
	}
	
	private ViewLocker getLock(View v)
	{
		ViewLocker lock=null;
		Object o=v.properties.get(ViewLocker.LOCKER_PROPERTY_NAME);
		if(o!=null && o instanceof ViewLocker)
		{
			lock=(ViewLocker)o;
		}
		return lock;
	}
	
	private int getLockNumber(View v)
	{
		ViewLocker lock=getLock(v);
		return (lock!=null)?lock.getLockNumber():-1;
	}
	
	
	
	private void enabledPopup()
	{
		int ind [] =list.getSelectedRows();
		if(ind.length>0)
		{
			remove.setEnabled(true);
			lock.setEnabled(true);
			showProfileView.setSelected(imProfile.isVisible());
			if(ind.length==1)
			{
				showImageInProfile.setEnabled(true);
				//System.out.println("ind " + ind[0] +"  v " +viewList.get(ind[0]));
				showImageInProfile.setSelected(imProfile.contains(viewList.get(ind[0]).getView()));
			}else showImageInProfile.setEnabled(false);
			
			int lnum = getLockNumber(((MiniViewPanel)viewList.get(ind[0])).getView());
			boolean same=true;
			for (int i = 1; i < ind.length && same; i++) {
				int lnum2 = getLockNumber(((MiniViewPanel)viewList.get(ind[i])).getView());
				if(lnum != lnum2)
					same=false;
			}
			if(ind.length==1 && lnum==-1)
			{lock.setForeground(Color.black);
				lock.setSelected(false);
				lock.setEnabled(false);
			}
			else if(same)
			{
				lock.setSelected(lnum!=-1);
				lock.setForeground(Color.black);
			}else
			{
				
				lock.setSelected(true);
				
				lock.setForeground(Color.LIGHT_GRAY);
			}
			
		}else {
			remove.setEnabled(false);
			lock.setEnabled(false);
		}
	}
	
	public void dispose(){
		if(imProfile!=null && imProfile.frame!=null)
			imProfile.frame.dispose();
		
	}
	
	private int findViewIndex(View v)
	{
		int i;
		for(i=0;i<viewList.size();i++)
		{
			if(((MiniViewPanel)viewList.get(i)).getView() == v)
				return i;
		}
		return -1;	
	}
	
	
	
	private MiniViewPanel findMiniView(View v)
	{
		
		for(int i=0;i<viewList.size();i++)
		{
			MiniViewPanel m =(MiniViewPanel)viewList.get(i);
			if(m.getView() == v)
				return m;
		}
		return null;	
	}
	
	private void remove(View v)
	{
		unlock(v);
		if(imProfile.remove(v))
			imProfile.refresh();
		viewList.remove(findMiniView(v));
		
	}
	
	private void unlock(View v)
	{
		ViewLocker lock=getLock(v);
		if(lock!=null)
		{
			lock.remove(v);
			if (lock.size()==1)
			{
				lock.clear();
				lockerList.remove(lock);
			}
				
		}
	}
	
	/**
	 * @param e
	 * @return
	 * @see java.util.ArrayList#add(java.lang.Object)
	 */
	public void add(View e) {
		//e.addChangeListener(this);
		MiniViewPanel mv=new MiniViewPanel(this,e);
		viewList.add(mv);
		list.invalidate();
		list.revalidate();
		list.repaint();
		if(viewList.size() == 1)
		{

			list.setRowSelectionInterval(0, 0);
			
			
		}else{
			list.revalidate();
		}
		

	}

	public void updateProfile()
	{
		imProfile.refresh();
	}
	
	public void clear(){
		viewList.clear();
	}
	
	/**
	 * @param e
	 * @return
	 */
	public View add(Image e) {
		View v=new View(null);
		v.setImage(e);
		add(v);
		return v;//list.validate();

	}
	
	/**
	 * @param e
	 * @return
	 */
	public View add(BufferedImage e) {
		View v=new View(null);
		v.setImage(e);
		
		add(v);
		return v;//list.validate();

	}
	
	/**
	 * @param e
	 * @return
	 */
	public View add(BufferedImage e, String name) {
		View v=new View(null);
		v.setImage(e);
		v.getImage().setName(name);
		add(v);
		return v;//list.validate();

	}
	
	/**
	 * @param e
	 * @return
	 * @see java.util.ArrayList#add(java.lang.Object)
	 */
	public View add(Image e, String name) {
		e.setName(name);
		View v=new View(null);
		v.setImage(e);
		add(v);
		return v;//list.validate();

	}
	
	public void add(Image ...images ){
		for(Image im:images)
			add(im);
	}
	

	/**
	 * @param o
	 * @return
	 * @see java.util.ArrayList#contains(java.lang.Object)
	 */
	/*public boolean contains(Object o) {
		return viewList.contains(o);
	}*/

	/**
	 * @param index
	 * @return
	 * @see java.util.ArrayList#get(int)
	 */
	public View get(int index) {
		return ((MiniViewPanel)viewList.get(index)).getView();
	}
	
	/**
	 * @param index
	 * @return
	 * @see java.util.ArrayList#get(int)
	 */
	public int nbViews() {
		return viewList.size();
	}

	public void refreshAll()
	{
		for(int i=0;i<viewList.size();i++)
		{
			get(i).refresh();
		}
		mPanel.repaint();
		if(imProfile.isVisible())
			imProfile.refresh();
	}
	
	
	public void addImage(Image img) {
		View v =new View(ipanel);
		v.setImage(img);
		this.add(v);
	}
	
	public static void main(String [] args)
	{
		
		JFrame frame = new JFrame("Mouhaha");
		MultiView panel=new MultiView();
		Image im=ImageLoader.exec("samples/AstronomicalImagesFITS/img1-12.fits");
		im.setName("img1-12");
		panel.addImage(im);
		
		im=ImageLoader.exec("samples/AstronomicalImagesFITS/img1-10.fits");
		im.setName("img1-10");
		panel.addImage(im);
		
		
		im=ImageLoader.exec("samples/lenna512.png");
		im.setName("Lenna");
		panel.addImage(im);
		
		im=ImageLoader.exec("samples/horse2.png");
		im.setName("Chwal");
		panel.addImage(im);
		
		/*im=ImageLoader.exec("samples/curious.png");
		im.setName("HumHum");
		panel.addImage(im);
		
		im=ImageLoader.exec("samples/blobs.png");
		im.setName("blop blop");
		panel.addImage(im);
		
		im=ImageLoader.exec("samples/camera.png");
		im.setName("Camocam");
		panel.addImage(im);
		
		im=ImageLoader.exec("samples/monsters.png");
		im.setName("Monsre & co");
		panel.addImage(im);*/
		
		frame.add(panel);
		frame.setSize(800, 800);
		//frame.pack();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setVisible(true);
	}

	
	public void valueChanged(ListSelectionEvent e) {
		
		View v=viewList.get(list.getSelectedRow()).getView();
		ipanel.setView(v);
		
	}

	private boolean maybeShowPopup(MouseEvent e) {
		return maybeShowPopup(e,e.getX(),e.getY()) ;
			
    }
	
	public boolean maybeShowPopup(MouseEvent e, int x, int y) {
		boolean res=false;
        if (listPopup.isPopupTrigger(e)) {
        	int row=list.rowAtPoint(new Point(x,y));
        	if(row==-1)
        		return false;
        	int [] ind = list.getSelectedRows();
        	boolean flag=false;
        	for(int i=0;i<ind.length && !flag;i++)
        		if(ind[i]==row)
        			flag=true;
        	if(!flag)
        		list.setRowSelectionInterval(row, row);
        	enabledPopup();
        	
        	listPopup.show(e.getComponent(),
                       e.getX(), e.getY());
        	res=true;
        }
        return res;
    }
	public void lockAllViews(){
		
		for (int i = 0; i < viewList.size(); i++) {
			unlock(get(i));
		}
		ViewLocker lock =new ViewLocker();
		lockerList.add(lock);
		for (int i = 0; i < viewList.size(); i++) {
			lock.add(get(i));
		}
	}
	
	private void lockSelected()
	{
		int ind [] = list.getSelectedRows();
		int lnum=lockedTogether(ind);
		if(lnum==-1)
		{
			unlock(ind);
			ViewLocker lock =new ViewLocker();
			lockerList.add(lock);
			for (int i = 0; i < ind.length; i++) {
				lock.add(get(ind[i]));
			}
		}
	}
	
	private int lockedTogether(int [] ind)
	{
		int res=-1;
		if(ind!=null )
		{
		res = getLockNumber(get(ind[0]));
		boolean same=true;
		for (int i = 1; i < ind.length && same; i++) {
			int lnum2 = getLockNumber(get(ind[i]));
			if(res != lnum2 )
				same=false;
		}
		}
		return res;
	}
	
	private void unlock(int [] ind)
	{
		
		
		for (int i = 0; i < ind.length; i++) 
			unlock(get(ind[i]));
	}
	
	private void unlockSelected()
	{
		int ind [] = list.getSelectedRows();
		
		unlock(ind);
	}
	
	private void lockOrUnlock()
	{
		if (lock.isSelected())
			lockSelected();
		else unlockSelected();
	}
	
	private void removeSelected()
	{
		int ind [] = list.getSelectedRows();
		Arrays.sort(ind);
	
		for (int i = ind.length-1; i >=0; i--) {
			{
				
				remove(get(ind[i]));
			}
			
		}
		
		ipanel.setView(null);
		if(viewList.size()==0)
			list.selectAll();
		else{list.setRowSelectionInterval(0,0);}
		list.updateUI();
		list.revalidate();
		list.repaint();
	}
	
	
	public void showImProfile(boolean show){
		if(show)
		{
			
			imProfile.setVisible(true,new Point(this.getLocation().x+this.getWidth()+10,this.getLocation().y));
			mPanel.setFg(imProfile);
			ipanel.repaint();
		}else {
			imProfile.setVisible(false);
			mPanel.setFg(null);
			ipanel.repaint();
		}
	}
	
	public void actionPerformed(ActionEvent e) {
		JMenuItem item=(JMenuItem)e.getSource();
		if(item==remove)
			removeSelected();
		else if(item == lock)
		{
		lockOrUnlock();
		list.validate();
		list.repaint();
		}else if (item== showProfileView)
		{
			showImProfile(showProfileView.isSelected());
		} else if (item== showImageInProfile)
		{
			if(showImageInProfile.isSelected())
			{
				
				imProfile.add(get(list.getSelectedRow()));
				if(showProfileView.isSelected()==false)
				{
					showProfileView.setSelected(true);
					showImProfile(true);
					/*imProfile.setVisible(true);
					mPanel.setFg(imProfile);
					ipanel.repaint();*/
				}
			}else {
				imProfile.remove(get(list.getSelectedRow()));
			}

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
	View draggingView;
	public void mousePressed(MouseEvent e) {
		maybeShowPopup(e);
		/*int ind=list.locationToIndex(e.getPoint());
		if(ind !=-1)
		{
			Rectangle r=list.getCellBounds(ind, ind);
			MiniViewPanel m =(MiniViewPanel)viewList.get(ind);
		
			m.mousePressed(convert(e,r));
		}*/
		/*if(e.getModifiers() == MouseEvent.BUTTON1_MASK)
		{
			int ind=list.locationToIndex(e.getPoint());
			draggingView=(View)viewList.get(ind);
			draggingPos.x=e.getX();
			draggingPos.y=e.getY();
			draggingHold=true;
			setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR) );
		}*/
		
	}

	
	public void mouseReleased(MouseEvent e) {
		maybeShowPopup(e);
		/*int ind=list.locationToIndex(e.getPoint());
		if(ind !=-1)
		{
			Rectangle r=list.getCellBounds(ind, ind);
			MiniViewPanel m =(MiniViewPanel)viewList.get(ind);
			m.mouseReleased(convert(e,r));
		}*/
	/*	if(e.getModifiers() == MouseEvent.BUTTON1_MASK)
		{
			draggingHold=false;
			mouseMoved(e) ;
		}*/
			
	}

	
	
	public void mouseDragged(MouseEvent e) {
		/*int ind=list.locationToIndex(e.getPoint());
		if(ind !=-1)
		{
			Rectangle r=list.getCellBounds(ind, ind);
			MiniViewPanel m =(MiniViewPanel)viewList.get(ind);
			m.mouseDragged(convert(e,r));
		}*/
	/*	if(draggingHold)
		{
			
			int ppx,ppy;
			ppx=e.getX();
			ppy=e.getY();
			double f=1.0/vcr.getFactor()*draggingView.getZoom();
			draggingView.shiftX((int)(f*(double)(draggingPos.x-ppx)));
			draggingView.shiftY((int)(f*(double)(draggingPos.y-ppy)));
			draggingPos.x=ppx;
			draggingPos.y=ppy;
			repaint();
		}*/
		
	}

	private MouseEvent convert(MouseEvent e, Rectangle r)
	{
		return new MouseEvent(list,e.getID(),e.getWhen(),e.getModifiers(),e.getX()-r.x,e.getY()-r.y,e.getX(),e.getY(),e.getClickCount(),e.isPopupTrigger(),e.getButton());
	}
	
	public void mouseMoved(MouseEvent e) {
		/*int ind=list.locationToIndex(e.getPoint());
		if(ind !=-1)
		{
			Rectangle r=list.getCellBounds(ind, ind);
			MiniViewPanel m =(MiniViewPanel)viewList.get(ind);
			m.mouseMoved(convert(e,r));
		}*/
		//int ind=list.locationToIndex(e.getPoint());
		//Rectangle r=list.getCellBounds(ind, ind);
		//draggingView=(View)viewList.get(ind);
		
	/*	int x=e.getX();
		int y=e.getY();
		;
		int ind=list.locationToIndex(e.getPoint());
		Rectangle r=list.getCellBounds(ind, ind);
		draggingView=(View)viewList.get(ind);
		if(vcr.getCursor(draggingView, x-r.x, y-r.y))
		{
			
			draggingMiniWindow=true;
			list.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		}
		else {
			list.setCursor(Cursor.getDefaultCursor());
			draggingMiniWindow=false;
		}*/
	}

	@Override
	public void stateChanged(ChangeEvent e) {
		/*if(e.getSource() instanceof View)
		{
			if ((View)e.getSource() == ipanel.getView())
				mPanel.repaint();
		}*/
		
	}

	class ViewTableModel extends AbstractTableModel {

		public String getColumnName(int col) {
			return "";
		}

		public int getRowCount() {
			return viewList.size();
		}

		public int getColumnCount() {
			return 1;
		}

		public Object getValueAt(int row, int col) {
			return viewList.get(row);
		}

		public boolean isCellEditable(int row, int col) {
			return true;
		}

		@Override
		public Class<?> getColumnClass(int columnIndex) {

			return MiniViewPanel.class;
		}

		/*
		 * public void setValueAt(Object value, int row, int col) {
		 * rowData[row][col] = value; fireTableCellUpdated(row, col); }
		 */
	}

	// Implementation of the DropTargetListener interface
	public void dragEnter(DropTargetDragEvent dtde) {
		DnDUtils.debugPrintln("dragEnter, drop action = "
				+ DnDUtils.showActions(dtde.getDropAction()));

		// Get the type of object being transferred and determine
		// whether it is appropriate.
		checkTransferType(dtde);

		// Accept or reject the drag.
		acceptOrRejectDrag(dtde);
	}

	public void dragExit(DropTargetEvent dte) {
		DnDUtils.debugPrintln("DropTarget dragExit");
	}

	public void dragOver(DropTargetDragEvent dtde) {
		DnDUtils.debugPrintln("DropTarget dragOver, drop action = "
				+ DnDUtils.showActions(dtde.getDropAction()));

		// Accept or reject the drag
		acceptOrRejectDrag(dtde);
	}

	public void dropActionChanged(DropTargetDragEvent dtde) {
		DnDUtils.debugPrintln("DropTarget dropActionChanged, drop action = "
				+ DnDUtils.showActions(dtde.getDropAction()));

		// Accept or reject the drag
		acceptOrRejectDrag(dtde);
	}

	public void drop(DropTargetDropEvent dtde) {
		DnDUtils.debugPrintln("DropTarget drop, drop action = "
				+ DnDUtils.showActions(dtde.getDropAction()));

		// Check the drop action
		if ((dtde.getDropAction() & DnDConstants.ACTION_COPY_OR_MOVE) != 0) {
			// Accept the drop and get the transfer data
			dtde.acceptDrop(dtde.getDropAction());
			Transferable transferable = dtde.getTransferable();

			try {
				boolean result = dropComponent(transferable);

				dtde.dropComplete(result);
				DnDUtils.debugPrintln("Drop completed, success: " + result);
			} catch (Exception e) {
				DnDUtils.debugPrintln("Exception while handling drop " + e);
				dtde.dropComplete(false);
			}
		} else {
			DnDUtils.debugPrintln("Drop target rejected drop");
			dtde.rejectDrop();
		}
	}

	// Internal methods start here

	protected boolean acceptableType; // Indicates whether data is acceptable

	protected DataFlavor targetFlavor; // Flavor to use for transfer

	protected boolean acceptOrRejectDrag(DropTargetDragEvent dtde) {
		int dropAction = dtde.getDropAction();
		int sourceActions = dtde.getSourceActions();
		boolean acceptedDrag = false;

		DnDUtils.debugPrintln("\tSource actions are "
				+ DnDUtils.showActions(sourceActions) + ", drop action is "
				+ DnDUtils.showActions(dropAction));

		// Reject if the object being transferred
		// or the operations available are not acceptable.
		if (!acceptableType
				|| (sourceActions & DnDConstants.ACTION_COPY_OR_MOVE) == 0) {
			DnDUtils.debugPrintln("Drop target rejecting drag");
			dtde.rejectDrag();
		} else if ((dropAction & DnDConstants.ACTION_COPY_OR_MOVE) == 0) {
			// Not offering copy or move - suggest a copy
			DnDUtils.debugPrintln("Drop target offering COPY");
			dtde.acceptDrag(DnDConstants.ACTION_COPY);
			acceptedDrag = true;
		} else {
			// Offering an acceptable operation: accept
			DnDUtils.debugPrintln("Drop target accepting drag");
			dtde.acceptDrag(dropAction);
			acceptedDrag = true;
		}

		return acceptedDrag;
	}

	protected void checkTransferType(DropTargetDragEvent dtde) {
		// Only accept a flavor that returns a Component
		acceptableType = false;
		DataFlavor[] fl = dtde.getCurrentDataFlavors();
		for (int i = 0; i < fl.length; i++) {
			// Class dataClass = fl[i].getRepresentationClass();
			if (fl[i].isMimeTypeEqual("application/x-java-file-list")) {
				// This flavor returns a Component - accept it.
				targetFlavor = fl[i];
				acceptableType = true;
				break;
			}
		}

		DnDUtils.debugPrintln("File type acceptable - " + acceptableType);
	}

	protected boolean dropComponent(Transferable transferable)
			throws IOException, UnsupportedFlavorException {
		Object o = transferable.getTransferData(targetFlavor);
		if (o instanceof List) {
			DnDUtils.debugPrintln("Dragged component class is "
					+ o.getClass().getName());
			List l = (List) o;
			for (Object a : l) {
				File f = (File) a;
				try {
					Image im = ImageLoader.exec(f.getAbsolutePath());
					im.setName(f.getName());
					this.add(im);
					this.list.setRowSelectionInterval(list.getRowCount() - 1,
							list.getRowCount() - 1);
				} catch (AlgorithmException e) {
					System.err.println("Cannot read " + f
							+ ". Error message was " + e);
				} catch (InvalidParameterException e) {
					System.err.println("Cannot read " + f
							+ ". Error message was " + e);
				}
			}
			// pane.add((Component) o);
			// pane.validate();
			return true;
		}
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.swing.JComponent#setVisible(boolean)
	 */
	@Override
	public void setVisible(boolean flag) {
		super.setVisible(flag);
		if (flag == false)
			imProfile.setVisible(false);
	}

	/**
	 * @return the imProfile
	 */
	public ImProfile getImProfile() {
		return imProfile;
	}

}

class DnDUtils {
	public static String showActions(int action) {
		String actions = "";
		if ((action & (DnDConstants.ACTION_LINK | DnDConstants.ACTION_COPY_OR_MOVE)) == 0) {
			return "None";
		}

		if ((action & DnDConstants.ACTION_COPY) != 0) {
			actions += "Copy ";
		}

		if ((action & DnDConstants.ACTION_MOVE) != 0) {
			actions += "Move ";
		}

		if ((action & DnDConstants.ACTION_LINK) != 0) {
			actions += "Link";
		}

		return actions;
	}

	public static boolean isDebugEnabled() {
		return debugEnabled;
	}

	public static void debugPrintln(String s) {
		if (debugEnabled) {
			System.out.println(s);
		}
	}

	private static boolean debugEnabled = false;
}

@SuppressWarnings("serial")
class MagicPanel extends JPanel implements ChangeListener {
	JPanel fg;

	ImagePanel bg;
	JLayeredPane layer;

	public MagicPanel(ImagePanel background, JPanel foreground) {
		super(new BorderLayout());

		layer = new JLayeredPane();
		layer.setLayout(new OverlappingLayout());
		// layer.setPreferredSize(new Dimension(300, 310));
		// layer.setBorder(BorderFactory.createTitledBorder(
		// "Move the Mouse to Move Duke"));

		this.add(layer, BorderLayout.CENTER);

		setFg(foreground);
		setBg(background);

	}

	/**
	 * @return the fg
	 */
	public JPanel getFg() {
		return fg;
	}

	/**
	 * @param fg
	 *            the fg to set
	 */
	public void setFg(JPanel fg) {
		if (this.fg != null)
			this.layer.remove(this.fg);
		this.fg = fg;
		if (fg != null) {
			fg.setOpaque(false);
			int w = this.getWidth();
			int h = this.getHeight();
			fg.setBounds(0, 0, w, h);
			layer.add(fg, 0);
		}

	}

	/**
	 * @return the bg
	 */
	public ImagePanel getBg() {
		return bg;
	}

	/**
	 * @param bg
	 *            the bg to set
	 */
	public void setBg(ImagePanel bg) {
		if (this.bg != null) {
			this.bg.removeChangeListener(this);
			this.layer.remove(this.bg);
		}
		this.bg = bg;
		if (bg != null) {
			int w = this.getWidth();
			int h = this.getHeight();
			bg.setBounds(0, 0, w, h);
			layer.add(bg, -3000);
			layer.moveToBack(bg);
			bg.addChangeListener(this);
		}
	}

	public void stateChanged(ChangeEvent e) {
		if (fg != null)
			fg.repaint();
		// System.out.println("grr");

	}

	/*
	 * *********************************** Change event thrower
	 */

	private ArrayList<ChangeListener> listeners = new ArrayList<ChangeListener>();

	public void addChangeListener(ChangeListener cl) {
		listeners.add(cl);
	}

	public void removeChangeListener(ChangeListener cl) {
		listeners.remove(cl);
	}

	public void fireChangeEvent() {
		ChangeEvent e = new ChangeEvent(this);
		for (ChangeListener cl : listeners)
			cl.stateChanged(e);
	}

}

class ViewListCellRenderer implements TableCellRenderer {

	@Override
	public Component getTableCellRendererComponent(JTable table, Object value,
			boolean isSelected, boolean hasFocus, int row, int column) {
		MiniViewPanel m = (MiniViewPanel) value;
		if (isSelected) {
			m.setBackground(table.getSelectionBackground());
			m.setForeground(table.getSelectionForeground());
		} else {
			m.setBackground(table.getBackground());
			m.setForeground(table.getForeground());
		}
		m.setSelected(isSelected);
		return m;
	}

}

class MyTableCellEditor extends AbstractCellEditor implements TableCellEditor {

	MiniViewPanel m;

	// This method is called when a cell value is edited by the user.
	public Component getTableCellEditorComponent(JTable table, Object value,
			boolean isSelected, int rowIndex, int vColIndex) {
		// 'value' is value contained in the cell located at (rowIndex,
		// vColIndex)
		m = (MiniViewPanel) value;
		// if (isSelected) {
		m.setBackground(table.getSelectionBackground());
		m.setForeground(table.getSelectionForeground());
		/*
		 * } else { m.setBackground(table.getBackground());
		 * m.setForeground(table.getForeground()); }
		 */
		m.setSelected(true);
		return m;
	}

	// This method is called when editing is completed.
	// It must return the new value to be stored in the cell.
	public Object getCellEditorValue() {
		return m;
	}
}









