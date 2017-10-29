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
package hellbound.AI.NPC.Kanaf;

import com.la2eden.gameserver.model.actor.L2Npc;
import com.la2eden.gameserver.model.actor.instance.L2PcInstance;

import ai.npc.AbstractNpcAI;

/**
 * Kanaf AI.
 * @author GKR
 */
public final class Kanaf extends AbstractNpcAI
{
	// NPCs
	private static final int KANAF = 32346;
	
	public Kanaf()
	{
		super(Kanaf.class.getSimpleName(), "hellbound/AI/NPC");
		addStartNpc(KANAF);
		addTalkId(KANAF);
		addFirstTalkId(KANAF);
	}
	
	@Override
	public final String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		if (event.equals("info"))
		{
			return "32346-0" + getRandom(1, 3) + ".htm";
		}
		return super.onAdvEvent(event, npc, player);
	}
}