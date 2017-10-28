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
package com.la2eden.gameserver.pathfinding.utils;

import java.util.ArrayList;

import com.la2eden.gameserver.pathfinding.AbstractNode;

/**
 * @author -Nemesiss-
 */
public class FastNodeList
{
	private final ArrayList<AbstractNode<?>> _list;
	
	public FastNodeList(int size)
	{
		_list = new ArrayList<>(size);
	}
	
	public void add(AbstractNode<?> n)
	{
		_list.add(n);
	}
	
	public boolean contains(AbstractNode<?> n)
	{
		return _list.contains(n);
	}
	
	public boolean containsRev(AbstractNode<?> n)
	{
		return _list.lastIndexOf(n) != -1;
	}
}
