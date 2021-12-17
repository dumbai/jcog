/*
 * Here comes the text of your license
 * Each line should be prefixed with  *
 */
package jcog.io;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.lang.Integer.compare;
import static java.lang.System.in;
import static java.lang.System.out;
import static java.util.Arrays.asList;
import static java.util.Collections.sort;
import static java.util.regex.Pattern.compile;

/**
 * CUSTOMIZED FROM:
 * https:
 * <p/>
 * Twokenize -- a tokenizer designed for Twitter text in English and some other
 * European languages. This is the Java version. If you want the old Python
 * version, see: http:
 * <p/>
 * This tokenizer code has gone through a long history:
 * <p/>
 * (1) Brendan O'Connor wrote original version in Python,
 * http:
 * Topic Summarization for Twitter. Brendan O'Connor, Michel Krieger, and David
 * Ahn. ICWSM-2010 (demo track),
 * http:
 * Gimpel and Daniel Mills modified it for POS tagging for the CMU ARK Twitter
 * POS Tagger (2b) Jason Baldridge and David Snyder ported it to Scala (3)
 * Brendan bugfixed the Scala port and merged with POS-specific changes for the
 * CMU ARK Twitter POS Tagger (4) Tobi Owoputi ported it back to Java and added
 * many improvements (2012-06)
 * <p/>
 * Current home is http:
 * http:
 * <p/>
 * There have been at least 2 other Java ports, but they are not in the lineage
 * for the code here.
 */
public enum Twokenize {
	;

	private static final String word = "[\\p{Alpha}]+";
	private static final String punctChars = "['\"“”‘’.?!…,:;]";
	private static final String entity = "&(?:amp|lt|gt|quot);";
	// BTO 2012-06: everyone thinks the daringfireball regex should be better, but they're wrong.
	// If you actually empirically test it the results are bad.
	// Please see https://github.com/brendano/ark-tweet-nlp/pull/9
	private static final String urlStart1 = "(?:https?://|\\bwww\\.)";
	private static final String commonTLDs = "(?:com|org|edu|gov|net|mil|aero|asia|biz|cat|coop|info|int|jobs|mobi|museum|name|pro|tel|travel|xxx)";
	private static final String ccTLDs = "(?:ac|ad|ae|af|ag|ai|al|am|an|ao|aq|ar|as|at|au|aw|ax|az|ba|bb|bd|be|bf|bg|bh|bi|bj|bm|bn|bo|br|bs|bt|"
		+ "bv|bw|by|bz|ca|cc|cd|cf|cg|ch|ci|ck|cl|cm|cn|co|cr|cs|cu|cv|cx|cy|cz|dd|de|dj|dk|dm|do|dz|ec|ee|eg|eh|"
		+ "er|es|et|eu|fi|fj|fk|fm|fo|fr|ga|gb|gd|ge|gf|gg|gh|gi|gl|gm|gn|gp|gq|gr|gs|gt|gu|gw|gy|hk|hm|hn|hr|ht|"
		+ "hu|id|ie|il|im|in|io|iq|ir|is|it|je|jm|jo|jp|ke|kg|kh|ki|km|kn|kp|kr|kw|ky|kz|la|lb|lc|li|lk|lr|ls|lt|"
		+ "lu|lv|ly|ma|mc|md|me|mg|mh|mk|ml|mm|mn|mo|mp|mq|mr|ms|mt|mu|mv|mw|mx|my|mz|na|nc|ne|nf|ng|ni|nl|no|np|"
		+ "nr|nu|nz|om|pa|pe|pf|pg|ph|pk|pl|pm|pn|pr|ps|pt|pw|py|qa|re|ro|rs|ru|rw|sa|sb|sc|sd|se|sg|sh|si|sj|sk|"
		+ "sl|sm|sn|so|sr|ss|st|su|sv|sy|sz|tc|td|tf|tg|th|tj|tk|tl|tm|tn|to|tp|tr|tt|tv|tw|tz|ua|ug|uk|us|uy|uz|"
		+ "va|vc|ve|vg|vi|vn|vu|wf|ws|ye|yt|za|zm|zw)";    //TODO: remove obscure country domains?
	private static final String urlStart2 = "\\b(?:[A-Za-z\\d-])+(?:\\.[A-Za-z0-9]+){0,3}\\." + "(?:" + commonTLDs + '|' + ccTLDs + ')' + "(?:\\." + ccTLDs + ")?(?=\\W|$)";
	private static final String urlBody = "(?:[^.\\s<>][^\\s<>]*?)?";
	private static final String urlExtraCrapBeforeEnd = "(?:" + punctChars + '|' + entity + ")+?";
	private static final String urlEnd = "(?:\\.\\.+|[<>]|\\s|$)";
	private static final String boundaryNotDot = "(?:$|\\s|[“\\u0022?!,:;]|" + entity + ')';
	private static final String aa1 = "(?:[A-Za-z]\\.){2,}(?=" + boundaryNotDot + ')';
	private static final String aa2 = "[^A-Za-z](?:[A-Za-z]\\.){1,}[A-Za-z](?=" + boundaryNotDot + ')';
	private static final String standardAbbreviations = "\\b(?:[Mm]r|[Mm]rs|[Mm]s|[Dd]r|[Ss]r|[Jj]r|[Rr]ep|[Ss]en|[Ss]t)\\.";
	private static final String normalEyes = "(?iu)[:=]";
	private static final String wink = "[;]";
	private static final String noseArea = "(?:|-|[^a-zA-Z0-9 ])";
	private static final String happyMouths = "[D\\)\\]\\}]+";
	private static final String sadMouths = "[\\(\\[\\{]+";
	private static final String tongue = "[pPd3]+";
	private static final String otherMouths = "(?:[oO]+|[/\\\\]+|[vV]+|[Ss]+|[|]+)";
	private static final String bfLeft = "(♥|0|o|°|v|\\$|t|x|;|\\u0CA0|@|ʘ|•|・|◕|\\^|¬|\\*)";
	private static final String bfCenter = "(?:[\\.]|[_-]+)";
	private static final String bfRight = "\\2";
	private static final String s3 = "(?:--['\"])";
	private static final String s4 = "(?:<|&lt;|>|&gt;)[\\._-]+(?:<|&lt;|>|&gt;)";
	private static final String s5 = "(?:[.][_]+[.])";
	private static final String basicface = "(?:(?i)" + bfLeft + bfCenter + bfRight + ")|" + s3 + '|' + s4 + '|' + s5;
	private static final String eeLeft = "[＼\\\\ƪԄ\\(（<>;ヽ\\-=~\\*]+";
	private static final String eeRight = "[\\-=\\);'\\u0022<>ʃ）/／ノﾉ丿╯σっµ~\\*]+";
	private static final String eeSymbol = "[^A-Za-z0-9\\s\\(\\)\\*:=-]";
	private static final String eastEmote = eeLeft + "(?:" + basicface + '|' + eeSymbol + ")+" + eeRight;
	private static final String Bound = "(?:\\W|^|$)";
	private static final String edgePunctChars = "'\"“”‘’«»{}\\(\\)\\[]\\*&";
	private static final String edgePunct = '[' + edgePunctChars + ']';
	private static final String notEdgePunct = "[a-zA-Z0-9]";
	private static final String offEdge = "(^|$|:|;|\\s|\\.|,)";
	private static final Pattern EdgePunctLeft = compile(offEdge + '(' + edgePunct + "+)(" + notEdgePunct + ')');
	private static final Pattern EdgePunctRight = compile('(' + notEdgePunct + ")(" + edgePunct + "+)" + offEdge);
	private static String url = "(?:" + urlStart1 + '|' + urlStart2 + ')' + urlBody + "(?=(?:" + urlExtraCrapBeforeEnd + ")?" + urlEnd + ')';
	private static String emoticon = OR(

		"(?:>|&gt;)?" + OR(normalEyes, wink) + OR(noseArea, "[Oo]")
			+ OR(tongue + "(?=\\W|$|RT|rt|Rt)", otherMouths + "(?=\\W|$|RT|rt|Rt)", sadMouths, happyMouths),


		"(?<=(?: |^))" + OR(sadMouths, happyMouths, otherMouths) + noseArea + OR(normalEyes, wink) + "(?:<|&lt;)?",

		eastEmote.replaceFirst("2", "1"), basicface


	);
	private static String Email = "(?<=" + Bound + ")[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,4}(?=" + Bound + ')';
	private static Pattern Contractions = compile("(?i)(\\w+)(n['’′]t|['’′]ve|['’′]ll|['’′]d|['’′]re|['’′]s|['’′]m)$");
	private static Pattern Whitespace = compile("[\\s\\p{Zs}]+");
	private static String punctSeq = "['\"“”‘’]+|[.?!,…]+|[:;]+";
	private static String timeLike = "\\d+(?::\\d+){1,2}";
	private static String numNum = "\\d+[.\\d+]";
	static String numberWithCommas = "(?:(?<!\\d)\\d{1,3},)+?\\d{3}" + "(?=(?:[^,\\d]|$))";
	private static String numComb = "\\p{Sc}?\\d+(?:\\.\\d+)+%?";
	private static String arbitraryAbbrev = "(?:" + aa1 + '|' + aa2 + '|' + standardAbbreviations + ')';
	private static String separators = "(?:--+|―|—|~|–|=)";
	private static String decorations = "(?:[♫♪]+|[★☆]+|[♥❤♡]+|[\\u2639-\\u263b]+|[\\ue001-\\uebbb]+)";
	static String thingsThatSplitWords = "[^\\s\\.,?\"]";
	private static String Hearts = "(?:<+/?3+)+";
	/*));

	 */
	private static String Arrows = "(?:<*[-―—=]*>+|<+[-―—=]*>*)|\\p{InArrows}+";
	private static String Hashtag = "#[a-zA-Z0-9_]+";
	private static String AtMention = "[@＠][a-zA-Z0-9_]+";
	private static String embeddedApostrophe = word + "[''']" + word;
	private static Map<String, Pattern> patterns = new HashMap() {
		{
            /*  Pattern.compile(
             OR(*/


			put("word", compile(word));
			put("hearticon", compile(Hearts));
			put("url", compile(url));
			put("email", compile(Email));
			put("temporal", compile(timeLike));
			put("numNum", compile(numNum));
			put("numComb", compile(numComb));
			put("emoticon", compile(emoticon));
			put("arrows", compile(Arrows));
			put("entity", compile(entity));
			put("punct", compile(punctSeq));
			put("abbrev", compile(arbitraryAbbrev));
			put("separator", compile(separators));
			put("decoration", compile(decorations));
			put("apostrophe", compile(embeddedApostrophe));
			put("hashtag", compile(Hashtag));
			put("mention", compile(AtMention));
		}
	};

	private static String OR(String... parts) {
		String prefix = "(?:";
		StringBuilder sb = new StringBuilder();
		for (String s : parts) {
			sb.append(prefix);
			prefix = "|";
			sb.append(s);
		}
		sb.append(')');
		return sb.toString();
	}

	private static String splitEdgePunct(String input) {
		Matcher m1 = EdgePunctLeft.matcher(input);
		input = m1.replaceAll("$1$2 $3");
		m1 = EdgePunctRight.matcher(input);
		input = m1.replaceAll("$1 $2$3");
		return input;
	}

	private static List<Span> simpleTokenize(String text) {


		String splitPunctText = splitEdgePunct(text);


		List<Span> spans = new ArrayList<>();

		for (Entry<String, Pattern> p : patterns.entrySet()) {
			Matcher matches = p.getValue().matcher(splitPunctText);
			while (matches.find()) {
                int ms = matches.start();
                int me = matches.end();
                if (ms != me) {
					spans.add(
						new Span(splitPunctText.substring(ms, me), p.getKey(), ms, me));
				}
			}
		}

		Collections.sort(spans);
		return spans;


	}

	private static List<Pair<String, Object>> addAllnonempty(List<Pair<String, Object>> master, List<Pair<String, Object>> smaller) {
		for (Pair<String, Object> s : smaller) {
			String strim = s.first.trim();
			if (!strim.isEmpty()) {
				s.first = strim;
				master.add(s);
			}
		}
		return master;
	}

	/**
	 * "foo bar " => "foo bar"
	 */
	private static String squeezeWhitespace(String input) {
		return Whitespace.matcher(input).replaceAll(" ").trim();
	}

	private static List<String> splitToken(String token) {

		Matcher m = Contractions.matcher(token);
		if (m.find()) {
			String[] contract = {m.group(1), m.group(2)};
			return asList(contract);
		}
		String[] contract = {token};
		return asList(contract);
	}

	/**
	 * Assume 'text' has no HTML escaping. *
	 */
	public static List<Span> tokenize(String text) {
		List<Span> l = simpleTokenize(squeezeWhitespace(text));

//		Set<Span> hidden = new HashSet(l.size());

//		for (Span a : l) {
//			if (hidden.contains(a)) continue;
//			for (Span b : l) {
//				if (hidden.contains(b)) continue;
//				if (a.contains(b))
//					hidden.add(b);
//				else if (b.contains(a)) {
//					hidden.add(a);
//					break;
//				}
//
//			}
//		}
//
//		l.removeAll(hidden);
		return l;
	}

	/**
	 * Twitter text comes HTML-escaped, so unescape it. We also first unescape
	 * &amp;'s, in case the text has been buggily double-escaped.
	 */
	private static String normalizeTextForTagger(String text) {
		return text.replaceAll("&amp;", "&");
	}

	/**
	 * This is intended for raw tweet text -- we do some HTML entity unescaping
	 * before running the tagger.
	 * <p/>
	 * This function normalizes the input text BEFORE calling the tokenizer. So
	 * the tokens you get back may not exactly correspond to substrings of the
	 * original text.
	 */
	public static List<Span> twokenize(String text) {
		List<Span> sp = tokenize(normalizeTextForTagger(text));
		sort(sp);
		return sp;
	}

	/**
	 * Tokenizes tweet texts on standard input, tokenizations on standard
	 * output. Input and output UTF-8.
	 */
	public static void main(String[] args) throws IOException {
		BufferedReader input = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));
		PrintStream output = new PrintStream(out, true, StandardCharsets.UTF_8);
		String line;
		while ((line = input.readLine()) != null) {
			List<Span> toks = twokenize(line);
			for (int i = 0; i < toks.size(); i++) {
				output.print(toks.get(i));
				if (i < toks.size() - 1) {
					output.print(" ");
				}
			}
			output.print("\n");
		}
	}

	private static class Pair<T1, T2> {

		T1 first;
		T2 second;

		Pair(T1 x, T2 y) {
			first = x;
			second = y;
		}

		@Override
		public String toString() {
			return "(" + first + ',' + second + ')';
		}

	}

	public static class Span implements Comparable<Span> {
		public final String content;
		public final String pattern;
		final int start;
		final int end;
//		public final int length;

		Span(String content, String pattern, int start, int end) {
			this.content = content;
			this.pattern = pattern;
			this.start = start;
			this.end = end;
//			length = stop - start;
		}

		@Override
		public boolean equals(Object obj) {
			Span t = (Span) obj;
			return start != t.start && end != t.end;
		}

		@Override
		public int compareTo(Span t) {
			return compare(start, t.start);
		}

		@Override
		public String toString() {
			return '(' + content + ',' + pattern + ')';
		}

		public int length() { return end - start; }

		private boolean contains(Span b) {
			return (b.start >= start) && (b.end <= end);
		}
	}

}