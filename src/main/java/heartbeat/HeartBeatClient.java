package heartbeat;

import java.io.IOException;
import java.net.*;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class HeartBeatClient implements Runnable {
    private InetAddress ip;
    private int port;
    private String myUUID;

    public HeartBeatClient(String ip, int port) throws UnknownHostException {
        this.ip = InetAddress.getByName(ip);
        this.port = port;
        this.myUUID = UUID.randomUUID().toString();
    }

    @Override
    public void run() {
        while (true) {
            MulticastSocket socket = initSocket();
            if (socket == null) {
                return;
            }

            handleHeartBeatPacket(socket);
        }
    }

    private void handleHeartBeatPacket(DatagramSocket socket) {
        DatagramPacket packet = new DatagramPacket(
                CommonConstants.HEART_BEAT_REQUEST.getBytes(), CommonConstants.HEART_BEAT_REQUEST.length());

        String messageToSend = String.format("%s#%s", CommonConstants.HEART_BEAT_RESPONSE, myUUID);
        DatagramPacket packetToSend = new DatagramPacket(
                messageToSend.getBytes(), messageToSend.length(),
                ip, port);

        try {
            socket.receive(packet);

            String message = new String(packet.getData(), packet.getOffset(), packet.getLength());
            if (message.equals(CommonConstants.HEART_BEAT_REQUEST)) {
                socket.send(packetToSend);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private MulticastSocket initSocket() {
        try {
            MulticastSocket socket = new MulticastSocket(port);
            socket.joinGroup(ip);
            return socket;
        } catch (IOException e) {
            System.out.println("Failed to bind socket.");
            e.printStackTrace();
            return null;
        }
    }
}
