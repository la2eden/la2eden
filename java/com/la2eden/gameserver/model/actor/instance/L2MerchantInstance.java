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
package com.la2eden.gameserver.model.actor.instance;

import com.la2eden.gameserver.data.xml.impl.BuyListData;
import com.la2eden.gameserver.datatables.MerchantPriceConfigTable;
import com.la2eden.gameserver.datatables.MerchantPriceConfigTable.MerchantPriceConfig;
import com.la2eden.gameserver.enums.InstanceType;
import com.la2eden.gameserver.model.actor.templates.L2NpcTemplate;
import com.la2eden.gameserver.model.buylist.L2BuyList;
import com.la2eden.gameserver.network.serverpackets.ActionFailed;
import com.la2eden.gameserver.network.serverpackets.BuyList;
import com.la2eden.gameserver.network.serverpackets.ExBuySellList;

/**
 * This class ...
 * @version $Revision: 1.10.4.9 $ $Date: 2005/04/11 10:06:08 $
 */
public class L2MerchantInstance extends L2NpcInstance
{
	private MerchantPriceConfig _mpc;
	
	/**
	 * Creates a merchant,
	 * @param template the merchant NPC template
	 */
	public L2MerchantInstance(L2NpcTemplate template)
	{
		super(template);
		setInstanceType(InstanceType.L2MerchantInstance);
	}
	
	@Override
	public void onSpawn()
	{
		super.onSpawn();
		_mpc = MerchantPriceConfigTable.getInstance().getMerchantPriceConfig(this);
	}
	
	@Override
	public String getHtmlPath(int npcId, int val)
	{
		String pom = "";
		
		if (val == 0)
		{
			pom = "" + npcId;
		}
		else
		{
			pom = npcId + "-" + val;
		}
		
		return "./datapack/html/merchant/" + pom + ".htm";
	}
	
	/**
	 * @return Returns the mpc.
	 */
	public MerchantPriceConfig getMpc()
	{
		return _mpc;
	}
	
	public final void showBuyWindow(L2PcInstance player, int val)
	{
		showBuyWindow(player, val, true);
	}
	
	public final void showBuyWindow(L2PcInstance player, int val, boolean applyTax)
	{
		final L2BuyList buyList = BuyListData.getInstance().getBuyList(val);
		if (buyList == null)
		{
			_log.warning("BuyList not found! BuyListId:" + val);
			player.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		if (!buyList.isNpcAllowed(getId()))
		{
			_log.warning("Npc not allowed in BuyList! BuyListId:" + val + " NpcId:" + getId());
			player.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		final double taxRate = (applyTax) ? getMpc().getTotalTaxRate() : 0;
		
		player.setInventoryBlockingStatus(true);
		
		player.sendPacket(new BuyList(buyList, player.getAdena(), taxRate));
		player.sendPacket(new ExBuySellList(player, false));
		player.sendPacket(ActionFailed.STATIC_PACKET);
	}
}
