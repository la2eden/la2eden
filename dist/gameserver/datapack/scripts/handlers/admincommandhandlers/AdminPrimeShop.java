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
import com.la2eden.gameserver.datatables.PrimeShopTable;
import com.la2eden.gameserver.handler.IAdminCommandHandler;
import com.la2eden.gameserver.model.actor.instance.L2PcInstance;

import java.util.StringTokenizer;

/**
 * @author Enkel
 */
public class AdminPrimeShop implements IAdminCommandHandler {
    private static final String SET_MSG = "You just received %points% Prime Points from an admin";
    private static final String GIVE_MSG = "You just received %points% Prime Points from an admin";
    private static final String CLEAR_MSG = "An admin clearead your prime points";
    private static final String REWARD_MSG = "You just received %points% Prime Points from an admin";

    private static final String[] ADMIN_COMMANDS =
            {
                    "admin_primeshop", // Show admin menu
                    "admin_primeshop_add", // Add a prime item to the primeshop table
                    "admin_primeshop_del", // Delete a prime item from the primeshop table
                    "admin_primeshop_set", // Updates a prime item from the primeshop table
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

        final StringTokenizer st = new StringTokenizer(command, " ");
        final String cmd = st.nextToken();

        switch (cmd) {
            case "admin_primeshop": {
                AdminHtml.showAdminHtml(activeChar, "primeshop.htm");
                return true;
            }
            case "admin_primeshop_add": {
                Integer brId = 0;
                Integer stock = 0;

                try {
                    brId = Integer.valueOf(st.nextToken());
                    stock = Integer.valueOf(st.nextToken());
                } catch (Exception e) {
                    activeChar.sendMessage("Usage: //primeshop_add <brId> [stock]");
                    return false;
                }

                return addPItem(activeChar, brId, stock);
            }
            case "admin_primeshop_del": {
                Integer brId = 0;

                try {
                    brId = Integer.valueOf(st.nextToken());
                } catch (Exception e) {
                    activeChar.sendMessage("Usage: //primeshop_del <brId>");
                    return false;
                }

                return deletePItem(activeChar, brId);
            }
            case "admin_primeshop_set": {

                Integer brId = 0;
                Integer stock = 0;
                Integer sold = 0;

                try {
                    brId = Integer.valueOf(st.nextToken());
                    stock = Integer.valueOf(st.nextToken());
                    sold = Integer.valueOf(st.nextToken());
                } catch (Exception e) {
                    activeChar.sendMessage("Usage: //primeshop_set <brId> [stock] [sold]");
                    return false;
                }

                return updatePItem(activeChar, brId, stock, sold);
            }
        }

        activeChar.sendMessage("Invalid command.");
        return false;
    }

    private boolean addPItem(L2PcInstance admin, int brId, int stock)
    {
        if ((brId > 0) && (stock > 0)
                && (!PrimeShopTable.PrimeShopHelper.hasPrimeItem(brId))
                && (PrimeShopTable.PrimeShopHelper.addPrimeItem(brId, stock)))
        {
            admin.sendMessage("You added " + brId + " to the primeshop table.");

            return true;
        }

        admin.sendMessage("Usage: //primeshop_add <brId> [stock]");
        return false;
    }

    private boolean deletePItem(L2PcInstance admin, int brId)
    {
        if ((brId > 0)
                && (PrimeShopTable.PrimeShopHelper.hasPrimeItem(brId))
                && (PrimeShopTable.PrimeShopHelper.removePrimeItem(brId)))
        {
            admin.sendMessage("You removed " + brId + " from the primeshop table.");

            return true;
        }

        admin.sendMessage("Usage: //primeshop_del <brId>");
        return false;
    }

    private boolean updatePItem(L2PcInstance admin, int brId, int stock, int sold)
    {
        if ((brId > 0) && (stock > 0) && (sold >= 0)
                && (PrimeShopTable.PrimeShopHelper.hasPrimeItem(brId))
                && (PrimeShopTable.PrimeShopHelper.setMaxStock(brId, stock))
                && (PrimeShopTable.PrimeShopHelper.setSoldCount(brId, sold)))
        {
            admin.sendMessage("You updated " + brId + " from the primeshop table.");

            return true;
        }

        admin.sendMessage("Usage: //primeshop_set <brId> [stock] [sold]");
        return false;
    }

    @Override
    public String[] getAdminCommandList()
    {
        return ADMIN_COMMANDS;
    }

    public static void main(String[] args)
    {
        new AdminPrimeShop();
    }
}
