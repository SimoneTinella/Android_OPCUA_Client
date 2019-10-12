package OpcUtils;


import org.opcfoundation.ua.application.SessionChannel;
import org.opcfoundation.ua.builtintypes.StatusCode;
import org.opcfoundation.ua.builtintypes.UnsignedInteger;
import org.opcfoundation.ua.common.ServiceResultException;
import org.opcfoundation.ua.core.CreateMonitoredItemsRequest;
import org.opcfoundation.ua.core.CreateMonitoredItemsResponse;
import org.opcfoundation.ua.core.CreateSubscriptionResponse;
import org.opcfoundation.ua.core.SubscriptionAcknowledgement;

import java.util.ArrayList;
import java.util.List;


public class SubscriptionElement {
    private CreateSubscriptionResponse subscription;
    private List<MonitoredItemElement> monitoreditems;
    private SessionChannel sessionChannel;

    private SubscriptionAcknowledgement subAck;

    public void setLastSeqNumber(UnsignedInteger lastSeqNumber) {
        subAck.setSequenceNumber(lastSeqNumber);
    }


    public SubscriptionAcknowledgement getSubAck() {
        return subAck;
    }

    public SubscriptionElement(CreateSubscriptionResponse subscription, SessionChannel sessionChannel) {
        this.subscription = subscription;
        this.monitoreditems = new ArrayList<>();
        this.sessionChannel = sessionChannel;

        subAck = new SubscriptionAcknowledgement();
        subAck.setSubscriptionId(new UnsignedInteger(subscription.getSubscriptionId()));

    }

    public CreateSubscriptionResponse getSubscription() {
        return subscription;
    }

    public List<MonitoredItemElement> getMonitoreditems() {
        return monitoreditems;
    }

    public SessionChannel getSession() {
        return sessionChannel;
    }

    public int CreateMonitoredItem(CreateMonitoredItemsRequest mirequest)
            throws ServiceResultException, MonItemNotCreatedException {
        CreateMonitoredItemsResponse response = sessionChannel.CreateMonitoredItems(mirequest);
        if (response.getResults()[0].getStatusCode().getValue().intValue() != StatusCode.GOOD.getValue()
                .intValue()) {
            throw new MonItemNotCreatedException(response.getResults()[0].getStatusCode()
                    .getDescription());
        }
        monitoreditems.add(new MonitoredItemElement(response, mirequest));
        return monitoreditems.size() - 1;
    }
}