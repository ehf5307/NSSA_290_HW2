 /**
 * Multi-Threaded Client/Server chat 
 * NSSA-290 HW 2
 * @author Ted Fitzgerald
 * @since 2-11-16
 */ 

import java.net.*;
import java.io.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;

public class ChatClient extends JFrame implements Constants{
   private JPanel jpMessage;
   private JPanel jpInformation;
   
   private JTextArea jtaSendText;
   private JTextArea jtaRecvText;
   private JTextArea jtaInfo;
   private JPanel 	jpSendRec;
   private JButton 	jbSend;
   private JButton 	jbExit;
   private JPanel 	jpButtonPanel;
   private JPanel 	jpRadioPanel;
   public InetAddress HOST;
   public String name;
   public int port;
   
   
   /** 
   *Constructor sets up the chat panel
   *@param name, the users name
   *@param HOST, the ip address of the server
   *@param port, port user wants to connect on
   */
   public ChatClient(String name, String HOST, int port, boolean useTCP){
      this.name = name;
      this.port = port;
      
      try{
         this.HOST = InetAddress.getByName(HOST);
      }
      catch(UnknownHostException uhe){System.out.println("Invalid IP");} 
      catch(NumberFormatException nfe){
         System.out.println("Invalid IP");
      }
      
      createGUI();
      
      //open connection
      //jtaRecvText.setText("Chat server is not running.");
      boolean restart = false;
      if(useTCP){
         new ConnectionThread(restart);
      }else{
         new udpConnection(restart);
      }
      
      setDefaultCloseOperation(EXIT_ON_CLOSE);
      pack();
      setLocationRelativeTo(null);
      setVisible(true);



   }  // end constructor
   
   /**
   *thread class for connecting to server
   */
   class ConnectionThread extends Thread{
      boolean restart; //tells this class if the connection is being restarted
      
      public ConnectionThread(boolean restart){
         this.restart = restart;
         start();
      }
      
      public void run() {
         boolean connected = false;
         //System.out.println(HOST);
         
         while(!connected){
            try {
               Socket cs = new Socket(HOST, port);
               //create IO threads
               connected = true;
               jtaInfo.append("\nConnected to server via TCP/IP.");
               BufferedReader in = new BufferedReader(new InputStreamReader(cs.getInputStream()));
               final PrintWriter out = new PrintWriter(new OutputStreamWriter(cs.getOutputStream()));
                  out.println(name); //tell the server our name 
                  out.flush();
              
               while(true){
                  String msg = in.readLine();//get old messages
                  if(!msg.equals(READY)){//see if server is ready to recieve messages
                     jtaRecvText.append("\n"+msg);
                  }
                  else{
                     jtaRecvText.append("\n");
                     break;
                  } 
               }
              
                 jbSend.addActionListener
                   (
                       new ActionListener()
                       {
                           public void actionPerformed(ActionEvent ae){
                         //on button click, send message
                              jtaInfo.append("\nSending: " + jtaSendText.getText().trim() + " via TCPIP");
                              out.println(jtaSendText.getText().trim());
                              out.flush();
                              jtaSendText.setText("");
                        }});
         

               
               jbSend.setEnabled(true);
               new InputThread(cs, jtaRecvText, jbSend, in);
               //new outputThread(cs, jtaSendText, jtaRecvText, jbSend, out);
            }

            catch(UnknownHostException uhe) {
               jtaRecvText.append("\nUnable to connect to host.");
               return;
            }
            catch(IOException ie) {
               if(restart){
                  jtaRecvText.append("\nChat server has stopped running, unable to send messages.");
                  restart = false;
               }   
            }
         } // end while loop 
      
      } // end run
   } // end class 
   
   class udpConnection extends Thread{
       boolean restart; //tells this class if the connection is being restarted
       private DatagramSocket udpSocket;       // passes port number and ip address
       
       public udpConnection(boolean restart)
       {
          this.restart = restart;
          start();
       } // end udpConnection 
       
       public void run()
       {
          try
          {
             
          this.udpSocket = new DatagramSocket();
          this.udpSocket.connect(HOST, PORT);
             // New input stream
                        
                // ask for old messages
                byte[] sendData = new byte[1024];
                byte[] receiveData = new byte[1024];
                sendData = (name).getBytes();
                DatagramPacket packMessage = new DatagramPacket(sendData, sendData.length, HOST, port);
                // now send UDP Packet to the server
                udpSocket.send(packMessage);
                
                while(true){
                  DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);       
                 udpSocket.receive(receivePacket);       
                 String msg = new String(receivePacket.getData() ,0 , receivePacket.getLength()); 
                  if(!msg.equals(READY)){//see if server is ready to recieve messages
                     jtaRecvText.append("\n"+msg);
                  }
                  else{
                     jtaRecvText.append("\n");
                     break;
                  } 
               }
              
                 jbSend.addActionListener
                   (
                       new ActionListener()
                       {
                           public void actionPerformed(ActionEvent ae){
                         //on button click, send message
                              jtaInfo.append("\nSending: "+jtaSendText.getText().trim()+" via UDP");
                              
                               byte[] msg = new byte[1024];
                               msg =  (jtaSendText.getText().trim()).getBytes();;
                               DatagramPacket packet = new DatagramPacket(msg, msg.length, HOST, port);
                               // now send UDP Packet to the server
                              try{
                                  udpSocket.send(packet);
                              }catch(IOException ioe){
                                 jtaInfo.append("\nError sending UDP message");
                              }    
                              jtaSendText.setText("");
                        }});

               
               jbSend.setEnabled(true);
               new ReceiveUDP(udpSocket, jtaRecvText, jbSend);

          } // end try
          
          catch(IOException e)
          {
             System.out.println(e);
          } // end catch   
       } // end run
    
    } // end udpSend class
   
   /**
   *thread class for handling reciept of tcpip messages
   */
   class InputThread extends Thread{
      Socket cs;
      BufferedReader in;
      JTextArea jtaRecvText;
      JButton jbSend;
      
       /**
      *@param cs, the socket
      *@param jtaRecvText, the text area to show messages from server
      *@param jbSend, the send button
      @param in, the BufferedReader input from server
      */
      public InputThread(Socket cs, JTextArea jtaRecvText, JButton jbSend, BufferedReader in)
      {
         this.cs = cs;      
         this.jtaRecvText = jtaRecvText;
         this.jbSend = jbSend;
         this.in = in;
         this.start();
      }//end inputthread construct
      
      public void run(){
         try{ 
              
            //wait for new messages       
            while(true){
               System.out.println("Waiting for new messages");
               
               //print messages
               String msg = in.readLine();
               if(msg!=null) {
                  jtaInfo.append("'\nRecieved: " +msg+" via TCP");
                  jtaRecvText.setText(jtaRecvText.getText()+msg+"\n");
               } 
               else {
                  break;
               } 
            }
            jtaRecvText.append("\nServer Disconnected.....");
                     }
         catch(UnknownHostException uhe) {
            jtaRecvText.append("\nUnable to connect to host.");
            return;
         }
         catch(IOException ie) {
            jbSend.setEnabled(false);
            boolean restart = true;
            new ConnectionThread(restart); 
            return;
         }  
                
      }//end run
   }//end inputthread class
   
   
   class ReceiveUDP extends Thread
   {
      private DatagramSocket udpSocket;
      private JTextArea jtaRecvText;
      private JButton jbSend;
      
      // CONSTRUCTOR
      public ReceiveUDP(DatagramSocket udpSocket, JTextArea jtaRecvText, JButton jbSend)
      {
         this.udpSocket = udpSocket;
         this.jtaRecvText = jtaRecvText;
         this.jbSend = jbSend;
         start();
      } // end recieveUDP constructor
      
      public void run()
      {
        // make array of bytes to recieve message
        byte[] dataReceived = new byte[1024];
        
        while(true)
        {
            // this is where the data will be coming into
            DatagramPacket receiveMessage = new DatagramPacket(dataReceived, dataReceived.length);
            
            try
            {
               // look for message from server
               udpSocket.receive(receiveMessage);
               
               // unpack the packet
               String msg = new String(receiveMessage.getData(), 0, receiveMessage.getLength());
               
               
               // print out on screen
               jtaRecvText.setText(jtaRecvText.getText()+msg+"\n");
               
            } // end try
            
            catch(IOException e)
            {
               System.out.println(e); 
            }
      
        } // end while loop 
        
              } // end run method
      
   } // end class recieveUDP
   
   /**
   *Creates the Gui  - code moved from constructor
   *@author Ted Fitzgerald
   *@since 3-10-16
   */
   private void createGUI(){
      // setup the frame components
   	// Text areas first
      jtaRecvText = new JTextArea("",20,30);
      jtaRecvText.setBorder(new EtchedBorder());
      jtaRecvText.setEditable(false);
      jtaRecvText.setLineWrap( true );           // wrap to new lines 
      jtaRecvText.setWrapStyleWord( true );      // split on whole words
      
      jtaSendText = new JTextArea("Send text",5,30);
      jtaSendText.setBorder(new EtchedBorder());
      jtaSendText.setLineWrap( true );           // wrap to new lines 
      jtaSendText.setWrapStyleWord( true );      // split on whole words
   
      jpSendRec = new JPanel(new BorderLayout());
   
   	// place the text areas in JScrollPanes
      JScrollPane recvPane = new JScrollPane(jtaRecvText);
      JScrollPane jbSendPane = new JScrollPane(jtaSendText);
   
      jpSendRec.add(recvPane, BorderLayout.NORTH);
      jpSendRec.add(jbSendPane, BorderLayout.SOUTH);
   	
   	// Buttons send & next
      jbSend = new JButton("Send");
      jbSend.setEnabled(false);
      jpButtonPanel = new JPanel();
      jpButtonPanel.add(jbSend);
         
      // now add the messaging componnets to the left panel
      jpMessage = new JPanel(new BorderLayout());
      jpMessage.add(new JLabel("Messages"), BorderLayout.NORTH);
      jpMessage.add(jpSendRec,  BorderLayout.CENTER);
      jpMessage.add(jpButtonPanel,BorderLayout.SOUTH);
      
      //set up information panel
      jpInformation = new JPanel(new BorderLayout());    
      jtaInfo = new JTextArea(20,20);
      jtaInfo.setEditable(false);
      JScrollPane jspInfo = new JScrollPane(jtaInfo);
      jpInformation.add(new JLabel("Connection Information"), BorderLayout.NORTH);
      jpInformation.add(jspInfo);
      
      setLayout(new GridLayout(0,2));
      add(jpMessage);
      add(jpInformation);
      
     

   }
}