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
package com.la2eden.gameserver.taskmanager.tasks;

import com.la2eden.gameserver.instancemanager.GlobalVariablesManager;
import com.la2eden.gameserver.taskmanager.Task;
import com.la2eden.gameserver.taskmanager.TaskManager;
import com.la2eden.gameserver.taskmanager.TaskManager.ExecutedTask;
import com.la2eden.gameserver.taskmanager.TaskTypes;

/**
 * @author Gigiikun
 */
public class TaskGlobalVariablesSave extends Task
{
	public static final String NAME = "global_varibales_save";
	
	@Override
	public String getName()
	{
		return NAME;
	}
	
	@Override
	public void onTimeElapsed(ExecutedTask task)
	{
		GlobalVariablesManager.getInstance().storeMe();
	}
	
	@Override
	public void initializate()
	{
		super.initializate();
		TaskManager.addUniqueTask(NAME, TaskTypes.TYPE_FIXED_SHEDULED, "500000", "1800000", "");
	}
}