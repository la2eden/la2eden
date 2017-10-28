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
package com.la2eden.gameserver.model;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import com.la2eden.gameserver.ai.CtrlIntention;
import com.la2eden.gameserver.ai.L2ControllableMobAI;
import com.la2eden.gameserver.datatables.SpawnTable;
import com.la2eden.gameserver.model.actor.L2Character;
import com.la2eden.gameserver.model.actor.instance.L2ControllableMobInstance;
import com.la2eden.gameserver.model.actor.instance.L2PcInstance;
import com.la2eden.gameserver.model.actor.templates.L2NpcTemplate;
import com.la2eden.util.Rnd;

/**
 * @author littlecrow
 */
public final class MobGroup
{
	private final L2NpcTemplate _npcTemplate;
	private final int _groupId;
	private final int _maxMobCount;
	
	private List<L2ControllableMobInstance> _mobs;
	
	public MobGroup(int groupId, L2NpcTemplate npcTemplate, int maxMobCount)
	{
		_groupId = groupId;
		_npcTemplate = npcTemplate;
		_maxMobCount = maxMobCount;
	}
	
	public int getActiveMobCount()
	{
		return getMobs().size();
	}
	
	public int getGroupId()
	{
		return _groupId;
	}
	
	public int getMaxMobCount()
	{
		return _maxMobCount;
	}
	
	public List<L2ControllableMobInstance> getMobs()
	{
		if (_mobs == null)
		{
			_mobs = new CopyOnWriteArrayList<>();
		}
		
		return _mobs;
	}
	
	public String getStatus()
	{
		try
		{
			switch (((L2ControllableMobAI) getMobs().get(0).getAI()).getAlternateAI())
			{
				case L2ControllableMobAI.AI_NORMAL:
				{
					return "Idle";
				}
				case L2ControllableMobAI.AI_FORCEATTACK:
				{
					return "Force Attacking";
				}
				case L2ControllableMobAI.AI_FOLLOW:
				{
					return "Following";
				}
				case L2ControllableMobAI.AI_CAST:
				{
					return "Casting";
				}
				case L2ControllableMobAI.AI_ATTACK_GROUP:
				{
					return "Attacking Group";
				}
				default:
				{
					return "Idle";
				}
			}
		}
		catch (Exception e)
		{
			return "Unspawned";
		}
	}
	
	public L2NpcTemplate getTemplate()
	{
		return _npcTemplate;
	}
	
	public boolean isGroupMember(L2ControllableMobInstance mobInst)
	{
		for (L2ControllableMobInstance groupMember : getMobs())
		{
			if (groupMember == null)
			{
				continue;
			}
			
			if (groupMember.getObjectId() == mobInst.getObjectId())
			{
				return true;
			}
		}
		
		return false;
	}
	
	public void spawnGroup(int x, int y, int z)
	{
		if (getActiveMobCount() > 0)
		{
			return;
		}
		
		try
		{
			for (int i = 0; i < getMaxMobCount(); i++)
			{
				final L2GroupSpawn spawn = new L2GroupSpawn(getTemplate());
				
				final int signX = (Rnd.nextInt(2) == 0) ? -1 : 1;
				final int signY = (Rnd.nextInt(2) == 0) ? -1 : 1;
				final int randX = Rnd.nextInt(MobGroupTable.RANDOM_RANGE);
				final int randY = Rnd.nextInt(MobGroupTable.RANDOM_RANGE);
				
				spawn.setX(x + (signX * randX));
				spawn.setY(y + (signY * randY));
				spawn.setZ(z);
				spawn.stopRespawn();
				
				SpawnTable.getInstance().addNewSpawn(spawn, false);
				getMobs().add((L2ControllableMobInstance) spawn.doGroupSpawn());
			}
		}
		catch (ClassNotFoundException e)
		{
		}
		catch (NoSuchMethodException e2)
		{
		}
	}
	
	public void spawnGroup(L2PcInstance activeChar)
	{
		spawnGroup(activeChar.getX(), activeChar.getY(), activeChar.getZ());
	}
	
	public void teleportGroup(L2PcInstance player)
	{
		removeDead();
		
		for (L2ControllableMobInstance mobInst : getMobs())
		{
			if (mobInst == null)
			{
				continue;
			}
			
			if (!mobInst.isDead())
			{
				final int x = player.getX() + Rnd.nextInt(50);
				final int y = player.getY() + Rnd.nextInt(50);
				
				mobInst.teleToLocation(new Location(x, y, player.getZ()), true);
				((L2ControllableMobAI) mobInst.getAI()).follow(player);
			}
		}
	}
	
	public L2ControllableMobInstance getRandomMob()
	{
		removeDead();
		return getActiveMobCount() == 0 ? null : getMobs().get(Rnd.nextInt(getActiveMobCount()));
	}
	
	public void unspawnGroup()
	{
		removeDead();
		
		if (getActiveMobCount() == 0)
		{
			return;
		}
		
		for (L2ControllableMobInstance mobInst : getMobs())
		{
			if (mobInst == null)
			{
				continue;
			}
			
			if (!mobInst.isDead())
			{
				mobInst.deleteMe();
			}
			
			SpawnTable.getInstance().deleteSpawn(mobInst.getSpawn(), false);
		}
		
		getMobs().clear();
	}
	
	public void killGroup(L2PcInstance activeChar)
	{
		removeDead();
		
		for (L2ControllableMobInstance mobInst : getMobs())
		{
			if (mobInst == null)
			{
				continue;
			}
			
			if (!mobInst.isDead())
			{
				mobInst.reduceCurrentHp(mobInst.getMaxHp() + 1, activeChar, null);
			}
			
			SpawnTable.getInstance().deleteSpawn(mobInst.getSpawn(), false);
		}
		
		getMobs().clear();
	}
	
	public void setAttackRandom()
	{
		removeDead();
		
		for (L2ControllableMobInstance mobInst : getMobs())
		{
			if (mobInst == null)
			{
				continue;
			}
			
			final L2ControllableMobAI ai = (L2ControllableMobAI) mobInst.getAI();
			ai.setAlternateAI(L2ControllableMobAI.AI_NORMAL);
			ai.setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
		}
	}
	
	public void setAttackTarget(L2Character target)
	{
		removeDead();
		
		for (L2ControllableMobInstance mobInst : getMobs())
		{
			if (mobInst == null)
			{
				continue;
			}
			
			((L2ControllableMobAI) mobInst.getAI()).forceAttack(target);
		}
	}
	
	public void setIdleMode()
	{
		removeDead();
		
		for (L2ControllableMobInstance mobInst : getMobs())
		{
			if (mobInst == null)
			{
				continue;
			}
			
			((L2ControllableMobAI) mobInst.getAI()).stop();
		}
	}
	
	public void returnGroup(L2Character activeChar)
	{
		setIdleMode();
		
		for (L2ControllableMobInstance mobInst : getMobs())
		{
			if (mobInst == null)
			{
				continue;
			}
			
			((L2ControllableMobAI) mobInst.getAI()).move(activeChar.getX() + (((Rnd.nextInt(2) == 0) ? -1 : 1) * Rnd.nextInt(MobGroupTable.RANDOM_RANGE)), activeChar.getY() + (((Rnd.nextInt(2) == 0) ? -1 : 1) * Rnd.nextInt(MobGroupTable.RANDOM_RANGE)), activeChar.getZ());
		}
	}
	
	public void setFollowMode(L2Character character)
	{
		removeDead();
		
		for (L2ControllableMobInstance mobInst : getMobs())
		{
			if (mobInst == null)
			{
				continue;
			}
			
			((L2ControllableMobAI) mobInst.getAI()).follow(character);
		}
	}
	
	public void setCastMode()
	{
		removeDead();
		
		for (L2ControllableMobInstance mobInst : getMobs())
		{
			if (mobInst == null)
			{
				continue;
			}
			
			((L2ControllableMobAI) mobInst.getAI()).setAlternateAI(L2ControllableMobAI.AI_CAST);
		}
	}
	
	public void setNoMoveMode(boolean enabled)
	{
		removeDead();
		
		for (L2ControllableMobInstance mobInst : getMobs())
		{
			if (mobInst == null)
			{
				continue;
			}
			
			((L2ControllableMobAI) mobInst.getAI()).setNotMoving(enabled);
		}
	}
	
	protected void removeDead()
	{
		final List<L2ControllableMobInstance> deadMobs = new LinkedList<>();
		for (L2ControllableMobInstance mobInst : getMobs())
		{
			if ((mobInst != null) && mobInst.isDead())
			{
				deadMobs.add(mobInst);
			}
		}
		
		getMobs().removeAll(deadMobs);
	}
	
	public void setInvul(boolean invulState)
	{
		removeDead();
		
		for (L2ControllableMobInstance mobInst : getMobs())
		{
			if (mobInst != null)
			{
				mobInst.setInvul(invulState);
			}
		}
	}
	
	public void setAttackGroup(MobGroup otherGrp)
	{
		removeDead();
		
		for (L2ControllableMobInstance mobInst : getMobs())
		{
			if (mobInst == null)
			{
				continue;
			}
			
			final L2ControllableMobAI ai = (L2ControllableMobAI) mobInst.getAI();
			ai.forceAttackGroup(otherGrp);
			ai.setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
		}
	}
}