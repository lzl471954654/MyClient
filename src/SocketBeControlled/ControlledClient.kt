package SocketBeControlled

import Protocol.ServerProtocol
import Utils.LogUtils
import java.io.*
import java.net.*

class ControlledClient(val ipAddress:String,val port:Int,val username:String,val password:String) {
    val reader: BufferedReader by lazy {
        BufferedReader(InputStreamReader(socket.getInputStream()))
    }

    val writer: PrintWriter by lazy {
        PrintWriter(OutputStreamWriter(socket.getOutputStream()))
    }
    lateinit var socket: Socket
    val classTag = javaClass.name
    val END = ServerProtocol.END_FLAG
    var line:String? = null
    var builder:StringBuilder = StringBuilder()
    var loop:Boolean = true
    var localPort:Int = 0

    fun clientRun(){
        try{
            socket = Socket(ipAddress,port)
            writer.println("${ServerProtocol.ONLINE}_${username}_${password}_$END")
            writer.flush()
            val onlineResult = readStringData()
            logInfo(onlineResult)
            if(onlineResult.startsWith(ServerProtocol.ONLINE_SUCCESS)&&onlineResult.endsWith(ServerProtocol.END_FLAG))
            {
                while (loop){
                    var instructions = readStringData()
                    println("From Server: $instructions")
                    var data = readLine()
                    if(data==null){
                        println("Exit")
                        break
                    }
                    writer.println(data+"_"+ServerProtocol.END_FLAG)
                    writer.flush()
                }
            }
        }catch (e:IOException){
            e.printStackTrace()
            LogUtils.logException(classTag,""+e.message)
            loop = false
        }catch (e:SocketException){
            loop = false
            LogUtils.logException(classTag,""+e.message)
        }
        finally {
            socket.close()
        }
    }

    fun dealInstructions(instructions:String){
        val params = instructions.split("_")
        when(params[0]){
            ServerProtocol.HEATR_BEAT->{
                println("isAlive")
            }
            ServerProtocol.MAKE_HOLE->{
                localPort = socket.localPort
                val ip = params[1]
                val port = params[2]
                logInfo("HOLE SIGN ip = $ip , port = $port")
                //connectionByHole(ip,port = port.toInt())
                conectionByHole(ip,port.toInt(),false)
            }
            else->{
                logInfo(instructions)
                println(instructions)
            }
        }
    }

    fun conectionByHole(ip: String,port: Int,UDP:Boolean){
        Thread{
            try {
                var bytes:ByteArray? = null
                var outpacket:DatagramPacket? =null
                var inbyte = ByteArray(1024)
                var data = "Hello ZMT! ${System.currentTimeMillis()}"
                bytes = data.toByteArray()
                outpacket = DatagramPacket(bytes,bytes!!.size,InetSocketAddress(ip,port))
                var socket:DatagramSocket = DatagramSocket(localPort)
                var inpacket = DatagramPacket(inbyte,inbyte.size)
                socket.send(outpacket)
                Thread.sleep(500)
                socket.send(outpacket)
                while (true){
                    socket.receive(inpacket)
                    logInfo(String(inpacket.data,0,inpacket.data.size))
                    socket.send(outpacket)
                    Thread.sleep(1000)
                }
            }catch (e:Exception){
                e.printStackTrace()
            }
        }.start()
    }

    fun connectionByHole(ip:String,port:Int){
        Thread{
            val newSocket:Socket = Socket()
            newSocket.reuseAddress = true
            newSocket.bind(InetSocketAddress(InetAddress.getLocalHost().hostAddress,localPort))
            logInfo("connect to ${InetSocketAddress(ip,port)}")
            newSocket.connect(InetSocketAddress(ip,port))
            logInfo("connection success")
            val b: BufferedReader = BufferedReader(InputStreamReader(newSocket.getInputStream()))
            val p: PrintWriter = PrintWriter(OutputStreamWriter(newSocket.getOutputStream()))
            while (true){
                p.println("hello ZMT ${System.currentTimeMillis()}")
                p.flush()

                var msg:String = b.readLine()
                println(msg)
                logInfo(msg)
                Thread.sleep(2000)
            }
        }.start()
    }

    fun readStringData():String{
        line = null
        builder.delete(0,builder.length)
        while (true){
            line = reader.readLine()
            if(line!=null)
                builder.append(line)
            if(line!=null&&line!!.endsWith(END))
                break
            Thread.sleep(100)
        }
        return builder.toString()
    }

    fun logInfo(msg:String){
        println(msg)
        LogUtils.logInfo(classTag,msg)
    }
}