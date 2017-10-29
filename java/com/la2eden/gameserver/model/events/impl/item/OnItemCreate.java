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
package com.la2eden.gameserver.model.events.impl.item;

import com.la2eden.gameserver.model.actor.instance.L2PcInstance;
import com.la2eden.gameserver.model.events.EventType;
import com.la2eden.gameserver.model.events.impl.IBaseEvent;
import com.la2eden.gameserver.model.items.instance.L2ItemInstance;

/**
 * @author UnAfraid
 */
public class OnItemCreate implements IBaseEvent
{
	private final String _process;
	private final L2ItemInstance _item;
	private final L2PcInstance _activeChar;
	private final Object _reference;
	
	public OnItemCreate(String process, L2ItemInstance item, L2PcInstance actor, Object reference)
	{
		_process = process;
		_item = item;
		_activeChar = actor;
		_reference = reference;
	}
	
	public String getProcess()
	{
		return _process;
	}
	
	public L2ItemInstance getItem()
	{
		return _item;
	}
	
	public L2PcInstance getActiveChar()
	{
		return _activeChar;
	}
	
	public Object getReference()
	{
		return _reference;
	}
	
	@Override
	public EventType getType()
	{
		return EventType.ON_ITEM_CREATE;
	}
}
