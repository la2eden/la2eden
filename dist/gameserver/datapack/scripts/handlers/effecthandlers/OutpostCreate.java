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
package handlers.effecthandlers;

import com.la2eden.gameserver.data.xml.impl.NpcData;
import com.la2eden.gameserver.instancemanager.TerritoryWarManager;
import com.la2eden.gameserver.model.StatsSet;
import com.la2eden.gameserver.model.actor.instance.L2PcInstance;
import com.la2eden.gameserver.model.actor.instance.L2SiegeFlagInstance;
import com.la2eden.gameserver.model.conditions.Condition;
import com.la2eden.gameserver.model.effects.AbstractEffect;
import com.la2eden.gameserver.model.skills.BuffInfo;

/**
 * Outpost Create effect implementation.
 * @author UnAfraid
 */
public final class OutpostCreate extends AbstractEffect
{
	private static final int HQ_NPC_ID = 36590;
	
	public OutpostCreate(Condition attachCond, Condition applyCond, StatsSet set, StatsSet params)
	{
		super(attachCond, applyCond, set, params);
	}
	
	@Override
	public boolean isInstant()
	{
		return true;
	}
	
	@Override
	public void onStart(BuffInfo info)
	{
		final L2PcInstance player = info.getEffector().getActingPlayer();
		if (!player.isClanLeader())
		{
			return;
		}
		
		if (TerritoryWarManager.getInstance().isTWInProgress())
		{
			// Spawn a new flag
			final L2SiegeFlagInstance flag = new L2SiegeFlagInstance(player, NpcData.getInstance().getTemplate(HQ_NPC_ID), true, true);
			flag.setTitle(player.getClan().getName());
			flag.setCurrentHpMp(flag.getMaxHp(), flag.getMaxMp());
			flag.setHeading(player.getHeading());
			flag.spawnMe(player.getX(), player.getY(), player.getZ() + 50);
			TerritoryWarManager.getInstance().setHQForClan(player.getClan(), flag);
		}
	}
}
