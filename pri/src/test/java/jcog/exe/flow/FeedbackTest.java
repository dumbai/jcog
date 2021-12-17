package jcog.exe.flow;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class FeedbackTest {

    @Test
    void BufferedPriFeedbackModel1() {
        Feedback.BufferedPriFeedback<Integer,String> f = new Feedback.BufferedPriFeedback<>();
        Feedback.set(f);
        assertFeedbackExample_Simple(f.PRI);
    }

    @Test
    void PriFeedbackModel1() {
        Feedback.PriFeedback<Integer,String> f = new Feedback.PriFeedback<>();
        Feedback.set(f);
        assertFeedbackExample_Simple(f.PRI);
    }

    private static void assertFeedbackExample_Simple(Map<Integer,Map<String,Double>> PRI) {
        Feedback.start(1);
        Feedback.is("good");
        Feedback.is("good");
        Feedback.is("bad");
        Feedback.end();

        Feedback.start(2);
        Feedback.is("good");
        Feedback.is("bad");
        Feedback.is("bad");
        Feedback.end();

        assertEquals("{1={bad=1.0, good=2.0}, 2={bad=2.0, good=1.0}}", PRI.toString());
    }


}