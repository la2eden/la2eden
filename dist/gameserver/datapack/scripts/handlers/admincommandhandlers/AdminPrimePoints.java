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
import com.la2eden.gameserver.model.L2World;
import com.la2eden.gameserver.model.actor.instance.L2PcInstance;

import java.util.StringTokenizer;

/**
 * Custom admin commands for primeshop
 *
 * @author Enkel
 */
public class AdminPrimePoints implements IAdminCommandHandler {
    private static final String SET_MSG = "You just received %points% Prime Points from an admin";
    private static final String GIVE_MSG = "You just received %points% Prime Points from an admin";
    private static final String CLEAR_MSG = "An admin clearead your prime points";
    private static final String REWARD_MSG = "You just received %points% Prime Points from an admin";

    private static final String[] ADMIN_COMMANDS =
            {
                    "admin_primepoints", // Show admin menu
                    "admin_set_primepoints", // Set by target
                    "admin_give_primepoints", // Set by name
                    "admin_clear_primepoints", // Set to 0 (target)
                    "admin_reward_primepoints" // Set to all players online
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

        switch (cmd) {
            case "admin_primepoints": {
                AdminHtml.showAdminHtml(activeChar, "primepoints.htm");
                return true;
            }
            case "admin_give_primepoints": {
                final String player = st.nextToken();

                try
                {
                    points = Integer.valueOf(st.nextToken());
                }
                catch (Exception e)
                {
                    activeChar.sendMessage("Usage: //give_primepoints <player> [points]");
                }

                return givePoints(activeChar, player, points);
            }
            case "admin_set_primepoints": {
                L2PcInstance target;
                target = (L2PcInstance) activeChar.getTarget();

                try
                {
                    points = Integer.valueOf(st.nextToken());
                }
                catch (Exception e)
                {
                    activeChar.sendMessage("Usage: //set_primepoints [points]");
                }

                return setPoints(activeChar, target.getName(), points);
            }
            case "admin_clear_primepoints": {
                final String player = st.nextToken();

                return clearPoints(activeChar, player);
            }
            case "admin_reward_primepoints": {
                try
                {
                    points = Integer.valueOf(st.nextToken());
                }
                catch (Exception e)
                {
                    activeChar.sendMessage("Usage: //reward_primepoints [points]");
                }

                return rewardPoints(activeChar, points);
            }
        }

        activeChar.sendMessage("Invalid command.");
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

    /**
     * Reward $points to everyone online
     *
     * @param admin command issuer
     * @param points points to give
     */
    private boolean rewardPoints(L2PcInstance admin, int points) {
        if (points > 0) {
            updatePlayerPoints(
                    null, (long) points, true, true,
                    REWARD_MSG.replaceAll("%points%", String.valueOf(points))
            );

            return true;
        }

        admin.sendMessage("Usage: //reward_primepoints [points]");
        return false;
    }

    /**
     * Clear the $player's prime points
     *
     * @param admin
     * @param player
     */
    private boolean clearPoints(L2PcInstance admin, String player)
    {
        if (player != null) {
            updatePlayerPoints(player, (long) 0, false, false, CLEAR_MSG);

            return true;
        }

        admin.sendMessage("Usage: //clear_primepoints <player>");
        return false;
    }

    /**
     * Set the new $points to player in target
     *
     * @param admin
     * @param player
     * @param points
     */
    private boolean setPoints(L2PcInstance admin, String player, int points)
    {
        if (admin.getTarget() instanceof L2PcInstance) {
            if (points > 0) {
                updatePlayerPoints(
                        player, (long) points, true, false,
                        SET_MSG.replaceAll("%points%", String.valueOf(points))
                );

                return true;
            }
        }

        admin.sendMessage("Target not found or is not a player");
        return false;
    }

    /**
     * Give $points the the specified $player
     *
     * @param admin
     * @param player
     * @param points
     */
    private boolean givePoints(L2PcInstance admin, String player, int points)
    {
        if (points > 0) {
            updatePlayerPoints(
                    player, (long) points, true, false,
                    GIVE_MSG.replaceAll("%points%", String.valueOf(points))
            );

            return true;
        }

        admin.sendMessage("Usage: //give_primepoints <player> [points]");
        return false;
    }

    /**
     * This method is just for updating the database WITHOUT L2PcInstance
     *
     * @param player_name player name as string
     * @param points points to give
     * @param add Should points be ADDED or REPLACED?
     * @param msg Message to send to the player, if online
     */
    private void updatePlayerPoints(String player_name, Long points, boolean add, boolean isReward, String msg)
    {
        // This can be handy when using the reward command
        if (player_name != null) {
            /*
            try (Connection con = DatabaseFactory.getInstance().getConnection()) {
                final PreparedStatement statement = con.prepareStatement("UPDATE characters SET prime_points=? WHERE char_name=?");

                statement.setLong(1, points);
                statement.setString(2, player_name);

                statement.execute();
                statement.close();
            } catch (SQLException e) {
                // TODO: Log to console?
            }
            */
        }

        for (L2PcInstance player : L2World.getInstance().getPlayers()) {
            if (player.getName().equalsIgnoreCase(player_name) || isReward) {
                if (add) {
                    player.setPrimePoints(player.getPrimePoints() + points);
                } else {
                    player.setPrimePoints(points);
                }

                player.sendMessage(msg);
                break;
            }
        }
    }
}
