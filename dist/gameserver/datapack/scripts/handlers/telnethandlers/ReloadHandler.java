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
package handlers.telnethandlers;

import com.la2eden.gameserver.cache.HtmCache;
import com.la2eden.gameserver.data.sql.impl.TeleportLocationTable;
import com.la2eden.gameserver.data.xml.impl.MultisellData;
import com.la2eden.gameserver.data.xml.impl.NpcData;
import com.la2eden.gameserver.datatables.ItemTable;
import com.la2eden.gameserver.datatables.SkillData;
import com.la2eden.gameserver.datatables.SpawnTable;
import com.la2eden.gameserver.handler.ITelnetHandler;
import com.la2eden.gameserver.instancemanager.DayNightSpawnManager;
import com.la2eden.gameserver.instancemanager.QuestManager;
import com.la2eden.gameserver.instancemanager.RaidBossSpawnManager;
import com.la2eden.gameserver.instancemanager.ZoneManager;
import com.la2eden.gameserver.model.L2World;
import com.la2eden.gameserver.scripting.ScriptEngineManager;

import java.io.PrintWriter;
import java.net.Socket;
import java.nio.file.Paths;
import java.util.StringTokenizer;
import java.util.logging.Level;

/**
 * @author UnAfraid
 */
public class ReloadHandler implements ITelnetHandler
{
	private final String[] _commands =
	{
		"reload"
	};
	
	@Override
	public boolean useCommand(String command, PrintWriter _print, Socket _cSocket, int _uptime)
	{
		if (command.startsWith("reload"))
		{
			final StringTokenizer st = new StringTokenizer(command.substring(7));
			try
			{
				final String type = st.nextToken();
				
				if (type.equals("multisell"))
				{
					_print.print("Reloading multisell... ");
					MultisellData.getInstance().load();
					_print.println("done");
				}
				else if (type.equals("skill"))
				{
					_print.print("Reloading skills... ");
					SkillData.getInstance().reload();
					_print.println("done");
				}
				else if (type.equals("npc"))
				{
					_print.print("Reloading npc templates... ");
					NpcData.getInstance().load();
					QuestManager.getInstance().reloadAllScripts();
					_print.println("done");
				}
				else if (type.equals("html"))
				{
					_print.print("Reloading html cache... ");
					HtmCache.getInstance().reload();
					_print.println("done");
				}
				else if (type.equals("item"))
				{
					_print.print("Reloading item templates... ");
					ItemTable.getInstance().reload();
					_print.println("done");
				}
				else if (type.equals("zone"))
				{
					_print.print("Reloading zone tables... ");
					ZoneManager.getInstance().reload();
					_print.println("done");
				}
				else if (type.equals("teleports"))
				{
					_print.print("Reloading telport location table... ");
					TeleportLocationTable.getInstance().reloadAll();
					_print.println("done");
				}
				else if (type.equals("spawns"))
				{
					_print.print("Reloading spawns... ");
					RaidBossSpawnManager.getInstance().cleanUp();
					DayNightSpawnManager.getInstance().cleanUp();
					L2World.getInstance().deleteVisibleNpcSpawns();
					NpcData.getInstance().load();
					SpawnTable.getInstance().load();
					RaidBossSpawnManager.getInstance().load();
					_print.println("done\n");
				}
				else if (type.equalsIgnoreCase("script"))
				{
					try
					{
						String questPath = st.hasMoreTokens() ? st.nextToken() : "";
						
						try
						{
							ScriptEngineManager.getInstance().executeScript(Paths.get(questPath));
							_print.println(questPath + " was successfully loaded!\n");
						}
						catch (Exception e)
						{
							_log.log(Level.WARNING, "Failed to execute script!", e);
						}
					}
					catch (StringIndexOutOfBoundsException e)
					{
						_print.println("Please Enter Some Text!");
					}
				}
			}
			catch (Exception e)
			{
			}
		}
		return false;
	}
	
	@Override
	public String[] getCommandList()
	{
		return _commands;
	}
}
