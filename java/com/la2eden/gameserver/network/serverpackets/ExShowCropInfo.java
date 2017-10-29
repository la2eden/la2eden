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

import java.util.List;

import com.la2eden.gameserver.instancemanager.CastleManorManager;
import com.la2eden.gameserver.model.CropProcure;
import com.la2eden.gameserver.model.L2Seed;

/**
 * @author l3x
 */
public class ExShowCropInfo extends L2GameServerPacket
{
	private final List<CropProcure> _crops;
	private final int _manorId;
	private final boolean _hideButtons;
	
	public ExShowCropInfo(int manorId, boolean nextPeriod, boolean hideButtons)
	{
		_manorId = manorId;
		_hideButtons = hideButtons;
		
		final CastleManorManager manor = CastleManorManager.getInstance();
		_crops = (nextPeriod && !manor.isManorApproved()) ? null : manor.getCropProcure(manorId, nextPeriod);
	}
	
	@Override
	protected void writeImpl()
	{
		writeC(0xFE); // Id
		writeH(0x24); // SubId
		writeC(_hideButtons ? 0x01 : 0x00); // Hide "Crop Sales" button
		writeD(_manorId); // Manor ID
		writeD(0x00);
		if (_crops == null)
		{
			writeD(0);
			return;
		}
		writeD(_crops.size());
		for (CropProcure crop : _crops)
		{
			writeD(crop.getId()); // Crop id
			writeQ(crop.getAmount()); // Buy residual
			writeQ(crop.getStartAmount()); // Buy
			writeQ(crop.getPrice()); // Buy price
			writeC(crop.getReward()); // Reward
			final L2Seed seed = CastleManorManager.getInstance().getSeedByCrop(crop.getId());
			if (seed == null)
			{
				writeD(0); // Seed level
				writeC(0x01); // Reward 1
				writeD(0); // Reward 1 - item id
				writeC(0x01); // Reward 2
				writeD(0); // Reward 2 - item id
			}
			else
			{
				writeD(seed.getLevel()); // Seed level
				writeC(0x01); // Reward 1
				writeD(seed.getReward(1)); // Reward 1 - item id
				writeC(0x01); // Reward 2
				writeD(seed.getReward(2)); // Reward 2 - item id
			}
		}
	}
}