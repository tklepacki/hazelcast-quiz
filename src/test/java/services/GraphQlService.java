package services;

import services.cluster.ClusterService;
import services.cluster.IClusterService;

public class GraphQlService {

    private IClusterService clusterService;

    public static GraphQlService getService() {
        return new GraphQlService();
    }

    private GraphQlService() {
        clusterService = new ClusterService();
    }

    public IClusterService getClusterService() {
        return clusterService;
    }

}


