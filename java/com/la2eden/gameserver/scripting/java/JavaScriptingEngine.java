/*
 * This file is part of the L2J Mobius project.
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

import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.ServiceLoader;

import javax.lang.model.SourceVersion;
import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;

import com.la2eden.gameserver.scripting.AbstractScriptingEngine;
import com.la2eden.gameserver.scripting.IExecutionContext;

/**
 * @author HorridoJoho
 */
public final class JavaScriptingEngine extends AbstractScriptingEngine
{
	private volatile JavaCompiler _compiler;
	
	public JavaScriptingEngine()
	{
		super("L2J Java Engine", "1.0", "java");
	}
	
	private void determineCompilerOrThrow()
	{
		final String preferedCompiler = getProperty("preferedCompiler");
		LinkedList<JavaCompiler> allCompilers = null;
		
		JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
		if (compiler != null)
		{
			if ((preferedCompiler == null) || compiler.getClass().getName().equals(preferedCompiler))
			{
				_compiler = compiler;
				return;
			}
			
			allCompilers = new LinkedList<>();
			allCompilers.add(compiler);
		}
		
		final ServiceLoader<JavaCompiler> thirdPartyCompilers = ServiceLoader.load(JavaCompiler.class);
		Iterator<JavaCompiler> compilersIterator = thirdPartyCompilers.iterator();
		while (compilersIterator.hasNext())
		{
			compiler = compilersIterator.next();
			if ((preferedCompiler == null) || compiler.getClass().getName().equals(preferedCompiler))
			{
				_compiler = compiler;
				return;
			}
			
			if (allCompilers == null)
			{
				allCompilers = new LinkedList<>();
			}
			allCompilers.add(compilersIterator.next());
		}
		
		if (allCompilers != null)
		{
			compilersIterator = allCompilers.iterator();
			while (compilersIterator.hasNext())
			{
				compiler = compilersIterator.next();
				if ((preferedCompiler == null) || compiler.getClass().getName().equals(preferedCompiler))
				{
					break;
				}
			}
		}
		
		if (compiler == null)
		{
			throw new IllegalStateException("No javax.tools.JavaCompiler service installed!");
		}
		
		_compiler = compiler;
	}
	
	private void ensureCompilerOrThrow()
	{
		if (_compiler == null)
		{
			synchronized (this)
			{
				if (_compiler == null)
				{
					determineCompilerOrThrow();
				}
			}
		}
	}
	
	JavaCompiler getCompiler()
	{
		return _compiler;
	}
	
	@Override
	public IExecutionContext createExecutionContext()
	{
		ensureCompilerOrThrow();
		return new JavaExecutionContext(this);
	}
	
	@Override
	public String getLanguageName()
	{
		return "Java";
	}
	
	@Override
	public String getLanguageVersion()
	{
		ensureCompilerOrThrow();
		return Arrays.deepToString(_compiler.getSourceVersions().toArray(new SourceVersion[0])).replace("RELEASE_", "");
	}
}