package jcog.io;

import jcog.Util;
import jcog.data.list.Lst;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.util.Date;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;

public enum FileCache {
	;

	public static <X> Stream<X> fileCache(URL u, String baseName, Supplier<Stream<X>> o,
                                          BiConsumer<Stream<X>, DataOutput> encoder,
                                          Function<DataInput, X> decoder,
                                          Logger logger
    ) throws IOException, URISyntaxException {
        return fileCache(new File(u.toURI()), baseName, o, encoder, decoder, logger);
    }

    public static <X> Stream<X> fileCache(Path p, String baseName, Supplier<Stream<X>> o,
                                          BiConsumer<Stream<X>, DataOutput> encoder,
                                          Function<DataInput, X> decoder,
                                          Logger logger
    ) throws IOException {
        return fileCache(p.toFile(), baseName, o, encoder, decoder, logger);
    }

    public static <X> Stream<X> fileCache(File f, String baseName, Supplier<Stream<X>> o,
                                          BiConsumer<Stream<X>, DataOutput> encoder,
                                          Function<DataInput, X> decoder,
                                          Logger logger
    ) throws IOException {

        long lastModified = f.lastModified();
        long size = f.length();
        String suffix = '_' + f.getName() + '_' + lastModified + '_' + size;

        List<X> buffer = new Lst(1024 /* estimate */);

        String tempDir = Util.tempDir();

        File cached = new File(tempDir, baseName + suffix);
        if (cached.exists()) {
            
            try {

                FileInputStream ff = new FileInputStream(cached);
                DataInputStream din = new DataInputStream(new BufferedInputStream(ff));
                while (din.available() > 0) {
                    buffer.add(decoder.apply(din));
                }
                din.close();

                logger.warn("cache loaded {}: ({} bytes, from {})", cached.getAbsolutePath(), cached.length(), new Date(cached.lastModified()));

                return buffer.stream();
            } catch (Exception e) {
                logger.warn("regenerating", e);
                
            }
        }

        
        buffer.clear();

        Stream<X> instanced = o.get();

        DataOutputStream dout = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(cached.getAbsolutePath())));
        encoder.accept(instanced.peek(buffer::add), dout);
        dout.close();
        logger.warn("cache saved {}: ({} bytes)", cached.getAbsolutePath(), dout.size());

        return buffer.stream();


    }

    static final Logger logger = LoggerFactory.getLogger(FileCache.class);

    public static <X> byte[] fileCache(String baseName, Supplier<byte[]> o) throws IOException {

        String tempDir = Util.tempDir();

        File cached = new File(tempDir, baseName);
        if (cached.exists()) {
            
            try {

                InputStream ff = new FileInputStream(cached);
                byte[] b = ff.readAllBytes();
                logger.warn("cache loaded {}: ({} bytes, from {})", cached.getAbsolutePath(), cached.length(), new Date(cached.lastModified()));
                ff.close();
                return b;

            } catch (Exception e) {
                logger.warn("regenerating", e);
                
            }
        }

        byte[] instanced = o.get();

        FileOutputStream dout = new FileOutputStream(cached.getAbsolutePath());
        dout.write(instanced);
        dout.close();
        logger.warn("cache saved {}: ({} bytes)", cached.getAbsolutePath(), instanced.length);

        return instanced;
    }

}
