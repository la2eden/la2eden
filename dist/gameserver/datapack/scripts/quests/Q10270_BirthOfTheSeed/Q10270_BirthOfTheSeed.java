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
package quests.Q10270_BirthOfTheSeed;

import com.la2eden.gameserver.model.L2CommandChannel;
import com.la2eden.gameserver.model.L2Party;
import com.la2eden.gameserver.model.actor.L2Npc;
import com.la2eden.gameserver.model.actor.instance.L2PcInstance;
import com.la2eden.gameserver.model.quest.Quest;
import com.la2eden.gameserver.model.quest.QuestState;
import com.la2eden.gameserver.model.quest.State;

public class Q10270_BirthOfTheSeed extends Quest
{
	private static final int PLENOS = 32563;
	private static final int KLODEKUS = 25665;
	private static final int KLANIKUS = 25666;
	private static final int COHEMENES = 25634;
	private static final int JINBI = 32566;
	private static final int RELRIKIA = 32567;
	private static final int ARTIUS = 32559;
	
	public Q10270_BirthOfTheSeed()
	{
		super(10270, Q10270_BirthOfTheSeed.class.getSimpleName(), "Birth of the Seed");
		questItemIds = new int[]
		{
			13868,
			13869,
			13870
		};
		addStartNpc(PLENOS);
		addTalkId(PLENOS);
		addTalkId(RELRIKIA);
		addTalkId(JINBI);
		addTalkId(ARTIUS);
		addKillId(KLANIKUS);
		addKillId(KLODEKUS);
		addKillId(COHEMENES);
	}
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		String htmltext = event;
		final QuestState qs = player.getQuestState(getName());
		if (event.equalsIgnoreCase("32563-05.html"))
		{
			qs.startQuest();
		}
		else if (event.equalsIgnoreCase("32559-03.html"))
		{
			qs.setCond(2, true);
		}
		else if (event.equalsIgnoreCase("32559-09.html"))
		{
			qs.setCond(4, true);
		}
		else if (event.equalsIgnoreCase("32559-13.html"))
		{
			qs.exitQuest(false);
			addExpAndSp(player, 251602, 25244);
			giveAdena(player, 41677, true);
		}
		else if (event.equalsIgnoreCase("32566-05.html"))
		{
			if (getQuestItemsCount(player, 57) < 10000)
			{
				htmltext = "32566-04a.html";
			}
			else
			{
				takeItems(player, 57, 10000);
				qs.set("pay", "1");
			}
		}
		else if (event.equalsIgnoreCase("32567-05.html"))
		{
			qs.setCond(5, true);
		}
		
		return htmltext;
	}
	
	@Override
	public String onTalk(L2Npc npc, L2PcInstance player)
	{
		final QuestState qs = getQuestState(player, true);
		final int cond = qs.getCond();
		String htmltext = getNoQuestMsg(player);
		
		switch (npc.getId())
		{
			case PLENOS:
			{
				switch (qs.getState())
				{
					case State.CREATED:
					{
						if (player.getLevel() < 75)
						{
							htmltext = "32563-02.html";
						}
						else
						{
							htmltext = "32563-01.htm";
						}
						break;
					}
					case State.STARTED:
					{
						if (cond == 1)
						{
							htmltext = "32563-06.html";
						}
						break;
					}
					case State.COMPLETED:
					{
						htmltext = "32563-03.html";
						break;
					}
				}
				break;
			}
			case ARTIUS:
			{
				if (cond == 1)
				{
					htmltext = "32559-01.html";
				}
				else if (cond == 2)
				{
					if ((getQuestItemsCount(player, 13868) < 1) && (getQuestItemsCount(player, 13869) < 1) && (getQuestItemsCount(player, 13870) < 1))
					{
						htmltext = "32559-04.html";
					}
					else if ((getQuestItemsCount(player, 13868) + getQuestItemsCount(player, 13869) + getQuestItemsCount(player, 13870)) < 3)
					{
						htmltext = "32559-05.html";
					}
					else if ((getQuestItemsCount(player, 13868) == 1) && (getQuestItemsCount(player, 13869) == 1) && (getQuestItemsCount(player, 13870) == 1))
					{
						htmltext = "32559-06.html";
						takeItems(player, 13868, 1);
						takeItems(player, 13869, 1);
						takeItems(player, 13870, 1);
						qs.setCond(3, true);
					}
				}
				else if ((cond == 3) || (cond == 4))
				{
					htmltext = "32559-07.html";
				}
				else if (cond == 5)
				{
					htmltext = "32559-12.html";
				}
				if (qs.getState() == State.COMPLETED)
				{
					htmltext = "32559-02.html";
				}
				break;
			}
			case JINBI:
			{
				if (cond < 4)
				{
					htmltext = "32566-02.html";
				}
				else if (cond == 4)
				{
					if (qs.getInt("pay") == 1)
					{
						htmltext = "32566-10.html";
					}
					else
					{
						htmltext = "32566-04.html";
					}
				}
				else if (cond > 4)
				{
					htmltext = "32566-12.html";
				}
				
				break;
			}
			case RELRIKIA:
			{
				if (cond == 4)
				{
					htmltext = "32567-01.html";
				}
				else if (cond == 5)
				{
					htmltext = "32567-07.html";
				}
				break;
			}
		}
		
		return htmltext;
	}
	
	@Override
	public String onKill(L2Npc npc, L2PcInstance player, boolean isPet)
	{
		if (npc.getId() == KLANIKUS)
		{
			if (player.getParty() != null)
			{
				final L2Party party = player.getParty();
				if (party.getCommandChannel() != null)
				{
					final L2CommandChannel cc = party.getCommandChannel();
					for (L2PcInstance partyMember : cc.getMembers())
					{
						final QuestState qs = partyMember.getQuestState(getName());
						if ((qs != null) && qs.isCond(2))
						{
							giveItems(partyMember, 13869, 1);
						}
					}
				}
				else
				{
					for (L2PcInstance partyMember : party.getMembers())
					{
						final QuestState qs = partyMember.getQuestState(getName());
						if ((qs != null) && qs.isCond(2))
						{
							giveItems(partyMember, 13869, 1);
						}
					}
				}
			}
			else
			{
				final QuestState qs = player.getQuestState(getName());
				if ((qs != null) && qs.isCond(2))
				{
					giveItems(player, 13869, 1);
				}
			}
		}
		else if (npc.getId() == KLODEKUS)
		{
			if (player.getParty() != null)
			{
				final L2Party party = player.getParty();
				if (party.getCommandChannel() != null)
				{
					final L2CommandChannel cc = party.getCommandChannel();
					for (L2PcInstance partyMember : cc.getMembers())
					{
						final QuestState qs = partyMember.getQuestState(getName());
						if ((qs != null) && qs.isCond(2))
						{
							giveItems(partyMember, 13868, 1);
						}
					}
				}
				else
				{
					for (L2PcInstance partyMember : party.getMembers())
					{
						final QuestState qs = partyMember.getQuestState(getName());
						if ((qs != null) && qs.isCond(2))
						{
							giveItems(partyMember, 13868, 1);
						}
					}
				}
			}
			else
			{
				final QuestState qs = player.getQuestState(getName());
				if ((qs != null) && qs.isCond(2))
				{
					giveItems(player, 13868, 1);
				}
			}
		}
		else if (npc.getId() == COHEMENES)
		{
			if (player.getParty() != null)
			{
				final L2Party party = player.getParty();
				if (party.getCommandChannel() != null)
				{
					final L2CommandChannel cc = party.getCommandChannel();
					for (L2PcInstance partyMember : cc.getMembers())
					{
						final QuestState qs = partyMember.getQuestState(getName());
						if ((qs != null) && qs.isCond(2))
						{
							giveItems(partyMember, 13870, 1);
						}
					}
				}
				else
				{
					for (L2PcInstance partyMember : party.getMembers())
					{
						final QuestState qs = partyMember.getQuestState(getName());
						if ((qs != null) && qs.isCond(2))
						{
							giveItems(partyMember, 13870, 1);
						}
					}
				}
			}
			else
			{
				final QuestState qs = player.getQuestState(getName());
				if ((qs != null) && qs.isCond(2))
				{
					giveItems(player, 13870, 1);
				}
			}
		}
		return super.onKill(npc, player, isPet);
	}
}