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
package handlers.chathandlers;

import com.la2eden.Config;
import com.la2eden.gameserver.enums.ChatType;
import com.la2eden.gameserver.handler.IChatHandler;
import com.la2eden.gameserver.instancemanager.PetitionManager;
import com.la2eden.gameserver.model.actor.instance.L2PcInstance;
import com.la2eden.gameserver.network.SystemMessageId;

/**
 * Petition chat handler.
 * @author durgus
 */
public final class ChatPetition implements IChatHandler
{
	private static final ChatType[] CHAT_TYPES =
	{
		ChatType.PETITION_PLAYER,
		ChatType.PETITION_GM,
	};
	
	@Override
	public void handleChat(ChatType type, L2PcInstance activeChar, String target, String text)
	{
		if (activeChar.isChatBanned() && Config.BAN_CHAT_CHANNELS.contains(type))
		{
			activeChar.sendPacket(SystemMessageId.CHATTING_IS_CURRENTLY_PROHIBITED_IF_YOU_TRY_TO_CHAT_BEFORE_THE_PROHIBITION_IS_REMOVED_THE_PROHIBITION_TIME_WILL_INCREASE_EVEN_FURTHER);
			return;
		}
		
		if (!PetitionManager.getInstance().isPlayerInConsultation(activeChar))
		{
			activeChar.sendPacket(SystemMessageId.YOU_ARE_CURRENTLY_NOT_IN_A_PETITION_CHAT);
			return;
		}
		PetitionManager.getInstance().sendActivePetitionMessage(activeChar, text);
	}
	
	@Override
	public ChatType[] getChatTypeList()
	{
		return CHAT_TYPES;
	}
}