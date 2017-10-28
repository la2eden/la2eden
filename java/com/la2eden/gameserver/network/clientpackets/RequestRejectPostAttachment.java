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
package com.la2eden.gameserver.network.clientpackets;

import com.la2eden.Config;
import com.la2eden.gameserver.instancemanager.MailManager;
import com.la2eden.gameserver.model.L2World;
import com.la2eden.gameserver.model.actor.instance.L2PcInstance;
import com.la2eden.gameserver.model.entity.Message;
import com.la2eden.gameserver.model.zone.ZoneId;
import com.la2eden.gameserver.network.SystemMessageId;
import com.la2eden.gameserver.network.serverpackets.ExChangePostState;
import com.la2eden.gameserver.network.serverpackets.SystemMessage;
import com.la2eden.gameserver.util.Util;

/**
 * @author Migi, DS
 */
public final class RequestRejectPostAttachment extends L2GameClientPacket
{
	private static final String _C__D0_6B_REQUESTREJECTPOSTATTACHMENT = "[C] D0:6B RequestRejectPostAttachment";
	
	private int _msgId;
	
	@Override
	protected void readImpl()
	{
		_msgId = readD();
	}
	
	@Override
	public void runImpl()
	{
		if (!Config.ALLOW_MAIL || !Config.ALLOW_ATTACHMENTS)
		{
			return;
		}
		
		final L2PcInstance activeChar = getClient().getActiveChar();
		if (activeChar == null)
		{
			return;
		}
		
		if (!getClient().getFloodProtectors().getTransaction().tryPerformAction("rejectattach"))
		{
			return;
		}
		
		if (!activeChar.isInsideZone(ZoneId.PEACE))
		{
			activeChar.sendPacket(SystemMessageId.YOU_CANNOT_RECEIVE_OR_SEND_MAIL_WITH_ATTACHED_ITEMS_IN_NON_PEACE_ZONE_REGIONS);
			return;
		}
		
		final Message msg = MailManager.getInstance().getMessage(_msgId);
		if (msg == null)
		{
			return;
		}
		
		if (msg.getReceiverId() != activeChar.getObjectId())
		{
			Util.handleIllegalPlayerAction(activeChar, "Player " + activeChar.getName() + " tried to reject not own attachment!", Config.DEFAULT_PUNISH);
			return;
		}
		
		if (!msg.hasAttachments() || (msg.getSendBySystem() != 0))
		{
			return;
		}
		
		MailManager.getInstance().sendMessage(new Message(msg));
		
		activeChar.sendPacket(SystemMessageId.MAIL_SUCCESSFULLY_RETURNED);
		activeChar.sendPacket(new ExChangePostState(true, _msgId, Message.REJECTED));
		
		final L2PcInstance sender = L2World.getInstance().getPlayer(msg.getSenderId());
		if (sender != null)
		{
			final SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.S1_RETURNED_THE_MAIL);
			sm.addCharName(activeChar);
			sender.sendPacket(sm);
		}
	}
	
	@Override
	public String getType()
	{
		return _C__D0_6B_REQUESTREJECTPOSTATTACHMENT;
	}
}
