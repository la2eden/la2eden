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
package com.la2eden.gameserver.taskmanager.tasks;

import java.sql.Connection;
import java.sql.PreparedStatement;

import com.la2eden.commons.database.DatabaseFactory;
import com.la2eden.gameserver.model.L2World;
import com.la2eden.gameserver.model.actor.instance.L2PcInstance;
import com.la2eden.gameserver.taskmanager.Task;
import com.la2eden.gameserver.taskmanager.TaskManager;
import com.la2eden.gameserver.taskmanager.TaskManager.ExecutedTask;
import com.la2eden.gameserver.taskmanager.TaskTypes;

/**
 * @author Layane
 */
public class TaskRecom extends Task
{
	private static final String NAME = "recommendations";
	
	@Override
	public String getName()
	{
		return NAME;
	}
	
	@Override
	public void onTimeElapsed(ExecutedTask task)
	{
		try (Connection con = DatabaseFactory.getInstance().getConnection())
		{
			try (PreparedStatement ps = con.prepareStatement("UPDATE character_reco_bonus SET rec_left=?, time_left=?, rec_have=0 WHERE rec_have <=  20"))
			{
				ps.setInt(1, 0); // Rec left = 0
				ps.setInt(2, 3600000); // Timer = 1 hour
				ps.execute();
			}
			
			try (PreparedStatement ps = con.prepareStatement("UPDATE character_reco_bonus SET rec_left=?, time_left=?, rec_have=GREATEST(rec_have-20,0) WHERE rec_have > 20"))
			{
				ps.setInt(1, 0); // Rec left = 0
				ps.setInt(2, 3600000); // Timer = 1 hour
				ps.execute();
			}
		}
		catch (Exception e)
		{
			_log.severe(getClass().getSimpleName() + ": Could not reset Recommendations System: " + e);
		}
		
		// Refresh reco bonus for online players
		for (L2PcInstance player : L2World.getInstance().getPlayers())
		{
			if (player != null)
			{
				player.stopNevitHourglassTask();
				player.startNevitHourglassTask();
			}
		}
		_log.info("Recommendations System reseted");
	}
	
	@Override
	public void initializate()
	{
		super.initializate();
		TaskManager.addUniqueTask(NAME, TaskTypes.TYPE_GLOBAL_TASK, "1", "06:30:00", "");
	}
}