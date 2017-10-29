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
package com.la2eden.gameserver.cache;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.la2eden.Config;
import com.la2eden.gameserver.ThreadPoolManager;
import com.la2eden.gameserver.model.actor.instance.L2PcInstance;

/**
 * @author -Nemesiss-
 */
public class WarehouseCacheManager
{
	protected final Map<L2PcInstance, Long> _cachedWh = new ConcurrentHashMap<>();
	protected final long _cacheTime = Config.WAREHOUSE_CACHE_TIME * 60000L;
	
	protected WarehouseCacheManager()
	{
		ThreadPoolManager.getInstance().scheduleAiAtFixedRate(new CacheScheduler(), 120000, 60000);
	}
	
	public void addCacheTask(L2PcInstance pc)
	{
		_cachedWh.put(pc, System.currentTimeMillis());
	}
	
	public void remCacheTask(L2PcInstance pc)
	{
		_cachedWh.remove(pc);
	}
	
	public class CacheScheduler implements Runnable
	{
		@Override
		public void run()
		{
			final long cTime = System.currentTimeMillis();
			for (L2PcInstance pc : _cachedWh.keySet())
			{
				if ((cTime - _cachedWh.get(pc)) > _cacheTime)
				{
					pc.clearWarehouse();
					_cachedWh.remove(pc);
				}
			}
		}
	}
	
	public static WarehouseCacheManager getInstance()
	{
		return SingletonHolder._instance;
	}
	
	private static class SingletonHolder
	{
		protected static final WarehouseCacheManager _instance = new WarehouseCacheManager();
	}
}
