/*
 * Copyright (C) 2000-2003 Greg Merrill (greghmerrill@yahoo.com) This file is part of Follow
 * (http://follow.sf.net). Follow is free software; you can redistribute it and/or modify it under
 * the terms of version 2 of the GNU General Public License as published by the Free Software
 * Foundation. Follow is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details. You should have received a copy of the GNU General
 * Public License along with Follow; if not, write to the Free Software Foundation, Inc., 59 Temple
 * Place, Suite 330, Boston, MA 02111-1307 USA
 */

package jcog.io;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.channels.Channels;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.IllegalCharsetNameException;
import java.nio.charset.StandardCharsets;
import java.text.MessageFormat;
import java.util.*;
import java.util.function.Consumer;

/**
 * Instances of this class 'follow' a particular text file, assmebling that file's characters into Strings and sending
 * them to instances of {@link OutputDestination}. The name and behavior of this class are inspired by the '-f'
 * (follow) flag of the UNIX command 'tail'.
 *
 * @author <a href="mailto:greghmerrill@yahoo.com">Greg Merrill </a>
 * @see OutputDestination
 */
public class FileFollower {

    static final Logger logger = LoggerFactory.getLogger(FileFollower.class);


    Charset encoding_;

    public final Charset getEncoding() {
        return encoding_;
    }

    /**
     * Constructs a new FileFollower; invoking this constructor does <em>not</em> cause the new object to begin
     * following the supplied file. In order to begin following, one must call {@link #start()}.
     *
     * @param file                      file to be followed
     * @param bufferSize                number of chars to be read each time the file is accessed
     * @param latency                   each time a FileFollower's running thread encounters the end of the file in its stream, it will rest
     *                                  for this many milliseconds before checking to see if there are any more bytes in the file
     * @param initialOutputDestinations an initial array of OutputDestinations which will be used when printing the
     *                                  contents of the file (this array may be <tt>null</tt>)
     */
    @SafeVarargs
    public FileFollower(File file, int bufferSize, int latency,
                        Consumer<String>... initialOutputDestinations) {
        file_ = file;
        bufferSize_ = bufferSize;
        latency_ = latency;
        try {
            encoding_ = Charset.forName(System.getProperty("file.encoding"));
        } catch (IllegalCharsetNameException e) {
            try {
                encoding_ = StandardCharsets.ISO_8859_1;
            } catch (IllegalCharsetNameException e1) {
                SortedMap m = Charset.availableCharsets();
                encoding_ = (Charset) m.get(m.firstKey());
            }
        }
        int initOutputDestsSize = (initialOutputDestinations != null) ? initialOutputDestinations.length : 0;
        outputDestinations_ = new ArrayList(initOutputDestsSize);
        if (initialOutputDestinations != null) {
			outputDestinations_.addAll(Arrays.asList(initialOutputDestinations).subList(0, initOutputDestsSize));
        }
    }

//  /**
//   * Identical to {@link #FileFollower(File, int, int, OutputDestination[])}, except that a default buffer size (32,768
//   * characters) and latency (1000 milliseconds) are used.
//   *
//   * @see #FileFollower(File, int, int, OutputDestination[])
//   */
//  public FileFollower(final File file, final OutputDestination[] initialOutputDestinations) {
//    // Initial buffer size pilfered from org.gjt.sp.jedit.Buffer. I'm not sure
//    // whether this is a truly optimal buffer size.
//    this(file, 32768, // Don't change without updating docs!
//        1000, // Don't change without updating docs!
//        initialOutputDestinations);
//  }

    /**
     * Cause this FileFollower to spawn a thread which will follow the file supplied in the constructor and send its
     * contents to all of the FileFollower's OutputDestinations.
     */
    public synchronized void start() {
        continueRunning_ = true;
        runnerThread_ = new Thread(new Runner());
        runnerThread_.start();
    }

    public synchronized void restart() {
        needsRestart_ = true;
        runnerThread_.interrupt();
    }

    /**
     * Cause this FileFollower to stop following the file supplied in the constructor after it flushes the characters it's
     * currently reading to all its OutputDestinations.
     */
    public synchronized void stop() {
        continueRunning_ = false;
        runnerThread_.interrupt();
    }

    /**
     * Like {@link #stop()}, but this method will not exit until the thread which is following the file has finished
     * executing (i.e., stop synchronously).
     */
    public synchronized void stopAndWait() {
        stop();
        while (runnerThread_.isAlive()) {
            Thread.yield();
        }
    }

//    /**
//     * Add another OutputDestination to which the followed file's contents should be printed.
//     *
//     * @param outputDestination OutputDestination to be added
//     */
//    public boolean addOutputDestination(final OutputDestination outputDestination) {
//        return outputDestinations_.add(outputDestination);
//    }
//
//    /**
//     * Remove the supplied OutputDestination from the list of OutputDestinations to which the followed file's contents
//     * should be printed.
//     *
//     * @param outputDestination OutputDestination to be removed
//     */
//    public boolean removeOutputDestination(final OutputDestination outputDestination) {
//        return outputDestinations_.remove(outputDestination);
//    }
//
//    /**
//     * Returns the List which maintains all OutputDestinations for this FileFollower.
//     *
//     * @return contains all OutputDestinations for this FileFollower
//     */
//    public List getOutputDestinations() {
//        return outputDestinations_;
//    }
//
    /**
     * Returns the file which is being followed by this FileFollower
     *
     * @return file being followed
     */
    public File getFollowedFile() {
        return file_;
    }

    /**
     * Returns the size of the character buffer used to read characters from the followed file. Each time the file is
     * accessed, this buffer is filled.
     *
     * @return size of the character buffer
     */
    public int getBufferSize() {
        return bufferSize_;
    }

    /**
     * Sets the size of the character buffer used to read characters from the followed file. Increasing buffer size will
     * improve efficiency but increase the amount of memory used by the FileFollower. <br>
     * <em>NOTE:</em> Setting this value will <em>not</em> cause a running FileFollower to immediately begin reading
     * characters into a buffer of the newly specified size. You must stop & restart the FileFollower in order for changes
     * to take effect.
     *
     * @param bufferSize size of the character buffer
     */
    public void setBufferSize(int bufferSize) {
        bufferSize_ = bufferSize;
    }

    public void setEncoding(String _s) {
        try {
            setEncoding(Charset.forName(_s));
        } catch (IllegalCharsetNameException e) {

        }
    }

    public void setReadFromBegin() {
        if (runnerThread_ != null) {
            stop();
        }
        forceReadBegin_ = true;
        if (runnerThread_ != null) {
            start();
        }
    }

    public void setEncoding(Charset _s) {
        if (_s != encoding_) {
            if (runnerThread_ != null) {
                stop();
            }
            encoding_ = _s;
            if (runnerThread_ != null) {
                start();
            }
        }
    }

    /**
     * Returns the time (in milliseconds) which a FileFollower spends sleeping each time it encounters the end of the
     * followed file.
     *
     * @return latency, in milliseconds
     */
    public int getLatency() {
        return latency_;
    }

    /**
     * Sets the time (in milliseconds) which a FileFollower spends sleeping each time it encounters the end of the
     * followed file. Note that extremely low latency values may cause thrashing between the FileFollower's running thread
     * and other threads in an application. A change in this value will be reflected the next time the FileFollower's
     * running thread sleeps.
     *
     * @param latency latency, in milliseconds
     */
    public void setLatency(int latency) {
        latency_ = latency;
    }

    protected int bufferSize_;
    protected int latency_;
    protected File file_;
    protected List<Consumer<String>> outputDestinations_;
    protected boolean continueRunning_;
    protected boolean needsRestart_;
    protected Thread runnerThread_;
    protected boolean isBeginRead_;
    protected boolean forceReadBegin_;

    /*
     * Instances of this class are used to run a thread which follows a FileFollower's file and sends prints its contents
     * to OutputDestinations.
     */
    class Runner implements Runnable {
        @Override
        public void run() {
            while (continueRunning_) {
                runAction();
            }
        }

        protected void runAction() {
            try {
//                clear();
                needsRestart_ = false;
                long fileSize = file_.length();
                char[] byteArray = new char[bufferSize_];

                try (FileInputStream fis = new FileInputStream(file_)) {
                    try (Reader r = Channels.newReader(fis.getChannel(), encoding_.newDecoder(), bufferSize_)) {
                        long lastActivityTime = 0;
                        int numBytesRead = -1;
                        if (fileSize > bufferSize_) {
                            // on lit rapidement le debut
                            if (forceReadBegin_) {
                                forceReadBegin_ = false;
                                // tant que c'est trop gros pour le buffer
                                char[] byteArrayTemp = new char[65536];
                                while (fis.available() > bufferSize_) {
                                    numBytesRead = r.read(byteArrayTemp, 0, byteArrayTemp.length);

                                    if (numBytesRead == 0) {
                                        break;
                                    }
                                    lastActivityTime = System.currentTimeMillis();
                                    print(new String(byteArrayTemp, 0, numBytesRead));
                                }
                                isBeginRead_ = true;
                            } else {
                                // Skip to the end of the file.
                                fis.skip(fileSize - bufferSize_);
                                isBeginRead_ = false;
                                print("!!! " + getBundle().getString("message.file.begin.skip"));
                                print(lineSeparator);
                            }
                        }
                        boolean isFileEmpty = (fileSize == 0);
                        while (continueRunning_ && !needsRestart_) {
                            numBytesRead = r.read(byteArray, 0, byteArray.length);

                            boolean dataWasFound = (numBytesRead > 0);
                            if (dataWasFound) {
                                lastActivityTime = System.currentTimeMillis();
                                print(new String(byteArray, 0, numBytesRead));
                                isFileEmpty = false;
                            } else if (!isFileEmpty) {
                                // Now check if the filehandle has become stale (file was
                                // modified, but no data could be read).
                                needsRestart_ = file_.exists() && (file_.lastModified() > lastActivityTime);
                                if (needsRestart_) {
                                    // on retente
                                    try {
                                        Thread.sleep(latency_);
                                    } catch (InterruptedException e) {
                                        Thread.currentThread().interrupt();
                                        // Interrupt may be thrown manually by stop()
                                    }
                                    numBytesRead = r.read(byteArray, 0, byteArray.length);
                                    dataWasFound = (numBytesRead > 0);
                                    if (dataWasFound) {
                                        lastActivityTime = System.currentTimeMillis();
                                        print(new String(byteArray, 0, numBytesRead));
                                        needsRestart_ = false;
                                    }
                                }
                            }

                            boolean noDataLeft = (numBytesRead < byteArray.length);
                            if (noDataLeft && !needsRestart_) {
                                try {
                                    Thread.sleep(latency_);
                                } catch (InterruptedException e) {
                                    // Interrupt may be thrown manually by stop()
                                }
                            }
                        }
                    }
                }
            } catch (CharacterCodingException _c) {
                badCharset();
            } catch (FileNotFoundException e) {

            } catch (IOException e) {
                logger.error("..",e);
//                FuLog.error(e);
                continueRunning_ = false;
            }
        } /* send the supplied string to all OutputDestinations */

        void print(String s) {
            for (Consumer<String> stringConsumer : outputDestinations_) {
                stringConsumer.accept(s);
            }
        }

//        /* clear all OutputDestinations */
//        void clear() {
//            final Iterator i = outputDestinations_.iterator();
//            while (i.hasNext()) {
//                ((OutputDestination) i.next()).clear();
//            }
//        }

        protected void badCharset() {
            continueRunning_ = false;
            logger.error(MessageFormat.format(
                    getBundle().getString("message.bad.charset"),
                    file_.getName()));
        }
    }

    /**
     * Line separator, retrieved from System properties & stored statically.
     */
    protected static final String lineSeparator = System.getProperty("line.separator");
    ResourceBundle bundle_;

    protected ResourceBundle getBundle() {
        if (bundle_ == null) {
            bundle_ = ResourceBundle.getBundle("ghm.followgui.FollowAppResourceBundle");
        }
        return bundle_;
    }
}
