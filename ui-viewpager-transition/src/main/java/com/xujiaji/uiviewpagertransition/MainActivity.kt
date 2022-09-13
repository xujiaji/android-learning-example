package com.xujiaji.uiviewpagertransition

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentStatePagerAdapter
import com.xujiaji.uiviewpagertransition.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    lateinit var binding : ActivityMainBinding
    val fragments = arrayOf(
        MainFragment.newInstance("One"),
        MainFragment.newInstance("Two"),
        MainFragment.newInstance("Three"),
        MainFragment.newInstance("Four"),
        MainFragment.newInstance("Five"))

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater).apply {
            setContentView(root)
        }

        binding.viewPager.isEnabled = false
        binding.viewPager.setPageTransformer(false, CustPagerTransformer(this))

        binding.viewPager.adapter = object : FragmentStatePagerAdapter(supportFragmentManager,
            BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT
        ) {
            override fun getCount() = fragments.size
            override fun getItem(position: Int) = fragments[position]
        }
    }
}