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
import java.util.StringTokenizer;

import com.la2eden.gameserver.data.xml.impl.AdminData;
import com.la2eden.gameserver.enums.ChatType;
import com.la2eden.gameserver.handler.ITelnetHandler;
import com.la2eden.gameserver.model.L2World;
import com.la2eden.gameserver.model.actor.instance.L2PcInstance;
import com.la2eden.gameserver.network.serverpackets.CreatureSay;
import com.la2eden.gameserver.util.Broadcast;

/**
 * @author UnAfraid
 */
public class ChatsHandler implements ITelnetHandler
{
	private final String[] _commands =
	{
		"announce",
		"msg",
		"gmchat"
	};
	
	@Override
	public boolean useCommand(String command, PrintWriter _print, Socket _cSocket, int _uptime)
	{
		if (command.startsWith("announce"))
		{
			try
			{
				command = command.substring(9);
				Broadcast.toAllOnlinePlayers(command);
				_print.println("Announcement Sent!");
			}
			catch (StringIndexOutOfBoundsException e)
			{
				_print.println("Please Enter Some Text To Announce!");
			}
		}
		else if (command.startsWith("msg"))
		{
			try
			{
				final String val = command.substring(4);
				final StringTokenizer st = new StringTokenizer(val);
				final String name = st.nextToken();
				final String message = val.substring(name.length() + 1);
				final L2PcInstance reciever = L2World.getInstance().getPlayer(name);
				final CreatureSay cs = new CreatureSay(0, ChatType.WHISPER, "Telnet Priv", message);
				if (reciever != null)
				{
					reciever.sendPacket(cs);
					_print.println("Telnet Priv->" + name + ": " + message);
					_print.println("Message Sent!");
				}
				else
				{
					_print.println("Unable To Find Username: " + name);
				}
			}
			catch (StringIndexOutOfBoundsException e)
			{
				_print.println("Please Enter Some Text!");
			}
		}
		else if (command.startsWith("gmchat"))
		{
			try
			{
				command = command.substring(7);
				final CreatureSay cs = new CreatureSay(0, ChatType.ALLIANCE, "Telnet GM Broadcast from " + _cSocket.getInetAddress().getHostAddress(), command);
				AdminData.getInstance().broadcastToGMs(cs);
				_print.println("Your Message Has Been Sent To " + getOnlineGMS() + " GM(s).");
			}
			catch (StringIndexOutOfBoundsException e)
			{
				_print.println("Please Enter Some Text To Announce!");
			}
		}
		return false;
	}
	
	private int getOnlineGMS()
	{
		return AdminData.getInstance().getAllGms(true).size();
	}
	
	@Override
	public String[] getCommandList()
	{
		return _commands;
	}
}
