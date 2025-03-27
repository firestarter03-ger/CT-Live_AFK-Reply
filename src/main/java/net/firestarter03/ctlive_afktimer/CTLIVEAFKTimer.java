package net.firestarter03.ctlive_afktimer;

import net.fabricmc.api.ModInitializer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CTLIVEAFKTimer implements ModInitializer {
	public static final String MOD_ID = "ctlive_afktimer";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		LOGGER.info("AFK-Reply started!");
	}
}