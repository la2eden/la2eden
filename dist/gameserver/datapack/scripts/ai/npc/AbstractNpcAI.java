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
package ai.npc;

import com.la2eden.gameserver.enums.ChatType;
import com.la2eden.gameserver.model.actor.L2Npc;
import com.la2eden.gameserver.model.actor.instance.L2MonsterInstance;
import com.la2eden.gameserver.model.actor.instance.L2PcInstance;
import com.la2eden.gameserver.model.holders.MinionHolder;
import com.la2eden.gameserver.model.quest.Quest;
import com.la2eden.gameserver.network.NpcStringId;
import com.la2eden.gameserver.network.serverpackets.NpcSay;
import com.la2eden.gameserver.util.Broadcast;

/**
 * Abstract NPC AI class for datapack based AIs.
 * @author UnAfraid, Zoey76
 */
public abstract class AbstractNpcAI extends Quest
{
	public AbstractNpcAI(String name, String descr)
	{
		super(-1, name, descr);
	}
	
	/**
	 * Simple on first talk event handler.
	 */
	@Override
	public String onFirstTalk(L2Npc npc, L2PcInstance player)
	{
		return npc.getId() + ".html";
	}
	
	/**
	 * Registers the following events to the current script:<br>
	 * <ul>
	 * <li>ON_ATTACK</li>
	 * <li>ON_KILL</li>
	 * <li>ON_SPAWN</li>
	 * <li>ON_SPELL_FINISHED</li>
	 * <li>ON_SKILL_SEE</li>
	 * <li>ON_FACTION_CALL</li>
	 * <li>ON_AGGR_RANGE_ENTER</li>
	 * </ul>
	 * @param mobs
	 */
	public void registerMobs(int... mobs)
	{
		addAttackId(mobs);
		addKillId(mobs);
		addSpawnId(mobs);
		addSpellFinishedId(mobs);
		addSkillSeeId(mobs);
		addAggroRangeEnterId(mobs);
		addFactionCallId(mobs);
	}
	
	/**
	 * Broadcasts NpcSay packet to all known players with custom string.
	 * @param npc
	 * @param type
	 * @param text
	 */
	protected void broadcastNpcSay(L2Npc npc, ChatType type, String text)
	{
		Broadcast.toKnownPlayers(npc, new NpcSay(npc.getObjectId(), type, npc.getTemplate().getDisplayId(), text));
	}
	
	/**
	 * Broadcasts NpcSay packet to all known players with npc string id.
	 * @param npc
	 * @param type
	 * @param stringId
	 */
	protected void broadcastNpcSay(L2Npc npc, ChatType type, NpcStringId stringId)
	{
		Broadcast.toKnownPlayers(npc, new NpcSay(npc.getObjectId(), type, npc.getTemplate().getDisplayId(), stringId));
	}
	
	/**
	 * Broadcasts NpcSay packet to all known players with npc string id.
	 * @param npc
	 * @param type
	 * @param stringId
	 * @param parameters
	 */
	protected void broadcastNpcSay(L2Npc npc, ChatType type, NpcStringId stringId, String... parameters)
	{
		final NpcSay say = new NpcSay(npc.getObjectId(), type, npc.getTemplate().getDisplayId(), stringId);
		if (parameters != null)
		{
			for (String parameter : parameters)
			{
				say.addStringParameter(parameter);
			}
		}
		Broadcast.toKnownPlayers(npc, say);
	}
	
	/**
	 * Broadcasts NpcSay packet to all known players with npc string id in specific radius.
	 * @param npc
	 * @param type
	 * @param stringId
	 * @param radius
	 */
	protected void broadcastNpcSay(L2Npc npc, ChatType type, NpcStringId stringId, int radius)
	{
		Broadcast.toKnownPlayersInRadius(npc, new NpcSay(npc.getObjectId(), type, npc.getTemplate().getDisplayId(), stringId), radius);
	}
	
	public void spawnMinions(L2Npc npc, String spawnName)
	{
		for (MinionHolder is : npc.getTemplate().getParameters().getMinionList(spawnName))
		{
			addMinion((L2MonsterInstance) npc, is.getId());
		}
	}
}