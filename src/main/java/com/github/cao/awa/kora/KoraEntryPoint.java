package com.github.cao.awa.kora;

import com.github.cao.awa.kora.config.KoraLaunchConfig;
import com.github.cao.awa.kora.constant.KoraInformation;
import com.github.cao.awa.kora.entry.KoraKotlinEntryPoint;
import com.github.cao.awa.kora.entry.lib.KoraLibraryLoader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;

public class KoraEntryPoint {
    private static final Logger LOGGER = LogManager.getLogger("KoraEntryPoint");

    public static void main(String... args) {
        LOGGER.info("Starting Kora({}) server...", KoraInformation.VERSION);

        KoraLaunchConfig config = KoraLaunchConfig.createConfig(new File("config/launch.json"));
        KoraKotlinEntryPoint.printConfigs(config);
        if (config.isDefaultEntrypoint()) {
            KoraKotlinEntryPoint.entry(config);
        } else  {
            KoraLibraryLoader.loadJars();
            KoraKotlinEntryPoint.entryToDeclared(config, args);
        }
    }
}