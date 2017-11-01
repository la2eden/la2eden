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
            NpcHtmlMessage msg = new NpcHtmlMessage(5);

            sb.append("<html noscrollbar><title>GrandBoss Status</title><body>");
            sb.append("<table cellspacing=0 cellpadding=0 width=294 height=359 background=\"L2UI_CH3.refinewnd_back_Pattern\">");
            sb.append("<tr><td height=10 width=300 align=center><br><center><font name=\"hs22\">Boss Watcher</font><br1><font color=4c4c4c>La2Eden</font></center><br></td></tr>");

            sb.append("<tr><td height=180 width=294><br><br>");

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

            sb.append("</td></tr><tr><td height=50 width=294><center><br><br><br><button value=\"Refresh\" action=\"bypass -h .grandboss\" width=80 height=30 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></center></td></tr>");

            sb.append("</table></body></html>");
            msg.setHtml(sb.toString());

            activeChar.sendPacket(msg);
        }

        return true;
    }

    @Override
    public String[] getVoicedCommandList()
    {
        return VOICED_COMMANDS;
    }
}
