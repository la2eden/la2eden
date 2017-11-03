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

import com.la2eden.gameserver.ThreadPoolManager;
import com.la2eden.gameserver.enums.ShotType;
import com.la2eden.gameserver.handler.IItemHandler;
import com.la2eden.gameserver.model.actor.L2Playable;
import com.la2eden.gameserver.model.actor.instance.L2PcInstance;
import com.la2eden.gameserver.model.holders.SkillHolder;
import com.la2eden.gameserver.model.items.L2Weapon;
import com.la2eden.gameserver.model.items.instance.L2ItemInstance;
import com.la2eden.gameserver.model.items.type.ActionType;
import com.la2eden.gameserver.network.SystemMessageId;
import com.la2eden.gameserver.network.serverpackets.ExAutoSoulShot;
import com.la2eden.gameserver.network.serverpackets.MagicSkillUse;
import com.la2eden.gameserver.network.serverpackets.SystemMessage;
import com.la2eden.gameserver.util.Broadcast;
import com.la2eden.util.Rnd;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

public class SoulShots implements IItemHandler
{
    private static Map<Integer, Integer> potions = new HashMap<>();

    @Override
    public boolean useItem(L2Playable playable, L2ItemInstance item, boolean forceUse)
    {
        potions.put(728, 2);   // Mana Potion Cooldown
        potions.put(1539, 12); // Greater Healing Potion Cooldown
        potions.put(5592, 2);  // Greater CP Potion Cooldown

        if (!playable.isPlayer())
        {
            playable.sendPacket(SystemMessageId.YOUR_PET_CANNOT_CARRY_THIS_ITEM);
            return false;
        }

        final L2PcInstance activeChar = playable.getActingPlayer();
        final L2ItemInstance weaponInst = activeChar.getActiveWeaponInstance();
        final L2Weapon weaponItem = activeChar.getActiveWeaponItem();
        final SkillHolder[] skills = item.getItem().getSkills();

        boolean isSoulPotion = false;
        final int itemId = item.getId();
        final Integer cooldown = potions.get(itemId);

        // if there is a cooldown, there is an auto potion.
        if (cooldown != null) {
            isSoulPotion = true;

            if (activeChar.isAutoPot(itemId))
            {
                activeChar.disableAutoShot(itemId);
                activeChar.sendPacket(new ExAutoSoulShot(itemId, 0));
                activeChar.setAutoPot(itemId, null, false);
            } else {
                if (activeChar.getInventory().getItemByItemId(itemId) != null)
                {
                    if (activeChar.getInventory().getItemByItemId(itemId).getCount() > 1)
                    {
                        activeChar.addAutoSoulShot(itemId);
                        activeChar.sendPacket(new ExAutoSoulShot(itemId, 1));
                        final SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.THE_AUTOMATIC_USE_OF_S1_HAS_BEEN_ACTIVATED);
                        sm.addItemName(itemId);
                        activeChar.sendPacket(sm);

                        activeChar.setAutoPot(itemId, ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(new AutoPot(1539, activeChar), 1000, cooldown * 1000), true);
                    }
                    else
                    {
                        MagicSkillUse msu = new MagicSkillUse(activeChar, activeChar, 2037, 1, 0, 100);
                        activeChar.broadcastPacket(msu);

                        ItemSkills is = new ItemSkills();
                        is.useItem(activeChar, activeChar.getInventory().getItemByItemId(itemId), true);
                    }
                }
            }
        }

        if (skills == null)
        {
            _log.log(Level.WARNING, getClass().getSimpleName() + ": is missing skills!");
            return false;
        }

        // Check if Soul shot can be used
        if ((weaponInst == null) || (weaponItem.getSoulShotCount() == 0))
        {
            if (!activeChar.getAutoSoulShot().contains(itemId))
            {
                activeChar.sendPacket(SystemMessageId.CANNOT_USE_SOULSHOTS);
            }
            return false;
        }

        boolean gradeCheck = item.isEtcItem() && (item.getEtcItem().getDefaultAction() == ActionType.SOULSHOT) && (weaponInst.getItem().getItemGradeSPlus() == item.getItem().getItemGradeSPlus());

        // Prevents SystemMessage if its a potion.
        if (isSoulPotion) {
            gradeCheck = true;
        }

        if (!gradeCheck)
        {
            if (!activeChar.getAutoSoulShot().contains(itemId))
            {
                activeChar.sendPacket(SystemMessageId.THE_SOULSHOT_YOU_ARE_ATTEMPTING_TO_USE_DOES_NOT_MATCH_THE_GRADE_OF_YOUR_EQUIPPED_WEAPON);
            }
            return false;
        }

        activeChar.soulShotLock.lock();
        try
        {
            // Check if Soul shot is already active
            if (activeChar.isChargedShot(ShotType.SOULSHOTS))
            {
                return false;
            }

            // Consume Soul shots if player has enough of them
            int SSCount = weaponItem.getSoulShotCount();
            if ((weaponItem.getReducedSoulShot() > 0) && (Rnd.get(100) < weaponItem.getReducedSoulShotChance()))
            {
                SSCount = weaponItem.getReducedSoulShot();
            }

            if (!activeChar.destroyItemWithoutTrace("Consume", item.getObjectId(), SSCount, null, false))
            {
                if (!activeChar.disableAutoShot(itemId))
                {
                    activeChar.sendPacket(SystemMessageId.YOU_DO_NOT_HAVE_ENOUGH_SOULSHOTS_FOR_THAT);
                }
                return false;
            }
            // Charge soul shot
            weaponInst.setChargedShot(ShotType.SOULSHOTS, true);
        }
        finally
        {
            activeChar.soulShotLock.unlock();
        }

        // Send message to client
        if (!isSoulPotion) {
            activeChar.sendPacket(SystemMessageId.YOUR_SOULSHOTS_ARE_ENABLED);
            Broadcast.toSelfAndKnownPlayersInRadius(activeChar, new MagicSkillUse(activeChar, activeChar, skills[0].getSkillId(), skills[0].getSkillLvl(), 0, 0), 600);
        }
        return true;
    }

    private class AutoPot implements Runnable
    {
        private int id;
        private L2PcInstance activeChar;

        public AutoPot(int id, L2PcInstance activeChar)
        {
            this.id = id;
            this.activeChar = activeChar;
        }

        @Override
        public void run()
        {
            if (activeChar.getInventory().getItemByItemId(id) == null)
            {
                activeChar.sendPacket(new ExAutoSoulShot(id, 0));
                activeChar.setAutoPot(id, null, false);
                return;
            }

            switch (id)
            {
                case 728:
                {
                    if (activeChar.getCurrentMp() < 0.70*activeChar.getMaxMp())
                    {
                        MagicSkillUse msu = new MagicSkillUse(activeChar, activeChar, 2279, 2, 0, 100);
                        activeChar.broadcastPacket(msu);

                        ItemSkills is = new ItemSkills();
                        is.useItem(activeChar, activeChar.getInventory().getItemByItemId(728), true);
                    }

                    break;
                }
                case 1539:
                {
                    if (activeChar.getCurrentHp() < 0.95*activeChar.getMaxHp())
                    {
                        MagicSkillUse msu = new MagicSkillUse(activeChar, activeChar, 2037, 1, 0, 100);
                        activeChar.broadcastPacket(msu);

                        ItemSkills is = new ItemSkills();
                        is.useItem(activeChar, activeChar.getInventory().getItemByItemId(1539), true);
                    }

                    break;
                }
                case 5592:
                {
                    if (activeChar.getCurrentCp() < 0.95*activeChar.getMaxCp())
                    {
                        MagicSkillUse msu = new MagicSkillUse(activeChar, activeChar, 2166, 2, 0, 100);
                        activeChar.broadcastPacket(msu);

                        ItemSkills is = new ItemSkills();
                        is.useItem(activeChar, activeChar.getInventory().getItemByItemId(5592), true);
                    }

                    break;
                }
            }

            if (activeChar.getInventory().getItemByItemId(id) == null)
            {
                activeChar.sendPacket(new ExAutoSoulShot(id, 0));
                activeChar.setAutoPot(id, null, false);
            }
        }
    }
}
