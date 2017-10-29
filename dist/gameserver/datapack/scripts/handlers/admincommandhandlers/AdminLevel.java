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

import java.util.StringTokenizer;

import com.la2eden.gameserver.data.xml.impl.ExperienceData;
import com.la2eden.gameserver.handler.IAdminCommandHandler;
import com.la2eden.gameserver.model.L2Object;
import com.la2eden.gameserver.model.actor.L2Playable;
import com.la2eden.gameserver.model.actor.instance.L2PcInstance;
import com.la2eden.gameserver.network.SystemMessageId;

public class AdminLevel implements IAdminCommandHandler
{
	private static final String[] ADMIN_COMMANDS =
	{
		"admin_add_level",
		"admin_set_level"
	};
	
	@Override
	public boolean useAdminCommand(String command, L2PcInstance activeChar)
	{
		final L2Object targetChar = activeChar.getTarget();
		final StringTokenizer st = new StringTokenizer(command, " ");
		final String actualCommand = st.nextToken(); // Get actual command
		
		String val = "";
		if (st.countTokens() >= 1)
		{
			val = st.nextToken();
		}
		
		if (actualCommand.equalsIgnoreCase("admin_add_level"))
		{
			try
			{
				if (targetChar instanceof L2Playable)
				{
					((L2Playable) targetChar).getStat().addLevel(Byte.parseByte(val));
				}
			}
			catch (NumberFormatException e)
			{
				activeChar.sendMessage("Wrong Number Format");
			}
		}
		else if (actualCommand.equalsIgnoreCase("admin_set_level"))
		{
			try
			{
				if (!(targetChar instanceof L2PcInstance))
				{
					activeChar.sendPacket(SystemMessageId.THAT_IS_AN_INCORRECT_TARGET); // incorrect target!
					return false;
				}
				final L2PcInstance targetPlayer = (L2PcInstance) targetChar;
				
				final byte lvl = Byte.parseByte(val);
				if ((lvl >= 1) && (lvl <= ExperienceData.getInstance().getMaxLevel()))
				{
					final long pXp = targetPlayer.getExp();
					final long tXp = ExperienceData.getInstance().getExpForLevel(lvl);
					
					if (pXp > tXp)
					{
						targetPlayer.removeExpAndSp(pXp - tXp, 0);
					}
					else if (pXp < tXp)
					{
						targetPlayer.addExpAndSp(tXp - pXp, 0);
					}
				}
				else
				{
					activeChar.sendMessage("You must specify level between 1 and " + ExperienceData.getInstance().getMaxLevel() + ".");
					return false;
				}
			}
			catch (NumberFormatException e)
			{
				activeChar.sendMessage("You must specify level between 1 and " + ExperienceData.getInstance().getMaxLevel() + ".");
				return false;
			}
		}
		return true;
	}
	
	@Override
	public String[] getAdminCommandList()
	{
		return ADMIN_COMMANDS;
	}
}
