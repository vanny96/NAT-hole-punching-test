import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress

inline fun <T> waitForReply(
    socket: DatagramSocket,
    address: InetAddress,
    port: Int,
    callback: (String, DatagramPacket) -> T
): T {
    val buf = ByteArray(256)

    var stringData = ""
    val packet = DatagramPacket(buf, buf.size)
    do {
        socket.receive(packet)
        if (packet.address != address) continue
        if (packet.port != port) continue

        stringData = String(packet.data, 0, packet.length)

    } while (stringData.isBlank())

    return callback(stringData, packet)
}
