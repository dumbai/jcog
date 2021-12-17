package alice.util;

import java.lang.reflect.Method;


/**
 *  Utility methods for reflective operations.
 *
 *  @author    Michele Mannino
 */
public class InspectionUtils
{
	
	/**
	 * @author Michele Mannino
	 * 
	 * @param type: class to be inspected
	 * @param methodName: name of method
	 * @param parms: array of params
	 */
	public static Method searchForMethod(Class<?> type, String methodName, Class<?>... parms) {
	    Method[] methods = type.getMethods();
        for (Method method : methods) {

            if (!method.getName().equals(methodName))
                continue;

            Class<?>[] types = method.getParameterTypes();


            if (types.length != parms.length)
                continue;


            if (areTypesCompatible(types, parms))
                return method;
        }
	    return null;
	}
	
    /**
     *  Returns true if all classes in the sources list are assignment compatible
     *  with the targets list.  In other words, if all targets[n].isAssignableFrom( sources[n] )
     *  then this method returns true.
     *  Any null values in sources are considered wild-cards and will skip the
     *  isAssignableFrom check as if it passed.
     */
    public static boolean areTypesCompatible(Class<?>[] targets, Class<?>... sources)
    {
        if(targets.length != sources.length)
            return( false );

        for(int i = 0; i < targets.length; i++)
        {
            if(sources[i] == null)
                continue;

            if(targets[i].isInterface())
            {
            	Class<?>[] interfaces = sources[i].getInterfaces();
            	for (Class<?> in : interfaces) {
					if(targets[i] == in)
						return true;
				}
            }
            	
            if(!translateFromPrimitive(targets[i]).isAssignableFrom(translateFromPrimitive(sources[i])))
                return false;
        }
        return true;
    }

    /**
     *  If this specified class represents a primitive type (int, float, etc.) then
     *  it is translated into its wrapper type (Integer, Float, etc.).  If the
     *  passed class is not a primitive then it is just returned.
     */
    static Class<?> translateFromPrimitive(Class<?> primitive)
    {
        if(!primitive.isPrimitive())
            return(primitive);

        if(Boolean.TYPE == primitive)
            return( Boolean.class );
        if(Character.TYPE == primitive)
            return(Character.class);
        if(Byte.TYPE == primitive)
            return( Byte.class);
        if(Short.TYPE == primitive)
            return( Short.class);
        if(Integer.TYPE == primitive)
            return(Integer.class);
        if(Long.TYPE == primitive)
            return(Long.class);
        if(Float.TYPE == primitive)
            return(Float.class);
        if(Double.TYPE == primitive)
            return(Double.class);

        throw new RuntimeException("Error translating type:" + primitive);
    }
}