/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import java.util.Enumeration;
import net.jxta.discovery.*;
import net.jxta.document.Advertisement;
import net.jxta.id.ID;
import net.jxta.peergroup.PeerGroup;
import net.jxta.protocol.DiscoveryResponseMsg;
import net.jxta.protocol.PipeAdvertisement;
/**
 *
 * @author My
 */
public class DiscoveryAdv implements DiscoveryListener
{
    private final DiscoveryService DiscoverySrv;
    public final PeerGroup NetPeerGroup;
    public Enumeration en = null;
    private boolean AddInAdvList;
    
    
    DiscoveryAdv(PeerGroup NetPeerGroup,boolean AddInAdvList)
    {
        this.AddInAdvList = AddInAdvList;
        this.NetPeerGroup = NetPeerGroup;
        DiscoverySrv = NetPeerGroup.getDiscoveryService();
    }

    //pour evite que 2 advertissemnt refer au meme ressource
    //on suppreme les advirtessements ayent le meme nom et publier par le meme pair 
    public static void DeletRepInAdvList()
    {
        int j=0;
        int i =0;
        
        while(i<MKP2P.BadAdv.size())
        {
            while(j<Client.AdvList.size())
            {
                PipeAdvertisement PAdv = (PipeAdvertisement)Client.AdvList.get(j);
                if(PAdv.getID()==(ID)MKP2P.BadAdv.get(i))
                {
                    //System.out.println("DiscoDelet\n\n MK"+PAdv.getID().toString());
                    Client.AdvList.remove(j);
                    //j=Client.AdvList.size();
                }
                else
                {
                    j++;
                }
            }
            i++;
        }
        i=0;
        j=1;
        while(i<Client.AdvList.size())
        {
            while(j<Client.AdvList.size())
            {
                if(Client.AdvList.get(i).equals(Client.AdvList.get(j)))
                {
                    Client.AdvList.remove(j);
                }
                else
                j++;
            }
            i++;
        }   
        
         
    
    }
    
    //dans cette methode je supose qu'il y a un seul element dans enumeration en
    public Enumeration startDiscovery(String name) throws InterruptedException 
    {
        long waittime = 2 * 1000L;

        try {
            // Add ourselves as a DiscoveryListener for DiscoveryResponse events
            DiscoverySrv.addDiscoveryListener(this);
            try {
                    //System.out.println("DiscovryAdv : "+"Sleeping for :" + waittime+"\n"+name);
                   // Thread.sleep(waittime);
                } catch (Exception e) {
                    // ignored
                }
            /*System.out.println("Enu : mmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmm"
            +DiscoverySrv.getLocalAdvertisements(3, "Name", name).toString());
            */
                
                //System.out.println("DiscovryAdv : "+"Sending a Discovery Message");
                // look for any peer
                DiscoverySrv.getRemoteAdvertisements(
                        // sans specifier un pair (propagate)
                        null,
                        // Adv type
                        DiscoveryService.ADV,
                        // Attribute = name
                        "Name",
                        // Value = nome de streaming
                        name,
                        // nombre d'advertissement a recevoire
                        10,
                        //
                        null);
                
       
        } catch (Exception e) {
        }
        
        
        return en;
    }

 
 
    /**
     * This method is called whenever a discovery response is received, which are
     * either in response to a query we sent, or a remote publish by another node
     *
     * @param ev the discovery event
     */
    @Override
    public void discoveryEvent(DiscoveryEvent ev) 
    {
        DiscoveryResponseMsg res = ev.getResponse();

        // let's get the responding peer's advertisement
        System.out.println("DiscovryAdv : "+" [  Got a Discovery Response [" + res.getResponseCount() + " elements]  from peer : " + ev.getSource() + "  ]");
        
        PipeAdvertisement adv;
        en = res.getAdvertisements();
        adv = (PipeAdvertisement) en.nextElement();
        //System.out.println("Disco\n\n MK"+adv.getID().toString());
        if(AddInAdvList)
            Client.AdvList.add(adv);
        else
        {
            StreamingFiles.RemotVideosList.add(adv.getName().substring(5));
            while(en.hasMoreElements())
            {
                adv = (PipeAdvertisement) en.nextElement();
                StreamingFiles.RemotVideosList.add(adv.getName().substring(5));
            }
        }
            
    }

}
