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
package ai.npc.DragonVortex;

import java.util.ArrayList;
import java.util.List;

import com.la2eden.gameserver.datatables.SpawnTable;
import com.la2eden.gameserver.model.L2Spawn;
import com.la2eden.gameserver.model.actor.L2Npc;
import com.la2eden.gameserver.model.actor.instance.L2PcInstance;

import ai.npc.AbstractNpcAI;

/**
 * Dragon Vortex AI.
 * @author UnAfraid, improved by Adry_85 & DreamStage
 */
public final class DragonVortex extends AbstractNpcAI
{
	// NPC
	private static final int VORTEX = 32871;
	// Raids
	private static final int[][] RAIDS =
	{
		{
			25718, // Emerald Horn 29.2%
			292
		},
		{
			25719, // Dust Rider 22.4%
			224
		},
		{
			25720, // Bleeding Fly 17.6%
			176
		},
		{
			25721, // Blackdagger Wing 11.6%
			116
		},
		{
			25723, // Spike Slasher 9.2%
			92
		},
		{
			25722, // Shadow Summoner 5.6%
			56
		},
		{
			25724, // Muscle Bomber 4.4%
			44
		}
	};
	// Item
	private static final int LARGE_DRAGON_BONE = 17248;
	// Misc
	private static final int DESPAWN_DELAY = 1800000; // 30min
	
	private DragonVortex()
	{
		super(DragonVortex.class.getSimpleName(), "ai/npc");
		addStartNpc(VORTEX);
		addFirstTalkId(VORTEX);
		addTalkId(VORTEX);
	}
	
	@Override
	public String onFirstTalk(L2Npc npc, L2PcInstance player)
	{
		return "32871.html";
	}
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		if ("Spawn".equals(event))
		{
			if (hasQuestItems(player, LARGE_DRAGON_BONE))
			{
				final int chance = getRandom(1000);
				final List<int[]> unspawnedRaids = new ArrayList<>();
				final List<int[]> unspawnedCandidates = new ArrayList<>();
				int raidChanceIncrease = 0;
				
				// Iterate over all Raids and check which ones are currently spawned, sum spawned Raids chance for unspawnedRaids List distribution
				for (int[] raidsList : RAIDS)
				{
					final int raidChance = raidsList[1];
					if (checkIfNpcSpawned(raidsList[0]))
					{
						raidChanceIncrease += raidChance;
					}
					else
					{
						unspawnedRaids.add(new int[]
						{
							raidsList[0],
							raidChance
						});
					}
				}
				
				// If there are unspawnedRaids onto the new List, distribute the amount of increased chances for each one and spawn a new Raid from the new chances
				if (!unspawnedRaids.isEmpty())
				{
					final int unspawnedRaidsSize = unspawnedRaids.size();
					final int chanceIncrease = (raidChanceIncrease / unspawnedRaidsSize);
					int raidChanceValue = 0;
					
					for (int[] unspawnedRaidsList : unspawnedRaids)
					{
						raidChanceValue += unspawnedRaidsList[1] + chanceIncrease;
						unspawnedCandidates.add(new int[]
						{
							unspawnedRaidsList[0],
							raidChanceValue
						});
					}
					
					for (int[] unspawnedCandidatesList : unspawnedCandidates)
					{
						if (chance <= unspawnedCandidatesList[1])
						{
							spawnRaid(unspawnedCandidatesList[0], npc, player);
							break;
						}
					}
					return null;
				}
				return "32871-noboss.html";
			}
			return "32871-no.html";
		}
		return super.onAdvEvent(event, npc, player);
	}
	
	/**
	 * Method used for spawning a Dragon Vortex Raid and take a Large Dragon Bone from the Player
	 * @param raidId
	 * @param npc
	 * @param player
	 */
	public void spawnRaid(int raidId, L2Npc npc, L2PcInstance player)
	{
		final L2Spawn spawnDat = addSpawn(raidId, npc.getX() + getRandom(-500, 500), npc.getY() + getRandom(-500, 500), npc.getZ() + 10, 0, false, DESPAWN_DELAY, true).getSpawn();
		SpawnTable.getInstance().addNewSpawn(spawnDat, false);
		takeItems(player, LARGE_DRAGON_BONE, 1);
	}
	
	/**
	 * Method used for checking if npc is spawned
	 * @param npcId
	 * @return if npc is spawned
	 */
	public boolean checkIfNpcSpawned(int npcId)
	{
		for (L2Spawn spawn : SpawnTable.getInstance().getSpawns(npcId))
		{
			final L2Npc spawnedWarpgate = spawn.getLastSpawn();
			if ((spawnedWarpgate != null))
			{
				return true;
			}
		}
		return false;
	}
	
	public static void main(String[] args)
	{
		new DragonVortex();
	}
}