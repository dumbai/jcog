//package jcog.util;
//
//import net.bytebuddy.ByteBuddy;
//import net.bytebuddy.dynamic.loading.ByteArrayClassLoader;
//import net.bytebuddy.dynamic.scaffold.TypeValidation;
//import net.bytebuddy.implementation.StubMethod;
//
//import java.lang.reflect.Method;
//import java.lang.reflect.Modifier;
//import java.lang.reflect.Type;
//import java.util.Arrays;
//import java.util.Map;
//
///** an patching/mocking/extending classloader which can be bootstrapped into
// *  by invoking the resulting classes in embedded context
// *  http:
// *  */
//public enum ClassPatch {
//    ;
//
////    /** http://bytebuddy.net/#/tutorial */
////    public static class TestByteBuddyAgent {
////
////        public static void main(String[] args) {
////            ByteBuddyAgent.install();
////            //Foo foo = new Foo();
////            new ByteBuddy()
////                    .redefine(String.class)
////                    .name("java.lang.StringX")
////                    .make()
////                    .load(String.class.getClassLoader(), ClassReloadingStrategy.fromInstalledAgent());
////            //assertThat(foo.m(), is("bar"));
////        }
////    }
//
//    public static void main(String[] args) throws Exception {
//
//
//
//        ClassLoader parentClassLoader =
//
//                Thread.currentThread().getContextClassLoader();
//
//
//
//
//
//
//        String entryClass = ClassPatch.class.getName();
//        Map<String, byte[]> overrides = Map.of(
//            entryClass,
//                new ByteBuddy().with(TypeValidation.DISABLED)
//
//                .redefine(ClassPatch.class)
//                .name(ClassPatch.class.getName())
//                .defineMethod("main2", void.class, Modifier.STATIC | Modifier.PUBLIC)
//                        .withParameters(new Type[] { String[].class })
//                        .intercept(StubMethod.INSTANCE)
//
//
//
//
//                .make().getBytes()
//        );
//
//        ClassLoader classloader =
//                new ByteArrayClassLoader.ChildFirst(parentClassLoader, overrides);
//
//        {
//            Thread.currentThread().setContextClassLoader(classloader);
//
//            Class mainClass = classloader.loadClass(entryClass);
//
//            System.out.println(Arrays.toString(mainClass.getMethods()));
//
//            Method main = mainClass.getMethod("main2", String[].class);
//
//            main.invoke(null, new Object[] {ArrayUtil.EMPTY_STRING_ARRAY});
//        }
//
//    }
//
//}