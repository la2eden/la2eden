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
package com.la2eden.gameserver.model.entity;

import java.util.Calendar;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import com.la2eden.Config;
import com.la2eden.gameserver.ThreadPoolManager;
import com.la2eden.gameserver.model.actor.instance.L2PcInstance;
import com.la2eden.gameserver.model.events.EventType;
import com.la2eden.gameserver.model.events.annotations.RegisterEvent;
import com.la2eden.gameserver.model.events.impl.character.player.OnPlayerLogin;
import com.la2eden.gameserver.model.events.impl.character.player.OnPlayerLogout;
import com.la2eden.gameserver.model.events.listeners.ConsumerEventListener;
import com.la2eden.gameserver.model.interfaces.IUniqueId;
import com.la2eden.gameserver.model.skills.AbnormalVisualEffect;
import com.la2eden.gameserver.network.SystemMessageId;
import com.la2eden.gameserver.network.serverpackets.ExNevitAdventEffect;
import com.la2eden.gameserver.network.serverpackets.ExNevitAdventPointInfoPacket;
import com.la2eden.gameserver.network.serverpackets.ExNevitAdventTimeChange;

/**
 * Nevit's Blessing handler.
 * @author Janiko
 */
public class NevitSystem implements IUniqueId
{
	public final L2PcInstance _player;
	
	private volatile ScheduledFuture<?> _adventTask;
	private volatile ScheduledFuture<?> _nevitEffectTask;
	
	public NevitSystem(L2PcInstance player)
	{
		_player = player;
		
		player.addListener(new ConsumerEventListener(player, EventType.ON_PLAYER_LOGIN, (OnPlayerLogin event) -> onPlayerLogin(event), this));
		player.addListener(new ConsumerEventListener(player, EventType.ON_PLAYER_LOGOUT, (OnPlayerLogout event) -> OnPlayerLogout(event), this));
	}
	
	@RegisterEvent(EventType.ON_PLAYER_LOGIN)
	private void onPlayerLogin(OnPlayerLogin event)
	{
		final Calendar cal = Calendar.getInstance();
		cal.set(Calendar.HOUR_OF_DAY, 6);
		cal.set(Calendar.MINUTE, 30);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		
		// Reset Nevit's Blessing
		if ((_player.getLastAccess() < (cal.getTimeInMillis() / 1000L)) && (System.currentTimeMillis() > cal.getTimeInMillis()))
		{
			_player.getVariables().set("hunting_time", 0);
		}
		
		// Send Packets
		_player.sendPacket(new ExNevitAdventPointInfoPacket(getAdventPoints()));
		_player.sendPacket(new ExNevitAdventTimeChange(getAdventTime(), true));
		
		startNevitEffect(_player.getVariables().getInt("nevit_b", 0));
		
		// Set percent
		final int percent = calcPercent(_player.getVariables().getInt("hunting_points", 0));
		
		if ((percent >= 45) && (percent < 50))
		{
			_player.sendPacket(SystemMessageId.YOUR_BIRTHDAY_GIFT_HAS_ARRIVED_YOU_CAN_OBTAIN_IT_FROM_THE_GATEKEEPER_IN_ANY_VILLAGE);
		}
		else if ((percent >= 50) && (percent < 75))
		{
			_player.sendPacket(SystemMessageId.YOU_ARE_FURTHER_INFUSED_WITH_THE_BLESSINGS_OF_NEVIT);
		}
		else if (percent >= 75)
		{
			_player.sendPacket(SystemMessageId.YOUR_CLOAK_HAS_BEEN_UNEQUIPPED_BECAUSE_YOUR_ARMOR_SET_IS_NO_LONGER_COMPLETE);
		}
	}
	
	@RegisterEvent(EventType.ON_PLAYER_LOGOUT)
	private void OnPlayerLogout(OnPlayerLogout event)
	{
		stopNevitEffectTask(true);
		stopAdventTask(false);
	}
	
	public void addPoints(int val)
	{
		if (getEffectTime() > 0)
		{
			setAdventPoints(0);
		}
		else
		{
			setAdventPoints(getAdventPoints() + val);
		}
		
		if (getAdventPoints() > Config.NEVIT_MAX_POINTS)
		{
			setAdventPoints(0);
			startNevitEffect(Config.NEVIT_BONUS_EFFECT_TIME);
		}
		
		final int percent = calcPercent(getAdventPoints());
		switch (percent)
		{
			case 45:
			{
				getPlayer().sendPacket(SystemMessageId.YOU_ARE_STARTING_TO_FEEL_THE_EFFECTS_OF_NEVIT_S_ADVENT_BLESSING);
				break;
			}
			case 50:
			{
				getPlayer().sendPacket(SystemMessageId.YOU_ARE_FURTHER_INFUSED_WITH_THE_BLESSINGS_OF_NEVIT);
				break;
			}
			case 75:
			{
				getPlayer().sendPacket(SystemMessageId.NEVIT_S_ADVENT_BLESSING_SHINES_STRONGLY_FROM_ABOVE);
				break;
			}
		}
		
		getPlayer().sendPacket(new ExNevitAdventPointInfoPacket(getAdventPoints()));
	}
	
	public void startAdventTask()
	{
		if (_adventTask == null)
		{
			synchronized (this)
			{
				if ((_adventTask == null) && (getAdventTime() < Config.NEVIT_ADVENT_TIME))
				{
					_adventTask = ThreadPoolManager.getInstance().scheduleGeneral(new AdventTask(), 30000);
					getPlayer().sendPacket(new ExNevitAdventTimeChange(getAdventTime(), false));
				}
			}
		}
	}
	
	public class AdventTask implements Runnable
	{
		@Override
		public void run()
		{
			setAdventTime(getAdventTime() + 30);
			if (getAdventTime() >= Config.NEVIT_ADVENT_TIME)
			{
				setAdventTime(Config.NEVIT_ADVENT_TIME);
				stopAdventTask(true);
			}
			else
			{
				addPoints(72);
				if ((getAdventTime() % 60) == 0)
				{
					getPlayer().sendPacket(new ExNevitAdventTimeChange(getAdventTime(), false));
				}
			}
			stopAdventTask(false);
		}
	}
	
	public synchronized void stopAdventTask(boolean sendPacket)
	{
		if (_adventTask != null)
		{
			_adventTask.cancel(true);
			_adventTask = null;
		}
		if (sendPacket)
		{
			getPlayer().sendPacket(new ExNevitAdventTimeChange(getAdventTime(), true));
		}
	}
	
	public synchronized void startNevitEffect(int time)
	{
		if (getEffectTime() > 0)
		{
			stopNevitEffectTask(false);
			time += getEffectTime();
		}
		if ((Config.NEVIT_IGNORE_ADVENT_TIME || (getAdventTime() < Config.NEVIT_ADVENT_TIME)) && (time > 0))
		{
			getPlayer().getVariables().set("nevit_b", time);
			getPlayer().sendPacket(new ExNevitAdventEffect(time));
			getPlayer().sendPacket(SystemMessageId.NEVIT_HAS_BLESSED_YOU_FROM_ABOVE);
			getPlayer().startAbnormalVisualEffect(true, AbnormalVisualEffect.NAVIT_ADVENT);
			_nevitEffectTask = ThreadPoolManager.getInstance().scheduleGeneral(new NevitEffectEnd(), time * 1000L);
		}
	}
	
	public class NevitEffectEnd implements Runnable
	{
		@Override
		public void run()
		{
			if (Config.NEVIT_IGNORE_ADVENT_TIME)
			{
				setAdventTime(0);
			}
			getPlayer().getVariables().remove("nevit_b");
			getPlayer().sendPacket(new ExNevitAdventEffect(0));
			getPlayer().sendPacket(new ExNevitAdventPointInfoPacket(getAdventPoints()));
			getPlayer().sendPacket(SystemMessageId.NEVIT_S_ADVENT_BLESSING_HAS_ENDED_CONTINUE_YOUR_JOURNEY_AND_YOU_WILL_SURELY_MEET_HIS_FAVOR_AGAIN_SOMETIME_SOON);
			getPlayer().stopAbnormalVisualEffect(true, AbnormalVisualEffect.NAVIT_ADVENT);
			stopNevitEffectTask(false);
		}
	}
	
	public synchronized void stopNevitEffectTask(boolean saveTime)
	{
		if (_nevitEffectTask != null)
		{
			if (saveTime)
			{
				final int time = getEffectTime();
				if (time > 0)
				{
					getPlayer().getVariables().set("nevit_b", time);
				}
				else
				{
					getPlayer().getVariables().remove("nevit_b");
				}
			}
			_nevitEffectTask.cancel(true);
			_nevitEffectTask = null;
		}
	}
	
	public L2PcInstance getPlayer()
	{
		return _player;
	}
	
	@Override
	public int getObjectId()
	{
		return _player.getObjectId();
	}
	
	private int getEffectTime()
	{
		if (_nevitEffectTask == null)
		{
			return 0;
		}
		return (int) Math.max(0, _nevitEffectTask.getDelay(TimeUnit.SECONDS));
	}
	
	public boolean isAdventBlessingActive()
	{
		return ((_nevitEffectTask != null) && (_nevitEffectTask.getDelay(TimeUnit.MILLISECONDS) > 0));
	}
	
	public static int calcPercent(int points)
	{
		return (int) ((100.0D / Config.NEVIT_MAX_POINTS) * points);
	}
	
	public void setAdventPoints(int points)
	{
		getPlayer().getVariables().set("hunting_points", points);
	}
	
	public void setAdventTime(int time)
	{
		getPlayer().getVariables().set("hunting_time", time);
	}
	
	public int getAdventPoints()
	{
		return Config.NEVIT_ENABLED ? getPlayer().getVariables().getInt("hunting_points", 0) : 0;
	}
	
	public int getAdventTime()
	{
		return Config.NEVIT_ENABLED ? getPlayer().getVariables().getInt("hunting_time", 0) : 0;
	}
}