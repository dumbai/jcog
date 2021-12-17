package jcog.lab;

import com.google.common.io.Files;
import jcog.Log;
import jcog.Str;
import jcog.Util;
import jcog.table.ARFF;
import org.slf4j.Logger;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Optilive - continuously runnable live optimizer / analyzer
 *   --model, same as Optimize
 *   --parameters, same as Optimize
 *   --goals, same as Optimize
 *
 * initially starts idle with 0 active parameters, 0 active goals
 *
 * when at least 1 parameter and 1 goal are activated, it begins
 * results are collected into a streaming ARFF
 * results are analyzed by zero or more analysis engines, which can include:
 *     a) decision tree
 *     b) nars
 *     c) etc..
 * these derive analyses that are collected in separate logs for each
 *
 *
 * remote control interface thru JMX RPC or something
 */
public class Optilive<S,E>  {
    private static final Logger logger = Log.log(Optilive.class);

    private File outDir;

    transient Optimize<S,E> current;

    final Supplier<S> subj;
    final Function<Supplier<S>, E> experiment;

    final List<Goal<E>> goals;
    final List<Var<S, ?>> vars;
    final List<Sensor<E, ?>> sensors;
    private Thread thread;

    private static final long SLEEP_TIME_MS = 500;
    private long currentStart;

    Scientist sci;
    private volatile State state = State.Decide;
    private volatile State nextState;

    public Optilive(Supplier<S> subj, Function<Supplier<S>, E> experiment, List<Goal<E>> goals, List<Var<S, ?>> vars, List<Sensor<E, ?>> sensors) {


        this.subj = subj;

        this.experiment = experiment;
        this.goals = goals;
        this.vars = vars;
        this.sensors = sensors;

        if (outDir!=null) {
            if (!outDir.exists()) {
                outDir.mkdirs();
            }
        }

//        try {
//            //TODO configurable
//            outDir = Files.createTempDirectory(Optilive.class.getSimpleName() + '_' + experiment.toString());
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        }
    }

    /** persist to disk or network, cleanup */
    private void shutdown() {

    }

    /** run analyses and store results */
    private void analyze() {

        sci.analyze(current);

        current = null;
        nextState = State.Decide;
    }


    private void save() {
            try {
                //save
                String path = outDir + "/" +
                        current.varKey() + '.' + currentStart + ".arff";
                logger.info("save {}", path);
                new ARFF(current.data).writeToFile(path);
            } catch (IOException e) {
                e.printStackTrace();
            }

    }


    private enum State {
        Decide {
            @Override
            public <S,E> void run(Optilive<S, E> o) {
                o.decide();
            }
        },
        Execute {
            @Override
            public <S,E> void run(Optilive<S, E> o) {
               o.execute();
            }
        },
        Analyze {
            @Override
            public <S,E> void run(Optilive<S, E> o) {
                o.analyze();
            }
        },
        Sleep {
            @Override
            public <S,E> void run(Optilive<S, E> o) {
                Util.sleepMS(SLEEP_TIME_MS);
            }
        };

        public abstract <S,E> void run(Optilive<S,E> o);
    }

    private void decide() {
        current = (Optimize<S, E>) sci.next(subj, experiment);
        //logger.info("next experiment {} {}", current, current.varKey());
        nextState = State.Execute;
    }

    private void execute() {

        currentStart = System.nanoTime();
        logger.info("experiment start {}", new Date(currentStart));
        try {
            current.runSync(sci.experimentIterations());
        } catch (RuntimeException t) {
            logger.error("{}", t);
        } finally {
            long currentEnd = System.nanoTime();
            logger.info("experiment end {}\t({})", new Date(currentEnd), Str.timeStr((currentEnd - currentStart)));

            if (outDir!=null) {
                save();
            }
            nextState = State.Analyze;
        }

    }



    private void run() {

        logger.info("start");
        logger.info("goals: {}", goals);
        logger.info("vars: {}", vars);
        logger.info("sensors: {}", sensors);

        sci.start(goals, vars, sensors);

        do {
            try {
                state.run(this);
                state = nextState;
            } catch (ThreadDeath e) {
                break;
            } catch (RuntimeException e) {
                logger.info("{}", e);
            }
        } while (this.thread!=null);

        shutdown();
    }

    public void pause() {
        synchronized(thread) {
            nextState = State.Sleep;
            thread.interrupt();
        }
    }

    public void resume() {
        synchronized(thread) {
            nextState = State.Execute;
            thread.interrupt();
        }

    }

    public void stop() {
        synchronized(thread) {
            Thread thread = this.thread;
            this.thread = null;
            thread.interrupt();
            thread.stop();
            try {
                thread.join();
            } catch (InterruptedException e) {
                logger.error("{}", e);
            }
        }
    }

    /** default scientist, in-memory (no file saving) */
    public final void start() {
        start(new DefaultScientist(ThreadLocalRandom.current()), null);
    }

    /** saves to temporary directory */
    public void start(Scientist sci) {
        start(sci, Files.createTempDir().getAbsoluteFile());
    }

    public void start(Scientist sci, File outDir) {
        this.sci = sci;
        this.outDir = outDir;

        (this.thread = new Thread(this::run)).start();
    }

}