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
package com.la2eden.gameserver.network.clientpackets;

import com.la2eden.Config;
import com.la2eden.gameserver.enums.PrivateStoreType;
import com.la2eden.gameserver.model.actor.instance.L2PcInstance;
import com.la2eden.gameserver.model.items.instance.L2ItemInstance;
import com.la2eden.gameserver.network.SystemMessageId;
import com.la2eden.gameserver.network.serverpackets.ExAutoSoulShot;
import com.la2eden.gameserver.network.serverpackets.SystemMessage;

/**
 * This class ...
 * @version $Revision: 1.0.0.0 $ $Date: 2005/07/11 15:29:30 $
 */
public final class RequestAutoSoulShot extends L2GameClientPacket
{
	private static final String _C__D0_0D_REQUESTAUTOSOULSHOT = "[C] D0:0D RequestAutoSoulShot";
	
	// format cd
	private int _itemId;
	private int _type; // 1 = on : 0 = off;
	
	@Override
	protected void readImpl()
	{
		_itemId = readD();
		_type = readD();
	}
	
	@Override
	protected void runImpl()
	{
		final L2PcInstance activeChar = getClient().getActiveChar();
		if (activeChar == null)
		{
			return;
		}
		
		if ((activeChar.getPrivateStoreType() == PrivateStoreType.NONE) && (activeChar.getActiveRequester() == null) && !activeChar.isDead())
		{
			if (Config.DEBUG)
			{
				_log.fine("AutoSoulShot:" + _itemId);
			}
			
			final L2ItemInstance item = activeChar.getInventory().getItemByItemId(_itemId);
			if (item == null)
			{
				return;
			}
			
			if (_type == 1)
			{
				if (!activeChar.getInventory().canManipulateWithItemId(item.getId()))
				{
					activeChar.sendMessage("Cannot use this item.");
					return;
				}
				
				// Fishingshots are not automatic on retail
				if ((_itemId < 6535) || (_itemId > 6540))
				{
					// Attempt to charge first shot on activation
					if ((_itemId == 6645) || (_itemId == 6646) || (_itemId == 6647) || (_itemId == 20332) || (_itemId == 20333) || (_itemId == 20334))
					{
						if (activeChar.hasSummon())
						{
							if (item.getEtcItem().getHandlerName().equals("BeastSoulShot"))
							{
								if (activeChar.getSummon().getSoulShotsPerHit() > item.getCount())
								{
									activeChar.sendPacket(SystemMessageId.YOU_DON_T_HAVE_ENOUGH_SOULSHOTS_NEEDED_FOR_A_PET_SERVITOR);
									return;
								}
							}
							else
							{
								if (activeChar.getSummon().getSpiritShotsPerHit() > item.getCount())
								{
									activeChar.sendPacket(SystemMessageId.YOU_DON_T_HAVE_ENOUGH_SOULSHOTS_NEEDED_FOR_A_PET_SERVITOR);
									return;
								}
							}
							activeChar.addAutoSoulShot(_itemId);
							activeChar.sendPacket(new ExAutoSoulShot(_itemId, _type));
							
							// start the auto soulshot use
							final SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.THE_AUTOMATIC_USE_OF_S1_HAS_BEEN_ACTIVATED);
							sm.addItemName(item);
							activeChar.sendPacket(sm);
							
							activeChar.rechargeShots(true, true);
							activeChar.getSummon().rechargeShots(true, true);
							
						}
						else
						{
							activeChar.sendPacket(SystemMessageId.YOU_DO_NOT_HAVE_A_SERVITOR_OR_PET_AND_THEREFORE_CANNOT_USE_THE_AUTOMATIC_USE_FUNCTION);
						}
					}
					else
					{
						if ((activeChar.getActiveWeaponItem() != activeChar.getFistsWeaponItem()) && (item.getItem().getCrystalType() == activeChar.getActiveWeaponItem().getItemGradeSPlus()))
						{
							activeChar.addAutoSoulShot(_itemId);
							activeChar.sendPacket(new ExAutoSoulShot(_itemId, _type));
						}
						else
						{
							if (((_itemId >= 2509) && (_itemId <= 2514)) || ((_itemId >= 3947) && (_itemId <= 3952)) || (_itemId == 5790) || ((_itemId >= 22072) && (_itemId <= 22081)))
							{
								activeChar.sendPacket(SystemMessageId.YOUR_SPIRITSHOT_DOES_NOT_MATCH_THE_WEAPON_S_GRADE);
							}
							else
							{
								activeChar.sendPacket(SystemMessageId.THE_SOULSHOT_YOU_ARE_ATTEMPTING_TO_USE_DOES_NOT_MATCH_THE_GRADE_OF_YOUR_EQUIPPED_WEAPON);
							}
							
							activeChar.addAutoSoulShot(_itemId);
							activeChar.sendPacket(new ExAutoSoulShot(_itemId, _type));
						}
						
						// start the auto soulshot use
						final SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.THE_AUTOMATIC_USE_OF_S1_HAS_BEEN_ACTIVATED);
						sm.addItemName(item);
						activeChar.sendPacket(sm);
						
						activeChar.rechargeShots(true, true);
					}
				}
			}
			else if (_type == 0)
			{
				activeChar.removeAutoSoulShot(_itemId);
				activeChar.sendPacket(new ExAutoSoulShot(_itemId, _type));
				
				// cancel the auto soulshot use
				final SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.THE_AUTOMATIC_USE_OF_S1_HAS_BEEN_DEACTIVATED);
				sm.addItemName(item);
				activeChar.sendPacket(sm);
			}
		}
	}
	
	@Override
	public String getType()
	{
		return _C__D0_0D_REQUESTAUTOSOULSHOT;
	}
	
	@Override
	protected boolean triggersOnActionRequest()
	{
		return false;
	}
}