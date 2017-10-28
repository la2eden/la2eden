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
package com.la2eden.gameserver.model;

import java.util.concurrent.ScheduledFuture;

import com.la2eden.gameserver.ThreadPoolManager;
import com.la2eden.gameserver.model.actor.instance.L2PcInstance;
import com.la2eden.gameserver.model.actor.instance.L2PetInstance;

/**
 * @author DrHouse
 */
public class DropProtection implements Runnable
{
	private volatile boolean _isProtected = false;
	private L2PcInstance _owner = null;
	private ScheduledFuture<?> _task = null;
	
	private static final long PROTECTED_MILLIS_TIME = 15000;
	
	@Override
	public synchronized void run()
	{
		_isProtected = false;
		_owner = null;
		_task = null;
	}
	
	public boolean isProtected()
	{
		return _isProtected;
	}
	
	public L2PcInstance getOwner()
	{
		return _owner;
	}
	
	public synchronized boolean tryPickUp(L2PcInstance actor)
	{
		return !_isProtected || (_owner == actor) || ((_owner.getParty() != null) && (_owner.getParty() == actor.getParty()));
	}
	
	public boolean tryPickUp(L2PetInstance pet)
	{
		return tryPickUp(pet.getOwner());
	}
	
	public synchronized void unprotect()
	{
		if (_task != null)
		{
			_task.cancel(false);
		}
		_isProtected = false;
		_owner = null;
		_task = null;
	}
	
	public synchronized void protect(L2PcInstance player)
	{
		unprotect();
		
		_isProtected = true;
		_owner = player;
		
		if (_owner == null)
		{
			throw new NullPointerException("Trying to protect dropped item to null owner");
		}
		
		_task = ThreadPoolManager.getInstance().scheduleGeneral(this, PROTECTED_MILLIS_TIME);
	}
}
