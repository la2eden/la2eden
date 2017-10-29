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
package handlers.chathandlers;

import java.util.StringTokenizer;
import java.util.logging.Logger;

import com.la2eden.Config;
import com.la2eden.gameserver.enums.ChatType;
import com.la2eden.gameserver.handler.IChatHandler;
import com.la2eden.gameserver.handler.IVoicedCommandHandler;
import com.la2eden.gameserver.handler.VoicedCommandHandler;
import com.la2eden.gameserver.model.BlockList;
import com.la2eden.gameserver.model.PcCondOverride;
import com.la2eden.gameserver.model.actor.instance.L2PcInstance;
import com.la2eden.gameserver.network.SystemMessageId;
import com.la2eden.gameserver.network.serverpackets.CreatureSay;

/**
 * General Chat Handler.
 * @author durgus
 */
public final class ChatGeneral implements IChatHandler
{
	private static Logger _log = Logger.getLogger(ChatGeneral.class.getName());
	
	private static final ChatType[] CHAT_TYPES =
	{
		ChatType.GENERAL,
	};
	
	@Override
	public void handleChat(ChatType type, L2PcInstance activeChar, String params, String text)
	{
		boolean vcd_used = false;
		if (text.startsWith("."))
		{
			final StringTokenizer st = new StringTokenizer(text);
			final IVoicedCommandHandler vch;
			String command = "";
			
			if (st.countTokens() > 1)
			{
				command = st.nextToken().substring(1);
				params = text.substring(command.length() + 2);
			}
			else
			{
				command = text.substring(1);
				if (Config.DEBUG)
				{
					_log.info("Command: " + command);
				}
			}
			vch = VoicedCommandHandler.getInstance().getHandler(command);
			if (vch != null)
			{
				vch.useVoicedCommand(command, activeChar, params);
				vcd_used = true;
			}
			else
			{
				if (Config.DEBUG)
				{
					_log.warning("No handler registered for bypass '" + command + "'");
				}
				vcd_used = false;
			}
		}
		
		if (!vcd_used)
		{
			if (activeChar.isChatBanned() && Config.BAN_CHAT_CHANNELS.contains(type))
			{
				activeChar.sendPacket(SystemMessageId.CHATTING_IS_CURRENTLY_PROHIBITED_IF_YOU_TRY_TO_CHAT_BEFORE_THE_PROHIBITION_IS_REMOVED_THE_PROHIBITION_TIME_WILL_INCREASE_EVEN_FURTHER);
				return;
			}
			
			if ((activeChar.getLevel() < Config.MINIMUM_CHAT_LEVEL) && !activeChar.canOverrideCond(PcCondOverride.CHAT_CONDITIONS))
			{
				activeChar.sendMessage("Players can use general chat after Lv. " + Config.MINIMUM_CHAT_LEVEL + ".");
				return;
			}
			
			final CreatureSay cs = new CreatureSay(activeChar.getObjectId(), type, activeChar.getAppearance().getVisibleName(), text);
			for (L2PcInstance player : activeChar.getKnownList().getKnownPlayers().values())
			{
				if ((player != null) && activeChar.isInsideRadius(player, 1250, false, true) && !BlockList.isBlocked(player, activeChar))
				{
					player.sendPacket(cs);
				}
			}
			
			activeChar.sendPacket(cs);
		}
	}
	
	@Override
	public ChatType[] getChatTypeList()
	{
		return CHAT_TYPES;
	}
}