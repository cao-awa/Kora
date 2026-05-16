package com.github.cao.awa.kora.server.network.http.asset

import java.io.File

abstract class KoraAsset<T: Any>(val file: File, val data: T) {
}