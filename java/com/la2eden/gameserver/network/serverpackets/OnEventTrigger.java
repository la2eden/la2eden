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
package com.la2eden.gameserver.network.serverpackets;

import com.la2eden.gameserver.model.actor.instance.L2DoorInstance;

/**
 * @author Gnacik, UnAfraid
 */
public class OnEventTrigger extends L2GameServerPacket
{
	private final int _emitterId;
	private final int _enabled;
	
	public OnEventTrigger(L2DoorInstance door, boolean enabled)
	{
		_emitterId = door.getEmitter();
		_enabled = enabled ? 1 : 0;
	}
	
	public OnEventTrigger(int id, boolean enabled)
	{
		_emitterId = id;
		_enabled = enabled ? 1 : 0;
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0xCF);
		writeD(_emitterId);
		writeC(_enabled);
	}
}