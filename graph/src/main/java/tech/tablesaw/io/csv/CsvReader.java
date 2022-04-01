//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package tech.tablesaw.io.csv;

import com.google.common.io.CharStreams;
import com.univocity.parsers.csv.CsvFormat;
import com.univocity.parsers.csv.CsvParser;
import com.univocity.parsers.csv.CsvParserSettings;
import org.eclipse.collections.api.tuple.Pair;
import tech.tablesaw.api.ColumnType;
import tech.tablesaw.api.Table;
import tech.tablesaw.io.*;

import javax.annotation.concurrent.Immutable;
import java.io.IOException;
import java.io.Reader;
import java.util.Optional;

import static org.eclipse.collections.impl.tuple.Tuples.pair;

/** monkeypatched to use eclipse collection's Pair instead of Apache Math's */
@Immutable public class CsvReader extends FileReader implements DataReader<CsvReadOptions> {
    private static final CsvReader INSTANCE = new CsvReader();

    public static void register(ReaderRegistry registry) {
        registry.registerExtension("csv", INSTANCE);
        registry.registerMimeType("text/csv", INSTANCE);
        registry.registerOptions(CsvReadOptions.class, INSTANCE);
    }

    public CsvReader() {
    }

    private Pair<Reader, ColumnType[]> getReaderAndColumnTypes(Source source, CsvReadOptions options) throws IOException {
        ColumnType[] types = options.columnTypes();
        byte[] bytesCache = null;
        if (types == null) {
            Reader reader = source.createReader(bytesCache);
            if (source.file() == null) {
                String s = CharStreams.toString(reader);
                bytesCache = source.getCharset() != null ? s.getBytes(source.getCharset()) : s.getBytes();
                reader = source.createReader(bytesCache);
            }

            types = this.detectColumnTypes(reader, options);
        }

        return pair(source.createReader(bytesCache), types);
    }

    public Table read(CsvReadOptions options)  {
        try {
            return this.read(options, false);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private Table read(CsvReadOptions options, boolean headerOnly) throws IOException {
        Pair<Reader, ColumnType[]> pair = this.getReaderAndColumnTypes(options.source(), options);
        Reader reader = pair.getOne();
        ColumnType[] types = pair.getTwo();
        CsvParser parser = this.csvParser(options);

        Table var7;
        try {
            ReadOptions.ColumnTypeReadOptions TYPES = (i, s) -> {
                return Optional.of(types[i]);//Optional.empty();
            };
            var7 = this.parseRows(options, headerOnly, reader,
                    TYPES, parser, options.sampleSize());
        } finally {
            if (options.source().reader() == null) {
                parser.stopParsing();
                reader.close();
            }

        }

        return var7;
    }

    public String printColumnTypes(CsvReadOptions options) throws IOException {
        Table structure = this.read(options, true).structure();
        return this.getTypeString(structure);
    }

    protected ColumnType[] detectColumnTypes(Reader reader, CsvReadOptions options) {
        boolean header = options.header();
        int linesToSkip = header ? 1 : 0;
        CsvParser parser = this.csvParser(options);

        ColumnType[] var6;
        try {
            var6 = this.getColumnTypes(reader, options, linesToSkip, parser);
        } finally {
            parser.stopParsing();
        }

        return var6;
    }

    private CsvParser csvParser(CsvReadOptions options) {
        CsvParserSettings settings = new CsvParserSettings();
        settings.setLineSeparatorDetectionEnabled(options.lineSeparatorDetectionEnabled());
        settings.setFormat(this.csvFormat(options));
        settings.setMaxCharsPerColumn(options.maxCharsPerColumn());
        if (options.maxNumberOfColumns() != null) {
            settings.setMaxColumns(options.maxNumberOfColumns());
        }

        return new CsvParser(settings);
    }

    private CsvFormat csvFormat(CsvReadOptions options) {
        CsvFormat format = new CsvFormat();
        if (options.quoteChar() != null) {
            format.setQuote(options.quoteChar());
        }

        if (options.escapeChar() != null) {
            format.setQuoteEscape(options.escapeChar());
        }

        if (options.separator() != null) {
            format.setDelimiter(options.separator());
        }

        if (options.lineEnding() != null) {
            format.setLineSeparator(options.lineEnding());
        }

        if (options.commentPrefix() != null) {
            format.setComment(options.commentPrefix());
        }

        return format;
    }

    public Table read(Source source) {
        return this.read(CsvReadOptions.builder(source).build());
    }

    static {
        register(Table.defaultReaderRegistry);
    }
}