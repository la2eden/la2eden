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

import com.la2eden.gameserver.datatables.PrimeShopTable.PrimeShopItem;

public class ExBrProductInfo extends L2GameServerPacket {
    private final int _brId;
    private final int _price;
    private final int _cat;
    private final int _itemId;
    private final int _count;
    private final int _weight;
    private final int _tradable;

    public ExBrProductInfo(int brId, PrimeShopItem item)
    {
        _brId = brId;
        _price = item.getPrimeItemPrice();
        _cat = item.getPrimeItemCat();
        _itemId = item.getPrimeItemId();
        _count = item.getPrimeItemCount();
        _weight = item.getPrimeWeight();
        _tradable = item.getPrimeTradable();
    }

    @Override
    protected final void writeImpl()
    {
        writeC(254);
        writeH(215);
        writeD(_brId);
        writeD(_price);
        writeD(_cat);
        writeD(_itemId);
        writeD(_count);
        writeD(_weight);
        writeD(_tradable);
    }

    public String getType()
    {
        return "[S] FE:D7 ExBRProductInfo";
    }
}
