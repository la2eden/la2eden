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
package handlers.telnethandlers;

import java.io.PrintWriter;
import java.net.Socket;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;

import com.la2eden.Config;
import com.la2eden.gameserver.data.sql.impl.CharNameTable;
import com.la2eden.gameserver.handler.ITelnetHandler;
import com.la2eden.gameserver.instancemanager.PunishmentManager;
import com.la2eden.gameserver.model.L2World;
import com.la2eden.gameserver.model.actor.instance.L2PcInstance;
import com.la2eden.gameserver.model.itemcontainer.Inventory;
import com.la2eden.gameserver.model.items.instance.L2ItemInstance;
import com.la2eden.gameserver.model.punishment.PunishmentAffect;
import com.la2eden.gameserver.model.punishment.PunishmentTask;
import com.la2eden.gameserver.model.punishment.PunishmentType;
import com.la2eden.gameserver.network.SystemMessageId;
import com.la2eden.gameserver.network.serverpackets.CharInfo;
import com.la2eden.gameserver.network.serverpackets.ExBrExtraUserInfo;
import com.la2eden.gameserver.network.serverpackets.InventoryUpdate;
import com.la2eden.gameserver.network.serverpackets.SystemMessage;
import com.la2eden.gameserver.network.serverpackets.UserInfo;
import com.la2eden.gameserver.util.GMAudit;
import com.la2eden.gameserver.util.Util;

/**
 * @author UnAfraid
 */
public class PlayerHandler implements ITelnetHandler
{
	private final String[] _commands =
	{
		"kick",
		"give",
		"enchant",
		"jail",
		"unjail"
	};

	@Override
	public boolean useCommand(String command, PrintWriter _print, Socket _cSocket, int _uptime)
	{
		if (command.startsWith("kick"))
		{
			try
			{
				command = command.substring(5);
				final L2PcInstance player = L2World.getInstance().getPlayer(command);
				if (player != null)
				{
					player.sendMessage("You are kicked by gm");
					player.logout();
					_print.println("Player kicked");
				}
			}
			catch (StringIndexOutOfBoundsException e)
			{
				_print.println("Please enter player name to kick");
			}
		}
		else if (command.startsWith("give"))
		{
			final StringTokenizer st = new StringTokenizer(command.substring(5));

			try
			{
				final L2PcInstance player = L2World.getInstance().getPlayer(st.nextToken());
				final int itemId = Integer.parseInt(st.nextToken());
				final int amount = Integer.parseInt(st.nextToken());

				if (player != null)
				{
					final L2ItemInstance item = player.getInventory().addItem("Status-Give", itemId, amount, null, null);
					final InventoryUpdate iu = new InventoryUpdate();
					iu.addItem(item);
					player.sendPacket(iu);
					final SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.YOU_HAVE_OBTAINED_S2_S1);
					sm.addItemName(itemId);
					sm.addLong(amount);
					player.sendPacket(sm);
					_print.println("ok");
					GMAudit.auditGMAction("Telnet Admin", "Give Item", player.getName(), "item: " + itemId + " amount: " + amount);
				}
				else
				{
					_print.println("Player not found");
				}
			}
			catch (Exception e)
			{

			}
		}
		else if (command.startsWith("enchant"))
		{
			final StringTokenizer st = new StringTokenizer(command.substring(8), " ");
			int enchant = 0, itemType = 0;

			try
			{
				final L2PcInstance player = L2World.getInstance().getPlayer(st.nextToken());
				itemType = Integer.parseInt(st.nextToken());
				enchant = Integer.parseInt(st.nextToken());

				switch (itemType)
				{
					case 1:
						itemType = Inventory.PAPERDOLL_HEAD;
						break;
					case 2:
						itemType = Inventory.PAPERDOLL_CHEST;
						break;
					case 3:
						itemType = Inventory.PAPERDOLL_GLOVES;
						break;
					case 4:
						itemType = Inventory.PAPERDOLL_FEET;
						break;
					case 5:
						itemType = Inventory.PAPERDOLL_LEGS;
						break;
					case 6:
						itemType = Inventory.PAPERDOLL_RHAND;
						break;
					case 7:
						itemType = Inventory.PAPERDOLL_LHAND;
						break;
					case 8:
						itemType = Inventory.PAPERDOLL_LEAR;
						break;
					case 9:
						itemType = Inventory.PAPERDOLL_REAR;
						break;
					case 10:
						itemType = Inventory.PAPERDOLL_LFINGER;
						break;
					case 11:
						itemType = Inventory.PAPERDOLL_RFINGER;
						break;
					case 12:
						itemType = Inventory.PAPERDOLL_NECK;
						break;
					case 13:
						itemType = Inventory.PAPERDOLL_UNDER;
						break;
					case 14:
						itemType = Inventory.PAPERDOLL_CLOAK;
						break;
					case 15:
						itemType = Inventory.PAPERDOLL_BELT;
						break;
					default:
						itemType = 0;
				}

				if (enchant > 65535)
				{
					enchant = 65535;
				}
				else if (enchant < 0)
				{
					enchant = 0;
				}

				boolean success = false;

				if ((player != null) && (itemType > 0))
				{
					success = setEnchant(player, enchant, itemType);
					if (success)
					{
						_print.println("Item enchanted successfully.");
					}
				}
				else if (!success)
				{
					_print.println("Item failed to enchant.");
				}
			}
			catch (Exception e)
			{

			}
		}
		else if (command.startsWith("jail"))
		{
			final StringTokenizer st = new StringTokenizer(command.substring(5));
			try
			{
				final String name = st.nextToken();
				final int charId = CharNameTable.getInstance().getIdByName(name);
				int delay = 0;
				String reason = "";
				if (st.hasMoreTokens())
				{
					final String token = st.nextToken();
					if (Util.isDigit(token))
					{
						delay = Integer.parseInt(token);
					}
					while (st.hasMoreTokens())
					{
						reason += st.nextToken() + " ";
					}
					if (!reason.isEmpty())
					{
						reason = reason.substring(0, reason.length() - 1);
					}
				}

				if (charId > 0)
				{
					final long expirationTime = delay > 0 ? System.currentTimeMillis() + (delay * 60 * 1000) : -1;
					PunishmentManager.getInstance().startPunishment(new PunishmentTask(charId, PunishmentAffect.CHARACTER, PunishmentType.JAIL, expirationTime, reason, "Telnet Admin: " + _cSocket.getInetAddress().getHostAddress()));
					_print.println("EventCharacter " + name + " jailed for " + (delay > 0 ? delay + " minutes." : "ever!"));
				}
				else
				{
					_print.println("EventCharacter with name: " + name + " was not found!");
				}
			}
			catch (NoSuchElementException nsee)
			{
				_print.println("Specify a character name.");
			}
			catch (Exception e)
			{
				if (Config.DEBUG)
				{
					e.printStackTrace();
				}
			}
		}
		else if (command.startsWith("unjail"))
		{
			final StringTokenizer st = new StringTokenizer(command.substring(7));
			try
			{
				final String name = st.nextToken();
				final int charId = CharNameTable.getInstance().getIdByName(name);

				if (charId > 0)
				{
					PunishmentManager.getInstance().stopPunishment(charId, PunishmentAffect.CHARACTER, PunishmentType.JAIL);
					_print.println("EventCharacter " + name + " have been unjailed");
				}
				else
				{
					_print.println("EventCharacter with name: " + name + " was not found!");
				}
			}
			catch (NoSuchElementException nsee)
			{
				_print.println("Specify a character name.");
			}
			catch (Exception e)
			{
				if (Config.DEBUG)
				{
					e.printStackTrace();
				}
			}
		}
		return false;
	}

	private boolean setEnchant(L2PcInstance activeChar, int ench, int armorType)
	{
		// now we need to find the equipped weapon of the targeted character...
		int curEnchant = 0; // display purposes only
		L2ItemInstance itemInstance = null;

		// only attempt to enchant if there is a weapon equipped
		L2ItemInstance parmorInstance = activeChar.getInventory().getPaperdollItem(armorType);
		if ((parmorInstance != null) && (parmorInstance.getLocationSlot() == armorType))
		{
			itemInstance = parmorInstance;
		}
		else
		{
			// for bows/crossbows and double handed weapons
			parmorInstance = activeChar.getInventory().getPaperdollItem(Inventory.PAPERDOLL_RHAND);
			if ((parmorInstance != null) && (parmorInstance.getLocationSlot() == Inventory.PAPERDOLL_RHAND))
			{
				itemInstance = parmorInstance;
			}
		}

		if (itemInstance != null)
		{
			curEnchant = itemInstance.getEnchantLevel();

			// set enchant value
			activeChar.getInventory().unEquipItemInSlot(armorType);
			itemInstance.setEnchantLevel(ench);
			activeChar.getInventory().equipItem(itemInstance);

			// send packets
			final InventoryUpdate iu = new InventoryUpdate();
			iu.addModifiedItem(itemInstance);
			activeChar.sendPacket(iu);
			activeChar.broadcastPacket(new CharInfo(activeChar));
			activeChar.sendPacket(new UserInfo(activeChar));
			activeChar.broadcastPacket(new ExBrExtraUserInfo(activeChar));

			// informations
			activeChar.sendMessage("Changed enchantment of " + activeChar.getName() + "'s " + itemInstance.getItem().getName() + " from " + curEnchant + " to " + ench + ".");
			activeChar.sendMessage("Admin has changed the enchantment of your " + itemInstance.getItem().getName() + " from " + curEnchant + " to " + ench + ".");

			// log
			GMAudit.auditGMAction("TelnetAdministrator", "enchant", activeChar.getName(), itemInstance.getItem().getName() + "(" + itemInstance.getObjectId() + ") from " + curEnchant + " to " + ench);
			return true;
		}
		return false;
	}

	@Override
	public String[] getCommandList()
	{
		return _commands;
	}
}
