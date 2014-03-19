package fr.unistra.pelican.util.largeImages;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryNotificationInfo;
import java.lang.management.MemoryPoolMXBean;
import java.lang.management.MemoryType;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

import javax.management.Notification;
import javax.management.NotificationEmitter;

import fr.unistra.pelican.PelicanException;

/**
 * The LargeImageMemoryManager follows the design pattern Singleton. It is used
 * to manage the available memory of the JVM. It records all LargeImages that
 * are created and choose which unit has to be discarded when memory goes low.
 * 
 */
public class LargeImageMemoryManager implements
		javax.management.NotificationListener {

	private static final LargeImageMemoryManager INSTANCE = new LargeImageMemoryManager();

	/**
	 * Eviction List used to choose the next unit to be discarded
	 */
	private LinkedList<UnitRef> evictionList;

	/**
	 * Memory Threshold use to launch a cleanup
	 */
	private long memoryThreshold;

	/**
	 * Second memory Threshold used to stop new units from being created
	 */
	private long memoryThreshold2;

	/**
	 * HashMap which associates an integer to each LargeImage that are created
	 */
	private HashMap<Integer, WeakReference<LargeImageInterface>> imageIndex;

	/**
	 * MemoryPoolMXBean used to listen the memory
	 */
	private MemoryPoolMXBean tenured = null;

	/**
	 * Lock used to make sure that two thread don't work in the evictionList at
	 * the same time
	 */
	public ReentrantLock lock;
	

	/**
	 * Constructor
	 */
	private LargeImageMemoryManager() {

		evictionList = new LinkedList<UnitRef>();
		lock = new ReentrantLock();

		imageIndex = new HashMap<Integer, WeakReference<LargeImageInterface>>();

		List<MemoryPoolMXBean> bean = ManagementFactory.getMemoryPoolMXBeans();

		for (MemoryPoolMXBean t : bean) {
			if ((t.getType() == MemoryType.HEAP)
					&& t.isUsageThresholdSupported()) {
				tenured = t;
			}
		}
		if (tenured == null) {
			throw new PelicanException("Damn where is the tenured generation ?");
		}

		this.memoryThreshold = (long) (((double) tenured.getUsage().getMax()) * (1.0-LargeImageUtil.DEFAULT_MEMORY_FREE_RATIO));

		this.memoryThreshold2 = (long) (((double) tenured.getUsage().getMax()) * (1.0-(LargeImageUtil.DEFAULT_MEMORY_FREE_RATIO/2)));

		if(tenured.getUsage().getUsed()>this.memoryThreshold){
			System.err.println("The MemoryManager was created in a low memory environment");
		}
		
		tenured.setUsageThreshold(this.memoryThreshold);

		MemoryMXBean mbean = ManagementFactory.getMemoryMXBean();
		NotificationEmitter emitter = (NotificationEmitter) mbean;
		emitter.addNotificationListener(this, null, null);
	}

	/**
	 * Singleton getter.
	 * 
	 * @return the instance of the MemoryManager
	 */
	public static LargeImageMemoryManager getInstance() {
		return INSTANCE;
	}

	/**
	 * Method called when the memory goes low.
	 */
	public void handleNotification(Notification notification, Object arg1) {
		String notifType = notification.getType();
		if (notifType.equals(MemoryNotificationInfo.MEMORY_THRESHOLD_EXCEEDED)) {
			this.discard();
		}
	}

	/**
	 * Checks if memory used has reach the second threshold and wait up to 20s if the free memory is too low.
	 */
	public void checkMemory() {
		lock.lock();
		try{
			int lockCount = ((ReentrantLock)lock).getHoldCount();
			if (tenured.getUsage().getUsed() > this.memoryThreshold2) {
				for( int i = 0;i<20;i++){
					if (tenured.getUsage().getUsed() < this.memoryThreshold){					
						break;
					}else{
						for (int j = 0; j<lockCount;j++){
							lock.unlock();
						}
						try {
							Thread.sleep(1000);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
						for (int j = 0; j<lockCount;j++){
							lock.lock();
						}
					}
					if(i==20){
						System.err.println("Someone waited 20s for the memory to lower");
					}
				}
			}
		}finally{
			lock.unlock();
		}
	}

	/**
	 * Discards the next 10 units in the evictionList to free memory until used
	 * memory goes under the first threshold.
	 */
	private void discard() {
		lock.lock();
		try{		
			UnitRef ref = null;
			while (tenured.getUsage().getUsed() > this.memoryThreshold) {				
				for (int i = 0; i < LargeImageUtil.DEFAULT_DISCARD_NUMBER; i++) {
					if ((ref = this.evictionList.pollFirst()) == null) {
						System.err.println("Low memory and nothing to poll, we hope this is because the GC is on strike");
						try {
							Thread.sleep(1000);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}						
						break;
					} else {
						LargeImageInterface currentImage = LargeImageMemoryManager.getInstance().imageIndex.get(ref.getImageId()).get();
						if (currentImage != null) {
							currentImage.discardUnit(ref.getUnitId());
						}
					}
				}
				System.gc();
			}
		}finally{
			lock.unlock();			
		}
	}

	/**
	 * Indicates to the MemoryManager that an unit has been modified
	 * 
	 * @param memoryId
	 *            Index of the Image
	 * @param unitId
	 *            Index of the Unit
	 */
	public void notifyUsage(int memoryId, int unitId) {
		lock.lock();
		try{
			UnitRef currentUnit = new UnitRef(memoryId, unitId);
			this.evictionList.remove(currentUnit);
			this.evictionList.add(currentUnit);
		}finally{
			lock.unlock();
		}
	}

	/**
	 * Allows to modify the MemoryFreeRatio
	 * 
	 * @param newMemoryFreeRatio
	 */
	public void setMemoryFreeRatio(double newMemoryFreeRatio) {
		this.memoryThreshold = (long) (Runtime.getRuntime().maxMemory() * newMemoryFreeRatio);
		this.tenured.setUsageThreshold(this.memoryThreshold);
	}

	/**
	 * Gets the value of the first memory threshold.
	 * 
	 * @return the first memory threshold.
	 */
	public long getMemoryThreshold() {
		return this.memoryThreshold;
	}

	/**
	 * Adds a new Large Image to be managed
	 * 
	 * @param img
	 *            the new image to be managed
	 * @return the index where the Image has been recorded
	 */
	public int addImage(LargeImageInterface img) {
		synchronized(this.imageIndex){
			int result = this.imageIndex.size();
			this.imageIndex.put(result, new WeakReference<LargeImageInterface>(img));
			return result;
		}
	}

	/**
	 * Closes all LargesImagesInterface registered to the Memory Manager. This
	 * method should be called
	 */
	public void closeAll() {
		synchronized(this.imageIndex){
			for (WeakReference<LargeImageInterface> weak : this.imageIndex.values()) {
				LargeImageInterface largeIm = weak.get();
				if (largeIm != null) {
					largeIm.close();
				}
			}
		}
	}

	/**
	 * Gets the max size of the Tenured generation. 
	 * @return
	 * 			the max size of the Tenured generation
	 */
	public long getMaxTenuredMemory() {
		return this.tenured.getUsage().getMax();
	}

	/**
	 * Class used to identify an unit in the MemoryManager. It stores the index
	 * of the Image from the imageIndex and the index of the unit in its image.
	 */
	public class UnitRef {

		int imageId;
		int unitId;

		/**
		 * Constructor
		 * 
		 * @param imageId
		 *            Image index
		 * @param unitId
		 *            Unit index
		 */
		public UnitRef(int imageId, int unitId) {
			this.imageId = imageId;
			this.unitId = unitId;
		}

		@Override
		public boolean equals(Object o) {
			if (!(o instanceof UnitRef)) {
				return false;
			}
			return (this.imageId == ((UnitRef) o).imageId)
					&& (this.unitId == ((UnitRef) o).unitId);
		}

		@Override
		public int hashCode() {
			return ((Integer) this.imageId).hashCode();
		}

		/**
		 * Gets the Image index
		 * 
		 * @return the Image index
		 */
		public int getImageId() {
			return this.imageId;
		}

		/**
		 * Gets the unit index
		 * 
		 * @return the unit index
		 */
		public int getUnitId() {
			return this.unitId;
		}

		@Override
		public String toString() {
			return ("(" + this.imageId + "," + this.unitId + ")");
		}
	}
}