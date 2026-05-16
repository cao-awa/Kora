package com.github.cao.awa.kora.server.network.http.php

import kotlin.jvm.Throws

class KoraHttpPHPNotFoundException(msg: String): RuntimeException(msg) {
    companion object {
        @Throws(KoraHttpPHPNotFoundException::class)
        fun notFoundPHP(msg: String): Nothing = throw KoraHttpPHPNotFoundException(msg)
    }
}