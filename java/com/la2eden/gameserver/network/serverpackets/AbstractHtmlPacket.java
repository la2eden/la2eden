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
package com.la2eden.gameserver.network.serverpackets;

import java.util.logging.Level;

import com.la2eden.gameserver.cache.HtmCache;
import com.la2eden.gameserver.enums.HtmlActionScope;
import com.la2eden.gameserver.model.actor.instance.L2PcInstance;
import com.la2eden.gameserver.util.Util;

/**
 * @author HorridoJoho
 */
public abstract class AbstractHtmlPacket extends L2GameServerPacket
{
	public static final char VAR_PARAM_START_CHAR = '$';
	
	private final int _npcObjId;
	private String _html = null;
	private boolean _disabledValidation = false;
	
	protected AbstractHtmlPacket()
	{
		_npcObjId = 0;
	}
	
	protected AbstractHtmlPacket(int npcObjId)
	{
		if (npcObjId < 0)
		{
			throw new IllegalArgumentException();
		}
		
		_npcObjId = npcObjId;
	}
	
	protected AbstractHtmlPacket(String html)
	{
		_npcObjId = 0;
		setHtml(html);
	}
	
	protected AbstractHtmlPacket(int npcObjId, String html)
	{
		if (npcObjId < 0)
		{
			throw new IllegalArgumentException();
		}
		
		_npcObjId = npcObjId;
		setHtml(html);
	}
	
	public final void disableValidation()
	{
		_disabledValidation = true;
	}
	
	public final void setHtml(String html)
	{
		if (html.length() > 17200)
		{
			_log.log(Level.WARNING, "Html is too long! this will crash the client!", new Throwable());
			_html = html.substring(0, 17200);
		}
		
		if (!html.contains("<html"))
		{
			html = "<html><body>" + html + "</body></html>";
		}
		
		_html = html;
	}
	
	public final boolean setFile(String prefix, String path)
	{
		final String content = HtmCache.getInstance().getHtm(prefix, path);
		if (content == null)
		{
			setHtml("<html><body>My Text is missing:<br>" + path + "</body></html>");
			_log.warning("missing html page " + path);
			return false;
		}
		
		setHtml(content);
		return true;
	}
	
	public final void replace(String pattern, String value)
	{
		_html = _html.replaceAll(pattern, value.replaceAll("\\$", "\\\\\\$"));
	}
	
	public final void replace(String pattern, CharSequence value)
	{
		replace(pattern, String.valueOf(value));
	}
	
	public final void replace(String pattern, boolean val)
	{
		replace(pattern, String.valueOf(val));
	}
	
	public final void replace(String pattern, int val)
	{
		replace(pattern, String.valueOf(val));
	}
	
	public final void replace(String pattern, long val)
	{
		replace(pattern, String.valueOf(val));
	}
	
	public final void replace(String pattern, double val)
	{
		replace(pattern, String.valueOf(val));
	}
	
	@Override
	public final void runImpl()
	{
		final L2PcInstance player = getClient().getActiveChar();
		if (player == null)
		{
			return;
		}
		
		player.clearHtmlActions(getScope());
		
		if (_disabledValidation)
		{
			return;
		}
		
		Util.buildHtmlActionCache(player, getScope(), _npcObjId, _html);
	}
	
	public final int getNpcObjId()
	{
		return _npcObjId;
	}
	
	public final String getHtml()
	{
		return _html;
	}
	
	public abstract HtmlActionScope getScope();
}
