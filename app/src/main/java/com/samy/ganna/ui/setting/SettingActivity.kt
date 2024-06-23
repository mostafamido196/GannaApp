package com.samy.ganna.ui.setting

import android.os.Bundle
import android.util.TypedValue
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.samy.ganna.R
import com.samy.ganna.databinding.ActivityMainBinding
import com.samy.ganna.databinding.ActivitySettingBinding
import com.samy.ganna.pojo.FontSizeType
import com.samy.ganna.utils.Constants
import com.samy.ganna.utils.Utils
import com.samy.ganna.utils.Utils.getSharedPreferencesFloat
import com.samy.ganna.utils.Utils.setSharedPreferencesFloat
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SettingActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySettingBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setup()
    }

    private fun setup() {
        fonts()
    }

    private fun fonts() {
        initialTextSize()
        fontSetting()
    }



    private fun initialTextSize() {
        val fontSize = Utils.getTextSizes(this)

        binding.tvTitleFount.setTextSize(TypedValue.COMPLEX_UNIT_SP, fontSize.median)
        binding.tvTxtShow.setTextSize(TypedValue.COMPLEX_UNIT_SP, fontSize.median)
        binding.tvTxtSize.setTextSize(TypedValue.COMPLEX_UNIT_SP,fontSize. median)
        binding.small.setTextSize(TypedValue.COMPLEX_UNIT_SP, fontSize.median)
        binding.median.setTextSize(TypedValue.COMPLEX_UNIT_SP, fontSize.median)
        binding.high.setTextSize(TypedValue.COMPLEX_UNIT_SP, fontSize.median)
    }

    private fun fontSetting() {
        initialFontSizeRadio()
        onclickFontSizeRadio()
    }


    private fun initialFontSizeRadio() {
        val fontSize = getSharedPreferencesFloat(
            this, Constants.FontSizeFile, Constants.MEDIAN, 18f
        )
        when (fontSize) {
            16f -> {
                binding.radioLlSize.check(R.id.small)
                binding.tvTxtShow.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f)
            }
            18f -> {
                binding.radioLlSize.check(R.id.median)
                binding.tvTxtShow.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18f)
            }
            20f -> {
                binding.radioLlSize.check(R.id.high)
                binding.tvTxtShow.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20f)
            }
        }
    }

    private fun onclickFontSizeRadio() {
        binding.radioLlSize.setOnCheckedChangeListener { radioGroup, id ->
            when (id) {
                R.id.small -> {
                    updateStyle(FontSizeType.SMALL)
                    radioGroup.check(R.id.small)
                }
                R.id.median -> {
                    updateStyle(FontSizeType.MEDIAN)
                    radioGroup.check(R.id.median)
                }
                R.id.high -> {
                    updateStyle(FontSizeType.HIGH)
                    radioGroup.check(R.id.high)
                }
            }

        }
    }

    private fun updateStyle(size: FontSizeType) {
        when (size.toString()) {
            FontSizeType.SMALL.toString() -> {

//                binding.tvTxtShow.setTextSizeFromDimenSspSdp(R.dimen._12ssp)
                binding.tvTxtShow.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f)
                update_text_description(14f)
                update_text_normal(16f)
                update_text_title(18f)
            }
            FontSizeType.MEDIAN.toString() -> {
                binding.tvTxtShow.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18f)
                update_text_description(16f)
                update_text_normal(18f)
                update_text_title(20f)
            }
            FontSizeType.HIGH.toString() -> {
                binding.tvTxtShow.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20f)
                update_text_description(18f)
                update_text_normal(20f)
                update_text_title(22f)
            }
        }
    }

    private fun update_text_description( textSizeFloat: Float) {
        setSharedPreferencesFloat(this,
            Constants.FontSizeFile,Constants.SMALL,textSizeFloat)

    }

    private fun update_text_normal(textSizeFloat: Float) {
        setSharedPreferencesFloat(this,Constants.FontSizeFile,Constants.MEDIAN,textSizeFloat)
    }

    private fun update_text_title(textSizeFloat: Float) {
        setSharedPreferencesFloat(this,Constants.FontSizeFile,Constants.HIGH,textSizeFloat)
    }

}