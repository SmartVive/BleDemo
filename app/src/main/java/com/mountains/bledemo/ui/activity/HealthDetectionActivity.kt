package com.mountains.bledemo.ui.activity

import android.os.Bundle
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentPagerAdapter
import androidx.fragment.app.FragmentStatePagerAdapter
import com.mountains.bledemo.R
import com.mountains.bledemo.base.BaseActivity
import com.mountains.bledemo.presenter.HealthDetectionPresenter
import com.mountains.bledemo.ui.fragment.BloodOxygenDetectionFragment
import com.mountains.bledemo.ui.fragment.BloodPressureDetectionFragment
import com.mountains.bledemo.ui.fragment.HeartRateDetectionFragment
import com.mountains.bledemo.view.HealthDetectionView
import kotlinx.android.synthetic.main.activity_health_detection.*

class HealthDetectionActivity : BaseActivity<HealthDetectionPresenter>(),HealthDetectionView {

    override fun createPresenter(): HealthDetectionPresenter {
        return HealthDetectionPresenter()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_health_detection)
        initView()
    }

    private fun initView(){
        titleBar.leftView.setOnClickListener {
            finish()
        }

        val fragments = listOf<Fragment>(HeartRateDetectionFragment(),BloodPressureDetectionFragment(),BloodOxygenDetectionFragment())
        val titles = listOf<String>("心率","血压","血氧")
        viewPager.offscreenPageLimit = 2
        viewPager.adapter = object : FragmentStatePagerAdapter(supportFragmentManager){
            override fun getItem(position: Int): Fragment {
                return fragments[position]
            }

            override fun getCount(): Int {
                return fragments.size
            }

            override fun getPageTitle(position: Int): CharSequence? {
                return titles[position]
            }

            override fun instantiateItem(container: ViewGroup, position: Int): Any {
                val fragment = super.instantiateItem(container, position) as Fragment
                supportFragmentManager.beginTransaction().show(fragment).commitAllowingStateLoss()
                return fragment
            }

            override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
                val fragment = fragments[position]
                supportFragmentManager.beginTransaction().hide(fragment).commitAllowingStateLoss()
            }
        }

        tabLayout.setupWithViewPager(viewPager)
    }
}