package jcog.grammar.parse;

import jcog.grammar.parse.tokens.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;


class GrammarTest {

    private Grammar grammar;
    private boolean called = false;

    @BeforeEach
    void init() {
        grammar = new Grammar("Test");
    }

    @Test
    void name() {
        assertEquals("Test", grammar.getName());
    }

    @Test
    void firstRuleIsStartRuleByDefault() {
        assertCheckFails();
        grammar.addRule("mystart", new Empty());
        grammar.check();
        assertTrue(grammar.parse("").isCompleteMatch());
    }

    private void assertCheckFails() {
        try {
            grammar.check();
            fail();
        } catch (GrammarException expected) {
        }
    }

    @Test
    void otherStartruleThanFirst() {
        grammar.addRule("nostart", new Empty());
        grammar.addRule("mystart", new RuleReference("nostart", grammar));
        grammar.markAsStartRule("mystart");
        grammar.check();
    }

    @Test
    void grammarWithSingleRule() {
        assertNull(grammar.getRule("mystart"));
        grammar.addRule("mystart", new Empty());
        assertTrue(grammar.getRule("mystart") instanceof Empty);

        assertTrue(grammar.parse("").isCompleteMatch());
        assertFalse(grammar.parse("hello").isCompleteMatch());
    }

    @Test
    void addingAssemblers() {
        class MyTarget implements PubliclyCloneable<MyTarget> {
            @Override
            public MyTarget clone() {
                return this;
            }
        }
        MyTarget target = new MyTarget();
        assertNull(grammar.getRule("mystart"));
        grammar.addRule("mystart", new Empty());
        grammar.addAssembler("mystart", (IAssembler) a -> a.setTarget(target));

        assertSame(target, grammar.parse("").getTarget());
    }


    @Test
    void assemblersCanOnlyBeAddedToExistingRules() {
        assertThrows(GrammarException.class, () -> grammar.addAssembler("mystart", (IAssembler) a -> {
        }));
    }

    @Test
    void resultStack() {
        grammar.addRule("mystart", new Literal("myliteral"));
        IParsingResult result = grammar.parse("myliteral");
        assertEquals(new Token("myliteral"), result.getStack().peek());
    }

    @Test
    void ruleReference() {
        grammar.addRule("mystart", new RuleReference("referenced", grammar));
        grammar.addRule("referenced", new Empty());
        assertTrue(grammar.parse("").isCompleteMatch());
        assertFalse(grammar.parse("something").isCompleteMatch());

        grammar.addRule("referenced", new Word());
        assertTrue(grammar.parse("Hello").isCompleteMatch());
        assertFalse(grammar.parse("12.34").isCompleteMatch());

        grammar.addRule("referenced", new Repetition(new CaselessLiteral("hello")));
        assertTrue(grammar.parse("HELLO hello HellO").isCompleteMatch());
        assertFalse(grammar.parse("hello 12.34").isCompleteMatch());

        Seq sequence = new Seq();
        sequence.get(new CaselessLiteral("hello"));
        sequence.get(new Num());
        grammar.addRule("referenced", sequence);
        assertTrue(grammar.parse("HELLO 12.34").isCompleteMatch());
        assertFalse(grammar.parse("hello you").isCompleteMatch());
    }

    @Test
    void ruleReferenceMustReferenceExistingClause() {
        grammar.addRule("mystart", new RuleReference("nothing", grammar));
        assertCheckFails();

        grammar.addRule("nothing", new Empty());
        grammar.check();
    }

    @Test
    void allClausesMustBeAccessibleFromStartParser() {
        grammar.addRule("mystart", new Empty());
        grammar.addRule("unused", new Empty());
        assertCheckFails();

        Seq startClause = new Seq();
        startClause.get(new Int());
        startClause.get(new RuleReference("unused", grammar));
        grammar.addRule("mystart", startClause);
        grammar.check();

        
        startClause.get(new Repetition(new RuleReference("mystart", grammar)));
        grammar.check();

        grammar.addRule("more unused", new Empty());
        assertCheckFails();
    }

    @Test
    void textualRules() {
        String ruleName = grammar.defineRule("command = \"go\"");
        assertEquals("command", ruleName);
        assertTrue(grammar.parse("go").isCompleteMatch());
    }


    @Test
    void textualRuleWithGroovyClosure() {
        List<Object> expectedMatches = new ArrayList<>();
        expectedMatches.add(new Token("test"));
        String ruleName = grammar.defineRule("mystart = \"test\"", (matches, stack) -> {
            assertEquals(expectedMatches, matches);
            assertTrue(stack.isEmpty());
            called = true;
        });
        assertEquals("mystart", ruleName);
        grammar.parse("test");
        assertTrue(called);
    }

    @Test
    void readFromReader() throws Exception {
        String text = "start = '<' more;\nmore = '>';";
        StringReader reader = new StringReader(text);
        grammar.addRulesFrom(reader);
        reader.close();
        assertTrue(grammar.parse("< >").isCompleteMatch());
    }

    @Test
    void leftRecursivenessCheckerIsPluggedIn() {
        assertThrows(GrammarException.class, () -> {
            grammar.defineRule("r = r '>'");
            grammar.check();
        });
    }


    @Test
    void complexScenario() {
        defineTrackRobotGrammar();
        String[] sentences = {"pick carrier from LINE_IN", "place carrier at DB101_IN", "scan DB101_OUT"};
        for (String sentence : sentences) {
            assertTrue(grammar.parse(sentence).isCompleteMatch());
        }
        StringWriter sw = new StringWriter();
        grammar.printOn(new PrintWriter(sw));
        System.out.println(sw);
    }

    @Test
    void performanceTest() {
        defineTrackRobotGrammar();
        grammar.check(); 
        String[] sentences = {"pick carrier from LINE_IN", "place carrier at DB101_IN", "scan DB101_OUT"};
        long before = System.currentTimeMillis();
        for (int i = 0; i < 1000; i++) {
            for (String sentence : sentences) {
                assertTrue(grammar.parse(sentence).isCompleteMatch());
            }
        }
        long after = System.currentTimeMillis();
        
    }

    @Test
    void defaultTerminalTypes() {
        assertTrue(grammar.terminal("Num") instanceof Num);
        assertTrue(grammar.terminal("Int") instanceof Int);
        assertTrue(grammar.terminal("Word") instanceof Word);
        assertTrue(grammar.terminal("QuotedString") instanceof QuotedString);
    }

    @Test
    void registeringTerminalTypes() {
        grammar.registerTerminal(UpperCaseWord.class);
        grammar.registerTerminal("UCW", UpperCaseWord.class);
        assertTrue(grammar.terminal("UpperCaseWord") instanceof UpperCaseWord);
        assertTrue(grammar.terminal("UCW") instanceof UpperCaseWord);
    }

    @Test
    void automaticConstantsDiscard() {
        assertFalse(grammar.areAllConstantsDiscarded());
        grammar.discardAllConstants();
        assertTrue(grammar.areAllConstantsDiscarded());
    }

    private void defineTrackRobotGrammar() {
        
        Alternation command = new Alternation();
        command.get(new RuleReference("pickCommand", grammar));
        command.get(new RuleReference("placeCommand", grammar));
        command.get(new RuleReference("scanCommand", grammar));
        grammar.addRule("command", command);

        Seq pickCommand = new Seq();
        pickCommand.get(new CaselessLiteral("pick"));
        pickCommand.get(new CaselessLiteral("carrier"));
        pickCommand.get(new CaselessLiteral("from"));
        pickCommand.get(new RuleReference("location", grammar));
        grammar.addRule("pickCommand", pickCommand);

        Seq placeCommand = new Seq();
        placeCommand.get(new CaselessLiteral("place"));
        placeCommand.get(new CaselessLiteral("carrier"));
        placeCommand.get(new CaselessLiteral("at"));
        placeCommand.get(new RuleReference("location", grammar));
        grammar.addRule("placeCommand", placeCommand);

        Seq scanCommand = new Seq();
        scanCommand.get(new CaselessLiteral("scan"));
        scanCommand.get(new RuleReference("location", grammar));
        grammar.addRule("scanCommand", scanCommand);

        grammar.addRule("location", new Word());
        grammar.check();
    }

    
    
    
    
    
    
    
    
    
    
    
    

}

















































