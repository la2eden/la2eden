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
package com.la2eden.tools.dbinstaller;

import com.la2eden.tools.dbinstaller.util.mysql.DBDumper;
import com.la2eden.tools.dbinstaller.util.mysql.ScriptExecutor;

import javax.swing.*;
import java.io.File;
import java.sql.SQLException;

/**
 * @author mrTJO
 */
public class RunTasks extends Thread
{
	DBOutputInterface _frame;
	String _db;
	String _sqlDir;

	public RunTasks(DBOutputInterface frame, String db, String sqlDir)
	{
		_frame = frame;
		_db = db;
		_sqlDir = sqlDir;
	}

	@Override
	public void run()
	{
		new DBDumper(_frame, _db);
		final ScriptExecutor exec = new ScriptExecutor(_frame);

		_frame.appendToProgressArea("Installing database content...");
		exec.execSqlBatch(new File(_sqlDir));

		_frame.appendToProgressArea(System.getProperty("line.separator") + "Database installation complete!");

		try
		{
			_frame.getConnection().close();
		}
		catch (SQLException e)
		{
			JOptionPane.showMessageDialog(null, "Cannot close MySQL Connection: " + e.getMessage(), "Connection Error", JOptionPane.ERROR_MESSAGE);
		}

		_frame.setFrameVisible(false);
		_frame.showMessage("DB Installer", "Database installation complete!", JOptionPane.INFORMATION_MESSAGE);
		System.exit(0);
	}
}
