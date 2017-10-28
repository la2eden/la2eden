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
package com.la2eden.gameserver.model.zone.type;

import com.la2eden.gameserver.model.actor.L2Character;
import com.la2eden.gameserver.model.actor.L2Npc;
import com.la2eden.gameserver.model.actor.instance.L2PcInstance;
import com.la2eden.gameserver.model.zone.L2ZoneType;
import com.la2eden.gameserver.model.zone.ZoneId;
import com.la2eden.gameserver.network.serverpackets.AbstractNpcInfo;
import com.la2eden.gameserver.network.serverpackets.ServerObjectInfo;

public class L2WaterZone extends L2ZoneType
{
	public L2WaterZone(int id)
	{
		super(id);
	}
	
	@Override
	protected void onEnter(L2Character character)
	{
		character.setInsideZone(ZoneId.WATER, true);
		
		// TODO: update to only send speed status when that packet is known
		if (character.isPlayer())
		{
			final L2PcInstance player = character.getActingPlayer();
			if (player.isTransformed() && !player.getTransformation().canSwim())
			{
				character.stopTransformation(true);
			}
			else
			{
				player.broadcastUserInfo();
			}
		}
		else if (character.isNpc())
		{
			for (L2PcInstance player : character.getKnownList().getKnownPlayers().values())
			{
				if (character.getRunSpeed() == 0)
				{
					player.sendPacket(new ServerObjectInfo((L2Npc) character, player));
				}
				else
				{
					player.sendPacket(new AbstractNpcInfo.NpcInfo((L2Npc) character, player));
				}
			}
		}
	}
	
	@Override
	protected void onExit(L2Character character)
	{
		character.setInsideZone(ZoneId.WATER, false);
		
		// TODO: update to only send speed status when that packet is known
		if (character.isPlayer())
		{
			character.getActingPlayer().broadcastUserInfo();
		}
		else if (character.isNpc())
		{
			for (L2PcInstance player : character.getKnownList().getKnownPlayers().values())
			{
				if (character.getRunSpeed() == 0)
				{
					player.sendPacket(new ServerObjectInfo((L2Npc) character, player));
				}
				else
				{
					player.sendPacket(new AbstractNpcInfo.NpcInfo((L2Npc) character, player));
				}
			}
		}
	}
	
	public int getWaterZ()
	{
		return getZone().getHighZ();
	}
}
