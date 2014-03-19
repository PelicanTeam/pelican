package fr.unistra.pelican.util.iterator;

import java.util.Iterator;
import fr.unistra.pelican.Image;
import fr.unistra.pelican.util.Pixel;



/**	
 *	Allows smooth X,Y,Z,T,B iteration on an unmasked image.
 *	@author Régis Witz
 */
public class ImageIterator extends AbstractImageIterator<Pixel> { 

	/**	Image on wich iteration is done. */
	protected Image image;
	/**	X offset of the iteration. */
	protected int xOffset;
	/**	Y offset of the iteration. */
	protected int yOffset;
	/**	Z offset of the iteration. */
	protected int zOffset;
	/**	T offset of the iteration. */
	protected int tOffset;
	/**	B offset of the iteration. */
	protected int bOffset;
	/**	X Dimension of {@link #image}. */
	protected int xdim;
	/**	Y Dimension of {@link #image}. */
	protected int ydim;
	/**	Z Dimension of {@link #image}. */
	protected int zdim;
	/**	T Dimension of {@link #image}. */
	protected int tdim;
	/**	B Dimension of {@link #image}. */
	protected int bdim;
	/**	Equals to {@link #bdim }. */
	protected int mulX;
	/**	Equals to {@link #xdim } x {@link #mulX }. */
	protected int mulY;
	/**	Equals to {@link #ydim } x {@link #mulY }. */
	protected int mulZ;
	/**	Equals to {@link #zdim } x {@link #mulZ }. */
	protected int mulT;

	/**	Next point in iteration. Please see {@link #next} description. */
	protected Pixel next = null;

	/**
	 * To avoid multiple index computing for hasNext function.
	 * NB: we should have some checks, perhaps true is not true at the beginning 
	 * (but don't worry i understand myself)
	 */
	protected boolean hasNext=true;


	public ImageIterator( Image image ) { 

		this.image = image;
		this.xOffset = 0;
		this.yOffset = 0;
		this.zOffset = 0;
		this.tOffset = 0;
		this.bOffset = 0; 

		this.xdim = this.image.getXDim();
		this.ydim = this.image.getYDim();
		this.zdim = this.image.getZDim();
		this.tdim = this.image.getTDim();
		this.bdim = this.image.getBDim();
		this.mulX = this.bdim;
		this.mulY = this.mulX * xdim;
		this.mulZ = this.mulY * ydim;
		this.mulT = this.mulZ * zdim;
		if(image.size()==0)
			hasNext=false;
		this.next = new Pixel();
	}

	/**	Returns <tt>true</tt> if the iteration has more elements. ( In other words, returns 
	 *	<tt>true</tt> if next would return an element rather than throwing an exception. )
	 *	@return <tt>true</tt> if the iterator has more elements.
	 */
	public boolean hasNext() { 

		//return this.getIndex() < this.image.size();
		return hasNext;
	}

	/**	Returns the next element in the iteration. Calling this method repeatedly until the 
	 *	{@link #hasNext()} method returns <tt>false</tt> will return each element in the underlying 
	 *	collection exactly once.
	 *	<p>
	 *	<b>IMPORTANT NOTE :</b> Successive calls to this method always return a reference to the 
	 *	<i>same</i> object. This should be of no consequence because in foreach loops, one reuses 
	 *	always the same object. Remember, tough, if you want to keep up the {@link Pixel} 
	 *	wich this method enumerate, you should use the {@link #clone} method before reinvoking this 
	 *	method.
	 *
	 *	@return Next element in the iteration.
	 */
	public Pixel next() { 

		this.next.x = this.xOffset;
		this.next.y = this.yOffset;
		this.next.z = this.zOffset;
		this.next.t = this.tOffset;
		this.next.b = this.bOffset;
		this.forward();

		return this.next;
	}

	/**	Unsupported. */
	public void remove() {} 

	protected int getIndex() { 

		return this.tOffset * this.mulT 
			 + this.zOffset * this.mulZ 
			 + this.yOffset * this.mulY 
			 + this.xOffset * this.mulX 
			 + this.bOffset;
	}

	protected void forward() { 

		
		this.bOffset++;
		if ( this.bOffset == this.bdim ) { 

			this.bOffset = 0;
			this.xOffset++;
			if ( this.xOffset == this.xdim ) { 

				this.xOffset = 0;
				this.yOffset++;
				if ( this.yOffset == this.ydim ) { 

					this.yOffset = 0;
					this.zOffset++;
					if ( this.zOffset == this.zdim ) { 

						this.zOffset = 0;
						this.tOffset++;
						 if ( this.tOffset == this.image.getTDim() ) 
							 hasNext=false;
						
					}
				}
			}
		}
	}

	/**
	 * @deprecated je ne sais pas si il y a 
	 * vraiment usage et si on la met il faut 
	 * la redéfinir dans tous les itérateurs dérivés 
	 * et j'ai pas envie de le faire ... Ben
	 */
	protected void rewind() { 

	//	currentIndex--;
		this.bOffset--;
		if ( this.bOffset < 0 ) { 

			this.bOffset = 0;
			this.yOffset--;
			if ( this.yOffset < 0 ) { 

				this.yOffset = 0;
				this.xOffset--;
				if ( this.xOffset < 0 ) { 

					this.xOffset = 0;
					this.zOffset--;
					if ( this.zOffset < 0 ) { 

						this.zOffset = 0;
						this.tOffset--;
						// be do not test if ( this.tOffset == this.image.getTDim() ) here, cuz
						// this method should never be called when this.getIndex() == 0 .
					}
				}
			}
		}
	}

	@Override
	public Iterator<Pixel> iterator() {
		return this; // ouh que c'est pas bien
	}

}
