/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */


import net.jxta.platform.NetworkConfigurator;
import net.jxta.platform.NetworkManager;
import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;
import net.jxta.exception.PeerGroupException;
import net.jxta.peergroup.PeerGroup;
/**
 *
 * @author My
 */
public class NetConfig 
{
    private static NetworkManager NetManager = null;
    private static NetworkConfigurator NetConf = null;
    public static PeerGroup NetPeerGroup = null;
    private static final int portTCP = 1993;
    NetConfig() throws IOException, PeerGroupException
    {
        File configFile = new File(new File(".cache"),"NetConfig");
        NetworkManager.RecursiveDelete(configFile);
        NetManager = new NetworkManager(NetworkManager.ConfigMode.SUPER,"NetConfig",configFile.toURI());
        //NetManager.setConfigPersistent(true);
        //NetManager.setUseDefaultSeeds(true);
        NetConf = NetManager.getConfigurator();
        // Configuration
            NetConf.setTcpPort(portTCP);
            NetConf.setTcpEnabled(true);
            NetConf.setTcpIncoming(true);
            NetConf.setTcpOutgoing(true);
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
        //boolean connected = NetManager.waitForRendezvousConnection(25000);
        //System.out.println(MessageFormat.format("Connected :{0}", connected));
    }
    public void StopNet()
    {
        NetManager.stopNetwork();
    }
        
}

