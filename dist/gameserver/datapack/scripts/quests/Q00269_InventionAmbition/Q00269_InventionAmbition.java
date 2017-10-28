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
package quests.Q00269_InventionAmbition;

import java.util.HashMap;
import java.util.Map;

import com.la2eden.gameserver.enums.QuestSound;
import com.la2eden.gameserver.model.actor.L2Npc;
import com.la2eden.gameserver.model.actor.instance.L2PcInstance;
import com.la2eden.gameserver.model.quest.Quest;
import com.la2eden.gameserver.model.quest.QuestState;
import com.la2eden.gameserver.model.quest.State;

/**
 * Invention Ambition (269)
 * @author xban1x
 */
public final class Q00269_InventionAmbition extends Quest
{
	// NPC
	private static final int INVENTOR_MARU = 32486;
	// Items
	private static final int ENERGY_ORE = 10866;
	// Monsters
	private static final Map<Integer, Integer> MONSTERS = new HashMap<>();
	static
	{
		MONSTERS.put(21124, 46); // Red Eye Barbed Bat
		MONSTERS.put(21125, 48); // Northern Trimden
		MONSTERS.put(21126, 50); // Kerope Werewolf
		MONSTERS.put(21127, 64); // Northern Goblin
		MONSTERS.put(21128, 66); // Spine Golem
		MONSTERS.put(21129, 68); // Kerope Werewolf Chief
		MONSTERS.put(21130, 76); // Northern Goblin Leader
		MONSTERS.put(21131, 78); // Enchanted Spine Golem
	}
	// Misc
	private static final int MIN_LVL = 18;
	
	public Q00269_InventionAmbition()
	{
		super(269, Q00269_InventionAmbition.class.getSimpleName(), "Invention Ambition");
		addStartNpc(INVENTOR_MARU);
		addTalkId(INVENTOR_MARU);
		addKillId(MONSTERS.keySet());
		registerQuestItems(ENERGY_ORE);
	}
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		final QuestState st = getQuestState(player, false);
		String htmltext = null;
		if (st != null)
		{
			switch (event)
			{
				case "32486-03.htm":
				{
					htmltext = (player.getLevel() >= MIN_LVL) ? event : null;
					break;
				}
				case "32486-04.htm":
				{
					st.startQuest();
					htmltext = event;
					break;
				}
				case "32486-07.html":
				{
					st.exitQuest(true, true);
					htmltext = event;
					break;
				}
				case "32486-08.html":
				{
					htmltext = event;
					break;
				}
			}
		}
		return htmltext;
	}
	
	@Override
	public String onKill(L2Npc npc, L2PcInstance killer, boolean isSummon)
	{
		final QuestState st = getQuestState(killer, false);
		if ((st != null) && (getRandom(100) < MONSTERS.get(npc.getId())))
		{
			giveItems(killer, ENERGY_ORE, 1);
			playSound(killer, QuestSound.ITEMSOUND_QUEST_ITEMGET);
		}
		return super.onKill(npc, killer, isSummon);
	}
	
	@Override
	public String onTalk(L2Npc npc, L2PcInstance player)
	{
		final QuestState st = getQuestState(player, true);
		String htmltext = getNoQuestMsg(player);
		if (st != null)
		{
			switch (st.getState())
			{
				case State.CREATED:
				{
					htmltext = (player.getLevel() >= MIN_LVL) ? "32486-01.htm" : "32486-02.html";
					break;
				}
				case State.STARTED:
				{
					if (hasQuestItems(player, ENERGY_ORE))
					{
						final long count = getQuestItemsCount(player, ENERGY_ORE);
						giveAdena(player, (count * 50) + (count >= 10 ? 2044 : null), true);
						takeItems(player, ENERGY_ORE, -1);
						htmltext = "32486-06.html";
					}
					else
					{
						htmltext = "32486-05.html";
					}
					break;
				}
			}
		}
		return htmltext;
	}
}
