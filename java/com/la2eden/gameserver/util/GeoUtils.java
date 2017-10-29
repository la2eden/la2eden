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
package com.la2eden.gameserver.util;

import java.awt.Color;

import com.la2eden.commons.geodriver.Cell;
import com.la2eden.gameserver.GeoData;
import com.la2eden.gameserver.model.actor.instance.L2PcInstance;
import com.la2eden.gameserver.network.serverpackets.ExServerPrimitive;

/**
 * @author HorridoJoho
 */
public final class GeoUtils
{
	public static void debug2DLine(L2PcInstance player, int x, int y, int tx, int ty, int z)
	{
		final int gx = GeoData.getInstance().getGeoX(x);
		final int gy = GeoData.getInstance().getGeoY(y);
		
		final int tgx = GeoData.getInstance().getGeoX(tx);
		final int tgy = GeoData.getInstance().getGeoY(ty);
		
		final ExServerPrimitive prim = new ExServerPrimitive("Debug2DLine", x, y, z);
		prim.addLine(Color.BLUE, GeoData.getInstance().getWorldX(gx), GeoData.getInstance().getWorldY(gy), z, GeoData.getInstance().getWorldX(tgx), GeoData.getInstance().getWorldY(tgy), z);
		
		final LinePointIterator iter = new LinePointIterator(gx, gy, tgx, tgy);
		
		while (iter.next())
		{
			prim.addPoint(Color.RED, GeoData.getInstance().getWorldX(iter.x()), GeoData.getInstance().getWorldY(iter.y()), z);
		}
		player.sendPacket(prim);
	}
	
	public static void debug3DLine(L2PcInstance player, int x, int y, int z, int tx, int ty, int tz)
	{
		final int gx = GeoData.getInstance().getGeoX(x);
		final int gy = GeoData.getInstance().getGeoY(y);
		
		final int tgx = GeoData.getInstance().getGeoX(tx);
		final int tgy = GeoData.getInstance().getGeoY(ty);
		
		final ExServerPrimitive prim = new ExServerPrimitive("Debug3DLine", x, y, z);
		prim.addLine(Color.BLUE, GeoData.getInstance().getWorldX(gx), GeoData.getInstance().getWorldY(gy), z, GeoData.getInstance().getWorldX(tgx), GeoData.getInstance().getWorldY(tgy), tz);
		
		final LinePointIterator3D iter = new LinePointIterator3D(gx, gy, z, tgx, tgy, tz);
		iter.next();
		int prevX = iter.x();
		int prevY = iter.y();
		int wx = GeoData.getInstance().getWorldX(prevX);
		int wy = GeoData.getInstance().getWorldY(prevY);
		int wz = iter.z();
		prim.addPoint(Color.RED, wx, wy, wz);
		
		while (iter.next())
		{
			final int curX = iter.x();
			final int curY = iter.y();
			
			if ((curX != prevX) || (curY != prevY))
			{
				wx = GeoData.getInstance().getWorldX(curX);
				wy = GeoData.getInstance().getWorldY(curY);
				wz = iter.z();
				
				prim.addPoint(Color.RED, wx, wy, wz);
				
				prevX = curX;
				prevY = curY;
			}
		}
		player.sendPacket(prim);
	}
	
	private static Color getDirectionColor(int x, int y, int z, int nswe)
	{
		return GeoData.getInstance().checkNearestNswe(x, y, z, nswe) ? Color.GREEN : Color.RED;
	}
	
	public static void debugGrid(L2PcInstance player)
	{
		final int geoRadius = 10;
		final int blocksPerPacket = 49;
		int iBlock = blocksPerPacket;
		int iPacket = 0;
		
		ExServerPrimitive exsp = null;
		final GeoData gd = GeoData.getInstance();
		final int playerGx = gd.getGeoX(player.getX());
		final int playerGy = gd.getGeoY(player.getY());
		for (int dx = -geoRadius; dx <= geoRadius; ++dx)
		{
			for (int dy = -geoRadius; dy <= geoRadius; ++dy)
			{
				if (iBlock >= blocksPerPacket)
				{
					iBlock = 0;
					if (exsp != null)
					{
						++iPacket;
						player.sendPacket(exsp);
					}
					exsp = new ExServerPrimitive("DebugGrid_" + iPacket, player.getX(), player.getY(), -16000);
				}
				
				if (exsp == null)
				{
					throw new IllegalStateException();
				}
				
				final int gx = playerGx + dx;
				final int gy = playerGy + dy;
				
				final int x = gd.getWorldX(gx);
				final int y = gd.getWorldY(gy);
				final int z = gd.getNearestZ(gx, gy, player.getZ());
				
				// north arrow
				Color col = getDirectionColor(gx, gy, z, Cell.NSWE_NORTH);
				exsp.addLine(col, x - 1, y - 7, z, x + 1, y - 7, z);
				exsp.addLine(col, x - 2, y - 6, z, x + 2, y - 6, z);
				exsp.addLine(col, x - 3, y - 5, z, x + 3, y - 5, z);
				exsp.addLine(col, x - 4, y - 4, z, x + 4, y - 4, z);
				
				// east arrow
				col = getDirectionColor(gx, gy, z, Cell.NSWE_EAST);
				exsp.addLine(col, x + 7, y - 1, z, x + 7, y + 1, z);
				exsp.addLine(col, x + 6, y - 2, z, x + 6, y + 2, z);
				exsp.addLine(col, x + 5, y - 3, z, x + 5, y + 3, z);
				exsp.addLine(col, x + 4, y - 4, z, x + 4, y + 4, z);
				
				// south arrow
				col = getDirectionColor(gx, gy, z, Cell.NSWE_SOUTH);
				exsp.addLine(col, x - 1, y + 7, z, x + 1, y + 7, z);
				exsp.addLine(col, x - 2, y + 6, z, x + 2, y + 6, z);
				exsp.addLine(col, x - 3, y + 5, z, x + 3, y + 5, z);
				exsp.addLine(col, x - 4, y + 4, z, x + 4, y + 4, z);
				
				col = getDirectionColor(gx, gy, z, Cell.NSWE_WEST);
				exsp.addLine(col, x - 7, y - 1, z, x - 7, y + 1, z);
				exsp.addLine(col, x - 6, y - 2, z, x - 6, y + 2, z);
				exsp.addLine(col, x - 5, y - 3, z, x - 5, y + 3, z);
				exsp.addLine(col, x - 4, y - 4, z, x - 4, y + 4, z);
				
				++iBlock;
			}
		}
		
		player.sendPacket(exsp);
	}
	
	/**
	 * difference between x values: never above 1<br>
	 * difference between y values: never above 1
	 * @param lastX
	 * @param lastY
	 * @param x
	 * @param y
	 * @return
	 */
	public static int computeNswe(int lastX, int lastY, int x, int y)
	{
		if (x > lastX) // east
		{
			if (y > lastY)
			{
				return Cell.NSWE_SOUTH_EAST; // Direction.SOUTH_EAST;
			}
			else if (y < lastY)
			{
				return Cell.NSWE_NORTH_EAST; // Direction.NORTH_EAST;
			}
			else
			{
				return Cell.NSWE_EAST; // Direction.EAST;
			}
		}
		else if (x < lastX) // west
		{
			if (y > lastY)
			{
				return Cell.NSWE_SOUTH_WEST; // Direction.SOUTH_WEST;
			}
			else if (y < lastY)
			{
				return Cell.NSWE_NORTH_WEST; // Direction.NORTH_WEST;
			}
			else
			{
				return Cell.NSWE_WEST; // Direction.WEST;
			}
		}
		else if (y > lastY)
		{
			return Cell.NSWE_SOUTH; // Direction.SOUTH;
		}
		else if (y < lastY)
		{
			return Cell.NSWE_NORTH; // Direction.NORTH;
		}
		else
		{
			throw new RuntimeException();
		}
	}
}