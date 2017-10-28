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
package com.la2eden.gameserver.instancemanager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Calendar;

import com.la2eden.Config;
import com.la2eden.commons.database.DatabaseFactory;
import com.la2eden.gameserver.model.L2World;
import com.la2eden.gameserver.model.actor.instance.L2PcInstance;

/**
 * @author Mobius
 */
public class PremiumManager
{
	private long endDate = 0;
	
	public long getPremiumEndDate(String accountName)
	{
		try (Connection con = DatabaseFactory.getInstance().getConnection())
		{
			final PreparedStatement statement = con.prepareStatement("SELECT premium_service,enddate FROM account_premium WHERE account_name=?");
			statement.setString(1, accountName);
			final ResultSet rset = statement.executeQuery();
			while (rset.next())
			{
				if (Config.PREMIUM_SYSTEM_ENABLED)
				{
					endDate = rset.getLong("enddate");
					if (endDate <= System.currentTimeMillis())
					{
						endDate = 0;
						removePremiumStatus(accountName);
					}
				}
			}
			statement.close();
		}
		catch (Exception e)
		{
		}
		
		return endDate;
	}
	
	public void updatePremiumData(int months, String accountName)
	{
		long remainingTime = getPremiumEndDate(accountName);
		if (remainingTime > 0)
		{
			remainingTime -= System.currentTimeMillis();
		}
		
		try (Connection con = DatabaseFactory.getInstance().getConnection())
		{
			final Calendar endDate = Calendar.getInstance();
			endDate.setTimeInMillis(System.currentTimeMillis() + remainingTime);
			endDate.set(Calendar.SECOND, 0);
			endDate.add(Calendar.MONTH, months);
			
			final PreparedStatement statement = con.prepareStatement("UPDATE account_premium SET premium_service=?,enddate=? WHERE account_name=?");
			statement.setInt(1, 1);
			statement.setLong(2, endDate.getTimeInMillis());
			statement.setString(3, accountName);
			statement.execute();
			statement.close();
		}
		catch (SQLException e)
		{
		}
		
		for (L2PcInstance player : L2World.getInstance().getPlayers())
		{
			if (player.getAccountNamePlayer().equalsIgnoreCase(accountName))
			{
				player.setPremiumStatus(getPremiumEndDate(accountName) > 0);
			}
		}
	}
	
	public void removePremiumStatus(String accountName)
	{
		// TODO: Add check if account exists. XD
		try (Connection con = DatabaseFactory.getInstance().getConnection())
		{
			final PreparedStatement statement = con.prepareStatement("INSERT INTO account_premium (account_name,premium_service,enddate) values(?,?,?) ON DUPLICATE KEY UPDATE premium_service = ?, enddate = ?");
			statement.setString(1, accountName);
			statement.setInt(2, 0);
			statement.setLong(3, 0);
			statement.setInt(4, 0);
			statement.setLong(5, 0);
			statement.execute();
			statement.close();
		}
		catch (SQLException e)
		{
		}
		
		for (L2PcInstance player : L2World.getInstance().getPlayers())
		{
			if (player.getAccountNamePlayer().equalsIgnoreCase(accountName))
			{
				player.setPremiumStatus(false);
			}
		}
	}
	
	public static PremiumManager getInstance()
	{
		return SingletonHolder._instance;
	}
	
	private static class SingletonHolder
	{
		protected static final PremiumManager _instance = new PremiumManager();
	}
}