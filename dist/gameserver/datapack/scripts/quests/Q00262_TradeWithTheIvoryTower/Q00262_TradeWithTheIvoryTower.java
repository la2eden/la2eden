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
package quests.Q00262_TradeWithTheIvoryTower;

import java.util.HashMap;
import java.util.Map;

import com.la2eden.Config;
import com.la2eden.gameserver.enums.QuestSound;
import com.la2eden.gameserver.model.actor.L2Npc;
import com.la2eden.gameserver.model.actor.instance.L2PcInstance;
import com.la2eden.gameserver.model.quest.Quest;
import com.la2eden.gameserver.model.quest.QuestState;
import com.la2eden.gameserver.model.quest.State;

/**
 * Trade With The Ivory Tower (262)
 * @author ivantotov
 */
public final class Q00262_TradeWithTheIvoryTower extends Quest
{
	// NPCs
	private static final int VOLLODOS = 30137;
	// Items
	private static final int SPORE_SAC = 707;
	// Misc
	private static final int MIN_LEVEL = 8;
	private static final int REQUIRED_ITEM_COUNT = 10;
	// Monsters
	private static final Map<Integer, Integer> MOBS_SAC = new HashMap<>();
	
	static
	{
		MOBS_SAC.put(20007, 3); // Green Fungus
		MOBS_SAC.put(20400, 4); // Blood Fungus
	}
	
	public Q00262_TradeWithTheIvoryTower()
	{
		super(262, Q00262_TradeWithTheIvoryTower.class.getSimpleName(), "Trade With The Ivory Tower");
		addStartNpc(VOLLODOS);
		addTalkId(VOLLODOS);
		addKillId(MOBS_SAC.keySet());
		registerQuestItems(SPORE_SAC);
	}
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		final QuestState st = getQuestState(player, false);
		if ((st != null) && event.equalsIgnoreCase("30137-03.htm"))
		{
			st.startQuest();
			return event;
		}
		return null;
	}
	
	@Override
	public String onKill(L2Npc npc, L2PcInstance player, boolean isSummon)
	{
		final L2PcInstance partyMember = getRandomPartyMember(player, 1);
		if (partyMember == null)
		{
			return super.onKill(npc, player, isSummon);
		}
		
		final QuestState st = getQuestState(partyMember, false);
		final float chance = (MOBS_SAC.get(npc.getId()) * Config.RATE_QUEST_DROP);
		if (getRandom(10) < chance)
		{
			rewardItems(player, SPORE_SAC, 1);
			if (getQuestItemsCount(player, SPORE_SAC) >= REQUIRED_ITEM_COUNT)
			{
				st.setCond(2, true);
			}
			else
			{
				playSound(player, QuestSound.ITEMSOUND_QUEST_ITEMGET);
			}
		}
		return super.onKill(npc, player, isSummon);
	}
	
	@Override
	public String onTalk(L2Npc npc, L2PcInstance player)
	{
		String htmltext = getNoQuestMsg(player);
		final QuestState st = getQuestState(player, true);
		if (st == null)
		{
			return htmltext;
		}
		
		switch (st.getState())
		{
			case State.CREATED:
			{
				htmltext = player.getLevel() >= MIN_LEVEL ? "30137-02.htm" : "30137-01.htm";
				break;
			}
			case State.STARTED:
			{
				switch (st.getCond())
				{
					case 1:
					{
						if (getQuestItemsCount(player, SPORE_SAC) < REQUIRED_ITEM_COUNT)
						{
							htmltext = "30137-04.html";
						}
						break;
					}
					case 2:
					{
						if (getQuestItemsCount(player, SPORE_SAC) >= REQUIRED_ITEM_COUNT)
						{
							htmltext = "30137-05.html";
							giveAdena(player, 3000, true);
							st.exitQuest(true, true);
						}
						break;
					}
				}
				break;
			}
		}
		return htmltext;
	}
}