/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */


import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import static java.lang.Thread.sleep;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.jxta.endpoint.Message;
import net.jxta.endpoint.StringMessageElement;
import net.jxta.pipe.PipeMsgEvent;
import net.jxta.pipe.PipeMsgListener;
import net.jxta.protocol.PipeAdvertisement;
import net.jxta.util.JxtaBiDiPipe;

/**
 *
 * @author My
 */
public class ReceiveData extends StreamingFiles implements PipeMsgListener , Runnable  
{
    private JxtaBiDiPipe BPipe = null;
    PipeAdvertisement pipeAdv = null;
    ArrayList<Integer> request;
    private int numPacketI;
    private int RIndex;
    private int advIndex;
    String videoName;
    
    
    
    public ReceiveData(PipeAdvertisement pipeAdv,ArrayList<Integer> request,int numPacketI,int advIndex) throws IOException, InterruptedException
    {
        this.numPacketI=numPacketI;
        Client.LastPck=numPacketI;
        this.pipeAdv = pipeAdv;
        this.request = request;
        this.advIndex = advIndex;
        BPipe = new JxtaBiDiPipe(NetConfig.NetPeerGroup, pipeAdv, 30000, this, true);
        videoName = pipeAdv.getName().substring(5);
    }
    
    
    
    //cette methode envoi les demandes (numeros de packete) au server 
    public void SendRequestForData(ArrayList<Integer> request) throws IOException, InterruptedException
    {
        RIndex=0;
        System.out.println("Rindex "+RIndex+" LastPsize "+request.size()+" StopRecieverLock "+MKP2P.StopRecieverLock);
       
        while(RIndex<request.size() && !MKP2P.StopRecieverLock)
        {
            while(request.get(RIndex)!= Client.LastPck)
            {
                sleep(300);
                System.out.println(Thread.currentThread().getName()+"sleep 300 : lastPck "+ Client.LastPck+"Rindex"+request.get(RIndex));
            }
            System.out.println("DR SenData Rindex "+RIndex+"req"+request.get(RIndex));
            
            Message msg = new Message();
            msg.addMessageElement(String.valueOf(request.get(RIndex)),new StringMessageElement(String.valueOf(request.get(RIndex)),"MK", null));
            BPipe.sendMessage(msg);
            
            synchronized ("Ordre")
            {
                ("Ordre").wait();
            }
            
            synchronized ("ClientLogText")
            {
                MKP2P.ClientLogText += "\nPackete N°"+request.get(RIndex)+" recu avec succe sur le Pipe : "+BPipe.toString();
                ("ClientLogText").notify();
            }
            
            synchronized ("LastPck")
            {
              Client.LastPck++;
            }
                       
            RIndex++;
            
        }
        
        synchronized ("ClientLogText")
        {
            MKP2P.ClientLogText += "\nConnexion fermer sur le Pipe :"+BPipe.toString();
            ("ClientLogText").notify();
        }
        Message end = new Message();
        end.addMessageElement("0",new StringMessageElement(String.valueOf("0"),"Close", null));
        
        MKP2P.BadAdv.add(BPipe.getPipeAdvertisement().getID());
        System.out.println("merd "+BPipe.getPipeAdvertisement().getID().toString() );
        BPipe.sendMessage(end);
        BPipe.close();
        MKP2P.StopRecieverLock=false;
    }
    
    
    
    //la methode run est un thread qui envoi les requete a l'aide de SendRequestForData
    //et cree un message listner pour attendre la reponse de serveur
     @Override
    public void run() 
    {
        {
            
            try 
            {
                
                //cette condition pour tester la bonne synchronisation des packetes
                //pour observe la reaction de systeme dans le cas de pleusieur server 
                /*if(!Thread.currentThread().getName().equals("ThDR0"))
                Thread.currentThread().sleep(1000);*/
                //System.out.println("run ReceiveData : "+"send data"+request.toString());
                SendRequestForData(request);
                BPipe.setMessageListener(this);
                //System.out.println("ReceiveData : "+"attent le declenchement de l'event"+Thread.currentThread().getName());
            
            }   catch (InterruptedException | IOException ex) {
                Logger.getLogger(ReceiveData.class.getName()).log(Level.SEVERE, null, ex);
                }
        
        }
    }
    

    
    //pipeMsgEvent est declancher lorsque une packete est disponible sur le Pipe
    @Override
    public void pipeMsgEvent(PipeMsgEvent pme) 
    {
        
        Message msg;
        msg = pme.getMessage();
        System.out.println("DataReceiver : "+"Event declanched numPacket = "+RIndex+"  "+request.get(RIndex));
        byte [] b = new byte[PacketSize];
        
        //on recoupere la packete dans le tableau b 
        b = msg.getMessageElement(String.valueOf(request.get(RIndex))).getBytes(true);
        
        //on ecrire le contenu de b dans le fichier qui a le nom de video comme nom
        try {
            FileOutputStream f = new FileOutputStream(".\\Streaming\\"+pipeAdv.getName().substring(5),true);
            f.write(b);
            
            //pas sur a confirme?? jeust pour simeuler le retard couse par une faible debit
            sleep(200);
            
            //Oredre synchronise les demandes de packete pour ne pas tembie dans un conflie
            synchronized ("Ordre")
            {
            "Ordre".notify();
            }
            
        } catch (FileNotFoundException ex) {
            Logger.getLogger(ReceiveData.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException | InterruptedException ex) {
            Logger.getLogger(ReceiveData.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }
    
}
