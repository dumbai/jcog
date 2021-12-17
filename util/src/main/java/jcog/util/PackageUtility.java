package jcog.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;

/**
 *
 */
public enum PackageUtility {
    ;

    public static List<Class> getClasses(String pkgName, boolean innerClasses) throws ClassNotFoundException {

        File directory = null;
        String pkgPath;
        try {
            ClassLoader cld = Thread.currentThread().getContextClassLoader();
            assert (cld != null);
            pkgPath = pkgName.replace('.', '/');
            URL resource = cld.getResource(pkgPath);
            if (resource == null)
                throw new ClassNotFoundException("No resource for " + pkgPath);
            directory = new File(resource.getFile());
        } catch (NullPointerException x) {
            throw new ClassNotFoundException(pkgName + " (" + directory + ") does not appear to be a valid package");
        }
        List<Class> classes = new ArrayList<>();
        if (directory.exists()) {
            
            String[] files = directory.list();
            
            for (String file : files) {
                            
                if ((!innerClasses) && (file.contains("$")))
                    continue;
                if (file.endsWith(".class")) {
                    
                        
                        classes.add(Class.forName(pkgName + '.'
                                + file.substring(0, file.length() - 6)));
                    }
                else {
                    classes.addAll(getClasses(pkgName + '.' + file, innerClasses));
                }
            }
        }
        
        if (!directory.exists()) {
            
            String jarPath = directory.toString().replace("!/" + pkgPath, "")
                    .replace("file:", "");
            
            jarPath = jarPath.replace("!\\" + pkgPath.replace("/", "\\"), "")
                    .replace("file:", "");
            try {
                classes.addAll( PackageUtility.getClasses(jarPath, pkgName) );
            } catch (FileNotFoundException caughtException) {
                throw new RuntimeException(
                        "Can not determine the location of the jar: "
                                + jarPath + ' ' + pkgName, caughtException);
            } catch (IOException caughtException) {
                throw new RuntimeException("IO error when opening jar: " + jarPath
                        + ' ' + pkgName, caughtException);
            }
        }
        return classes;
    }

    public static List<Class> getClasses(String jarName, String packageName) throws IOException {
        List<Class> classes = new ArrayList<>();

        String cleanedPackageName = packageName.replaceAll("\\.", "/");

        JarInputStream jarFile = new JarInputStream(new FileInputStream(
                jarName));

        while (true) {
            JarEntry jarEntry = jarFile.getNextJarEntry();
            if (jarEntry == null)
                break;
            if ((jarEntry.getName().startsWith(cleanedPackageName))
                    && (jarEntry.getName().endsWith(".class"))) {
                String classFileName = jarEntry.getName().replaceAll("/",
                        "\\.");
                try {
                    classes.add(Class.forName(classFileName.substring(0,
                            classFileName.length() - 6)));
                } catch (ClassNotFoundException caughtException) {
                    throw new FileNotFoundException(
                            "class not found, do you have the right jar file?");
                }
            }
        }

        jarFile.close();

        return classes;
    }
    
}
