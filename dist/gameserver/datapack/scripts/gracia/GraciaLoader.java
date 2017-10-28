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
package gracia;

import gracia.AI.EnergySeeds;
import gracia.AI.Lindvior;
import gracia.AI.Maguen;
import gracia.AI.NPC.AbyssGaze.AbyssGaze;
import gracia.AI.NPC.EkimusMouth.EkimusMouth;
import gracia.AI.NPC.FortuneTelling.FortuneTelling;
import gracia.AI.NPC.GeneralDilios.GeneralDilios;
import gracia.AI.NPC.Lekon.Lekon;
import gracia.AI.NPC.Nemo.Nemo;
import gracia.AI.NPC.Nottingale.Nottingale;
import gracia.AI.NPC.Seyo.Seyo;
import gracia.AI.NPC.ZealotOfShilen.ZealotOfShilen;
import gracia.AI.SeedOfAnnihilation.SeedOfAnnihilation;
import gracia.AI.StarStones;
import gracia.instances.HallOfErosionAttack.HallOfErosionAttack;
import gracia.instances.HallOfErosionDefence.HallOfErosionDefence;
import gracia.instances.HallOfSufferingAttack.HallOfSufferingAttack;
import gracia.instances.HallOfSufferingDefence.HallOfSufferingDefence;
import gracia.instances.HeartInfinityAttack.HeartInfinityAttack;
import gracia.instances.HeartInfinityDefence.HeartInfinityDefence;
import gracia.instances.SecretArea.SecretArea;
import gracia.instances.SeedOfDestruction.SeedOfDestruction;
import gracia.vehicles.AirShipGludioGracia.AirShipGludioGracia;
import gracia.vehicles.KeucereusNorthController.KeucereusNorthController;
import gracia.vehicles.KeucereusSouthController.KeucereusSouthController;
import gracia.vehicles.SoDController.SoDController;
import gracia.vehicles.SoIController.SoIController;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Gracia class-loader.
 * @author Pandragon
 */
public final class GraciaLoader
{
	private static final Logger _log = Logger.getLogger(GraciaLoader.class.getName());
	
	private static final Class<?>[] SCRIPTS =
	{
		// AIs
		EnergySeeds.class,
		Lindvior.class,
		Maguen.class,
		StarStones.class,
		// NPCs
		AbyssGaze.class,
		EkimusMouth.class,
		FortuneTelling.class,
		GeneralDilios.class,
		Lekon.class,
		Nemo.class,
		Nottingale.class,
		Seyo.class,
		ZealotOfShilen.class,
		// Seed of Annihilation
		SeedOfAnnihilation.class,
		// Instances
		SecretArea.class,
		SeedOfDestruction.class,
		HallOfErosionAttack.class,
		HallOfErosionDefence.class,
		HallOfSufferingAttack.class,
		HallOfSufferingDefence.class,
		HeartInfinityAttack.class,
		HeartInfinityDefence.class,
		// Vehicles
		AirShipGludioGracia.class,
		KeucereusNorthController.class,
		KeucereusSouthController.class,
		SoIController.class,
		SoDController.class,
	};
	
	public static void main(String[] args)
	{
		_log.info(GraciaLoader.class.getSimpleName() + ": Loading Gracia related scripts.");
		for (Class<?> script : SCRIPTS)
		{
			try
			{
				script.newInstance();
			}
			catch (Exception e)
			{
				_log.log(Level.SEVERE, GraciaLoader.class.getSimpleName() + ": Failed loading " + script.getSimpleName() + ":", e);
			}
		}
	}
}
