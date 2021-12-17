package jcog.math.optimize.cmaes.examples;
import jcog.math.optimize.cmaes.IObjectiveFunction;

import java.util.Random;

/** one can access the desired fitness function by giving its number
 * in the constructor method. Refer to the source code for the
 * numbers. This class is a stub (and hack) so far.
 * 
 */
public class FunctionCollector implements IObjectiveFunction {

	public FunctionCollector (double function_number, 
			int flgRotate, 
			double axisratio) {

		actFun = (int) (function_number);
		rotate = flgRotate; 
		scaling = axisratio == 0 ? 1.0 : axisratio;

		if (actFun > maxFuncNumber)
			actFun = 1; /* sphere */
		
		// assign all functions by number here
		funs[0]  = new RandFun();
		funs[10]  = new Sphere();

		// convex-quadratic
        funs[30]  = new Cigar(axisratio == 0 ? 1.0e3 : scaling);
        funs[40]  = new Tablet(axisratio == 0 ? 1.0e3 : scaling);
		funs[50]  = new Elli(axisratio == 0 ? 1.0e3 : scaling);
        funs[60]  = new CigTab(axisratio == 0 ? 1.0e4 : scaling);
        funs[70]  = new TwoAxes(axisratio == 0 ? 1.0e3 : scaling);

        // uni-modal, well, essentially 
		funs[80]  = new Rosen();
		funs[90]  = new DiffPow();
        funs[91]  = new ssDiffPow();

        // multi-modal
        funs[150] = new Rastrigin(scaling, 10); 
        funs[160] = new Ackley(scaling);

//      funs[999]  = new Experimental();
//      funs[]  = new ();
//      funs[]  = new ();
        
	}
	private final int maxFuncNumber = 999;
	private final IObjectiveFunction[] funs = new IObjectiveFunction[maxFuncNumber+1];
	private int actFun;
	private int rotate;
	private double scaling = 1;
	private final Basis B = new Basis();
	
	/** implements the fitness function evaluation according to interface {@link IObjectiveFunction}
	 * 
	 */ 
	@Override
	public double applyAsDouble(double[] x) {
		x = x.clone(); // regard input as imutable, not really Java philosophy
		if (rotate > 0)     // rotate
			x = B.Rotate(x);
		if (scaling != 1) { // scale 
			for (int i = 0; i < x.length; ++i)
				x[i] = Math.pow(10, i/(x.length - 1.0)) * x[i];
		}
		return funs[actFun] == null ? funs[0].applyAsDouble(x) : funs[actFun].applyAsDouble(x);
	}
	public boolean test(double[] x) { // unfortunate code duplication
    	//int i;
    	//for (i = 0; i < x.length; ++i)
    	//	if (x[i] < 0.01)
    	//		return false;
    	//return true;
		return funs[actFun].test(x);
	}
}

/** provides rotation of a search point, basis is chosen with constant seed.
 * 
 */
class RandFun implements IObjectiveFunction {
    private final Random rand = new Random(0);
    @Override
    public double applyAsDouble(double[] x) {
        return rand.nextDouble();
    }
}
class Sphere implements IObjectiveFunction {
    @Override
    public double applyAsDouble(double[] x) {
        double res = 0;
        for (double v : x) res += v * v;
        return res;
    }

}

class Cigar implements IObjectiveFunction {
    Cigar() {
        this(1.0e3);
    }
    Cigar(double axisratio) {
        factor = axisratio * axisratio;
    }
    private double factor = 1.0e6;
    @Override
    public double applyAsDouble(double[] x) {
        double res = x[0] * x[0];
        for (int i = 1; i < x.length; ++i)
            res += factor * x[i] * x[i];
        return res;
    }
}
class Tablet implements IObjectiveFunction {
    Tablet() {
        this(1.0e3);
    }
    Tablet(double axisratio) {
        factor = axisratio * axisratio;
    }
    private double factor = 1.0e6;
    @Override
    public double applyAsDouble(double[] x) {
        double res = factor * x[0] * x[0];
        for (int i = 1; i < x.length; ++i)
            res += x[i] * x[i];
        return res;
    }
}
class CigTab implements IObjectiveFunction {
    CigTab() {
        this(1.0e4);
    }
    CigTab(double axisratio) {
        factor = axisratio;
    }
    private double factor = 1.0e6;
    @Override
    public double applyAsDouble(double[] x) {
    	int end = x.length-1;
        double res = x[0] * x[0] / factor + factor * x[end] * x[end];
        for (int i = 1; i < end; ++i)
            res += x[i] * x[i];
        return res;
    }
}
class TwoAxes implements IObjectiveFunction {
    private double factor = 1.0e6;
    TwoAxes() {
    }
    TwoAxes(double axisratio) {
        factor = axisratio * axisratio;
    }
    @Override
    public double applyAsDouble(double[] x) {
        double res = 0;
        for (int i = 0; i < x.length; ++i)
            res += (i < x.length/2 ? factor : 1) * x[i] * x[i];
        return res;
    }
}
class ElliRotated implements IObjectiveFunction {
    ElliRotated() {
        this(1.0e3);
    }
    private ElliRotated(double axisratio) {
        factor = axisratio * axisratio;
    }
    private final Basis B = new Basis();
    private double factor = 1.0e6;
    @Override
    public double applyAsDouble(double[] x) {
        x = B.Rotate(x);
        double res = 0;
        for (int i = 0; i < x.length; ++i)
            res += Math.pow(factor,i/(x.length- 1.0)) * x[i] * x[i];
        return res;
    }
}
/** dimensionality must be larger than one */
class Elli implements IObjectiveFunction {
    Elli() {
        this(1.0e3);
    }
    Elli(double axisratio) {
        factor = axisratio * axisratio;
    }
    private double factor = 1.0e6;
    @Override
    public double applyAsDouble(double[] x) {
        double res = 0;
        for (int i = 0; i < x.length; ++i)
            res += Math.pow(factor,i/(x.length- 1.0)) * x[i] * x[i];
        return res;
    }
//    public boolean isFeasible(double x[]) {
//    	int i;
//    	for (i = 0; i < x.length; ++i) {
//    		if (x[i] < -0.20 || x[i] > 80) 
//    			return false;
//    	}
//    	return true;
//    }
    
}/** dimensionality must be larger than one */

class DiffPow implements IObjectiveFunction {
    @Override
    public double applyAsDouble(double[] x) {
        double res = 0;
        for (int i = 0; i < x.length; ++i)
            res += Math.pow(Math.abs(x[i]), 2.0 +10*(double)i/(x.length- 1.0));
        return res;
    }
    
}class ssDiffPow implements IObjectiveFunction {
    @Override
    public double applyAsDouble(double[] x) {
        return Math.pow(new DiffPow().applyAsDouble(x), 0.25);
    }
    
}
class Rosen implements IObjectiveFunction {
    @Override
    public double applyAsDouble(double[] x) {
        double res = 0;
        for (int i = 0; i < x.length-1; ++i)
            res += 1.0e2 * (x[i]*x[i] - x[i+1]) * (x[i]*x[i] - x[i+1]) +
            (x[i] - 1.0) * (x[i] - 1.0);
        return res;
    }
}

class Ackley implements IObjectiveFunction {
    private double axisratio = 1.0;
    Ackley(double axra) {
        axisratio = axra;
    }
    Ackley() {
    }
    @Override
    public double applyAsDouble(double[] x) {
        double res = 0;
        double res2 = 0;
        double fac = 0;
        for (int i = 0; i < x.length; ++i) {
            fac = Math.pow(axisratio, (i- 1.0)/(x.length- 1.0));
            res += fac * fac * x[i]*x[i];
            res2 += Math.cos(2.0 * Math.PI * fac * x[i]);
        }
        return (20.0 - 20.0 * Math.exp(-0.2 * Math.sqrt(res/x.length))
                + Math.exp(1.0) - Math.exp(res2/x.length));
    }
}
class Rastrigin implements IObjectiveFunction {
    Rastrigin() {
        this(1, 10);
    }
    Rastrigin(double axisratio, double amplitude) {
        this.axisratio = axisratio;
        this.amplitude = amplitude;
    }
    private double axisratio = 1;
    private double amplitude = 10;
    @Override
    public double applyAsDouble(double[] x) {
        double fac;
        double res = 0;
        for (int i = 0; i < x.length; ++i) {
            fac = Math.pow(axisratio,(i- 1.0)/(x.length- 1.0));
            if (i == 0 && x[i] < 0)
                fac *= 1.0;
            res +=  fac * fac * x[i] * x[i]
               + amplitude * (1.0 - Math.cos(2.0 *Math.PI * fac * x[i]));
        }
        return res;
    }
}
/* Template fitness function 
class fff extends AbstractObjectiveFunction {
    public double valueOf(double[] x) {
        double res = 0;
        for (int i = 0; i < x.length; ++i) {
        }
        return res;
    }
}
*/

class Basis {
	private double [][] B; // usually field names should be lower case
    private final Random rand = new Random(2); // use not always the same basis

    double[] Rotate(double[] x) {
    	GenBasis(x.length);
    	double[] y = new double[x.length];
    	for (int i = 0; i < x.length; ++i) {
    		y[i] = 0;
    		for (int j = 0; j < x.length; ++j)
    			y[i] += B[i][j] * x[j]; 
    	}
    	return y;
    }
    double[][] Rotate(double[][] pop) {
    	double[][] y = new double[pop.length][];
    	for (int i = 0; i < pop.length; ++i) {
    		y[i] = Rotate(pop[i]);
    	}
    	return y;
    }
    
    private void GenBasis(int DIM)
    {
    	if (B != null && B.length == DIM)
    		return;

    	double sp;
    	int i,j,k;

    	/* generate orthogonal basis */
    	B = new double[DIM][DIM];
    	for (i = 0; i < DIM; ++i) {
    		/* sample components gaussian */
    		for (j = 0; j < DIM; ++j) 
    			B[i][j] = rand.nextGaussian();
    		/* substract projection of previous vectors */
    		for (j = i-1; j >= 0; --j) {
    			for (sp = 0.0, k = 0; k < DIM; ++k)
    				sp += B[i][k]*B[j][k]; /* scalar product */
    			for (k = 0; k < DIM; ++k)
    				B[i][k] -= sp * B[j][k]; /* substract */
    		}
    		/* normalize */
    		for (sp = 0.0, k = 0; k < DIM; ++k)
    			sp += B[i][k]*B[i][k]; /* squared norm */
    		for (k = 0; k < DIM; ++k)
    			B[i][k] /= Math.sqrt(sp); 
    	}
    }
}