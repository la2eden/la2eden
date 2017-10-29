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
package quests.Q10295_SevenSignsSolinasTomb;

import com.la2eden.gameserver.enums.QuestSound;
import com.la2eden.gameserver.model.actor.L2Npc;
import com.la2eden.gameserver.model.actor.instance.L2PcInstance;
import com.la2eden.gameserver.model.quest.Quest;
import com.la2eden.gameserver.model.quest.QuestState;
import com.la2eden.gameserver.model.quest.State;
import com.la2eden.gameserver.network.serverpackets.OnEventTrigger;
import com.la2eden.gameserver.network.serverpackets.SocialAction;

import quests.Q10294_SevenSignsToTheMonasteryOfSilence.Q10294_SevenSignsToTheMonasteryOfSilence;

public final class Q10295_SevenSignsSolinasTomb extends Quest
{
	// NPCs
	private static final int ErissEvilThoughts = 32792;
	private static final int Elcadia = 32785;
	private static final int TeleportControlDevice1 = 32837;
	private static final int TeleportControlDevice2 = 32842;
	private static final int TombOfTheSaintess = 32843;
	private static final int SolinasEvilThoughts = 32793;
	private static final int AltarOfHallows_Staff = 32857;
	private static final int AltarOfHallows_Sword = 32858;
	private static final int AltarOfHallows_Scroll = 32859;
	private static final int AltarOfHallows_Shield = 32860;
	private static final int PowerfulDevice_Scroll = 32838;
	private static final int PowerfulDevice_Shield = 32839;
	private static final int PowerfulDevice_Sword = 32840;
	private static final int PowerfulDevice_Staff = 32841;
	// Mobs
	private static final int[] SOLINAS =
	{
		18952,
		18953,
		18954,
		18955
	};
	private static final int[] GUARDIANS =
	{
		18956,
		18957,
		18958,
		18959
	};
	// Client Effects
	private static final int TeleportDeviceEvent = 21100100;
	private static final int ScrollDeviceEvent = 21100200;
	private static final int ShieldDeviceEvent = 21100202;
	private static final int SwordDeviceEvent = 21100204;
	private static final int StaffDeviceEvent = 21100206;
	// Items
	private static final int ScrollOfAbstinence = 17228;
	private static final int ShieldOfSacrifice = 17229;
	private static final int SwordOfHolySpirit = 17230;
	private static final int StaffOfBlessing = 17231;
	// Misc
	private static final int MIN_LEVEL = 81;
	private static final int[] TALK =
	{
		ErissEvilThoughts,
		Elcadia,
		TeleportControlDevice1,
		TeleportControlDevice2,
		TombOfTheSaintess,
		SolinasEvilThoughts,
		AltarOfHallows_Staff,
		AltarOfHallows_Sword,
		AltarOfHallows_Scroll,
		AltarOfHallows_Shield,
		PowerfulDevice_Scroll,
		PowerfulDevice_Shield,
		PowerfulDevice_Sword,
		PowerfulDevice_Staff
	};
	
	public Q10295_SevenSignsSolinasTomb()
	{
		super(10295, Q10295_SevenSignsSolinasTomb.class.getSimpleName(), "Seven Signs, Solina's Tomb");
		addStartNpc(ErissEvilThoughts);
		for (int NPC : TALK)
		{
			addTalkId(NPC);
		}
		addKillId(SOLINAS);
		addKillId(GUARDIANS);
		questItemIds = new int[]
		{
			ScrollOfAbstinence,
			ShieldOfSacrifice,
			SwordOfHolySpirit,
			StaffOfBlessing
		};
	}
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		String htmltext = event;
		final QuestState qs = player.getQuestState(getName());
		int progress = qs.getInt("progress");
		switch (event)
		{
			case "32792-05.html":
			{
				qs.startQuest();
				qs.set("progress", "0");
				break;
			}
			case "32857-02.html":
			{
				if (hasQuestItems(player, StaffOfBlessing))
				{
					htmltext = "32857-03.html";
				}
				else
				{
					giveItems(player, StaffOfBlessing, 1);
					progress++;
					qs.set("progress", progress);
					playSound(player, QuestSound.ITEMSOUND_QUEST_MIDDLE);
					if (progress == 4)
					{
						openDoor(21100001, player.getInstanceId());
						openDoor(21100010, player.getInstanceId());
						openDoor(21100014, player.getInstanceId());
						openDoor(21100006, player.getInstanceId());
					}
					return null;
				}
				break;
			}
			case "32858-02.html":
			{
				if (hasQuestItems(player, SwordOfHolySpirit))
				{
					htmltext = "32858-03.html";
				}
				else
				{
					giveItems(player, SwordOfHolySpirit, 1);
					progress++;
					qs.set("progress", progress);
					playSound(player, QuestSound.ITEMSOUND_QUEST_MIDDLE);
					if (progress == 4)
					{
						openDoor(21100001, player.getInstanceId());
						openDoor(21100010, player.getInstanceId());
						openDoor(21100014, player.getInstanceId());
						openDoor(21100006, player.getInstanceId());
					}
					return null;
				}
				break;
			}
			case "32859-02.html":
			{
				if (hasQuestItems(player, ScrollOfAbstinence))
				{
					htmltext = "32859-03.html";
				}
				else
				{
					giveItems(player, ScrollOfAbstinence, 1);
					progress++;
					qs.set("progress", progress);
					playSound(player, QuestSound.ITEMSOUND_QUEST_MIDDLE);
					if (progress == 4)
					{
						openDoor(21100001, player.getInstanceId());
						openDoor(21100010, player.getInstanceId());
						openDoor(21100014, player.getInstanceId());
						openDoor(21100006, player.getInstanceId());
					}
					return null;
				}
				break;
			}
			case "32860-02.html":
			{
				if (hasQuestItems(player, ShieldOfSacrifice))
				{
					htmltext = "32860-03.html";
				}
				else
				{
					giveItems(player, ShieldOfSacrifice, 1);
					progress++;
					qs.set("progress", progress);
					playSound(player, QuestSound.ITEMSOUND_QUEST_MIDDLE);
					if (progress == 4)
					{
						openDoor(21100001, player.getInstanceId());
						openDoor(21100010, player.getInstanceId());
						openDoor(21100014, player.getInstanceId());
						openDoor(21100006, player.getInstanceId());
					}
					return null;
				}
				break;
			}
			case "deactivate-17231-21100206-18955":
			{
				if (hasQuestItems(player, StaffOfBlessing))
				{
					takeItems(player, StaffOfBlessing, -1);
					player.sendPacket(new OnEventTrigger(StaffDeviceEvent, false));
					addSpawn(18952, 41838, -249630, -6761, 0, false, 0, false, player.getInstanceId());
					return null;
				}
				htmltext = "32841-03.html";
				break;
			}
			case "deactivate-17228-21100200-18952":
			{
				if (hasQuestItems(player, ScrollOfAbstinence))
				{
					takeItems(player, ScrollOfAbstinence, -1);
					player.sendPacket(new OnEventTrigger(ScrollDeviceEvent, false));
					addSpawn(18953, 45391, -253186, -6761, 0, false, 0, false, player.getInstanceId());
					return null;
				}
				htmltext = "32838-03.html";
				break;
			}
			case "deactivate-17230-21100204-18954":
			{
				if (hasQuestItems(player, SwordOfHolySpirit))
				{
					takeItems(player, SwordOfHolySpirit, -1);
					player.sendPacket(new OnEventTrigger(SwordDeviceEvent, false));
					addSpawn(18955, 45396, -246124, -6761, 0, false, 0, false, player.getInstanceId());
					return null;
				}
				htmltext = "32840-03.html";
				break;
			}
			case "deactivate-17229-21100202-18953":
			{
				if (hasQuestItems(player, ShieldOfSacrifice))
				{
					takeItems(player, ShieldOfSacrifice, -1);
					player.sendPacket(new OnEventTrigger(ShieldDeviceEvent, false));
					addSpawn(18954, 48912, -249639, -6761, 0, false, 0, false, player.getInstanceId());
					return null;
				}
				htmltext = "32839-03.html";
				break;
			}
			case "32843-02.html":
			{
				qs.set("tomb_opened", 1);
				openDoor(21100101, player.getInstanceId());
				openDoor(21100102, player.getInstanceId());
				openDoor(21100103, player.getInstanceId());
				openDoor(21100104, player.getInstanceId());
				addSpawn(18956, 56504, -252840, -6760, 0, false, 0, false, player.getInstanceId());
				addSpawn(18956, 56504, -252728, -6760, 0, false, 0, false, player.getInstanceId());
				addSpawn(18956, 56392, -252728, -6760, 0, false, 0, false, player.getInstanceId());
				addSpawn(18956, 56408, -252840, -6760, 0, false, 0, false, player.getInstanceId());
				
				addSpawn(18957, 55672, -252728, -6760, 0, false, 0, false, player.getInstanceId());
				addSpawn(18957, 55752, -252840, -6760, 0, false, 0, false, player.getInstanceId());
				addSpawn(18957, 55768, -252840, -6760, 0, false, 0, false, player.getInstanceId());
				addSpawn(18957, 55752, -252712, -6760, 0, false, 0, false, player.getInstanceId());
				
				addSpawn(18958, 55672, -252120, -6760, 0, false, 0, false, player.getInstanceId());
				addSpawn(18958, 55752, -252120, -6760, 0, false, 0, false, player.getInstanceId());
				addSpawn(18958, 55656, -252216, -6760, 0, false, 0, false, player.getInstanceId());
				addSpawn(18958, 55736, -252216, -6760, 0, false, 0, false, player.getInstanceId());
				
				addSpawn(18959, 56520, -252232, -6760, 0, false, 0, false, player.getInstanceId());
				addSpawn(18959, 56520, -252104, -6760, 0, false, 0, false, player.getInstanceId());
				addSpawn(18959, 56424, -252104, -6760, 0, false, 0, false, player.getInstanceId());
				addSpawn(18959, 56440, -252216, -6760, 0, false, 0, false, player.getInstanceId());
				return null;
			}
			case "32793-04.html":
			{
				qs.setCond(2, true);
				return null;
			}
			case "32793-07.html":
			{
				qs.setCond(3, true);
				return null;
			}
			case "StartMovie":
			{
				player.showQuestMovie(26);
				openDoor(21100008, player.getInstanceId());
				openDoor(21100012, player.getInstanceId());
				openDoor(21100016, player.getInstanceId());
				openDoor(21100003, player.getInstanceId());
				openDoor(21100005, player.getInstanceId());
				openDoor(21100007, player.getInstanceId());
				openDoor(21100002, player.getInstanceId());
				openDoor(21100004, player.getInstanceId());
				openDoor(21100015, player.getInstanceId());
				openDoor(21100013, player.getInstanceId());
				openDoor(21100011, player.getInstanceId());
				openDoor(21100009, player.getInstanceId());
				player.sendPacket(new OnEventTrigger(TeleportDeviceEvent, true));
				player.sendPacket(new OnEventTrigger(ScrollDeviceEvent, true));
				player.sendPacket(new OnEventTrigger(ShieldDeviceEvent, true));
				player.sendPacket(new OnEventTrigger(SwordDeviceEvent, true));
				player.sendPacket(new OnEventTrigger(StaffDeviceEvent, true));
				return null;
			}
			case "seal_removed":
			{
				qs.set("seal_removed", "1");
				playSound(player, QuestSound.ITEMSOUND_QUEST_MIDDLE);
				player.showQuestMovie(27);
				return null;
			}
			case "open_door":
			{
				openDoor(21100018, player.getInstanceId());
				playSound(player, QuestSound.ITEMSOUND_QUEST_MIDDLE);
				return null;
			}
			case "NotReady":
			{
				return null;
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
		
		final int cond = qs.getCond();
		final int npcId = npc.getId();
		switch (qs.getState())
		{
			case State.CREATED:
			{
				final QuestState ToTheMonastery = player.getQuestState(Q10294_SevenSignsToTheMonasteryOfSilence.class.getSimpleName());
				if ((player.getLevel() >= 81) && ToTheMonastery.isCompleted())
				{
					htmltext = "32792-01.htm";
				}
				else
				{
					htmltext = "32792-00.html";
					qs.exitQuest(true);
				}
				break;
			}
			case State.STARTED:
			{
				if (npcId == ErissEvilThoughts)
				{
					if (qs.isCond(1) || qs.isCond(2))
					{
						htmltext = "32792-06.html";
					}
					if (qs.isCond(3) && (player.getLevel() >= MIN_LEVEL))
					{
						htmltext = "32792-07.html";
						addExpAndSp(player, 125000000, 12500000);
						player.broadcastPacket(new SocialAction(player.getObjectId(), 3));
						qs.exitQuest(false, true);
					}
				}
				else if (npcId == Elcadia)
				{
					if (qs.isCond(1))
					{
						if (qs.getInt("seal_removed") == 1)
						{
							htmltext = "32785-02.html";
						}
						else
						{
							htmltext = "32785-01.html";
						}
					}
					else if (qs.isCond(2))
					{
						htmltext = "32785-03.html";
					}
				}
				else if (npcId == TeleportControlDevice1)
				{
					if (qs.getInt("seal_removed") == 1)
					{
						htmltext = "32837-02.html";
					}
					else
					{
						htmltext = "32837-01.html";
					}
				}
				else if (npcId == TeleportControlDevice2)
				{
					if (qs.getInt("tomb_opened") == 1)
					{
						htmltext = "32842-01.html";
					}
				}
				else if (npcId == TombOfTheSaintess)
				{
					if (qs.getInt("tomb_opened") != 1)
					{
						htmltext = "32843-01.html";
					}
				}
				else if (npcId == SolinasEvilThoughts)
				{
					if (qs.isCond(1))
					{
						htmltext = "32793-01.html";
					}
					else if (qs.isCond(2))
					{
						htmltext = "32793-04.html";
					}
					else if (qs.isCond(3))
					{
						htmltext = "32793-07.html";
					}
				}
				if ((cond == 1) && (qs.getInt("seal_removed") != 1))
				{
					if (npcId == AltarOfHallows_Staff)
					{
						htmltext = "32857-01.html";
					}
					else if (npcId == AltarOfHallows_Sword)
					{
						htmltext = "32858-01.html";
					}
					else if (npcId == AltarOfHallows_Scroll)
					{
						htmltext = "32859-01.html";
					}
					else if (npcId == AltarOfHallows_Shield)
					{
						htmltext = "32860-01.html";
					}
					else if (npcId == PowerfulDevice_Scroll)
					{
						htmltext = "32838-01.html";
					}
					else if (npcId == PowerfulDevice_Shield)
					{
						htmltext = "32839-01.html";
					}
					else if (npcId == PowerfulDevice_Sword)
					{
						htmltext = "32840-01.html";
					}
					else if (npcId == PowerfulDevice_Staff)
					{
						htmltext = "32841-01.html";
					}
				}
				break;
			}
			case State.COMPLETED:
			{
				htmltext = getNoQuestMsg(player);
				break;
			}
		}
		return htmltext;
	}
	
	@Override
	public String onKill(L2Npc npc, L2PcInstance player, boolean isPet)
	{
		final QuestState qs = player.getQuestState(getName());
		int tomb = qs.getInt("tomb");
		int solina = qs.getInt("solina");
		final int npcId = npc.getId();
		if (qs.isCond(1))
		{
			if (npcId == SOLINAS[0])
			{
				solina++;
				qs.set("solina", solina);
				if (solina == 4)
				{
					qs.startQuestTimer("seal_removed", 1000);
				}
			}
			if (npcId == SOLINAS[1])
			{
				solina++;
				qs.set("solina", solina);
				if (solina == 4)
				{
					qs.startQuestTimer("seal_removed", 1000);
				}
			}
			if (npcId == SOLINAS[2])
			{
				solina++;
				qs.set("solina", solina);
				if (solina == 4)
				{
					qs.startQuestTimer("seal_removed", 1000);
				}
			}
			if (npcId == SOLINAS[3])
			{
				solina++;
				qs.set("solina", solina);
				if (solina == 4)
				{
					qs.startQuestTimer("seal_removed", 1000);
				}
			}
			if (npcId == GUARDIANS[0])
			{
				tomb++;
				qs.set("tomb", tomb);
				if (tomb == 16)
				{
					qs.startQuestTimer("open_door", 1000);
				}
			}
			if (npcId == GUARDIANS[1])
			{
				tomb++;
				qs.set("tomb", tomb);
				if (tomb == 16)
				{
					qs.startQuestTimer("open_door", 1000);
				}
			}
			if (npcId == GUARDIANS[2])
			{
				tomb++;
				qs.set("tomb", tomb);
				if (tomb == 16)
				{
					qs.startQuestTimer("open_door", 1000);
				}
			}
			if (npcId == GUARDIANS[3])
			{
				tomb++;
				qs.set("tomb", tomb);
				if (tomb == 16)
				{
					qs.startQuestTimer("open_door", 1000);
				}
			}
		}
		return null;
	}
}