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
package com.la2eden.loginserver.network;

import java.io.IOException;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.security.interfaces.RSAPrivateKey;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import com.la2eden.Config;
import com.la2eden.commons.mmocore.MMOClient;
import com.la2eden.commons.mmocore.MMOConnection;
import com.la2eden.commons.mmocore.SendablePacket;
import com.la2eden.loginserver.LoginController;
import com.la2eden.loginserver.SessionKey;
import com.la2eden.loginserver.network.serverpackets.L2LoginServerPacket;
import com.la2eden.loginserver.network.serverpackets.LoginFail;
import com.la2eden.loginserver.network.serverpackets.LoginFail.LoginFailReason;
import com.la2eden.loginserver.network.serverpackets.PlayFail;
import com.la2eden.loginserver.network.serverpackets.PlayFail.PlayFailReason;
import com.la2eden.util.Rnd;
import com.la2eden.util.crypt.LoginCrypt;
import com.la2eden.util.crypt.ScrambledKeyPair;

/**
 * Represents a client connected into the LoginServer
 * @author KenM
 */
public final class L2LoginClient extends MMOClient<MMOConnection<L2LoginClient>>
{
	private static final Logger _log = Logger.getLogger(L2LoginClient.class.getName());
	
	public static enum LoginClientState
	{
		CONNECTED,
		AUTHED_GG,
		AUTHED_LOGIN
	}
	
	private LoginClientState _state;
	
	// Crypt
	private final LoginCrypt _loginCrypt;
	private final ScrambledKeyPair _scrambledPair;
	private final byte[] _blowfishKey;
	
	private String _account;
	private int _accessLevel;
	private int _lastServer;
	private SessionKey _sessionKey;
	private final int _sessionId;
	private boolean _joinedGS;
	private Map<Integer, Integer> _charsOnServers;
	private Map<Integer, long[]> _charsToDelete;
	
	private final long _connectionStartTime;
	
	/**
	 * @param con
	 */
	public L2LoginClient(MMOConnection<L2LoginClient> con)
	{
		super(con);
		_state = LoginClientState.CONNECTED;
		_scrambledPair = LoginController.getInstance().getScrambledRSAKeyPair();
		_blowfishKey = LoginController.getInstance().getBlowfishKey();
		_sessionId = Rnd.nextInt();
		_connectionStartTime = System.currentTimeMillis();
		_loginCrypt = new LoginCrypt();
		_loginCrypt.setKey(_blowfishKey);
	}
	
	@Override
	public boolean decrypt(ByteBuffer buf, int size)
	{
		boolean isChecksumValid = false;
		try
		{
			isChecksumValid = _loginCrypt.decrypt(buf.array(), buf.position(), size);
			if (!isChecksumValid)
			{
				_log.warning("Wrong checksum from client: " + toString());
				super.getConnection().close((SendablePacket<L2LoginClient>) null);
				return false;
			}
			return true;
		}
		catch (IOException e)
		{
			_log.warning(getClass().getSimpleName() + ": " + e.getMessage());
			super.getConnection().close((SendablePacket<L2LoginClient>) null);
			return false;
		}
	}
	
	@Override
	public boolean encrypt(ByteBuffer buf, int size)
	{
		final int offset = buf.position();
		try
		{
			size = _loginCrypt.encrypt(buf.array(), offset, size);
		}
		catch (IOException e)
		{
			_log.warning(getClass().getSimpleName() + ": " + e.getMessage());
			return false;
		}
		buf.position(offset + size);
		return true;
	}
	
	public LoginClientState getState()
	{
		return _state;
	}
	
	public void setState(LoginClientState state)
	{
		_state = state;
	}
	
	public byte[] getBlowfishKey()
	{
		return _blowfishKey;
	}
	
	public byte[] getScrambledModulus()
	{
		return _scrambledPair._scrambledModulus;
	}
	
	public RSAPrivateKey getRSAPrivateKey()
	{
		return (RSAPrivateKey) _scrambledPair._pair.getPrivate();
	}
	
	public String getAccount()
	{
		return _account;
	}
	
	public void setAccount(String account)
	{
		_account = account;
	}
	
	public void setAccessLevel(int accessLevel)
	{
		_accessLevel = accessLevel;
	}
	
	public int getAccessLevel()
	{
		return _accessLevel;
	}
	
	public void setLastServer(int lastServer)
	{
		_lastServer = lastServer;
	}
	
	public int getLastServer()
	{
		return _lastServer;
	}
	
	public int getSessionId()
	{
		return _sessionId;
	}
	
	public boolean hasJoinedGS()
	{
		return _joinedGS;
	}
	
	public void setJoinedGS(boolean val)
	{
		_joinedGS = val;
	}
	
	public void setSessionKey(SessionKey sessionKey)
	{
		_sessionKey = sessionKey;
	}
	
	public SessionKey getSessionKey()
	{
		return _sessionKey;
	}
	
	public long getConnectionStartTime()
	{
		return _connectionStartTime;
	}
	
	public void sendPacket(L2LoginServerPacket lsp)
	{
		getConnection().sendPacket(lsp);
	}
	
	public void close(LoginFailReason reason)
	{
		getConnection().close(new LoginFail(reason));
	}
	
	public void close(PlayFailReason reason)
	{
		getConnection().close(new PlayFail(reason));
	}
	
	public void close(L2LoginServerPacket lsp)
	{
		getConnection().close(lsp);
	}
	
	public void setCharsOnServ(int servId, int chars)
	{
		if (_charsOnServers == null)
		{
			_charsOnServers = new HashMap<>();
		}
		_charsOnServers.put(servId, chars);
	}
	
	public Map<Integer, Integer> getCharsOnServ()
	{
		return _charsOnServers;
	}
	
	public void serCharsWaitingDelOnServ(int servId, long[] charsToDel)
	{
		if (_charsToDelete == null)
		{
			_charsToDelete = new HashMap<>();
		}
		_charsToDelete.put(servId, charsToDel);
	}
	
	public Map<Integer, long[]> getCharsWaitingDelOnServ()
	{
		return _charsToDelete;
	}
	
	@Override
	public void onDisconnection()
	{
		if (Config.DEBUG)
		{
			_log.info("DISCONNECTED: " + toString());
		}
		
		if (!hasJoinedGS() || ((getConnectionStartTime() + LoginController.LOGIN_TIMEOUT) < System.currentTimeMillis()))
		{
			LoginController.getInstance().removeAuthedLoginClient(getAccount());
		}
	}
	
	@Override
	public String toString()
	{
		final InetAddress address = getConnection().getInetAddress();
		if (getState() == LoginClientState.AUTHED_LOGIN)
		{
			return "[" + getAccount() + " (" + (address == null ? "disconnected" : address.getHostAddress()) + ")]";
		}
		return "[" + (address == null ? "disconnected" : address.getHostAddress()) + "]";
	}
	
	@Override
	protected void onForcedDisconnection()
	{
		// Empty
	}
}
