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
package com.la2eden.gameserver.model.actor.instance;

import com.la2eden.Config;
import com.la2eden.gameserver.ai.CtrlIntention;
import com.la2eden.gameserver.enums.InstanceType;
import com.la2eden.gameserver.model.L2World;
import com.la2eden.gameserver.model.L2WorldRegion;
import com.la2eden.gameserver.model.actor.L2Attackable;
import com.la2eden.gameserver.model.actor.L2Character;
import com.la2eden.gameserver.model.actor.knownlist.GuardKnownList;
import com.la2eden.gameserver.model.actor.templates.L2NpcTemplate;
import com.la2eden.gameserver.model.events.EventDispatcher;
import com.la2eden.gameserver.model.events.EventType;
import com.la2eden.gameserver.model.events.impl.character.npc.OnNpcFirstTalk;
import com.la2eden.gameserver.network.serverpackets.ActionFailed;
import com.la2eden.gameserver.network.serverpackets.SocialAction;
import com.la2eden.util.Rnd;

import java.util.logging.Logger;

/**
 * This class manages all Guards in the world. It inherits all methods from L2Attackable and adds some more such as tracking PK and aggressive L2MonsterInstance.
 */
public class L2GuardInstance extends L2Attackable
{
	private static Logger _log = Logger.getLogger(L2GuardInstance.class.getName());
	
	/**
	 * Creates a guard.
	 * @param template the guard NPC template
	 */
	public L2GuardInstance(L2NpcTemplate template)
	{
		super(template);
		setInstanceType(InstanceType.L2GuardInstance);
	}
	
	@Override
	public final GuardKnownList getKnownList()
	{
		return (GuardKnownList) super.getKnownList();
	}
	
	@Override
	public void initKnownList()
	{
		setKnownList(new GuardKnownList(this));
	}
	
	/**
	 * Return True if hte attacker is a L2MonsterInstance.
	 */
	@Override
	public boolean isAutoAttackable(L2Character attacker)
	{
		return attacker instanceof L2MonsterInstance;
	}
	
	/**
	 * Set the home location of its L2GuardInstance.
	 */
	@Override
	public void onSpawn()
	{
		setIsNoRndWalk(true);
		super.onSpawn();
		
		// check the region where this mob is, do not activate the AI if region is inactive.
		final L2WorldRegion region = L2World.getInstance().getRegion(getX(), getY());
		if ((region != null) && (!region.isActive()))
		{
			getAI().stopAITask();
		}
	}
	
	/**
	 * Return the pathfile of the selected HTML file in function of the L2GuardInstance Identifier and of the page number.<br>
	 * <B><U> Format of the pathfile </U> :</B>
	 * <ul>
	 * <li>if page number = 0 : <B>datapack/html/guard/12006.htm</B> (npcId-page number)</li>
	 * <li>if page number > 0 : <B>datapack/html/guard/12006-1.htm</B> (npcId-page number)</li>
	 * </ul>
	 * @param npcId The Identifier of the L2NpcInstance whose text must be display
	 * @param val The number of the page to display
	 */
	@Override
	public String getHtmlPath(int npcId, int val)
	{
		String pom = "";
		if (val == 0)
		{
			pom = "" + npcId;
		}
		else
		{
			pom = npcId + "-" + val;
		}
		return "./datapack/html/guard/" + pom + ".htm";
	}
	
	/**
	 * Manage actions when a player click on the L2GuardInstance.<br>
	 * <B><U> Actions on first click on the L2GuardInstance (Select it)</U> :</B>
	 * <ul>
	 * <li>Set the L2GuardInstance as target of the L2PcInstance player (if necessary)</li>
	 * <li>Send a Server->Client packet MyTargetSelected to the L2PcInstance player (display the select window)</li>
	 * <li>Set the L2PcInstance Intention to AI_INTENTION_IDLE</li>
	 * <li>Send a Server->Client packet ValidateLocation to correct the L2GuardInstance position and heading on the client</li>
	 * </ul>
	 * <B><U> Actions on second click on the L2GuardInstance (Attack it/Interact with it)</U> :</B>
	 * <ul>
	 * <li>If L2PcInstance is in the _aggroList of the L2GuardInstance, set the L2PcInstance Intention to AI_INTENTION_ATTACK</li>
	 * <li>If L2PcInstance is NOT in the _aggroList of the L2GuardInstance, set the L2PcInstance Intention to AI_INTENTION_INTERACT (after a distance verification) and show message</li>
	 * </ul>
	 * <B><U> Example of use </U> :</B>
	 * <ul>
	 * <li>Client packet : Action, AttackRequest</li>
	 * </ul>
	 * @param player The L2PcInstance that start an action on the L2GuardInstance
	 */
	@Override
	public void onAction(L2PcInstance player, boolean interact)
	{
		if (!canTarget(player))
		{
			return;
		}
		
		// Check if the L2PcInstance already target the L2GuardInstance
		if (getObjectId() != player.getTargetId())
		{
			// Set the target of the L2PcInstance player
			player.setTarget(this);
		}
		else if (interact)
		{
			// Check if the L2PcInstance is in the _aggroList of the L2GuardInstance
			if (isInAggroList(player))
			{
				if (Config.DEBUG)
				{
					_log.fine(player.getObjectId() + ": Attacked guard " + getObjectId());
				}
				
				// Set the L2PcInstance Intention to AI_INTENTION_ATTACK
				player.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, this);
			}
			else
			{
				// Calculate the distance between the L2PcInstance and the L2NpcInstance
				if (!canInteract(player))
				{
					// Set the L2PcInstance Intention to AI_INTENTION_INTERACT
					player.getAI().setIntention(CtrlIntention.AI_INTENTION_INTERACT, this);
				}
				else
				{
					// Send a Server->Client packet SocialAction to the all L2PcInstance on the _knownPlayer of the L2NpcInstance
					// to display a social action of the L2GuardInstance on their client
					broadcastPacket(new SocialAction(getObjectId(), Rnd.nextInt(8)));
					
					player.setLastFolkNPC(this);
					
					// Open a chat window on client with the text of the L2GuardInstance
					if (hasListener(EventType.ON_NPC_QUEST_START))
					{
						player.setLastQuestNpcObject(getObjectId());
					}
					
					if (hasListener(EventType.ON_NPC_FIRST_TALK))
					{
						EventDispatcher.getInstance().notifyEventAsync(new OnNpcFirstTalk(this, player), this);
					}
					else
					{
						showChatWindow(player, 0);
					}
				}
			}
		}
		// Send a Server->Client ActionFailed to the L2PcInstance in order to avoid that the client wait another packet
		player.sendPacket(ActionFailed.STATIC_PACKET);
	}
}
