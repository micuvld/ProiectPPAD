package heartbeat;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class HeartBeatCleaner implements Runnable{
    public static final int HEART_BEAT_CLEANUP_DELAY = 10000;
    public Map<String, Integer> mapToClean;

    public HeartBeatCleaner(HeartBeatRequester requester) {
        this.mapToClean = requester.clientsResponses;
    }

    @Override
    public void run() {
        while(true) {
            try {
                Thread.sleep(HEART_BEAT_CLEANUP_DELAY);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            System.out.println("-------------");
            System.out.println("Cleaning the clients");
            System.out.println("-------------");
            List<String> nodesToRemove = mapToClean.entrySet().stream()
                    .filter(entry -> entry.getValue() == 0)
                    .map(Map.Entry::getKey)
                    .collect(Collectors.toList());

            nodesToRemove.forEach(mapToClean::remove);
            mapToClean.keySet().forEach(key -> {
                mapToClean.compute(key, (theKey, value) -> 0);
            });
        }
    }
}
