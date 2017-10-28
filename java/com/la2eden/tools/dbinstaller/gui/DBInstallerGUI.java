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
package com.la2eden.tools.dbinstaller.gui;

import com.la2eden.tools.dbinstaller.DBOutputInterface;
import com.la2eden.tools.dbinstaller.images.ImagesTable;

import javax.swing.*;
import java.awt.*;
import java.sql.Connection;

/**
 * @author mrTJO
 */
public class DBInstallerGUI extends JFrame implements DBOutputInterface
{
	private final JProgressBar _progBar;
	private final JTextArea _progArea;
	private final Connection _con;
	
	public DBInstallerGUI(Connection con)
	{
		super("Database Installer");
		setLayout(new BorderLayout());
		setDefaultLookAndFeelDecorated(true);
		setIconImage(ImagesTable.getImage("l2j.png").getImage());
		
		_con = con;
		
		final int width = 480;
		final int height = 360;
		final Dimension resolution = Toolkit.getDefaultToolkit().getScreenSize();
		
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds((resolution.width - width) / 2, (resolution.height - height) / 2, width, height);
		setResizable(false);
		
		_progBar = new JProgressBar();
		_progBar.setIndeterminate(true);
		add(_progBar, BorderLayout.PAGE_START);
		
		_progArea = new JTextArea();
		final JScrollPane scrollPane = new JScrollPane(_progArea);
		
		_progArea.setEditable(false);
		appendToProgressArea("Connected");
		
		add(scrollPane, BorderLayout.CENTER);
	}
	
	@Override
	public void setProgressIndeterminate(boolean value)
	{
		_progBar.setIndeterminate(value);
	}
	
	@Override
	public void setProgressMaximum(int maxValue)
	{
		_progBar.setMaximum(maxValue);
	}
	
	@Override
	public void setProgressValue(int value)
	{
		_progBar.setValue(value);
	}
	
	@Override
	public void appendToProgressArea(String text)
	{
		_progArea.append(text + System.getProperty("line.separator"));
		_progArea.setCaretPosition(_progArea.getDocument().getLength());
	}
	
	@Override
	public Connection getConnection()
	{
		return _con;
	}
	
	@Override
	public void setFrameVisible(boolean value)
	{
		setVisible(value);
	}
	
	@Override
	public int requestConfirm(String title, String message, int type)
	{
		return JOptionPane.showConfirmDialog(null, message, title, type);
	}
	
	@Override
	public void showMessage(String title, String message, int type)
	{
		JOptionPane.showMessageDialog(null, message, title, type);
	}
}
