package com.github.cao.awa.kalmia.server.network.http.asset.producer

import com.github.cao.awa.kalmia.server.network.http.asset.KalmiaAsset
import com.github.cao.awa.kalmia.server.network.http.context.KalmiaHttpContext
import com.github.cao.awa.kalmia.server.network.http.pipeline.KalmiaHttpRequestPipeline

class KalmiaAssetProducer(val context: KalmiaHttpContext) {
    fun getAsset(pipeline: KalmiaHttpRequestPipeline): KalmiaAsset<*> {
        return pipeline.getAsset(this.context)
    }
}