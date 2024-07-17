package com.samy.ganna.ui.main

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.samy.ganna.R
import com.samy.ganna.databinding.ActivityMainBinding
import com.samy.ganna.pojo.Book
import com.samy.ganna.pojo.Title
import com.samy.ganna.ui.main.adapter.PageAdapter
import com.samy.ganna.ui.main.adapter.TitleAdapter
import com.samy.ganna.utils.Constants
import com.samy.ganna.utils.Constants.NOTIFICATION_PERMISSION_REQUEST_CODE
import com.samy.ganna.utils.NetworkState
import com.samy.ganna.utils.NotificationUtils
import com.samy.ganna.utils.Utils
import com.samy.ganna.utils.Utils.getSharedPreferencesBoolean
import com.samy.ganna.utils.Utils.getSharedPreferencesInt
import com.samy.ganna.utils.Utils.myLog
import com.samy.ganna.utils.Utils.setSharedPreferencesBoolean
import com.samy.ganna.utils.Utils.setSharedPreferencesInt
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

import com.google.android.play.core.appupdate.AppUpdateInfo
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.appupdate.AppUpdateOptions
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.UpdateAvailability
import com.google.android.play.core.tasks.Task

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    @Inject
    lateinit var titleAdapter: TitleAdapter
    private val viewModel: BookViewModel by viewModels()

    @Inject
    lateinit var pagesAdapter: PageAdapter
    var lastIndexSelected: Int = 0

    var titles: List<Title>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setup()
        data()
        onViewPageCallBack()
        onclick()
        observe()

    }

    override fun onStart() {
        super.onStart()
        makeNotificationDaily()
    }
    private fun showNotificationItem() {
        closeDrawer()
        val itemCLicked = intent.getIntExtra(Constants.ISAUTOOPENDNOTIFICATION, -1)
        binding.viewpager.currentItem = itemCLicked
    }


    private fun makeNotificationDaily() {
        myLog("makeNotificationDaily")
        if (areNotificationsEnabled()) {
            sendRunTimePermission()
        } else {
            showNotificationDisabledDialog()
        }
    }

    private fun areNotificationsEnabled(): Boolean {
        return NotificationManagerCompat.from(this).areNotificationsEnabled()
    }

    private fun sendRunTimePermission() {
        myLog("sendRunTimePermission")
        if (Build.VERSION.SDK_INT >= 33) {
            requestPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
        } else {
            hasNotificationPermissionGranted = true
            scheduleNotification()
        }
    }

    var hasNotificationPermissionGranted = false

    private val requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        hasNotificationPermissionGranted = isGranted
        if (isGranted) {
            scheduleNotification()
            Toast.makeText(applicationContext, "Notification permission granted", Toast.LENGTH_SHORT).show()
        } else {
            handlePermissionDenied()
        }
    }

    private fun handlePermissionDenied() {
        myLog("handlePermissionDenied")
        myLog("Build.VERSION.SDK_INT: ${Build.VERSION.SDK_INT >= 33}")
        myLog("shouldShowRequestPermissionRationale(android.Manifest.permission.POST_NOTIFICATIONS: ${shouldShowRequestPermissionRationale(android.Manifest.permission.POST_NOTIFICATIONS)}")
        myLog("Build.VERSION.SDK_INT >= 33 && shouldShowRequestPermissionRationale(android.Manifest.permission.POST_NOTIFICATIONS)): ${Build.VERSION.SDK_INT >= 33 && shouldShowRequestPermissionRationale(android.Manifest.permission.POST_NOTIFICATIONS)}")
        if (Build.VERSION.SDK_INT >= 33 && shouldShowRequestPermissionRationale(android.Manifest.permission.POST_NOTIFICATIONS)) {
            showNotificationPermissionRationale()
        } else {
            showSettingDialog()
        }
    }

    private fun showNotificationPermissionRationale() {
        MaterialAlertDialogBuilder(this, com.google.android.material.R.style.MaterialAlertDialog_Material3)
            .setTitle("Alert")
            .setMessage("Notification permission is required to show notifications.")
            .setPositiveButton("Ok") { _, _ ->
                if (Build.VERSION.SDK_INT >= 33) {
                    requestPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showSettingDialog() {
        myLog("showSettingDialog")
        MaterialAlertDialogBuilder(this, com.google.android.material.R.style.MaterialAlertDialog_Material3)
            .setTitle("Notification Permission")
            .setMessage("Notification permission is required. Please allow notification permission from settings.")
            .setPositiveButton("Ok") { _, _ ->
                val intent = Intent(ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                    data = Uri.parse("package:$packageName")
                }
                startActivity(intent)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }


    private fun showNotificationDisabledDialog() {
        MaterialAlertDialogBuilder(this, com.google.android.material.R.style.MaterialAlertDialog_Material3)
            .setTitle("Notifications Disabled")
            .setMessage("Notifications are disabled for this app. Please enable notifications from settings.")
            .setPositiveButton("Go to Settings") { _, _ ->
                val intent = Intent(ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                    data = Uri.parse("package:$packageName")
                }
                startActivity(intent)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    private fun scheduleNotification() {
        myLog("scheduleNotification")
        val old = getSharedPreferencesBoolean(
            this,
            Constants.ScheduleTask,
            Constants.IsInitialised,
            false
        )
        myLog("old: $old")
        if (!old) {
            DailyNotificationWorker.scheduleNextNotification(this)
            setSharedPreferencesBoolean(this, Constants.ScheduleTask, Constants.IsInitialised, true)
        }
    }


    private fun data() {
        viewModel.getBook()
        viewModel.getTitles()
    }

    private fun onViewPageCallBack() {
        binding.viewpager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            // This method is triggered when there is any scrolling activity for the current page
            override fun onPageScrolled(
                position: Int, positionOffset: Float, positionOffsetPixels: Int,
            ) {
                super.onPageScrolled(position, positionOffset, positionOffsetPixels)
//                statePage = position
//                myLog("onPageScrolled: statePage: $statePage")
            }

            // triggered when you select a new page
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
//                myLog("MainActivity: onPageScrollStateChanged binding.viewpager.currentItem: ${binding.viewpager.currentItem}")
                //                statePage = position
                //                myLog("onPageSelected: statePage: $position")

            }

            // triggered when there is
            // scroll state will be changed
            override fun onPageScrollStateChanged(state: Int) {
                super.onPageScrollStateChanged(state)
                //                statePage = state
//                myLog("onPageScrollStateChanged: statePage: $state")

//                pringLogsRVMenu("onPageScrollStateChanged")
            }
        })

    }


    private fun onclick() {
        titleAdapter.setOnItemClickListener { view, data, position ->
            binding.viewpager.currentItem = position
            select(position)
        }
        binding.ivMenu.setOnClickListener {
            openNavSideAndSelectItem()
        }
//
//        binding.bookTitle.setOnClickListener {
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//                myLog("binding.title.setOnClickListener")
//                NotificationUtils.generateNotification(this, "testMainActivity")
//
//
//            }
//        }

    }

    private fun openNavSideAndSelectItem() {
        titles?.get(lastIndexSelected)?.selected = false
        titles?.get(binding.viewpager.currentItem)?.selected = true
        titleAdapter.notifyDataSetChanged()
        if (binding.drawerLayout.isOpen)
            closeDrawer()
        else {
            openNavSideBar()
        }
    }

    private fun select(position: Int) {
        titles?.get(lastIndexSelected)?.selected = false
        lastIndexSelected = position
        titles?.get(lastIndexSelected)?.selected = true
        titleAdapter.notifyDataSetChanged()
        closeDrawer()
    }


    private fun observe() {
        lifecycleScope.launchWhenStarted {
            viewModel.bookStateFlow.collect {
//                myLog("viewModel.bookStateFlow.collect ")
                when (it) {
                    is NetworkState.Idle -> {
                        return@collect
                    }

                    is NetworkState.Loading -> {
//                        visProgress(true)
                    }

                    is NetworkState.Error -> {
//                        visProgress(false)
//                        it.handleErrors(mContext, null)
                    }

                    is NetworkState.Result<*> -> {
//                        visProgress(false)
                        handleResult(it.response as Book)

                    }
                }

            }
        }
        lifecycleScope.launchWhenStarted {

            viewModel.titleStateFlow.collect {
//                myLog("viewModel.titleStateFlow.collect ")
                when (it) {
                    is NetworkState.Idle -> {
                        return@collect
//                        myLog("viewModel.titleStateFlow.collect: NetworkState.Idle ")
                    }

                    is NetworkState.Loading -> {
//                        visProgress(true)
//                        myLog("viewModel.titleStateFlow.collect: NetworkState.Loading ")
                    }

                    is NetworkState.Error -> {
//                        visProgress(false)
//                        it.handleErrors(mContext, null)
//                        myLog("viewModel.titleStateFlow.collect: NetworkState.Error ")
                    }

                    is NetworkState.Result<*> -> {
//                        visProgress(false)
                        handleResult(it.response as List<*>)//List<Title>
//                        myLog("viewModel.titleStateFlow.collect: NetworkState.Result :${(it.response as List<*>).size}")
                    }
                }

            }


        }
    }

    private fun setup() {
        navContent()
        viewPager()
    }


    private fun navContent() {
        binding.navContent.rv.adapter = titleAdapter

    }

    private fun viewPager() {
        binding.viewpager.adapter = pagesAdapter

    }


    private fun <T> handleResult(data: T) {
        when (data) {
            is Book -> {
                ui(data)
            }

            is List<*> -> {//List<Title>
                ui(data)
            }
        }
    }

    private fun ui(data: Any) {
        when (data) {
            is Book -> {
                binding.bookTitle.text = data.name
                binding.navContent.tv.text = data.fancyName
                pagesAdapter.submitList(data.arr)
                initialStateViewPager()
            }

            is List<*> -> { // list of titles
                titles = data as List<Title>
                titleAdapter.submitList(titles)
                initialNavSideBar()
            }

        }
    }

    private fun openNavSideAndSelectIntro() {
//        myLog("openNavSideAndSelectIntro()")
        lastIndexSelected = 0
        titles?.get(0)?.selected = true
        titleAdapter.notifyDataSetChanged()
        binding.drawerLayout.openDrawer(GravityCompat.START)

    }


    private fun initialNavSideBar() {
        if (!ifAppOpenedFromNotification()) {
            openNavSideAndSelectIntro()
        }
    }


    private fun initialStateViewPager() {
//        myLog("initialStateViewPager()")
//        myLog("!ifAppOpenedFromNotification(): ${!ifAppOpenedFromNotification()}")
        if (ifAppOpenedFromNotification())
            showNotificationItem()

    }


    private fun ifAppOpenedFromNotification(): Boolean {
        val getIntExstraFromNotifiacation =
            intent.getIntExtra(Constants.ISAUTOOPENDNOTIFICATION, -1)
//        myLog("MainActivity:ifAppOpenedFromNotification: getIntExstraFromNotifiacation:${getIntExstraFromNotifiacation} ")
        return getIntExstraFromNotifiacation != -1
    }

    // orientation
//    override fun onSaveInstanceState(outState: Bundle) {
//        super.onSaveInstanceState(outState)
//        outState.putInt("current_page", binding.viewpager.currentItem)
//    }
//
//    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
//        super.onRestoreInstanceState(savedInstanceState)
//        val currentPage = savedInstanceState.getInt("current_page", 0)
//        binding.viewpager.post {
//            binding.viewpager.setCurrentItem(currentPage, false)
//        }
//    }


//    var indexOfCurrentPage: Int = 0 //when the phone power off then power on
//    override fun onResume() {
//        super.onResume()
//        binding.viewpager.currentItem =
//            indexOfCurrentPage //when the phone power off then power on
//
//    }
//
//    override fun onPause() {
//        super.onPause()
//        indexOfCurrentPage =
//            binding.viewpager.currentItem //when the phone power off then power on
//    }


    private fun closeDrawer() {
        if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
            binding.drawerLayout.closeDrawer(GravityCompat.START)
        }
    }


    private fun openNavSideBar() {
        binding.drawerLayout.openDrawer(GravityCompat.START)
        scrollToPosition(binding.viewpager.currentItem)
        lastIndexSelected = binding.viewpager.currentItem
    }


    private fun scrollToPosition(position: Int) {
        binding.navContent.rv.scrollToPosition(position)
        binding.navContent.rv.post(Runnable {
//             Scroll to the position after the RecyclerView has completed its layout pass
            (binding.navContent.rv.layoutManager as LinearLayoutManager).scrollToPositionWithOffset(
                position,
                0
            )
        })
    }

    override fun onBackPressed() {
        val drawer = findViewById<DrawerLayout>(R.id.drawerLayout)
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }
}