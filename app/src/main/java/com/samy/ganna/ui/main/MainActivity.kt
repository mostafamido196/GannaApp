package com.samy.ganna.ui.main

import android.content.Intent
import android.os.Bundle
import android.util.TypedValue
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.viewpager2.widget.ViewPager2
import com.samy.ganna.R
import com.samy.ganna.databinding.ActivityMainBinding
import com.samy.ganna.pojo.Book
import com.samy.ganna.pojo.Title
import com.samy.ganna.ui.setting.SettingActivity
import com.samy.ganna.ui.main.adapter.PageAdapter
import com.samy.ganna.ui.main.adapter.TitleAdapter
import com.samy.ganna.utils.NetworkState
import com.samy.ganna.utils.Utils
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
        data()
        observe()
        setup()
        onclick()
        onViewPageCallBack()

        // to make orientation
        if (savedInstanceState != null) {
            val currentPage = savedInstanceState.getInt("current_page", 0)
            // Ensure this is run after the view has been laid out
            binding.viewpager.post {
                binding.viewpager.setCurrentItem(currentPage, false)
            }
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
//                statePage = position
//                myLog("onPageSelected: statePage: $statePage")
//                pringLogsRVMenu("onPageSelected")

            }

            // triggered when there is
            // scroll state will be changed
            override fun onPageScrollStateChanged(state: Int) {
                super.onPageScrollStateChanged(state)
//                statePage = state
//                myLog("onPageScrollStateChanged: statePage: $statePage")

//                pringLogsRVMenu("onPageScrollStateChanged")
            }
        })

    }

    private fun onclick() {
        titleAdapter.setOnItemClickListener { view, data, position ->
//            selectItemNavDrower(position)
            binding.viewpager.currentItem = position
            select(position)
        }
        binding.ivMenu.setOnClickListener {
//            pringLogsRVMenu("binding.ivMenu.setOnClickListener")
            titles?.get(lastIndexSelected)?.selected = false
            titles?.get(binding.viewpager.currentItem)?.selected = true
            titleAdapter.notifyDataSetChanged()
            if (binding.drawerLayout.isOpen)
                closeDrawer()
            else {
                openNavSideBar()
            }
        }
//        binding.ivSetting.setOnClickListener {
//            startActivity(Intent(this, SettingActivity::class.java))
//        }
//        binding.navContent.tv.setOnClickListener {
////            pringLogsRVMenu("binding.navContent.tv.setOnClickListener")
//
//        }
    }

    private fun select(position: Int) {
        titles?.get(lastIndexSelected)?.selected = false
        lastIndexSelected = position
        titles?.get(lastIndexSelected)?.selected = true
        titleAdapter.notifyDataSetChanged()
        closeDrawer()
    }

//    private fun manageVisPosition(position: Int): Int {
//        val firstVisibleItemPosition =
//            (binding.navContent.rv.layoutManager as LinearLayoutManager).findFirstVisibleItemPosition()
//        return position - firstVisibleItemPosition
//
//    }


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
        viewPager()
        navContent()
        initialTextSize()
//        initialTitleRV()
    }

    private fun initialTextSize() {
        val fontSize = Utils.getTextSizes(this)

//        binding.title.setTextSize(TypedValue.COMPLEX_UNIT_SP, high)
//        binding.tvAbout.setTextSize(TypedValue.COMPLEX_UNIT_SP, fontSize.median)
    }


//    private fun initialTitleRV() {
//        titles?.get(0)?.selected  = true
//        lastIndexSelected = 0
//    }

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
                binding.title.text = data.name
                binding.navContent.tv.text = data.fancyName
                pagesAdapter.submitList(data.arr)
            }
            is List<*> -> {
                titles = data as List<Title>
                titleAdapter.submitList(titles)
            }
        }

    }

    // orientation
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
//        myLog("Saved current page: ${binding.viewpager.currentItem}")
        outState.putInt("current_page", binding.viewpager.currentItem)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        val currentPage = savedInstanceState.getInt("current_page", 0)
        binding.viewpager.post {
            binding.viewpager.setCurrentItem(currentPage, false)
//            myLog("Restored current page: $currentPage")
        }
    }


//    private fun selectItemNavDrower(position: Int) {
//        deselect()
//        lastIndexSelected = position
//        select()
//    }

    var indexOfCurrentPage: Int = 0 //when the phone power off then power on
    override fun onResume() {
        super.onResume()
        binding.viewpager.currentItem =
            indexOfCurrentPage //when the phone power off then power on

    }

    override fun onPause() {
        super.onPause()
        indexOfCurrentPage =
            binding.viewpager.currentItem //when the phone power off then power on
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