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
package com.la2eden;

import com.la2eden.gameserver.GameServer;
import com.la2eden.gameserver.enums.ChatType;
import com.la2eden.gameserver.enums.IllegalActionPunishmentType;
import com.la2eden.gameserver.model.L2World;
import com.la2eden.gameserver.model.holders.ItemHolder;
import com.la2eden.gameserver.util.FloodProtectorConfig;
import com.la2eden.gameserver.util.Util;
import com.la2eden.util.PropertiesParser;
import com.la2eden.util.data.xml.IXmlReader;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import java.io.*;
import java.math.BigInteger;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * This class loads all the game server related configurations from files.<br>
 * The files are usually located in config folder in server root folder.<br>
 */
public final class Config
{
	private static final Logger _log = Logger.getLogger(Config.class.getName());
	
	// --------------------------------------------------
	// Constants
	// --------------------------------------------------
	public static final String EOL = System.lineSeparator();

    // --------------------------------------------------
    // Project-related constants
    // --------------------------------------------------
	public static final ProjectType PROJECT_TYPE = ProjectType.VIP;
	public static final Integer MAX_FREE_USERS = 300;
    public static final Integer MAX_VIP_USERS = 5000;
	
	// --------------------------------------------------
	// L2J Initialization File Definitions
	// --------------------------------------------------
	public static final String CHARACTER_CONFIG_FILE = "./config/main/Character.ini";
	public static final String FEATURE_CONFIG_FILE = "./config/main/Feature.ini";
	public static final String FORTSIEGE_CONFIGURATION_FILE = "./config/main/FortSiege.ini";
	public static final String GENERAL_CONFIG_FILE = "./config/main/General.ini";
	public static final String HEXID_FILE = "./config/hexid.txt";
	public static final String ID_CONFIG_FILE = "./config/core/IdFactory.ini";
	public static final String LOGIN_CONFIGURATION_FILE = "./config/network/LoginServer.ini";
	public static final String NPC_CONFIG_FILE = "./config/main/NPC.ini";
	public static final String PVP_CONFIG_FILE = "./config/main/PvP.ini";
	public static final String RATES_CONFIG_FILE = "./config/main/Rates.ini";
	public static final String CONFIGURATION_FILE = "./config/network/GameServer.ini";
	public static final String IP_CONFIG_FILE = "./config/network/ipconfig.xml";
	public static final String SIEGE_CONFIGURATION_FILE = "./config/main/Siege.ini";
	public static final String TW_CONFIGURATION_FILE = "./config/main/TerritoryWar.ini";
	public static final String TELNET_FILE = "./config/network/Telnet.ini";
	public static final String FLOOD_PROTECTOR_FILE = "./config/core/FloodProtector.ini";
	public static final String MMO_CONFIG_FILE = "./config/network/MMO.ini";
	public static final String OLYMPIAD_CONFIG_FILE = "./config/main/Olympiad.ini";
	public static final String GRANDBOSS_CONFIG_FILE = "./config/main/GrandBoss.ini";
	public static final String GRACIASEEDS_CONFIG_FILE = "./config/main/GraciaSeeds.ini";
	public static final String CHAT_FILTER_FILE = "./config/main/chatfilter.txt";
	public static final String CH_SIEGE_FILE = "./config/main/ConquerableHallSiege.ini";
	public static final String GEODATA_FILE = "./config/main/GeoData.ini";

    // Configuration files for events
    public static final String TVT_EVENT_FILE = "./config/events/TvT.ini";
    public static final String CTF_EVENT_FILE = "./config/events/CTF.ini";
    public static final String DEATHMATCH_EVENT_FILE = "./config/mods/DM.ini";

    // Configuration files for custom mods
    public static final String BANKING_FILE = "./config/mods/Banking.ini";
    public static final String CHAMPION_FILE = "./config/mods/Champion.ini";
    public static final String CHAT_FILE = "./config/mods/Chat.ini";
    public static final String COMMUNITY_FILE = "./config/mods/CommunityBoard.ini";
    public static final String CUSTOM_CONFIG_FILE = "./config/mods/Custom.ini";
    public static final String HITMAN_FILE = "./config/mods/Hitman.ini";
    public static final String OFFLINE_TRADE_FILE = "./config/mods/OfflineShop.ini";
    public static final String PREMIUM_FILE = "./config/mods/Premium.ini";
    public static final String WEDDING_FILE = "./config/mods/Wedding.ini";

    // Version files
    public static final String SERVER_VERSION_FILE = "./config/core/Version.ini";

	// --------------------------------------------------
	// L2J Variable Definitions
	// --------------------------------------------------
	public static boolean PLAYER_DELEVEL;
	public static boolean DECREASE_SKILL_LEVEL;
	public static double ALT_WEIGHT_LIMIT;
	public static int RUN_SPD_BOOST;
	public static int DEATH_PENALTY_CHANCE;
	public static double RESPAWN_RESTORE_CP;
	public static double RESPAWN_RESTORE_HP;
	public static double RESPAWN_RESTORE_MP;
	public static boolean ENABLE_MODIFY_SKILL_DURATION;
	public static Map<Integer, Integer> SKILL_DURATION_LIST;
	public static boolean ENABLE_MODIFY_SKILL_REUSE;
	public static Map<Integer, Integer> SKILL_REUSE_LIST;
	public static boolean AUTO_LEARN_SKILLS;
	public static boolean AUTO_LEARN_FS_SKILLS;
	public static boolean AUTO_LOOT_HERBS;
	public static byte BUFFS_MAX_AMOUNT;
	public static byte TRIGGERED_BUFFS_MAX_AMOUNT;
	public static byte DANCES_MAX_AMOUNT;
	public static boolean DANCE_CANCEL_BUFF;
	public static boolean DANCE_CONSUME_ADDITIONAL_MP;
	public static boolean ALT_STORE_DANCES;
	public static boolean AUTO_LEARN_DIVINE_INSPIRATION;
	public static boolean ALT_GAME_CANCEL_BOW;
	public static boolean ALT_GAME_CANCEL_CAST;
	public static boolean ALT_GAME_MAGICFAILURES;
	public static int PLAYER_FAKEDEATH_UP_PROTECTION;
	public static boolean STORE_SKILL_COOLTIME;
	public static boolean SUBCLASS_STORE_SKILL_COOLTIME;
	public static boolean SUMMON_STORE_SKILL_COOLTIME;
	public static boolean ALT_GAME_SHIELD_BLOCKS;
	public static int ALT_PERFECT_SHLD_BLOCK;
	public static long EFFECT_TICK_RATIO;
	public static boolean ALLOW_CLASS_MASTERS;
	public static ClassMasterSettings CLASS_MASTER_SETTINGS;
	public static boolean ALLOW_ENTIRE_TREE;
	public static boolean ALTERNATE_CLASS_MASTER;
	public static boolean LIFE_CRYSTAL_NEEDED;
	public static boolean ES_SP_BOOK_NEEDED;
	public static boolean DIVINE_SP_BOOK_NEEDED;
	public static boolean ALT_GAME_SKILL_LEARN;
	public static boolean ALT_GAME_SUBCLASS_WITHOUT_QUESTS;
	public static boolean ALT_GAME_SUBCLASS_EVERYWHERE;
	public static boolean ALLOW_TRANSFORM_WITHOUT_QUEST;
	public static int FEE_DELETE_TRANSFER_SKILLS;
	public static int FEE_DELETE_SUBCLASS_SKILLS;
	public static boolean RESTORE_SERVITOR_ON_RECONNECT;
	public static boolean RESTORE_PET_ON_RECONNECT;
	public static double MAX_BONUS_EXP;
	public static double MAX_BONUS_SP;
	public static int MAX_RUN_SPEED;
	public static int MAX_PCRIT_RATE;
	public static int MAX_MCRIT_RATE;
	public static int MAX_PATK_SPEED;
	public static int MAX_MATK_SPEED;
	public static int MAX_EVASION;
	public static int MIN_ABNORMAL_STATE_SUCCESS_RATE;
	public static int MAX_ABNORMAL_STATE_SUCCESS_RATE;
	public static byte MAX_SUBCLASS;
	public static byte BASE_SUBCLASS_LEVEL;
	public static byte MAX_SUBCLASS_LEVEL;
	public static int MAX_PVTSTORESELL_SLOTS_DWARF;
	public static int MAX_PVTSTORESELL_SLOTS_OTHER;
	public static int MAX_PVTSTOREBUY_SLOTS_DWARF;
	public static int MAX_PVTSTOREBUY_SLOTS_OTHER;
	public static int INVENTORY_MAXIMUM_NO_DWARF;
	public static int INVENTORY_MAXIMUM_DWARF;
	public static int INVENTORY_MAXIMUM_GM;
	public static int INVENTORY_MAXIMUM_QUEST_ITEMS;
	public static int WAREHOUSE_SLOTS_DWARF;
	public static int WAREHOUSE_SLOTS_NO_DWARF;
	public static int WAREHOUSE_SLOTS_CLAN;
	public static int ALT_FREIGHT_SLOTS;
	public static int ALT_FREIGHT_PRICE;
	public static boolean ALT_GAME_KARMA_PLAYER_CAN_BE_KILLED_IN_PEACEZONE;
	public static boolean ALT_GAME_KARMA_PLAYER_CAN_SHOP;
	public static boolean ALT_GAME_KARMA_PLAYER_CAN_TELEPORT;
	public static boolean ALT_GAME_KARMA_PLAYER_CAN_USE_GK;
	public static boolean ALT_GAME_KARMA_PLAYER_CAN_TRADE;
	public static boolean ALT_GAME_KARMA_PLAYER_CAN_USE_WAREHOUSE;
	public static int MAX_PERSONAL_FAME_POINTS;
	public static int FORTRESS_ZONE_FAME_TASK_FREQUENCY;
	public static int FORTRESS_ZONE_FAME_AQUIRE_POINTS;
	public static int CASTLE_ZONE_FAME_TASK_FREQUENCY;
	public static int CASTLE_ZONE_FAME_AQUIRE_POINTS;
	public static boolean FAME_FOR_DEAD_PLAYERS;
	public static boolean IS_CRAFTING_ENABLED;
	public static boolean CRAFT_MASTERWORK;
	public static int DWARF_RECIPE_LIMIT;
	public static int COMMON_RECIPE_LIMIT;
	public static boolean ALT_GAME_CREATION;
	public static double ALT_GAME_CREATION_SPEED;
	public static double ALT_GAME_CREATION_XP_RATE;
	public static double ALT_GAME_CREATION_RARE_XPSP_RATE;
	public static double ALT_GAME_CREATION_SP_RATE;
	public static boolean ALT_BLACKSMITH_USE_RECIPES;
	public static int ALT_CLAN_LEADER_DATE_CHANGE;
	public static String ALT_CLAN_LEADER_HOUR_CHANGE;
	public static boolean ALT_CLAN_LEADER_INSTANT_ACTIVATION;
	public static int ALT_CLAN_JOIN_DAYS;
	public static int ALT_CLAN_CREATE_DAYS;
	public static int ALT_CLAN_DISSOLVE_DAYS;
	public static int ALT_ALLY_JOIN_DAYS_WHEN_LEAVED;
	public static int ALT_ALLY_JOIN_DAYS_WHEN_DISMISSED;
	public static int ALT_ACCEPT_CLAN_DAYS_WHEN_DISMISSED;
	public static int ALT_CREATE_ALLY_DAYS_WHEN_DISSOLVED;
	public static int ALT_MAX_NUM_OF_CLANS_IN_ALLY;
	public static int ALT_CLAN_MEMBERS_FOR_WAR;
	public static boolean ALT_MEMBERS_CAN_WITHDRAW_FROM_CLANWH;
	public static boolean REMOVE_CASTLE_CIRCLETS;
	public static int ALT_PARTY_RANGE;
	public static int ALT_PARTY_RANGE2;
	public static boolean ALT_LEAVE_PARTY_LEADER;
	public static boolean INITIAL_EQUIPMENT_EVENT;
	public static long STARTING_ADENA;
	public static byte STARTING_LEVEL;
	public static int STARTING_SP;
	public static long MAX_ADENA;
	public static boolean AUTO_LOOT;
	public static boolean AUTO_LOOT_RAIDS;
	public static int LOOT_RAIDS_PRIVILEGE_INTERVAL;
	public static int LOOT_RAIDS_PRIVILEGE_CC_SIZE;
	public static int UNSTUCK_INTERVAL;
	public static int TELEPORT_WATCHDOG_TIMEOUT;
	public static int PLAYER_SPAWN_PROTECTION;
	public static List<Integer> SPAWN_PROTECTION_ALLOWED_ITEMS;
	public static int PLAYER_TELEPORT_PROTECTION;
	public static boolean RANDOM_RESPAWN_IN_TOWN_ENABLED;
	public static boolean OFFSET_ON_TELEPORT_ENABLED;
	public static int MAX_OFFSET_ON_TELEPORT;
	public static boolean PETITIONING_ALLOWED;
	public static int MAX_PETITIONS_PER_PLAYER;
	public static int MAX_PETITIONS_PENDING;
	public static boolean FREE_TELEPORTING;
	public static int DELETE_DAYS;
	public static float ALT_GAME_EXPONENT_XP;
	public static float ALT_GAME_EXPONENT_SP;
	public static String PARTY_XP_CUTOFF_METHOD;
	public static double PARTY_XP_CUTOFF_PERCENT;
	public static int PARTY_XP_CUTOFF_LEVEL;
	public static int[][] PARTY_XP_CUTOFF_GAPS;
	public static int[] PARTY_XP_CUTOFF_GAP_PERCENTS;
	public static boolean DISABLE_TUTORIAL;
	public static boolean EXPERTISE_PENALTY;
	public static boolean STORE_RECIPE_SHOPLIST;
	public static boolean STORE_UI_SETTINGS;
	public static String[] FORBIDDEN_NAMES;
	public static boolean SILENCE_MODE_EXCLUDE;
	public static boolean ALT_VALIDATE_TRIGGER_SKILLS;
	
	public static boolean NEVIT_ENABLED;
	public static int NEVIT_MAX_POINTS;
	public static int NEVIT_BONUS_EFFECT_TIME;
	public static int NEVIT_ADVENT_TIME;
	public static boolean NEVIT_IGNORE_ADVENT_TIME;
	
	// --------------------------------------------------
	// ClanHall Settings
	// --------------------------------------------------
	public static long CH_TELE_FEE_RATIO;
	public static int CH_TELE1_FEE;
	public static int CH_TELE2_FEE;
	public static long CH_ITEM_FEE_RATIO;
	public static int CH_ITEM1_FEE;
	public static int CH_ITEM2_FEE;
	public static int CH_ITEM3_FEE;
	public static long CH_MPREG_FEE_RATIO;
	public static int CH_MPREG1_FEE;
	public static int CH_MPREG2_FEE;
	public static int CH_MPREG3_FEE;
	public static int CH_MPREG4_FEE;
	public static int CH_MPREG5_FEE;
	public static long CH_HPREG_FEE_RATIO;
	public static int CH_HPREG1_FEE;
	public static int CH_HPREG2_FEE;
	public static int CH_HPREG3_FEE;
	public static int CH_HPREG4_FEE;
	public static int CH_HPREG5_FEE;
	public static int CH_HPREG6_FEE;
	public static int CH_HPREG7_FEE;
	public static int CH_HPREG8_FEE;
	public static int CH_HPREG9_FEE;
	public static int CH_HPREG10_FEE;
	public static int CH_HPREG11_FEE;
	public static int CH_HPREG12_FEE;
	public static int CH_HPREG13_FEE;
	public static long CH_EXPREG_FEE_RATIO;
	public static int CH_EXPREG1_FEE;
	public static int CH_EXPREG2_FEE;
	public static int CH_EXPREG3_FEE;
	public static int CH_EXPREG4_FEE;
	public static int CH_EXPREG5_FEE;
	public static int CH_EXPREG6_FEE;
	public static int CH_EXPREG7_FEE;
	public static long CH_SUPPORT_FEE_RATIO;
	public static int CH_SUPPORT1_FEE;
	public static int CH_SUPPORT2_FEE;
	public static int CH_SUPPORT3_FEE;
	public static int CH_SUPPORT4_FEE;
	public static int CH_SUPPORT5_FEE;
	public static int CH_SUPPORT6_FEE;
	public static int CH_SUPPORT7_FEE;
	public static int CH_SUPPORT8_FEE;
	public static long CH_CURTAIN_FEE_RATIO;
	public static int CH_CURTAIN1_FEE;
	public static int CH_CURTAIN2_FEE;
	public static long CH_FRONT_FEE_RATIO;
	public static int CH_FRONT1_FEE;
	public static int CH_FRONT2_FEE;
	public static boolean CH_BUFF_FREE;
	// --------------------------------------------------
	// Castle Settings
	// --------------------------------------------------
	public static long CS_TELE_FEE_RATIO;
	public static int CS_TELE1_FEE;
	public static int CS_TELE2_FEE;
	public static long CS_MPREG_FEE_RATIO;
	public static int CS_MPREG1_FEE;
	public static int CS_MPREG2_FEE;
	public static long CS_HPREG_FEE_RATIO;
	public static int CS_HPREG1_FEE;
	public static int CS_HPREG2_FEE;
	public static long CS_EXPREG_FEE_RATIO;
	public static int CS_EXPREG1_FEE;
	public static int CS_EXPREG2_FEE;
	public static long CS_SUPPORT_FEE_RATIO;
	public static int CS_SUPPORT1_FEE;
	public static int CS_SUPPORT2_FEE;
	public static List<Integer> SIEGE_HOUR_LIST;
	public static int OUTER_DOOR_UPGRADE_PRICE2;
	public static int OUTER_DOOR_UPGRADE_PRICE3;
	public static int OUTER_DOOR_UPGRADE_PRICE5;
	public static int INNER_DOOR_UPGRADE_PRICE2;
	public static int INNER_DOOR_UPGRADE_PRICE3;
	public static int INNER_DOOR_UPGRADE_PRICE5;
	public static int WALL_UPGRADE_PRICE2;
	public static int WALL_UPGRADE_PRICE3;
	public static int WALL_UPGRADE_PRICE5;
	public static int TRAP_UPGRADE_PRICE1;
	public static int TRAP_UPGRADE_PRICE2;
	public static int TRAP_UPGRADE_PRICE3;
	public static int TRAP_UPGRADE_PRICE4;
	
	// --------------------------------------------------
	// Fortress Settings
	// --------------------------------------------------
	public static long FS_TELE_FEE_RATIO;
	public static int FS_TELE1_FEE;
	public static int FS_TELE2_FEE;
	public static long FS_MPREG_FEE_RATIO;
	public static int FS_MPREG1_FEE;
	public static int FS_MPREG2_FEE;
	public static long FS_HPREG_FEE_RATIO;
	public static int FS_HPREG1_FEE;
	public static int FS_HPREG2_FEE;
	public static long FS_EXPREG_FEE_RATIO;
	public static int FS_EXPREG1_FEE;
	public static int FS_EXPREG2_FEE;
	public static long FS_SUPPORT_FEE_RATIO;
	public static int FS_SUPPORT1_FEE;
	public static int FS_SUPPORT2_FEE;
	public static int FS_BLOOD_OATH_COUNT;
	public static int FS_UPDATE_FRQ;
	public static int FS_MAX_SUPPLY_LEVEL;
	public static int FS_FEE_FOR_CASTLE;
	public static int FS_MAX_OWN_TIME;
	// --------------------------------------------------
	// Feature Settings
	// --------------------------------------------------
	public static int TAKE_FORT_POINTS;
	public static int LOOSE_FORT_POINTS;
	public static int TAKE_CASTLE_POINTS;
	public static int LOOSE_CASTLE_POINTS;
	public static int CASTLE_DEFENDED_POINTS;
	public static int FESTIVAL_WIN_POINTS;
	public static int HERO_POINTS;
	public static int ROYAL_GUARD_COST;
	public static int KNIGHT_UNIT_COST;
	public static int KNIGHT_REINFORCE_COST;
	public static int BALLISTA_POINTS;
	public static int BLOODALLIANCE_POINTS;
	public static int BLOODOATH_POINTS;
	public static int KNIGHTSEPAULETTE_POINTS;
	public static int REPUTATION_SCORE_PER_KILL;
	public static int JOIN_ACADEMY_MIN_REP_SCORE;
	public static int JOIN_ACADEMY_MAX_REP_SCORE;
	public static int RAID_RANKING_1ST;
	public static int RAID_RANKING_2ND;
	public static int RAID_RANKING_3RD;
	public static int RAID_RANKING_4TH;
	public static int RAID_RANKING_5TH;
	public static int RAID_RANKING_6TH;
	public static int RAID_RANKING_7TH;
	public static int RAID_RANKING_8TH;
	public static int RAID_RANKING_9TH;
	public static int RAID_RANKING_10TH;
	public static int RAID_RANKING_UP_TO_50TH;
	public static int RAID_RANKING_UP_TO_100TH;
	public static int CLAN_LEVEL_6_COST;
	public static int CLAN_LEVEL_7_COST;
	public static int CLAN_LEVEL_8_COST;
	public static int CLAN_LEVEL_9_COST;
	public static int CLAN_LEVEL_10_COST;
	public static int CLAN_LEVEL_11_COST;
	public static int CLAN_LEVEL_6_REQUIREMENT;
	public static int CLAN_LEVEL_7_REQUIREMENT;
	public static int CLAN_LEVEL_8_REQUIREMENT;
	public static int CLAN_LEVEL_9_REQUIREMENT;
	public static int CLAN_LEVEL_10_REQUIREMENT;
	public static int CLAN_LEVEL_11_REQUIREMENT;
	public static boolean ALLOW_WYVERN_ALWAYS;
	public static boolean ALLOW_WYVERN_DURING_SIEGE;
	public static boolean ALLOW_MOUNTS_DURING_SIEGE;
	
	// --------------------------------------------------
	// General Settings
	// --------------------------------------------------
	public static boolean EVERYBODY_HAS_ADMIN_RIGHTS;
	public static boolean SERVER_LIST_BRACKET;
	public static int SERVER_LIST_TYPE;
	public static int SERVER_LIST_AGE;
	public static boolean SERVER_GMONLY;
	public static boolean GM_HERO_AURA;
	public static boolean GM_STARTUP_INVULNERABLE;
	public static boolean GM_STARTUP_INVISIBLE;
	public static boolean GM_STARTUP_SILENCE;
	public static boolean GM_STARTUP_AUTO_LIST;
	public static boolean GM_STARTUP_DIET_MODE;
	public static boolean GM_ITEM_RESTRICTION;
	public static boolean GM_SKILL_RESTRICTION;
	public static boolean GM_TRADE_RESTRICTED_ITEMS;
	public static boolean GM_RESTART_FIGHTING;
	public static boolean GM_ANNOUNCER_NAME;
	public static boolean GM_CRITANNOUNCER_NAME;
	public static boolean GM_GIVE_SPECIAL_SKILLS;
	public static boolean GM_GIVE_SPECIAL_AURA_SKILLS;
	public static boolean GAMEGUARD_ENFORCE;
	public static boolean GAMEGUARD_PROHIBITACTION;
	public static boolean LOG_CHAT;
	public static boolean LOG_AUTO_ANNOUNCEMENTS;
	public static boolean LOG_ITEMS;
	public static boolean LOG_ITEMS_SMALL_LOG;
	public static boolean LOG_ITEM_ENCHANTS;
	public static boolean LOG_SKILL_ENCHANTS;
	public static boolean GMAUDIT;
	public static boolean SKILL_CHECK_ENABLE;
	public static boolean SKILL_CHECK_REMOVE;
	public static boolean SKILL_CHECK_GM;
	public static boolean DEBUG;
	public static boolean DEBUG_INSTANCES;
	public static boolean HTML_ACTION_CACHE_DEBUG;
	public static boolean PACKET_HANDLER_DEBUG;
	public static boolean DEVELOPER;
	public static boolean ALT_DEV_NO_HANDLERS;
	public static boolean ALT_DEV_NO_QUESTS;
	public static boolean ALT_DEV_NO_SPAWNS;
	public static boolean ALT_DEV_SHOW_QUESTS_LOAD_IN_LOGS;
	public static boolean ALT_DEV_SHOW_SCRIPTS_LOAD_IN_LOGS;
	public static int THREAD_P_EFFECTS;
	public static int THREAD_P_GENERAL;
	public static int THREAD_E_EVENTS;
	public static int GENERAL_PACKET_THREAD_CORE_SIZE;
	public static int IO_PACKET_THREAD_CORE_SIZE;
	public static int GENERAL_THREAD_CORE_SIZE;
	public static int AI_MAX_THREAD;
	public static int EVENT_MAX_THREAD;
	public static int CLIENT_PACKET_QUEUE_SIZE;
	public static int CLIENT_PACKET_QUEUE_MAX_BURST_SIZE;
	public static int CLIENT_PACKET_QUEUE_MAX_PACKETS_PER_SECOND;
	public static int CLIENT_PACKET_QUEUE_MEASURE_INTERVAL;
	public static int CLIENT_PACKET_QUEUE_MAX_AVERAGE_PACKETS_PER_SECOND;
	public static int CLIENT_PACKET_QUEUE_MAX_FLOODS_PER_MIN;
	public static int CLIENT_PACKET_QUEUE_MAX_OVERFLOWS_PER_MIN;
	public static int CLIENT_PACKET_QUEUE_MAX_UNDERFLOWS_PER_MIN;
	public static int CLIENT_PACKET_QUEUE_MAX_UNKNOWN_PER_MIN;
	public static boolean DEADLOCK_DETECTOR;
	public static int DEADLOCK_CHECK_INTERVAL;
	public static boolean RESTART_ON_DEADLOCK;
	public static boolean ALLOW_DISCARDITEM;
	public static int AUTODESTROY_ITEM_AFTER;
	public static int HERB_AUTO_DESTROY_TIME;
	public static List<Integer> LIST_PROTECTED_ITEMS;
	public static boolean DATABASE_CLEAN_UP;
	public static int CHAR_STORE_INTERVAL;
	public static boolean LAZY_ITEMS_UPDATE;
	public static boolean UPDATE_ITEMS_ON_CHAR_STORE;
	public static boolean DESTROY_DROPPED_PLAYER_ITEM;
	public static boolean DESTROY_EQUIPABLE_PLAYER_ITEM;
	public static boolean SAVE_DROPPED_ITEM;
	public static boolean EMPTY_DROPPED_ITEM_TABLE_AFTER_LOAD;
	public static int SAVE_DROPPED_ITEM_INTERVAL;
	public static boolean CLEAR_DROPPED_ITEM_TABLE;
	public static boolean AUTODELETE_INVALID_QUEST_DATA;
	public static boolean MULTIPLE_ITEM_DROP;
	public static boolean FORCE_INVENTORY_UPDATE;
	public static boolean LAZY_CACHE;
	public static boolean CACHE_CHAR_NAMES;
	public static int MIN_NPC_ANIMATION;
	public static int MAX_NPC_ANIMATION;
	public static int MIN_MONSTER_ANIMATION;
	public static int MAX_MONSTER_ANIMATION;
	public static boolean ENABLE_FALLING_DAMAGE;
	public static boolean GRIDS_ALWAYS_ON;
	public static int GRID_NEIGHBOR_TURNON_TIME;
	public static int GRID_NEIGHBOR_TURNOFF_TIME;
	public static boolean MOVE_BASED_KNOWNLIST;
	public static long KNOWNLIST_UPDATE_INTERVAL;
	public static int PEACE_ZONE_MODE;
	public static String DEFAULT_GLOBAL_CHAT;
	public static String DEFAULT_TRADE_CHAT;
	public static int MINIMUM_CHAT_LEVEL;
	public static boolean ALLOW_WAREHOUSE;
	public static boolean WAREHOUSE_CACHE;
	public static int WAREHOUSE_CACHE_TIME;
	public static boolean ALLOW_REFUND;
	public static boolean ALLOW_MAIL;
	public static boolean ALLOW_ATTACHMENTS;
	public static boolean ALLOW_WEAR;
	public static int WEAR_DELAY;
	public static int WEAR_PRICE;
	public static int INSTANCE_FINISH_TIME;
	public static boolean RESTORE_PLAYER_INSTANCE;
	public static boolean ALLOW_SUMMON_IN_INSTANCE;
	public static int EJECT_DEAD_PLAYER_TIME;
	public static boolean ALLOW_LOTTERY;
	public static boolean ALLOW_RACE;
	public static boolean ALLOW_WATER;
	public static boolean ALLOW_RENTPET;
	public static boolean ALLOWFISHING;
	public static boolean ALLOW_BOAT;
	public static int BOAT_BROADCAST_RADIUS;
	public static boolean ALLOW_CURSED_WEAPONS;
	public static boolean ALLOW_MANOR;
	public static boolean SERVER_NEWS;
	public static boolean ENABLE_COMMUNITY_BOARD;
	public static String BBS_DEFAULT;
	public static boolean USE_SAY_FILTER;
	public static String CHAT_FILTER_CHARS;
	public static Set<ChatType> BAN_CHAT_CHANNELS;
	public static int ALT_OLY_START_TIME;
	public static int ALT_OLY_MIN;
	public static int ALT_OLY_MAX_BUFFS;
	public static long ALT_OLY_CPERIOD;
	public static long ALT_OLY_BATTLE;
	public static long ALT_OLY_WPERIOD;
	public static long ALT_OLY_VPERIOD;
	public static int ALT_OLY_START_POINTS;
	public static int ALT_OLY_WEEKLY_POINTS;
	public static int ALT_OLY_CLASSED;
	public static int ALT_OLY_NONCLASSED;
	public static int ALT_OLY_TEAMS;
	public static int ALT_OLY_REG_DISPLAY;
	public static int[][] ALT_OLY_CLASSED_REWARD;
	public static int[][] ALT_OLY_NONCLASSED_REWARD;
	public static int[][] ALT_OLY_TEAM_REWARD;
	public static int ALT_OLY_COMP_RITEM;
	public static int ALT_OLY_MIN_MATCHES;
	public static int ALT_OLY_GP_PER_POINT;
	public static int ALT_OLY_HERO_POINTS;
	public static int ALT_OLY_RANK1_POINTS;
	public static int ALT_OLY_RANK2_POINTS;
	public static int ALT_OLY_RANK3_POINTS;
	public static int ALT_OLY_RANK4_POINTS;
	public static int ALT_OLY_RANK5_POINTS;
	public static int ALT_OLY_MAX_POINTS;
	public static int ALT_OLY_DIVIDER_CLASSED;
	public static int ALT_OLY_DIVIDER_NON_CLASSED;
	public static int ALT_OLY_MAX_WEEKLY_MATCHES;
	public static int ALT_OLY_MAX_WEEKLY_MATCHES_NON_CLASSED;
	public static int ALT_OLY_MAX_WEEKLY_MATCHES_CLASSED;
	public static int ALT_OLY_MAX_WEEKLY_MATCHES_TEAM;
	public static boolean ALT_OLY_LOG_FIGHTS;
	public static boolean ALT_OLY_SHOW_MONTHLY_WINNERS;
	public static boolean ALT_OLY_ANNOUNCE_GAMES;
	public static List<Integer> LIST_OLY_RESTRICTED_ITEMS;
	public static int ALT_OLY_ENCHANT_LIMIT;
	public static int ALT_OLY_WAIT_TIME;
	public static int ALT_MANOR_REFRESH_TIME;
	public static int ALT_MANOR_REFRESH_MIN;
	public static int ALT_MANOR_APPROVE_TIME;
	public static int ALT_MANOR_APPROVE_MIN;
	public static int ALT_MANOR_MAINTENANCE_MIN;
	public static boolean ALT_MANOR_SAVE_ALL_ACTIONS;
	public static int ALT_MANOR_SAVE_PERIOD_RATE;
	public static long ALT_LOTTERY_PRIZE;
	public static long ALT_LOTTERY_TICKET_PRICE;
	public static float ALT_LOTTERY_5_NUMBER_RATE;
	public static float ALT_LOTTERY_4_NUMBER_RATE;
	public static float ALT_LOTTERY_3_NUMBER_RATE;
	public static long ALT_LOTTERY_2_AND_1_NUMBER_PRIZE;
	public static boolean ALT_FISH_CHAMPIONSHIP_ENABLED;
	public static int ALT_FISH_CHAMPIONSHIP_REWARD_ITEM;
	public static int ALT_FISH_CHAMPIONSHIP_REWARD_1;
	public static int ALT_FISH_CHAMPIONSHIP_REWARD_2;
	public static int ALT_FISH_CHAMPIONSHIP_REWARD_3;
	public static int ALT_FISH_CHAMPIONSHIP_REWARD_4;
	public static int ALT_FISH_CHAMPIONSHIP_REWARD_5;
	public static boolean ALT_ITEM_AUCTION_ENABLED;
	public static int ALT_ITEM_AUCTION_EXPIRED_AFTER;
	public static long ALT_ITEM_AUCTION_TIME_EXTENDS_ON_BID;
	public static int FS_TIME_ATTACK;
	public static int FS_TIME_COOLDOWN;
	public static int FS_TIME_ENTRY;
	public static int FS_TIME_WARMUP;
	public static int FS_PARTY_MEMBER_COUNT;
	public static int RIFT_MIN_PARTY_SIZE;
	public static int RIFT_SPAWN_DELAY;
	public static int RIFT_MAX_JUMPS;
	public static int RIFT_AUTO_JUMPS_TIME_MIN;
	public static int RIFT_AUTO_JUMPS_TIME_MAX;
	public static float RIFT_BOSS_ROOM_TIME_MUTIPLY;
	public static int RIFT_ENTER_COST_RECRUIT;
	public static int RIFT_ENTER_COST_SOLDIER;
	public static int RIFT_ENTER_COST_OFFICER;
	public static int RIFT_ENTER_COST_CAPTAIN;
	public static int RIFT_ENTER_COST_COMMANDER;
	public static int RIFT_ENTER_COST_HERO;
	public static IllegalActionPunishmentType DEFAULT_PUNISH;
	public static int DEFAULT_PUNISH_PARAM;
	public static boolean ONLY_GM_ITEMS_FREE;
	public static boolean JAIL_IS_PVP;
	public static boolean JAIL_DISABLE_CHAT;
	public static boolean JAIL_DISABLE_TRANSACTION;
	public static boolean CUSTOM_SPAWNLIST_TABLE;
	public static boolean SAVE_GMSPAWN_ON_CUSTOM;
	public static boolean CUSTOM_NPC_DATA;
	public static boolean CUSTOM_TELEPORT_TABLE;
	public static boolean CUSTOM_NPCBUFFER_TABLES;
	public static boolean CUSTOM_SKILLS_LOAD;
	public static boolean CUSTOM_ITEMS_LOAD;
	public static boolean CUSTOM_MULTISELL_LOAD;
	public static boolean CUSTOM_BUYLIST_LOAD;
	public static int ALT_BIRTHDAY_GIFT;
	public static String ALT_BIRTHDAY_MAIL_SUBJECT;
	public static String ALT_BIRTHDAY_MAIL_TEXT;
	public static boolean ENABLE_BLOCK_CHECKER_EVENT;
	public static int MIN_BLOCK_CHECKER_TEAM_MEMBERS;
	public static boolean HBCE_FAIR_PLAY;
	public static boolean HELLBOUND_WITHOUT_QUEST;
	public static int PLAYER_MOVEMENT_BLOCK_TIME;
	public static int NORMAL_ENCHANT_COST_MULTIPLIER;
	public static int SAFE_ENCHANT_COST_MULTIPLIER;
	public static boolean BOTREPORT_ENABLE;
	public static String[] BOTREPORT_RESETPOINT_HOUR;
	public static long BOTREPORT_REPORT_DELAY;
	public static boolean BOTREPORT_ALLOW_REPORTS_FROM_SAME_CLAN_MEMBERS;
	
	// --------------------------------------------------
	// FloodProtector Settings
	// --------------------------------------------------
	public static FloodProtectorConfig FLOOD_PROTECTOR_USE_ITEM;
	public static FloodProtectorConfig FLOOD_PROTECTOR_ROLL_DICE;
	public static FloodProtectorConfig FLOOD_PROTECTOR_FIREWORK;
	public static FloodProtectorConfig FLOOD_PROTECTOR_ITEM_PET_SUMMON;
	public static FloodProtectorConfig FLOOD_PROTECTOR_HERO_VOICE;
	public static FloodProtectorConfig FLOOD_PROTECTOR_GLOBAL_CHAT;
	public static FloodProtectorConfig FLOOD_PROTECTOR_SUBCLASS;
	public static FloodProtectorConfig FLOOD_PROTECTOR_DROP_ITEM;
	public static FloodProtectorConfig FLOOD_PROTECTOR_SERVER_BYPASS;
	public static FloodProtectorConfig FLOOD_PROTECTOR_MULTISELL;
	public static FloodProtectorConfig FLOOD_PROTECTOR_TRANSACTION;
	public static FloodProtectorConfig FLOOD_PROTECTOR_MANUFACTURE;
	public static FloodProtectorConfig FLOOD_PROTECTOR_MANOR;
	public static FloodProtectorConfig FLOOD_PROTECTOR_SENDMAIL;
	public static FloodProtectorConfig FLOOD_PROTECTOR_CHARACTER_SELECT;
	public static FloodProtectorConfig FLOOD_PROTECTOR_ITEM_AUCTION;
	
	// --------------------------------------------------
	// Custom Settings
	// --------------------------------------------------
	public static boolean L2JMOD_CHAMPION_ENABLE;
	public static boolean L2JMOD_CHAMPION_PASSIVE;
	public static int L2JMOD_CHAMPION_FREQUENCY;
	public static String L2JMOD_CHAMP_TITLE;
	public static boolean L2JMOD_SHOW_CHAMPION_AURA;
	public static int L2JMOD_CHAMP_MIN_LVL;
	public static int L2JMOD_CHAMP_MAX_LVL;
	public static int L2JMOD_CHAMPION_HP;
	public static float L2JMOD_CHAMPION_REWARDS_EXP_SP;
	public static float L2JMOD_CHAMPION_REWARDS_CHANCE;
	public static float L2JMOD_CHAMPION_REWARDS_AMOUNT;
	public static float L2JMOD_CHAMPION_ADENAS_REWARDS_CHANCE;
	public static float L2JMOD_CHAMPION_ADENAS_REWARDS_AMOUNT;
	public static float L2JMOD_CHAMPION_HP_REGEN;
	public static float L2JMOD_CHAMPION_ATK;
	public static float L2JMOD_CHAMPION_SPD_ATK;
	public static int L2JMOD_CHAMPION_REWARD_LOWER_LVL_ITEM_CHANCE;
	public static int L2JMOD_CHAMPION_REWARD_HIGHER_LVL_ITEM_CHANCE;
	public static int L2JMOD_CHAMPION_REWARD_ID;
	public static int L2JMOD_CHAMPION_REWARD_QTY;
	public static boolean L2JMOD_CHAMPION_ENABLE_VITALITY;
	public static boolean L2JMOD_CHAMPION_ENABLE_IN_INSTANCES;
	public static boolean TVT_EVENT_ENABLED;
	public static boolean TVT_EVENT_IN_INSTANCE;
	public static String TVT_EVENT_INSTANCE_FILE;
	public static String[] TVT_EVENT_INTERVAL;
	public static int TVT_EVENT_PARTICIPATION_TIME;
	public static int TVT_EVENT_RUNNING_TIME;
	public static int TVT_EVENT_PARTICIPATION_NPC_ID;
	public static int[] TVT_EVENT_PARTICIPATION_NPC_COORDINATES = new int[4];
	public static int[] TVT_EVENT_PARTICIPATION_FEE = new int[2];
	public static int TVT_EVENT_MIN_PLAYERS_IN_TEAMS;
	public static int TVT_EVENT_MAX_PLAYERS_IN_TEAMS;
	public static int TVT_EVENT_RESPAWN_TELEPORT_DELAY;
	public static int TVT_EVENT_START_LEAVE_TELEPORT_DELAY;
	public static String TVT_EVENT_TEAM_1_NAME;
	public static int[] TVT_EVENT_TEAM_1_COORDINATES = new int[3];
	public static String TVT_EVENT_TEAM_2_NAME;
	public static int[] TVT_EVENT_TEAM_2_COORDINATES = new int[3];
	public static List<int[]> TVT_EVENT_REWARDS;
	public static boolean TVT_EVENT_TARGET_TEAM_MEMBERS_ALLOWED;
	public static boolean TVT_EVENT_SCROLL_ALLOWED;
	public static boolean TVT_EVENT_POTIONS_ALLOWED;
	public static boolean TVT_EVENT_SUMMON_BY_ITEM_ALLOWED;
	public static List<Integer> TVT_DOORS_IDS_TO_OPEN;
	public static List<Integer> TVT_DOORS_IDS_TO_CLOSE;
	public static boolean TVT_REWARD_TEAM_TIE;
	public static byte TVT_EVENT_MIN_LVL;
	public static byte TVT_EVENT_MAX_LVL;
	public static int TVT_EVENT_EFFECTS_REMOVAL;
	public static Map<Integer, Integer> TVT_EVENT_FIGHTER_BUFFS;
	public static Map<Integer, Integer> TVT_EVENT_MAGE_BUFFS;
	public static int TVT_EVENT_MAX_PARTICIPANTS_PER_IP;
	public static boolean TVT_ALLOW_VOICED_COMMAND;
	public static boolean L2JMOD_ALLOW_WEDDING;
	public static int L2JMOD_WEDDING_PRICE;
	public static boolean L2JMOD_WEDDING_PUNISH_INFIDELITY;
	public static boolean L2JMOD_WEDDING_TELEPORT;
	public static int L2JMOD_WEDDING_TELEPORT_PRICE;
	public static int L2JMOD_WEDDING_TELEPORT_DURATION;
	public static boolean L2JMOD_WEDDING_SAMESEX;
	public static boolean L2JMOD_WEDDING_FORMALWEAR;
	public static int L2JMOD_WEDDING_DIVORCE_COSTS;
	public static boolean L2JMOD_HELLBOUND_STATUS;
	public static boolean BANKING_SYSTEM_ENABLED;
	public static int BANKING_SYSTEM_GOLDBARS;
	public static int BANKING_SYSTEM_ADENA;
	public static boolean L2JMOD_ENABLE_WAREHOUSESORTING_CLAN;
	public static boolean L2JMOD_ENABLE_WAREHOUSESORTING_PRIVATE;
	public static boolean OFFLINE_TRADE_ENABLE;
	public static boolean OFFLINE_CRAFT_ENABLE;
	public static boolean OFFLINE_MODE_IN_PEACE_ZONE;
	public static boolean OFFLINE_MODE_NO_DAMAGE;
	public static boolean RESTORE_OFFLINERS;
	public static int OFFLINE_MAX_DAYS;
	public static boolean OFFLINE_DISCONNECT_FINISHED;
	public static boolean OFFLINE_SET_NAME_COLOR;
	public static int OFFLINE_NAME_COLOR;
	public static boolean OFFLINE_FAME;
	public static boolean STORE_OFFLINE_TRADE_IN_REALTIME;
	public static boolean L2JMOD_DISPLAY_SERVER_TIME;
	public static boolean WELCOME_MESSAGE_ENABLED;
	public static String WELCOME_MESSAGE_TEXT;
	public static int WELCOME_MESSAGE_TIME;
	public static boolean L2JMOD_ANTIFEED_ENABLE;
	public static boolean L2JMOD_ANTIFEED_DUALBOX;
	public static boolean L2JMOD_ANTIFEED_DISCONNECTED_AS_DUALBOX;
	public static int L2JMOD_ANTIFEED_INTERVAL;
	public static boolean ANNOUNCE_PK_PVP;
	public static boolean ANNOUNCE_PK_PVP_NORMAL_MESSAGE;
	public static String ANNOUNCE_PK_MSG;
	public static String ANNOUNCE_PVP_MSG;
	public static boolean L2JMOD_CHAT_ADMIN;
	public static boolean L2JMOD_MULTILANG_ENABLE;
	public static List<String> L2JMOD_MULTILANG_ALLOWED = new ArrayList<>();
	public static String L2JMOD_MULTILANG_DEFAULT;
	public static boolean L2JMOD_MULTILANG_VOICED_ALLOW;
	public static boolean L2JMOD_MULTILANG_SM_ENABLE;
	public static List<String> L2JMOD_MULTILANG_SM_ALLOWED = new ArrayList<>();
	public static boolean L2JMOD_MULTILANG_NS_ENABLE;
	public static List<String> L2JMOD_MULTILANG_NS_ALLOWED = new ArrayList<>();
	public static boolean L2WALKER_PROTECTION;
	public static boolean L2JMOD_DEBUG_VOICE_COMMAND;
	public static int L2JMOD_DUALBOX_CHECK_MAX_PLAYERS_PER_IP;
	public static int L2JMOD_DUALBOX_CHECK_MAX_OLYMPIAD_PARTICIPANTS_PER_IP;
	public static int L2JMOD_DUALBOX_CHECK_MAX_L2EVENT_PARTICIPANTS_PER_IP;
	public static Map<Integer, Integer> L2JMOD_DUALBOX_CHECK_WHITELIST;
	public static boolean L2JMOD_ALLOW_CHANGE_PASSWORD;
	public static boolean L2JMOD_OLD_DROP_BEHAVIOR;
	public static boolean CUSTOM_STARTING_LOC;
	public static int CUSTOM_STARTING_LOC_X;
	public static int CUSTOM_STARTING_LOC_Y;
	public static int CUSTOM_STARTING_LOC_Z;
	// Shop Distance
	public static int SHOP_MIN_RANGE_FROM_NPC;
	public static int SHOP_MIN_RANGE_FROM_PLAYER;
	public static boolean ENABLE_RANDOM_MONSTER_SPAWNS;
	public static int MOB_MIN_SPAWN_RANGE;
	public static int MOB_MAX_SPAWN_RANGE;
	public static List<Integer> MOBS_LIST_NOT_RANDOM;
	// Custom CB
	public static boolean CUSTOM_CB_ENABLED;
	public static int COMMUNITYBOARD_CURRENCY;
	public static boolean COMMUNITYBOARD_ENABLE_MULTISELLS;
	public static boolean COMMUNITYBOARD_ENABLE_TELEPORTS;
	public static boolean COMMUNITYBOARD_ENABLE_BUFFS;
	public static int COMMUNITYBOARD_TELEPORT_PRICE;
	public static int COMMUNITYBOARD_BUFF_PRICE;
	public static boolean COMMUNITYBOARD_COMBAT_DISABLED;
	public static boolean COMMUNITYBOARD_KARMA_DISABLED;
	public static boolean PREMIUM_SYSTEM_ENABLED;
	public static float PREMIUM_RATE_XP;
	public static float PREMIUM_RATE_SP;
	public static Map<Integer, Float> PREMIUM_RATE_DROP_ITEMS_ID;
	public static float PREMIUM_RATE_DROP_CHANCE;
	public static float PREMIUM_RATE_DROP_AMOUNT;
	public static float PREMIUM_RATE_SPOIL_CHANCE;
	public static float PREMIUM_RATE_SPOIL_AMOUNT;
	public static Map<Integer, Float> PREMIUM_RATE_DROP_CHANCE_BY_ID;
	public static Map<Integer, Float> PREMIUM_RATE_DROP_AMOUNT_BY_ID;
	// --------------------------------------------------
	// NPC Settings
	// --------------------------------------------------
	public static boolean ANNOUNCE_MAMMON_SPAWN;
	public static boolean ALT_MOB_AGRO_IN_PEACEZONE;
	public static boolean ALT_ATTACKABLE_NPCS;
	public static boolean ALT_GAME_VIEWNPC;
	public static int MAX_AGGRO_RANGE;
	public static int MAX_DRIFT_RANGE;
	public static boolean SHOW_NPC_LVL;
	public static boolean SHOW_CREST_WITHOUT_QUEST;
	public static boolean ENABLE_RANDOM_ENCHANT_EFFECT;
	public static int MIN_NPC_LVL_DMG_PENALTY;
	public static Map<Integer, Float> NPC_DMG_PENALTY;
	public static Map<Integer, Float> NPC_CRIT_DMG_PENALTY;
	public static Map<Integer, Float> NPC_SKILL_DMG_PENALTY;
	public static int MIN_NPC_LVL_MAGIC_PENALTY;
	public static Map<Integer, Float> NPC_SKILL_CHANCE_PENALTY;
	public static int DECAY_TIME_TASK;
	public static int DEFAULT_CORPSE_TIME;
	public static int SPOILED_CORPSE_EXTEND_TIME;
	public static int CORPSE_CONSUME_SKILL_ALLOWED_TIME_BEFORE_DECAY;
	public static boolean GUARD_ATTACK_AGGRO_MOB;
	public static boolean ENABLE_GUARD_RETURN;
	public static boolean ALLOW_WYVERN_UPGRADER;
	public static List<Integer> LIST_PET_RENT_NPC;
	public static double RAID_HP_REGEN_MULTIPLIER;
	public static double RAID_MP_REGEN_MULTIPLIER;
	public static double RAID_PDEFENCE_MULTIPLIER;
	public static double RAID_MDEFENCE_MULTIPLIER;
	public static double RAID_PATTACK_MULTIPLIER;
	public static double RAID_MATTACK_MULTIPLIER;
	public static double RAID_MINION_RESPAWN_TIMER;
	public static Map<Integer, Integer> MINIONS_RESPAWN_TIME;
	public static float RAID_MIN_RESPAWN_MULTIPLIER;
	public static float RAID_MAX_RESPAWN_MULTIPLIER;
	public static boolean RAID_DISABLE_CURSE;
	public static int RAID_CHAOS_TIME;
	public static int GRAND_CHAOS_TIME;
	public static int MINION_CHAOS_TIME;
	public static int INVENTORY_MAXIMUM_PET;
	public static double PET_HP_REGEN_MULTIPLIER;
	public static double PET_MP_REGEN_MULTIPLIER;
	public static int DROP_ADENA_MIN_LEVEL_DIFFERENCE;
	public static int DROP_ADENA_MAX_LEVEL_DIFFERENCE;
	public static double DROP_ADENA_MIN_LEVEL_GAP_CHANCE;
	public static int DROP_ITEM_MIN_LEVEL_DIFFERENCE;
	public static int DROP_ITEM_MAX_LEVEL_DIFFERENCE;
	public static double DROP_ITEM_MIN_LEVEL_GAP_CHANCE;
	
	// --------------------------------------------------
	// PvP Settings
	// --------------------------------------------------
	public static boolean KARMA_DROP_GM;
	public static boolean KARMA_AWARD_PK_KILL;
	public static int KARMA_PK_LIMIT;
	public static String KARMA_NONDROPPABLE_PET_ITEMS;
	public static String KARMA_NONDROPPABLE_ITEMS;
	public static int[] KARMA_LIST_NONDROPPABLE_PET_ITEMS;
	public static int[] KARMA_LIST_NONDROPPABLE_ITEMS;
	
	// --------------------------------------------------
	// Rate Settings
	// --------------------------------------------------
	public static float RATE_XP;
	public static float RATE_SP;
	public static float RATE_PARTY_XP;
	public static float RATE_PARTY_SP;
	public static float RATE_HB_TRUST_INCREASE;
	public static float RATE_HB_TRUST_DECREASE;
	public static float RATE_EXTRACTABLE;
	public static int RATE_DROP_MANOR;
	public static float RATE_QUEST_DROP;
	public static float RATE_QUEST_REWARD;
	public static float RATE_QUEST_REWARD_XP;
	public static float RATE_QUEST_REWARD_SP;
	public static float RATE_QUEST_REWARD_ADENA;
	public static boolean RATE_QUEST_REWARD_USE_MULTIPLIERS;
	public static float RATE_QUEST_REWARD_POTION;
	public static float RATE_QUEST_REWARD_SCROLL;
	public static float RATE_QUEST_REWARD_RECIPE;
	public static float RATE_QUEST_REWARD_MATERIAL;
	public static float RATE_DEATH_DROP_AMOUNT_MULTIPLIER;
	public static float RATE_CORPSE_DROP_AMOUNT_MULTIPLIER;
	public static float RATE_HERB_DROP_AMOUNT_MULTIPLIER;
	public static float RATE_RAID_DROP_AMOUNT_MULTIPLIER;
	public static float RATE_DEATH_DROP_CHANCE_MULTIPLIER;
	public static float RATE_CORPSE_DROP_CHANCE_MULTIPLIER;
	public static float RATE_HERB_DROP_CHANCE_MULTIPLIER;
	public static float RATE_RAID_DROP_CHANCE_MULTIPLIER;
	public static Map<Integer, Float> RATE_DROP_AMOUNT_BY_ID;
	public static Map<Integer, Float> RATE_DROP_CHANCE_BY_ID;
	public static float RATE_KARMA_LOST;
	public static float RATE_KARMA_EXP_LOST;
	public static float RATE_SIEGE_GUARDS_PRICE;
	public static float RATE_DROP_COMMON_HERBS;
	public static float RATE_DROP_HP_HERBS;
	public static float RATE_DROP_MP_HERBS;
	public static float RATE_DROP_SPECIAL_HERBS;
	public static int PLAYER_DROP_LIMIT;
	public static int PLAYER_RATE_DROP;
	public static int PLAYER_RATE_DROP_ITEM;
	public static int PLAYER_RATE_DROP_EQUIP;
	public static int PLAYER_RATE_DROP_EQUIP_WEAPON;
	public static float PET_XP_RATE;
	public static int PET_FOOD_RATE;
	public static float SINEATER_XP_RATE;
	public static int KARMA_DROP_LIMIT;
	public static int KARMA_RATE_DROP;
	public static int KARMA_RATE_DROP_ITEM;
	public static int KARMA_RATE_DROP_EQUIP;
	public static int KARMA_RATE_DROP_EQUIP_WEAPON;
	
	// --------------------------------------------------
	// Seven Signs Settings
	// --------------------------------------------------
	public static boolean ALT_GAME_CASTLE_DAWN;
	public static boolean ALT_GAME_CASTLE_DUSK;
	public static boolean ALT_GAME_REQUIRE_CLAN_CASTLE;
	public static int ALT_FESTIVAL_MIN_PLAYER;
	public static int ALT_MAXIMUM_PLAYER_CONTRIB;
	public static long ALT_FESTIVAL_MANAGER_START;
	public static long ALT_FESTIVAL_LENGTH;
	public static long ALT_FESTIVAL_CYCLE_LENGTH;
	public static long ALT_FESTIVAL_FIRST_SPAWN;
	public static long ALT_FESTIVAL_FIRST_SWARM;
	public static long ALT_FESTIVAL_SECOND_SPAWN;
	public static long ALT_FESTIVAL_SECOND_SWARM;
	public static long ALT_FESTIVAL_CHEST_SPAWN;
	public static double ALT_SIEGE_DAWN_GATES_PDEF_MULT;
	public static double ALT_SIEGE_DUSK_GATES_PDEF_MULT;
	public static double ALT_SIEGE_DAWN_GATES_MDEF_MULT;
	public static double ALT_SIEGE_DUSK_GATES_MDEF_MULT;
	public static boolean ALT_STRICT_SEVENSIGNS;
	public static boolean ALT_SEVENSIGNS_LAZY_UPDATE;
	public static int SSQ_DAWN_TICKET_QUANTITY;
	public static int SSQ_DAWN_TICKET_PRICE;
	public static int SSQ_DAWN_TICKET_BUNDLE;
	public static int SSQ_MANORS_AGREEMENT_ID;
	public static int SSQ_JOIN_DAWN_ADENA_FEE;
	
	// --------------------------------------------------
	// Server Settings
	// --------------------------------------------------
	public static int PORT_GAME;
	public static int PORT_LOGIN;
	public static String LOGIN_BIND_ADDRESS;
	public static int LOGIN_TRY_BEFORE_BAN;
	public static int LOGIN_BLOCK_AFTER_BAN;
	public static String GAMESERVER_HOSTNAME;
	public static String DATABASE_DRIVER;
	public static String DATABASE_URL;
	public static String DATABASE_LOGIN;
	public static String DATABASE_PASSWORD;
	public static int DATABASE_MAX_CONNECTIONS;
	public static int DATABASE_MAX_IDLE_TIME;
	public static int MAXIMUM_ONLINE_USERS;
	public static String CNAME_TEMPLATE;
	public static String PET_NAME_TEMPLATE;
	public static String CLAN_NAME_TEMPLATE;
	public static int MAX_CHARACTERS_NUMBER_PER_ACCOUNT;
	public static File DATAPACK_ROOT;
	public static boolean ACCEPT_ALTERNATE_ID;
	public static int REQUEST_ID;
	public static boolean RESERVE_HOST_ON_LOGIN = false;
	public static List<Integer> PROTOCOL_LIST;
	public static boolean LOGIN_SERVER_SCHEDULE_RESTART;
	public static long LOGIN_SERVER_SCHEDULE_RESTART_TIME;
	public static boolean SERVER_RESTART_SCHEDULE_ENABLED;
	public static boolean SERVER_RESTART_SCHEDULE_MESSAGE;
	public static int SERVER_RESTART_SCHEDULE_COUNTDOWN;
	public static String[] SERVER_RESTART_SCHEDULE;
    public static String SERVER_VERSION;
    public static String BUILD_DATE;
	
	// --------------------------------------------------
	// MMO Settings
	// --------------------------------------------------
	public static int MMO_SELECTOR_SLEEP_TIME;
	public static int MMO_MAX_SEND_PER_PASS;
	public static int MMO_MAX_READ_PER_PASS;
	public static int MMO_HELPER_BUFFER_COUNT;
	public static boolean MMO_TCP_NODELAY;
	
	// --------------------------------------------------
	// Vitality Settings
	// --------------------------------------------------
	public static boolean ENABLE_VITALITY;
	public static boolean RECOVER_VITALITY_ON_RECONNECT;
	public static boolean ENABLE_DROP_VITALITY_HERBS;
	public static float RATE_VITALITY_LEVEL_1;
	public static float RATE_VITALITY_LEVEL_2;
	public static float RATE_VITALITY_LEVEL_3;
	public static float RATE_VITALITY_LEVEL_4;
	public static float RATE_DROP_VITALITY_HERBS;
	public static float RATE_RECOVERY_VITALITY_PEACE_ZONE;
	public static float RATE_VITALITY_LOST;
	public static float RATE_VITALITY_GAIN;
	public static float RATE_RECOVERY_ON_RECONNECT;
	public static int STARTING_VITALITY_POINTS;
	
	// --------------------------------------------------
	// No classification assigned to the following yet
	// --------------------------------------------------
	public static int MAX_ITEM_IN_PACKET;
	public static boolean CHECK_KNOWN;
	public static int GAME_SERVER_LOGIN_PORT;
	public static String GAME_SERVER_LOGIN_HOST;
	public static List<String> GAME_SERVER_SUBNETS;
	public static List<String> GAME_SERVER_HOSTS;
	public static int PVP_NORMAL_TIME;
	public static int PVP_PVP_TIME;
	
	public static enum IdFactoryType
	{
		Compaction,
		BitSet,
		Stack
	}
	
	public static IdFactoryType IDFACTORY_TYPE;
	public static boolean BAD_ID_CHECKING;
	
	public static double ENCHANT_CHANCE_ELEMENT_STONE;
	public static double ENCHANT_CHANCE_ELEMENT_CRYSTAL;
	public static double ENCHANT_CHANCE_ELEMENT_JEWEL;
	public static double ENCHANT_CHANCE_ELEMENT_ENERGY;
	public static int[] ENCHANT_BLACKLIST;
	public static int AUGMENTATION_NG_SKILL_CHANCE;
	public static int AUGMENTATION_NG_GLOW_CHANCE;
	public static int AUGMENTATION_MID_SKILL_CHANCE;
	public static int AUGMENTATION_MID_GLOW_CHANCE;
	public static int AUGMENTATION_HIGH_SKILL_CHANCE;
	public static int AUGMENTATION_HIGH_GLOW_CHANCE;
	public static int AUGMENTATION_TOP_SKILL_CHANCE;
	public static int AUGMENTATION_TOP_GLOW_CHANCE;
	public static int AUGMENTATION_BASESTAT_CHANCE;
	public static int AUGMENTATION_ACC_SKILL_CHANCE;
	public static boolean RETAIL_LIKE_AUGMENTATION;
	public static int[] RETAIL_LIKE_AUGMENTATION_NG_CHANCE;
	public static int[] RETAIL_LIKE_AUGMENTATION_MID_CHANCE;
	public static int[] RETAIL_LIKE_AUGMENTATION_HIGH_CHANCE;
	public static int[] RETAIL_LIKE_AUGMENTATION_TOP_CHANCE;
	public static boolean RETAIL_LIKE_AUGMENTATION_ACCESSORY;
	public static int[] AUGMENTATION_BLACKLIST;
	public static boolean ALT_ALLOW_AUGMENT_PVP_ITEMS;
	public static double HP_REGEN_MULTIPLIER;
	public static double MP_REGEN_MULTIPLIER;
	public static double CP_REGEN_MULTIPLIER;
	public static boolean IS_TELNET_ENABLED;
	public static boolean SHOW_LICENCE;
	public static boolean ACCEPT_NEW_GAMESERVER;
	public static int SERVER_ID;
	public static byte[] HEX_ID;
	public static boolean AUTO_CREATE_ACCOUNTS;
	public static boolean FLOOD_PROTECTION;
	public static int FAST_CONNECTION_LIMIT;
	public static int NORMAL_CONNECTION_TIME;
	public static int FAST_CONNECTION_TIME;
	public static int MAX_CONNECTION_PER_IP;
	
	// GrandBoss Settings
	
	// Antharas
	public static int ANTHARAS_WAIT_TIME;
	public static int ANTHARAS_SPAWN_INTERVAL;
	public static int ANTHARAS_SPAWN_RANDOM;
	
	// Valakas
	public static int VALAKAS_WAIT_TIME;
	public static int VALAKAS_SPAWN_INTERVAL;
	public static int VALAKAS_SPAWN_RANDOM;
	
	// Baium
	public static int BAIUM_SPAWN_INTERVAL;
	public static int BAIUM_SPAWN_RANDOM;
	
	// Core
	public static int CORE_SPAWN_INTERVAL;
	public static int CORE_SPAWN_RANDOM;
	
	// Offen
	public static int ORFEN_SPAWN_INTERVAL;
	public static int ORFEN_SPAWN_RANDOM;
	
	// Queen Ant
	public static int QUEEN_ANT_SPAWN_INTERVAL;
	public static int QUEEN_ANT_SPAWN_RANDOM;
	
	// Beleth
	public static int BELETH_MIN_PLAYERS;
	public static int BELETH_SPAWN_INTERVAL;
	public static int BELETH_SPAWN_RANDOM;
	
	// Gracia Seeds Settings
	public static int SOD_TIAT_KILL_COUNT;
	public static long SOD_STAGE_2_LENGTH;
	
	// Tiat
	public static int MIN_TIAT_PLAYERS;
	public static int MAX_TIAT_PLAYERS;
	public static int MIN_TIAT_LEVEL;
	
	// Ekimus
	public static int SOI_EKIMUS_KILL_COUNT;
	public static int EROSION_ATTACK_MIN_PLAYERS;
	public static int EROSION_ATTACK_MAX_PLAYERS;
	public static int EROSION_DEFENCE_MIN_PLAYERS;
	public static int EROSION_DEFENCE_MAX_PLAYERS;
	public static int HEART_ATTACK_MIN_PLAYERS;
	public static int HEART_ATTACK_MAX_PLAYERS;
	public static int HEART_DEFENCE_MIN_PLAYERS;
	public static int HEART_DEFENCE_MAX_PLAYERS;
	
	// chatfilter
	public static List<String> FILTER_LIST;
	
	// Conquerable Halls Settings
	public static int CHS_CLAN_MINLEVEL;
	public static int CHS_MAX_ATTACKERS;
	public static int CHS_MAX_FLAGS_PER_CLAN;
	public static boolean CHS_ENABLE_FAME;
	public static int CHS_FAME_AMOUNT;
	public static int CHS_FAME_FREQUENCY;
	
	// GeoData Settings
	public static int PATHFINDING;
	public static Path PATHNODE_PATH;
	public static String PATHFIND_BUFFERS;
	public static float LOW_WEIGHT;
	public static float MEDIUM_WEIGHT;
	public static float HIGH_WEIGHT;
	public static boolean ADVANCED_DIAGONAL_STRATEGY;
	public static float DIAGONAL_WEIGHT;
	public static int MAX_POSTFILTER_PASSES;
	public static boolean DEBUG_PATH;
	public static boolean FORCE_GEODATA;
	public static int COORD_SYNCHRONIZE;
	public static Path GEODATA_PATH;
	public static boolean TRY_LOAD_UNSPECIFIED_REGIONS;
	public static Map<String, Boolean> GEODATA_REGIONS;
	
	/**
	 * This class initializes all global variables for configuration.<br>
	 * If the key doesn't appear in properties file, a default value is set by this class. {@link #CONFIGURATION_FILE} (properties file) for configuring your server.
	 */
	public static void load()
	{
		if (Server.serverMode == Server.MODE_GAMESERVER)
		{
			FLOOD_PROTECTOR_USE_ITEM = new FloodProtectorConfig("UseItemFloodProtector");
			FLOOD_PROTECTOR_ROLL_DICE = new FloodProtectorConfig("RollDiceFloodProtector");
			FLOOD_PROTECTOR_FIREWORK = new FloodProtectorConfig("FireworkFloodProtector");
			FLOOD_PROTECTOR_ITEM_PET_SUMMON = new FloodProtectorConfig("ItemPetSummonFloodProtector");
			FLOOD_PROTECTOR_HERO_VOICE = new FloodProtectorConfig("HeroVoiceFloodProtector");
			FLOOD_PROTECTOR_GLOBAL_CHAT = new FloodProtectorConfig("GlobalChatFloodProtector");
			FLOOD_PROTECTOR_SUBCLASS = new FloodProtectorConfig("SubclassFloodProtector");
			FLOOD_PROTECTOR_DROP_ITEM = new FloodProtectorConfig("DropItemFloodProtector");
			FLOOD_PROTECTOR_SERVER_BYPASS = new FloodProtectorConfig("ServerBypassFloodProtector");
			FLOOD_PROTECTOR_MULTISELL = new FloodProtectorConfig("MultiSellFloodProtector");
			FLOOD_PROTECTOR_TRANSACTION = new FloodProtectorConfig("TransactionFloodProtector");
			FLOOD_PROTECTOR_MANUFACTURE = new FloodProtectorConfig("ManufactureFloodProtector");
			FLOOD_PROTECTOR_MANOR = new FloodProtectorConfig("ManorFloodProtector");
			FLOOD_PROTECTOR_SENDMAIL = new FloodProtectorConfig("SendMailFloodProtector");
			FLOOD_PROTECTOR_CHARACTER_SELECT = new FloodProtectorConfig("CharacterSelectFloodProtector");
			FLOOD_PROTECTOR_ITEM_AUCTION = new FloodProtectorConfig("ItemAuctionFloodProtector");
			
			final PropertiesParser serverSettings = new PropertiesParser(CONFIGURATION_FILE);
			
			GAMESERVER_HOSTNAME = serverSettings.getString("GameserverHostname", "*");
			PORT_GAME = serverSettings.getInt("GameserverPort", 7777);
			
			GAME_SERVER_LOGIN_PORT = serverSettings.getInt("LoginPort", 9014);
			GAME_SERVER_LOGIN_HOST = serverSettings.getString("LoginHost", "127.0.0.1");
			
			REQUEST_ID = serverSettings.getInt("RequestServerID", 0);
			ACCEPT_ALTERNATE_ID = serverSettings.getBoolean("AcceptAlternateID", true);
			
			DATABASE_DRIVER = serverSettings.getString("Driver", "com.mysql.jdbc.Driver");
			DATABASE_URL = serverSettings.getString("URL", "jdbc:mysql://localhost/la2eden?useUnicode=true&characterEncoding=utf-8");
			DATABASE_LOGIN = serverSettings.getString("Login", "root");
			DATABASE_PASSWORD = serverSettings.getString("Password", "");
			DATABASE_MAX_CONNECTIONS = serverSettings.getInt("MaximumDbConnections", 500);
			DATABASE_MAX_IDLE_TIME = serverSettings.getInt("MaximumDbIdleTime", 0);
			
			try
			{
				DATAPACK_ROOT = new File(serverSettings.getString("DatapackRoot", ".").replaceAll("\\\\", "/")).getCanonicalFile();
			}
			catch (IOException e)
			{
				_log.log(Level.WARNING, "Error setting datapack root!", e);
				DATAPACK_ROOT = new File(".");
			}
			
			CNAME_TEMPLATE = serverSettings.getString("CnameTemplate", ".*");
			PET_NAME_TEMPLATE = serverSettings.getString("PetNameTemplate", ".*");
			CLAN_NAME_TEMPLATE = serverSettings.getString("ClanNameTemplate", ".*");
			
			MAX_CHARACTERS_NUMBER_PER_ACCOUNT = serverSettings.getInt("CharMaxNumber", 7);

			switch (PROJECT_TYPE) {
                case FREE:
                    MAXIMUM_ONLINE_USERS = MAX_FREE_USERS;
                    break;
                case VIP:
                    MAXIMUM_ONLINE_USERS = MAX_VIP_USERS;
                    break;
            }
			
			final String[] protocols = serverSettings.getString("AllowedProtocolRevisions", "267;268;271;273").split(";");
			PROTOCOL_LIST = new ArrayList<>(protocols.length);
			for (String protocol : protocols)
			{
				try
				{
					PROTOCOL_LIST.add(Integer.parseInt(protocol.trim()));
				}
				catch (NumberFormatException e)
				{
					_log.log(Level.WARNING, "Wrong config protocol version: " + protocol + ". Skipped.");
				}
			}
			
			SERVER_RESTART_SCHEDULE_ENABLED = serverSettings.getBoolean("ServerRestartScheduleEnabled", false);
			SERVER_RESTART_SCHEDULE_MESSAGE = serverSettings.getBoolean("ServerRestartScheduleMessage", false);
			SERVER_RESTART_SCHEDULE_COUNTDOWN = serverSettings.getInt("ServerRestartScheduleCountdown", 600);
			SERVER_RESTART_SCHEDULE = serverSettings.getString("ServerRestartSchedule", "08:00").split(",");
			
			// Hosts and Subnets
			final IPConfigData ipcd = new IPConfigData();
			GAME_SERVER_SUBNETS = ipcd.getSubnets();
			GAME_SERVER_HOSTS = ipcd.getHosts();
			
			// Load Feature L2Properties file (if exists)
			final PropertiesParser Feature = new PropertiesParser(FEATURE_CONFIG_FILE);
			
			CH_TELE_FEE_RATIO = Feature.getLong("ClanHallTeleportFunctionFeeRatio", 604800000);
			CH_TELE1_FEE = Feature.getInt("ClanHallTeleportFunctionFeeLvl1", 7000);
			CH_TELE2_FEE = Feature.getInt("ClanHallTeleportFunctionFeeLvl2", 14000);
			CH_SUPPORT_FEE_RATIO = Feature.getLong("ClanHallSupportFunctionFeeRatio", 86400000);
			CH_SUPPORT1_FEE = Feature.getInt("ClanHallSupportFeeLvl1", 2500);
			CH_SUPPORT2_FEE = Feature.getInt("ClanHallSupportFeeLvl2", 5000);
			CH_SUPPORT3_FEE = Feature.getInt("ClanHallSupportFeeLvl3", 7000);
			CH_SUPPORT4_FEE = Feature.getInt("ClanHallSupportFeeLvl4", 11000);
			CH_SUPPORT5_FEE = Feature.getInt("ClanHallSupportFeeLvl5", 21000);
			CH_SUPPORT6_FEE = Feature.getInt("ClanHallSupportFeeLvl6", 36000);
			CH_SUPPORT7_FEE = Feature.getInt("ClanHallSupportFeeLvl7", 37000);
			CH_SUPPORT8_FEE = Feature.getInt("ClanHallSupportFeeLvl8", 52000);
			CH_MPREG_FEE_RATIO = Feature.getLong("ClanHallMpRegenerationFunctionFeeRatio", 86400000);
			CH_MPREG1_FEE = Feature.getInt("ClanHallMpRegenerationFeeLvl1", 2000);
			CH_MPREG2_FEE = Feature.getInt("ClanHallMpRegenerationFeeLvl2", 3750);
			CH_MPREG3_FEE = Feature.getInt("ClanHallMpRegenerationFeeLvl3", 6500);
			CH_MPREG4_FEE = Feature.getInt("ClanHallMpRegenerationFeeLvl4", 13750);
			CH_MPREG5_FEE = Feature.getInt("ClanHallMpRegenerationFeeLvl5", 20000);
			CH_HPREG_FEE_RATIO = Feature.getLong("ClanHallHpRegenerationFunctionFeeRatio", 86400000);
			CH_HPREG1_FEE = Feature.getInt("ClanHallHpRegenerationFeeLvl1", 700);
			CH_HPREG2_FEE = Feature.getInt("ClanHallHpRegenerationFeeLvl2", 800);
			CH_HPREG3_FEE = Feature.getInt("ClanHallHpRegenerationFeeLvl3", 1000);
			CH_HPREG4_FEE = Feature.getInt("ClanHallHpRegenerationFeeLvl4", 1166);
			CH_HPREG5_FEE = Feature.getInt("ClanHallHpRegenerationFeeLvl5", 1500);
			CH_HPREG6_FEE = Feature.getInt("ClanHallHpRegenerationFeeLvl6", 1750);
			CH_HPREG7_FEE = Feature.getInt("ClanHallHpRegenerationFeeLvl7", 2000);
			CH_HPREG8_FEE = Feature.getInt("ClanHallHpRegenerationFeeLvl8", 2250);
			CH_HPREG9_FEE = Feature.getInt("ClanHallHpRegenerationFeeLvl9", 2500);
			CH_HPREG10_FEE = Feature.getInt("ClanHallHpRegenerationFeeLvl10", 3250);
			CH_HPREG11_FEE = Feature.getInt("ClanHallHpRegenerationFeeLvl11", 3270);
			CH_HPREG12_FEE = Feature.getInt("ClanHallHpRegenerationFeeLvl12", 4250);
			CH_HPREG13_FEE = Feature.getInt("ClanHallHpRegenerationFeeLvl13", 5166);
			CH_EXPREG_FEE_RATIO = Feature.getLong("ClanHallExpRegenerationFunctionFeeRatio", 86400000);
			CH_EXPREG1_FEE = Feature.getInt("ClanHallExpRegenerationFeeLvl1", 3000);
			CH_EXPREG2_FEE = Feature.getInt("ClanHallExpRegenerationFeeLvl2", 6000);
			CH_EXPREG3_FEE = Feature.getInt("ClanHallExpRegenerationFeeLvl3", 9000);
			CH_EXPREG4_FEE = Feature.getInt("ClanHallExpRegenerationFeeLvl4", 15000);
			CH_EXPREG5_FEE = Feature.getInt("ClanHallExpRegenerationFeeLvl5", 21000);
			CH_EXPREG6_FEE = Feature.getInt("ClanHallExpRegenerationFeeLvl6", 23330);
			CH_EXPREG7_FEE = Feature.getInt("ClanHallExpRegenerationFeeLvl7", 30000);
			CH_ITEM_FEE_RATIO = Feature.getLong("ClanHallItemCreationFunctionFeeRatio", 86400000);
			CH_ITEM1_FEE = Feature.getInt("ClanHallItemCreationFunctionFeeLvl1", 30000);
			CH_ITEM2_FEE = Feature.getInt("ClanHallItemCreationFunctionFeeLvl2", 70000);
			CH_ITEM3_FEE = Feature.getInt("ClanHallItemCreationFunctionFeeLvl3", 140000);
			CH_CURTAIN_FEE_RATIO = Feature.getLong("ClanHallCurtainFunctionFeeRatio", 604800000);
			CH_CURTAIN1_FEE = Feature.getInt("ClanHallCurtainFunctionFeeLvl1", 2000);
			CH_CURTAIN2_FEE = Feature.getInt("ClanHallCurtainFunctionFeeLvl2", 2500);
			CH_FRONT_FEE_RATIO = Feature.getLong("ClanHallFrontPlatformFunctionFeeRatio", 259200000);
			CH_FRONT1_FEE = Feature.getInt("ClanHallFrontPlatformFunctionFeeLvl1", 1300);
			CH_FRONT2_FEE = Feature.getInt("ClanHallFrontPlatformFunctionFeeLvl2", 4000);
			CH_BUFF_FREE = Feature.getBoolean("AltClanHallMpBuffFree", false);
			SIEGE_HOUR_LIST = new ArrayList<>();
			for (String hour : Feature.getString("SiegeHourList", "").split(","))
			{
				if (Util.isDigit(hour))
				{
					SIEGE_HOUR_LIST.add(Integer.parseInt(hour));
				}
			}
			CS_TELE_FEE_RATIO = Feature.getLong("CastleTeleportFunctionFeeRatio", 604800000);
			CS_TELE1_FEE = Feature.getInt("CastleTeleportFunctionFeeLvl1", 1000);
			CS_TELE2_FEE = Feature.getInt("CastleTeleportFunctionFeeLvl2", 10000);
			CS_SUPPORT_FEE_RATIO = Feature.getLong("CastleSupportFunctionFeeRatio", 604800000);
			CS_SUPPORT1_FEE = Feature.getInt("CastleSupportFeeLvl1", 49000);
			CS_SUPPORT2_FEE = Feature.getInt("CastleSupportFeeLvl2", 120000);
			CS_MPREG_FEE_RATIO = Feature.getLong("CastleMpRegenerationFunctionFeeRatio", 604800000);
			CS_MPREG1_FEE = Feature.getInt("CastleMpRegenerationFeeLvl1", 45000);
			CS_MPREG2_FEE = Feature.getInt("CastleMpRegenerationFeeLvl2", 65000);
			CS_HPREG_FEE_RATIO = Feature.getLong("CastleHpRegenerationFunctionFeeRatio", 604800000);
			CS_HPREG1_FEE = Feature.getInt("CastleHpRegenerationFeeLvl1", 12000);
			CS_HPREG2_FEE = Feature.getInt("CastleHpRegenerationFeeLvl2", 20000);
			CS_EXPREG_FEE_RATIO = Feature.getLong("CastleExpRegenerationFunctionFeeRatio", 604800000);
			CS_EXPREG1_FEE = Feature.getInt("CastleExpRegenerationFeeLvl1", 63000);
			CS_EXPREG2_FEE = Feature.getInt("CastleExpRegenerationFeeLvl2", 70000);
			
			OUTER_DOOR_UPGRADE_PRICE2 = Feature.getInt("OuterDoorUpgradePriceLvl2", 3000000);
			OUTER_DOOR_UPGRADE_PRICE3 = Feature.getInt("OuterDoorUpgradePriceLvl3", 4000000);
			OUTER_DOOR_UPGRADE_PRICE5 = Feature.getInt("OuterDoorUpgradePriceLvl5", 5000000);
			INNER_DOOR_UPGRADE_PRICE2 = Feature.getInt("InnerDoorUpgradePriceLvl2", 750000);
			INNER_DOOR_UPGRADE_PRICE3 = Feature.getInt("InnerDoorUpgradePriceLvl3", 900000);
			INNER_DOOR_UPGRADE_PRICE5 = Feature.getInt("InnerDoorUpgradePriceLvl5", 1000000);
			WALL_UPGRADE_PRICE2 = Feature.getInt("WallUpgradePriceLvl2", 1600000);
			WALL_UPGRADE_PRICE3 = Feature.getInt("WallUpgradePriceLvl3", 1800000);
			WALL_UPGRADE_PRICE5 = Feature.getInt("WallUpgradePriceLvl5", 2000000);
			TRAP_UPGRADE_PRICE1 = Feature.getInt("TrapUpgradePriceLvl1", 3000000);
			TRAP_UPGRADE_PRICE2 = Feature.getInt("TrapUpgradePriceLvl2", 4000000);
			TRAP_UPGRADE_PRICE3 = Feature.getInt("TrapUpgradePriceLvl3", 5000000);
			TRAP_UPGRADE_PRICE4 = Feature.getInt("TrapUpgradePriceLvl4", 6000000);
			
			FS_TELE_FEE_RATIO = Feature.getLong("FortressTeleportFunctionFeeRatio", 604800000);
			FS_TELE1_FEE = Feature.getInt("FortressTeleportFunctionFeeLvl1", 1000);
			FS_TELE2_FEE = Feature.getInt("FortressTeleportFunctionFeeLvl2", 10000);
			FS_SUPPORT_FEE_RATIO = Feature.getLong("FortressSupportFunctionFeeRatio", 86400000);
			FS_SUPPORT1_FEE = Feature.getInt("FortressSupportFeeLvl1", 7000);
			FS_SUPPORT2_FEE = Feature.getInt("FortressSupportFeeLvl2", 17000);
			FS_MPREG_FEE_RATIO = Feature.getLong("FortressMpRegenerationFunctionFeeRatio", 86400000);
			FS_MPREG1_FEE = Feature.getInt("FortressMpRegenerationFeeLvl1", 6500);
			FS_MPREG2_FEE = Feature.getInt("FortressMpRegenerationFeeLvl2", 9300);
			FS_HPREG_FEE_RATIO = Feature.getLong("FortressHpRegenerationFunctionFeeRatio", 86400000);
			FS_HPREG1_FEE = Feature.getInt("FortressHpRegenerationFeeLvl1", 2000);
			FS_HPREG2_FEE = Feature.getInt("FortressHpRegenerationFeeLvl2", 3500);
			FS_EXPREG_FEE_RATIO = Feature.getLong("FortressExpRegenerationFunctionFeeRatio", 86400000);
			FS_EXPREG1_FEE = Feature.getInt("FortressExpRegenerationFeeLvl1", 9000);
			FS_EXPREG2_FEE = Feature.getInt("FortressExpRegenerationFeeLvl2", 10000);
			FS_UPDATE_FRQ = Feature.getInt("FortressPeriodicUpdateFrequency", 360);
			FS_BLOOD_OATH_COUNT = Feature.getInt("FortressBloodOathCount", 1);
			FS_MAX_SUPPLY_LEVEL = Feature.getInt("FortressMaxSupplyLevel", 6);
			FS_FEE_FOR_CASTLE = Feature.getInt("FortressFeeForCastle", 25000);
			FS_MAX_OWN_TIME = Feature.getInt("FortressMaximumOwnTime", 168);
			
			ALT_GAME_CASTLE_DAWN = Feature.getBoolean("AltCastleForDawn", true);
			ALT_GAME_CASTLE_DUSK = Feature.getBoolean("AltCastleForDusk", true);
			ALT_GAME_REQUIRE_CLAN_CASTLE = Feature.getBoolean("AltRequireClanCastle", false);
			ALT_FESTIVAL_MIN_PLAYER = Feature.getInt("AltFestivalMinPlayer", 5);
			ALT_MAXIMUM_PLAYER_CONTRIB = Feature.getInt("AltMaxPlayerContrib", 1000000);
			ALT_FESTIVAL_MANAGER_START = Feature.getLong("AltFestivalManagerStart", 120000);
			ALT_FESTIVAL_LENGTH = Feature.getLong("AltFestivalLength", 1080000);
			ALT_FESTIVAL_CYCLE_LENGTH = Feature.getLong("AltFestivalCycleLength", 2280000);
			ALT_FESTIVAL_FIRST_SPAWN = Feature.getLong("AltFestivalFirstSpawn", 120000);
			ALT_FESTIVAL_FIRST_SWARM = Feature.getLong("AltFestivalFirstSwarm", 300000);
			ALT_FESTIVAL_SECOND_SPAWN = Feature.getLong("AltFestivalSecondSpawn", 540000);
			ALT_FESTIVAL_SECOND_SWARM = Feature.getLong("AltFestivalSecondSwarm", 720000);
			ALT_FESTIVAL_CHEST_SPAWN = Feature.getLong("AltFestivalChestSpawn", 900000);
			ALT_SIEGE_DAWN_GATES_PDEF_MULT = Feature.getDouble("AltDawnGatesPdefMult", 1.1);
			ALT_SIEGE_DUSK_GATES_PDEF_MULT = Feature.getDouble("AltDuskGatesPdefMult", 0.8);
			ALT_SIEGE_DAWN_GATES_MDEF_MULT = Feature.getDouble("AltDawnGatesMdefMult", 1.1);
			ALT_SIEGE_DUSK_GATES_MDEF_MULT = Feature.getDouble("AltDuskGatesMdefMult", 0.8);
			ALT_STRICT_SEVENSIGNS = Feature.getBoolean("StrictSevenSigns", true);
			ALT_SEVENSIGNS_LAZY_UPDATE = Feature.getBoolean("AltSevenSignsLazyUpdate", true);
			
			SSQ_DAWN_TICKET_QUANTITY = Feature.getInt("SevenSignsDawnTicketQuantity", 300);
			SSQ_DAWN_TICKET_PRICE = Feature.getInt("SevenSignsDawnTicketPrice", 1000);
			SSQ_DAWN_TICKET_BUNDLE = Feature.getInt("SevenSignsDawnTicketBundle", 10);
			SSQ_MANORS_AGREEMENT_ID = Feature.getInt("SevenSignsManorsAgreementId", 6388);
			SSQ_JOIN_DAWN_ADENA_FEE = Feature.getInt("SevenSignsJoinDawnFee", 50000);
			
			TAKE_FORT_POINTS = Feature.getInt("TakeFortPoints", 200);
			LOOSE_FORT_POINTS = Feature.getInt("LooseFortPoints", 0);
			TAKE_CASTLE_POINTS = Feature.getInt("TakeCastlePoints", 1500);
			LOOSE_CASTLE_POINTS = Feature.getInt("LooseCastlePoints", 3000);
			CASTLE_DEFENDED_POINTS = Feature.getInt("CastleDefendedPoints", 750);
			FESTIVAL_WIN_POINTS = Feature.getInt("FestivalOfDarknessWin", 200);
			HERO_POINTS = Feature.getInt("HeroPoints", 1000);
			ROYAL_GUARD_COST = Feature.getInt("CreateRoyalGuardCost", 5000);
			KNIGHT_UNIT_COST = Feature.getInt("CreateKnightUnitCost", 10000);
			KNIGHT_REINFORCE_COST = Feature.getInt("ReinforceKnightUnitCost", 5000);
			BALLISTA_POINTS = Feature.getInt("KillBallistaPoints", 30);
			BLOODALLIANCE_POINTS = Feature.getInt("BloodAlliancePoints", 500);
			BLOODOATH_POINTS = Feature.getInt("BloodOathPoints", 200);
			KNIGHTSEPAULETTE_POINTS = Feature.getInt("KnightsEpaulettePoints", 20);
			REPUTATION_SCORE_PER_KILL = Feature.getInt("ReputationScorePerKill", 1);
			JOIN_ACADEMY_MIN_REP_SCORE = Feature.getInt("CompleteAcademyMinPoints", 190);
			JOIN_ACADEMY_MAX_REP_SCORE = Feature.getInt("CompleteAcademyMaxPoints", 650);
			RAID_RANKING_1ST = Feature.getInt("1stRaidRankingPoints", 1250);
			RAID_RANKING_2ND = Feature.getInt("2ndRaidRankingPoints", 900);
			RAID_RANKING_3RD = Feature.getInt("3rdRaidRankingPoints", 700);
			RAID_RANKING_4TH = Feature.getInt("4thRaidRankingPoints", 600);
			RAID_RANKING_5TH = Feature.getInt("5thRaidRankingPoints", 450);
			RAID_RANKING_6TH = Feature.getInt("6thRaidRankingPoints", 350);
			RAID_RANKING_7TH = Feature.getInt("7thRaidRankingPoints", 300);
			RAID_RANKING_8TH = Feature.getInt("8thRaidRankingPoints", 200);
			RAID_RANKING_9TH = Feature.getInt("9thRaidRankingPoints", 150);
			RAID_RANKING_10TH = Feature.getInt("10thRaidRankingPoints", 100);
			RAID_RANKING_UP_TO_50TH = Feature.getInt("UpTo50thRaidRankingPoints", 25);
			RAID_RANKING_UP_TO_100TH = Feature.getInt("UpTo100thRaidRankingPoints", 12);
			CLAN_LEVEL_6_COST = Feature.getInt("ClanLevel6Cost", 5000);
			CLAN_LEVEL_7_COST = Feature.getInt("ClanLevel7Cost", 10000);
			CLAN_LEVEL_8_COST = Feature.getInt("ClanLevel8Cost", 20000);
			CLAN_LEVEL_9_COST = Feature.getInt("ClanLevel9Cost", 40000);
			CLAN_LEVEL_10_COST = Feature.getInt("ClanLevel10Cost", 40000);
			CLAN_LEVEL_11_COST = Feature.getInt("ClanLevel11Cost", 75000);
			CLAN_LEVEL_6_REQUIREMENT = Feature.getInt("ClanLevel6Requirement", 30);
			CLAN_LEVEL_7_REQUIREMENT = Feature.getInt("ClanLevel7Requirement", 50);
			CLAN_LEVEL_8_REQUIREMENT = Feature.getInt("ClanLevel8Requirement", 80);
			CLAN_LEVEL_9_REQUIREMENT = Feature.getInt("ClanLevel9Requirement", 120);
			CLAN_LEVEL_10_REQUIREMENT = Feature.getInt("ClanLevel10Requirement", 140);
			CLAN_LEVEL_11_REQUIREMENT = Feature.getInt("ClanLevel11Requirement", 170);
			ALLOW_WYVERN_ALWAYS = Feature.getBoolean("AllowRideWyvernAlways", false);
			ALLOW_WYVERN_DURING_SIEGE = Feature.getBoolean("AllowRideWyvernDuringSiege", true);
			ALLOW_MOUNTS_DURING_SIEGE = Feature.getBoolean("AllowRideMountsDuringSiege", false);
			
			// Load Character L2Properties file (if exists)
			final PropertiesParser character = new PropertiesParser(CHARACTER_CONFIG_FILE);
			
			PLAYER_DELEVEL = character.getBoolean("Delevel", true);
			DECREASE_SKILL_LEVEL = character.getBoolean("DecreaseSkillOnDelevel", true);
			ALT_WEIGHT_LIMIT = character.getDouble("AltWeightLimit", 1);
			RUN_SPD_BOOST = character.getInt("RunSpeedBoost", 0);
			DEATH_PENALTY_CHANCE = character.getInt("DeathPenaltyChance", 20);
			RESPAWN_RESTORE_CP = character.getDouble("RespawnRestoreCP", 0) / 100;
			RESPAWN_RESTORE_HP = character.getDouble("RespawnRestoreHP", 65) / 100;
			RESPAWN_RESTORE_MP = character.getDouble("RespawnRestoreMP", 0) / 100;
			HP_REGEN_MULTIPLIER = character.getDouble("HpRegenMultiplier", 100) / 100;
			MP_REGEN_MULTIPLIER = character.getDouble("MpRegenMultiplier", 100) / 100;
			CP_REGEN_MULTIPLIER = character.getDouble("CpRegenMultiplier", 100) / 100;
			ENABLE_MODIFY_SKILL_DURATION = character.getBoolean("EnableModifySkillDuration", false);
			
			// Create Map only if enabled
			if (ENABLE_MODIFY_SKILL_DURATION)
			{
				final String[] propertySplit = character.getString("SkillDurationList", "").split(";");
				SKILL_DURATION_LIST = new HashMap<>(propertySplit.length);
				for (String skill : propertySplit)
				{
					final String[] skillSplit = skill.split(",");
					if (skillSplit.length != 2)
					{
						_log.warning("[SkillDurationList]: invalid config property -> SkillDurationList \"" + skill + "\"");
					}
					else
					{
						try
						{
							SKILL_DURATION_LIST.put(Integer.parseInt(skillSplit[0]), Integer.parseInt(skillSplit[1]));
						}
						catch (NumberFormatException nfe)
						{
							if (!skill.isEmpty())
							{
								_log.warning("[SkillDurationList]: invalid config property -> SkillList \"" + skillSplit[0] + "\"" + skillSplit[1]);
							}
						}
					}
				}
			}
			ENABLE_MODIFY_SKILL_REUSE = character.getBoolean("EnableModifySkillReuse", false);
			// Create Map only if enabled
			if (ENABLE_MODIFY_SKILL_REUSE)
			{
				final String[] propertySplit = character.getString("SkillReuseList", "").split(";");
				SKILL_REUSE_LIST = new HashMap<>(propertySplit.length);
				for (String skill : propertySplit)
				{
					final String[] skillSplit = skill.split(",");
					if (skillSplit.length != 2)
					{
						_log.warning("[SkillReuseList]: invalid config property -> SkillReuseList \"" + skill + "\"");
					}
					else
					{
						try
						{
							SKILL_REUSE_LIST.put(Integer.parseInt(skillSplit[0]), Integer.parseInt(skillSplit[1]));
						}
						catch (NumberFormatException nfe)
						{
							if (!skill.isEmpty())
							{
								_log.warning("[SkillReuseList]: invalid config property -> SkillList \"" + skillSplit[0] + "\"" + skillSplit[1]);
							}
						}
					}
				}
			}
			
			AUTO_LEARN_SKILLS = character.getBoolean("AutoLearnSkills", false);
			AUTO_LEARN_FS_SKILLS = character.getBoolean("AutoLearnForgottenScrollSkills", false);
			AUTO_LOOT_HERBS = character.getBoolean("AutoLootHerbs", false);
			BUFFS_MAX_AMOUNT = character.getByte("MaxBuffAmount", (byte) 20);
			TRIGGERED_BUFFS_MAX_AMOUNT = character.getByte("MaxTriggeredBuffAmount", (byte) 12);
			DANCES_MAX_AMOUNT = character.getByte("MaxDanceAmount", (byte) 12);
			DANCE_CANCEL_BUFF = character.getBoolean("DanceCancelBuff", false);
			DANCE_CONSUME_ADDITIONAL_MP = character.getBoolean("DanceConsumeAdditionalMP", true);
			ALT_STORE_DANCES = character.getBoolean("AltStoreDances", false);
			AUTO_LEARN_DIVINE_INSPIRATION = character.getBoolean("AutoLearnDivineInspiration", false);
			ALT_GAME_CANCEL_BOW = character.getString("AltGameCancelByHit", "Cast").equalsIgnoreCase("bow") || character.getString("AltGameCancelByHit", "Cast").equalsIgnoreCase("all");
			ALT_GAME_CANCEL_CAST = character.getString("AltGameCancelByHit", "Cast").equalsIgnoreCase("cast") || character.getString("AltGameCancelByHit", "Cast").equalsIgnoreCase("all");
			ALT_GAME_MAGICFAILURES = character.getBoolean("MagicFailures", true);
			PLAYER_FAKEDEATH_UP_PROTECTION = character.getInt("PlayerFakeDeathUpProtection", 0);
			STORE_SKILL_COOLTIME = character.getBoolean("StoreSkillCooltime", true);
			SUBCLASS_STORE_SKILL_COOLTIME = character.getBoolean("SubclassStoreSkillCooltime", false);
			SUMMON_STORE_SKILL_COOLTIME = character.getBoolean("SummonStoreSkillCooltime", true);
			ALT_GAME_SHIELD_BLOCKS = character.getBoolean("AltShieldBlocks", false);
			ALT_PERFECT_SHLD_BLOCK = character.getInt("AltPerfectShieldBlockRate", 10);
			EFFECT_TICK_RATIO = character.getLong("EffectTickRatio", 666);
			ALLOW_CLASS_MASTERS = character.getBoolean("AllowClassMasters", false);
			ALLOW_ENTIRE_TREE = character.getBoolean("AllowEntireTree", false);
			ALTERNATE_CLASS_MASTER = character.getBoolean("AlternateClassMaster", false);
			if (ALLOW_CLASS_MASTERS || ALTERNATE_CLASS_MASTER)
			{
				CLASS_MASTER_SETTINGS = new ClassMasterSettings(character.getString("ConfigClassMaster", ""));
			}
			LIFE_CRYSTAL_NEEDED = character.getBoolean("LifeCrystalNeeded", true);
			ES_SP_BOOK_NEEDED = character.getBoolean("EnchantSkillSpBookNeeded", true);
			DIVINE_SP_BOOK_NEEDED = character.getBoolean("DivineInspirationSpBookNeeded", true);
			ALT_GAME_SKILL_LEARN = character.getBoolean("AltGameSkillLearn", false);
			ALT_GAME_SUBCLASS_WITHOUT_QUESTS = character.getBoolean("AltSubClassWithoutQuests", false);
			ALT_GAME_SUBCLASS_EVERYWHERE = character.getBoolean("AltSubclassEverywhere", false);
			RESTORE_SERVITOR_ON_RECONNECT = character.getBoolean("RestoreServitorOnReconnect", true);
			RESTORE_PET_ON_RECONNECT = character.getBoolean("RestorePetOnReconnect", true);
			ALLOW_TRANSFORM_WITHOUT_QUEST = character.getBoolean("AltTransformationWithoutQuest", false);
			FEE_DELETE_TRANSFER_SKILLS = character.getInt("FeeDeleteTransferSkills", 10000000);
			FEE_DELETE_SUBCLASS_SKILLS = character.getInt("FeeDeleteSubClassSkills", 10000000);
			ENABLE_VITALITY = character.getBoolean("EnableVitality", true);
			RECOVER_VITALITY_ON_RECONNECT = character.getBoolean("RecoverVitalityOnReconnect", true);
			STARTING_VITALITY_POINTS = character.getInt("StartingVitalityPoints", 20000);
			MAX_BONUS_EXP = character.getDouble("MaxExpBonus", 3.5);
			MAX_BONUS_SP = character.getDouble("MaxSpBonus", 3.5);
			MAX_RUN_SPEED = character.getInt("MaxRunSpeed", 250);
			MAX_PCRIT_RATE = character.getInt("MaxPCritRate", 500);
			MAX_MCRIT_RATE = character.getInt("MaxMCritRate", 200);
			MAX_PATK_SPEED = character.getInt("MaxPAtkSpeed", 1500);
			MAX_MATK_SPEED = character.getInt("MaxMAtkSpeed", 1999);
			MAX_EVASION = character.getInt("MaxEvasion", 250);
			MIN_ABNORMAL_STATE_SUCCESS_RATE = character.getInt("MinAbnormalStateSuccessRate", 10);
			MAX_ABNORMAL_STATE_SUCCESS_RATE = character.getInt("MaxAbnormalStateSuccessRate", 90);
			MAX_SUBCLASS = character.getByte("MaxSubclass", (byte) 3);
			BASE_SUBCLASS_LEVEL = character.getByte("BaseSubclassLevel", (byte) 40);
			MAX_SUBCLASS_LEVEL = character.getByte("MaxSubclassLevel", (byte) 80);
			MAX_PVTSTORESELL_SLOTS_DWARF = character.getInt("MaxPvtStoreSellSlotsDwarf", 4);
			MAX_PVTSTORESELL_SLOTS_OTHER = character.getInt("MaxPvtStoreSellSlotsOther", 3);
			MAX_PVTSTOREBUY_SLOTS_DWARF = character.getInt("MaxPvtStoreBuySlotsDwarf", 5);
			MAX_PVTSTOREBUY_SLOTS_OTHER = character.getInt("MaxPvtStoreBuySlotsOther", 4);
			INVENTORY_MAXIMUM_NO_DWARF = character.getInt("MaximumSlotsForNoDwarf", 80);
			INVENTORY_MAXIMUM_DWARF = character.getInt("MaximumSlotsForDwarf", 100);
			INVENTORY_MAXIMUM_GM = character.getInt("MaximumSlotsForGMPlayer", 250);
			INVENTORY_MAXIMUM_QUEST_ITEMS = character.getInt("MaximumSlotsForQuestItems", 100);
			MAX_ITEM_IN_PACKET = Math.max(INVENTORY_MAXIMUM_NO_DWARF, Math.max(INVENTORY_MAXIMUM_DWARF, INVENTORY_MAXIMUM_GM));
			WAREHOUSE_SLOTS_DWARF = character.getInt("MaximumWarehouseSlotsForDwarf", 120);
			WAREHOUSE_SLOTS_NO_DWARF = character.getInt("MaximumWarehouseSlotsForNoDwarf", 100);
			WAREHOUSE_SLOTS_CLAN = character.getInt("MaximumWarehouseSlotsForClan", 150);
			ALT_FREIGHT_SLOTS = character.getInt("MaximumFreightSlots", 200);
			ALT_FREIGHT_PRICE = character.getInt("FreightPrice", 1000);
			ENCHANT_CHANCE_ELEMENT_STONE = character.getDouble("EnchantChanceElementStone", 50);
			ENCHANT_CHANCE_ELEMENT_CRYSTAL = character.getDouble("EnchantChanceElementCrystal", 30);
			ENCHANT_CHANCE_ELEMENT_JEWEL = character.getDouble("EnchantChanceElementJewel", 20);
			ENCHANT_CHANCE_ELEMENT_ENERGY = character.getDouble("EnchantChanceElementEnergy", 10);
			final String[] notenchantable = character.getString("EnchantBlackList", "7816,7817,7818,7819,7820,7821,7822,7823,7824,7825,7826,7827,7828,7829,7830,7831,13293,13294,13296").split(",");
			ENCHANT_BLACKLIST = new int[notenchantable.length];
			for (int i = 0; i < notenchantable.length; i++)
			{
				ENCHANT_BLACKLIST[i] = Integer.parseInt(notenchantable[i]);
			}
			Arrays.sort(ENCHANT_BLACKLIST);
			
			AUGMENTATION_NG_SKILL_CHANCE = character.getInt("AugmentationNGSkillChance", 15);
			AUGMENTATION_NG_GLOW_CHANCE = character.getInt("AugmentationNGGlowChance", 0);
			AUGMENTATION_MID_SKILL_CHANCE = character.getInt("AugmentationMidSkillChance", 30);
			AUGMENTATION_MID_GLOW_CHANCE = character.getInt("AugmentationMidGlowChance", 40);
			AUGMENTATION_HIGH_SKILL_CHANCE = character.getInt("AugmentationHighSkillChance", 45);
			AUGMENTATION_HIGH_GLOW_CHANCE = character.getInt("AugmentationHighGlowChance", 70);
			AUGMENTATION_TOP_SKILL_CHANCE = character.getInt("AugmentationTopSkillChance", 60);
			AUGMENTATION_TOP_GLOW_CHANCE = character.getInt("AugmentationTopGlowChance", 100);
			AUGMENTATION_BASESTAT_CHANCE = character.getInt("AugmentationBaseStatChance", 1);
			AUGMENTATION_ACC_SKILL_CHANCE = character.getInt("AugmentationAccSkillChance", 0);
			
			RETAIL_LIKE_AUGMENTATION = character.getBoolean("RetailLikeAugmentation", true);
			String[] array = character.getString("RetailLikeAugmentationNoGradeChance", "55,35,7,3").split(",");
			RETAIL_LIKE_AUGMENTATION_NG_CHANCE = new int[array.length];
			for (int i = 0; i < 4; i++)
			{
				RETAIL_LIKE_AUGMENTATION_NG_CHANCE[i] = Integer.parseInt(array[i]);
			}
			array = character.getString("RetailLikeAugmentationMidGradeChance", "55,35,7,3").split(",");
			RETAIL_LIKE_AUGMENTATION_MID_CHANCE = new int[array.length];
			for (int i = 0; i < 4; i++)
			{
				RETAIL_LIKE_AUGMENTATION_MID_CHANCE[i] = Integer.parseInt(array[i]);
			}
			array = character.getString("RetailLikeAugmentationHighGradeChance", "55,35,7,3").split(",");
			RETAIL_LIKE_AUGMENTATION_HIGH_CHANCE = new int[array.length];
			for (int i = 0; i < 4; i++)
			{
				RETAIL_LIKE_AUGMENTATION_HIGH_CHANCE[i] = Integer.parseInt(array[i]);
			}
			array = character.getString("RetailLikeAugmentationTopGradeChance", "55,35,7,3").split(",");
			RETAIL_LIKE_AUGMENTATION_TOP_CHANCE = new int[array.length];
			for (int i = 0; i < 4; i++)
			{
				RETAIL_LIKE_AUGMENTATION_TOP_CHANCE[i] = Integer.parseInt(array[i]);
			}
			RETAIL_LIKE_AUGMENTATION_ACCESSORY = character.getBoolean("RetailLikeAugmentationAccessory", true);
			
			array = character.getString("AugmentationBlackList", "6656,6657,6658,6659,6660,6661,6662,8191,10170,10314,13740,13741,13742,13743,13744,13745,13746,13747,13748,14592,14593,14594,14595,14596,14597,14598,14599,14600,14664,14665,14666,14667,14668,14669,14670,14671,14672,14801,14802,14803,14804,14805,14806,14807,14808,14809,15282,15283,15284,15285,15286,15287,15288,15289,15290,15291,15292,15293,15294,15295,15296,15297,15298,15299,16025,16026,21712,22173,22174,22175").split(",");
			AUGMENTATION_BLACKLIST = new int[array.length];
			
			for (int i = 0; i < array.length; i++)
			{
				AUGMENTATION_BLACKLIST[i] = Integer.parseInt(array[i]);
			}
			
			Arrays.sort(AUGMENTATION_BLACKLIST);
			ALT_ALLOW_AUGMENT_PVP_ITEMS = character.getBoolean("AltAllowAugmentPvPItems", false);
			ALT_GAME_KARMA_PLAYER_CAN_BE_KILLED_IN_PEACEZONE = character.getBoolean("AltKarmaPlayerCanBeKilledInPeaceZone", false);
			ALT_GAME_KARMA_PLAYER_CAN_SHOP = character.getBoolean("AltKarmaPlayerCanShop", true);
			ALT_GAME_KARMA_PLAYER_CAN_TELEPORT = character.getBoolean("AltKarmaPlayerCanTeleport", true);
			ALT_GAME_KARMA_PLAYER_CAN_USE_GK = character.getBoolean("AltKarmaPlayerCanUseGK", false);
			ALT_GAME_KARMA_PLAYER_CAN_TRADE = character.getBoolean("AltKarmaPlayerCanTrade", true);
			ALT_GAME_KARMA_PLAYER_CAN_USE_WAREHOUSE = character.getBoolean("AltKarmaPlayerCanUseWareHouse", true);
			MAX_PERSONAL_FAME_POINTS = character.getInt("MaxPersonalFamePoints", 100000);
			FORTRESS_ZONE_FAME_TASK_FREQUENCY = character.getInt("FortressZoneFameTaskFrequency", 300);
			FORTRESS_ZONE_FAME_AQUIRE_POINTS = character.getInt("FortressZoneFameAquirePoints", 31);
			CASTLE_ZONE_FAME_TASK_FREQUENCY = character.getInt("CastleZoneFameTaskFrequency", 300);
			CASTLE_ZONE_FAME_AQUIRE_POINTS = character.getInt("CastleZoneFameAquirePoints", 125);
			FAME_FOR_DEAD_PLAYERS = character.getBoolean("FameForDeadPlayers", true);
			IS_CRAFTING_ENABLED = character.getBoolean("CraftingEnabled", true);
			CRAFT_MASTERWORK = character.getBoolean("CraftMasterwork", true);
			DWARF_RECIPE_LIMIT = character.getInt("DwarfRecipeLimit", 50);
			COMMON_RECIPE_LIMIT = character.getInt("CommonRecipeLimit", 50);
			ALT_GAME_CREATION = character.getBoolean("AltGameCreation", false);
			ALT_GAME_CREATION_SPEED = character.getDouble("AltGameCreationSpeed", 1);
			ALT_GAME_CREATION_XP_RATE = character.getDouble("AltGameCreationXpRate", 1);
			ALT_GAME_CREATION_SP_RATE = character.getDouble("AltGameCreationSpRate", 1);
			ALT_GAME_CREATION_RARE_XPSP_RATE = character.getDouble("AltGameCreationRareXpSpRate", 2);
			ALT_BLACKSMITH_USE_RECIPES = character.getBoolean("AltBlacksmithUseRecipes", true);
			ALT_CLAN_LEADER_DATE_CHANGE = character.getInt("AltClanLeaderDateChange", 3);
			if ((ALT_CLAN_LEADER_DATE_CHANGE < 1) || (ALT_CLAN_LEADER_DATE_CHANGE > 7))
			{
				_log.log(Level.WARNING, "Wrong value specified for AltClanLeaderDateChange: " + ALT_CLAN_LEADER_DATE_CHANGE);
				ALT_CLAN_LEADER_DATE_CHANGE = 3;
			}
			ALT_CLAN_LEADER_HOUR_CHANGE = character.getString("AltClanLeaderHourChange", "00:00:00");
			ALT_CLAN_LEADER_INSTANT_ACTIVATION = character.getBoolean("AltClanLeaderInstantActivation", false);
			ALT_CLAN_JOIN_DAYS = character.getInt("DaysBeforeJoinAClan", 1);
			ALT_CLAN_CREATE_DAYS = character.getInt("DaysBeforeCreateAClan", 10);
			ALT_CLAN_DISSOLVE_DAYS = character.getInt("DaysToPassToDissolveAClan", 7);
			ALT_ALLY_JOIN_DAYS_WHEN_LEAVED = character.getInt("DaysBeforeJoinAllyWhenLeaved", 1);
			ALT_ALLY_JOIN_DAYS_WHEN_DISMISSED = character.getInt("DaysBeforeJoinAllyWhenDismissed", 1);
			ALT_ACCEPT_CLAN_DAYS_WHEN_DISMISSED = character.getInt("DaysBeforeAcceptNewClanWhenDismissed", 1);
			ALT_CREATE_ALLY_DAYS_WHEN_DISSOLVED = character.getInt("DaysBeforeCreateNewAllyWhenDissolved", 1);
			ALT_MAX_NUM_OF_CLANS_IN_ALLY = character.getInt("AltMaxNumOfClansInAlly", 3);
			ALT_CLAN_MEMBERS_FOR_WAR = character.getInt("AltClanMembersForWar", 15);
			ALT_MEMBERS_CAN_WITHDRAW_FROM_CLANWH = character.getBoolean("AltMembersCanWithdrawFromClanWH", false);
			REMOVE_CASTLE_CIRCLETS = character.getBoolean("RemoveCastleCirclets", true);
			ALT_PARTY_RANGE = character.getInt("AltPartyRange", 1600);
			ALT_PARTY_RANGE2 = character.getInt("AltPartyRange2", 1400);
			ALT_LEAVE_PARTY_LEADER = character.getBoolean("AltLeavePartyLeader", false);
			INITIAL_EQUIPMENT_EVENT = character.getBoolean("InitialEquipmentEvent", false);
			STARTING_ADENA = character.getLong("StartingAdena", 0);
			STARTING_LEVEL = character.getByte("StartingLevel", (byte) 1);
			STARTING_SP = character.getInt("StartingSP", 0);
			MAX_ADENA = character.getLong("MaxAdena", 99900000000L);
			if (MAX_ADENA < 0)
			{
				MAX_ADENA = Long.MAX_VALUE;
			}
			AUTO_LOOT = character.getBoolean("AutoLoot", false);
			AUTO_LOOT_RAIDS = character.getBoolean("AutoLootRaids", false);
			LOOT_RAIDS_PRIVILEGE_INTERVAL = character.getInt("RaidLootRightsInterval", 900) * 1000;
			LOOT_RAIDS_PRIVILEGE_CC_SIZE = character.getInt("RaidLootRightsCCSize", 45);
			UNSTUCK_INTERVAL = character.getInt("UnstuckInterval", 300);
			TELEPORT_WATCHDOG_TIMEOUT = character.getInt("TeleportWatchdogTimeout", 0);
			PLAYER_SPAWN_PROTECTION = character.getInt("PlayerSpawnProtection", 0);
			final String[] items = character.getString("PlayerSpawnProtectionAllowedItems", "0").split(",");
			SPAWN_PROTECTION_ALLOWED_ITEMS = new ArrayList<>(items.length);
			for (String item : items)
			{
				try
				{
					if (!item.isEmpty())
					{
						SPAWN_PROTECTION_ALLOWED_ITEMS.add(Integer.parseInt(item));
					}
				}
				catch (NumberFormatException nfe)
				{
					_log.warning("Player Spawn Protection: Wrong ItemId passed: " + item);
					_log.warning(nfe.getMessage());
				}
			}
			
			PLAYER_TELEPORT_PROTECTION = character.getInt("PlayerTeleportProtection", 0);
			RANDOM_RESPAWN_IN_TOWN_ENABLED = character.getBoolean("RandomRespawnInTownEnabled", true);
			OFFSET_ON_TELEPORT_ENABLED = character.getBoolean("OffsetOnTeleportEnabled", true);
			MAX_OFFSET_ON_TELEPORT = character.getInt("MaxOffsetOnTeleport", 50);
			PETITIONING_ALLOWED = character.getBoolean("PetitioningAllowed", true);
			MAX_PETITIONS_PER_PLAYER = character.getInt("MaxPetitionsPerPlayer", 5);
			MAX_PETITIONS_PENDING = character.getInt("MaxPetitionsPending", 25);
			FREE_TELEPORTING = character.getBoolean("FreeTeleporting", false);
			DELETE_DAYS = character.getInt("DeleteCharAfterDays", 7);
			ALT_GAME_EXPONENT_XP = character.getFloat("AltGameExponentXp", 0);
			ALT_GAME_EXPONENT_SP = character.getFloat("AltGameExponentSp", 0);
			PARTY_XP_CUTOFF_METHOD = character.getString("PartyXpCutoffMethod", "highfive");
			PARTY_XP_CUTOFF_PERCENT = character.getDouble("PartyXpCutoffPercent", 3);
			PARTY_XP_CUTOFF_LEVEL = character.getInt("PartyXpCutoffLevel", 20);
			final String[] gaps = character.getString("PartyXpCutoffGaps", "0,9;10,14;15,99").split(";");
			PARTY_XP_CUTOFF_GAPS = new int[gaps.length][2];
			for (int i = 0; i < gaps.length; i++)
			{
				PARTY_XP_CUTOFF_GAPS[i] = new int[]
				{
					Integer.parseInt(gaps[i].split(",")[0]),
					Integer.parseInt(gaps[i].split(",")[1])
				};
			}
			final String[] percents = character.getString("PartyXpCutoffGapPercent", "100;30;0").split(";");
			PARTY_XP_CUTOFF_GAP_PERCENTS = new int[percents.length];
			for (int i = 0; i < percents.length; i++)
			{
				PARTY_XP_CUTOFF_GAP_PERCENTS[i] = Integer.parseInt(percents[i]);
			}
			DISABLE_TUTORIAL = character.getBoolean("DisableTutorial", false);
			EXPERTISE_PENALTY = character.getBoolean("ExpertisePenalty", true);
			STORE_RECIPE_SHOPLIST = character.getBoolean("StoreRecipeShopList", false);
			STORE_UI_SETTINGS = character.getBoolean("StoreCharUiSettings", false);
			FORBIDDEN_NAMES = character.getString("ForbiddenNames", "").split(",");
			SILENCE_MODE_EXCLUDE = character.getBoolean("SilenceModeExclude", false);
			ALT_VALIDATE_TRIGGER_SKILLS = character.getBoolean("AltValidateTriggerSkills", false);
			PLAYER_MOVEMENT_BLOCK_TIME = character.getInt("NpcTalkBlockingTime", 0) * 1000;
			
			NEVIT_ENABLED = character.getBoolean("NevitEnabled", true);
			NEVIT_MAX_POINTS = character.getInt("NevitMaxPoints", 7200);
			NEVIT_BONUS_EFFECT_TIME = character.getInt("NevitBonusEffectTime", 180);
			NEVIT_ADVENT_TIME = character.getInt("NevitAdventTime", 14400);
			NEVIT_IGNORE_ADVENT_TIME = character.getBoolean("NevitIgnoreAdventTime", false);
			
			// Load Telnet L2Properties file (if exists)
			final PropertiesParser telnetSettings = new PropertiesParser(TELNET_FILE);
			
			IS_TELNET_ENABLED = telnetSettings.getBoolean("EnableTelnet", false);
			
			// MMO
			final PropertiesParser mmoSettings = new PropertiesParser(MMO_CONFIG_FILE);
			
			MMO_SELECTOR_SLEEP_TIME = mmoSettings.getInt("SleepTime", 20);
			MMO_MAX_SEND_PER_PASS = mmoSettings.getInt("MaxSendPerPass", 12);
			MMO_MAX_READ_PER_PASS = mmoSettings.getInt("MaxReadPerPass", 12);
			MMO_HELPER_BUFFER_COUNT = mmoSettings.getInt("HelperBufferCount", 20);
			MMO_TCP_NODELAY = mmoSettings.getBoolean("TcpNoDelay", false);
			
			// Load IdFactory L2Properties file (if exists)
			final PropertiesParser IdFactory = new PropertiesParser(ID_CONFIG_FILE);
			IDFACTORY_TYPE = IdFactory.getEnum("IDFactory", IdFactoryType.class, IdFactoryType.BitSet);
			BAD_ID_CHECKING = IdFactory.getBoolean("BadIdChecking", true);

            // Load Version files
            final PropertiesParser CoreVersion = new PropertiesParser(SERVER_VERSION_FILE);
            SERVER_VERSION = CoreVersion.getString("ProjectVersion", "Unknown project version");
            BUILD_DATE = CoreVersion.getString("ProjectDate", "Unknown project date");
			
			// Load General L2Properties file (if exists)
			final PropertiesParser General = new PropertiesParser(GENERAL_CONFIG_FILE);
			EVERYBODY_HAS_ADMIN_RIGHTS = General.getBoolean("EverybodyHasAdminRights", false);
			SERVER_LIST_BRACKET = General.getBoolean("ServerListBrackets", false);
			SERVER_LIST_TYPE = getServerTypeId(General.getString("ServerListType", "Normal").split(","));
			SERVER_LIST_AGE = General.getInt("ServerListAge", 0);
			SERVER_GMONLY = General.getBoolean("ServerGMOnly", false);
			GM_HERO_AURA = General.getBoolean("GMHeroAura", false);
			GM_STARTUP_INVULNERABLE = General.getBoolean("GMStartupInvulnerable", false);
			GM_STARTUP_INVISIBLE = General.getBoolean("GMStartupInvisible", false);
			GM_STARTUP_SILENCE = General.getBoolean("GMStartupSilence", false);
			GM_STARTUP_AUTO_LIST = General.getBoolean("GMStartupAutoList", false);
			GM_STARTUP_DIET_MODE = General.getBoolean("GMStartupDietMode", false);
			GM_ITEM_RESTRICTION = General.getBoolean("GMItemRestriction", true);
			GM_SKILL_RESTRICTION = General.getBoolean("GMSkillRestriction", true);
			GM_TRADE_RESTRICTED_ITEMS = General.getBoolean("GMTradeRestrictedItems", false);
			GM_RESTART_FIGHTING = General.getBoolean("GMRestartFighting", true);
			GM_ANNOUNCER_NAME = General.getBoolean("GMShowAnnouncerName", false);
			GM_CRITANNOUNCER_NAME = General.getBoolean("GMShowCritAnnouncerName", false);
			GM_GIVE_SPECIAL_SKILLS = General.getBoolean("GMGiveSpecialSkills", false);
			GM_GIVE_SPECIAL_AURA_SKILLS = General.getBoolean("GMGiveSpecialAuraSkills", false);
			GAMEGUARD_ENFORCE = General.getBoolean("GameGuardEnforce", false);
			GAMEGUARD_PROHIBITACTION = General.getBoolean("GameGuardProhibitAction", false);
			LOG_CHAT = General.getBoolean("LogChat", false);
			LOG_AUTO_ANNOUNCEMENTS = General.getBoolean("LogAutoAnnouncements", false);
			LOG_ITEMS = General.getBoolean("LogItems", false);
			LOG_ITEMS_SMALL_LOG = General.getBoolean("LogItemsSmallLog", false);
			LOG_ITEM_ENCHANTS = General.getBoolean("LogItemEnchants", false);
			LOG_SKILL_ENCHANTS = General.getBoolean("LogSkillEnchants", false);
			GMAUDIT = General.getBoolean("GMAudit", false);
			SKILL_CHECK_ENABLE = General.getBoolean("SkillCheckEnable", false);
			SKILL_CHECK_REMOVE = General.getBoolean("SkillCheckRemove", false);
			SKILL_CHECK_GM = General.getBoolean("SkillCheckGM", true);
			DEBUG = General.getBoolean("Debug", false);
			DEBUG_INSTANCES = General.getBoolean("InstanceDebug", false);
			HTML_ACTION_CACHE_DEBUG = General.getBoolean("HtmlActionCacheDebug", false);
			PACKET_HANDLER_DEBUG = General.getBoolean("PacketHandlerDebug", false);
			DEVELOPER = General.getBoolean("Developer", false);
			ALT_DEV_NO_HANDLERS = General.getBoolean("AltDevNoHandlers", false) || Boolean.getBoolean("nohandlers");
			ALT_DEV_NO_QUESTS = General.getBoolean("AltDevNoQuests", false) || Boolean.getBoolean("noquests");
			ALT_DEV_NO_SPAWNS = General.getBoolean("AltDevNoSpawns", false) || Boolean.getBoolean("nospawns");
			ALT_DEV_SHOW_QUESTS_LOAD_IN_LOGS = General.getBoolean("AltDevShowQuestsLoadInLogs", false);
			ALT_DEV_SHOW_SCRIPTS_LOAD_IN_LOGS = General.getBoolean("AltDevShowScriptsLoadInLogs", false);
			THREAD_P_EFFECTS = General.getInt("ThreadPoolSizeEffects", 10);
			THREAD_P_GENERAL = General.getInt("ThreadPoolSizeGeneral", 13);
			THREAD_E_EVENTS = General.getInt("ThreadPoolSizeEvents", 2);
			IO_PACKET_THREAD_CORE_SIZE = General.getInt("UrgentPacketThreadCoreSize", 2);
			GENERAL_PACKET_THREAD_CORE_SIZE = General.getInt("GeneralPacketThreadCoreSize", 4);
			GENERAL_THREAD_CORE_SIZE = General.getInt("GeneralThreadCoreSize", 4);
			AI_MAX_THREAD = General.getInt("AiMaxThread", 6);
			EVENT_MAX_THREAD = General.getInt("EventsMaxThread", 5);
			CLIENT_PACKET_QUEUE_SIZE = General.getInt("ClientPacketQueueSize", 0);
			if (CLIENT_PACKET_QUEUE_SIZE == 0)
			{
				CLIENT_PACKET_QUEUE_SIZE = MMO_MAX_READ_PER_PASS + 2;
			}
			CLIENT_PACKET_QUEUE_MAX_BURST_SIZE = General.getInt("ClientPacketQueueMaxBurstSize", 0);
			if (CLIENT_PACKET_QUEUE_MAX_BURST_SIZE == 0)
			{
				CLIENT_PACKET_QUEUE_MAX_BURST_SIZE = MMO_MAX_READ_PER_PASS + 1;
			}
			CLIENT_PACKET_QUEUE_MAX_PACKETS_PER_SECOND = General.getInt("ClientPacketQueueMaxPacketsPerSecond", 80);
			CLIENT_PACKET_QUEUE_MEASURE_INTERVAL = General.getInt("ClientPacketQueueMeasureInterval", 5);
			CLIENT_PACKET_QUEUE_MAX_AVERAGE_PACKETS_PER_SECOND = General.getInt("ClientPacketQueueMaxAveragePacketsPerSecond", 40);
			CLIENT_PACKET_QUEUE_MAX_FLOODS_PER_MIN = General.getInt("ClientPacketQueueMaxFloodsPerMin", 2);
			CLIENT_PACKET_QUEUE_MAX_OVERFLOWS_PER_MIN = General.getInt("ClientPacketQueueMaxOverflowsPerMin", 1);
			CLIENT_PACKET_QUEUE_MAX_UNDERFLOWS_PER_MIN = General.getInt("ClientPacketQueueMaxUnderflowsPerMin", 1);
			CLIENT_PACKET_QUEUE_MAX_UNKNOWN_PER_MIN = General.getInt("ClientPacketQueueMaxUnknownPerMin", 5);
			DEADLOCK_DETECTOR = General.getBoolean("DeadLockDetector", true);
			DEADLOCK_CHECK_INTERVAL = General.getInt("DeadLockCheckInterval", 20);
			RESTART_ON_DEADLOCK = General.getBoolean("RestartOnDeadlock", false);
			ALLOW_DISCARDITEM = General.getBoolean("AllowDiscardItem", true);
			AUTODESTROY_ITEM_AFTER = General.getInt("AutoDestroyDroppedItemAfter", 600);
			HERB_AUTO_DESTROY_TIME = General.getInt("AutoDestroyHerbTime", 60) * 1000;
			final String[] split = General.getString("ListOfProtectedItems", "0").split(",");
			LIST_PROTECTED_ITEMS = new ArrayList<>(split.length);
			for (String id : split)
			{
				LIST_PROTECTED_ITEMS.add(Integer.parseInt(id));
			}
			DATABASE_CLEAN_UP = General.getBoolean("DatabaseCleanUp", true);
			CHAR_STORE_INTERVAL = General.getInt("CharacterDataStoreInterval", 15);
			LAZY_ITEMS_UPDATE = General.getBoolean("LazyItemsUpdate", false);
			UPDATE_ITEMS_ON_CHAR_STORE = General.getBoolean("UpdateItemsOnCharStore", false);
			DESTROY_DROPPED_PLAYER_ITEM = General.getBoolean("DestroyPlayerDroppedItem", false);
			DESTROY_EQUIPABLE_PLAYER_ITEM = General.getBoolean("DestroyEquipableItem", false);
			SAVE_DROPPED_ITEM = General.getBoolean("SaveDroppedItem", false);
			EMPTY_DROPPED_ITEM_TABLE_AFTER_LOAD = General.getBoolean("EmptyDroppedItemTableAfterLoad", false);
			SAVE_DROPPED_ITEM_INTERVAL = General.getInt("SaveDroppedItemInterval", 60) * 60000;
			CLEAR_DROPPED_ITEM_TABLE = General.getBoolean("ClearDroppedItemTable", false);
			AUTODELETE_INVALID_QUEST_DATA = General.getBoolean("AutoDeleteInvalidQuestData", false);
			MULTIPLE_ITEM_DROP = General.getBoolean("MultipleItemDrop", true);
			FORCE_INVENTORY_UPDATE = General.getBoolean("ForceInventoryUpdate", false);
			LAZY_CACHE = General.getBoolean("LazyCache", true);
			CACHE_CHAR_NAMES = General.getBoolean("CacheCharNames", true);
			MIN_NPC_ANIMATION = General.getInt("MinNPCAnimation", 10);
			MAX_NPC_ANIMATION = General.getInt("MaxNPCAnimation", 20);
			MIN_MONSTER_ANIMATION = General.getInt("MinMonsterAnimation", 5);
			MAX_MONSTER_ANIMATION = General.getInt("MaxMonsterAnimation", 20);
			MOVE_BASED_KNOWNLIST = General.getBoolean("MoveBasedKnownlist", false);
			KNOWNLIST_UPDATE_INTERVAL = General.getLong("KnownListUpdateInterval", 1250);
			GRIDS_ALWAYS_ON = General.getBoolean("GridsAlwaysOn", false);
			GRID_NEIGHBOR_TURNON_TIME = General.getInt("GridNeighborTurnOnTime", 1);
			GRID_NEIGHBOR_TURNOFF_TIME = General.getInt("GridNeighborTurnOffTime", 90);
			PEACE_ZONE_MODE = General.getInt("PeaceZoneMode", 0);
			DEFAULT_GLOBAL_CHAT = General.getString("GlobalChat", "ON");
			DEFAULT_TRADE_CHAT = General.getString("TradeChat", "ON");
			MINIMUM_CHAT_LEVEL = General.getInt("MinimumChatLevel", 0);
			ALLOW_WAREHOUSE = General.getBoolean("AllowWarehouse", true);
			WAREHOUSE_CACHE = General.getBoolean("WarehouseCache", false);
			WAREHOUSE_CACHE_TIME = General.getInt("WarehouseCacheTime", 15);
			ALLOW_REFUND = General.getBoolean("AllowRefund", true);
			ALLOW_MAIL = General.getBoolean("AllowMail", true);
			ALLOW_ATTACHMENTS = General.getBoolean("AllowAttachments", true);
			ALLOW_WEAR = General.getBoolean("AllowWear", true);
			WEAR_DELAY = General.getInt("WearDelay", 5);
			WEAR_PRICE = General.getInt("WearPrice", 10);
			INSTANCE_FINISH_TIME = 1000 * General.getInt("DefaultFinishTime", 300);
			RESTORE_PLAYER_INSTANCE = General.getBoolean("RestorePlayerInstance", false);
			ALLOW_SUMMON_IN_INSTANCE = General.getBoolean("AllowSummonInInstance", false);
			EJECT_DEAD_PLAYER_TIME = 1000 * General.getInt("EjectDeadPlayerTime", 60);
			ALLOW_LOTTERY = General.getBoolean("AllowLottery", true);
			ALLOW_RACE = General.getBoolean("AllowRace", true);
			ALLOW_WATER = General.getBoolean("AllowWater", true);
			ALLOW_RENTPET = General.getBoolean("AllowRentPet", false);
			ALLOWFISHING = General.getBoolean("AllowFishing", true);
			ALLOW_MANOR = General.getBoolean("AllowManor", true);
			ALLOW_BOAT = General.getBoolean("AllowBoat", true);
			BOAT_BROADCAST_RADIUS = General.getInt("BoatBroadcastRadius", 20000);
			ALLOW_CURSED_WEAPONS = General.getBoolean("AllowCursedWeapons", true);
			SERVER_NEWS = General.getBoolean("ShowServerNews", false);
			ENABLE_COMMUNITY_BOARD = General.getBoolean("EnableCommunityBoard", true);
			BBS_DEFAULT = General.getString("BBSDefault", "_bbshome");
			USE_SAY_FILTER = General.getBoolean("UseChatFilter", false);
			CHAT_FILTER_CHARS = General.getString("ChatFilterChars", "^_^");
			final String[] propertySplit4 = General.getString("BanChatChannels", "GENERAL;SHOUT;WORLD;TRADE;HERO_VOICE").trim().split(";");
			BAN_CHAT_CHANNELS = new HashSet<>();
			try
			{
				for (String chatId : propertySplit4)
				{
					BAN_CHAT_CHANNELS.add(Enum.valueOf(ChatType.class, chatId));
				}
			}
			catch (NumberFormatException nfe)
			{
				_log.log(Level.WARNING, nfe.getMessage(), nfe);
			}
			ALT_MANOR_REFRESH_TIME = General.getInt("AltManorRefreshTime", 20);
			ALT_MANOR_REFRESH_MIN = General.getInt("AltManorRefreshMin", 0);
			ALT_MANOR_APPROVE_TIME = General.getInt("AltManorApproveTime", 4);
			ALT_MANOR_APPROVE_MIN = General.getInt("AltManorApproveMin", 30);
			ALT_MANOR_MAINTENANCE_MIN = General.getInt("AltManorMaintenanceMin", 6);
			ALT_MANOR_SAVE_ALL_ACTIONS = General.getBoolean("AltManorSaveAllActions", false);
			ALT_MANOR_SAVE_PERIOD_RATE = General.getInt("AltManorSavePeriodRate", 2);
			ALT_LOTTERY_PRIZE = General.getLong("AltLotteryPrize", 50000);
			ALT_LOTTERY_TICKET_PRICE = General.getLong("AltLotteryTicketPrice", 2000);
			ALT_LOTTERY_5_NUMBER_RATE = General.getFloat("AltLottery5NumberRate", 0.6f);
			ALT_LOTTERY_4_NUMBER_RATE = General.getFloat("AltLottery4NumberRate", 0.2f);
			ALT_LOTTERY_3_NUMBER_RATE = General.getFloat("AltLottery3NumberRate", 0.2f);
			ALT_LOTTERY_2_AND_1_NUMBER_PRIZE = General.getLong("AltLottery2and1NumberPrize", 200);
			ALT_FISH_CHAMPIONSHIP_ENABLED = General.getBoolean("AltFishChampionshipEnabled", true);
			ALT_FISH_CHAMPIONSHIP_REWARD_ITEM = General.getInt("AltFishChampionshipRewardItemId", 57);
			ALT_FISH_CHAMPIONSHIP_REWARD_1 = General.getInt("AltFishChampionshipReward1", 800000);
			ALT_FISH_CHAMPIONSHIP_REWARD_2 = General.getInt("AltFishChampionshipReward2", 500000);
			ALT_FISH_CHAMPIONSHIP_REWARD_3 = General.getInt("AltFishChampionshipReward3", 300000);
			ALT_FISH_CHAMPIONSHIP_REWARD_4 = General.getInt("AltFishChampionshipReward4", 200000);
			ALT_FISH_CHAMPIONSHIP_REWARD_5 = General.getInt("AltFishChampionshipReward5", 100000);
			ALT_ITEM_AUCTION_ENABLED = General.getBoolean("AltItemAuctionEnabled", true);
			ALT_ITEM_AUCTION_EXPIRED_AFTER = General.getInt("AltItemAuctionExpiredAfter", 14);
			ALT_ITEM_AUCTION_TIME_EXTENDS_ON_BID = General.getInt("AltItemAuctionTimeExtendsOnBid", 0) * 1000;
			FS_TIME_ATTACK = General.getInt("TimeOfAttack", 50);
			FS_TIME_COOLDOWN = General.getInt("TimeOfCoolDown", 5);
			FS_TIME_ENTRY = General.getInt("TimeOfEntry", 3);
			FS_TIME_WARMUP = General.getInt("TimeOfWarmUp", 2);
			FS_PARTY_MEMBER_COUNT = General.getInt("NumberOfNecessaryPartyMembers", 4);
			if (FS_TIME_ATTACK <= 0)
			{
				FS_TIME_ATTACK = 50;
			}
			if (FS_TIME_COOLDOWN <= 0)
			{
				FS_TIME_COOLDOWN = 5;
			}
			if (FS_TIME_ENTRY <= 0)
			{
				FS_TIME_ENTRY = 3;
			}
			if (FS_TIME_ENTRY <= 0)
			{
				FS_TIME_ENTRY = 3;
			}
			if (FS_TIME_ENTRY <= 0)
			{
				FS_TIME_ENTRY = 3;
			}
			RIFT_MIN_PARTY_SIZE = General.getInt("RiftMinPartySize", 5);
			RIFT_MAX_JUMPS = General.getInt("MaxRiftJumps", 4);
			RIFT_SPAWN_DELAY = General.getInt("RiftSpawnDelay", 10000);
			RIFT_AUTO_JUMPS_TIME_MIN = General.getInt("AutoJumpsDelayMin", 480);
			RIFT_AUTO_JUMPS_TIME_MAX = General.getInt("AutoJumpsDelayMax", 600);
			RIFT_BOSS_ROOM_TIME_MUTIPLY = General.getFloat("BossRoomTimeMultiply", 1.5f);
			RIFT_ENTER_COST_RECRUIT = General.getInt("RecruitCost", 18);
			RIFT_ENTER_COST_SOLDIER = General.getInt("SoldierCost", 21);
			RIFT_ENTER_COST_OFFICER = General.getInt("OfficerCost", 24);
			RIFT_ENTER_COST_CAPTAIN = General.getInt("CaptainCost", 27);
			RIFT_ENTER_COST_COMMANDER = General.getInt("CommanderCost", 30);
			RIFT_ENTER_COST_HERO = General.getInt("HeroCost", 33);
			DEFAULT_PUNISH = IllegalActionPunishmentType.findByName(General.getString("DefaultPunish", "KICK"));
			DEFAULT_PUNISH_PARAM = General.getInt("DefaultPunishParam", 0);
			ONLY_GM_ITEMS_FREE = General.getBoolean("OnlyGMItemsFree", true);
			JAIL_IS_PVP = General.getBoolean("JailIsPvp", false);
			JAIL_DISABLE_CHAT = General.getBoolean("JailDisableChat", true);
			JAIL_DISABLE_TRANSACTION = General.getBoolean("JailDisableTransaction", false);
			CUSTOM_SPAWNLIST_TABLE = General.getBoolean("CustomSpawnlistTable", false);
			SAVE_GMSPAWN_ON_CUSTOM = General.getBoolean("SaveGmSpawnOnCustom", false);
			CUSTOM_NPC_DATA = General.getBoolean("CustomNpcData", false);
			CUSTOM_TELEPORT_TABLE = General.getBoolean("CustomTeleportTable", false);
			CUSTOM_NPCBUFFER_TABLES = General.getBoolean("CustomNpcBufferTables", false);
			CUSTOM_SKILLS_LOAD = General.getBoolean("CustomSkillsLoad", false);
			CUSTOM_ITEMS_LOAD = General.getBoolean("CustomItemsLoad", false);
			CUSTOM_MULTISELL_LOAD = General.getBoolean("CustomMultisellLoad", false);
			CUSTOM_BUYLIST_LOAD = General.getBoolean("CustomBuyListLoad", false);
			ALT_BIRTHDAY_GIFT = General.getInt("AltBirthdayGift", 22187);
			ALT_BIRTHDAY_MAIL_SUBJECT = General.getString("AltBirthdayMailSubject", "Happy Birthday!");
			ALT_BIRTHDAY_MAIL_TEXT = General.getString("AltBirthdayMailText", "Hello Adventurer!! Seeing as you're one year older now, I thought I would send you some birthday cheer :) Please find your birthday pack attached. May these gifts bring you joy and happiness on this very special day." + EOL + EOL + "Sincerely, Alegria");
			ENABLE_BLOCK_CHECKER_EVENT = General.getBoolean("EnableBlockCheckerEvent", false);
			MIN_BLOCK_CHECKER_TEAM_MEMBERS = General.getInt("BlockCheckerMinTeamMembers", 2);
			if (MIN_BLOCK_CHECKER_TEAM_MEMBERS < 1)
			{
				MIN_BLOCK_CHECKER_TEAM_MEMBERS = 1;
			}
			else if (MIN_BLOCK_CHECKER_TEAM_MEMBERS > 6)
			{
				MIN_BLOCK_CHECKER_TEAM_MEMBERS = 6;
			}
			HBCE_FAIR_PLAY = General.getBoolean("HBCEFairPlay", false);
			HELLBOUND_WITHOUT_QUEST = General.getBoolean("HellboundWithoutQuest", false);
			
			NORMAL_ENCHANT_COST_MULTIPLIER = General.getInt("NormalEnchantCostMultipiler", 1);
			SAFE_ENCHANT_COST_MULTIPLIER = General.getInt("SafeEnchantCostMultipiler", 5);
			
			BOTREPORT_ENABLE = General.getBoolean("EnableBotReportButton", false);
			BOTREPORT_RESETPOINT_HOUR = General.getString("BotReportPointsResetHour", "00:00").split(":");
			BOTREPORT_REPORT_DELAY = General.getInt("BotReportDelay", 30) * 60000;
			BOTREPORT_ALLOW_REPORTS_FROM_SAME_CLAN_MEMBERS = General.getBoolean("AllowReportsFromSameClanMembers", false);
			ENABLE_FALLING_DAMAGE = General.getBoolean("EnableFallingDamage", true);
			
			// Load FloodProtector L2Properties file
			final PropertiesParser FloodProtectors = new PropertiesParser(FLOOD_PROTECTOR_FILE);
			
			loadFloodProtectorConfigs(FloodProtectors);
			
			// Load NPC L2Properties file (if exists)
			final PropertiesParser NPC = new PropertiesParser(NPC_CONFIG_FILE);
			
			ANNOUNCE_MAMMON_SPAWN = NPC.getBoolean("AnnounceMammonSpawn", false);
			ALT_MOB_AGRO_IN_PEACEZONE = NPC.getBoolean("AltMobAgroInPeaceZone", true);
			ALT_ATTACKABLE_NPCS = NPC.getBoolean("AltAttackableNpcs", true);
			ALT_GAME_VIEWNPC = NPC.getBoolean("AltGameViewNpc", false);
			MAX_AGGRO_RANGE = NPC.getInt("MaxAggroRange", 450);
			MAX_DRIFT_RANGE = NPC.getInt("MaxDriftRange", 300);
			SHOW_NPC_LVL = NPC.getBoolean("ShowNpcLevel", false);
			SHOW_CREST_WITHOUT_QUEST = NPC.getBoolean("ShowCrestWithoutQuest", false);
			ENABLE_RANDOM_ENCHANT_EFFECT = NPC.getBoolean("EnableRandomEnchantEffect", false);
			MIN_NPC_LVL_DMG_PENALTY = NPC.getInt("MinNPCLevelForDmgPenalty", 78);
			NPC_DMG_PENALTY = parseConfigLine(NPC.getString("DmgPenaltyForLvLDifferences", "0.7, 0.6, 0.6, 0.55"));
			NPC_CRIT_DMG_PENALTY = parseConfigLine(NPC.getString("CritDmgPenaltyForLvLDifferences", "0.75, 0.65, 0.6, 0.58"));
			NPC_SKILL_DMG_PENALTY = parseConfigLine(NPC.getString("SkillDmgPenaltyForLvLDifferences", "0.8, 0.7, 0.65, 0.62"));
			MIN_NPC_LVL_MAGIC_PENALTY = NPC.getInt("MinNPCLevelForMagicPenalty", 78);
			NPC_SKILL_CHANCE_PENALTY = parseConfigLine(NPC.getString("SkillChancePenaltyForLvLDifferences", "2.5, 3.0, 3.25, 3.5"));
			DECAY_TIME_TASK = NPC.getInt("DecayTimeTask", 5000);
			DEFAULT_CORPSE_TIME = NPC.getInt("DefaultCorpseTime", 7);
			SPOILED_CORPSE_EXTEND_TIME = NPC.getInt("SpoiledCorpseExtendTime", 10);
			CORPSE_CONSUME_SKILL_ALLOWED_TIME_BEFORE_DECAY = NPC.getInt("CorpseConsumeSkillAllowedTimeBeforeDecay", 2000);
			GUARD_ATTACK_AGGRO_MOB = NPC.getBoolean("GuardAttackAggroMob", false);
			ENABLE_GUARD_RETURN = NPC.getBoolean("EnableGuardReturn", false);
			ALLOW_WYVERN_UPGRADER = NPC.getBoolean("AllowWyvernUpgrader", false);
			final String[] listPetRentNpc = NPC.getString("ListPetRentNpc", "30827").split(",");
			LIST_PET_RENT_NPC = new ArrayList<>(listPetRentNpc.length);
			for (String id : listPetRentNpc)
			{
				LIST_PET_RENT_NPC.add(Integer.valueOf(id));
			}
			RAID_HP_REGEN_MULTIPLIER = NPC.getDouble("RaidHpRegenMultiplier", 100) / 100;
			RAID_MP_REGEN_MULTIPLIER = NPC.getDouble("RaidMpRegenMultiplier", 100) / 100;
			RAID_PDEFENCE_MULTIPLIER = NPC.getDouble("RaidPDefenceMultiplier", 100) / 100;
			RAID_MDEFENCE_MULTIPLIER = NPC.getDouble("RaidMDefenceMultiplier", 100) / 100;
			RAID_PATTACK_MULTIPLIER = NPC.getDouble("RaidPAttackMultiplier", 100) / 100;
			RAID_MATTACK_MULTIPLIER = NPC.getDouble("RaidMAttackMultiplier", 100) / 100;
			RAID_MIN_RESPAWN_MULTIPLIER = NPC.getFloat("RaidMinRespawnMultiplier", 1.0f);
			RAID_MAX_RESPAWN_MULTIPLIER = NPC.getFloat("RaidMaxRespawnMultiplier", 1.0f);
			RAID_MINION_RESPAWN_TIMER = NPC.getInt("RaidMinionRespawnTime", 300000);
			final String[] propertySplit = NPC.getString("CustomMinionsRespawnTime", "").split(";");
			MINIONS_RESPAWN_TIME = new HashMap<>(propertySplit.length);
			for (String prop : propertySplit)
			{
				final String[] propSplit = prop.split(",");
				if (propSplit.length != 2)
				{
					_log.warning("[CustomMinionsRespawnTime]: invalid config property -> CustomMinionsRespawnTime \"" + prop + "\"");
				}
				
				try
				{
					MINIONS_RESPAWN_TIME.put(Integer.valueOf(propSplit[0]), Integer.valueOf(propSplit[1]));
				}
				catch (NumberFormatException nfe)
				{
					if (!prop.isEmpty())
					{
						_log.warning("[CustomMinionsRespawnTime]: invalid config property -> CustomMinionsRespawnTime \"" + propSplit[0] + "\"" + propSplit[1]);
					}
				}
			}
			
			RAID_DISABLE_CURSE = NPC.getBoolean("DisableRaidCurse", false);
			RAID_CHAOS_TIME = NPC.getInt("RaidChaosTime", 10);
			GRAND_CHAOS_TIME = NPC.getInt("GrandChaosTime", 10);
			MINION_CHAOS_TIME = NPC.getInt("MinionChaosTime", 10);
			INVENTORY_MAXIMUM_PET = NPC.getInt("MaximumSlotsForPet", 12);
			PET_HP_REGEN_MULTIPLIER = NPC.getDouble("PetHpRegenMultiplier", 100) / 100;
			PET_MP_REGEN_MULTIPLIER = NPC.getDouble("PetMpRegenMultiplier", 100) / 100;
			
			DROP_ADENA_MIN_LEVEL_DIFFERENCE = NPC.getInt("DropAdenaMinLevelDifference", 8);
			DROP_ADENA_MAX_LEVEL_DIFFERENCE = NPC.getInt("DropAdenaMaxLevelDifference", 15);
			DROP_ADENA_MIN_LEVEL_GAP_CHANCE = NPC.getDouble("DropAdenaMinLevelGapChance", 10);
			
			DROP_ITEM_MIN_LEVEL_DIFFERENCE = NPC.getInt("DropItemMinLevelDifference", 5);
			DROP_ITEM_MAX_LEVEL_DIFFERENCE = NPC.getInt("DropItemMaxLevelDifference", 10);
			DROP_ITEM_MIN_LEVEL_GAP_CHANCE = NPC.getDouble("DropItemMinLevelGapChance", 10);
			
			// Load Rates L2Properties file (if exists)
			final PropertiesParser RatesSettings = new PropertiesParser(RATES_CONFIG_FILE);
			
			RATE_XP = RatesSettings.getFloat("RateXp", 1);
			RATE_SP = RatesSettings.getFloat("RateSp", 1);
			RATE_PARTY_XP = RatesSettings.getFloat("RatePartyXp", 1);
			RATE_PARTY_SP = RatesSettings.getFloat("RatePartySp", 1);
			RATE_EXTRACTABLE = RatesSettings.getFloat("RateExtractable", 1);
			RATE_DROP_MANOR = RatesSettings.getInt("RateDropManor", 1);
			RATE_QUEST_DROP = RatesSettings.getFloat("RateQuestDrop", 1);
			RATE_QUEST_REWARD = RatesSettings.getFloat("RateQuestReward", 1);
			RATE_QUEST_REWARD_XP = RatesSettings.getFloat("RateQuestRewardXP", 1);
			RATE_QUEST_REWARD_SP = RatesSettings.getFloat("RateQuestRewardSP", 1);
			RATE_QUEST_REWARD_ADENA = RatesSettings.getFloat("RateQuestRewardAdena", 1);
			RATE_QUEST_REWARD_USE_MULTIPLIERS = RatesSettings.getBoolean("UseQuestRewardMultipliers", false);
			RATE_QUEST_REWARD_POTION = RatesSettings.getFloat("RateQuestRewardPotion", 1);
			RATE_QUEST_REWARD_SCROLL = RatesSettings.getFloat("RateQuestRewardScroll", 1);
			RATE_QUEST_REWARD_RECIPE = RatesSettings.getFloat("RateQuestRewardRecipe", 1);
			RATE_QUEST_REWARD_MATERIAL = RatesSettings.getFloat("RateQuestRewardMaterial", 1);
			RATE_HB_TRUST_INCREASE = RatesSettings.getFloat("RateHellboundTrustIncrease", 1);
			RATE_HB_TRUST_DECREASE = RatesSettings.getFloat("RateHellboundTrustDecrease", 1);
			
			RATE_VITALITY_LEVEL_1 = RatesSettings.getFloat("RateVitalityLevel1", 1.5f);
			RATE_VITALITY_LEVEL_2 = RatesSettings.getFloat("RateVitalityLevel2", 2);
			RATE_VITALITY_LEVEL_3 = RatesSettings.getFloat("RateVitalityLevel3", 2.5f);
			RATE_VITALITY_LEVEL_4 = RatesSettings.getFloat("RateVitalityLevel4", 3);
			RATE_RECOVERY_VITALITY_PEACE_ZONE = RatesSettings.getFloat("RateRecoveryPeaceZone", 1);
			RATE_VITALITY_LOST = RatesSettings.getFloat("RateVitalityLost", 1);
			RATE_VITALITY_GAIN = RatesSettings.getFloat("RateVitalityGain", 1);
			RATE_RECOVERY_ON_RECONNECT = RatesSettings.getFloat("RateRecoveryOnReconnect", 4);
			RATE_KARMA_LOST = RatesSettings.getFloat("RateKarmaLost", -1);
			if (RATE_KARMA_LOST == -1)
			{
				RATE_KARMA_LOST = RATE_XP;
			}
			RATE_KARMA_EXP_LOST = RatesSettings.getFloat("RateKarmaExpLost", 1);
			RATE_SIEGE_GUARDS_PRICE = RatesSettings.getFloat("RateSiegeGuardsPrice", 1);
			PLAYER_DROP_LIMIT = RatesSettings.getInt("PlayerDropLimit", 3);
			PLAYER_RATE_DROP = RatesSettings.getInt("PlayerRateDrop", 5);
			PLAYER_RATE_DROP_ITEM = RatesSettings.getInt("PlayerRateDropItem", 70);
			PLAYER_RATE_DROP_EQUIP = RatesSettings.getInt("PlayerRateDropEquip", 25);
			PLAYER_RATE_DROP_EQUIP_WEAPON = RatesSettings.getInt("PlayerRateDropEquipWeapon", 5);
			PET_XP_RATE = RatesSettings.getFloat("PetXpRate", 1);
			PET_FOOD_RATE = RatesSettings.getInt("PetFoodRate", 1);
			SINEATER_XP_RATE = RatesSettings.getFloat("SinEaterXpRate", 1);
			KARMA_DROP_LIMIT = RatesSettings.getInt("KarmaDropLimit", 10);
			KARMA_RATE_DROP = RatesSettings.getInt("KarmaRateDrop", 70);
			KARMA_RATE_DROP_ITEM = RatesSettings.getInt("KarmaRateDropItem", 50);
			KARMA_RATE_DROP_EQUIP = RatesSettings.getInt("KarmaRateDropEquip", 40);
			KARMA_RATE_DROP_EQUIP_WEAPON = RatesSettings.getInt("KarmaRateDropEquipWeapon", 10);
			
			RATE_DEATH_DROP_AMOUNT_MULTIPLIER = RatesSettings.getFloat("DeathDropAmountMultiplier", 1);
			RATE_CORPSE_DROP_AMOUNT_MULTIPLIER = RatesSettings.getFloat("CorpseDropAmountMultiplier", 1);
			RATE_HERB_DROP_AMOUNT_MULTIPLIER = RatesSettings.getFloat("HerbDropAmountMultiplier", 1);
			RATE_RAID_DROP_AMOUNT_MULTIPLIER = RatesSettings.getFloat("RaidDropAmountMultiplier", 1);
			RATE_DEATH_DROP_CHANCE_MULTIPLIER = RatesSettings.getFloat("DeathDropChanceMultiplier", 1);
			RATE_CORPSE_DROP_CHANCE_MULTIPLIER = RatesSettings.getFloat("CorpseDropChanceMultiplier", 1);
			RATE_HERB_DROP_CHANCE_MULTIPLIER = RatesSettings.getFloat("HerbDropChanceMultiplier", 1);
			RATE_RAID_DROP_CHANCE_MULTIPLIER = RatesSettings.getFloat("RaidDropChanceMultiplier", 1);
			
			final String[] dropAmountMultiplier = RatesSettings.getString("DropAmountMultiplierByItemId", "").split(";");
			RATE_DROP_AMOUNT_BY_ID = new HashMap<>(dropAmountMultiplier.length);
			if (!dropAmountMultiplier[0].isEmpty())
			{
				for (String item : dropAmountMultiplier)
				{
					final String[] itemSplit = item.split(",");
					if (itemSplit.length != 2)
					{
						_log.warning("Config.load(): invalid config property -> RateDropItemsById \"" + item + "\"");
					}
					else
					{
						try
						{
							RATE_DROP_AMOUNT_BY_ID.put(Integer.valueOf(itemSplit[0]), Float.valueOf(itemSplit[1]));
						}
						catch (NumberFormatException nfe)
						{
							if (!item.isEmpty())
							{
								_log.warning("Config.load(): invalid config property -> RateDropItemsById \"" + item + "\"");
							}
						}
					}
				}
			}
			
			final String[] dropChanceMultiplier = RatesSettings.getString("DropChanceMultiplierByItemId", "").split(";");
			RATE_DROP_CHANCE_BY_ID = new HashMap<>(dropChanceMultiplier.length);
			if (!dropChanceMultiplier[0].isEmpty())
			{
				for (String item : dropChanceMultiplier)
				{
					final String[] itemSplit = item.split(",");
					if (itemSplit.length != 2)
					{
						_log.warning("Config.load(): invalid config property -> RateDropItemsById \"" + item + "\"");
					}
					else
					{
						try
						{
							RATE_DROP_CHANCE_BY_ID.put(Integer.valueOf(itemSplit[0]), Float.valueOf(itemSplit[1]));
						}
						catch (NumberFormatException nfe)
						{
							if (!item.isEmpty())
							{
								_log.warning("Config.load(): invalid config property -> RateDropItemsById \"" + item + "\"");
							}
						}
					}
				}
			}
			
			// Load Custom L2Properties file (if exists)
			final PropertiesParser CustomSettings = new PropertiesParser(CUSTOM_CONFIG_FILE);
			
			L2JMOD_CHAMPION_ENABLE = CustomSettings.getBoolean("ChampionEnable", false);
			L2JMOD_CHAMPION_PASSIVE = CustomSettings.getBoolean("ChampionPassive", false);
			L2JMOD_CHAMPION_FREQUENCY = CustomSettings.getInt("ChampionFrequency", 0);
			L2JMOD_CHAMP_TITLE = CustomSettings.getString("ChampionTitle", "Champion");
			L2JMOD_SHOW_CHAMPION_AURA = CustomSettings.getBoolean("ChampionAura", true);
			L2JMOD_CHAMP_MIN_LVL = CustomSettings.getInt("ChampionMinLevel", 20);
			L2JMOD_CHAMP_MAX_LVL = CustomSettings.getInt("ChampionMaxLevel", 60);
			L2JMOD_CHAMPION_HP = CustomSettings.getInt("ChampionHp", 7);
			L2JMOD_CHAMPION_HP_REGEN = CustomSettings.getFloat("ChampionHpRegen", 1);
			L2JMOD_CHAMPION_REWARDS_EXP_SP = CustomSettings.getFloat("ChampionRewardsExpSp", 8);
			L2JMOD_CHAMPION_REWARDS_CHANCE = CustomSettings.getFloat("ChampionRewardsChance", 8);
			L2JMOD_CHAMPION_REWARDS_AMOUNT = CustomSettings.getFloat("ChampionRewardsAmount", 1);
			L2JMOD_CHAMPION_ADENAS_REWARDS_CHANCE = CustomSettings.getFloat("ChampionAdenasRewardsChance", 1);
			L2JMOD_CHAMPION_ADENAS_REWARDS_AMOUNT = CustomSettings.getFloat("ChampionAdenasRewardsAmount", 1);
			L2JMOD_CHAMPION_ATK = CustomSettings.getFloat("ChampionAtk", 1);
			L2JMOD_CHAMPION_SPD_ATK = CustomSettings.getFloat("ChampionSpdAtk", 1);
			L2JMOD_CHAMPION_REWARD_LOWER_LVL_ITEM_CHANCE = CustomSettings.getInt("ChampionRewardLowerLvlItemChance", 0);
			L2JMOD_CHAMPION_REWARD_HIGHER_LVL_ITEM_CHANCE = CustomSettings.getInt("ChampionRewardHigherLvlItemChance", 0);
			L2JMOD_CHAMPION_REWARD_ID = CustomSettings.getInt("ChampionRewardItemID", 6393);
			L2JMOD_CHAMPION_REWARD_QTY = CustomSettings.getInt("ChampionRewardItemQty", 1);
			L2JMOD_CHAMPION_ENABLE_VITALITY = CustomSettings.getBoolean("ChampionEnableVitality", false);
			L2JMOD_CHAMPION_ENABLE_IN_INSTANCES = CustomSettings.getBoolean("ChampionEnableInInstances", false);
			
			TVT_EVENT_ENABLED = CustomSettings.getBoolean("TvTEventEnabled", false);
			TVT_EVENT_IN_INSTANCE = CustomSettings.getBoolean("TvTEventInInstance", false);
			TVT_EVENT_INSTANCE_FILE = CustomSettings.getString("TvTEventInstanceFile", "coliseum.xml");
			TVT_EVENT_INTERVAL = CustomSettings.getString("TvTEventInterval", "20:00").split(",");
			TVT_EVENT_PARTICIPATION_TIME = CustomSettings.getInt("TvTEventParticipationTime", 3600);
			TVT_EVENT_RUNNING_TIME = CustomSettings.getInt("TvTEventRunningTime", 1800);
			TVT_EVENT_PARTICIPATION_NPC_ID = CustomSettings.getInt("TvTEventParticipationNpcId", 0);
			
			L2JMOD_ALLOW_WEDDING = CustomSettings.getBoolean("AllowWedding", false);
			L2JMOD_WEDDING_PRICE = CustomSettings.getInt("WeddingPrice", 250000000);
			L2JMOD_WEDDING_PUNISH_INFIDELITY = CustomSettings.getBoolean("WeddingPunishInfidelity", true);
			L2JMOD_WEDDING_TELEPORT = CustomSettings.getBoolean("WeddingTeleport", true);
			L2JMOD_WEDDING_TELEPORT_PRICE = CustomSettings.getInt("WeddingTeleportPrice", 50000);
			L2JMOD_WEDDING_TELEPORT_DURATION = CustomSettings.getInt("WeddingTeleportDuration", 60);
			L2JMOD_WEDDING_SAMESEX = CustomSettings.getBoolean("WeddingAllowSameSex", false);
			L2JMOD_WEDDING_FORMALWEAR = CustomSettings.getBoolean("WeddingFormalWear", true);
			L2JMOD_WEDDING_DIVORCE_COSTS = CustomSettings.getInt("WeddingDivorceCosts", 20);
			
			L2JMOD_ENABLE_WAREHOUSESORTING_CLAN = CustomSettings.getBoolean("EnableWarehouseSortingClan", false);
			L2JMOD_ENABLE_WAREHOUSESORTING_PRIVATE = CustomSettings.getBoolean("EnableWarehouseSortingPrivate", false);
			
			L2JMOD_OLD_DROP_BEHAVIOR = CustomSettings.getBoolean("OldDropBehavior", false);
			
			if (TVT_EVENT_PARTICIPATION_NPC_ID == 0)
			{
				TVT_EVENT_ENABLED = false;
				_log.warning("TvTEventEngine[Config.load()]: invalid config property -> TvTEventParticipationNpcId");
			}
			else
			{
				String[] tvtNpcCoords = CustomSettings.getString("TvTEventParticipationNpcCoordinates", "0,0,0").split(",");
				if (tvtNpcCoords.length < 3)
				{
					TVT_EVENT_ENABLED = false;
					_log.warning("TvTEventEngine[Config.load()]: invalid config property -> TvTEventParticipationNpcCoordinates");
				}
				else
				{
					TVT_EVENT_REWARDS = new ArrayList<>();
					TVT_DOORS_IDS_TO_OPEN = new ArrayList<>();
					TVT_DOORS_IDS_TO_CLOSE = new ArrayList<>();
					TVT_EVENT_PARTICIPATION_NPC_COORDINATES = new int[4];
					TVT_EVENT_TEAM_1_COORDINATES = new int[3];
					TVT_EVENT_TEAM_2_COORDINATES = new int[3];
					TVT_EVENT_PARTICIPATION_NPC_COORDINATES[0] = Integer.parseInt(tvtNpcCoords[0]);
					TVT_EVENT_PARTICIPATION_NPC_COORDINATES[1] = Integer.parseInt(tvtNpcCoords[1]);
					TVT_EVENT_PARTICIPATION_NPC_COORDINATES[2] = Integer.parseInt(tvtNpcCoords[2]);
					if (tvtNpcCoords.length == 4)
					{
						TVT_EVENT_PARTICIPATION_NPC_COORDINATES[3] = Integer.parseInt(tvtNpcCoords[3]);
					}
					TVT_EVENT_MIN_PLAYERS_IN_TEAMS = CustomSettings.getInt("TvTEventMinPlayersInTeams", 1);
					TVT_EVENT_MAX_PLAYERS_IN_TEAMS = CustomSettings.getInt("TvTEventMaxPlayersInTeams", 20);
					TVT_EVENT_MIN_LVL = CustomSettings.getByte("TvTEventMinPlayerLevel", (byte) 1);
					TVT_EVENT_MAX_LVL = CustomSettings.getByte("TvTEventMaxPlayerLevel", (byte) 80);
					TVT_EVENT_RESPAWN_TELEPORT_DELAY = CustomSettings.getInt("TvTEventRespawnTeleportDelay", 20);
					TVT_EVENT_START_LEAVE_TELEPORT_DELAY = CustomSettings.getInt("TvTEventStartLeaveTeleportDelay", 20);
					TVT_EVENT_EFFECTS_REMOVAL = CustomSettings.getInt("TvTEventEffectsRemoval", 0);
					TVT_EVENT_MAX_PARTICIPANTS_PER_IP = CustomSettings.getInt("TvTEventMaxParticipantsPerIP", 0);
					TVT_ALLOW_VOICED_COMMAND = CustomSettings.getBoolean("TvTAllowVoicedInfoCommand", false);
					TVT_EVENT_TEAM_1_NAME = CustomSettings.getString("TvTEventTeam1Name", "Team1");
					tvtNpcCoords = CustomSettings.getString("TvTEventTeam1Coordinates", "0,0,0").split(",");
					if (tvtNpcCoords.length < 3)
					{
						TVT_EVENT_ENABLED = false;
						_log.warning("TvTEventEngine[Config.load()]: invalid config property -> TvTEventTeam1Coordinates");
					}
					else
					{
						TVT_EVENT_TEAM_1_COORDINATES[0] = Integer.parseInt(tvtNpcCoords[0]);
						TVT_EVENT_TEAM_1_COORDINATES[1] = Integer.parseInt(tvtNpcCoords[1]);
						TVT_EVENT_TEAM_1_COORDINATES[2] = Integer.parseInt(tvtNpcCoords[2]);
						TVT_EVENT_TEAM_2_NAME = CustomSettings.getString("TvTEventTeam2Name", "Team2");
						tvtNpcCoords = CustomSettings.getString("TvTEventTeam2Coordinates", "0,0,0").split(",");
						if (tvtNpcCoords.length < 3)
						{
							TVT_EVENT_ENABLED = false;
							_log.warning("TvTEventEngine[Config.load()]: invalid config property -> TvTEventTeam2Coordinates");
						}
						else
						{
							TVT_EVENT_TEAM_2_COORDINATES[0] = Integer.parseInt(tvtNpcCoords[0]);
							TVT_EVENT_TEAM_2_COORDINATES[1] = Integer.parseInt(tvtNpcCoords[1]);
							TVT_EVENT_TEAM_2_COORDINATES[2] = Integer.parseInt(tvtNpcCoords[2]);
							tvtNpcCoords = CustomSettings.getString("TvTEventParticipationFee", "0,0").split(",");
							try
							{
								TVT_EVENT_PARTICIPATION_FEE[0] = Integer.parseInt(tvtNpcCoords[0]);
								TVT_EVENT_PARTICIPATION_FEE[1] = Integer.parseInt(tvtNpcCoords[1]);
							}
							catch (NumberFormatException nfe)
							{
								if (tvtNpcCoords.length > 0)
								{
									_log.warning("TvTEventEngine[Config.load()]: invalid config property -> TvTEventParticipationFee");
								}
							}
							tvtNpcCoords = CustomSettings.getString("TvTEventReward", "57,100000").split(";");
							for (String reward : tvtNpcCoords)
							{
								final String[] rewardSplit = reward.split(",");
								if (rewardSplit.length != 2)
								{
									_log.warning("TvTEventEngine[Config.load()]: invalid config property -> TvTEventReward \"" + reward + "\"");
								}
								else
								{
									try
									{
										TVT_EVENT_REWARDS.add(new int[]
										{
											Integer.parseInt(rewardSplit[0]),
											Integer.parseInt(rewardSplit[1])
										});
									}
									catch (NumberFormatException nfe)
									{
										if (!reward.isEmpty())
										{
											_log.warning("TvTEventEngine[Config.load()]: invalid config property -> TvTEventReward \"" + reward + "\"");
										}
									}
								}
							}
							
							TVT_EVENT_TARGET_TEAM_MEMBERS_ALLOWED = CustomSettings.getBoolean("TvTEventTargetTeamMembersAllowed", true);
							TVT_EVENT_SCROLL_ALLOWED = CustomSettings.getBoolean("TvTEventScrollsAllowed", false);
							TVT_EVENT_POTIONS_ALLOWED = CustomSettings.getBoolean("TvTEventPotionsAllowed", false);
							TVT_EVENT_SUMMON_BY_ITEM_ALLOWED = CustomSettings.getBoolean("TvTEventSummonByItemAllowed", false);
							TVT_REWARD_TEAM_TIE = CustomSettings.getBoolean("TvTRewardTeamTie", false);
							tvtNpcCoords = CustomSettings.getString("TvTDoorsToOpen", "").split(";");
							for (String door : tvtNpcCoords)
							{
								try
								{
									TVT_DOORS_IDS_TO_OPEN.add(Integer.parseInt(door));
								}
								catch (NumberFormatException nfe)
								{
									if (!door.isEmpty())
									{
										_log.warning("TvTEventEngine[Config.load()]: invalid config property -> TvTDoorsToOpen \"" + door + "\"");
									}
								}
							}
							
							tvtNpcCoords = CustomSettings.getString("TvTDoorsToClose", "").split(";");
							for (String door : tvtNpcCoords)
							{
								try
								{
									TVT_DOORS_IDS_TO_CLOSE.add(Integer.parseInt(door));
								}
								catch (NumberFormatException nfe)
								{
									if (!door.isEmpty())
									{
										_log.warning("TvTEventEngine[Config.load()]: invalid config property -> TvTDoorsToClose \"" + door + "\"");
									}
								}
							}
							
							tvtNpcCoords = CustomSettings.getString("TvTEventFighterBuffs", "").split(";");
							if (!tvtNpcCoords[0].isEmpty())
							{
								TVT_EVENT_FIGHTER_BUFFS = new HashMap<>(tvtNpcCoords.length);
								for (String skill : tvtNpcCoords)
								{
									final String[] skillSplit = skill.split(",");
									if (skillSplit.length != 2)
									{
										_log.warning("TvTEventEngine[Config.load()]: invalid config property -> TvTEventFighterBuffs \"" + skill + "\"");
									}
									else
									{
										try
										{
											TVT_EVENT_FIGHTER_BUFFS.put(Integer.parseInt(skillSplit[0]), Integer.parseInt(skillSplit[1]));
										}
										catch (NumberFormatException nfe)
										{
											if (!skill.isEmpty())
											{
												_log.warning("TvTEventEngine[Config.load()]: invalid config property -> TvTEventFighterBuffs \"" + skill + "\"");
											}
										}
									}
								}
							}
							
							tvtNpcCoords = CustomSettings.getString("TvTEventMageBuffs", "").split(";");
							if (!tvtNpcCoords[0].isEmpty())
							{
								TVT_EVENT_MAGE_BUFFS = new HashMap<>(tvtNpcCoords.length);
								for (String skill : tvtNpcCoords)
								{
									final String[] skillSplit = skill.split(",");
									if (skillSplit.length != 2)
									{
										_log.warning("TvTEventEngine[Config.load()]: invalid config property -> TvTEventMageBuffs \"" + skill + "\"");
									}
									else
									{
										try
										{
											TVT_EVENT_MAGE_BUFFS.put(Integer.parseInt(skillSplit[0]), Integer.parseInt(skillSplit[1]));
										}
										catch (NumberFormatException nfe)
										{
											if (!skill.isEmpty())
											{
												_log.warning("TvTEventEngine[Config.load()]: invalid config property -> TvTEventMageBuffs \"" + skill + "\"");
											}
										}
									}
								}
							}
						}
					}
				}
			}
			
			BANKING_SYSTEM_ENABLED = CustomSettings.getBoolean("BankingEnabled", false);
			BANKING_SYSTEM_GOLDBARS = CustomSettings.getInt("BankingGoldbarCount", 1);
			BANKING_SYSTEM_ADENA = CustomSettings.getInt("BankingAdenaCount", 500000000);
			
			OFFLINE_TRADE_ENABLE = CustomSettings.getBoolean("OfflineTradeEnable", false);
			OFFLINE_CRAFT_ENABLE = CustomSettings.getBoolean("OfflineCraftEnable", false);
			OFFLINE_MODE_IN_PEACE_ZONE = CustomSettings.getBoolean("OfflineModeInPeaceZone", false);
			OFFLINE_MODE_NO_DAMAGE = CustomSettings.getBoolean("OfflineModeNoDamage", false);
			OFFLINE_SET_NAME_COLOR = CustomSettings.getBoolean("OfflineSetNameColor", false);
			OFFLINE_NAME_COLOR = Integer.decode("0x" + CustomSettings.getString("OfflineNameColor", "808080"));
			OFFLINE_FAME = CustomSettings.getBoolean("OfflineFame", true);
			RESTORE_OFFLINERS = CustomSettings.getBoolean("RestoreOffliners", false);
			OFFLINE_MAX_DAYS = CustomSettings.getInt("OfflineMaxDays", 10);
			STORE_OFFLINE_TRADE_IN_REALTIME = CustomSettings.getBoolean("StoreOfflineTradeInRealtime", true);
			OFFLINE_DISCONNECT_FINISHED = CustomSettings.getBoolean("OfflineDisconnectFinished", true);
			
			L2JMOD_DISPLAY_SERVER_TIME = CustomSettings.getBoolean("DisplayServerTime", false);
			
			WELCOME_MESSAGE_ENABLED = CustomSettings.getBoolean("ScreenWelcomeMessageEnable", false);
			WELCOME_MESSAGE_TEXT = CustomSettings.getString("ScreenWelcomeMessageText", "Welcome to L2J server!");
			WELCOME_MESSAGE_TIME = CustomSettings.getInt("ScreenWelcomeMessageTime", 10) * 1000;
			
			L2JMOD_ANTIFEED_ENABLE = CustomSettings.getBoolean("AntiFeedEnable", false);
			L2JMOD_ANTIFEED_DUALBOX = CustomSettings.getBoolean("AntiFeedDualbox", true);
			L2JMOD_ANTIFEED_DISCONNECTED_AS_DUALBOX = CustomSettings.getBoolean("AntiFeedDisconnectedAsDualbox", true);
			L2JMOD_ANTIFEED_INTERVAL = CustomSettings.getInt("AntiFeedInterval", 120) * 1000;
			ANNOUNCE_PK_PVP = CustomSettings.getBoolean("AnnouncePkPvP", false);
			ANNOUNCE_PK_PVP_NORMAL_MESSAGE = CustomSettings.getBoolean("AnnouncePkPvPNormalMessage", true);
			ANNOUNCE_PK_MSG = CustomSettings.getString("AnnouncePkMsg", "$killer has slaughtered $target");
			ANNOUNCE_PVP_MSG = CustomSettings.getString("AnnouncePvpMsg", "$killer has defeated $target");
			
			L2JMOD_CHAT_ADMIN = CustomSettings.getBoolean("ChatAdmin", false);
			
			L2JMOD_MULTILANG_DEFAULT = CustomSettings.getString("MultiLangDefault", "en");
			L2JMOD_MULTILANG_ENABLE = CustomSettings.getBoolean("MultiLangEnable", false);
			String[] allowed = CustomSettings.getString("MultiLangAllowed", L2JMOD_MULTILANG_DEFAULT).split(";");
			L2JMOD_MULTILANG_ALLOWED = new ArrayList<>(allowed.length);
			for (String lang : allowed)
			{
				L2JMOD_MULTILANG_ALLOWED.add(lang);
			}
			
			if (!L2JMOD_MULTILANG_ALLOWED.contains(L2JMOD_MULTILANG_DEFAULT))
			{
				_log.warning("MultiLang[Config.load()]: default language: " + L2JMOD_MULTILANG_DEFAULT + " is not in allowed list !");
			}
			
			L2JMOD_HELLBOUND_STATUS = CustomSettings.getBoolean("HellboundStatus", false);
			L2JMOD_MULTILANG_VOICED_ALLOW = CustomSettings.getBoolean("MultiLangVoiceCommand", true);
			L2JMOD_MULTILANG_SM_ENABLE = CustomSettings.getBoolean("MultiLangSystemMessageEnable", false);
			allowed = CustomSettings.getString("MultiLangSystemMessageAllowed", "").split(";");
			L2JMOD_MULTILANG_SM_ALLOWED = new ArrayList<>(allowed.length);
			for (String lang : allowed)
			{
				if (!lang.isEmpty())
				{
					L2JMOD_MULTILANG_SM_ALLOWED.add(lang);
				}
			}
			L2JMOD_MULTILANG_NS_ENABLE = CustomSettings.getBoolean("MultiLangNpcStringEnable", false);
			allowed = CustomSettings.getString("MultiLangNpcStringAllowed", "").split(";");
			L2JMOD_MULTILANG_NS_ALLOWED = new ArrayList<>(allowed.length);
			for (String lang : allowed)
			{
				if (!lang.isEmpty())
				{
					L2JMOD_MULTILANG_NS_ALLOWED.add(lang);
				}
			}
			
			L2WALKER_PROTECTION = CustomSettings.getBoolean("L2WalkerProtection", false);
			L2JMOD_DEBUG_VOICE_COMMAND = CustomSettings.getBoolean("DebugVoiceCommand", false);
			
			L2JMOD_DUALBOX_CHECK_MAX_PLAYERS_PER_IP = CustomSettings.getInt("DualboxCheckMaxPlayersPerIP", 0);
			L2JMOD_DUALBOX_CHECK_MAX_OLYMPIAD_PARTICIPANTS_PER_IP = CustomSettings.getInt("DualboxCheckMaxOlympiadParticipantsPerIP", 0);
			L2JMOD_DUALBOX_CHECK_MAX_L2EVENT_PARTICIPANTS_PER_IP = CustomSettings.getInt("DualboxCheckMaxL2EventParticipantsPerIP", 0);
			final String[] dualboxCheckWhiteList = CustomSettings.getString("DualboxCheckWhitelist", "127.0.0.1,0").split(";");
			L2JMOD_DUALBOX_CHECK_WHITELIST = new HashMap<>(dualboxCheckWhiteList.length);
			for (String entry : dualboxCheckWhiteList)
			{
				final String[] entrySplit = entry.split(",");
				if (entrySplit.length != 2)
				{
					_log.warning("DualboxCheck[Config.load()]: invalid config property -> DualboxCheckWhitelist \"" + entry + "\"");
				}
				else
				{
					try
					{
						int num = Integer.parseInt(entrySplit[1]);
						num = num == 0 ? -1 : num;
						L2JMOD_DUALBOX_CHECK_WHITELIST.put(InetAddress.getByName(entrySplit[0]).hashCode(), num);
					}
					catch (UnknownHostException e)
					{
						_log.warning("DualboxCheck[Config.load()]: invalid address -> DualboxCheckWhitelist \"" + entrySplit[0] + "\"");
					}
					catch (NumberFormatException e)
					{
						_log.warning("DualboxCheck[Config.load()]: invalid number -> DualboxCheckWhitelist \"" + entrySplit[1] + "\"");
					}
				}
			}
			L2JMOD_ALLOW_CHANGE_PASSWORD = CustomSettings.getBoolean("AllowChangePassword", false);
			
			CUSTOM_STARTING_LOC = CustomSettings.getBoolean("CustomStartingLocation", false);
			CUSTOM_STARTING_LOC_X = CustomSettings.getInt("CustomStartingLocX", 83020);
			CUSTOM_STARTING_LOC_Y = CustomSettings.getInt("CustomStartingLocY", 147880);
			CUSTOM_STARTING_LOC_Z = CustomSettings.getInt("CustomStartingLocZ", -3469);
			
			SHOP_MIN_RANGE_FROM_PLAYER = CustomSettings.getInt("ShopMinRangeFromPlayer", 50);
			SHOP_MIN_RANGE_FROM_NPC = CustomSettings.getInt("ShopMinRangeFromNpc", 100);
			
			ENABLE_RANDOM_MONSTER_SPAWNS = CustomSettings.getBoolean("EnableRandomMonsterSpawns", false);
			MOB_MAX_SPAWN_RANGE = CustomSettings.getInt("MaxSpawnMobRange", 150);
			MOB_MIN_SPAWN_RANGE = MOB_MAX_SPAWN_RANGE * -1;
			if (ENABLE_RANDOM_MONSTER_SPAWNS)
			{
				final String[] mobsIds = CustomSettings.getString("MobsSpawnNotRandom", "18812,18813,18814,22138").split(",");
				MOBS_LIST_NOT_RANDOM = new ArrayList<>(mobsIds.length);
				for (String id : mobsIds)
				{
					MOBS_LIST_NOT_RANDOM.add(Integer.valueOf(id));
				}
			}
			
			CUSTOM_CB_ENABLED = CustomSettings.getBoolean("CustomCommunityBoard", false);
			COMMUNITYBOARD_CURRENCY = CustomSettings.getInt("CommunityCurrencyId", 57);
			COMMUNITYBOARD_ENABLE_MULTISELLS = CustomSettings.getBoolean("CommunityEnableMultisells", true);
			COMMUNITYBOARD_ENABLE_TELEPORTS = CustomSettings.getBoolean("CommunityEnableTeleports", true);
			COMMUNITYBOARD_ENABLE_BUFFS = CustomSettings.getBoolean("CommunityEnableBuffs", true);
			COMMUNITYBOARD_TELEPORT_PRICE = CustomSettings.getInt("CommunityTeleportPrice", 0);
			COMMUNITYBOARD_BUFF_PRICE = CustomSettings.getInt("CommunityBuffPrice", 0);
			COMMUNITYBOARD_COMBAT_DISABLED = CustomSettings.getBoolean("CommunityCombatDisabled", true);
			COMMUNITYBOARD_KARMA_DISABLED = CustomSettings.getBoolean("CommunityKarmaDisabled", true);
			
			PREMIUM_SYSTEM_ENABLED = CustomSettings.getBoolean("EnablePremiumSystem", false);
			PREMIUM_RATE_XP = CustomSettings.getFloat("PremiumRateXp", 2);
			PREMIUM_RATE_SP = CustomSettings.getFloat("PremiumRateSp", 2);
			PREMIUM_RATE_DROP_CHANCE = CustomSettings.getFloat("PremiumRateDropChance", 2);
			PREMIUM_RATE_DROP_AMOUNT = CustomSettings.getFloat("PremiumRateDropAmount", 1);
			PREMIUM_RATE_SPOIL_CHANCE = CustomSettings.getFloat("PremiumRateSpoilChance", 2);
			PREMIUM_RATE_SPOIL_AMOUNT = CustomSettings.getFloat("PremiumRateSpoilAmount", 1);
			final String[] premiumDropChanceMultiplier = CustomSettings.getString("PremiumRateDropChanceByItemId", "").split(";");
			PREMIUM_RATE_DROP_CHANCE_BY_ID = new HashMap<>(premiumDropChanceMultiplier.length);
			if (!premiumDropChanceMultiplier[0].isEmpty())
			{
				for (String item : premiumDropChanceMultiplier)
				{
					final String[] itemSplit = item.split(",");
					if (itemSplit.length != 2)
					{
						_log.warning("Config.load(): invalid config property -> PremiumRateDropChanceByItemId \"" + item + "\"");
					}
					else
					{
						try
						{
							PREMIUM_RATE_DROP_CHANCE_BY_ID.put(Integer.valueOf(itemSplit[0]), Float.valueOf(itemSplit[1]));
						}
						catch (NumberFormatException nfe)
						{
							if (!item.isEmpty())
							{
								_log.warning("Config.load(): invalid config property -> PremiumRateDropChanceByItemId \"" + item + "\"");
							}
						}
					}
				}
			}
			final String[] premiumDropAmountMultiplier = CustomSettings.getString("PremiumRateDropAmountByItemId", "").split(";");
			PREMIUM_RATE_DROP_AMOUNT_BY_ID = new HashMap<>(premiumDropAmountMultiplier.length);
			if (!premiumDropAmountMultiplier[0].isEmpty())
			{
				for (String item : premiumDropAmountMultiplier)
				{
					final String[] itemSplit = item.split(",");
					if (itemSplit.length != 2)
					{
						_log.warning("Config.load(): invalid config property -> PremiumRateDropAmountByItemId \"" + item + "\"");
					}
					else
					{
						try
						{
							PREMIUM_RATE_DROP_AMOUNT_BY_ID.put(Integer.valueOf(itemSplit[0]), Float.valueOf(itemSplit[1]));
						}
						catch (NumberFormatException nfe)
						{
							if (!item.isEmpty())
							{
								_log.warning("Config.load(): invalid config property -> PremiumRateDropAmountByItemId \"" + item + "\"");
							}
						}
					}
				}
			}
			
			// Load PvP L2Properties file (if exists)
			final PropertiesParser PVPSettings = new PropertiesParser(PVP_CONFIG_FILE);
			
			KARMA_DROP_GM = PVPSettings.getBoolean("CanGMDropEquipment", false);
			KARMA_AWARD_PK_KILL = PVPSettings.getBoolean("AwardPKKillPVPPoint", false);
			KARMA_PK_LIMIT = PVPSettings.getInt("MinimumPKRequiredToDrop", 5);
			KARMA_NONDROPPABLE_PET_ITEMS = PVPSettings.getString("ListOfPetItems", "2375,3500,3501,3502,4422,4423,4424,4425,6648,6649,6650,9882");
			KARMA_NONDROPPABLE_ITEMS = PVPSettings.getString("ListOfNonDroppableItems", "57,1147,425,1146,461,10,2368,7,6,2370,2369,6842,6611,6612,6613,6614,6615,6616,6617,6618,6619,6620,6621,7694,8181,5575,7694,9388,9389,9390");
			
			String[] karma = KARMA_NONDROPPABLE_PET_ITEMS.split(",");
			KARMA_LIST_NONDROPPABLE_PET_ITEMS = new int[karma.length];
			
			for (int i = 0; i < karma.length; i++)
			{
				KARMA_LIST_NONDROPPABLE_PET_ITEMS[i] = Integer.parseInt(karma[i]);
			}
			
			karma = KARMA_NONDROPPABLE_ITEMS.split(",");
			KARMA_LIST_NONDROPPABLE_ITEMS = new int[karma.length];
			
			for (int i = 0; i < karma.length; i++)
			{
				KARMA_LIST_NONDROPPABLE_ITEMS[i] = Integer.parseInt(karma[i]);
			}
			
			// sorting so binarySearch can be used later
			Arrays.sort(KARMA_LIST_NONDROPPABLE_PET_ITEMS);
			Arrays.sort(KARMA_LIST_NONDROPPABLE_ITEMS);
			
			PVP_NORMAL_TIME = PVPSettings.getInt("PvPVsNormalTime", 120000);
			PVP_PVP_TIME = PVPSettings.getInt("PvPVsPvPTime", 60000);
			
			// Load Olympiad L2Properties file (if exists)
			final PropertiesParser Olympiad = new PropertiesParser(OLYMPIAD_CONFIG_FILE);
			
			ALT_OLY_START_TIME = Olympiad.getInt("AltOlyStartTime", 18);
			ALT_OLY_MIN = Olympiad.getInt("AltOlyMin", 0);
			ALT_OLY_MAX_BUFFS = Olympiad.getInt("AltOlyMaxBuffs", 5);
			ALT_OLY_CPERIOD = Olympiad.getLong("AltOlyCPeriod", 21600000);
			ALT_OLY_BATTLE = Olympiad.getLong("AltOlyBattle", 300000);
			ALT_OLY_WPERIOD = Olympiad.getLong("AltOlyWPeriod", 604800000);
			ALT_OLY_VPERIOD = Olympiad.getLong("AltOlyVPeriod", 86400000);
			ALT_OLY_START_POINTS = Olympiad.getInt("AltOlyStartPoints", 10);
			ALT_OLY_WEEKLY_POINTS = Olympiad.getInt("AltOlyWeeklyPoints", 10);
			ALT_OLY_CLASSED = Olympiad.getInt("AltOlyClassedParticipants", 11);
			ALT_OLY_NONCLASSED = Olympiad.getInt("AltOlyNonClassedParticipants", 11);
			ALT_OLY_TEAMS = Olympiad.getInt("AltOlyTeamsParticipants", 6);
			ALT_OLY_REG_DISPLAY = Olympiad.getInt("AltOlyRegistrationDisplayNumber", 100);
			ALT_OLY_CLASSED_REWARD = parseItemsList(Olympiad.getString("AltOlyClassedReward", "13722,50"));
			ALT_OLY_NONCLASSED_REWARD = parseItemsList(Olympiad.getString("AltOlyNonClassedReward", "13722,40"));
			ALT_OLY_TEAM_REWARD = parseItemsList(Olympiad.getString("AltOlyTeamReward", "13722,85"));
			ALT_OLY_COMP_RITEM = Olympiad.getInt("AltOlyCompRewItem", 13722);
			ALT_OLY_MIN_MATCHES = Olympiad.getInt("AltOlyMinMatchesForPoints", 15);
			ALT_OLY_GP_PER_POINT = Olympiad.getInt("AltOlyGPPerPoint", 1000);
			ALT_OLY_HERO_POINTS = Olympiad.getInt("AltOlyHeroPoints", 200);
			ALT_OLY_RANK1_POINTS = Olympiad.getInt("AltOlyRank1Points", 100);
			ALT_OLY_RANK2_POINTS = Olympiad.getInt("AltOlyRank2Points", 75);
			ALT_OLY_RANK3_POINTS = Olympiad.getInt("AltOlyRank3Points", 55);
			ALT_OLY_RANK4_POINTS = Olympiad.getInt("AltOlyRank4Points", 40);
			ALT_OLY_RANK5_POINTS = Olympiad.getInt("AltOlyRank5Points", 30);
			ALT_OLY_MAX_POINTS = Olympiad.getInt("AltOlyMaxPoints", 10);
			ALT_OLY_DIVIDER_CLASSED = Olympiad.getInt("AltOlyDividerClassed", 5);
			ALT_OLY_DIVIDER_NON_CLASSED = Olympiad.getInt("AltOlyDividerNonClassed", 5);
			ALT_OLY_MAX_WEEKLY_MATCHES = Olympiad.getInt("AltOlyMaxWeeklyMatches", 70);
			ALT_OLY_MAX_WEEKLY_MATCHES_NON_CLASSED = Olympiad.getInt("AltOlyMaxWeeklyMatchesNonClassed", 60);
			ALT_OLY_MAX_WEEKLY_MATCHES_CLASSED = Olympiad.getInt("AltOlyMaxWeeklyMatchesClassed", 30);
			ALT_OLY_MAX_WEEKLY_MATCHES_TEAM = Olympiad.getInt("AltOlyMaxWeeklyMatchesTeam", 10);
			ALT_OLY_LOG_FIGHTS = Olympiad.getBoolean("AltOlyLogFights", false);
			ALT_OLY_SHOW_MONTHLY_WINNERS = Olympiad.getBoolean("AltOlyShowMonthlyWinners", true);
			ALT_OLY_ANNOUNCE_GAMES = Olympiad.getBoolean("AltOlyAnnounceGames", true);
			final String[] olyRestrictedItems = Olympiad.getString("AltOlyRestrictedItems", "6611,6612,6613,6614,6615,6616,6617,6618,6619,6620,6621,9388,9389,9390,17049,17050,17051,17052,17053,17054,17055,17056,17057,17058,17059,17060,17061,20759,20775,20776,20777,20778,14774").split(",");
			LIST_OLY_RESTRICTED_ITEMS = new ArrayList<>(olyRestrictedItems.length);
			for (String id : olyRestrictedItems)
			{
				LIST_OLY_RESTRICTED_ITEMS.add(Integer.parseInt(id));
			}
			ALT_OLY_ENCHANT_LIMIT = Olympiad.getInt("AltOlyEnchantLimit", -1);
			ALT_OLY_WAIT_TIME = Olympiad.getInt("AltOlyWaitTime", 120);
			
			final File hexIdFile = new File(HEXID_FILE);
			if (hexIdFile.exists())
			{
				final PropertiesParser hexId = new PropertiesParser(hexIdFile);
				
				if (hexId.containskey("ServerID") && hexId.containskey("HexID"))
				{
					SERVER_ID = hexId.getInt("ServerID", 1);
					try
					{
						HEX_ID = new BigInteger(hexId.getString("HexID", null), 16).toByteArray();
					}
					catch (Exception e)
					{
						_log.warning("Could not load HexID file (" + HEXID_FILE + "). Hopefully login will give us one.");
					}
				}
				else
				{
					_log.warning("Could not load HexID file (" + HEXID_FILE + "). Hopefully login will give us one.");
				}
			}
			else
			{
				_log.warning("Could not load HexID file (" + HEXID_FILE + "). Hopefully login will give us one.");
			}
			
			// Grand bosses
			final PropertiesParser GrandBossSettings = new PropertiesParser(GRANDBOSS_CONFIG_FILE);
			
			ANTHARAS_WAIT_TIME = GrandBossSettings.getInt("AntharasWaitTime", 30);
			ANTHARAS_SPAWN_INTERVAL = GrandBossSettings.getInt("IntervalOfAntharasSpawn", 264);
			ANTHARAS_SPAWN_RANDOM = GrandBossSettings.getInt("RandomOfAntharasSpawn", 72);
			
			VALAKAS_WAIT_TIME = GrandBossSettings.getInt("ValakasWaitTime", 30);
			VALAKAS_SPAWN_INTERVAL = GrandBossSettings.getInt("IntervalOfValakasSpawn", 264);
			VALAKAS_SPAWN_RANDOM = GrandBossSettings.getInt("RandomOfValakasSpawn", 72);
			
			BAIUM_SPAWN_INTERVAL = GrandBossSettings.getInt("IntervalOfBaiumSpawn", 168);
			BAIUM_SPAWN_RANDOM = GrandBossSettings.getInt("RandomOfBaiumSpawn", 48);
			
			CORE_SPAWN_INTERVAL = GrandBossSettings.getInt("IntervalOfCoreSpawn", 60);
			CORE_SPAWN_RANDOM = GrandBossSettings.getInt("RandomOfCoreSpawn", 24);
			
			ORFEN_SPAWN_INTERVAL = GrandBossSettings.getInt("IntervalOfOrfenSpawn", 48);
			ORFEN_SPAWN_RANDOM = GrandBossSettings.getInt("RandomOfOrfenSpawn", 20);
			
			QUEEN_ANT_SPAWN_INTERVAL = GrandBossSettings.getInt("IntervalOfQueenAntSpawn", 36);
			QUEEN_ANT_SPAWN_RANDOM = GrandBossSettings.getInt("RandomOfQueenAntSpawn", 17);
			
			BELETH_SPAWN_INTERVAL = GrandBossSettings.getInt("IntervalOfBelethSpawn", 192);
			BELETH_SPAWN_RANDOM = GrandBossSettings.getInt("RandomOfBelethSpawn", 148);
			BELETH_MIN_PLAYERS = GrandBossSettings.getInt("BelethMinPlayers", 36);
			
			// Gracia Seeds
			final PropertiesParser GraciaSeedsSettings = new PropertiesParser(GRACIASEEDS_CONFIG_FILE);
			
			// Seed of Destruction
			SOD_TIAT_KILL_COUNT = GraciaSeedsSettings.getInt("TiatKillCountForNextState", 10);
			SOD_STAGE_2_LENGTH = GraciaSeedsSettings.getLong("Stage2Length", 720) * 60000;
			
			MIN_TIAT_PLAYERS = GraciaSeedsSettings.getInt("MinPlayers", 36);
			MAX_TIAT_PLAYERS = GraciaSeedsSettings.getInt("MaxPlayers", 45);
			MIN_TIAT_LEVEL = GraciaSeedsSettings.getInt("MinLevel", 75);
			
			SOI_EKIMUS_KILL_COUNT = GraciaSeedsSettings.getInt("EkimusKillCount", 5);
			EROSION_ATTACK_MIN_PLAYERS = GraciaSeedsSettings.getInt("MinEroAttPlayers", 18);
			EROSION_ATTACK_MAX_PLAYERS = GraciaSeedsSettings.getInt("MaxEroAttPlayers", 27);
			EROSION_DEFENCE_MIN_PLAYERS = GraciaSeedsSettings.getInt("MinEroDefPlayers", 18);
			EROSION_DEFENCE_MAX_PLAYERS = GraciaSeedsSettings.getInt("MaxEroDefPlayers", 27);
			HEART_ATTACK_MIN_PLAYERS = GraciaSeedsSettings.getInt("MinHeaAttPlayers", 18);
			HEART_ATTACK_MAX_PLAYERS = GraciaSeedsSettings.getInt("MaxHeaAttPlayers", 27);
			HEART_DEFENCE_MIN_PLAYERS = GraciaSeedsSettings.getInt("MinHeaDefPlayers", 18);
			HEART_DEFENCE_MAX_PLAYERS = GraciaSeedsSettings.getInt("MaxHeaDefPlayers", 27);
			
			try
			{
				//@formatter:off
				FILTER_LIST = Files.lines(Paths.get(CHAT_FILTER_FILE), StandardCharsets.UTF_8)
					.map(String::trim)
					.filter(line -> (!line.isEmpty() && (line.charAt(0) != '#')))
					.collect(Collectors.toList());
				//@formatter:on
				_log.info("Loaded " + FILTER_LIST.size() + " Filter Words.");
			}
			catch (IOException ioe)
			{
				_log.log(Level.WARNING, "Error while loading chat filter words!", ioe);
			}
			
			final PropertiesParser ClanHallSiege = new PropertiesParser(CH_SIEGE_FILE);
			
			CHS_MAX_ATTACKERS = ClanHallSiege.getInt("MaxAttackers", 500);
			CHS_CLAN_MINLEVEL = ClanHallSiege.getInt("MinClanLevel", 4);
			CHS_MAX_FLAGS_PER_CLAN = ClanHallSiege.getInt("MaxFlagsPerClan", 1);
			CHS_ENABLE_FAME = ClanHallSiege.getBoolean("EnableFame", false);
			CHS_FAME_AMOUNT = ClanHallSiege.getInt("FameAmount", 0);
			CHS_FAME_FREQUENCY = ClanHallSiege.getInt("FameFrequency", 0);
			
			final PropertiesParser geoData = new PropertiesParser(GEODATA_FILE);
			
			PATHNODE_PATH = Paths.get(Config.DATAPACK_ROOT.getPath() + "/data/" + geoData.getString("PathnodePath", "pathnode"));
			PATHFINDING = geoData.getInt("PathFinding", 0);
			PATHFIND_BUFFERS = geoData.getString("PathFindBuffers", "100x6;128x6;192x6;256x4;320x4;384x4;500x2");
			LOW_WEIGHT = geoData.getFloat("LowWeight", 0.5f);
			MEDIUM_WEIGHT = geoData.getFloat("MediumWeight", 2);
			HIGH_WEIGHT = geoData.getFloat("HighWeight", 3);
			ADVANCED_DIAGONAL_STRATEGY = geoData.getBoolean("AdvancedDiagonalStrategy", true);
			DIAGONAL_WEIGHT = geoData.getFloat("DiagonalWeight", 0.707f);
			MAX_POSTFILTER_PASSES = geoData.getInt("MaxPostfilterPasses", 3);
			DEBUG_PATH = geoData.getBoolean("DebugPath", false);
			FORCE_GEODATA = geoData.getBoolean("ForceGeoData", true);
			COORD_SYNCHRONIZE = geoData.getInt("CoordSynchronize", -1);
			GEODATA_PATH = Paths.get(Config.DATAPACK_ROOT.getPath() + "/data/" + geoData.getString("GeoDataPath", "geodata"));
			TRY_LOAD_UNSPECIFIED_REGIONS = geoData.getBoolean("TryLoadUnspecifiedRegions", true);
			GEODATA_REGIONS = new HashMap<>();
			for (int regionX = L2World.TILE_X_MIN; regionX <= L2World.TILE_X_MAX; regionX++)
			{
				for (int regionY = L2World.TILE_Y_MIN; regionY <= L2World.TILE_Y_MAX; regionY++)
				{
					if (geoData.containskey(regionX + "_" + regionY))
					{
						GEODATA_REGIONS.put(regionX + "_" + regionY, geoData.getBoolean(regionX + "_" + regionY, false));
					}
				}
			}
		}
		else if (Server.serverMode == Server.MODE_LOGINSERVER)
		{
			final PropertiesParser ServerSettings = new PropertiesParser(LOGIN_CONFIGURATION_FILE);
			
			GAME_SERVER_LOGIN_HOST = ServerSettings.getString("LoginHostname", "127.0.0.1");
			GAME_SERVER_LOGIN_PORT = ServerSettings.getInt("LoginPort", 9013);
			
			LOGIN_BIND_ADDRESS = ServerSettings.getString("LoginserverHostname", "*");
			PORT_LOGIN = ServerSettings.getInt("LoginserverPort", 2106);
			
			try
			{
				DATAPACK_ROOT = new File(ServerSettings.getString("DatapackRoot", ".").replaceAll("\\\\", "/")).getCanonicalFile();
			}
			catch (IOException e)
			{
				_log.log(Level.WARNING, "Error setting datapack root!", e);
				DATAPACK_ROOT = new File(".");
			}
			
			DEBUG = ServerSettings.getBoolean("Debug", false);
			
			ACCEPT_NEW_GAMESERVER = ServerSettings.getBoolean("AcceptNewGameServer", true);
			
			LOGIN_TRY_BEFORE_BAN = ServerSettings.getInt("LoginTryBeforeBan", 5);
			LOGIN_BLOCK_AFTER_BAN = ServerSettings.getInt("LoginBlockAfterBan", 900);
			
			LOGIN_SERVER_SCHEDULE_RESTART = ServerSettings.getBoolean("LoginRestartSchedule", false);
			LOGIN_SERVER_SCHEDULE_RESTART_TIME = ServerSettings.getLong("LoginRestartTime", 24);
			
			DATABASE_DRIVER = ServerSettings.getString("Driver", "com.mysql.jdbc.Driver");
			DATABASE_URL = ServerSettings.getString("URL", "jdbc:mysql://localhost/l2jmobiush5?useUnicode=true&characterEncoding=utf-8");
			DATABASE_LOGIN = ServerSettings.getString("Login", "root");
			DATABASE_PASSWORD = ServerSettings.getString("Password", "");
			DATABASE_MAX_CONNECTIONS = ServerSettings.getInt("MaximumDbConnections", 10);
			DATABASE_MAX_IDLE_TIME = ServerSettings.getInt("MaximumDbIdleTime", 0);
			
			SHOW_LICENCE = ServerSettings.getBoolean("ShowLicence", true);
			
			AUTO_CREATE_ACCOUNTS = ServerSettings.getBoolean("AutoCreateAccounts", true);
			
			FLOOD_PROTECTION = ServerSettings.getBoolean("EnableFloodProtection", true);
			FAST_CONNECTION_LIMIT = ServerSettings.getInt("FastConnectionLimit", 15);
			NORMAL_CONNECTION_TIME = ServerSettings.getInt("NormalConnectionTime", 700);
			FAST_CONNECTION_TIME = ServerSettings.getInt("FastConnectionTime", 350);
			MAX_CONNECTION_PER_IP = ServerSettings.getInt("MaxConnectionPerIP", 50);
			
			// MMO
			final PropertiesParser mmoSettings = new PropertiesParser(MMO_CONFIG_FILE);
			
			MMO_SELECTOR_SLEEP_TIME = mmoSettings.getInt("SleepTime", 20);
			MMO_MAX_SEND_PER_PASS = mmoSettings.getInt("MaxSendPerPass", 12);
			MMO_MAX_READ_PER_PASS = mmoSettings.getInt("MaxReadPerPass", 12);
			MMO_HELPER_BUFFER_COUNT = mmoSettings.getInt("HelperBufferCount", 20);
			MMO_TCP_NODELAY = mmoSettings.getBoolean("TcpNoDelay", false);
			
			// Load Telnet L2Properties file (if exists)
			final PropertiesParser telnetSettings = new PropertiesParser(TELNET_FILE);
			
			IS_TELNET_ENABLED = telnetSettings.getBoolean("EnableTelnet", false);
		}
		else
		{
			_log.severe("Could not Load Config: server mode was not set!");
		}
	}
	
	/**
	 * Save hexadecimal ID of the server in the L2Properties file.<br>
	 * Check {@link #HEXID_FILE}.
	 * @param serverId the ID of the server whose hexId to save
	 * @param hexId the hexadecimal ID to store
	 */
	public static void saveHexid(int serverId, String hexId)
	{
		Config.saveHexid(serverId, hexId, HEXID_FILE);
	}
	
	/**
	 * Save hexadecimal ID of the server in the L2Properties file.
	 * @param serverId the ID of the server whose hexId to save
	 * @param hexId the hexadecimal ID to store
	 * @param fileName name of the L2Properties file
	 */
	public static void saveHexid(int serverId, String hexId, String fileName)
	{
		try
		{
			final Properties hexSetting = new Properties();
			final File file = new File(fileName);
			// Create a new empty file only if it doesn't exist
			file.createNewFile();
			try (OutputStream out = new FileOutputStream(file))
			{
				hexSetting.setProperty("ServerID", String.valueOf(serverId));
				hexSetting.setProperty("HexID", hexId);
				hexSetting.store(out, "The HexId to Auth into LoginServer");
			}
		}
		catch (Exception e)
		{
			_log.warning("Failed to save hex id to " + fileName + " File.");
			_log.warning("Config: " + e.getMessage());
		}
	}
	
	/**
	 * Loads flood protector configurations.
	 * @param properties the properties object containing the actual values of the flood protector configs
	 */
	private static void loadFloodProtectorConfigs(PropertiesParser properties)
	{
		loadFloodProtectorConfig(properties, FLOOD_PROTECTOR_USE_ITEM, "UseItem", 4);
		loadFloodProtectorConfig(properties, FLOOD_PROTECTOR_ROLL_DICE, "RollDice", 42);
		loadFloodProtectorConfig(properties, FLOOD_PROTECTOR_FIREWORK, "Firework", 42);
		loadFloodProtectorConfig(properties, FLOOD_PROTECTOR_ITEM_PET_SUMMON, "ItemPetSummon", 16);
		loadFloodProtectorConfig(properties, FLOOD_PROTECTOR_HERO_VOICE, "HeroVoice", 100);
		loadFloodProtectorConfig(properties, FLOOD_PROTECTOR_GLOBAL_CHAT, "GlobalChat", 5);
		loadFloodProtectorConfig(properties, FLOOD_PROTECTOR_SUBCLASS, "Subclass", 20);
		loadFloodProtectorConfig(properties, FLOOD_PROTECTOR_DROP_ITEM, "DropItem", 10);
		loadFloodProtectorConfig(properties, FLOOD_PROTECTOR_SERVER_BYPASS, "ServerBypass", 5);
		loadFloodProtectorConfig(properties, FLOOD_PROTECTOR_MULTISELL, "MultiSell", 1);
		loadFloodProtectorConfig(properties, FLOOD_PROTECTOR_TRANSACTION, "Transaction", 10);
		loadFloodProtectorConfig(properties, FLOOD_PROTECTOR_MANUFACTURE, "Manufacture", 3);
		loadFloodProtectorConfig(properties, FLOOD_PROTECTOR_MANOR, "Manor", 30);
		loadFloodProtectorConfig(properties, FLOOD_PROTECTOR_SENDMAIL, "SendMail", 100);
		loadFloodProtectorConfig(properties, FLOOD_PROTECTOR_CHARACTER_SELECT, "CharacterSelect", 30);
		loadFloodProtectorConfig(properties, FLOOD_PROTECTOR_ITEM_AUCTION, "ItemAuction", 9);
	}
	
	/**
	 * Loads single flood protector configuration.
	 * @param properties properties file reader
	 * @param config flood protector configuration instance
	 * @param configString flood protector configuration string that determines for which flood protector configuration should be read
	 * @param defaultInterval default flood protector interval
	 */
	private static void loadFloodProtectorConfig(PropertiesParser properties, FloodProtectorConfig config, String configString, int defaultInterval)
	{
		config.FLOOD_PROTECTION_INTERVAL = properties.getInt("FloodProtector" + configString + "Interval", defaultInterval);
		config.LOG_FLOODING = properties.getBoolean("FloodProtector" + configString + "LogFlooding", false);
		config.PUNISHMENT_LIMIT = properties.getInt("FloodProtector" + configString + "PunishmentLimit", 0);
		config.PUNISHMENT_TYPE = properties.getString("FloodProtector" + configString + "PunishmentType", "none");
		config.PUNISHMENT_TIME = properties.getInt("FloodProtector" + configString + "PunishmentTime", 0) * 60000;
	}
	
	public static int getServerTypeId(String[] serverTypes)
	{
		int serverType = 0;
		for (String cType : serverTypes)
		{
			switch (cType.trim().toLowerCase())
			{
				case "normal":
				{
					serverType |= 0x01;
					break;
				}
				case "relax":
				{
					serverType |= 0x02;
					break;
				}
				case "test":
				{
					serverType |= 0x04;
					break;
				}
				case "nolabel":
				{
					serverType |= 0x08;
					break;
				}
				case "restricted":
				{
					serverType |= 0x10;
					break;
				}
				case "event":
				{
					serverType |= 0x20;
					break;
				}
				case "free":
				{
					serverType |= 0x40;
					break;
				}
				default:
				{
					break;
				}
			}
		}
		return serverType;
	}
	
	public static final class ClassMasterSettings
	{
		private final Map<Integer, List<ItemHolder>> _claimItems = new HashMap<>(3);
		private final Map<Integer, List<ItemHolder>> _rewardItems = new HashMap<>(3);
		private final Map<Integer, Boolean> _allowedClassChange = new HashMap<>(3);
		
		public ClassMasterSettings(String configLine)
		{
			parseConfigLine(configLine.trim());
		}
		
		private void parseConfigLine(String configLine)
		{
			if (configLine.isEmpty())
			{
				return;
			}
			
			final StringTokenizer st = new StringTokenizer(configLine, ";");
			
			while (st.hasMoreTokens())
			{
				// get allowed class change
				final int job = Integer.parseInt(st.nextToken());
				
				_allowedClassChange.put(job, true);
				
				final List<ItemHolder> requiredItems = new ArrayList<>();
				// parse items needed for class change
				if (st.hasMoreTokens())
				{
					final StringTokenizer st2 = new StringTokenizer(st.nextToken(), "[],");
					
					while (st2.hasMoreTokens())
					{
						final StringTokenizer st3 = new StringTokenizer(st2.nextToken(), "()");
						final int itemId = Integer.parseInt(st3.nextToken());
						final int quantity = Integer.parseInt(st3.nextToken());
						requiredItems.add(new ItemHolder(itemId, quantity));
					}
				}
				
				_claimItems.put(job, requiredItems);
				
				final List<ItemHolder> rewardItems = new ArrayList<>();
				// parse gifts after class change
				if (st.hasMoreTokens())
				{
					final StringTokenizer st2 = new StringTokenizer(st.nextToken(), "[],");
					
					while (st2.hasMoreTokens())
					{
						final StringTokenizer st3 = new StringTokenizer(st2.nextToken(), "()");
						final int itemId = Integer.parseInt(st3.nextToken());
						final int quantity = Integer.parseInt(st3.nextToken());
						rewardItems.add(new ItemHolder(itemId, quantity));
					}
				}
				
				_rewardItems.put(job, rewardItems);
			}
		}
		
		public boolean isAllowed(int job)
		{
			return (_allowedClassChange != null) && _allowedClassChange.containsKey(job) && _allowedClassChange.get(job);
		}
		
		public List<ItemHolder> getRewardItems(int job)
		{
			return _rewardItems.get(job);
		}
		
		public List<ItemHolder> getRequireItems(int job)
		{
			return _claimItems.get(job);
		}
	}
	
	/**
	 * @param line the string line to parse
	 * @return a parsed float map
	 */
	private static Map<Integer, Float> parseConfigLine(String line)
	{
		final String[] propertySplit = line.split(",");
		final Map<Integer, Float> ret = new HashMap<>(propertySplit.length);
		int i = 0;
		for (String value : propertySplit)
		{
			ret.put(i++, Float.parseFloat(value));
		}
		return ret;
	}
	
	/**
	 * Parse a config value from its string representation to a two-dimensional int array.<br>
	 * The format of the value to be parsed should be as follows: "item1Id,item1Amount;item2Id,item2Amount;...itemNId,itemNAmount".
	 * @param line the value of the parameter to parse
	 * @return the parsed list or {@code null} if nothing was parsed
	 */
	private static int[][] parseItemsList(String line)
	{
		final String[] propertySplit = line.split(";");
		if (propertySplit.length == 0)
		{
			// nothing to do here
			return null;
		}
		
		int i = 0;
		String[] valueSplit;
		final int[][] result = new int[propertySplit.length][];
		int[] tmp;
		for (String value : propertySplit)
		{
			valueSplit = value.split(",");
			if (valueSplit.length != 2)
			{
				_log.warning("parseItemsList[Config.load()]: invalid entry -> \"" + valueSplit[0] + "\", should be itemId,itemNumber. Skipping to the next entry in the list.");
				continue;
			}
			
			tmp = new int[2];
			try
			{
				tmp[0] = Integer.parseInt(valueSplit[0]);
			}
			catch (NumberFormatException e)
			{
				_log.warning("parseItemsList[Config.load()]: invalid itemId -> \"" + valueSplit[0] + "\", value must be an integer. Skipping to the next entry in the list.");
				continue;
			}
			try
			{
				tmp[1] = Integer.parseInt(valueSplit[1]);
			}
			catch (NumberFormatException e)
			{
				_log.warning("parseItemsList[Config.load()]: invalid item number -> \"" + valueSplit[1] + "\", value must be an integer. Skipping to the next entry in the list.");
				continue;
			}
			result[i++] = tmp;
		}
		return result;
	}
	
	private static class IPConfigData implements IXmlReader
	{
		private static final List<String> _subnets = new ArrayList<>(5);
		private static final List<String> _hosts = new ArrayList<>(5);
		
		public IPConfigData()
		{
			load();
		}
		
		@Override
		public void load()
		{
			GameServer.printSection("Network Configuration");
			if ((new File(IP_CONFIG_FILE)).exists())
			{
				LOGGER.log(Level.INFO, "Using existing ipconfig.xml.");
				parseFile(new File(IP_CONFIG_FILE));
			}
			else
			// Auto configuration...
			{
				LOGGER.log(Level.INFO, "Using automatic network configuration.");
				autoIpConfig();
			}
		}
		
		@Override
		public void parseDocument(Document doc)
		{
			NamedNodeMap attrs;
			for (Node n = doc.getFirstChild(); n != null; n = n.getNextSibling())
			{
				if ("gameserver".equalsIgnoreCase(n.getNodeName()))
				{
					for (Node d = n.getFirstChild(); d != null; d = d.getNextSibling())
					{
						if ("define".equalsIgnoreCase(d.getNodeName()))
						{
							attrs = d.getAttributes();
							_subnets.add(attrs.getNamedItem("subnet").getNodeValue());
							_hosts.add(attrs.getNamedItem("address").getNodeValue());
							
							if (_hosts.size() != _subnets.size())
							{
								LOGGER.log(Level.WARNING, "Failed to Load " + IP_CONFIG_FILE + " File - subnets does not match server addresses.");
							}
						}
					}
					
					final Node att = n.getAttributes().getNamedItem("address");
					if (att == null)
					{
						LOGGER.log(Level.WARNING, "Failed to load " + IP_CONFIG_FILE + " file - default server address is missing.");
						_hosts.add("127.0.0.1");
					}
					else
					{
						_hosts.add(att.getNodeValue());
					}
					_subnets.add("0.0.0.0/0");
				}
			}
		}
		
		protected void autoIpConfig()
		{
			String externalIp = "127.0.0.1";
			try
			{
				final URL autoIp = new URL("http://ip1.dynupdate.no-ip.com:8245/");
				try (BufferedReader in = new BufferedReader(new InputStreamReader(autoIp.openStream())))
				{
					externalIp = in.readLine();
				}
			}
			catch (IOException e)
			{
				LOGGER.log(Level.INFO, "Failed to connect to api.externalip.net please check your internet connection using 127.0.0.1!");
				externalIp = "127.0.0.1";
			}
			
			try
			{
				final Enumeration<NetworkInterface> niList = NetworkInterface.getNetworkInterfaces();
				
				while (niList.hasMoreElements())
				{
					final NetworkInterface ni = niList.nextElement();
					
					if (!ni.isUp() || ni.isVirtual())
					{
						continue;
					}
					
					if (!ni.isLoopback() && ((ni.getHardwareAddress() == null) || (ni.getHardwareAddress().length != 6)))
					{
						continue;
					}
					
					for (InterfaceAddress ia : ni.getInterfaceAddresses())
					{
						if (ia.getAddress() instanceof Inet6Address)
						{
							continue;
						}
						
						final String hostAddress = ia.getAddress().getHostAddress();
						final int subnetPrefixLength = ia.getNetworkPrefixLength();
						final int subnetMaskInt = IntStream.rangeClosed(1, subnetPrefixLength).reduce((r, e) -> (r << 1) + 1).orElse(0) << (32 - subnetPrefixLength);
						final int hostAddressInt = Arrays.stream(hostAddress.split("\\.")).mapToInt(Integer::parseInt).reduce((r, e) -> (r << 8) + e).orElse(0);
						final int subnetAddressInt = hostAddressInt & subnetMaskInt;
						final String subnetAddress = ((subnetAddressInt >> 24) & 0xFF) + "." + ((subnetAddressInt >> 16) & 0xFF) + "." + ((subnetAddressInt >> 8) & 0xFF) + "." + (subnetAddressInt & 0xFF);
						final String subnet = subnetAddress + '/' + subnetPrefixLength;
						if (!_subnets.contains(subnet) && !subnet.equals("0.0.0.0/0"))
						{
							_subnets.add(subnet);
							_hosts.add(hostAddress);
							LOGGER.log(Level.INFO, "Adding new subnet: " + subnet + " address: " + hostAddress);
						}
					}
				}
				
				// External host and subnet
				_hosts.add(externalIp);
				_subnets.add("0.0.0.0/0");
				LOGGER.log(Level.INFO, "Adding new subnet: 0.0.0.0/0 address: " + externalIp);
			}
			catch (SocketException e)
			{
				LOGGER.log(Level.INFO, "Configuration failed please manually configure ipconfig.xml", e);
				System.exit(0);
			}
		}
		
		protected List<String> getSubnets()
		{
			return _subnets.isEmpty() ? Arrays.asList("0.0.0.0/0") : _subnets;
		}
		
		protected List<String> getHosts()
		{
			return _hosts.isEmpty() ? Arrays.asList("127.0.0.1") : _hosts;
		}
	}
}
