package services.cluster;

import com.squareup.okhttp.*;
import helpers.ConfigPropertiesReader;
import services.cluster.vo.Cluster;

import java.io.IOException;

public class ClusterService implements IClusterService {

    private static final String CLOUD_ENDPOINT = ConfigPropertiesReader.getCloudEndpoint();
    private static final String BEARER_TOKEN = ConfigPropertiesReader.getBearerToken();

    OkHttpClient client = new OkHttpClient();
    MediaType mediaType = MediaType.parse("application/json");

    public Response createStandardCluster(Cluster cluster) {
        RequestBody body = RequestBody.create(mediaType,
                "{\"query\":\"mutation { createStarterCluster( input: { " +
                        "name: \\\"" + cluster.getName() + "\\\" " +
                        "cloudProvider: \\\"" + cluster.getCloudProvider() + "\\\" " +
                        "region: \\\"" + cluster.getRegion() + "\\\" " +
                        "clusterType: " + cluster.getClusterType() + " " +
                        "totalMemory: " + cluster.getTotalMemory() + " " +
                        "hazelcastVersion: \\\"" + cluster.getHazelcastVersion() + "\\\" } ) { id }}\",\"variables\":{}}");
        Request request = new Request.Builder()
                .url(CLOUD_ENDPOINT)
                .method("POST", body)
                .addHeader("Authorization", BEARER_TOKEN)
                .addHeader("Content-Type", "application/json")
                .build();
        Response response = null;
        try {
            response = client.newCall(request).execute();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return response;
    }

    public Response getClusterList(String productType) {
        RequestBody body = RequestBody.create(mediaType, "{\"query\":\"query {  clusters(productType:\\\"" + productType + "\\\") {    id    name    password    port    hazelcastVersion    isAutoScalingEnabled    isHotBackupEnabled    isHotRestartEnabled    isIpWhitelistEnabled    isTlsEnabled    productType {      name      isFree    }    state    createdAt    startedAt    stoppedAt    discoveryTokens {source,token}  }}\",\"variables\":{}}");
        Request request = new Request.Builder()
                .url(CLOUD_ENDPOINT)
                .method("POST", body)
                .addHeader("Authorization", BEARER_TOKEN)
                .addHeader("Content-Type", "application/json")
                .build();
        Response response = null;
        try {
            response = client.newCall(request).execute();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return response;
    }

    public void deleteCluster(String clusterId) {
        RequestBody body = RequestBody.create(mediaType, "{\"query\":\"mutation {  deleteCluster(clusterId:\\\"" + clusterId + "\\\") {    clusterId  }}\",\"variables\":{}}");
        Request request = new Request.Builder()
                .url(CLOUD_ENDPOINT)
                .method("POST", body)
                .addHeader("Authorization", BEARER_TOKEN)
                .addHeader("Content-Type", "application/json")
                .build();
        try {
            client.newCall(request).execute();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}