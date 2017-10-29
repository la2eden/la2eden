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
import com.la2eden.gameserver.data.xml.impl.AdminData;
import com.la2eden.gameserver.enums.PrivateStoreType;
import com.la2eden.gameserver.model.PcCondOverride;
import com.la2eden.gameserver.model.actor.instance.L2PcInstance;
import com.la2eden.gameserver.model.itemcontainer.Inventory;
import com.la2eden.gameserver.model.items.L2Item;
import com.la2eden.gameserver.model.items.instance.L2ItemInstance;
import com.la2eden.gameserver.model.items.type.EtcItemType;
import com.la2eden.gameserver.model.zone.ZoneId;
import com.la2eden.gameserver.network.SystemMessageId;
import com.la2eden.gameserver.network.serverpackets.InventoryUpdate;
import com.la2eden.gameserver.network.serverpackets.ItemList;
import com.la2eden.gameserver.util.GMAudit;
import com.la2eden.gameserver.util.Util;

/**
 * This class ...
 * @version $Revision: 1.11.2.1.2.7 $ $Date: 2005/04/02 21:25:21 $
 */
public final class RequestDropItem extends L2GameClientPacket
{
	private static final String _C__17_REQUESTDROPITEM = "[C] 17 RequestDropItem";
	
	private int _objectId;
	private long _count;
	private int _x;
	private int _y;
	private int _z;
	
	@Override
	protected void readImpl()
	{
		_objectId = readD();
		_count = readQ();
		_x = readD();
		_y = readD();
		_z = readD();
	}
	
	@Override
	protected void runImpl()
	{
		final L2PcInstance activeChar = getClient().getActiveChar();
		if ((activeChar == null) || activeChar.isDead())
		{
			return;
		}
		// Flood protect drop to avoid packet lag
		if (!getClient().getFloodProtectors().getDropItem().tryPerformAction("drop item"))
		{
			return;
		}
		
		final L2ItemInstance item = activeChar.getInventory().getItemByObjectId(_objectId);
		
		if ((item == null) || (_count == 0) || !activeChar.validateItemManipulation(_objectId, "drop") || (!Config.ALLOW_DISCARDITEM && !activeChar.canOverrideCond(PcCondOverride.DROP_ALL_ITEMS)) || (!item.isDropable() && !(activeChar.canOverrideCond(PcCondOverride.DROP_ALL_ITEMS) && Config.GM_TRADE_RESTRICTED_ITEMS)) || ((item.getItemType() == EtcItemType.PET_COLLAR) && activeChar.havePetInvItems()) || activeChar.isInsideZone(ZoneId.NO_ITEM_DROP))
		{
			activeChar.sendPacket(SystemMessageId.THIS_ITEM_CANNOT_BE_DISCARDED);
			return;
		}
		if (item.isQuestItem() && !(activeChar.canOverrideCond(PcCondOverride.DROP_ALL_ITEMS) && Config.GM_TRADE_RESTRICTED_ITEMS))
		{
			return;
		}
		
		if (_count > item.getCount())
		{
			activeChar.sendPacket(SystemMessageId.THIS_ITEM_CANNOT_BE_DISCARDED);
			return;
		}
		
		if ((Config.PLAYER_SPAWN_PROTECTION > 0) && activeChar.isInvul() && !activeChar.isGM())
		{
			activeChar.sendPacket(SystemMessageId.THIS_ITEM_CANNOT_BE_DISCARDED);
			return;
		}
		
		if (_count < 0)
		{
			Util.handleIllegalPlayerAction(activeChar, "[RequestDropItem] Character " + activeChar.getName() + " of account " + activeChar.getAccountName() + " tried to drop item with oid " + _objectId + " but has count < 0!", Config.DEFAULT_PUNISH);
			return;
		}
		
		if (!item.isStackable() && (_count > 1))
		{
			Util.handleIllegalPlayerAction(activeChar, "[RequestDropItem] Character " + activeChar.getName() + " of account " + activeChar.getAccountName() + " tried to drop non-stackable item with oid " + _objectId + " but has count > 1!", Config.DEFAULT_PUNISH);
			return;
		}
		
		if (Config.JAIL_DISABLE_TRANSACTION && activeChar.isJailed())
		{
			activeChar.sendMessage("You cannot drop items in Jail.");
			return;
		}
		
		if (!activeChar.getAccessLevel().allowTransaction())
		{
			activeChar.sendMessage("Transactions are disabled for your Access Level.");
			activeChar.sendPacket(SystemMessageId.NOTHING_HAPPENED);
			return;
		}
		
		if (activeChar.isProcessingTransaction() || (activeChar.getPrivateStoreType() != PrivateStoreType.NONE))
		{
			activeChar.sendPacket(SystemMessageId.WHILE_OPERATING_A_PRIVATE_STORE_OR_WORKSHOP_YOU_CANNOT_DISCARD_DESTROY_OR_TRADE_AN_ITEM);
			return;
		}
		if (activeChar.isFishing())
		{
			// You can't mount, dismount, break and drop items while fishing
			activeChar.sendPacket(SystemMessageId.YOU_CANNOT_DO_THAT_WHILE_FISHING2);
			return;
		}
		if (activeChar.isFlying())
		{
			return;
		}
		
		// Cannot discard item that the skill is consuming
		if (activeChar.isCastingNow())
		{
			if ((activeChar.getCurrentSkill() != null) && (activeChar.getCurrentSkill().getSkill().getItemConsumeId() == item.getId()))
			{
				activeChar.sendPacket(SystemMessageId.THIS_ITEM_CANNOT_BE_DISCARDED);
				return;
			}
		}
		
		// Cannot discard item that the skill is consuming
		if (activeChar.isCastingSimultaneouslyNow())
		{
			if ((activeChar.getLastSimultaneousSkillCast() != null) && (activeChar.getLastSimultaneousSkillCast().getItemConsumeId() == item.getId()))
			{
				activeChar.sendPacket(SystemMessageId.THIS_ITEM_CANNOT_BE_DISCARDED);
				return;
			}
		}
		
		if ((L2Item.TYPE2_QUEST == item.getItem().getType2()) && !activeChar.canOverrideCond(PcCondOverride.DROP_ALL_ITEMS))
		{
			if (Config.DEBUG)
			{
				_log.finest(activeChar.getObjectId() + ":player tried to drop quest item");
			}
			activeChar.sendPacket(SystemMessageId.THAT_ITEM_CANNOT_BE_DISCARDED_OR_EXCHANGED);
			return;
		}
		
		if (!activeChar.isInsideRadius(_x, _y, 0, 150, false, false) || (Math.abs(_z - activeChar.getZ()) > 50))
		{
			if (Config.DEBUG)
			{
				_log.finest(activeChar.getObjectId() + ": trying to drop too far away");
			}
			activeChar.sendPacket(SystemMessageId.YOU_CANNOT_DISCARD_SOMETHING_THAT_FAR_AWAY_FROM_YOU);
			return;
		}
		
		if (!activeChar.getInventory().canManipulateWithItemId(item.getId()))
		{
			activeChar.sendMessage("You cannot use this item.");
			return;
		}
		
		if (Config.DEBUG)
		{
			_log.fine("requested drop item " + _objectId + "(" + item.getCount() + ") at " + _x + "/" + _y + "/" + _z);
		}
		
		if (item.isEquipped())
		{
			final L2ItemInstance[] unequiped = activeChar.getInventory().unEquipItemInSlotAndRecord(item.getLocationSlot());
			final InventoryUpdate iu = new InventoryUpdate();
			for (L2ItemInstance itm : unequiped)
			{
				itm.unChargeAllShots();
				iu.addModifiedItem(itm);
			}
			activeChar.sendPacket(iu);
			activeChar.broadcastUserInfo();
			
			final ItemList il = new ItemList(activeChar, true);
			activeChar.sendPacket(il);
		}
		
		final L2ItemInstance dropedItem = activeChar.dropItem("Drop", _objectId, _count, _x, _y, _z, null, false, false);
		
		if (Config.DEBUG)
		{
			_log.fine("dropping " + _objectId + " item(" + _count + ") at: " + _x + " " + _y + " " + _z);
		}
		
		// activeChar.broadcastUserInfo();
		
		if (activeChar.isGM())
		{
			final String target = (activeChar.getTarget() != null ? activeChar.getTarget().getName() : "no-target");
			GMAudit.auditGMAction(activeChar.getName() + " [" + activeChar.getObjectId() + "]", "Drop", target, "(id: " + dropedItem.getId() + " name: " + dropedItem.getItemName() + " objId: " + dropedItem.getObjectId() + " x: " + activeChar.getX() + " y: " + activeChar.getY() + " z: " + activeChar.getZ() + ")");
		}
		
		if ((dropedItem != null) && (dropedItem.getId() == Inventory.ADENA_ID) && (dropedItem.getCount() >= 1000000))
		{
			final String msg = "Character (" + activeChar.getName() + ") has dropped (" + dropedItem.getCount() + ")adena at (" + _x + "," + _y + "," + _z + ")";
			_log.warning(msg);
			AdminData.getInstance().broadcastMessageToGMs(msg);
		}
	}
	
	@Override
	public String getType()
	{
		return _C__17_REQUESTDROPITEM;
	}
	
	@Override
	protected boolean triggersOnActionRequest()
	{
		return false;
	}
}
