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

import com.la2eden.gameserver.model.actor.instance.L2PcInstance;

public class ExBrGamePoint extends L2GameServerPacket {
    private final int _charId;
    private final int _charPoints;

    public ExBrGamePoint(L2PcInstance player)
    {
        _charId = player.getObjectId();
        _charPoints = ((int) player.getPrimePoints());
    }

    @Override
    protected final void writeImpl()
    {
        writeC(254);
        writeH(213);
        writeD(_charId);
        writeQ(_charPoints);
        writeD(0);
    }

    public String getType()
    {
        return "[S] FE:D5 ExBRGamePoint";
    }
}
