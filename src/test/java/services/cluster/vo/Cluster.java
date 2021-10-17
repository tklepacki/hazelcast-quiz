package services.cluster.vo;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Cluster {
    private String name;
    private String cloudProvider;
    private String region;
    private String clusterType;
    private String totalMemory;
    private String hazelcastVersion;
}