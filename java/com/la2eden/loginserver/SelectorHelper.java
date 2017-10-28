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
package com.la2eden.loginserver;

import java.net.UnknownHostException;
import java.nio.channels.SocketChannel;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import com.la2eden.commons.mmocore.IAcceptFilter;
import com.la2eden.commons.mmocore.IClientFactory;
import com.la2eden.commons.mmocore.IMMOExecutor;
import com.la2eden.commons.mmocore.MMOConnection;
import com.la2eden.commons.mmocore.ReceivablePacket;
import com.la2eden.loginserver.network.L2LoginClient;
import com.la2eden.loginserver.network.serverpackets.Init;
import com.la2eden.util.IPv4Filter;

/**
 * @author KenM
 */
public class SelectorHelper implements IMMOExecutor<L2LoginClient>, IClientFactory<L2LoginClient>, IAcceptFilter
{
	private static final Logger LOG = Logger.getLogger(LoginController.class.getName());
	private final ThreadPoolExecutor _generalPacketsThreadPool;
	private final IPv4Filter _ipv4filter;
	
	public SelectorHelper()
	{
		_generalPacketsThreadPool = new ThreadPoolExecutor(4, 6, 15L, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>());
		_ipv4filter = new IPv4Filter();
	}
	
	@Override
	public void execute(ReceivablePacket<L2LoginClient> packet)
	{
		_generalPacketsThreadPool.execute(packet);
	}
	
	@Override
	public L2LoginClient create(MMOConnection<L2LoginClient> con)
	{
		final L2LoginClient client = new L2LoginClient(con);
		client.sendPacket(new Init(client));
		return client;
	}
	
	@Override
	public boolean accept(SocketChannel sc)
	{
		try
		{
			return _ipv4filter.accept(sc) && !LoginController.getInstance().isBannedAddress(sc.socket().getInetAddress());
		}
		catch (UnknownHostException e)
		{
			LOG.severe(SelectorHelper.class.getSimpleName() + ": Invalid address: " + sc.socket().getInetAddress() + "; " + e.getMessage());
		}
		return false;
	}
}
