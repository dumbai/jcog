package alice.util.proxyGenerator;

import javax.tools.*;

class GeneratingJavaFileManager extends ForwardingJavaFileManager<JavaFileManager> {
	
  private final GeneratedClassFile gcf;

  GeneratingJavaFileManager(StandardJavaFileManager sjfm, GeneratedClassFile gcf) {
    super(sjfm);
    this.gcf = gcf;
  }

  @Override
  public JavaFileObject getJavaFileForOutput(Location location, String className, JavaFileObject.Kind kind,
                                             FileObject sibling) {
    return gcf;
  }
}
  

/*
  This class forces the JavaCompiler to use the GeneratedClassFile's output stream for writing the class
*/