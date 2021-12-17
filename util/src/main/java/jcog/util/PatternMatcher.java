package jcog.util;

import java.util.Arrays;

/**
 * A simple pattern matcher, which is safe to use on untrusted data: it does
 * not provide full reg-exp support, only simple globbing that can not be
 * used maliciously.
 * https:
 */
public class PatternMatcher {
    /**
     * Pattern type: the given pattern must exactly match the string it is
     * tested against.
     */
    public static final int PATTERN_LITERAL = 0;
    
    /**
     * Pattern type: the given pattern must match the
     * beginning of the string it is tested against.
     */
    public static final int PATTERN_PREFIX = 1;
    
    /**
     * Pattern type: the given pattern is interpreted with a
     * simple glob syntax for matching against the string it is tested against.
     * In this syntax, you can use the '*' character to match against zero or
     * more occurrences of the character immediately before.  If the
     * character before it is '.' it will match any character.  The character
     * '\' can be used as an escape.  This essentially provides only the '*'
     * wildcard part of a normal regexp. 
     */
    public static final int PATTERN_SIMPLE_GLOB = 2;
    /**
     * Pattern type: the given pattern is interpreted with a regular
     * expression-like syntax for matching against the string it is tested
     * against. Supported tokens include dot ({@code .}) and sets ({@code [...]})
     * with full support for character ranges and the not ({@code ^}) modifier.
     * Supported modifiers include star ({@code *}) for zero-or-more, plus ({@code +})
     * for one-or-more and full range ({@code {...}}) support. This is a simple
     * evaluation implementation in which matching is done against the pattern in
     * real time with no backtracking support.
     */
    public static final int PATTERN_ADVANCED_GLOB = 3;
    
    private static final int TOKEN_TYPE_LITERAL = 0;
    private static final int TOKEN_TYPE_ANY = 1;
    private static final int TOKEN_TYPE_SET = 2;
    private static final int TOKEN_TYPE_INVERSE_SET = 3;
    
    private static final int NO_MATCH = -1;
    private static final String TAG = "PatternMatcher";
    
    private static final int PARSED_TOKEN_CHAR_SET_START = -1;
    private static final int PARSED_TOKEN_CHAR_SET_INVERSE_START = -2;
    private static final int PARSED_TOKEN_CHAR_SET_STOP = -3;
    private static final int PARSED_TOKEN_CHAR_ANY = -4;
    private static final int PARSED_MODIFIER_RANGE_START = -5;
    private static final int PARSED_MODIFIER_RANGE_STOP = -6;
    private static final int PARSED_MODIFIER_ZERO_OR_MORE = -7;
    private static final int PARSED_MODIFIER_ONE_OR_MORE = -8;
    private final String mPattern;
    private final int mType;
    private final int[] mParsedPattern;
    private static final int MAX_PATTERN_STORAGE = 2048;
    
    private static final int[] sParsedPatternScratch = new int[MAX_PATTERN_STORAGE];
    public PatternMatcher(String pattern, int type) {
        mPattern = pattern;
        mType = type;
		mParsedPattern = mType == PATTERN_ADVANCED_GLOB ? parseAndVerifyAdvancedPattern(pattern) : null;
    }
    public final String getPath() {
        return mPattern;
    }
    
    public final int getType() {
        return mType;
    }
    
    public boolean match(String str) {
        return matchPattern(str, mPattern, mParsedPattern, mType);
    }
    public String toString() {
        String type = switch (mType) {
            case PATTERN_LITERAL -> "LITERAL: ";
            case PATTERN_PREFIX -> "PREFIX: ";
            case PATTERN_SIMPLE_GLOB -> "GLOB: ";
            case PATTERN_ADVANCED_GLOB -> "ADVANCED: ";
            default -> "? ";
        };
        return "PatternMatcher{" + type + mPattern + '}';
    }


    
    static boolean matchPattern(String match, String pattern, int[] parsedPattern, int type) {
        if (match == null) return false;
        if (type == PATTERN_LITERAL) {
            return pattern.equals(match);
        }
        return switch (type) {
            case PATTERN_PREFIX -> match.startsWith(pattern);
            case PATTERN_SIMPLE_GLOB -> matchGlobPattern(pattern, match);
            case PATTERN_ADVANCED_GLOB -> matchAdvancedPattern(parsedPattern, match);
            default -> false;
        };
    }
    static boolean matchGlobPattern(String pattern, String match) {
        int NP = pattern.length();
        if (NP <= 0) {
            return match.length() <= 0;
        }
        int NM = match.length();
        int ip = 0, im = 0;
        char nextChar = pattern.charAt(0);
        while ((ip<NP) && (im<NM)) {
            char c = nextChar;
            ip++;
            nextChar = ip < NP ? pattern.charAt(ip) : 0;
            boolean escaped = (c == '\\');
            if (escaped) {
                c = nextChar;
                ip++;
                nextChar = ip < NP ? pattern.charAt(ip) : 0;
            }
            if (nextChar == '*') {
                if (!escaped && c == '.') {
                    if (ip >= (NP-1)) {
                        
                        
                        return true;
                    }
                    ip++;
                    nextChar = pattern.charAt(ip);
                    
                    
                    if (nextChar == '\\') {
                        ip++;
                        nextChar = ip < NP ? pattern.charAt(ip) : 0;
                    }
                    do {
                        if (match.charAt(im) == nextChar) {
                            break;
                        }
                        im++;
                    } while (im < NM);
                    if (im == NM) {
                        
                        
                        return false;
                    }
                    ip++;
                    nextChar = ip < NP ? pattern.charAt(ip) : 0;
                    im++;
                } else {
                    
                    do {
                        if (match.charAt(im) != c) {
                            break;
                        }
                        im++;
                    } while (im < NM);
                    ip++;
                    nextChar = ip < NP ? pattern.charAt(ip) : 0;
                }
            } else {
                if (c != '.' && match.charAt(im) != c) return false;
                im++;
            }
        }
        
        if (ip >= NP && im >= NM) {
            
            return true;
        }
        
        
        
        
        return ip == NP - 2 && pattern.charAt(ip) == '.'
                && pattern.charAt(ip + 1) == '*';

    }
    /**
     * Parses the advanced pattern and returns an integer array representation of it. The integer
     * array treats each field as a character if positive and a unique token placeholder if
     * negative. This method will throw on any pattern structure violations.
     */
    static synchronized int[] parseAndVerifyAdvancedPattern(String pattern) {
        int ip = 0;
        int LP = pattern.length();
        int it = 0;
        boolean inSet = false;
        boolean inRange = false;
        boolean inCharClass = false;
        while (ip < LP) {
            if (it > MAX_PATTERN_STORAGE - 3) {
                throw new IllegalArgumentException("Pattern is too large!");
            }
            char c = pattern.charAt(ip);
            boolean addToParsedPattern = false;
            switch (c) {
                case '[':
                    if (inSet) {
                        addToParsedPattern = true; 
                    } else {
                        if (pattern.charAt(ip + 1) == '^') {
                            sParsedPatternScratch[it++] = PARSED_TOKEN_CHAR_SET_INVERSE_START;
                            ip++; 
                        } else {
                            sParsedPatternScratch[it++] = PARSED_TOKEN_CHAR_SET_START;
                        }
                        ip++; 
                        inSet = true;
                        continue;
                    }
                    break;
                case ']':
                    if (!inSet) {
                        addToParsedPattern = true; 
                    } else {
                        int parsedToken = sParsedPatternScratch[it - 1];
                        if (parsedToken == PARSED_TOKEN_CHAR_SET_START ||
                            parsedToken == PARSED_TOKEN_CHAR_SET_INVERSE_START) {
                            throw new IllegalArgumentException(
                                    "You must define characters in a setAt.");
                        }
                        sParsedPatternScratch[it++] = PARSED_TOKEN_CHAR_SET_STOP;
                        inSet = false;
                        inCharClass = false;
                    }
                    break;
                case '{':
                    if (!inSet) {
                        if (it == 0 || isParsedModifier(sParsedPatternScratch[it - 1])) {
                            throw new IllegalArgumentException("Modifier must follow a token.");
                        }
                        sParsedPatternScratch[it++] = PARSED_MODIFIER_RANGE_START;
                        ip++;
                        inRange = true;
                    }
                    break;
                case '}':
                    if (inRange) { 
                        sParsedPatternScratch[it++] = PARSED_MODIFIER_RANGE_STOP;
                        inRange = false;
                    }
                    break;
                case '*':
                    if (!inSet) {
                        if (it == 0 || isParsedModifier(sParsedPatternScratch[it - 1])) {
                            throw new IllegalArgumentException("Modifier must follow a token.");
                        }
                        sParsedPatternScratch[it++] = PARSED_MODIFIER_ZERO_OR_MORE;
                    }
                    break;
                case '+':
                    if (!inSet) {
                        if (it == 0 || isParsedModifier(sParsedPatternScratch[it - 1])) {
                            throw new IllegalArgumentException("Modifier must follow a token.");
                        }
                        sParsedPatternScratch[it++] = PARSED_MODIFIER_ONE_OR_MORE;
                    }
                    break;
                case '.':
                    if (!inSet) {
                        sParsedPatternScratch[it++] = PARSED_TOKEN_CHAR_ANY;
                    }
                    break;
                case '\\': 
                    if (ip + 1 >= LP) {
                        throw new IllegalArgumentException("Escape found at end of pattern!");
                    }
                    c = pattern.charAt(++ip);
                    addToParsedPattern = true;
                    break;
                default:
                    addToParsedPattern = true;
                    break;
            }
            if (inSet) {
                if (inCharClass) {
                    sParsedPatternScratch[it++] = c;
                    inCharClass = false;
                } else {
                    
                    if (ip + 2 < LP
                            && pattern.charAt(ip + 1) == '-'
                            && pattern.charAt(ip + 2) != ']') {
                        inCharClass = true;
                        sParsedPatternScratch[it++] = c; 
                        ip++; 
                    } else { 
                        sParsedPatternScratch[it++] = c; 
                        sParsedPatternScratch[it++] = c; 
                    }
                }
            } else if (inRange) {
                int endOfSet = pattern.indexOf('}', ip);
                if (endOfSet < 0) {
                    throw new IllegalArgumentException("Range not ended with '}'");
                }
                String rangeString = pattern.substring(ip, endOfSet);
                try {
                    int rangeMin;
                    int rangeMax;
                    int commaIndex = rangeString.indexOf(',');
                    if (commaIndex < 0) {
                        int parsedRange = Integer.parseInt(rangeString);
                        rangeMin = rangeMax = parsedRange;
                    } else {
                        rangeMin = Integer.parseInt(rangeString.substring(0, commaIndex));
						rangeMax = commaIndex == rangeString.length() - 1 ? Integer.MAX_VALUE : Integer.parseInt(rangeString.substring(commaIndex + 1));
                    }
                    if (rangeMin > rangeMax) {
                        throw new IllegalArgumentException(
                            "Range quantifier minimum is greater than maximum");
                    }
                    sParsedPatternScratch[it++] = rangeMin;
                    sParsedPatternScratch[it++] = rangeMax;
                } catch (NumberFormatException e) {
                    throw new IllegalArgumentException("Range number format incorrect", e);
                }
                ip = endOfSet;
                continue; 
            } else if (addToParsedPattern) {
                sParsedPatternScratch[it++] = c;
            }
            ip++;
        }
        if (inSet) {
            throw new IllegalArgumentException("Set was not terminated!");
        }
        return Arrays.copyOf(sParsedPatternScratch, it);
    }
    private static boolean isParsedModifier(int parsedChar) {
        return parsedChar == PARSED_MODIFIER_ONE_OR_MORE ||
                parsedChar == PARSED_MODIFIER_ZERO_OR_MORE ||
                parsedChar == PARSED_MODIFIER_RANGE_STOP ||
                parsedChar == PARSED_MODIFIER_RANGE_START;
    }
    static boolean matchAdvancedPattern(int[] parsedPattern, String match) {
        
        int ip = 0, im = 0;
        
        int LP = parsedPattern.length, LM = match.length();

        int charSetStart = 0, charSetEnd = 0;
        while (ip < LP) {
            int patternChar = parsedPattern[ip];

            int tokenType;
            switch (patternChar) {
                case PARSED_TOKEN_CHAR_ANY -> {
                    tokenType = TOKEN_TYPE_ANY;
                    ip++;
                }
                case PARSED_TOKEN_CHAR_SET_START, PARSED_TOKEN_CHAR_SET_INVERSE_START -> {
                    tokenType = patternChar == PARSED_TOKEN_CHAR_SET_START
                            ? TOKEN_TYPE_SET
                            : TOKEN_TYPE_INVERSE_SET;
                    charSetStart = ip + 1;
                    while (++ip < LP && parsedPattern[ip] != PARSED_TOKEN_CHAR_SET_STOP) ;
                    charSetEnd = ip - 1;
                    ip++;
                }
                default -> {
                    charSetStart = ip;
                    tokenType = TOKEN_TYPE_LITERAL;
                    ip++;
                }
            }
            int minRepetition;
            int maxRepetition;
            
            if (ip >= LP) {
                minRepetition = maxRepetition = 1;
            } else {
                patternChar = parsedPattern[ip];
                switch (patternChar) {
                    case PARSED_MODIFIER_ZERO_OR_MORE -> {
                        minRepetition = 0;
                        maxRepetition = Integer.MAX_VALUE;
                        ip++;
                    }
                    case PARSED_MODIFIER_ONE_OR_MORE -> {
                        minRepetition = 1;
                        maxRepetition = Integer.MAX_VALUE;
                        ip++;
                    }
                    case PARSED_MODIFIER_RANGE_START -> {
                        minRepetition = parsedPattern[++ip];
                        maxRepetition = parsedPattern[++ip];
                        ip += 2;
                    }
                    default -> minRepetition = maxRepetition = 1;
                }
            }
            if (minRepetition > maxRepetition) {
                return false;
            }
            
            int matched = matchChars(match, im, LM, tokenType, minRepetition, maxRepetition,
                    parsedPattern, charSetStart, charSetEnd);
            
            if (matched == NO_MATCH) {
                return false;
            }
            
            im += matched;
        }
        return ip >= LP && im >= LM; 
    }
    private static int matchChars(String match, int im, int lm, int tokenType,
            int minRepetition, int maxRepetition, int[] parsedPattern,
            int tokenStart, int tokenEnd) {
        int matched = 0;
        while(matched < maxRepetition
                && matchChar(match, im + matched, lm, tokenType, parsedPattern, tokenStart,
                    tokenEnd)) {
            matched++;
        }
        return matched < minRepetition ? NO_MATCH : matched;
    }
    private static boolean matchChar(String match, int im, int lm, int tokenType,
            int[] parsedPattern, int tokenStart, int tokenEnd) {
        if (im >= lm) { 
            return false;
        }
        switch (tokenType) {
            case TOKEN_TYPE_ANY:
                return true;
            case TOKEN_TYPE_SET:
                for (int i = tokenStart; i < tokenEnd; i += 2) {
                    char matchChar = match.charAt(im);
                    if (matchChar >= parsedPattern[i] && matchChar <= parsedPattern[i + 1]) {
                        return true;
                    }
                }
                return false;
            case TOKEN_TYPE_INVERSE_SET:
                for (int i = tokenStart; i < tokenEnd; i += 2) {
                    char matchChar = match.charAt(im);
                    if (matchChar >= parsedPattern[i] && matchChar <= parsedPattern[i + 1]) {
                        return false;
                    }
                }
                return true;
            case TOKEN_TYPE_LITERAL:
                return match.charAt(im) == parsedPattern[tokenStart];
            default:
                return false;
        }
    }
}