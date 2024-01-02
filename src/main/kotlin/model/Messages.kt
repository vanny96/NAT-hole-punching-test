package model

import java.net.InetAddress

data class Command(val command: CommandType, val lobby: ListLobbiesMessage.Lobby? = null) {
    enum class CommandType {
        Create, List, Join
    }
}

data class ListLobbiesMessage(val lobbies: List<Lobby>) {
    data class Lobby(val number: Int, val address: String, val port: Int) {
        val addressNet = InetAddress.getByName(address)

        override fun toString(): String {
            return "$number) address='$address', port=$port"
        }
    }

    override fun toString(): String {
        return lobbies.joinToString("\n") { it.toString() } + "\n"
    }
}

data class JoinRequest(val address: String, val port: Int) {
    val addressNet = InetAddress.getByName(address)
}