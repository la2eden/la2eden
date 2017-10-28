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
package quests.Q00050_LanoscosSpecialBait;

import com.la2eden.Config;
import com.la2eden.gameserver.enums.QuestSound;
import com.la2eden.gameserver.model.actor.L2Npc;
import com.la2eden.gameserver.model.actor.instance.L2PcInstance;
import com.la2eden.gameserver.model.quest.Quest;
import com.la2eden.gameserver.model.quest.QuestState;
import com.la2eden.gameserver.model.quest.State;

/**
 * Lanosco's Special Bait (50)<br>
 * Original Jython script by Kilkenny.
 * @author nonom
 */
public class Q00050_LanoscosSpecialBait extends Quest
{
	// NPCs
	private static final int LANOSCO = 31570;
	private static final int SINGING_WIND = 21026;
	// Items
	private static final int ESSENCE_OF_WIND = 7621;
	private static final int WIND_FISHING_LURE = 7610;
	
	public Q00050_LanoscosSpecialBait()
	{
		super(50, Q00050_LanoscosSpecialBait.class.getSimpleName(), "Lanosco's Special Bait");
		addStartNpc(LANOSCO);
		addTalkId(LANOSCO);
		addKillId(SINGING_WIND);
		registerQuestItems(ESSENCE_OF_WIND);
	}
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		final QuestState st = getQuestState(player, false);
		if (st == null)
		{
			return getNoQuestMsg(player);
		}
		
		String htmltext = event;
		
		switch (event)
		{
			case "31570-03.htm":
				st.startQuest();
				break;
			case "31570-07.html":
				if ((st.isCond(2)) && (getQuestItemsCount(player, ESSENCE_OF_WIND) >= 100))
				{
					htmltext = "31570-06.htm";
					giveItems(player, WIND_FISHING_LURE, 4);
					st.exitQuest(false, true);
				}
				break;
		}
		return htmltext;
	}
	
	@Override
	public String onKill(L2Npc npc, L2PcInstance player, boolean isSummon)
	{
		final L2PcInstance partyMember = getRandomPartyMember(player, 1);
		if (partyMember == null)
		{
			return null;
		}
		
		final QuestState st = getQuestState(partyMember, false);
		
		if (getQuestItemsCount(player, ESSENCE_OF_WIND) < 100)
		{
			final float chance = 33 * Config.RATE_QUEST_DROP;
			if (getRandom(100) < chance)
			{
				rewardItems(player, ESSENCE_OF_WIND, 1);
				playSound(player, QuestSound.ITEMSOUND_QUEST_ITEMGET);
			}
		}
		
		if (getQuestItemsCount(player, ESSENCE_OF_WIND) >= 100)
		{
			st.setCond(2, true);
			
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
			case State.COMPLETED:
				htmltext = getAlreadyCompletedMsg(player);
				break;
			case State.CREATED:
				htmltext = (player.getLevel() >= 27) ? "31570-01.htm" : "31570-02.html";
				break;
			case State.STARTED:
				htmltext = (st.isCond(1)) ? "31570-05.html" : "31570-04.html";
				break;
		}
		return htmltext;
	}
}
