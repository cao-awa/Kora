package com.github.cao.awa.kora.server.network.control.abort.reason

data class AbortReason<T: Throwable>(val exception: T, val reason: String) {

}