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
package com.la2eden.gameserver.model.zone.type;

import com.la2eden.Config;
import com.la2eden.gameserver.ThreadPoolManager;
import com.la2eden.gameserver.model.Location;
import com.la2eden.gameserver.model.actor.L2Character;
import com.la2eden.gameserver.model.actor.instance.L2PcInstance;
import com.la2eden.gameserver.model.actor.tasks.player.TeleportTask;
import com.la2eden.gameserver.model.zone.L2ZoneType;
import com.la2eden.gameserver.model.zone.ZoneId;
import com.la2eden.gameserver.network.SystemMessageId;

/**
 * A jail zone
 * @author durgus
 */
public class L2JailZone extends L2ZoneType
{
	private static final Location JAIL_IN_LOC = new Location(-114356, -249645, -2984);
	private static final Location JAIL_OUT_LOC = new Location(17836, 170178, -3507);
	
	public L2JailZone(int id)
	{
		super(id);
	}
	
	@Override
	protected void onEnter(L2Character character)
	{
		if (!character.isPlayer())
		{
			return;
		}
		character.setInsideZone(ZoneId.JAIL, true);
		character.setInsideZone(ZoneId.NO_SUMMON_FRIEND, true);
		if (Config.JAIL_IS_PVP)
		{
			character.setInsideZone(ZoneId.PVP, true);
			character.sendPacket(SystemMessageId.YOU_HAVE_ENTERED_A_COMBAT_ZONE);
		}
		if (Config.JAIL_DISABLE_TRANSACTION)
		{
			character.setInsideZone(ZoneId.NO_STORE, true);
		}
	}
	
	@Override
	protected void onExit(L2Character character)
	{
		if (!character.isPlayer())
		{
			return;
		}
		final L2PcInstance player = character.getActingPlayer();
		player.setInsideZone(ZoneId.JAIL, false);
		player.setInsideZone(ZoneId.NO_SUMMON_FRIEND, false);
		if (Config.JAIL_IS_PVP)
		{
			character.setInsideZone(ZoneId.PVP, false);
			character.sendPacket(SystemMessageId.YOU_HAVE_LEFT_A_COMBAT_ZONE);
		}
		if (player.isJailed())
		{
			// when a player wants to exit jail even if he is still jailed, teleport him back to jail
			ThreadPoolManager.getInstance().scheduleGeneral(new TeleportTask(player, JAIL_IN_LOC), 2000);
			character.sendMessage("You cannot cheat your way out of here. You must wait until your jail time is over.");
		}
		if (Config.JAIL_DISABLE_TRANSACTION)
		{
			character.setInsideZone(ZoneId.NO_STORE, false);
		}
	}
	
	public static Location getLocationIn()
	{
		return JAIL_IN_LOC;
	}
	
	public static Location getLocationOut()
	{
		return JAIL_OUT_LOC;
	}
}
