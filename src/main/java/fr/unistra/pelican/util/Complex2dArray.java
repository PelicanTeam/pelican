package fr.unistra.pelican.util;

/**
 * TwoDArray is a data structure to represent a two-dimensional array
 * of complex numbers.
 * 
 * Largely copied from the HIPR2 project
 */
public class Complex2dArray{
  /**
   * The actual width of the image represented by the Complex2dArray.
   */
  public int width;
  /**
   * The actual height of the image represented by the Complex2dArray.
   */
  public int height;
  /**
   * Smallest value of 2^n such that the 2^n > width and 2^n > height.
   * The dimensions of the square 2D array storing the image.
   */
  public int size;
  /**
   * The 2D array of complex numbers padded out with (0,0) 
   * to 2^n width and height.
   */
  public ComplexNumber [][] values;
  
  /**
   * Default no-arg constructor.
   */
  public Complex2dArray(){
  }
  
  /**
   * Constructor that takes a Complex2dArray and duplicates it exactly.
   *
   * @param a Complex2dArray to be duplicated.
   */
  public Complex2dArray(Complex2dArray a){

    width = a.width;
    height = a.height;    
    size = a.size;
    values = new ComplexNumber[size][size];
    for(int j=0;j<size;++j){
      for(int i=0;i<size;++i){
	ComplexNumber c = new ComplexNumber(a.values[i][j]);
	values[i][j] = c;
      }
    }
  }

  /**
   * Constructor that takes a width and a height, generates the appropriate
   * size values and then sets up an array of (0,0) complex numbers.
   *
   * @param w Width of the new Complex2dArray.
   * @param h Height of the new Complex2dArray.
   */
  public Complex2dArray(int w, int h){
    width = w;
    height = h;  
    int n=0;
    while(Math.pow(2,n)<Math.max(w,h)){
      ++n;
    }
    size = (int) Math.pow(2,n);
    values = new ComplexNumber [size][size];
    for(int j=0;j<size;++j){
      for(int i=0;i<size;++i){
	values[i][j] = new ComplexNumber(0,0);
      }
    }
  }

  /** 
   * Constructor that takes a single dimension, generates an appropriate
   * size and sets up a size x size array of (0,0) complex numbers.
   *
   * @param s Width or height of new Complex2dArray.
   */
  public Complex2dArray(int s){
    width = s;
    height = s; 
    int n=0;
    while(Math.pow(2,n)<s){
      ++n;
    }
    size = (int) Math.pow(2,n);
    values = new ComplexNumber [size][size];
    
    for(int j=0;j<size;++j){
      for(int i=0;i<size;++i){
	values[i][j] = new ComplexNumber(0,0);
      }
    }
  }

  /** 
   * Constructor taking int array of pixel values and width and height
   * of the image represented by the array of pixels, sets values to
   * (x,0) for each pixel x.
   *
   * @param p int array of pixel values.
   * @param w Width of image.
   * @param h Height of image.
   */
  public Complex2dArray(double [] p, int w, int h){
    width = w;
    height = h;  
    int n=0;

    while(Math.pow(2,n)<Math.max(w,h)){
	++n;
    }

    size = (int) Math.pow(2,n);
    values = new ComplexNumber [size][size];
    for(int j=0;j<size;++j){
      for(int i=0;i<size;++i){
	values[i][j] = new ComplexNumber(0,0);
      }
    }
    for(int j=0;j<h;++j){
      for(int i=0;i<w;++i){
	  
	values[i][j] = new ComplexNumber(p[i+(j*w)], 0.0);
      }
    }
  }
  
  public Complex2dArray(double[] r,double[] img,int w, int h){
    width = w;
    height = h;  
    int n=0;

    while(Math.pow(2,n)<Math.max(w,h)){
	++n;
    }

    size = (int) Math.pow(2,n);
    values = new ComplexNumber [size][size];
    for(int j=0;j<size;++j){
      for(int i=0;i<size;++i){
	values[i][j] = new ComplexNumber(0,0);
      }
    }
    for(int j=0;j<h;++j){
      for(int i=0;i<w;++i){
	  
	values[i][j] = new ComplexNumber(r[i+(j*w)],img[i+(j*w)]);
      }
    }
  }
  
  /** 
   * Constructor taking 2D int array of pixels values, width and height,
   * sets values to (x,0) for each pixel x.
   *
   * @param v 2D array of pixel values.
   * @param w Width of image.
   * @param h Height of image.
   */
  public Complex2dArray(int [][] v, int w, int h){
    width = w;
    height = h;
    int n=0;
    while(Math.pow(2,n)<Math.max(w,h)){
      ++n;
    }
    size = (int) Math.pow(2,n);
    values = new ComplexNumber [size][size];
    
    for(int j=0;j<size;++j){
      for(int i=0;i<size;++i){
	values[i][j] = new ComplexNumber(0,0);
      }
    }
    for(int j=0;j<h;++j){
      for(int i=0;i<w;++i){
	values[i][j] = new ComplexNumber(v[i][j], 0.0);
      }
    }
  }

  /**
   * Constructor taking 2D array of complex numbers, width and height.
   *
   * @param v 2D array of complex numbers.
   * @param w Width of image.
   * @param h Height of image.
   */
  public Complex2dArray(ComplexNumber [][] v, int w, int h){
    width = w;
    height = h;
    int n=0;
    while(Math.pow(2,n)<Math.max(w,h)){
      ++n;
    }
    size = (int) Math.pow(2,n);
    values = new ComplexNumber [size][size];

    for(int j=0;j<size;++j){
      for(int i=0;i<size;++i){
	values[i][j] = new ComplexNumber(0,0);
      }
    }
    for(int j=0;j<h;++j){
      for(int i=0;i<w;++i){
	values[i][j] = new ComplexNumber(v[i][j]);
      }
    }
  }
  
  /** 
   * Takes a column number and returns an array containing the 
   * complex numbers in that column.
   *
   * @param n int column number (0 is first column).
   * @return ComplexNumber array containing column.
   */
  public ComplexNumber [] getColumn(int n){
    ComplexNumber [] c = new ComplexNumber [size];
    for(int i=0;i<size;++i){
	c[i] = new ComplexNumber(values[n][i]);
    }
    return c;
  }
  
  /**
   * Takes a column number and an array of complex numbers and replaces
   * that column with the new data.
   *
   * @param n int column number (0 is first column).
   * @param Array of complex numbers representing the new data.
   */
  public void putColumn(int n, ComplexNumber [] c){
    for(int i=0;i<size;++i){
	values[n][i] = new ComplexNumber(c[i]);
    }
  }
 
  /**
   * Takes a row number and an array of complex numbers and replaces
   * that row with the new data.
   *
   * @param n int row number (0 is first row).
   * @param c Array of complex numbers representing the new data.
   */
  public void putRow(int n, ComplexNumber [] c){
    for(int i=0;i<size;++i){
	values[i][n] = new ComplexNumber(c[i]);
    }
  }
  
  /** 
   * Takes a row number and returns an array containing the 
   * complex numbers in that row.
   *
   * @param n int row number (0 is first row).
   * @return ComplexNumber array containing row.
   */
  public ComplexNumber [] getRow(int n){
    ComplexNumber [] r = new ComplexNumber [size];
    for(int i=0;i<size;++i){
	r[i] = new ComplexNumber(values[i][n]);
    }
    return r;
  }
  
  public double[][] getReals()
  {
	  double[][] reals = new double[size][size];
	  
	  for(int i = 0; i < size; i++){
		  for(int j = 0; j < size; j++){
			  reals[i][j] = values[i][j].real;
		  }
	  }
	  
	  return reals;
  }
  
  public double[][] getImags()
  {
	  double[][] imags = new double[size][size];
	  
	  for(int i = 0; i < size; i++){
		  for(int j = 0; j < size; j++){
			  imags[i][j] = values[i][j].imag;
		  }
	  }
	  
	  return imags;
  }
  
  public double[][] getMagnitudes()
  {
	  double[][] mags = new double[size][size];
	  
	  for(int i = 0; i < size; i++){
		  for(int j = 0; j < size; j++){
			  mags[i][j] = values[i][j].magnitude();
		  }
	  }
	  
	  return mags;
  }
}