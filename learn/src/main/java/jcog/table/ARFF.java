/*
 * Copyright (c) 2008, Mikio L. Braun, Cheng Soon Ong, Soeren Sonnenburg
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 *   * Redistributions of source code must retain the above copyright
 *   notice, this list of conditions and the following disclaimer.
 *
 *   * Redistributions in binary form must reproduce the above
 *   copyright notice, this list of conditions and the following
 *   disclaimer in the documentation and/or other materials provided
 *   with the distribution.
 *
 *   * Neither the names of the Technical University of Berlin, ETH
 *   Zürich, or Fraunhofer FIRST nor the names of its contributors may
 *   be used to endorse or promote products derived from this software
 *   without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS
 * FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
 * COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package jcog.table;

import com.google.common.primitives.Primitives;
import jcog.Str;
import jcog.TODO;
import jcog.data.list.Lst;
import jcog.util.Reflect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.tablesaw.api.ColumnType;
import tech.tablesaw.api.Row;
import tech.tablesaw.api.Table;
import tech.tablesaw.columns.Column;
import tech.tablesaw.columns.numbers.DoubleColumnType;
import tech.tablesaw.columns.strings.StringColumnType;

import java.io.*;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.IntStream;


/**
 * https:
 *
 * <p>A class for reading and writing Arff-Files.</p>
 *
 * <p>You can either load a file, parse a string or a BufferedReader. Afterwards, you
 * can extract the information with the methods getComment(), getNumberOfAttributes(),
 * getAttributeName(), getAttributeType(), getAttributeData().</p>
 *
 * <p>Alternative, you can construct an empty ArffFile object, and then use setComment(),
 * defineAttribute(), addAt() to fill in the data and then save to a file with save(), or
 * to a string with write().</p>
 *
 * <p>The first comment in an ArffFile is extracted and made available through the *Comment()
 * accessors. Usually, this comment contains some interesting information about the data setAt.</p>
 *
 * <p>Currently, the class only supports numerical, string and nominal attributes. It also does
 * not support sparse storage (yet).</p>
 *
 * @author Mikio L. Braun, mikio@cs.tu-berlin.de
 * <p>
 * https:
 * <p>
 * https:
 * https:
 * TODO rewrite as output strategy for Schema
 */
@Deprecated public class ARFF extends DataTable {


    static final String NEW_LINE = System.getProperty("line.separator");
    private static final int COMMENT = 0;
    private static final int HEADER = 1;
    private static final int DATA = 2;
    /**
     * data 'rows'
     * TODO abstract this to different underlying data model
     */

    static final double valueIfNull =
            //Double.NaN;
            0;

    private String relation;
    private String comment;

//    public final Collection<ImmutableList> data;

    public ARFF(ARFF copyMetadataFrom) {
        super(copyMetadataFrom);
        this.relation = copyMetadataFrom.relation;
        this.comment = copyMetadataFrom.comment;
    }

    public ARFF(DataTable copyMetadataFrom) {
        super(copyMetadataFrom);
        this.relation = this.comment = "";
    }

    public ARFF(String l) throws IOException, ARFFParseError {
        this(new BufferedReader(new StringReader(l)));
    }




    public ARFF() {
        super();
        relation = "data";
        comment = null;
    }

    public ARFF(File f) throws Exception {
        this(new BufferedReader(new FileReader(f)));
    }

    /**
     * Parse an ArffFile from a BufferedReader.
     */
    public ARFF(BufferedReader r) throws IOException, ARFFParseError {
        this();
        int[] state = {COMMENT};

        StringBuilder collectedComment = new StringBuilder();

        String line;
        int lineno = 0;
        while ((line = r.readLine()) != null) {
            readLine(++lineno, state, line.trim(), collectedComment);
        }
        this.comment = collectedComment.toString();
    }

//    /**
//     * Formats an array of Objects in the passed StringBuilder using toString()
//     * and using del as the delimiter.
//     * <p>
//     * For example, on <tt>objects = { 1, 2, 3 }</tt>, and <tt>del = " + "</tt>, you get
//     * <tt>"1 + 2 + 3"</tt>.
//     */
//    private static void joinWith(Object[] objects, Appendable s, CharSequence del) throws IOException {
//        boolean first = true;
//        for (Object o : objects) {
//            if (!first) {
//                s.append(del);
//            }
//            s.append(o.toString());
//            first = false;
//        }
//    }

//    private static void joinWith(ImmutableList objects, Appendable s, CharSequence del) throws IOException {
//        boolean first = true;
//        for (Object o : objects) {
//            if (!first)
//                s.append(del);
//
//            String oo = o!=null ? o.toString() : "null";
//
//            s.append(o instanceof Number ? oo : quoteIfNecessary(oo));
//
//            first = false;
//        }
//    }
private static void joinWith(Row r, Appendable s, CharSequence del) throws IOException {
    boolean first = true;
    for (int i = 0; i < r.columnCount(); i++) {

        Object o = r.getObject(i);
        if (!first)
            s.append(del);

        String oo = o!=null ? o.toString() : "null";

        s.append(o instanceof Number ? oo : quoteIfNecessary(oo));

        first = false;
    }
}
    static boolean isQuoteNecessary(CharSequence t) {
        int len = t.length();

        if (len > 1 && t.charAt(0) == '\"' && t.charAt(len - 1) == '\"')
            return false; 

        for (int i = 0; i < len; i++) {
            char x = t.charAt(i);
            switch (x) {
                case ' ':
                case '.':
                case '+':
                case '-':
                case ',':
                    return true;

            }
        }

        return false;
    }

    public Table clone() {
        throw new TODO();
    }

    private void readLine(int lineNum, int[] state, String line, StringBuilder collectedComment) throws ARFFParseError {
        int ll = line.length();
        switch (state[0]) {
            case COMMENT:
                if (ll > 1 && line.charAt(0) == '%') {
                    collectedComment.append(line.substring(2));
                    collectedComment.append(NEW_LINE);
                } else {
                    state[0] = HEADER;
                    readLine(lineNum, state, line, collectedComment);
                }
                break;
            case HEADER:
                String lowerline = line.toLowerCase();
                if (lowerline.startsWith("@relation")) {
                    readRelationDefinition(line);
                } else if (lowerline.startsWith("@attribute")) {
                    try {
                        readAttributeDefinition(lineNum, line);
                    } catch (ARFFParseError e) {
                        System.err.println("Warning: " + e.getMessage());
                    }
                } else if (lowerline.startsWith("@data")) {
                    state[0] = DATA;
                }
                break;

            case DATA:
                if (ll > 0 && line.charAt(0) != '%')
                    parseData(lineNum, line);
                break;
        }
    }

    private void readRelationDefinition(String line) {
        int i = line.indexOf(' ');
        relation = line.substring(i + 1);
    }


    private void readAttributeDefinition(int lineno, String line) throws ARFFParseError {
        Scanner s = new Scanner(line);
        Pattern p = Pattern.compile("[a-zA-Z_][a-zA-Z0-9_]*|\\{[^}]+}|'[^']+'|\"[^\"]+\"");
        String keyword = s.findInLine(p);
        String name = s.findInLine(p);
        String type = s.findInLine(p);

        if (name == null || type == null) {
            throw new ARFFParseError(lineno, "Attribute definition cannot be parsed");
        }

        String lowertype = type.toLowerCase();

        if (List.of("real", "numeric", "integer").contains(lowertype)) {
            defineNumeric(name);
        } else if ("string".equals(lowertype)) {
            defineText(name);
        } else  {
            int a = line.indexOf('{');
            if (a != -1) {
                int b = line.indexOf('}');
                if (b != -1) {
                    line = line.substring(a+1, b);
                    defineNominal(name, line.split("\\s*,\\s*"));
                    return;
                }
            }
            throw new ARFFParseError(lineno, "Attribute of type \"" + type + "\" not supported (yet)");
        }
    }

    private void parseData(int lineno, String line) throws ARFFParseError {
        int num_attributes = columnCount();


//        if (line.charAt(0) == '{' && line.charAt(line.length() - 1) == '}') {
//            throw new ARFFParseError(lineno, "Sparse data not supported (yet).");
//        } else {
        {
            String[] tokens = line.split(",");
            if (tokens.length != num_attributes) {
                throw new ARFFParseError(lineno, "Warning: line " + lineno + " does not contain the right " +
                        "number of elements (should be " + num_attributes + ", got " + tokens.length + ".\n\t" + line);
            }

            Object[] datum = new Object[num_attributes];
            for (int i = 0; i < num_attributes; i++) {
                
                //String name = attrName(i);
                ColumnType t = column(i).type();
                if (t == DoubleColumnType.instance()) {
					datum[i] = tokens[i] == null || "null".equals(tokens[i]) ? valueIfNull : Double.parseDouble(tokens[i]);
                } else if (t == StringColumnType.instance()) {
                    datum[i] = tokens[i];
                } else {
//                    if (!isNominalValueValid(name, tokens[i])) {
//                        throw new ARFFParseError(lineno, "Undefined nominal value \"" +
//                                tokens[i] + "\" for field " + name + ".");
//                    }
//                    datum[i] = tokens[i];
                    throw new TODO();
                }
            }

            add(datum);
        }
    }

    public void saveOnShutdown(String file) {
        Runtime.getRuntime().addShutdownHook(new Thread(()->{
            try {
                writeToFile(file);
                System.out.println("saved " + rowCount() + " experiment results to: " + file);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }));
    }

    private boolean isNominalValueValid(String name, String token) {
        switch (token) {
            case "?":
            case "_":
                return true;
        }

        String[] values = categories(name);
        return Arrays.asList(values).contains(token);
    }

//    /**
//     * Generate a string which describes the data setAt.
//     */
//    public StringBuilder describe() {
//        StringBuilder s = new StringBuilder();
//
//        try {
//            s.append("Relation " + relation).append(NEW_LINE).append("with attributes").append(NEW_LINE);
//            for (String n : columnNames()) {
//                ColumnType at = attrType(n);
//                s.append("   " + n + " of type " + at);
//                if (at instanceof NominalColumnType) {
//                    s.append(" with values ");
//                    joinWith(nominalCats.get(n), s, ", ");
//                }
//                s.append(NEW_LINE);
//            }
//
//            s.append(NEW_LINE).append("Data (first 10 lines of " + data.size() + "):").append(NEW_LINE);
//
//            int i = 0;
//            for (ImmutableList row : data) {
//
//                joinWith(row, s, ", ");
//                s.append(NEW_LINE);
//                if (i++ > 10) break;
//            }
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        }
//        return s;
//    }

    /**
     * Write the ArffFile to a string.
     */
    public void write(Appendable s) throws IOException {

        if (comment != null) {
            s.append("% ").append(comment.replaceAll(NEW_LINE, NEW_LINE + "% ")).append(NEW_LINE);
        }

        s.append("@relation ").append(relation).append(NEW_LINE);

        List<String> columnNames = columnNames();
        for (int i = 0, columnNamesSize = columnNames.size(); i < columnNamesSize; i++) {
            Column<?> cc = column(i);

            String name = cc.name();
            s.append("@attribute ").append(quoteIfNecessary(name)).append(" ");

            ColumnType type = cc.type();
            if (type == ColumnType.DOUBLE)
                s.append("numeric");
            else if (type == ColumnType.STRING)
                s.append("string");

//                case Nominal:
//                    s.append("{");
//                    joinWith(nominalCats.get(name), s, ",");
//                    s.append("}");
//                    break;
//            }

            else
                throw new UnsupportedOperationException();


            s.append(NEW_LINE);

            s.append("@data").append(NEW_LINE);

            for (Row r : this) {
                joinWith(r, s, ",");
                s.append(NEW_LINE);
            }
        }
    }

    public static String quoteIfNecessary(String name) {
        return isQuoteNecessary(name) ? Str.quote(name) : name;
    }

    /**
     * Save the data into a file.
     */
    public void writeToFile(String filename) throws IOException {
        try (FileWriter w = new FileWriter(filename)) {
            write(w);
            w.flush();
        }
    }

    /**
     * Get the name of the relation.
     */
    public String getRelation() {
        return relation;
    }

    /**
     * Set the name of the relation.
     */
    public void setRelation(String relation) {
        this.relation = relation;
    }

    /**
     * Get the initial comment of the relation.
     */
    public String getComment() {
        return comment;
    }

    /**
     * Set the initial comment of the relation.
     */
    public void setComment(String comment) {
        this.comment = comment;
    }

    /** TODO use StringBuilder or something */
    public void addComment(String s) {
        this.comment += s;
    }


    //    public Iterator<ImmutableList> iterator() {
//        return data.iterator();
//    }

//    public void print() {
//        try {
//            write(System.out);
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        }
//    }


    public boolean addAll(DataTable incoming) {
        if (this == incoming)
            return false;
        if (!equalSchema(incoming)) {
            throw new RuntimeException("schemas differ");
        }
        boolean[] changed = {false};
        for (Row p : incoming) {
            changed[0] |= add(p);
        }
        return changed[0];
    }

    //    public FloatTable<String> toFloatTable(int... columns) {
//
//        Arrays.sort(columns);
//
//        int n = columns.length;
//        String[] names = new String[n];
//        int i = 0;
//        for (int c : columns) {
//            names[i++] = attrName(c);
//        }
//        FloatTable<String> data = new FloatTable<>(names);
//
//
//        for (ImmutableList exp : this.data) {
//            float[] r = new float[n];
//            int k = 0;
//            for (int c : columns)
//                r[k++] = ((Number) exp.get(c)).floatValue();
//            data.addAt(r);
//        }
//
//        return data;
//
//    }


    public static class ARFFParseError extends Exception {

        /**
         * Construct a new ArffFileParseErrro object.
         */
        ARFFParseError(int lineno, String string) {
            super("Parse error line " + lineno + ": " + string);
        }

    }

    /**
     * ARFF that is defined by and can be bound to/from a simple POJO
     */
    public static class ARFFObject<X> extends ARFF {

        static final Logger logger = LoggerFactory.getLogger(ARFFObject.class);
        private final Function[] extractor;

        /**
         * TODO hints for extracting Nominals
         */
        public ARFFObject(Class<X> c) {
            Reflect C = Reflect.on(c);

            Lst<Function<X, ?>> extractor = new Lst();

            for (Map.Entry<String, Reflect> e : C.fields(true, false).entrySet()) {
                String n = e.getKey();

                Field field = e.getValue().get();
                field.trySetAccessible();

                Class<?> t = Primitives.wrap(field.getType());
                if (Byte.class.isAssignableFrom(t) || Short.class.isAssignableFrom(t) || Integer.class.isAssignableFrom(t) || Long.class.isAssignableFrom(t)) {
                    defineNumeric(n);
                    extractor.add(x -> {
                        try {
                            return ((Number) field.get(x)).longValue();
                        } catch (IllegalAccessException e1) {
                            logger.error("field", e1);
                            return Double.NaN;
                        }
                    });

                } else if (Boolean.class.isAssignableFrom(t)) {
                    defineNominal(n, "true", "false");
                    extractor.add(x -> {
                        try {
                            return Boolean.toString((Boolean) field.get(x));
                        } catch (IllegalAccessException e1) {
                            logger.error("field", e1);
                            return null;
                        }
                    });
                } else if (Number.class.isAssignableFrom(t)) {

                    defineNumeric(n);
                    extractor.add(x -> {
                        try {
                            return ((Number) field.get(x)).doubleValue();
                        } catch (IllegalAccessException e1) {
                            logger.error("field", e1);
                            return Double.NaN;
                        }
                    });
                } else {
                    
                    defineText(n);
                    extractor.add(x -> {
                        try {
                            return field.get(x).toString();
                        } catch (IllegalAccessException e1) {
                            logger.error("field", e1);
                            return "?";
                        }
                    });
                }

            }

            if (extractor.isEmpty())
                throw new RuntimeException("no fields accessed");

            this.extractor = extractor.toArrayRecycled(Function[]::new);
        }

        public boolean put(X x) {
            int n = columnCount();
            Object[] o = IntStream.range(0, n).mapToObj(i -> extractor[i].apply(x)).toArray();
            return add(o);
        }
    }


}