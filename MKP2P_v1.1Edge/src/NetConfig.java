/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */


import net.jxta.platform.NetworkConfigurator;
import net.jxta.platform.NetworkManager;
import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.URI;
import java.text.MessageFormat;
import net.jxta.exception.PeerGroupException;
import net.jxta.id.IDFactory;
import static net.jxta.impl.endpoint.router.EndpointRouterMessage.Name;
import net.jxta.peer.PeerID;
import net.jxta.peergroup.PeerGroup;
import net.jxta.peergroup.PeerGroupID;
/**
 *
 * @author My
 */
public class NetConfig 
{
    private static NetworkManager NetManager = null;
    private static NetworkConfigurator NetConf = null;
    public static PeerGroup NetPeerGroup = null;
    private String TheSeed;
    private static final int portTCP = 1306;
    NetConfig() throws IOException, PeerGroupException
    {
        File configFile = new File(new File(".cache"),"NetConfig");
        NetworkManager.RecursiveDelete(configFile);
        NetManager = new NetworkManager(NetworkManager.ConfigMode.EDGE,"NetConfig",configFile.toURI());
        NetManager.setConfigPersistent(true);
        NetConf = NetManager.getConfigurator();
        NetConf.clearRendezvousSeeds();
        
        TheSeed = "tcp://" +"192.168.1.3" /*InetAddress.getLocalHost().getHostAddress()*/ + ":" +1993; 
        NetConf.addSeedRendezvous(URI.create(TheSeed));
        // Configuration
            NetConf.setTcpPort(portTCP);
            NetConf.setTcpEnabled(true);
            NetConf.setTcpIncoming(true);
            NetConf.setTcpOutgoing(true);
            //NetConf.setPeerID(IDFactory.newPeerID(PeerGroupID.defaultNetPeerGroupID));
        if(!NetConf.exists())
        {
            /* String NewName = "MK @ " + System.currentTimeMillis();
            NetConf.setName(NewName);
            NetConf.save();*/
        }
     
        
    }
    public void StartNet() throws PeerGroupException, IOException
    {
        NetManager.startNetwork();
        NetPeerGroup = NetManager.getNetPeerGroup();
        NetPeerGroup.getRendezVousService().setAutoStart(false);
        boolean connected = NetManager.waitForRendezvousConnection(25000);
        System.out.println("Connected : "+connected);
    }
    public void StopNet()
    {
        NetManager.stopNetwork();
    }
        
}

