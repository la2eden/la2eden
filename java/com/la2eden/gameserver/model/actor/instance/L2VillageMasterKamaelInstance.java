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
package com.la2eden.gameserver.model.actor.instance;

import com.la2eden.Config;
import com.la2eden.gameserver.enums.Race;
import com.la2eden.gameserver.model.actor.templates.L2NpcTemplate;
import com.la2eden.gameserver.model.base.PlayerClass;
import com.la2eden.gameserver.model.quest.QuestState;

public final class L2VillageMasterKamaelInstance extends L2VillageMasterInstance
{
	/**
	 * Creates a village master.
	 * @param template the village master NPC template
	 */
	public L2VillageMasterKamaelInstance(L2NpcTemplate template)
	{
		super(template);
	}
	
	@Override
	protected final String getSubClassMenu(Race race)
	{
		if (Config.ALT_GAME_SUBCLASS_EVERYWHERE || (race == Race.KAMAEL))
		{
			return "./datapack/html/villagemaster/SubClass.htm";
		}
		
		return "./datapack/html/villagemaster/SubClass_NoKamael.htm";
	}
	
	@Override
	protected final String getSubClassFail()
	{
		return "./datapack/html/villagemaster/SubClass_Fail_Kamael.htm";
	}
	
	@Override
	protected final boolean checkQuests(L2PcInstance player)
	{
		// Noble players can add subbclasses without quests
		if (player.isNoble())
		{
			return true;
		}
		
		QuestState qs = player.getQuestState("Q00234_FatesWhisper");
		if ((qs == null) || !qs.isCompleted())
		{
			return false;
		}
		
		qs = player.getQuestState("Q00236_SeedsOfChaos");
		if ((qs == null) || !qs.isCompleted())
		{
			return false;
		}
		
		return true;
	}
	
	@Override
	protected final boolean checkVillageMasterRace(PlayerClass pclass)
	{
		if (pclass == null)
		{
			return false;
		}
		
		return pclass.isOfRace(Race.KAMAEL);
	}
}