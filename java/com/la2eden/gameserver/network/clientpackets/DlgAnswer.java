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
package com.la2eden.gameserver.network.clientpackets;

import com.la2eden.Config;
import com.la2eden.gameserver.data.xml.impl.AdminData;
import com.la2eden.gameserver.enums.PlayerAction;
import com.la2eden.gameserver.handler.AdminCommandHandler;
import com.la2eden.gameserver.handler.IAdminCommandHandler;
import com.la2eden.gameserver.model.actor.instance.L2PcInstance;
import com.la2eden.gameserver.model.events.EventDispatcher;
import com.la2eden.gameserver.model.events.impl.character.player.OnPlayerDlgAnswer;
import com.la2eden.gameserver.model.events.returns.TerminateReturn;
import com.la2eden.gameserver.model.holders.DoorRequestHolder;
import com.la2eden.gameserver.model.holders.SummonRequestHolder;
import com.la2eden.gameserver.network.SystemMessageId;
import com.la2eden.gameserver.util.GMAudit;

/**
 * @author Dezmond_snz
 */
public final class DlgAnswer extends L2GameClientPacket
{
	private static final String _C__C6_DLGANSWER = "[C] C6 DlgAnswer";
	private int _messageId;
	private int _answer;
	private int _requesterId;
	
	@Override
	protected void readImpl()
	{
		_messageId = readD();
		_answer = readD();
		_requesterId = readD();
	}
	
	@Override
	public void runImpl()
	{
		final L2PcInstance activeChar = getClient().getActiveChar();
		if (activeChar == null)
		{
			return;
		}
		
		final TerminateReturn term = EventDispatcher.getInstance().notifyEvent(new OnPlayerDlgAnswer(activeChar, _messageId, _answer, _requesterId), activeChar, TerminateReturn.class);
		if ((term != null) && term.terminate())
		{
			return;
		}
		
		if (_messageId == SystemMessageId.S1.getId())
		{
			if (activeChar.removeAction(PlayerAction.USER_ENGAGE))
			{
				if (Config.L2JMOD_ALLOW_WEDDING)
				{
					activeChar.engageAnswer(_answer);
				}
			}
			else if (activeChar.removeAction(PlayerAction.ADMIN_COMMAND))
			{
				final String cmd = activeChar.getAdminConfirmCmd();
				activeChar.setAdminConfirmCmd(null);
				if (_answer == 0)
				{
					return;
				}
				final String command = cmd.split(" ")[0];
				final IAdminCommandHandler ach = AdminCommandHandler.getInstance().getHandler(command);
				if (AdminData.getInstance().hasAccess(command, activeChar.getAccessLevel()))
				{
					if (Config.GMAUDIT)
					{
						GMAudit.auditGMAction(activeChar.getName() + " [" + activeChar.getObjectId() + "]", cmd, (activeChar.getTarget() != null ? activeChar.getTarget().getName() : "no-target"));
					}
					ach.useAdminCommand(cmd, activeChar);
				}
			}
		}
		else if ((_messageId == SystemMessageId.C1_IS_MAKING_AN_ATTEMPT_TO_RESURRECT_YOU_IF_YOU_CHOOSE_THIS_PATH_S2_EXPERIENCE_POINTS_WILL_BE_RETURNED_TO_YOU_DO_YOU_WANT_TO_BE_RESURRECTED.getId()) || (_messageId == SystemMessageId.YOUR_CHARM_OF_COURAGE_IS_TRYING_TO_RESURRECT_YOU_WOULD_YOU_LIKE_TO_RESURRECT_NOW.getId()))
		{
			activeChar.reviveAnswer(_answer);
		}
		else if (_messageId == SystemMessageId.C1_WISHES_TO_SUMMON_YOU_FROM_S2_DO_YOU_ACCEPT.getId())
		{
			final SummonRequestHolder holder = activeChar.removeScript(SummonRequestHolder.class);
			if ((_answer == 1) && (holder != null) && (holder.getTarget().getObjectId() == _requesterId))
			{
				activeChar.teleToLocation(holder.getTarget().getLocation(), true);
			}
		}
		else if (_messageId == SystemMessageId.WOULD_YOU_LIKE_TO_OPEN_THE_GATE.getId())
		{
			final DoorRequestHolder holder = activeChar.removeScript(DoorRequestHolder.class);
			if ((holder != null) && (holder.getDoor() == activeChar.getTarget()) && (_answer == 1))
			{
				holder.getDoor().openMe();
			}
		}
		else if (_messageId == SystemMessageId.WOULD_YOU_LIKE_TO_CLOSE_THE_GATE.getId())
		{
			final DoorRequestHolder holder = activeChar.removeScript(DoorRequestHolder.class);
			if ((holder != null) && (holder.getDoor() == activeChar.getTarget()) && (_answer == 1))
			{
				holder.getDoor().closeMe();
			}
		}
	}
	
	@Override
	public String getType()
	{
		return _C__C6_DLGANSWER;
	}
}
