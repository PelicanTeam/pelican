package fr.unistra.pelican.util.mask;

import java.util.Stack;

import fr.unistra.pelican.BooleanImage;

/**
 * This represent a stack of mask. Each MaskStack uses his stack of masks and a
 * comparison policy to decide if a given pixel is present or not. With policy
 * FALSE, a pixel is never present. With policy TRUE, a pixel is always present.
 * With policy AND, a pixel is present if it is present in every mask of the
 * stack. With policy OR, a pixel is present if it is present in at least one
 * mask of the stack.
 * 
 * @see Mask
 * @author Benjamin Perret, Régis Witz
 */
public class MaskStack extends Stack<Mask> implements Mask {

	// /////////////
	// CONSTANTS //
	// /////////////

	private static final long serialVersionUID = 1L;

	// DEVNOTE:
	// if you add a policy, you should :
	// step 1: add its name and ID below.
	// step 2: update the "should I throw an exception ?" part in
	// this.setPolicy( int policy ).
	// step 3: update with your processing all the isInMask( xxx ) methods.
	public static final int FALSE = 0;
	public static final int TRUE = 1;
	public static final int AND = 2;
	public static final int OR = 3;

	// //////////
	// FIELDS //
	// //////////

	/** Comparison policy. */
	private int policy = AND;

	// ////////////////
	// CONSTRUCTORS //
	// ////////////////

	/**
	 * Default constructor : create a stack of masks and push a mask on it
	 * 
	 * @param m
	 *            Mask to push
	 */
	public MaskStack(Mask m) {
		super();
		if (m != null)
			push(m);
	}

	/**
	 * Create a stack of masks, affect it a comparison policy and push a mask on
	 * it.
	 * 
	 * @param m
	 *            Mask to push.
	 * @param policy
	 *            Comparaison policy.
	 */
	public MaskStack(Mask m, int policy) {
		this(m);
		this.setPolicy(policy);
	}

	/** Create an empty stack of masks. */
	public MaskStack() {
		this(null);
	}

	/**
	 * Create an empty stack of masks and affect it a comparison policy .
	 * 
	 * @param policy
	 *            Comparaison policy.
	 */
	public MaskStack(int policy) {
		this(null, policy);
	}

	// ///////////
	// METHODS //
	// ///////////

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.unistra.pelican.util.mask.Mask#cloneMask()
	 */
	public Mask cloneMask() {
		MaskStack ms = new MaskStack(this.policy);
		for (Mask m : this)
			ms.push(m.cloneMask());
		return ms;
	}

	public boolean isInMask(int loc) {

		switch (this.policy) {

		case TRUE:
			return true;
		case FALSE:
			return false;
		case AND:
			// if ( this.size() == 0 ) return true;
			for (Mask m : this)
				if (!m.isInMask(loc))
					return false;
			return true;
		case OR:
			if (this.size() == 0)
				return true;
			for (Mask m : this)
				if (m.isInMask(loc))
					return true;
			return false;
		default:
			return false;
		}

	}
	
	public boolean isInMask(long loc) {

		switch (this.policy) {

		case TRUE:
			return true;
		case FALSE:
			return false;
		case AND:
			// if ( this.size() == 0 ) return true;
			for (Mask m : this)
				if (!m.isInMask(loc))
					return false;
			return true;
		case OR:
			if (this.size() == 0)
				return true;
			for (Mask m : this)
				if (m.isInMask(loc))
					return true;
			return false;
		default:
			return false;
		}

	}

	public boolean isInMask(int x, int y, int z, int t, int b) {

		if (this.policy == TRUE)
			return true;
		else if (this.policy == FALSE)
			return false;
		else if (this.policy == AND) {

			// if ( this.size() == 0 ) return true;
			for (Mask m : this)
				if (!m.isInMask(x, y, z, t, b))
					return false;
			return true;
		} else if (this.policy == OR) {

			 if ( this.size() == 0 ) return true;
			for (Mask m : this)
				if (m.isInMask(x, y, z, t, b))
					return true;
			return false;
		} else
			return false;
	}

	/**
	 * @return <tt>true</tt> if a pixel is NOT hidden by the mask.
	 */
	public boolean isInMaskXY(int x, int y) {

		if (this.policy == TRUE)
			return true;
		else if (this.policy == FALSE)
			return false;
		else if (this.policy == AND) {

			// if ( this.size() == 0 ) return true;
			for (Mask m : this)
				if (!m.isInMaskXY(x, y))
					return false;
			return true;
		} else if (this.policy == OR) {

			if ( this.size() == 0 ) return true;
			for (Mask m : this)
				if (m.isInMaskXY(x, y))
					return true;
			return false;
		} else
			return false;
	}

	public boolean isInMaskXYB(int x, int y, int b) {

		if (this.policy == TRUE)
			return true;
		else if (this.policy == FALSE)
			return false;
		else if (this.policy == AND) {

			// if ( this.size() == 0 ) return true;
			for (Mask m : this)
				if (!m.isInMaskXYB(x, y, b))
					return false;
			return true;
		} else if (this.policy == OR) {

			if ( this.size() == 0 ) return true;
			for (Mask m : this)
				if (m.isInMaskXYB(x, y, b))
					return true;
			return false;
		} else
			return false;
	}

	public boolean isInMaskXYT(int x, int y, int t) {

		if (this.policy == TRUE)
			return true;
		else if (this.policy == FALSE)
			return false;
		else if (this.policy == AND) {

			// if ( this.size() == 0 ) return true;
			for (Mask m : this)
				if (!m.isInMaskXYT(x, y, t))
					return false;
			return true;
		} else if (this.policy == OR) {

			if ( this.size() == 0 ) return true;
			for (Mask m : this)
				if (m.isInMaskXYT(x, y, t))
					return true;
			return false;
		} else
			return false;
	}

	public boolean isInMaskXYTB(int x, int y, int t, int b) {

		if (this.policy == TRUE)
			return true;
		else if (this.policy == FALSE)
			return false;
		else if (this.policy == AND) {

			// if ( this.size() == 0 ) return true;
			for (Mask m : this)
				if (!m.isInMaskXYTB(x, y, t, b))
					return false;
			return true;
		} else if (this.policy == OR) {

			if ( this.size() == 0 ) return true;
			for (Mask m : this)
				if (m.isInMaskXYTB(x, y, t, b))
					return true;
			return false;
		} else
			return false;
	}

	public boolean isInMaskXYZ(int x, int y, int z) {

		if (this.policy == TRUE)
			return true;
		else if (this.policy == FALSE)
			return false;
		else if (this.policy == AND) {

			// if ( this.size() == 0 ) return true;
			for (Mask m : this)
				if (!m.isInMaskXYZ(x, y, z))
					return false;
			return true;
		} else if (this.policy == OR) {

			 if ( this.size() == 0 ) return true;
			for (Mask m : this)
				if (m.isInMaskXYZ(x, y, z))
					return true;
			return false;
		} else
			return false;
	}

	public boolean isInMaskXYZB(int x, int y, int z, int b) {

		if (this.policy == TRUE)
			return true;
		else if (this.policy == FALSE)
			return false;
		else if (this.policy == AND) {

			// if ( this.size() == 0 ) return true;
			for (Mask m : this)
				if (!m.isInMaskXYZB(x, y, z, b))
					return false;
			return true;
		} else if (this.policy == OR) {

			if ( this.size() == 0 ) return true;
			for (Mask m : this)
				if (m.isInMaskXYZB(x, y, z, b))
					return true;
			return false;
		} else
			return false;
	}

	public boolean isInMaskXYZT(int x, int y, int z, int t) {

		if (this.policy == TRUE)
			return true;
		else if (this.policy == FALSE)
			return false;
		else if (this.policy == AND) {

			// if ( this.size() == 0 ) return true;
			for (Mask m : this)
				if (!m.isInMaskXYZT(x, y, z, t))
					return false;
			return true;
		} else if (this.policy == OR) {

			 if ( this.size() == 0 ) return true;
			for (Mask m : this)
				if (m.isInMaskXYZT(x, y, z, t))
					return true;
			return false;
		} else
			return false;
	}

	public boolean isInMaskXYZTB(int x, int y, int z, int t, int b) {

		if (this.policy == TRUE)
			return true;
		else if (this.policy == FALSE)
			return false;
		else if (this.policy == AND) {

			// if ( this.size() == 0 ) return true;
			for (Mask m : this)
				if (!m.isInMaskXYZTB(x, y, z, t, b))
					return false;
			return true;
		} else if (this.policy == OR) {

			 if ( this.size() == 0 ) return true;
			for (Mask m : this)
				if (m.isInMaskXYZTB(x, y, z, t, b))
					return true;
			return false;
		} else
			return false;
	}

	/**
	 * Set this MaskStack's comparaison policy.
	 * 
	 * @param policy
	 *            Comparison policy. Must be one of the
	 *            "public static final int" constants wich are at the beginning
	 *            of this class declaration.
	 */
	public void setPolicy(int policy) {

		// "should I throw an exception ?"
		if ((policy != FALSE) && (policy != TRUE) && (policy != AND)
				&& (policy != OR))
			throw new fr.unistra.pelican.PelicanException("Policy unknown.");

		// update
		this.policy = policy;
	}

	/**
	 * Attempts to merge BooleanMasks contained in {@link #mask} if they are all
	 * equal to each other. A time consumer, but room-saver.
	 */
	public void mergeBooleanMasks() {

		java.util.ArrayList<BooleanMask> array = new java.util.ArrayList<BooleanMask>();
		for (java.util.Enumeration<Mask> e = this.elements(); e
				.hasMoreElements();) {

			Mask m = e.nextElement();
			if (m instanceof BooleanMask)
				array.add((BooleanMask) m);
		}
		if (array.size() == 0)
			return;

		BooleanImage bi = new BooleanImage(array.get(0).getXDim(), array.get(0)
				.getYDim(), array.get(0).getZDim(), array.get(0).getTDim(),
				array.get(0).getBDim());
		boolean val;
		for (int x = 0; x < bi.xdim; x++)
			for (int y = 0; y < bi.ydim; y++)
				for (int z = 0; z < bi.zdim; z++)
					for (int t = 0; t < bi.tdim; t++)
						for (int b = 0; b < bi.bdim; b++) {

							val = array.get(0).isInMask(x, y, z, t, b);
							for (int i = 1; i < array.size(); i++)
								if (val != array.get(i).isInMask(x, y, z, t, b))
									return;
							bi.setPixelXYZTBBoolean(x, y, z, t, b, val);
						}

		for (int i = 0; i < array.size(); i++)
			this.removeElement(array.get(i));
		this.push(new BooleanMask(bi));
	}

	/**
	 * Push all masks from a given MaskStack into the current MaskStack
	 * 
	 * @param stack
	 * @return the given MaskStack
	 * @author Lefevre
	 */
	public MaskStack push(MaskStack stack) {
		for (Mask m : stack)
			this.push(m);
		return stack;
	}

}
