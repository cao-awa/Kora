package com.github.cao.awa.kora.server.network.http.asset

import java.io.File

class KoraAsset(val file: File, val data: ByteArray = file.readBytes()) {

}