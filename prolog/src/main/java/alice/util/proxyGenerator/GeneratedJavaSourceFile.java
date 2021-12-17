package alice.util.proxyGenerator;

import javax.tools.SimpleJavaFileObject;
import java.net.URI;

class GeneratedJavaSourceFile extends SimpleJavaFileObject {
  private final CharSequence javaSource;

  GeneratedJavaSourceFile(String className, CharSequence javaSource) {
    super(URI.create(className + ".java"), Kind.SOURCE);
    this.javaSource = javaSource;
  }

  @Override
  public CharSequence getCharContent(boolean ignoreEncodeErrors) {
    return javaSource;
  }
}

/*
 I had known about the javax.tools.JavaCompiler class for a number of years, but had not managed to use it to 
 my satisfaction. I figured out how to compile classes from strings, but the resulting class files were dumped 
 on the disk instead of being loaded into the current class loader.
 After much searching and head scratching, I found a website that described how to do this in Groovy, using 
 the JavaCompiler: the key was the ForwardingJavaFileManager class. 
 This led to another excellent article called Dynamic In-memory Compilation. 
 Both articles showed how to convert a String into a byte[] representing the Java class.
 Once we have obtained the byte[], we need to turn this into a class. 
 One easy solution is to make a ClassLoader that inherits from our current one. One of the risks is that we then 
 enter ClassLoader hell. I wanted to rather take the dynamic proxy approach, which lets the user specify into 
 which ClassLoader we want our class to be injected. 
 In my solution I use the same mechanism by calling the private static Proxy.defineClass0() method. We could 
 probably also have used the public Unsafe.defineClass() method, but both "solutions" bind us to an implementation 
 of the JDK and are thus not ideal.
 
 In this newsletter, we look at how the Generator works. 
 It uses a GeneratedJavaSourceFile to store the String, in this case actually a CharSequence. The CharSequence 
 interface is implemented by String, StringBuffer and StringBuilder, thus we do not need to create an unnecessary 
 String. We can simply pass in our existing StringBuilder. I wish more classes used the CharSequence interface!

 According to the JavaDocs, the recommended URI for a Java source String object is "string:
 but "NameOfClass.java" also works, so that is what we will use.
*/