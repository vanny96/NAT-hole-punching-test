import com.google.gson.Gson
import kotlinx.coroutines.runBlocking
import model.Command
import model.JoinRequest
import model.ListLobbiesMessage
import model.startChat
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress

private val gson = Gson()

fun main() {
    println("Enter server address")
    val serverAddress = InetAddress.getByName(readln())
    println("Enter server port")
    val serverPort = readln().toInt()

    DatagramSocket().use { socket ->
        mainSelect(socket, serverAddress, serverPort)
    }
}

fun mainSelect(socket: DatagramSocket, serverAddress: InetAddress, serverPort: Int) {
    println(
        """
        0) Exit
        1) Create Lobby
        2) Join Lobby
    """.trimIndent()
    )

    when (readlnOrNull()) {
        "0" -> return
        "1" -> create(socket, serverAddress, serverPort)
        "2" -> join(socket, serverAddress, serverPort)
    }
}

private fun create(socket: DatagramSocket, serverAddress: InetAddress, serverPort: Int) {
    val message = gson.toJson(Command(command = Command.CommandType.Create)).toByteArray()
    val packet = DatagramPacket(message, message.size, serverAddress, serverPort)
    socket.send(packet)

    println("Waiting for users to join")
    val joinRequest = waitForReply(socket, serverAddress, serverPort) { stringData, packet ->
        gson.fromJson(stringData, JoinRequest::class.java)
    }

    startChat(socket, joinRequest.addressNet, joinRequest.port)
}

private fun join(socket: DatagramSocket, serverAddress: InetAddress, serverPort: Int) {
    val lobbyMessage = list(socket, serverAddress, serverPort)
    println(lobbyMessage)

    println("Which lobby do you want to join?")
    val lobbyId = readln().toInt()
    val lobby = lobbyMessage.lobbies[lobbyId]

    val message = gson.toJson(Command(command = Command.CommandType.Join, lobby = lobby)).toByteArray()
    val joinPacket = DatagramPacket(message, message.size, serverAddress, serverPort)
    socket.send(joinPacket)

    startChat(socket, lobby.addressNet, lobby.port)
}


private fun list(socket: DatagramSocket, serverAddress: InetAddress, serverPort: Int): ListLobbiesMessage {
    fun sendListLobbiesRequest(serverAddress: InetAddress, serverPort: Int, socket: DatagramSocket) {
        val message = gson.toJson(Command(command = Command.CommandType.List)).toByteArray()
        val packet = DatagramPacket(message, message.size, serverAddress, serverPort)
        socket.send(packet)
    }

    println("Listing lobbies...\n")
    sendListLobbiesRequest(serverAddress, serverPort, socket)
    return waitForReply(socket, serverAddress, serverPort) { data, _ ->
        gson.fromJson(data, ListLobbiesMessage::class.java)
    }
}
