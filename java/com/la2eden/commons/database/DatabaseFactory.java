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
package com.la2eden.commons.database;

import com.la2eden.Config;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.logging.Logger;

/**
 * Database Factory implementation.
 * @author Zoey76
 */
public class DatabaseFactory
{
	private static final Logger _log = Logger.getLogger(DatabaseFactory.class.getName());

	private final HikariDataSource _dataSource;

	public DatabaseFactory()
	{
		if (Config.DATABASE_MAX_CONNECTIONS < 2)
		{
			Config.DATABASE_MAX_CONNECTIONS = 2;
			_log.warning("A minimum of 2 connections are required.");
		}

		// Hello Hikari!
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(Config.DATABASE_URL);
        config.setUsername(Config.DATABASE_PASSWORD);
        config.setPassword(Config.DATABASE_PASSWORD);
        config.addDataSourceProperty("cachePrepStmts", "true");
        config.addDataSourceProperty("prepStmtCacheSize", "250");
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
        config.addDataSourceProperty("useServerPrepStmts", "true");
        config.addDataSourceProperty("useLocalSessionState", "true");
        config.addDataSourceProperty("useLocalTransactionState", "true");
        config.addDataSourceProperty("rewriteBatchedStatements", "true");
        config.addDataSourceProperty("cacheServerConfiguration", "true");
        config.addDataSourceProperty("cacheResultSetMetadata", "true");
        config.addDataSourceProperty("maintainTimeStats", "false");

		_dataSource = new HikariDataSource(config);
		_dataSource.setAutoCommit(true);
		_dataSource.setMinimumIdle(10);

		_dataSource.setValidationTimeout(500); // 500 milliseconds wait before try to acquire connection again
		_dataSource.setConnectionTimeout(0); // 0 = wait indefinitely for new connection if pool is exhausted
		_dataSource.setMaximumPoolSize(Config.DATABASE_MAX_CONNECTIONS);
		_dataSource.setIdleTimeout(Config.DATABASE_MAX_IDLE_TIME); // 0 = idle connections never expire
        _dataSource.setDriverClassName(Config.DATABASE_DRIVER);

		/* Test the connection */
		try
		{
			_dataSource.getConnection().close();
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}
	}

	public Connection getConnection()
	{
		Connection con = null;
		while (con == null)
		{
			try
			{
				con = _dataSource.getConnection();
			}
			catch (SQLException e)
			{
				_log.warning(getClass().getSimpleName() + ": Unable to get a connection: " + e.getMessage());
			}
		}
		return con;
	}

	public void close()
	{
		try
		{
			_dataSource.close();
		}
		catch (Exception e)
		{
			_log.info(e.getMessage());
		}
	}

	public static DatabaseFactory getInstance()
	{
		return SingletonHolder.INSTANCE;
	}

	private static class SingletonHolder
	{
		protected static final DatabaseFactory INSTANCE = new DatabaseFactory();
	}
}
