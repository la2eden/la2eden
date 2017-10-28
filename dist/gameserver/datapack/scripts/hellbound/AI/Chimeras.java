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
package hellbound.AI;

import com.la2eden.Config;
import com.la2eden.gameserver.ThreadPoolManager;
import com.la2eden.gameserver.model.L2Object;
import com.la2eden.gameserver.model.Location;
import com.la2eden.gameserver.model.actor.L2Npc;
import com.la2eden.gameserver.model.actor.instance.L2PcInstance;
import com.la2eden.gameserver.model.skills.Skill;

import ai.npc.AbstractNpcAI;
import hellbound.HellboundEngine;

/**
 * Chimeras AI.
 * @author DS
 */
public final class Chimeras extends AbstractNpcAI
{
	// NPCs
	private static final int[] NPCS =
	{
		22349, // Chimera of Earth
		22350, // Chimera of Darkness
		22351, // Chimera of Wind
		22352, // Chimera of Fire
	};
	private static final int CELTUS = 22353;
	// Locations
	private static final Location[] LOCATIONS =
	{
		new Location(3678, 233418, -3319),
		new Location(2038, 237125, -3363),
		new Location(7222, 240617, -2033),
		new Location(9969, 235570, -1993)
	};
	// Skills
	private static final int BOTTLE = 2359; // Magic Bottle
	// Items
	private static final int DIM_LIFE_FORCE = 9680;
	private static final int LIFE_FORCE = 9681;
	private static final int CONTAINED_LIFE_FORCE = 9682;
	// Misc
	private static final int CONTAINED_LIFE_FORCE_AMOUNT = Config.RATE_DEATH_DROP_AMOUNT_MULTIPLIER > 1 ? (int) Config.RATE_DEATH_DROP_AMOUNT_MULTIPLIER : 1; // Retail value is 1
	
	public Chimeras()
	{
		super(Chimeras.class.getSimpleName(), "hellbound/AI");
		addSkillSeeId(NPCS);
		addSpawnId(CELTUS);
		addSkillSeeId(CELTUS);
	}
	
	@Override
	public final String onSpawn(L2Npc npc)
	{
		if (HellboundEngine.getInstance().getLevel() == 7) // Have random spawn points only in 7 lvl
		{
			final Location loc = LOCATIONS[getRandom(LOCATIONS.length)];
			if (!npc.isInsideRadius(loc, 200, false, false))
			{
				npc.getSpawn().setLocation(loc);
				ThreadPoolManager.getInstance().scheduleGeneral(new Teleport(npc, loc), 100);
			}
		}
		return super.onSpawn(npc);
	}
	
	@Override
	public final String onSkillSee(L2Npc npc, L2PcInstance caster, Skill skill, L2Object[] targets, boolean isSummon)
	{
		if ((skill.getId() == BOTTLE) && !npc.isDead())
		{
			if ((targets.length > 0) && (targets[0] == npc))
			{
				if (npc.getCurrentHp() < (npc.getMaxHp() * 0.1))
				{
					if (HellboundEngine.getInstance().getLevel() == 7)
					{
						HellboundEngine.getInstance().updateTrust(3, true);
					}
					
					npc.setIsDead(true);
					if (npc.getId() == CELTUS)
					{
						npc.dropItem(caster, CONTAINED_LIFE_FORCE, CONTAINED_LIFE_FORCE_AMOUNT);
					}
					else
					{
						if (getRandom(100) < 80)
						{
							npc.dropItem(caster, DIM_LIFE_FORCE, 1);
						}
						else if (getRandom(100) < 80)
						{
							npc.dropItem(caster, LIFE_FORCE, 1);
						}
					}
					npc.onDecay();
				}
			}
		}
		return super.onSkillSee(npc, caster, skill, targets, isSummon);
	}
	
	private static class Teleport implements Runnable
	{
		private final L2Npc _npc;
		private final Location _loc;
		
		public Teleport(L2Npc npc, Location loc)
		{
			_npc = npc;
			_loc = loc;
		}
		
		@Override
		public void run()
		{
			_npc.teleToLocation(_loc, false);
		}
	}
}