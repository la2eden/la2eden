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

import com.la2eden.Config;
import com.la2eden.gameserver.RecipeController;
import com.la2eden.gameserver.model.actor.instance.L2PcInstance;
import com.la2eden.gameserver.network.SystemMessageId;

public final class RequestRecipeBookOpen extends L2GameClientPacket
{
	private static final String _C__B5_REQUESTRECIPEBOOKOPEN = "[C] B5 RequestRecipeBookOpen";
	
	private boolean _isDwarvenCraft;
	
	@Override
	protected void readImpl()
	{
		_isDwarvenCraft = (readD() == 0);
		if (Config.DEBUG)
		{
			_log.info("RequestRecipeBookOpen : " + (_isDwarvenCraft ? "dwarvenCraft" : "commonCraft"));
		}
	}
	
	@Override
	protected void runImpl()
	{
		final L2PcInstance activeChar = getClient().getActiveChar();
		if (activeChar == null)
		{
			return;
		}
		
		if (activeChar.isCastingNow() || activeChar.isCastingSimultaneouslyNow())
		{
			activeChar.sendPacket(SystemMessageId.YOUR_RECIPE_BOOK_MAY_NOT_BE_ACCESSED_WHILE_USING_A_SKILL);
			return;
		}
		
		if (activeChar.getActiveRequester() != null)
		{
			activeChar.sendMessage("You may not alter your recipe book while trading.");
			return;
		}
		
		RecipeController.getInstance().requestBookOpen(activeChar, _isDwarvenCraft);
	}
	
	@Override
	public String getType()
	{
		return _C__B5_REQUESTRECIPEBOOKOPEN;
	}
}
