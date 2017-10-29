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
package handlers.itemhandlers;

import com.la2eden.Config;
import com.la2eden.gameserver.handler.IItemHandler;
import com.la2eden.gameserver.instancemanager.CastleManorManager;
import com.la2eden.gameserver.instancemanager.MapRegionManager;
import com.la2eden.gameserver.model.L2Object;
import com.la2eden.gameserver.model.L2Seed;
import com.la2eden.gameserver.model.actor.L2Playable;
import com.la2eden.gameserver.model.actor.instance.L2ChestInstance;
import com.la2eden.gameserver.model.actor.instance.L2MonsterInstance;
import com.la2eden.gameserver.model.actor.instance.L2PcInstance;
import com.la2eden.gameserver.model.holders.SkillHolder;
import com.la2eden.gameserver.model.items.instance.L2ItemInstance;
import com.la2eden.gameserver.network.SystemMessageId;
import com.la2eden.gameserver.network.serverpackets.ActionFailed;

/**
 * @author l3x
 */
public class Seed implements IItemHandler
{
	@Override
	public boolean useItem(L2Playable playable, L2ItemInstance item, boolean forceUse)
	{
		if (!Config.ALLOW_MANOR)
		{
			return false;
		}
		else if (!playable.isPlayer())
		{
			playable.sendPacket(SystemMessageId.YOUR_PET_CANNOT_CARRY_THIS_ITEM);
			return false;
		}
		
		final L2Object tgt = playable.getTarget();
		if (!tgt.isNpc())
		{
			playable.sendPacket(SystemMessageId.INVALID_TARGET);
			return false;
		}
		else if (!tgt.isMonster() || ((L2MonsterInstance) tgt).isRaid() || (tgt instanceof L2ChestInstance))
		{
			playable.sendPacket(SystemMessageId.THE_TARGET_IS_UNAVAILABLE_FOR_SEEDING);
			return false;
		}
		
		final L2MonsterInstance target = (L2MonsterInstance) tgt;
		if (target.isDead())
		{
			playable.sendPacket(SystemMessageId.INVALID_TARGET);
			return false;
		}
		else if (target.isSeeded())
		{
			playable.sendPacket(ActionFailed.STATIC_PACKET);
			return false;
		}
		
		final L2Seed seed = CastleManorManager.getInstance().getSeed(item.getId());
		if (seed == null)
		{
			return false;
		}
		else if (seed.getCastleId() != MapRegionManager.getInstance().getAreaCastle(playable)) // TODO: replace me with tax zone
		{
			playable.sendPacket(SystemMessageId.THIS_SEED_MAY_NOT_BE_SOWN_HERE);
			return false;
		}
		
		final L2PcInstance activeChar = playable.getActingPlayer();
		target.setSeeded(seed, activeChar);
		
		final SkillHolder[] skills = item.getItem().getSkills();
		if (skills != null)
		{
			for (SkillHolder sk : skills)
			{
				activeChar.useMagic(sk.getSkill(), false, false);
			}
		}
		return true;
	}
}