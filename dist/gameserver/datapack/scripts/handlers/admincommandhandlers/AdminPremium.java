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

import java.text.SimpleDateFormat;

import com.la2eden.Config;
import com.la2eden.gameserver.cache.HtmCache;
import com.la2eden.gameserver.handler.IAdminCommandHandler;
import com.la2eden.gameserver.instancemanager.PremiumManager;
import com.la2eden.gameserver.model.actor.instance.L2PcInstance;
import com.la2eden.gameserver.network.serverpackets.NpcHtmlMessage;

/**
 * @author Mobius
 */
public class AdminPremium implements IAdminCommandHandler
{
	private static final String[] ADMIN_COMMANDS =
	{
		"admin_premium_menu",
		"admin_premium_add1",
		"admin_premium_add2",
		"admin_premium_add3",
		"admin_premium_info",
		"admin_premium_remove"
	};
	
	@Override
	public boolean useAdminCommand(String command, L2PcInstance activeChar)
	{
		if (command.equals("admin_premium_menu"))
		{
			AdminHtml.showAdminHtml(activeChar, "premium_menu.htm");
		}
		else if (command.startsWith("admin_premium_add1"))
		{
			try
			{
				addPremiumStatus(activeChar, 1, command.substring(19));
			}
			catch (StringIndexOutOfBoundsException e)
			{
				activeChar.sendMessage("Please enter a valid account name.");
			}
		}
		else if (command.startsWith("admin_premium_add2"))
		{
			try
			{
				addPremiumStatus(activeChar, 2, command.substring(19));
			}
			catch (StringIndexOutOfBoundsException e)
			{
				activeChar.sendMessage("Please enter a valid account name.");
			}
		}
		else if (command.startsWith("admin_premium_add3"))
		{
			try
			{
				addPremiumStatus(activeChar, 3, command.substring(19));
			}
			catch (StringIndexOutOfBoundsException e)
			{
				activeChar.sendMessage("Please enter a valid account name.");
			}
		}
		else if (command.startsWith("admin_premium_info"))
		{
			try
			{
				viewPremiumInfo(activeChar, command.substring(19));
			}
			catch (StringIndexOutOfBoundsException e)
			{
				activeChar.sendMessage("Please enter a valid account name.");
			}
		}
		else if (command.startsWith("admin_premium_remove"))
		{
			try
			{
				removePremium(activeChar, command.substring(21));
			}
			catch (StringIndexOutOfBoundsException e)
			{
				activeChar.sendMessage("Please enter a valid account name.");
			}
		}
		
		final NpcHtmlMessage html = new NpcHtmlMessage(0, 0);
		html.setHtml(HtmCache.getInstance().getHtm(activeChar.getHtmlPrefix(), "./datapack/html/admin/premium_menu.htm"));
		activeChar.sendPacket(html);
		return true;
	}
	
	private void addPremiumStatus(L2PcInstance admin, int months, String accountName)
	{
		if (!Config.PREMIUM_SYSTEM_ENABLED)
		{
			admin.sendMessage("Premium system is disabled.");
			return;
		}
		
		// TODO: Add check if account exists XD
		PremiumManager.getInstance().updatePremiumData(months, accountName);
		admin.sendMessage("Account " + accountName + " will now have premium status until " + String.valueOf(new SimpleDateFormat("dd.MM.yyyy HH:mm").format(PremiumManager.getInstance().getPremiumEndDate(accountName))) + ".");
	}
	
	private void viewPremiumInfo(L2PcInstance admin, String accountName)
	{
		if (PremiumManager.getInstance().getPremiumEndDate(accountName) > 0)
		{
			admin.sendMessage("Account " + accountName + " has premium status until " + String.valueOf(new SimpleDateFormat("dd.MM.yyyy HH:mm").format(PremiumManager.getInstance().getPremiumEndDate(accountName))) + ".");
		}
		else
		{
			admin.sendMessage("Account " + accountName + " has no premium status.");
		}
	}
	
	private void removePremium(L2PcInstance admin, String accountName)
	{
		if (PremiumManager.getInstance().getPremiumEndDate(accountName) > 0)
		{
			PremiumManager.getInstance().removePremiumStatus(accountName);
			admin.sendMessage("Account " + accountName + " has no longer premium status.");
		}
		else
		{
			admin.sendMessage("Account " + accountName + " has no premium status.");
		}
	}
	
	@Override
	public String[] getAdminCommandList()
	{
		return ADMIN_COMMANDS;
	}
}