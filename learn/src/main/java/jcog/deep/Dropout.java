package jcog.deep;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Dropout {
    public final int N;
    public final int n_in;
    public final int[] hidden_layer_sizes;
    public final int n_out;
    public final int n_layers;
    public final HiddenLayer[] hiddenLayers;
    public final LogisticRegression logisticLayer;
    public final Random rng;


    public Dropout(int N, int n_in, int[] hidden_layer_sizes, int n_out, Random rng, String activation) {
        this.N = N;
        this.n_in = n_in;
        this.hidden_layer_sizes = hidden_layer_sizes;
        this.n_layers = hidden_layer_sizes.length;
        this.n_out = n_out;

        this.hiddenLayers = new HiddenLayer[n_layers];

        if (rng == null) rng = new Random(1234);
        this.rng = rng;

        if (activation == null) activation = "ReLU";


        for(int i=0; i<this.n_layers; i++) {

            int input_size;
			input_size = i == 0 ? n_in : hidden_layer_sizes[i - 1];

            
            this.hiddenLayers[i] = new HiddenLayer(input_size, hidden_layer_sizes[i], null, null, rng, activation);

        }

        
        this.logisticLayer = new LogisticRegression(hidden_layer_sizes[this.n_layers-1], n_out);

    }

    public void train(int epochs, double[][] train_X, double[][] train_Y, boolean dropout, double p_dropout, double lr) {
        double[] layer_output = new double[0];

        for(int epoch=0; epoch<epochs; epoch++) {

            for(int n=0; n<N; n++) {

                List<double[]> dropout_masks = new ArrayList<>(n_layers);
                List<double[]> layer_inputs = new ArrayList<>(n_layers + 1);


                for(int i=0; i<n_layers; i++) {

                    double[] layer_input;
					layer_input = i == 0 ? train_X[n] : layer_output.clone();

                    layer_inputs.add(layer_input.clone());

                    layer_output = new double[hidden_layer_sizes[i]];
                    hiddenLayers[i].forward(layer_input, layer_output);

                    if(dropout) {
                        double[] mask = HiddenLayer.dropout(layer_output.length, p_dropout, rng);
                        for(int j=0; j<layer_output.length; j++) layer_output[j] *= mask[j];

                        dropout_masks.add(mask.clone());
                    }

                }


                double[] logistic_layer_dy = logisticLayer.put(layer_output, train_Y[n], lr);
                layer_inputs.add(layer_output.clone());

                
                double[] prev_dy = logistic_layer_dy;
                double[] dy = new double[0];

                for(int i=n_layers-1; i>=0; i--) {

                    double[][] prev_W;
                    if(i == n_layers-1) {
                        prev_W = logisticLayer.W;
                    } else {
                        prev_dy = dy.clone();
                        prev_W = hiddenLayers[i+1].W;
                    }

                    if(dropout) {
                        for(int j=0; j<prev_dy.length; j++) {
                            prev_dy[j] *= dropout_masks.get(i)[j];
                        }
                    }

                    dy = new double[hidden_layer_sizes[i]];
                    hiddenLayers[i].backward(layer_inputs.get(i), dy, layer_inputs.get(i+1), prev_dy, prev_W, lr);
                }

            }
        }
    }


    public void pretest(double p_dropout) {
        for(int i=0; i<n_layers; i++) {
            int in;

			in = i == 0 ? n_in : hidden_layer_sizes[i];

            int out;
			out = i == n_layers - 1 ? n_out : hidden_layer_sizes[i + 1];


            for (int l = 0; l < out; l++) {
                for (int m = 0; m < in; m++) {
                    hiddenLayers[i].W[l][m] *= 1 - p_dropout;
                }
            }
        }
    }


    public void predict(double[] x, double[] y) {
        double[] layer_output = new double[0];

        for(int i=0; i<n_layers; i++) {

            double[] layer_input;
			layer_input = i == 0 ? x : layer_output.clone();

            layer_output = new double[hidden_layer_sizes[i]];

            hiddenLayers[i].forward(layer_input, layer_output);
        }

        logisticLayer.predict(layer_output, y);
    }


    private static void test_dropout() {
        Random rng = new Random(123);

        double learning_rate = 0.1;
        int n_epochs = 5000;

        int train_N = 4;
        int n_in = 2;
        int[] hidden_layer_sizes = {10, 10};
        int n_out = 2;

        boolean dropout = true;
        double p_dropout = 0.5;


        double[][] train_X = {
                {0., 0.},
                {0., 1.},
                {1., 0.},
                {1., 1.},
        };

        double[][] train_Y = {
                {0, 1},
                {1, 0},
                {1, 0},
                {0, 1},
        };

        
        Dropout classifier = new Dropout(train_N, n_in, hidden_layer_sizes, n_out, rng, "ReLU");

        
        classifier.train(n_epochs, train_X, train_Y, true, p_dropout, learning_rate);


        classifier.pretest(p_dropout);


        
        double[][] test_X = {
                {0., 0.},
                {0., 1.},
                {1., 0.},
                {1., 1.},
        };

        int test_N = 4;
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
        test_dropout();
    }
}