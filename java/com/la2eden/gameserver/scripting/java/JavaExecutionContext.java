/*
 * This file is part of the La2Eden project.
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package com.la2eden.gameserver.scripting.java;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Logger;

import javax.tools.Diagnostic;
import javax.tools.DiagnosticCollector;
import javax.tools.JavaFileObject;

import com.la2eden.gameserver.scripting.AbstractExecutionContext;

/**
 * @author HorridoJoho
 */
public final class JavaExecutionContext extends AbstractExecutionContext<JavaScriptingEngine>
{
	private static final Logger LOGGER = Logger.getLogger(JavaExecutionContext.class.getName());
	
	JavaExecutionContext(final JavaScriptingEngine engine)
	{
		super(engine);
	}
	
	private boolean addOptionIfNotNull(List<String> list, String nullChecked, String before)
	{
		if (nullChecked == null)
		{
			return false;
		}
		
		if (before.endsWith(":"))
		{
			list.add(before + nullChecked);
		}
		else
		{
			list.add(before);
			list.add(nullChecked);
		}
		
		return true;
	}
	
	private ClassLoader determineScriptParentClassloader()
	{
		String classloader = getProperty("classloader");
		if (classloader == null)
		{
			return ClassLoader.getSystemClassLoader();
		}
		
		switch (classloader)
		{
			case "ThreadContext":
				return Thread.currentThread().getContextClassLoader();
			case "System":
				return ClassLoader.getSystemClassLoader();
			default:
				try
				{
					return Class.forName(classloader).getClassLoader();
				}
				catch (ClassNotFoundException e)
				{
					return ClassLoader.getSystemClassLoader();
				}
		}
	}
	
	@Override
	public Map<Path, Throwable> executeScripts(Iterable<Path> sourcePaths) throws Exception
	{
		final DiagnosticCollector<JavaFileObject> fileManagerDiagnostics = new DiagnosticCollector<>();
		final DiagnosticCollector<JavaFileObject> compilationDiagnostics = new DiagnosticCollector<>();
		
		try (final ScriptingFileManager fileManager = new ScriptingFileManager(getScriptingEngine().getCompiler().getStandardFileManager(fileManagerDiagnostics, null, StandardCharsets.UTF_8)))
		{
			final List<String> options = new LinkedList<>();
			addOptionIfNotNull(options, getProperty("source"), "-source");
			addOptionIfNotNull(options, getProperty("sourcepath"), "-sourcepath");
			if (!addOptionIfNotNull(options, getProperty("cp"), "-cp") && !addOptionIfNotNull(options, getProperty("classpath"), "-classpath"))
			{
				addOptionIfNotNull(options, System.getProperty("java.class.path"), "-cp");
			}
			addOptionIfNotNull(options, getProperty("g"), "-g:");
			
			// we always add the target JVM to the current running version
			final String targetVersion = System.getProperty("java.specification.version");
			if (!targetVersion.contains("."))
			{
				options.add("-target");
				options.add(targetVersion);
			}
			else
			{
				String[] versionSplit = targetVersion.split("\\.");
				if (versionSplit.length > 1)
				{
					options.add("-target");
					options.add(versionSplit[0] + '.' + versionSplit[1]);
				}
				else
				{
					throw new JavaCompilerException("Could not determine target version!");
				}
			}
			
			// we really need an iterable of files or strings
			final List<String> sourcePathStrings = new LinkedList<>();
			for (Path sourcePath : sourcePaths)
			{
				sourcePathStrings.add(sourcePath.toString());
			}
			
			final StringWriter strOut = new StringWriter();
			final PrintWriter out = new PrintWriter(strOut);
			final boolean compilationSuccess = getScriptingEngine().getCompiler().getTask(out, fileManager, compilationDiagnostics, options, null, fileManager.getJavaFileObjectsFromStrings(sourcePathStrings)).call();
			if (!compilationSuccess)
			{
				out.println();
				out.println("----------------");
				out.println("File diagnostics");
				out.println("----------------");
				for (Diagnostic<? extends JavaFileObject> diagnostic : fileManagerDiagnostics.getDiagnostics())
				{
					out.println("\t" + diagnostic.getKind().toString() + ": " + diagnostic.getSource().getName() + ", Line " + diagnostic.getLineNumber() + ", Column " + diagnostic.getColumnNumber());
					out.println("\t\tcode: " + diagnostic.getCode());
					out.println("\t\tmessage: " + diagnostic.getMessage(null));
				}
				
				out.println();
				out.println("-----------------------");
				out.println("Compilation diagnostics");
				out.println("-----------------------");
				for (Diagnostic<? extends JavaFileObject> diagnostic : compilationDiagnostics.getDiagnostics())
				{
					out.println("\t" + diagnostic.getKind().toString() + ": " + diagnostic.getSource().getName() + ", Line " + diagnostic.getLineNumber() + ", Column " + diagnostic.getColumnNumber());
					out.println("\t\tcode: " + diagnostic.getCode());
					out.println("\t\tmessage: " + diagnostic.getMessage(null));
				}
				
				throw new JavaCompilerException(strOut.toString());
			}
			
			final ClassLoader parentClassLoader = determineScriptParentClassloader();
			
			final Map<Path, Throwable> executionFailures = new LinkedHashMap<>();
			final Iterable<ScriptingOutputFileObject> compiledClasses = fileManager.getCompiledClasses();
			for (Path sourcePath : sourcePaths)
			{
				boolean found = false;
				
				for (ScriptingOutputFileObject compiledClass : compiledClasses)
				{
					Path compiledSourcePath = compiledClass.getSourcePath();
					// sourePath can be relative, so we have to use endsWith
					if ((compiledSourcePath != null) && (compiledSourcePath.equals(sourcePath) || compiledSourcePath.endsWith(sourcePath)))
					{
						String javaName = compiledClass.getJavaName();
						if (javaName.indexOf('$') != -1)
						{
							continue;
						}
						
						found = true;
						setCurrentExecutingScript(compiledSourcePath);
						try
						{
							ScriptingClassLoader loader = new ScriptingClassLoader(parentClassLoader, compiledClasses);
							Class<?> javaCls = loader.loadClass(javaName);
							javaCls.getMethod("main", String[].class).invoke(null, (Object) new String[]
							{
								compiledSourcePath.toString()
							});
						}
						catch (Exception e)
						{
							executionFailures.put(compiledSourcePath, e);
						}
						finally
						{
							setCurrentExecutingScript(null);
						}
						
						break;
					}
				}
				
				if (!found)
				{
					LOGGER.severe("Compilation successfull, but class coresponding to " + sourcePath.toString() + " not found!");
				}
			}
			
			return executionFailures;
		}
	}
	
	@Override
	public Entry<Path, Throwable> executeScript(Path sourcePath) throws Exception
	{
		final Map<Path, Throwable> executionFailures = executeScripts(Arrays.asList(sourcePath));
		if (!executionFailures.isEmpty())
		{
			return executionFailures.entrySet().iterator().next();
		}
		return null;
	}
}
