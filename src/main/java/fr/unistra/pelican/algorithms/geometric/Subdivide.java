package fr.unistra.pelican.algorithms.geometric;

import fr.unistra.pelican.*;



/**
 *	This class perform a uniform spatial subdivision of an image
 * 
 *	The supported outputs are: 
 *	<ul>
 *	<li> {@link #Uniform2x2} (4 subimages) : ByteImage format
 *	<li> {@link #Uniform3x3} (9 subimages) : ByteImage format
 *	<li> {@link #Map5} (5 subimages: north,south,west,east,center) : ByteImage format 
 *	<li> {@link #Lil22} (2x2 subimages) : all formats
 *	</ul>
 * 
 * @author Erchan Aptoula, Régis Witz
 */
public class Subdivide extends Algorithm {

	/** Input image. */
	public Image input;

	/**	Subdivision method to be used. */
	public int type;

	/** Output images. */
	public Image[] output;

	/**	Represents Uniform2x2 method. */
	public static final int Uniform2x2 = 0;

	/**	Represents Uniform3x3 method. */
	public static final int Uniform3x3 = 1;

	/**	Represents Map5 method. */
	public static final int Map5 = 2;

	/**	Represents Lil4 method. 
	 *	This method subdivides an input image into as much 
	 *	littles 2 pixels x 2 pixels subimages as possible. 
	 */
	public static final int Lil22 = 3;

	/**	Default constructor. */
	public Subdivide() {
		super.inputs = "input,type";
		super.outputs = "output";
		
	}
	
	/**
	 * 
	 * @param input input image
	 * @param type subdivision type
	 * @return output images
	 */
	public static Image[] exec(Image input,Integer type)
	{
		return (Image[]) new Subdivide().process(input,type);
	}


	private void computeMap5Kernel( int k, int xOffset, int yOffset, int xSize, int ySize ) { 

		output[k] = new ByteImage( xSize,ySize,1,1,input.getBDim() );
		for ( int b = 0 ; b < input.getBDim() ; b++ ) 
			for ( int x = 0 ; x < xSize; x++ ) 
				for ( int y = 0 ; y < ySize; y++ ) 
					output[k].setPixelXYBByte( x,y,b, 
						input.getPixelXYBByte( x+xOffset, y+yOffset, b ) );

	}

	/*
	 * (non-Javadoc)
	 * @see fr.unistra.pelican.Algorithm#launch()
	 */
	public void launch() throws AlgorithmException { 

		int _xDim,_yDim;
		switch ( this.type ) { 

			case Uniform2x2 : 

				output = new Image[4];
				_xDim = input.getXDim() / 2;
				_yDim = input.getYDim() / 2;
				for (int _x = 0; _x < 2; _x++) {
					for (int _y = 0; _y < 2; _y++) {
						output[_x * 2 + _y] = new ByteImage(_xDim, _yDim, 1, 1,
								input.getBDim());
						// fill it up
						for (int b = 0; b < input.getBDim(); b++) {
							for (int x = _xDim * _x; x < _xDim * (_x + 1); x++) {
								for (int y = _yDim * _y; y < _yDim * (_y + 1); y++) {
									output[_x * 2 + _y].setPixelXYBByte(x - _xDim
											* _x, y - _yDim * _y, b, input
											.getPixelXYBByte(x, y, b));
								}
							}
						}
					}
				}

				break;
			case Uniform3x3 : 

				output = new Image[9];
				_xDim = input.getXDim() / 3;
				_yDim = input.getYDim() / 3;
				for (int _x = 0; _x < 3; _x++) {
					for (int _y = 0; _y < 3; _y++) {
						output[_x * 3 + _y] = new ByteImage(_xDim, _yDim, 1, 1,
								input.getBDim());
						// fill it up
						for (int b = 0; b < input.getBDim(); b++) {
							for (int x = _xDim * _x; x < _xDim * (_x + 1); x++) {
								for (int y = _yDim * _y; y < _yDim * (_y + 1); y++) {
									output[_x * 3 + _y].setPixelXYBByte(x - _xDim
											* _x, y - _yDim * _y, b, input
											.getPixelXYBByte(x, y, b));
								}
							}
						}
					}
				}

				break;
			case Map5 : 

				output = new Image[5];
				_xDim = (int) Math.ceil( input.getXDim()*0.25 );
				_yDim = (int) Math.ceil( input.getYDim()*0.25 );
				// compute NORTH
				this.computeMap5Kernel( 0, 0,0, input.getXDim(),_yDim );
				// compute SOUTH
				this.computeMap5Kernel( 1, 0,input.getYDim()-_yDim, input.getXDim(),_yDim );
				// compute WEST
				this.computeMap5Kernel( 2, 0,_yDim, _xDim,input.getYDim()-2*_yDim );
				// compute CENTER
				this.computeMap5Kernel( 3,_xDim,_yDim,input.getXDim()-2*_xDim,input.getYDim()-2*_yDim );
				// compute EAST
				this.computeMap5Kernel( 4, input.getXDim()-_xDim,_yDim, _xDim,input.getYDim()-2*_yDim );

				break;
			case Lil22 : 

				// nb. in java int = int / int is the division floored.
				int xdim = this.input.getXDim() / 2;
				int ydim = this.input.getYDim() / 2;
				int zdim = this.input.getZDim();
				int tdim = this.input.getTDim();
				int bdim = this.input.getBDim();
				this.output = new Image[ xdim*ydim ];
				int index;
				double p;
				for ( int y = 0 ; y < ydim ; y++ ) { 

					_yDim = y*2;
					for ( int x = 0 ; x < xdim ; x++ ) { 

						_xDim = x*2;
						index = y*xdim+x;
						if ( this.input instanceof BooleanImage )
							this.output[ index ] = new BooleanImage( 2,2, zdim,tdim,bdim );
						else if ( this.input instanceof ByteImage )
							this.output[ index ] = new ByteImage( 2,2, zdim,tdim,bdim );
						else if ( this.input instanceof IntegerImage )
							this.output[ index ] = new IntegerImage( 2,2, zdim,tdim,bdim );
						else if ( this.input instanceof DoubleImage )
							this.output[ index ] = new DoubleImage( 2,2, zdim,tdim,bdim );

						for ( int t = 0 ; t < tdim ; t++ ) { 
							for ( int z = 0 ; z < zdim ; z++ ) { 
								for ( int b = 0 ; b < bdim ; b++ ) { 

									p = this.input.getPixelXYZTBDouble( _xDim  ,_yDim  , z,t,b );
									this.output[ index ].setPixelXYZTBDouble( 0,0,z,t,b, p );
									p = this.input.getPixelXYZTBDouble( _xDim+1,_yDim  , z,t,b );
									this.output[ index ].setPixelXYZTBDouble( 1,0,z,t,b, p );
									p = this.input.getPixelXYZTBDouble( _xDim  ,_yDim+1, z,t,b );
									this.output[ index ].setPixelXYZTBDouble( 0,1,z,t,b, p );
									p = this.input.getPixelXYZTBDouble( _xDim+1,_yDim+1, z,t,b );
									this.output[ index ].setPixelXYZTBDouble( 1,1,z,t,b, p );
						}	}	}

					}
				}

				break;
			default : throw new PelicanException( "Unsupported subdivision type." );
		}

	}

}
