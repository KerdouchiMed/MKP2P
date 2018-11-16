

import java.io.IOException;
import static java.lang.Thread.sleep;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author MK
 */
public class Server extends StreamingFiles 
{
         
    String [] VideoList;
    public Server(){}
    
    public void SetAdvAndPipeServer(String [] VideoList) throws IOException, InterruptedException
    {
        int nbts=0;
        for (int i=0; i<VideoList.length; i++) 
        {
            long StreamSize;
            if (((getVideoSize(VideoList[i])) % (PacketSize)) != 0) 
            {
                StreamSize = (getVideoSize(VideoList[i])) / (PacketSize);
            } 
            else 
            {
                StreamSize = ((getVideoSize(VideoList[i])) / (PacketSize)) - 1;
            }
            String name = VideoList[i];
            if(!name.substring(name.length()-3).equals(".ts"))
            {
                sleep(i*100);
                //System.out.println("mk \n mk"+name.substring(name.length()-3));
                PublishAdv Adv = new PublishAdv(VideoList[i], (int)StreamSize, 15000);
                Thread AdvThread = new Thread(Adv, "AdvThread");
                //AdvThread.setDaemon(true);
                AdvThread.start();
                SendData DS = new SendData(Adv.GetPipeAdv());
                Thread DSThread = new Thread(DS, "DSThread");
                DSThread.start();
                //DSThread.setDaemon(true);
            }
            else
                nbts++;
        }
            for (int i=0;i<VideoList.length-nbts;i++)
            {
                //System.out.println("Server : WaitForAdvAndPipe"+i+"  "+Thread.currentThread().toString());
                synchronized ("WaitForAdvAndPipe")
                {
                    "WaitForAdvAndPipe".wait();
                }
                //System.out.println("Server WaitForAdvAndPipe declanched : "+i+"  "+VideoList[i]);
            }
        
         
    }
        
    
}
