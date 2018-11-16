

import java.io.File;
import java.io.IOException;
import static java.lang.System.exit;
import static java.lang.Thread.sleep;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author My
 */
public class StreamingFiles implements Runnable {
    //declaration des variables 
    protected final String nomRep;
    protected final String defaultRep= ".\\Streaming";
    protected final File rep;
    protected String [] LocalVideosList;
    public static List RemotVideosList;
    protected String NomVideo=null;
    protected int PacketSize = 262144;
    final Runtime mplayer;
    //private Boolean StopStream = false;
    
    // Constructure 
    public StreamingFiles(String nomRep)
    {
        mplayer = Runtime.getRuntime();
        this.RemotVideosList = new ArrayList();
        
        this.nomRep = nomRep;
        this.rep = new File(this.nomRep);
        if(this.rep.isDirectory())
        this.LocalVideosList = this.rep.list();
    }
    
    public static void DeleteRep()
    {
        Set set = new HashSet() ;
        set.addAll(RemotVideosList);
        RemotVideosList.clear();
        /*ArrayList distinctList*/RemotVideosList = new ArrayList(set) ;
    }
    
    public StreamingFiles()
    {
        mplayer = Runtime.getRuntime();
        this.RemotVideosList = new ArrayList();
        //RemotVideosList.add("NULL");
        this.nomRep = this.defaultRep;
        this.rep = new File(nomRep);
        if(this.rep.isDirectory())
        this.LocalVideosList = this.rep.list();
    }
    
    //methodes pour acceder aux variables privee 
    /*public String[] getVideosList()
    {
    
    //String [] VideosList = new String[LocalVideosList.length+RemotVideosList.size()];
    //VideosList=LocalVideosList;
    //System.arraycopy(LocalVideosList,0,VideosList,0,LocalVideosList.length);
    //System.arraycopy(RemotVideosList,0,VideosList,LocalVideosList.length, RemotVideosList.length);
    return LocalVideosList;
    }*/
    public String[] getLocalVideoList()
    {
        return this.rep.list();
    }
    
    public String getStreamPath()
    {
        return nomRep;
    }
    //methodes 
    /*public void LireStream(String nomVideo) throws IOException
    {
    this.NomVideo = nomVideo;
    
    mplayer.exec(new String[] {"cmd.exe", "/C",".\\mplayer\\mplayer.exe -slave -fs -quiet -idle "+"\""+nomRep+"\\"+nomVideo+"\""});
    
    
    }*/
    public void StopStream() throws IOException
    {
        //MKP2P.LireLock=true;
        mplayer.exec(new String[] {"cmd.exe", "/C","taskkill /F /IM mplayer.exe"});
        
    }
    
    public String getVideoName(String NomVideo)
    {
        this.NomVideo=NomVideo;
        return this.NomVideo;
    }
    
    protected long getVideoSize(String Name)
    {
        String pathname = (nomRep+"\\"+Name);   
        File video = new File(pathname);
        if(video.exists()&& video.isFile())
        {
           return video.length();
        }
        else
        {
            return -1;
        }
    }

    @Override
    public void run() 
    {
        //cette while a une prob !Stop??
        
            while(getVideoSize(NomVideo)<1000000&& MKP2P.LireLock)//LireLock pas sur que ca march
            {
                try {
                    sleep(1000);
                    } catch (InterruptedException ex) {
                    Logger.getLogger(StreamingFiles.class.getName()).log(Level.SEVERE, null, ex);
                    }
            }
            try {
            //if(!StopStream)
            {
                final Runtime mplayer = Runtime.getRuntime();
                mplayer.exec(new String[] {"cmd.exe", "/C",".\\mplayer\\mplayer.exe -slave -quiet -idle "+"\""+nomRep+"\\"+NomVideo+"\""});
                System.out.println("lancement de lecture");
            }
            } catch (IOException ex) {
            Logger.getLogger(StreamingFiles.class.getName()).log(Level.SEVERE, null, ex);
            }
            
        
    }
    
}
