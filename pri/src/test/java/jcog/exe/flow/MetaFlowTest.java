package jcog.exe.flow;

import jcog.WTF;
import org.junit.jupiter.api.Test;

import static jcog.exe.flow.MetaFlow.exe;

//import net.bytebuddy.ByteBuddy;
//import net.bytebuddy.ClassFileVersion;
//import net.bytebuddy.dynamic.loading.ClassLoadingStrategy;
//import net.bytebuddy.implementation.MethodDelegation;
//import net.bytebuddy.implementation.bind.annotation.AllArguments;
//import net.bytebuddy.implementation.bind.annotation.RuntimeType;
//import net.bytebuddy.implementation.bind.annotation.SuperCall;
//import net.bytebuddy.jar.asm.ClassReader;
//import net.bytebuddy.jar.asm.ClassVisitor;
//import net.bytebuddy.jar.asm.ClassWriter;
//import net.bytebuddy.jar.asm.MethodVisitor;
//import net.bytebuddy.matcher.ElementMatchers;
//import static net.bytebuddy.jar.asm.Opcodes.ASM7;

//import org.objectweb.asm.ClassReader;
//import org.objectweb.asm.ClassVisitor;
//import org.objectweb.asm.ClassWriter;
//import org.objectweb.asm.MethodVisitor;
//import static org.objectweb.asm.Opcodes.ASM7_EXPERIMENTAL;

class MetaFlowTest {

//    @Test
//    void testMetaFlow1() {
//
//
//        MetaFlow f = exe();
//        stack().reset().print();
//        System.out.println();
//
//
//    }

    private static void a() {
        exe().good((float) (Math.random()*1f));
    }
    private static int b() {
        if (Math.random() < 0.5f)
            exe().bad( 0.2f, "x" );
        else {
            if (Math.random() < 0.1f) {
                throw new WTF();
            } else {
                exe().bad(0.5f, "y");
            }
        }
        return 0;
    }

    private static Object c(int x) {
        a(); b();
        return null;
    }

    @Test
    void testMetaFlowExample() {

        MetaFlow m = exe().forkUntil(System.nanoTime() + 500L * 1_000_000L,
                MetaFlowTest::a,
                MetaFlowTest::b,
                () -> c(0));
        System.out.println(m.plan.prettyPrint());
    }

//    @Disabled
//    @Test
//    void testbyteBuddy() throws IllegalAccessException, InstantiationException {
//        Class<?> m = new ByteBuddy(ClassFileVersion.JAVA_V13).redefine(MyClass.class)
////
////        Class<?> m = new ByteBuddy(ClassFileVersion.JAVA_V11)
////                .subclass(MyClass.class)
////                //.rebase(NAR.class, ClassFileLocator.ForClassLoader.ofClassPath())
//                //.annotateType(AnnotationDescription.Builder.ofType(Baz.class).build())
//                .method(ElementMatchers.isAnnotatedWith(MetaFlow.Value.class)).
//
//                        intercept(MethodDelegation.to(new GeneralInterceptor()))
////                        intercept(InvocationHandlerAdapter.of((objWrapper, method, margs) -> {
////                            try {
////                                Method superMethod = objWrapper.getClass().getSuperclass().getDeclaredMethod(
////                                        method.getName(), Util.typesOfArray(margs));
////                                return superMethod.invoke(objWrapper, margs); //method.invoke(objWrapper, margs);
////                            } catch (Throwable t) {
////                                throw new RuntimeException(t);
////                            }
////                        }))
//                .make()
//
//                //.load(Thread.currentThread().getContextClassLoader(), ClassLoadingStrategy.Default.CHILD_FIRST)
//                .load(MyClass.class.getClassLoader(), ClassLoadingStrategy.Default.CHILD_FIRST)
//                .getLoaded();
//
//        Object mm = m.newInstance();
//
//        System.out.println(m);
//        System.out.println(mm);
//        System.out.println(mm.getClass() + " extends " + mm.getClass().getSuperclass());
////        mm.test();
////        mm.test(2);
//    }

//    static class GeneralInterceptor {
//        @RuntimeType
//        public static Object intercept(@AllArguments Object[] args,
//                                       //@Origin Method method
//                                       @SuperCall Callable<?> zuper
//        ) {
//            // intercept any method of any signature
//            try {
//                Object returnValue = zuper.call();
//                System.out.println(returnValue + " " + Arrays.toString(args));
//                return returnValue;
//            } catch (Exception e) {
//                throw new RuntimeException(e);
//            }
//        }
//    }
//
//    public static class MyClass {
//
//        public MyClass() {
//        }
//
//        @MetaFlow.Value
//        public static float test() {
//            return 1f;
//        }
//        @MetaFlow.Value
//        public static float test(float param) {
//            return param;
//        }
//    }
//
//    @Test
//    void testASMClassReader() throws IOException {
//        ClassReader cr = new ClassReader(MyClass.class.getName());
//        cr.accept(new ClassVisitor(ASM7) {
//            @Override
//            public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
//                super.visit(version, access, name, signature, superName, interfaces);
//                System.out.println(name);
//            }
//
//            @Override
//            public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
//                System.out.println(name);
//                return super.visitMethod(access, name, descriptor, signature, exceptions);
//            }
//        },0);
//        ClassWriter cw = new ClassWriter(cr, 0);
//        byte[] b = cw.toByteArray();
//        System.out.println(b.length + " "  + new String(b));
//
//    }

}