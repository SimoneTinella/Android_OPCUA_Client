package OpcUtils.ConnectionThread;


import org.opcfoundation.ua.builtintypes.ExtensionObject;
import org.opcfoundation.ua.common.ServiceResultException;
import org.opcfoundation.ua.core.DataChangeNotification;
import org.opcfoundation.ua.core.MonitoredItemNotification;
import org.opcfoundation.ua.core.NotificationMessage;
import org.opcfoundation.ua.core.PublishResponse;
import org.opcfoundation.ua.core.SubscriptionAcknowledgement;

import OpcUtils.ManagerOPC;
import OpcUtils.SessionElement;

public class ThreadPublish extends Thread {

    private SessionElement sessionElement;
    public ThreadPublish(SessionElement sessionElement) {
        this.sessionElement = sessionElement;
    }

    @Override
    public void run() {
        super.run();

        while(sessionElement.isRunning()){
            if(sessionElement.getSubscriptions().size()>0){
                PublishResponse publishResponse;
                try {
                    SubscriptionAcknowledgement[] subacks= new SubscriptionAcknowledgement[sessionElement.getSubscriptions().size()];
                    for(int i=0;i<sessionElement.getSubscriptions().size();i++){
                        subacks[i]=sessionElement.getSubscriptions().get(i).getSubAck();
                    }
                    publishResponse = sessionElement.getSession().Publish(null, subacks);
                    for(int i=0;i<sessionElement.getSubscriptions().size();i++){
                        if(sessionElement.getSubscriptions().get(i).getSubscription().getSubscriptionId().getValue()==publishResponse.getSubscriptionId().getValue()){
                            sessionElement.getSubscriptions().get(i).setLastSeqNumber(publishResponse.getNotificationMessage().getSequenceNumber());
                            NotificationMessage nm = publishResponse.getNotificationMessage();
                            ExtensionObject[] ex = nm.getNotificationData();
                            for (ExtensionObject ob : ex) {
                                Object change = ob.decode(ManagerOPC.getIstance().getClient().getEncoderContext());
                                if (change instanceof DataChangeNotification) {
                                    DataChangeNotification dataChange = (DataChangeNotification) change;
                                    MonitoredItemNotification[] mnchange = dataChange.getMonitoredItems();
                                    for (MonitoredItemNotification monitoredItemNotification : mnchange) {
                                        for (int j = 0; j < sessionElement.getSubscriptions().get(i).getMonitoreditems().size(); j++) {
                                            if (monitoredItemNotification.getClientHandle().intValue() == sessionElement.getSubscriptions().get(i).getMonitoreditems()
                                                    .get(j).getMonitoreditem_request().getItemsToCreate()[0]
                                                    .getRequestedParameters().getClientHandle().intValue()) {
                                                sessionElement.getSubscriptions().get(i).getMonitoreditems().get(j).insertNotification(monitoredItemNotification);
                                                break;
                                            }
                                        }
                                    }
                                }
                            }

                            break;
                        }
                    }
                } catch (ServiceResultException e) {}
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}