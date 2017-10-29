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

import java.nio.BufferUnderflowException;

import com.la2eden.Config;
import com.la2eden.gameserver.ai.CtrlIntention;
import com.la2eden.gameserver.model.Location;
import com.la2eden.gameserver.model.actor.instance.L2PcInstance;
import com.la2eden.gameserver.model.events.EventDispatcher;
import com.la2eden.gameserver.model.events.impl.character.player.OnPlayerMoveRequest;
import com.la2eden.gameserver.model.events.returns.TerminateReturn;
import com.la2eden.gameserver.network.SystemMessageId;
import com.la2eden.gameserver.network.serverpackets.ActionFailed;
import com.la2eden.gameserver.network.serverpackets.StopMove;
import com.la2eden.gameserver.util.Util;

/**
 * This class ...
 * @version $Revision: 1.11.2.4.2.4 $ $Date: 2005/03/27 15:29:30 $
 */
public class MoveBackwardToLocation extends L2GameClientPacket
{
	private static final String _C__0F_MOVEBACKWARDTOLOC = "[C] 0F MoveBackwardToLoc";
	
	// cdddddd
	private int _targetX;
	private int _targetY;
	private int _targetZ;
	private int _originX;
	private int _originY;
	private int _originZ;
	
	private int _moveMovement;
	
	@Override
	protected void readImpl()
	{
		_targetX = readD();
		_targetY = readD();
		_targetZ = readD();
		_originX = readD();
		_originY = readD();
		_originZ = readD();
		try
		{
			_moveMovement = readD(); // is 0 if cursor keys are used 1 if mouse is used
		}
		catch (BufferUnderflowException e)
		{
			if (Config.L2WALKER_PROTECTION)
			{
				final L2PcInstance activeChar = getClient().getActiveChar();
				Util.handleIllegalPlayerAction(activeChar, "Player " + activeChar.getName() + " is trying to use L2Walker and got kicked.", Config.DEFAULT_PUNISH);
			}
		}
	}
	
	@Override
	protected void runImpl()
	{
		final L2PcInstance activeChar = getClient().getActiveChar();
		if (activeChar == null)
		{
			return;
		}
		
		if ((Config.PLAYER_MOVEMENT_BLOCK_TIME > 0) && !activeChar.isGM() && (activeChar.getNotMoveUntil() > System.currentTimeMillis()))
		{
			activeChar.sendPacket(SystemMessageId.YOU_CANNOT_MOVE_WHILE_SPEAKING_TO_AN_NPC_ONE_MOMENT_PLEASE);
			activeChar.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		if ((_targetX == _originX) && (_targetY == _originY) && (_targetZ == _originZ))
		{
			activeChar.sendPacket(new StopMove(activeChar));
			return;
		}
		
		// Correcting targetZ from floor level to head level (?)
		// Client is giving floor level as targetZ but that floor level doesn't
		// match our current geodata and teleport coords as good as head level!
		// L2J uses floor, not head level as char coordinates. This is some
		// sort of incompatibility fix.
		// Validate position packets sends head level.
		_targetZ += activeChar.getTemplate().getCollisionHeight();
		
		if (_moveMovement == 1)
		{
			final TerminateReturn terminate = EventDispatcher.getInstance().notifyEvent(new OnPlayerMoveRequest(activeChar, new Location(_targetX, _targetY, _targetZ)), activeChar, TerminateReturn.class);
			if ((terminate != null) && terminate.terminate())
			{
				activeChar.sendPacket(ActionFailed.STATIC_PACKET);
				return;
			}
		}
		
		if (activeChar.getTeleMode() > 0)
		{
			if (activeChar.getTeleMode() == 1)
			{
				activeChar.setTeleMode(0);
			}
			activeChar.sendPacket(ActionFailed.STATIC_PACKET);
			activeChar.teleToLocation(new Location(_targetX, _targetY, _targetZ));
			return;
		}
		
		final double dx = _targetX - activeChar.getX();
		final double dy = _targetY - activeChar.getY();
		// Can't move if character is confused, or trying to move a huge distance
		if (activeChar.isOutOfControl() || (((dx * dx) + (dy * dy)) > 98010000)) // 9900*9900
		{
			activeChar.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		activeChar.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, new Location(_targetX, _targetY, _targetZ));
	}
	
	@Override
	public String getType()
	{
		return _C__0F_MOVEBACKWARDTOLOC;
	}
}
