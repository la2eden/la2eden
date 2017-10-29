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
package ai.fantasy_isle;

import java.text.SimpleDateFormat;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ScheduledFuture;

import com.la2eden.gameserver.GameTimeController;
import com.la2eden.gameserver.ThreadPoolManager;
import com.la2eden.gameserver.ai.CtrlIntention;
import com.la2eden.gameserver.model.Location;
import com.la2eden.gameserver.model.actor.L2Npc;

import ai.npc.AbstractNpcAI;

/**
 * Fantasy Isle Parade
 * @author JOJO, Pandragon
 */
public final class Parade extends AbstractNpcAI
{
	// @formatter:off
	final int[] ACTORS =
	{
		32379,	0,	32379,
		32379,	0,	32379,
		32379,	0,	32379,
		0,	0,	0,
		32380,	0,	32380,
		32380,	32381,	32380,
		32380,	0,	32380,
		32380,	32381,	32380,
		0,	0,	0,
		32382,	32382,	32382,
		32382,	32383,	32382,
		32383,	32384,	32383,
		32383,	32384,	32383,
		0,	0,	0,
		0,	32385,	0,
		32385,	0,	32385,
		0,	32385,	0,
		0,	0,	0,
		32412,	0,	32411,
		0,	0,	0,
		32421,	0,	32409,
		32423,	0,	32422,
		0,	0,	0,
		32420,	32419,	32417,
		32418,	0,	32416,
		0,	0,	0,
		32414,	0,	32414,
		0,	32413,	0,
		32414,	0,	32414,
		0,	0,	0,
		32393,	0,	32394,
		0,	32430,	0,
		32392,	0,	32391,
		0,	0,	0,
		0,	32404,	0,
		32403,	0,	32401,
		0,	0,	0,
		0,	32408,	0,
		32406,	0,	32407,
		0,	32405,	0,
		0,	0,	0,
		32390,	32389,	32387,
		32388,	0,	32386,
		0,	0,	0,
		0,	32400,	0,
		32397,	32398,	32396,
		0,	0,	0,
		0,	32450,	0,
		32448,	32449,	32447,
		0,	0,	0,
		32380,	0,	32380,
		32380,	32381,	32380,
		32380,	0,	32380,
		32380,	32381,	32380,
		0,	0,	0,
		32379,	0,	32379,
		32379,	0,	32379,
		32379,	0,	32379,
		0,	0,	0,
		0,	32415,	0
	};
	
	//(Northbound 270 degrees) Route 1
	private final int[][] START1 = {{-54780, -56810, -2015, 49152},{-54860, -56810, -2015, 49152},{-54940, -56810, -2015, 49152}};
	private final int[][] GOAL1  = {{-54780, -57965, -2015, 49152},{-54860, -57965, -2015, 49152},{-54940, -57965, -2015, 49152}};
	//(Westbound 180 degrees) Route 2
	private final int[][] START2 = {{-55715, -58900, -2015, 32768},{-55715, -58820, -2015, 32768},{-55715, -58740, -2015, 32768}};
	private final int[][] GOAL2  = {{-60850, -58900, -2015, 32768},{-60850, -58820, -2015, 32768},{-60850, -58740, -2015, 32768}};
	//(Southbound 90 degrees) Route 3
	private final int[][] START3 = {{-61790, -57965, -2015, 16384},{-61710, -57965, -2015, 16384},{-61630, -57965, -2015, 16384}};
	private final int[][] GOAL3  = {{-61790, -53890, -2116, 16384},{-61710, -53890, -2116, 16384},{-61630, -53890, -2116, 16384}};
	//(Eastbound 0 degrees) Route 4
	private final int[][] START4 = {{-60840, -52990, -2108, 0},{-60840, -53070, -2108, 0},{-60840, -53150, -2108, 0}};
	private final int[][] GOAL4  = {{-58620, -52990, -2015, 0},{-58620, -53070, -2015, 0},{-58620, -53150, -2015, 0}};
	//(To 315 degrees northeast) Route 5
	private final int[][] START5 = {{-57233, -53554, -2015, 57344},{-57290, -53610, -2015, 57344},{-57346, -53667, -2015, 57344}};
	private final int[][] GOAL5  = {{-55338, -55435, -2015, 57344},{-55395, -55491, -2015, 57344},{-55451, -55547, -2015, 57344}};
	
	final int[][][] START = {START1, START2, START3, START4, START5};
	final int[][][] GOAL  = {GOAL1, GOAL2, GOAL3, GOAL4, GOAL5};
	// @formatter:on
	
	int npcIndex;
	CopyOnWriteArrayList<L2Npc> spawns;
	ScheduledFuture<?> spawnTask;
	ScheduledFuture<?> deleteTask;
	ScheduledFuture<?> cleanTask;
	
	public Parade()
	{
		super(Parade.class.getSimpleName(), "ai/fantasy_isle");
		
		// Starts at 8:00 and repeats every 6 hours.
		final long diff = timeLeftMilli(8, 0, 0), cycle = 3600000L;
		ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(new Start(), diff, cycle);
		
		// Test - Starts 3 minutes after server startup and repeats every 20 minutes.
		// final long diff = timeLeftMilli(8, 0, 0), cycle = 600000L;
		// ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(new Start(), 180000L, cycle);
		
		_log.info("Fantasy Isle: Parade starting at " + new SimpleDateFormat("yyyy/MM/dd HH:mm").format(System.currentTimeMillis() + diff) + " and is scheduled each next " + (cycle / 3600000) + " hours.");
	}
	
	void load()
	{
		npcIndex = 0;
		spawns = new CopyOnWriteArrayList<>();
	}
	
	void clean()
	{
		if (spawns != null)
		{
			spawns.forEach(L2Npc::deleteMe);
		}
		spawns = null;
	}
	
	private long timeLeftMilli(int hh, int mm, int ss)
	{
		final int now = (GameTimeController.getInstance().getGameTicks() * 60) / 100;
		int dd = ((hh * 3600) + (mm * 60) + ss) - (now % 86400);
		if (dd < 0)
		{
			dd += 86400;
		}
		
		return (dd * 1000L) / 6L;
	}
	
	class Start implements Runnable
	{
		@Override
		public void run()
		{
			load();
			spawnTask = ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(new Spawn(), 0, 5000);
			deleteTask = ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(new Delete(), 10000, 1000);
			cleanTask = ThreadPoolManager.getInstance().scheduleGeneral(new Clean(), 420000);
		}
	}
	
	class Spawn implements Runnable
	{
		@Override
		public void run()
		{
			for (int i = 0; i < 3; ++i)
			{
				if (npcIndex >= ACTORS.length)
				{
					spawnTask.cancel(false);
					break;
				}
				final int npcId = ACTORS[npcIndex++];
				if (npcId == 0)
				{
					continue;
				}
				for (int route = 0; route < 5; ++route)
				{
					final int[] start = START[route][i];
					final int[] goal = GOAL[route][i];
					final L2Npc actor = addSpawn(npcId, start[0], start[1], start[2], start[3], false, 0);
					actor.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, new Location(goal[0], goal[1], goal[2], goal[3]));
					spawns.add(actor);
				}
			}
		}
	}
	
	class Delete implements Runnable
	{
		@Override
		public void run()
		{
			if (spawns.size() <= 0)
			{
				return;
			}
			for (L2Npc actor : spawns)
			{
				if (actor != null)
				{
					if (actor.calculateDistance(actor.getXdestination(), actor.getYdestination(), 0, false, true) < (100 * 100))
					{
						actor.deleteMe();
						spawns.remove(actor);
					}
					else if (!actor.isMoving())
					{
						actor.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, new Location(actor.getXdestination(), actor.getYdestination(), actor.getZdestination(), actor.getHeading()));
					}
				}
			}
			if (spawns.size() == 0)
			{
				deleteTask.cancel(false);
			}
		}
	}
	
	class Clean implements Runnable
	{
		@Override
		public void run()
		{
			spawnTask.cancel(false);
			spawnTask = null;
			deleteTask.cancel(false);
			deleteTask = null;
			cleanTask.cancel(false);
			cleanTask = null;
			clean();
		}
	}
	
	public static void main(String[] args)
	{
		new Parade();
	}
}