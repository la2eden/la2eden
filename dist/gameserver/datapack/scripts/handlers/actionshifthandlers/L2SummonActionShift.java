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
package handlers.actionshifthandlers;

import com.la2eden.gameserver.enums.InstanceType;
import com.la2eden.gameserver.handler.AdminCommandHandler;
import com.la2eden.gameserver.handler.IActionShiftHandler;
import com.la2eden.gameserver.handler.IAdminCommandHandler;
import com.la2eden.gameserver.model.L2Object;
import com.la2eden.gameserver.model.actor.instance.L2PcInstance;

public class L2SummonActionShift implements IActionShiftHandler
{
	@Override
	public boolean action(L2PcInstance activeChar, L2Object target, boolean interact)
	{
		if (activeChar.isGM())
		{
			if (activeChar.getTarget() != target)
			{
				// Set the target of the L2PcInstance activeChar
				activeChar.setTarget(target);
			}
			
			final IAdminCommandHandler ach = AdminCommandHandler.getInstance().getHandler("admin_summon_info");
			if (ach != null)
			{
				ach.useAdminCommand("admin_summon_info", activeChar);
			}
		}
		return true;
	}
	
	@Override
	public InstanceType getInstanceType()
	{
		return InstanceType.L2Summon;
	}
}