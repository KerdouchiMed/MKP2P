
import com.github.sarxos.webcam.Webcam;
import java.io.IOException;
import static java.lang.Thread.sleep;
import java.util.logging.Level;
import java.util.logging.Logger;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author MK
 */
public class LiveStreaming implements Runnable 
{
    private String name;
    private String Framnbr;
    private String h;
    private String w;
    public LiveStreaming(String name,String fram,String h,String w)
    {
        this.name=name;
        this.Framnbr = fram;
        this.h = h;
        this.w = w;
    }

    
    
    @Override
    public void run() 
    {
        try {
            
            Webcam webcam = Webcam.getDefault();
            String camName=webcam.getName();
            int length = camName.length();
            String CamNamePur=camName.substring(0, length-2);
            if (webcam != null) {
            System.out.println("Webcam: " + webcam.getName());
            } else {
            System.out.println("No webcam detected");
            }
            final Runtime ffmpeg = Runtime.getRuntime();
            Process exec = ffmpeg.exec(".\\VLC\\vlc.exe -I rc dshow:// :dshow-vdev= \""+CamNamePur+"\" :dshow-adev= Default  :dshow-caching=200 --sout=\"#duplicate{dst='transcode{vcodec=h264,width="+w+",height="+h+",acodec=mp3,ab=96,channels=2,samplerate=44100}:std{access=file,mux=ts,dst=.\\Streaming\\"+name+"}'}\"");
            //,dst='Display'
            synchronized ("Live")
            {
            "Live".wait(0);
            }
            System.out.println("fine de diffusion");
            
            exec.destroyForcibly();
            
        } catch (IOException | InterruptedException ex) {
            Logger.getLogger(LiveStreaming.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    
}
