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

import java.util.ArrayList;
import java.util.List;

import com.la2eden.gameserver.model.interfaces.ILocational;

/**
 * Note: <b>There is known issue with this packet, it cannot be removed unless game client is restarted!</b>
 * @author UnAfraid
 */
public class ExShowTerritory extends L2GameServerPacket
{
	private final int _minZ;
	private final int _maxZ;
	private final List<ILocational> _vertices = new ArrayList<>();
	
	public ExShowTerritory(int minZ, int maxZ)
	{
		_minZ = minZ;
		_maxZ = maxZ;
	}
	
	public void addVertice(ILocational loc)
	{
		_vertices.add(loc);
	}
	
	@Override
	protected void writeImpl()
	{
		writeC(0xFE);
		writeH(0x8D);
		writeD(_vertices.size());
		writeD(_minZ);
		writeD(_maxZ);
		for (ILocational loc : _vertices)
		{
			writeD(loc.getX());
			writeD(loc.getY());
		}
	}
}
