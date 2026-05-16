package com.github.cao.awa.kora.server.network.http.asset

import java.io.File

class KoraBinaryAsset(file: File) : KoraAsset<ByteArray>(file, file.readBytes()) {

}