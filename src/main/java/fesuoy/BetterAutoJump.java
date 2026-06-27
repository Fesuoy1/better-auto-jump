package fesuoy;

import fesuoy.config.BetterAutoJumpConfig;
import net.fabricmc.api.ModInitializer;

import net.minecraft.resources.Identifier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BetterAutoJump implements ModInitializer {
	public static final String MOD_ID = "better-auto-jump";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		BetterAutoJumpConfig.getInstance();
		//LOGGER.info("Better Auto-Jump initialized");
	}
}
