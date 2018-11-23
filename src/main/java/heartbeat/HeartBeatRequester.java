package heartbeat;

import java.io.IOException;
import java.net.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class HeartBeatRequester implements Runnable {
    public static final int HEART_BEAT_TIMEOUT = 1000;
    public static final int HEART_BEAT_DELAY = 3000;
    private static final int UUID_LENGTH = 36;
    private static final int SEPARATOR_LENGTH = 1;

    private InetAddress ip;
    private int port;
    public Map<String, Integer> clientsResponses;
    private byte[] buf = new byte[256];

    public HeartBeatRequester(String ip, int port) throws UnknownHostException {
        this.ip = InetAddress.getByName(ip);
        this.port = port;
        this.clientsResponses = new ConcurrentHashMap<>();
    }

    @Override
    public void run() {
        while(true) {
            MulticastSocket socket = initSocket();
            if (socket == null) {
                return;
            }

            sendHeartbeatRequest(socket);
            receiveReplies(socket);

            waitForNextRound();
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

    private void sendHeartbeatRequest(DatagramSocket socket) {
        DatagramPacket datagramPacket = new DatagramPacket(
                CommonConstants.HEART_BEAT_REQUEST.getBytes(), CommonConstants.HEART_BEAT_REQUEST.length(),
                ip, port);

        try {
            if (socket != null) {
                socket.send(datagramPacket);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void incrementOrInsert(String hostAddress) {
        if (clientsResponses.containsKey(hostAddress)) {
            clientsResponses.put(hostAddress, clientsResponses.get(hostAddress) + 1);
        } else {
            clientsResponses.put(hostAddress, 1);
        }
    }

    private void receiveReplies(DatagramSocket socket) {
        boolean timedOut = false;
        int messageLength = CommonConstants.HEART_BEAT_RESPONSE.length() + UUID_LENGTH + SEPARATOR_LENGTH;
        DatagramPacket packet = new DatagramPacket(buf, buf.length);
        while(!timedOut) {
            try {
                socket.setSoTimeout(HEART_BEAT_TIMEOUT);
                socket.receive(packet);

                String message = new String(packet.getData(), packet.getOffset(), messageLength);
                String[] messageParts = message.split("#");
                if (messageParts[0].equals(CommonConstants.HEART_BEAT_RESPONSE)) {
                    String peerUUID = messageParts[1];
                    incrementOrInsert(peerUUID);
                }
            } catch (SocketTimeoutException e) {
                System.out.println("End of heartbeat round; clients: ");
                printClients();
                timedOut = true;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void waitForNextRound() {
        try {
            Thread.sleep(HEART_BEAT_DELAY);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void printClients() {
        System.out.println("Client\t\t\t\t\t\tReplies");
        clientsResponses.entrySet().forEach(entry -> {
            System.out.println(entry.getKey() + "\t\t" + entry.getValue());
        });
    }
}
