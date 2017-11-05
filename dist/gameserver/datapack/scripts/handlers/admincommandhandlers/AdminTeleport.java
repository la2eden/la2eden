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
package handlers.admincommandhandlers;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;
import java.util.logging.Logger;

import com.la2eden.Config;
import com.la2eden.commons.database.DatabaseFactory;
import com.la2eden.gameserver.ai.CtrlIntention;
import com.la2eden.gameserver.datatables.SpawnTable;
import com.la2eden.gameserver.handler.IAdminCommandHandler;
import com.la2eden.gameserver.instancemanager.MapRegionManager;
import com.la2eden.gameserver.instancemanager.RaidBossSpawnManager;
import com.la2eden.gameserver.model.L2Object;
import com.la2eden.gameserver.model.L2Spawn;
import com.la2eden.gameserver.model.L2World;
import com.la2eden.gameserver.model.Location;
import com.la2eden.gameserver.model.actor.L2Npc;
import com.la2eden.gameserver.model.actor.instance.L2GrandBossInstance;
import com.la2eden.gameserver.model.actor.instance.L2PcInstance;
import com.la2eden.gameserver.model.actor.instance.L2RaidBossInstance;
import com.la2eden.gameserver.network.SystemMessageId;
import com.la2eden.gameserver.network.serverpackets.NpcHtmlMessage;

/**
 * This class handles following admin commands: - show_moves - show_teleport - teleport_to_character - move_to - teleport_character
 * @version $Revision: 1.3.2.6.2.4 $ $Date: 2005/04/11 10:06:06 $ con.close() change and small typo fix by Zoey76 24/02/2011
 */
public class AdminTeleport implements IAdminCommandHandler
{
	private static final Logger _log = Logger.getLogger(AdminTeleport.class.getName());

	private static final String[] ADMIN_COMMANDS =
	{
		"admin_show_moves",
		"admin_show_moves_other",
		"admin_show_teleport",
		"admin_teleport_to_character",
		"admin_teleportto",
		"admin_move_to",
		"admin_teleport_character",
		"admin_recall",
		"admin_walk",
		"teleportto",
		"recall",
		"admin_recall_npc",
		"admin_gonorth",
		"admin_gosouth",
		"admin_goeast",
		"admin_gowest",
		"admin_goup",
		"admin_godown",
		"admin_tele",
		"admin_teleto",
		"admin_instant_move",
		"admin_sendhome"
	};

	@Override
	public boolean useAdminCommand(String command, L2PcInstance activeChar)
	{
		if (command.equals("admin_teleto"))
		{
			activeChar.setTeleMode(1);
		}
		if (command.equals("admin_instant_move"))
		{
			activeChar.sendMessage("Instant move ready. Click where you want to go.");
			activeChar.setTeleMode(1);
		}
		if (command.equals("admin_teleto r"))
		{
			activeChar.setTeleMode(2);
		}
		if (command.equals("admin_teleto end"))
		{
			activeChar.setTeleMode(0);
		}
		if (command.equals("admin_show_moves"))
		{
			AdminHtml.showAdminHtml(activeChar, "teleports.htm");
		}
		if (command.equals("admin_show_moves_other"))
		{
			AdminHtml.showAdminHtml(activeChar, "tele/other.html");
		}
		else if (command.equals("admin_show_teleport"))
		{
			showTeleportCharWindow(activeChar);
		}
		else if (command.equals("admin_recall_npc"))
		{
			recallNPC(activeChar);
		}
		else if (command.equals("admin_teleport_to_character"))
		{
			teleportToCharacter(activeChar, activeChar.getTarget());
		}
		else if (command.startsWith("admin_walk"))
		{
			try
			{
				final String val = command.substring(11);
				final StringTokenizer st = new StringTokenizer(val);
				final int x = Integer.parseInt(st.nextToken());
				final int y = Integer.parseInt(st.nextToken());
				final int z = Integer.parseInt(st.nextToken());
				activeChar.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, new Location(x, y, z, 0));
			}
			catch (Exception e)
			{
				if (Config.DEBUG)
				{
					_log.info("admin_walk: " + e);
				}
			}
		}
		else if (command.startsWith("admin_move_to"))
		{
			try
			{
				final String val = command.substring(14);
				teleportTo(activeChar, val);
			}
			catch (StringIndexOutOfBoundsException e)
			{
				// Case of empty or missing coordinates
				AdminHtml.showAdminHtml(activeChar, "teleports.htm");
			}
			catch (NumberFormatException nfe)
			{
				activeChar.sendMessage("Usage: //move_to <x> <y> <z>");
				AdminHtml.showAdminHtml(activeChar, "teleports.htm");
			}
		}
		else if (command.startsWith("admin_teleport_character"))
		{
			try
			{
				final String val = command.substring(25);

				teleportCharacter(activeChar, val);
			}
			catch (StringIndexOutOfBoundsException e)
			{
				// Case of empty coordinates
				activeChar.sendMessage("Wrong or no Coordinates given.");
				showTeleportCharWindow(activeChar); // back to character teleport
			}
		}
		else if (command.startsWith("admin_teleportto "))
		{
			try
			{
				final String targetName = command.substring(17);
				final L2PcInstance player = L2World.getInstance().getPlayer(targetName);
				teleportToCharacter(activeChar, player);
			}
			catch (StringIndexOutOfBoundsException e)
			{
			}
		}
		else if (command.startsWith("admin_recall "))
		{
			try
			{
				final String[] param = command.split(" ");
				if (param.length != 2)
				{
					activeChar.sendMessage("Usage: //recall <playername>");
					return false;
				}
				final String targetName = param[1];
				final L2PcInstance player = L2World.getInstance().getPlayer(targetName);
				if (player != null)
				{
					teleportCharacter(player, activeChar.getLocation(), activeChar);
				}
				else
				{
					changeCharacterPosition(activeChar, targetName);
				}
			}
			catch (StringIndexOutOfBoundsException e)
			{
			}
		}
		else if (command.equals("admin_tele"))
		{
			showTeleportWindow(activeChar);
		}
		else if (command.startsWith("admin_go"))
		{
			int intVal = 150;
			int x = activeChar.getX(), y = activeChar.getY(), z = activeChar.getZ();
			try
			{
				final String val = command.substring(8);
				final StringTokenizer st = new StringTokenizer(val);
				final String dir = st.nextToken();
				if (st.hasMoreTokens())
				{
					intVal = Integer.parseInt(st.nextToken());
				}
				if (dir.equals("east"))
				{
					x += intVal;
				}
				else if (dir.equals("west"))
				{
					x -= intVal;
				}
				else if (dir.equals("north"))
				{
					y -= intVal;
				}
				else if (dir.equals("south"))
				{
					y += intVal;
				}
				else if (dir.equals("up"))
				{
					z += intVal;
				}
				else if (dir.equals("down"))
				{
					z -= intVal;
				}
				activeChar.teleToLocation(new Location(x, y, z));
				showTeleportWindow(activeChar);
			}
			catch (Exception e)
			{
				activeChar.sendMessage("Usage: //go<north|south|east|west|up|down> [offset] (default 150)");
			}
		}
		else if (command.startsWith("admin_sendhome"))
		{
			final StringTokenizer st = new StringTokenizer(command, " ");
			st.nextToken(); // Skip command.
			if (st.countTokens() > 1)
			{
				activeChar.sendMessage("Usage: //sendhome <playername>");
			}
			else if (st.countTokens() == 1)
			{
				final String name = st.nextToken();
				final L2PcInstance player = L2World.getInstance().getPlayer(name);
				if (player == null)
				{
					activeChar.sendPacket(SystemMessageId.THAT_PLAYER_IS_NOT_ONLINE);
					return false;
				}
				teleportHome(player);
			}
			else
			{
				final L2Object target = activeChar.getTarget();
				if (target instanceof L2PcInstance)
				{
					teleportHome(target.getActingPlayer());
				}
				else
				{
					activeChar.sendPacket(SystemMessageId.INVALID_TARGET);
				}
			}
		}
		return true;
	}

	@Override
	public String[] getAdminCommandList()
	{
		return ADMIN_COMMANDS;
	}

	/**
	 * This method sends a player to it's home town.
	 * @param player the player to teleport.
	 */
	private void teleportHome(L2PcInstance player)
	{
		String regionName;
		switch (player.getRace())
		{
			case ELF:
				regionName = "elf_town";
				break;
			case DARK_ELF:
				regionName = "darkelf_town";
				break;
			case ORC:
				regionName = "orc_town";
				break;
			case DWARF:
				regionName = "dwarf_town";
				break;
			case KAMAEL:
				regionName = "kamael_town";
				break;
			case HUMAN:
			default:
				regionName = "talking_island_town";
		}

		player.teleToLocation(MapRegionManager.getInstance().getMapRegionByName(regionName).getSpawnLoc(), true);
		player.setInstanceId(0);
		player.setIsIn7sDungeon(false);
	}

	private void teleportTo(L2PcInstance activeChar, String Coords)
	{
		try
		{
			final StringTokenizer st = new StringTokenizer(Coords);
			final String x1 = st.nextToken();
			final int x = Integer.parseInt(x1);
			final String y1 = st.nextToken();
			final int y = Integer.parseInt(y1);
			final String z1 = st.nextToken();
			final int z = Integer.parseInt(z1);

			activeChar.getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
			activeChar.teleToLocation(x, y, z);

			activeChar.sendMessage("You have been teleported to " + Coords);
		}
		catch (NoSuchElementException nsee)
		{
			activeChar.sendMessage("Wrong or no Coordinates given.");
		}
	}

	private void showTeleportWindow(L2PcInstance activeChar)
	{
		AdminHtml.showAdminHtml(activeChar, "move.htm");
	}

	private void showTeleportCharWindow(L2PcInstance activeChar)
	{
		final L2Object target = activeChar.getTarget();
		L2PcInstance player = null;
		if (target instanceof L2PcInstance)
		{
			player = (L2PcInstance) target;
		}
		else
		{
			activeChar.sendPacket(SystemMessageId.INVALID_TARGET);
			return;
		}
		final NpcHtmlMessage adminReply = new NpcHtmlMessage();

		final String replyMSG = "<html><title>Teleport EventCharacter</title><body>The character you will teleport is " + player.getName() + ".<br>Co-ordinate x<edit var=\"char_cord_x\" width=110>Co-ordinate y<edit var=\"char_cord_y\" width=110>Co-ordinate z<edit var=\"char_cord_z\" width=110><button value=\"Teleport\" action=\"bypass -h admin_teleport_character $char_cord_x $char_cord_y $char_cord_z\" width=60 height=15 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"><button value=\"Teleport near you\" action=\"bypass -h admin_teleport_character " + activeChar.getX() + " " + activeChar.getY() + " " + activeChar.getZ() + "\" width=115 height=15 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"><center><button value=\"Back\" action=\"bypass -h admin_current_player\" width=40 height=15 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></center></body></html>";
		adminReply.setHtml(replyMSG);
		activeChar.sendPacket(adminReply);
	}

	private void teleportCharacter(L2PcInstance activeChar, String Cords)
	{
		final L2Object target = activeChar.getTarget();
		L2PcInstance player = null;
		if (target instanceof L2PcInstance)
		{
			player = (L2PcInstance) target;
		}
		else
		{
			activeChar.sendPacket(SystemMessageId.INVALID_TARGET);
			return;
		}

		if (player.getObjectId() == activeChar.getObjectId())
		{
			player.sendPacket(SystemMessageId.YOU_CANNOT_USE_THIS_ON_YOURSELF);
		}
		else
		{
			try
			{
				final StringTokenizer st = new StringTokenizer(Cords);
				final String x1 = st.nextToken();
				final int x = Integer.parseInt(x1);
				final String y1 = st.nextToken();
				final int y = Integer.parseInt(y1);
				final String z1 = st.nextToken();
				final int z = Integer.parseInt(z1);
				teleportCharacter(player, new Location(x, y, z), null);
			}
			catch (NoSuchElementException nsee)
			{
			}
		}
	}

	/**
	 * @param player
	 * @param loc
	 * @param activeChar
	 */
	private void teleportCharacter(L2PcInstance player, Location loc, L2PcInstance activeChar)
	{
		if (player != null)
		{
			// Check for jail
			if (player.isJailed())
			{
				activeChar.sendMessage("Sorry, player " + player.getName() + " is in Jail.");
			}
			else
			{
				// Set player to same instance as GM teleporting.
				if ((activeChar != null) && (activeChar.getInstanceId() >= 0))
				{
					player.setInstanceId(activeChar.getInstanceId());
					activeChar.sendMessage("You have recalled " + player.getName());
				}
				else
				{
					player.setInstanceId(0);
				}
				player.sendMessage("Admin is teleporting you.");
				player.getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
				player.teleToLocation(loc, true);
			}
		}
	}

	private void teleportToCharacter(L2PcInstance activeChar, L2Object target)
	{
		if (target == null)
		{
			activeChar.sendPacket(SystemMessageId.INVALID_TARGET);
			return;
		}

		L2PcInstance player = null;
		if (target instanceof L2PcInstance)
		{
			player = (L2PcInstance) target;
		}
		else
		{
			activeChar.sendPacket(SystemMessageId.INVALID_TARGET);
			return;
		}

		if (player.getObjectId() == activeChar.getObjectId())
		{
			player.sendPacket(SystemMessageId.YOU_CANNOT_USE_THIS_ON_YOURSELF);
		}
		else
		{
			// move to targets instance
			activeChar.setInstanceId(target.getInstanceId());

			final int x = player.getX();
			final int y = player.getY();
			final int z = player.getZ();

			activeChar.getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
			activeChar.teleToLocation(new Location(x, y, z), true);

			activeChar.sendMessage("You have teleported to character " + player.getName() + ".");
		}
	}

	private void changeCharacterPosition(L2PcInstance activeChar, String name)
	{
		final int x = activeChar.getX();
		final int y = activeChar.getY();
		final int z = activeChar.getZ();
		try (Connection con = DatabaseFactory.getInstance().getConnection())
		{
			final PreparedStatement statement = con.prepareStatement("UPDATE characters SET x=?, y=?, z=? WHERE char_name=?");
			statement.setInt(1, x);
			statement.setInt(2, y);
			statement.setInt(3, z);
			statement.setString(4, name);
			statement.execute();
			final int count = statement.getUpdateCount();
			statement.close();
			if (count == 0)
			{
				activeChar.sendMessage("EventCharacter not found or position unaltered.");
			}
			else
			{
				activeChar.sendMessage("Player's [" + name + "] position is now set to (" + x + "," + y + "," + z + ").");
			}
		}
		catch (SQLException se)
		{
			activeChar.sendMessage("SQLException while changing offline character's position");
		}
	}

	private void recallNPC(L2PcInstance activeChar)
	{
		final L2Object obj = activeChar.getTarget();
		if ((obj instanceof L2Npc) && !((L2Npc) obj).isMinion() && !(obj instanceof L2RaidBossInstance) && !(obj instanceof L2GrandBossInstance))
		{
			final L2Npc target = (L2Npc) obj;
			L2Spawn spawn = target.getSpawn();
			if (spawn == null)
			{
				activeChar.sendMessage("Incorrect monster spawn.");
				_log.warning("ERROR: NPC " + target.getObjectId() + " has a 'null' spawn.");
				return;
			}
			final int respawnTime = spawn.getRespawnDelay() / 1000;

			target.deleteMe();
			spawn.stopRespawn();
			SpawnTable.getInstance().deleteSpawn(spawn, true);

			try
			{
				spawn = new L2Spawn(target.getTemplate().getId());
				if (Config.SAVE_GMSPAWN_ON_CUSTOM)
				{
					spawn.setCustom(true);
				}
				spawn.setX(activeChar.getX());
				spawn.setY(activeChar.getY());
				spawn.setZ(activeChar.getZ());
				spawn.setAmount(1);
				spawn.setHeading(activeChar.getHeading());
				spawn.setRespawnDelay(respawnTime);
				if (activeChar.getInstanceId() >= 0)
				{
					spawn.setInstanceId(activeChar.getInstanceId());
				}
				else
				{
					spawn.setInstanceId(0);
				}
				SpawnTable.getInstance().addNewSpawn(spawn, true);
				spawn.init();

				activeChar.sendMessage("Created " + target.getTemplate().getName() + " on " + target.getObjectId() + ".");

				if (Config.DEBUG)
				{
					_log.fine("Spawn at X=" + spawn.getX() + " Y=" + spawn.getY() + " Z=" + spawn.getZ());
					_log.warning("GM: " + activeChar.getName() + "(" + activeChar.getObjectId() + ") moved NPC " + target.getObjectId());
				}
			}
			catch (Exception e)
			{
				activeChar.sendMessage("Target is not in game.");
			}

		}
		else if (obj instanceof L2RaidBossInstance)
		{
			final L2RaidBossInstance target = (L2RaidBossInstance) obj;
			final L2Spawn spawn = target.getSpawn();
			final double curHP = target.getCurrentHp();
			final double curMP = target.getCurrentMp();
			if (spawn == null)
			{
				activeChar.sendMessage("Incorrect raid spawn.");
				_log.warning("ERROR: NPC Id" + target.getId() + " has a 'null' spawn.");
				return;
			}
			RaidBossSpawnManager.getInstance().deleteSpawn(spawn, true);
			try
			{
				final L2Spawn spawnDat = new L2Spawn(target.getId());
				if (Config.SAVE_GMSPAWN_ON_CUSTOM)
				{
					spawn.setCustom(true);
				}
				spawnDat.setX(activeChar.getX());
				spawnDat.setY(activeChar.getY());
				spawnDat.setZ(activeChar.getZ());
				spawnDat.setAmount(1);
				spawnDat.setHeading(activeChar.getHeading());
				spawnDat.setRespawnMinDelay(43200);
				spawnDat.setRespawnMaxDelay(129600);

				RaidBossSpawnManager.getInstance().addNewSpawn(spawnDat, 0, curHP, curMP, true);
			}
			catch (Exception e)
			{
				activeChar.sendPacket(SystemMessageId.YOUR_TARGET_CANNOT_BE_FOUND);
			}
		}
		else
		{
			activeChar.sendPacket(SystemMessageId.INVALID_TARGET);
		}
	}

}
