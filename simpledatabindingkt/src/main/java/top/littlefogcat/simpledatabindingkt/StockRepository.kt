package top.littlefogcat.simpledatabindingkt

import okhttp3.Callback

class StockRepository {
    companion object {
        const val URL_PREFIX = "http://qt.gtimg.cn/q="
    }

    fun getStockInfo(code: String, callback: Callback) {
        val realUrl = URL_PREFIX + code
        HttpUtil.httpGet(realUrl, callback)
    }

}