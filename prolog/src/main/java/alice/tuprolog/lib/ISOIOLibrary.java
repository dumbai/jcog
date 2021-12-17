package alice.tuprolog.lib;

/**
 * @author: Sara Sabioni
 */

import alice.tuprolog.*;
import alice.util.Tools;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * This class provides basic ISO I/O predicates.
 * <p>
 * Library/Theory Dependency: IOLibrary
 */

public class ISOIOLibrary extends PrologLib {
    protected static final int files = 1000;
    private static final long serialVersionUID = 1L;
    protected final Hashtable<InputStream, Hashtable<String, Term>> inputStreams = new Hashtable<>(files);
    protected final Hashtable<OutputStream, Hashtable<String, Term>> outputStreams = new Hashtable<>(files);

    protected InputStream inputStream;
    protected OutputStream outputStream;
    protected String inputStreamName;
    protected String outputStreamName;
    protected IOLibrary IOLib;

    private int flag;
    private int write_flag = 1;

    public ISOIOLibrary() {

    }

    private static void analize_term(List<Term> variables, Term t) {
        if (!t.isCompound()) {
            variables.add(t);
        } else {
            Struct term_struct = (Struct) t.term();
            for (int i = 0; i < term_struct.subs(); i++) {
                analize_term(variables, term_struct.sub(i));
            }
        }
    }

    private static boolean inizialize_properties(Hashtable<String, Term> map) {
        Struct s = Struct.emptyList();
        map.put("file_name", s);
        map.put("mode", s);
        map.put("input", new Struct("false"));
        map.put("output", new Struct("false"));
        map.put("alias", s);
        map.put("position", new NumberTerm.Int(0));
        map.put("end_of_stream", new Struct("not"));
        map.put("eof_action", new Struct("error"));
        map.put("reposition", new Struct("false"));
        map.put("type", s);
        return true;
    }

    public boolean open_4(Term source_sink, Term mode, Term stream, Term options) throws PrologError {
        initLibrary();
        source_sink = source_sink.term();
        mode = mode.term();

        if (source_sink instanceof Var) {
            throw PrologError.instantiation_error(prolog, 1);
        }

        File file = new File(((Struct) source_sink).name());
        if (!file.exists()) {
            throw PrologError.existence_error(prolog, 1, "source_sink", source_sink, new Struct("File not found."));
        }

        if (mode instanceof Var) {
            throw PrologError.instantiation_error(prolog, 2);
        } else if (!mode.isAtomic()) {
            throw PrologError.type_error(prolog, 1, "atom", mode);
        }

        if (!(stream instanceof Var)) {
            throw PrologError.type_error(prolog, 3, "variable", stream);
        }

        Hashtable<String, Term> properties = new Hashtable<>(10);
        boolean result = inizialize_properties(properties);

        if (result) {
            Struct openOptions = (Struct) options;
            Struct in_out = (Struct) source_sink;
            if (openOptions.isList()) {
                if (!openOptions.isEmptyList()) {
                    Iterator<? extends Term> i = openOptions.listIterator();
                    while (i.hasNext()) {
                        Object obj = i.next();
                        if (obj instanceof Var) {
                            throw PrologError.instantiation_error(prolog, 4);
                        }
                        Struct option = (Struct) obj;
                        if (!properties.containsKey(option.name())) {
                            throw PrologError.domain_error(prolog, 4, "stream_option", option);
                        }


                        if ("alias".equals(option.name())) {

                            for (Map.Entry<InputStream, Hashtable<String, Term>> currentElement : inputStreams.entrySet()) {
                                for (Map.Entry<String, Term> currentElement2 : currentElement.getValue().entrySet()) {
                                    if ("alias".equals(currentElement2.getKey())) {
                                        Term alias = currentElement2.getValue();
                                        for (int k = 0; k < option.subs(); k++) {
                                            if (((Struct) alias).subs() > 1) {
                                                for (int z = 0; z < ((Struct) alias).subs(); z++) {
                                                    if ((((Struct) alias).sub(z)).equals(option.sub(k))) {
                                                        throw PrologError.permission_error(prolog, "open", "source_sink", alias, new Struct("Alias is already associated with an open stream."));
                                                    }
                                                }
                                            } else if (alias.equals(option.sub(k))) {
                                                throw PrologError.permission_error(prolog, "open", "source_sink", alias, new Struct("Alias is already associated with an open stream."));
                                            }
                                        }
                                    }
                                }
                            }


                            for (Map.Entry<OutputStream, Hashtable<String, Term>> currentElement : outputStreams.entrySet()) {
                                for (Map.Entry<String, Term> currentElement2 : currentElement.getValue().entrySet()) {
                                    if ("alias".equals(currentElement2.getKey())) {
                                        Term alias = currentElement2.getValue();
                                        for (int k = 0; k < option.subs(); k++) {
                                            if (((Struct) alias).subs() > 1) {
                                                for (int z = 0; z < ((Struct) alias).subs(); z++) {
                                                    if ((((Struct) alias).sub(z)).equals(option.sub(k))) {
                                                        throw PrologError.permission_error(prolog, "open", "source_sink", alias, new Struct("Alias is already associated with an open stream."));
                                                    }
                                                }
                                            } else if (alias.equals(option.sub(k))) {
                                                throw PrologError.permission_error(prolog, "open", "source_sink", alias, new Struct("Alias is already associated with an open stream."));
                                            }
                                        }
                                    }
                                }
                            }
                            int arity = option.subs();
                            if (arity > 1) {
                                Term[] arrayTerm = IntStream.range(0, arity).mapToObj(option::sub).toArray(Term[]::new);
                                properties.put(option.name(), new Struct(".", arrayTerm));
                            } else {
                                properties.put(option.name(), option.sub(0));
                            }
                        } else {
                            Struct value = (Struct) option.sub(0);
                            properties.put(option.name(), value);
                        }
                    }
                    properties.put("mode", mode);
                    properties.put("file_name", source_sink);
                }
            } else {
                throw PrologError.type_error(prolog, 4, "list", openOptions);
            }

            Struct structMode = (Struct) mode;
            BufferedInputStream input = null;
            BufferedOutputStream output;
            switch (structMode.name()) {
                case "write" -> {
                    try {
                        output = new BufferedOutputStream(new FileOutputStream(in_out.name()));
                    } catch (Exception e) {

                        throw PrologError.permission_error(prolog, "open", "source_sink", source_sink,
                                new Struct("The source_sink specified by Source_sink cannot be opened."));
                    }
                    properties.put("output", new Struct("true"));
                    outputStreams.put(output, properties);
                    return unify(stream, new Struct(output.toString()));
                }
                case "read" -> {
                    return out(source_sink, stream, properties, in_out);
                }
                case "append" -> {
                    try {
                        output = new BufferedOutputStream(new FileOutputStream(in_out.name(), true));
                    } catch (Exception e) {
                        throw PrologError.permission_error(prolog, "open", "source_sink", source_sink,
                                new Struct("The source_sink specified by Source_sink cannot be opened."));
                    }
                    properties.put("output", new Struct("true"));
                    outputStreams.put(output, properties);
                    return unify(stream, new Struct(output.toString()));
                }
                default -> throw PrologError.domain_error(prolog, 2, "io_mode", mode);
            }
        } else {
            PrologError.system_error(new Struct("A problem has occurred with initialization of properties' hashmap."));
            return false;
        }
    }

    private boolean out(Term source_sink, Term stream, Hashtable<String, Term> properties, Struct in_out) throws PrologError {
        BufferedInputStream input;
        {
            try {
                input = new BufferedInputStream(new FileInputStream(in_out.name()));
            } catch (Exception e) {
                throw PrologError.permission_error(prolog, "open", "source_sink", source_sink,
                        new Struct("The source_sink specified by Source_sink cannot be opened."));
            }
            properties.put("input", new Struct("true"));
            if ("true".equals(((Struct) properties.get("reposition")).name())) {
                try {
                    input.mark((input.available()) + 5);
                } catch (IOException e) {

                    try {
                        input.close();
                    } catch (IOException e2) {
                        throw PrologError.system_error(new Struct("An error has occurred in open when closing the input file."));
                    }

                    throw PrologError.system_error(new Struct("An error has occurred in open."));
                }
            }
            inputStreams.put(input, properties);
            return unify(stream, new Struct(input.toString()));
        }
    }

    public boolean open_3(Term source_sink, Term mode, Term stream) throws PrologError {
        initLibrary();

        source_sink = source_sink.term();
        File file = new File(((Struct) source_sink).name());
        if (!file.exists()) {
            throw PrologError.existence_error(prolog, 1, "source_sink", source_sink, new Struct("File not found"));
        }
        mode = mode.term();
//        if (source_sink instanceof Var) {
//            throw PrologError.instantiation_error(prolog, 1);
//        }

        if (mode instanceof Var) {
            throw PrologError.instantiation_error(prolog, 2);
        } else if (!mode.isAtomic()) {
            throw PrologError.type_error(prolog, 1, "atom", mode);
        }

        if (!(stream instanceof Var)) {
            throw PrologError.type_error(prolog, 3, "variable", stream);
        }


        Hashtable<String, Term> properties = new Hashtable<>(10);
        boolean result = inizialize_properties(properties);

        Struct structMode = (Struct) mode;

        if (result) {
            Struct in_out = (Struct) source_sink;
            Struct value = new Struct(in_out.name());
            properties.put("file_name", value);
            properties.put("mode", mode);

//            BufferedInputStream input = null;
            BufferedOutputStream output;
            switch (structMode.name()) {
                case "write" -> {
                    try {
                        output = new BufferedOutputStream(new FileOutputStream(in_out.name()));
                    } catch (Exception e) {

                        throw PrologError.permission_error(prolog, "open", "source_sink", source_sink,
                                new Struct("The source_sink specified by Source_sink cannot be opened."));
                    }
                    properties.put("output", new Struct("true"));
                    outputStreams.put(output, properties);
                    return unify(stream, new Struct(output.toString()));
                }
                case "read" -> {
                    return out(source_sink, stream, properties, in_out);
                }
                case "append" -> {
                    try {
                        output = new BufferedOutputStream(new FileOutputStream(in_out.name(), true));
                    } catch (Exception e) {
                        throw PrologError.permission_error(prolog, "open", "source_sink", source_sink,
                                new Struct("The source_sink specified by Source_sink cannot be opened."));
                    }
                    properties.put("output", new Struct("true"));
                    outputStreams.put(output, properties);
                    return unify(stream, new Struct(output.toString()));
                }
                default -> throw PrologError.domain_error(prolog, 1, "stream", in_out);
            }
        } else {
            PrologError.system_error(new Struct("A problem has occurred with the initialization of the hashmap properties."));
            return false;
        }
    }

    public boolean close_2(Term stream_or_alias, Term closeOptions) throws PrologError {
        initLibrary();


        boolean force = false;
        Struct closeOption = (Struct) closeOptions;

        if (closeOptions.isList()) {
            if (!closeOptions.isEmptyList()) {
                Iterator<? extends Term> i = closeOption.listIterator();
                while (i.hasNext()) {
                    Object obj = i.next();
                    if (obj instanceof Var) {
                        throw PrologError.instantiation_error(prolog, 4);
                    }
                    Struct option = (Struct) obj;
                    if ("force".equals(option.name())) {
                        Struct closeOptionValue = (Struct) option.sub(0);
                        force = "true".equals(closeOptionValue.name());
                    } else {
                        throw PrologError.domain_error(prolog, 2, "close_option", option);
                    }
                }
            }
        } else {
            throw PrologError.type_error(prolog, 4, "list", closeOptions);
        }


        InputStream in = null;
        OutputStream out = null;
        try {
            in = find_input_stream(stream_or_alias);
        } catch (PrologError p) {
            out = find_output_stream(stream_or_alias);
        }

        if (out != null) {
            String out_name = get_output_name(out);
            if ("stdout".equals(out_name)) {
                return true;
            }
            try {
                flush_output_1(stream_or_alias);
                out.close();
            } catch (IOException e) {
                if (force) {


                    outputStreams.remove(in);


                    if (out_name.equals(outputStreamName)) {
                        outputStreamName = "stdout";
                        outputStream = System.out;
                    }
                } else {
                    throw PrologError.system_error(new Struct("An error has occurred on stream closure."));
                }
            }
        } else if (in != null) {
            String in_name = get_input_name(in);
            if ("stdin".equals(in_name)) {
                return true;
            }
            try {
                in.close();
            } catch (IOException e) {
                if (force) {
                    inputStreams.remove(in);
                    in = null;
                    if (in_name.equals(inputStreamName)) {
                        inputStreamName = "stdin";
                        inputStream = System.in;
                    }
                } else {
                    throw PrologError.system_error(new Struct("An error has occurred on stream closure."));
                }
            }
            inputStreams.remove(in);
        }
        return true;
    }

    public boolean close_1(Term stream_or_alias) throws PrologError {
        initLibrary();

        OutputStream out = null;
        InputStream in = null;

        try {
            in = find_input_stream(stream_or_alias);
        } catch (PrologError p) {
            out = find_output_stream(stream_or_alias);
        }

        if (out != null) {
            String out_name = get_output_name(out);
            if ("stdout".equals(out_name)) {
                return true;
            }
            flush_output_1(stream_or_alias);
            try {
                out.close();
            } catch (IOException e) {
                throw PrologError.system_error(new Struct("An error has occurred on stream closure."));
            }
            if (out_name.equals(outputStreamName)) {
                outputStreamName = "stdout";
                outputStream = System.out;
            }
            outputStreams.remove(out);
        } else if (in != null) {
            String in_name = get_input_name(in);
            if ("stdin".equals(in_name)) {
                return true;
            }
            try {
                in.close();
            } catch (IOException e) {
                throw PrologError.system_error(new Struct("An error has occurred on stream closure."));
            }
            if (in_name.equals(inputStreamName)) {
                inputStreamName = "stdin";
                inputStream = System.in;
            }
            inputStreams.remove(in);
        }
        return true;
    }

    public boolean set_input_1(Term stream_or_alias) throws PrologError {
        initLibrary();
        InputStream stream = find_input_stream(stream_or_alias);
        Hashtable<String, Term> entry = inputStreams.get(stream);
        Struct name = (Struct) entry.get("file_name");
        inputStream = stream;
        inputStreamName = name.name();
        return true;
    }

    public boolean set_output_1(Term stream_or_alias) throws PrologError {
        initLibrary();
        OutputStream stream = find_output_stream(stream_or_alias);
        Hashtable<String, Term> entry = outputStreams.get(stream);
        Struct name = (Struct) entry.get("file_name");
        outputStream = stream;
        outputStreamName = name.name();
        return true;
    }

    public boolean find_property_2(Term list, Term property) throws PrologError {
        initLibrary();
        if (outputStreams.isEmpty() && inputStreams.isEmpty()) {
            return false;
        }

        if (!(list instanceof Var)) {
            throw PrologError.instantiation_error(prolog, 1);
        }

        property = property.term();
        Struct prop = (Struct) property;
        String propertyName = prop.name();
        Struct propertyValue = null;
        if (!"input".equals(propertyName) && !"output".equals(propertyName)) {
            propertyValue = (Struct) prop.sub(0);
        }
        List<Struct> resultList = new ArrayList<>();

        switch (propertyName) {
            case "input" -> {
                resultList = inputStreams.keySet().stream().map(stringTermHashtable -> new Struct(stringTermHashtable.toString())).collect(Collectors.toList());
                Struct result = new Struct(resultList.toArray(new Struct[1]));
                return unify(list, result);
            }
            case "output" -> {
                resultList = outputStreams.keySet().stream().map(stringTermHashtable -> new Struct(stringTermHashtable.toString())).collect(Collectors.toList());
                Struct result = new Struct(resultList.toArray(new Struct[1]));
                return unify(list, result);
            }
            default -> {
                for (Map.Entry<InputStream, Hashtable<String, Term>> currentElement : inputStreams.entrySet()) {
                    for (Map.Entry<String, Term> currentElement2 : currentElement.getValue().entrySet()) {
                        if (currentElement2.getKey().equals(propertyName)) {
                            if ("alias".equals(propertyName)) {
                                int arity = ((Struct) currentElement2.getValue()).subs();
                                if (arity == 0) {
                                    if (propertyValue.equals(currentElement2.getValue())) {
                                        resultList.add(new Struct(currentElement.getKey().toString()));
                                        break;
                                    }
                                }
                                for (int i = 0; i < arity; i++) {
                                    if (propertyValue.equals(((Struct) currentElement2.getValue()).sub(i))) {
                                        resultList.add(new Struct(currentElement.getKey().toString()));
                                        break;
                                    }
                                }
                            } else if (currentElement2.getValue().equals(propertyValue)) {
                                resultList.add(new Struct(currentElement.getKey().toString()));
                            }
                        }
                    }
                }
                for (Map.Entry<OutputStream, Hashtable<String, Term>> currentElement : outputStreams.entrySet()) {
                    for (Map.Entry<String, Term> currentElement2 : currentElement.getValue().entrySet()) {
                        if (currentElement2.getKey().equals(propertyName)) {
                            if ("alias".equals(propertyName)) {
                                int arity = ((Struct) currentElement2.getValue()).subs();
                                if (arity == 0) {
                                    if (propertyValue.equals(currentElement2.getValue())) {
                                        resultList.add(new Struct(currentElement.getKey().toString()));
                                        break;
                                    }
                                }
                                for (int i = 0; i < arity; i++) {
                                    if (propertyValue.equals(((Struct) currentElement2.getValue()).sub(i))) {
                                        resultList.add(new Struct(currentElement.getKey().toString()));
                                        break;
                                    }
                                }
                            } else if (currentElement2.getValue().equals(propertyValue)) {
                                resultList.add(new Struct(currentElement.getKey().toString()));
                            }
                        }
                    }
                }
            }
        }
        Struct result = new Struct(resultList.toArray(new Struct[1]));
        return unify(list, result);
    }

    @Override
    public String getTheory() {
        return "stream_property(S,P) :- find_property(L,P),member(S,L).\n";
    }

    public boolean at_end_of_stream_0() {
        initLibrary();
        Hashtable<String, Term> entry = inputStreams.get(inputStream);
        Term value = entry.get("end_of_stream");
        Struct eof = (Struct) value;
        return !"not".equals(eof.name());
    }

    public boolean at_end_of_stream_1(Term stream_or_alias) throws PrologError {
        initLibrary();
        InputStream stream = find_input_stream(stream_or_alias);
        Hashtable<String, Term> entry = inputStreams.get(stream);
        Term value = entry.get("end_of_stream");
        Struct eof = (Struct) value;
        return !"not".equals(eof.name());
    }

    public boolean set_stream_position_2(Term stream_or_alias, Term position) throws PrologError {

        initLibrary();
        InputStream in = find_input_stream(stream_or_alias);

        if (position instanceof Var) {
            throw PrologError.instantiation_error(prolog, 2);
        } else {
            if (!(position instanceof NumberTerm)) {
                throw PrologError.domain_error(prolog, 2, "stream_position", position);
            }
        }

        Hashtable<String, Term> entry = inputStreams.get(in);
        Term reposition = entry.get("reposition");

        Struct value = (Struct) reposition;
        if ("false".equals(value.name())) {
            throw PrologError.permission_error(prolog, "reposition", "stream", stream_or_alias, new Struct("Stream has property reposition(false)"));
        }

        BufferedInputStream buffer = null;
        if (in instanceof BufferedInputStream) {
            buffer = (BufferedInputStream) in;
        }

        if (buffer.markSupported()) {
            try {
                buffer.reset();

                NumberTerm n = (NumberTerm) position;
                long pos = n.longValue();
                if (pos < 0) {
                    throw PrologError.domain_error(prolog, 2, "+long", position);
                }

                int size = in.available();

                if (pos > size) {
                    throw PrologError.system_error(new Struct("Invalid operation. Input position is greater than file size."));
                }
                if (pos == size) {
                    entry.put("end_of_file", new Struct("at"));
                }

                buffer.skip(pos);
                int new_pos = (new NumberTerm.Long(pos)).intValue();
                entry.put("position", new NumberTerm.Int(new_pos));
                inputStreams.put(buffer, entry);

            } catch (IOException e) {

                e.printStackTrace();
                throw PrologError.system_error(new Struct("An error has occurred in method 'set_stream_position'."));
            }
        }
        return true;
    }

    public boolean flush_output_0() throws PrologError {
        initLibrary();
        try {
            outputStream.flush();
        } catch (IOException e) {
            throw PrologError.system_error(new Struct("An error has occurred in method 'flush_output_0'."));
        }
        return true;
    }

    public boolean flush_output_1(Term stream_or_alias) throws PrologError {
        initLibrary();
        OutputStream stream = find_output_stream(stream_or_alias);
        try {
            stream.flush();
        } catch (IOException e) {
            throw PrologError.system_error(new Struct("An error has occurred in method 'flush_output_1'."));
        }
        return true;
    }

    public boolean get_char_2(Term stream_or_alias, Term arg) throws PrologError {
        initLibrary();
        InputStream stream = find_input_stream(stream_or_alias);

        if (!(arg instanceof Var)) {
            throw PrologError.instantiation_error(prolog, 1);
        }

        Hashtable<String, Term> element = inputStreams.get(stream);

        Struct struct_name = (Struct) element.get("file_name");
        String file_name = struct_name.toString();

        Struct type = (Struct) element.get("type");
        if ("binary".equals(type.name())) {
            throw PrologError.permission_error(prolog, "input", "binary_stream", stream_or_alias, new Struct("The target stream is associated with a binary stream."));
        }


        if ("stdin".equals(file_name)) {
            IOLib.get0_1(arg);
            return true;
        }


        try {
            NumberTerm position = (NumberTerm) (element.get("position"));
            Struct eof = (Struct) element.get("end_of_stream");


            if ("past".equals(eof.name())) {
                Term actionTemp = element.get("eof_action");
                String action = ((Struct) actionTemp).name();

                switch (action) {
                    case "error":
                        throw PrologError.permission_error(prolog, "input", "past_end_of_stream", new Struct("reader"), new Struct("End of file is reached."));
                    case "eof_code":
                        return unify(arg, new Struct("-1"));
                    case "reset":
                        element.put("end_of_stream", new Struct("not"));
                        element.put("position", new NumberTerm.Int(0));
                        stream.reset();
                        break;
                }
            }


            int value = stream.read();

            if (!Character.isDefined(value)) {
                if (value == -1) {
                    element.put("end_of_stream", new Struct("past"));
                } else {
                    throw PrologError.representation_error(prolog, 2, "character");
                }
            }
            NumberTerm.Int i = (NumberTerm.Int) position;
            int i2 = i.intValue();
            i2++;
            element.put("position", new NumberTerm.Int(i2));

            if (value != -1) {


                Var nextChar = new Var();
                peek_code_2(stream_or_alias, nextChar);
                Term nextCharTerm = nextChar.term();
                NumberTerm nextCharValue = (NumberTerm) nextCharTerm;
                if (nextCharValue.intValue() == -1) {
                    element.put("end_of_stream", new Struct("at"));
                }
            }

            inputStreams.put(stream, element);

            if (value == -1) {
                return unify(arg, Term.term(-1 + ""));
            }
            char c = (char) value;
            return unify(arg, new Struct(Character.toString(c)));
        } catch (IOException ioe) {
            ioe.printStackTrace();
            throw PrologError.system_error(new Struct("An I/O error has occurred"));
        }
    }

    public boolean get_code_1(Term char_code) throws PrologError {
        initLibrary();
        Struct s_or_a = new Struct(inputStream.toString());
        return get_code_2(s_or_a, char_code);
    }

    public boolean get_code_2(Term stream_or_alias, Term char_code) throws PrologError {
        initLibrary();
        InputStream stream = find_input_stream(stream_or_alias);

        if (!(char_code instanceof Var)) {
            throw PrologError.instantiation_error(prolog, 1);
        }

        Hashtable<String, Term> element = inputStreams.get(stream);
        Struct type = (Struct) element.get("type");
        if ("binary".equals(type.name())) {
            throw PrologError.permission_error(prolog, "input", "binary_stream", stream_or_alias, new Struct("The target stream is associated with a binary stream."));
        }


        Struct struct_name = (Struct) element.get("file_name");
        String file_name = struct_name.toString();
        int value;
        if ("stdin".equals(file_name)) {
            try {
                value = inputStream.read();
            } catch (IOException e) {
                throw PrologError.permission_error(prolog,
                        "input", "stream", new Struct(inputStreamName), new Struct(
                                e.getMessage()));
            }

            return unify(char_code, new NumberTerm.Int(value));
        }


        try {
            NumberTerm position = (NumberTerm) (element.get("position"));
            Struct eof = (Struct) element.get("end_of_stream");
            if (eof.name().equals("past")) {
                Term actionTemp = element.get("eof_action");
                String action = ((Struct) actionTemp).name();
                switch (action) {
                    case "error":
                        throw PrologError.permission_error(prolog, "input", "past_end_of_stream", new Struct("reader"), new Struct("End of file is reached."));
                    case "eof_code":
                        return unify(char_code, new Struct("-1"));
                    case "reset":
                        element.put("end_of_stream", new Struct("not"));
                        element.put("position", new NumberTerm.Int(0));
                        stream.reset();
                        break;
                }
            }

            value = stream.read();

            if (!Character.isDefined(value)) {
                if (value == -1) {
                    element.put("end_of_stream", new Struct("past"));
                } else {
                    throw PrologError.representation_error(prolog, 2, "character");
                }
            }
            NumberTerm.Int i = (NumberTerm.Int) position;
            int i2 = i.intValue();
            i2++;
            element.put("position", new NumberTerm.Int(i2));

            if (value != -1) {
                Var nextChar = new Var();
                peek_code_2(stream_or_alias, nextChar);
                Term nextCharTerm = nextChar.term();
                NumberTerm nextCharValue = (NumberTerm) nextCharTerm;
                if (nextCharValue.intValue() == -1) {
                    element.put("end_of_stream", new Struct("at"));
                }
            }

            inputStreams.put(stream, element);
            return unify(char_code, new NumberTerm.Int(value));
        } catch (IOException ioe) {
            ioe.printStackTrace();
            throw PrologError.system_error(new Struct("An I/O error has occurred."));
        }
    }

    public boolean peek_char_1(Term in_char) throws PrologError {
        initLibrary();
        Struct s_or_a = new Struct(inputStream.toString());
        if ("stdin".equals(inputStreamName)) {
            inputStream.mark(5);
            boolean var = get_char_2(s_or_a, in_char);
            try {
                inputStream.reset();
            } catch (IOException e) {

                e.printStackTrace();
                PrologError.system_error(new Struct("An error has occurred in peek_char_1."));
            }
            return var;
        } else {
            return peek_char_2(s_or_a, in_char);
        }
    }

    public boolean peek_char_2(Term stream_or_alias, Term in_char) throws PrologError {

        initLibrary();
        InputStream stream = find_input_stream(stream_or_alias);
        Hashtable<String, Term> element = inputStreams.get(stream);
        String file_name = ((Struct) element.get("file_name")).name();

        if ("stdin".equals(file_name)) {
            return get_char_2(stream_or_alias, in_char);
        }

        FileInputStream stream2 = null;
        try {
            stream2 = new FileInputStream(file_name);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            PrologError.system_error(new Struct("File not found."));
        }

        if (!(in_char instanceof Var)) {
            throw PrologError.instantiation_error(prolog, 1);
        }
        Struct type = (Struct) element.get("type");
        if ("binary".equals(type.name())) {
            throw PrologError.permission_error(prolog, "input", "binary_stream", stream_or_alias, new Struct("Target stream is associated with a binary stream."));
        }

        try {
            NumberTerm position = (NumberTerm) (element.get("position"));
            Struct eof = (Struct) element.get("end_of_stream");
            int value = 0;
            if (eof.equals("past")) {
                Term actionTemp = element.get("eof_action");
                String action = ((Struct) actionTemp).name();
                switch (action) {
                    case "error":
                        throw PrologError.permission_error(prolog, "input", "past_end_of_stream", new Struct("reader"), new Struct("End of file has been reached."));
                    case "eof_code":
                        return unify(in_char, new Struct("-1"));
                    case "reset":
                        element.put("end_of_stream", new Struct("not"));
                        element.put("position", new NumberTerm.Int(0));
                        stream.reset();
                        break;
                }
            } else {
                NumberTerm.Int i = (NumberTerm.Int) position;
                long nBytes = i.longValue();
                stream2.skip(nBytes);
                value = stream2.read();

                stream2.close();
            }
            if (!Character.isDefined(value) && value != -1) {

                throw PrologError.representation_error(prolog, 2, "character");
            }
            inputStreams.put(stream, element);

            if (value == -1) {
                return unify(in_char, Term.term(-1 + ""));
            }

            char c = (char) value;
            return unify(in_char, Term.term(Character.toString(c)));
        } catch (IOException ioe) {
            ioe.printStackTrace();
            throw PrologError.system_error(new Struct("An I/O error has occurred."));
        }
    }

    public boolean peek_code_1(Term char_code) throws PrologError {
        initLibrary();
        Struct stream = new Struct(inputStream.toString());
        if ("stdin".equals(inputStreamName)) {
            inputStream.mark(5);
            boolean var = get_code_2(stream, char_code);
            try {
                inputStream.reset();
            } catch (IOException e) {

                e.printStackTrace();
                PrologError.system_error(new Struct("An error has occurred in peek_code_1."));
            }
            return var;
        } else {
            return peek_char_2(stream, char_code);
        }
    }

    public boolean peek_code_2(Term stream_or_alias, Term char_code) throws PrologError {
        initLibrary();

        InputStream stream = find_input_stream(stream_or_alias);
        Hashtable<String, Term> element = inputStreams.get(stream);
        String file_name = ((Struct) element.get("file_name")).name();

        FileInputStream stream2 = null;
        try {
            stream2 = new FileInputStream(file_name);
        } catch (FileNotFoundException e) {

            e.printStackTrace();
            PrologError.system_error(new Struct("File not found."));
        }

        if (!(char_code instanceof Var)) {
            throw PrologError.instantiation_error(prolog, 1);
        }
        Struct type = (Struct) element.get("type");
        if ("binary".equals(type.name())) {
            throw PrologError.permission_error(prolog, "input", "binary_stream", stream_or_alias, new Struct("Target stream is associated with a binary stream."));
        }

        try {
            NumberTerm position = (NumberTerm) (element.get("position"));
            Struct eof = (Struct) element.get("end_of_stream");
            int value = 0;
            if (eof.equals("past")) {
                Term actionTemp = element.get("eof_action");
                String action = ((Struct) actionTemp).name();
                switch (action) {
                    case "error":
                        throw PrologError.permission_error(prolog, "input", "past_end_of_stream", new Struct("reader"), new Struct("End of file is reached."));
                    case "eof_code":
                        return unify(char_code, new Struct("-1"));
                    case "reset":
                        element.put("end_of_stream", new Struct("not"));
                        element.put("position", new NumberTerm.Int(0));
                        stream.reset();
                        break;
                }
            } else {
                NumberTerm.Int i = (NumberTerm.Int) position;
                long nBytes = i.longValue();
                stream2.skip(nBytes);
                value = stream2.read();
                stream2.close();
            }
            if (!Character.isDefined(value) && value != -1) {

                throw PrologError.representation_error(prolog, 2, "character");
            }
            inputStreams.put(stream, element);
            return unify(char_code, new NumberTerm.Int(value));
        } catch (IOException ioe) {
            ioe.printStackTrace();
            throw PrologError.system_error(new Struct("An I/O error has occurred."));
        }
    }

    public boolean put_char_2(Term stream_or_alias, Term in_char) throws PrologError {
        initLibrary();
        OutputStream stream = find_output_stream(stream_or_alias);
        String stream_name = get_output_name(stream);

        Hashtable<String, Term> element = outputStreams.get(stream);
        Struct type = (Struct) element.get("type");
        if ("binary".equals(type.name())) {
            throw PrologError.permission_error(prolog, "input", "binary_stream", stream_or_alias, new Struct("Target stream is associated with a binary stream."));
        }

        Struct arg0 = (Struct) in_char.term();

        if (!arg0.isAtomic()) {
            throw PrologError.type_error(prolog, 2, "character", arg0);
        } else {
            String ch = arg0.name();
            if (!(Character.isDefined(ch.charAt(0)))) {
                throw PrologError.representation_error(prolog, 2, "character");
            }
            if (ch.length() > 1) {
                throw PrologError.type_error(prolog, 2, "character", new Struct(ch));
            } else {
                if ("stdout".equals(stream_name)) {
                    prolog.output(ch);
                } else {
                    try {
                        stream.write((byte) ch.charAt(0));
                    } catch (IOException ioe) {
                        ioe.printStackTrace();
                        throw PrologError.system_error(new Struct("An I/O error has occurred."));
                    }
                }
                return true;
            }
        }
    }

    public boolean put_code_1(Term char_code) throws PrologError {
        initLibrary();
        Struct stream = new Struct(outputStream.toString());
        return put_code_2(stream, char_code);
    }

    public boolean put_code_2(Term stream_or_alias, Term char_code) throws PrologError {
        initLibrary();
        OutputStream stream = find_output_stream(stream_or_alias);
        String stream_name = get_output_name(stream);

        Hashtable<String, Term> element = outputStreams.get(stream);
        Struct type = (Struct) element.get("type");
        if ("binary".equals(type.name())) {
            throw PrologError.permission_error(prolog, "input", "binary_stream", stream_or_alias, new Struct("Target stream is associated with a binary stream."));
        }

        NumberTerm arg0 = (NumberTerm) char_code.term();


        if (!(arg0 instanceof NumberTerm)) {
            throw PrologError.type_error(prolog, 2, "character", arg0);
        } else {
            if (Character.isDefined(arg0.intValue())) {
                throw PrologError.representation_error(prolog, 2, "character_code");
            }
            if ("stdout".equals(stream_name)) {
                prolog.output(String.valueOf(arg0.intValue()));
            } else {
                try {
                    stream.write(arg0.intValue());
                } catch (IOException ioe) {
                    ioe.printStackTrace();
                    throw PrologError.system_error(new Struct("An I/O error has occurred."));
                }
            }
        }
        return true;
    }

    public boolean nl_1(Term stream_or_alias) throws PrologError {
        initLibrary();
        OutputStream stream = find_output_stream(stream_or_alias);
        String stream_name = get_output_name(stream);
        if ("stdout".equals(stream_name)) {
            prolog.output("\n");
        } else {
            try {
                stream.write('\n');
            } catch (IOException e) {
                throw PrologError.permission_error(prolog,
                        "output", "stream", new Struct(outputStreamName),
                        new Struct(e.getMessage()));
            }
        }
        return true;
    }

    public boolean get_byte_1(Term in_byte) throws PrologError {


        initLibrary();
        Struct stream_or_alias = new Struct(inputStream.toString());
        return get_byte_2(stream_or_alias, in_byte);
    }

    public boolean get_byte_2(Term stream_or_alias, Term in_byte) throws PrologError {
        initLibrary();
        InputStream stream = find_input_stream(stream_or_alias);
        Hashtable<String, Term> element = inputStreams.get(stream);
        Struct type = (Struct) element.get("type");
        if ("text".equals(type.name())) {
            throw PrologError.permission_error(prolog, "input", "text_stream", stream_or_alias, new Struct("Target stream is associated with a text stream."));
        }

        if (!(in_byte instanceof Var))
            throw PrologError.instantiation_error(prolog, 1);

        try {
            DataInputStream reader = new DataInputStream(stream);
            NumberTerm position = (NumberTerm) (element.get("position"));
            NumberTerm.Int i = (NumberTerm.Int) position;
            int i2 = i.intValue();
            reader.skipBytes(i2 - 1);
            Struct eof = (Struct) element.get("end_of_stream");
            if (eof.equals("past")) {
                Term actionTemp = element.get("eof_action");
                String action = ((Struct) actionTemp).name();
                switch (action) {
                    case "error":
                        throw PrologError.permission_error(prolog, "input", "past_end_of_stream", new Struct("reader"), new Struct("End of file is reached."));
                    case "eof_code":
                        return unify(in_byte, new Struct("-1"));
                    case "reset":
                        element.put("end_of_stream", new Struct("not"));
                        element.put("position", new NumberTerm.Int(0));
                        reader.reset();
                        break;
                }

            }

            byte b = reader.readByte();

            i2++;
            element.put("position", new NumberTerm.Int(i2));


            Var nextByte = new Var();
            peek_byte_2(stream_or_alias, nextByte);
            Term nextByteTerm = nextByte.term();
            NumberTerm nextByteValue = (NumberTerm) nextByteTerm;
            if (nextByteValue.intValue() == -1) {
                element.put("end_of_stream", new Struct("at"));
            }


            inputStreams.put(stream, element);
            return unify(in_byte, Term.term(Byte.toString(b)));
        } catch (IOException ioe) {
            element.put("end_of_stream", new Struct("past"));
            return unify(in_byte, Term.term("-1"));
        }
    }

    public boolean peek_byte_1(Term in_byte) throws PrologError {
        initLibrary();
        Struct stream_or_alias = new Struct(inputStream.toString());
        return peek_char_2(stream_or_alias, in_byte);
    }

    public boolean peek_byte_2(Term stream_or_alias, Term in_byte) throws PrologError {
        initLibrary();
        InputStream stream = find_input_stream(stream_or_alias);
        Hashtable<String, Term> element = inputStreams.get(stream);
        Struct type = (Struct) element.get("type");
        if ("text".equals(type.name())) {
            throw PrologError.permission_error(prolog, "input", "text_stream", stream_or_alias, new Struct("Target stream is associated with a text stream."));
        }

        if (!(in_byte instanceof Var))
            throw PrologError.instantiation_error(prolog, 1);

        try {
            DataInputStream reader = new DataInputStream(stream);
            NumberTerm position = (NumberTerm) (element.get("position"));
            NumberTerm.Int i = (NumberTerm.Int) position;
            int i2 = i.intValue();
            reader.skipBytes(i2 - 2);
            Struct eof = (Struct) element.get("end_of_stream");
            Byte b = null;
            if (eof.equals("past")) {
                Term actionTemp = element.get("eof_action");
                String action = ((Struct) actionTemp).name();
                switch (action) {
                    case "error":
                        throw PrologError.permission_error(prolog, "input", "past_end_of_stream", new Struct("reader"), new Struct("End of file is reached."));
                    case "eof_code":
                        return unify(in_byte, new Struct("-1"));
                    case "reset":
                        element.put("end_of_stream", new Struct("not"));
                        element.put("position", new NumberTerm.Int(0));
                        reader.reset();
                        break;
                }

            } else {
                b = reader.readByte();
            }

            inputStreams.put(stream, element);
            return unify(in_byte, Term.term(b.toString()));
        } catch (IOException e) {
            element.put("end_of_stream", new Struct("past"));
            return unify(in_byte, Term.term("-1"));
        }
    }

    public boolean put_byte_1(Term out_byte) throws PrologError {


        initLibrary();
        Struct stream_or_alias = new Struct(outputStream.toString());
        return put_byte_2(stream_or_alias, out_byte);

    }

    public boolean put_byte_2(Term stream_or_alias, Term out_byte) throws PrologError {
        initLibrary();
        OutputStream stream = find_output_stream(stream_or_alias);
        out_byte = out_byte.term();
        NumberTerm b = (NumberTerm) out_byte.term();

        Hashtable<String, Term> element = outputStreams.get(stream);
        Struct type = (Struct) element.get("type");
        if ("text".equals(type.name())) {
            throw PrologError.permission_error(prolog, "output", "text_stream", stream_or_alias, new Struct("Target stream is associated with a text stream."));
        }

        if (out_byte instanceof Var)
            throw PrologError.instantiation_error(prolog, 1);

        if (stream.equals("stdout")) {


            prolog.output(out_byte.toString());
        } else {
            try {
                DataOutputStream writer = new DataOutputStream(stream);
                NumberTerm position = (NumberTerm) (element.get("position"));
                NumberTerm.Int i = (NumberTerm.Int) position;
                int i2 = i.intValue();

                writer.writeByte(b.intValue());

                i2++;
                element.put("position", new NumberTerm.Int(i2));
                outputStreams.put(stream, element);
            } catch (IOException e) {
                throw PrologError.permission_error(prolog, "output", "stream", new Struct(outputStreamName), new Struct(e.getMessage()));
            }
        }
        return true;
    }

    public boolean read_term_2(Term in_term, Term options) throws PrologError {
        initLibrary();
        Struct stream_or_alias = new Struct(inputStream.toString());
        return read_term_3(stream_or_alias, in_term, options);
    }

    public boolean read_term_3(Term stream_or_alias, Term in_term, Term options) throws PrologError {
        initLibrary();
        InputStream stream = find_input_stream(stream_or_alias);

        if (options instanceof Var) {
            throw PrologError.instantiation_error(prolog, 3);
        }

        Hashtable<String, Term> element = inputStreams.get(stream);
        Struct type = (Struct) element.get("type");
        Struct eof = (Struct) element.get("end_of_stream");
        Struct action = (Struct) element.get("eof_action");
        NumberTerm position = (NumberTerm) element.get("position");
        if ("binary".equals(type.name())) {
            throw PrologError.permission_error(prolog, "input", "binary_stream", stream_or_alias, new Struct("Target stream is associated with a binary stream."));
        }
        if ("past".equals(eof.name()) && "error".equals(action.name())) {
            throw PrologError.permission_error(prolog, "past_end_of_stream", "stream", stream_or_alias, new Struct("Target stream has position at past_end_of_stream"));
        }

        boolean variables_bool = false;
        boolean variable_names_bool = false;
        boolean singletons_bool = false;

        Struct readOptions = (Struct) options;
        if (readOptions.isList()) {
            if (!readOptions.isEmptyList()) {
                Iterator<? extends Term> i = readOptions.listIterator();
                while (i.hasNext()) {
                    Object obj = i.next();
                    if (obj instanceof Var) {
                        throw PrologError.instantiation_error(prolog, 3);
                    }
                    Struct option = (Struct) obj;
                    switch (option.name()) {
                        case "variables" -> variables_bool = true;
                        case "variable_name" -> variable_names_bool = true;
                        case "singletons" -> singletons_bool = true;
                        default -> PrologError.domain_error(prolog, 3, "read_option", option);
                    }
                }
            }
        } else {
            throw PrologError.type_error(prolog, 3, "list", options);
        }

        try {

            in_term = in_term.term();
            String st = "";
            boolean open_apices2 = false;
            boolean open_apices = false;
            int ch;
            label:
            do {
                ch = stream.read();

                if (ch == -1) {
                    break;
                }

                switch (ch) {
                    case '\'':
                        open_apices = !open_apices;
                        break;
                    case '\"':
                        open_apices2 = !open_apices2;
                        break;
                    default:
                        if (ch == '.') {
                            if (!open_apices && !open_apices2) {
                                break label;
                            }
                        }
                        break;
                }
                boolean can_add = true;
                if (can_add) {
                    st += Character.toString(((char) ch));
                }
            } while (true);

            NumberTerm.Int p = (NumberTerm.Int) position;
            int p2 = p.intValue();
            p2 += (st.getBytes()).length;

            if (ch == -1) {
                st = "-1";
                element.put("end_of_stream", new Struct("past"));
                element.put("position", new NumberTerm.Int(p2));
                inputStreams.put(stream, element);
                return unify(in_term, Term.term(st));
            }

            if (!variables_bool && !variable_names_bool && !singletons_bool) {
                return unify(in_term, prolog.toTerm(st));
            }
            Var input_term = new Var();
            unify(input_term, Term.term(st));


            List<Term> variables_list = new ArrayList<>();
            analize_term(variables_list, input_term);

            Hashtable<Term, String> associations_table = new Hashtable<>(variables_list.size());


            Hashtable<Term, String> association_for_replace = new Hashtable<>(variables_list.size());

            LinkedHashSet<Term> set = new LinkedHashSet<>(variables_list);
            List<Var> vars = new ArrayList<>();

            if (variables_bool) {
                int num = 0;
                for (Term t : set) {
                    num++;
                    if (variable_names_bool) {
                        association_for_replace.put(t, "X" + num);
                        if (!((t.toString()).startsWith("_"))) {
                            associations_table.put(t, "X" + num);
                        }
                    }
                    vars.add(new Var("X" + num));
                }
            }


            List<Term> singl = new ArrayList<>();
            if (singletons_bool) {
                List<Term> temporanyList = new ArrayList<>(variables_list);
                int flag;
                for (Term t : variables_list) {
                    temporanyList.remove(t);
                    flag = 0;
                    if (temporanyList.stream().anyMatch(temp -> temp.equals(t))) {
                        flag = 1;
                    }
                    if (flag == 0) {
                        if (!((t.toString()).startsWith("_"))) {
                            singl.add(t);
                        }
                    }
                    temporanyList.add(t);
                }
            }


            Iterator<? extends Term> i = readOptions.listIterator();
            Struct option;
            Struct singletons;
            Struct variable_names;
            Struct variables;
            while (i.hasNext()) {
                Object obj = i.next();
                option = (Struct) obj;
                switch (option.name()) {
                    case "variables" -> {
                        variables = (Struct) Term.term(vars.toString());
                        unify(option.sub(0), variables);
                    }
                    case "variable_name" -> {
                        variable_names = (Struct) Term.term(associations_table.toString());
                        unify(option.sub(0), variable_names);
                    }
                    case "singletons" -> {
                        singletons = (Struct) Term.term(singl.toString());
                        unify(option.sub(0), singletons);
                    }
                }
            }

            String string_term = input_term.toString();

            for (Map.Entry<Term, String> entry : association_for_replace.entrySet()) {
                String regex = entry.getKey().toString();
                String replacement = entry.getValue();
                string_term = string_term.replaceAll(regex, replacement);
            }


            element.put("position", new NumberTerm.Int(p2));
            inputStreams.put(stream, element);
            return unify(in_term, prolog.toTerm(string_term));
        } catch (Exception ex) {
            return false;
        }
    }

    public boolean read_2(Term stream_or_alias, Term in_term) throws PrologError {
        initLibrary();
        Struct options = new Struct(".", Struct.emptyList());
        return read_term_3(stream_or_alias, in_term, options);
    }

    public boolean write_term_2(Term out_term, Term options) throws PrologError {
        initLibrary();
        Struct stream_or_alias = new Struct(outputStream.toString());
        return write_term_3(stream_or_alias, out_term, options);
    }

    public boolean write_term_3(Term stream_or_alias, Term out_term, Term optionsTerm) throws PrologError {
        initLibrary();
        out_term = out_term.term();

        OutputStream output = find_output_stream(stream_or_alias);
        String output_name = get_output_name(output);
        Struct writeOptionsList = (Struct) optionsTerm.term();

        Hashtable<String, Term> element = outputStreams.get(output);
        Struct type = (Struct) element.get("type");
        if ("binary".equals(type.name())) {
            throw PrologError.permission_error(prolog, "output", "binary_stream", stream_or_alias, new Struct("Target stream is associated with a binary stream."));
        }

        boolean numbervars = false;
        boolean ignore_ops = false;
        boolean quoted = false;
        if (writeOptionsList.isList()) {
            if (!writeOptionsList.isEmptyList()) {
                Iterator<? extends Term> i = writeOptionsList.listIterator();
                Struct writeOption;
                while (i.hasNext()) {


                    Object obj = i.next();
                    if (obj instanceof Var) {
                        throw PrologError.instantiation_error(prolog, 3);
                    }
                    writeOption = (Struct) obj;
                    switch (writeOption.name()) {
                        case "quoted" -> quoted = "true".equals(((Struct) writeOption.sub(0)).name());
                        case "ignore_ops" -> ignore_ops = "true".equals(((Struct) writeOption.sub(0)).name());
                        case "numbervars" -> numbervars = "true".equals(((Struct) writeOption.sub(0)).name());
                        default -> throw PrologError.domain_error(prolog, 3, "write_options", writeOptionsList.term());
                    }
                }
            }
        } else {
            PrologError.type_error(prolog, 3, "list", writeOptionsList);
        }
        try {
            if (!out_term.isCompound() && !(out_term instanceof Var)) {

                if ("stdout".equals(output_name)) {
                    if (quoted) {


                        prolog.output((Tools.removeApostrophes(out_term.toString())));
                    } else {
                        prolog.output((out_term.toString()));
                    }
                } else {
                    if (quoted) {
                        output.write((Tools.removeApostrophes(out_term.toString())).getBytes());
                    } else {
                        output.write((out_term.toString()).getBytes());
                    }
                }

                return true;
            }


            if (out_term instanceof Var) {

                if ("stdout".equals(output_name)) {
                    if (quoted) {
                        prolog.output((Tools.removeApostrophes(out_term.toString()) + ' '));
                    } else {
                        prolog.output((out_term.toString() + ' '));
                    }
                } else {
                    if (quoted) {
                        output.write((Tools.removeApostrophes(out_term.toString()) + ' ').getBytes());
                    } else {
                        output.write((out_term.toString() + ' ').getBytes());
                    }
                }

                return true;
            }

            Struct term = (Struct) out_term;
            Hashtable<String, Boolean> options = new Hashtable<>(3);
            options.put("numbervars", numbervars);
            options.put("ignore_ops", ignore_ops);
            options.put("quoted", quoted);

            String result = create_string(options, term);

            if ("stdout".equals(output_name)) {
                prolog.output(result);
            } else {
                output.write((result + ' ').getBytes());
            }

        } catch (IOException ioe) {
            PrologError.system_error(new Struct("Write error has occurred in write_term/3."));
        }
        return true;
    }

    private String create_string(Hashtable<String, Boolean> options, Struct term) {

        boolean numbervars = options.get("numbervars");
        boolean quoted = options.get("quoted");
        boolean ignore_ops = options.get("ignore_ops");

        if (term.isList()) {
            String list = print_list(term, options);
            return ignore_ops ? list : '[' + list + ']';
        }

        Iterable<PrologOp> operatorList = prolog.operators();
        String operator = "";
        int flagOp = 0;
        for (PrologOp op : operatorList) {
            if (op.name.equals(term.name())) {
                operator = op.name;
                flagOp = 1;
                break;
            }
        }

        String result = "";
        if (flagOp == 0) {
            result += term.name() + '(';
        }

        int arity = term.subs();
        for (int i = 0; i < arity; i++) {
            if (i > 0 && flagOp == 0)
                result += ",";
            Term arg = term.sub(i);
            if (arg instanceof NumberTerm) {
                if (term.name().contains("$VAR")) {

                    if (numbervars) {
                        NumberTerm.Int argNumber = (NumberTerm.Int) term.sub(i);
                        int res = argNumber.intValue() % 26;
                        int div = argNumber.intValue() / 26;
                        char ch = 'A';
                        int num = (ch + res);
                        result = new String(Character.toChars(num));
                        if (div != 0) {
                            result += div;
                        }
                    } else {
                        if (quoted) {
                            return term.toString();
                        } else {
                            result += Tools.removeApostrophes(arg.toString());
                        }
                    }
                } else {

                    if (!ignore_ops) {
                        result += arg.toString();
                        if (i % 2 == 0 && operator != "") {
                            result += ' ' + operator + ' ';
                        }
                    } else {
                        result = term.toString();
                        return result;
                    }
                }
            } else if (arg instanceof Var) {

                if (!ignore_ops) {
                    result += arg.toString();
                    if (i % 2 == 0 && operator != "") {
                        result += ' ' + operator + ' ';
                    }
                } else {
                    result += arg.toString();
                }
            } else if (arg.isCompound()) {
                if (!ignore_ops) {
                    result += create_string(options, (Struct) arg);
                    if (i % 2 == 0 && operator != "") {
                        result += ' ' + operator + ' ';
                    }
                } else {
                    result += create_string(options, (Struct) arg);
                }

            } else {
                if (quoted) {
                    if (!ignore_ops) {
                        result += arg.toString();
                        if (i % 2 == 0 && operator != "") {
                            result += ' ' + operator + ' ';
                        }
                    } else {
                        result += arg.toString();
                    }
                } else {
                    if (!ignore_ops) {
                        result += Tools.removeApostrophes(arg.toString());
                        if (i % 2 == 0 && operator != "") {
                            result += ' ' + operator + ' ';
                        }
                    } else {
                        result += Tools.removeApostrophes(arg.toString());
                    }
                }
            }
        }

        if (flagOp == 0 && result.contains("(")) {
            result += ")";
        }
        return result;
    }

    private String print_list(Struct term, Hashtable<String, Boolean> options) {


        boolean ignore_ops = options.get("ignore_ops");

        String result = "";

        if (ignore_ops) {
            result = '\'' + term.name() + '\'' + " (";
            for (int i = 0; i < term.subs(); i++) {
                if (i > 0) {
                    result += ",";
                }
                result += term.sub(i).isList() && !(term.sub(i).isEmptyList()) ? print_list((Struct) term.sub(i), options) : term.sub(i);
            }
            return result + ')';
        } else {
            for (int i = 0; i < term.subs(); i++) {
                if (i > 0 && !(term.sub(i).isEmptyList())) {
                    result += ",";
                }
                if ((term.sub(i)).isCompound() && !(term.sub(i).isList())) {
                    result += create_string(options, (Struct) term.sub(i));
                } else {

                    if ((term.sub(i).isList()) && !(term.sub(i).isEmptyList()))
                        result += print_list((Struct) term.sub(i), options);
                    else {
                        if (!(term.sub(i).isEmptyList()))
                            result += term.sub(i).toString();
                    }

                }
            }
            return result;
        }
    }

    public boolean write_2(Term stream_or_alias, Term out_term) throws PrologError {
        initLibrary();
        Struct options = new Struct(".", new Struct("quoted", new Struct("false")),
                new Struct(".", new Struct("ignore_ops", new Struct("false")),
                        new Struct(".", new Struct("numbervars", new Struct("true")), Struct.emptyList())));
        return write_term_3(stream_or_alias, out_term, options);
    }

    public boolean write_1(Term out_term) throws PrologError {
        return write_flag == 0 ? write_iso_1(out_term) : IOLib.write_base_1(out_term);
    }

    public boolean write_iso_1(Term out_term) throws PrologError {
        initLibrary();
        Struct stream_or_alias = new Struct(outputStream.toString());
        Struct options = new Struct(".", new Struct("quoted", new Struct("false")),
                new Struct(".", new Struct("ignore_ops", new Struct("false")),
                        new Struct(".", new Struct("numbervars", new Struct("true")), Struct.emptyList())));
        return write_term_3(stream_or_alias, out_term, options);
    }

    public boolean writeq_1(Term out_term) throws PrologError {
        initLibrary();
        Struct stream_or_alias = new Struct(outputStream.toString());
        Struct options = new Struct(".", new Struct("quoted", new Struct("true")),
                new Struct(".", new Struct("ignore_ops", new Struct("false")),
                        new Struct(".", new Struct("numbervars", new Struct("true")), Struct.emptyList())));
        return write_term_3(stream_or_alias, out_term, options);
    }

    public boolean writeq_2(Term stream_or_alias, Term out_term) throws PrologError {
        initLibrary();
        Struct options = new Struct(".", new Struct("quoted", new Struct("true")),
                new Struct(".", new Struct("ignore_ops", new Struct("false")),
                        new Struct(".", new Struct("numbervars", new Struct("true")), Struct.emptyList())));
        return write_term_3(stream_or_alias, out_term, options);
    }

    public boolean write_canonical_1(Term out_term) throws PrologError {
        initLibrary();
        Struct stream_or_alias = new Struct(outputStream.toString());
        Struct options = new Struct(".", new Struct("quoted", new Struct("true")),
                new Struct(".", new Struct("ignore_ops", new Struct("true")),
                        new Struct(".", new Struct("numbervars", new Struct("false")), Struct.emptyList())));
        return write_term_3(stream_or_alias, out_term, options);
    }

    public boolean write_canonical_2(Term stream_or_alias, Term out_term) throws PrologError {
        initLibrary();
        Struct options = new Struct(".", new Struct("quoted", new Struct("true")),
                new Struct(".", new Struct("ignore_ops", new Struct("true")),
                        new Struct(".", new Struct("numbervars", new Struct("false")), Struct.emptyList())));
        return write_term_3(stream_or_alias, out_term, options);
    }

    private void initLibrary() {
        if (flag == 1)
            return;

        PrologLib prologLib = prolog.library("alice.tuprolog.lib.IOLibrary");
        if (prologLib == null) {
            try {
                prologLib = prolog.addLibrary("alice.tuprolog.lib.IOLibrary");
            } catch (InvalidLibraryException e) {

                e.printStackTrace();
                PrologError.system_error(new Struct("IOLibrary does not exists."));
            }
        }

        IOLib = (IOLibrary) prologLib;
        inputStream = IOLib.inputStream;
        outputStream = IOLib.outputStream;
        inputStreamName = IOLib.inputStreamName;
        outputStreamName = IOLib.outputStreamName;
        flag = 1;


        Hashtable<String, Term> propertyInput = new Hashtable<>(10);
        inizialize_properties(propertyInput);
        propertyInput.put("input", new Struct("true"));
        propertyInput.put("mode", new Struct("read"));
        propertyInput.put("alias", new Struct("user_input"));

        propertyInput.put("file_name", new Struct("stdin"));
        propertyInput.put("eof_action", new Struct("reset"));
        propertyInput.put("type", new Struct("text"));
        Hashtable<String, Term> propertyOutput = new Hashtable<>(10);
        inizialize_properties(propertyOutput);
        propertyOutput.put("output", new Struct("true"));
        propertyOutput.put("mode", new Struct("append"));
        propertyOutput.put("alias", new Struct("user_output"));
        propertyOutput.put("eof_action", new Struct("reset"));
        propertyOutput.put("file_name", new Struct("stdout"));
        propertyOutput.put("type", new Struct("text"));
        inputStreams.put(inputStream, propertyInput);
        outputStreams.put(outputStream, propertyOutput);

    }

    private InputStream find_input_stream(Term stream_or_alias) throws PrologError {
        stream_or_alias = stream_or_alias.term();
        Struct stream = (Struct) stream_or_alias;
        String stream_name = stream.name();

        if (stream_or_alias instanceof Var) {
            throw PrologError.instantiation_error(prolog, 1);
        }


        InputStream result = null;
        int flag = 0;
        for (Map.Entry<InputStream, Hashtable<String, Term>> currentElement : inputStreams.entrySet()) {
            for (Map.Entry<String, Term> currentElement2 : currentElement.getValue().entrySet()) {


                if ((currentElement.getKey().toString()).equals(stream_name)) {
                    result = currentElement.getKey();
                    flag = 1;
                    break;
                } else if ("file_name".equals(currentElement2.getKey())) {
                    if (stream_or_alias.equals(currentElement2.getValue())) {
                        result = currentElement.getKey();
                        flag = 1;
                        break;
                    }
                } else if ("alias".equals(currentElement2.getKey())) {
                    Struct alias = (Struct) currentElement2.getValue();
                    int arity = alias.subs();


                    if (arity > 1) {
                        for (int k = 0; k < alias.subs(); k++) {
                            if ((alias.sub(k)).equals(stream_or_alias)) {
                                result = currentElement.getKey();
                                flag = 1;
                                break;
                            }
                        }
                    } else {

                        if (alias.name().equals(stream_name)) {
                            result = currentElement.getKey();
                            flag = 1;
                            break;
                        }
                    }
                }
            }
        }


        if (stream_name.contains("Output") || "stdout".equals(stream_name))
            throw PrologError.permission_error(prolog, "output", "stream", stream_or_alias, new Struct("S_or_a is an output stream"));

        if (flag == 0)

            throw PrologError.existence_error(prolog, 1, "stream", stream_or_alias, new Struct("Input stream should be opened."));

        return result;
    }


    private OutputStream find_output_stream(Term stream_or_alias) throws PrologError {
        stream_or_alias = stream_or_alias.term();
        Struct stream = (Struct) stream_or_alias;
        String stream_name = stream.name();

        if (stream_or_alias instanceof Var) {
            throw PrologError.instantiation_error(prolog, 1);
        }

        OutputStream result = null;
        int flag = 0;
        for (Map.Entry<OutputStream, Hashtable<String, Term>> currentElement : outputStreams.entrySet()) {
            for (Map.Entry<String, Term> currentElement2 : currentElement.getValue().entrySet()) {

                if ((currentElement.getKey().toString()).equals(stream_name)) {
                    result = currentElement.getKey();
                    flag = 1;
                    break;
                } else if ("file_name".equals(currentElement2.getKey())) {
                    if (stream_or_alias.equals(currentElement2.getValue())) {
                        result = currentElement.getKey();
                        flag = 1;
                        break;
                    }
                } else if ("alias".equals(currentElement2.getKey())) {
                    Struct alias = (Struct) currentElement2.getValue();
                    int arity = alias.subs();


                    if (arity > 1) {
                        for (int k = 0; k < alias.subs(); k++) {
                            if ((alias.sub(k)).equals(stream_or_alias)) {
                                result = currentElement.getKey();
                                flag = 1;
                                break;
                            }
                        }
                    } else {

                        if (alias.name().equals(stream_name)) {
                            result = currentElement.getKey();
                            flag = 1;
                            break;
                        }
                    }
                }
            }
        }

        if (stream_name.contains("Input") || "stdin".equals(stream_name))
            throw PrologError.permission_error(prolog, "input", "stream", stream_or_alias, new Struct("S_or_a is an input stream."));

        if (flag == 0)

            throw PrologError.existence_error(prolog, 1, "stream", stream_or_alias, new Struct("Output stream should be opened."));

        return result;
    }


    private String get_output_name(OutputStream output) {
        Hashtable<String, Term> found = outputStreams.entrySet().stream().filter(element -> (element.getKey().toString()).equals(output.toString())).map(Map.Entry::getValue).findFirst().orElse(null);
        Term file_name = found != null ? found.get("file_name") : null;
        Struct returnElement = (Struct) file_name;
        return returnElement.name();
    }

    private String get_input_name(InputStream input) {
        Term file_name = null;
        for (Map.Entry<InputStream, Hashtable<String, Term>> element : inputStreams.entrySet()) {
            if ((element.getKey().toString()).equals(input.toString())) {
                Hashtable<String, Term> properties = element.getValue();
                file_name = properties.get("file_name");
                break;
            }
        }
        Struct returnElement = (Struct) file_name;
        return returnElement.name();
    }

    public boolean set_write_flag_1(Term number) throws PrologError {
        NumberTerm n = (NumberTerm) number;
        if (n.intValue() == 1) {
            write_flag = 1;
            return true;
        } else if (n.intValue() == 0) {
            write_flag = 0;
            return true;
        } else {
            throw PrologError.domain_error(prolog, 1, "0-1", number);
        }
    }
}