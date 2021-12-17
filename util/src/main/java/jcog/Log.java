package jcog;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.PatternLayout;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.ConsoleAppender;
import ch.qos.logback.core.encoder.LayoutWrappingEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
//import org.slf4j.NDC;

public enum Log {
	;

    public static Logger log(String c) {
        return LoggerFactory.getLogger(c);
    }

    public static Logger log(Class c) {
        return LoggerFactory.getLogger(c);
    }


        public static final Logger ROOT;
    static {

        ROOT =  LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
        if (ROOT instanceof ch.qos.logback.classic.Logger) {
            initLogger(_root());
            (_root()).setLevel(Level.INFO);
        } else {
            //WTF SubstituteLogger
            //from JUnit?
        }
    }

    private static void initLogger(ch.qos.logback.classic.Logger _ROOT) {
        //return (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
//            Logger l = LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
//            return (ch.qos.logback.classic.Logger) l;

        ch.qos.logback.classic.Logger R = _ROOT;
        LoggerContext c = R.getLoggerContext();
        c.reset();

        ConsoleAppender<ILoggingEvent> ca = new ConsoleAppender<>();
        ca.setContext(c);
        ca.setName("*");

        PatternLayout layout = new PatternLayout();
        //layout.setPattern("%d{HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n");
        layout.setPattern(
            "%X{NDC0} %highlight(%.-1level) %logger - %msg%n"
            //"%highlight(%.-1level) %logger{36} - %msg [%thread]%n"
        );

        layout.setContext(c);
        layout.start();

        //ca.setImmediateFlush(false);
        ca.setLayout(layout);

        LayoutWrappingEncoder<ILoggingEvent> encoder = new LayoutWrappingEncoder<>();
        encoder.setContext(c);
        encoder.setLayout(layout);
        ca.setEncoder(encoder);
        ca.start();

        R.addAppender(ca);
    }
    public static void on() {
        try {
            _root().setLevel(Level.INFO);
        } catch (Exception e) { e.printStackTrace(); }
    }

    private static ch.qos.logback.classic.Logger _root() {
        return (ch.qos.logback.classic.Logger) ROOT;
    }
    public static void off() {
        try {
            _root().info("logging disabled");
            _root().setLevel(Level.ERROR);
        } catch (Exception e) { e.printStackTrace(); }
    }





//    /** https://logback.qos.ch/manual/receivers.html */
//    public static class LogSend  extends ch.qos.logback.classic.net.server.ServerSocketAppender {
//        //TODO
//    }
//
//    /** https://logback.qos.ch/manual/receivers.html */
//    public static class LogReceive extends ch.qos.logback.classic.net.server.ServerSocketReceiver {
//        //TODO
//    }

//    public static void enter(String zone) {
//        NDC.push(zone);
//    }
//
//    public static void exit() {
//        NDC.pop();
//    }

}