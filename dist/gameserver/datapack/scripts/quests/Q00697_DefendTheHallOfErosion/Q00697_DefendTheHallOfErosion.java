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
package quests.Q00697_DefendTheHallOfErosion;

import com.la2eden.gameserver.instancemanager.SoIManager;
import com.la2eden.gameserver.model.actor.L2Npc;
import com.la2eden.gameserver.model.actor.instance.L2PcInstance;
import com.la2eden.gameserver.model.quest.Quest;
import com.la2eden.gameserver.model.quest.QuestState;
import com.la2eden.gameserver.model.quest.State;
import com.la2eden.util.Rnd;

public class Q00697_DefendTheHallOfErosion extends Quest
{
	private static final int TEPIOS = 32603;
	private static final int VESPER_STONE = 14052;
	
	public Q00697_DefendTheHallOfErosion()
	{
		super(697, Q00697_DefendTheHallOfErosion.class.getSimpleName(), "Defend the Hall of Erosion");
		addStartNpc(TEPIOS);
		addTalkId(TEPIOS);
	}
	
	@Override
	public final String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		final String htmltext = event;
		
		final QuestState qs = player.getQuestState(getName());
		if (qs == null)
		{
			return htmltext;
		}
		
		if (event.equalsIgnoreCase("32603-03.html"))
		{
			qs.startQuest();
		}
		return htmltext;
	}
	
	@Override
	public final String onTalk(L2Npc npc, L2PcInstance player)
	{
		String htmltext = getNoQuestMsg(player);
		final QuestState qs = getQuestState(player, true);
		if (qs == null)
		{
			return htmltext;
		}
		
		switch (qs.getState())
		{
			case State.CREATED:
			{
				if (player.getLevel() < 75)
				{
					htmltext = "32603-00.html";
					qs.exitQuest(true);
				}
				if (SoIManager.getCurrentStage() != 4)
				{
					htmltext = "32603-00a.html";
					qs.exitQuest(true);
				}
				htmltext = "32603-01.htm";
				break;
			}
			case State.STARTED:
			{
				if (qs.isCond(1) && (qs.getInt("defenceDone") == 0))
				{
					htmltext = "32603-04.html";
				}
				else if (qs.isCond(1) && (qs.getInt("defenceDone") != 0))
				{
					giveItems(player, VESPER_STONE, Rnd.get(12, 20));
					qs.unset("defenceDone");
					qs.exitQuest(true);
					htmltext = "32603-05.html";
				}
				break;
			}
		}
		return htmltext;
	}
}