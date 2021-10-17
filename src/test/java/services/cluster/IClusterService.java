package services.cluster;

import com.squareup.okhttp.Response;
import services.cluster.vo.Cluster;

public interface IClusterService {

    Response createStandardCluster(Cluster cluster);

    Response getClusterList(String productType);

    void deleteCluster(String clusterId);

}