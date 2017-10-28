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
package handlers.bypasshandlers;

import com.la2eden.gameserver.handler.IBypassHandler;
import com.la2eden.gameserver.model.actor.L2Character;
import com.la2eden.gameserver.model.actor.instance.L2PcInstance;
import com.la2eden.gameserver.network.serverpackets.TutorialCloseHtml;

/**
 * @author UnAfraid
 */
public class TutorialClose implements IBypassHandler
{
	private static final String[] COMMANDS =
	{
		"tutorial_close",
	};
	
	@Override
	public boolean useBypass(String command, L2PcInstance activeChar, L2Character target)
	{
		activeChar.sendPacket(TutorialCloseHtml.STATIC_PACKET);
		return false;
	}
	
	@Override
	public String[] getBypassList()
	{
		return COMMANDS;
	}
}