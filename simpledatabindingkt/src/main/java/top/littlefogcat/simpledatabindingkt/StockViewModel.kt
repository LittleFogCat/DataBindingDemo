package top.littlefogcat.simpledatabindingkt

import android.util.Log
import androidx.databinding.ObservableField
import androidx.databinding.ObservableInt
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.lang.Exception

class StockViewModel() {
    companion object {
        const val TAG = "StockViewModel"

        const val STATE_INITIAL = 0
        const val STATE_QUERYING = 1
        const val STATE_SUCCESS = 2
        const val STATE_FAILURE = 3
    }

    private val repository = StockRepository()

    val stockInfoObservable = ObservableField<StockInfo>()

    /**
     * 保存当前查询状态。
     * 0：初始状态；
     * 1：查询中；
     * 2：查询成功；
     * 3：查询失败；
     */
    val stateObservable = ObservableInt(STATE_INITIAL)

    fun queryStock(code: String) {
        val realCode = StockUtil.reformatCode(code) ?: return

        stateObservable.set(STATE_QUERYING)

        GlobalScope.launch {
            try {
                val s = repository.getStockInfo(realCode) ?: return@launch
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
                stateObservable.set(STATE_SUCCESS)
            } catch (e: Exception) {
                Log.e(TAG, "onFailure: ${e.message}")
                e.printStackTrace()
                stateObservable.set(STATE_FAILURE)
            }
        }
    }


}