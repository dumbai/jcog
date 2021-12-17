package jcog.data.atomic;

import org.jctools.util.UnsafeAccess;
import sun.misc.Unsafe;

import java.lang.reflect.Field;
import java.util.concurrent.atomic.AtomicIntegerFieldUpdater;

@Deprecated public final class MetalAtomicIntegerFieldUpdater<T> extends AtomicIntegerFieldUpdater<T> {
    private static final Unsafe U = UnsafeAccess.UNSAFE;
    private final long offset;
//    private final VarHandle INT;


    //private final Class<?> cclass;
//        private final Class<T> tclass;

    public MetalAtomicIntegerFieldUpdater(Class<T> tclass, String fieldName) {

//            int modifiers;
        try {
            //field = AccessController.doPrivileged((PrivilegedExceptionAction<Field>)
            Field f = tclass.getDeclaredField(fieldName);
            f.setAccessible(true);
            this.offset = U.objectFieldOffset(f);

            //                modifiers = field.getModifiers();
            ////ReflectUtil.ensureMemberAccess(caller, tclass, (Object)null, modifiers);
            //ClassLoader cl = tclass.getClassLoader();
//                ClassLoader ccl = caller.getClassLoader();
            //if (ccl != null && ccl != cl && (cl == null || !isAncestor(cl, ccl))) {
            //  ReflectUtil.checkPackageAccess(tclass);
            //}
        } catch (Exception t) {
            throw new RuntimeException(t);
        }

//            if (field.getType() != Integer.TYPE) {
//                throw new IllegalArgumentException("Must be integer type");
//            } else if (!Modifier.isVolatile(modifiers)) {
//                throw new IllegalArgumentException("Must be volatile type");
//            } else {
        //this.cclass = Modifier.isProtected(modifiers) && tclass.isAssignableFrom(caller) && !isSamePackage(tclass, caller) ? caller : tclass;
//                this.tclass = tclass;
//            }

        //    private static final VarHandle INT;


//        try {
//            INT = MethodHandles.privateLookupIn(tclass, MethodHandles.lookup()).in(tclass)
//                    .unreflectVarHandle(field);
//                //.findVarHandle(tclass,fieldName,int.class);
//        } catch (Exception e) {
//            throw new WTF(e);
//        }

    }


    //        private final void throwAccessCheckException(T obj) {
//            if (this.cclass == this.tclass) {
//                throw new ClassCastException();
//            } else {
//                throw new RuntimeException(new IllegalAccessException("Class " + this.cclass.getName() + " can not access a protected member of class " + this.tclass.getName() + " using an instance of " + obj.getClass().getName()));
//            }
//        }

    public final boolean compareAndSet(T obj, int expect, int update) {
        //return U.compareAndSetInt(obj, this.offset, expect, update);
        return U.compareAndSwapInt(obj, this.offset, expect, update);
    }

    public final boolean weakCompareAndSet(T obj, int expect, int update) {
        //return U.compareAndSwapInt(obj, this.offset, expect, update);
        return compareAndSet(obj, expect, update);
        //return INT.weakCompareAndSet(obj, expect, update);
    }

    public final void set(T obj, int newValue) {
        U.putIntVolatile(obj, this.offset, newValue);
    }
    public final void set(T obj, float newValue) {
        U.putFloatVolatile(obj, this.offset, newValue);
    }

    public final void lazySet(T obj, int newValue) {
        U.putOrderedInt(obj, this.offset, newValue);
    }

    public final int get(T obj) {
        return U.getIntVolatile(obj, this.offset);
    }
    public final float getFloat(T obj) {
        return U.getFloatVolatile(obj, this.offset);
    }

    public final int getOpaque(T obj) {
        return U.getIntVolatile(obj, this.offset);
        //return (int) INT.getOpaque(obj);
    }


    public final int getAndSet(T obj, int newValue) {
        return U.getAndSetInt(obj, this.offset, newValue);
    }


    public final int getAndAdd(T obj, int delta) {
        return delta == 0 ? get(obj) : U.getAndAddInt(obj, this.offset, delta);
    }

    public final int getAndIncrement(T obj) {
        return U.getAndAddInt(obj, offset, 1);
    }

    public final int getAndDecrement(T obj) {
        return U.getAndAddInt(obj, offset, -1);
    }

    public final int incrementAndGet(T obj) {
        return addAndGet(obj, +1);
    }

    public final int decrementAndGet(T obj) {
        return addAndGet(obj, -1);
    }

    public final int addAndGet(T obj, int delta) {
        return this.getAndAdd(obj, delta) + delta;
    }



}