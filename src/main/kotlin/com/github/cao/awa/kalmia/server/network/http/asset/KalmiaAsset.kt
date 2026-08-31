package com.github.cao.awa.kalmia.server.network.http.asset

import java.io.File

abstract class KalmiaAsset<T: Any>(val file: File, val data: T) {
}