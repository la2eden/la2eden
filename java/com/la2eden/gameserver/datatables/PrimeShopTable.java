package com.la2eden.gameserver.datatables;

import com.la2eden.Config;
import com.la2eden.commons.database.DatabaseFactory;
import com.la2eden.gameserver.model.actor.instance.L2PcInstance;
import com.la2eden.gameserver.model.items.L2Item;
import com.la2eden.gameserver.network.serverpackets.ExBrBuyProduct;
import com.la2eden.gameserver.network.serverpackets.ExBrProductInfo;
import com.la2eden.gameserver.util.Util;
import com.la2eden.util.data.xml.IXmlReader;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author fissban
 * @author Enkel
 */
public class PrimeShopTable implements IXmlReader
{
    private static final Logger _log = Logger.getLogger(PrimeShopTable.class.getName());
    private static final String INSERT_ITEM = "INSERT INTO primeshop (brId, stock) VALUES (?,?)";
    private static final String GET_STOCK = "SELECT stock FROM primeshop WHERE brId=?";
    private static final String UPDATE_STOCK_COUNT = "UPDATE primeshop SET stock=? WHERE brId=?";
    private static final String GET_SOLD_COUNT = "SELECT sold FROM primeshop WHERE brId=?";
    private static final String UPDATE_SOLD_COUNT = "UPDATE primeshop SET sold=? WHERE brId=?";
    private static final String REMOVE_ITEM = "DELETE FROM primeshop WHERE brId=?";
    private static final String ITEM_EXISTS = "SELECT * FROM primeshop WHERE brId=?";

    private final Map<Integer, PrimeShopItem> _primeItems = new HashMap<>();

    public final int BR_BUY_SUCCESS = 1;
    public final int BR_BUY_LACK_OF_POINT = -1;
    public final int BR_BUY_INVALID_PRODUCT = -2;
    public final int BR_BUY_USER_CANCEL = -3;
    public final int BR_BUY_INVENTROY_OVERFLOW = -4;
    public final int BR_BUY_CLOSED_PRODUCT = -5;
    public final int BR_BUY_SERVER_ERROR = -6;
    public final int BR_BUY_BEFORE_SALE_DATE = -7;
    public final int BR_BUY_AFTER_SALE_DATE = -8;
    public final int BR_BUY_INVALID_USER = -9;
    public final int BR_BUY_INVALID_ITEM = -10;
    public final int BR_BUY_INVALID_USER_STATE = -11;
    public final int BR_BUY_NOT_DAY_OF_WEEK = -12;
    public final int BR_BUY_NOT_TIME_OF_DAY = -13;
    public final int BR_BUY_SOLD_OUT = -14;

    public PrimeShopTable()
    {
        // code
    }

    @Override
    public void load()
    {
        _primeItems.clear();
        parseDatapackFile("datapack/PrimeShop.xml");
    }

    public final void reload()
    {
        _primeItems.clear();
        load();
    }

    @Override
    public void parseDocument(Document doc)
    {
        for (Node a = doc.getFirstChild(); a != null; a = a.getNextSibling())
        {
            if (!"list".equalsIgnoreCase(a.getNodeName()))
            {
                continue;
            }
            for (Node b = a.getFirstChild(); b != null; b = b.getNextSibling())
            {
                if (!"item".equalsIgnoreCase(b.getNodeName()))
                {
                    continue;
                }
                NamedNodeMap attrs = b.getAttributes();

                int type = 0;

                Node att = attrs.getNamedItem("brId");
                if (att == null)
                {
                    _log.severe("[PrimeShop]: Missing brId, skipping");
                    continue;
                }
                final int brId = Integer.parseInt(att.getNodeValue());

                att = attrs.getNamedItem("itemId");
                if (att == null)
                {
                    _log.severe("[PrimeShop]: Missing itemId, skipping");
                    continue;
                }
                final int itemId = Integer.parseInt(att.getNodeValue());

                att = attrs.getNamedItem("cat");
                if (att == null)
                {
                    _log.severe("[PrimeShop]: Missing category, skipping");
                    continue;
                }
                final int cat = Integer.parseInt(att.getNodeValue());

                att = attrs.getNamedItem("price");
                if (att == null)
                {
                    _log.severe("[PrimeShop]: Missing price, skipping");
                    continue;
                }
                final int price = Integer.parseInt(att.getNodeValue());

                att = attrs.getNamedItem("count");
                if (att == null)
                {
                    _log.severe("[PrimeShop]: Missing count, skipping");
                    continue;
                }
                final int count = Integer.parseInt(att.getNodeValue());

                att = attrs.getNamedItem("event");
                if (att == null)
                {
                    _log.severe("[PrimeShop]: Missing event, skipping");
                    continue;
                }
                final boolean event = Boolean.parseBoolean(att.getNodeValue());

                att = attrs.getNamedItem("best");
                if (att == null)
                {
                    _log.severe("[PrimeShop]: Missing best, skipping");
                    continue;
                }
                final boolean best = Boolean.parseBoolean(att.getNodeValue());

                if (event)
                {
                    type++;
                }
                if (best)
                {
                    type += 2;
                }
                L2Item item = ItemTable.getInstance().getTemplate(itemId);
                if (item == null)
                {
                    _log.severe("[PrimeShop]: Item template null");
                }
                else
                {
                    att = attrs.getNamedItem("sale_start_date");
                    if (att == null)
                    {
                        _log.severe("[PrimeShop]: Missing item start date, skipping");
                        continue;
                    }
                    final String sale_start_date = att.getNodeValue();

                    att = attrs.getNamedItem("sale_end_date");
                    if (att == null)
                    {
                        _log.severe("[PrimeShop]: Missing item end date, skipping");
                        continue;
                    }
                    final String sale_end_date = att.getNodeValue();

                    _primeItems.put(brId, new PrimeShopItem(itemId, cat, price, count, item.getWeight(), item.isTradeable(), type, sale_start_date, sale_end_date));
                }
            }
        }

        if (_primeItems.size() > 0)
        {
            _log.info("[PrimeShop]: Loaded " + _primeItems.size() + " items");
        }
    }

    public void buyItem(L2PcInstance player, int brId, int count)
    {
        if ((count < 1) || (count > 99))
        {
            Util.handleIllegalPlayerAction(player, "Player " + player.getName() + " tried to buy invalid itemcount [" + count + "] from Prime Shop", Config.DEFAULT_PUNISH);
            return;
        }

        int Stock = PrimeShopHelper.getSoldCount(brId);
        int MaxStock = PrimeShopHelper.getMaxStock(brId);

        if (MaxStock != 0) // You can only buy if there is a stock of it
        {
            if ((MaxStock - Stock) < count)
            {
                player.sendPacket(new ExBrBuyProduct(-14)); // Can't buy more than we can sell
                return;
            }
        }

        if (_primeItems.containsKey(brId))
        {
            PrimeShopItem item = _primeItems.get(brId);

            long points = player.getPrimePoints();

            // (Fissban text)
            // TODO hace falta poner las 2 condiciones? quizas solo con la segunda y sin el && ultimo deberia funcionar
            if (points >= (item.getPrimeItemPrice() * count))
            {
                L2Item dummy = ItemTable.getInstance().getTemplate(item.getPrimeItemId());
                if (dummy != null)
                {
                    int weight = item.getPrimeItemCount() * item.getPrimeWeight() * count;

                    if (player.getInventory().validateWeight(weight))
                    {
                        int slots;

                        if (dummy.isStackable())
                        {
                            slots = 1;
                        }
                        else
                        {
                            slots = item.getPrimeItemCount() * count;
                        }
                        if (player.getInventory().validateCapacity(slots))
                        {
                            if (player.addItem("PrimeShop", item.getPrimeItemId(), count * item.getPrimeItemCount(), player, true) != null)
                            {
                                player.sendPacket(new ExBrBuyProduct(1));

                                player.setPrimePoints(player.getPrimePoints() - (item.getPrimeItemPrice() * count));

                                if (MaxStock != 0)
                                {
                                    PrimeShopHelper.setSoldCount(brId, Stock, count);
                                }
                            }
                            else
                            {
                                player.sendPacket(new ExBrBuyProduct(-6));
                            }
                        }
                        else
                        {
                            player.sendPacket(new ExBrBuyProduct(-4));
                        }
                    }
                    else
                    {
                        player.sendPacket(new ExBrBuyProduct(-4));
                    }
                }
                else
                {
                    player.sendPacket(new ExBrBuyProduct(-6));
                }
            }
            else
            {
                player.sendPacket(new ExBrBuyProduct(-1));
            }
        }
        else
        {
            player.sendPacket(new ExBrBuyProduct(-2));
            Util.handleIllegalPlayerAction(player, "Player " + player.getName() + " tried to buy invalid brId from Prime", Config.DEFAULT_PUNISH);
        }
    }

    public void showProductInfo(L2PcInstance player, int itemId)
    {
        if ((player == null) || (itemId == 0))
        {
            return;
        }
        if (_primeItems.containsKey(itemId))
        {
            PrimeShopItem item = _primeItems.get(itemId);

            player.sendPacket(new ExBrProductInfo(itemId, item));
        }
    }

    public Map<Integer, PrimeShopItem> getPrimeItems()
    {
        return _primeItems;
    }

    public class PrimeShopItem
    {
        int _itemId;
        int _category;
        int _price;
        int _count;
        int _weight;
        int _tradable;
        int _type;

        long _sale_start_date;
        long _sale_end_date;

        int _startHour;
        int _endHour;
        int _startMin;
        int _endMin;

        private PrimeShopItem(int itemId, int category, int price, int count, int weight, boolean tradable, int type, String sale_start_date, String sale_end_date)
        {
            new Date().getTime();

            _itemId = itemId;
            _category = category;
            _price = price;
            _weight = weight;
            _count = count;
            _tradable = (tradable ? 1 : 0);
            _type = type;

            DateFormat df = new SimpleDateFormat("yyyy.MM.dd HH:mm");
            try
            {
                Date time_start = df.parse(sale_start_date);
                Date time_end = df.parse(sale_end_date);
                Calendar calendar = Calendar.getInstance();

                calendar.setTime(time_start);
                _sale_start_date = time_start.getTime();
                _startHour = calendar.get(Calendar.HOUR_OF_DAY);
                _startMin = calendar.get(Calendar.MINUTE);

                calendar.setTime(time_end);
                _sale_end_date = time_end.getTime();
                _endHour = calendar.get(Calendar.HOUR_OF_DAY);
                _endMin = calendar.get(Calendar.MINUTE);
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }

        public int getPrimeItemId()
        {
            return _itemId;
        }

        public int getPrimeItemCat()
        {
            return _category;
        }

        public int getPrimeItemPrice()
        {
            return _price;
        }

        public int getPrimeItemCount()
        {
            return _count;
        }

        public int getPrimeWeight()
        {
            return _weight;
        }

        public int getPrimeTradable()
        {
            return _tradable;
        }

        public long sale_start_date()
        {
            return _sale_start_date;
        }

        public long sale_end_date()
        {
            return _sale_end_date;
        }

        public int getStartHour()
        {
            return _startHour;
        }

        public int getStartMin()
        {
            return _startMin;
        }

        public int getEndHour()
        {
            return _endHour;
        }

        public int getEndMin()
        {
            return _endMin;
        }

        public int getPrimeType()
        {
            return _type;
        }
    }

    public static class PrimeShopHelper
    {
        public static int getMaxStock(int brId)
        {
            if (!hasPrimeItem(brId)) {
                _log.log(Level.WARNING, "[PrimeShop]: Trying to get stock count from an invalid product (" + brId + ")");
                return 0;
            }

            int maxStock = 0;

            try (Connection con = DatabaseFactory.getInstance().getConnection();
                 PreparedStatement statement = con.prepareStatement(GET_STOCK))
            {
                statement.setInt(1, brId);
                try (ResultSet rset = statement.executeQuery())
                {
                    while (rset.next())
                    {
                        maxStock = rset.getInt("total");
                    }
                }
            }
            catch (Exception e)
            {
                _log.log(Level.WARNING, "[PrimeShop]: Error while loading stock for product: " + brId + e, e);
            }

            return maxStock;
        }

        public static boolean setMaxStock(int brId, int total)
        {
            boolean ok = false;

            if (!hasPrimeItem(brId)) {
                _log.log(Level.WARNING, "[PrimeShop]: Trying to set new stock count to an invalid product (" + brId + ")");
                return false;
            }

            try (Connection con = DatabaseFactory.getInstance().getConnection();
                 PreparedStatement statement = con.prepareStatement(UPDATE_STOCK_COUNT))
            {
                statement.setInt(1, total);
                statement.setInt(2, brId);
                ok = statement.execute();
            }
            catch (Exception e)
            {
                _log.log(Level.WARNING, "[PrimeShop]: Error while updating product: " + brId + e, e);
            }

            return ok;
        }

        public static int getSoldCount(int brId)
        {
            if (!hasPrimeItem(brId)) {
                _log.log(Level.WARNING, "[PrimeShop]: Trying to get sold count from an invalid product (" + brId + ")");

                return 0;
            }

            int getActualStock = 0;

            try (Connection con = DatabaseFactory.getInstance().getConnection();
                 PreparedStatement statement = con.prepareStatement(GET_SOLD_COUNT))
            {
                statement.setInt(1, brId);
                try (ResultSet rset = statement.executeQuery())
                {
                    while (rset.next())
                    {
                        getActualStock = rset.getInt("sold");
                    }
                }
            }
            catch (Exception e)
            {
                _log.log(Level.WARNING, "[PrimeShop]: Error while loading current stock for product: " + brId + e, e);
            }

            return getActualStock;
        }

        public static boolean setSoldCount(int brId, int total, int count)
        {
            boolean ok = false;
            if (!hasPrimeItem(brId)) {
                _log.log(Level.WARNING, "[PrimeShop]: Trying to set new sold count to an invalid product (" + brId + ")");
                return false;
            }

            total += count;

            try (Connection con = DatabaseFactory.getInstance().getConnection();
                 PreparedStatement statement = con.prepareStatement(UPDATE_SOLD_COUNT))
            {
                statement.setInt(1, total);
                statement.setInt(2, brId);
                ok = statement.execute();
            }
            catch (Exception e)
            {
                _log.log(Level.WARNING, "[PrimeShop]: Error while updating product: " + brId + e, e);
            }

            return ok;
        }

        public static boolean addPrimeItem(int brId, int total)
        {
            boolean added = false;

            if (!hasPrimeItem(brId)) {
                try (Connection con = DatabaseFactory.getInstance().getConnection();
                     PreparedStatement statement = con.prepareStatement(INSERT_ITEM))
                {
                    statement.setInt(1, brId);
                    statement.setInt(2, total);
                    added = statement.execute();
                }
                catch (Exception e)
                {
                    _log.log(Level.WARNING, "[PrimeShop]: Error while inserting product: " + brId + e, e);
                }
            } else {
                _log.log(Level.WARNING, "[PrimeShop]: Trying to add product (" + brId + ") that already exists");
            }

            return added;
        }

        public static boolean removePrimeItem(int brId)
        {
            boolean removed = false;

            if (!hasPrimeItem(brId)) {
                _log.log(Level.WARNING, "[PrimeShop]: Product (" + brId + ") cannot be removed from primeshop because it's not there!");
                return false;
            }

            try (Connection con = DatabaseFactory.getInstance().getConnection();
                 PreparedStatement statement = con.prepareStatement(REMOVE_ITEM))
            {
                statement.setInt(1, brId);
                removed = statement.execute();
            }
            catch (Exception e)
            {
                _log.log(Level.WARNING, "[PrimeShop]: Error while removing product: " + brId + e, e);
            }

            return removed;
        }

        public static boolean hasPrimeItem(int brId)
        {
            boolean exists = false;

            try (Connection con = DatabaseFactory.getInstance().getConnection();
                 PreparedStatement statement = con.prepareStatement(ITEM_EXISTS))
            {
                statement.setInt(1, brId);
                exists = statement.execute();
            }
            catch (Exception e)
            {
                _log.log(Level.WARNING, "[PrimeShop]: Error while checking product: " + brId + e, e);
            }

            return exists;
        }
    }

    public PrimeShopItem getProduct(int id)
    {
        return _primeItems.get(id);
    }

    public static PrimeShopTable getInstance()
    {
        return SingletonHolder._instance;
    }

    private static class SingletonHolder
    {
        protected static final PrimeShopTable _instance = new PrimeShopTable();
    }
}
