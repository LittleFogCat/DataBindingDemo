package top.littlefogcat.simpledatabindingkt

import android.util.Log
import androidx.databinding.ObservableBoolean
import androidx.databinding.ObservableField
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Response
import java.io.IOException

class StockViewModel() {
    companion object {
        const val TAG = "StockViewModel"
    }

    private val repository = StockRepository()

    val stockInfoObservable = ObservableField<StockInfo>()
    val refreshingObservable = ObservableBoolean(false)

    fun queryStock(code: String) {
        val realCode = reformatCode(code) ?: return

        refreshingObservable.set(true)

        repository.getStockInfo(realCode, object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e(TAG, "onFailure: ${e.message}")
                refreshingObservable.set(false)
            }

            override fun onResponse(call: Call, response: Response) {
                val s = response.body?.string() ?: return
                Log.d(TAG, "onResponse: $s")
                val data = s.substring(s.indexOf("\"") + 1, s.lastIndexOf("\""))
                val dataArr = data.split("~")
                val stockInfo = StockInfo(
                    code = dataArr[2],
                    name = dataArr[1],
                    price = dataArr[3].toDouble(),
                    increase = dataArr[32].toDouble()
                )
                stockInfoObservable.set(stockInfo)
                refreshingObservable.set(false)
                Log.d(TAG, "queryStock: thread=" + Thread.currentThread().name)
            }
        })
    }

    /**
     * 代码格式：000xxx或600xxx
     */
    private fun reformatCode(code: String): String? {
        if (code.length != 6) return null
        if (code.startsWith("000")) {
            return "sz$code"
        }
        if (code.startsWith("600")) {
            return "sh$code"
        }
        return null
    }

}