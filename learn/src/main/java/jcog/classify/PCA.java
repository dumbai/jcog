package jcog.classify;

import org.hipparchus.linear.EigenDecomposition;
import org.hipparchus.linear.MatrixUtils;
import org.hipparchus.linear.RealMatrix;
import org.hipparchus.stat.correlation.Covariance;

/** basic PCA class
 * from: https://stackoverflow.com/a/43819389
 *
 * row: data vector
 * col: the "variable"'s composing the vector
 *
 * TODO
 * */
public class PCA {

    /** orthogonal eigenvectors spanning the data, with associated  */
    final double[][] vectors;

    /** the magnitude of each input variable's influence */
    double[] sensitivity;

    public PCA() {
        //create points in a double array
        double[][] pointsArray = {
                new double[] { -1.0, -1.0 },
                new double[] { -1.0, 1.0 },
                new double[] { 1.0, 1.0 }
        };

//create real matrix
        RealMatrix realMatrix = MatrixUtils.createRealMatrix(pointsArray);

//create covariance matrix of points, then find eigen vectors
//see https://stats.stackexchange.com/questions/2691/making-sense-of-principal-component-analysis-eigenvectors-eigenvalues

        Covariance covariance = new Covariance(realMatrix);
        RealMatrix covarianceMatrix = covariance.getCovarianceMatrix();
        EigenDecomposition ed = new EigenDecomposition(covarianceMatrix);

        /* feature vectors:
         * Gets the matrix V of the decomposition.
         * V is an orthogonal matrix, i.e. its transpose is also its inverse.
         * The columns of V are the eigenvectors of the original matrix.
         * No assumption is made about the orientation of the system axes formed
         * by the columns of V (e.g. in a 3-dimension space, V can form a left-
         * or right-handed system).
         *
         * @return the V matrix.
         */
        //public RealMatrix getV() {

        RealMatrix V = ed.getV();
        vectors = V.getData();

        System.out.println(V);
    }

    public static void main(String[] args) {
        new PCA();
    }
}
