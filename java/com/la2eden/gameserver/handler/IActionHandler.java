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
package com.la2eden.gameserver.handler;

import java.util.logging.Logger;

import com.la2eden.gameserver.enums.InstanceType;
import com.la2eden.gameserver.model.L2Object;
import com.la2eden.gameserver.model.actor.instance.L2PcInstance;

public interface IActionHandler
{
	Logger _log = Logger.getLogger(IActionHandler.class.getName());
	
	boolean action(L2PcInstance activeChar, L2Object target, boolean interact);
	
	InstanceType getInstanceType();
}