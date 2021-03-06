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
package com.la2eden.gameserver.ai;

import static com.la2eden.gameserver.ai.CtrlIntention.AI_INTENTION_ACTIVE;
import static com.la2eden.gameserver.ai.CtrlIntention.AI_INTENTION_ATTACK;

import java.util.List;
import java.util.stream.Collectors;

import com.la2eden.gameserver.model.L2Object;
import com.la2eden.gameserver.model.MobGroup;
import com.la2eden.gameserver.model.MobGroupTable;
import com.la2eden.gameserver.model.actor.L2Attackable;
import com.la2eden.gameserver.model.actor.L2Character;
import com.la2eden.gameserver.model.actor.L2Npc;
import com.la2eden.gameserver.model.actor.L2Playable;
import com.la2eden.gameserver.model.actor.instance.L2ControllableMobInstance;
import com.la2eden.gameserver.model.actor.instance.L2DoorInstance;
import com.la2eden.gameserver.model.actor.instance.L2NpcInstance;
import com.la2eden.gameserver.model.actor.instance.L2PcInstance;
import com.la2eden.gameserver.model.skills.Skill;
import com.la2eden.gameserver.util.Util;
import com.la2eden.util.Rnd;

/**
 * AI for controllable mobs
 * @author littlecrow
 */
public final class L2ControllableMobAI extends L2AttackableAI
{
	public static final int AI_IDLE = 1;
	public static final int AI_NORMAL = 2;
	public static final int AI_FORCEATTACK = 3;
	public static final int AI_FOLLOW = 4;
	public static final int AI_CAST = 5;
	public static final int AI_ATTACK_GROUP = 6;
	
	private int _alternateAI;
	
	private boolean _isThinking; // to prevent thinking recursively
	private boolean _isNotMoving;
	
	private L2Character _forcedTarget;
	private MobGroup _targetGroup;
	
	public L2ControllableMobAI(L2ControllableMobInstance creature)
	{
		super(creature);
		setAlternateAI(AI_IDLE);
	}
	
	protected void thinkFollow()
	{
		if (!Util.checkIfInRange(MobGroupTable.FOLLOW_RANGE, _actor, getForcedTarget(), true))
		{
			moveTo(getForcedTarget().getX() + (((Rnd.nextInt(2) == 0) ? -1 : 1) * Rnd.nextInt(MobGroupTable.FOLLOW_RANGE)), getForcedTarget().getY() + (((Rnd.nextInt(2) == 0) ? -1 : 1) * Rnd.nextInt(MobGroupTable.FOLLOW_RANGE)), getForcedTarget().getZ());
		}
	}
	
	@Override
	protected void onEvtThink()
	{
		if (isThinking())
		{
			return;
		}
		
		setThinking(true);
		
		try
		{
			switch (getAlternateAI())
			{
				case AI_IDLE:
				{
					if (getIntention() != CtrlIntention.AI_INTENTION_ACTIVE)
					{
						setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
					}
					break;
				}
				case AI_FOLLOW:
				{
					thinkFollow();
					break;
				}
				case AI_CAST:
				{
					thinkCast();
					break;
				}
				case AI_FORCEATTACK:
				{
					thinkForceAttack();
					break;
				}
				case AI_ATTACK_GROUP:
				{
					thinkAttackGroup();
					break;
				}
				default:
				{
					if (getIntention() == AI_INTENTION_ACTIVE)
					{
						thinkActive();
					}
					else if (getIntention() == AI_INTENTION_ATTACK)
					{
						thinkAttack();
					}
					break;
				}
			}
		}
		finally
		{
			setThinking(false);
		}
	}
	
	@Override
	protected void thinkCast()
	{
		final L2Attackable npc = (L2Attackable) _actor;
		if ((getAttackTarget() == null) || getAttackTarget().isAlikeDead())
		{
			setAttackTarget(findNextRndTarget());
			clientStopMoving(null);
		}
		
		if (getAttackTarget() == null)
		{
			return;
		}
		
		npc.setTarget(getAttackTarget());
		
		if (_actor.isMuted())
		{
			return;
		}
		
		int max_range = 0;
		// check distant skills
		
		for (Skill sk : _actor.getAllSkills())
		{
			if (Util.checkIfInRange(sk.getCastRange(), _actor, getAttackTarget(), true) && !_actor.isSkillDisabled(sk) && (_actor.getCurrentMp() > _actor.getStat().getMpConsume(sk)))
			{
				_actor.doCast(sk);
				return;
			}
			
			max_range = Math.max(max_range, sk.getCastRange());
		}
		
		if (!isNotMoving())
		{
			moveToPawn(getAttackTarget(), max_range);
		}
	}
	
	protected void thinkAttackGroup()
	{
		final L2Character target = getForcedTarget();
		if ((target == null) || target.isAlikeDead())
		{
			// try to get next group target
			setForcedTarget(findNextGroupTarget());
			clientStopMoving(null);
		}
		
		if (target == null)
		{
			return;
		}
		
		_actor.setTarget(target);
		// as a response, we put the target in a forced attack mode
		final L2ControllableMobInstance theTarget = (L2ControllableMobInstance) target;
		final L2ControllableMobAI ctrlAi = (L2ControllableMobAI) theTarget.getAI();
		ctrlAi.forceAttack(_actor);
		
		final double dist2 = _actor.calculateDistance(target, false, true);
		final int range = _actor.getPhysicalAttackRange() + _actor.getTemplate().getCollisionRadius() + target.getTemplate().getCollisionRadius();
		int max_range = range;
		
		if (!_actor.isMuted() && (dist2 > ((range + 20) * (range + 20))))
		{
			// check distant skills
			for (Skill sk : _actor.getAllSkills())
			{
				final int castRange = sk.getCastRange();
				
				if (((castRange * castRange) >= dist2) && !_actor.isSkillDisabled(sk) && (_actor.getCurrentMp() > _actor.getStat().getMpConsume(sk)))
				{
					_actor.doCast(sk);
					return;
				}
				
				max_range = Math.max(max_range, castRange);
			}
			
			if (!isNotMoving())
			{
				moveToPawn(target, range);
			}
			
			return;
		}
		_actor.doAttack(target);
	}
	
	protected void thinkForceAttack()
	{
		if ((getForcedTarget() == null) || getForcedTarget().isAlikeDead())
		{
			clientStopMoving(null);
			setIntention(AI_INTENTION_ACTIVE);
			setAlternateAI(AI_IDLE);
		}
		
		_actor.setTarget(getForcedTarget());
		final double dist2 = _actor.calculateDistance(getForcedTarget(), false, true);
		final int range = _actor.getPhysicalAttackRange() + _actor.getTemplate().getCollisionRadius() + getForcedTarget().getTemplate().getCollisionRadius();
		int max_range = range;
		
		if (!_actor.isMuted() && (dist2 > ((range + 20) * (range + 20))))
		{
			// check distant skills
			for (Skill sk : _actor.getAllSkills())
			{
				final int castRange = sk.getCastRange();
				
				if (((castRange * castRange) >= dist2) && !_actor.isSkillDisabled(sk) && (_actor.getCurrentMp() > _actor.getStat().getMpConsume(sk)))
				{
					_actor.doCast(sk);
					return;
				}
				
				max_range = Math.max(max_range, castRange);
			}
			
			if (!isNotMoving())
			{
				moveToPawn(getForcedTarget(), _actor.getPhysicalAttackRange()/* range */);
			}
			
			return;
		}
		
		_actor.doAttack(getForcedTarget());
	}
	
	@Override
	protected void thinkAttack()
	{
		if ((getAttackTarget() == null) || getAttackTarget().isAlikeDead())
		{
			if (getAttackTarget() != null)
			{
				// stop hating
				((L2Attackable) _actor).stopHating(getAttackTarget());
			}
			setIntention(AI_INTENTION_ACTIVE);
		}
		else
		{
			// notify aggression
			if (((L2Npc) _actor).getTemplate().getClans() != null)
			{
				for (L2Object obj : _actor.getKnownList().getKnownObjects().values())
				{
					if (!(obj instanceof L2Npc))
					{
						continue;
					}
					final L2Npc npc = (L2Npc) obj;
					if (!npc.isInMyClan((L2Npc) _actor))
					{
						continue;
					}
					if (_actor.isInsideRadius(npc, npc.getTemplate().getClanHelpRange(), false, true) && (Math.abs(getAttackTarget().getZ() - npc.getZ()) < 200))
					{
						npc.getAI().notifyEvent(CtrlEvent.EVT_AGGRESSION, getAttackTarget(), 1);
					}
				}
			}
			
			_actor.setTarget(getAttackTarget());
			final double dist2 = _actor.calculateDistance(getAttackTarget(), false, true);
			final int range = _actor.getPhysicalAttackRange() + _actor.getTemplate().getCollisionRadius() + getAttackTarget().getTemplate().getCollisionRadius();
			int max_range = range;
			
			if (!_actor.isMuted() && (dist2 > ((range + 20) * (range + 20))))
			{
				// check distant skills
				for (Skill sk : _actor.getAllSkills())
				{
					final int castRange = sk.getCastRange();
					
					if (((castRange * castRange) >= dist2) && !_actor.isSkillDisabled(sk) && (_actor.getCurrentMp() > _actor.getStat().getMpConsume(sk)))
					{
						_actor.doCast(sk);
						return;
					}
					
					max_range = Math.max(max_range, castRange);
				}
				
				moveToPawn(getAttackTarget(), range);
				return;
			}
			
			// Force mobs to attack anybody if confused.
			final L2Character hated = _actor.isConfused() ? findNextRndTarget() : getAttackTarget();
			
			if (hated == null)
			{
				setIntention(AI_INTENTION_ACTIVE);
				return;
			}
			
			if (hated != getAttackTarget())
			{
				setAttackTarget(hated);
			}
			
			if (!_actor.isMuted() && (Rnd.nextInt(5) == 3))
			{
				for (Skill sk : _actor.getAllSkills())
				{
					final int castRange = sk.getCastRange();
					
					if (((castRange * castRange) >= dist2) && !_actor.isSkillDisabled(sk) && (_actor.getCurrentMp() < _actor.getStat().getMpConsume(sk)))
					{
						_actor.doCast(sk);
						return;
					}
				}
			}
			
			_actor.doAttack(getAttackTarget());
		}
	}
	
	@Override
	protected void thinkActive()
	{
		setAttackTarget(findNextRndTarget());
		
		final L2Character hated = _actor.isConfused() ? findNextRndTarget() : getAttackTarget();
		if (hated == null)
		{
			return;
		}
		
		_actor.setRunning();
		setIntention(CtrlIntention.AI_INTENTION_ATTACK, hated);
	}
	
	private boolean checkAutoAttackCondition(L2Character target)
	{
		if ((target == null) || (target instanceof L2NpcInstance) || (target instanceof L2DoorInstance))
		{
			return false;
		}
		
		// TODO(Zoey76)[#112]: This check must change if summon fall in L2Npc hierarchy.
		if (target.isNpc())
		{
			return false;
		}
		
		// Check if the target isn't invulnerable
		if (target.isInvul() || target.isAlikeDead())
		{
			return false;
		}
		
		// Spawn protection (only against mobs)
		if (target.isPlayer() && ((L2PcInstance) target).isSpawnProtected())
		{
			return false;
		}
		
		final L2Attackable me = getActiveChar();
		if (!me.isInsideRadius(target, me.getAggroRange(), false, false) || (Math.abs(_actor.getZ() - target.getZ()) > 100))
		{
			return false;
		}
		
		// Check if the target isn't in silent move mode
		if (target.isPlayable() && ((L2Playable) target).isSilentMovingAffected())
		{
			return false;
		}
		return me.isAggressive();
	}
	
	private L2Character findNextRndTarget()
	{
		final List<L2Character> potentialTarget = _actor.getKnownList().getKnownCharactersInRadius(getActiveChar().getAggroRange()).stream().filter(this::checkAutoAttackCondition).collect(Collectors.toList());
		return potentialTarget.isEmpty() ? null : potentialTarget.get(Rnd.nextInt(potentialTarget.size()));
	}
	
	private L2ControllableMobInstance findNextGroupTarget()
	{
		return getGroupTarget().getRandomMob();
	}
	
	public int getAlternateAI()
	{
		return _alternateAI;
	}
	
	public void setAlternateAI(int _alternateai)
	{
		_alternateAI = _alternateai;
	}
	
	public void forceAttack(L2Character target)
	{
		setAlternateAI(AI_FORCEATTACK);
		setForcedTarget(target);
	}
	
	public void forceAttackGroup(MobGroup group)
	{
		setForcedTarget(null);
		setGroupTarget(group);
		setAlternateAI(AI_ATTACK_GROUP);
	}
	
	public void stop()
	{
		setAlternateAI(AI_IDLE);
		clientStopMoving(null);
	}
	
	public void move(int x, int y, int z)
	{
		moveTo(x, y, z);
	}
	
	public void follow(L2Character target)
	{
		setAlternateAI(AI_FOLLOW);
		setForcedTarget(target);
	}
	
	public boolean isThinking()
	{
		return _isThinking;
	}
	
	public boolean isNotMoving()
	{
		return _isNotMoving;
	}
	
	public void setNotMoving(boolean isNotMoving)
	{
		_isNotMoving = isNotMoving;
	}
	
	public void setThinking(boolean isThinking)
	{
		_isThinking = isThinking;
	}
	
	private L2Character getForcedTarget()
	{
		return _forcedTarget;
	}
	
	private MobGroup getGroupTarget()
	{
		return _targetGroup;
	}
	
	private void setForcedTarget(L2Character forcedTarget)
	{
		_forcedTarget = forcedTarget;
	}
	
	private void setGroupTarget(MobGroup targetGroup)
	{
		_targetGroup = targetGroup;
	}
}
