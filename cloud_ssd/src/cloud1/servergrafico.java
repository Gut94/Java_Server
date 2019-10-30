package cloud1;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.*;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;



public class servergrafico extends JFrame{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	JPanel panel_1;  
    JButton bServer;
    JLabel ePuerto;
    JTextField tPuerto;
    
    public servergrafico(){
        super("Arrancar Servidor");        
        Menus();         
        setSize (207,103);
        setVisible(true);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
      }
    
    private void Menus(){ 
    	
		
        Container contentPane = getContentPane();
        contentPane.setLayout(new BorderLayout());
        panel_1 = new JPanel();
        contentPane.add(panel_1, BorderLayout.CENTER);   	
        ePuerto = new JLabel ("Introduzca puerto");
        panel_1.add(ePuerto);
        panel_1.add(tPuerto = new JTextField(6));
        bServer = new JButton("Arrancar Server");
        bServer.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
            	
            		String stringInput = tPuerto.getText();          	    
            		if(stringInput.equals("")){
            	    	JOptionPane.showMessageDialog(null, "No puede estar vacio!!");}
            		else{
            	    int puerto = Integer.parseInt(stringInput);
            	    new server(puerto);
            	    }			
            }
        });   
        panel_1.add(bServer);	
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);	  
        
        
        
     }
    
	public static void main(String[] args) {
		servergrafico S = new servergrafico();
		
	}
	
	
		
	
}
