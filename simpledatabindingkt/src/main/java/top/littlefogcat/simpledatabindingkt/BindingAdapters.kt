package top.littlefogcat.simpledatabindingkt

import android.annotation.SuppressLint
import android.widget.TextView
import androidx.databinding.BindingAdapter
import top.littlefogcat.simpledatabindingkt.StockViewModel.Companion.STATE_FAILURE
import top.littlefogcat.simpledatabindingkt.StockViewModel.Companion.STATE_INITIAL
import top.littlefogcat.simpledatabindingkt.StockViewModel.Companion.STATE_QUERYING
import top.littlefogcat.simpledatabindingkt.StockViewModel.Companion.STATE_SUCCESS

@SuppressLint("SetTextI18n")
@BindingAdapter("app:queryState", "app:stockInfo")
fun setTextByQueryState(textView: TextView?, state: Int, info: StockInfo?) {
    println("setTextByQueryState: $textView, $state, $info")
    if (textView == null) return

    when (state) {
        STATE_INITIAL -> textView.text = "点击“查询”按钮查询股票信息"
        STATE_QUERYING -> textView.text = "查询中"
        STATE_SUCCESS -> {
            if (info != null) {
                info.apply {
                    textView.text = "$name（$code）\n" +
                            "当前价：$price\n" +
                            "涨幅：$increase%"
                }
            } else {
                textView.text = "数据异常"
            }
        }
        STATE_FAILURE -> textView.text = "查询失败"
    }
}