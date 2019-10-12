package OpcUtils.ConnectionThread;

import android.os.Handler;
import android.os.Message;

import org.opcfoundation.ua.common.ServiceResultException;
import org.opcfoundation.ua.core.BrowseResponse;

import OpcUtils.ManagerOPC;

public class ThreadBrowse extends Thread {

    private Handler handler;
    private int session_position;
    private int position;
    private boolean sent = false;

    public ThreadBrowse(int session_position, int position) {
        this.session_position = session_position;
        this.position = position;
    }

    private synchronized void send(Message msg) {
        if (!sent) {
            msg.sendToTarget();
            sent = true;
        }
    }

    public void start(Handler handler) {
        super.start();
        this.handler = handler;
    }

    @Override
    public void run() {
        super.run();
        try {
            Thread t = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        BrowseResponse res;
                        res = ManagerOPC.getIstance().browseOperation(position, session_position);
                        send(handler.obtainMessage(0, res));
                    } catch (ServiceResultException e) {
                        send(handler.obtainMessage(-1, e.getStatusCode()));
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
