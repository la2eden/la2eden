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
package com.la2eden.gameserver.data.sql.impl;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.la2eden.commons.database.DatabaseFactory;
import com.la2eden.gameserver.model.L2Territory;
import com.la2eden.gameserver.model.Location;

/**
 * @author Balancer, Mr
 */
public class TerritoryTable
{
	private static final Logger LOGGER = Logger.getLogger(TerritoryTable.class.getName());
	
	private static final Map<Integer, L2Territory> _territory = new HashMap<>();
	
	/**
	 * Instantiates a new territory.
	 */
	protected TerritoryTable()
	{
		load();
	}
	
	/**
	 * Gets the random point.
	 * @param terr the territory Id?
	 * @return the random point
	 */
	public Location getRandomPoint(int terr)
	{
		return _territory.get(terr).getRandomPoint();
	}
	
	/**
	 * Gets the proc max.
	 * @param terr the territory Id?
	 * @return the proc max
	 */
	public int getProcMax(int terr)
	{
		return _territory.get(terr).getProcMax();
	}
	
	/**
	 * Load the data from database.
	 */
	public void load()
	{
		_territory.clear();
		try (Connection con = DatabaseFactory.getInstance().getConnection();
			Statement stmt = con.createStatement();
			ResultSet rset = stmt.executeQuery("SELECT * FROM locations WHERE loc_id>0"))
		{
			while (rset.next())
			{
				final int terrId = rset.getInt("loc_id");
				L2Territory terr = _territory.get(terrId);
				if (terr == null)
				{
					terr = new L2Territory(terrId);
					_territory.put(terrId, terr);
				}
				terr.add(rset.getInt("loc_x"), rset.getInt("loc_y"), rset.getInt("loc_zmin"), rset.getInt("loc_zmax"), rset.getInt("proc"));
			}
			LOGGER.info("TerritoryTable: Loaded " + _territory.size() + " territories from database.");
		}
		catch (SQLException e)
		{
			LOGGER.log(Level.SEVERE, "TerritoryTable: Failed to load territories from database!", e);
		}
	}
	
	/**
	 * Gets the single instance of Territory.
	 * @return single instance of Territory
	 */
	public static TerritoryTable getInstance()
	{
		return SingletonHolder._instance;
	}
	
	private static class SingletonHolder
	{
		protected static final TerritoryTable _instance = new TerritoryTable();
	}
}
