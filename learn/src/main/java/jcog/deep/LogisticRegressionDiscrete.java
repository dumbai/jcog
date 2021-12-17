package jcog.deep;

public class LogisticRegressionDiscrete extends LogisticRegression {
    public int n_in;
    public int n_out;
    public double[][] W;
    public double[] b;

    public LogisticRegressionDiscrete(int n_in, int n_out) {
        super(n_in, n_out);

        this.n_in = n_in;
        this.n_out = n_out;

        W = new double[this.n_out][this.n_in];
        b = new double[this.n_out];
    }

    @Override
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
        return p_y_given_x;
    }

    public void predict(int[] x, double[] y) {
        for(int i=0; i<n_out; i++) {
            y[i] = 0;
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
                {1, 1, 1, 0, 0, 0},
                {1, 0, 1, 0, 0, 0},
                {1, 1, 1, 0, 0, 0},
                {0, 0, 1, 1, 1, 0},
                {0, 0, 1, 1, 0, 0},
                {0, 0, 1, 1, 1, 0}
        };

        double[][] train_Y = {
                {1, 0},
                {1, 0},
                {1, 0},
                {0, 1},
                {0, 1},
                {0, 1}
        };

        
        LogisticRegressionDiscrete classifier = new LogisticRegressionDiscrete(n_in, n_out);

        
        for(int epoch=0; epoch<n_epochs; epoch++) {
            for(int i=0; i<train_N; i++) {
                classifier.put(train_X[i], train_Y[i], learning_rate);
            }
            
        }

        
        int[][] test_X = {
                {1, 0, 1, 0, 0, 0},
                {0, 0, 1, 1, 1, 0}
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

//    public static void main(String[] args) {
//        test_lr();
//    }
}