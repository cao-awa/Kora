package com.github.cao.awa.kora;

import com.github.cao.awa.kora.launch.config.KoraLaunchConfig;
import com.github.cao.awa.kora.constant.KoraInformation;
import com.github.cao.awa.kora.entrypoint.KoraKotlinEntrypoint;
import com.github.cao.awa.kora.entrypoint.lib.KoraLibraryLoader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;

public class KoraEntrypoint {
    private static final Logger LOGGER = LogManager.getLogger("KoraEntryPoint");

    public static void main(String... args) {
        LOGGER.info("Starting Kora({}) server...", KoraInformation.VERSION);
        LOGGER.info("Kora running on directory '{}'", new File("").getAbsolutePath());

        KoraLaunchConfig config = KoraLaunchConfig.createConfig(new File("configs/launch.json"));
        KoraKotlinEntrypoint.printConfigs(config);
        if (config.isDefaultEntrypoint()) {
            KoraKotlinEntrypoint.entry(config);
        } else  {
            KoraLibraryLoader.loadJars();
            KoraKotlinEntrypoint.entryToDeclared(config, args);
        }
    }
}