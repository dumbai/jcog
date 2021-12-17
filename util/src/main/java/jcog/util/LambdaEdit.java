package jcog.util;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.lang.invoke.SerializedLambda;
import java.lang.reflect.Method;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.function.Supplier;


/**
 * experiments for dynamic lambda mutation
 * https:
 */
public class LambdaEdit {
    static SerializedLambda getSerializedLambda(Serializable lambda) throws Exception {
        Method method = lambda.getClass().getDeclaredMethod("writeReplace");
        method.setAccessible(true);
        return (SerializedLambda) method.invoke(lambda);
    }

    static byte[] classByteCode(Class<?> c) {
        
        String n = c.getName();
        return classByteCode(n);
    }

    static byte[] classByteCode(String n) {
        try {
            File[] l = new File(LambdaEdit.class.getResource(".").toURI()).listFiles();
            System.out.println(Arrays.toString(l));
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        String name = "./jcog/util/" + n.replace('.', '/') + ".class";
        try (InputStream input = LambdaEdit.class.getResourceAsStream(name)) {
            if (input == null)
                return null;

            byte[] result = new byte[input.available()];
            input.read(result);
            return result;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    protected final byte[] loadOriginalBytecode(String originalResources, String name) throws IOException {
        try (InputStream is = getResourceStream(originalResources + name + ".class")) {
            return is.readAllBytes();
        }
    }
    private InputStream getResourceStream(String path) {
        return getClass().getClassLoader().getResourceAsStream(path);
    }

    final Supplier<Integer> MY_LAMBDA = (Supplier<Integer>&Serializable)() -> 1;

    {
        try {
            SerializedLambda sl = getSerializedLambda((Serializable) MY_LAMBDA);
            byte[] bc1 = classByteCode(sl.getImplMethodName());
            byte[] bc2 = classByteCode(sl.getImplClass());

            byte[] bytecode = classByteCode(MY_LAMBDA.getClass());
            System.out.println(Arrays.toString(bytecode));
            
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public static void main(String[] args) {

        new LambdaEdit();






    }





















    public static Method getLambdaMethod(SerializedLambda lambda) {
        try {
            String implClassName = lambda.getImplClass().replace('/', '.');
            Class<?> implClass = Class.forName(implClassName);

            String lambdaName = lambda.getImplMethodName();

            for (Method m : implClass.getDeclaredMethods()) {
                if (m.getName().equals(lambdaName)) {
                    return m;
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        throw new RuntimeException("Lambda Method not found");
    }
}
