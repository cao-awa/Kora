package com.github.cao.awa.kora.entrypoint.lib

import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import java.io.File
import java.net.URL
import java.net.URLClassLoader
import java.util.jar.JarFile
import kotlin.sequences.forEach

object KoraLibraryLoader {
    private val LOGGER: Logger = LogManager.getLogger("KoraLibraryLoader")
    var classLoader: URLClassLoader? = null

    @JvmStatic
    fun loadJars() {
        val customLoader = addJarToClassLoader(collectJars(File("libs"), mutableListOf()))

        this.classLoader = customLoader
        Thread.currentThread().contextClassLoader = customLoader
    }

    fun collectJars(file: File, jarFiles: MutableList<File>): MutableList<File> {
        val files = file.listFiles()
        // Only collect libraries when file list present.
        if (files != null) {
            for (file in files) {
                // If is file and extension is .jar, then add to collection.
                if (file.isFile && file.extension == "jar") {
                    jarFiles.add(file)
                } else if (file.isDirectory) {
                    // If file is a directory, load child jars.
                    collectJars(file, jarFiles)
                }
            }
        }
        return jarFiles
    }

    fun addJarToClassLoader(jarFiles: MutableList<File>): URLClassLoader {
        // Map file collection to URL array.
        val urls: Array<URL> = Array(jarFiles.size) { index ->
            jarFiles[index].toURI().toURL()
        }

        // Use custom URL class loader, because Java 9+ default AppClassLoader cannot attach new classes.
        val customLoader = URLClassLoader(urls, ClassLoader.getSystemClassLoader())

        // Load classes.
        for (jarFile in jarFiles) {
            addJarToClassLoader(jarFile, customLoader)
        }

        return customLoader
    }

    fun addJarToClassLoader(jarFile: File, classLoader: ClassLoader) {
        LOGGER.info("Loading library file: {}", jarFile.absolutePath)

        // Add every class in the jar.
        JarFile(jarFile).use { jar ->
            jar.entries().asSequence()
                .filter {
                    it.name.endsWith(".class")
                }
                .map { entry ->
                    entry.name.removeSuffix(".class").replace('/', '.')
                }
                .forEach { className ->
                    try {
                        classLoader.loadClass(className)
                        LOGGER.debug("Loaded class '{}'", className.replace(".", "/") + ".class")
                    } catch (e: NoClassDefFoundError) {
                        LOGGER.error("Failed to load class '{}'", className, e)
                    }
                }
        }
    }
}