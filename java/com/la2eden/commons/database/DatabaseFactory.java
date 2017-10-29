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

import java.beans.PropertyVetoException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.logging.Logger;

import com.la2eden.Config;
import com.mchange.v2.c3p0.ComboPooledDataSource;

/**
 * Database Factory implementation.
 * @author Zoey76
 */
public class DatabaseFactory
{
	private static final Logger _log = Logger.getLogger(DatabaseFactory.class.getName());
	
	private final ComboPooledDataSource _dataSource;
	
	public DatabaseFactory()
	{
		if (Config.DATABASE_MAX_CONNECTIONS < 2)
		{
			Config.DATABASE_MAX_CONNECTIONS = 2;
			_log.warning("A minimum of 2 connections are required.");
		}
		
		_dataSource = new ComboPooledDataSource();
		_dataSource.setAutoCommitOnClose(true);
		
		_dataSource.setInitialPoolSize(10);
		_dataSource.setMinPoolSize(10);
		_dataSource.setMaxPoolSize(Math.max(10, Config.DATABASE_MAX_CONNECTIONS));
		
		_dataSource.setAcquireRetryAttempts(0); // try to obtain connections indefinitely (0 = never quit)
		_dataSource.setAcquireRetryDelay(500); // 500 milliseconds wait before try to acquire connection again
		_dataSource.setCheckoutTimeout(0); // 0 = wait indefinitely for new connection if pool is exhausted
		_dataSource.setAcquireIncrement(5); // if pool is exhausted, get 5 more connections at a time cause there is
		// a "long" delay on acquire connection so taking more than one connection at once will make connection pooling more effective.
		
		// this "connection_test_table" is automatically created if not already there
		_dataSource.setAutomaticTestTable("connection_test_table");
		_dataSource.setTestConnectionOnCheckin(false);
		
		// testing OnCheckin used with IdleConnectionTestPeriod is faster than testing on checkout
		
		_dataSource.setIdleConnectionTestPeriod(3600); // test idle connection every 60 sec
		_dataSource.setMaxIdleTime(Config.DATABASE_MAX_IDLE_TIME); // 0 = idle connections never expire
		// *THANKS* to connection testing configured above but I prefer to disconnect all connections not used for more than 1 hour
		
		// enables statement caching, there is a "semi-bug" in c3p0 0.9.0 but in 0.9.0.2 and later it's fixed
		_dataSource.setMaxStatementsPerConnection(100);
		
		_dataSource.setBreakAfterAcquireFailure(false); // never fail if any way possible setting this to true will make c3p0 "crash"
		// and refuse to work till restart thus making acquire errors "FATAL" ... we don't want that it should be possible to recover
		
		try
		{
			_dataSource.setDriverClass(Config.DATABASE_DRIVER);
		}
		catch (PropertyVetoException e)
		{
			e.printStackTrace();
		}
		_dataSource.setJdbcUrl(Config.DATABASE_URL);
		_dataSource.setUser(Config.DATABASE_LOGIN);
		_dataSource.setPassword(Config.DATABASE_PASSWORD);
		
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
