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
import com.la2eden.gameserver.model.actor.L2Attackable;
import com.la2eden.gameserver.model.actor.L2Character;
import com.la2eden.gameserver.model.actor.L2Summon;
import com.la2eden.gameserver.model.actor.instance.L2PcInstance;
import com.la2eden.gameserver.model.skills.Skill;

public class DamageFormatter extends AbstractFormatter
{
	@Override
	public String format(LogRecord record)
	{
		final Object[] params = record.getParameters();
		final StringBuilder output = new StringBuilder(32 + record.getMessage().length() + (params != null ? 10 * params.length : 0));
		output.append(super.format(record));
		
		if (params != null)
		{
			for (Object p : params)
			{
				if (p == null)
				{
					continue;
				}
				
				if (p instanceof L2Character)
				{
					final L2Character creature = (L2Character) p;
					if ((p instanceof L2Attackable) && ((L2Attackable) p).isRaid())
					{
						output.append("RaidBoss ");
					}
					
					output.append(creature.getName());
					output.append("(");
					output.append(creature.getObjectId());
					output.append(") ");
					output.append(creature.getLevel());
					output.append(" lvl");
					
					if (p instanceof L2Summon)
					{
						L2PcInstance owner = ((L2Summon) p).getOwner();
						if (owner != null)
						{
							output.append(" Owner:");
							output.append(owner.getName());
							output.append("(");
							output.append(owner.getObjectId());
							output.append(")");
						}
					}
				}
				else if (p instanceof Skill)
				{
					output.append(" with skill ");
					output.append(p);
				}
				else
				{
					output.append(p);
				}
			}
		}
		
		output.append(Config.EOL);
		return output.toString();
	}
}
