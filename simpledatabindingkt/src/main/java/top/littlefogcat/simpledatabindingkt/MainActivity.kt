package top.littlefogcat.simpledatabindingkt

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View.VISIBLE
import androidx.databinding.DataBindingUtil
import kotlinx.android.synthetic.main.activity_main.*
import top.littlefogcat.simpledatabindingkt.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var mViewModel: StockViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = DataBindingUtil.setContentView<ActivityMainBinding>(
            this, R.layout.activity_main
        )
        mViewModel = StockViewModel()
        binding.viewmodel = mViewModel

        btnGo.setOnClickListener {
            val code = input.text
            mViewModel.queryStock(code.toString())
            if (result.visibility != VISIBLE) {
                result.visibility = VISIBLE
            }
        }
    }
}
