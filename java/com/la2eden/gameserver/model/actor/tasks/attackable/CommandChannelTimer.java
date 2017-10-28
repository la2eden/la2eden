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
package com.la2eden.gameserver.model.actor.tasks.attackable;

import com.la2eden.Config;
import com.la2eden.gameserver.ThreadPoolManager;
import com.la2eden.gameserver.model.actor.L2Attackable;

/**
 * @author xban1x
 */
public final class CommandChannelTimer implements Runnable
{
	private final L2Attackable _attackable;
	
	public CommandChannelTimer(L2Attackable attackable)
	{
		_attackable = attackable;
	}
	
	@Override
	public void run()
	{
		if (_attackable == null)
		{
			return;
		}
		
		if ((System.currentTimeMillis() - _attackable.getCommandChannelLastAttack()) > Config.LOOT_RAIDS_PRIVILEGE_INTERVAL)
		{
			_attackable.setCommandChannelTimer(null);
			_attackable.setFirstCommandChannelAttacked(null);
			_attackable.setCommandChannelLastAttack(0);
		}
		else
		{
			ThreadPoolManager.getInstance().scheduleGeneral(this, 10000); // 10sec
		}
	}
}
