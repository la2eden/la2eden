/*
 * This file is part of the L2J Mobius project.
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

import com.la2eden.gameserver.enums.PrivateStoreType;
import com.la2eden.gameserver.model.actor.instance.L2PcInstance;
import com.la2eden.gameserver.network.serverpackets.ActionFailed;
import com.la2eden.gameserver.network.serverpackets.RecipeShopManageList;

public final class RequestRecipeShopManageList extends L2GameClientPacket
{
	private static final String _C__B9_RequestRecipeShopManageList = "[C] B9 RequestRecipeShopManageList";
	
	@Override
	protected void readImpl()
	{
		// trigger
	}
	
	@Override
	protected void runImpl()
	{
		final L2PcInstance player = getActiveChar();
		if (player == null)
		{
			return;
		}
		
		// Player shouldn't be able to set stores if he/she is alike dead (dead or fake death)
		if (player.isAlikeDead())
		{
			sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		if (player.getPrivateStoreType() != PrivateStoreType.NONE)
		{
			player.setPrivateStoreType(PrivateStoreType.NONE);
			player.broadcastUserInfo();
			if (player.isSitting())
			{
				player.standUp();
			}
		}
		
		player.sendPacket(new RecipeShopManageList(player, true));
	}
	
	@Override
	public String getType()
	{
		return _C__B9_RequestRecipeShopManageList;
	}
}
