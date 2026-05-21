package com.github.cao.awa.kora.entry.lib

import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import java.io.File
import java.net.URL
import java.net.URLClassLoader
import java.util.jar.JarFile
import kotlin.sequences.forEach

object KoraLibraryLoader {
    private val LOGGER: Logger = LogManager.getLogger("KoraLibraryLoader")
    private var urlClassLoader: URLClassLoader? = null
    val classLoader: URLClassLoader
        get() = this.urlClassLoader!!

    @JvmStatic
    fun loadJars() {
        val customLoader = addJarToSystemClassLoader(collectJars(File("libs"), mutableListOf()))

        this.urlClassLoader = customLoader
        Thread.currentThread().contextClassLoader = customLoader
    }

    fun collectJars(file: File, jarFiles: MutableList<File>): MutableList<File> {
        for (file in file.listFiles()) {
            if (file.isFile && file.extension == "jar") {
                if (file.isFile) {
                    jarFiles.add(file)
                }
            } else if (file.isDirectory) {
                collectJars(file, jarFiles)
            }
        }

        return jarFiles
    }

    fun addJarToSystemClassLoader(jarFiles: MutableList<File>): URLClassLoader {
        val urls: Array<URL> = Array(jarFiles.size) { index ->
            jarFiles[index].toURI().toURL()
        }
        val customLoader = URLClassLoader(urls, ClassLoader.getSystemClassLoader())

        for (jarFile in jarFiles) {
            addJarToSystemClassLoader(jarFile, customLoader)
        }

        return customLoader
    }

    fun addJarToSystemClassLoader(jarFile: File, classLoader: ClassLoader) {
        require(jarFile.exists() && jarFile.isFile) { "JAR file not found: ${jarFile.absolutePath}" }

        LOGGER.info("Loading library file: {}", jarFile.absolutePath)

        JarFile(jarFile).use { jar ->
            jar.entries().asSequence()
                .filter {
                    it.name != "module-info.class" && it.name.endsWith(".class")
                }
                .map { entry ->
                    entry.name.removeSuffix(".class").replace('/', '.')
                }
                .forEach { className ->
                    try {
                        classLoader.loadClass(className)
                        LOGGER.debug("Loaded class '{}'", className.replace(".", "/") +".class")
                    } catch (e: NoClassDefFoundError) {
                        LOGGER.error("Failed to load class '{}'", className, e)
                    }
                }
        }
    }
}