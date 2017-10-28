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
package com.la2eden.gameserver.model.instancezone;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

import com.la2eden.gameserver.instancemanager.InstanceManager;
import com.la2eden.gameserver.model.actor.L2Character;
import com.la2eden.gameserver.model.entity.Instance;
import com.la2eden.gameserver.network.SystemMessageId;
import com.la2eden.gameserver.network.serverpackets.SystemMessage;

/**
 * Basic instance zone data transfer object.
 * @author Zoey76
 */
public class InstanceWorld
{
	private int _instanceId;
	private int _templateId = -1;
	private final List<Integer> _allowed = new CopyOnWriteArrayList<>();
	private final AtomicInteger _status = new AtomicInteger();
	public int tag = -1;
	
	public List<Integer> getAllowed()
	{
		return _allowed;
	}
	
	public void removeAllowed(int id)
	{
		_allowed.remove(_allowed.indexOf(Integer.valueOf(id)));
	}
	
	public void addAllowed(int id)
	{
		_allowed.add(id);
	}
	
	public boolean isAllowed(int id)
	{
		return _allowed.contains(id);
	}
	
	/**
	 * Gets the dynamically generated instance ID.
	 * @return the instance ID
	 */
	public int getInstanceId()
	{
		return _instanceId;
	}
	
	/**
	 * Sets the instance ID.
	 * @param instanceId the instance ID
	 */
	public void setInstanceId(int instanceId)
	{
		_instanceId = instanceId;
	}
	
	/**
	 * Gets the client's template instance ID.
	 * @return the template ID
	 */
	public int getTemplateId()
	{
		return _templateId;
	}
	
	/**
	 * Sets the template ID.
	 * @param templateId the template ID
	 */
	public void setTemplateId(int templateId)
	{
		_templateId = templateId;
	}
	
	public int getStatus()
	{
		return _status.get();
	}
	
	public boolean isStatus(int status)
	{
		return _status.get() == status;
	}
	
	public void setStatus(int status)
	{
		_status.set(status);
	}
	
	public void incStatus()
	{
		_status.incrementAndGet();
	}
	
	/**
	 * @param killer
	 * @param victim
	 */
	public void onDeath(L2Character killer, L2Character victim)
	{
		if ((victim == null) || !victim.isPlayer())
		{
			return;
		}
		final Instance instance = InstanceManager.getInstance().getInstance(getInstanceId());
		if (instance == null)
		{
			return;
		}
		final SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.IF_YOU_ARE_NOT_RESURRECTED_WITHIN_S1_MINUTES_YOU_WILL_BE_EXPELLED_FROM_THE_INSTANT_ZONE);
		sm.addInt(instance.getEjectTime() / 60 / 1000);
		victim.getActingPlayer().sendPacket(sm);
		instance.addEjectDeadTask(victim.getActingPlayer());
	}
}
