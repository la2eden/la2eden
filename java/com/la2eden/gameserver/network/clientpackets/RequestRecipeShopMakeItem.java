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

import com.la2eden.gameserver.RecipeController;
import com.la2eden.gameserver.enums.PrivateStoreType;
import com.la2eden.gameserver.model.L2World;
import com.la2eden.gameserver.model.actor.instance.L2PcInstance;
import com.la2eden.gameserver.util.Util;

/**
 * @author Administrator
 */
public final class RequestRecipeShopMakeItem extends L2GameClientPacket
{
	private static final String _C__BF_REQUESTRECIPESHOPMAKEITEM = "[C] BF RequestRecipeShopMakeItem";
	
	private int _id;
	private int _recipeId;
	@SuppressWarnings("unused")
	private long _unknown;
	
	@Override
	protected void readImpl()
	{
		_id = readD();
		_recipeId = readD();
		_unknown = readQ();
	}
	
	@Override
	protected void runImpl()
	{
		final L2PcInstance activeChar = getClient().getActiveChar();
		if (activeChar == null)
		{
			return;
		}
		
		if (!getClient().getFloodProtectors().getManufacture().tryPerformAction("RecipeShopMake"))
		{
			return;
		}
		
		final L2PcInstance manufacturer = L2World.getInstance().getPlayer(_id);
		if (manufacturer == null)
		{
			return;
		}
		
		if ((manufacturer.getInstanceId() != activeChar.getInstanceId()) && (activeChar.getInstanceId() != -1))
		{
			return;
		}
		
		if (activeChar.getPrivateStoreType() != PrivateStoreType.NONE)
		{
			activeChar.sendMessage("You cannot create items while trading.");
			return;
		}
		if (manufacturer.getPrivateStoreType() != PrivateStoreType.MANUFACTURE)
		{
			// activeChar.sendMessage("You cannot create items while trading.");
			return;
		}
		
		if (activeChar.isInCraftMode() || manufacturer.isInCraftMode())
		{
			activeChar.sendMessage("You are currently in Craft Mode.");
			return;
		}
		if (Util.checkIfInRange(150, activeChar, manufacturer, true))
		{
			RecipeController.getInstance().requestManufactureItem(manufacturer, _recipeId, activeChar);
		}
	}
	
	@Override
	public String getType()
	{
		return _C__BF_REQUESTRECIPESHOPMAKEITEM;
	}
}
