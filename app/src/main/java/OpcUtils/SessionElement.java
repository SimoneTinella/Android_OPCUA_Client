package OpcUtils;

import org.opcfoundation.ua.application.SessionChannel;
import org.opcfoundation.ua.common.ServiceResultException;
import org.opcfoundation.ua.core.CreateSubscriptionRequest;
import org.opcfoundation.ua.core.CreateSubscriptionResponse;

import java.util.ArrayList;
import java.util.List;

import OpcUtils.ConnectionThread.ThreadPublish;

public class SessionElement {
    private SessionChannel session;
    private String url;
    private List<SubscriptionElement> subscriptions;

    private boolean running = false;
    private ThreadPublish thread = null;

    public SessionElement(SessionChannel session, String url) {
        this.session = session;
        this.url = url;
        subscriptions = new ArrayList<>();
    }

    public int CreateSubscription(CreateSubscriptionRequest request) throws ServiceResultException {
        CreateSubscriptionResponse response = session.CreateSubscription(request);

        if (thread != null) {
            try {
                stopRunning();
                thread.join();
                subscriptions.add(new SubscriptionElement(response, this.session));
                startRunning();
                thread = new ThreadPublish(this);
                thread.start();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } else {
            subscriptions.add(new SubscriptionElement(response, this.session));
            startRunning();
            thread = new ThreadPublish(this);
            thread.start();
        }

        return subscriptions.size() - 1;
    }

    public synchronized void stopRunning() {
        running = false;
    }

    public synchronized void startRunning() {
        running = true;
    }

    public synchronized boolean isRunning() {
        return running;
    }

    public SessionChannel getSession() {
        return session;
    }

    public String getUrl() {
        return url;
    }

    public List<SubscriptionElement> getSubscriptions() {
        return subscriptions;
    }
}