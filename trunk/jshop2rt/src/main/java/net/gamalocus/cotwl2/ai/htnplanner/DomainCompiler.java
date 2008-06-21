package net.gamalocus.cotwl2.ai.htnplanner;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.StringWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.logging.Logger;

import javax.tools.FileObject;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileManager;
import javax.tools.JavaFileObject;
import javax.tools.SimpleJavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.StandardLocation;
import javax.tools.ToolProvider;
import javax.tools.JavaCompiler.CompilationTask;
import javax.tools.JavaFileObject.Kind;

import net.gamalocus.jshop2rt.InternalDomain;

import antlr.RecognitionException;
import antlr.TokenStreamException;

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
		
		JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
		if (compiler == null)
		{
			throw new IOException(String.format("Cannot recompile domain %s: No Java compiler available. " +
					"Hint: Include tools.jar in the classpath.", domainClass.getName()));
		}
		
		StandardJavaFileManager fm = compiler.getStandardFileManager(null, null, null);
		fm.setLocation(StandardLocation.CLASS_OUTPUT, Arrays.asList(outputPath));
		
		StringWriter messages = new StringWriter();
		CompilationTask task = compiler.getTask(messages, fm, null, null, null, 
				fm.getJavaFileObjects(javaSource));
		if (!task.call())
		{
			throw new IOException(messages.toString());
		}
	}
	
	/**
	 * Cause regeneration of domain from domain description.
	 * 
	 * @param javaSourceCode Buffer to receive generated Java source code. May be <code>null</code>.
	 * @return Byte-code of generated class files. 
	 */
	public static Map<String, byte[]> generateDomainCode(File inputPath, final Class<?> domainClass, 
			StringBuffer javaSourceCode) throws Exception
	{
		File domainSource = new File(inputPath, domainClass.getName().replace(".", "/"));

		logger.info(String.format("Compiling JSHOP2 source: %s...", domainSource.getAbsolutePath()));
		final InternalDomain generator = new InternalDomain(domainSource, null, Integer.MAX_VALUE, 
				domainClass.getPackage().getName());
		generator.getParser().domain();

		logger.info(String.format("Compiling Java source..."));
		
		JavaFileObject javaSource = new SimpleJavaFileObject(new URI("string", null, 
				"/" + domainClass.getName().replace(".", "/") + Kind.SOURCE.extension, null), Kind.SOURCE)
		{
			@Override
			public CharSequence getCharContent(boolean ignoreEncodingErrors)
			{
				return generator.getOutput();
			}
		};

		JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
		
		final StandardJavaFileManager fm = compiler.getStandardFileManager(null, null, null);
		final Map<String, byte[]> classData = new HashMap<String, byte[]>();
		JavaFileManager fm2 = new JavaFileManager()
		{
			Map<String, ByteArrayOutputStream> outputs = new HashMap<String, ByteArrayOutputStream>();
			
			public void close() throws IOException 
			{ 
				for (Entry<String, ByteArrayOutputStream> entry : outputs.entrySet())
				{
					ByteArrayOutputStream buf = entry.getValue();
					buf.flush();
					classData.put(entry.getKey(), buf.toByteArray());
				}
			}

			public JavaFileObject getJavaFileForOutput(Location location, final String className, Kind kind, FileObject sibling) throws IOException
			{
				try
				{
					return new SimpleJavaFileObject(new URI("string", null, "/" + className, null), kind) 
					{

						@Override
						public OutputStream openOutputStream() throws IOException
						{
							ByteArrayOutputStream out = new ByteArrayOutputStream();
							outputs.put(className, out);
							return out;
						}
					};
				}
				catch (URISyntaxException e)
				{
					throw new IOException(e);
				}
			}

			public void flush() throws IOException 
			{ 
				logger.info("Called.");
			}
			public ClassLoader getClassLoader(Location location) 
			{ 
				ClassLoader result = fm.getClassLoader(location);
				logger.info(String.format("Called, location: %s. Standard result: %s.", location, result));
				return result; 
			}
			public FileObject getFileForInput(Location location, String packageName, String relativeName) throws IOException 
			{
				FileObject result = fm.getFileForInput(location, packageName, relativeName);
				logger.info(String.format("Called, location: %s, packageName: %s, relativeName: %s. Standard result: %s.", 
						location, packageName, relativeName, result));
				return result; 
			}
			public FileObject getFileForOutput(Location location, String packageName, String relativeName, FileObject sibling) throws IOException 
			{
				logger.info("Called.");
				throw new UnsupportedOperationException("Not implemented."); 
			}
			public JavaFileObject getJavaFileForInput(Location location, String className, Kind kind) throws IOException 
			{
				JavaFileObject result = fm.getJavaFileForInput(location, className, kind);
				logger.info(String.format("Called, location: %s, className: %s, kind %s. Standard result: %s.", 
						location, className, kind, result));
				return result; 
			}
			public boolean handleOption(String current, Iterator<String> remaining) 
			{
				logger.info("Called.");
				return false; 
			}
			public boolean hasLocation(Location location) 
			{ 
				boolean result = fm.hasLocation(location);
				logger.info(String.format("Called, location: %s. Standard result: %s.", location, result));
				return result; 
			}
			public String inferBinaryName(Location location, JavaFileObject file) 
			{ 
				String result = fm.inferBinaryName(location, file);
				logger.info(String.format("Called, location: %s, file: %s. Standard result: %s.", 
						location, file.getName(), result)); 
				return result; 
			}
			public boolean isSameFile(FileObject a, FileObject b) 
			{ 
				logger.info("Called.");
				throw new UnsupportedOperationException(); 
			}
			public Iterable<JavaFileObject> list(Location location, String packageName, Set<Kind> kinds, boolean recurse) throws IOException 
			{ 
				Iterable<JavaFileObject> list = fm.list(location, packageName, kinds, recurse);

				logger.info(String.format("Called, location: %s, packageName: %s, kinds: %s, recurse: %s. Standard result:\n%s",
						location, packageName, kinds, Boolean.toString(recurse),
						list));
				
				return list; 
			}
			public int isSupportedOption(String option) 
			{ 
				logger.info("Called.");
				return 0; 
			}
		};
		
		
		StringWriter out = new StringWriter();
		CompilationTask task = compiler.getTask(out, fm2, null, null, null, Arrays.asList(javaSource));
		if (!task.call())
		{
			throw new IOException(out.toString());
		}
		
		fm2.close();
		
		return classData;
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
