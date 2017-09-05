package SocketBeControlled

import Utils.LogUtils

fun main(args: Array<String>) {
    //val client = ControlledClient("139.199.20.248",10087,"lzl471954654","Test")

    val client = ControlledClient("127.0.0.1",10087,"lzl471954654","Test")
    LogUtils.initLog()
    LogUtils.logInfo("Main","LogInit!")
    println("LogInit")
    try{
        client.clientRun()
    }catch (e:Exception){
        LogUtils.logException("Main",""+e.message)
    }
}