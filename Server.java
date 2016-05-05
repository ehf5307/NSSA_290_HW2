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
   Vector<PrintWriter> tcpConnections = new Vector<PrintWriter>();
   //vector of all messges sent since server start
   Vector<String> prevMsgs = new Vector<String>();
   Vector<InetAddress> udpConnections = new Vector<InetAddress>();

   public DatagramSocket UDPSocket;
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
         
         
         //start UDP listener thread
         new UDPListener();
         
         //continue to wait for TCP connections, start new thread for each connection
         while(true){
            jtaMain.append(SECTIONBREAK);
            jtaMain.append("Waiting for connections\n");
            Socket cs = ss.accept();
            new TCPConnection(cs);
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
   *thread class to handle chat connecitons using TCP/IP
   */
   public class TCPConnection extends Thread{
     Socket cs;
     String name;
     PrintWriter out;
     BufferedReader in; 
      
      /*
      *@param cs, the client socket
      *@param name, the users name
      */
      public TCPConnection(Socket cs){
         this.cs = cs;
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
               BufferedReader in = new BufferedReader(new InputStreamReader(cs.getInputStream()));
               this.name = in.readLine();
               jtaMain.append(name + " connected via TCP/IP from " + cs.getInetAddress().getHostAddress()+"\n");
            }//end try
         catch(IOException ioe){
            jtaChat.append("Problem opening input/output with Client\n");
            return;
         }
      
         try{
            out = new PrintWriter(new OutputStreamWriter(cs.getOutputStream()));
            tcpConnections.add(out);
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
                  jtaChat.append("TCP Message from: "+name+": "+ msg + "\n");
                  prevMsgs.add(name+": "+msg);
                  String data = name+": "+msg;
                  
                  for(PrintWriter pw : tcpConnections){
                     jtaChat.append("Forwarding TCP message: "+name+": "+ msg + " |to TCP connection "+(tcpConnections.indexOf(pw)+1)+"/"+tcpConnections.size()+"\n");
                     pw.println(data);
                     pw.flush();
                  }
                  
                  for(InetAddress i : udpConnections){
                     jtaChat.append("Forwarding TCP message: "+name+": "+ msg + " |to UDP connection @ "+i.getHostAddress()+"\n");
                     DatagramPacket sendPacket = new DatagramPacket(data.getBytes(), data.getBytes().length, i, PORT);                   
                     try{
                        UDPSocket.send(sendPacket);
                     }catch(IOException ioe){
                        jtaChat.append("Error sending UDP message to " + i.getHostAddress() + "\n");
                     }  
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
         
         for(int i=0; i < tcpConnections.size(); i++){     
            if(out==tcpConnections.get(i)) {
               tcpConnections.remove(i);
               jtaChat.append(name+" removed from Broadcast" + "\n" );
            }
         }
         String data = name+" has disconnected";
         
         for(PrintWriter pw : tcpConnections){
            jtaChat.append("Sending server message: "+ data + " |to TCP connection "+(tcpConnections.indexOf(pw)+1)+"/"+tcpConnections.size()+"\n");
            pw.println(data);
            pw.flush();
         }
         
         for(InetAddress i : udpConnections){
            jtaChat.append("Sending server message: "+ data + " |to UDP connection @ "+i.getHostAddress()+"\n");
            DatagramPacket sendPacket = new DatagramPacket(data.getBytes(), data.getBytes().length, i, PORT);                   
            try{
               UDPSocket.send(sendPacket);
            }catch(IOException ioe){
               jtaChat.append("Error sending UDP message to " + i.getHostAddress() + "\n");
            }  
         }

         
         out.close();
      }
   }
   
   
   /**
   *thread class to handle chat connecitons using UDP
    */
   public class UDPListener extends Thread{
      /*
      *listens for incoming UDP messages
      *adds to list of known UDP connection addresses
      *@author Ted Fitzgerald
      *@since 3-10-16
      */
      public UDPListener(){
         start();
      }
      
      public void run(){
         try{
            UDPSocket = new DatagramSocket(PORT);
         }catch(SocketException se){
            jtaMain.append("Error creating UDP soceket\n");
         }   
         DatagramPacket messagePacket = new DatagramPacket(UDP_PACKET_SIZE, UDP_PACKET_SIZE.length);                   
         while(true){ 
            try{
               UDPSocket.receive(messagePacket);
               new UDPMessageHandler(messagePacket);
            }catch(IOException ioe){
               jtaChat.append("Error recieveing UDP packet\n");
            } 
         }
      }
   }
   
   public class UDPMessageHandler extends Thread{
      private DatagramPacket packet;
      private InetAddress ip;
      private int port;
      
      public UDPMessageHandler(DatagramPacket packet){
         this.packet = packet;
         start();
      }
      
      public void run(){
         String data = new String(packet.getData() ,0 , packet.getLength());
         ip = packet.getAddress();
         port = packet.getPort(); 
         jtaChat.append("\nIncoming UDP packet: " + data);
         //check if disconnect message
         if(data == UDP_DISCONECT){
            terminateClient(ip);
         }
         
         //do stuff with IP
         if(!udpConnections.contains(ip)){
            //if we don;t already know about this connection, add it to the list and send it previous messages
            udpConnections.add(ip);
            jtaMain.append("New UDP connetction from"+ip.getHostAddress()+":"+port);
            if(prevMsgs.size()!=0){
               for(String s: prevMsgs){
                  jtaChat.append("\nForwarding old messages to " +ip.getHostAddress()+" via UDP");
                  DatagramPacket sendPacket = new DatagramPacket(s.getBytes(), s.getBytes().length, ip, port);                   
                  try{
                     UDPSocket.send(sendPacket);
                  }catch(IOException ioe){
                     jtaChat.append("Error sending UDP message to " + ip + "\n");
                  }  
               }
            }
            DatagramPacket sendPacket = new DatagramPacket(READY.getBytes(), READY.getBytes().length, ip, port);
            
            try{
               jtaChat.append("\nSending 'end of old messages signal' to " +ip.getHostAddress()+" via UDP. Starting normal chat mode");
               UDPSocket.send(sendPacket);
            }catch(IOException ioe){
               jtaMain.append("\nError sending to: " +ip.getHostAddress()+ " via UDP");
            }      
         }else{
            
            //do stuff with message
            prevMsgs.add(data);
            //send to tcp connections
            for(PrintWriter pw : tcpConnections){
               jtaChat.append("Forwarding UDP message: "+ data + " |to TCP connection "+(tcpConnections.indexOf(pw)+1)+"/"+tcpConnections.size()+"\n");
               pw.println(data);
               pw.flush();
            }
            
            for(InetAddress i : udpConnections){
               jtaChat.append("Forwarding UDP message: "+ data + " |to UDP connection "+i.getHostAddress()+"\n");
               DatagramPacket sendPacket = new DatagramPacket(data.getBytes(), data.getBytes().length, i, PORT);                   
               try{
                  UDPSocket.send(sendPacket);
               }catch(IOException ioe){
                  jtaChat.append("Error sending UDP message to " + i.getHostAddress() + "\n");
               }  
            }
         }
      }
      
      private void terminateClient(InetAddress ip){
         jtaChat.append(ip.getHostAddress()+" removed from Broadcast" + "\n" );
         udpConnections.remove(ip);
         for(PrintWriter pw : tcpConnections){
            pw.println("UDP Connection @ "+ ip.getHostAddress() +" has disconnected");
            pw.flush();
         }
         for(InetAddress i : udpConnections){
            String data = "UDP Connection @ "+ ip.getHostAddress()+" has disconnected";
            DatagramPacket sendPacket = new DatagramPacket(data.getBytes(), data.getBytes().length, i, port);                   
            try{
               UDPSocket.send(sendPacket);
            }catch(IOException ioe){
               jtaChat.append("Error sending UDP message to " + i.getHostAddress() + "\n");
            }  
         }
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
            
      setSize(600,500);
      setVisible(true);
      setLocationRelativeTo(null);
      setDefaultCloseOperation(EXIT_ON_CLOSE);
      pack(); 
   }    
   
   }//end threadserver class