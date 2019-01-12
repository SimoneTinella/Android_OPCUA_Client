package OpcUtils;

public class BrowseDataStamp {
    public String name;
    public String namespace;
    public String nodeindex;
    public String nodeclass;

    public BrowseDataStamp(String name, String namespace, String nodeindex, String nodeclass) {
        this.name = name;
        this.namespace = namespace;
        this.nodeindex = nodeindex;
        this.nodeclass = nodeclass;
    }
}
