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
package com.la2eden.gameserver.model.itemauction;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import com.la2eden.Config;
import com.la2eden.commons.database.DatabaseFactory;
import com.la2eden.gameserver.ThreadPoolManager;
import com.la2eden.gameserver.data.sql.impl.CharNameTable;
import com.la2eden.gameserver.enums.ItemLocation;
import com.la2eden.gameserver.instancemanager.ItemAuctionManager;
import com.la2eden.gameserver.model.L2World;
import com.la2eden.gameserver.model.StatsSet;
import com.la2eden.gameserver.model.actor.instance.L2PcInstance;
import com.la2eden.gameserver.model.items.instance.L2ItemInstance;
import com.la2eden.gameserver.network.SystemMessageId;
import com.la2eden.gameserver.network.serverpackets.SystemMessage;
import com.la2eden.util.Rnd;

public final class ItemAuctionInstance
{
	protected static final Logger _log = Logger.getLogger(ItemAuctionInstance.class.getName());
	private final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("HH:mm:ss dd.MM.yy");
	
	private static final long START_TIME_SPACE = TimeUnit.MILLISECONDS.convert(1, TimeUnit.MINUTES);
	private static final long FINISH_TIME_SPACE = TimeUnit.MILLISECONDS.convert(10, TimeUnit.MINUTES);
	
	// SQL queries
	private static final String SELECT_AUCTION_ID_BY_INSTANCE_ID = "SELECT auctionId FROM item_auction WHERE instanceId = ?";
	private static final String SELECT_AUCTION_INFO = "SELECT auctionItemId, startingTime, endingTime, auctionStateId FROM item_auction WHERE auctionId = ? ";
	private static final String DELETE_AUCTION_INFO_BY_AUCTION_ID = "DELETE FROM item_auction WHERE auctionId = ?";
	private static final String DELETE_AUCTION_BID_INFO_BY_AUCTION_ID = "DELETE FROM item_auction_bid WHERE auctionId = ?";
	private static final String SELECT_PLAYERS_ID_BY_AUCTION_ID = "SELECT playerObjId, playerBid FROM item_auction_bid WHERE auctionId = ?";
	
	private final int _instanceId;
	private final AtomicInteger _auctionIds;
	private final Map<Integer, ItemAuction> _auctions;
	private final ArrayList<AuctionItem> _items;
	private final AuctionDateGenerator _dateGenerator;
	
	private ItemAuction _currentAuction;
	private ItemAuction _nextAuction;
	private ScheduledFuture<?> _stateTask;
	
	public ItemAuctionInstance(int instanceId, AtomicInteger auctionIds, Node node) throws Exception
	{
		_instanceId = instanceId;
		_auctionIds = auctionIds;
		_auctions = new HashMap<>();
		_items = new ArrayList<>();
		
		final NamedNodeMap nanode = node.getAttributes();
		final StatsSet generatorConfig = new StatsSet();
		for (int i = nanode.getLength(); i-- > 0;)
		{
			final Node n = nanode.item(i);
			if (n != null)
			{
				generatorConfig.set(n.getNodeName(), n.getNodeValue());
			}
		}
		
		_dateGenerator = new AuctionDateGenerator(generatorConfig);
		
		for (Node na = node.getFirstChild(); na != null; na = na.getNextSibling())
		{
			try
			{
				if ("item".equalsIgnoreCase(na.getNodeName()))
				{
					final NamedNodeMap naa = na.getAttributes();
					final int auctionItemId = Integer.parseInt(naa.getNamedItem("auctionItemId").getNodeValue());
					final int auctionLenght = Integer.parseInt(naa.getNamedItem("auctionLenght").getNodeValue());
					final long auctionInitBid = Integer.parseInt(naa.getNamedItem("auctionInitBid").getNodeValue());
					
					final int itemId = Integer.parseInt(naa.getNamedItem("itemId").getNodeValue());
					final int itemCount = Integer.parseInt(naa.getNamedItem("itemCount").getNodeValue());
					
					if (auctionLenght < 1)
					{
						throw new IllegalArgumentException("auctionLenght < 1 for instanceId: " + _instanceId + ", itemId " + itemId);
					}
					
					final StatsSet itemExtra = new StatsSet();
					final AuctionItem item = new AuctionItem(auctionItemId, auctionLenght, auctionInitBid, itemId, itemCount, itemExtra);
					
					if (!item.checkItemExists())
					{
						throw new IllegalArgumentException("Item with id " + itemId + " not found");
					}
					
					for (AuctionItem tmp : _items)
					{
						if (tmp.getAuctionItemId() == auctionItemId)
						{
							throw new IllegalArgumentException("Dublicated auction item id " + auctionItemId);
						}
					}
					
					_items.add(item);
					
					for (Node nb = na.getFirstChild(); nb != null; nb = nb.getNextSibling())
					{
						if ("extra".equalsIgnoreCase(nb.getNodeName()))
						{
							final NamedNodeMap nab = nb.getAttributes();
							for (int i = nab.getLength(); i-- > 0;)
							{
								final Node n = nab.item(i);
								if (n != null)
								{
									itemExtra.set(n.getNodeName(), n.getNodeValue());
								}
							}
						}
					}
				}
			}
			catch (IllegalArgumentException e)
			{
				_log.log(Level.WARNING, getClass().getSimpleName() + ": Failed loading auction item", e);
			}
		}
		
		if (_items.isEmpty())
		{
			throw new IllegalArgumentException("No items defined");
		}
		
		try (Connection con = DatabaseFactory.getInstance().getConnection();
			PreparedStatement ps = con.prepareStatement(SELECT_AUCTION_ID_BY_INSTANCE_ID))
		{
			ps.setInt(1, _instanceId);
			try (ResultSet rset = ps.executeQuery())
			{
				while (rset.next())
				{
					final int auctionId = rset.getInt(1);
					try
					{
						final ItemAuction auction = loadAuction(auctionId);
						if (auction != null)
						{
							_auctions.put(auctionId, auction);
						}
						else
						{
							ItemAuctionManager.deleteAuction(auctionId);
						}
					}
					catch (SQLException e)
					{
						_log.log(Level.WARNING, getClass().getSimpleName() + ": Failed loading auction: " + auctionId, e);
					}
				}
			}
		}
		catch (SQLException e)
		{
			_log.log(Level.SEVERE, getClass().getSimpleName() + ": Failed loading auctions.", e);
			return;
		}
		
		_log.log(Level.INFO, getClass().getSimpleName() + ": Loaded " + _items.size() + " item(s) and registered " + _auctions.size() + " auction(s) for instance " + _instanceId + ".");
		checkAndSetCurrentAndNextAuction();
	}
	
	public final ItemAuction getCurrentAuction()
	{
		return _currentAuction;
	}
	
	public final ItemAuction getNextAuction()
	{
		return _nextAuction;
	}
	
	public final void shutdown()
	{
		if (_stateTask != null)
		{
			_stateTask.cancel(false);
		}
	}
	
	private final AuctionItem getAuctionItem(int auctionItemId)
	{
		for (int i = _items.size(); i-- > 0;)
		{
			final AuctionItem item = _items.get(i);
			if (item.getAuctionItemId() == auctionItemId)
			{
				return item;
			}
		}
		return null;
	}
	
	final void checkAndSetCurrentAndNextAuction()
	{
		final ItemAuction[] auctions = _auctions.values().toArray(new ItemAuction[_auctions.size()]);
		
		ItemAuction currentAuction = null;
		ItemAuction nextAuction = null;
		
		switch (auctions.length)
		{
			case 0:
			{
				nextAuction = createAuction(System.currentTimeMillis() + START_TIME_SPACE);
				break;
			}
			case 1:
			{
				switch (auctions[0].getAuctionState())
				{
					case CREATED:
					{
						if (auctions[0].getStartingTime() < (System.currentTimeMillis() + START_TIME_SPACE))
						{
							currentAuction = auctions[0];
							nextAuction = createAuction(System.currentTimeMillis() + START_TIME_SPACE);
						}
						else
						{
							nextAuction = auctions[0];
						}
						break;
					}
					case STARTED:
					{
						currentAuction = auctions[0];
						nextAuction = createAuction(Math.max(currentAuction.getEndingTime() + FINISH_TIME_SPACE, System.currentTimeMillis() + START_TIME_SPACE));
						break;
					}
					case FINISHED:
					{
						currentAuction = auctions[0];
						nextAuction = createAuction(System.currentTimeMillis() + START_TIME_SPACE);
						break;
					}
					default:
					{
						throw new IllegalArgumentException();
					}
				}
				break;
			}
			default:
			{
				Arrays.sort(auctions, Comparator.comparingLong(ItemAuction::getStartingTime).reversed());
				
				// just to make sure we won't skip any auction because of little different times
				final long currentTime = System.currentTimeMillis();
				
				for (ItemAuction auction : auctions)
				{
					if ((auction.getAuctionState() == ItemAuctionState.STARTED) || (auction.getStartingTime() <= currentTime))
					{
						currentAuction = auction;
						break;
					}
				}
				
				for (ItemAuction auction : auctions)
				{
					if ((auction.getStartingTime() > currentTime) && (currentAuction != auction))
					{
						nextAuction = auction;
						break;
					}
				}
				
				if (nextAuction == null)
				{
					nextAuction = createAuction(System.currentTimeMillis() + START_TIME_SPACE);
				}
				break;
			}
		}
		
		_auctions.put(nextAuction.getAuctionId(), nextAuction);
		
		_currentAuction = currentAuction;
		_nextAuction = nextAuction;
		
		if ((currentAuction != null) && (currentAuction.getAuctionState() != ItemAuctionState.FINISHED))
		{
			if (currentAuction.getAuctionState() == ItemAuctionState.STARTED)
			{
				setStateTask(ThreadPoolManager.getInstance().scheduleGeneral(new ScheduleAuctionTask(currentAuction), Math.max(currentAuction.getEndingTime() - System.currentTimeMillis(), 0L)));
			}
			else
			{
				setStateTask(ThreadPoolManager.getInstance().scheduleGeneral(new ScheduleAuctionTask(currentAuction), Math.max(currentAuction.getStartingTime() - System.currentTimeMillis(), 0L)));
			}
			_log.log(Level.INFO, getClass().getSimpleName() + ": Schedule current auction " + currentAuction.getAuctionId() + " for instance " + _instanceId);
		}
		else
		{
			setStateTask(ThreadPoolManager.getInstance().scheduleGeneral(new ScheduleAuctionTask(nextAuction), Math.max(nextAuction.getStartingTime() - System.currentTimeMillis(), 0L)));
			_log.log(Level.INFO, getClass().getSimpleName() + ": Schedule next auction " + nextAuction.getAuctionId() + " on " + DATE_FORMAT.format(new Date(nextAuction.getStartingTime())) + " for instance " + _instanceId);
		}
	}
	
	public final ItemAuction getAuction(int auctionId)
	{
		return _auctions.get(auctionId);
	}
	
	public final ItemAuction[] getAuctionsByBidder(int bidderObjId)
	{
		final Collection<ItemAuction> auctions = getAuctions();
		final ArrayList<ItemAuction> stack = new ArrayList<>(auctions.size());
		for (ItemAuction auction : getAuctions())
		{
			if ((auction.getAuctionState() != ItemAuctionState.CREATED) && (auction.getBidFor(bidderObjId) != null))
			{
				stack.add(auction);
			}
		}
		return stack.toArray(new ItemAuction[stack.size()]);
	}
	
	public final Collection<ItemAuction> getAuctions()
	{
		final Collection<ItemAuction> auctions;
		
		synchronized (_auctions)
		{
			auctions = _auctions.values();
		}
		
		return auctions;
	}
	
	private final class ScheduleAuctionTask implements Runnable
	{
		private final ItemAuction _auction;
		
		public ScheduleAuctionTask(ItemAuction auction)
		{
			_auction = auction;
		}
		
		@Override
		public final void run()
		{
			try
			{
				runImpl();
			}
			catch (Exception e)
			{
				_log.log(Level.SEVERE, getClass().getSimpleName() + ": Failed scheduling auction " + _auction.getAuctionId(), e);
			}
		}
		
		private final void runImpl() throws Exception
		{
			final ItemAuctionState state = _auction.getAuctionState();
			switch (state)
			{
				case CREATED:
				{
					if (!_auction.setAuctionState(state, ItemAuctionState.STARTED))
					{
						throw new IllegalStateException("Could not set auction state: " + ItemAuctionState.STARTED.toString() + ", expected: " + state.toString());
					}
					
					_log.log(Level.INFO, getClass().getSimpleName() + ": Auction " + _auction.getAuctionId() + " has started for instance " + _auction.getInstanceId());
					checkAndSetCurrentAndNextAuction();
					break;
				}
				case STARTED:
				{
					switch (_auction.getAuctionEndingExtendState())
					{
						case EXTEND_BY_5_MIN:
						{
							if (_auction.getScheduledAuctionEndingExtendState() == ItemAuctionExtendState.INITIAL)
							{
								_auction.setScheduledAuctionEndingExtendState(ItemAuctionExtendState.EXTEND_BY_5_MIN);
								setStateTask(ThreadPoolManager.getInstance().scheduleGeneral(this, Math.max(_auction.getEndingTime() - System.currentTimeMillis(), 0L)));
								return;
							}
							break;
						}
						case EXTEND_BY_3_MIN:
						{
							if (_auction.getScheduledAuctionEndingExtendState() != ItemAuctionExtendState.EXTEND_BY_3_MIN)
							{
								_auction.setScheduledAuctionEndingExtendState(ItemAuctionExtendState.EXTEND_BY_3_MIN);
								setStateTask(ThreadPoolManager.getInstance().scheduleGeneral(this, Math.max(_auction.getEndingTime() - System.currentTimeMillis(), 0L)));
								return;
							}
							break;
						}
						case EXTEND_BY_CONFIG_PHASE_A:
						{
							if (_auction.getScheduledAuctionEndingExtendState() != ItemAuctionExtendState.EXTEND_BY_CONFIG_PHASE_B)
							{
								_auction.setScheduledAuctionEndingExtendState(ItemAuctionExtendState.EXTEND_BY_CONFIG_PHASE_B);
								setStateTask(ThreadPoolManager.getInstance().scheduleGeneral(this, Math.max(_auction.getEndingTime() - System.currentTimeMillis(), 0L)));
								return;
							}
							break;
						}
						case EXTEND_BY_CONFIG_PHASE_B:
						{
							if (_auction.getScheduledAuctionEndingExtendState() != ItemAuctionExtendState.EXTEND_BY_CONFIG_PHASE_A)
							{
								_auction.setScheduledAuctionEndingExtendState(ItemAuctionExtendState.EXTEND_BY_CONFIG_PHASE_A);
								setStateTask(ThreadPoolManager.getInstance().scheduleGeneral(this, Math.max(_auction.getEndingTime() - System.currentTimeMillis(), 0L)));
								return;
							}
						}
					}
					
					if (!_auction.setAuctionState(state, ItemAuctionState.FINISHED))
					{
						throw new IllegalStateException("Could not set auction state: " + ItemAuctionState.FINISHED.toString() + ", expected: " + state.toString());
					}
					
					onAuctionFinished(_auction);
					checkAndSetCurrentAndNextAuction();
					break;
				}
				default:
				{
					throw new IllegalStateException("Invalid state: " + state);
				}
			}
		}
	}
	
	final void onAuctionFinished(ItemAuction auction)
	{
		auction.broadcastToAllBiddersInternal(SystemMessage.getSystemMessage(SystemMessageId.S1_S_AUCTION_HAS_ENDED).addInt(auction.getAuctionId()));
		
		final ItemAuctionBid bid = auction.getHighestBid();
		if (bid != null)
		{
			final L2ItemInstance item = auction.createNewItemInstance();
			final L2PcInstance player = bid.getPlayer();
			if (player != null)
			{
				player.getWarehouse().addItem("ItemAuction", item, null, null);
				player.sendPacket(SystemMessageId.YOU_HAVE_BID_THE_HIGHEST_PRICE_AND_HAVE_WON_THE_ITEM_THE_ITEM_CAN_BE_FOUND_IN_YOUR_PERSONAL_WAREHOUSE);
				
				_log.log(Level.INFO, getClass().getSimpleName() + ": Auction " + auction.getAuctionId() + " has finished. Highest bid by " + player.getName() + " for instance " + _instanceId);
			}
			else
			{
				item.setOwnerId(bid.getPlayerObjId());
				item.setItemLocation(ItemLocation.WAREHOUSE);
				item.updateDatabase();
				L2World.getInstance().removeObject(item);
				
				_log.log(Level.INFO, getClass().getSimpleName() + ": Auction " + auction.getAuctionId() + " has finished. Highest bid by " + CharNameTable.getInstance().getNameById(bid.getPlayerObjId()) + " for instance " + _instanceId);
			}
			
			// Clean all canceled bids
			auction.clearCanceledBids();
		}
		else
		{
			_log.log(Level.INFO, getClass().getSimpleName() + ": Auction " + auction.getAuctionId() + " has finished. There have not been any bid for instance " + _instanceId);
		}
	}
	
	final void setStateTask(ScheduledFuture<?> future)
	{
		final ScheduledFuture<?> stateTask = _stateTask;
		if (stateTask != null)
		{
			stateTask.cancel(false);
		}
		
		_stateTask = future;
	}
	
	private final ItemAuction createAuction(long after)
	{
		final AuctionItem auctionItem = _items.get(Rnd.get(_items.size()));
		final long startingTime = _dateGenerator.nextDate(after);
		final long endingTime = startingTime + TimeUnit.MILLISECONDS.convert(auctionItem.getAuctionLength(), TimeUnit.MINUTES);
		final ItemAuction auction = new ItemAuction(_auctionIds.getAndIncrement(), _instanceId, startingTime, endingTime, auctionItem);
		auction.storeMe();
		return auction;
	}
	
	private final ItemAuction loadAuction(int auctionId) throws SQLException
	{
		try (Connection con = DatabaseFactory.getInstance().getConnection())
		{
			int auctionItemId = 0;
			long startingTime = 0;
			long endingTime = 0;
			byte auctionStateId = 0;
			try (PreparedStatement ps = con.prepareStatement(SELECT_AUCTION_INFO))
			{
				ps.setInt(1, auctionId);
				try (ResultSet rset = ps.executeQuery())
				{
					if (!rset.next())
					{
						_log.log(Level.WARNING, getClass().getSimpleName() + ": Auction data not found for auction: " + auctionId);
						return null;
					}
					auctionItemId = rset.getInt(1);
					startingTime = rset.getLong(2);
					endingTime = rset.getLong(3);
					auctionStateId = rset.getByte(4);
				}
			}
			
			if (startingTime >= endingTime)
			{
				_log.log(Level.WARNING, getClass().getSimpleName() + ": Invalid starting/ending paramaters for auction: " + auctionId);
				return null;
			}
			
			final AuctionItem auctionItem = getAuctionItem(auctionItemId);
			if (auctionItem == null)
			{
				_log.log(Level.WARNING, getClass().getSimpleName() + ": AuctionItem: " + auctionItemId + ", not found for auction: " + auctionId);
				return null;
			}
			
			final ItemAuctionState auctionState = ItemAuctionState.stateForStateId(auctionStateId);
			if (auctionState == null)
			{
				_log.log(Level.WARNING, getClass().getSimpleName() + ": Invalid auctionStateId: " + auctionStateId + ", for auction: " + auctionId);
				return null;
			}
			
			if ((auctionState == ItemAuctionState.FINISHED) && (startingTime < (System.currentTimeMillis() - TimeUnit.MILLISECONDS.convert(Config.ALT_ITEM_AUCTION_EXPIRED_AFTER, TimeUnit.DAYS))))
			{
				_log.log(Level.INFO, getClass().getSimpleName() + ": Clearing expired auction: " + auctionId);
				try (PreparedStatement ps = con.prepareStatement(DELETE_AUCTION_INFO_BY_AUCTION_ID))
				{
					ps.setInt(1, auctionId);
					ps.execute();
				}
				
				try (PreparedStatement ps = con.prepareStatement(DELETE_AUCTION_BID_INFO_BY_AUCTION_ID))
				{
					ps.setInt(1, auctionId);
					ps.execute();
				}
				return null;
			}
			
			final List<ItemAuctionBid> auctionBids = new ArrayList<>();
			try (PreparedStatement ps = con.prepareStatement(SELECT_PLAYERS_ID_BY_AUCTION_ID))
			{
				ps.setInt(1, auctionId);
				try (ResultSet rs = ps.executeQuery())
				{
					while (rs.next())
					{
						auctionBids.add(new ItemAuctionBid(rs.getInt(1), rs.getLong(2)));
					}
				}
			}
			return new ItemAuction(auctionId, _instanceId, startingTime, endingTime, auctionItem, auctionBids, auctionState);
		}
	}
}