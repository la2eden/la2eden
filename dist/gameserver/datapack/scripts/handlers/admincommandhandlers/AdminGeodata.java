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
package handlers.admincommandhandlers;

import java.util.StringTokenizer;

import com.la2eden.gameserver.GeoData;
import com.la2eden.gameserver.handler.IAdminCommandHandler;
import com.la2eden.gameserver.model.L2Object;
import com.la2eden.gameserver.model.L2World;
import com.la2eden.gameserver.model.actor.instance.L2PcInstance;
import com.la2eden.gameserver.network.SystemMessageId;
import com.la2eden.gameserver.network.serverpackets.SystemMessage;
import com.la2eden.gameserver.util.GeoUtils;

/**
 * @author -Nemesiss-, HorridoJoho
 */
public class AdminGeodata implements IAdminCommandHandler
{
	private static final String[] ADMIN_COMMANDS =
	{
		"admin_geo_pos",
		"admin_geo_spawn_pos",
		"admin_geo_can_move",
		"admin_geo_can_see",
		"admin_geogrid",
		"admin_geomap"
	};
	
	@Override
	public boolean useAdminCommand(String command, L2PcInstance activeChar)
	{
		final StringTokenizer st = new StringTokenizer(command, " ");
		final String actualCommand = st.nextToken();
		switch (actualCommand.toLowerCase())
		{
			case "admin_geo_pos":
			{
				final int worldX = activeChar.getX();
				final int worldY = activeChar.getY();
				final int worldZ = activeChar.getZ();
				final int geoX = GeoData.getInstance().getGeoX(worldX);
				final int geoY = GeoData.getInstance().getGeoY(worldY);
				
				if (GeoData.getInstance().hasGeoPos(geoX, geoY))
				{
					activeChar.sendMessage("WorldX: " + worldX + ", WorldY: " + worldY + ", WorldZ: " + worldZ + ", GeoX: " + geoX + ", GeoY: " + geoY + ", GeoZ: " + GeoData.getInstance().getNearestZ(geoX, geoY, worldZ));
				}
				else
				{
					activeChar.sendMessage("There is no geodata at this position.");
				}
				break;
			}
			case "admin_geo_spawn_pos":
			{
				final int worldX = activeChar.getX();
				final int worldY = activeChar.getY();
				final int worldZ = activeChar.getZ();
				final int geoX = GeoData.getInstance().getGeoX(worldX);
				final int geoY = GeoData.getInstance().getGeoY(worldY);
				
				if (GeoData.getInstance().hasGeoPos(geoX, geoY))
				{
					activeChar.sendMessage("WorldX: " + worldX + ", WorldY: " + worldY + ", WorldZ: " + worldZ + ", GeoX: " + geoX + ", GeoY: " + geoY + ", GeoZ: " + GeoData.getInstance().getSpawnHeight(worldX, worldY, worldZ));
				}
				else
				{
					activeChar.sendMessage("There is no geodata at this position.");
				}
				break;
			}
			case "admin_geo_can_move":
			{
				final L2Object target = activeChar.getTarget();
				if (target != null)
				{
					if (GeoData.getInstance().canSeeTarget(activeChar, target))
					{
						activeChar.sendMessage("Can move beeline.");
					}
					else
					{
						activeChar.sendMessage("Can not move beeline!");
					}
				}
				else
				{
					activeChar.sendPacket(SystemMessageId.INVALID_TARGET);
				}
				break;
			}
			case "admin_geo_can_see":
			{
				final L2Object target = activeChar.getTarget();
				if (target != null)
				{
					if (GeoData.getInstance().canSeeTarget(activeChar, target))
					{
						activeChar.sendMessage("Can see target.");
					}
					else
					{
						activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.CANNOT_SEE_TARGET));
					}
				}
				else
				{
					activeChar.sendPacket(SystemMessageId.INVALID_TARGET);
				}
				break;
			}
			case "admin_geogrid":
			{
				GeoUtils.debugGrid(activeChar);
				break;
			}
			case "admin_geomap":
			{
				final int x = ((activeChar.getX() - L2World.MAP_MIN_X) >> 15) + L2World.TILE_X_MIN;
				final int y = ((activeChar.getY() - L2World.MAP_MIN_Y) >> 15) + L2World.TILE_Y_MIN;
				activeChar.sendMessage("GeoMap: " + x + "_" + y);
				break;
			}
		}
		return true;
	}
	
	@Override
	public String[] getAdminCommandList()
	{
		return ADMIN_COMMANDS;
	}
}
