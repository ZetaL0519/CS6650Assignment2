package org.client2;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class Client2 {
    protected static Counter counter = new Counter();

    public static void main(String[] args) throws InterruptedException, IOException {
        if (args.length != 4) {
            System.out.println("Usage: java LoadTestOrchestrator <threadGroupSize> <numThreadGroups> <delay> <serverIP>");
            System.exit(1);
        }

        int threadGroupSize = Integer.parseInt(args[0]);
        int numThreadGroups = Integer.parseInt(args[1]);
        int delay = Integer.parseInt(args[2]);
        String serverIP = args[3];

        long start = System.currentTimeMillis();
        execute(1000, numThreadGroups, threadGroupSize, serverIP, delay);
        long end = System.currentTimeMillis();
        long wallTime = end - start;
        int success = counter.getSuccessfulReq();
        int failed = counter.getFailedReq();
        long throughPut = 1000L * (success + failed) / wallTime;
        System.out.println("\nClient Part 2 Result:");
        System.out.println("-----------------------------------------------");
        System.out.println("Number of successful requests sent: " + success);
        System.out.println("Number of unsuccessful requests: " + failed);
        System.out.println("The total run time(wall time): " + wallTime + " milliseconds");
        System.out.println("The total throughput per Sec: " + throughPut);

        StringBuilder metricsFilePath = new StringBuilder("metrics");
        metricsFilePath.append("-");
        if (serverIP.contains("Music")) {
            metricsFilePath.append("java");
            metricsFilePath.append("-");
        } else {
            metricsFilePath.append("go");
            metricsFilePath.append("-");
        }
        metricsFilePath.append(threadGroupSize);
        metricsFilePath.append("-");
        metricsFilePath.append(numThreadGroups);
        metricsFilePath.append("-");
        metricsFilePath.append(delay);
        metricsFilePath.append(".csv");

        MetricsReport report = new MetricsReport(metricsFilePath.toString());
        report.calculateMetrics();
    }

    private static void execute(int ApiCallCount, int numThreadGroups,
                                int threadGroupSize, String serverIP, long delay)
            throws InterruptedException {
        CountDownLatch doneSignal = new CountDownLatch(numThreadGroups * threadGroupSize);
        for (int i = 0; i < numThreadGroups; i++) {
            for (int j = 0; j < threadGroupSize; j++) {
                AlbumThread thread = new AlbumThread(ApiCallCount, doneSignal, serverIP);
                thread.start();
            }

            if (i < numThreadGroups - 1 && delay > 0) {
                TimeUnit.SECONDS.sleep(delay);
            }
        }
        doneSignal.await(); // Wait for all threads to complete
    }
}
