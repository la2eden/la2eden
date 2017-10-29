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
package com.la2eden.gameserver;

import java.lang.Thread.UncaughtExceptionHandler;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.la2eden.Config;
import com.la2eden.gameserver.model.StatsSet;

/**
 * <p>
 * This class is made to handle all the ThreadPools used in L2J.
 * </p>
 * <p>
 * Scheduled Tasks can either be sent to a {@link #_generalScheduledThreadPool "general"} or {@link #_effectsScheduledThreadPool "effects"} {@link ScheduledThreadPoolExecutor ScheduledThreadPool}: The "effects" one is used for every effects (skills, hp/mp regen ...) while the "general" one is used
 * for everything else that needs to be scheduled.<br>
 * There also is an {@link #_aiScheduledThreadPool "ai"} {@link ScheduledThreadPoolExecutor ScheduledThreadPool} used for AI Tasks.
 * </p>
 * <p>
 * Tasks can be sent to {@link ScheduledThreadPoolExecutor ScheduledThreadPool} either with:
 * <ul>
 * <li>{@link #scheduleEffect(Runnable, long, TimeUnit)} and {@link #scheduleEffect(Runnable, long)} : for effects Tasks that needs to be executed only once.</li>
 * <li>{@link #scheduleGeneral(Runnable, long, TimeUnit)} and {@link #scheduleGeneral(Runnable, long)} : for scheduled Tasks that needs to be executed once.</li>
 * <li>{@link #scheduleAi(Runnable, long, TimeUnit)} and {@link #scheduleAi(Runnable, long)} : for AI Tasks that needs to be executed once</li>
 * </ul>
 * or
 * <ul>
 * <li>{@link #scheduleEffectAtFixedRate(Runnable, long, long, TimeUnit)} and {@link #scheduleEffectAtFixedRate(Runnable, long, long)} : for effects Tasks that needs to be executed periodically.</li>
 * <li>{@link #scheduleGeneralAtFixedRate(Runnable, long, long, TimeUnit)} and {@link #scheduleGeneralAtFixedRate(Runnable, long, long)} : for scheduled Tasks that needs to be executed periodically.</li>
 * <li>{@link #scheduleAiAtFixedRate(Runnable, long, long, TimeUnit)} and {@link #scheduleAiAtFixedRate(Runnable, long, long)} : for AI Tasks that needs to be executed periodically</li>
 * </ul>
 * </p>
 * <p>
 * For all Tasks that should be executed with no delay asynchronously in a ThreadPool there also are usual {@link ThreadPoolExecutor ThreadPools} that can grow/shrink according to their load.:
 * <ul>
 * <li>{@link #_generalPacketsThreadPool GeneralPackets} where most packets handler are executed.</li>
 * <li>{@link #_ioPacketsThreadPool I/O Packets} where all the i/o packets are executed.</li>
 * <li>There will be an AI ThreadPool where AI events should be executed</li>
 * <li>A general ThreadPool where everything else that needs to run asynchronously with no delay should be executed ({@link com.la2eden.gameserver.model.actor.knownlist KnownList} updates, SQL updates/inserts...)?</li>
 * </ul>
 * </p>
 * @author -Wooden-
 */
public class ThreadPoolManager
{
	protected static final Logger _log = Logger.getLogger(ThreadPoolManager.class.getName());
	
	private static final class RunnableWrapper implements Runnable
	{
		private final Runnable _r;
		
		public RunnableWrapper(Runnable r)
		{
			_r = r;
		}
		
		@Override
		public final void run()
		{
			try
			{
				_r.run();
			}
			catch (Throwable e)
			{
				final Thread t = Thread.currentThread();
				final UncaughtExceptionHandler h = t.getUncaughtExceptionHandler();
				if (h != null)
				{
					h.uncaughtException(t, e);
				}
			}
		}
	}
	
	protected ScheduledThreadPoolExecutor _effectsScheduledThreadPool;
	protected ScheduledThreadPoolExecutor _generalScheduledThreadPool;
	protected ScheduledThreadPoolExecutor _aiScheduledThreadPool;
	protected ScheduledThreadPoolExecutor _eventScheduledThreadPool;
	private final ThreadPoolExecutor _generalPacketsThreadPool;
	private final ThreadPoolExecutor _ioPacketsThreadPool;
	private final ThreadPoolExecutor _generalThreadPool;
	private final ThreadPoolExecutor _eventThreadPool;
	
	private boolean _shutdown;
	
	public static ThreadPoolManager getInstance()
	{
		return SingletonHolder._instance;
	}
	
	protected ThreadPoolManager()
	{
		_effectsScheduledThreadPool = new ScheduledThreadPoolExecutor(Config.THREAD_P_EFFECTS, new PriorityThreadFactory("EffectsSTPool", Thread.NORM_PRIORITY));
		_generalScheduledThreadPool = new ScheduledThreadPoolExecutor(Config.THREAD_P_GENERAL, new PriorityThreadFactory("GeneralSTPool", Thread.NORM_PRIORITY));
		_eventScheduledThreadPool = new ScheduledThreadPoolExecutor(Config.THREAD_E_EVENTS, new PriorityThreadFactory("EventSTPool", Thread.NORM_PRIORITY));
		_ioPacketsThreadPool = new ThreadPoolExecutor(Config.IO_PACKET_THREAD_CORE_SIZE, Integer.MAX_VALUE, 5L, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>(), new PriorityThreadFactory("I/O Packet Pool", Thread.NORM_PRIORITY + 1));
		_generalPacketsThreadPool = new ThreadPoolExecutor(Config.GENERAL_PACKET_THREAD_CORE_SIZE, Config.GENERAL_PACKET_THREAD_CORE_SIZE + 2, 15L, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>(), new PriorityThreadFactory("Normal Packet Pool", Thread.NORM_PRIORITY + 1));
		_generalThreadPool = new ThreadPoolExecutor(Config.GENERAL_THREAD_CORE_SIZE, Config.GENERAL_THREAD_CORE_SIZE + 2, 5L, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>(), new PriorityThreadFactory("General Pool", Thread.NORM_PRIORITY));
		_aiScheduledThreadPool = new ScheduledThreadPoolExecutor(Config.AI_MAX_THREAD, new PriorityThreadFactory("AISTPool", Thread.NORM_PRIORITY));
		_eventThreadPool = new ThreadPoolExecutor(Config.EVENT_MAX_THREAD, Config.EVENT_MAX_THREAD + 2, 5L, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>(), new PriorityThreadFactory("Event Pool", Thread.NORM_PRIORITY));
		
		scheduleGeneralAtFixedRate(new PurgeTask(), 10, 5, TimeUnit.MINUTES);
	}
	
	/**
	 * Schedules an effect task to be executed after the given delay.
	 * @param task the task to execute
	 * @param delay the delay in the given time unit
	 * @param unit the time unit of the delay parameter
	 * @return a ScheduledFuture representing pending completion of the task, and whose get() method will throw an exception upon cancellation
	 */
	public ScheduledFuture<?> scheduleEffect(Runnable task, long delay, TimeUnit unit)
	{
		try
		{
			return _effectsScheduledThreadPool.schedule(new RunnableWrapper(task), delay, unit);
		}
		catch (RejectedExecutionException e)
		{
			return null;
		}
	}
	
	/**
	 * Schedules an effect task to be executed after the given delay.
	 * @param task the task to execute
	 * @param delay the delay in milliseconds
	 * @return a ScheduledFuture representing pending completion of the task, and whose get() method will throw an exception upon cancellation
	 */
	public ScheduledFuture<?> scheduleEffect(Runnable task, long delay)
	{
		return scheduleEffect(task, delay, TimeUnit.MILLISECONDS);
	}
	
	/**
	 * Schedules an effect task to be executed at fixed rate.
	 * @param task the task to execute
	 * @param initialDelay the initial delay in the given time unit
	 * @param period the period between executions in the given time unit
	 * @param unit the time unit of the initialDelay and period parameters
	 * @return a ScheduledFuture representing pending completion of the task, and whose get() method will throw an exception upon cancellation
	 */
	public ScheduledFuture<?> scheduleEffectAtFixedRate(Runnable task, long initialDelay, long period, TimeUnit unit)
	{
		try
		{
			return _effectsScheduledThreadPool.scheduleAtFixedRate(new RunnableWrapper(task), initialDelay, period, unit);
		}
		catch (RejectedExecutionException e)
		{
			return null; /* shutdown, ignore */
		}
	}
	
	/**
	 * Schedules an effect task to be executed at fixed rate.
	 * @param task the task to execute
	 * @param initialDelay the initial delay in milliseconds
	 * @param period the period between executions in milliseconds
	 * @return a ScheduledFuture representing pending completion of the task, and whose get() method will throw an exception upon cancellation
	 */
	public ScheduledFuture<?> scheduleEffectAtFixedRate(Runnable task, long initialDelay, long period)
	{
		return scheduleEffectAtFixedRate(task, initialDelay, period, TimeUnit.MILLISECONDS);
	}
	
	/**
	 * Schedules a general task to be executed after the given delay.
	 * @param task the task to execute
	 * @param delay the delay in the given time unit
	 * @param unit the time unit of the delay parameter
	 * @return a ScheduledFuture representing pending completion of the task, and whose get() method will throw an exception upon cancellation
	 */
	public ScheduledFuture<?> scheduleGeneral(Runnable task, long delay, TimeUnit unit)
	{
		try
		{
			return _generalScheduledThreadPool.schedule(new RunnableWrapper(task), delay, unit);
		}
		catch (RejectedExecutionException e)
		{
			return null; /* shutdown, ignore */
		}
	}
	
	/**
	 * Schedules a general task to be executed after the given delay.
	 * @param task the task to execute
	 * @param delay the delay in milliseconds
	 * @return a ScheduledFuture representing pending completion of the task, and whose get() method will throw an exception upon cancellation
	 */
	public ScheduledFuture<?> scheduleGeneral(Runnable task, long delay)
	{
		return scheduleGeneral(task, delay, TimeUnit.MILLISECONDS);
	}
	
	/**
	 * Schedules a general task to be executed at fixed rate.
	 * @param task the task to execute
	 * @param initialDelay the initial delay in the given time unit
	 * @param period the period between executions in the given time unit
	 * @param unit the time unit of the initialDelay and period parameters
	 * @return a ScheduledFuture representing pending completion of the task, and whose get() method will throw an exception upon cancellation
	 */
	public ScheduledFuture<?> scheduleGeneralAtFixedRate(Runnable task, long initialDelay, long period, TimeUnit unit)
	{
		try
		{
			return _generalScheduledThreadPool.scheduleAtFixedRate(new RunnableWrapper(task), initialDelay, period, unit);
		}
		catch (RejectedExecutionException e)
		{
			return null; /* shutdown, ignore */
		}
	}
	
	/**
	 * Schedules a event task to be executed after the given delay.
	 * @param task the task to execute
	 * @param delay the delay in the given time unit
	 * @param unit the time unit of the delay parameter
	 * @return a ScheduledFuture representing pending completion of the task, and whose get() method will throw an exception upon cancellation
	 */
	public ScheduledFuture<?> scheduleEvent(Runnable task, long delay, TimeUnit unit)
	{
		try
		{
			return _eventScheduledThreadPool.schedule(new RunnableWrapper(task), delay, unit);
		}
		catch (RejectedExecutionException e)
		{
			return null; /* shutdown, ignore */
		}
	}
	
	/**
	 * Schedules a event task to be executed after the given delay.
	 * @param task the task to execute
	 * @param delay the delay in milliseconds
	 * @return a ScheduledFuture representing pending completion of the task, and whose get() method will throw an exception upon cancellation
	 */
	public ScheduledFuture<?> scheduleEvent(Runnable task, long delay)
	{
		return scheduleEvent(task, delay, TimeUnit.MILLISECONDS);
	}
	
	/**
	 * Schedules a event task to be executed at fixed rate.
	 * @param task the task to execute
	 * @param initialDelay the initial delay in the given time unit
	 * @param period the period between executions in the given time unit
	 * @param unit the time unit of the initialDelay and period parameters
	 * @return a ScheduledFuture representing pending completion of the task, and whose get() method will throw an exception upon cancellation
	 */
	public ScheduledFuture<?> scheduleEventAtFixedRate(Runnable task, long initialDelay, long period, TimeUnit unit)
	{
		try
		{
			return _eventScheduledThreadPool.scheduleAtFixedRate(new RunnableWrapper(task), initialDelay, period, unit);
		}
		catch (RejectedExecutionException e)
		{
			return null; /* shutdown, ignore */
		}
	}
	
	/**
	 * Schedules a general task to be executed at fixed rate.
	 * @param task the task to execute
	 * @param initialDelay the initial delay in milliseconds
	 * @param period the period between executions in milliseconds
	 * @return a ScheduledFuture representing pending completion of the task, and whose get() method will throw an exception upon cancellation
	 */
	public ScheduledFuture<?> scheduleGeneralAtFixedRate(Runnable task, long initialDelay, long period)
	{
		return scheduleGeneralAtFixedRate(task, initialDelay, period, TimeUnit.MILLISECONDS);
	}
	
	/**
	 * Schedules an AI task to be executed after the given delay.
	 * @param task the task to execute
	 * @param delay the delay in the given time unit
	 * @param unit the time unit of the delay parameter
	 * @return a ScheduledFuture representing pending completion of the task, and whose get() method will throw an exception upon cancellation
	 */
	public ScheduledFuture<?> scheduleAi(Runnable task, long delay, TimeUnit unit)
	{
		try
		{
			return _aiScheduledThreadPool.schedule(new RunnableWrapper(task), delay, unit);
		}
		catch (RejectedExecutionException e)
		{
			return null; /* shutdown, ignore */
		}
	}
	
	/**
	 * Schedules an AI task to be executed after the given delay.
	 * @param task the task to execute
	 * @param delay the delay in milliseconds
	 * @return a ScheduledFuture representing pending completion of the task, and whose get() method will throw an exception upon cancellation
	 */
	public ScheduledFuture<?> scheduleAi(Runnable task, long delay)
	{
		return scheduleAi(task, delay, TimeUnit.MILLISECONDS);
	}
	
	/**
	 * Schedules a general task to be executed at fixed rate.
	 * @param task the task to execute
	 * @param initialDelay the initial delay in the given time unit
	 * @param period the period between executions in the given time unit
	 * @param unit the time unit of the initialDelay and period parameters
	 * @return a ScheduledFuture representing pending completion of the task, and whose get() method will throw an exception upon cancellation
	 */
	public ScheduledFuture<?> scheduleAiAtFixedRate(Runnable task, long initialDelay, long period, TimeUnit unit)
	{
		try
		{
			return _aiScheduledThreadPool.scheduleAtFixedRate(new RunnableWrapper(task), initialDelay, period, unit);
		}
		catch (RejectedExecutionException e)
		{
			return null; /* shutdown, ignore */
		}
	}
	
	/**
	 * Schedules a general task to be executed at fixed rate.
	 * @param task the task to execute
	 * @param initialDelay the initial delay in milliseconds
	 * @param period the period between executions in milliseconds
	 * @return a ScheduledFuture representing pending completion of the task, and whose get() method will throw an exception upon cancellation
	 */
	public ScheduledFuture<?> scheduleAiAtFixedRate(Runnable task, long initialDelay, long period)
	{
		return scheduleAiAtFixedRate(task, initialDelay, period, TimeUnit.MILLISECONDS);
	}
	
	/**
	 * Executes a packet task sometime in future in another thread.
	 * @param task the task to execute
	 */
	public void executePacket(Runnable task)
	{
		try
		{
			_generalPacketsThreadPool.execute(task);
		}
		catch (RejectedExecutionException e)
		{
			/* shutdown, ignore */
		}
	}
	
	/**
	 * Executes an IO packet task sometime in future in another thread.
	 * @param task the task to execute
	 */
	public void executeIOPacket(Runnable task)
	{
		try
		{
			_ioPacketsThreadPool.execute(task);
		}
		catch (RejectedExecutionException e)
		{
			/* shutdown, ignore */
		}
	}
	
	/**
	 * Executes a general task sometime in future in another thread.
	 * @param task the task to execute
	 */
	public void executeGeneral(Runnable task)
	{
		try
		{
			_generalThreadPool.execute(new RunnableWrapper(task));
		}
		catch (RejectedExecutionException e)
		{
			/* shutdown, ignore */
		}
	}
	
	/**
	 * Executes an AI task sometime in future in another thread.
	 * @param task the task to execute
	 */
	public void executeAi(Runnable task)
	{
		try
		{
			_aiScheduledThreadPool.execute(new RunnableWrapper(task));
		}
		catch (RejectedExecutionException e)
		{
			/* shutdown, ignore */
		}
	}
	
	/**
	 * Executes an Event task sometime in future in another thread.
	 * @param task the task to execute
	 */
	public void executeEvent(Runnable task)
	{
		try
		{
			_eventThreadPool.execute(new RunnableWrapper(task));
		}
		catch (RejectedExecutionException e)
		{
			/* shutdown, ignore */
		}
	}
	
	public StatsSet getStats(String category)
	{
		final StatsSet StatsSet = new StatsSet();
		
		switch (category)
		{
			case "EFFECTS":
			{
				StatsSet.set("ActiveThreads", _effectsScheduledThreadPool.getActiveCount());
				StatsSet.set("getCorePoolSize", _effectsScheduledThreadPool.getCorePoolSize());
				StatsSet.set("PoolSize", _effectsScheduledThreadPool.getPoolSize());
				StatsSet.set("MaximumPoolSize", _effectsScheduledThreadPool.getMaximumPoolSize());
				StatsSet.set("CompletedTasks", (int) _effectsScheduledThreadPool.getCompletedTaskCount());
				StatsSet.set("ScheduledTasks", _effectsScheduledThreadPool.getQueue().size());
				break;
			}
			case "GENERAL":
			{
				StatsSet.set("ActiveThreads", _generalScheduledThreadPool.getActiveCount());
				StatsSet.set("getCorePoolSizes", _generalScheduledThreadPool.getCorePoolSize());
				StatsSet.set("PoolSizes", _generalScheduledThreadPool.getPoolSize());
				StatsSet.set("MaximumPoolSizes", _generalScheduledThreadPool.getMaximumPoolSize());
				StatsSet.set("CompletedTasks", (int) _generalScheduledThreadPool.getCompletedTaskCount());
				StatsSet.set("ScheduledTasks", _generalScheduledThreadPool.getQueue().size());
				break;
			}
			case "AI":
			{
				StatsSet.set("ActiveThreads", _aiScheduledThreadPool.getActiveCount());
				StatsSet.set("getCorePoolSize", _aiScheduledThreadPool.getCorePoolSize());
				StatsSet.set("PoolSize", _aiScheduledThreadPool.getPoolSize());
				StatsSet.set("MaximumPoolSize", _aiScheduledThreadPool.getMaximumPoolSize());
				StatsSet.set("CompletedTasks", (int) _aiScheduledThreadPool.getCompletedTaskCount());
				StatsSet.set("ScheduledTasks", _aiScheduledThreadPool.getQueue().size());
				break;
			}
			case "EVENT":
			{
				StatsSet.set("ActiveThreads", _eventScheduledThreadPool.getActiveCount());
				StatsSet.set("getCorePoolSize", _eventScheduledThreadPool.getCorePoolSize());
				StatsSet.set("PoolSize", _eventScheduledThreadPool.getPoolSize());
				StatsSet.set("MaximumPoolSize", _eventScheduledThreadPool.getMaximumPoolSize());
				StatsSet.set("CompletedTasks", (int) _eventScheduledThreadPool.getCompletedTaskCount());
				StatsSet.set("ScheduledTasks", _eventScheduledThreadPool.getQueue().size());
				break;
			}
			case "PACKETS":
			{
				StatsSet.set("ActiveThreads", _generalPacketsThreadPool.getActiveCount());
				StatsSet.set("getCorePoolSize", _generalPacketsThreadPool.getCorePoolSize());
				StatsSet.set("MaximumPoolSize", _generalPacketsThreadPool.getMaximumPoolSize());
				StatsSet.set("LargestPoolSize", _generalPacketsThreadPool.getLargestPoolSize());
				StatsSet.set("PoolSize", _generalPacketsThreadPool.getPoolSize());
				StatsSet.set("CompletedTasks", (int) _generalPacketsThreadPool.getCompletedTaskCount());
				StatsSet.set("QueuedTasks", _generalPacketsThreadPool.getQueue().size());
				break;
			}
			case "IOPACKETS":
			{
				StatsSet.set("ActiveThreads", _ioPacketsThreadPool.getActiveCount());
				StatsSet.set("getCorePoolSize", _ioPacketsThreadPool.getCorePoolSize());
				StatsSet.set("MaximumPoolSize", _ioPacketsThreadPool.getMaximumPoolSize());
				StatsSet.set("LargestPoolSize", _ioPacketsThreadPool.getLargestPoolSize());
				StatsSet.set("PoolSize", _ioPacketsThreadPool.getPoolSize());
				StatsSet.set("CompletedTasks", (int) _ioPacketsThreadPool.getCompletedTaskCount());
				StatsSet.set("QueuedTasks", _ioPacketsThreadPool.getQueue().size());
				break;
			}
			case "GENERAL_TASKS":
			{
				StatsSet.set("ActiveThreads", _generalThreadPool.getActiveCount());
				StatsSet.set("getCorePoolSize", _generalThreadPool.getCorePoolSize());
				StatsSet.set("MaximumPoolSize", _generalThreadPool.getMaximumPoolSize());
				StatsSet.set("LargestPoolSize", _generalThreadPool.getLargestPoolSize());
				StatsSet.set("PoolSize", _generalThreadPool.getPoolSize());
				StatsSet.set("CompletedTasks", (int) _generalThreadPool.getCompletedTaskCount());
				StatsSet.set("QueuedTasks", _generalThreadPool.getQueue().size());
				break;
			}
			case "EVENT_TASKS":
			{
				StatsSet.set("ActiveThreads", _eventThreadPool.getActiveCount());
				StatsSet.set("getCorePoolSize", _eventThreadPool.getCorePoolSize());
				StatsSet.set("MaximumPoolSize", _eventThreadPool.getMaximumPoolSize());
				StatsSet.set("LargestPoolSize", _eventThreadPool.getLargestPoolSize());
				StatsSet.set("PoolSize", _eventThreadPool.getPoolSize());
				StatsSet.set("CompletedTasks", (int) _eventThreadPool.getCompletedTaskCount());
				StatsSet.set("QueuedTasks", _eventThreadPool.getQueue().size());
				break;
			}
		}
		return StatsSet;
	}
	
	public String[] getStats()
	{
		return new String[]
		{
			"STP:",
			" + Effects:",
			" |- ActiveThreads:   " + _effectsScheduledThreadPool.getActiveCount(),
			" |- getCorePoolSize: " + _effectsScheduledThreadPool.getCorePoolSize(),
			" |- PoolSize:        " + _effectsScheduledThreadPool.getPoolSize(),
			" |- MaximumPoolSize: " + _effectsScheduledThreadPool.getMaximumPoolSize(),
			" |- CompletedTasks:  " + _effectsScheduledThreadPool.getCompletedTaskCount(),
			" |- ScheduledTasks:  " + _effectsScheduledThreadPool.getQueue().size(),
			" | -------",
			" + General:",
			" |- ActiveThreads:   " + _generalScheduledThreadPool.getActiveCount(),
			" |- getCorePoolSize: " + _generalScheduledThreadPool.getCorePoolSize(),
			" |- PoolSize:        " + _generalScheduledThreadPool.getPoolSize(),
			" |- MaximumPoolSize: " + _generalScheduledThreadPool.getMaximumPoolSize(),
			" |- CompletedTasks:  " + _generalScheduledThreadPool.getCompletedTaskCount(),
			" |- ScheduledTasks:  " + _generalScheduledThreadPool.getQueue().size(),
			" | -------",
			" + AI:",
			" |- ActiveThreads:   " + _aiScheduledThreadPool.getActiveCount(),
			" |- getCorePoolSize: " + _aiScheduledThreadPool.getCorePoolSize(),
			" |- PoolSize:        " + _aiScheduledThreadPool.getPoolSize(),
			" |- MaximumPoolSize: " + _aiScheduledThreadPool.getMaximumPoolSize(),
			" |- CompletedTasks:  " + _aiScheduledThreadPool.getCompletedTaskCount(),
			" |- ScheduledTasks:  " + _aiScheduledThreadPool.getQueue().size(),
			" | -------",
			" + Event:",
			" |- ActiveThreads:   " + _eventScheduledThreadPool.getActiveCount(),
			" |- getCorePoolSize: " + _eventScheduledThreadPool.getCorePoolSize(),
			" |- PoolSize:        " + _eventScheduledThreadPool.getPoolSize(),
			" |- MaximumPoolSize: " + _eventScheduledThreadPool.getMaximumPoolSize(),
			" |- CompletedTasks:  " + _eventScheduledThreadPool.getCompletedTaskCount(),
			" |- ScheduledTasks:  " + _eventScheduledThreadPool.getQueue().size(),
			"TP:",
			" + Packets:",
			" |- ActiveThreads:   " + _generalPacketsThreadPool.getActiveCount(),
			" |- getCorePoolSize: " + _generalPacketsThreadPool.getCorePoolSize(),
			" |- MaximumPoolSize: " + _generalPacketsThreadPool.getMaximumPoolSize(),
			" |- LargestPoolSize: " + _generalPacketsThreadPool.getLargestPoolSize(),
			" |- PoolSize:        " + _generalPacketsThreadPool.getPoolSize(),
			" |- CompletedTasks:  " + _generalPacketsThreadPool.getCompletedTaskCount(),
			" |- QueuedTasks:     " + _generalPacketsThreadPool.getQueue().size(),
			" | -------",
			" + I/O Packets:",
			" |- ActiveThreads:   " + _ioPacketsThreadPool.getActiveCount(),
			" |- getCorePoolSize: " + _ioPacketsThreadPool.getCorePoolSize(),
			" |- MaximumPoolSize: " + _ioPacketsThreadPool.getMaximumPoolSize(),
			" |- LargestPoolSize: " + _ioPacketsThreadPool.getLargestPoolSize(),
			" |- PoolSize:        " + _ioPacketsThreadPool.getPoolSize(),
			" |- CompletedTasks:  " + _ioPacketsThreadPool.getCompletedTaskCount(),
			" |- QueuedTasks:     " + _ioPacketsThreadPool.getQueue().size(),
			" | -------",
			" + General Tasks:",
			" |- ActiveThreads:   " + _generalThreadPool.getActiveCount(),
			" |- getCorePoolSize: " + _generalThreadPool.getCorePoolSize(),
			" |- MaximumPoolSize: " + _generalThreadPool.getMaximumPoolSize(),
			" |- LargestPoolSize: " + _generalThreadPool.getLargestPoolSize(),
			" |- PoolSize:        " + _generalThreadPool.getPoolSize(),
			" |- CompletedTasks:  " + _generalThreadPool.getCompletedTaskCount(),
			" |- QueuedTasks:     " + _generalThreadPool.getQueue().size(),
			" | -------",
			" + Event Tasks:",
			" |- ActiveThreads:   " + _eventThreadPool.getActiveCount(),
			" |- getCorePoolSize: " + _eventThreadPool.getCorePoolSize(),
			" |- MaximumPoolSize: " + _eventThreadPool.getMaximumPoolSize(),
			" |- LargestPoolSize: " + _eventThreadPool.getLargestPoolSize(),
			" |- PoolSize:        " + _eventThreadPool.getPoolSize(),
			" |- CompletedTasks:  " + _eventThreadPool.getCompletedTaskCount(),
			" |- QueuedTasks:     " + _eventThreadPool.getQueue().size(),
			" | -------"
		};
	}
	
	private static class PriorityThreadFactory implements ThreadFactory
	{
		private final int _prio;
		private final String _name;
		private final AtomicInteger _threadNumber = new AtomicInteger(1);
		private final ThreadGroup _group;
		
		public PriorityThreadFactory(String name, int prio)
		{
			_prio = prio;
			_name = name;
			_group = new ThreadGroup(_name);
		}
		
		@Override
		public Thread newThread(Runnable r)
		{
			final Thread t = new Thread(_group, r, _name + "-" + _threadNumber.getAndIncrement());
			t.setPriority(_prio);
			return t;
		}
		
		public ThreadGroup getGroup()
		{
			return _group;
		}
	}
	
	public void shutdown()
	{
		_shutdown = true;
		try
		{
			_effectsScheduledThreadPool.awaitTermination(1, TimeUnit.SECONDS);
			_generalScheduledThreadPool.awaitTermination(1, TimeUnit.SECONDS);
			_generalPacketsThreadPool.awaitTermination(1, TimeUnit.SECONDS);
			_ioPacketsThreadPool.awaitTermination(1, TimeUnit.SECONDS);
			_generalThreadPool.awaitTermination(1, TimeUnit.SECONDS);
			_eventThreadPool.awaitTermination(1, TimeUnit.SECONDS);
			_effectsScheduledThreadPool.shutdown();
			_generalScheduledThreadPool.shutdown();
			_generalPacketsThreadPool.shutdown();
			_ioPacketsThreadPool.shutdown();
			_generalThreadPool.shutdown();
			_eventThreadPool.shutdown();
			_log.info("All ThreadPools are now stopped");
		}
		catch (InterruptedException e)
		{
			_log.log(Level.WARNING, "There has been a problem shuting down the thread pool manager!", e);
		}
	}
	
	public boolean isShutdown()
	{
		return _shutdown;
	}
	
	public void purge()
	{
		_effectsScheduledThreadPool.purge();
		_generalScheduledThreadPool.purge();
		_aiScheduledThreadPool.purge();
		_eventScheduledThreadPool.purge();
		_ioPacketsThreadPool.purge();
		_generalPacketsThreadPool.purge();
		_generalThreadPool.purge();
		_eventThreadPool.purge();
	}
	
	public String getPacketStats()
	{
		final StringBuilder sb = new StringBuilder(1000);
		final ThreadFactory tf = _generalPacketsThreadPool.getThreadFactory();
		if (tf instanceof PriorityThreadFactory)
		{
			final PriorityThreadFactory ptf = (PriorityThreadFactory) tf;
			final int count = ptf.getGroup().activeCount();
			final Thread[] threads = new Thread[count + 2];
			ptf.getGroup().enumerate(threads);
			sb.append("General Packet Thread Pool:" + Config.EOL + "Tasks in the queue: " + _generalPacketsThreadPool.getQueue().size() + Config.EOL + "Showing threads stack trace:" + Config.EOL + "There should be " + count + " Threads" + Config.EOL);
			for (Thread t : threads)
			{
				if (t == null)
				{
					continue;
				}
				
				sb.append(t.getName());
				sb.append(Config.EOL);
				for (StackTraceElement ste : t.getStackTrace())
				{
					sb.append(ste);
					sb.append(Config.EOL);
				}
			}
		}
		
		sb.append("Packet Tp stack traces printed.");
		sb.append(Config.EOL);
		return sb.toString();
	}
	
	public String getIOPacketStats()
	{
		final StringBuilder sb = new StringBuilder(1000);
		final ThreadFactory tf = _ioPacketsThreadPool.getThreadFactory();
		
		if (tf instanceof PriorityThreadFactory)
		{
			final PriorityThreadFactory ptf = (PriorityThreadFactory) tf;
			final int count = ptf.getGroup().activeCount();
			final Thread[] threads = new Thread[count + 2];
			ptf.getGroup().enumerate(threads);
			sb.append("I/O Packet Thread Pool:" + Config.EOL + "Tasks in the queue: " + _ioPacketsThreadPool.getQueue().size() + Config.EOL + "Showing threads stack trace:" + Config.EOL + "There should be " + count + " Threads" + Config.EOL);
			
			for (Thread t : threads)
			{
				if (t == null)
				{
					continue;
				}
				
				sb.append(t.getName());
				sb.append(Config.EOL);
				for (StackTraceElement ste : t.getStackTrace())
				{
					sb.append(ste);
					sb.append(Config.EOL);
				}
			}
		}
		
		sb.append("Packet Tp stack traces printed." + Config.EOL);
		
		return sb.toString();
	}
	
	public String getGeneralStats()
	{
		final StringBuilder sb = new StringBuilder(1000);
		final ThreadFactory tf = _generalThreadPool.getThreadFactory();
		
		if (tf instanceof PriorityThreadFactory)
		{
			final PriorityThreadFactory ptf = (PriorityThreadFactory) tf;
			final int count = ptf.getGroup().activeCount();
			final Thread[] threads = new Thread[count + 2];
			ptf.getGroup().enumerate(threads);
			sb.append("General Thread Pool:" + Config.EOL + "Tasks in the queue: " + _generalThreadPool.getQueue().size() + Config.EOL + "Showing threads stack trace:" + Config.EOL + "There should be " + +count + " Threads" + Config.EOL);
			
			for (Thread t : threads)
			{
				if (t == null)
				{
					continue;
				}
				
				sb.append(t.getName());
				sb.append(Config.EOL);
				for (StackTraceElement ste : t.getStackTrace())
				{
					sb.append(ste);
					sb.append(Config.EOL);
				}
			}
		}
		
		sb.append("Packet Tp stack traces printed." + Config.EOL);
		
		return sb.toString();
	}
	
	protected class PurgeTask implements Runnable
	{
		@Override
		public void run()
		{
			_effectsScheduledThreadPool.purge();
			_generalScheduledThreadPool.purge();
			_aiScheduledThreadPool.purge();
			_eventScheduledThreadPool.purge();
		}
	}
	
	private static class SingletonHolder
	{
		protected static final ThreadPoolManager _instance = new ThreadPoolManager();
	}
}