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
package quests.Q00172_NewHorizons;

import com.la2eden.gameserver.enums.Race;
import com.la2eden.gameserver.model.actor.L2Npc;
import com.la2eden.gameserver.model.actor.instance.L2PcInstance;
import com.la2eden.gameserver.model.quest.Quest;
import com.la2eden.gameserver.model.quest.QuestState;
import com.la2eden.gameserver.model.quest.State;

/**
 * New Horizons (172)
 * @author malyelfik
 */
public class Q00172_NewHorizons extends Quest
{
	// NPCs
	private static final int ZENYA = 32140;
	private static final int RAGARA = 32163;
	
	// Items
	private static final int SCROLL_OF_ESCAPE_GIRAN = 7559;
	private static final int MARK_OF_TRAVELER = 7570;
	
	// Misc
	private static final int MIN_LEVEL = 3;
	
	public Q00172_NewHorizons()
	{
		super(172, Q00172_NewHorizons.class.getSimpleName(), "New Horizons");
		addStartNpc(ZENYA);
		addTalkId(ZENYA, RAGARA);
	}
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		final QuestState st = getQuestState(player, false);
		if (st == null)
		{
			return null;
		}
		
		String htmltext = event;
		switch (event)
		{
			case "32140-04.htm":
				st.startQuest();
				break;
			case "32163-02.html":
				giveItems(player, SCROLL_OF_ESCAPE_GIRAN, 1);
				giveItems(player, MARK_OF_TRAVELER, 1);
				st.exitQuest(false, true);
				break;
			default:
				htmltext = null;
				break;
		}
		return htmltext;
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
		
		switch (npc.getId())
		{
			case ZENYA:
				switch (st.getState())
				{
					case State.CREATED:
						htmltext = (player.getRace() == Race.KAMAEL) ? (player.getLevel() >= MIN_LEVEL) ? "32140-01.htm" : "32140-02.htm" : "32140-03.htm";
						break;
					case State.STARTED:
						htmltext = "32140-05.html";
						break;
					case State.COMPLETED:
						htmltext = getAlreadyCompletedMsg(player);
						break;
				}
				break;
			case RAGARA:
				if (st.isStarted())
				{
					htmltext = "32163-01.html";
				}
				break;
		}
		return htmltext;
	}
}