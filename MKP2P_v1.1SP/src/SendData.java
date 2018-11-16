/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */


import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import static java.lang.Thread.sleep;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.jxta.endpoint.ByteArrayMessageElement;
import net.jxta.endpoint.Message;
import net.jxta.pipe.PipeMsgEvent;
import net.jxta.pipe.PipeMsgListener;
import net.jxta.protocol.PipeAdvertisement;
import net.jxta.util.JxtaBiDiPipe;
import net.jxta.util.JxtaServerPipe;

/**
 *
 * @author MKPC
 la fonction SendDataRequested doive etre changie  
 */
public class SendData extends StreamingFiles implements PipeMsgListener, Runnable 
{
    private JxtaBiDiPipe BPipe;
    private final JxtaServerPipe PipeServer;
    PipeAdvertisement pipeAdv;
    
    public SendData(PipeAdvertisement pipeAdv) throws IOException, InterruptedException
    {
        this.pipeAdv = pipeAdv;
        PipeServer = new JxtaServerPipe(NetConfig.NetPeerGroup, pipeAdv);
        PipeServer.setPipeTimeout(20000);//duree de vie de pipe avant qu'une cnx soit etablir
        
    }
    //on multiplier le nermero de paquet par la taille on trouve l'octet ini a envoyer 
    public void SendDataRequested(String FileName,String NumPacket) throws IOException, InterruptedException
    {
        
        byte [] b = new byte [PacketSize];
        File videoFile = new File(".\\Streaming\\"+FileName);
        FileInputStream video = new FileInputStream(videoFile);
        long StartByte = Integer.parseInt(NumPacket)* PacketSize ;
        //taille de b; dans le cas de EOF
        if(Integer.parseInt(NumPacket) == Integer.parseInt(pipeAdv.getName().substring(0, 4)))
        {
            System.out.println("Data server : send a last packet ");
        }
        else
        {
            while(videoFile.length()<StartByte+PacketSize)
            {
                sleep(2000);
                System.out.println("Data server :wait for streaming data");
            }
        }
        video.skip(StartByte);
        int size = video.read(b,0,PacketSize); 
        Message msg = new Message();
        msg.addMessageElement(NumPacket,new ByteArrayMessageElement(NumPacket, null, b, null));
        BPipe.sendMessage(msg);
        synchronized ("ServerLogText")
            {
                
                MKP2P.ServerLogText += "\nLa packete N°"+NumPacket+" du Video : "+FileName+". Est envoyer avec succee" ;
                "ServerLogText".notify();
            }
        System.out.println("DataServer : "+"send finished");
    }

     @Override
    public void run()
    {
        try {
            
            //System.out.println("SendData : "+"wait for connexion");
            BPipe = PipeServer.accept();
            synchronized ("WaitForAdvAndPipe")
                {
                    "WaitForAdvAndPipe".notify();
                }
            System.out.println("DataServer : "+"connexion accepted");
            BPipe.setMessageListener(this);
            synchronized ("ServerLogText")
            {
                
                MKP2P.ServerLogText += "\nEtablisselent d'une connexion sur le Pipe : "+PipeServer.getPipeAdv().getName();
                "ServerLogText".notify();
            }
            //il faut ajouter un ID pour synchronise les chaque run avec son Event par exp (ADVName)
            synchronized(pipeAdv.getName())
            {
                try {
                    //System.out.println("SendData : "+"attent le declenchement de l'event");
                    
                    pipeAdv.getName().wait();
                } catch (InterruptedException ex) {
                    Logger.getLogger(SendData.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        } catch (IOException ex) {
            Logger.getLogger(SendData.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void pipeMsgEvent(PipeMsgEvent pme) 
    {
        Message msg;
        String NumPacket;
        msg = pme.getMessage();
        Message.ElementIterator messageElements = msg.getMessageElements();
        while(messageElements.hasNext())
        {
            try {
                NumPacket = messageElements.next().getElementName();
                String str = new String(msg.getMessageElement(NumPacket).getBytes(true), "UTF-8");
                //System.out.println("yes \\n\\n\\n\\n\\n\\n\\n\\nmk "+str);
                if(str.equals("Close"))
                {
                    System.out.println("la connexion est fermer :"+BPipe.toString());
                    PipeServer.close();
                    BPipe.close();
                }
                else
                    try {
                        this.SendDataRequested(pipeAdv.getName().substring(5), NumPacket);
                        } catch (InterruptedException ex) {
                            Logger.getLogger(SendData.class.getName()).log(Level.SEVERE, null, ex);
                            }
                }
                catch (IOException ex) {
                Logger.getLogger(SendData.class.getName()).log(Level.SEVERE, null, ex);
                }
        }
       
        synchronized(pipeAdv.getName())
        {
            System.out.println("DataServer : "+"Evnt declenched");                  
            pipeAdv.getName().notifyAll();
        }
                        
        //System.out.println("SendData : "+"Evnt end");
    }
    
    private void WaitForEvent(String WFE) throws InterruptedException
    {
        synchronized (WFE)
        {
            WFE.wait();
        }
    }
    
}
