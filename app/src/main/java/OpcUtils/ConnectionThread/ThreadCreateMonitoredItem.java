package OpcUtils.ConnectionThread;

import android.os.Handler;
import android.os.Message;

import org.opcfoundation.ua.common.ServiceResultException;
import org.opcfoundation.ua.core.CreateMonitoredItemsRequest;

import OpcUtils.MonItemNotCreatedException;
import OpcUtils.SubscriptionElement;

public class ThreadCreateMonitoredItem extends Thread {

    private Handler handler;
    private SubscriptionElement subElement;
    private CreateMonitoredItemsRequest request;
    private int position =-1;
    private boolean sent =false;

    public ThreadCreateMonitoredItem(SubscriptionElement subElement, CreateMonitoredItemsRequest request){
        this.subElement=subElement;
        this.request=request;
    }

    private synchronized void send(Message msg){
        if(!sent) {
            msg.sendToTarget();
            sent =true;
        }
    }

    public void start(Handler handler) {
        super.start();
        this.handler=handler;
    }

    @Override
    public void run() {
        super.run();
        try {
            Thread t= new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        position =subElement.CreateMonitoredItem(request);
                        send(handler.obtainMessage(0, position));
                    } catch (ServiceResultException e) {
                        send(handler.obtainMessage(-1,e.getStatusCode()));
                    } catch (MonItemNotCreatedException e) {
                        send(handler.obtainMessage(-3,e.getMessage()));
                    }
                }
            });
            t.start();
            t.join(8000);
            send(handler.obtainMessage(-2));
        } catch (InterruptedException e) {
            send(handler.obtainMessage(-2));

        }

    }
}
