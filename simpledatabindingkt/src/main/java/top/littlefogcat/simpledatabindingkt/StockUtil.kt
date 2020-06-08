package top.littlefogcat.simpledatabindingkt

object StockUtil {
    /**
     * 给股票代码加上前缀
     */
    fun reformatCode(code: String): String? {
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