import com.squareup.okhttp.Response;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import services.GraphQlService;
import services.cluster.vo.Cluster;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import static java.util.UUID.randomUUID;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ClusterTest {

    private String randomClusterName;
    private String clusterId;

    protected final Logger log = Logger.getLogger(getClass().getName());

    @BeforeEach
    public void init() {
        randomClusterName = "Test-Cluster-" + randomUUID();
    }

    @Test
    void createAndDeleteClusterTest() throws Exception {

        Cluster cluster = new Cluster();
        cluster.setName(randomClusterName);
        cluster.setCloudProvider("aws");
        cluster.setRegion("eu-west-2");
        cluster.setClusterType("SMALL");
        cluster.setTotalMemory("2");
        cluster.setHazelcastVersion("4.2");

        Response createClusterResponse = GraphQlService.getService().getClusterService().createStandardCluster(cluster);
        if (!createClusterResponse.isSuccessful()) {
            throw new Exception("Creating Standard Cluster request ended up with failed status: " + createClusterResponse.code());
        }

        JSONObject createClusterResponseObject = new JSONObject(createClusterResponse.body().string());
        clusterId = createClusterResponseObject.getJSONObject("data").getJSONObject("createStarterCluster").get("id").toString();

        Response getClusterListResponse = GraphQlService.getService().getClusterService().getClusterList("STANDARD");

        JSONObject getClusterListObject = new JSONObject(getClusterListResponse.body().string());
        JSONArray clusterList = getClusterListObject.getJSONObject("data").getJSONArray("clusters");

        String actualClusterId = getClusterListObject.getJSONObject("data").getJSONArray("clusters").getJSONObject(clusterList.length() - 1).get("id").toString();
        String actualClusterName = getClusterListObject.getJSONObject("data").getJSONArray("clusters").getJSONObject(clusterList.length() - 1).get("name").toString();

        assertEquals(clusterId, actualClusterId, "Cluster with the following id: " + clusterId + " is not visible in the clusters list/was not created.");
        assertEquals(randomClusterName, actualClusterName, "Cluster with the following name: " + randomClusterName + " is not visible in the clusters list/was not created.");

        String clusterState = waitAndGetClusterState(360, 5000, "RUNNING", actualClusterId, "STANDARD");

        assertEquals("RUNNING", clusterState, "After Clusters creation cluster with the following id: " + clusterId + " was in Pending state for more than 360 Seconds.");

        GraphQlService.getService().getClusterService().deleteCluster(clusterId);

        boolean isClusterNotAvailableInTheList = isClusterNotAvailableInTheList(300, 2000, actualClusterId, "STANDARD");

        assertTrue(isClusterNotAvailableInTheList, "Something went wrong. Cluster with the following id: " + clusterId + " has not been deleted.");
    }

    @AfterEach
    public void cleanUp() throws IOException {
        Response getClusterListResponse = GraphQlService.getService().getClusterService().getClusterList("STANDARD");
        JSONObject getClusterListObject = new JSONObject(getClusterListResponse.body().string());
        JSONArray clusterList = getClusterListObject.getJSONObject("data").getJSONArray("clusters");
        int clusterListSize = clusterList.length();
        if (clusterListSize > 0) {
            HashMap<String, String> currentClusterMap = new HashMap<>();
            for (int i = 0; i < clusterListSize; i++) {
                String currentId = clusterList.getJSONObject(i).get("id").toString();
                String currentState = clusterList.getJSONObject(i).get("state").toString();
                currentClusterMap.put(currentId, currentState);
            }
            if (currentClusterMap.containsKey(clusterId)) {
                if (currentClusterMap.get(clusterId).equals("RUNNING")) {
                    GraphQlService.getService().getClusterService().deleteCluster(clusterId);
                }
            }
        }
    }

    private String waitAndGetClusterState(int waitTimeInSeconds, long poolingTime, String clusterState, String clusterId, String clusterType) throws IOException {
        String actualClusterState = null;
        while (System.currentTimeMillis() < System.currentTimeMillis() + waitTimeInSeconds * 1000) {
            Response getClusterListResponse = GraphQlService.getService().getClusterService().getClusterList(clusterType);
            JSONObject getClusterListObject = new JSONObject(getClusterListResponse.body().string());
            JSONArray clusterList = getClusterListObject.getJSONObject("data").getJSONArray("clusters");
            int clusterIndex = 0;
            for (int i = 0; i < clusterList.length(); i++) {
                if (clusterId.equals(clusterList.getJSONObject(i).get("id"))) {
                    clusterIndex = i;
                }
            }
            actualClusterState = getClusterListObject.getJSONObject("data").getJSONArray("clusters").getJSONObject(clusterIndex).get("state").toString();
            if (actualClusterState.equals(clusterState)) {
                log.log(Level.INFO, "Cluster with id: " + clusterId + " is in " + actualClusterState + " state.");
                break;
            } else {
                log.log(Level.INFO, "Cluster with id: " + clusterId + " is in " + actualClusterState + " state.");
                try {
                    Thread.sleep(poolingTime);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
        return actualClusterState;
    }

    private boolean isClusterNotAvailableInTheList(int waitTimeInSeconds, long poolingTime, String clusterId, String clusterType) throws IOException {
        boolean isClusterNotAvailableInTheList = false;
        while (System.currentTimeMillis() < System.currentTimeMillis() + waitTimeInSeconds * 1000) {
            Response getClusterListResponse = GraphQlService.getService().getClusterService().getClusterList(clusterType);
            JSONObject getClusterListObject = new JSONObject(getClusterListResponse.body().string());
            JSONArray clusterList = getClusterListObject.getJSONObject("data").getJSONArray("clusters");
            int clusterListSize = clusterList.length();
            if (clusterListSize == 0) {
                isClusterNotAvailableInTheList = true;
                log.log(Level.INFO, "Cluster with id: " + clusterId + " is NOT AVAILABLE in the cluster list and was permanently deleted.");
                break;
            } else if (clusterListSize > 0) {
                List<String> currentClusterList = new ArrayList<>();
                for (int i = 0; i < clusterListSize; i++) {
                    String currentId = clusterList.getJSONObject(i).get("id").toString();
                    currentClusterList.add(currentId);
                }
                if (!currentClusterList.contains(clusterId)) {
                    isClusterNotAvailableInTheList = true;
                    log.log(Level.INFO, "Cluster with id: " + clusterId + " is NOT AVAILABLE in the cluster list and was permanently deleted.");
                    break;
                } else {
                    log.log(Level.INFO, "Cluster with id: " + clusterId + " IS STILL AVAILABLE in the cluster list.");
                    try {
                        Thread.sleep(poolingTime);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        return isClusterNotAvailableInTheList;
    }
}