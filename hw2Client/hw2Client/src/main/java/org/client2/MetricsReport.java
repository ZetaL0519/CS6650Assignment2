package org.client2;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MetricsReport {
    private FileWriter writer;
    static final List<Record> recordList =  Collections.synchronizedList(new ArrayList<>());

    public MetricsReport(String filePath) throws IOException {
        try (FileWriter writer = new FileWriter(filePath)) {
            writer.write("startTime,requestType,latency,responseCode\n");
            for (Record record : recordList) {
                writer.append(record.toString());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void calculateMetrics() {
        List<Record> postRecords = new ArrayList<>();
        List<Record> getRecords = new ArrayList<>();

        for (Record record : recordList) {
            if ("POST".equals(record.getRequestType())) {
                postRecords.add(record);
            } else if ("GET".equals(record.getRequestType())) {
                getRecords.add(record);
            }
        }

        calculateAndPrintMetrics("POST", postRecords);
        calculateAndPrintMetrics("GET", getRecords);
    }

    private void calculateAndPrintMetrics(String requestType, List<Record> requestRecords) {
        if (requestRecords.isEmpty()) {
            System.out.println("No " + requestType + " records.");
            return;
        }

        // Calculate mean, median, p99, min, and max response times
        List<Long> latencies = new ArrayList<>();
        for (Record record : requestRecords) {
            latencies.add(record.getLatency());
        }

        Collections.sort(latencies);
        long min = latencies.get(0);
        long max = latencies.get(latencies.size() - 1);
        long mean = latencies.stream().mapToLong(Long::longValue).sum() / latencies.size();
        long median = latencies.get(latencies.size() / 2);
        int p99Index = (int) (0.99 * latencies.size());
        long p99 = latencies.get(p99Index);

        System.out.println(requestType + " Metrics:");
        System.out.println("Mean Response Time: " + mean + " millisecs");
        System.out.println("Median Response Time: " + median + " millisecs");
        System.out.println("P99 Response Time: " + p99 + " millisecs");
        System.out.println("Min Response Time: " + min + " millisecs");
        System.out.println("Max Response Time: " + max + " millisecs");
    }
}
