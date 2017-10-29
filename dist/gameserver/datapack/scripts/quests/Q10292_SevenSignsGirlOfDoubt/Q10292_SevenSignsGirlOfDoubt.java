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
package quests.Q10292_SevenSignsGirlOfDoubt;

import com.la2eden.gameserver.ThreadPoolManager;
import com.la2eden.gameserver.enums.QuestSound;
import com.la2eden.gameserver.model.actor.L2Npc;
import com.la2eden.gameserver.model.actor.instance.L2PcInstance;
import com.la2eden.gameserver.model.holders.ItemHolder;
import com.la2eden.gameserver.model.quest.Quest;
import com.la2eden.gameserver.model.quest.QuestState;
import com.la2eden.gameserver.util.Util;

import quests.Q00198_SevenSignsEmbryo.Q00198_SevenSignsEmbryo;

/**
 * Seven Signs, Girl of Doubt (10292)
 * @author Adry_85
 */
public final class Q10292_SevenSignsGirlOfDoubt extends Quest
{
	// NPCs
	private static final int HARDIN = 30832;
	private static final int WOOD = 32593;
	private static final int FRANZ = 32597;
	private static final int JAINA = 32617;
	private static final int ELCADIA = 32784;
	// Item
	private static final ItemHolder ELCADIAS_MARK = new ItemHolder(17226, 10);
	// Misc
	private static final int MIN_LEVEL = 81;
	private boolean isBusy = false;
	// Monster
	private static final int CREATURE_OF_THE_DUSK1 = 27422;
	private static final int CREATURE_OF_THE_DUSK2 = 27424;
	private static final int[] MOBS =
	{
		22801, // Cruel Pincer Golem
		22802, // Cruel Pincer Golem
		22803, // Cruel Pincer Golem
		22804, // Horrifying Jackhammer Golem
		22805, // Horrifying Jackhammer Golem
		22806, // Horrifying Jackhammer Golem
	};
	
	public Q10292_SevenSignsGirlOfDoubt()
	{
		super(10292, Q10292_SevenSignsGirlOfDoubt.class.getSimpleName(), "Seven Signs, Girl of Doubt");
		addStartNpc(WOOD);
		addTalkId(WOOD, FRANZ, JAINA, ELCADIA, HARDIN);
		addKillId(MOBS);
		addKillId(CREATURE_OF_THE_DUSK1, CREATURE_OF_THE_DUSK2);
		registerQuestItems(ELCADIAS_MARK.getId());
	}
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		final QuestState qs = getQuestState(player, false);
		if (qs == null)
		{
			return null;
		}
		
		String htmltext = null;
		switch (event)
		{
			case "32593-04.htm":
			case "32593-06.htm":
			{
				htmltext = event;
				break;
			}
			case "32593-05.htm":
			{
				qs.startQuest();
				htmltext = event;
				break;
			}
			case "32597-02.html":
			case "32597-06.html":
			{
				htmltext = event;
				break;
			}
			case "32597-07.html":
			{
				qs.setCond(2, true);
				htmltext = event;
				break;
			}
			case "32784-02.html":
			{
				if (qs.isCond(2))
				{
					htmltext = event;
				}
				break;
			}
			case "32784-03.html":
			{
				if (qs.isCond(2))
				{
					qs.setCond(3, true);
					htmltext = event;
				}
				break;
			}
			case "32784-06.html":
			{
				if (qs.isCond(4) && hasItem(player, ELCADIAS_MARK))
				{
					playSound(player, QuestSound.ITEMSOUND_QUEST_MIDDLE);
					htmltext = event;
				}
				break;
			}
			case "32784-08.html":
			{
				if (qs.isCond(4) && hasItem(player, ELCADIAS_MARK))
				{
					takeItem(player, ELCADIAS_MARK);
					qs.setCond(5, true);
					htmltext = event;
				}
				break;
			}
			case "32784-12.html":
			case "32784-13.html":
			{
				if (qs.isCond(6))
				{
					htmltext = event;
				}
				break;
			}
			case "32784-14.html":
			{
				if (qs.isCond(6))
				{
					qs.setCond(7, true);
					htmltext = event;
				}
				break;
			}
			case "SPAWN":
			{
				if (qs.isCond(5))
				{
					isBusy = true;
					final L2Npc creature1 = addSpawn(CREATURE_OF_THE_DUSK1, 89440, -238016, -9632, 335, false, 0, false, player.getInstanceId());
					creature1.setIsNoRndWalk(true);
					final L2Npc creature2 = addSpawn(CREATURE_OF_THE_DUSK2, 89524, -238131, -9632, 56, false, 0, false, player.getInstanceId());
					creature2.setIsNoRndWalk(true);
					ThreadPoolManager.getInstance().scheduleGeneral(() ->
					{
						creature1.deleteMe();
						creature2.deleteMe();
						qs.unset("ex");
						isBusy = false;
					}, 60000);
				}
				break;
			}
			case "30832-02.html":
			{
				if (qs.isCond(7))
				{
					qs.setCond(8, true);
					htmltext = event;
				}
				break;
			}
			case "30832-03.html":
			{
				if (qs.isCond(8))
				{
					htmltext = event;
				}
				break;
			}
		}
		return htmltext;
	}
	
	@Override
	public String onKill(L2Npc npc, L2PcInstance player, boolean isSummon)
	{
		if (Util.contains(MOBS, npc.getId()))
		{
			final QuestState qs = getRandomPartyMemberState(player, 3, 3, npc);
			if ((qs != null) && giveItemRandomly(qs.getPlayer(), npc, ELCADIAS_MARK.getId(), 1, ELCADIAS_MARK.getCount(), 1.0, true))
			{
				qs.setCond(4, true);
			}
		}
		else
		{
			final QuestState qs = getQuestState(player, false);
			if ((qs != null) && qs.isCond(5))
			{
				final int value = qs.getInt("ex") + 1;
				qs.set("ex", value);
				if (value == 2)
				{
					qs.setCond(6, true);
				}
			}
		}
		return super.onKill(npc, player, isSummon);
	}
	
	@Override
	public String onTalk(L2Npc npc, L2PcInstance player)
	{
		QuestState qs = getQuestState(player, true);
		String htmltext = getNoQuestMsg(player);
		switch (npc.getId())
		{
			case WOOD:
			{
				if (qs.isCompleted())
				{
					htmltext = "32593-02.html";
				}
				else if (qs.isCreated())
				{
					qs = player.getQuestState(Q00198_SevenSignsEmbryo.class.getSimpleName());
					htmltext = ((player.getLevel() >= MIN_LEVEL) && (qs != null) && qs.isCompleted()) ? "32593-01.htm" : "32593-03.htm";
				}
				else if (qs.isStarted())
				{
					htmltext = "32593-07.html";
				}
				break;
			}
			case FRANZ:
			{
				if (qs.isCond(1))
				{
					htmltext = "32597-01.html";
				}
				else if (qs.isCond(2))
				{
					htmltext = "32597-03.html";
				}
				break;
			}
			case ELCADIA:
			{
				switch (qs.getCond())
				{
					case 2:
					{
						htmltext = "32784-01.html";
						break;
					}
					case 3:
					{
						htmltext = "32784-04.html";
						break;
					}
					case 4:
					{
						if (hasItem(player, ELCADIAS_MARK))
						{
							playSound(player, QuestSound.ITEMSOUND_QUEST_MIDDLE);
							htmltext = "32784-05.html";
						}
						break;
					}
					case 5:
					{
						htmltext = isBusy ? "32784-17.html" : "32784-07.html";
						break;
					}
					case 6:
					{
						htmltext = "32784-11.html";
						break;
					}
					case 7:
					{
						htmltext = "32784-15.html";
						break;
					}
					case 8:
					{
						if (player.isSubClassActive())
						{
							htmltext = "32784-18.html";
						}
						else
						{
							addExpAndSp(player, 10000000, 1000000);
							qs.exitQuest(false, true);
							htmltext = "32784-16.html";
						}
						break;
					}
				}
				break;
			}
			case HARDIN:
			{
				if (qs.isCond(7))
				{
					htmltext = "30832-01.html";
				}
				else if (qs.isCond(8))
				{
					htmltext = "30832-04.html";
				}
				break;
			}
		}
		return htmltext;
	}
}
