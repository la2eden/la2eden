/*
 * This file is part of the L2J Mavis project.
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
import com.la2eden.gameserver.handler.IVoicedCommandHandler;
import com.la2eden.gameserver.instancemanager.AwayManager;
import com.la2eden.gameserver.instancemanager.SiegeManager;
import com.la2eden.gameserver.model.actor.instance.L2PcInstance;
import com.la2eden.gameserver.model.entity.Siege;
import com.la2eden.gameserver.model.zone.ZoneId;

/**
 * Shows the amount of online players to any1 who calls it.
 * @author mirand0x
 */
public class Away implements IVoicedCommandHandler
{
    private static final String[] VOICED_COMMANDS =
            {
                    "away",
                    "back"
            };

    @Override
    public boolean useVoicedCommand(String command, L2PcInstance activeChar, String text)
    {
        if (command.startsWith("away"))
        {
            return away(activeChar, text);
        }
        else if (command.startsWith("back"))
        {
            return back(activeChar);
        }

        return false;
    }

    @Override
    public String[] getVoicedCommandList()
    {
        return VOICED_COMMANDS;
    }

    private boolean away(L2PcInstance activeChar, String text)
    {
        Siege siege = SiegeManager.getInstance().getSiege(activeChar);

        activeChar.sendMessage("Usage #1: .away");
        activeChar.sendMessage("Usage #2: .away <away_text>");

        // is there an away text?
        if (text == null)
        {
            text = "";
        }

        // check char is already in away mode
        if (activeChar.isAway())
        {
            activeChar.sendMessage("You are already in away mode!");
            return false;
        }

        if (!activeChar.isInsideZone(ZoneId.PEACE) && Config.AWAY_PEACE_ZONE)
        {
            activeChar.sendMessage("You can only go in away mode in a Peace Zone!");
            return false;
        }
        if (activeChar.isTransformed())
        {
            activeChar.sendMessage("You can't go away while transformed");
            return false;
        }
        // check player is death/fake death and movement disable
        if (activeChar.isMovementDisabled() || activeChar.isAlikeDead())
        {
            return false;
        }
        // Check if player is in Siege
        if ((siege != null) && siege.isInProgress())
        {
            activeChar.sendMessage("You are in siege, you cannot go AFK!");
            return false;
        }
        // Check if player is a Cursed Weapon owner
        if (activeChar.isCursedWeaponEquipped())
        {
            activeChar.sendMessage("You can't go AFK! You are currently holding a cursed weapon.");
            return false;
        }
        // Check if player is in Duel
        if (activeChar.isInDuel())
        {
            activeChar.sendMessage("You can't go AFK! You are in a duel.");
            return false;
        }
        // check is in Command Channel
        if (activeChar.isInParty() && activeChar.getParty().isInCommandChannel())
        {
            activeChar.sendMessage("You can't go AFK! Your command channel needs you.");
            return false;
        }
        // Check to see if the player is in an event
        if (activeChar.isOnEvent())
        {
            activeChar.sendMessage("You can't go AFK! You are in event now.");
            return false;
        }
        // check player is in Olympiade
        if (activeChar.isInOlympiadMode() || (activeChar.getOlympiadGameId() != -1))
        {
            activeChar.sendMessage("You cannot go Afk! Your are fighting in Olympiad!");
            return false;
        }
        // Check player is in observer mode
        if (activeChar.inObserverMode())
        {
            activeChar.sendMessage("You can't go AFK in Observer mode!");
            return false;
        }
        // check player have karma/pk/pvp status
        if (activeChar.getPvpFlag() > 0)
        {
            activeChar.sendMessage("Player in PvP or with Karma cannot use the away command!");
            return false;
        }
        if (activeChar.isImmobilized())
        {
            return false;
        }
        // check away text have not more then 10 letter
        if (text.length() > 10)
        {
            activeChar.sendMessage("Your away message cannot be longer than 10 letters!");
            return false;
        }
        // check if player have no one in target
        if (((activeChar.getTarget() == null) && (text.length() <= 1)) || (text.length() <= 10))
        {
            // set this Player status away in AwayManager
            AwayManager.getInstance().setAway(activeChar, text);
        }

        return true;
    }

    private boolean back(L2PcInstance activeChar)
    {
        if (!activeChar.isAway())
        {
            activeChar.sendMessage("You are not in away mode!");
            return false;
        }

        AwayManager.getInstance().setBack(activeChar);
        return true;
    }
}
