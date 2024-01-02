package model

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import waitForReply
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress

fun startChat(socket: DatagramSocket, address: InetAddress, port: Int) {
    val ping = "Hello".toByteArray()
    val pingPacket = DatagramPacket(ping, ping.size, address, port)
    socket.send(pingPacket)

    println("Chat started with ${address.hostAddress} ${port}")
    runBlocking {
        launch(Dispatchers.IO) { sendMessages(socket, address, port) }
        launch(Dispatchers.IO) { receiveMessages(socket, address, port) }
    }
}

fun receiveMessages(socket: DatagramSocket, address: InetAddress, port: Int) {
    while (true) {
        waitForReply(socket, address, port) { stringData, _ ->
            println(stringData)
        }
    }
}

private fun sendMessages(socket: DatagramSocket, address: InetAddress, port: Int) {
    while (true) {
        val message = readln().toByteArray()
        val messagePacket = DatagramPacket(message, message.size, address, port)
        socket.send(messagePacket)
    }
}

