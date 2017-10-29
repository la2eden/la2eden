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
package com.la2eden.gameserver.model.entity.clanhall;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.la2eden.commons.database.DatabaseFactory;
import com.la2eden.gameserver.ThreadPoolManager;
import com.la2eden.gameserver.data.sql.impl.ClanTable;
import com.la2eden.gameserver.instancemanager.ClanHallAuctionManager;
import com.la2eden.gameserver.instancemanager.ClanHallManager;
import com.la2eden.gameserver.model.L2Clan;
import com.la2eden.gameserver.model.StatsSet;
import com.la2eden.gameserver.model.entity.ClanHall;
import com.la2eden.gameserver.model.itemcontainer.Inventory;
import com.la2eden.gameserver.network.SystemMessageId;
import com.la2eden.gameserver.network.serverpackets.SystemMessage;

public final class AuctionableHall extends ClanHall
{
	protected long _paidUntil;
	private final int _grade;
	protected boolean _paid;
	private final int _lease;
	private final int _chRate = 604800000;
	
	public AuctionableHall(StatsSet set)
	{
		super(set);
		_paidUntil = set.getLong("paidUntil");
		_grade = set.getInt("grade");
		_paid = set.getBoolean("paid");
		_lease = set.getInt("lease");
		
		if (getOwnerId() != 0)
		{
			_isFree = false;
			initialyzeTask(false);
			loadFunctions();
		}
	}
	
	/**
	 * @return if clanHall is paid or not
	 */
	public final boolean getPaid()
	{
		return _paid;
	}
	
	/** Return lease */
	@Override
	public final int getLease()
	{
		return _lease;
	}
	
	/** Return PaidUntil */
	@Override
	public final long getPaidUntil()
	{
		return _paidUntil;
	}
	
	/** Return Grade */
	@Override
	public final int getGrade()
	{
		return _grade;
	}
	
	@Override
	public final void free()
	{
		super.free();
		_paidUntil = 0;
		_paid = false;
	}
	
	@Override
	public final void setOwner(L2Clan clan)
	{
		super.setOwner(clan);
		_paidUntil = System.currentTimeMillis();
		initialyzeTask(true);
	}
	
	/**
	 * Initialize Fee Task
	 * @param forced
	 */
	private final void initialyzeTask(boolean forced)
	{
		final long currentTime = System.currentTimeMillis();
		if (_paidUntil > currentTime)
		{
			ThreadPoolManager.getInstance().scheduleGeneral(new FeeTask(), _paidUntil - currentTime);
		}
		else if (!_paid && !forced)
		{
			if ((System.currentTimeMillis() + (3600000 * 24)) <= (_paidUntil + _chRate))
			{
				ThreadPoolManager.getInstance().scheduleGeneral(new FeeTask(), System.currentTimeMillis() + (3600000 * 24));
			}
			else
			{
				ThreadPoolManager.getInstance().scheduleGeneral(new FeeTask(), (_paidUntil + _chRate) - System.currentTimeMillis());
			}
		}
		else
		{
			ThreadPoolManager.getInstance().scheduleGeneral(new FeeTask(), 0);
		}
	}
	
	/** Fee Task */
	protected class FeeTask implements Runnable
	{
		private final Logger _log = Logger.getLogger(FeeTask.class.getName());
		
		@Override
		public void run()
		{
			try
			{
				final long _time = System.currentTimeMillis();
				
				if (isFree())
				{
					return;
				}
				
				if (_paidUntil > _time)
				{
					ThreadPoolManager.getInstance().scheduleGeneral(new FeeTask(), _paidUntil - _time);
					return;
				}
				
				final L2Clan Clan = ClanTable.getInstance().getClan(getOwnerId());
				if (ClanTable.getInstance().getClan(getOwnerId()).getWarehouse().getAdena() >= getLease())
				{
					if (_paidUntil != 0)
					{
						while (_paidUntil <= _time)
						{
							_paidUntil += _chRate;
						}
					}
					else
					{
						_paidUntil = _time + _chRate;
					}
					ClanTable.getInstance().getClan(getOwnerId()).getWarehouse().destroyItemByItemId("CH_rental_fee", Inventory.ADENA_ID, getLease(), null, null);
					ThreadPoolManager.getInstance().scheduleGeneral(new FeeTask(), _paidUntil - _time);
					_paid = true;
					updateDb();
				}
				else
				{
					_paid = false;
					if (_time > (_paidUntil + _chRate))
					{
						if (ClanHallManager.getInstance().loaded())
						{
							ClanHallAuctionManager.getInstance().initNPC(getId());
							ClanHallManager.getInstance().setFree(getId());
							Clan.broadcastToOnlineMembers(SystemMessage.getSystemMessage(SystemMessageId.THE_CLAN_HALL_FEE_IS_ONE_WEEK_OVERDUE));
						}
						else
						{
							ThreadPoolManager.getInstance().scheduleGeneral(new FeeTask(), 3000);
						}
					}
					else
					{
						updateDb();
						final SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.PAYMENT_FOR_YOUR_CLAN_HALL_HAS_NOT_BEEN_MADE_PLEASE_MAKE_PAYMENT_TO_YOUR_CLAN_WAREHOUSE_BY_S1_TOMORROW);
						sm.addInt(getLease());
						Clan.broadcastToOnlineMembers(sm);
						if ((_time + (3600000 * 24)) <= (_paidUntil + _chRate))
						{
							ThreadPoolManager.getInstance().scheduleGeneral(new FeeTask(), _time + (3600000 * 24));
						}
						else
						{
							ThreadPoolManager.getInstance().scheduleGeneral(new FeeTask(), (_paidUntil + _chRate) - _time);
						}
					}
				}
			}
			catch (Exception e)
			{
				_log.log(Level.SEVERE, "", e);
			}
		}
	}
	
	@Override
	public final void updateDb()
	{
		try (Connection con = DatabaseFactory.getInstance().getConnection();
			PreparedStatement ps = con.prepareStatement("UPDATE clanhall SET ownerId=?, paidUntil=?, paid=? WHERE id=?"))
		{
			ps.setInt(1, getOwnerId());
			ps.setLong(2, getPaidUntil());
			ps.setInt(3, getPaid() ? 1 : 0);
			ps.setInt(4, getId());
			ps.execute();
		}
		catch (Exception e)
		{
			_log.log(Level.WARNING, "Exception: updateOwnerInDB(L2Clan clan): " + e.getMessage(), e);
		}
	}
}
