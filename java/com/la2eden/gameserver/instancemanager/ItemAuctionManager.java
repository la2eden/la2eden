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
package com.la2eden.gameserver.instancemanager;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import com.la2eden.Config;
import com.la2eden.commons.database.DatabaseFactory;
import com.la2eden.gameserver.model.itemauction.ItemAuctionInstance;

/**
 * @author Forsaiken
 */
public final class ItemAuctionManager
{
	private static final Logger _log = Logger.getLogger(ItemAuctionManager.class.getName());
	
	private final Map<Integer, ItemAuctionInstance> _managerInstances = new HashMap<>();
	private final AtomicInteger _auctionIds;
	
	protected ItemAuctionManager()
	{
		_auctionIds = new AtomicInteger(1);
		
		if (!Config.ALT_ITEM_AUCTION_ENABLED)
		{
			_log.log(Level.INFO, getClass().getSimpleName() + ": Disabled by config.");
			return;
		}
		
		try (Connection con = DatabaseFactory.getInstance().getConnection();
			Statement s = con.createStatement();
			ResultSet rs = s.executeQuery("SELECT auctionId FROM item_auction ORDER BY auctionId DESC LIMIT 0, 1"))
		{
			if (rs.next())
			{
				_auctionIds.set(rs.getInt(1) + 1);
			}
		}
		catch (SQLException e)
		{
			_log.log(Level.SEVERE, getClass().getSimpleName() + ": Failed loading auctions.", e);
		}
		
		final File file = new File(Config.DATAPACK_ROOT, "./datapack/ItemAuctions.xml");
		if (!file.exists())
		{
			_log.log(Level.WARNING, getClass().getSimpleName() + ": Missing ItemAuctions.xml!");
			return;
		}
		
		final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setValidating(false);
		factory.setIgnoringComments(true);
		
		try
		{
			final Document doc = factory.newDocumentBuilder().parse(file);
			for (Node na = doc.getFirstChild(); na != null; na = na.getNextSibling())
			{
				if ("list".equalsIgnoreCase(na.getNodeName()))
				{
					for (Node nb = na.getFirstChild(); nb != null; nb = nb.getNextSibling())
					{
						if ("instance".equalsIgnoreCase(nb.getNodeName()))
						{
							final NamedNodeMap nab = nb.getAttributes();
							final int instanceId = Integer.parseInt(nab.getNamedItem("id").getNodeValue());
							
							if (_managerInstances.containsKey(instanceId))
							{
								throw new Exception("Dublicated instanceId " + instanceId);
							}
							
							_managerInstances.put(instanceId, new ItemAuctionInstance(instanceId, _auctionIds, nb));
						}
					}
				}
			}
			_log.log(Level.INFO, getClass().getSimpleName() + ": Loaded " + _managerInstances.size() + " instance(s).");
		}
		catch (Exception e)
		{
			_log.log(Level.SEVERE, getClass().getSimpleName() + ": Failed loading auctions from xml.", e);
		}
	}
	
	public final void shutdown()
	{
		for (ItemAuctionInstance instance : _managerInstances.values())
		{
			instance.shutdown();
		}
	}
	
	public final ItemAuctionInstance getManagerInstance(int instanceId)
	{
		return _managerInstances.get(instanceId);
	}
	
	public final int getNextAuctionId()
	{
		return _auctionIds.getAndIncrement();
	}
	
	public static void deleteAuction(int auctionId)
	{
		try (Connection con = DatabaseFactory.getInstance().getConnection())
		{
			try (PreparedStatement ps = con.prepareStatement("DELETE FROM item_auction WHERE auctionId=?"))
			{
				ps.setInt(1, auctionId);
				ps.execute();
			}
			
			try (PreparedStatement ps = con.prepareStatement("DELETE FROM item_auction_bid WHERE auctionId=?"))
			{
				ps.setInt(1, auctionId);
				ps.execute();
			}
		}
		catch (SQLException e)
		{
			_log.log(Level.SEVERE, "L2ItemAuctionManagerInstance: Failed deleting auction: " + auctionId, e);
		}
	}
	
	/**
	 * Gets the single instance of {@code ItemAuctionManager}.
	 * @return single instance of {@code ItemAuctionManager}
	 */
	public static ItemAuctionManager getInstance()
	{
		return SingletonHolder._instance;
	}
	
	private static class SingletonHolder
	{
		protected static final ItemAuctionManager _instance = new ItemAuctionManager();
	}
}