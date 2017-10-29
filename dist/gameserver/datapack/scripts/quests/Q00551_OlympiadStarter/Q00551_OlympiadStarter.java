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
package quests.Q00551_OlympiadStarter;

import com.la2eden.gameserver.enums.QuestType;
import com.la2eden.gameserver.model.actor.L2Npc;
import com.la2eden.gameserver.model.actor.instance.L2PcInstance;
import com.la2eden.gameserver.model.olympiad.CompetitionType;
import com.la2eden.gameserver.model.olympiad.Participant;
import com.la2eden.gameserver.model.quest.Quest;
import com.la2eden.gameserver.model.quest.QuestState;
import com.la2eden.gameserver.model.quest.State;

/**
 * Olympiad Starter (551)
 * @author Gnacik
 */
public class Q00551_OlympiadStarter extends Quest
{
	private static final int MANAGER = 31688;
	
	private static final int CERT_3 = 17238;
	private static final int CERT_5 = 17239;
	private static final int CERT_10 = 17240;
	
	private static final int OLY_CHEST = 17169;
	private static final int MEDAL_OF_GLORY = 21874;
	
	public Q00551_OlympiadStarter()
	{
		super(551, Q00551_OlympiadStarter.class.getSimpleName(), "Olympiad Starter");
		addStartNpc(MANAGER);
		addTalkId(MANAGER);
		registerQuestItems(CERT_3, CERT_5, CERT_10);
		addOlympiadMatchFinishId();
	}
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		final QuestState qs = getQuestState(player, false);
		if (qs == null)
		{
			return getNoQuestMsg(player);
		}
		String htmltext = event;
		
		if (event.equalsIgnoreCase("31688-03.html"))
		{
			qs.startQuest();
		}
		else if (event.equalsIgnoreCase("31688-04.html"))
		{
			final long count = Math.min(getQuestItemsCount(player, CERT_3) + getQuestItemsCount(player, CERT_5), 2);
			if (count > 0)
			{
				giveItems(player, OLY_CHEST, count); // max 2
				if (count == 2)
				{
					giveItems(player, MEDAL_OF_GLORY, 3);
				}
				qs.exitQuest(QuestType.DAILY, true);
			}
			else
			{
				htmltext = getNoQuestMsg(player);
			}
		}
		return htmltext;
	}
	
	@Override
	public void onOlympiadLose(L2PcInstance loser, CompetitionType type)
	{
		if (loser == null)
		{
			return;
		}
		final QuestState qs = getQuestState(loser, false);
		if ((qs == null) || !qs.isStarted())
		{
			return;
		}
		final int matches = qs.getInt("matches") + 1;
		switch (matches)
		{
			case 3:
			{
				if (!hasQuestItems(loser, CERT_3))
				{
					giveItems(loser, CERT_3, 1);
				}
				break;
			}
			case 5:
			{
				if (!hasQuestItems(loser, CERT_5))
				{
					giveItems(loser, CERT_5, 1);
				}
				break;
			}
			case 10:
			{
				if (!hasQuestItems(loser, CERT_10))
				{
					giveItems(loser, CERT_10, 1);
				}
				break;
			}
		}
		qs.set("matches", String.valueOf(matches));
	}
	
	@Override
	public void onOlympiadMatchFinish(Participant winner, Participant looser, CompetitionType type)
	{
		if (winner != null)
		{
			final L2PcInstance player = winner.getPlayer();
			if (player == null)
			{
				return;
			}
			final QuestState qs = getQuestState(player, false);
			if ((qs != null) && qs.isStarted())
			{
				final int matches = qs.getInt("matches") + 1;
				switch (matches)
				{
					case 3:
					{
						if (!hasQuestItems(player, CERT_3))
						{
							giveItems(player, CERT_3, 1);
						}
						break;
					}
					case 5:
					{
						if (!hasQuestItems(player, CERT_5))
						{
							giveItems(player, CERT_5, 1);
						}
						break;
					}
					case 10:
					{
						if (!hasQuestItems(player, CERT_10))
						{
							giveItems(player, CERT_10, 1);
						}
						break;
					}
				}
				qs.set("matches", String.valueOf(matches));
			}
		}
		
		if (looser == null)
		{
			return;
		}
		final L2PcInstance player = looser.getPlayer();
		if (player == null)
		{
			return;
		}
		final QuestState qs = getQuestState(player, false);
		if ((qs == null) || !qs.isStarted())
		{
			return;
		}
		final int matches = qs.getInt("matches") + 1;
		switch (matches)
		{
			case 3:
			{
				if (!hasQuestItems(player, CERT_3))
				{
					giveItems(player, CERT_3, 1);
				}
				break;
			}
			case 5:
			{
				if (!hasQuestItems(player, CERT_5))
				{
					giveItems(player, CERT_5, 1);
				}
				break;
			}
			case 10:
			{
				if (!hasQuestItems(player, CERT_10))
				{
					giveItems(player, CERT_10, 1);
				}
				break;
			}
		}
		qs.set("matches", String.valueOf(matches));
	}
	
	@Override
	public String onTalk(L2Npc npc, L2PcInstance player)
	{
		String htmltext = getNoQuestMsg(player);
		final QuestState qs = getQuestState(player, true);
		if (qs == null)
		{
			return htmltext;
		}
		
		if ((player.getLevel() < 75) || !player.isNoble())
		{
			htmltext = "31688-00.htm";
		}
		else if (qs.isCreated())
		{
			htmltext = "31688-01.htm";
		}
		else if (qs.isCompleted())
		{
			if (qs.isNowAvailable())
			{
				qs.setState(State.CREATED);
				htmltext = (player.getLevel() < 75) || !player.isNoble() ? "31688-00.htm" : "31688-01.htm";
			}
			else
			{
				htmltext = "31688-05.html";
			}
		}
		else if (qs.isStarted())
		{
			final long count = Math.min(getQuestItemsCount(player, CERT_3) + getQuestItemsCount(player, CERT_5) + getQuestItemsCount(player, CERT_10), 3);
			if (count >= 3)
			{
				htmltext = "31688-04.html";
				giveItems(player, OLY_CHEST, 4);
				giveItems(player, MEDAL_OF_GLORY, 5);
				qs.exitQuest(QuestType.DAILY, true);
			}
			else
			{
				htmltext = "31688-s" + count + ".html";
			}
		}
		return htmltext;
	}
}
