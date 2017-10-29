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
package com.la2eden.gameserver.network.serverpackets;

/**
 * @author UnAfraid
 */
public class ExChangeClientEffectInfo extends L2GameServerPacket
{
	public static final ExChangeClientEffectInfo STATIC_FREYA_DEFAULT = new ExChangeClientEffectInfo(0, 0, 1);
	public static final ExChangeClientEffectInfo STATIC_FREYA_DESTROYED = new ExChangeClientEffectInfo(0, 0, 2);
	
	private final int _type, _key, _value;
	
	/**
	 * @param type
	 *            <ul>
	 *            <li>0 - ChangeZoneState</li>
	 *            <li>1 - SetL2Fog</li>
	 *            <li>2 - postEffectData</li>
	 *            </ul>
	 * @param key
	 * @param value
	 */
	public ExChangeClientEffectInfo(int type, int key, int value)
	{
		_type = type;
		_key = key;
		_value = value;
	}
	
	@Override
	protected void writeImpl()
	{
		writeC(0xFE);
		writeH(0xC2);
		writeD(_type);
		writeD(_key);
		writeD(_value);
	}
}