package com.mountains.bledemo.ble

import android.content.Intent
import android.content.pm.PackageManager
import android.util.SparseArray
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import java.util.*

object SuperFragment {
    fun getSuperFragment(activity: FragmentActivity): SuperFragment {
        val supportFragmentManager = activity.supportFragmentManager
        var superFragment = supportFragmentManager.findFragmentByTag(BleManager.PERMISSION_FRAGMENT_TAG)
        if (superFragment == null) {
            val transaction = supportFragmentManager.beginTransaction()
            superFragment = SuperFragment()
            transaction.add(superFragment, BleManager.PERMISSION_FRAGMENT_TAG)
            transaction.commitNow()
        }
        return superFragment as SuperFragment
    }

    class SuperFragment : Fragment() {
        private val permissionListenerList: SparseArray<PermissionListener> = SparseArray<PermissionListener>()
        private val activityListenerList: SparseArray<ActivityResultListener> = SparseArray<ActivityResultListener>()
        private val random: Random = Random()

        fun startActivityForResult(intent: Intent, listener: ActivityResultListener) {
            val requestCode = makeRequestCode()
            activityListenerList.put(requestCode, listener)
            startActivityForResult(intent, requestCode)
        }

        fun requestPermissions(permissionArray: Array<String>, listener: PermissionListener) {
            val requestCode = makeRequestCode()
            permissionListenerList.put(requestCode, listener)
            requestPermissions(permissionArray, requestCode)
        }

        override fun onRequestPermissionsResult(
            requestCode: Int,
            permissions: Array<out String>,
            grantResults: IntArray
        ) {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults)
            val listener = permissionListenerList.get(requestCode) ?: return
            permissionListenerList.remove(requestCode)
            for (result in grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    listener.onRequestFail()
                    return
                }
            }
            listener.onRequestSuccess()
        }

        override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
            super.onActivityResult(requestCode, resultCode, data)
            val listener = activityListenerList.get(requestCode) ?: return
            activityListenerList.remove(requestCode)
            listener.onActivityResult(resultCode, data)
        }

        /**
         * 随机生成唯一的requestCode，最多尝试10次
         */
        private fun makeRequestCode(): Int {
            var requestCode: Int
            var tryCount = 0
            do {
                requestCode = random.nextInt(0x0000FFFF)
                tryCount++
            } while ((permissionListenerList.indexOfKey(requestCode) >= 0 || activityListenerList.indexOfKey(requestCode) >= 0) && tryCount < 10)
            return requestCode
        }


    }

    interface PermissionListener {
        fun onRequestSuccess()

        fun onRequestFail()
    }

    interface ActivityResultListener {
        fun onActivityResult(resultCode: Int, data: Intent?)
    }
}