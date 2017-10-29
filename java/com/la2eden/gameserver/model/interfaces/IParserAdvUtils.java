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
package com.la2eden.gameserver.model.interfaces;

/**
 * More advanced interface for parsers.<br>
 * Allows usage of get methods without fall back value.<br>
 * @author xban1x
 */
public interface IParserAdvUtils extends IParserUtils
{
	
	boolean getBoolean(String key);
	
	byte getByte(String key);
	
	short getShort(String key);
	
	int getInt(String key);
	
	long getLong(String key);
	
	float getFloat(String key);
	
	double getDouble(String key);
	
	String getString(String key);
	
	<T extends Enum<T>> T getEnum(String key, Class<T> clazz);
}
