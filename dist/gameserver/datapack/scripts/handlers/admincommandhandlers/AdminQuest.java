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
package handlers.admincommandhandlers;

import java.nio.file.Paths;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.la2eden.gameserver.handler.IAdminCommandHandler;
import com.la2eden.gameserver.instancemanager.QuestManager;
import com.la2eden.gameserver.model.actor.L2Character;
import com.la2eden.gameserver.model.actor.instance.L2PcInstance;
import com.la2eden.gameserver.model.events.EventType;
import com.la2eden.gameserver.model.events.ListenerRegisterType;
import com.la2eden.gameserver.model.events.listeners.AbstractEventListener;
import com.la2eden.gameserver.model.quest.Quest;
import com.la2eden.gameserver.model.quest.QuestTimer;
import com.la2eden.gameserver.network.serverpackets.NpcHtmlMessage;
import com.la2eden.gameserver.scripting.ScriptEngineManager;
import com.la2eden.gameserver.util.Util;

public class AdminQuest implements IAdminCommandHandler
{
	public static final Logger LOGGER = Logger.getLogger(AdminQuest.class.getName());
	
	private static final String[] ADMIN_COMMANDS =
	{
		"admin_quest_reload",
		"admin_script_load",
		"admin_script_unload",
		"admin_show_quests",
		"admin_quest_info"
	};
	
	private static Quest findScript(String script)
	{
		if (Util.isDigit(script))
		{
			return QuestManager.getInstance().getQuest(Integer.parseInt(script));
		}
		return QuestManager.getInstance().getQuest(script);
	}
	
	@Override
	public boolean useAdminCommand(String command, L2PcInstance activeChar)
	{
		if (command.startsWith("admin_quest_reload"))
		{
			StringTokenizer st = new StringTokenizer(command);
			st.nextToken(); // skip command token
			
			if (!st.hasMoreTokens())
			{
				activeChar.sendMessage("Usage: //quest_reload <questName> or <questId>");
				return false;
			}
			
			String script = st.nextToken();
			Quest quest = findScript(script);
			if (quest == null)
			{
				activeChar.sendMessage("The script " + script + " couldn't be found!");
				return false;
			}
			
			if (!quest.reload())
			{
				activeChar.sendMessage("Failed to reload " + script + "!");
				return false;
			}
			
			activeChar.sendMessage("Script successful reloaded.");
		}
		else if (command.startsWith("admin_script_load"))
		{
			StringTokenizer st = new StringTokenizer(command);
			st.nextToken(); // skip command token
			
			if (!st.hasMoreTokens())
			{
				activeChar.sendMessage("Usage: //script_load path/to/script.java");
				return false;
			}
			
			String script = st.nextToken();
			try
			{
				ScriptEngineManager.getInstance().executeScript(Paths.get(script));
				activeChar.sendMessage("Script loaded seccessful!");
			}
			catch (Exception e)
			{
				activeChar.sendMessage("Failed to load script!");
				LOGGER.log(Level.WARNING, "Failed to load script " + script + "!", e);
			}
		}
		else if (command.startsWith("admin_script_unload"))
		{
			StringTokenizer st = new StringTokenizer(command);
			st.nextToken(); // skip command token
			
			if (!st.hasMoreTokens())
			{
				activeChar.sendMessage("Usage: //script_load path/to/script.java");
				return false;
			}
			
			String script = st.nextToken();
			Quest quest = findScript(script);
			if (quest == null)
			{
				activeChar.sendMessage("The script " + script + " couldn't be found!");
				return false;
			}
			
			quest.unload();
			activeChar.sendMessage("Script successful unloaded!");
		}
		else if (command.startsWith("admin_show_quests"))
		{
			if (activeChar.getTarget() == null)
			{
				activeChar.sendMessage("Get a target first.");
			}
			else if (!activeChar.getTarget().isCharacter())
			{
				activeChar.sendMessage("Invalid Target.");
			}
			else
			{
				final L2Character character = (L2Character) activeChar.getTarget();
				final StringBuilder sb = new StringBuilder();
				final Set<String> questNames = new TreeSet<>();
				for (EventType type : EventType.values())
				{
					for (AbstractEventListener listener : character.getListeners(type))
					{
						if (listener.getOwner() instanceof Quest)
						{
							final Quest quest = (Quest) listener.getOwner();
							if (!questNames.add(quest.getName()))
							{
								continue;
							}
							sb.append("<tr><td colspan=\"4\"><font color=\"LEVEL\"><a action=\"bypass -h admin_quest_info " + quest.getName() + "\">" + quest.getName() + "</a></font></td></tr>");
						}
					}
				}
				
				final NpcHtmlMessage msg = new NpcHtmlMessage(0, 1);
				msg.setFile(activeChar.getHtmlPrefix(), "./datapack/html/admin/npc-quests.htm");
				msg.replace("%quests%", sb.toString());
				msg.replace("%objid%", character.getObjectId());
				msg.replace("%questName%", "");
				activeChar.sendPacket(msg);
			}
		}
		else if (command.startsWith("admin_quest_info "))
		{
			final String questName = command.substring("admin_quest_info ".length());
			final Quest quest = QuestManager.getInstance().getQuest(questName);
			String events = "", npcs = "", items = "", timers = "";
			int counter = 0;
			if (quest == null)
			{
				activeChar.sendMessage("Couldn't find quest or script with name " + questName + " !");
				return false;
			}
			
			final Set<EventType> listenerTypes = new TreeSet<>();
			for (AbstractEventListener listener : quest.getListeners())
			{
				if (listenerTypes.add(listener.getType()))
				{
					events += ", " + listener.getType().name();
					counter++;
				}
				if (counter > 10)
				{
					counter = 0;
					break;
				}
			}
			
			final Set<Integer> npcIds = new TreeSet<>(quest.getRegisteredIds(ListenerRegisterType.NPC));
			for (int npcId : npcIds)
			{
				npcs += ", " + npcId;
				counter++;
				if (counter > 50)
				{
					counter = 0;
					break;
				}
			}
			
			if (!events.isEmpty())
			{
				events = listenerTypes.size() + ": " + events.substring(2);
			}
			
			if (!npcs.isEmpty())
			{
				npcs = npcIds.size() + ": " + npcs.substring(2);
			}
			
			if (quest.getRegisteredItemIds() != null)
			{
				for (int itemId : quest.getRegisteredItemIds())
				{
					items += ", " + itemId;
					counter++;
					if (counter > 20)
					{
						counter = 0;
						break;
					}
				}
				items = quest.getRegisteredItemIds().length + ":" + items.substring(2);
			}
			
			for (List<QuestTimer> list : quest.getQuestTimers().values())
			{
				for (QuestTimer timer : list)
				{
					timers += "<tr><td colspan=\"4\"><table width=270 border=0 bgcolor=131210><tr><td width=270><font color=\"LEVEL\">" + timer.getName() + ":</font> <font color=00FF00>Active: " + timer.getIsActive() + " Repeatable: " + timer.getIsRepeating() + " Player: " + timer.getPlayer() + " Npc: " + timer.getNpc() + "</font></td></tr></table></td></tr>";
					counter++;
					if (counter > 10)
					{
						break;
					}
				}
			}
			
			final StringBuilder sb = new StringBuilder();
			sb.append("<tr><td colspan=\"4\"><table width=270 border=0 bgcolor=131210><tr><td width=270><font color=\"LEVEL\">ID:</font> <font color=00FF00>" + quest.getId() + "</font></td></tr></table></td></tr>");
			sb.append("<tr><td colspan=\"4\"><table width=270 border=0 bgcolor=131210><tr><td width=270><font color=\"LEVEL\">Name:</font> <font color=00FF00>" + quest.getName() + "</font></td></tr></table></td></tr>");
			sb.append("<tr><td colspan=\"4\"><table width=270 border=0 bgcolor=131210><tr><td width=270><font color=\"LEVEL\">Descr:</font> <font color=00FF00>" + quest.getDescr() + "</font></td></tr></table></td></tr>");
			sb.append("<tr><td colspan=\"4\"><table width=270 border=0 bgcolor=131210><tr><td width=270><font color=\"LEVEL\">Path:</font> <font color=00FF00>" + quest.getClass().getName().substring(0, quest.getClass().getName().lastIndexOf('.')).replaceAll("\\.", "/") + "</font></td></tr></table></td></tr>");
			sb.append("<tr><td colspan=\"4\"><table width=270 border=0 bgcolor=131210><tr><td width=270><font color=\"LEVEL\">Events:</font> <font color=00FF00>" + events + "</font></td></tr></table></td></tr>");
			if (!npcs.isEmpty())
			{
				sb.append("<tr><td colspan=\"4\"><table width=270 border=0 bgcolor=131210><tr><td width=270><font color=\"LEVEL\">NPCs:</font> <font color=00FF00>" + npcs + "</font></td></tr></table></td></tr>");
			}
			if (!items.isEmpty())
			{
				sb.append("<tr><td colspan=\"4\"><table width=270 border=0 bgcolor=131210><tr><td width=270><font color=\"LEVEL\">Items:</font> <font color=00FF00>" + items + "</font></td></tr></table></td></tr>");
			}
			if (!timers.isEmpty())
			{
				sb.append("<tr><td colspan=\"4\"><table width=270 border=0 bgcolor=131210><tr><td width=270><font color=\"LEVEL\">Timers:</font> <font color=00FF00></font></td></tr></table></td></tr>");
				sb.append(timers);
			}
			
			final NpcHtmlMessage msg = new NpcHtmlMessage(0, 1);
			msg.setFile(activeChar.getHtmlPrefix(), "./datapack/html/admin/npc-quests.htm");
			msg.replace("%quests%", sb.toString());
			msg.replace("%questName%", "<table><tr><td width=\"50\" align=\"left\"><a action=\"bypass -h admin_script_load " + quest.getName() + "\">Reload</a></td> <td width=\"150\"  align=\"center\"><a action=\"bypass -h admin_quest_info " + quest.getName() + "\">" + quest.getName() + "</a></td> <td width=\"50\" align=\"right\"><a action=\"bypass -h admin_script_unload " + quest.getName() + "\">Unload</a></tr></td></table>");
			activeChar.sendPacket(msg);
		}
		return true;
	}
	
	@Override
	public String[] getAdminCommandList()
	{
		return ADMIN_COMMANDS;
	}
}
