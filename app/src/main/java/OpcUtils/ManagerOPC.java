package OpcUtils;

import org.apache.http.conn.ssl.SSLSocketFactory;
import org.opcfoundation.ua.application.Application;
import org.opcfoundation.ua.application.Client;
import org.opcfoundation.ua.application.SessionChannel;
import org.opcfoundation.ua.builtintypes.ByteString;
import org.opcfoundation.ua.builtintypes.NodeId;
import org.opcfoundation.ua.builtintypes.UnsignedByte;
import org.opcfoundation.ua.builtintypes.UnsignedInteger;
import org.opcfoundation.ua.common.ServiceResultException;
import org.opcfoundation.ua.core.BrowseDescription;
import org.opcfoundation.ua.core.BrowseDirection;
import org.opcfoundation.ua.core.BrowseResponse;
import org.opcfoundation.ua.core.BrowseResultMask;
import org.opcfoundation.ua.core.EndpointDescription;
import org.opcfoundation.ua.core.Identifiers;
import org.opcfoundation.ua.core.NodeClass;
import org.opcfoundation.ua.transport.security.Cert;
import org.opcfoundation.ua.transport.security.CertificateValidator;
import org.opcfoundation.ua.transport.security.KeyPair;
import org.opcfoundation.ua.transport.security.PrivKey;
import org.opcfoundation.ua.utils.CertificateUtils;

import java.io.File;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import java.util.UUID;

public class ManagerOPC {

    //Default parameters subscription creation
    public static UnsignedByte Default_Priority = new UnsignedByte(0);
    public static Double Default_RequestedPublishingInterval = 1000d;
    public static UnsignedInteger Default_RequestedMaxKeepAliveCount = new UnsignedInteger(20);
    public static UnsignedInteger Default_RequestedLifetimeCount = new UnsignedInteger(60);
    public static UnsignedInteger Default_MaxNotificationsPerPublish = new UnsignedInteger(0);

    //Default parameters monitored item creation
    public static UnsignedInteger Default_QueueSize = new UnsignedInteger(4);
    public static Double Default_AbsoluteDeadBand = 1d;
    public static Double Default_SamplingInterval = 1000d;

    static private Client client;
    private static ManagerOPC istance = null;
    private List<SessionElement> sessions;

    private List<NodeId> basicnodes;
    private Stack<List<NodeId>> stack;

    private static String Key = "xftghbxcvjkxhvbjnkdfgvjcfgvsyujgtxyudfgywiagweyruigxcukvnbduihfguieyrshbgfoiwbhfioqhuifpquhdivgianioahwdioqgifopabnpiv3287468976781ryui1gh843858934h5hfg98gr41734105g14fjrs6tjwt78";
    private static Application myClientApplication;
    private static KeyPair keys;

    private ManagerOPC() {
        basicnodes = new ArrayList<>();
        basicnodes.add(Identifiers.RootFolder);
        basicnodes.add(Identifiers.ObjectsFolder);
        basicnodes.add(Identifiers.ViewsFolder);
        basicnodes.add(Identifiers.TypesFolder);
        basicnodes.add(Identifiers.Server);

        initStack();

        sessions = new ArrayList<>();
    }

    public static ManagerOPC CreateManagerOPC(final File certFile, final File privKeyFile) {

        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                myClientApplication = new Application();

                try {
                    Cert myCertificate = Cert.load(certFile);
                    PrivKey myPrivateKey = PrivKey.load(privKeyFile, Key);
                    keys = new KeyPair(myCertificate, myPrivateKey);
                } catch (Exception e1) {
                    e1.printStackTrace();
                    try {
                        keys = CertificateUtils
                                .createApplicationInstanceCertificate("Android_OPC_Client", "twistedfatedeveloper",
                                        "org.twistedfatedeveloper.opclient", 3650);
                        keys.getCertificate().save(certFile);
                        keys.getPrivateKey().save(privKeyFile, Key);
                    } catch (GeneralSecurityException e) {
                        e.printStackTrace();
                        keys = null;
                    } catch (IOException e) {
                        e.printStackTrace();
                        keys = null;
                    }
                }
                myClientApplication.addApplicationInstanceCertificate(keys);
                myClientApplication.getOpctcpSettings().setCertificateValidator(CertificateValidator.ALLOW_ALL);
                myClientApplication.getHttpsSettings().setCertificateValidator(CertificateValidator.ALLOW_ALL);
                myClientApplication.getHttpsSettings().setHostnameVerifier(SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
                myClientApplication.setApplicationUri("org.twistedfatedeveloper.opclient");
                client = new Client(myClientApplication);
            }
        });
        t.start();
        try {
            t.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        if (istance == null) {
            istance = new ManagerOPC();
        }
        return istance;
    }

    public static ManagerOPC getIstance() {
        return istance;
    }

    public int CreateSession(String url, EndpointDescription endpoint) throws ServiceResultException {
        SessionChannel tmp = client.createSessionChannel(url, endpoint);
        tmp.activate();
        sessions.add(new SessionElement(tmp, url));
        return sessions.size() - 1; //new session position
    }

    public List<SessionElement> getSessions() {
        return sessions;
    }

    public Client getClient() {
        return client;
    }

    private NodeId getNode(int pos) {
        return stack.peek().get(pos);
    }

    public void initStack() {
        stack = new Stack<>();
        stack.add(basicnodes);
    }

    void pop() {
        if (stack.size() > 1)
            stack.pop();
    }

    public BrowseResponse browseOperation(int position, int session_position) throws ServiceResultException {
        BrowseDescription browse = new BrowseDescription();
        browse.setNodeId(ManagerOPC.getIstance().getNode(position));
        browse.setBrowseDirection(BrowseDirection.Forward);
        browse.setIncludeSubtypes(true);
        browse.setNodeClassMask(NodeClass.Object, NodeClass.Variable);
        browse.setResultMask(BrowseResultMask.All);

        BrowseResponse res = sessions.get(session_position).getSession()
                .Browse(null, null, null, browse);
        ArrayList<NodeId> nodes = new ArrayList<>();

        for (int i = 0; i < res.getResults().length; i++) {
            if (res.getResults()[i].getReferences() != null) {
                for (int j = 0; j < res.getResults()[i].getReferences().length; j++) {
                    int namespace = res.getResults()[i].getReferences()[j].getNodeId().getNamespaceIndex();
                    NodeId node;

                    Object index = res.getResults()[i].getReferences()[j].getNodeId().getValue();
                    if (index instanceof String)
                        node = new NodeId(namespace, index.toString());
                    else if (index instanceof UnsignedInteger)
                        node = new NodeId(namespace, (UnsignedInteger) index);
                    else if (index instanceof UUID)
                        node = new NodeId(namespace, (UUID) index);
                    else if (index instanceof byte[])
                        node = new NodeId(namespace, (byte[]) index);
                    else if (index instanceof ByteString)
                        node = new NodeId(namespace, (ByteString) index);
                    else
                        node = new NodeId(namespace, (int) index);

                    nodes.add(node);
                }
            }
        }

        if (nodes.size() > 0)
            stack.push(nodes);
        return res;
    }


}
