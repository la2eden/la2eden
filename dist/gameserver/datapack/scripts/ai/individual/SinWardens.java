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
package ai.individual;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.la2eden.gameserver.enums.ChatType;
import com.la2eden.gameserver.model.actor.L2Npc;
import com.la2eden.gameserver.model.actor.instance.L2MonsterInstance;
import com.la2eden.gameserver.model.actor.instance.L2PcInstance;
import com.la2eden.gameserver.network.NpcStringId;
import com.la2eden.gameserver.network.serverpackets.NpcSay;

import ai.npc.AbstractNpcAI;

/**
 * Manages Sin Wardens disappearing and chat.
 * @author GKR
 */
public final class SinWardens extends AbstractNpcAI
{
	private static final int[] SIN_WARDEN_MINIONS =
	{
		22424,
		22425,
		22426,
		22427,
		22428,
		22429,
		22430,
		22432,
		22433,
		22434,
		22435,
		22436,
		22437,
		22438
	};
	
	private final Map<Integer, Integer> killedMinionsCount = new ConcurrentHashMap<>();
	
	private SinWardens()
	{
		super(SinWardens.class.getSimpleName(), "ai/individual");
		addKillId(SIN_WARDEN_MINIONS);
	}
	
	@Override
	public String onKill(L2Npc npc, L2PcInstance killer, boolean isSummon)
	{
		if (npc.isMinion())
		{
			final L2MonsterInstance master = ((L2MonsterInstance) npc).getLeader();
			if ((master != null) && !master.isDead())
			{
				int killedCount = killedMinionsCount.containsKey(master.getObjectId()) ? killedMinionsCount.get(master.getObjectId()) : 0;
				killedCount++;
				
				if (killedCount == 5)
				{
					master.broadcastPacket(new NpcSay(master.getObjectId(), ChatType.NPC_GENERAL, master.getId(), NpcStringId.WE_MIGHT_NEED_NEW_SLAVES_I_LL_BE_BACK_SOON_SO_WAIT));
					master.doDie(killer);
					killedMinionsCount.remove(master.getObjectId());
				}
				else
				{
					killedMinionsCount.put(master.getObjectId(), killedCount);
				}
			}
		}
		return super.onKill(npc, killer, isSummon);
	}
	
	public static void main(String[] args)
	{
		new SinWardens();
	}
}
