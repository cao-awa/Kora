package com.github.cao.awa.kora.launch.config

import java.util.LinkedList

object KoraLaunchDefaultConfig: KoraLaunchConfig() {
    private fun throwWhenSet(): Nothing {
        error("Cannot set config in default server config instance")
    }

    override fun printConfigDetails(print: Boolean): KoraLaunchConfig {
        throwWhenSet()
    }

    override fun entrypoint(entrypoint: LinkedList<String>): KoraLaunchConfig {
        throwWhenSet()
    }

    override fun error(error: Throwable): KoraLaunchConfig {
        throwWhenSet()
    }
}