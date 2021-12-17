package jcog.deep;

import jcog.Util;
import org.junit.jupiter.api.Test;

import java.util.Random;

class RBMTest {

    @Test
    void test1() {
                Random rng = new Random(123);

        double learning_rate = 0.1;
        int training_epochs = 1000;
        int k = 1;

        int train_N = 6;
        int n_visible = 6;
        int n_hidden = 3;

        
        double[][] train_X = {
                {1, 1, 1, 0, 0, 0},
                {1, 0, 1, 0, 0, 0},
                {1, 1, 1, 0, 0, 0},
                {0, 0, 1, 1, 1, 0},
                {0, 0, 1, 0, 1, 0},
                {0, 0, 1, 1, 1, 0}
        };



        RBM rbm = new RBM(n_visible, n_hidden, null, null, null, rng) {
            @Override
            public double activate(double a) {
                return Util.sigmoidBipolar((float) a, 5);
                
            }
        };

        
        for(int epoch=0; epoch<training_epochs; epoch++) {
            for(int i=0; i<train_N; i++) {
                rbm.contrastive_divergence(train_X[i], learning_rate, k);
            }
        }

        
        double[][] test_X = {
                {1, 1, 0, 0, 0, 0},
                {0, 0, 0, 1, 1, 0}
        };

        int test_N = 2;
        double[][] reconstructed_X = new double[test_N][n_visible];

        for(int i=0; i<test_N; i++) {
            rbm.reconstruct(test_X[i], reconstructed_X[i]);
            for(int j=0; j<n_visible; j++) {
                System.out.printf("%.5f ", reconstructed_X[i][j]);
            }
            System.out.println();
        }

    }
}