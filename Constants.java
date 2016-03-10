/**
*Interface that holds constants used by all other classes
*@author Ted Fitzgerald
*@since 11-15-14
*/

public interface Constants{

   public final int PORT = 16789; //the port to chat on
   public final String CHAT = "CHAT"; //message to tell server that we want to chat
   public final String READY = "READY"; //message to tell server/client  we are ready to receive
   public final String DONE = "DONE"; // message to tell server/client that we are done
   
   public final byte[] UDP_PACKET_SIZE = new byte[65508];
   public final String UDP_NAME_DELIM = ": ";
   public final String UDP_DISCONECT = "DISCONECT";
   
   public final String SECTIONBREAK = "\n************************\n";
   
}