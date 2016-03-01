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

public class ChatClient extends JFrame implements Constants {
   private JTextArea jtaSendText;
   private JTextArea jtaRecvText;
   private JPanel 	jpTextPanel;
   private JPanel 	jpTextPane2;
   private JButton 	jbSend;
   private JButton 	jbExit;
   private JPanel 	jpButtonPanel;
   private JPanel 	jpRadioPanel;
   public InetAddress HOST;
   public String name;
   
   /** 
   *Constructor sets up the chat panel
   *@param name, the users name
   *@param HOST, the ip address of the server
   */
   public ChatClient(String name, String HOST) {
      this.name = name;
      try{
         this.HOST = InetAddress.getByName(HOST);
      }
      catch(UnknownHostException uhe){System.out.println("Invalid IP");} 
      
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
   
      jpTextPanel = new JPanel();
      jpTextPane2 = new JPanel();
   
   	// place the text areas in JScrollPanes
      JScrollPane recvPane = new JScrollPane(jtaRecvText);
      JScrollPane jbSendPane = new JScrollPane(jtaSendText);
   
      jpTextPanel.add(recvPane);
      jpTextPane2.add(jbSendPane);
   	
   	// Buttons send & next
      jbSend = new JButton("Send");
      jbSend.setEnabled(false);
      jpButtonPanel = new JPanel();
      jpButtonPanel.add(jbSend);
         
      // now add the components to the frame
      setLayout(new BorderLayout());
      add(jpTextPanel, BorderLayout.NORTH);
      add(jpTextPane2,  BorderLayout.CENTER);
      add(jpButtonPanel,BorderLayout.SOUTH);
     
      //open connection
      jtaRecvText.setText("Chat server is not running.");
      boolean restart = false;
      new ConnectionThread(restart);
      
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
      
      public void run(){
         boolean connected = false;
         System.out.println(HOST);
         
         while(!connected){
            try {
               Socket cs = new Socket(HOST, PORT);
               //create IO threads
               connected = true;
               jtaRecvText.append("\nConnected to server.");
               BufferedReader in = new BufferedReader(new InputStreamReader(cs.getInputStream()));
               final PrintWriter out = new PrintWriter(new OutputStreamWriter(cs.getOutputStream()));
                  out.println(name); //tell the server our name 
                  out.println(CHAT); //tell the server that we are a chat connection
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
               
               jbSend.addActionListener(
                  new ActionListener(){
                     public void actionPerformed(ActionEvent ae){
                        //on button click, send message
                        out.println(jtaSendText.getText().trim());
                        out.flush();
                        jtaSendText.setText("");
                  }});
               
               jbSend.setEnabled(true);
               new inputThread(cs, jtaRecvText, jbSend, in);
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
         }  
      
      }
   }
   
   /**
   *thread class for handling reciept of messages
   */
   class inputThread extends Thread{
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
      public inputThread(Socket cs, JTextArea jtaRecvText, JButton jbSend, BufferedReader in){
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
   

}