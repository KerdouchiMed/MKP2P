
import java.io.IOException;
import static java.lang.Thread.sleep;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.jxta.protocol.PipeAdvertisement;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author MK
 * la classe client charger des fonction de demande et de recevoire les packets
 * 2 autres classes sont utilise pour cela (DataReciever et DiscoveryAdv)
 */
public class Client extends StreamingFiles implements Runnable
{
    public static List AdvList;
    private int index;
    private  DiscoveryAdv Disc = null;
    private PipeAdvertisement Padv;
    private String VideoName;
    
    //la variable numPacketI represent le numero de la premiere packete demandie par le client 
    private int numPacketI;
    int RemainPacket=0;
    
    //LastPck est une variable entier utilise dans la class ReceiveData 
    //pour pour assure l'arriver des packetes dans le bon ordre
    public static int LastPck;
    
    
    
    Client(String FileName) throws InterruptedException
    {       
        //LastPck=numPacketI;
        Client.AdvList = Collections.synchronizedList(new ArrayList());
        this.VideoName=FileName;
        Disc = new DiscoveryAdv(NetConfig.NetPeerGroup,true);             
    }
    
    public void triAdvList()
    {
        Collections.sort(Client.AdvList, new Comparator<PipeAdvertisement>() 
        {
            @Override
            public int compare(PipeAdvertisement o1, PipeAdvertisement o2) 
            {
               int i1 = Integer.parseInt(o1.getName().substring(0, 4));
               int i2 = Integer.parseInt(o2.getName().substring(0, 4));
               if(i1>i2)return 1;
               else if(i1<i2) return -1;
               else return 0;
                       
            }
        });
    }
   
    //cette methode cree une liste de packetes a demandie a chaque source
    public ArrayList<Integer> getRequestForEveryPeer(int advindex,int numPacketI)
    {
        int i=advindex+numPacketI;
        if(advindex>0)
        {
            ArrayList<Integer> request = new ArrayList();
            PipeAdvertisement PipAdvPred = (PipeAdvertisement) Client.AdvList.get(advindex-1);
            PipeAdvertisement PipAdvThis = (PipeAdvertisement) Client.AdvList.get(advindex);
            int lastPackPred = Integer.parseInt(PipAdvPred.getName().substring(0, 4));
            int lastPackThis = Integer.parseInt(PipAdvThis.getName().substring(0, 4));
        
            //System.out.println("i = "+i+" last ="+lastPackThis);
        
            while(i<=lastPackPred)
            {
                //System.out.println("i = "+i+" last ="+lastPackThis);
                request.add(i);
                i=i+Client.AdvList.size();
            }
            i=lastPackPred+1;
            while(i<=lastPackThis)
            {
                //System.out.println("i = "+i+" last ="+lastPackThis);
                request.add(i);
                i++;
            }
            //System.out.println("client get Req "+request.toString());
            return request;
        }
        else
        {
            ArrayList<Integer> request = new ArrayList();
        PipeAdvertisement PipAdv = (PipeAdvertisement) Client.AdvList.get(advindex);
        int lastPacket = Integer.parseInt(PipAdv.getName().substring(0, 4));
        //System.out.println("i = "+i+" last ="+lastPacket);
        
        while(i<=lastPacket)
        {
            //System.out.println("i = "+i+" last ="+lastPacket);
            request.add(i);
            i=i+Client.AdvList.size();
        }
        
        //System.out.println("client get Req "+request.toString());
        
        return request;
        }
        
    }
    
    
    
    //ce thread est utilise pour trouver les Advertessements des pipe conserné
    // et demandie les packetes
    //la reception des packetes est dans la classe ReceiveData au niveau de pipeMsgEvent
    //un seul copie de ce thread est lancer 
    //On a besoin de ce thread pour libéré la bouton lireStreaming
    @Override
    public void run() 
    {

        try {
            synchronized ("ClientLogText")
            {
                MKP2P.ClientLogText = "Recherche des advertissements conserne le video :"+this.VideoName;
                ("ClientLogText").notify();
            }
            
            Disc.startDiscovery("* "+this.VideoName);
            
            if(getVideoSize(this.VideoName)==-1)
            {
                numPacketI = 0;
            }
            else
            {
                numPacketI =(int)(getVideoSize(this.VideoName)/PacketSize);
            }
            
            //demandie une packet a chaque server
            System.out.println("Client : wait for connexion"+Client.AdvList.toString());
            sleep(1000); //attent que tout les adv soit disponible 
            DiscoveryAdv.DeletRepInAdvList();
            triAdvList();
            synchronized ("ClientLogText")
            {
                MKP2P.ClientLogText += "\nRecherch Terminer, "+Client.AdvList.size()+" Advertissement trouver";
                ("ClientLogText").notify();
            }
            index=0;
            
            while(index<Client.AdvList.size())
            {
                try {
                    System.out.println(index);
                
                    Padv = (PipeAdvertisement) Client.AdvList.get(index);
                    ArrayList<Integer> request = getRequestForEveryPeer(index,numPacketI);
                    
                    if(!request.isEmpty())
                    {
                        ReceiveData DR = new ReceiveData(Padv,request,numPacketI,index);
                        Thread ThDR = new Thread( DR, "ThDR"+index);
                        ThDR.start();
                    }
                    else System.out.println("request vide");
                    RemainPacket = Integer.parseInt(Padv.getName().substring(0, 4))-numPacketI;
                
                } catch (IOException | InterruptedException ex) {
                    Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
                }
              index++;  
                
            }
           
        } catch (InterruptedException ex) {
            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
