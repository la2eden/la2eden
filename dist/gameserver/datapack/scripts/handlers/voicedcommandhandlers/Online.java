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
package handlers.voicedcommandhandlers;

import com.la2eden.Config;
import com.la2eden.gameserver.cache.HtmCache;
import com.la2eden.gameserver.handler.IVoicedCommandHandler;
import com.la2eden.gameserver.model.L2World;
import com.la2eden.gameserver.model.actor.instance.L2PcInstance;
import com.la2eden.gameserver.network.serverpackets.NpcHtmlMessage;

import java.util.Collection;

/**
 * Shows the amount of online players to any1 who calls it.
 * @author mirand0x, St3et
 */
public class Online implements IVoicedCommandHandler
{
    private static long LAST_UPDATE = 0;
    private static int OFFLINE_COUNT = 0;
    private static int ONLINE_COUNT = 0;

    private static final String[] VOICED_COMMANDS =
            {
                    "online"
            };

    @Override
    public boolean useVoicedCommand(String command, L2PcInstance activeChar, String target)
    {
        if (command.startsWith("online"))
        {
            showPlayers(activeChar, target);
        }

        return true;
    }

    @Override
    public String[] getVoicedCommandList()
    {
        return VOICED_COMMANDS;
    }

    public void showPlayers(L2PcInstance player, String target)
    {
        if (System.currentTimeMillis() > (LAST_UPDATE + (5 * 60 * 1000)))
        {
            LAST_UPDATE = System.currentTimeMillis();
            ONLINE_COUNT = L2World.getInstance().getAllPlayersCount();
            int offlineCount = 0;

            final Collection<L2PcInstance> objs = L2World.getInstance().getPlayers();
            for (L2PcInstance playerInstance : objs)
            {
                if ((playerInstance.getClient() == null) || playerInstance.getClient().isDetached())
                {
                    offlineCount++;
                }
            }
            OFFLINE_COUNT = offlineCount;
        }

        if (Config.ENABLE_ONLINE_STATUS_HTML)
        {
            String file = Config.ENABLE_ONLINE_STATUS_SHOW_OFFLINE ? "html/mods/Online/OnlineStatus-off.htm" : "html/mods/Online/OnlineStatus.htm";
            String content = HtmCache.getInstance().getHtm(player.getHtmlPrefix(), file);
            NpcHtmlMessage html = new NpcHtmlMessage(content);

            html.replace("%OnlineCount%", String.valueOf(ONLINE_COUNT));
            html.replace("%OfflineCount%", String.valueOf(OFFLINE_COUNT));
            player.sendPacket(html);
        }
        else
        {
            player.sendMessage("======< Online >======");
            player.sendMessage("Online players: " + ONLINE_COUNT);

            if (Config.ENABLE_ONLINE_STATUS_SHOW_OFFLINE)
            {
                player.sendMessage("Offline traders/crafters: " + OFFLINE_COUNT);
            }

            player.sendMessage("===================");
        }
    }
}
