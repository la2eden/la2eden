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
package com.la2eden.gameserver.model.events.impl.character.player.clan;

import com.la2eden.gameserver.model.L2Clan;
import com.la2eden.gameserver.model.L2ClanMember;
import com.la2eden.gameserver.model.events.EventType;
import com.la2eden.gameserver.model.events.impl.IBaseEvent;

/**
 * @author UnAfraid
 */
public class OnPlayerClanLeaderChange implements IBaseEvent
{
	private final L2ClanMember _oldLeader;
	private final L2ClanMember _newLeader;
	private final L2Clan _clan;
	
	public OnPlayerClanLeaderChange(L2ClanMember oldLeader, L2ClanMember newLeader, L2Clan clan)
	{
		_oldLeader = oldLeader;
		_newLeader = newLeader;
		_clan = clan;
	}
	
	public L2ClanMember getOldLeader()
	{
		return _oldLeader;
	}
	
	public L2ClanMember getNewLeader()
	{
		return _newLeader;
	}
	
	public L2Clan getClan()
	{
		return _clan;
	}
	
	@Override
	public EventType getType()
	{
		return EventType.ON_PLAYER_CLAN_LEADER_CHANGE;
	}
}
