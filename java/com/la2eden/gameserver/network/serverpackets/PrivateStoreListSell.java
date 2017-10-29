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

import com.la2eden.gameserver.model.TradeItem;
import com.la2eden.gameserver.model.actor.instance.L2PcInstance;

public class PrivateStoreListSell extends AbstractItemPacket
{
	private final int _objId;
	private final long _playerAdena;
	private final boolean _packageSale;
	private final TradeItem[] _items;
	
	public PrivateStoreListSell(L2PcInstance player, L2PcInstance storePlayer)
	{
		_objId = storePlayer.getObjectId();
		_playerAdena = player.getAdena();
		_items = storePlayer.getSellList().getItems();
		_packageSale = storePlayer.getSellList().isPackaged();
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0xA1);
		writeD(_objId);
		writeD(_packageSale ? 1 : 0);
		writeQ(_playerAdena);
		writeD(_items.length);
		for (TradeItem item : _items)
		{
			writeItem(item);
			writeQ(item.getPrice());
			writeQ(item.getItem().getReferencePrice() * 2);
		}
	}
}
