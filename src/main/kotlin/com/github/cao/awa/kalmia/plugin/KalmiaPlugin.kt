package com.github.cao.awa.kalmia.plugin

class KalmiaPlugin(
    val name: String,
    val entrypoint: String,
    val dependsOn: Array<String>,
    val fallback: String = "",
    val unload: String = ""
) {

}