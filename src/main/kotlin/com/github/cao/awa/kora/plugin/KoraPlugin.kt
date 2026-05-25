package com.github.cao.awa.kora.plugin

class KoraPlugin(
    val name: String,
    val entrypoint: String,
    val dependsOn: Array<String>,
    val fallback: String = "",
    val unload: String = ""
) {

}