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
package handlers.effecthandlers;

import com.la2eden.gameserver.enums.ShotType;
import com.la2eden.gameserver.model.StatsSet;
import com.la2eden.gameserver.model.actor.L2Character;
import com.la2eden.gameserver.model.conditions.Condition;
import com.la2eden.gameserver.model.effects.AbstractEffect;
import com.la2eden.gameserver.model.effects.L2EffectType;
import com.la2eden.gameserver.model.skills.BuffInfo;
import com.la2eden.gameserver.model.stats.Formulas;
import com.la2eden.gameserver.model.stats.Stats;
import com.la2eden.util.Rnd;

/**
 * Death Link effect implementation.
 * @author Adry_85
 */
public final class DeathLink extends AbstractEffect
{
	public DeathLink(Condition attachCond, Condition applyCond, StatsSet set, StatsSet params)
	{
		super(attachCond, applyCond, set, params);
	}
	
	@Override
	public L2EffectType getEffectType()
	{
		return L2EffectType.DEATH_LINK;
	}
	
	@Override
	public boolean isInstant()
	{
		return true;
	}
	
	@Override
	public void onStart(BuffInfo info)
	{
		final L2Character target = info.getEffected();
		final L2Character activeChar = info.getEffector();
		
		if (activeChar.isAlikeDead())
		{
			return;
		}
		
		final boolean sps = info.getSkill().useSpiritShot() && activeChar.isChargedShot(ShotType.SPIRITSHOTS);
		final boolean bss = info.getSkill().useSpiritShot() && activeChar.isChargedShot(ShotType.BLESSED_SPIRITSHOTS);
		
		if (target.isPlayer() && target.getActingPlayer().isFakeDeath())
		{
			target.stopFakeDeath(true);
		}
		
		final boolean mcrit = Formulas.calcMCrit(activeChar.getMCriticalHit(target, info.getSkill()));
		final byte shld = Formulas.calcShldUse(activeChar, target, info.getSkill());
		final int damage = (int) Formulas.calcMagicDam(activeChar, target, info.getSkill(), shld, sps, bss, mcrit);
		
		if (damage > 0)
		{
			// Manage attack or cast break of the target (calculating rate, sending message...)
			if (!target.isRaid() && Formulas.calcAtkBreak(target, damage))
			{
				target.breakAttack();
				target.breakCast();
			}
			
			// Shield Deflect Magic: Reflect all damage on caster.
			if (target.getStat().calcStat(Stats.VENGEANCE_SKILL_MAGIC_DAMAGE, 0, target, info.getSkill()) > Rnd.get(100))
			{
				activeChar.reduceCurrentHp(damage, target, info.getSkill());
				activeChar.notifyDamageReceived(damage, target, info.getSkill(), mcrit, false);
			}
			else
			{
				target.reduceCurrentHp(damage, activeChar, info.getSkill());
				target.notifyDamageReceived(damage, activeChar, info.getSkill(), mcrit, false);
				activeChar.sendDamageMessage(target, damage, mcrit, false, false);
			}
		}
		
		if (info.getSkill().isSuicideAttack())
		{
			activeChar.doDie(activeChar);
		}
	}
}