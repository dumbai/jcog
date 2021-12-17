package jcog.learn;

import jcog.Str;
import jcog.deep.Autoencoder;
import jcog.deep.StackedAutoencoder;
import org.junit.jupiter.api.Test;

import java.util.Random;

/**
 * Created by me on 2/18/16.
 */
class AutoencoderTest {

    @Test
    void stackedAutoEncoder1() {
        Random rng = new Random(123);

        double pretrain_lr = 0.1;
        double corruption_level = 0.3;
        int pretraining_epochs = 1000;

        int n_ins = 28;
        int n_outs = 2;
        int[] hidden_layer_sizes = {15, 15};


        double[][] train_X = {
                {1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                {0, 1, 1, 1, 1, 0, 1, 1, 1, 1, 1, 1, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                {1, 1, 0, 1, 1, 1, 0, 1, 1, 1, 1, 1, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                {0, 1, 1, 1, 1, 1, 1, 1, 0, 0, 1, 1, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                {1, 1, 0, 0, 1, 1, 1, 1, 1, 1, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1},
                {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 1, 1, 1, 1, 1, 1, 1, 1, 0, 1, 0, 1},
                {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 0, 1, 1, 1, 1, 1, 0, 1, 1, 0, 1, 1},
                {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 1, 1, 0, 0, 1, 1, 1, 1, 0, 1, 1, 1},
                {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 1, 1, 1, 1, 1, 1, 0, 1, 1, 1, 1}
        };


        StackedAutoencoder sda = new StackedAutoencoder(n_ins,
                hidden_layer_sizes, n_outs, rng);


        //sda.pretrain(train_X, pretrain_lr, corruption_level, pretraining_epochs);


        double[][] train_Y = {
                {1, 0},
                {1, 0},
                {1, 0},
                {1, 0},
                {1, 0},
                {0, 1},
                {0, 1},
                {0, 1},
                {0, 1},
                {0, 1}
        };
        int finetune_epochs = 500;
        double finetune_lr = 0.1;

        for (int i = 0; i < finetune_epochs; i++)
            sda.put(train_X, train_Y, finetune_lr);


        double[][] test_X = {
                {1, 1, 1, 1, 1, 1, 0, 1, 0, 1, 1, 1, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                {1, 1, 1, 1, 1, 0, 1, 0, 1, 1, 0, 1, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 1, 1, 0, 0, 1, 1, 0, 1, 1, 1, 1, 1},
                {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 1, 1, 1, 1, 0, 1, 1, 1, 1, 1, 1, 1}
        };

        int test_N = 4;
        double[][] test_Y = new double[test_N][n_outs];


        for (int i = 0; i < test_N; i++) {
            sda.predict(test_X[i], test_Y[i]);
            for (int j = 0; j < n_outs; j++) {
                System.out.print(test_Y[i][j] + " ");
            }
            System.out.println();
        }

    }

    @Test
    void test_dA() {
        Random rng = new Random(123);
        float noise_level = 0.01f, corruption_level = 0.01f;
        int training_epochs = 40;
        int train_N = 10;
        int n_visible = 20;
        int n_hidden = 7;
        float learning_rate = 0.1f; //0.1f / train_N;
        boolean sigmoid = true, normalize = false;
        float[][] train_X = {
                {1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                {1, 0, 1, 1, 1, 1, 1, 1, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                {1, 1, 0, 1, 1, 1, 1, 1, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                {1, 1, 1, 1, 0, 0, 1, 1, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                {0, 1, 1, 1, 1, 1, 1, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1},
                {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 1, 1, 1, 1, 1, 1, 1, 1},
                {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 0, 1, 1, 1, 1, 1, 0, 1},
                {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 1, 1, 0, 0, 1, 1, 1, 1},
                {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 1, 1, 1, 1, 1, 1, 0}};
        Autoencoder da = new Autoencoder(n_visible, n_hidden, rng);
        da.noise.set(noise_level);
        da.corruption.set(corruption_level);
        da.normalize.set(normalize);

        //DescriptiveStatistics meanErrorPerInput = new DescriptiveStatistics(train_N * 2);
        for (int epoch = 0; epoch < training_epochs; epoch++) {
            for (int i = 0; i < train_N; i++) {
                da.put(train_X[i], learning_rate);
                //System.out.println(err);
                //meanErrorPerInput.addValue(err/train_X[i].length);
            }
        }
        //System.out.println("mean error per input: " + meanErrorPerInput);
        //assertTrue(meanErrorPerInput.getMean() < 0.25f);


        float[][] test_X = {
                {1, 0, 1, 0, 1, 0, 1, 0, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 0, 1, 1, 0, 1, 1, 0}};

        int test_N = 2;
        for (int i = 0; i < test_N; i++) {
            System.out.println(Str.n4(test_X[i]));
            float[] reconstructed_X = da.reconstruct(test_X[i]);
            System.out.println(Str.n4(reconstructed_X));
            System.out.println('\t' + Str.n4(da.get(test_X[i])));
            System.out.println();
        }
    }
}