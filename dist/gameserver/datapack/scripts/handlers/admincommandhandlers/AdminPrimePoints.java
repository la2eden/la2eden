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
import com.la2eden.gameserver.handler.IAdminCommandHandler;
import com.la2eden.gameserver.model.actor.instance.L2PcInstance;

import java.util.StringTokenizer;

/**
 * @author Enkel
 */
public class AdminPrimePoints implements IAdminCommandHandler {
    private static final String[] ADMIN_COMMANDS =
            {
                    "admin_set_primepoints"
            };

    @Override
    public boolean useAdminCommand(String command, L2PcInstance activeChar)
    {
        if (activeChar == null)
        {
            return false;
        }

        if (!Config.PRIMESHOP_ENABLED)
        {
            activeChar.sendMessage("PrimeShop system is not enabled on the server!");
            return false;
        }

        int points = 0;

        final StringTokenizer st = new StringTokenizer(command, " ");
        final String cmd = st.nextToken();

        if (activeChar.getTarget() instanceof L2PcInstance)
        {
            L2PcInstance target;
            target = (L2PcInstance) activeChar.getTarget();

            if (cmd.equals("admin_set_primepoints"))
            {
                try
                {
                    points = Integer.valueOf(st.nextToken());
                }
                catch (Exception e)
                {
                    activeChar.sendMessage("Usage: //set_primepoints <points>");
                }

                if (points > 0) {
                    target.setPrimePoints(target.getPrimePoints() + points);
                    target.sendMessage("Received " + points + " Prime Points from admin");
                }
            }

            return true;
        }

        activeChar.sendMessage("Target not found or is not a player");
        return false;
    }

    @Override
    public String[] getAdminCommandList()
    {
        return ADMIN_COMMANDS;
    }

    public static void main(String[] args)
    {
        new AdminPrimePoints();
    }
}
