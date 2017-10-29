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
package com.la2eden.gameserver.network.clientpackets;

import java.util.ArrayList;
import java.util.List;

import com.la2eden.Config;
import com.la2eden.gameserver.enums.MacroType;
import com.la2eden.gameserver.model.Macro;
import com.la2eden.gameserver.model.MacroCmd;
import com.la2eden.gameserver.model.actor.instance.L2PcInstance;
import com.la2eden.gameserver.network.SystemMessageId;

public final class RequestMakeMacro extends L2GameClientPacket
{
	private static final String _C__CD_REQUESTMAKEMACRO = "[C] CD RequestMakeMacro";
	
	private Macro _macro;
	private int _commandsLenght = 0;
	
	private static final int MAX_MACRO_LENGTH = 12;
	
	@Override
	protected void readImpl()
	{
		final int _id = readD();
		final String _name = readS();
		final String _desc = readS();
		final String _acronym = readS();
		final int _icon = readC();
		int _count = readC();
		if (_count > MAX_MACRO_LENGTH)
		{
			_count = MAX_MACRO_LENGTH;
		}
		
		if (Config.DEBUG)
		{
			_log.info("Make macro id:" + _id + "\tname:" + _name + "\tdesc:" + _desc + "\tacronym:" + _acronym + "\ticon:" + _icon + "\tcount:" + _count);
		}
		
		final List<MacroCmd> commands = new ArrayList<>(_count);
		for (int i = 0; i < _count; i++)
		{
			final int entry = readC();
			final int type = readC(); // 1 = skill, 3 = action, 4 = shortcut
			final int d1 = readD(); // skill or page number for shortcuts
			final int d2 = readC();
			final String command = readS();
			_commandsLenght += command.length();
			commands.add(new MacroCmd(entry, MacroType.values()[(type < 1) || (type > 6) ? 0 : type], d1, d2, command));
		}
		_macro = new Macro(_id, _icon, _name, _desc, _acronym, commands);
	}
	
	@Override
	protected void runImpl()
	{
		final L2PcInstance player = getClient().getActiveChar();
		if (player == null)
		{
			return;
		}
		if (_commandsLenght > 255)
		{
			// Invalid macro. Refer to the Help file for instructions.
			player.sendPacket(SystemMessageId.INVALID_MACRO_REFER_TO_THE_HELP_FILE_FOR_INSTRUCTIONS);
			return;
		}
		if (player.getMacros().getAllMacroses().size() > 48)
		{
			// You may create up to 48 macros.
			player.sendPacket(SystemMessageId.YOU_MAY_CREATE_UP_TO_48_MACROS);
			return;
		}
		if (_macro.getName().isEmpty())
		{
			// Enter the name of the macro.
			player.sendPacket(SystemMessageId.ENTER_THE_NAME_OF_THE_MACRO);
			return;
		}
		if (_macro.getDescr().length() > 32)
		{
			// Macro descriptions may contain up to 32 characters.
			player.sendPacket(SystemMessageId.MACRO_DESCRIPTIONS_MAY_CONTAIN_UP_TO_32_CHARACTERS);
			return;
		}
		player.registerMacro(_macro);
	}
	
	@Override
	public String getType()
	{
		return _C__CD_REQUESTMAKEMACRO;
	}
}
