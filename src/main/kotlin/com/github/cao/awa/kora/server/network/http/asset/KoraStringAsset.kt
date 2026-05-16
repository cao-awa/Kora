package com.github.cao.awa.kora.server.network.http.asset

import java.io.File
import java.nio.charset.StandardCharsets

class KoraStringAsset(file: File) : KoraAsset<String>(file, file.readText(StandardCharsets.UTF_8)) {

}