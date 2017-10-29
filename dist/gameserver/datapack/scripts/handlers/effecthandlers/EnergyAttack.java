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
package handlers.effecthandlers;

import com.la2eden.gameserver.enums.ShotType;
import com.la2eden.gameserver.model.StatsSet;
import com.la2eden.gameserver.model.actor.L2Character;
import com.la2eden.gameserver.model.actor.instance.L2PcInstance;
import com.la2eden.gameserver.model.conditions.Condition;
import com.la2eden.gameserver.model.effects.AbstractEffect;
import com.la2eden.gameserver.model.effects.L2EffectType;
import com.la2eden.gameserver.model.items.L2Weapon;
import com.la2eden.gameserver.model.items.type.WeaponType;
import com.la2eden.gameserver.model.skills.BuffInfo;
import com.la2eden.gameserver.model.skills.Skill;
import com.la2eden.gameserver.model.stats.BaseStats;
import com.la2eden.gameserver.model.stats.Formulas;
import com.la2eden.gameserver.model.stats.Stats;
import com.la2eden.util.Rnd;

/**
 * Energy Attack effect implementation.
 * @author NosBit
 */
public final class EnergyAttack extends AbstractEffect
{
	private final double _power;
	private final int _criticalChance;
	private final boolean _ignoreShieldDefence;
	
	public EnergyAttack(Condition attachCond, Condition applyCond, StatsSet set, StatsSet params)
	{
		super(attachCond, applyCond, set, params);
		
		_power = params.getDouble("power", 0);
		_criticalChance = params.getInt("criticalChance", 0);
		_ignoreShieldDefence = params.getBoolean("ignoreShieldDefence", false);
	}
	
	@Override
	public boolean calcSuccess(BuffInfo info)
	{
		// TODO: Verify this on retail
		return !Formulas.calcPhysicalSkillEvasion(info.getEffector(), info.getEffected(), info.getSkill());
	}
	
	@Override
	public L2EffectType getEffectType()
	{
		return L2EffectType.PHYSICAL_ATTACK;
	}
	
	@Override
	public boolean isInstant()
	{
		return true;
	}
	
	@Override
	public void onStart(BuffInfo info)
	{
		final L2PcInstance attacker = info.getEffector() instanceof L2PcInstance ? (L2PcInstance) info.getEffector() : null;
		if (attacker == null)
		{
			return;
		}
		
		final L2Character target = info.getEffected();
		final Skill skill = info.getSkill();
		
		double attack = attacker.getPAtk(target);
		double defence = target.getPDef(attacker);
		
		if (!_ignoreShieldDefence)
		{
			final byte shield = Formulas.calcShldUse(attacker, target, skill, true);
			switch (shield)
			{
				case Formulas.SHIELD_DEFENSE_FAILED:
				{
					break;
				}
				case Formulas.SHIELD_DEFENSE_SUCCEED:
				{
					defence += target.getShldDef();
					break;
				}
				case Formulas.SHIELD_DEFENSE_PERFECT_BLOCK:
				{
					defence = -1;
					break;
				}
			}
		}
		
		double damage = 1;
		boolean critical = false;
		
		if (defence != -1)
		{
			final double damageMultiplier = Formulas.calcWeaponTraitBonus(attacker, target) * Formulas.calcAttributeBonus(attacker, target, skill) * Formulas.calcGeneralTraitBonus(attacker, target, skill.getTraitType(), true);
			
			final boolean ss = info.getSkill().useSoulShot() && attacker.isChargedShot(ShotType.SOULSHOTS);
			final double ssBoost = ss ? 2 : 1.0;
			
			double weaponTypeBoost;
			final L2Weapon weapon = attacker.getActiveWeaponItem();
			if ((weapon != null) && ((weapon.getItemType() == WeaponType.BOW) || (weapon.getItemType() == WeaponType.CROSSBOW)))
			{
				weaponTypeBoost = 70;
			}
			else
			{
				weaponTypeBoost = 77;
			}
			
			// charge count should be the count before casting the skill but since its reduced before calling effects
			// we add skill consume charges to current charges
			final double energyChargesBoost = (((attacker.getCharges() + skill.getChargeConsume()) - 1) * 0.2) + 1;
			
			attack += _power;
			attack *= ssBoost;
			attack *= energyChargesBoost;
			attack *= weaponTypeBoost;
			
			damage = attack / defence;
			damage *= damageMultiplier;
			if (target instanceof L2PcInstance)
			{
				damage *= attacker.getStat().calcStat(Stats.PVP_PHYS_SKILL_DMG, 1.0);
				damage *= target.getStat().calcStat(Stats.PVP_PHYS_SKILL_DEF, 1.0);
				damage = attacker.getStat().calcStat(Stats.PHYSICAL_SKILL_POWER, damage);
			}
			
			critical = (BaseStats.STR.calcBonus(attacker) * _criticalChance) > (Rnd.nextDouble() * 100);
			if (critical)
			{
				damage *= 2;
			}
		}
		
		if (damage > 0)
		{
			attacker.sendDamageMessage(target, (int) damage, false, critical, false);
			target.reduceCurrentHp(damage, attacker, skill);
			target.notifyDamageReceived(damage, attacker, skill, critical, false);
			
			// Check if damage should be reflected
			Formulas.calcDamageReflected(attacker, target, skill, critical);
		}
	}
}