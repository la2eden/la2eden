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
package handlers.effecthandlers;

import com.la2eden.gameserver.instancemanager.MapRegionManager;
import com.la2eden.gameserver.model.StatsSet;
import com.la2eden.gameserver.model.TeleportWhereType;
import com.la2eden.gameserver.model.actor.L2Npc;
import com.la2eden.gameserver.model.actor.instance.L2GuardInstance;
import com.la2eden.gameserver.model.conditions.Condition;
import com.la2eden.gameserver.model.effects.AbstractEffect;
import com.la2eden.gameserver.model.effects.L2EffectType;
import com.la2eden.gameserver.model.skills.BuffInfo;

/**
 * Escape effect implementation.
 * @author Adry_85
 */
public final class Escape extends AbstractEffect
{
	private final TeleportWhereType _escapeType;
	
	public Escape(Condition attachCond, Condition applyCond, StatsSet set, StatsSet params)
	{
		super(attachCond, applyCond, set, params);
		
		_escapeType = params.getEnum("escapeType", TeleportWhereType.class, null);
	}
	
	@Override
	public L2EffectType getEffectType()
	{
		return L2EffectType.TELEPORT;
	}
	
	@Override
	public boolean isInstant()
	{
		return true;
	}
	
	@Override
	public void onStart(BuffInfo info)
	{
		if (_escapeType == null)
		{
			return;
		}
		
		if (info.getEffected() instanceof L2GuardInstance)
		{
			info.getEffected().teleToLocation(((L2Npc) info.getEffected()).getSpawn());
			info.getEffected().setHeading(((L2Npc) info.getEffected()).getSpawn().getHeading());
		}
		else
		{
			info.getEffected().teleToLocation(MapRegionManager.getInstance().getTeleToLocation(info.getEffected(), _escapeType), true);
			info.getEffected().getActingPlayer().setIsIn7sDungeon(false);
			info.getEffected().setInstanceId(0);
		}
	}
}