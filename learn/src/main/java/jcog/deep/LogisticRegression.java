package jcog.deep;

public class LogisticRegression {
    public int n_in;
    public int n_out;
    public double[][] W;
    public double[] b;

    public LogisticRegression(int n_in, int n_out) {
        this.n_in = n_in;
        this.n_out = n_out;

        W = new double[n_out][n_in];
        b = new double[n_out];
    }

    /** returns delta y */
    public double[] put(double[] x, double[] y, double lr) {
        double[] p_y_given_x = new double[n_out];
        double[] dy = new double[n_out];

        for(int i=0; i<n_out; i++) {
            p_y_given_x[i] = 0;
            for(int j=0; j<n_in; j++) {
                p_y_given_x[i] += W[i][j] * x[j];
            }
            p_y_given_x[i] += b[i];
        }
        softmax(p_y_given_x);

        for(int i=0; i<n_out; i++) {
            dy[i] = y[i] - p_y_given_x[i];

            for(int j=0; j<n_in; j++) {
                W[i][j] += lr * dy[i] * x[j];
            }

            b[i] += lr * dy[i];
        }

        return dy;
    }

    @Deprecated void softmax(double[] x) {

        boolean seen = false;
        double best = 0;
        int bound = n_out;
        for (int j = 0; j < bound; j++) {
            double v = x[j];
            if (v >= 0.0) {
                if (!seen || Double.compare(v, best) > 0) {
                    seen = true;
                    best = v;
                }
            }
        }
        double max = seen ? best : 0.0;

        double sum = 0.0;
        for(int i = 0; i<n_out; i++) {
            x[i] = Math.exp(x[i] - max);
            sum += x[i];
        }

        for(int i=0; i<n_out; i++) x[i] /= sum;
    }

    public void predict(double[] x, double[] y) {
        for(int i=0; i<n_out; i++) {
            y[i] = 0.;
            for(int j=0; j<n_in; j++) {
                y[i] += W[i][j] * x[j];
            }
            y[i] += b[i];
        }

        softmax(y);
    }

    private static void test_lr() {
        double learning_rate = 0.1;
        int n_epochs = 500;

        int train_N = 6;
        int n_in = 6;
        int n_out = 2;

        double[][] train_X = {
                {1., 1., 1., 0., 0., 0.},
                {1., 0., 1., 0., 0., 0.},
                {1., 1., 1., 0., 0., 0.},
                {0., 0., 1., 1., 1., 0.},
                {0., 0., 1., 1., 0., 0.},
                {0., 0., 1., 1., 1., 0.}
        };

        double[][] train_Y = {
                {1, 0},
                {1, 0},
                {1, 0},
                {0, 1},
                {0, 1},
                {0, 1}
        };

        
        LogisticRegression classifier = new LogisticRegression(n_in, n_out);

        
        for(int epoch=0; epoch<n_epochs; epoch++) {
            for(int i=0; i<train_N; i++) {
                classifier.put(train_X[i], train_Y[i], learning_rate);
            }
            
        }

        
        double[][] test_X = {
                {1., 0., 1., 0., 0., 0.},
                {0., 0., 1., 1., 1., 0.}
        };

        int test_N = 2;
        double[][] test_Y = new double[test_N][n_out];


        
        for(int i=0; i<test_N; i++) {
            classifier.predict(test_X[i], test_Y[i]);
            for(int j=0; j<n_out; j++) {
                System.out.print(test_Y[i][j] + " ");
            }
            System.out.println();
        }
    }

    public static void main(String[] args) {
        test_lr();
    }
}