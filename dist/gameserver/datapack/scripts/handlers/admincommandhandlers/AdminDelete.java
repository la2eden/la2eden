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
package handlers.admincommandhandlers;

import com.la2eden.gameserver.datatables.SpawnTable;
import com.la2eden.gameserver.handler.IAdminCommandHandler;
import com.la2eden.gameserver.instancemanager.RaidBossSpawnManager;
import com.la2eden.gameserver.model.L2Object;
import com.la2eden.gameserver.model.L2Spawn;
import com.la2eden.gameserver.model.actor.L2Npc;
import com.la2eden.gameserver.model.actor.instance.L2PcInstance;

/**
 * This class handles following admin commands: - delete = deletes target
 * @version $Revision: 1.2.2.1.2.4 $ $Date: 2005/04/11 10:05:56 $
 */
public class AdminDelete implements IAdminCommandHandler
{
	private static final String[] ADMIN_COMMANDS =
	{
		"admin_delete"
	};
	
	@Override
	public boolean useAdminCommand(String command, L2PcInstance activeChar)
	{
		if (command.equals("admin_delete"))
		{
			handleDelete(activeChar);
		}
		return true;
	}
	
	@Override
	public String[] getAdminCommandList()
	{
		return ADMIN_COMMANDS;
	}
	
	// TODO: add possibility to delete any L2Object (except L2PcInstance)
	private void handleDelete(L2PcInstance activeChar)
	{
		final L2Object obj = activeChar.getTarget();
		if (obj instanceof L2Npc)
		{
			final L2Npc target = (L2Npc) obj;
			target.deleteMe();
			
			final L2Spawn spawn = target.getSpawn();
			if (spawn != null)
			{
				spawn.stopRespawn();
				
				if (RaidBossSpawnManager.getInstance().isDefined(spawn.getId()))
				{
					RaidBossSpawnManager.getInstance().deleteSpawn(spawn, true);
				}
				else
				{
					SpawnTable.getInstance().deleteSpawn(spawn, true);
				}
			}
			
			activeChar.sendMessage("Deleted " + target.getName() + " from " + target.getObjectId() + ".");
		}
		else
		{
			activeChar.sendMessage("Incorrect target.");
		}
	}
}
