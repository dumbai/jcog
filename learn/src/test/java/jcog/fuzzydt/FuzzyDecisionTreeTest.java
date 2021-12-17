package jcog.fuzzydt;

import jcog.fuzzydt.data.Attribute;
import jcog.fuzzydt.data.Dataset;
import jcog.fuzzydt.data.Row;
import jcog.fuzzydt.utils.*;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;

class FuzzyDecisionTreeTest {

    static final String data = 
            "#Temperature:Hot Mild Cool\n" +
            "#Outlook:Sunny Cloudy Rain\n" +
            "#Humidity:Humid Normal\n" +
            "#Wind:Windy Not-windy\n" +
            "@Plan:Volleyball Swimming Weightlifting\n" +
            "=Data=\n" +
            "=ROW=1 0.7 0.2 0.1 1.0 0.0 0.0 0.7 0.3 0.4 0.6 0.0 0.6 0.4\n" +
            "=ROW=2 0.6 0.2 0.2 0.6 0.4 0.0 0.6 0.4 0.9 0.1 0.7 0.6 0.0\n" +
            "=ROW=3 0.0 0.7 0.3 0.8 0.2 0.0 0.2 0.8 0.2 0.8 0.3 0.6 0.1\n" +
            "=ROW=4 0.2 0.7 0.1 0.3 0.7 0.0 0.8 0.2 0.3 0.7 0.9 0.1 0.0\n" +
            "=ROW=5 0.0 0.1 0.9 0.7 0.3 0.0 0.5 0.5 0.5 0.5 1.0 0.0 0.0\n" +
            "=ROW=6 0.0 0.7 0.3 0.0 0.3 0.7 0.3 0.7 0.4 0.6 0.2 0.2 0.6\n" +
            "=ROW=7 0.0 0.3 0.7 0.0 0.0 1.0 0.8 0.2 0.1 0.9 0.0 0.0 1.0\n" +
            "=ROW=8 0.0 1.0 0.0 0.0 0.9 0.1 0.1 0.9 0.0 1.0 0.3 0.0 0.7\n" +
            "=ROW=9 1.0 0.0 0.0 1.0 0.0 0.0 0.4 0.6 0.4 0.6 0.4 0.7 0.0\n" +
            "=ROW=10 0.7 0.2 0.1 0.0 0.3 0.7 0.8 0.2 0.9 0.1 0.0 0.3 0.7\n" +
            "=ROW=11 0.6 0.3 0.1 1.0 0.0 0.0 0.7 0.3 0.2 0.8 0.4 0.7 0.0\n" +
            "=ROW=12 0.2 0.6 0.2 0.0 1.0 0.0 0.7 0.3 0.7 0.3 0.7 0.2 0.1\n" +
            "=ROW=13 0.7 0.3 0.0 0.0 0.9 0.1 0.1 0.9 0.0 1.0 0.0 0.4 0.6\n" +
            "=ROW=14 0.1 0.6 0.3 0.0 0.9 0.1 0.7 0.3 0.7 0.3 1.0 0.0 0.0\n" +
            "=ROW=15 0.0 0.0 1.0 0.0 0.3 0.7 0.2 0.8 0.8 0.2 0.4 0.0 0.6\n" +
            "=ROW=16 1.0 0.0 0.0 0.5 0.5 0.0 1.0 0.0 1.0 0.0 0.7 0.6 0.0";

    static final Dataset d = new Dataset();
    static {
        try {
            BufferedReader br = new BufferedReader(
                    //new FileReader("data/test.txt")
                    new StringReader(data)
            );
            String line;
            String className;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.startsWith("#")) {
                    String[] terms = line.substring(line.indexOf(':') + 1).split(" ");
                    d.addAttribute(new Attribute(line.substring(1, line.indexOf(':')), terms));
                } else if (line.startsWith("@")) {
                    String[] terms = line.substring(line.indexOf(':') + 1).split(" ");
                    d.addAttribute(new Attribute(line.substring(1, line.indexOf(':')), terms));
                    className = line.substring(1, line.indexOf(':'));
                    d.setClassName(className);
                } else if (line.startsWith("=ROW=")) {
                    String[] data = line.split(" ");
                    Object[] crispRow = new Object[d.getAttributesCount()];
                    double[][] fuzzyValues = new double[d.getAttributesCount()][];
                    int k = 1;
                    for (int i = 0; i < crispRow.length; i++) {
                        crispRow[i] = "Dummy";
                        fuzzyValues[i] = new double[d.attr(i).getLinguisticTermsCount()];
                        for (int j = 0; j < fuzzyValues[i].length; j++)
                            fuzzyValues[i][j] = Double.parseDouble(data[k++]);
                    }

                    d.addRow(new Row(crispRow, fuzzyValues));
                }
            }

            br.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    PreferenceMeasure preferenceMeasure;
    LeafDeterminer leafDeterminer;
    TreeNode root;
    FuzzyDecisionTree descisionTree;
    String[] rules;

    @Test
    void test1() {
        System.out.println("Ambiguity induction fuzzy decision tree");
        preferenceMeasure = new AmbiguityMeasure(0.5);
        leafDeterminer = new LeafDeterminerBase(0.8);
        descisionTree = new FuzzyDecisionTree(preferenceMeasure, leafDeterminer);

        root = descisionTree.buildTree(d);

        FuzzyDecisionTree.printTree(root, "");

        rules = FuzzyDecisionTree.generateRules(root);
        for (String rule : rules) System.out.println(rule);
        System.out.println("Accuracy : " + FuzzyDecisionTree.getAccuracy(rules));
        System.out.println();

    }
    @Test
    void test2() {
        System.out.println("FID3 fuzzy decision tree");
        preferenceMeasure = new FuzzyPartitionEntropyMeasure();
        leafDeterminer = new LeafDeterminerBase(0.8);
        descisionTree = new FuzzyDecisionTree(preferenceMeasure, leafDeterminer);

        root = descisionTree.buildTree(d);

        FuzzyDecisionTree.printTree(root, "");

        rules = FuzzyDecisionTree.generateRules(root);
        for (String rule : rules) System.out.println(rule);

        System.out.println("Accuracy : " + FuzzyDecisionTree.getAccuracy(rules));

        System.out.println();
    }

    @Test void test3() {

        System.out.println("GFID3 fuzzy decision tree, with linear maping function {I(t) = t}");
        preferenceMeasure = new GeneralizedFuzzyPartitionEntropyMeasure();
        leafDeterminer = new GeneralizedLeafDeterminer(0.8);
        descisionTree = new FuzzyDecisionTree(preferenceMeasure, leafDeterminer);


        root = descisionTree.buildTree(d);

        FuzzyDecisionTree.printTree(root, "");

        rules = FuzzyDecisionTree.generateRules(root);

        for (String rule : rules) System.out.println(rule);

        System.out.println("Accuracy : " + FuzzyDecisionTree.getAccuracy(rules));

        System.out.println();
    }

    @Test void test4() {
        System.out.println("GFID3 fuzzy decision tree, with quadratic maping function {I(t) = t^2}");
        preferenceMeasure = new GeneralizedFuzzyPartitionEntropyMeasure(new MappingFunction.QuadraticMappingFunction());
        leafDeterminer = new GeneralizedLeafDeterminer(0.8, new MappingFunction.QuadraticMappingFunction());
        descisionTree = new FuzzyDecisionTree(preferenceMeasure, leafDeterminer);


        root = descisionTree.buildTree(d);

        FuzzyDecisionTree.printTree(root, "");

        rules = FuzzyDecisionTree.generateRules(root);
        for (String rule : rules) System.out.println(rule);

        System.out.println("Accuracy : " + FuzzyDecisionTree.getAccuracy(rules));

        System.out.println();
    }

    @Test void test5() {

        System.out.println("GFID3 fuzzy decision tree, with quadratic maping function {I(t) = t^0.5}");
        preferenceMeasure = new GeneralizedFuzzyPartitionEntropyMeasure(new MappingFunction.SqrtMappingFunction());
        leafDeterminer = new GeneralizedLeafDeterminer(0.8, new MappingFunction.SqrtMappingFunction());
        descisionTree = new FuzzyDecisionTree(preferenceMeasure, leafDeterminer);


        root = descisionTree.buildTree(d);

        FuzzyDecisionTree.printTree(root, "");

        rules = FuzzyDecisionTree.generateRules(root);
        for (String rule : rules) System.out.println(rule);

        System.out.println("Accuracy : " + FuzzyDecisionTree.getAccuracy(rules));

        System.out.println();
    }

    @Test void test6() {

        System.out.println("GFID3 fuzzy decision tree, with quadratic maping function {I(t) = t^3}");
        preferenceMeasure = new GeneralizedFuzzyPartitionEntropyMeasure(new MappingFunction.QuadraticMappingFunction());
        leafDeterminer = new GeneralizedLeafDeterminer(0.8, new MappingFunction.QuadraticMappingFunction());
        descisionTree = new FuzzyDecisionTree(preferenceMeasure, leafDeterminer);


        root = descisionTree.buildTree(d);

        FuzzyDecisionTree.printTree(root, "");

        rules = FuzzyDecisionTree.generateRules(root);
        for (String rule : rules) System.out.println(rule);

        System.out.println("Accuracy : " + FuzzyDecisionTree.getAccuracy(rules));

        System.out.println();


    }

}