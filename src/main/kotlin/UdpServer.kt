import com.google.gson.Gson
import model.Command
import model.JoinRequest
import model.ListLobbiesMessage
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress

private val gson = Gson()

private val lobbies = mutableListOf<Pair<InetAddress, Int>>()

fun main() {
    DatagramSocket(8000).use { socket ->
        val buf = ByteArray(256)
        while (true) {
            val packet = DatagramPacket(buf, buf.size).also(socket::receive)

            val stringData = String(packet.data, 0, packet.length)

            if (stringData.isBlank()) continue
            val data = gson.fromJson(stringData, Command::class.java)

            println("${data.command} received by ${packet.port}")

            when (data.command) {
                Command.CommandType.Create -> onCreate(packet)
                Command.CommandType.List -> onList(socket, packet)
                Command.CommandType.Join -> onJoin(socket, data, packet)
            }
        }
    }

}

private fun onCreate(packet: DatagramPacket) {
    lobbies.addLast(packet.address to packet.port)
}

private fun onList(socket: DatagramSocket, packet: DatagramPacket) {
    val message = lobbies
        .mapIndexed { index, pair -> ListLobbiesMessage.Lobby(index, pair.first.hostAddress, pair.second) }
        .let { ListLobbiesMessage(it) }
        .let { gson.toJson(it) }
        .toByteArray()

    val responsePacket = DatagramPacket(message, message.size, packet.address, packet.port)
    socket.send(responsePacket)
}

private fun onJoin(socket: DatagramSocket, data: Command, packet: DatagramPacket) {
    val message = gson.toJson(JoinRequest(address = packet.address.hostAddress, port = packet.port)).toByteArray()
    data.lobby!!.let { lobby ->
        val joinPacket = DatagramPacket(message, message.size, lobby.addressNet, lobby.port)
        socket.send(joinPacket)
    }
}
