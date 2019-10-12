package OpcUtils;

import org.opcfoundation.ua.core.CreateMonitoredItemsRequest;
import org.opcfoundation.ua.core.CreateMonitoredItemsResponse;
import org.opcfoundation.ua.core.MonitoredItemNotification;

import java.util.LinkedList;

public class MonitoredItemElement {
    private CreateMonitoredItemsResponse monitoreditem;
    private CreateMonitoredItemsRequest monitoreditem_request;
    private LinkedList<MonitoredItemNotification> readings;
    public static final int buffersize = 5;

    public MonitoredItemElement(CreateMonitoredItemsResponse monitoreditem,
                                CreateMonitoredItemsRequest monitoreditem_request) {
        this.monitoreditem = monitoreditem;
        this.monitoreditem_request = monitoreditem_request;
        readings = new LinkedList<>();
    }

    public CreateMonitoredItemsResponse getMonitoreditem() {
        return monitoreditem;
    }

    public LinkedList<MonitoredItemNotification> getReadings() {
        return readings;
    }

    public void insertNotification(MonitoredItemNotification notification) {
        if (readings.size() == buffersize)
            readings.removeLast();
        readings.addFirst(notification);
    }

    public CreateMonitoredItemsRequest getMonitoreditem_request() {
        return monitoreditem_request;
    }
}
