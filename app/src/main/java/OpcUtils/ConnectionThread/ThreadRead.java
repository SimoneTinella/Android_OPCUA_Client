package OpcUtils.ConnectionThread;

import android.os.Handler;
import android.os.Message;

import org.opcfoundation.ua.application.SessionChannel;
import org.opcfoundation.ua.builtintypes.NodeId;
import org.opcfoundation.ua.builtintypes.UnsignedInteger;
import org.opcfoundation.ua.common.ServiceResultException;
import org.opcfoundation.ua.core.ReadResponse;
import org.opcfoundation.ua.core.ReadValueId;
import org.opcfoundation.ua.core.TimestampsToReturn;

public class ThreadRead extends Thread {

    private Handler handler;
    private double MaxAge;
    private TimestampsToReturn timestamp;
    private int namespace;
    private int nodeId;
    private String nodeId_String;
    private UnsignedInteger attribute;
    private SessionChannel session;
    private boolean sent =false;

    public ThreadRead(SessionChannel session,double MaxAge, TimestampsToReturn timestamp, int namespace, int nodeId, UnsignedInteger attribute){
        this.MaxAge=MaxAge;
        this.timestamp=timestamp;
        this.namespace=namespace;
        this.nodeId=nodeId;
        this.attribute=attribute;
        this.session=session;
        nodeId_String=null;
    }
    public ThreadRead(SessionChannel session,double MaxAge, TimestampsToReturn timestamp, int namespace, String nodeId, UnsignedInteger attribute){
        this.MaxAge=MaxAge;
        this.timestamp=timestamp;
        this.namespace=namespace;
        this.nodeId_String=nodeId;
        this.attribute=attribute;
        this.session=session;
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
                        ReadResponse res;
                        if(nodeId_String==null)
                            res = session.Read(null, MaxAge, timestamp,
                                    new ReadValueId(new NodeId(namespace, nodeId), attribute,null, null));
                        else
                            res = session.Read(null, MaxAge, timestamp,
                                    new ReadValueId(new NodeId(namespace, nodeId_String), attribute,null, null));

                        send(handler.obtainMessage(0,res));
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
