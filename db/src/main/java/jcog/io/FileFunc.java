package jcog.io;

import jcog.WTF;
import org.apache.commons.vfs2.*;
import org.apache.commons.vfs2.util.WeakRefFileListener;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;

/** File and URL access interface (through VFS) */
public enum FileFunc {
	;

	public static FILE get(String path) throws FileSystemException {
        return new FILE(fs.resolveFile(path));
    }

    static FILE get(URL url) throws FileSystemException {
        return new FILE(fs.resolveFile(url));
    }

    static @Nullable FILE getOrNull(String path)  {
        try {
            return get(path);
        } catch (FileSystemException e) {
            return null;
        }
    }
    static @Nullable FILE getOrNull(URL path)  {
        try {
            return get(path);
        } catch (FileSystemException e) {
            return null;
        }
    }


    public static final FileSystemManager fs;
    static {
        try {
            fs = VFS.getManager();
        } catch (FileSystemException e) {
            throw new WTF(e);
        }
    }



    static final Logger logger = LoggerFactory.getLogger(FileFunc.class);

    public static final class FILE {

        public final FileObject file;

        public FILE(FileObject file) {
            this.file = file;
        }

        public void on(FileListener l) {
            on(l, true);
        }

        public void on(FileListener l, boolean includeExistingInitially) {

            WeakRefFileListener.installListener(file, l);

            if (includeExistingInitially) {
                try {
                    //invoke listener with existing children
                    for (FileObject c : file.getChildren()) {
                        try {
                            l.fileCreated(new FileChangeEvent(c));
                        } catch (Exception e) {
                            logger.error("{} {} {}", file, c, e);
                        }
                    }
                } catch (FileSystemException e) {
                    logger.error("{} {}", file, e);
                }
            }
        }
    }


}
