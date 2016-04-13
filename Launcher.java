/**
*NSSA-290 HW-2
*class to get user name and server ip
*starts the rest of the program
*@author Edward Fitzgewrald
*@since 02-11-16
*/
 

import java.awt.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.JOptionPane;
import javax.swing.JDialog;


/* *
*class to create interface which allows plays enters their names 
*/
public class Launcher extends JFrame implements ActionListener {


   // instance attributes
   private JButton jbStart; 
   private JButton jbExit;
   private JTextField jtfName; 
   private JTextField jtfIp;
   private JTextField jtfPort;
   private JMenuItem mItemHowToplayGame = null;
   private JMenuItem mItemExit = null;  
   private JMenuItem mItemNavigatingThroughGUI= null;  

   /** The main method
   *@param args, command line arguments. not utilized in this release
    */
   public static void main(String [] args) {
      new Launcher();
   }// end main
      
    /** Constructor */
   public Launcher() {
   
      setTitle("Open Chat Client");
         
      // Header in North
      JPanel jpHead = new JPanel( );

      JLabel title1 = new JLabel("Chat Client",JLabel.CENTER);
      Font fontTitle = new Font("Arial", Font.BOLD, 36);
      title1.setFont( fontTitle );
      title1.setForeground(Color.BLUE); 
      title1.setVerticalTextPosition(JLabel.BOTTOM);
      title1.setHorizontalTextPosition(JLabel.CENTER);

      jpHead.add( title1 );
            add( jpHead, BorderLayout.NORTH );
              
      JPanel jpCenter = new JPanel();
      JLabel entername = new JLabel("Enter Your Name:");
      jpCenter.add(entername);
      entername.setForeground(Color.RED);
      jpCenter.add(jtfName = new JTextField("",10));
      
      JLabel enterIP = new JLabel("Enter the IP of the server:");
      jpCenter.add(enterIP);
      enterIP.setForeground(Color.RED);
      jpCenter.add(jtfIp = new JTextField("",10));
   
        
      add( jpCenter, BorderLayout.CENTER );
      
      // South is for the control buttons
      JPanel jpSouth = new JPanel( );
      
      jbStart  = new JButton("Open Chat");
      jbExit  = new JButton("Exit");
   
      jpSouth.add( jbStart);
      jpSouth.add( jbExit );
      add( jpSouth, BorderLayout.SOUTH );
      
   
      jbStart.addActionListener( this );
      jbExit.addActionListener( this );
      
      /* East for UDP or TCP connects */
      
      // new panel
      JPanel jpEast = new JPanel();
      
      JLabel label = new JLabel("Port");
      jpEast.add(label); 
      label.setForeground(Color.RED);
      jpEast.add(jtfPort = new JTextField("",5));  
           
      // udp radio button
      JRadioButton udpConnect = new JRadioButton("UPD Connect");      
      jpEast.add(udpConnect);

      JRadioButton tcpConnect = new JRadioButton("TCP Connect");
      jpEast.add(tcpConnect); 
      
      
      // new button group 
      ButtonGroup howConnect = new ButtonGroup(); 

      // add buttons to button group 
      howConnect.add(udpConnect);
      howConnect.add(tcpConnect);

       
      add(jpEast, BorderLayout.EAST);

      
      // Size the GUI and MAKE IT VISIBLE!
      pack();
      setDefaultCloseOperation( EXIT_ON_CLOSE );
      setResizable(false);
      setLocationRelativeTo( null );
      setVisible(true);
   } // end constructor 
    
   //method to perform action of buttons
   public void actionPerformed(ActionEvent ae){
      Object choice = ae.getSource();  
      String sChoice = ae.getActionCommand(); 
      
      if( choice == jbStart ){
      
         String name = jtfName.getText();
         String ip = jtfIp.getText();
           
         if( !name.equals("") && !ip.equals("")){
            new ChatClient(name,ip);
            setVisible(false);
         
         }
         else{
            JOptionPane.showMessageDialog(null, "Please fill in all fields");
          
         } 
      
      }// end if 
      else if( choice == jbExit || choice == mItemExit ){ 
      
          // to show meassge JDialog ( yes , No)
         JDialog.setDefaultLookAndFeelDecorated(true);
         int response = JOptionPane.showConfirmDialog(null, "Do you want to exit the chat?", "Confirm",
            JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
            // when click on the button No
         if (response == JOptionPane.NO_OPTION) {
            setVisible(true);
         } 
         // when click on the button Yes
         else if (response == JOptionPane.YES_OPTION) {
            System.exit(0);
         } 
      
           
      }// else if 
            
   }// end actionPerformed
   
}// end class
