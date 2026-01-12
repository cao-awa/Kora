package com.github.cao.awa.kora.server.network.http.control.abort.reason

data class AbortReason<T: Exception>(val exception: T, val reason: String) {

}