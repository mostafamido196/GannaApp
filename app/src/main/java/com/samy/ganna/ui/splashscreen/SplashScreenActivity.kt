package com.samy.ganna.ui.splashscreen

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import androidx.appcompat.app.AppCompatActivity
import com.samy.ganna.R
import com.samy.ganna.ui.main.MainActivity
import com.samy.ganna.utils.Constants
import com.samy.ganna.utils.NotificationUtils

class SplashScreenActivity : AppCompatActivity() {
    var isOpenFromNotification = -1
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)

        isOpenFromNotification = intent.getIntExtra(NotificationUtils.EXTRA_NOTIFICATION, -1)

        Handler().postDelayed({
            val i = Intent(
                this,
                MainActivity::class.java
            )
            i.putExtra(Constants.ISAUTOOPENDNOTIFICATION,isOpenFromNotification)
            startActivity(i)
            finish()
        }, 2000)
    }
}