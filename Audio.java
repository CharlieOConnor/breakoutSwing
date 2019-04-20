import java.io.*;
import sun.audio.*;
import java.io.IOException;
import java.util.Observable;

      
public class Audio 
{   

  public Audio() 
  {        
    }
    
    public void brickSound() 
  {
    try {
    
    // open the sound file as a Java input stream
    String gongFile = "brickBreaking.wav";
    InputStream in = new FileInputStream(gongFile);

    // create an audiostream from the inputstream
    AudioStream audioStream = new AudioStream(in);

    // play the audio clip with the audioplayer class
    AudioPlayer.player.start(audioStream);
    }
    catch (Exception e) {}
  }
  
   public void batSound() 
  {
    try {
    // open the sound file as a Java input stream
    String gongFile = "bat.wav";
    InputStream in = new FileInputStream(gongFile);

    // create an audiostream from the inputstream
    AudioStream audioStream = new AudioStream(in);

    // play the audio clip with the audioplayer class
    AudioPlayer.player.start(audioStream);
     }
     catch (Exception e) {}
    }
  
   public void youWin() 
  {
    try {
    // open the sound file as a Java input stream
    String gongFile = "youWin.wav";
    InputStream in = new FileInputStream(gongFile);

    // create an audiostream from the inputstream
    AudioStream audioStream = new AudioStream(in);

    // play the audio clip with the audioplayer class
    AudioPlayer.player.start(audioStream);
     }
     catch (Exception e) {}
  }
  
   public void perfectWin() 
  {
    try {
    // open the sound file as a Java input stream
    String gongFile = "perfectWin.wav";
    InputStream in = new FileInputStream(gongFile);

    // create an audiostream from the inputstream
    AudioStream audioStream = new AudioStream(in);

    // play the audio clip with the audioplayer class
    AudioPlayer.player.start(audioStream);
     }
     catch (Exception e) {}
  }
  
   public void youLose(boolean start) 
  {
    try {
    // open the sound file as a Java input stream
    String gongFile = "youLose.wav";
    InputStream in = new FileInputStream(gongFile);

    // create an audiostream from the inputstream
    AudioStream audioStream = new AudioStream(in);
  
    // play the audio clip with the audioplayer class
    if (start) {
      AudioPlayer.player.start(audioStream);
    } else {
        AudioPlayer.player.stop(audioStream);
    }
     }
     catch (Exception e) {}   
  }
  
  public static void stop() {
      AudioPlayer.player.stop();
    }
}