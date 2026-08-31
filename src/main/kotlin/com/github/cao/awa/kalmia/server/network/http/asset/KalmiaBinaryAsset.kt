package com.github.cao.awa.kalmia.server.network.http.asset

import java.io.File

class KalmiaBinaryAsset(file: File) : KalmiaAsset<ByteArray>(file, file.readBytes()) {

}