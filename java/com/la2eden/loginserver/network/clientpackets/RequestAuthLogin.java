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
package com.la2eden.loginserver.network.clientpackets;

import java.net.InetAddress;
import java.security.GeneralSecurityException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.crypto.Cipher;

import com.la2eden.Config;
import com.la2eden.loginserver.GameServerTable.GameServerInfo;
import com.la2eden.loginserver.LoginController;
import com.la2eden.loginserver.model.data.AccountInfo;
import com.la2eden.loginserver.network.L2LoginClient;
import com.la2eden.loginserver.network.L2LoginClient.LoginClientState;
import com.la2eden.loginserver.network.serverpackets.AccountKicked;
import com.la2eden.loginserver.network.serverpackets.AccountKicked.AccountKickedReason;
import com.la2eden.loginserver.network.serverpackets.LoginFail.LoginFailReason;
import com.la2eden.loginserver.network.serverpackets.LoginOk;
import com.la2eden.loginserver.network.serverpackets.ServerList;

/**
 * <pre>
 * Format: x 0 (a leading null) x: the rsa encrypted block with the login an password.
 * 
 * <pre>
 */
public class RequestAuthLogin extends L2LoginClientPacket
{
	private static Logger _log = Logger.getLogger(RequestAuthLogin.class.getName());
	
	private final byte[] _raw = new byte[128];
	
	private String _user;
	private String _password;
	private int _ncotp;
	
	/**
	 * @return
	 */
	public String getPassword()
	{
		return _password;
	}
	
	/**
	 * @return
	 */
	public String getUser()
	{
		return _user;
	}
	
	public int getOneTimePassword()
	{
		return _ncotp;
	}
	
	@Override
	public boolean readImpl()
	{
		if (super._buf.remaining() >= 128)
		{
			readB(_raw);
			return true;
		}
		return false;
	}
	
	@Override
	public void run()
	{
		byte[] decrypted = null;
		final L2LoginClient client = getClient();
		try
		{
			final Cipher rsaCipher = Cipher.getInstance("RSA/ECB/nopadding");
			rsaCipher.init(Cipher.DECRYPT_MODE, client.getRSAPrivateKey());
			decrypted = rsaCipher.doFinal(_raw, 0x00, 0x80);
		}
		catch (GeneralSecurityException e)
		{
			_log.log(Level.INFO, "", e);
			return;
		}
		
		try
		{
			_user = new String(decrypted, 0x5E, 14).trim().toLowerCase();
			_password = new String(decrypted, 0x6C, 16).trim();
			_ncotp = decrypted[0x7c];
			_ncotp |= decrypted[0x7d] << 8;
			_ncotp |= decrypted[0x7e] << 16;
			_ncotp |= decrypted[0x7f] << 24;
		}
		catch (Exception e)
		{
			_log.log(Level.WARNING, "", e);
			return;
		}
		
		final InetAddress clientAddr = getClient().getConnection().getInetAddress();
		
		final LoginController lc = LoginController.getInstance();
		final AccountInfo info = lc.retriveAccountInfo(clientAddr, _user, _password);
		if (info == null)
		{
			// user or pass wrong
			client.close(LoginFailReason.REASON_USER_OR_PASS_WRONG);
			return;
		}
		
		switch (lc.tryCheckinAccount(client, clientAddr, info))
		{
			case AUTH_SUCCESS:
			{
				client.setAccount(info.getLogin());
				client.setState(LoginClientState.AUTHED_LOGIN);
				client.setSessionKey(lc.assignSessionKeyToClient(info.getLogin(), client));
				lc.getCharactersOnAccount(info.getLogin());
				if (Config.SHOW_LICENCE)
				{
					client.sendPacket(new LoginOk(getClient().getSessionKey()));
				}
				else
				{
					getClient().sendPacket(new ServerList(getClient()));
				}
				break;
			}
			case INVALID_PASSWORD:
			{
				client.close(LoginFailReason.REASON_USER_OR_PASS_WRONG);
				break;
			}
			case ACCOUNT_BANNED:
			{
				client.close(new AccountKicked(AccountKickedReason.REASON_PERMANENTLY_BANNED));
				return;
			}
			case ALREADY_ON_LS:
			{
				final L2LoginClient oldClient = lc.getAuthedClient(info.getLogin());
				if (oldClient != null)
				{
					// kick the other client
					oldClient.close(LoginFailReason.REASON_ACCOUNT_IN_USE);
					lc.removeAuthedLoginClient(info.getLogin());
				}
				// kick also current client
				client.close(LoginFailReason.REASON_ACCOUNT_IN_USE);
				break;
			}
			case ALREADY_ON_GS:
			{
				final GameServerInfo gsi = lc.getAccountOnGameServer(info.getLogin());
				if (gsi != null)
				{
					client.close(LoginFailReason.REASON_ACCOUNT_IN_USE);
					
					// kick from there
					if (gsi.isAuthed())
					{
						gsi.getGameServerThread().kickPlayer(info.getLogin());
					}
				}
				break;
			}
		}
	}
}
