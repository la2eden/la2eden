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
package com.la2eden.gameserver.taskmanager;

import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.la2eden.gameserver.ThreadPoolManager;
import com.la2eden.gameserver.model.actor.L2Character;
import com.la2eden.gameserver.model.actor.instance.L2CubicInstance;
import com.la2eden.gameserver.network.serverpackets.AutoAttackStop;

/**
 * Attack stance task manager.
 * @author Luca Baldi, Zoey76
 */
public class AttackStanceTaskManager
{
	protected static final Logger _log = Logger.getLogger(AttackStanceTaskManager.class.getName());
	
	protected static final Map<L2Character, Long> _attackStanceTasks = new ConcurrentHashMap<>();
	
	/**
	 * Instantiates a new attack stance task manager.
	 */
	protected AttackStanceTaskManager()
	{
		ThreadPoolManager.getInstance().scheduleAiAtFixedRate(new FightModeScheduler(), 0, 1000);
	}
	
	/**
	 * Adds the attack stance task.
	 * @param actor the actor
	 */
	public void addAttackStanceTask(L2Character actor)
	{
		if (actor == null)
		{
			return;
		}
		if (actor.isPlayable())
		{
			for (L2CubicInstance cubic : actor.getActingPlayer().getCubics().values())
			{
				if (cubic.getId() != L2CubicInstance.LIFE_CUBIC)
				{
					cubic.doAction();
				}
			}
		}
		_attackStanceTasks.put(actor, System.currentTimeMillis());
	}
	
	/**
	 * Removes the attack stance task.
	 * @param actor the actor
	 */
	public void removeAttackStanceTask(L2Character actor)
	{
		if (actor == null)
		{
			return;
		}
		if (actor.isSummon())
		{
			actor = actor.getActingPlayer();
		}
		_attackStanceTasks.remove(actor);
	}
	
	/**
	 * Checks for attack stance task.<br>
	 * @param actor the actor
	 * @return {@code true} if the character has an attack stance task, {@code false} otherwise
	 */
	public boolean hasAttackStanceTask(L2Character actor)
	{
		if (actor == null)
		{
			return false;
		}
		if (actor.isSummon())
		{
			actor = actor.getActingPlayer();
		}
		return _attackStanceTasks.containsKey(actor);
	}
	
	protected class FightModeScheduler implements Runnable
	{
		@Override
		public void run()
		{
			final long current = System.currentTimeMillis();
			try
			{
				L2Character actor;
				for (Entry<L2Character, Long> e : _attackStanceTasks.entrySet())
				{
					if ((current - e.getValue()) > 15000)
					{
						actor = e.getKey();
						if (actor != null)
						{
							actor.broadcastPacket(new AutoAttackStop(actor.getObjectId()));
							actor.getAI().setAutoAttacking(false);
							if (actor.isPlayer() && actor.hasSummon())
							{
								actor.getSummon().broadcastPacket(new AutoAttackStop(actor.getSummon().getObjectId()));
							}
						}
						_attackStanceTasks.remove(e.getKey());
					}
				}
			}
			catch (Exception e)
			{
				// Unless caught here, players remain in attack positions.
				_log.log(Level.WARNING, "Error in FightModeScheduler: " + e.getMessage(), e);
			}
		}
	}
	
	/**
	 * Gets the single instance of AttackStanceTaskManager.
	 * @return single instance of AttackStanceTaskManager
	 */
	public static AttackStanceTaskManager getInstance()
	{
		return SingletonHolder._instance;
	}
	
	private static class SingletonHolder
	{
		protected static final AttackStanceTaskManager _instance = new AttackStanceTaskManager();
	}
}
