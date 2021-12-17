













package jcog.grammar.synthesize.util;

import java.util.*;

public enum CharacterUtils {
	;

	public static boolean isNewlineOrTabCharacter(char c) {
        return c == '\n' || c == '\t';
    }

    public static boolean isNumeric(char c) {
        return c >= '0' && c <= '9';
    }

    public static boolean isAlphaUpperCase(char c) {
        return c >= 'A' && c <= 'Z';
    }

    public static boolean isAlphaLowerCase(char c) {
        return c >= 'a' && c <= 'z';
    }

    public static boolean isAlpha(char c) {
        return isAlphaUpperCase(c) || isAlphaLowerCase(c);
    }

    public static boolean isAlphaNumeric(char c) {
        return isAlpha(c) || isNumeric(c);
    }

    public static boolean isNonAlphaNumeric(char c) {
        return !isNumeric(c) && !isAlphaUpperCase(c) && !isAlphaLowerCase(c);
    }

    public static boolean isSingleQuote(char c) {
        return c == 39;
    }

    public static boolean isDoubleQuote(char c) {
        return c == 34;
    }

    public static class CharacterGeneralization {
        public final Set<Character> triggers;
        public final List<Character> characters;
        public final List<Character> checks;

        public CharacterGeneralization(Collection<Character> triggers, Collection<Character> characters, Collection<Character> checks) {
            this.triggers = new HashSet<>(triggers);
            this.characters = new ArrayList<>(characters);
            this.checks = new ArrayList<>(checks);
        }
    }

    private static final List<Character> allCharacters = new ArrayList<>();
    private static final List<Character> numericCharacters = new ArrayList<>();
    private static final List<Character> alphaUpperCaseCharacters = new ArrayList<>();
    private static final List<Character> alphaLowerCaseCharacters = new ArrayList<>();
    private static final List<Character> nonAlphaNumericCharacters = new ArrayList<>();
    private static final List<Character> numericChecks = new ArrayList<>();
    private static final List<Character> alphaUpperCaseChecks = new ArrayList<>();
    private static final List<Character> alphaLowerCaseChecks = new ArrayList<>();
    private static final List<CharacterGeneralization> generalizations = new ArrayList<>();

    static {
        for (char c = 0; c < 128; c++) {
            allCharacters.add(c);
            if (isNumeric(c)) {
                numericCharacters.add(c);
            } else if (isAlphaUpperCase(c)) {
                alphaUpperCaseCharacters.add(c);
            } else if (isAlphaLowerCase(c)) {
                alphaLowerCaseCharacters.add(c);
            } else {
                nonAlphaNumericCharacters.add(c);
            }
        }
        numericChecks.add('0');
        numericChecks.add('1');
        numericChecks.add('9');
        alphaUpperCaseChecks.add('E');
        alphaUpperCaseChecks.add('Q');
        alphaLowerCaseChecks.add('e');
        alphaLowerCaseChecks.add('q');
        for (char c : nonAlphaNumericCharacters) {
            List<Character> curC = GrammarUtils.getList(c);
            generalizations.add(new CharacterGeneralization(numericCharacters, curC, curC));
            generalizations.add(new CharacterGeneralization(alphaLowerCaseCharacters, curC, curC));
            generalizations.add(new CharacterGeneralization(alphaUpperCaseCharacters, curC, curC));
        }
        generalizations.add(new CharacterGeneralization(numericCharacters, numericCharacters, numericChecks));
        generalizations.add(new CharacterGeneralization(numericCharacters, alphaUpperCaseCharacters, alphaUpperCaseChecks));
        generalizations.add(new CharacterGeneralization(numericCharacters, alphaLowerCaseCharacters, alphaLowerCaseChecks));
        generalizations.add(new CharacterGeneralization(alphaUpperCaseCharacters, numericCharacters, numericChecks));
        generalizations.add(new CharacterGeneralization(alphaUpperCaseCharacters, alphaUpperCaseCharacters, alphaUpperCaseChecks));
        generalizations.add(new CharacterGeneralization(alphaUpperCaseCharacters, alphaLowerCaseCharacters, alphaLowerCaseChecks));
        generalizations.add(new CharacterGeneralization(alphaLowerCaseCharacters, numericCharacters, numericChecks));
        generalizations.add(new CharacterGeneralization(alphaLowerCaseCharacters, alphaUpperCaseCharacters, alphaUpperCaseChecks));
        generalizations.add(new CharacterGeneralization(alphaLowerCaseCharacters, alphaLowerCaseCharacters, alphaLowerCaseChecks));
    }

    public static List<Character> getAllCharacters() {
        return allCharacters;
    }

    public static List<Character> getNumericCharacters() {
        return numericCharacters;
    }

    public static List<Character> getAlphaUpperCaseCharacters() {
        return alphaUpperCaseCharacters;
    }

    public static List<Character> getAlphaLowerCaseCharacters() {
        return alphaLowerCaseCharacters;
    }

    public static List<Character> getNonAlphaNumericCharacters() {
        return nonAlphaNumericCharacters;
    }

    public static List<Character> getNumericChecks() {
        return numericChecks;
    }

    public static List<Character> getAlphaUpperCaseChecks() {
        return alphaUpperCaseChecks;
    }

    public static List<Character> getAlphaLowerCaseChecks() {
        return alphaLowerCaseChecks;
    }

    public static Iterable<CharacterGeneralization> getGeneralizations() {
        return generalizations;
    }
}
