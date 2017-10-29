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
package com.la2eden.gameserver.model.zone.form;

import com.la2eden.gameserver.GeoData;
import com.la2eden.gameserver.model.itemcontainer.Inventory;
import com.la2eden.gameserver.model.zone.L2ZoneForm;
import com.la2eden.util.Rnd;

/**
 * A primitive circular zone
 * @author durgus
 */
public class ZoneCylinder extends L2ZoneForm
{
	private final int _x, _y, _z1, _z2, _rad, _radS;
	
	public ZoneCylinder(int x, int y, int z1, int z2, int rad)
	{
		_x = x;
		_y = y;
		_z1 = z1;
		_z2 = z2;
		_rad = rad;
		_radS = rad * rad;
	}
	
	@Override
	public boolean isInsideZone(int x, int y, int z)
	{
		return ((Math.pow(_x - x, 2) + Math.pow(_y - y, 2)) <= _radS) && (z >= _z1) && (z <= _z2);
	}
	
	@Override
	public boolean intersectsRectangle(int ax1, int ax2, int ay1, int ay2)
	{
		return ((_x > ax1) && (_x < ax2) && (_y > ay1) && (_y < ay2)) || //
			((Math.pow(ax1 - _x, 2) + Math.pow(ay1 - _y, 2)) < _radS) || //
			((Math.pow(ax1 - _x, 2) + Math.pow(ay2 - _y, 2)) < _radS) || //
			((Math.pow(ax2 - _x, 2) + Math.pow(ay1 - _y, 2)) < _radS) || //
			((Math.pow(ax2 - _x, 2) + Math.pow(ay2 - _y, 2)) < _radS) || //
			((_x > ax1) && (_x < ax2) && ((Math.abs(_y - ay2) < _rad) || (Math.abs(_y - ay1) < _rad))) || //
			((_y > ay1) && (_y < ay2) && ((Math.abs(_x - ax2) < _rad) || (Math.abs(_x - ax1) < _rad)));
	}
	
	@Override
	public double getDistanceToZone(int x, int y)
	{
		return Math.sqrt(Math.pow(_x - x, 2) + Math.pow(_y - y, 2)) - _rad;
	}
	
	// getLowZ() / getHighZ() - These two functions were added to cope with the demand of the new fishing algorithms, wich are now able to correctly place the hook in the water, thanks to getHighZ(). getLowZ() was added, considering potential future modifications.
	@Override
	public int getLowZ()
	{
		return _z1;
	}
	
	@Override
	public int getHighZ()
	{
		return _z2;
	}
	
	@Override
	public void visualizeZone(int z)
	{
		final int count = (int) ((2 * Math.PI * _rad) / STEP);
		final double angle = (2 * Math.PI) / count;
		for (int i = 0; i < count; i++)
		{
			dropDebugItem(Inventory.ADENA_ID, 1, _x + (int) (Math.cos(angle * i) * _rad), _y + (int) (Math.sin(angle * i) * _rad), z);
		}
	}
	
	@Override
	public int[] getRandomPoint()
	{
		double x, y;
		final double q = Rnd.get() * 2 * Math.PI;
		final double r = Math.sqrt(Rnd.get());
		
		x = (_rad * r * Math.cos(q)) + _x;
		y = (_rad * r * Math.sin(q)) + _y;
		
		return new int[]
		{
			(int) x,
			(int) y,
			GeoData.getInstance().getHeight((int) x, (int) y, _z1)
		};
	}
}
