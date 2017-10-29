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
package com.la2eden.loginserver.network.gameserverpackets;

import java.util.Arrays;
import java.util.logging.Logger;

import com.la2eden.Config;
import com.la2eden.loginserver.GameServerTable;
import com.la2eden.loginserver.GameServerTable.GameServerInfo;
import com.la2eden.loginserver.GameServerThread;
import com.la2eden.loginserver.network.L2JGameServerPacketHandler.GameServerState;
import com.la2eden.loginserver.network.loginserverpackets.AuthResponse;
import com.la2eden.loginserver.network.loginserverpackets.LoginServerFail;
import com.la2eden.util.network.BaseRecievePacket;

/**
 * <pre>
 * Format: cccddb
 * c desired ID
 * c accept alternative ID
 * c reserve Host
 * s ExternalHostName
 * s InetranlHostName
 * d max players
 * d hexid size
 * b hexid
 * </pre>
 * 
 * @author -Wooden-
 */
public class GameServerAuth extends BaseRecievePacket
{
	protected static Logger _log = Logger.getLogger(GameServerAuth.class.getName());
	GameServerThread _server;
	private final byte[] _hexId;
	private final int _desiredId;
	@SuppressWarnings("unused")
	private final boolean _hostReserved;
	private final boolean _acceptAlternativeId;
	private final int _maxPlayers;
	private final int _port;
	private final String[] _hosts;
	
	/**
	 * @param decrypt
	 * @param server
	 */
	public GameServerAuth(byte[] decrypt, GameServerThread server)
	{
		super(decrypt);
		_server = server;
		_desiredId = readC();
		_acceptAlternativeId = readC() != 0;
		_hostReserved = readC() != 0;
		_port = readH();
		_maxPlayers = readD();
		int size = readD();
		_hexId = readB(size);
		size = 2 * readD();
		_hosts = new String[size];
		for (int i = 0; i < size; i++)
		{
			_hosts[i] = readS();
		}
		
		if (Config.DEBUG)
		{
			_log.info("Auth request received");
		}
		
		if (handleRegProcess())
		{
			final AuthResponse ar = new AuthResponse(server.getGameServerInfo().getId());
			server.sendPacket(ar);
			if (Config.DEBUG)
			{
				_log.info("Authed: id: " + server.getGameServerInfo().getId());
			}
			server.broadcastToTelnet("GameServer [" + server.getServerId() + "] " + GameServerTable.getInstance().getServerNameById(server.getServerId()) + " is connected");
			server.setLoginConnectionState(GameServerState.AUTHED);
		}
	}
	
	private boolean handleRegProcess()
	{
		final GameServerTable gameServerTable = GameServerTable.getInstance();
		
		final int id = _desiredId;
		final byte[] hexId = _hexId;
		
		GameServerInfo gsi = gameServerTable.getRegisteredGameServerById(id);
		// is there a gameserver registered with this id?
		if (gsi != null)
		{
			// does the hex id match?
			if (Arrays.equals(gsi.getHexId(), hexId))
			{
				// check to see if this GS is already connected
				synchronized (gsi)
				{
					if (gsi.isAuthed())
					{
						_server.forceClose(LoginServerFail.REASON_ALREADY_LOGGED8IN);
						return false;
					}
					_server.attachGameServerInfo(gsi, _port, _hosts, _maxPlayers);
				}
			}
			else
			{
				if (!Config.ACCEPT_NEW_GAMESERVER || !_acceptAlternativeId)
				{
					_server.forceClose(LoginServerFail.REASON_WRONG_HEXID);
					return false;
				}
				gsi = new GameServerInfo(id, hexId, _server);
				if (!gameServerTable.registerWithFirstAvailableId(gsi))
				{
					_server.forceClose(LoginServerFail.REASON_NO_FREE_ID);
					return false;
				}
				_server.attachGameServerInfo(gsi, _port, _hosts, _maxPlayers);
				gameServerTable.registerServerOnDB(gsi);
			}
		}
		else
		{
			if (!Config.ACCEPT_NEW_GAMESERVER)
			{
				_server.forceClose(LoginServerFail.REASON_WRONG_HEXID);
				return false;
			}
			gsi = new GameServerInfo(id, hexId, _server);
			if (!gameServerTable.register(id, gsi))
			{
				_server.forceClose(LoginServerFail.REASON_ID_RESERVED);
				return false;
			}
			_server.attachGameServerInfo(gsi, _port, _hosts, _maxPlayers);
			gameServerTable.registerServerOnDB(gsi);
		}
		
		return true;
	}
}
