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
package handlers.usercommandhandlers;

import com.la2eden.Config;
import com.la2eden.gameserver.GameTimeController;
import com.la2eden.gameserver.ThreadPoolManager;
import com.la2eden.gameserver.ai.CtrlIntention;
import com.la2eden.gameserver.datatables.SkillData;
import com.la2eden.gameserver.handler.IUserCommandHandler;
import com.la2eden.gameserver.model.TeleportWhereType;
import com.la2eden.gameserver.model.actor.instance.L2PcInstance;
import com.la2eden.gameserver.model.entity.TvTEvent;
import com.la2eden.gameserver.model.skills.Skill;
import com.la2eden.gameserver.network.SystemMessageId;
import com.la2eden.gameserver.network.serverpackets.ActionFailed;
import com.la2eden.gameserver.network.serverpackets.MagicSkillUse;
import com.la2eden.gameserver.network.serverpackets.SetupGauge;
import com.la2eden.gameserver.util.Broadcast;

/**
 * Unstuck user command.
 */
public class Unstuck implements IUserCommandHandler
{
	private static final int[] COMMAND_IDS =
	{
		52
	};
	
	@Override
	public boolean useUserCommand(int id, L2PcInstance activeChar)
	{
		// Thanks nbd
		if (!TvTEvent.onEscapeUse(activeChar.getObjectId()))
		{
			activeChar.sendPacket(ActionFailed.STATIC_PACKET);
			return false;
		}
		else if (activeChar.isJailed())
		{
			activeChar.sendMessage("You cannot use this function while you are jailed.");
			return false;
		}
		
		final int unstuckTimer = (activeChar.getAccessLevel().isGm() ? 1000 : Config.UNSTUCK_INTERVAL * 1000);
		
		if (activeChar.isInOlympiadMode())
		{
			activeChar.sendPacket(SystemMessageId.YOU_CANNOT_USE_THAT_SKILL_IN_A_GRAND_OLYMPIAD_MATCH);
			return false;
		}
		
		if (activeChar.isCastingNow() || activeChar.isMovementDisabled() || activeChar.isMuted() || activeChar.isAlikeDead() || activeChar.inObserverMode() || activeChar.isCombatFlagEquipped())
		{
			return false;
		}
		
		activeChar.forceIsCasting(GameTimeController.getInstance().getGameTicks() + (unstuckTimer / GameTimeController.MILLIS_IN_TICK));
		
		final Skill escape = SkillData.getInstance().getSkill(2099, 1); // 5 minutes escape
		final Skill GM_escape = SkillData.getInstance().getSkill(2100, 1); // 1 second escape
		if (activeChar.getAccessLevel().isGm())
		{
			if (GM_escape != null)
			{
				activeChar.doCast(GM_escape);
				return true;
			}
			activeChar.sendMessage("You use Escape: 1 second.");
		}
		else if ((Config.UNSTUCK_INTERVAL == 300) && (escape != null))
		{
			activeChar.doCast(escape);
			return true;
		}
		else
		{
			if (Config.UNSTUCK_INTERVAL > 100)
			{
				activeChar.sendMessage("You use Escape: " + (unstuckTimer / 60000) + " minutes.");
			}
			else
			{
				activeChar.sendMessage("You use Escape: " + (unstuckTimer / 1000) + " seconds.");
			}
		}
		activeChar.getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
		// SoE Animation section
		activeChar.setTarget(activeChar);
		activeChar.disableAllSkills();
		
		final MagicSkillUse msk = new MagicSkillUse(activeChar, 1050, 1, unstuckTimer, 0);
		Broadcast.toSelfAndKnownPlayersInRadius(activeChar, msk, 900);
		final SetupGauge sg = new SetupGauge(0, unstuckTimer);
		activeChar.sendPacket(sg);
		// End SoE Animation section
		
		// continue execution later
		activeChar.setSkillCast(ThreadPoolManager.getInstance().scheduleGeneral(new EscapeFinalizer(activeChar), unstuckTimer));
		
		return true;
	}
	
	private static class EscapeFinalizer implements Runnable
	{
		private final L2PcInstance _activeChar;
		
		protected EscapeFinalizer(L2PcInstance activeChar)
		{
			_activeChar = activeChar;
		}
		
		@Override
		public void run()
		{
			if (_activeChar.isDead())
			{
				return;
			}
			
			_activeChar.setIsIn7sDungeon(false);
			_activeChar.enableAllSkills();
			_activeChar.setIsCastingNow(false);
			_activeChar.setInstanceId(0);
			_activeChar.teleToLocation(TeleportWhereType.TOWN);
		}
	}
	
	@Override
	public int[] getUserCommandList()
	{
		return COMMAND_IDS;
	}
}