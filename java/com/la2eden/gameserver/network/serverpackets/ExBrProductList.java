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

public class ExBrProductList extends L2GameServerPacket
{
    private static final String TYPE = "[S] FE:D6 ExBRProductList";

    private final Map<Integer, PrimeShopItem> primeList;

    public ExBrProductList()
    {
        primeList = PrimeShopTable.getInstance().getPrimeItems();
    }

    @Override
    protected final void writeImpl()
    {
        writeC(0xFE); // 254
        writeH(0xD6); // 214
        writeH(primeList.size());
        // writeH(0);

        for (Map.Entry<Integer, PrimeShopTable.PrimeShopItem> entrySet : primeList.entrySet())
        {
            PrimeShopTable.PrimeShopItem item = entrySet.getValue();

            if ((System.currentTimeMillis() >= item.sale_start_date()) && (System.currentTimeMillis() <= item.sale_end_date()))
            {
                int brId = entrySet.getKey();

                writeD(brId); // product id
                writeH(item.getPrimeItemCat()); // category 1 - enchant 2 - supplies 3 - decoration 4 - package 5 - other
                writeD(item.getPrimeItemPrice()); // points
                writeD(item.getPrimeType());// show tab 2-th group - 1 ?????????? ?????? ??? ???? 1 - event 2 - best 3 - event & best
                writeD((int) (item.sale_start_date() / 1000)); // start sale
                writeD((int) (item.sale_end_date() / 1000)); // end sale
                writeC(1); // day week... needs checking... (0x00)
                writeC(item.getStartHour()); // start hour
                writeC(item.getStartMin()); // start min
                writeC(item.getEndHour()); // end hour
                writeC(item.getEndMin()); // end min

                writeD(PrimeShopTable.PrimeShopHelper.getSoldCount(brId)); // current stock
                writeD(PrimeShopTable.PrimeShopHelper.getMaxStock(brId)); // max stock
            }
        }
    }

    public String getType()
    {
        return TYPE;
    }
}
