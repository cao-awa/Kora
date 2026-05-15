package com.github.cao.awa.kora.server.network.http.asset

import com.github.cao.awa.kora.server.network.http.context.KoraHttpContext
import com.github.cao.awa.kora.server.network.http.pipeline.KoraHttpRequestPipeline

class KoraAssetProducer(val context: KoraHttpContext) {
    fun getAsset(pipeline: KoraHttpRequestPipeline): KoraAsset {
        return pipeline.getAsset(this.context)
    }
}