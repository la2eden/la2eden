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
package com.la2eden.gameserver.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.la2eden.gameserver.model.holders.MinionHolder;
import com.la2eden.gameserver.model.holders.SkillHolder;
import com.la2eden.gameserver.model.interfaces.IParserAdvUtils;

/**
 * This class is meant to hold a set of (key,value) pairs.<br>
 * They are stored as object but can be retrieved in any type wanted. As long as cast is available.<br>
 * @author mkizub
 */
public class StatsSet implements IParserAdvUtils
{
	private static final Logger _log = Logger.getLogger(StatsSet.class.getName());
	/** Static empty immutable map, used to avoid multiple null checks over the source. */
	public static final StatsSet EMPTY_STATSET = new StatsSet(Collections.<String, Object> emptyMap());
	
	private final Map<String, Object> _set;
	
	public StatsSet()
	{
		this(new LinkedHashMap<>());
	}
	
	public StatsSet(Map<String, Object> map)
	{
		_set = map;
	}
	
	/**
	 * Returns the set of values
	 * @return HashMap
	 */
	public final Map<String, Object> getSet()
	{
		return _set;
	}
	
	/**
	 * Add a set of couple values in the current set
	 * @param newSet : StatsSet pointing out the list of couples to add in the current set
	 */
	public void add(StatsSet newSet)
	{
		_set.putAll(newSet.getSet());
	}
	
	/**
	 * Verifies if the stat set is empty.
	 * @return {@code true} if the stat set is empty, {@code false} otherwise
	 */
	public boolean isEmpty()
	{
		return _set.isEmpty();
	}
	
	/**
	 * Return the boolean value associated with key.
	 * @param key : String designating the key in the set
	 * @return boolean : value associated to the key
	 * @throws IllegalArgumentException : If value is not set or value is not boolean
	 */
	@Override
	public boolean getBoolean(String key)
	{
		final Object val = _set.get(key);
		if (val == null)
		{
			throw new IllegalArgumentException("Boolean value required, but not specified");
		}
		if (val instanceof Boolean)
		{
			return ((Boolean) val).booleanValue();
		}
		try
		{
			return Boolean.parseBoolean((String) val);
		}
		catch (Exception e)
		{
			throw new IllegalArgumentException("Boolean value required, but found: " + val);
		}
	}
	
	/**
	 * Return the boolean value associated with key.<br>
	 * If no value is associated with key, or type of value is wrong, returns defaultValue.
	 * @param key : String designating the key in the entry set
	 * @return boolean : value associated to the key
	 */
	@Override
	public boolean getBoolean(String key, boolean defaultValue)
	{
		final Object val = _set.get(key);
		if (val == null)
		{
			return defaultValue;
		}
		if (val instanceof Boolean)
		{
			return ((Boolean) val).booleanValue();
		}
		try
		{
			return Boolean.parseBoolean((String) val);
		}
		catch (Exception e)
		{
			return defaultValue;
		}
	}
	
	@Override
	public byte getByte(String key)
	{
		final Object val = _set.get(key);
		if (val == null)
		{
			throw new IllegalArgumentException("Byte value required, but not specified");
		}
		if (val instanceof Number)
		{
			return ((Number) val).byteValue();
		}
		try
		{
			return Byte.parseByte((String) val);
		}
		catch (Exception e)
		{
			throw new IllegalArgumentException("Byte value required, but found: " + val);
		}
	}
	
	@Override
	public byte getByte(String key, byte defaultValue)
	{
		final Object val = _set.get(key);
		if (val == null)
		{
			return defaultValue;
		}
		if (val instanceof Number)
		{
			return ((Number) val).byteValue();
		}
		try
		{
			return Byte.parseByte((String) val);
		}
		catch (Exception e)
		{
			throw new IllegalArgumentException("Byte value required, but found: " + val);
		}
	}
	
	public byte[] getByteArray(String key, String splitOn)
	{
		final Object val = _set.get(key);
		if (val == null)
		{
			throw new IllegalArgumentException("Byte value required, but not specified");
		}
		if (val instanceof Number)
		{
			final byte[] result =
			{
				((Number) val).byteValue()
			};
			return result;
		}
		int c = 0;
		final String[] vals = ((String) val).split(splitOn);
		final byte[] result = new byte[vals.length];
		for (String v : vals)
		{
			try
			{
				result[c++] = Byte.parseByte(v);
			}
			catch (Exception e)
			{
				throw new IllegalArgumentException("Byte value required, but found: " + val);
			}
		}
		return result;
	}
	
	public List<Byte> getByteList(String key, String splitOn)
	{
		final List<Byte> result = new ArrayList<>();
		for (Byte i : getByteArray(key, splitOn))
		{
			result.add(i);
		}
		return result;
	}
	
	@Override
	public short getShort(String key)
	{
		final Object val = _set.get(key);
		if (val == null)
		{
			throw new IllegalArgumentException("Short value required, but not specified");
		}
		if (val instanceof Number)
		{
			return ((Number) val).shortValue();
		}
		try
		{
			return Short.parseShort((String) val);
		}
		catch (Exception e)
		{
			throw new IllegalArgumentException("Short value required, but found: " + val);
		}
	}
	
	@Override
	public short getShort(String key, short defaultValue)
	{
		final Object val = _set.get(key);
		if (val == null)
		{
			return defaultValue;
		}
		if (val instanceof Number)
		{
			return ((Number) val).shortValue();
		}
		try
		{
			return Short.parseShort((String) val);
		}
		catch (Exception e)
		{
			throw new IllegalArgumentException("Short value required, but found: " + val);
		}
	}
	
	@Override
	public int getInt(String key)
	{
		final Object val = _set.get(key);
		if (val == null)
		{
			throw new IllegalArgumentException("Integer value required, but not specified: " + key + "!");
		}
		
		if (val instanceof Number)
		{
			return ((Number) val).intValue();
		}
		
		try
		{
			return Integer.parseInt((String) val);
		}
		catch (Exception e)
		{
			throw new IllegalArgumentException("Integer value required, but found: " + val + "!");
		}
	}
	
	@Override
	public int getInt(String key, int defaultValue)
	{
		final Object val = _set.get(key);
		if (val == null)
		{
			return defaultValue;
		}
		if (val instanceof Number)
		{
			return ((Number) val).intValue();
		}
		try
		{
			return Integer.parseInt((String) val);
		}
		catch (Exception e)
		{
			throw new IllegalArgumentException("Integer value required, but found: " + val);
		}
	}
	
	public int[] getIntArray(String key, String splitOn)
	{
		final Object val = _set.get(key);
		if (val == null)
		{
			throw new IllegalArgumentException("Integer value required, but not specified");
		}
		if (val instanceof Number)
		{
			final int[] result =
			{
				((Number) val).intValue()
			};
			return result;
		}
		int c = 0;
		final String[] vals = ((String) val).split(splitOn);
		final int[] result = new int[vals.length];
		for (String v : vals)
		{
			try
			{
				result[c++] = Integer.parseInt(v);
			}
			catch (Exception e)
			{
				throw new IllegalArgumentException("Integer value required, but found: " + val);
			}
		}
		return result;
	}
	
	public List<Integer> getIntegerList(String key, String splitOn)
	{
		final List<Integer> result = new ArrayList<>();
		for (int i : getIntArray(key, splitOn))
		{
			result.add(i);
		}
		return result;
	}
	
	@Override
	public long getLong(String key)
	{
		final Object val = _set.get(key);
		if (val == null)
		{
			throw new IllegalArgumentException("Long value required, but not specified");
		}
		if (val instanceof Number)
		{
			return ((Number) val).longValue();
		}
		try
		{
			return Long.parseLong((String) val);
		}
		catch (Exception e)
		{
			throw new IllegalArgumentException("Long value required, but found: " + val);
		}
	}
	
	@Override
	public long getLong(String key, long defaultValue)
	{
		final Object val = _set.get(key);
		if (val == null)
		{
			return defaultValue;
		}
		if (val instanceof Number)
		{
			return ((Number) val).longValue();
		}
		try
		{
			return Long.parseLong((String) val);
		}
		catch (Exception e)
		{
			throw new IllegalArgumentException("Long value required, but found: " + val);
		}
	}
	
	@Override
	public float getFloat(String key)
	{
		final Object val = _set.get(key);
		if (val == null)
		{
			throw new IllegalArgumentException("Float value required, but not specified");
		}
		if (val instanceof Number)
		{
			return ((Number) val).floatValue();
		}
		try
		{
			return Float.parseFloat((String) val);
		}
		catch (Exception e)
		{
			throw new IllegalArgumentException("Float value required, but found: " + val);
		}
	}
	
	@Override
	public float getFloat(String key, float defaultValue)
	{
		final Object val = _set.get(key);
		if (val == null)
		{
			return defaultValue;
		}
		if (val instanceof Number)
		{
			return ((Number) val).floatValue();
		}
		try
		{
			return Float.parseFloat((String) val);
		}
		catch (Exception e)
		{
			throw new IllegalArgumentException("Float value required, but found: " + val);
		}
	}
	
	@Override
	public double getDouble(String key)
	{
		final Object val = _set.get(key);
		if (val == null)
		{
			throw new IllegalArgumentException("Double value required, but not specified");
		}
		if (val instanceof Number)
		{
			return ((Number) val).doubleValue();
		}
		try
		{
			return Double.parseDouble((String) val);
		}
		catch (Exception e)
		{
			throw new IllegalArgumentException("Double value required, but found: " + val);
		}
	}
	
	@Override
	public double getDouble(String key, double defaultValue)
	{
		final Object val = _set.get(key);
		if (val == null)
		{
			return defaultValue;
		}
		if (val instanceof Number)
		{
			return ((Number) val).doubleValue();
		}
		try
		{
			return Double.parseDouble((String) val);
		}
		catch (Exception e)
		{
			throw new IllegalArgumentException("Double value required, but found: " + val);
		}
	}
	
	@Override
	public String getString(String key)
	{
		final Object val = _set.get(key);
		if (val == null)
		{
			throw new IllegalArgumentException("String value required, but not specified");
		}
		return String.valueOf(val);
	}
	
	@Override
	public String getString(String key, String defaultValue)
	{
		final Object val = _set.get(key);
		return val == null ? defaultValue : String.valueOf(val);
	}
	
	
	@Override
	@SuppressWarnings("unchecked")
	public <T extends Enum<T>> T getEnum(String key, Class<T> enumClass)
	{
		final Object val = _set.get(key);
		if (val == null)
		{
			throw new IllegalArgumentException("Enum value of type " + enumClass.getName() + " required, but not specified");
		}
		if (enumClass.isInstance(val))
		{
			return (T) val;
		}
		try
		{
			return Enum.valueOf(enumClass, String.valueOf(val));
		}
		catch (Exception e)
		{
			throw new IllegalArgumentException("Enum value of type " + enumClass.getName() + " required, but found: " + val);
		}
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public <T extends Enum<T>> T getEnum(String key, Class<T> enumClass, T defaultValue)
	{
		final Object val = _set.get(key);
		if (val == null)
		{
			return defaultValue;
		}
		if (enumClass.isInstance(val))
		{
			return (T) val;
		}
		try
		{
			return Enum.valueOf(enumClass, String.valueOf(val));
		}
		catch (Exception e)
		{
			throw new IllegalArgumentException("Enum value of type " + enumClass.getName() + " required, but found: " + val);
		}
	}
	
	@SuppressWarnings("unchecked")
	public final <A> A getObject(String name, Class<A> type)
	{
		final Object obj = _set.get(name);
		return (obj == null) || !type.isAssignableFrom(obj.getClass()) ? null : (A) obj;
	}
	
	public SkillHolder getSkillHolder(String key)
	{
		final Object obj = _set.get(key);
		return (obj == null) || !(obj instanceof SkillHolder) ? null : (SkillHolder) obj;
	}
	
	@SuppressWarnings("unchecked")
	public List<MinionHolder> getMinionList(String key)
	{
		final Object obj = _set.get(key);
		return (obj == null) || !(obj instanceof List<?>) ? Collections.emptyList() : (List<MinionHolder>) obj;
	}
	
	public void set(String name, Object value)
	{
		_set.put(name, value);
	}
	
	public void set(String key, boolean value)
	{
		_set.put(key, value);
	}
	
	public void set(String key, byte value)
	{
		_set.put(key, value);
	}
	
	public void set(String key, short value)
	{
		_set.put(key, value);
	}
	
	public void set(String key, int value)
	{
		_set.put(key, value);
	}
	
	public void set(String key, long value)
	{
		_set.put(key, value);
	}
	
	public void set(String key, float value)
	{
		_set.put(key, value);
	}
	
	public void set(String key, double value)
	{
		_set.put(key, value);
	}
	
	public void set(String key, String value)
	{
		_set.put(key, value);
	}
	
	public void set(String key, Enum<?> value)
	{
		_set.put(key, value);
	}
	
	public void safeSet(String key, int value, int min, int max, String reference)
	{
		assert ((min > max) || ((value >= min) && (value < max)));
		if ((min <= max) && ((value < min) || (value >= max)))
		{
			_log.log(Level.SEVERE, "Incorrect value: " + value + "for: " + key + "Ref: " + reference);
		}
		
		set(key, value);
	}
}
