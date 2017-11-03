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

import com.la2eden.Config;
import com.la2eden.Server;
import com.la2eden.commons.database.DatabaseFactory;
import com.la2eden.commons.mmocore.SelectorConfig;
import com.la2eden.commons.mmocore.SelectorThread;
import com.la2eden.gameserver.cache.HtmCache;
import com.la2eden.gameserver.data.sql.impl.*;
import com.la2eden.gameserver.data.xml.impl.*;
import com.la2eden.gameserver.datatables.*;
import com.la2eden.gameserver.handler.EffectHandler;
import com.la2eden.gameserver.idfactory.IdFactory;
import com.la2eden.gameserver.instancemanager.*;
import com.la2eden.gameserver.model.AutoSpawnHandler;
import com.la2eden.gameserver.model.L2World;
import com.la2eden.gameserver.model.PartyMatchRoomList;
import com.la2eden.gameserver.model.PartyMatchWaitingList;
import com.la2eden.gameserver.model.entity.Hero;
import com.la2eden.gameserver.model.entity.TvTManager;
import com.la2eden.gameserver.model.events.EventDispatcher;
import com.la2eden.gameserver.model.olympiad.Olympiad;
import com.la2eden.gameserver.network.L2GameClient;
import com.la2eden.gameserver.network.L2GamePacketHandler;
import com.la2eden.gameserver.pathfinding.PathFinding;
import com.la2eden.gameserver.scripting.ScriptEngineManager;
import com.la2eden.gameserver.taskmanager.KnownListUpdateTaskManager;
import com.la2eden.gameserver.taskmanager.TaskManager;
import com.la2eden.status.Status;
import com.la2eden.util.DeadLockDetector;
import com.la2eden.util.IPv4Filter;

import java.awt.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Calendar;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

public final class GameServer
{
	private static final Logger _log = Logger.getLogger(GameServer.class.getName());

	// Local Constants
	private static final String LOG_FOLDER = "log"; // Name of folder for log file
	private static final String LOG_NAME = "./log.cfg"; // Name of log file

	private final SelectorThread<L2GameClient> _selectorThread;
	private final L2GamePacketHandler _gamePacketHandler;
	private final DeadLockDetector _deadDetectThread;
	public static GameServer gameServer;
	public static final Calendar dateTimeServerStarted = Calendar.getInstance();

	public GameServer() throws Exception
	{
		final long serverLoadStart = System.currentTimeMillis();

		if (!IdFactory.getInstance().isInitialized())
		{
			_log.severe(getClass().getSimpleName() + ": Could not read object IDs from database. Please check your configuration.");
			throw new Exception("Could not initialize the ID factory!");
		}

		ThreadPoolManager.getInstance();
		EventDispatcher.getInstance();

		new File("log/game").mkdirs();

		// load script engines
		printSection("Scripting Engines");
		ScriptEngineManager.getInstance();

		printSection("World");
		// start game time control early
		GameTimeController.init();
		InstanceManager.getInstance();
		L2World.getInstance();
		MapRegionManager.getInstance();
		AnnouncementsTable.getInstance();
		GlobalVariablesManager.getInstance();

		printSection("Data");
		CategoryData.getInstance();
		SecondaryAuthData.getInstance();

		printSection("Skills");
		EffectHandler.getInstance().executeScript();
		EnchantSkillGroupsData.getInstance();
		SkillTreesData.getInstance();
		SkillData.getInstance();
		SummonSkillsTable.getInstance();

		printSection("Items");
		ItemTable.getInstance();
		EnchantItemGroupsData.getInstance();
		EnchantItemData.getInstance();
		EnchantItemOptionsData.getInstance();
		OptionData.getInstance();
		EnchantItemHPBonusData.getInstance();
		MerchantPriceConfigTable.getInstance().loadInstances();
		BuyListData.getInstance();
		MultisellData.getInstance();
		RecipeData.getInstance();
		ArmorSetsData.getInstance();
		FishData.getInstance();
		FishingMonstersData.getInstance();
		FishingRodsData.getInstance();
		HennaData.getInstance();

		printSection("Characters");
		ClassListData.getInstance();
		InitialEquipmentData.getInstance();
		InitialShortcutData.getInstance();
		ExperienceData.getInstance();
		PlayerXpPercentLostData.getInstance();
		KarmaData.getInstance();
		HitConditionBonusData.getInstance();
		PlayerTemplateData.getInstance();
		CharNameTable.getInstance();
		AdminData.getInstance();
		RaidBossPointsManager.getInstance();
		PetDataTable.getInstance();
		CharSummonTable.getInstance().init();

		if (Config.PREMIUM_SYSTEM_ENABLED)
		{
			_log.info("PremiumManager: Premium system is enabled.");
			PremiumManager.getInstance();
		}

		if (Config.PRIMESHOP_ENABLED) {
            _log.info("PrimeShop: system is enabled.");
            PrimeShopTable.getInstance();
        }

		printSection("Clans");
		ClanTable.getInstance();
		CHSiegeManager.getInstance();
		ClanHallManager.getInstance();
		ClanHallAuctionManager.getInstance();

		printSection("Geodata");
		long geodataMemory = getUsedMemoryMB();
		GeoData.getInstance();
		if (Config.PATHFINDING > 0)
		{
			PathFinding.getInstance();
		}
		geodataMemory -= getUsedMemoryMB();
		if (geodataMemory < 0)
		{
			geodataMemory = 0;
		}

		printSection("NPCs");
		SkillLearnData.getInstance();
		NpcData.getInstance();
		WalkingManager.getInstance();
		StaticObjectData.getInstance();
		ZoneManager.getInstance();
		DoorData.getInstance();
		ItemAuctionManager.getInstance();
		CastleManager.getInstance().loadInstances();
		NpcBufferTable.getInstance();
		GrandBossManager.getInstance().initZones();
		EventDroplist.getInstance();

		printSection("Olympiad");
		Olympiad.getInstance();
		Hero.getInstance();

		printSection("Seven Signs");
		SevenSigns.getInstance();

		// Call to load caches
		printSection("Cache");
		HtmCache.getInstance();
		CrestTable.getInstance();
		TeleportLocationTable.getInstance();
		UIData.getInstance();
		PartyMatchWaitingList.getInstance();
		PartyMatchRoomList.getInstance();
		PetitionManager.getInstance();
		AugmentationData.getInstance();
		CursedWeaponsManager.getInstance();
		TransformData.getInstance();
		BotReportTable.getInstance();

		printSection("Scripts");
		QuestManager.getInstance();
		BoatManager.getInstance();
		AirShipManager.getInstance();
		SoDManager.getInstance();
		SoIManager.getInstance();

		try
		{
			_log.info("Loading server scripts...");
			ScriptEngineManager.getInstance().executeScriptList();
		}
		catch (Exception e)
		{
			_log.log(Level.WARNING, "Failed to execute script list!", e);
		}

		SpawnTable.getInstance().load();
		DayNightSpawnManager.getInstance().trim().notifyChangeMode();
		FourSepulchersManager.getInstance().init();
		DimensionalRiftManager.getInstance();
		RaidBossSpawnManager.getInstance();

		printSection("Siege");
		SiegeManager.getInstance().getSieges();
		CastleManager.getInstance().activateInstances();
		FortManager.getInstance().loadInstances();
		FortManager.getInstance().activateInstances();
		FortSiegeManager.getInstance();
		SiegeScheduleData.getInstance();

		MerchantPriceConfigTable.getInstance().updateReferences();
		TerritoryWarManager.getInstance();
		CastleManorManager.getInstance();
		MercTicketManager.getInstance();

		QuestManager.getInstance().report();

		if (Config.SAVE_DROPPED_ITEM)
		{
			ItemsOnGroundManager.getInstance();
		}

		if ((Config.AUTODESTROY_ITEM_AFTER > 0) || (Config.HERB_AUTO_DESTROY_TIME > 0))
		{
			ItemsAutoDestroy.getInstance();
		}

		MonsterRace.getInstance();

		SevenSigns.getInstance().spawnSevenSignsNPC();
		SevenSignsFestival.getInstance();
		AutoSpawnHandler.getInstance();

		_log.info("AutoSpawnHandler: Loaded " + AutoSpawnHandler.getInstance().size() + " handlers in total.");

		if (Config.L2JMOD_ALLOW_WEDDING)
		{
			CoupleManager.getInstance();
		}


		if (Config.ALT_FISH_CHAMPIONSHIP_ENABLED)
		{
			FishingChampionshipManager.getInstance();
		}

		TaskManager.getInstance();

		AntiFeedManager.getInstance().registerEvent(AntiFeedManager.GAME_ID);

		if (Config.ALLOW_MAIL)
		{
			MailManager.getInstance();
		}

		PunishmentManager.getInstance();

		Runtime.getRuntime().addShutdownHook(Shutdown.getInstance());

		_log.info("IdFactory: Free ObjectID's remaining: " + IdFactory.getInstance().size());

		TvTManager.getInstance();
		KnownListUpdateTaskManager.getInstance();

		if (Config.ALLOW_AWAY_STATUS) {
			// printSection("Away System");
			AwayManager.getInstance();
		}

		if ((Config.OFFLINE_TRADE_ENABLE || Config.OFFLINE_CRAFT_ENABLE) && Config.RESTORE_OFFLINERS)
		{
			OfflineTradersTable.getInstance().restoreOfflineTraders();
		}

		if (Config.DEADLOCK_DETECTOR)
		{
			_deadDetectThread = new DeadLockDetector();
			_deadDetectThread.setDaemon(true);
			_deadDetectThread.start();
		}
		else
		{
			_deadDetectThread = null;
		}
		System.gc();
		final long totalMem = Runtime.getRuntime().maxMemory() / 1048576;
		_log.info(getClass().getSimpleName() + ": Started, using " + getUsedMemoryMB() + " of " + totalMem + " MB total memory.");
		_log.info(getClass().getSimpleName() + ": Geodata use " + geodataMemory + " MB of memory.");
		_log.info(getClass().getSimpleName() + ": Server loaded in " + ((System.currentTimeMillis() - serverLoadStart) / 1000) + " seconds.");

		final SelectorConfig sc = new SelectorConfig();
		sc.MAX_READ_PER_PASS = Config.MMO_MAX_READ_PER_PASS;
		sc.MAX_SEND_PER_PASS = Config.MMO_MAX_SEND_PER_PASS;
		sc.SLEEP_TIME = Config.MMO_SELECTOR_SLEEP_TIME;
		sc.HELPER_BUFFER_COUNT = Config.MMO_HELPER_BUFFER_COUNT;
		sc.TCP_NODELAY = Config.MMO_TCP_NODELAY;

		_gamePacketHandler = new L2GamePacketHandler();
		_selectorThread = new SelectorThread<>(sc, _gamePacketHandler, _gamePacketHandler, _gamePacketHandler, new IPv4Filter());

		InetAddress bindAddress = null;
		if (!Config.GAMESERVER_HOSTNAME.equals("*"))
		{
			try
			{
				bindAddress = InetAddress.getByName(Config.GAMESERVER_HOSTNAME);
			}
			catch (UnknownHostException e1)
			{
				_log.log(Level.SEVERE, getClass().getSimpleName() + ": WARNING: The GameServer bind address is invalid, using all avaliable IPs! Reason: " + e1.getMessage(), e1);
			}
		}

		try
		{
			_selectorThread.openServerSocket(bindAddress, Config.PORT_GAME);
			_selectorThread.start();
			_log.log(Level.INFO, getClass().getSimpleName() + ": is now listening on: " + Config.GAMESERVER_HOSTNAME + ":" + Config.PORT_GAME);
		}
		catch (IOException e)
		{
			_log.log(Level.SEVERE, getClass().getSimpleName() + ": FATAL: Failed to open server socket. Reason: " + e.getMessage(), e);
			System.exit(1);
		}

		if (Config.SERVER_RESTART_SCHEDULE_ENABLED)
		{
			ServerRestartManager.getInstance();
		}

		LoginServerThread.getInstance().start();
		Toolkit.getDefaultToolkit().beep();
	}

	public static void main(String[] args) throws Exception
	{
		Server.serverMode = Server.MODE_GAMESERVER;

		/*** Main ***/
		// Create log folder
		final File logFolder = new File(Config.DATAPACK_ROOT, LOG_FOLDER);
		logFolder.mkdir();

		// Create input stream for log file -- or store file data into memory
		try (InputStream is = new FileInputStream(new File(LOG_NAME)))
		{
			LogManager.getLogManager().readConfiguration(is);
		}

		// Initialize config
		Config.load();

        // Show project information
        printSection("La2Eden");
        projectInfo();

		printSection("Database");
		DatabaseFactory.getInstance();
		gameServer = new GameServer();

		if (Config.IS_TELNET_ENABLED)
		{
			new Status(Server.serverMode).start();
		}
	}

	public long getUsedMemoryMB()
	{
		return (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / 1048576;
	}

	public SelectorThread<L2GameClient> getSelectorThread()
	{
		return _selectorThread;
	}

	public L2GamePacketHandler getL2GamePacketHandler()
	{
		return _gamePacketHandler;
	}

	public DeadLockDetector getDeadLockDetectorThread()
	{
		return _deadDetectThread;
	}

	public static void printSection(String s)
	{
		s = "=[ " + s + " ]";
		while (s.length() < 61)
		{
			s = "-" + s;
		}
		_log.info(s);
	}

    public static void projectInfo()
    {
        _log.info("    -> Get access to source files at our website. <-    ");
        _log.info("               -> https://la2eden.com <-                ");
        _log.info("");
		_log.info("Max online players: .......... " + Config.MAXIMUM_ONLINE_USERS);
        _log.info("Build Number: ................ " + Config.BUILD_NUMBER);
		_log.info("Build Commit: ................ " + Config.SHORT_COMMIT);
		_log.info("Build Date: .................. " + Config.BUILD_DATE);
        _log.info("");
    }
}
