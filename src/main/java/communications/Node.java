package communications;

import heartbeat.CommonConstants;
import heartbeat.HeartBeatCleaner;
import heartbeat.HeartBeatClient;
import heartbeat.HeartBeatRequester;

import java.net.UnknownHostException;

public class Node {
    public static void main(String[] args) throws UnknownHostException {
        HeartBeatRequester heartBeatRequester = new HeartBeatRequester(
                CommonConstants.HEART_BEAT_GROUP, CommonConstants.HEART_BEAT_PORT);
        new Thread(heartBeatRequester).start();
        new Thread(new HeartBeatCleaner(heartBeatRequester)).start();
        new Thread(new HeartBeatClient(CommonConstants.HEART_BEAT_GROUP, CommonConstants.HEART_BEAT_PORT)).start();
    }
}
