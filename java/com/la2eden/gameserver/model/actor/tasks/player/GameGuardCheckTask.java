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
package com.la2eden.gameserver.model.actor.tasks.player;

import java.util.logging.Logger;

import com.la2eden.gameserver.data.xml.impl.AdminData;
import com.la2eden.gameserver.model.actor.instance.L2PcInstance;
import com.la2eden.gameserver.network.L2GameClient;
import com.la2eden.gameserver.network.serverpackets.LeaveWorld;

/**
 * Task dedicated to verify client's game guard.
 * @author UnAfraid
 */
public class GameGuardCheckTask implements Runnable
{
	private static final Logger _log = Logger.getLogger(GameGuardCheckTask.class.getName());
	
	private final L2PcInstance _player;
	
	public GameGuardCheckTask(L2PcInstance player)
	{
		_player = player;
	}
	
	@Override
	public void run()
	{
		if (_player == null)
		{
			return;
		}
		
		final L2GameClient client = _player.getClient();
		if ((client == null) || client.isAuthedGG() || !_player.isOnline())
		{
			return;
		}
		
		AdminData.getInstance().broadcastMessageToGMs("Client " + client + " failed to reply GameGuard query and is being kicked!");
		_log.info("Client " + client + " failed to reply GameGuard query and is being kicked!");
		client.close(LeaveWorld.STATIC_PACKET);
	}
}
