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
package instances.ElcadiasTent;

import com.la2eden.gameserver.instancemanager.InstanceManager;
import com.la2eden.gameserver.model.Location;
import com.la2eden.gameserver.model.actor.L2Npc;
import com.la2eden.gameserver.model.actor.instance.L2PcInstance;
import com.la2eden.gameserver.model.instancezone.InstanceWorld;
import com.la2eden.gameserver.model.quest.QuestState;

import instances.AbstractInstance;
import quests.Q10292_SevenSignsGirlOfDoubt.Q10292_SevenSignsGirlOfDoubt;
import quests.Q10293_SevenSignsForbiddenBookOfTheElmoreAdenKingdom.Q10293_SevenSignsForbiddenBookOfTheElmoreAdenKingdom;
import quests.Q10294_SevenSignsToTheMonasteryOfSilence.Q10294_SevenSignsToTheMonasteryOfSilence;
import quests.Q10296_SevenSignsPowerOfTheSeal.Q10296_SevenSignsPowerOfTheSeal;

/**
 * Elcadia's Tent instance zone.
 * @author Adry_85
 */
public final class ElcadiasTent extends AbstractInstance
{
	// NPCs
	private static final int ELCADIA = 32784;
	private static final int GRUFF_LOOKING_MAN = 32862;
	// Locations
	private static final Location START_LOC = new Location(89706, -238074, -9632, 0, 0);
	private static final Location EXIT_LOC = new Location(43316, -87986, -2832, 0, 0);
	// Misc
	private static final int TEMPLATE_ID = 158;
	
	public ElcadiasTent()
	{
		super(ElcadiasTent.class.getSimpleName());
		addFirstTalkId(GRUFF_LOOKING_MAN, ELCADIA);
		addStartNpc(GRUFF_LOOKING_MAN, ELCADIA);
		addTalkId(GRUFF_LOOKING_MAN, ELCADIA);
	}
	
	@Override
	public String onTalk(L2Npc npc, L2PcInstance talker)
	{
		if (npc.getId() == GRUFF_LOOKING_MAN)
		{
			final QuestState GirlOfDoubt = talker.getQuestState(Q10292_SevenSignsGirlOfDoubt.class.getSimpleName());
			final QuestState ForbiddenBook = talker.getQuestState(Q10293_SevenSignsForbiddenBookOfTheElmoreAdenKingdom.class.getSimpleName());
			final QuestState Monastery = talker.getQuestState(Q10294_SevenSignsToTheMonasteryOfSilence.class.getSimpleName());
			final QuestState PowerOfSeal = talker.getQuestState(Q10296_SevenSignsPowerOfTheSeal.class.getSimpleName());
			if (((GirlOfDoubt != null) && GirlOfDoubt.isStarted()) //
				|| ((GirlOfDoubt != null) && GirlOfDoubt.isCompleted() && (ForbiddenBook == null)) //
				|| ((ForbiddenBook != null) && ForbiddenBook.isStarted()) //
				|| ((ForbiddenBook != null) && ForbiddenBook.isCompleted() && (Monastery == null)) //
				|| ((PowerOfSeal != null) && PowerOfSeal.isStarted()))
			{
				enterInstance(talker, "ElcadiasTent.xml", TEMPLATE_ID);
			}
			else
			{
				return "32862-01.html";
			}
		}
		else
		{
			final InstanceWorld world = InstanceManager.getInstance().getPlayerWorld(talker);
			world.removeAllowed(talker.getObjectId());
			talker.setInstanceId(0);
			talker.teleToLocation(EXIT_LOC);
		}
		return super.onTalk(npc, talker);
	}
	
	@Override
	public void onEnterInstance(L2PcInstance player, InstanceWorld world, boolean firstEntrance)
	{
		if (firstEntrance)
		{
			world.addAllowed(player.getObjectId());
		}
		teleportPlayer(player, START_LOC, world.getInstanceId(), false);
	}
}