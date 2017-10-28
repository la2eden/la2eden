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
package com.la2eden.gameserver.engines;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import com.la2eden.Config;
import com.la2eden.gameserver.datatables.SkillData;
import com.la2eden.gameserver.engines.items.DocumentItem;
import com.la2eden.gameserver.engines.skills.DocumentSkill;
import com.la2eden.gameserver.model.items.L2Item;
import com.la2eden.gameserver.model.skills.Skill;
import com.la2eden.util.file.filter.XMLFilter;

/**
 * @author mkizub
 */
public class DocumentEngine
{
	private static final Logger _log = Logger.getLogger(DocumentEngine.class.getName());
	
	private final List<File> _itemFiles = new ArrayList<>();
	private final List<File> _skillFiles = new ArrayList<>();
	
	public static DocumentEngine getInstance()
	{
		return SingletonHolder._instance;
	}
	
	protected DocumentEngine()
	{
		hashFiles("./datapack/stats/items", _itemFiles);
		if (Config.CUSTOM_ITEMS_LOAD)
		{
			hashFiles("./datapack/stats/items/custom", _itemFiles);
		}
		hashFiles("./datapack/stats/skills", _skillFiles);
		if (Config.CUSTOM_SKILLS_LOAD)
		{
			hashFiles("./datapack/stats/skills/custom", _skillFiles);
		}
	}
	
	private void hashFiles(String dirname, List<File> hash)
	{
		final File dir = new File(Config.DATAPACK_ROOT, dirname);
		if (!dir.exists())
		{
			_log.warning("Dir " + dir.getAbsolutePath() + " not exists");
			return;
		}
		
		final File[] files = dir.listFiles(new XMLFilter());
		if (files != null)
		{
			for (File f : files)
			{
				hash.add(f);
			}
		}
	}
	
	public List<Skill> loadSkills(File file)
	{
		if (file == null)
		{
			_log.warning("Skill file not found.");
			return null;
		}
		final DocumentSkill doc = new DocumentSkill(file);
		doc.parse();
		return doc.getSkills();
	}
	
	public void loadAllSkills(Map<Integer, Skill> allSkills)
	{
		int count = 0;
		for (File file : _skillFiles)
		{
			final List<Skill> s = loadSkills(file);
			if (s == null)
			{
				continue;
			}
			for (Skill skill : s)
			{
				allSkills.put(SkillData.getSkillHashCode(skill), skill);
				count++;
			}
		}
		_log.info(getClass().getSimpleName() + ": Loaded " + count + " Skill templates from XML files.");
	}
	
	/**
	 * Return created items
	 * @return List of {@link L2Item}
	 */
	public List<L2Item> loadItems()
	{
		final List<L2Item> list = new ArrayList<>();
		for (File f : _itemFiles)
		{
			final DocumentItem document = new DocumentItem(f);
			document.parse();
			list.addAll(document.getItemList());
		}
		return list;
	}
	
	private static class SingletonHolder
	{
		protected static final DocumentEngine _instance = new DocumentEngine();
	}
}
