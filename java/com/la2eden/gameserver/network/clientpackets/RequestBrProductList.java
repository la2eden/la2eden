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
package com.la2eden.gameserver.network.clientpackets;

import com.la2eden.Config;
import com.la2eden.gameserver.model.actor.instance.L2PcInstance;
import com.la2eden.gameserver.network.serverpackets.ExBrProductList;

public final class RequestBrProductList extends L2GameClientPacket
{
    private static final String TYPE = "[C] D0:89 RequestBrProductList";

    @Override
    protected void readImpl()
    {
    }

    @Override
    protected void runImpl() {
        L2PcInstance player = getClient().getActiveChar();

        if (Config.PRIMESHOP_PREMIUM_ONLY && player.hasPremiumStatus()) {
            player.sendMessage("Only premium players can use the PrimeShop.");
            return;
        }

        if (Config.PRIMESHOP_COMBAT_USE && player.isInCombat()) {
            player.sendMessage("You can't use PrimeShop while you are in combat.");
            return;
        }

        if (Config.PRIMESHOP_KARMA_USE && (player.getKarma() != 0)) {
            player.sendMessage("Players with Karma can't use the PrimeShop.");
            return;
        }

        if (Config.PRIMESHOP_FLAGGED_USE && (player.getPvpFlag() != 0)) {
            player.sendMessage("");
            return;
        }

        if (player != null) {
            player.sendPacket(new ExBrProductList());
        }
    }

    @Override
    public String getType()
    {
        return TYPE;
    }
}
