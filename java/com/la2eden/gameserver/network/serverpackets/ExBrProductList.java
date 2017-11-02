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
package com.la2eden.gameserver.network.serverpackets;

import com.la2eden.gameserver.datatables.PrimeShopTable;
import com.la2eden.gameserver.datatables.PrimeShopTable.PrimeShopItem;

import java.util.Map;

public class ExBrProductList extends L2GameServerPacket {
    private final Map<Integer, PrimeShopItem> primeList;

    public ExBrProductList()
    {
        primeList = PrimeShopTable.getInstance().getPrimeItems();
    }

    @Override
    protected final void writeImpl()
    {
        writeC(254);
        writeH(214);
        writeH(primeList.size());
        writeH(0);
        for (Map.Entry<Integer, PrimeShopTable.PrimeShopItem> entrySet : primeList.entrySet())
        {
            PrimeShopTable.PrimeShopItem item = entrySet.getValue();

            writeD(entrySet.getKey().intValue());
            writeH(item.getPrimeItemCat());
            writeD(item.getPrimeItemPrice());
            writeD(item.getPrimeType());
            writeD(0);
            writeD(0);
            writeC(0);
            writeC(0);
            writeC(0);
            writeC(0);
            writeC(0);
            writeD(0);
            writeD(0);
        }
    }

    public String getType()
    {
        return "[S] FE:D6 ExBRProductList";
    }
}
