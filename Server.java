import java.net.*;
import java.io.*;
import java.util.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

 /**
 * Multi-Threaded Server for chat 
 * NSSA-290 HW2
 * @Ted Fitzgerald
 * @since 2/11/16
 */ 
class Server extends JFrame implements Constants{
   //text areas for print outs
   public JTextArea jtaChat;
   public JTextArea jtaMain;
   //vector of all chat PrintWtiters
   Vector<PrintWriter> chatOuts = new Vector<PrintWriter>();
   //vector of all messges sent since server start
   Vector<String> prevMsgs = new Vector<String>();

   //instance of the question mod class.
   boolean running = true;
   
   public static void main(String[] args){
      new Server();
   }//end main
   
   
   public Server(){
      //generate the gui and load questions form files
      createGUI();
      
      try{
         ServerSocket ss = new ServerSocket(PORT);   
         
         jtaMain.append("Server Started.\n");   
         jtaMain.append("IP Address: " + InetAddress.getLocalHost().getHostAddress() + "\n");
         jtaMain.append("Listening on port: " + ss.getLocalPort() +"\n");
         
         
         //continue to wait for connections, start new thread for each connection
                  
         while(true){
            jtaMain.append(SECTIONBREAK);
            jtaMain.append("Waiting for connections\n");
            Socket cs = ss.accept();
            new ConnectionThread(cs);
         }//end loop
      } 
      catch( UnknownHostException uhe ){
         jtaMain.append("the host is Unknown\n");
      }      
      catch( BindException be ) {
         jtaMain.append("Server already running on this computer, stopping.\n");
      }
      catch( IOException ioe ) {
         jtaMain.append("IO Error\n");
      }  
   }//end threadserver construct
   
   
   /**
   *thread class that determines if connection is for chat or quiz
   *starts appropriate thread based on above
   */
   class ConnectionThread extends Thread{
      Socket cs;
      
      /**
      *@param cs, the client socket
      */     
      public ConnectionThread(Socket cs){
         this.cs = cs;
         start();
      }
     
      /**
      *Determines if connection is for chat or quiz
      */
      public void run(){
         try{
            BufferedReader in = new BufferedReader(new InputStreamReader(cs.getInputStream()));
            String name = in.readLine();
            String type = in.readLine();
            jtaMain.append("connected to "+name+". Type: "+type+"\n");
            if(type.equals(CHAT)){
               new ChatConnection(cs, name);
            }   
         }//end try
         
         catch(IOException ioe){jtaChat.append("Problem opening input/output with Client\n");}
      
         
      }//end run method
   }
   
   /**
   *thread class to handle chat connecitons
   */
   public class ChatConnection extends Thread{
     Socket cs;
     String name;
     PrintWriter out;
     BufferedReader in; 
      
      /*
      *@param cs, the client socket
      *@param name, the users name
      */
      public ChatConnection(Socket cs, String name){
         this.cs = cs;
         this.name = name;
         start();
      }
      
      /**
      *adds printwriter to vector of all chat client pws
      *sends any messages recieved between server start and client join
      *sends ready message to client
      *begins waiting for essages to client
      *prints messages to all clients
      */
      public void run(){
         try{
            out = new PrintWriter(new OutputStreamWriter(cs.getOutputStream()));
            chatOuts.add(out);
            in = new BufferedReader(new InputStreamReader(cs.getInputStream()));
             
            //send old messages
            if(prevMsgs.size()!=0){
               for(String s: prevMsgs){
                  out.println(s);
                  out.flush();
               }
            }
            else{//no old messages
               out.println("No new messages\n");
               out.flush();
            }    
            //tell client we are ready for normal chat
            out.println(READY);
            out.flush();
            
            //wait for messages, print messages to all users           
            while(true){	
               String msg = in.readLine();
               if(msg!=null) {
                  jtaChat.append("Message from: "+name+": "+ msg + "\n");
                  prevMsgs.add(name+": "+msg);
                  for(PrintWriter pw : chatOuts){
                     pw.println(name+": "+msg);
                     pw.flush();
                  }
               }
               else {
                  break;
               }
            }
            
            //after loop, remove client, they have left
            terminateClient();
         }//end try
         catch(UnknownHostException uhe){jtaChat.append("Problem Connecting to Client\n");}
         catch(IOException IOE){terminateClient();}
      }
      
      /**
      *called when a chat client disconects
      *remvoves client from vector
      *informs othe clients that this client has left
      *shuts down conections
      */   
      public void terminateClient(){
         jtaChat.append(name+" Disconnected.." + "\n" );
         
         for(int i=0; i < chatOuts.size(); i++){     
            if(out==chatOuts.get(i)) {
               chatOuts.remove(i);
               jtaChat.append(name+" removed from Broadcast" + "\n" );
            }
         }
         for(PrintWriter pw : chatOuts){
            pw.println(name+" has disconnected");
            pw.flush();
         }
         out.close();
      }
   }
   
  
   /**
   *creates GUI for server outputs
   */
   public void createGUI(){
      //three panel gui for server
      //shows main server IO and chat IO
            
      setTitle("Server  Window");
      setLayout(new GridLayout(0,2));
      
      JPanel jpC = new JPanel(new BorderLayout());
      jtaChat = new JTextArea(20,20);
      jtaChat.setEditable(false);
      JScrollPane jspC = new JScrollPane(jtaChat);
      jpC.add(new JLabel("CHAT SERVICES"), BorderLayout.NORTH);
      jpC.add(jspC);
      
      JPanel jpM = new JPanel(new BorderLayout());    
      jtaMain = new JTextArea(20,20);
      jtaMain.setEditable(false);
      JScrollPane jspM = new JScrollPane(jtaMain);
      jpM.add(new JLabel("MAIN SERVER"), BorderLayout.NORTH);
      jpM.add(jspM);
           
      add(jpM);
      add(jpC);
            
      setSize(500,500);
      setVisible(true);
      setLocationRelativeTo(null);
      setDefaultCloseOperation(EXIT_ON_CLOSE);
      pack(); 
   }    
   
   }//end threadserver class