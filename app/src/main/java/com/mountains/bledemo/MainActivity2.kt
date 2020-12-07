package com.mountains.bledemo

import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.fragment.app.Fragment
import com.mountains.bledemo.base.BaseActivity
import com.mountains.bledemo.base.BasePresenter
import com.mountains.bledemo.base.BaseView
import com.mountains.bledemo.service.DeviceConnectService
import com.mountains.bledemo.ui.fragment.DeviceFragment
import com.mountains.bledemo.ui.fragment.HomeFragment
import kotlinx.android.synthetic.main.activity_main2.*

class MainActivity2 : BaseActivity<BasePresenter<BaseView>>() {
    companion object{
        const val HOME_TAG = "home"
        const val DEVICE_TAG = "device"
    }


    override fun createPresenter(): BasePresenter<BaseView> {
        return BasePresenter()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main2)

        initView()
        initService()
        initFragment()
    }

    private fun initView(){
        bottomNavigationView.setOnNavigationItemSelectedListener {
            hideAllFragment()
            when(it.itemId){
                R.id.menuHome->{
                    showFragment(HOME_TAG)
                }
                R.id.menuDevice->{
                    showFragment(DEVICE_TAG)
                }
            }
            true
        }
    }

    private fun initFragment(){
        showFragment(DEVICE_TAG)
        showFragment(HOME_TAG)
    }

    private fun showFragment(tag:String){
        val transaction = supportFragmentManager.beginTransaction()
        var fragment = supportFragmentManager.findFragmentByTag(tag)
        if (fragment == null){
            fragment = createFragment(tag)
            fragment?.let {
                transaction.add(R.id.flMain,fragment,tag)
            }
        }else{
            transaction.show(fragment)
        }
        transaction.commit()
    }

    private fun createFragment(tag: String):Fragment?{
        when(tag){
            HOME_TAG->{
                return HomeFragment()
            }
            DEVICE_TAG->{
                return DeviceFragment()
            }
        }
        return null
    }

    private fun hideAllFragment(){
        val transaction = supportFragmentManager.beginTransaction()
        val fragments = supportFragmentManager.fragments
        for (fragment in fragments){
            transaction.hide(fragment)
        }
        transaction.commit()
    }

    private fun initService(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val intent = Intent(this, DeviceConnectService::class.java)
            startForegroundService(intent)
        } else {
            val intent = Intent(this, DeviceConnectService::class.java)
            startService(intent)
        }
    }
}