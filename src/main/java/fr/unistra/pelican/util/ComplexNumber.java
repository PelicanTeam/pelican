package fr.unistra.pelican.util;

/**
 * Class representing a complex number.
 * Largely copied from the HIPR2 project.
 */
public class ComplexNumber
{
  public double real;
  public double imag;

  public ComplexNumber(){
  }

  public ComplexNumber(double r, double i){
    real = r;
    imag = i;
  }

  public ComplexNumber(ComplexNumber c){
    real = c.real;
    imag = c.imag;
  }

  public double magnitude(){
    return Math.sqrt(this.cNorm());
  }

  public double phaseAngle(){
    if(real==0 && imag == 0) return 0;
    else return Math.atan(imag/real);
  }

  double cNorm (){
    return real*real + imag*imag;
  }

  public static ComplexNumber cExp (ComplexNumber z){
    ComplexNumber x,y;
    x = new ComplexNumber(Math.exp(z.real),0.0);
    y = new ComplexNumber(Math.cos(z.imag),Math.sin(z.imag));
    return cMult (x,y);
  }

  public static ComplexNumber cMult (ComplexNumber z1, ComplexNumber z2){
    ComplexNumber z3 = new ComplexNumber();
    z3.real = (z1.real)*(z2.real) - (z1.imag)*(z2.imag);
    z3.imag = (z1.real)*(z2.imag) + (z1.imag)*(z2.real);
    return z3;
  }

  public static ComplexNumber cSum (ComplexNumber z1, ComplexNumber z2)
  {
    ComplexNumber z3 = new ComplexNumber();
    
    z3.real = z1.real + z2.real;
    z3.imag = z1.imag + z2.imag;
    
    return z3;
  }

  public static ComplexNumber cDiv (ComplexNumber z1, ComplexNumber z2)
  {
    ComplexNumber z3 = new ComplexNumber();
    
    double n = z2.cNorm();
    
    z3.real = ((z1.real*z2.real) + (z1.imag*z2.imag)) / n;
    z3.imag = ((z2.real*z1.imag) - (z1.real*z2.imag)) / n;
    
    return z3;
  }


  public static ComplexNumber cDiff (ComplexNumber z1, ComplexNumber z2)
  {
    ComplexNumber z3 = new ComplexNumber();

    z3.real = z1.real - z2.real;
    z3.imag = z1.imag - z2.imag;
    
    return z3;
  }
}