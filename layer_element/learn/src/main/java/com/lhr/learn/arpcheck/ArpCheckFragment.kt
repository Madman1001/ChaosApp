package com.lhr.learn.arpcheck

import android.os.Bundle
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.lifecycleScope
import com.lhr.common.ui.BaseFragment
import com.lhr.learn.databinding.FragmentArpCheckBinding
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress

/**
 * @CreateDate: 2022/12/29
 * @Author: mac
 * @Description:
 */
class ArpCheckFragment : BaseFragment<FragmentArpCheckBinding>()  {
    val dataText = MutableLiveData("")

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        mBinding.fragment = this
    }

    fun readArpData(){
        lifecycleScope.launch {
/*            withContext(IO){
                val dp = DatagramPacket(ByteArray(0), 0, 0)
                var socket = DatagramSocket()
                var position = 2
                while (position < 255) {
                    dp.address = InetAddress.getByName("192.168.1.$position")
                    socket.send(dp)
                    position++
                    if (position == 125) {
                        socket.close()
                        socket = DatagramSocket()
                    }
                }
                socket.close()
            }*/
            withContext(IO){
                val data = ArpNDK.getARP()
                withContext(Main){
                    dataText.value = data
                }
            }
        }
    }
}