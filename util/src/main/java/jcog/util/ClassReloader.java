package jcog.util;

///** https://github.com/natanbc/reloader */
//public class ClassReloader {
//    private final ClassLoader parent;
//    private final List<Unloadable> instanceUnloaders = new FasterList<>();
//    final List<Unloadable> classUnloaders = new FasterList<>();
//    final Object lock = new Object();
//    ReloaderClassLoader currentLoader;
//
//    public ClassReloader(ClassLoader parent) {
//        this.parent = Objects.requireNonNull(parent, "parent");
//        this.currentLoader = new ReloaderClassLoader(this, parent);
//    }
//
//    public ClassReloader() {
//        this(ClassLoader.getSystemClassLoader());
//    }
//
//    /**
//     * Loads a class by name.
//     *
//     * @param name The class name.
//     *
//     * @return The loaded class.
//     *
//     * @throws RuntimeException if the class can't be loaded.
//     */
//    public Class<?> loadClass(String name) {
//        synchronized(lock) {
//            try {
//                return currentLoader.loadClass(name);
//            } catch(ClassNotFoundException e) {
//                throw new RuntimeException(e);
//            }
//        }
//    }
//
//    /**
//     * Loads a class by name and returns a new instance of it using a specific constructor. If the class implements {@link Unloadable} it will be saved to an internal list so it can be notified
//     * when {@link #unload() unload()} is called.
//     *
//     * @param className The class name.
//     * @param argTypes The classes of the constructor arguments.
//     * @param args The constructor arguments.
//     *
//     * @return A new instance of the class.
//     *
//     * @throws RuntimeException if there's an error loading the class or creating the instance.
//     */
//    public <X> X newInstance(String className, Class<?>[] argTypes, Object[] args) {
//        synchronized(lock) {
//            try {
//                Class<?> cls = currentLoader.loadClass(className);
//                Constructor<?> ctor = cls.getDeclaredConstructor(argTypes);
//                ctor.setAccessible(true);
//                Object obj = ctor.newInstance(args);
//                if(obj instanceof Unloadable) instanceUnloaders.addAt((Unloadable)obj);
//                return (X) obj;
//            } catch(ClassNotFoundException | IllegalAccessException | InstantiationException | InvocationTargetException | NoSuchMethodException e) {
//                throw new RuntimeException(e);
//            }
//        }
//    }
//
//    /**
//     * Loads a class by name and returns a new instance of it using a zero arg constructor. If the class implements {@link Unloadable} it will be saved to an internal list so it can be notified
//     * when {@link #unload() unload()} is called.
//     *
//     * @param name The class name.
//     *
//     * @return A new instance of the class.
//     *
//     * @see #newInstance(String, Class[], Object[])
//     */
//    public <X> X newInstance(String name) {
//        return (X) newInstance(name, new Class[0], new Object[0]);
//    }
//
//    /**
//     * Replaces the current class loader and notifies loaded classes and unloadable objects created by {@link #newInstance(String, Class[], Object[])} to free resources.
//     *
//     * @see #newInstance(String, Class[], Object[])
//     *
//     * @throws RuntimeException if an exception is thrown on the unload process and {@link #onUnloadError(Unloadable, Throwable) onUnloadError()} returns {@code false}.
//     *
//     * @see #onUnloadError(Unloadable, Throwable)
//     */
//    public void unload() {
//        synchronized(lock) {
//            currentLoader = new ReloaderClassLoader(this, parent);
//            unload(instanceUnloaders);
//            unload(classUnloaders);
//        }
//    }
//
//    private void unload(List<Unloadable> list) {
//        for(Iterator<Unloadable> it = list.iterator(); it.hasNext();) {
//            Unloadable u = it.next();
//            try {
//                u.unload();
//            } catch(Throwable t) {
//                if(!onUnloadError(u, t)) throw new RuntimeException(t);
//            }
//            it.remove();
//        }
//    }
//
//    /**
//     * Called when an unloader method throws an exception.
//     *
//     * @param object The object that threw the exception.
//     * @param cause The exception thrown.
//     *
//     * @return {@code true} if the unload process should continue.
//     */
//    @SuppressWarnings({"unused", "WeakerAccess"})
//    protected boolean onUnloadError(Unloadable object, Throwable cause) {
//        return true;
//    }
//
//
//    /**
//     * Marks a method as an unloader.
//     * <br>Unloader methods will be called by {@link Reloader Reloader} when their classes need to be unloaded so they
//     * can free resources.
//     * <br>Unloader methods <b>must</b> be static and take zero arguments.
//     */
//    @Retention(RetentionPolicy.RUNTIME)
//    @Target(ElementType.METHOD)
//    @SuppressWarnings("WeakerAccess")
//    public @interface Unloader {
//    }
//
//    /**
//     * Objects that implement this interface will be saved and notified by their {@link Reloader Reloader} when they should
//     * free resources. Only objects created using {@link Reloader#load(String, Class[], Object[])} will be notified.
//     */
//    public interface Unloadable {
//        void unload() throws Exception;
//    }
//
//    static class ReloaderClassLoader extends ClassLoader {
//        private final Map<String, Class<?>> map = new HashMap<>();
//        private final ClassReloader reloader;
//
//        ReloaderClassLoader(ClassReloader reloader, ClassLoader parent) {
//            super(parent);
//            this.reloader = reloader;
//        }
//
//        @Override
//        protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
//            return findClass(name);
//        }
//
//        private Class<?> getClass(String name) throws ClassNotFoundException {
//            Class<?> cls = map.get(name);
//            if(cls != null) return cls;
//            try {
//                cls = super.findClass(name);
//                map.put(name, cls);
//                return cls;
//            } catch(ClassNotFoundException ignored) {}
//            ByteArrayOutputStream baos = new ByteArrayOutputStream();
//            try(InputStream i = getResourceAsStream(name)) {
//                byte[] buffer = new byte[1024];
//                int r;
//                while((r = i.read(buffer)) != -1) baos.write(buffer, 0, r);
//            } catch(IOException e) {
//                throw new ClassNotFoundException("Error reading class bytes", e);
//            }
//            byte[] bytes = baos.toByteArray();
//            Class<?> c = defineClass(null, bytes, 0, bytes.length, new ProtectionDomain(
//                    new CodeSource(getResource(name), (Certificate[])null),
//                    null
//            ));
//            map.put(name, c);
//            return c;
//        }
//
//        @Override
//        public Class<?> findClass(String name) throws ClassNotFoundException {
//            Class<?> cls = getClass(name);
//            if(reloader.currentLoader == this) {
//                for(final Method method : cls.getMethods()) {
//                    if(method.getAnnotation(Unloader.class) != null) {
//                        if(!Modifier.isStatic(method.getModifiers())) {
//                            throw new ClassNotFoundException("Unable to load class " + name, new ClassFormatError("Methods annotated with @Unloader must be static"));
//                        }
//                        if(method.getParameterTypes().length != 0) {
//                            throw new ClassNotFoundException("Unable to load class " + name, new ClassFormatError("Methods annotated with @Unloader must take zero arguments"));
//                        }
//                        synchronized(reloader.lock) {
//                            reloader.classUnloaders.addAt(new Unloadable() {
//                                @Override
//                                public void unload() throws Exception {
//                                    method.invoke(null);
//                                }
//                            });
//                        }
//                    }
//                }
//            }
//            return cls;
//        }
//    }
//
//}

import com.google.common.collect.Iterables;
import jcog.Str;

import java.io.*;
import java.lang.reflect.Field;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.lang.Boolean.TRUE;

//import java.util.Iterator;

/**
 * from: https://github.com/saiema/JavaClassReloader
 *
 * This class is used to reload and re-link classes.
 * <p>
 * <p>
 * Features of this class includes:
 * <li>maintaining a cache of classes to be reloaded</li>
 * <li>defining a priority path to load classes first from this one</li>
 * <li>reloading and re-linking a class and all classes marked as reloadable</li>
 * <li>instead of requiring to load a class as reloadable this version allows to just mark a class as reloadable</li>
 * <li>mark all classes in a folder and set this folder as priority path</li>
 * <li>Allows to define path per class, this avoids the need to move .class files</li>
 * <p>
 * <p>
 * Main changes from previous version:
 * <li>Old Reloaders are freed to improve performance</li>
 * <li>Classes byte code are stored to avoid reloading classes that don't change</li>
 * <p>
 *
 * @author Simon Emmanuel Gutierrez Brida
 * @version 2.5.1
 */
public class ClassReloader extends ClassLoader {
    protected final Set<String> classpath;
    protected String priorityPath;
    protected Set<String> reloadableCache;
    protected Map<String, Boolean> reloadedClasses;
    protected final List<Class<?>> reloadableClassCache;
    protected ClassReloader child;
    private int reloadersCreated = 0;
    private final ByteCodeContainer byteCodeContainer;
    public static final int MAX_RELOADERS_BEFORE_CLEANING = 150;

    /**
     * This map allows to define a specific path for each class
     */
    protected Map<String, String> specificClassPaths;

//    public ClassReloader()  {
//        this(()-> {
//                    try {
//                        return ClassPath.from(
//                                ClassLoader.getSystemClassLoader()).getAllClasses().stream().map(ClassPath.ClassInfo::getName).iterator();
//                    } catch (IOException e) {
//                        throw new RuntimeException(e);
//                    }
//                },
//                ClassLoader.getSystemClassLoader());
//    }


    public static ClassReloader inClassPathOf(Class clazz) throws URISyntaxException {

        URI pkgLocal = clazz.getResource(".").toURI();

        int depth = Str.countRows(clazz.getPackage().getName(), '.')+1;
        for  (int i = 0; i < depth; i++) {
            pkgLocal = pkgLocal.resolve("..");
        }

        File f = new File(pkgLocal);
        assert(f.exists());
        String cp = f.getAbsolutePath() + '/';

        ClassReloader r = new ClassReloader(List.of(cp), Thread.currentThread().getContextClassLoader());
        //r.markEveryClassInFolderAsReloadable(f.getAbsolutePath());
        return r;
    }

    public ClassReloader(String classpath) {
        this(List.of(classpath), ClassLoader.getSystemClassLoader());
    }

    public ClassReloader(Iterable<String> classpath, ClassLoader parent) {
        super(parent);
        if (parent instanceof ClassReloader) {
            this.reloadersCreated = ((ClassReloader)parent).reloadersCreated + 1;
            this.byteCodeContainer = ((ClassReloader)parent).byteCodeContainer;
            initReloadedClasses(((ClassReloader)parent).reloadedClasses);
        } else {
            this.byteCodeContainer = new ByteCodeContainer();
            initReloadedClasses();
        }

        this.classpath = new TreeSet<>();
        Iterables.addAll(this.classpath, classpath);

        this.reloadableCache = new TreeSet<>();
        this.reloadableClassCache = new LinkedList<>();
        this.specificClassPaths = new TreeMap<>();

    }

    private void initReloadedClasses() {
        this.reloadedClasses = new TreeMap<>();
        if (this.reloadableCache == null) return;
        for (String rc : this.reloadableCache) {
            this.reloadedClasses.put(rc, Boolean.FALSE);
        }
    }

    private void initReloadedClasses(Map<String, Boolean> parentsReloadedClasses) {
        this.reloadedClasses = new TreeMap<>();
        for (Entry<String, Boolean> rc : parentsReloadedClasses.entrySet()) {
            this.reloadedClasses.put(rc.getKey(), Boolean.FALSE);
        }
    }

    private ClassReloader(Set<String> classpath, ClassLoader parent, Set<String> reloadableCache, Map<String, String> specificClassPaths) {
        this(classpath, parent);
        this.reloadableCache = reloadableCache;
        this.specificClassPaths = specificClassPaths;
    }

    private ClassReloader(Set<String> classpath, ClassLoader parent) {
        super(parent);
        if (parent instanceof ClassReloader) {
            this.reloadersCreated = ((ClassReloader)parent).reloadersCreated + 1;
            this.byteCodeContainer = ((ClassReloader)parent).byteCodeContainer;
            initReloadedClasses(((ClassReloader)parent).reloadedClasses);
        } else {
            this.byteCodeContainer = new ByteCodeContainer();
            initReloadedClasses();
        }
        this.classpath = classpath;
        this.reloadableClassCache = new LinkedList<>();
    }

    public void setSpecificClassPath(String className, String path) {
        this.specificClassPaths.put(className, path);
    }

    private ClassReloader cleanReloader() {
        Set<String> childReloadableCache = new TreeSet<>(this.reloadableCache);
        ClassLoader firstClassloader = getFirstClassLoader();
        ClassReloader cleanSlate = new ClassReloader(this.classpath, firstClassloader, childReloadableCache, this.specificClassPaths);
        cleanSlate.reloadersCreated = 1;
        unlinkPreviousReloadersAndLinkWithFirstReloader(firstClassloader, cleanSlate);
        return cleanSlate;
    }

    private void unlinkPreviousReloadersAndLinkWithFirstReloader(ClassLoader until, ClassReloader newReloader) {
        ClassLoader current = this; //this reloader is the only one to be left unchanged
        while (current instanceof ClassReloader && current != until) {
            if (current != this) {
                ((ClassReloader)current).child = null;
            }
            ClassLoader parent = current.getParent();
            Field[] fields = ClassLoader.class.getDeclaredFields();
            Field parentField = null;
            for (Field f : fields) {
                if (f.getName().compareTo("parent") == 0) {
                    parentField = f;
                    break;
                }
            }
            if (parentField != null) {
                parentField.setAccessible(true);
                try {
                    parentField.set(current, null);
                } catch (IllegalArgumentException | IllegalAccessException e) {
                    e.printStackTrace();
                }
                parentField.setAccessible(false);
            }
            current = parent;
        }
        if (until instanceof ClassReloader) {
            ((ClassReloader)until).child = newReloader;
        }
    }

    private ClassLoader getFirstClassLoader() {
        ClassLoader fcl = this;
        while (fcl.getParent() instanceof ClassReloader) {
            fcl = fcl.getParent();
        }
        return fcl;
    }

    @Override
    public Class<?> loadClass(String s) throws ClassNotFoundException {
        Class<?> clazz = this.reloadableCache.contains(s)?retrieveFromCache(s):null;
        if (clazz == null) {
            if (this.getParent() != null) {
                try {
                    clazz = this.getParent().loadClass(s);
                } catch (ClassNotFoundException e) {}
            }
            if (clazz == null) {
                clazz = findClass(s);
            }
        }
        if (this.reloadableCache.contains(s)) {
            addToCache(clazz);
            reloadedClasses.computeIfPresent(s,(x,y)->TRUE);
//            if (this.reloadedClasses.containsKey(s)) {
//                this.reloadedClasses.put(s, Boolean.TRUE);
//            }
        }
        return clazz;
    }

    public void markClassAsReloadable(String s) {
        this.reloadableCache.add(s);
        this.reloadedClasses.put(s, Boolean.FALSE);
    }

    public Class<?> loadClassAsReloadable(Class c) throws ClassNotFoundException {
        return loadClassAsReloadable(c.getName());
    }

    public Class<?> loadClassAsReloadable(String s) throws ClassNotFoundException {
        reloadableCache.add(s);
        Class<?> clazz = loadClass(s);
        if (clazz != null) {
            addToCache(clazz);
            markClassAsReloadable(s);
        }
        return clazz;
    }

    public Class<?> reloadClass(Class c) throws ClassNotFoundException {
        return reloadClass(c.getName());
    }

    public Class<?> reloadClass(String s) throws ClassNotFoundException {
        return reloadClass(s, true);
    }

    private Class<?> reloadClass(String s, boolean reload) throws ClassNotFoundException {
        Class<?> clazz = null;
        if (reload) {
            this.byteCodeContainer.eliminateClass(s);
            clazz = reload(s);
        }
        if (clazz == null) {
            clazz = loadClass(s);
        }
        if (clazz != null && reload) {
            addToCache(clazz);
            markClassAsReloadable(s);
        }
        return clazz;
    }

    public void setPathAsPriority(String path) {
        this.priorityPath = path;
    }

    public void markEveryClassInFolderAsReloadable(String folder) {
        markEveryClassInFolderAsReloadable(folder, null);
    }

    public void markEveryClassInFolderAsReloadable(String folder, Set<String> allowedPackages) {
        cleanIfRescaning(folder);
        File pathFile = new File(folder);
        if (pathFile.exists() && pathFile.isDirectory()) {
            setPathAsPriority(folder);
            crawlAndMark(pathFile, "", allowedPackages);
        }
    }

    private void crawlAndMark(File dir, String pkg, Set<String> allowedPackages) {
        File[] files = dir.listFiles();
        for (File file : files) {
            if (file.getName().startsWith(".")) {
                continue;
            } else if (file.isFile() && file.getName().endsWith(".class")) {
                if (allowedPackages != null && !allowedPackages.contains(pkg)) {
                    continue;
                }
                this.markClassAsReloadable(ClassReloader.getClassName(file, pkg));
            } else if (file.isDirectory()) {
                String newPkg;
                newPkg = pkg.isEmpty() ? file.getName() : pkg + '.' + file.getName();
                crawlAndMark(file, newPkg, allowedPackages);
            }
        }
    }

    private static String getClassName(File file, String pkg) {
        String classSimpleName = file.getName();
        int lastDotIdx = classSimpleName.lastIndexOf('.');
        String className = classSimpleName.substring(0, lastDotIdx);
        if (!pkg.isEmpty()) {
            className = pkg + '.' + className;
        }
        return className;
    }

    protected Class<?> loadAgain(String s) throws ClassNotFoundException {
        Class<?> clazz = null;
        clazz = classExist(s, this.classpath.toArray(new String[0])) ? findClass(s) : loadClassAsReloadable(s);
        return clazz;
    }

    protected Class<?> reload(String s) throws ClassNotFoundException {
        ClassReloader r = newReloader();
        for (String c : this.reloadableCache) {
            if (c.compareTo(s) != 0) {
                Class<?> newClass = null;
                newClass = r.reloadedClasses.getOrDefault(c, false) ? r.loadClass(c) : r.loadAgain(c);
                r.addToCache(newClass);
                //Class<?> newClass = r.loadAgain(c);
                //r.addToCache(newClass);
            }
        }
        Class<?> clazz = r.loadAgain(s);
        this.child = r;
        r.addToCache(clazz);
        return clazz;
    }

    private ClassReloader newReloader() {
        if (this.reloadersCreated == ClassReloader.MAX_RELOADERS_BEFORE_CLEANING) {
            return this.cleanReloader();
        } else {
            Set<String> childReloadableCache = new TreeSet<>(this.reloadableCache);
            return new ClassReloader(this.classpath, this, childReloadableCache, this.specificClassPaths);
        }
    }

    protected Class<?> retrieveFromCache(String s) {
        for (Class<?> c : this.reloadableClassCache) {
            if (c.getName().compareTo(s) == 0) {
                return c;
            }
        }
        return null;
    }

    @Override
    public Class<?> findClass(String s) throws ClassNotFoundException {
        try {
            byte[] bytes = loadClassData(s);
            Class<?> clazz = this.defineClass(s, bytes, 0, bytes.length);
            resolveClass(clazz);
            return clazz;
        } catch (IOException ioe) {
            throw new ClassNotFoundException("unable to find class " + s, ioe);
        } catch (IllegalAccessError e) {
            return getFirstClassLoader().loadClass(s);
        }
    }

    protected byte[] loadClassData(String className) throws IOException {
        boolean found = false;
        File f = null;
        if (this.specificClassPaths.containsKey(className)) {
            String specificPath = this.specificClassPaths.get(className);
            f = new File(specificPath + className.replaceAll("\\.", File.separator) + ".class");
            if (f != null && f.exists()) {
                found = true;
            }
        }
        if (!found) {
            if (this.priorityPath != null) f = new File(this.priorityPath + className.replaceAll("\\.", File.separator) + ".class");
            if (f != null && f.exists()) {
                found = true;
            }
        }
        if (!found) {
            for (String cp : this.classpath) {
                f = new File(cp + className.replaceAll("\\.", File.separator) + ".class");
                found = f.exists();
                if (found) break;
            }
        }
        if (!found) {
            throw new IOException("File " + className + " doesn't exist\n");
        }
        byte[] classDef = this.byteCodeContainer.loadByteCodeFile(f, className);
        return classDef;
    }

    private void addToCache(Class<?> clazz) {
        boolean found = false;
        int i = 0;
        for (Class<?> c : this.reloadableClassCache) {
            found = c.getName().compareTo(clazz.getName()) == 0;
            if (found) break;
            i++;
        }
        if (found) this.reloadableClassCache.remove(i);
        this.reloadableClassCache.add(clazz);
    }

    public ClassReloader getChild() {
        return this.child;
    }

    public ClassReloader getLastChild() {
        ClassReloader lastChild = this;
        while (lastChild.child != null) {
            lastChild = lastChild.child;
        }
        return lastChild;
    }

    private boolean classExist(String s, String[] classpath) {
        boolean found = false;
        File f = null;
        for (String cp : classpath) {
            f = new File(cp + s.replaceAll("\\.", File.separator) + ".class");
            found = this.byteCodeContainer.byteCodeExist(f);
            if (found) break;
        }
        return found;
    }

    private void cleanIfRescaning(String folder) {
        File pathFile = new File(folder);
        if (pathFile.exists() && pathFile.isDirectory()) {
            if (this.priorityPath != null && this.priorityPath.compareTo(folder) == 0) {
                cleanDeletedClasses();
            } else if (this.classpath.contains(folder)) {
                cleanDeletedClasses();
            } else if (this.specificClassPaths.containsKey(folder)) {
                this.reloadableCache.remove(this.specificClassPaths.get(folder));
                this.specificClassPaths.remove(folder);
            }
        }
    }

    private void cleanDeletedClasses() {
        Set<String> cleanedClasses = new TreeSet<>();
        for (String c : this.reloadableCache) {
            if (classExists(c)) {
                cleanedClasses.add(c);
            } else {
                this.byteCodeContainer.eliminateClass(c);
            }
        }
        this.reloadableCache = cleanedClasses;
    }

    private boolean classExists(String className) {
        boolean found = false;
        File f = null;
        for (String cp : classpath) {
            f = new File(cp + className.replaceAll("\\.", File.separator) + ".class");
            found = f.exists() && f.isFile();
            if (found) break;
        }
        return found;
    }

    private static class ByteCodeContainer {
        private final Map<String, byte[]> classByteCodeMap;
        private final Map<String, String> filePerClass;
        private final Set<String> classesToReload;
        private final boolean verifyFileChanges;
        public static final boolean reuseByteCode = true;

        private ByteCodeContainer(Map<String, byte[]> classByteCodeMap, Set<String> classesToReload, boolean verifyFileChanges) {
            this.classByteCodeMap = classByteCodeMap;
            this.classesToReload = classesToReload;
            this.verifyFileChanges = verifyFileChanges;
            this.filePerClass = new TreeMap<>();
        }

        ByteCodeContainer() {
            this(new TreeMap<>(), new TreeSet<>(), false);
        }

        ByteCodeContainer(Set<String> classesToReload) {
            this(new TreeMap<>(), classesToReload, false);
        }

        ByteCodeContainer(boolean verifyFileChanges) {
            this(new TreeMap<>(), new TreeSet<>(), verifyFileChanges);
        }

        public void eliminateClass(String className) {
            String classFilePath = this.filePerClass.remove(className);
            if (classFilePath != null) this.classByteCodeMap.remove(classFilePath);
        }

        public byte[] loadByteCodeFile(File file) throws IOException {
            return loadByteCodeFile(file, null);
        }

        public byte[] loadByteCodeFile(File file, String asClass) throws IOException {
            if (asClass != null && this.filePerClass.containsKey(asClass)) {
                String associatedPath = this.filePerClass.get(asClass);
                if (associatedPath.compareTo(file.getAbsolutePath()) != 0) {
                    this.classByteCodeMap.remove(associatedPath);
                    this.filePerClass.put(asClass, file.getAbsolutePath());
                }
            } else if (asClass != null) {
                this.filePerClass.put(asClass, file.getAbsolutePath());
            }
            String key = file.getAbsolutePath();
            if (ByteCodeContainer.reuseByteCode && this.classByteCodeMap.containsKey(key)) {
                if (this.classesToReload.contains(key)) {
                    byte[] buff = loadFile(file);
                    this.classByteCodeMap.put(key, buff);
                    return buff;
                } else if (this.verifyFileChanges) {
                    byte[] lastBuff = this.classByteCodeMap.get(key);
                    byte[] currentBuff = JustCodeDigest.digest(file, false);
                    if (Arrays.equals(lastBuff, currentBuff)) {
                        return lastBuff;
                    } else {
                        this.classByteCodeMap.put(key, currentBuff);
                        return currentBuff;
                    }
                } else {
                    return this.classByteCodeMap.get(key);
                }
            } else {
                byte[] buff = loadFile(file);
                this.classByteCodeMap.put(key, buff);
                return buff;
            }
        }

        public boolean byteCodeExist(File file) {
            return this.classByteCodeMap.containsKey(file.getAbsolutePath()) || file.exists();
        }

        private static byte[] loadFile(File file) throws IOException {
            int size = (int) file.length();
            byte[] buff = new byte[size];
            FileInputStream fis = new FileInputStream(file);
            DataInputStream dis = new DataInputStream(fis);
            dis.readFully(buff);
            dis.close();
            return buff;
        }


        public String containerStatusAsString() {
            return "Container has " + this.classByteCodeMap.size() + " values";
        }

        @Override
        public String toString() {
            String res = "";
            res += "Reuse bytecode      : " + ByteCodeContainer.reuseByteCode + '\n';
            res += "Verify file changes : " + this.verifyFileChanges + '\n';
            res += "Classes to reload   : \n" + classesToReloadAsString(true) + '\n';
            res += "File per class      : \n" + filePerClassAsString(true) + '\n';
            res += "Bytecode per class  : \n" + byteCodePerClass(true, false) + '\n';
            return res;
        }

        public String classesToReloadAsString(boolean indent) {
            String res = "";
            Iterator<String> it = this.classesToReload.iterator();
            while (it.hasNext()) {
                res += indentation(indent) + it.next();
                if (it.hasNext()) res += "\n";
            }
            return res;
        }

        public String filePerClassAsString(boolean indent) {
            String res = "";
            Iterator<Entry<String, String>> it = this.filePerClass.entrySet().iterator();
            while (it.hasNext()) {
                Entry<String, String> entry = it.next();
                res += indentation(indent) + entry.getKey() + " : " + entry.getValue();
                if (it.hasNext()) res += "\n";
            }
            return res;
        }

        public String byteCodePerClass(boolean indent, boolean fullByteCode) {
            String res = "";
            Iterator<Entry<String, byte[]>> it = this.classByteCodeMap.entrySet().iterator();
            while (it.hasNext()) {
                Entry<String, byte[]> entry = it.next();
                String byteCodeAsString = fullByteCode? Arrays.toString(entry.getValue()):entry.getValue().toString();
                res += indentation(indent) + entry.getKey() + " : " + byteCodeAsString;
                if (it.hasNext()) res += "\n";
            }
            return res;
        }

        private static String indentation(boolean indent) {
            return indent ? "    " : "";
        }

    }
    /**
     * Utility class to obtain a md5 digest of a java file prior to strip all coments and whitespace
     *
     * @author Simón Emmanuel Gutiérrez Brida
     * @version 0.4
     */
    private enum JustCodeDigest {
        ;
        private static final Pattern COMMENT_JAVADOC = Pattern.compile("/\\*\\*(.*)\\*/");
        private static final Pattern COMMENT_MULTILINE = Pattern.compile("/\\*((?<!\\*).*)\\*/");
        private static final Pattern COMMENT_SINGLE = Pattern.compile("//.*");
        private static final Pattern WHITESPACE = Pattern.compile("\\s");
        private static boolean printExceptions = false;
        private static Exception lastException;

        public static void printExceptions(boolean b) {
            JustCodeDigest.printExceptions = b;
        }

        public static Exception getLastException() {
            return JustCodeDigest.lastException;
        }

        private static String getJustCode(String original) {
            Matcher javadoc = COMMENT_JAVADOC.matcher(original);
            Matcher comment_multi = COMMENT_MULTILINE.matcher(javadoc.replaceAll(""));
            Matcher comment_single = COMMENT_SINGLE.matcher(comment_multi.replaceAll(""));
            Matcher whitespace = WHITESPACE.matcher(comment_single.replaceAll(""));
            return whitespace.replaceAll("");
        }

        public static byte[] digest(String original) {
            return digest(original, true);
        }

        static final MessageDigest md5;

        static {
            try {
                md5 = MessageDigest.getInstance("MD5");
            } catch (NoSuchAlgorithmException e) {
                throw new RuntimeException(e);
            }
        }

        public static byte[] digest(String original, boolean justCode) {
            String code = justCode?getJustCode(original):original;
            InputStream is = new ByteArrayInputStream(code.getBytes());
            JustCodeDigest.lastException = null;
            DigestInputStream dis = null;
            try {
                dis = new DigestInputStream(is, md5);
                while (dis.read() != -1) {}
                return dis.getMessageDigest().digest();
            } /*catch (NoSuchAlgorithmException e) {
                if (JustCodeDigest.printExceptions) e.printStackTrace();
                JustCodeDigest.lastException = e;
            } */catch (IOException e) {
                if (JustCodeDigest.printExceptions) e.printStackTrace();
                JustCodeDigest.lastException = e;
            } finally {
                if (dis != null) {
                    try {
                        dis.close();
                    } catch (IOException e) {
                        if (JustCodeDigest.printExceptions) e.printStackTrace();
                        if (JustCodeDigest.lastException != null) {
                            Exception exc = new Exception(e);
                            exc.initCause(JustCodeDigest.lastException);
                            JustCodeDigest.lastException = exc;
                        } else {
                            JustCodeDigest.lastException = e;
                        }
                    }
                }
            }
            return null;
        }

        public static byte[] digest(File file) {
            return digest(file, true);
        }

        public static byte[] digest(File file, boolean justCode) {
            JustCodeDigest.lastException = null;
            try {
                byte[] bytes = Files.readAllBytes(file.toPath());
                String text = new String(bytes, StandardCharsets.UTF_8);
                return digest(text, justCode);
            } catch (IOException e) {
                if (JustCodeDigest.printExceptions) e.printStackTrace();
                JustCodeDigest.lastException = e;
            }
            return null;
        }

    }
}