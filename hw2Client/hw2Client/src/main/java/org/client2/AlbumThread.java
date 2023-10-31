package org.client2;

import io.swagger.client.ApiClient;
import io.swagger.client.ApiException;
import io.swagger.client.ApiResponse;
import io.swagger.client.api.DefaultApi;
import io.swagger.client.model.AlbumInfo;
import io.swagger.client.model.AlbumsProfile;
import io.swagger.client.model.ImageMetaData;

import java.io.File;
import java.util.concurrent.CountDownLatch;

public class AlbumThread extends Thread {
    private static final int MAX_RETRIES = 5;
    private final Integer numberOfRequests;
    private final CountDownLatch doneSignal;
    private final String serverIP;

    public AlbumThread(Integer numberOfRequests, CountDownLatch doneSignal, String serverIP) {
        this.numberOfRequests = numberOfRequests;
        this.doneSignal = doneSignal;
        this.serverIP = serverIP;
    }

    @Override
    public void run() {
        DefaultApi api = new DefaultApi();
        ApiClient client = api.getApiClient();
        AlbumsProfile albumsProfile = new AlbumsProfile().artist("www").year("1988").title("huhu");
        File image = new File("/Users/zoelee/Documents/neu/cs6650/Assignment1/HW1Servlet/textimage.png");
        client.setBasePath(serverIP);

        for (int i = 0; i < 2 * numberOfRequests; i++) {
            boolean postSuccess = false;
            boolean getSuccess = false;

            int postRetries = 1;
            int getRetries = 1;
            long PoststartTime = System.currentTimeMillis();
//            while (!postSuccess && postRetries <= MAX_RETRIES) {
//                try {
//                    // Send POST request
//                    ApiResponse<ImageMetaData> postRes = api.newAlbumWithHttpInfo(image, albumsProfile);
//                    int statusCode = postRes.getStatusCode();
//                    if (statusCode == 200 || statusCode == 201) {
//                        long PostendTime = System.currentTimeMillis();
//                        MetricsReport.recordList.add(new Record(PoststartTime, "POST",
//                                PostendTime - PoststartTime, statusCode));
//                        Client2.counter.incrementSuccessfulReq(1); // Count the POST request as successful
//                        postSuccess = true;
//                    }
//                } catch (ApiException e) {
//                    postRetries++;
//                    System.out.println("Exception when calling DefaultAPI (POST). Tried " + postRetries + " times");
//                    e.printStackTrace();
//                }
//            }

            long getStartTime = System.currentTimeMillis();
            while (!getSuccess && getRetries <= MAX_RETRIES) {
                try {
                    ApiResponse<AlbumInfo> getRes = api.getAlbumByKeyWithHttpInfo("1");
                    if (getRes.getStatusCode() == 200 || getRes.getStatusCode() == 201) {
                        long getEndTime = System.currentTimeMillis();
                        MetricsReport.recordList.add(new Record(getStartTime, "GET",
                                getEndTime - getStartTime, getRes.getStatusCode()));
                        Client2.counter.incrementSuccessfulReq(1); // Count the GET request as successful
                        getSuccess = true;
                    }
                } catch (ApiException e) {
                    getRetries++;
                    System.out.println("Exception when calling DefaultAPI (GET). Tried " + getRetries + " times");
                    e.printStackTrace();
                }
            }
            // If either POST or GET requests fail all retry attempts, count it as a failed request
            if (!postSuccess) {
                Client2.counter.incrementFailedReq(1); // Count POST failure as a failed request
            }

            if (!getSuccess) {
                Client2.counter.incrementFailedReq(1); // Count GET failure as a failed request
            }
        }

        doneSignal.countDown(); //signal that this thread has completed
    }
}