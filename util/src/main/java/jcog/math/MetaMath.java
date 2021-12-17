package jcog.math;// mm.java Copyright (c) 2015 Norman Megill nm at alum dot mit dot edu

// Copyright under terms of GNU General Public License

// To compile on Windows:  Download and install the Java SDK.  The setup
// file that I downloaded from java.sun.com on 11-Nov-2015 was called
// jdk-8u66-windows-x64.exe (186.65 MB).  After installing, open the
// Command Prompt, cd to the directory with mm.java, and type
//
//    "C:\Program Files\Java\jdk1.8.0_66\bin\javac.exe" mm.java
//
// To run the applet locally, make sure mmsviewer.html is in the same
// directory as mm.java and open it with a browser.  The URL "file:///" may
// have to be added in Security -> Exception Site List in the Java Control
// Panel (invoked by "Configure Java" in the Start menu).
//

// History: 5-Aug-1997   Initial release
//          13-Apr-2001  Upgraded to Java 2 by Marcello DeMarinis
//          5-Jun-2002   Fixed "Save As Axiom" bug that incorrectly suppressed
//                       user-created theorems from the list of choices when
//                       the proof of the theorem contained hypotheses
//          7-Aug-2003   Fixed typos in Euclid's axioms eu8 and eu3
//                       (found by Russell O'Connor)
//          28-Mar-2006  (Scott Fenton) upgrade to use Java 2 SDK 1.5
//          11-Nov-2015  Workaround problem caused by different java.awt.Choice
//                       behavior in Oracle Java update

// Note:  I have included the entire program in one file (contrary to
// recommended Java programming guidelines) as I believe this is more
// convenient for distribution.  Also, the main class mm is not upper-case
// (contrary to recommended Java programming guidelines) because I got tired
// of shifting; thus this file must be called mm.java, not MM.java nor Mm.java.

// A future version may or may not include sound effects.  This program was
// originally written when such things were new and trendy.  Do you think they
// should be kept, or are they too silly?  Anyway, if you strip out all lines
// with the tag "/* [sound] */", you will only need the standalone
// file "mm.java" to recompile the program and will not need any .au files.

/* [sound] */ // The .au files used are:
/* [sound] */ //
/* [sound] */ // bart.aye_carumba.au
/* [sound] */ // beep_spring.au
/* [sound] */ // boing.au
/* [sound] */ // bomb.au
/* [sound] */ // bubble1.au
/* [sound] */ // clink.au
/* [sound] */ // drip.au
/* [sound] */ // gate.au
/* [sound] */ // hypspc.au
/* [sound] */ // ni.au
/* [sound] */ // ouch.au
/* [sound] */ // splat.au
/* [sound] */ // whoosh.au
/* [sound] */ // wzzz.au
/* [sound] */ // wzzz2.au
/* [sound] */ // zoom.au

import javax.swing.*;
import java.applet.AudioClip;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Stack;

@SuppressWarnings("serial")
class MetaMath extends /*@Deprecated */ JPanel  implements ActionListener, ItemListener {

    static final int MAX_AXIOM_CHOICE_LEN = 30; // So axiom choice menu
    static final int UNDO_STACK_KEEP = 100; // Undo depth
    // Axiom storage
    private static final int PROP_CALC = 0;
    private static final int PROP_DEFS = 1;
    private static final int PRED_CALC = 2;
    private static final int PRED_DEFS = 3;
    private static final int SET_THEORY = 4;
    private static final int SET_DEFS = 5;
    private static final int IMPL_LOGIC = 6;
    private static final int INTUIT_LOGIC = 7;
    private static final int MODAL_LOGIC = 8;
    //  won't be too long
    private static final int GODEL_LOGIC = 9;
    private static final int QUANTUM_LOGIC = 10;
    private static final int EUCLID = 11;
    private static final int WEAKD_LOGIC = 12;
    private static final String[] familyName = {
            "Propositional Calculus",
            "Propositional Calculus + Definitions",
            "Predicate Calculus",
            "Predicate Calculus + Definitions",
            "ZFC Set Theory",
            "ZFC Set Theory + Definitions",
            "Implicational Logic",
            "Intuitionist Propositional Calculus",
            "Modal Logic",
            "Modal Provability Logic",
            "Quantum Logic",
            "Euclidean Geometry",
            "Weak D-Complete Logic"
    };

    // A future version should parse this from a file?
    private static final int FAMILIES = 13;
    @SuppressWarnings("unchecked")
    private static final ArrayList<Axiom>[] axiomFamily = new ArrayList[FAMILIES]; // ArrayLists of State
    static final Color DARK_GREEN = new Color(0, 100, 0);
    private static final Color BACKGROUND_COLOR = new Color(210, 255, 255);
    private static final Color PROOF_BACKGROUND_COLOR = new Color(255, 255, 165);
    private static final Color INFO_BACKGROUND_COLOR = new Color(255, 210, 255);
    // Font parameters
    private static final int FONT_SIZE = 12;
    private static final String MATH_FONT_NAME
            = "Courier"; //"TimesRoman"; // math variables & ASCII symbols
    private static final Font MATH_PLAIN_FONT =
            new Font(MATH_FONT_NAME, Font.PLAIN, FONT_SIZE);
    static final Font MATH_ITALIC_FONT =
            new Font(MATH_FONT_NAME, Font.ITALIC, FONT_SIZE);
    static final int Y_INCREMENT = (FONT_SIZE * 3) / 2; // Line-to-line spacing
    static final int X_INIT = FONT_SIZE / 2; // Left margin
    static final int CHAR_SPACE = -1; // Space between chars of token
    static final int WHITE_SPACE = 2; // Space between tokens
    /* [sound] */ // Sound effects
    /* [sound] */ private static String audioName;
    /* [sound] */ private static boolean enableAudioFlag = false;
    private static boolean proofInfoModeFlag = false;
    private static boolean axiomInfoModeFlag = false;
    private static boolean selectLogicModeFlag = false;
    private static int infoModeAxiomToShow;
    // Current state of stack
    private static State currentState;
    // Undo/redo variables
    private static Stack<State> undoStack;
    private static Stack<State> redoStack; // (Future - not implemented yet - NDM)
    // Connective storage
    static Connective[] connectiveArr;
    static String connectiveLabels; // For speedup
    static String connectiveLabelMap; // For speedup
    static int currentFamily; // Current family
    static Axiom[] axiomArr; // Current axiom set
    static ArrayList<Axiom> userTheorems; // ArrayList of State
    private static int maxAxiomHypotheses; // set by axiom with the most hypotheses
    private static int currentX;
    private static int currentY;
    /* [sound] */ private static final ArrayList<String> audioSaveNameVec = new ArrayList<>();
    /* [sound] */ private static final ArrayList<AudioClip> audioSaveClipVec = new ArrayList<>();
    //private Button clear_button;
    private Choice option_choices;
    private Button proof_exit_button;
    private Button info_exit_button;
    private Choice axiom_choices;
    // private Label axiom_label = new Label("Axioms:", Label.RIGHT);
    // 11-Nov-2015 nm "Axioms:" label is now 1st menu entry
    private final Label axiom_label = new Label("       ", Label.RIGHT);
    private ArrayList<Integer> axiomChoiceVec;
    private TextArea proof_text;
    private final Checkbox[] logic_select = new Checkbox[FAMILIES];

    public static void main(String[] argv) {
        MetaMath m = new MetaMath();
        m.init();
        JFrame j = new JFrame();
        j.setSize(500,500);
        j.setContentPane(m);
        j.setVisible(true);

        for (Axiom axiom : axiomArr) {
            VariableName.init();
            System.out.println(PrimFormula.getDisplay(axiom.assertion, false));
        }
    }

    // Builds the axiomArr array based on the chosen logic familiy
    // and adds to it all user theorems that are valid in that
    // logic family
    private static Axiom[] buildAxiomArr(int logicFamily) {
        ArrayList<Axiom> axiomVec = new ArrayList<>(axiomFamily[logicFamily]);

        // Build a string with all axiom labels
        String axiomLabels = " ";
        if (!userTheorems.isEmpty()) { // Only build label list if it will be used
            for (Axiom axiom : axiomVec) {
                axiomLabels = axiomLabels + axiom.label + " ";
            }
        }

        // For each user theorem, accept it only if all steps of each proof
        // are in the axiomLabels string
        for (Axiom userTh : userTheorems) {
            String userProof = " " + userTh.proof + " ";
            int position0 = 1;
            int position = userProof.indexOf(' ', position0);
            boolean validProof = true;
            while (position != -1) {
                String userProofStepLabel = userProof.substring(position0 - 1,
                    position + 1);
                if (!axiomLabels.contains(userProofStepLabel)
                    // 5-Jun-2002 (ndm) The condition below was added in case the
                    //   users proof contains hypotheses.
                    && userProofStepLabel.charAt(1) != '$')
                // The userProofStepLabel is surrounded by spaces, so we match
                //     $hypnn starting at position 1, not 0
                // future: make sure that $ is not allowed in user theorem
                // names if an enhancement is made to accept user names
                {
                    // If it's not an axiom or hypothesis, it's not a valid theorem for
                    // this logic family, so we don't put it in the list of choices
                    // available to the user.
                    validProof = false;
                    break;
                }
                position0 = position + 1;
                position = userProof.indexOf(' ', position0);
            }
            if (validProof) {
                axiomVec.add(0, userTh);
                // Add label since a later proof could use it
                axiomLabels = axiomLabels + userTh.label + " ";
            }
        } // for i
        Axiom[] axArr = new Axiom[axiomVec.size()];
        axiomVec.toArray(axArr);
        return axArr;
    }

    private void init() {

        userTheorems = new ArrayList<>();
        currentFamily = PROP_CALC;
        currentState = new State();
        undoStack = new Stack<>();
        redoStack = new Stack<>();

        // A future version could parse this from a file?

        // ****** Connectives ********

        // Arguments are label, type of result, # vars, display notation template
        // Labels are same as in Metamath set.mm

        // Implication
        Connective tmpConnective = new Connective("wi", "wff", 2, "( $1 -> $2 )");
        tmpConnective.setArgtype(0, "wff");
        tmpConnective.setArgtype(1, "wff");
        ArrayList<Connective> connectiveVec = new ArrayList<>();
        connectiveVec.add(tmpConnective);

        // Negation
        tmpConnective = new Connective("wn", "wff", 1, "-. $1");
        tmpConnective.setArgtype(0, "wff");
        connectiveVec.add(tmpConnective);

        // Universal quantifier
        tmpConnective = new Connective("wal", "wff", 2, "A. $1 $2");
        tmpConnective.setArgtype(0, "var");
        tmpConnective.setArgtype(1, "wff");
        connectiveVec.add(tmpConnective);

        // Define equality as primitive on classes, not variables for convenience
        tmpConnective = new Connective("weq", "wff", 2, "$1 = $2");
        tmpConnective.setArgtype(0, "class");
        tmpConnective.setArgtype(1, "class");
        connectiveVec.add(tmpConnective);

        // Define membership as primitive on classes, not variable for convenience
        tmpConnective = new Connective("wel", "wff", 2, "$1 e. $2");
        tmpConnective.setArgtype(0, "class");
        tmpConnective.setArgtype(1, "class");
        connectiveVec.add(tmpConnective);

        // Convert variable to class (invisible notation)
        tmpConnective = new Connective("cv", "class", 1, "$1");
        tmpConnective.setArgtype(0, "var");
        connectiveVec.add(tmpConnective);

        // Biconditional
        tmpConnective = new Connective("wb", "wff", 2, "( $1 <-> $2 )");
        tmpConnective.setArgtype(0, "wff");
        tmpConnective.setArgtype(1, "wff");
        connectiveVec.add(tmpConnective);

        // Disjunction ('or')
        tmpConnective = new Connective("wo", "wff", 2, "( $1 \\/ $2 )");
        tmpConnective.setArgtype(0, "wff");
        tmpConnective.setArgtype(1, "wff");
        connectiveVec.add(tmpConnective);

        // Conjunction ('and')
        tmpConnective = new Connective("wa", "wff", 2, "( $1 /\\ $2 )");
        tmpConnective.setArgtype(0, "wff");
        tmpConnective.setArgtype(1, "wff");
        connectiveVec.add(tmpConnective);

        // Existential quantifier
        tmpConnective = new Connective("wex", "wff", 2, "E. $1 $2");
        tmpConnective.setArgtype(0, "var");
        tmpConnective.setArgtype(1, "wff");
        connectiveVec.add(tmpConnective);

        // Proper substitution of $1 for $2
        tmpConnective = new Connective("wsb", "wff", 3, "[ $1 / $2 ] $3");
        tmpConnective.setArgtype(0, "var");
        tmpConnective.setArgtype(1, "var");
        tmpConnective.setArgtype(2, "wff");
        connectiveVec.add(tmpConnective);

        // Abstraction class notation ('the class of sets x such that
        // P is true').  A class is not necessarily a set (i.e. may not exist)
        tmpConnective = new Connective("cab", "class", 2, "{ $1 | $2 }");
        tmpConnective.setArgtype(0, "var");
        tmpConnective.setArgtype(1, "wff");
        connectiveVec.add(tmpConnective);

        // Subclass
        tmpConnective = new Connective("wss", "wff", 2, "$1 (_ $2");
        tmpConnective.setArgtype(0, "class");
        tmpConnective.setArgtype(1, "class");
        connectiveVec.add(tmpConnective);

        // Empty set
        tmpConnective = new Connective("cnul", "class", 0, "(/)");
        connectiveVec.add(tmpConnective);

        // Union of two classes
        tmpConnective = new Connective("cun", "class", 2, "( $1 u. $2 )");
        tmpConnective.setArgtype(0, "class");
        tmpConnective.setArgtype(1, "class");
        connectiveVec.add(tmpConnective);

        // Intersection of two classes
        tmpConnective = new Connective("cin", "class", 2, "( $1 i^i $2 )");
        tmpConnective.setArgtype(0, "class");
        tmpConnective.setArgtype(1, "class");
        connectiveVec.add(tmpConnective);

        // Union of a class
        tmpConnective = new Connective("cuni", "class", 1, "U. $1");
        tmpConnective.setArgtype(0, "class");
        connectiveVec.add(tmpConnective);

        // Intersection of a class
        tmpConnective = new Connective("cint", "class", 1, "|^| $1");
        tmpConnective.setArgtype(0, "class");
        connectiveVec.add(tmpConnective);

        // Modal logic connectives

        // Necessity (box)
        tmpConnective = new Connective("wnec", "wff", 1, "[] $1");
        tmpConnective.setArgtype(0, "wff");
        connectiveVec.add(tmpConnective);

        // Possibility (diamond)
        tmpConnective = new Connective("wposs", "wff", 1, "<> $1");
        tmpConnective.setArgtype(0, "wff");
        connectiveVec.add(tmpConnective);

        // False constant
        tmpConnective = new Connective("wfalse", "wff", 0, "_|_");
        connectiveVec.add(tmpConnective);

        // Betweenness predicate (Euclidean geometry)
        tmpConnective = new Connective("wbt", "wff", 3, "B $1 $2 $3");
        tmpConnective.setArgtype(0, "var");
        tmpConnective.setArgtype(1, "var");
        tmpConnective.setArgtype(2, "var");
        connectiveVec.add(tmpConnective);

        // Distance predicate (Euclidean geometry)
        tmpConnective = new Connective("wd", "wff", 4, "D $1 $2 $3 $4");
        tmpConnective.setArgtype(0, "var");
        tmpConnective.setArgtype(1, "var");
        tmpConnective.setArgtype(2, "var");
        tmpConnective.setArgtype(3, "var");
        connectiveVec.add(tmpConnective);


        // Convert ArrayList to array
        connectiveArr = new Connective[connectiveVec.size()];
        connectiveVec.toArray(connectiveArr);

        // Build connective label and map strings for faster lookup
        connectiveLabels = " ";
        connectiveLabelMap = "";
        for (int i = 0; i < connectiveArr.length; i++) {
            connectiveLabels = connectiveLabels + connectiveArr[i].label + " ";
            // Only the valueOf will be used; the other characters are placeholders
            connectiveLabelMap = connectiveLabelMap + (char) i
                    + connectiveArr[i].label;
        }


        // ********************** Axioms *****************************

        // All axioms are specified in RPN; $n is variable placeholder
        // Labels are same as in Metamath set.mm

        // ************ Propositional calculus

        ArrayList<Axiom> axiomVec = new ArrayList<>();

        // ax-1 $a |- ( P -> ( Q -> P ) ) $.
        // We define a new variable (ax_1Axiom) for the axiom if
        // we will use it again in another system later.  Otherwise
        // we just use the variable tmpAxiom.
        Axiom ax_1Axiom = new Axiom("ax-1", "wi $1 wi $2 $1",
                "Axiom of simplification (propositional calculus)");
        axiomVec.add(ax_1Axiom);

        // ax-2 $a |- ( ( P -> ( Q -> R ) ) -> ( ( P -> Q ) ->
        //   ( P -> R ) ) ) $.
        Axiom ax_2Axiom = new Axiom("ax-2",
                "wi wi $1 wi $2 $3 wi wi $1 $2 wi $1 $3",
                "Axiom of distribution (propositional calculus)");
        axiomVec.add(ax_2Axiom);

        // ax-3 $a |- ( ( -. P -> -. Q ) -> ( Q -> P ) ) $.
        Axiom tmpAxiom = new Axiom("ax-3", "wi wi wn $1 wn $2 wi $2 $1",
                "Axiom of contraposition (propositional calculus)");
        axiomVec.add(tmpAxiom);

        // maj   $e |- ( P -> Q ) $.
        // min   $e |- P $.
        // ax-mp $a |- Q $.
        Axiom rule_mpAxiom = new Axiom("ax-mp", "$2",
                "Inference rule of modus ponens (propositional calculus)");
        rule_mpAxiom.addHyp("$1");
        rule_mpAxiom.addHyp("wi $1 $2");
        axiomVec.add(rule_mpAxiom);

        axiomFamily[PROP_CALC] = axiomVec;


        // ************ Propositional calculus with definitions

        axiomVec = new ArrayList<>(axiomFamily[PROP_CALC]); // Start w/ prop calc

        // df-bi1 $a |- ( ( P <-> Q ) -> ( P -> Q ) ) $.
        Axiom df_bi1Axiom = new Axiom("df-bi1", "wi wb $1 $2 wi $1 $2",
                "Definition of biconditional (part 1 of 3)");
        axiomVec.add(df_bi1Axiom);

        // df-bi2 $a |- ( ( P <-> Q ) -> ( Q -> P ) ) $.
        Axiom df_bi2Axiom = new Axiom("df-bi2", "wi wb $1 $2 wi $2 $1",
                "Definition of biconditional (part 2 of 3)");
        axiomVec.add(df_bi2Axiom);

        // df-bi3 $a |- ( ( P -> Q ) -> ( ( Q -> P ) -> ( P <-> Q ) ) ) $.
        Axiom df_bi3Axiom = new Axiom("df-bi3", "wi wi $1 $2 wi wi $2 $1 wb $1 $2",
                "Definition of biconditional (part 3 of 3)");
        axiomVec.add(df_bi3Axiom);

        // df-or $a |- ( ( P \/ Q ) <-> ( -. P -> Q ) ) $.
        Axiom df_orAxiom = new Axiom("df-or", "wb wo $1 $2 wi wn $1 $2",
                "Definition of disjunction (logical OR)");
        axiomVec.add(df_orAxiom);

        // df-an $a |- ( ( P /\ Q ) <-> -. ( P -> -. Q ) ) $.
        Axiom df_anAxiom = new Axiom("df-an", "wb wa $1 $2 wn wi $1 wn $2",
                "Definition of conjunction (logical AND)");
        axiomVec.add(df_anAxiom);

        axiomFamily[PROP_DEFS] = axiomVec;

        // ************ Predicate calculus

        axiomVec = new ArrayList<>(axiomFamily[PROP_CALC]); // Start w/ prop calc

        // ax-4 $a |- ( A. x P -> P ) $.
        tmpAxiom = new Axiom("ax-4", "wi wal $1 $2 $2",
                "Axiom of specification (predicate calculus)");
        axiomVec.add(tmpAxiom);

        // ax-5 $a |- ( A. x ( A. x P -> Q ) -> ( A. x P -> A. x Q ) ) $.
        tmpAxiom = new Axiom("ax-5",
                "wi wal $1 wi wal $1 $2 $3 wi wal $1 $2 wal $1 $3",
                "Axiom of quantified implication (predicate calculus)");
        axiomVec.add(tmpAxiom);

        // ax-6 $a |- ( -. A. x -. A. x P -> P ) $.
        tmpAxiom = new Axiom("ax-6", "wi wn $1 wal $2 wn wal $2 $1",
                "Axiom of quantified negation (predicate calculus)");
        axiomVec.add(tmpAxiom);

        // ax-7 $a |- ( A. x A. y P -> A. y A. x P ) $.
        tmpAxiom = new Axiom("ax-7",
                "wi wal $1 wal $2 $3 wal $2 wal $1 $3",
                "Axiom of quantifier commutation (predicate calculus)");
        axiomVec.add(tmpAxiom);

        // ax-g.1 $e |- P $.
        // ax-gen   $a |- A. x P $.
        tmpAxiom = new Axiom("ax-gen", "wal $2 $1",
                "Inference rule of generalization (predicate calculus)");
        tmpAxiom.addHyp("$1");
        axiomVec.add(tmpAxiom);

        // ax-8  $a |- ( x = y -> ( x = z -> y = z ) ) $.
        tmpAxiom = new Axiom("ax-8",
                "wi weq cv $1 cv $2 wi weq cv $1 cv $3 weq cv $2 cv $3",
                "Axiom of equality (predicate calculus)");
        axiomVec.add(tmpAxiom);

        // ax-9 $a |- ( A. x ( x = y -> A. x P ) -> P ) $.
        tmpAxiom = new Axiom("ax-9",
                "wi wal $1 wi weq cv $1 cv $2 wal $1 $3 $3",
                "Axiom of existence (predicate calculus)");
        axiomVec.add(tmpAxiom);

        // ax-10 $a |- ( A. x x = y -> ( A. x P -> A. y P ) ) $.
        tmpAxiom = new Axiom("ax-10",
                "wi wal $1 weq cv $1 cv $2 wi wal $1 $3 wal $2 $3",
                "Axiom of quantifier substitution (predicate calculus)");
        axiomVec.add(tmpAxiom);

        // ax-11 $a |- ( -. A. x x = y ->
        //          ( x = y -> ( P -> A. x ( x = y -> P ) ) ) ) $.
        tmpAxiom = new Axiom("ax-11",
                "wi wn wal $1 weq cv $1 cv $2 wi weq cv $1 cv $2 wi"
                        + " $3 wal $1 wi weq cv $1 cv $2 $3",
                "Axiom of variable substitution (predicate calculus)");
        axiomVec.add(tmpAxiom);

        // ax-12 $a |- ( -. A. z z = x -> ( -. A. z z = y ->
        //          ( x = y -> A. z x = y ) ) ) $.
        tmpAxiom = new Axiom("ax-12",
                "wi wn wal $1 weq cv $1 cv $2 wi wn wal $1 weq cv $1 cv $3 wi"
                        + " weq cv $2 cv $3 wal $1 weq cv $2 cv $3",
                "Axiom of quantifier introduction (predicate calculus)");
        axiomVec.add(tmpAxiom);

        // ax-13 $a |- ( x = y -> ( x e. z -> y e. z ) ) $.
        tmpAxiom = new Axiom("ax-13",
                "wi weq cv $1 cv $2 wi wel cv $1 cv $3 wel cv $2 cv $3",
                "Axiom of equality (predicate calculus)");
        axiomVec.add(tmpAxiom);

        // ax-14 $a |- ( x = y -> ( z e. x -> z e. y ) ) $.
        tmpAxiom = new Axiom("ax-14",
                "wi weq cv $1 cv $2 wi wel cv $3 cv $1 wel cv $3 cv $2",
                "Axiom of equality (predicate calculus)");
        axiomVec.add(tmpAxiom);

        // ax-15 and ax-16 are called ax-16 and ax-17 in set.mm and Metamath book

        // ax-16 $a |- ( A. x x = y -> ( P -> A. x P ) ) $.
        tmpAxiom = new Axiom("ax-15",
                "wi wal $1 weq cv $1 cv $2 wi $3 wal $1 $3",
                "Axiom of distinct variables (predicate calculus)");
        tmpAxiom.addDistinct("$1 $2");
        axiomVec.add(tmpAxiom);

        // ax-17 $a |- ( P -> A. x P ) $.
        tmpAxiom = new Axiom("ax-16", "wi $1 wal $2 $1",
                "Axiom of quantifier introduction (predicate calculus)");
        tmpAxiom.addDistinct("$1 $2");
        axiomVec.add(tmpAxiom);

        axiomFamily[PRED_CALC] = axiomVec;


        // ************ Predicate calculus with definitions

        axiomVec = new ArrayList<>(axiomFamily[PRED_CALC]); // Start w/ pred calc

        axiomVec.add(df_bi1Axiom);
        axiomVec.add(df_bi2Axiom);
        axiomVec.add(df_bi3Axiom);
        axiomVec.add(df_orAxiom);
        axiomVec.add(df_anAxiom);

        // df-ex $a |- ( E. x P <-> -. A. x -. P ) $.
        Axiom df_exAxiom = new Axiom("df-ex", "wb wex $1 $2 wn wal $1 wn $2",
                "Definition of existential quantifier");
        axiomVec.add(df_exAxiom);

        //  df-sub $a |- [ x / y ] P <->
        //             ( ( y = x -> P ) /\ E. y ( y = x /\ P ) ) ) $.
        Axiom df_subAxiom = new Axiom("df-sub",
                "wb wsb $1 $2 $3 wa wi weq cv $2 cv $1 $3 wex $2 wa weq cv $2 cv $1 $3",
                "Definition of proper substitution of x for y in P(y) to result in P(x)");
        axiomVec.add(df_subAxiom);

        axiomFamily[PRED_DEFS] = axiomVec;

        // ************ Set theory

        axiomVec = new ArrayList<>(axiomFamily[PRED_CALC]); // Start w/ pred calc

        axiomVec.add(df_bi1Axiom);
        axiomVec.add(df_bi2Axiom);
        axiomVec.add(df_bi3Axiom);
        axiomVec.add(df_orAxiom);
        axiomVec.add(df_anAxiom);
        axiomVec.add(df_exAxiom);

        // ax-ext $a |- ( A. x ( x e. y <-> x e. z ) -> y = z ) $.
        tmpAxiom = new Axiom("ax-ext",
                "wi wal $1 wb wel cv $1 cv $2 wel cv $1 cv $3 weq cv $2 cv $3",
                "Axiom of extensionality (set theory)");
        tmpAxiom.addDistinct("$1 $2");
        tmpAxiom.addDistinct("$1 $3");
        axiomVec.add(tmpAxiom);

        // ax-rep $a |- E. x ( E. y A. z ( P -> z = y ) ->
        //                         A. z ( z e. x <-> E. x ( x e. y /\ P ) ) ) $.
        tmpAxiom = new Axiom("ax-rep",
                "wex $1 wi wex $2 wal $3 wi $4 weq cv $3 cv $2 wal $3 wb wel cv $3 cv $1"
                        + " wex $1 wa wel cv $1 cv $2 $4",
                "Axiom of replacement (set theory)");
        tmpAxiom.addDistinct("$1 $2");
        tmpAxiom.addDistinct("$1 $3");
        tmpAxiom.addDistinct("$2 $3");
        tmpAxiom.addDistinct("$2 $4");
        axiomVec.add(tmpAxiom);

        // ax-un  $a |- E. x A. y ( E. x ( y e. x /\ x e. z ) -> y e. x ) $.
        tmpAxiom = new Axiom("ax-un",
                "wex $1 wal $2 wi wex $1 wa wel cv $2 cv $1 wel cv $1 cv $3 wel cv $2 cv $1"
                , "Axiom of union (set theory)");
        tmpAxiom.addDistinct("$1 $2");
        tmpAxiom.addDistinct("$1 $3");
        tmpAxiom.addDistinct("$2 $3");
        axiomVec.add(tmpAxiom);

        // ax-pow $a |- E. x A. y ( A. x ( x e. y -> x e. z ) -> y e. x ) $.
        tmpAxiom = new Axiom("ax-pow",
                "wex $1 wal $2 wi wal $1 wi wel cv $1 cv $2 wel cv $1 cv $3 wel cv $2 cv $1"
                , "Axiom of power set (set theory)");
        tmpAxiom.addDistinct("$1 $2");
        tmpAxiom.addDistinct("$1 $3");
        tmpAxiom.addDistinct("$2 $3");
        axiomVec.add(tmpAxiom);

        // ax-reg $a |- ( x e. y ->
        //                  E. x ( x e. y /\ A. z ( z e. x -> -. z e. y ) ) ) $.
        tmpAxiom = new Axiom("ax-reg",
                "wi wel cv $1 cv $2 wex $1 wa wel cv $1 cv $2 wal $3 wi wel cv $3 cv $1 wn"
                        + " wel cv $3 cv $2"
                , "Axiom of regularity (set theory)");
        tmpAxiom.addDistinct("$1 $2");
        tmpAxiom.addDistinct("$1 $3");
        tmpAxiom.addDistinct("$2 $3");
        axiomVec.add(tmpAxiom);

        // ax-inf $a |- E. x ( y e. x /\
        //              A. y ( y e. x -> E. z ( y e. z /\ z e. x ) ) ) $.
        tmpAxiom = new Axiom("ax-inf",
                "wex $1 wa wel cv $2 cv $1 wal $2 wi wel cv $2 cv $1 wex $3 wa wel cv $2 cv $3"
                        + " wel cv $3 cv $1"
                , "Axiom of infinity (set theory)");
        tmpAxiom.addDistinct("$1 $2");
        tmpAxiom.addDistinct("$1 $3");
        tmpAxiom.addDistinct("$2 $3");
        axiomVec.add(tmpAxiom);

        // ax-ac $a  |- E. x A. y A. z ( ( y e. z /\ z e. w ) -> E. w A. y ( E. w
        //       ( ( y e. z /\ z e. w ) /\ ( y e. w /\ w e. x ) ) <-> y = w ) ) $.

        tmpAxiom = new Axiom("ax-ac",
                "wex $1 wal $2 wal $3 wi wa wel cv $2 cv $3 wel cv $3 cv $4 wex $4 wal $2"
                        + " wb wex $4 wa wa wel cv $2 cv $3 wel cv $3 cv $4 wa wel cv $2 cv $4 wel"
                        + " cv $4 cv $1 weq cv $2 cv $4"
                , "Axiom of choice (set theory)");
        tmpAxiom.addDistinct("$1 $2");
        tmpAxiom.addDistinct("$1 $3");
        tmpAxiom.addDistinct("$1 $4");
        tmpAxiom.addDistinct("$2 $3");
        tmpAxiom.addDistinct("$2 $4");
        tmpAxiom.addDistinct("$3 $4");
        axiomVec.add(tmpAxiom);

        axiomFamily[SET_THEORY] = axiomVec;


        // ************ Some set theory definitions

        axiomVec = new ArrayList<>(axiomFamily[SET_THEORY]);

        axiomVec.add(df_subAxiom);

        //  df-clab $a |- ( y e. { x | P } <-> [ y / x ] P ) $.
        tmpAxiom = new Axiom("df-ab",
                "wb wel cv $2 cab $1 $3 wsb $2 $1 $3",
                "Definition of class abstraction (set theory)");
        axiomVec.add(tmpAxiom);

        // df-cleq $p |- ( A = B <-> A. x ( x e. A <-> x e. B ) ) $.
        tmpAxiom = new Axiom("df-ceq",
                "wb weq $1 $2 wal $3 wb wel cv $3 $1 wel cv $3 $2",
                "Definition of class equality (set theory)");
        tmpAxiom.addDistinct("$1 $3");
        tmpAxiom.addDistinct("$2 $3");
        axiomVec.add(tmpAxiom);

        // df-clel $a |- ( A e. B <-> E. x ( x = A /\ x e. B ) ) $.
        tmpAxiom = new Axiom("df-cel",
                "wb wel $1 $2 wex $3 wa weq cv $3 $1 wel cv $3 $2",
                "Definition of class membership (set theory)");
        tmpAxiom.addDistinct("$1 $3");
        tmpAxiom.addDistinct("$2 $3");
        axiomVec.add(tmpAxiom);

        // df-ss $a |- ( A (_ B <-> A. x ( x e. A -> x e. B ) ) $.
        tmpAxiom = new Axiom("df-ss",
                "wb wss $1 $2 wal $3 wi wel cv $3 $1 wel cv $3 $2",
                "Definition of subclass (set theory)");
        tmpAxiom.addDistinct("$1 $3");
        tmpAxiom.addDistinct("$2 $3");
        axiomVec.add(tmpAxiom);

        // df-un $p |- ( A u. B ) = { x | ( x e. A \/ x e. B ) } $.
        tmpAxiom = new Axiom("df-un",
                "weq cun $1 $2 cab $3 wo wel cv $3 $1 wel cv $3 $2",
                "Definition of union of two classes (set theory)");
        tmpAxiom.addDistinct("$1 $3");
        tmpAxiom.addDistinct("$2 $3");
        axiomVec.add(tmpAxiom);

        // df-in $p |- ( A i^i B ) = { x | ( x e. A /\ x e. B ) } $.
        tmpAxiom = new Axiom("df-in",
                "weq cin $1 $2 cab $3 wa wel cv $3 $1 wel cv $3 $2",
                "Definition of intersection of two classes (set theory)");
        tmpAxiom.addDistinct("$1 $3");
        tmpAxiom.addDistinct("$2 $3");
        axiomVec.add(tmpAxiom);

        // df-nul $p |- (/) = { x | -. x = x } $.
        tmpAxiom = new Axiom("df-nul",
                "weq cnul cab $1 wn weq cv $1 cv $1",
                "Definition of empty set (set theory)");
        axiomVec.add(tmpAxiom);

        // df-uni $a |- U. A = { x | E. y ( x e. y /\ y e. A ) } $.
        tmpAxiom = new Axiom("df-uni",
                "weq cuni $1 cab $2 wex $3 wa wel cv $2 cv $3 wel cv $3 $1",
                "Definition of union of a class (set theory)");
        tmpAxiom.addDistinct("$1 $2");
        tmpAxiom.addDistinct("$1 $3");
        axiomVec.add(tmpAxiom);

        // df-int $a |- |^| A = { x | A. y ( y e. A -> x e. y ) } $.
        tmpAxiom = new Axiom("df-int",
                "weq cint $1 cab $2 wal $3 wi wel cv $3 $1 wel cv $2 cv $3",
                "Definition of intersection of a class (set theory)");
        tmpAxiom.addDistinct("$1 $2");
        tmpAxiom.addDistinct("$1 $3");
        axiomVec.add(tmpAxiom);

        axiomFamily[SET_DEFS] = axiomVec;


        // ************ Implicational logic

        axiomVec = new ArrayList<>();

        // (P -> P)
        Axiom ax_IAxiom = new Axiom("I", "wi $1 $1",
                "Principal type-scheme for combinator I");
        axiomVec.add(ax_IAxiom);

        // ((P -> Q) -> ((R -> P) -> (R -> Q)))
        tmpAxiom = new Axiom("B", "wi wi $1 $2 wi wi $3 $1 wi $3 $2",
                "Principal type-scheme for combinator B");
        axiomVec.add(tmpAxiom);

        // ((P -> Q) -> ((Q -> R) -> (P -> R)))
        tmpAxiom = new Axiom("B'", "wi wi $1 $2 wi wi $2 $3 wi $1 $3",
            "Principal type-scheme for combinator B'");
        axiomVec.add(tmpAxiom);

        // ((P -> (Q -> R)) -> (Q -> (P -> R)))
        tmpAxiom = new Axiom("C", "wi wi $1 wi $2 $3 wi $2 wi $1 $3",
                "Principal type-scheme for combinator C");
        axiomVec.add(tmpAxiom);

        // ax-1 $a |- ( P -> ( Q -> P ) ) $.
        tmpAxiom = new Axiom("K", "wi $1 wi $2 $1",
                "Principal type-scheme for combinator K");
        axiomVec.add(tmpAxiom);

        // ((P -> (P -> Q)) -> (P -> Q))
        tmpAxiom = new Axiom("W", "wi wi $1 wi $1 $2 wi $1 $2",
                "Principal type-scheme for combinator W");
        axiomVec.add(tmpAxiom);

        // ax-2 $a |- ( ( P -> ( Q -> R ) ) -> ( ( P -> Q ) ->
        //   ( P -> R ) ) ) $.
        tmpAxiom = new Axiom("S",
                "wi wi $1 wi $2 $3 wi wi $1 $2 wi $1 $3",
                "Principal type-scheme for combinator S");
        axiomVec.add(tmpAxiom);

        // (((P -> Q) -> P) -> P)
        tmpAxiom = new Axiom("Peirce", "wi wi wi $1 $2 $1 $1",
                "Peirce's law");
        axiomVec.add(tmpAxiom);

        // _|_ -> P
        tmpAxiom = new Axiom("ax-f", "wi wfalse $1",
                "Axiom for false constant");
        axiomVec.add(tmpAxiom);

        // maj   $e |- ( P -> Q ) $.
        // min   $e |- P $.
        // ax-mp $a |- Q $.
        Axiom rule_DAxiom = new Axiom("D", "$2",
                "Condensed detachment (modus ponens)");
        rule_DAxiom.addHyp("$1");
        rule_DAxiom.addHyp("wi $1 $2");
        axiomVec.add(rule_DAxiom);

        axiomFamily[IMPL_LOGIC] = axiomVec;


        // ************ Intuitionistic propositional calculus
        // Source:  T. Thatcher Robinson, JSL Vol. 33 No. 2 265-270 (1968)

        axiomVec = new ArrayList<>();

        axiomVec.add(ax_1Axiom);
        axiomVec.add(ax_2Axiom);

        // P -> (P \/ Q)
        tmpAxiom = new Axiom("ax-I3", "wi $1 wo $1 $2",
                "Axiom 3 for intuitionist propositional calculus");
        axiomVec.add(tmpAxiom);

        // Q -> (P \/ Q)
        tmpAxiom = new Axiom("ax-I4", "wi $2 wo $1 $2",
                "Axiom 4 for intuitionist propositional calculus");
        axiomVec.add(tmpAxiom);

        // ((P -> R) -> ((Q -> R) -> ((P \/ Q) -> R)))
        tmpAxiom = new Axiom("ax-I5", "wi wi $1 $3 wi wi $2 $3 wi wo $1 $2 $3",
                "Axiom 5 for intuitionist propositional calculus");
        axiomVec.add(tmpAxiom);

        // (P /\ Q) -> P
        tmpAxiom = new Axiom("ax-I6", "wi wa $1 $2 $1",
                "Axiom 6 for intuitionist propositional calculus");
        axiomVec.add(tmpAxiom);

        // (P /\ Q) -> Q
        tmpAxiom = new Axiom("ax-I7", "wi wa $1 $2 $2",
                "Axiom 7 for intuitionist propositional calculus");
        axiomVec.add(tmpAxiom);

        // P -> (Q -> (P /\ Q))
        tmpAxiom = new Axiom("ax-I8", "wi $1 wi $2 wa $1 $2",
                "Axiom 8 for intuitionist propositional calculus");
        axiomVec.add(tmpAxiom);

        // (P -> ~Q) -> (Q -> ~P)
        tmpAxiom = new Axiom("ax-I9", "wi wi $1 wn $2 wi $2 wn $1",
                "Axiom 9 for intuitionist propositional calculus");
        axiomVec.add(tmpAxiom);

        // ~P -> (P -> Q)
        tmpAxiom = new Axiom("ax-I10", "wi wn $1 wi $1 $2",
                "Axiom 10 for intuitionist propositional calculus");
        axiomVec.add(tmpAxiom);

        axiomVec.add(rule_mpAxiom);

        axiomFamily[INTUIT_LOGIC] = axiomVec;


        // ************ Modal logic
        // Source:  K. Goedel

        axiomVec = new ArrayList<>(axiomFamily[PROP_CALC]);

        // []P -> P
        tmpAxiom = new Axiom("ax-m1", "wi wnec $1 $1",
                "Axiom 1 for modal logic system M");
        axiomVec.add(tmpAxiom);

        // [](P -> Q) -> ([]P -> []Q)
        tmpAxiom = new Axiom("ax-m2", "wi wnec wi $1 $2 wi wnec $1 wnec $2",
                "Axiom 2 for modal logic system M");
        axiomVec.add(tmpAxiom);

        // []P -> [][]P
        tmpAxiom = new Axiom("ax-S4", "wi wnec $1 wnec wnec $1",
                "Axiom extending modal logic to system S4");
        axiomVec.add(tmpAxiom);

        // $e |- P $.
        // $a |- [] P $.
        Axiom rule_necAxiom = new Axiom("ax-nec", "wnec $1",
                "Rule of necessitation (modal logic)");
        rule_necAxiom.addHyp("$1");
        axiomVec.add(rule_necAxiom);

        axiomVec.add(df_bi1Axiom);
        axiomVec.add(df_bi2Axiom);
        axiomVec.add(df_bi3Axiom);

        // <> P <-> -. [] -. P
        Axiom df_possAxiom = new Axiom("df-poss", "wb wposs $1 wn wnec wn $1",
                "Definition of possibility");
        axiomVec.add(df_possAxiom);

        // <>P -> []<>P
        tmpAxiom = new Axiom("ax-S5", "wi wposs $1 wnec wposs $1",
                "Axiom extending modal logic to system S5");
        axiomVec.add(tmpAxiom);

        // []P -> <>P
        tmpAxiom = new Axiom("ax-m1'", "wi wnec $1 wposs $1",
                "Weaker alternate axiom 1 for modal logic");
        axiomVec.add(tmpAxiom);

        // _|_ <-> -. ( P -> P )
        Axiom df_falseAxiom = new Axiom("df-false", "wb wfalse wn wi $1 $1",
                "Definition of logical false constant");
        axiomVec.add(df_falseAxiom);

        axiomVec.add(df_orAxiom);
        axiomVec.add(df_anAxiom);

        axiomFamily[MODAL_LOGIC] = axiomVec;


        // ************ Modal provability logic
        // Source:  G. Boolos & R. Jeffrey, "Computability and Logic", 1989, ch. 27

        axiomVec = new ArrayList<>(axiomFamily[PROP_CALC]);

        // [](P -> Q) -> ([]P -> []Q)
        tmpAxiom = new Axiom("ax-g1", "wi wnec wi $1 $2 wi wnec $1 wnec $2",
                "Axiom 1 for modal provability logic system G");
        axiomVec.add(tmpAxiom);

        // []P -> [][]P
        tmpAxiom = new Axiom("ax-g2", "wi wnec $1 wnec wnec $1",
                "Axiom 2 for modal provability logic system G");
        axiomVec.add(tmpAxiom);

        // []([]P -> P) -> []P
        tmpAxiom = new Axiom("ax-g3", "wi wnec wi wnec $1 $1 wnec $1",
                "Axiom 3 for modal provability logic system G");
        axiomVec.add(tmpAxiom);

        axiomVec.add(rule_necAxiom);

        axiomVec.add(df_bi1Axiom);
        axiomVec.add(df_bi2Axiom);
        axiomVec.add(df_bi3Axiom);
        axiomVec.add(df_possAxiom);
        axiomVec.add(df_falseAxiom);
        axiomVec.add(df_orAxiom);
        axiomVec.add(df_anAxiom);

        axiomFamily[GODEL_LOGIC] = axiomVec;


        // ************ Quantum logic
        // Source: M. Pavicic, Int. J. of Theoretical Physics 32, 1993, p. 1490.

        axiomVec = new ArrayList<>();

        // A1.  |-  A <-> - - A
        tmpAxiom = new Axiom("A1", "wb $1 wn wn $1",
                "Axiom A1 for unified quantum logic AUQL");
        axiomVec.add(tmpAxiom);

        // A2.  |-  A \/ B <-> B \/ A
        tmpAxiom = new Axiom("A2", "wb wo $1 $2 wo $2 $1",
                "Axiom A2 for unified quantum logic AUQL");
        axiomVec.add(tmpAxiom);

        // A3.  |-  ( A \/ B) \/ C  <-> A \/ ( B \/ C )
        tmpAxiom = new Axiom("A3", "wb wo wo $1 $2 $3 wo $1 wo $2 $3",
                "Axiom A3 for unified quantum logic AUQL");
        axiomVec.add(tmpAxiom);

        // A4.  |-  A \/ ( B \/ - B ) <-> B \/ - B
        tmpAxiom = new Axiom("A4", "wb wo $1 wo $2 wn $2 wo $2 wn $2",
                "Axiom A4 for unified quantum logic AUQL");
        axiomVec.add(tmpAxiom);

        // A5.  |-  A \/ - ( - A \/ - B ) <-> A
        tmpAxiom = new Axiom("A5", "wb wo $1 wn wo wn $1 wn $2 $1",
                "Axiom A5 for unified quantum logic AUQL");
        axiomVec.add(tmpAxiom);

        // A6.  |-  ( A <-> B ) <-> - ( - - A \/ - - B ) \/ - ( - A \/ - B )
        tmpAxiom = new Axiom("A6",
                "wb wb $1 $2 wo wn wo wn wn $1 wn wn $2 wn wo wn $1 wn $2",
                "Axiom A6 for unified quantum logic AUQL");
        axiomVec.add(tmpAxiom);

        // R1.  |-  A <-> B    <=>    |-  B <-> A
        tmpAxiom = new Axiom("R1", "wb $2 $1",
                "Rule R1 for unified quantum logic AUQL");
        tmpAxiom.addHyp("wb $1 $2");
        axiomVec.add(tmpAxiom);

        // R2.  |-  A <-> B    &    |-  B <-> C    =>    |-  A <-> C
        tmpAxiom = new Axiom("R2", "wb $1 $3",
                "Rule R2 for unified quantum logic AUQL");
        tmpAxiom.addHyp("wb $2 $3");
        tmpAxiom.addHyp("wb $1 $2");
        axiomVec.add(tmpAxiom);

        // R3.  |-  ( C \/ - C ) <-> ( A <-> B )     =>     |-   A <-> B
        tmpAxiom = new Axiom("R3", "wb $1 $2",
                "Rule R3 for unified quantum logic AUQL");
        tmpAxiom.addHyp("wb wo $3 wn $3 wb $1 $2");
        axiomVec.add(tmpAxiom);

        // R4.  |-  A <-> B   =>    |-  - A <-> - B
        tmpAxiom = new Axiom("R4", "wb wn $1 wn $2",
                "Rule R4 for unified quantum logic AUQL");
        tmpAxiom.addHyp("wb $1 $2");
        axiomVec.add(tmpAxiom);

        // R5.  |-  A <-> B   =>    |-  A \/ C <-> B \/ C
        tmpAxiom = new Axiom("R5", "wb wo $1 $3 wo $2 $3",
                "Rule R5 for unified quantum logic AUQL");
        tmpAxiom.addHyp("wb $1 $2");
        axiomVec.add(tmpAxiom);

        // D1.  |-  ( A /\ B ) <-> - ( - A \/ - B )
        tmpAxiom = new Axiom("D1",
                "wb wa $1 $2 wn wo wn $1 wn $2",
                "Definition of conjunction for unified quantum logic AUQL");
        axiomVec.add(tmpAxiom);

        axiomFamily[QUANTUM_LOGIC] = axiomVec;


        // ************ Euclidean geometry
        // Source:  Alfred Tarski, "What is Elementary Geometry?", in _The Axiomatic
        // Method_ (ed. Leon Henkin, et. al.), North-Holland, 1959, pp. 16-29

        // Axioms are also discussed in:
        // http://www.math.ucla.edu/~asl/bsl/0502/0502-002.ps

        axiomVec = new ArrayList<>(axiomFamily[PRED_DEFS]); // Start w/ pred calc

        // Remove equality axioms for membership connective
        axiomVec.remove(axiomFamily[PRED_CALC].size() - 3);
        axiomVec.remove(axiomFamily[PRED_CALC].size() - 4);

        // Note that the 'cv' connective is unnecessary for geometry (since there
        // are no classes) but we will keep it with the equality connective to
        // allow us to reuse the axioms of predicate calculus from set theory
        // without modification.

        // ax-eq1 $a |- ( x = w -> ( Bxyz -> Bwyz ) ) $.
        tmpAxiom = new Axiom("eueq1",
                "wi weq cv $1 cv $4 wi wbt $1 $2 $3 wbt $4 $2 $3",
                "Axiom of equality for betweenness predicate (Euclidean geometry)");
        axiomVec.add(tmpAxiom);

        // ax-eq2 $a |- ( y = w -> ( Bxyz -> Bxwz ) ) $.
        tmpAxiom = new Axiom("eueq2",
                "wi weq cv $2 cv $4 wi wbt $1 $2 $3 wbt $1 $4 $3",
                "Axiom of equality for betweenness predicate (Euclidean geometry)");
        axiomVec.add(tmpAxiom);

        // ax-eq3 $a |- ( z = w -> ( Bxyz -> Bxyw ) ) $.
        tmpAxiom = new Axiom("eueq3",
                "wi weq cv $3 cv $4 wi wbt $1 $2 $3 wbt $1 $2 $4",
                "Axiom of equality for betweenness predicate (Euclidean geometry)");
        axiomVec.add(tmpAxiom);

        // ax-eq4 $a |- ( x = v -> ( Dxyzw -> Dvyzw ) ) $.
        tmpAxiom = new Axiom("eueq4",
                "wi weq cv $1 cv $5 wi wd $1 $2 $3 $4 wd $5 $2 $3 $4",
                "Axiom of equality for distance predicate (Euclidean geometry)");
        axiomVec.add(tmpAxiom);

        // ax-eq5 $a |- ( y = v -> ( Dxyzw -> Dxvzw ) ) $.
        tmpAxiom = new Axiom("eueq5",
                "wi weq cv $2 cv $5 wi wd $1 $2 $3 $4 wd $1 $5 $3 $4",
                "Axiom of equality for distance predicate (Euclidean geometry)");
        axiomVec.add(tmpAxiom);

        // ax-eq6 $a |- ( z = v -> ( Dxyzw -> Dxyvw ) ) $.
        tmpAxiom = new Axiom("eueq6",
                "wi weq cv $3 cv $5 wi wd $1 $2 $3 $4 wd $1 $2 $5 $4",
                "Axiom of equality for distance predicate (Euclidean geometry)");
        axiomVec.add(tmpAxiom);

        // ax-eq7 $a |- ( w = v -> ( Dxyzw -> Dxyzv ) ) $.
        tmpAxiom = new Axiom("eueq7",
                "wi weq cv $4 cv $5 wi wd $1 $2 $3 $4 wd $1 $2 $3 $5",
                "Axiom of equality for distance predicate (Euclidean geometry)");
        axiomVec.add(tmpAxiom);

        // Bxyx -> x=y
        tmpAxiom = new Axiom("eu1",
                "wi wbt $1 $2 $1 weq cv $1 cv $2",
                "Identity axiom for betweenness (Euclidean geometry)");
        axiomVec.add(tmpAxiom);

        // Bxyu /\ Byzu -> Bxyz
        tmpAxiom = new Axiom("eu2",
                "wi wa wbt $1 $2 $4 wbt $2 $3 $4 wbt $1 $2 $3",
                "Transitivity axiom for betweenness (Euclidean geometry)");
        axiomVec.add(tmpAxiom);

        // (Bxyz /\ Bxyu) /\ -. x=y -> Bxzu \/ Bxuz
        tmpAxiom = new Axiom("eu3",
                "wi wa wa wbt $1 $2 $3 wbt $1 $2 $4 wn weq cv $1 cv $2"
                        + " wo wbt $1 $3 $4 wbt $1 $4 $3",
                "Connectivity axiom for betweenness (Euclidean geometry)");
        axiomVec.add(tmpAxiom);

        // Dxyyx
        tmpAxiom = new Axiom("eu4",
                "wd $1 $2 $2 $1",
                "Reflexivity axiom for equidistance (Euclidean geometry)");
        axiomVec.add(tmpAxiom);

        // Dxyzz -> x=y
        tmpAxiom = new Axiom("eu5",
                "wi wd $1 $2 $3 $3 weq cv $1 cv $2",
                "Identity axiom for equidistance (Euclidean geometry)");
        axiomVec.add(tmpAxiom);

        // Dxyzu /\ Dxyvw -> Dzuvw
        tmpAxiom = new Axiom("eu6",
                "wi wa wd $1 $2 $3 $4 wd $1 $2 $5 $6 wd $3 $4 $5 $6",
                "Transitivity axiom for equidistance (Euclidean geometry)");
        axiomVec.add(tmpAxiom);

        // E. v (Bxtu /\ Byuz -> Bxvy /\ Bztv)
        tmpAxiom = new Axiom("eu7",
                "wex $6 wi wa wbt $1 $2 $3 wbt $4 $3 $5 wa wbt $1 $6 $4 wbt $5 $2 $6",
            "Pasch's axiom (Euclidean geometry)");
        tmpAxiom.addDistinct("$6 $1");
        tmpAxiom.addDistinct("$6 $2");
        tmpAxiom.addDistinct("$6 $3");
        tmpAxiom.addDistinct("$6 $4");
        tmpAxiom.addDistinct("$6 $5");
        axiomVec.add(tmpAxiom);

        // 3-Aug-03 - fixed a typo in this equation
        // E. v E. w ((Bxut /\ Byuz) /\ -. x=u -> (Bxzv /\ Bxyw) /\ Bvtw
        // E. 1 E. 2 ((B345 /\ B647) /\ -. 3=4 -> (B371 /\ B362) /\ B152
        tmpAxiom = new Axiom("eu8",
                "wex $1 wex $2 wi wa wa wbt $3 $4 $5 wbt $6 $4 $7 wn weq cv $3 cv $4"
                        + " wa wa wbt $3 $7 $1 wbt $3 $6 $2 wbt $1 $5 $2",
            "Euclid's axiom (Euclidean geometry)");
        tmpAxiom.addDistinct("$1 $2");
        tmpAxiom.addDistinct("$1 $3");
        tmpAxiom.addDistinct("$1 $4");
        tmpAxiom.addDistinct("$1 $5");
        tmpAxiom.addDistinct("$1 $6");
        tmpAxiom.addDistinct("$1 $7");
        tmpAxiom.addDistinct("$2 $3");
        tmpAxiom.addDistinct("$2 $4");
        tmpAxiom.addDistinct("$2 $5");
        tmpAxiom.addDistinct("$2 $6");
        tmpAxiom.addDistinct("$2 $7");
        axiomVec.add(tmpAxiom);

        // DxyXY /\ DyzYZ /\ DxuXU /\ DyuYU /\ Bxyz /\ BXYZ /\ -.x=y -> DzuZU
        // D1234 /\ D2546 /\ D1738 /\ D2748 /\ B125 /\ B346 /\ -.1=2 -> D5768
        tmpAxiom = new Axiom("eu9",
                "wi wa wa wa wa wa wa wd $1 $2 $3 $4 wd $2 $5 $4 $6 wd $1 $7 $3 $8"
                        + " wd $2 $7 $4 $8 wbt $1 $2 $5 wbt $3 $4 $6 wn weq cv $1 cv $2"
                        + " wd $5 $7 $6 $8",
                "Five-segment axiom (Euclidean geometry)");
        axiomVec.add(tmpAxiom);

        // E. z (Bxyz /\ Dyzuv)
        // E. 1 (B231 /\ D3145)
        tmpAxiom = new Axiom("eu10",
                "wex $1 wa wbt $2 $3 $1 wd $3 $1 $4 $5",
                "Axiom of segment construction (Euclidean geometry)");
        tmpAxiom.addDistinct("$1 $2");
        tmpAxiom.addDistinct("$1 $3");
        tmpAxiom.addDistinct("$1 $4");
        tmpAxiom.addDistinct("$1 $5");
        axiomVec.add(tmpAxiom);

        // E. x E. y E. z (-. Bxyz /\ -. Byzx /\ -. Bzxy)
        tmpAxiom = new Axiom("eu11",
                "wex $1 wex $2 wex $3 wa wa wn wbt $1 $2 $3 wn wbt $2 $3 $1 wn wbt"
                        + " $3 $1 $2",
                "Lower dimension axiom (Euclidean geometry)");
        tmpAxiom.addDistinct("$1 $2");
        tmpAxiom.addDistinct("$1 $3");
        tmpAxiom.addDistinct("$2 $3");
        axiomVec.add(tmpAxiom);

        // Dxuxv /\ Dyuyv /\ Dzuzv /\ -.u=v -> Bxyz \/ Byzx \/ Bzxy
        // D1213 /\ D4243 /\ D5253 /\ -.2=3 -> B145 \/ B451 \/ B514
        tmpAxiom = new Axiom("eu12",
                "wi wa wa wa wd $1 $2 $1 $3 wd $4 $2 $4 $3 wd $5 $2 $5 $3"
                        + " wn weq cv $2 cv $3 wo wo wbt $1 $4 $5 wbt $4 $5 $1 wbt $5 $1 $4",
                "Upper dimension axiom (Euclidean geometry)");
        axiomVec.add(tmpAxiom);

        // E.zA.xA.y (P /\ Q -> Bzxy) -> E.zA.xA.y (P /\ Q -> Bxzy)
        tmpAxiom = new Axiom("eu13",
                "wi wex $3 wal $1 wal $2 wi wa $4 $5 wbt $3 $1 $2"
                        + " wex $3 wal $1 wal $2 wi wa $4 $5 wbt $1 $3 $2",
                "Elementary continuity axiom scheme (Euclidean geometry)");
        tmpAxiom.addDistinct("$1 $2");
        tmpAxiom.addDistinct("$1 $3");
        tmpAxiom.addDistinct("$2 $3");
        tmpAxiom.addDistinct("$1 $4");
        tmpAxiom.addDistinct("$3 $4");
        tmpAxiom.addDistinct("$2 $5");
        tmpAxiom.addDistinct("$3 $5");
        axiomVec.add(tmpAxiom);

        axiomFamily[EUCLID] = axiomVec;

        // ************ Weak D-complete logic
        // Source:  N. Megill & M. Bunder, "Weaker D-Complete Logics",
        //     J. of the IGPL, Vol. 4 No. 2, pp. 215-225 (1996)

        axiomVec = new ArrayList<>();

        axiomVec.add(ax_IAxiom);

        // ((a->a)->((c->(b->b))->(c->((a->b)->(a->b)))))
        tmpAxiom = new Axiom("A1w",
                "wi wi $1 $1 wi wi $3 wi $2 $2 wi $3 wi wi $1 $2 wi $1 $2",
                "Axiom A1 for weak D-complete logic");
        axiomVec.add(tmpAxiom);

        // ((b->b)->((c->(a->a))->(c->((a->b)->(a->b)))))
        tmpAxiom = new Axiom("A2w",
                "wi wi $2 $2 wi wi $3 wi $1 $1 wi $3 wi wi $1 $2 wi $1 $2",
                "Axiom A2 for weak D-complete logic");
        axiomVec.add(tmpAxiom);

        // ((c->(a->a))->((c->(b->b))->(c->((a->b)->(a->b)))))
        tmpAxiom = new Axiom("A3w",
                "wi wi $3 wi $1 $1 wi wi $3 wi $2 $2 wi $3 wi wi $1 $2 wi $1 $2",
                "Axiom A3 for weak D-complete logic");
        axiomVec.add(tmpAxiom);

        axiomVec.add(rule_DAxiom);

        axiomFamily[WEAKD_LOGIC] = axiomVec;

        // ***************** END OF AXIOMS *************************************

        // Determine the largest number of hypotheses
        maxAxiomHypotheses = 0;
        for (ArrayList<Axiom> axioms : axiomFamily) {
            for (Axiom axiom : axioms) {
                if (axiom.axiomHypothesisVec.size()
                        > maxAxiomHypotheses) {
                    maxAxiomHypotheses
                            = axiom.axiomHypothesisVec.size();
                }
            }
        }

        // Initialize to prop calc
        axiomArr = buildAxiomArr(currentFamily);

        // Set the background color
        this.setBackground(BACKGROUND_COLOR);

        proof_exit_button = new Button("Hide Proof Information");
        proof_exit_button.setActionCommand("proof_exit_button");
        proof_exit_button.addActionListener(this);

        info_exit_button = new Button("Exit Axiom Information");
        info_exit_button.setActionCommand("info_exit_button");
        info_exit_button.addActionListener(this);

        // Create option selection menu
        buildOptionChoices();

        // Create axiom selection menu
        buildAxiomChoices();

    } // init

    public void actionPerformed(ActionEvent e) {

        if ("proof_exit_button".equals(e.getActionCommand())) {

            this.remove(proof_text);
            this.remove(proof_exit_button);
            proofInfoModeFlag = false;

            /* [sound] */
            audioName = "wzzz"; // Sound effect
            /* [sound] */ // Play sound if any
            /* [sound] */
            playAudio();

            // Rebuild options
            buildOptionChoices();
            // Rebuild axiom selection menu and redisplay choice
            buildAxiomChoices();
            // Repaint screen
            paint(this.getGraphics());
            // return true;   (actionPerformed returns void)

        } else if ("info_exit_button".equals(e.getActionCommand())) {

            this.setBackground(BACKGROUND_COLOR);
            this.remove(info_exit_button);
            axiomInfoModeFlag = false;

            /* [sound] */
            audioName = "wzzz"; // Sound effect
            /* [sound] */ // Play sound if any
            /* [sound] */
            playAudio();

            // Rebuild options
            buildOptionChoices();
            // Rebuild axiom selection menu and redisplay choice
            buildAxiomChoices();
            // Repaint screen
            paint(this.getGraphics());
            // return true;   (actionPerformed returns void)

        }

    }

    public void itemStateChanged(ItemEvent e) {

        if (e.getItemSelectable() == option_choices) {

            boolean rebuildFlag = false;

            if (e.getItem() == "Undo") {

                redoStack.push(currentState.makeClone());
                currentState = undoStack.pop();
                userTheorems = currentState.userThVec;
                currentFamily = currentState.currentFam;
                axiomArr = buildAxiomArr(currentFamily);
                /* [sound] */
                audioName = "zoom"; // Sound effect
                rebuildFlag = true;

            } else if (e.getItem() == "Redo") {
                undoStack.push(currentState.makeClone());
                currentState = redoStack.pop();
                /* [sound] */
                audioName = "whoosh"; // Sound effect
                rebuildFlag = true;
            } else if (e.getItem() == "Rotate Stack" || e.getItem() == "Swap Stack Top") {
                /* [sound] */
                audioName = "gate"; // Sound effect
                undoStack.push(currentState.makeClone());
                redoStack = new Stack<>();
                int iEnd = currentState.assertionVec.size() - 1;
                int iStart;
                iStart = e.getItem() == "Rotate Stack" ? 0 : iEnd - 1;
                String tmpStr = currentState.assertionVec.get(iStart);
                String tmpPStr = currentState.proofVec.get(iStart);
                for (int j = iStart; j < iEnd; j++) {
                    currentState.assertionVec.set(j,
                            currentState.assertionVec.get(j + 1));
                    currentState.proofVec.set(j,
                            currentState.proofVec.get(j + 1));
                }
                currentState.assertionVec.set(iEnd, tmpStr);
                currentState.proofVec.set(iEnd, tmpPStr);
                rebuildFlag = true;
            } else if (e.getItem() == "Delete Stack Top") {
                /* [sound] */
                audioName = "ouch"; // Sound effect
                undoStack.push(currentState.makeClone());
                redoStack = new Stack<>();
                currentState.assertionVec.remove(
                        currentState.assertionVec.size() - 1);
                currentState.proofVec.remove(
                        currentState.proofVec.size() - 1);
                if (currentState.assertionVec.size() != // Should never be different!
                        currentState.proofVec.size()) System.out.println("Bug #2");
                // normalize to trim any distinct variable pairs that become unused
                currentState.normalize();
                rebuildFlag = true;
            } else if (e.getItem() == "Erase Stack") {
                /* [sound] */
                audioName = "splat"; // Sound effect
                undoStack.push(currentState.makeClone());
                redoStack = new Stack<>();
                currentState = new State();
                rebuildFlag = true;
            } else if (e.getItem() == "Erase All") {
                /* [sound] */
                audioName = "bomb"; // Sound effect
                //init(); // rebuild is done in init()
                undoStack.push(currentState.makeClone());
                redoStack = new Stack<>();
                userTheorems = new ArrayList<>();
                currentState = new State();
                currentFamily = PROP_CALC;
                axiomArr = buildAxiomArr(currentFamily);
                rebuildFlag = true;
                //paint(this.getGraphics());
            } else if (e.getItem() == "Proof Information") {
                /* [sound] */
                audioName = "wzzz2"; // Sound effect
                proofInfoModeFlag = true;
                rebuildFlag = true; // Need to rebuild option choice & display proof
            } else if (e.getItem() == "Axiom Information") {
                /* [sound] */
                audioName = "wzzz2"; // Sound effect
                axiomInfoModeFlag = true;
                infoModeAxiomToShow = -1; // means no axiom selected by user yet
                rebuildFlag = true; // We will re-paint in this special mode
                this.setBackground(INFO_BACKGROUND_COLOR);
            } else if (e.getItem() == "Select Logic Family") {
                /* [sound] */
                audioName = "gate"; // Sound effect
                this.removeAll();
                CheckboxGroup cg = new CheckboxGroup();
                for (int i = 0; i < FAMILIES; i++) {
                    boolean enable = i == currentFamily;
                    logic_select[i] = new Checkbox(familyName[i], cg, enable);
                    this.add(logic_select[i]);
                    logic_select[i].addItemListener(this);
                }
                selectLogicModeFlag = true;

                /* [sound] */ // Play sound if any
                /* [sound] */
                playAudio();

                paint(this.getGraphics());
                // return true;   (itemStateChanged returns void)
            } else if (e.getItem() == "Add Hypothesis") {
                /* [sound] */
                audioName = "clink"; // Sound effect
                undoStack.push(currentState.makeClone());
                redoStack = new Stack<>();
                currentState.addHyp();
                rebuildFlag = true;
            } else if (e.getItem() == "Save as Axiom") {
                /* [sound] */
                audioName = "beep_spring"; // Sound effect
                undoStack.push(currentState.makeClone());
                redoStack = new Stack<>();
                // Save theorem at stack top as a new axiom - must rebuild array
                // with greater bound
                userTheorems.add(new Axiom(currentState));
                currentState.userThVec = userTheorems;
                axiomArr = buildAxiomArr(currentFamily);
                // Update the largest number of hypotheses
                if (axiomArr[0].axiomHypothesisVec.size() > maxAxiomHypotheses) {
                    maxAxiomHypotheses = axiomArr[0].axiomHypothesisVec.size();
                }
                // Re-display screen so user's new axiom will become an axiom pick
                rebuildFlag = true;
            } else if (e.getItem() == "Refresh Screen") {
                rebuildFlag = true;
                /* [sound] */
            } else if (e.getItem() == "Turn Sound On") {
                /* [sound] */
                enableAudioFlag = true;
                /* [sound] */
                audioName = "whoosh";
                /* [sound] */
                rebuildFlag = true;
                /* [sound] */
            } else if (e.getItem() == "Turn Sound Off") {
                /* [sound] */
                enableAudioFlag = false;
                /* [sound] */
                rebuildFlag = true;
            }

            /* [sound] */ // Play sound if any
            /* [sound] */
            playAudio();

            if (rebuildFlag) {

                // Rebuild options
                buildOptionChoices();

                // Rebuild axiom selection menu and redisplay choice
                buildAxiomChoices();

                // If in display proof mode, put proof after choice
                if (proofInfoModeFlag) {
                    proof_text = new TextArea(
                            "The top stack entry is:\n\n",
                            20, 65);
                    // Future: in Java 1.1 add: , SCROLLBARS_VERTICAL_ONLY);
                    proof_text.append("    " + PrimFormula.getDisplay(
                            currentState.assertionVec.get(currentState.assertionVec.size() - 1), false));
                    proof_text.append(
                            "\n\nTo reconstruct the"
                                    + " top stack entry, enter axioms in this order:\n\n");
                    proof_text.append("    " +
                            currentState.proofVec.get(currentState.proofVec.size() - 1));
                    proof_text.setBackground(PROOF_BACKGROUND_COLOR);

                    // Display fleshed-out proof detail
                    proof_text.append("\n\nDetailed proof:\n\n");
                    State proofInfoState = State.buildProofInfoState(currentState);
                    for (int i = 0; i < proofInfoState.assertionVec.size(); i++) {
                        proof_text.append(" " +
                                proofInfoState.proofVec.get(i) + "    "
                                + PrimFormula.getDisplay(
                                proofInfoState.assertionVec.get(i), false)
                                + "\n");
                    }
                    if (!proofInfoState.distinctVarVec.isEmpty()) {
                        proof_text.append("\nDistinct variable pairs for this proof:\n\n");
                        for (int i = 0; i < proofInfoState.distinctVarVec.size(); i++) {
                            proof_text.append("   ");
                            for (int j = 0; j < 2; j++) {
                                short v = (short) (
                                        proofInfoState.distinctVarVec.get(i).charAt(j));
                                // We assume type is already assigned, so 0 is OK
                                proof_text.append(VariableName.name(v, (short) 0) + " ");
                            }
                        }
                    }

                    this.add(proof_text);
                }

                // Repaint screen
                paint(this.getGraphics());
            }

        } else if (e.getItemSelectable() == axiom_choices) {

            // Lookup what the choice corresponds to
            int choice = axiomChoiceVec.get(
                    axiom_choices.getSelectedIndex()
          /* 11-Nov-2015 nm - Subtract 1 to account for dummy menu item
             called "Select axiom:" at top of list. */
                            - 1
            );

            /* [sound] */ // Sound effects
            /* [sound] */
            audioName = "drip";
            /* [sound] */
            if (choice > 0) {
                /* [sound] */
                /* [sound] */
                switch (axiomArr[choice].label) {
                    case "ax-inf" -> audioName = "hypspc";
                    case "ax-ac" -> audioName = "ni";
                }
                /* [sound] */
            }
            /* [sound] */ // End sound effects

            if (axiomInfoModeFlag) {
                infoModeAxiomToShow = choice;
            } else {
                if (choice < 0) {
                    // Push undo stack
                    undoStack.push(currentState.makeClone());
                    redoStack = new Stack<>();
                    // It's a hypothesis; add it to the assertion stack
                    currentState.pushAssertion(
                            currentState.hypothesisVec.get(-choice - 1),
                            // The proof is one step, just the hypothesis
                            "$hyp" + -choice);
                } else {

                    /* [sound] */ // Sound effects
                    /* [sound] */
                    int rAudio = axiomArr[choice].axiomHypothesisVec.size();
                    /* [sound] */
                    int aAudio = currentState.assertionVec.size();
                    /* [sound] */
                    if (rAudio == aAudio && rAudio > 1) audioName = "bart.aye_carumba";
                        /* [sound] */
                    else if (rAudio == 1) audioName = "bubble1"; // ax-gen
                        /* [sound] */
                    else if (rAudio > 0) audioName = "boing"; // ax-mp
                    /* [sound] */ // End sound effects

                    // Push undo stack - no need to clone since unify does this
                    undoStack.push(currentState);
                    redoStack = new Stack<>();
                    // It's a axiom - it will always unify since that was determined
                    // when choice list was built
                    currentState = Unification.unify(axiomArr[choice], currentState,
                            false);
                    // Squish down variables and trim unused distinct var pairs
                    currentState.normalize();
                }

                // Rebuild option selection
                buildOptionChoices();

                // Rebuild axiom selection menu and redisplay choice
                buildAxiomChoices();

            } // not info mode

            // (If it's info mode, menus will not change until we exit it; no need
            // to rebuild)

            /* [sound] */ // Play sound if any
            /* [sound] */
            playAudio();

            // Repaint screen
            paint(this.getGraphics());
            // return true;   (itemStateChanged returns void)

            // end if (event.target == axiom_choices)

        } else {
            for (int i = 0; i < FAMILIES; i++) {

                if (e.getItemSelectable() == logic_select[i]) {

                    selectLogicModeFlag = false;
                    if (currentFamily != i) {
                        // User selected a new logic family
                        /* [sound] */
                        audioName = "clink"; // Sound effect
                        undoStack.push(currentState.makeClone());
                        redoStack = new Stack<>();
                        currentFamily = i;
                        axiomArr = buildAxiomArr(currentFamily);
                        currentState = new State();
                    }

                    /* [sound] */ // Play sound if any
                    /* [sound] */
                    playAudio();

                    // Rebuild option selection
                    buildOptionChoices();

                    // Rebuild axiom selection menu and redisplay choice
                    buildAxiomChoices();

                    // Repaint screen
                    paint(this.getGraphics());
                    // return true;   (itemStateChanged returns void)
                }
            }
        }
    }


    // Build list of choices from axiom menu
    private void buildOptionChoices() {

        // Remove everything at once for less display glitches
        this.removeAll();

        if (proofInfoModeFlag) {
            this.add(proof_exit_button);
            return;
        } else if (axiomInfoModeFlag) {
            this.add(info_exit_button);
            return;
        } else {
            option_choices = new Choice();

            option_choices.addItemListener(this);

      /* 11-Nov-2015 nm - Oracle changed the behavior of java.awt.Choice so
         that it no longer fires the itemStateChanged event when the first menu
         item is selected.  As a workaround, we add a dummy item to serve
         as the initial selection. */
            option_choices.addItem("Options:");
            // option_choices.select("Options:");  // Doesn't seem necessary

            if (!undoStack.empty()) {
                option_choices.addItem("Undo");
            }
            if (currentState.assertionVec.size() > 1) {
                option_choices.addItem("Swap Stack Top");
                option_choices.addItem("Rotate Stack");
            }
            if (!currentState.assertionVec.isEmpty()) {
                option_choices.addItem("Delete Stack Top");
            }
            if (!currentState.assertionVec.isEmpty()
                    || !currentState.hypothesisVec.isEmpty()) {
                option_choices.addItem("Erase Stack");
            }

            //if (currentState.assertionVec.size() > 0
            //    || currentState.hypothesisVec.size() > 0
            //    || !undoStack.empty()
            //    || numAxioms != builtInNumAxioms) {
            //  option_choices.addItem("Erase All");
            //}

            if (!currentState.assertionVec.isEmpty()) {
                option_choices.addItem("Proof Information");
            }
            option_choices.addItem("Axiom Information");
            option_choices.addItem("Add Hypothesis");
            option_choices.addItem("Select Logic Family");
            if (currentState != null && !currentState.assertionVec.isEmpty()) {
                option_choices.addItem("Save as Axiom");
            }
            // User workaround for Java graphics bugs
            option_choices.addItem("Refresh Screen");
            /* [sound] */
            if (enableAudioFlag) {
                /* [sound] */
                option_choices.addItem("Turn Sound Off");
                /* [sound] */
            } else {
                /* [sound] */   // Comment out next line to disable audio effects (if you don't have
                /* [sound] */   // the .au files)
                /* [sound] */
                option_choices.addItem("Turn Sound On");
                /* [sound] */
            }
        }

        this.add(option_choices);
    } // Build option choices

    // Build list of choices from axiom menu
    private void buildAxiomChoices() {

        if (proofInfoModeFlag) {
            return; // Disable selection in proof display mode
        }

        if (axiomInfoModeFlag) {
            axiom_label.setBackground(INFO_BACKGROUND_COLOR);
        } else {
            axiom_label.setBackground(BACKGROUND_COLOR);
        }

        this.add(axiom_label);
        axiomChoiceVec = new ArrayList<>();
        axiom_choices = new Choice();

        axiom_choices.addItemListener(this);

      /* 11-Nov-2015 nm - Oracle changed the behavior of java.awt.Choice so
         that it no longer fires the itemStateChanged event when the first menu
         item is selected.  As a workaround, we add a dummy item to serve
         as the initial selection. */
        axiom_choices.addItem("Axioms:");
        // axiom_choices.select("Axioms:"); // Doesn't seem necessary

        if (!currentState.hypothesisVec.isEmpty()) {
            // If there are hypotheses, do a dummy run-thru of assertions and
            // hypotheses to get desired variable names for axiom choice menu
            VariableName.init();
            for (int i = currentState.assertionVec.size() - 1; i >= 0; i--) {
                PrimFormula.getDisplay(
                        currentState.assertionVec.get(i), false);
            }
            for (int i = currentState.hypothesisVec.size() - 1; i >= 0; i--) {
                PrimFormula.getDisplay(
                        currentState.hypothesisVec.get(i), false);
            }
        }

        // Put any user hypotheses first
        // Variable names have not been reinitialized here; we want to use
        // the names in the currentState display for best user info
        String menuString = "";
        if (!axiomInfoModeFlag) {
            for (int i = 0; i < currentState.hypothesisVec.size(); i++) {
                menuString = "1 $hyp" + (i + 1) + " " +
                        PrimFormula.getDisplay(
                                currentState.hypothesisVec.get(i), false);
                if (menuString.length() > MAX_AXIOM_CHOICE_LEN) {
                    // Trim to size
                    menuString = menuString.substring(0, MAX_AXIOM_CHOICE_LEN - 3) + "...";
                }
                axiom_choices.addItem(menuString);
                axiomChoiceVec.add(-i - 1);
            }
        }

        // For each axiom, if it unifies with the state stack, add it in
        // Scan in reverse order of number of hypotheses, so axioms that reduce
        // stack the most will be displayed first
        // Note: in info mode we show *all* axioms in their natural order, whether
        // or not they unify
        String menuFormula = "";
        for (int hyps = maxAxiomHypotheses; hyps >= 0; hyps--) {
            if (axiomInfoModeFlag && hyps > 0) continue;
            for (int i = 0; i < axiomArr.length; i++) {
                if (!axiomInfoModeFlag &&
                        axiomArr[i].axiomHypothesisVec.size() != hyps) continue;
                if (hyps == 0) { // (or axiomInfoModeFlag - see continue logic above)
                    // If there are no hypotheses, don't bother to unify for speedup
                    menuString = axiomArr[i].menuEntry; // use pre-computed entry for speed
                } else {
                    State dummyState = Unification.unify(axiomArr[i], currentState, false);
                    if (dummyState == null) continue; // Unification not possible
                    menuFormula = dummyState.getStackTop();
                    VariableName.init(); // Initialize so types don't get mixed up
                    // Show how much stack will grow, name of axiom, & the top of the
                    // stack that would result
                    menuString = (1 - hyps)
                            + " " + axiomArr[i].label + " "
                            + PrimFormula.getDisplay(menuFormula, false);
                    if (menuString.length() > MAX_AXIOM_CHOICE_LEN) {
                        // Trim to size
                        menuString = menuString.substring(0, MAX_AXIOM_CHOICE_LEN - 3)
                                + "...";
                    }
                }
                axiom_choices.addItem(menuString);
                axiomChoiceVec.add(i);
            } /* next i */
        } /* next hyps */

        this.add(axiom_choices);
    }

    public void paint(Graphics g) {

        // validate makes an added Component show up in the display
        // (not documented in Java spec?)
        this.validate();

        VariableName.init(); // Initialize so types don't get mixed up

        // Clear screen
        Rectangle r = this.getBounds();
        g.setColor(this.getBackground());
        g.fillRect(r.x, r.y, r.width, r.height);

        currentX = X_INIT;
        currentY = 3 * Y_INCREMENT;

        // Display title
        g.setFont(new Font("Dialog", Font.PLAIN, FONT_SIZE));
        // Apply watermark to background
        // \u00a9 = copyright symbol
        if (axiomInfoModeFlag) {
            g.setColor(Color.magenta);
        } else {
            g.setColor(Color.cyan);
        }
        FontMetrics fm = g.getFontMetrics();
        String token = "Metamath Solitaire \u00a9 2003 (GPL) Norman Megill nm" +
                "@" +
                "alum.mit.edu";
        g.drawString(token, (r.width - fm.stringWidth(token)) / 2, r.height - 10);

        // Display type colors
        g.setFont(new Font("Dialog", Font.PLAIN, FONT_SIZE));
        g.setColor(Color.black);
        if (selectLogicModeFlag) {
            token = "Click on the logic family you want to use.";
            g.drawString(token, (r.width - fm.stringWidth(token)) / 2, r.height / 2);
            return;
        }

        g.setFont(new Font("Dialog", Font.BOLD, FONT_SIZE));
        token = familyName[currentFamily];
        g.drawString(token, X_INIT, currentY);
        fm = g.getFontMetrics();
        currentX += fm.stringWidth(token) + 2 * FONT_SIZE;
        g.setFont(new Font("Dialog", Font.PLAIN, FONT_SIZE));

        if (axiomInfoModeFlag && infoModeAxiomToShow == -1) {
            currentY = 5 * Y_INCREMENT;
            // User has not selected an axiom yet
            token =
                    "To see information about an axiom, choose it from the 'Axioms' menu.";
            g.drawString(token, X_INIT, currentY);
            return;
        }
        if (!axiomInfoModeFlag && currentState.assertionVec.isEmpty()
                && currentState.hypothesisVec.isEmpty()) {
            // There is nothing to display yet.  Just after startup or erase.
            currentY = 5 * Y_INCREMENT;
            g.drawString(
                    "The 'Axioms' menu shows how much the stack will grow, the axiom name,",
                    X_INIT, currentY);
            currentY += Y_INCREMENT;
            g.drawString(
                    "and as much of the axiom as can be displayed.",
                    X_INIT, currentY);
            currentY += 2 * Y_INCREMENT;
            g.drawString(
                    "Select repeatedly from the 'Axioms' menu.  The stack will grow and shrink",
                    X_INIT, currentY);
            currentY += Y_INCREMENT;
            g.drawString(
                    "with theorems.  The goal is to end up with a single stack entry containing",
                    X_INIT, currentY);
            currentY += Y_INCREMENT;
            g.drawString(
                    "a nice theorem.  You can clip out its proof with 'Proof Information'.",
                    X_INIT, currentY);
            currentY += 2 * Y_INCREMENT;
            if (currentFamily == PROP_CALC) {
                g.drawString(
                        "Example:  ax-1, ax-1, ax-2, ax-mp, ax-mp proves (P->P).",
                        X_INIT, currentY);
            }
            if (currentFamily == EUCLID) {
                currentY += Y_INCREMENT;
                g.drawString(
                        "For Euclidean geometry, Bxyz means \"y lies between x and z\", and Dxyzw means",
                        X_INIT, currentY);
                currentY += Y_INCREMENT;
                g.drawString(
                        "\"x is as distant from y as z is from w\".",
                        X_INIT, currentY);
                currentY += Y_INCREMENT;
                currentY += Y_INCREMENT;
                g.drawString(
                        "Reference:  A. Tarski, in The Axiomatic Method (ed. Henkin et. al.) (1959), p. 19",
                        X_INIT, currentY);
            }
            currentY += Y_INCREMENT;
            if (currentFamily == IMPL_LOGIC) {
                g.drawString(
                        "Reference:  R. Hindley and D. Meredith, J. Symb. Logic 55, 90-105 (1990)",
                        X_INIT, currentY);
            }
            if (currentFamily == INTUIT_LOGIC) {
                g.drawString(
                        "Reference:  T. Thatcher Robinson, J. Symb. Logic 33, 265-270 (1968)",
                        X_INIT, currentY);
            }
            if (currentFamily == MODAL_LOGIC) {
                g.drawString(
                        "Reference:  http://www-formal.stanford.edu/pub/jmc/mcchay69/node22.html",
                        X_INIT, currentY);
            }
            if (currentFamily == GODEL_LOGIC) {
                g.drawString(
                        "Reference:  G. Boolos and R. Jeffrey, Computability and Logic (1989), ch. 27",
                        X_INIT, currentY);
            }
            if (currentFamily == QUANTUM_LOGIC) {
                g.drawString(
                        "Reference:  M. Pavicic, Int. J. of Theoretical Physics 32, 1481-1505 (1993)",
                        X_INIT, currentY);
            }
            if (currentFamily == WEAKD_LOGIC) {
                g.drawString(
                        "Reference:  http://www.mpi-sb.mpg.de/igpl/Bulletin/V4-2/#Megill",
                        X_INIT, currentY);
            }
            return;
        }
        token = "Colors of variable types:";
        g.drawString(token, currentX, currentY);
        fm = g.getFontMetrics();
        currentX += fm.stringWidth(token) + 2 * FONT_SIZE;

        g.setFont(MATH_PLAIN_FONT);
        fm = g.getFontMetrics();

        //for (int i = 0; i < 4; i++) {  // Future
        for (int i = 0; i < 3; i++) {
            Color c = Color.black;
            switch (i) {
                case 0 -> {
                    c = Color.blue;
                    token = "wff";
                }
                case 1 -> {
                    c = Color.red;
                    token = currentFamily == EUCLID ? "point" : "set";
                }
                case 2 -> {
                    c = Color.magenta;
                    token = "class";
                }
                case 3 -> {
                    c = DARK_GREEN;
                    token = "digit";
                }
            }
            g.setColor(c);
            g.drawString(token, currentX, currentY);
            currentX += fm.stringWidth(token) + 2 * FONT_SIZE;
            // Only show wff color for propositional families
            if (currentFamily != PRED_CALC && currentFamily != PRED_DEFS
                    && currentFamily != SET_THEORY && currentFamily != SET_DEFS
                    && currentFamily != EUCLID && i == 0) break;
            // Show classes only for set theory definitions
            if (currentFamily != SET_DEFS && i == 1) break;
        }

        // Display stack (or requested axiom in info mode)
        String axiomOrTheorem = "axiom";
        if (!currentState.assertionVec.isEmpty() || axiomInfoModeFlag) {
            currentY += Y_INCREMENT * 3 / 2;
            g.setFont(new Font("Dialog", Font.PLAIN, FONT_SIZE));
            g.setColor(Color.black);
            if (axiomInfoModeFlag) {
                if (!axiomArr[infoModeAxiomToShow].proof.isEmpty()) {
                    axiomOrTheorem = "theorem";
                }
                g.setFont(new Font("Dialog", Font.BOLD, FONT_SIZE));
                token = "Information for " + axiomOrTheorem + " " +
                        axiomArr[infoModeAxiomToShow].label;
                g.drawString(token, X_INIT, currentY);
                currentY += Y_INCREMENT;
                g.setFont(new Font("Dialog", Font.PLAIN, FONT_SIZE));
                token = "Description:  " + axiomArr[infoModeAxiomToShow].description;
                g.drawString(token, X_INIT, currentY);
                if (!axiomArr[infoModeAxiomToShow].proof.isEmpty()) {
                    currentY += Y_INCREMENT;
                    token = "Proof:  " + axiomArr[infoModeAxiomToShow].proof;
                    g.drawString(token, X_INIT, currentY);
                }
                currentY += Y_INCREMENT * 3 / 2;
                token = "Assertion made by this " + axiomOrTheorem + ":";
            } else {
                token = currentState.hypothesisVec.isEmpty() ? "Assertion stack (each line is a theorem scheme of this logic family):" : "Assertion stack (each line is an inference from the hypotheses):";
            }
            g.drawString(token, X_INIT, currentY);
            g.setFont(MATH_PLAIN_FONT);
            VariableName.init(); // Initialize name map so var's will be renumbered
            if (axiomInfoModeFlag) {
                currentY += Y_INCREMENT;
                DrawSymbols.drawFormula(g, currentY,
                        axiomArr[infoModeAxiomToShow].assertion);
            } else {
                // Display from top of stack down
                for (int i = currentState.assertionVec.size() - 1; i >= 0; i--) {
                    currentY += Y_INCREMENT;
                    DrawSymbols.drawFormula(g, currentY,
                            currentState.assertionVec.get(i));
                }
            }
        }

        // Display hypotheses
        if (!(axiomInfoModeFlag ? axiomArr[infoModeAxiomToShow].axiomHypothesisVec : currentState.hypothesisVec).isEmpty()) {
            currentY += Y_INCREMENT * 3 / 2;
            g.setFont(new Font("Dialog", Font.PLAIN, FONT_SIZE));
            g.setColor(Color.black);
            // in reverse order
            // in reverse order
            token = axiomInfoModeFlag ? "Hypotheses for this " + axiomOrTheorem + ":" : "Hypotheses for the assertions in the stack:";
            g.drawString(token, X_INIT, currentY);
            g.setFont(MATH_PLAIN_FONT);
            if (axiomInfoModeFlag) {
                // Normal order
                //for (int i = 0; i
                //     < axiomArr[infoModeAxiomToShow].axiomHypothesisVec.size();
                //    i++) {
                // Reverse order
                for (int i =
                     axiomArr[infoModeAxiomToShow].axiomHypothesisVec.size() - 1;
                     i >= 0; i--) {
                    currentY += Y_INCREMENT;
                    DrawSymbols.drawFormula(g, currentY,
                            axiomArr[infoModeAxiomToShow].axiomHypothesisVec.get(i));
                }
            } else {
                // Normal order
                //for (int i = 0; i < currentState.hypothesisVec.size(); i++) {
                // Reverse order
                for (int i = currentState.hypothesisVec.size() - 1; i >= 0; i--) {
                    currentY += Y_INCREMENT;
                    DrawSymbols.drawFormula(g, currentY, currentState.hypothesisVec.get(i));
                }
            }
        }

        // Display distinct variable pairs
        if (!(axiomInfoModeFlag ? axiomArr[infoModeAxiomToShow].axiomDistVarVec : currentState.distinctVarVec).isEmpty()) {
            currentY += Y_INCREMENT + Y_INCREMENT / 2;
            g.setFont(new Font("Dialog", Font.PLAIN, FONT_SIZE));
            g.setColor(Color.black);
            g.drawString(
                    "Substitutions for these variable pairs may not have variables in",
                    X_INIT, currentY);
            currentY += Y_INCREMENT;
            token = axiomInfoModeFlag ? "common for an instance of the " + axiomOrTheorem +
                " to remain valid:" : "common for the assertions to remain valid:";
            g.drawString(token, X_INIT, currentY);
            g.setFont(MATH_PLAIN_FONT);
            currentY += Y_INCREMENT;

            ArrayList<String> dVarVec;
            dVarVec = axiomInfoModeFlag ? axiomArr[infoModeAxiomToShow].axiomDistVarVec : currentState.distinctVarVec;
            currentY = DrawSymbols.drawDistinct(g, currentY, dVarVec);
        }
    } // paint

    /* [sound] */ // Sound effects
    /* [sound] */
    private static void playAudio() {
        /* [sound] */
        if (enableAudioFlag && audioName != null) {
            /* [sound] */     // Find out if we've already read this one in so we don't read it again
            /* [sound] */     // (Is this really necessary or does Java cache the stuff internally?)
            /* [sound] */
            AudioClip a = null;
            /* [sound] */
            for (int i = 0; i < audioSaveNameVec.size(); i++) {
                /* [sound] */
                if (audioSaveNameVec.get(i).equals(audioName)) {
                    /* [sound] */
                    a = audioSaveClipVec.get(i);
                    /* [sound] */
                    break;
                    /* [sound] */
                }
                /* [sound] */
            }
//  /* [sound] */     if (a == null) {
//  /* [sound] */       a = getAudioClip(getCodeBase(), audioName + ".au");
//  /* [sound] */       audioSaveNameVec.add(audioName);
//  /* [sound] */       audioSaveClipVec.add(a);
//  /* [sound] */     }
            /* [sound] */
            a.play();
            /* [sound] */
        }
        /* [sound] */
        audioName = null;
        /* [sound] */
    } // playAudio

} // class mm

// Formula drawing
enum DrawSymbols {
	;

	static Graphics g;
    static int currentX;
    static int currentY;
    static FontMetrics fm;
    static short lastTokenType;

    // Box coordinates for special characters
    static int leftX;
    static int rightX;
    static int bottomY;
    static int topY;
    static int middleX;
    static int middleY;
    static int one4thX;
    static int three4thX;
    static int one4thY;
    static int three4thY;

    private static void setBox(int width) {
        leftX = currentX;
        rightX = currentX + width;
        bottomY = currentY;
        topY = currentY - fm.getHeight() * 2 / 3;
        middleX = (leftX + rightX) / 2;
        middleY = (bottomY + topY) / 2;
        one4thX = (leftX + middleX) / 2;
        three4thX = (middleX + rightX) / 2;
        one4thY = (bottomY + middleY) / 2;
        three4thY = (middleY + topY) / 2;
    }

    private static void drawToken(String token, short type) {
        Color c = Color.black; // -1 = connective
        if (type == (short) (-1)) {
            // Connective
            g.setColor(c);
            // Compose special tokens
            switch (token) {
// forall
                case "A." -> {
                    setBox(fm.stringWidth("M"));
                    g.drawLine(leftX, topY, middleX, bottomY);
                    g.drawLine(middleX, bottomY, rightX, topY);
                    g.drawLine(one4thX, middleY, three4thX, middleY);
                }
// arrow
                case "->" -> {
                    setBox(fm.stringWidth("M") * 2);
                    g.drawLine(leftX, middleY, rightX, middleY);
                    g.drawLine(middleX, topY, rightX, middleY);
                    g.drawLine(middleX, bottomY, rightX, middleY);
                }
// double arrow
                case "<->" -> {
                    setBox(fm.stringWidth("M") * 3);
                    g.drawLine(leftX, middleY, rightX, middleY);
                    g.drawLine(one4thX, topY, leftX, middleY);
                    g.drawLine(one4thX, bottomY, leftX, middleY);
                    g.drawLine(three4thX, topY, rightX, middleY);
                    g.drawLine(three4thX, bottomY, rightX, middleY);
                }
// vee
                case "\\/" -> {
                    setBox(fm.stringWidth("M"));
                    g.drawLine(leftX, three4thY, middleX, bottomY);
                    g.drawLine(rightX, three4thY, middleX, bottomY);
                }
// wedge
                case "/\\" -> {
                    setBox(fm.stringWidth("M"));
                    g.drawLine(leftX, bottomY, middleX, three4thY);
                    g.drawLine(rightX, bottomY, middleX, three4thY);
                }
// exists
                case "E." -> {
                    setBox(fm.stringWidth("M"));
                    g.drawLine(leftX, bottomY, rightX, bottomY);
                    g.drawLine(leftX, middleY, rightX, middleY);
                    g.drawLine(leftX, topY, rightX, topY);
                    g.drawLine(rightX, topY, rightX, bottomY);
                }
// not
                case "-." -> {
                    setBox(fm.stringWidth("M"));
                    g.drawLine(leftX, middleY, rightX, middleY);
                    g.drawLine(rightX, middleY, rightX, bottomY);
                }
// epsilon
                case "e." -> {
                    setBox(fm.stringWidth("M"));
                    g.drawLine(leftX, middleY, middleX, topY);
                    g.drawLine(middleX, topY, rightX, topY);
                    g.drawLine(leftX, middleY, middleX, bottomY);
                    g.drawLine(middleX, bottomY, rightX, bottomY);
                    g.drawLine(leftX, middleY, three4thX, middleY);
                }
// union
                case "u." -> {
                    setBox(fm.stringWidth("M"));
                    g.drawLine(leftX, three4thY, leftX, one4thY);
                    g.drawLine(leftX, one4thY, middleX, bottomY);
                    g.drawLine(middleX, bottomY, rightX, one4thY);
                    g.drawLine(rightX, one4thY, rightX, three4thY);
                }
// intersection
                case "i^i" -> {
                    setBox(fm.stringWidth("M"));
                    g.drawLine(leftX, bottomY, leftX, middleY);
                    g.drawLine(leftX, middleY, middleX, three4thY);
                    g.drawLine(middleX, three4thY, rightX, middleY);
                    g.drawLine(rightX, middleY, rightX, bottomY);
                }
// Union
                case "U." -> {
                    setBox(fm.stringWidth("M"));
                    g.drawLine(leftX, topY, leftX, one4thY);
                    g.drawLine(leftX, one4thY, middleX, bottomY);
                    g.drawLine(middleX, bottomY, rightX, one4thY);
                    g.drawLine(rightX, one4thY, rightX, topY);
                }
// Intersection
                case "|^|" -> {
                    setBox(fm.stringWidth("M"));
                    g.drawLine(leftX, bottomY, leftX, three4thY);
                    g.drawLine(leftX, three4thY, middleX, topY);
                    g.drawLine(middleX, topY, rightX, three4thY);
                    g.drawLine(rightX, three4thY, rightX, bottomY);
                }
// subset
                case "(_" -> {
                    setBox(fm.stringWidth("M"));
                    g.drawLine(rightX, topY, leftX, topY);
                    g.drawLine(leftX, topY, leftX, one4thY);
                    g.drawLine(leftX, one4thY, rightX, one4thY);
                    g.drawLine(leftX, bottomY, rightX, bottomY);
                }
// empty set
                case "(/)" -> {
                    setBox(fm.stringWidth("M"));
                    g.drawOval(leftX, topY, rightX - leftX, bottomY - topY);
                    g.drawLine(leftX, bottomY, rightX, topY);
                }
// box
                case "[]" -> {
                    setBox(fm.stringWidth("M"));
                    g.drawRect(leftX, topY, rightX - leftX, bottomY - topY);
                }
// diamond
                case "<>" -> {
                    setBox(fm.stringWidth("M"));
                    g.drawLine(leftX, middleY, middleX, topY);
                    g.drawLine(middleX, topY, rightX, middleY);
                    g.drawLine(rightX, middleY, middleX, bottomY);
                    g.drawLine(middleX, bottomY, leftX, middleY);
                }
// upside-down T (false)
                case "_|_" -> {
                    setBox(fm.stringWidth("M"));
                    g.drawLine(leftX, bottomY, rightX, bottomY);
                    g.drawLine(middleX, bottomY, middleX, topY);
                }
// Output as is
                default -> {
                    g.drawString(token, currentX, currentY);
                    rightX = currentX + fm.stringWidth(token);
                }
            }
            currentX = rightX + 1;
        } else {
            // Variable
            c = switch (type) {
// wff
                case 0 -> Color.blue;
// set (var)
                case 1 -> Color.red;
// class
                case 2 -> Color.magenta;
// digit
                case 3 -> MetaMath.DARK_GREEN;
                default -> Color.black;
            };
            g.setColor(c);

            // Use italics for variables
            //g.setFont(mm.MATH_ITALIC_FONT);

            // Put extra space between two variables for better appearance
            if (lastTokenType >= 0) currentX += MetaMath.WHITE_SPACE / 2;
            g.drawString(token, currentX, currentY);
            currentX += fm.stringWidth(token);

            // Restore to non-italic
            //g.setFont(mm.MATH_PLAIN_FONT);

        }

        lastTokenType = type;

        //for (int i = 0; i < token.length(); i++) {
        //  character = token.substring(i, i + 1);
        //  g.drawString(character, currentX, currentY);
        //  currentX += fm.stringWidth(character) + mm.CHAR_SPACE;
        //}
        currentX += MetaMath.WHITE_SPACE;

    }


    static void drawFormula(Graphics wg, int wcurrentY, String formula) {

        lastTokenType = (-1); /* Init */

        g = wg;
        fm = g.getFontMetrics();
        currentY = wcurrentY;
        currentX = MetaMath.X_INIT;
        formula = PrimFormula.getDisplay(formula, true); // true returns variables
        //   as $var:type

        formula += " ";
        int position0 = 0;
        int position = formula.indexOf(' ');
        while (position != -1) {
            String token = formula.substring(position0, position);
            if (token.charAt(0) == '$') { // Variable
                int p2 = token.indexOf(':');
                short varNum = (short) Integer.parseInt(token.substring(1, p2));
                short varType = (short) Integer.parseInt(token.substring(p2 + 1));
                drawToken(VariableName.name(varNum, varType), varType);
            } else { // Connective
                drawToken(token, (short) (-1));
            }
            position0 = position + 1;
            position = formula.indexOf(' ', position0);
        }
    }

    // Draw the distinct variable pair list
    // Returns the new currentY
    static int drawDistinct(Graphics wg, int wcurrentY,
                            ArrayList<String> distinctVarVec) {

        lastTokenType = (-1); /* Init */

        g = wg;
        fm = g.getFontMetrics();
        currentY = wcurrentY;
        currentX = MetaMath.X_INIT;

        for (int i = 0; i < distinctVarVec.size(); i++) {
            for (int j = 0; j < 2; j++) {
                short v = (short) (distinctVarVec.get(i).charAt(j));
                // We assume type is already assigned, so 0 is OK */
                drawToken(VariableName.name(v, (short) 0), VariableName.type(v));
                currentX += 4;
            }
            currentX += 20;
            // New line every 10 pairs
            if ((i + 1) % 10 == 0) {
                currentX = MetaMath.X_INIT;
                currentY += MetaMath.Y_INCREMENT;
            }
        }
        return currentY;
    }


} // class DrawSymbols

// Primitive formula handler
enum PrimFormula {
	;

	// Return formula in standard (display) notation
    // If raw, then each variable is in the form $n:m, where m is the type
    static String typesList;

    // Get shortest primitive formula
    static String pformula(String formula, int start) {
        if ((short) (formula.charAt(start)) > 0) {
            // It's a variable
            return formula.substring(start, start + 1);
        } else {
            // It's a connective
            short connNum = (short) (formula.charAt(start));
            connNum = (short) (-(connNum + 1));
            String subformula = formula.substring(start, start + 1);
            int position = start;
            for (int i = 0; i < MetaMath.connectiveArr[connNum].argtypes.length; i++) {
                position = start + subformula.length();
                subformula += pformula(formula, position);
            }
            return subformula;
        }
    } // pformula

    // Return variable/connective types in a formula string
    static String getTypes(String formula, int start) {
        if ((short) (formula.charAt(start)) > 0) {
            // It's a variable; we don't know the type yet; default to wff
            return String.valueOf((char) 0);
        } else {
            // It's a connective
            short connNum = (short) (formula.charAt(start));
            connNum = (short) (-(connNum + 1));
            short typeNum = MetaMath.connectiveArr[connNum].type;
            String typesList = String.valueOf((char) typeNum);
            int position = start;
            for (int i = 0; i < MetaMath.connectiveArr[connNum].argtypes.length; i++) {
                position = start + typesList.length();
                typeNum = MetaMath.connectiveArr[connNum].argtypes[i];
                // Override the type of the first return char (could be
                // a variable with type not yet known)
                typesList = typesList + (char) typeNum
                        + (getTypes(formula, position)).substring(1);
            }
            return typesList;
        }
    } // getTypes

    static String getDisplay(String formula, boolean raw) {
        typesList = getTypes(formula, 0);
        return subGetDisplay(formula, 0, raw);
    }

    static String subGetDisplay(String formula, int start, boolean raw) {
        // String tokenSeparator = " "; // Separator character between tokens in axiom menu
        String tokenSeparator = ""; // Separator character between tokens in axiom menu
        // Must always be a space for further parsing
        // Separator character between tokens in axiom menu
        tokenSeparator = raw ? " " : "";
        if ((short) (formula.charAt(start)) > 0) {
            // It's a variable
            return raw ? "$" + (int) formula.charAt(start) + ":"
                + (int) typesList.charAt(start) : VariableName.name((short) (formula.charAt(start)),
                (short) typesList.charAt(start));
        } else {
            // It's a connective
            short connNum = (short) (formula.charAt(start));
            connNum = (short) (-(connNum + 1));
            String subformula = formula.substring(start, start + 1);
            int position = start;
            String[] displayArgs = new String[MetaMath.connectiveArr[connNum].argtypes.length];
            // Collect the arguments in display notation
            for (int i = 0; i < MetaMath.connectiveArr[connNum].argtypes.length; i++) {
                position = start + subformula.length();
                subformula += pformula(formula, position);
                displayArgs[i] = subGetDisplay(formula, position, raw);
            }
            String tmpNotation = MetaMath.connectiveArr[connNum].notation + " ";
            String displayFormula = "";
            // Replace the arguments in the connectives display notation
            int charPosition0 = 0;
            int charPosition = tmpNotation.indexOf(' ');
            while (charPosition != -1) {
                String token = tmpNotation.substring(charPosition0, charPosition);
                if (token.charAt(0) == '$') { // Display template argument
                    short argNum = (short) Integer.parseInt(token.substring(1));
                    displayFormula = displayFormula.isEmpty() ? displayArgs[argNum - 1] : displayFormula + tokenSeparator
                        + displayArgs[argNum - 1];
                } else { // Display connective - output as is
                    displayFormula = displayFormula.isEmpty() ? token : displayFormula + tokenSeparator + token;
                }
                charPosition0 = charPosition + 1;
                charPosition = tmpNotation.indexOf(' ', charPosition0);
            }
            return displayFormula;
        }
    } // subGetDisplay

} // class PrimFormula

// Get name for display of variable
enum VariableName {
	;
	static ArrayList<String> varNameVec = new ArrayList<>();
    static ArrayList<Integer> varTypeVec = new ArrayList<>();
    static int[] varSoFar = new int[4]; // Counter for how many so far for
    // each type

    // Initialize (e.g. after renormalizing variables)
    static void init() {
        varNameVec = new ArrayList<>();
        varTypeVec = new ArrayList<>();
        varSoFar = new int[4]; // Initialized to 0; there are 4 types
    }

    // Get name of variable - type must be 0 thru 3 (wff thru digit)
    static String name(short var, short type) {
        // wff, var, class, digit
        if (var >= varNameVec.size()) { // extend to accomodate variable
            varNameVec.ensureCapacity(var + 1);
            varTypeVec.ensureCapacity(var + 1);
        }
        if (varNameVec.size() <= var || varNameVec.get(var) == null) { // hasn't been assigned yet
            while (varNameVec.size() <= var) {
                varNameVec.add(null);
                varTypeVec.add(null);
            }
            // Get name based on type and previous names
            int v = varSoFar[type];
            varSoFar[type]++;
            String[] letters = {"PQRSTUWXYZ", "xyzwvutsrqpnmlkjihgfdcba",
                    "ABCDFGHJKLMN", "e"};
            int quotient = v / letters[type].length();
            int remainder = v % letters[type].length();
            String suffix;
            suffix = quotient == 0 ? "" : Integer.toString(quotient - 1);
            varNameVec.set(var, letters[type].charAt(remainder) + suffix);
            varTypeVec.set(var, (int) type);
        }
        return varNameVec.get(var);
    }

    // This is a handy way to find out variable's type, but should only
    // be used after getting the variable's name
    static short type(short var) {
        return (short) (varTypeVec.get(var).intValue());
    }

} // VariableDisplay

// Define a logical connective
class Connective {
    String label;
    short type;
    short[] argtypes;
    String notation;

    Connective(String label, String wtype, int numArgs, String notation) {
        this.label = label;
        this.type = getExprType(wtype);
        this.argtypes = new short[numArgs];
        this.notation = notation;
    }

    // Get expression type number for input string; return -1 if bad
    private static short getExprType(String stype) {
		String[] TYPE_LIST = {"wff", "var", "class", "digit"};
        for (int i = 0; i < TYPE_LIST.length; i++) {
            if (TYPE_LIST[i].equals(stype)) return (short) i;
        }
        return (short) (-1);
    }

    void setArgtype(int arg, String sarg) {
        this.argtypes[arg] = getExprType(sarg);
    }

} // class Connective

// Define an axiom
// (Future:  make Axiom same class as State for uniformity & to simplify pgm?)
class Axiom {
    final String label;
    final String assertion;
    final ArrayList<String> axiomHypothesisVec;
    final ArrayList<String> axiomDistVarVec; // Each string always has length 2
    final String proof; // when converted from currentState
    final String description;
    String menuEntry; // String to put into menu when no unification is

    // needed (store instead of recompute to speed up menu build)
    Axiom(String label, String englRPN, String description) {
        // This is the constructor for built-in axioms
        this.label = label;
        this.assertion = englToNumStr(englRPN);
        this.axiomHypothesisVec = new ArrayList<>();
        this.axiomDistVarVec = new ArrayList<>();
        this.description = description;
        this.proof = ""; // no proof for axiom (user-added theorems have proofs)
        makeMenuEntry();
    }

    // This constructor converts the top of the assertion stack into a Axiom
    // (Future major revision:  make Axiom and State the same class for
    //  simplicity?)
    Axiom(State st) {
        // We have to make clones because state contents will be changing
        State stCopy = st.makeClone();

        // Remove all assertions except last
        ArrayList<String> trimmedAssertionVec = new ArrayList<>();
        trimmedAssertionVec.add(stCopy.assertionVec.get(stCopy.assertionVec.size() - 1));
        ArrayList<String> trimmedProofVec = new ArrayList<>();
        trimmedProofVec.add(stCopy.proofVec.get(stCopy.proofVec.size() - 1));
        stCopy.assertionVec = trimmedAssertionVec;
        stCopy.proofVec = trimmedProofVec;
        stCopy.normalize(); // Trim distinct vars

        label = "user-" + (MetaMath.userTheorems.size() + 1);
        assertion = stCopy.assertionVec.get(stCopy.assertionVec.size() - 1);
        proof = stCopy.proofVec.get(stCopy.proofVec.size() - 1);
        axiomHypothesisVec = stCopy.hypothesisVec;
        axiomDistVarVec = stCopy.distinctVarVec;
        description = "Theorem added by user";
        makeMenuEntry();
    }

    // Convert RPN character, space-separated strings to RPN numeric strings
    // Connectives are negative, variables are positive
    private static String englToNumStr(String englRPN) {
        StringBuilder numRPNbuf = new StringBuilder();

        englRPN += " ";
        numRPNbuf.ensureCapacity(englRPN.length() / 2);
        int position0 = 0;
        int position = englRPN.indexOf(' ');
        while (position != -1) {
            String token = englRPN.substring(position0, position);
            if (token.charAt(0) == '$') { // Variable
                short varNum = (short) Integer.parseInt(token.substring(1));
                numRPNbuf.append((char) varNum);
            } else { // Connective
                int i = MetaMath.connectiveLabels.indexOf(" " + token + " ");
                if (i == -1) System.out.println("Bug: Unknown connective " + token);
                short connNum = (short) (MetaMath.connectiveLabelMap.charAt(i));
                numRPNbuf.append((char) (-(connNum + 1)));
            }
            position0 = position + 1;
            position = englRPN.indexOf(' ', position0);
        }
        return numRPNbuf.toString();
    } // englToNumStr

    void addHyp(String hyp) {
        this.axiomHypothesisVec.add(englToNumStr(hyp));
    }

    void addDistinct(String distinctVarPair) {
        this.axiomDistVarVec.add(englToNumStr(distinctVarPair));
        // Future error check:  string length == 2
    }

    private void makeMenuEntry() {
        // Make string for axiom menu when no unification required
        VariableName.init();  // Reset variable vs. variable name & type
        // for PrimFormula.getDisplay(..)
        menuEntry = (1 - axiomHypothesisVec.size()) // Amt stack increases
                + " " + label + " "                              // Label
                + PrimFormula.getDisplay(assertion, false);      // ASCII formula
        if (menuEntry.length() > MetaMath.MAX_AXIOM_CHOICE_LEN) {
            // Trim to size
            menuEntry = menuEntry.substring(0, MetaMath.MAX_AXIOM_CHOICE_LEN - 3) + "...";
        }
    }

} // class Axiom


// Define the stack (state)
class State {
    short maxVar; /* Largest variable used */
    ArrayList<String> assertionVec;
    ArrayList<String> proofVec; // Proof for each assertion
    ArrayList<String> hypothesisVec;
    ArrayList<String> distinctVarVec; // each string always has length 2

    ArrayList<Axiom> userThVec; // copy of userTheorems; used for Undo
    final int currentFam; // for Undo

    State() {  // Constructor
        maxVar = 0;
        assertionVec = new ArrayList<>();
        proofVec = new ArrayList<>();
        hypothesisVec = new ArrayList<>();
        distinctVarVec = new ArrayList<>();
        userThVec = MetaMath.userTheorems;
        currentFam = MetaMath.currentFamily;
    }

    // Build a special version of a State containing all steps of the proof
    // instead of just the stack entries.  Also, each proofVec string in the
    // special State has <step# label,step-ref,step-ref,...> instead of an
    // axiom list.  Used to display detailed proof for the 'Proof Info'
    // option.
    static State buildProofInfoState(State currentState) {
        // Add proof steps one by one with special unify() mode, keeping all steps
        // Get the axiom-list version of the proof of the top of the stack
        String proof = currentState.proofVec.get(currentState.proofVec.size() - 1);
        State proofInfoState = new State();
        // Copy any hypotheses
        for (int i = 0; i < currentState.hypothesisVec.size(); i++) {
            proofInfoState.addHyp();
        }
        // Scan the axiom-list proof
        proof += " ";
        int position0 = 0;
        int position = proof.indexOf(' ');
        while (position != -1) {
            String label = proof.substring(position0, position);
            if (label.charAt(0) == '$') { // Hypothesis $hypnn - future: make sure
                //   that $ is not allowed if user name accepted for user proofs
                int hypNum = Integer.parseInt(label.substring(4)) - 1;
                proofInfoState.pushAssertion(
                        proofInfoState.hypothesisVec.get(hypNum),
                        // Special label for hypothesis step
                    (proofInfoState.assertionVec.size() + 1) + " "
                                + label);
            } else {
                // Find the axiom with this label
                // Linear seach -- future speedup?
                int i;
                for (i = 0; i < MetaMath.axiomArr.length; i++) {
                    if (MetaMath.axiomArr[i] == null) continue;
                    if (MetaMath.axiomArr[i].label.equals(label)) break;
                }
                proofInfoState = Unification.unify(MetaMath.axiomArr[i], proofInfoState,
                        true);  // true means unification will not delete popped hypotheses
            }
            position0 = position + 1;
            position = proof.indexOf(' ', position0);
        } // end while position != -1

        // Sort the proof steps (they are not sorted in proofInfoState)
        int[] stepSortMap = new int[proofInfoState.assertionVec.size()];
        for (int i = 0; i < stepSortMap.length; i++) {
            String labl = proofInfoState.proofVec.get(i);
            int stepNum = Integer.parseInt(labl.substring(0,
                    labl.indexOf(' '))) - 1;
            stepSortMap[stepNum] = i;
        }
        ArrayList<String> sortedAssertionVec = new ArrayList<>();
        ArrayList<String> sortedProofVec = new ArrayList<>();
        for (int step : stepSortMap) {
            sortedAssertionVec.add(proofInfoState.assertionVec.get(step));
            sortedProofVec.add(proofInfoState.proofVec.get(step));
        }
        proofInfoState.assertionVec = sortedAssertionVec;
        proofInfoState.proofVec = sortedProofVec;
        // end sort

        proofInfoState.normalize(); // Trim distinct vars, init var names
        // Dummy run thru steps in reverse order for desired variable name
        // assignment
        for (int i = proofInfoState.assertionVec.size() - 1; i >= 0; i--) {
            PrimFormula.getDisplay(proofInfoState.assertionVec.get(i), false);
        }
        return proofInfoState;
    } // buildProofInfoState

    State makeClone() {
        State c = new State();
        c.maxVar = maxVar;
        c.assertionVec = new ArrayList<>(assertionVec);
        c.proofVec = new ArrayList<>(proofVec);
        c.hypothesisVec = new ArrayList<>(hypothesisVec);
        c.distinctVarVec = new ArrayList<>(distinctVarVec);
        c.userThVec = new ArrayList<>(userThVec);
        return c;
    }

    String getAssertion(int position) {
        return assertionVec.get(position);
    }

    String getStackTop() {
        return assertionVec.get(assertionVec.size() - 1);
    }

    void pushAssertion(String assertion, String proof) {
        assertionVec.add(assertion);
        proofVec.add(proof);
    }

    void removeAssertionAt(int position) {
        assertionVec.remove(position);
    }

    String getHyp(int position) {
        return hypothesisVec.get(position);
    }

    void addHyp() {
        hypothesisVec.add(String.valueOf((char) (++maxVar)));
    }

    void removeHypAt(int position) {
        hypothesisVec.remove(position);
    }

    void normalize() {
        // Renumber all variables; reduce maxVar if gaps were eliminated
        // Also, trim off any distinct pairs that aren't in assertion or hyp
        // (important, otherwise distinct var list will have garbage entries)
        // Note:  variables are numbered starting at 1, not 0.
        short newMax = 0;
        short[] varMap = new short[maxVar + 1];
        StringBuilder scanBuf;
        int i;
        int j;
        short c;
        // Scan assertions
        for (i = 0; i < assertionVec.size(); i++) {
            scanBuf = new StringBuilder(assertionVec.get(i));
            for (j = 0; j < scanBuf.length(); j++) {
                c = (short) (scanBuf.charAt(j));
                if (c < 0) continue; // not a variable
                if (varMap[c] == 0) {
                    // Add new variable
                    newMax++;
                    varMap[c] = newMax;
                }
                scanBuf.setCharAt(j, (char) (varMap[c]));
            }
            assertionVec.set(i, scanBuf.toString());
        }
        // Scan hypotheses
        for (i = 0; i < hypothesisVec.size(); i++) {
            scanBuf = new StringBuilder(hypothesisVec.get(i));
            for (j = 0; j < scanBuf.length(); j++) {
                c = (short) (scanBuf.charAt(j));
                if (c < 0) continue; // not a variable
                if (varMap[c] == 0) {
                    // Add new variable
                    newMax++;
                    varMap[c] = newMax;
                }
                scanBuf.setCharAt(j, (char) (varMap[c]));
            }
            hypothesisVec.set(i, scanBuf.toString());
        }
        // Scan distinct variable pairs
        for (i = 0; i < distinctVarVec.size(); i++) {
            scanBuf = new StringBuilder(distinctVarVec.get(i));
            boolean discardFlag = false;
            for (j = 0; j < scanBuf.length(); j++) {
                if (j > 1) System.out.println("Bug #1"); // S.b. only two vars each
                c = (short) (scanBuf.charAt(j));
                if (varMap[c] == 0) {
                    // In the case of distinct variables, we want to throw away
                    // ones not yet mapped (i.e. in no assertion or hypothesis)
                    discardFlag = true;
                    break;
                }
                scanBuf.setCharAt(j, (char) (varMap[c]));
            }
            if (!discardFlag) {
                distinctVarVec.set(i, scanBuf.toString());
            } else {
                distinctVarVec.remove(i);
                i--;
            }
        }
        // Update maxVar
        maxVar = newMax;
        // Initialize variable name/type finder
        VariableName.init();
    }

} // class State

// Define a substitution
class Substitution {
    final short substVar;      // The variable being substituted
    String substString;  // What it's substituted with

    Substitution(short substVar, String substString) {
        this.substVar = substVar;
        this.substString = substString;
    }

    // Makes a substitution into a formula
    static String makeSubst(String formula, Substitution subst) {
        int i = -1;
        while (true) {
            i = formula.indexOf(subst.substVar, i + 1);
            if (i < 0) break;
            formula = formula.substring(0, i) + subst.substString
                    + formula.substring(i + 1);
        }
        return formula;
    }

    // Makes a set of substitutions into a formula
    static String makeVecSubst(String formula, ArrayList substVec) {
        for (Object o : substVec) {
            formula = makeSubst(formula, (Substitution) o);
        }
        return formula;
    }
} // class Substitution

// Define unification methods
enum Unification {
	;

	// These variables are used by calling program
    static ArrayList<String> newDistinctVarVec;
    static short oldMaxVar; // Original largest variable
    static short newMaxVar; // New largest variable
    static ArrayList<Substitution> substVec; // Substitution list to make throughout State

    // Local static variables
    static ArrayList<String> axiomHypVec = new ArrayList<>();
    static ArrayList<String> stateHypVec = new ArrayList<>();

    // Unification algorithm - returns a new State if unification
    // possible, null otherwise
    static State unify(Axiom testAxiom, State currentState,
                             boolean proofInfoFlag) {

        substVec = new ArrayList<>();
        int currentStateStackSize = currentState.assertionVec.size();
        if (testAxiom == null) return null; // To allow for sloppy axiom array
        int axiomHypSize = testAxiom.axiomHypothesisVec.size();

        // See if stack has enough entries
        if (currentStateStackSize < axiomHypSize) {
            return null;
        }

        // Build state hypothesis ArrayList
        stateHypVec = new ArrayList<>();
        int hyp;
        for (hyp = 0; hyp < axiomHypSize; hyp++) {
            stateHypVec.add(currentState.assertionVec.get(
                    currentStateStackSize - axiomHypSize + hyp));
        }

        // Don't destroy caller's distinctVarVec
        newDistinctVarVec = new ArrayList<>(currentState.distinctVarVec);

        oldMaxVar = currentState.maxVar;
        newMaxVar = currentState.maxVar;

        // Build axiom hypothesis ArrayList with renumbered variables
        axiomHypVec = new ArrayList<>();
        for (hyp = 0; hyp < axiomHypSize; hyp++) {
            axiomHypVec.add(renumberVars(testAxiom.axiomHypothesisVec.get(hyp)));
        }
        // Renumber distinct variables of axiom and add to dist var ArrayList
        int i;
        for (i = 0; i < testAxiom.axiomDistVarVec.size(); i++) {
            newDistinctVarVec.add(renumberVars(testAxiom.axiomDistVarVec.get(i)));
        }

        // Unify each hypothesis
        for (hyp = 0; hyp < axiomHypSize; hyp++) {
            i = -1;
            String stateHyp;
            String axiomHyp;
            while (true) {
                i++;
                // Assign working hypotheses strings; also reassign them each
                // pass thru loop to reflect result of substitution at end of loop
                axiomHyp = axiomHypVec.get(hyp);
                stateHyp = stateHypVec.get(hyp);
                if (i >= axiomHyp.length() || i >= stateHyp.length()) {
                    break;
                }
                short cr = (short) (axiomHyp.charAt(i));
                short cs = (short) (stateHyp.charAt(i));
                if (cr == cs) continue;
                String substStr;
                short substVar;
                if (cr > 0) { // Variable in axiom
                    substStr = PrimFormula.pformula(stateHyp, i); // Get subformula
                    substVar = cr;
                } else {
                    if (cs > 0) { // Variable in state hyp
                        substStr = PrimFormula.pformula(axiomHyp, i); // Get subformula
                        substVar = cs;
                    } else {
                        return null; // Unif not possible - connectives mismatch
                    }
                }
                if (substStr.indexOf((char) substVar) >= 0) {
                    return null; // Unif not possible - substituted var in substitution
                }
                Substitution subst = new Substitution(substVar, substStr);
                if (!rebuildDistinct(subst)) {
                    return null; // Dist var violation
                }
                makeSub(subst); // Make subst to hyp's and substVec
                substVec.add(subst);
            }
            if (axiomHyp.length() != stateHyp.length()) {
                return null; // Unif not possible
            }
        }

        // Build new State to return to caller
        State newState = new State();
        // Build new assertion stack
        newState.assertionVec = new ArrayList<>();

        if (proofInfoFlag) {
            // Don't discard used-up assertions in this mode, but put them
            // at the bottom of the stack so they'll be available for the detailed
            // proof
            for (i = currentStateStackSize - axiomHypSize; i < currentStateStackSize;
                 i++) {
                newState.pushAssertion(Substitution.makeVecSubst(
                        currentState.assertionVec.get(i), substVec),
                        currentState.proofVec.get(i));
            }
        }

        // Copy assertions and their proofs that were not popped by unification
        for (i = 0; i < currentStateStackSize - axiomHypSize; i++) {
            newState.pushAssertion(Substitution.makeVecSubst(
                    currentState.assertionVec.get(i), substVec),
                    currentState.proofVec.get(i));
        }
        // Build proof for new assertion
        String newProof = testAxiom.label;
        if (proofInfoFlag) {
            // Format is step#, axiom used, steps used by hypotheses of axiom
            newProof = (currentStateStackSize + 1) + " " + newProof;
            for (i = currentStateStackSize - 1;
                 i >= currentStateStackSize - axiomHypSize; i--) {
                newProof = newProof + "," +
                        currentState.proofVec.get(i).substring(0,
                                currentState.proofVec.get(i).indexOf(' '));
            }
        } else {
            // Format is axiom used, preceded by concatenated proofs of hypotheses
            for (i = currentStateStackSize - 1;
                 i >= currentStateStackSize - axiomHypSize; i--) {
                newProof = currentState.proofVec.get(i) + " " + newProof;
            }
        }
        // Push new, substituted assertion and proof from result of axiom
        newState.pushAssertion(
                Substitution.makeVecSubst(renumberVars(testAxiom.assertion), substVec),
                newProof);
        // Copy hypotheses with substitutions made to them
        for (i = 0; i < currentState.hypothesisVec.size(); i++) {
            newState.hypothesisVec.add(Substitution.makeVecSubst(
                    currentState.hypothesisVec.get(i), substVec));
        }
        // Assign distinct variable list
        newState.distinctVarVec = newDistinctVarVec;
        // Assign largest variable
        newState.maxVar = newMaxVar;
        return newState;
    }

    // Renumber variables in a formula from a axiom, by adding axiom's var #
    // (which must be > 0) to oldMaxVar
    static String renumberVars(String axiomFormula) {
        StringBuilder formulaBuf = new StringBuilder(axiomFormula);
        // Renumber variables
        for (int i = 0; i < formulaBuf.length(); i++) {
            if ((short) (formulaBuf.charAt(i)) > 0) {
                short newVar = (short) (oldMaxVar + (short) (formulaBuf.charAt(i)));
                formulaBuf.setCharAt(i, (char) newVar);
                if (newVar > newMaxVar) newMaxVar = newVar;
            }
        }
        return formulaBuf.toString();
    }


    // Make substitution to hyp's and substVec (substVec is theoretically
    // not necessary but done to speed things up)
    static void makeSub(Substitution subst) {
        for (int hyp = 0; hyp < stateHypVec.size(); hyp++) {
            stateHypVec.set(hyp, Substitution.makeSubst(stateHypVec.get(hyp), subst));
            axiomHypVec.set(hyp, Substitution.makeSubst(axiomHypVec.get(hyp), subst));
        }
        for (Substitution substitution : substVec) {
            substitution.substString =
                Substitution.makeSubst(substitution.substString, subst);
        }
    }

    // Rebuild newDistinctVarVec after a substitution
    // Returns true if no distinct variable violations, false if there were
    static boolean rebuildDistinct(Substitution subst) {
        int ilimit = newDistinctVarVec.size();
        int i;
        boolean found = false;
        short v0;
        short v1;
        String vi = null;
        String dpair;
        for (i = 0; i < ilimit; i++) {
            vi = newDistinctVarVec.get(i);
            v0 = (short) (vi.charAt(0));
            v1 = (short) (vi.charAt(1));
            if (v1 == subst.substVar) {
                short vtmp = v0;
                v0 = v1;
                v1 = vtmp;
            }
            if (v0 == subst.substVar) {
                // 1st var is substituted
                for (int j = 0; j < (subst.substString).length(); j++) {
                    short vsub = (short) ((subst.substString).charAt(j));
                    if (vsub < 0) continue; // Not a variable
                    if (vsub == v1) {
                        // Distinct variable conflict
                        return false;
                    }
                    // Spawn off a new pair
                    dpair = String.valueOf((char) vsub) + (char) v1;
                    newDistinctVarVec.add(dpair);
                }
                // Remove substituted pair
                newDistinctVarVec.remove(i);
                // And lower limits
                i--;
                ilimit--;
                found = true;
            }
        }
        if (found) { // If a substitution was made, clean up the list
            // Put variables in ascending order in each pair
            ilimit = newDistinctVarVec.size();
            for (i = 0; i < ilimit; i++) {
                v0 = (short) (vi.charAt(0));
                v1 = (short) (vi.charAt(1));
                if (v0 > v1) {
                    // Swap vars
                    dpair = String.valueOf((char) v1) + (char) v0;
                    newDistinctVarVec.set(i, dpair);
                }
            }
            // Sort the list
            Collections.sort(newDistinctVarVec);
            // Remove duplicates
            for (i = ilimit - 1; i > 0; i--) {
                if (vi.equals(newDistinctVarVec.get(i - 1))) {
                    // Remove one of them
                    newDistinctVarVec.remove(i);
                }
            }
        }
        return true;
    } // rebuildDistinct
}