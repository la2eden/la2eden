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
package instances.LibraryOfSages;

import com.la2eden.gameserver.enums.ChatType;
import com.la2eden.gameserver.instancemanager.InstanceManager;
import com.la2eden.gameserver.model.Location;
import com.la2eden.gameserver.model.actor.L2Npc;
import com.la2eden.gameserver.model.actor.instance.L2PcInstance;
import com.la2eden.gameserver.model.instancezone.InstanceWorld;
import com.la2eden.gameserver.network.NpcStringId;

import instances.AbstractInstance;

/**
 * Library of Sages instance zone.
 * @author Adry_85
 */
public final class LibraryOfSages extends AbstractInstance
{
	protected class LoSWorld extends InstanceWorld
	{
		protected L2Npc elcadia = null;
	}
	
	// NPCs
	private static final int SOPHIA1 = 32596;
	private static final int PILE_OF_BOOKS1 = 32809;
	private static final int PILE_OF_BOOKS2 = 32810;
	private static final int PILE_OF_BOOKS3 = 32811;
	private static final int PILE_OF_BOOKS4 = 32812;
	private static final int PILE_OF_BOOKS5 = 32813;
	private static final int SOPHIA2 = 32861;
	private static final int SOPHIA3 = 32863;
	private static final int ELCADIA_INSTANCE = 32785;
	// Locations
	private static final Location START_LOC = new Location(37063, -49813, -1128);
	private static final Location EXIT_LOC = new Location(37063, -49813, -1128, 0, 0);
	private static final Location LIBRARY_LOC = new Location(37355, -50065, -1127);
	// NpcString
	private static final NpcStringId[] ELCADIA_DIALOGS =
	{
		NpcStringId.I_MUST_ASK_LIBRARIAN_SOPHIA_ABOUT_THE_BOOK,
		NpcStringId.THIS_LIBRARY_IT_S_HUGE_BUT_THERE_AREN_T_MANY_USEFUL_BOOKS_RIGHT,
		NpcStringId.AN_UNDERGROUND_LIBRARY_I_HATE_DAMP_AND_SMELLY_PLACES,
		NpcStringId.THE_BOOK_THAT_WE_SEEK_IS_CERTAINLY_HERE_SEARCH_INCH_BY_INCH
	};
	// Misc
	private static final int TEMPLATE_ID = 156;
	
	public LibraryOfSages()
	{
		super(LibraryOfSages.class.getSimpleName());
		addFirstTalkId(SOPHIA2, ELCADIA_INSTANCE, PILE_OF_BOOKS1, PILE_OF_BOOKS2, PILE_OF_BOOKS3, PILE_OF_BOOKS4, PILE_OF_BOOKS5);
		addStartNpc(SOPHIA1, SOPHIA2, SOPHIA3);
		addTalkId(SOPHIA1, SOPHIA2, SOPHIA3);
	}
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		final InstanceWorld tmpworld = InstanceManager.getInstance().getPlayerWorld(player);
		if (tmpworld instanceof LoSWorld)
		{
			final LoSWorld world = (LoSWorld) tmpworld;
			switch (event)
			{
				case "TELEPORT2":
				{
					teleportPlayer(player, LIBRARY_LOC, world.getInstanceId());
					world.elcadia.teleToLocation(LIBRARY_LOC.getX(), LIBRARY_LOC.getY(), LIBRARY_LOC.getZ(), 0, world.getInstanceId());
					break;
				}
				case "exit":
				{
					cancelQuestTimer("FOLLOW", npc, player);
					player.teleToLocation(EXIT_LOC);
					world.elcadia.deleteMe();
					break;
				}
				case "FOLLOW":
				{
					npc.setIsRunning(true);
					npc.getAI().startFollow(player);
					broadcastNpcSay(npc, ChatType.NPC_GENERAL, ELCADIA_DIALOGS[getRandom(ELCADIA_DIALOGS.length)]);
					startQuestTimer("FOLLOW", 10000, npc, player);
					break;
				}
				case "ENTER":
				{
					cancelQuestTimer("FOLLOW", npc, player);
					teleportPlayer(player, START_LOC, world.getInstanceId());
					world.elcadia.teleToLocation(START_LOC.getX(), START_LOC.getY(), START_LOC.getZ(), 0, world.getInstanceId());
					break;
				}
			}
		}
		return super.onAdvEvent(event, npc, player);
	}
	
	@Override
	public String onTalk(L2Npc npc, L2PcInstance talker)
	{
		enterInstance(talker, new LoSWorld(), "LibraryOfSages.xml", TEMPLATE_ID);
		return super.onTalk(npc, talker);
	}
	
	@Override
	public void onEnterInstance(L2PcInstance player, InstanceWorld world, boolean firstEntrance)
	{
		if (firstEntrance)
		{
			world.addAllowed(player.getObjectId());
		}
		teleportPlayer(player, START_LOC, world.getInstanceId(), false);
		spawnElcadia(player, (LoSWorld) world);
	}
	
	private void spawnElcadia(L2PcInstance player, LoSWorld world)
	{
		if (world.elcadia != null)
		{
			world.elcadia.deleteMe();
		}
		world.elcadia = addSpawn(ELCADIA_INSTANCE, player, false, 0, false, player.getInstanceId());
		startQuestTimer("FOLLOW", 3000, world.elcadia, player);
	}
}
