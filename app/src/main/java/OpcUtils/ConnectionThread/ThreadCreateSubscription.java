package OpcUtils.ConnectionThread;

import android.os.Handler;
import android.os.Message;

import org.opcfoundation.ua.common.ServiceResultException;
import org.opcfoundation.ua.core.CreateSubscriptionRequest;

import OpcUtils.SessionElement;

public class ThreadCreateSubscription extends Thread {

    private Handler handler;
    private SessionElement session;
    private CreateSubscriptionRequest request;
    private int position =-1;
    private boolean sent =false;

    public ThreadCreateSubscription(SessionElement session, CreateSubscriptionRequest request){
        this.session=session;
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
                        position = session.CreateSubscription(request);
                        send(handler.obtainMessage(0, position));
                    } catch (ServiceResultException e) {
                        send(handler.obtainMessage(-1,e.getStatusCode()));
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
