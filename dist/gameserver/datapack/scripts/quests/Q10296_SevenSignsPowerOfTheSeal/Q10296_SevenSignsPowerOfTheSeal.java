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
package quests.Q10296_SevenSignsPowerOfTheSeal;

import com.la2eden.gameserver.model.actor.L2Npc;
import com.la2eden.gameserver.model.actor.instance.L2PcInstance;
import com.la2eden.gameserver.model.quest.Quest;
import com.la2eden.gameserver.model.quest.QuestState;
import com.la2eden.gameserver.model.quest.State;

public final class Q10296_SevenSignsPowerOfTheSeal extends Quest
{
	// NPCs
	private static final int ErissEvilThoughts = 32792;
	private static final int Elcadia = 32784;
	private static final int Elcadia_Support = 32785;
	private static final int Hardin = 30832;
	private static final int Wood = 32593;
	private static final int Franz = 32597;
	// Mobs
	private static final int EtisVanEtina = 18949;
	// Items
	private static final int CertificateOfDawn = 17265;
	// Misc
	private static final int MIN_LEVEL = 81;
	
	public Q10296_SevenSignsPowerOfTheSeal()
	{
		super(10296, Q10296_SevenSignsPowerOfTheSeal.class.getSimpleName(), "Seven Signs, One Who Seeks the Power of the Seal");
		addStartNpc(ErissEvilThoughts);
		addTalkId(ErissEvilThoughts, Elcadia, Hardin, Wood, Franz, Elcadia_Support);
		addKillId(EtisVanEtina);
	}
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		final QuestState qs = getQuestState(player, false);
		if (qs == null)
		{
			return null;
		}
		
		String htmltext = event;
		switch (event)
		{
			case "32792-04.html":
			{
				qs.startQuest();
				break;
			}
			case "32784-03.html":
			{
				qs.setCond(4);
				break;
			}
			case "see":
			{
				qs.setCond(5);
				htmltext = "30832-03.html";
				break;
			}
			case "presentation":
			{
				player.showQuestMovie(28);
				break;
			}
			case "reward":
			{
				if (player.isSubClassActive())
				{
					htmltext = "32597-04.html";
				}
				else if (player.getLevel() >= MIN_LEVEL)
				{
					addExpAndSp(player, 125000000, 12500000);
					giveItems(player, CertificateOfDawn, 1);
					htmltext = "32597-03.html";
					qs.unset("boss");
					qs.exitQuest(false, true);
				}
				break;
			}
		}
		return htmltext;
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
		
		switch (npc.getId())
		{
			case ErissEvilThoughts:
			{
				switch (qs.getState())
				{
					case State.CREATED:
					{
						final QuestState SolinasTomb = player.getQuestState("Q10295_SevenSignsSolinasTomb");
						if ((player.getLevel() >= 81) && SolinasTomb.isCompleted())
						{
							htmltext = "32792-01.htm";
						}
						else
						{
							htmltext = "32792-12.html";
							qs.exitQuest(true);
						}
						break;
					}
					case State.STARTED:
					{
						switch (qs.getCond())
						{
							case 1:
							{
								qs.setCond(2, true);
								htmltext = "32792-05.html";
								break;
							}
							case 2:
							{
								htmltext = "32792-06.html";
								break;
							}
						}
						break;
					}
					case State.COMPLETED:
					{
						htmltext = "32792-02.html";
					}
				}
				break;
			}
			case Elcadia:
			{
				if (qs.isStarted())
				{
					switch (qs.getCond())
					{
						case 3:
						{
							htmltext = "32784-01.html";
							break;
						}
						case 4:
						{
							htmltext = "32784-04.html";
							break;
						}
					}
				}
				break;
			}
			case Hardin:
			{
				if (qs.isStarted())
				{
					switch (qs.getCond())
					{
						case 4:
						{
							htmltext = "30832-01.html";
							break;
						}
						case 5:
						{
							htmltext = "30832-03.html";
							break;
						}
					}
				}
				break;
			}
			case Wood:
			{
				if (qs.isStarted() && qs.isCond(5))
				{
					htmltext = "32593-01.html";
				}
				break;
			}
			case Franz:
			{
				if (qs.isStarted() && qs.isCond(5))
				{
					htmltext = "32597-01.html";
				}
				break;
			}
			case Elcadia_Support:
			{
				if (qs.isStarted())
				{
					if (qs.isCond(2) && (qs.getInt("boss") == 1))
					{
						qs.setCond(3, true);
						htmltext = "32785-01.html";
					}
					else if (qs.isCond(3))
					{
						htmltext = "32785-01.html";
					}
				}
				break;
			}
		}
		return htmltext;
	}
	
	@Override
	public String onKill(L2Npc npc, L2PcInstance player, boolean isPet)
	{
		final QuestState qs = player.getQuestState(getName());
		if (qs == null)
		{
			return null;
		}
		
		player.showQuestMovie(30);
		if (qs.getInt("boss") != 1)
		{
			qs.set("boss", "1");
		}
		
		return null;
	}
}