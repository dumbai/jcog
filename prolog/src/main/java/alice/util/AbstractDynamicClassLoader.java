package alice.util;

import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

/**
 * 
 * @author Alessio Mercurio
 * 
 * Custom abstract classloader used to addAt/remove dynamically URLs from it
 * needed by JavaLibrary.
 *
 */

public abstract class AbstractDynamicClassLoader extends ClassLoader
{
	protected final ArrayList<URL> listURLs;
	protected final HashMap<String, Class<?>> classCache = new HashMap<>();
	
	public AbstractDynamicClassLoader()
	{
		super(AbstractDynamicClassLoader.class.getClassLoader());
		listURLs = new ArrayList<>();
	}
	
	public AbstractDynamicClassLoader(URL... urls)
	{
		super(AbstractDynamicClassLoader.class.getClassLoader());
		listURLs = new ArrayList<>(Arrays.asList(urls));
	}
	
	public AbstractDynamicClassLoader(URL[] urls, ClassLoader parent)
	{
		super(parent);
		listURLs = new ArrayList<>(Arrays.asList(urls));
	}
	
	@Override
	public Class<?> loadClass(String className) throws ClassNotFoundException {
        return findClass(className);  
	}
		
	public void addURLs(URL... urls) {
		if(urls == null)
			throw new IllegalArgumentException("Array URLs must not be null.");
		for (URL url : urls) {
			if(!listURLs.contains(url))
				listURLs.add(url);
		}
	}
	
	public void removeURL(URL url) throws IllegalArgumentException
	{
		if(!listURLs.contains(url))
			throw new IllegalArgumentException("URL: " + url + "not found.");
		listURLs.remove(url);
	}
	
	public void removeURLs(URL... urls) throws IllegalArgumentException
	{
		if(urls == null)
			throw new IllegalArgumentException("Array URLs must not be null.");
		for (URL url : urls) {
			if(!listURLs.contains(url))
				throw new IllegalArgumentException("URL: " + url + "not found.");
			listURLs.remove(url);
		}
	}
	
	public void removeAllURLs()
	{
		if(!listURLs.isEmpty())
			listURLs.clear();
	}

	public URL[] getURLs()
	{
		URL[] result = new URL[listURLs.size()];
		listURLs.toArray(result);
		return result;
	}

	public Class<?>[] getLoadedClasses() {
		return classCache.values().toArray(new Class[0]);
	}
	


















}
