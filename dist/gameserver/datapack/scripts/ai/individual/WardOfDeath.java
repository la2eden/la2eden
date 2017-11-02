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
package ai.individual;

import ai.npc.AbstractNpcAI;
import com.la2eden.gameserver.enums.ChatType;
import com.la2eden.gameserver.model.actor.L2Npc;
import com.la2eden.gameserver.model.actor.instance.L2PcInstance;
import com.la2eden.gameserver.model.holders.SkillHolder;
import com.la2eden.gameserver.network.NpcStringId;
import com.la2eden.util.Rnd;

/**
 * Ward Of Death AI.
 * @author Sacrifice
 * @since 2.6.0.0
 */
public final class WardOfDeath extends AbstractNpcAI
{
    private static final int WARD_OF_DEATH_NPC_ID_1 = 18667;
    private static final int WARD_OF_DEATH_NPC_ID_2 = 18668;

    private static final int[] TRAP_MOBS = {
        22516, // Butcher of Infinity
        22520, // Body Severer
        22522, // Soul Devourer
        22524, // Emissary of Death
        22526, // Butcher of Infinity
        22532 // Law Scholar of Conclusions
    };

    private static final SkillHolder[] TRAP_SKILLS =
    {
    	new SkillHolder(5423, 9), // Poison Level 9
    	new SkillHolder(5424, 9) // Bleed Level 9
    };

    private WardOfDeath()
    {
        super(WardOfDeath.class.getSimpleName(), "ai/individual");
    	addSpawnId(WARD_OF_DEATH_NPC_ID_1, WARD_OF_DEATH_NPC_ID_2);
    	addAggroRangeEnterId(WARD_OF_DEATH_NPC_ID_1, WARD_OF_DEATH_NPC_ID_2);
    }

    @Override
    public String onSpawn(L2Npc npc)
    {
        npc.setIsImmobilized(true);
        return super.onSpawn(npc);
    }

    @Override
    public String onAggroRangeEnter(L2Npc npc, L2PcInstance player, boolean isSummon)
    {
        if (player.isInsideRadius(npc.getLocation(), npc.getAggroRange(), true, false))
        {
        	broadcastNpcSay(npc, ChatType.NPC_GENERAL, NpcStringId.INTRUDER_DETECTED);

        	switch (npc.getId())
        	{
        		case WARD_OF_DEATH_NPC_ID_1:
        			npc.doCast(TRAP_SKILLS[Rnd.get(0, TRAP_SKILLS.length - 1)].getSkill());
        			npc.doDie(npc);
        			break;
        		case WARD_OF_DEATH_NPC_ID_2:
        			for (int i = 0; i < Rnd.get(1, 4); i++)
        			{
        				addSpawn(TRAP_MOBS[Rnd.get(0, TRAP_MOBS.length - 1)], npc.getLocation().getX(), npc.getLocation().getY(), npc.getLocation().getZ(), 0, true, 0, false, npc.getInstanceId());
        			}
        			npc.doDie(npc);
        			break;
        	}
        }

        return super.onAggroRangeEnter(npc, player, isSummon);
    }

    public static void main(String[] args)
    {
        new WardOfDeath();
    }
}
