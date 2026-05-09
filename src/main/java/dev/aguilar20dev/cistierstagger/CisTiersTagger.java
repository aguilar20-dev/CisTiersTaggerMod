package dev.aguilar20dev.cistierstagger;

import dev.aguilar20dev.cistierstagger.command.CisTiersCommand;
import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CisTiersTagger implements ModInitializer {
  public static final String MOD_ID = "cistierstagger";
  public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

  @Override
  public void onInitialize() {
    LOGGER.info("CisTiersTagger initialize");
    CisTiersCommand.register();
  }

}
