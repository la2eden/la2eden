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
package handlers.effecthandlers;

import com.la2eden.gameserver.GeoData;
import com.la2eden.gameserver.model.Location;
import com.la2eden.gameserver.model.StatsSet;
import com.la2eden.gameserver.model.conditions.Condition;
import com.la2eden.gameserver.model.effects.AbstractEffect;
import com.la2eden.gameserver.model.effects.EffectFlag;
import com.la2eden.gameserver.model.skills.BuffInfo;
import com.la2eden.gameserver.network.serverpackets.FlyToLocation;
import com.la2eden.gameserver.network.serverpackets.FlyToLocation.FlyType;
import com.la2eden.gameserver.network.serverpackets.ValidateLocation;

/**
 * Throw Up effect implementation.
 */
public final class ThrowUp extends AbstractEffect
{
	public ThrowUp(Condition attachCond, Condition applyCond, StatsSet set, StatsSet params)
	{
		super(attachCond, applyCond, set, params);
	}
	
	@Override
	public int getEffectFlags()
	{
		return EffectFlag.STUNNED.getMask();
	}
	
	@Override
	public boolean isInstant()
	{
		return true;
	}
	
	@Override
	public void onStart(BuffInfo info)
	{
		// Get current position of the L2Character
		final int curX = info.getEffected().getX();
		final int curY = info.getEffected().getY();
		final int curZ = info.getEffected().getZ();
		
		// Calculate distance between effector and effected current position
		final double dx = info.getEffector().getX() - curX;
		final double dy = info.getEffector().getY() - curY;
		final double dz = info.getEffector().getZ() - curZ;
		final double distance = Math.sqrt((dx * dx) + (dy * dy));
		if (distance > 2000)
		{
			_log.info("EffectThrow was going to use invalid coordinates for characters, getEffected: " + curX + "," + curY + " and getEffector: " + info.getEffector().getX() + "," + info.getEffector().getY());
			return;
		}
		int offset = Math.min((int) distance + info.getSkill().getFlyRadius(), 1400);
		
		double cos;
		double sin;
		
		// approximation for moving futher when z coordinates are different
		// TODO: handle Z axis movement better
		offset += Math.abs(dz);
		if (offset < 5)
		{
			offset = 5;
		}
		
		// If no distance
		if (distance < 1)
		{
			return;
		}
		
		// Calculate movement angles needed
		sin = dy / distance;
		cos = dx / distance;
		
		// Calculate the new destination with offset included
		final int x = info.getEffector().getX() - (int) (offset * cos);
		final int y = info.getEffector().getY() - (int) (offset * sin);
		final int z = info.getEffected().getZ();
		
		final Location destination = GeoData.getInstance().moveCheck(info.getEffected().getX(), info.getEffected().getY(), info.getEffected().getZ(), x, y, z, info.getEffected().getInstanceId());
		
		info.getEffected().broadcastPacket(new FlyToLocation(info.getEffected(), destination, FlyType.THROW_UP));
		// TODO: Review.
		info.getEffected().setXYZ(destination);
		info.getEffected().broadcastPacket(new ValidateLocation(info.getEffected()));
	}
}