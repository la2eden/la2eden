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

import com.la2eden.Config;
import com.la2eden.gameserver.GameServer;
import com.la2eden.gameserver.GameTimeController;
import com.la2eden.gameserver.ThreadPoolManager;
import com.la2eden.gameserver.cache.HtmCache;
import com.la2eden.gameserver.handler.IAdminCommandHandler;
import com.la2eden.gameserver.model.L2World;
import com.la2eden.gameserver.model.actor.instance.L2PcInstance;
import com.la2eden.gameserver.network.serverpackets.NpcHtmlMessage;

import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * @author St3eT
 */
public class AdminServerInfo implements IAdminCommandHandler
{
	private static final SimpleDateFormat fmt = new SimpleDateFormat("hh:mm a");
	
	private static final String[] ADMIN_COMMANDS =
	{
		"admin_serverinfo"
	};
	
	@Override
	public boolean useAdminCommand(String command, L2PcInstance activeChar)
	{
		if (command.equals("admin_serverinfo"))
		{
			final NpcHtmlMessage html = new NpcHtmlMessage();
			final Runtime RunTime = Runtime.getRuntime();
			final int mb = 1024 * 1024;
			html.setHtml(HtmCache.getInstance().getHtm(activeChar.getHtmlPrefix(), "./datapack/html/admin/serverinfo.htm"));
			
			html.replace("%os_name%", System.getProperty("os.name"));
			html.replace("%os_ver%", System.getProperty("os.version"));
			html.replace("%slots%", getPlayersCount("ALL") + "/" + Config.MAXIMUM_ONLINE_USERS);
			html.replace("%gameTime%", GameTimeController.getInstance().getGameHour() + ":" + GameTimeController.getInstance().getGameMinute());
			html.replace("%dayNight%", GameTimeController.getInstance().isNight() ? "Night" : "Day");
			html.replace("%geodata%", Config.PATHFINDING > 0 ? "Enabled" : "Disabled");
			html.replace("%serverTime%", fmt.format(new Date(System.currentTimeMillis())));
			html.replace("%serverUpTime%", getServerUpTime());
			html.replace("%onlineAll%", getPlayersCount("ALL"));
			html.replace("%offlineTrade%", getPlayersCount("OFF_TRADE"));
			html.replace("%onlineGM%", getPlayersCount("GM"));
			html.replace("%onlineReal%", getPlayersCount("ALL_REAL"));
			html.replace("%usedMem%", (RunTime.maxMemory() / mb) - (((RunTime.maxMemory() - RunTime.totalMemory()) + RunTime.freeMemory()) / mb));
			html.replace("%freeMem%", ((RunTime.maxMemory() - RunTime.totalMemory()) + RunTime.freeMemory()) / mb);
			html.replace("%totalMem%", Runtime.getRuntime().maxMemory() / 1048576);
			html.replace("%theardInfoGen%", buildTheardInfo("GENERAL"));
			html.replace("%theardInfoEff%", buildTheardInfo("EFFECTS"));
			html.replace("%theardInfoAi%", buildTheardInfo("AI"));
			html.replace("%theardInfoEvent%", buildTheardInfo("EVENT"));
			html.replace("%theardInfoPack%", buildTheardInfo("PACKETS"));
			html.replace("%theardInfoIOPack%", buildTheardInfo("IOPACKETS"));
			html.replace("%theardInfoGenTask%", buildTheardInfo("GENERAL_TASKS"));
			html.replace("%theardInfoEvnTask%", buildTheardInfo("EVENT_TASKS"));
			activeChar.sendPacket(html);
		}
		return true;
	}
	
	private String getServerUpTime()
	{
		long time = System.currentTimeMillis() - GameServer.dateTimeServerStarted.getTimeInMillis();
		
		final long days = TimeUnit.MILLISECONDS.toDays(time);
		time -= TimeUnit.DAYS.toMillis(days);
		final long hours = TimeUnit.MILLISECONDS.toHours(time);
		time -= TimeUnit.HOURS.toMillis(hours);
		final long minutes = TimeUnit.MILLISECONDS.toMinutes(time);
		
		return days + " Days, " + hours + " Hours, " + minutes + " Minutes";
	}
	
	private String buildTheardInfo(String category)
	{
		final StringBuilder tb = new StringBuilder();
		
		tb.append("<table width=\"270\" border=\"0\" bgcolor=\"444444\">");
		for (Entry<String, Object> info : ThreadPoolManager.getInstance().getStats(category).getSet().entrySet())
		{
			tb.append("<tr>");
			tb.append("<td>" + info.getKey() + ":</td>");
			tb.append("<td><font color=\"00FF00\">" + info.getValue() + "</font></td>");
			tb.append("</tr>");
		}
		tb.append("</table>");
		return tb.toString();
	}
	
	private int getPlayersCount(String type)
	{
		switch (type)
		{
			case "ALL":
			{
				return L2World.getInstance().getAllPlayersCount();
			}
			case "OFF_TRADE":
			{
				int offlineCount = 0;
				
				final Collection<L2PcInstance> objs = L2World.getInstance().getPlayers();
				for (L2PcInstance player : objs)
				{
					if ((player.getClient() == null) || player.getClient().isDetached())
					{
						offlineCount++;
					}
				}
				return offlineCount;
			}
			case "GM":
			{
				int onlineGMcount = 0;
				for (L2PcInstance gm : L2World.getInstance().getAllGMs())
				{
					if ((gm != null) && gm.isOnline() && (gm.getClient() != null) && !gm.getClient().isDetached())
					{
						onlineGMcount++;
					}
				}
				return onlineGMcount;
			}
			case "ALL_REAL":
			{
				final Set<String> realPlayers = new HashSet<>();
				
				for (L2PcInstance onlinePlayer : L2World.getInstance().getPlayers())
				{
					if (((onlinePlayer != null) && (onlinePlayer.getClient() != null)) && !onlinePlayer.getClient().isDetached())
					{
						realPlayers.add(onlinePlayer.getIPAddress());
					}
				}
				return realPlayers.size();
			}
		}
		return 0;
	}
	
	@Override
	public String[] getAdminCommandList()
	{
		return ADMIN_COMMANDS;
	}
}
