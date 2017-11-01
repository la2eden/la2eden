package com.la2eden.gameserver.instancemanager;

import com.la2eden.Config;
import com.la2eden.gameserver.ThreadPoolManager;
import com.la2eden.gameserver.ai.CtrlIntention;
import com.la2eden.gameserver.model.actor.instance.L2PcInstance;
import com.la2eden.gameserver.network.serverpackets.SetupGauge;
import com.la2eden.gameserver.network.serverpackets.SocialAction;

import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.logging.Logger;

/**
 * @author Michiru
 */
public final class AwayManager
{
	private static final Logger _log = Logger.getLogger(AwayManager.class.getName());
	private static AwayManager _instance;
	final Map<L2PcInstance, RestoreData> _awayPlayers;
	
	public static final AwayManager getInstance()
	{
		if (_instance == null)
		{
			_instance = new AwayManager();
			_log.info("AwayManager: Away system has been initialized.");
		}
		return _instance;
	}
	
	private final class RestoreData
	{
		private final String _originalTitle;
		private final int _originalTitleColor;
		private final boolean _sitForced;
		
		public RestoreData(L2PcInstance activeChar)
		{
			_originalTitle = activeChar.getTitle();
			_originalTitleColor = activeChar.getAppearance().getTitleColor();
			_sitForced = !activeChar.isSitting();
		}
		
		public boolean isSitForced()
		{
			return _sitForced;
		}
		
		public void restore(L2PcInstance activeChar)
		{
			activeChar.getAppearance().setTitleColor(_originalTitleColor);
			activeChar.setTitle(_originalTitle);
		}
	}
	
	private AwayManager()
	{
		_awayPlayers = Collections.synchronizedMap(new WeakHashMap<L2PcInstance, RestoreData>());
	}
	
	/**
	 * @param activeChar
	 * @param text
	 */
	public void setAway(L2PcInstance activeChar, String text)
	{
		activeChar.broadcastPacket(new SocialAction(activeChar.getObjectId(), 9));
		activeChar.sendMessage("You are going into away mode in " + Config.AWAY_TIMER + " seconds.");
		activeChar.getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
		SetupGauge sg = new SetupGauge(SetupGauge.BLUE, Config.AWAY_TIMER * 1000);
		activeChar.sendPacket(sg);
		activeChar.setIsImmobilized(true);
		ThreadPoolManager.getInstance().scheduleGeneral(new setPlayerAwayTask(activeChar, text), Config.AWAY_TIMER * 1000);
	}
	
	/**
	 * @param activeChar
	 */
	public void setBack(L2PcInstance activeChar)
	{
		activeChar.sendMessage("You will be back from away mode in " + Config.BACK_TIMER + " seconds.");
		SetupGauge sg = new SetupGauge(SetupGauge.BLUE, Config.BACK_TIMER * 1000);
		activeChar.sendPacket(sg);
		ThreadPoolManager.getInstance().scheduleGeneral(new setPlayerBackTask(activeChar), Config.BACK_TIMER * 1000);
	}
	
	class setPlayerAwayTask implements Runnable
	{
		
		private final L2PcInstance _activeChar;
		private final String _awayText;
		
		setPlayerAwayTask(L2PcInstance activeChar, String awayText)
		{
			_activeChar = activeChar;
			_awayText = awayText;
		}
		
		@Override
		public void run()
		{
			if (_activeChar == null)
			{
				return;
			}
			if (_activeChar.isAttackingNow() || _activeChar.isCastingNow())
			{
				return;
			}
			
			_awayPlayers.put(_activeChar, new RestoreData(_activeChar));
			
			_activeChar.disableAllSkills();
			_activeChar.abortAttack();
			_activeChar.abortCast();
			_activeChar.setTarget(null);
			_activeChar.setIsImmobilized(false);
			if (!_activeChar.isSitting())
			{
				_activeChar.sitDown(true);
			}
			if (_awayText.length() <= 1)
			{
				_activeChar.sendMessage("You are now in away mode.");
			}
			else
			{
				_activeChar.sendMessage("You are now in away mode (" + _awayText + ").");
			}
			
			_activeChar.getAppearance().setTitleColor(Config.AWAY_TITLE_COLOR);
			if (_awayText.length() <= 1)
			{
				_activeChar.setTitle("* Away *");
			}
			else
			{
				_activeChar.setTitle("Away <" + _awayText + ">");
			}
			_activeChar.broadcastUserInfo();
			_activeChar.setIsParalyzed(true);
			_activeChar.setIsAway(true);
		}
	}
	
	class setPlayerBackTask implements Runnable
	{
		
		private final L2PcInstance _activeChar;
		
		setPlayerBackTask(L2PcInstance activeChar)
		{
			_activeChar = activeChar;
		}
		
		@Override
		public void run()
		{
			if (_activeChar == null)
			{
				return;
			}
			RestoreData rd = _awayPlayers.get(_activeChar);
			if (rd == null)
			{
				return;
			}
			_activeChar.setIsParalyzed(false);
			_activeChar.enableAllSkills();
			_activeChar.setIsAway(false);
			if (rd.isSitForced())
			{
				_activeChar.standUp();
			}
			rd.restore(_activeChar);
			_awayPlayers.remove(_activeChar);
			_activeChar.broadcastUserInfo();
			_activeChar.sendMessage("Welcome back!");
		}
	}
}