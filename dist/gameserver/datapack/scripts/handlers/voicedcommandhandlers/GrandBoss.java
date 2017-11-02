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
import com.la2eden.gameserver.data.xml.impl.NpcData;
import com.la2eden.gameserver.handler.IVoicedCommandHandler;
import com.la2eden.gameserver.instancemanager.GrandBossManager;
import com.la2eden.gameserver.model.actor.instance.L2PcInstance;
import com.la2eden.gameserver.model.actor.templates.L2NpcTemplate;
import com.la2eden.gameserver.network.serverpackets.NpcHtmlMessage;

/**
 * Grandboss information voiced command
 * @author mirand0x
 */
public class GrandBoss implements IVoicedCommandHandler
{
    private static final String[] VOICED_COMMANDS =
            {
                    "grandboss"
            };

    @Override
    public boolean useVoicedCommand(String command, L2PcInstance activeChar, String target)
    {
        if (command.startsWith("grandboss"))
        {
            StringBuilder sb = new StringBuilder();
            String file = "datapack/html/mods/GrandBoss/Status.htm";
            String content = HtmCache.getInstance().getHtm(activeChar.getHtmlPrefix(), file);
            NpcHtmlMessage html = new NpcHtmlMessage(content);

            for (int boss : Config.GRANDBOSS_LIST)
            {
                L2NpcTemplate npc = NpcData.getInstance().getTemplate(boss);
                String name = npc.getName();
                byte lvl = npc.getLevel();
                long delay = GrandBossManager.getInstance().getStatsSet(boss).getLong("respawn_time");

                if (delay <= System.currentTimeMillis())
                {
                    sb.append("&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<font color=\"00C3FF\">" + name + " (" + lvl + ")</font>: " + "<font color=\"9CC300\">alive</font>" + "<br1>");
                }
                else
                {
                    int hours = (int) ((delay - System.currentTimeMillis()) / 1000 / 60 / 60);
                    int mins = (int) (((delay - (hours * 60 * 60 * 1000)) - System.currentTimeMillis()) / 1000 / 60);
                    int seconds = (int) (((delay - ((hours * 60 * 60 * 1000) + (mins * 60 * 1000))) - System.currentTimeMillis()) / 1000);

                    sb.append("&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<font color=\"00C3FF\">" + name + " (" + lvl + ")</font>: <font color=\"FFFFFF\">will respawn in</font> <font color=\"FF3333\">" + hours + "h " + mins + "m " + seconds + "s</font><br1>");
                }
            }

            html.replace("%BossStatus%", sb.toString());

            activeChar.sendPacket(html);
        }

        return true;
    }

    @Override
    public String[] getVoicedCommandList()
    {
        return VOICED_COMMANDS;
    }
}
