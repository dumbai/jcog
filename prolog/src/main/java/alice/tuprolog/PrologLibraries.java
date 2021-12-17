/*
 * Created on 1-ott-2005
 *
 */
package alice.tuprolog;

import alice.tuprolog.event.LibraryEvent;
import alice.tuprolog.event.WarningEvent;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @author Alex Benini
 */
public class PrologLibraries {

    /* dynamically loaded built-in libraries */
    private final List<PrologLib> currentLibraries;

    /*  */
    private Prolog prolog;
    private Theories theories;
    private PrologPrimitives prims;
    private final HashMap<String, URL> externalLibraries = new HashMap<>();

    /**
     * @author Alessio Mercurio
     * <p>
     * This is the directory where optimized dex files should be written.
     * Is required to the DexClassLoader.
     */
    private String optimizedDirectory;

    PrologLibraries() {
        currentLibraries = new CopyOnWriteArrayList<>();
    }

    /**
     * Config this Manager
     */
    void start(Prolog vm) {
        prolog = vm;
        theories = vm.theories;
        prims = vm.prims;
    }

    /**
     * Loads a library.
     * <p>
     * If a library with the same name is already present, a warning event is
     * notified and the request is ignored.
     *
     * @param the name of the Java class containing the library to be loaded
     * @return the reference to the Library just loaded
     * @throws InvalidLibraryException if name is not a valid library
     */
    public synchronized PrologLib loadClass(String className)
            throws InvalidLibraryException {

        PrologLib alib = getLibrary(className);
        if (alib != null) {
            if (prolog.isWarning()) {
                prolog.notifyWarning(new WarningEvent(prolog,
                        "library " + alib.getName() + " already loaded."));
            }
            return alib;
        }

        try {
            PrologLib lib = (PrologLib) Class.forName(className).getConstructor().newInstance();
            bindLibrary(lib);
            prolog.notifyLoadedLibrary(new LibraryEvent(prolog, className));
            return lib;
        } catch (Exception e) {
//            e.printStackTrace();
//        } catch (Exception ex) {
            throw new InvalidLibraryException(className, -1, -1);
        }

    }

    /**
     * Loads a library.
     * <p>
     * If a library with the same name is already present, a warning event is
     * notified and the request is ignored.
     *
     * @param the name of the Java class containing the library to be loaded
     * @param the list of the paths where the library may be contained
     * @return the reference to the Library just loaded
     * @throws InvalidLibraryException if name is not a valid library
     */
    public synchronized PrologLib loadClass(String className, String... paths) throws InvalidLibraryException {
        PrologLib lib = null;

        try {
            /**
             * @author Alessio Mercurio
             *
             * Dalvik Virtual Machine
             */
            ClassLoader loader;
            if ("Dalvik".equals(System.getProperty("java.vm.name"))) {
                /*
                 * Only the first path is used. Dex file doesn't contain .class files
                 * and therefore getResource() method can't be used to locate the files at runtime.
                 */

                String dexPath = paths[0];

                /**
                 * Description of DexClassLoader
                 * A class loader that loads classes from .jar files containing a classes.dex entry.
                 * This can be used to execute code not installed as part of an application.
                 * @param dexPath jar file path where is contained the library.
                 * @param optimizedDirectory directory where optimized dex files should be written; must not be null
                 * @param libraryPath the list of directories containing native libraries, delimited by File.pathSeparator; may be null
                 * @param parent the parent class loader
                 */
                /**
                 * Here before we were using directly the class DexClassLoader referencing android.jar that
                 * contains all the stub classes of Android.
                 * This caused the need to have the file android.jar in the classpath even during the execution
                 * on the Java SE platform even if it is clearly useless. Therefore we decided to remove this
                 * reference and instantiate the DexClassLoader through reflection.
                 * This is simplified by the fact that, a part the constructor, we do not use any specific method
                 * of DexClassLoader but we use it as any other ClassLoader.
                 * A similar approach has been adopted also in the class AndroidDynamicClassLoader.
                 */
                loader = (ClassLoader) Class.forName("dalvik.system.DexClassLoader")
                        .getConstructor(String.class, String.class, String.class, ClassLoader.class)
                        .newInstance(dexPath, optimizedDirectory, null, getClass().getClassLoader());
                lib = (PrologLib) Class.forName(className, true, loader).getConstructor().newInstance();
            } else {
                URL[] urls = new URL[paths.length];

                for (int i = 0; i < paths.length; i++) {
                    File file = new File(paths[i]);
                    if (paths[i].contains(".class"))
                        file = new File(paths[i].substring(0,
                                paths[i].lastIndexOf(File.separator) + 1));
                    urls[i] = (file.toURI().toURL());
                }

                if (!"IKVM.NET".equals(System.getProperty("java.vm.name"))) {
                    loader = URLClassLoader.newInstance(urls, getClass()
                            .getClassLoader());
                    lib = (PrologLib) Class.forName(className, true, loader).getConstructor().newInstance();
                }


            }

            String name = lib.getName();
            PrologLib alib = getLibrary(name);
            if (alib != null) {
                if (prolog.isWarning()) {
                    String msg = "library " + alib.getName()
                            + " already loaded.";
                    prolog.notifyWarning(new WarningEvent(prolog, msg));
                }
                return alib;
            }
        }catch (NoSuchMethodException | InvocationTargetException e) {
            e.printStackTrace();
        } catch (Exception ex) {
            throw new InvalidLibraryException(className, -1, -1);
        }

        /**
         * @author Alessio Mercurio
         *
         * Dalvik Virtual Machine
         */
        if ("Dalvik".equals(System.getProperty("java.vm.name"))) {
            try {
                /*
                 * getResource() can't be used with dex files.
                 */

                File file = new File(paths[0]);
                URL url = (file.toURI().toURL());
                externalLibraries.put(className, url);
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
        } else {
            externalLibraries.put(className, getClassResource(lib.getClass()));
        }

        bindLibrary(lib);
        LibraryEvent ev = new LibraryEvent(prolog, lib.getName());
        prolog.notifyLoadedLibrary(ev);
        return lib;
    }

    /**
     * Loads a specific instance of a library.
     * <p>
     * If a library of the same class is already present, a warning event is
     * notified. Then, the current instance of that library is discarded, and
     * the new instance gets loaded.
     *
     * @param lib the (Java class) name of the library to be loaded
     * @throws InvalidLibraryException if name is not a valid library
     */
    public synchronized void load(PrologLib lib) throws InvalidLibraryException {
        String name = lib.getName();
        PrologLib alib = getLibrary(name);
        if (alib != null) {
            if (prolog.isWarning())
                prolog.notifyWarning(new WarningEvent(prolog, "library " + alib.getName() + " already loaded."));

            unload(name);
        }
        bindLibrary(lib);
        prolog.notifyLoadedLibrary(new LibraryEvent(prolog, lib.getName()));
    }


    /**
     * Unloads a previously loaded library
     *
     * @param name of the library to be unloaded
     * @throws InvalidLibraryException if name is not a valid loaded library
     */
    public synchronized void unload(String name) throws InvalidLibraryException {
        boolean found = currentLibraries.removeIf(lib -> {
            if (lib.getName().equals(name)) {
                lib.dismiss();
                prims.stop(lib);
                return true;
            }
            return false;
        });

        if (!found)
            throw new InvalidLibraryException();

        externalLibraries.remove(name);

        theories.removeLibraryTheory(name);
        theories.rebindPrimitives();

        prolog.notifyUnloadedLibrary(new LibraryEvent(prolog, name));
    }

    /**
     * Binds a library.
     *
     * @param lib is library object
     * @return the reference to the Library just loaded
     * @throws InvalidLibraryException if name is not a valid library
     */
    private PrologLib bindLibrary(PrologLib lib) throws InvalidLibraryException {
        try {
            String name = lib.getName();
            lib.setProlog(prolog);
            currentLibraries.add(lib);

            prims.start(lib);

            String th = lib.getTheory();
            if (th != null) {
                theories.consult(new Theory(th), false, name);
                theories.solveTheoryGoal();
            }

            theories.rebindPrimitives();

            return lib;
        } catch (InvalidTheoryException ex) {


            throw new InvalidLibraryException(lib.getName(), ex.line, ex.pos);
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new InvalidLibraryException(lib.getName(), -1, -1);
        }

    }

    /**
     * Gets the reference to a loaded library
     *
     * @param name the name of the library already loaded
     * @return the reference to the library loaded, null if the library is not
     * found
     */
    public PrologLib getLibrary(String name) {
        for (PrologLib alib : currentLibraries) {
            if (alib.getName().equals(name)) return alib;
        }
        return null;
    }

    public void onSolveBegin(Term g) {
        for (PrologLib alib : currentLibraries) {
            alib.onSolveBegin(g);
        }
    }

    public void onSolveHalt() {
        for (PrologLib currentLibrary : currentLibraries) {
            currentLibrary.onSolveHalt();
        }
    }

    public void onSolveEnd() {
        for (PrologLib currentLibrary : currentLibraries) {
            currentLibrary.onSolveEnd();
        }
    }

    public URL getExternalLibraryURL(String name) {
        return isExternalLibrary(name) ? externalLibraries.get(name) : null;
    }

    public boolean isExternalLibrary(String name) {
        return externalLibraries.containsKey(name);
    }

    private static URL getClassResource(Class<?> klass) {
        if (klass == null)
            return null;
        return klass.getClassLoader().getResource(
                klass.getName().replace('.', '/') + ".class");
    }

    /**
     * @author Alessio Mercurio
     * <p>
     * Used to set optimized directory required by the DexClassLoader.
     * The directory is created Android side.
     */

    public void setOptimizedDirectory(String optimizedDirectory) {
        this.optimizedDirectory = optimizedDirectory;
    }

}