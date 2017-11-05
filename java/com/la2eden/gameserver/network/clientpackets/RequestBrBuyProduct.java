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

import com.la2eden.gameserver.datatables.PrimeShopTable;
import com.la2eden.gameserver.model.actor.instance.L2PcInstance;

public final class RequestBrBuyProduct extends L2GameClientPacket
{
    private static final String TYPE = "[C] D0:8C RequestBrBuyProduct";

    private int _product_id;
    private int _count;

    @Override
    protected void readImpl()
    {
        this._product_id = readD();
        this._count = readD();
    }

    @Override
    protected void runImpl()
    {
        L2PcInstance player = getClient().getActiveChar();
        if (player != null)
        {
            PrimeShopTable.getInstance().buyItem(player, this._product_id, this._count);
        }
    }

    @Override
    public String getType()
    {
        return TYPE;
    }
}
