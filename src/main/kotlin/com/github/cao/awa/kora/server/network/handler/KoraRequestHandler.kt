package com.github.cao.awa.kora.server.network.handler

import com.github.cao.awa.kora.server.network.context.KoraContext
import com.github.cao.awa.kora.server.network.context.abort.KoraAbortContext
import com.github.cao.awa.kora.server.network.holder.PathByteBufHolder
import com.github.cao.awa.kora.server.network.control.abort.reason.AbortReason

abstract class KoraRequestHandler<B: PathByteBufHolder, C: KoraContext<B, C, A>, A: KoraAbortContext<B>> {
    abstract fun hasRoute(path: String): Boolean

    abstract fun handle(context: C): Any

    abstract fun hasAbortHandler(abortReason: AbortReason<out Throwable>): Boolean

    abstract fun handleAbort(abortScope: A, abortReason: AbortReason<out Throwable>): Any
}