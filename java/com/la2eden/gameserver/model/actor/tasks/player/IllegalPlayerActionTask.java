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
package com.la2eden.gameserver.model.actor.tasks.player;

import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import com.la2eden.Config;
import com.la2eden.gameserver.data.xml.impl.AdminData;
import com.la2eden.gameserver.enums.IllegalActionPunishmentType;
import com.la2eden.gameserver.instancemanager.PunishmentManager;
import com.la2eden.gameserver.model.actor.instance.L2PcInstance;
import com.la2eden.gameserver.model.punishment.PunishmentAffect;
import com.la2eden.gameserver.model.punishment.PunishmentTask;
import com.la2eden.gameserver.model.punishment.PunishmentType;

/**
 * Task that handles illegal player actions.
 */
public final class IllegalPlayerActionTask implements Runnable
{
	private static final Logger _log = Logger.getLogger("audit");
	
	private final String _message;
	private final IllegalActionPunishmentType _punishment;
	private final L2PcInstance _actor;
	
	public IllegalPlayerActionTask(L2PcInstance actor, String message, IllegalActionPunishmentType punishment)
	{
		_message = message;
		_punishment = punishment;
		_actor = actor;
		
		switch (punishment)
		{
			case KICK:
			{
				_actor.sendMessage("You will be kicked for illegal action, GM informed.");
				break;
			}
			case KICKBAN:
			{
				if (!_actor.isGM())
				{
					_actor.setAccessLevel(-1);
					_actor.setAccountAccesslevel(-1);
				}
				_actor.sendMessage("You are banned for illegal action, GM informed.");
				break;
			}
			case JAIL:
			{
				_actor.sendMessage("Illegal action performed!");
				_actor.sendMessage("You will be teleported to GM Consultation Service area and jailed.");
				break;
			}
		}
	}
	
	@Override
	public void run()
	{
		final LogRecord record = new LogRecord(Level.INFO, "AUDIT:" + _message);
		record.setLoggerName("audit");
		//@formatter:off
		record.setParameters(new Object[] { _actor, _punishment	});
		//@formatter:on
		_log.log(record);
		
		AdminData.getInstance().broadcastMessageToGMs(_message);
		if (!_actor.isGM())
		{
			switch (_punishment)
			{
				case BROADCAST:
				{
					return;
				}
				case KICK:
				{
					_actor.logout(false);
					break;
				}
				case KICKBAN:
				{
					PunishmentManager.getInstance().startPunishment(new PunishmentTask(_actor.getObjectId(), PunishmentAffect.CHARACTER, PunishmentType.BAN, System.currentTimeMillis() + (Config.DEFAULT_PUNISH_PARAM * 1000), _message, getClass().getSimpleName()));
					break;
				}
				case JAIL:
				{
					PunishmentManager.getInstance().startPunishment(new PunishmentTask(_actor.getObjectId(), PunishmentAffect.CHARACTER, PunishmentType.JAIL, System.currentTimeMillis() + (Config.DEFAULT_PUNISH_PARAM * 1000), _message, getClass().getSimpleName()));
					break;
				}
			}
		}
	}
}
