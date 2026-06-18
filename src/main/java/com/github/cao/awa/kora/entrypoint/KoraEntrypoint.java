package com.github.cao.awa.kora.entrypoint;

import com.github.cao.awa.kora.command.KoraCommandExecutor;
import com.github.cao.awa.kora.entrypoint.exception.KoraEntrypointStageFailedException;
import com.github.cao.awa.kora.launch.config.KoraLaunchConfig;
import com.github.cao.awa.kora.constant.KoraInformation;
import com.github.cao.awa.kora.entrypoint.lib.KoraLibraryLoader;
import com.github.cao.awa.kora.locker.KoraEntrypointLocker;
import com.github.cao.awa.kora.plugin.KoraPluginDependenciesManager;
import com.github.cao.awa.kora.status.KoraStatus;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.util.Scanner;

public class KoraEntrypoint {
    private static final Logger LOGGER = LogManager.getLogger("KoraEntryPoint");
    public static final KoraPluginDependenciesManager DEPENDENCIES_MANAGER = new KoraPluginDependenciesManager();
    private static String[] launchArgs = new String[0];
    public static final long START_TIME = System.currentTimeMillis();
    private static final KoraEntrypointLocker LOCKER = new KoraEntrypointLocker();

    public static void main(String... args) {
        launchArgs = args;
        LOGGER.info("Starting Kora({}) server...",
                    KoraInformation.VERSION
        );

        Scanner scanner = new Scanner(System.in);
        if (launch(args)) {
            while (KoraStatus.isRunning()) {
                if (KoraStatus.isReloading()) {
                    DEPENDENCIES_MANAGER.getCleaners()
                                        .forEach((name, cleaner) -> {
                                            LOGGER.info("Clearing resources for '{}'",
                                                        name
                                            );
                                            cleaner.invoke();
                                        });

                    LOGGER.info("Cleaning cleaners...");
                    DEPENDENCIES_MANAGER.clearCleaners();

                    KoraEntrypoint.reload();
                }

                if (scanner.hasNextLine()) {
                String command = scanner.nextLine();
                KoraCommandExecutor.executeCommand(command);
                } else {
                    LOCKER.waitFor();
                }
            }
        }

        LOGGER.info("Stopping Kora");
        LOGGER.info("Kora stopped");
        System.exit(0);
    }

    private static boolean launch(String[] args) {
        LOGGER.info("Kora running on directory '{}'",
                    new File("").getAbsolutePath()
        );

        try {
            KoraLaunchConfig config = KoraLaunchConfig.createConfig(new File("configs/launch.json"));
            KoraKotlinEntrypoint.printConfigs(config);

            if (KoraStatus.isReloading()) {
                KoraKotlinEntrypoint.unloadPlugins();
            }

            KoraLibraryLoader.loadJars();
            KoraKotlinEntrypoint.entryToDeclared(
                    config,
                    args
            );

            return true;
        } catch (KoraEntrypointStageFailedException ex) {
            LOGGER.error("Failed to startup Kora server on entrypoint stage: '{}'",
                         ex.stage,
                         ex.cause
            );
        }

        return false;
    }

    public static void reload() {
        LOGGER.info("Reloading Kora({}) server...",
                    KoraInformation.VERSION
        );

        LOCKER.offer();

        if (KoraStatus.isReloading()) {
            KoraStatus.completeReload();
        }

        launch(launchArgs);
    }
}