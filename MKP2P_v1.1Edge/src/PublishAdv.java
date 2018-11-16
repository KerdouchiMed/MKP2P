/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */


import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.jxta.protocol.PipeAdvertisement;
import net.jxta.document.AdvertisementFactory;
import net.jxta.id.IDFactory;
import net.jxta.peergroup.PeerGroup;
import net.jxta.peergroup.PeerGroupID;
import net.jxta.pipe.PipeService;
import net.jxta.discovery.*;
import net.jxta.platform.ModuleClassID;
import net.jxta.protocol.ModuleClassAdvertisement;
import net.jxta.protocol.ModuleImplAdvertisement;
import net.jxta.protocol.ModuleSpecAdvertisement;

/**
 *
 * @author My
 * des modufication sont sugere pour cette class
 * 1- cree une advertismenet avec un nom envoyer en parametre
 * 2- s'il on a de temp en publier un service au lieux de Pipe
 * 3- l'evanemenet n'est pas obligatoire ici on peut le supprimer
 * 4- StartPubPipeAdv est inutil j'ai utilise un thread (run)
 * 
 */
public final class PublishAdv implements Runnable 
{
    private final PeerGroup NetPeerGroup;
    private final DiscoveryService DiscoverySrv;
    private PipeService PipeSrv;
    private final PipeAdvertisement PipeAdv;
    private String PipeName;
    private int StreamSize;
    private long lifeTime;
    
    
    public PublishAdv(String PipeName, int StreamSize, long lifeTime) throws IOException
    {
        this.lifeTime = lifeTime;
        this.PipeName = PipeName;
        this.StreamSize = StreamSize;
        this.NetPeerGroup = NetConfig.NetPeerGroup;
        this.DiscoverySrv = NetPeerGroup.getDiscoveryService();
        this.PipeAdv = GetPipeAdvForConst();
    }
    
    
    
    
    
    //Convertir un entier à un string sur 4 caractères.
    //pour l'utilise dans pipe Advertissement
    public String StreamSizeToString(int StreamSize)
    {
        String SStreamSize = String.valueOf(StreamSize);
        int length = SStreamSize.length();
        
        if(length == 1)
        {
            SStreamSize = "000"+SStreamSize;
        }
        else if(length==2)
        {
            SStreamSize = "00"+SStreamSize;
        }
        else if(length==3)
        {
            SStreamSize = "0"+SStreamSize;
        }
        return SStreamSize;
    }
    
        
    
    //Cette méthode est utilisée pour récupérer un seul coupé dans le constant PipeAdv
    // À chaque exécution cette méthode retourne un avertissement défirent 
    // cette methode cree une advertissement
    private PipeAdvertisement GetPipeAdvForConst()
    {
        PipeAdvertisement PAdv = (PipeAdvertisement) AdvertisementFactory.newAdvertisement(PipeAdvertisement.getAdvertisementType());
        PAdv.setPipeID(IDFactory.newPipeID(PeerGroupID.defaultNetPeerGroupID));
        PAdv.setType(PipeService.UnicastType);
        PAdv.setName(StreamSizeToString(StreamSize)+" "+PipeName);
        return PAdv;
    }
    
    
    
    //methode pour recupere l'advertissement PipeAdv
    public PipeAdvertisement GetPipeAdv()
    {
        return this.PipeAdv;
    }
    
    
    
    //cette thread publier l'advertisment PipeAdv
    //j'utilise pleusiurs copie de cette thread pour publier tout les advertisment de l'application
    @Override
    public void run() 
    {
        try {
            long lifetime = lifeTime;
            long expiration = lifeTime;
            long waittime = lifeTime+1000;
   
            PipeAdvertisement pipeAdv = GetPipeAdv();
            
            // publish the advertisement with a lifetime of 1 mintutes
            //System.out.println("Publishing the following advertisement with lifetime :" + lifetime + " expiration :" + expiration);
            //System.out.println(pipeAdv.toString());
            DiscoverySrv.publish(pipeAdv, lifetime, expiration);
            DiscoverySrv.remotePublish(pipeAdv, expiration);
            
            synchronized ("ServerLogText")
            {
                MKP2P.ServerLogText += "\nPublication de l'Adv :"+pipeAdv.getName();
                "ServerLogText".notify();
            }
            //System.out.println("Sleeping for :" + waittime);
            Thread.sleep(waittime);
            //System.out.println("Pourqqoi ca ne declanch ps"+pipeAdv.getName());
            synchronized ("WaitForAdvAndPipe")
            {
                //System.out.println("WWWWWWWWWWWWZPourqqoi ca ne declanch ps"+pipeAdv.getName());
                "WaitForAdvAndPipe".notifyAll();
            }
            
        } catch (IOException | InterruptedException ex) {
            Logger.getLogger(PublishAdv.class.getName()).log(Level.SEVERE, null, ex);
        }
            
    }
        
}
