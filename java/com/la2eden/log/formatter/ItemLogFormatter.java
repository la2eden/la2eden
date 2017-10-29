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
package com.la2eden.log.formatter;

import java.util.logging.LogRecord;

import com.la2eden.Config;
import com.la2eden.gameserver.model.items.instance.L2ItemInstance;

/**
 * @author Advi
 */
public class ItemLogFormatter extends AbstractFormatter
{
	@Override
	public String format(LogRecord record)
	{
		final Object[] params = record.getParameters();
		final StringBuilder output = new StringBuilder(32 + record.getMessage().length() + (params != null ? 10 * params.length : 0));
		output.append(super.format(record));
		
		for (Object p : record.getParameters())
		{
			if (p == null)
			{
				continue;
			}
			output.append(", ");
			if (p instanceof L2ItemInstance)
			{
				L2ItemInstance item = (L2ItemInstance) p;
				output.append("item ");
				output.append(item.getObjectId());
				output.append(":");
				if (item.getEnchantLevel() > 0)
				{
					output.append("+");
					output.append(item.getEnchantLevel());
					output.append(" ");
				}
				
				output.append(item.getItem().getName());
				output.append("(");
				output.append(item.getCount());
				output.append(")");
			}
			else
			{
				output.append(p);
			}
		}
		output.append(Config.EOL);
		
		return output.toString();
	}
}
