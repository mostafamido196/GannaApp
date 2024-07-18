package com.samy.ganna.ui.main

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationManagerCompat
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.viewpager2.widget.ViewPager2
import androidx.work.WorkManager
import com.samy.ganna.R
import com.samy.ganna.databinding.ActivityMainBinding
import com.samy.ganna.pojo.Book
import com.samy.ganna.pojo.Title
import com.samy.ganna.ui.main.adapter.PageAdapter
import com.samy.ganna.ui.main.adapter.TitleAdapter
import com.samy.ganna.utils.Constants
import com.samy.ganna.utils.NetworkState
import com.samy.ganna.utils.Utils
import com.samy.ganna.utils.Utils.getSharedPreferencesBoolean
import com.samy.ganna.utils.Utils.myLog
import com.samy.ganna.utils.Utils.setSharedPreferencesBoolean
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject


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
        makeNotificationDaily()
        prepareGooglePlayNewVersion()
    }

    private fun prepareGooglePlayNewVersion() {

    }


    private fun showNotificationItem() {
        closeDrawer()
        val itemCLicked = intent.getIntExtra(Constants.ISAUTOOPENDNOTIFICATION, -1)
        binding.viewpager.currentItem = itemCLicked
    }


    private fun makeNotificationDaily() {
        myLog("makeNotificationDaily")
        myLog("areNotificationsEnabled: ${areNotificationsEnabled()}")
        if (areNotificationsEnabled()) {
            scheduleNotification()
        } else {
            sendRunTimePermission()
        }
    }

    private fun areNotificationsEnabled(): Boolean {
        return NotificationManagerCompat.from(this).areNotificationsEnabled()
    }

    private fun sendRunTimePermission() {
        myLog("sendRunTimePermission")
        myLog("Build.VERSION.SDK_INT: ${Build.VERSION.SDK_INT}")
        if (Build.VERSION.SDK_INT >= 33) {
            requestPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
        } else {
            showCustomNotificationDisabledDialog()
        }
    }

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            myLog("registerForActivityResult")
            myLog("isGranted: ${isGranted}")
            callBackPermissionAction(isGranted)
        }

    private fun callBackPermissionAction(isGranted: Boolean) {
        myLog("callBackPermissionAction")
        myLog("isGranted: $isGranted")
        if (isGranted) {
            // Permission is granted. Continue with showing notifications.
            Toast.makeText(applicationContext, "تم تفعيل الأشعارات بنجاح", Toast.LENGTH_SHORT)
                .show()
            scheduleNotification()
        } else {
            Toast.makeText(applicationContext, "لم يتم تفعيل الأشعارات", Toast.LENGTH_SHORT)
                .show()
            // Explain to the user that the feature is unavailable because the
            // features requires a permission that the user has denied.
        }
    }

    companion object {
        val REQUEST_CODE_SETTINGS = 45
    }

    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        myLog("onActivityResult")
        myLog("areNotificationsEnabled: ${areNotificationsEnabled()}")
        myLog("requestCode: ${requestCode}")
        myLog("REQUEST_CODE_SETTINGS: ${REQUEST_CODE_SETTINGS}")
//        if (data.getIntExtra(REQUEST_CODE_SETTINGS) == REQUEST_CODE_SETTINGS) {
        callBackPermissionAction(areNotificationsEnabled())
//        }
    }


    private val dialogHandler = Handler(Looper.getMainLooper())
    private val dialogRunnable = Runnable {
        val dialog = CenterDialogFragment()
        dialog.show(supportFragmentManager, "TopDialog")
    }
    private val hideDialogRunnable = Runnable {
        supportFragmentManager.findFragmentByTag("TopDialog")?.let {
            (it as CenterDialogFragment).dismiss()
        }
    }

    private fun showCustomNotificationDisabledDialog() {
        myLog("showCustomNotificationDialog")  // Show the dialog immediately
        dialogHandler.post(dialogRunnable)
        // Hide the dialog after 10 sec
        dialogHandler.postDelayed(hideDialogRunnable, 10 * 1000) // 10 sec
    }

    override fun onDestroy() {
        super.onDestroy()
//        In onDestroy, the callbacks are removed to avoid memory leaks.
        dialogHandler.removeCallbacks(dialogRunnable)
        dialogHandler.removeCallbacks(hideDialogRunnable)
    }

    private fun scheduleNotification() {
        myLog("scheduleNotification")
        myLog("areNotificationsEnabled: ${areNotificationsEnabled()}")
        myLog("makeScheduler before: ${ifWillMakeScheduler()}")
        if (areNotificationsEnabled() && !ifWillMakeScheduler()) {
            // if the user edit notification sitting after download
            WorkManager.getInstance(this).cancelAllWork()
            setMakeScheduler(true)
        }
        myLog("makeScheduler after: ${ifWillMakeScheduler()}")
        if (ifWillMakeScheduler()) {
            DailyNotificationWorker.scheduleNextNotification(this)
            setMakeScheduler(false)
        }
    }



    private fun ifWillMakeScheduler(): Boolean {
        return getSharedPreferencesBoolean(
            this,
            Constants.ScheduleTask,
            Constants.IsNotSchedule,
            true
        )
    }

    private fun setMakeScheduler(b: Boolean) {
        setSharedPreferencesBoolean(
            this,
            Constants.ScheduleTask,
            Constants.IsNotSchedule,
            b
        )
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