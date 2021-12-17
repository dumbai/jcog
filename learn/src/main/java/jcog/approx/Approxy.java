package jcog.approx;

import jcog.Is;
import jcog.Research;
import jcog.Util;
import jcog.activation.SigmoidActivation;
import jcog.data.list.Lst;
import jcog.io.BinTxt;
import jcog.nn.MLP;
import jcog.pri.UnitPri;
import jcog.pri.bag.Bag;
import jcog.pri.bag.impl.hijack.PriPriHijackBag;
import jcog.pri.op.PriMerge;
import jcog.random.XoRoShiRo128PlusRandom;
import net.bytebuddy.ByteBuddy;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.dynamic.loading.ClassLoadingStrategy;
import net.bytebuddy.dynamic.scaffold.TypeValidation;
import net.bytebuddy.implementation.MethodDelegation;
import net.bytebuddy.implementation.bind.annotation.AllArguments;
import net.bytebuddy.implementation.bind.annotation.RuntimeType;
import net.bytebuddy.implementation.bind.annotation.SuperCall;
import net.bytebuddy.matcher.ElementMatchers;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

/** Approximate Computing Dynmaic Proxy and Runtime System */
@Research
@Is("Approximate_computing") public enum Approxy {
    ;

    /** inputs and outputs training data pairs; effectively a replay buffer of POJO vectors */
    public static class ActualExecution extends UnitPri {
        public final Object input;
        public final Object output;
        final int hash;

        public ActualExecution(Object input, Object output, float pri) {
            super(pri);
            this.input = input;
            this.output = output;
            this.hash = computeHash();
        }

        private int computeHash() {
            return Util.hashCombine(Arrays.hashCode((Object[])input), output);
        }

        @Override
        public String toString() {
            return BinTxt.toString(hash) + "\t$" + super.toString() + " " + Arrays.toString(((Object[])input)) + " |- " + output;
        }

        @Override
        public int hashCode() {
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (!(obj instanceof ActualExecution ae)) return false;

            if (hash != ae.hash) return false;

            return output.equals(ae.output) && Arrays.equals(((Object[])input), (Object[])ae.input);
        }
    }

    /** implements a particular way of approximating a function */
    @FunctionalInterface public interface Approximator extends Function<Object[], Object> {

    }

    /** inputs and output schema and data mapping */
    public abstract static class Approximation {

        static final int trainIterations = 10;

        final AtomicInteger iteration = new AtomicInteger();
        final Bag<ActualExecution,ActualExecution> experience = new PriPriHijackBag<>(PriMerge.plus, 64, 2);

        volatile Approximator approx;

        protected Approximation() {

        }

        protected void train() {
            MLP m = new MLP(3,
                new MLP.Dense(2, SigmoidActivation.the),
                new MLP.Dense(1, SigmoidActivation.the)
            );
            m.clear(new XoRoShiRo128PlusRandom());

            for (ActualExecution xy : experience) {
                //TODO m.put(xEncoded,yEncoded..)
            }
            approx = (x)-> experience.get(null /*encode(x)*/);
        }

        protected void learn(Object x, Object y) {

            experience.put(new ActualExecution(x,y,1f/experience.capacity()));

//            experience.print();
//            System.out.println();

            if (iteration.incrementAndGet() % trainIterations == 0) {
                //TODO more sophisticated condition
                train();
            }
        }
    }

    /** wraps a complete pure Method */
    public static class MethodApproximation extends Approximation {

        final Method m;


        protected MethodApproximation(Method m) {
            this.m = m;
            //TODO check for method purity
        }

        @RuntimeType
        public final Object intercept(@SuperCall Callable<?> zuper, @AllArguments Object[] x /*, @This Object obj */ /*, @SuperMethod Method method, */) throws Exception {
            Approximator a = approx;
            if (a==null) {
                //native execution mode
                Object y = zuper.call();
                learn(x, y);
                return y;
            } else {
                //approximation mode
                return a.apply(x);
            }
        }
    }

    /** model of class components being approximated */
    public static class ClassApproximation<X> {

        final Class<X> klass;
        public final List<Approximation> components = new Lst();
        private DynamicType.Builder<X> gen;

        public ClassApproximation(Class<X> klass) {

            this.klass = klass;
            this.gen = new ByteBuddy().with(TypeValidation.DISABLED).subclass(klass);
        }

        public ClassApproximation<X> method(String method, Class... types) throws NoSuchMethodException {
            return wrap(klass.getMethod(method, types));
        }

        public ClassApproximation<X> wrap(Method method) {
//                .method(ElementMatchers.named("sayFoo").or(ElementMatchers.named("sayBar")))
//                    .intercept(MethodDelegation.to(MyServiceInterceptor.class))
            MethodApproximation m = new MethodApproximation(method);
            components.add(m);
            gen = gen.method(ElementMatchers.isMethod().and(ElementMatchers.is(method)))
                    .intercept(MethodDelegation.to(new MethodApproximation(method)));

//                .intercept(InvocationHandlerAdapter.of(new InvocationHandler() {
//                    @Override
//                    public Object invoke(Object objWrapper, Method method, Object[] margs) throws Throwable {
//                        System.out.println("x");
//                        return null;
//                    }
//                }));

//            M.intercept(
//                    InvocationHandlerAdapter.of(new MethodApproximation(method))
//                    //InvokeDynamic.lambda()
////                    MethodDelegation.to(
////                        m, MethodApproximation.class
////                        //MyServiceInterceptor.class
////                    )
//            );
            return this;
        }

        public Class<? extends X> get() {
            return gen.make()

                  .load(Thread.currentThread().getContextClassLoader(),
                            ClassLoadingStrategy.Default.WRAPPER)
                    .getLoaded();
//            Reflection.newProxy(klass,  new InvocationHandler() {
//                @Override
//                public Object invoke(Object o, Method method, Object[] objects) throws Throwable {
//                    return o;
//                }
//            });
        }

        public X newInstance(Object... ctorArgs) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
            return get().getConstructor(Util.typesOfArray(ctorArgs)).newInstance(ctorArgs);
        }


//        public static class MyServiceInterceptor {
//            @RuntimeType
//            public static Object intercept(@SuperCall Callable<?> zuper) throws Exception {
//                return zuper.call();
//            }
//        }
    }


    public enum TestClass1 {
        ;

        public static float compute(float x, float y) {
            return x * y;
        }
    }

    public static void main(String[] args) throws Exception {

        TestClass1 c = new ClassApproximation<>(TestClass1.class)
                .method("compute", float.class, float.class)
                .newInstance();

        Random rng = new XoRoShiRo128PlusRandom(1);
        for (int i = 0; i < 100; i++) {
            float x = Util.round(rng.nextFloat(), 0.1f);
            float y = Util.round(rng.nextFloat(), 0.1f);
            float z = TestClass1.compute(x, y);
        }
    }
}

// works
//        TestClass1 c = new ByteBuddy().with(TypeValidation.DISABLED)
//                .subclass(TestClass1.class)
//                //.method(ElementMatchers.isPublic().and(ElementMatchers.not(ElementMatchers.isDeclaredBy(Object.class))))
//                .method(ElementMatchers.isMethod().and(ElementMatchers.is(m)))
//                .intercept(InvocationHandlerAdapter.of(new InvocationHandler() {
//                    @Override
//                    public Object invoke(Object objWrapper, Method method, Object[] margs) throws Throwable {
//                        System.out.println("x");
//                        return null;
//                    }
//                }))
//                .make()
//                .load(
//                        Thread.currentThread().getContextClassLoader(),
//                        WRAPPER
//                )
//                .getLoaded().newInstance();