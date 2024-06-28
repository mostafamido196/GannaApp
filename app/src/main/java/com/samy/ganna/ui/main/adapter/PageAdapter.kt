package com.samy.ganna.ui.main.adapter

import android.graphics.Color
import android.text.Spannable
import android.text.SpannableString
import android.text.Spanned
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.samy.ganna.R
import com.samy.ganna.databinding.ItemPageBinding
import com.samy.ganna.pojo.Page
import com.samy.ganna.ui.main.span.CenteredBoldMarginSpan
import com.samy.ganna.ui.main.span.CenteredSpan
import com.samy.ganna.ui.main.span.CustomSpan
import com.samy.ganna.ui.main.span.SmallPaddingSpan
import com.samy.ganna.utils.Constants
import com.samy.ganna.utils.Utils
import com.samy.ganna.utils.Utils.myLog
import com.samy.ganna.utils.Utils.replaceArabicNumbers
import javax.inject.Inject


class PageAdapter @Inject constructor() :
    ListAdapter<Page, PageAdapter.ViewHolder>(DiffCallback()) {


    class DiffCallback : DiffUtil.ItemCallback<Page>() {
        override fun areItemsTheSame(
            oldItem: Page, newItem: Page,
        ): Boolean = newItem == oldItem

        override fun areContentsTheSame(
            oldItem: Page, newItem: Page,
        ): Boolean = newItem == oldItem
    }

    inner class ViewHolder(private val binding: ItemPageBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(data: Page, position: Int) {
            if (position == 0) //data.description.isEmpty()
                binding.lineBetween.visibility = View.INVISIBLE
            else binding.lineBetween.visibility = View.VISIBLE
            // Scroll to the top
            binding.sv.post {
                binding.sv.scrollTo(0, 0)
            }

            binding.bookTitle.text = data.title.replaceArabicNumbers()
            data.mainText = data.mainText.replaceArabicNumbers()
            binding.tvIsnad.text = data.description.replaceArabicNumbers()
            onclick(data)
//            initialTextSize()
            convertReferenceNums(data)

            if (data.id == 0) {
                designStyleIntro(data)
            }
        }

        private fun convertReferenceNums(page: Page) {
            val spannableString = SpannableString(page.mainText)
            for (i in page.startRef until page.endRef + 1) {
                page.mainText.indexOf(
                    "(${
                        i.toString().replaceArabicNumbers()
                    })"
                )
                makeSmallReference(
                    spannableString, page.mainText, "(${i.toString().replaceArabicNumbers()})"
                )
            }
            binding.tvMatn.text = spannableString

        }

        private fun makeSmallReference(
            spannableString: SpannableString,
            str: String,
            key: String,
        ) {

            applySpan(
                str, key, spannableString,
                SmallPaddingSpan(binding.root.context,binding.root.context.resources.getDimension(com.intuit.ssp.R.dimen._10ssp), binding.root.context.resources.getDimension(com.intuit.sdp.R.dimen._8sdp), Color.parseColor("#0091EA"))
            )



        }

        private fun designStyleIntro(data: Page) {
            val spannableString = SpannableString(data.mainText)
            makeTextCenter(data.mainText, "بسم الله الرحمن الرحيم", spannableString)
            makeTextCenter(data.mainText, "والحمد لله رب العالمين .", spannableString)
            makeTextCenterBold(data.mainText, "وكتبه", spannableString)
            makeTextCenterBold(data.mainText, "الشيخ الدكتور", spannableString)
            makeTextCenterBoldFamily(data.mainText, " أبو وسام وليد بن أمين الرفاعي", spannableString)

            binding.tvMatn.text = spannableString

        }
        private fun makeTextCenterBoldFamily(
            data: String,
            key: String,
            spannableString: SpannableString,
        ) {
            applySpan(data, key, spannableString, CustomSpan(binding.root.context,32,32,32f, R.font.alfont_com_alfont_com_4_30,true,ContextCompat.getColor(binding.root.context, R.color.c7_special_author)))
        }

        private fun applySpan(
            data: String,
            key: String,
            spannableString: SpannableString,
            span: Any,
        ) {
            val startIndex = data.indexOf(key)
            if (startIndex != -1) { // Key found
                spannableString.setSpan(
                    span, startIndex, startIndex + key.length, Spannable.SPAN_INCLUSIVE_INCLUSIVE
                )
            }
        }

        private fun makeTextCenterBold(
            data: String,
            key: String,
            spannableString: SpannableString,
        ) {
            applySpan(data, key, spannableString, CenteredBoldMarginSpan())
        }

        // Function to make text center
        private fun makeTextCenter(data: String, key: String, spannableString: SpannableString) {
            applySpan(data, key, spannableString, CenteredSpan() )
        }


        private fun convertSpecialChars(str: String): SpannableString {
            val mainSpecialCharArr: List<Int> = findIndexOfSpecialChars(str)
            val newTextSpanner: SpannableString = replaceSpecialChar(str, mainSpecialCharArr)
            return newTextSpanner
        }

        private fun replaceSpecialChar(str: String, arr: List<Int>): SpannableString {
            val spannedString = SpannableString(str)
            val specialFont =
                ResourcesCompat.getFont(binding.root.context, R.font.aga_islamic_phrases)
            arr.forEach { index ->
                spannedString.setSpan(
                    specialFont, index, index + 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                )
            }
            return spannedString
        }

        private fun findIndexOfSpecialChars(str: String): List<Int> {
            val arr = ArrayList<Int>()
            //  '\uF072' -->  "صلى الله علية وسلم"
            //  '\uF074' -->  "رضي الله عنه"

            str.forEachIndexed { index, c ->
                if (c == '\uF072' || c == '\uF074') {
                    arr.add(index)
                    myLog("findIndexOfSpecialChars: \nc :$c \nindex :$index")
                }
            }

            return arr
        }

        private fun onclick(data: Page) {
            binding.tvIsnad.setOnClickListener {
                onItemClickListener?.let { it -> it(data) }
            }


        }

        private fun initialTextSize() {
            val small = Utils.getSharedPreferencesFloat(
                binding.root.context, Constants.FontSizeFile, Constants.SMALL, 15.60f
            )
            val median = Utils.getSharedPreferencesFloat(
                binding.root.context, Constants.FontSizeFile, Constants.MEDIAN, 18.20f
            )
            val high = Utils.getSharedPreferencesFloat(
                binding.root.context, Constants.FontSizeFile, Constants.HIGH, 20.80f
            )

            binding.tvMatn.setTextSize(TypedValue.COMPLEX_UNIT_SP, median)
            binding.tvIsnad.setTextSize(TypedValue.COMPLEX_UNIT_SP, small)
        }
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder = ViewHolder(
        ItemPageBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
    )

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position), position)

    }

    private var onItemClickListener: ((Page) -> Unit)? = null


    fun setOnItemClickListener(listener: (Page) -> Unit) {
        onItemClickListener = listener
    }


}