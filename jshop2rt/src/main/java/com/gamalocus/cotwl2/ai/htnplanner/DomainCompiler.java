package com.gamalocus.cotwl2.ai.htnplanner;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.logging.Logger;

import antlr.RecognitionException;
import antlr.TokenStreamException;

import com.gamalocus.jshop2rt.InternalDomain;

public class DomainCompiler 
{
  private final static Logger logger = Logger.getLogger(DomainCompiler.class.getName());

  /**
   * Writes generated java code to the same folder as the domain source code.
   * 
   * For debugging only.
   */
  private static final boolean WRITE_TO_SOURCE_DIR = true;

  private DomainCompiler() {}

  /**
   * Cause regeneration of domain from domain description.
   * @param inputPath 
   * @throws IOException 
   * @throws TokenStreamException 
   * @throws RecognitionException 
   */
  public static void generateDomainCode(File inputPath, Class<?> domainClass, File outputPath) throws Exception
  {
    logger.info(String.format("Class output: %s.", outputPath.getAbsolutePath()));

    File srcOutputPath = WRITE_TO_SOURCE_DIR ? inputPath : outputPath;
    logger.info(String.format("Source output: %s.", srcOutputPath.getAbsolutePath()));

    File domainSource = new File(inputPath, domainClass.getName().replace(".", "/"));
    File javaSource = new File(srcOutputPath, domainClass.getName().replace(".", "/") + ".java");

    logger.info(String.format("Compiling JSHOP2 source: %s...", 
        domainSource.getAbsolutePath()));

    // TODO Emit java code as string, and use a SimpleJavaFileObject subclass
    // to wrap it. 
    InternalDomain generator = new InternalDomain(domainSource, javaSource, 
        Integer.MAX_VALUE, domainClass.getPackage().getName());

    generator.getParser().domain();

    FileWriter out = null;
    try
    {
      // Make sure the output path exists.
      javaSource.getParentFile().mkdirs();
      out = new FileWriter(javaSource);
      out.write(generator.getOutput());
    }
    finally
    {
      if (out != null)
      {
        out.close();
      }
    }

    logger.info(String.format("Compiling Java source: %s...", 
        javaSource.getAbsolutePath()));

    // NOTE: The following code requires Java 1.5.
    final Class<?> compiler;
    try
    {
      compiler = Class.forName("com.sun.tools.javac.Main");
    }
    catch (ClassNotFoundException e)
    {
      throw new IOException(String.format("Cannot recompile domain %s: No Java compiler available. " +
          "Hint: Include tools.jar in the classpath.", domainClass.getName()));
    }

    // FIXME Set output path  to outputPath
    String[] args = new String[]{
        // Verbose output
        "-verbose",  
        // Debugging info
        "-g",     
        // Class output path
        "-d", outputPath.getAbsolutePath(),
        // Source file(s)
        javaSource.getAbsolutePath() };
    
    final StringWriter compilerMessages = new StringWriter();

    Object result = compiler
    .getMethod("compile", String[].class, PrintWriter.class)
    .invoke(args, new PrintWriter(compilerMessages));

    int status = ((Integer)result).intValue();
    if (status != 0)
    {
      throw new IOException(compilerMessages.toString());
    }
  }

  /**
   * FIXME Non-atomic, may leave stuff behind on partial completion.
   */
  public static File compileToTempFolder(File sourcePath, Class<?> domainClass) throws Exception
  {
    File tempPlannerClassPath = File.createTempFile(DomainCompiler.class.getName(), "");
    tempPlannerClassPath.delete();
    tempPlannerClassPath.mkdir();

    generateDomainCode(sourcePath, domainClass, tempPlannerClassPath);

    return tempPlannerClassPath;
  }
}
