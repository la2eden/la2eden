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

import com.la2eden.gameserver.data.xml.impl.EnchantItemData;
import com.la2eden.gameserver.model.actor.instance.L2PcInstance;
import com.la2eden.gameserver.model.items.enchant.EnchantScroll;
import com.la2eden.gameserver.model.items.enchant.EnchantSupportItem;
import com.la2eden.gameserver.model.items.instance.L2ItemInstance;
import com.la2eden.gameserver.network.SystemMessageId;
import com.la2eden.gameserver.network.serverpackets.ExPutEnchantSupportItemResult;

/**
 * @author KenM
 */
public class RequestExTryToPutEnchantSupportItem extends L2GameClientPacket
{
	private static final String _C__D0_4D_REQUESTEXTRYTOPUTENCHANTSUPPORTITEM = "[C] D0:4D RequestExTryToPutEnchantSupportItem";
	
	private int _supportObjectId;
	private int _enchantObjectId;
	
	@Override
	protected void readImpl()
	{
		_supportObjectId = readD();
		_enchantObjectId = readD();
	}
	
	@Override
	protected void runImpl()
	{
		final L2PcInstance activeChar = getClient().getActiveChar();
		if (activeChar == null)
		{
			return;
		}
		
		if (activeChar.isEnchanting())
		{
			final L2ItemInstance item = activeChar.getInventory().getItemByObjectId(_enchantObjectId);
			final L2ItemInstance scroll = activeChar.getInventory().getItemByObjectId(activeChar.getActiveEnchantItemId());
			final L2ItemInstance support = activeChar.getInventory().getItemByObjectId(_supportObjectId);
			
			if ((item == null) || (scroll == null) || (support == null))
			{
				// message may be custom
				activeChar.sendPacket(SystemMessageId.INAPPROPRIATE_ENCHANT_CONDITIONS);
				activeChar.setActiveEnchantSupportItemId(L2PcInstance.ID_NONE);
				return;
			}
			
			final EnchantScroll scrollTemplate = EnchantItemData.getInstance().getEnchantScroll(scroll);
			final EnchantSupportItem supportTemplate = EnchantItemData.getInstance().getSupportItem(support);
			
			if ((scrollTemplate == null) || (supportTemplate == null) || !scrollTemplate.isValid(item, supportTemplate))
			{
				// message may be custom
				activeChar.sendPacket(SystemMessageId.INAPPROPRIATE_ENCHANT_CONDITIONS);
				activeChar.setActiveEnchantSupportItemId(L2PcInstance.ID_NONE);
				activeChar.sendPacket(new ExPutEnchantSupportItemResult(0));
				return;
			}
			activeChar.setActiveEnchantSupportItemId(support.getObjectId());
			activeChar.sendPacket(new ExPutEnchantSupportItemResult(_supportObjectId));
		}
	}
	
	@Override
	public String getType()
	{
		return _C__D0_4D_REQUESTEXTRYTOPUTENCHANTSUPPORTITEM;
	}
}
