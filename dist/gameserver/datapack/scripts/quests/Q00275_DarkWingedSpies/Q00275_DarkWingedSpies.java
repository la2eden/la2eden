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
package quests.Q00275_DarkWingedSpies;

import com.la2eden.gameserver.ai.CtrlIntention;
import com.la2eden.gameserver.enums.Race;
import com.la2eden.gameserver.model.actor.L2Attackable;
import com.la2eden.gameserver.model.actor.L2Character;
import com.la2eden.gameserver.model.actor.L2Npc;
import com.la2eden.gameserver.model.actor.instance.L2PcInstance;
import com.la2eden.gameserver.model.quest.Quest;
import com.la2eden.gameserver.model.quest.QuestState;
import com.la2eden.gameserver.model.quest.State;
import com.la2eden.gameserver.util.Util;

/**
 * Dark Winged Spies (275)
 * @author xban1x
 */
public final class Q00275_DarkWingedSpies extends Quest
{
	// Npc
	private static final int NERUGA_CHIEF_TANTUS = 30567;
	// Items
	private static final int DARKWING_BAT_FANG = 1478;
	private static final int VARANGKAS_PARASITE = 1479;
	// Monsters
	private static final int DARKWING_BAT = 20316;
	private static final int VARANGKAS_TRACKER = 27043;
	// Misc
	private static final int MIN_LVL = 11;
	private static final int FANG_PRICE = 60;
	private static final int MAX_BAT_FANG_COUNT = 70;
	
	public Q00275_DarkWingedSpies()
	{
		super(275, Q00275_DarkWingedSpies.class.getSimpleName(), "Dark Winged Spies");
		addStartNpc(NERUGA_CHIEF_TANTUS);
		addTalkId(NERUGA_CHIEF_TANTUS);
		addKillId(DARKWING_BAT, VARANGKAS_TRACKER);
		addSeeCreatureId(VARANGKAS_TRACKER);
		registerQuestItems(DARKWING_BAT_FANG, VARANGKAS_PARASITE);
	}
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		final QuestState st = getQuestState(player, false);
		if ((st != null) && event.equals("30567-03.htm"))
		{
			st.startQuest();
			return event;
		}
		return null;
	}
	
	@Override
	public String onKill(L2Npc npc, L2PcInstance killer, boolean isSummon)
	{
		final QuestState st = getQuestState(killer, false);
		
		if ((st != null) && st.isCond(1) && Util.checkIfInRange(1500, npc, killer, true))
		{
			final long count = getQuestItemsCount(killer, DARKWING_BAT_FANG);
			
			switch (npc.getId())
			{
				case DARKWING_BAT:
				{
					if (giveItemRandomly(killer, DARKWING_BAT_FANG, 1, MAX_BAT_FANG_COUNT, 1, true))
					{
						st.setCond(2);
					}
					else if ((count > 10) && (count < 66) && (getRandom(100) < 10))
					{
						st.addSpawn(VARANGKAS_TRACKER);
						giveItems(killer, VARANGKAS_PARASITE, 1);
					}
					break;
				}
				case VARANGKAS_TRACKER:
				{
					if ((count < 66) && hasQuestItems(killer, VARANGKAS_PARASITE))
					{
						if (giveItemRandomly(killer, DARKWING_BAT_FANG, 5, MAX_BAT_FANG_COUNT, 1, true))
						{
							st.setCond(2);
						}
						takeItems(killer, VARANGKAS_PARASITE, -1);
					}
					break;
				}
			}
		}
		
		return super.onKill(npc, killer, isSummon);
	}
	
	@Override
	public String onSeeCreature(L2Npc npc, L2Character creature, boolean isSummon)
	{
		if (creature.isPlayer())
		{
			npc.setRunning();
			((L2Attackable) npc).addDamageHate(creature, 0, 1);
			npc.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, creature);
		}
		return super.onSeeCreature(npc, creature, isSummon);
	}
	
	@Override
	public String onTalk(L2Npc npc, L2PcInstance talker)
	{
		final QuestState st = getQuestState(talker, true);
		String htmltext = getNoQuestMsg(talker);
		if (st == null)
		{
			return htmltext;
		}
		
		switch (st.getState())
		{
			case State.CREATED:
			{
				htmltext = (talker.getRace() == Race.ORC) ? (talker.getLevel() >= MIN_LVL) ? "30567-02.htm" : "30567-01.htm" : "30567-00.htm";
				break;
			}
			case State.STARTED:
			{
				switch (st.getCond())
				{
					case 1:
					{
						htmltext = "30567-05.html";
						break;
					}
					case 2:
					{
						final long count = getQuestItemsCount(talker, DARKWING_BAT_FANG);
						if (count >= MAX_BAT_FANG_COUNT)
						{
							giveAdena(talker, count * FANG_PRICE, true);
							st.exitQuest(true, true);
							htmltext = "30567-05.html";
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
