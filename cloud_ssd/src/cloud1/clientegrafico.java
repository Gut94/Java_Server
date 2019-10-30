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
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.NoSuchAlgorithmException;



public class clientegrafico extends JFrame{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	JPanel panelc;  
    JButton bCliente;
    JLabel eCliente;
    static JTextField tCliente;
    public static String user = "";
    
    public clientegrafico(){
        super("Arrancar Cliente");        
        Menus();         
        setSize (205,103);
        setVisible(true);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
      }
    
    private void Menus(){ 
    	
		
        Container contentPane = getContentPane();
        contentPane.setLayout(new BorderLayout());
        panelc = new JPanel();
        contentPane.add(panelc, BorderLayout.CENTER);   	
        eCliente = new JLabel ("Introduzca Usuario");
        panelc.add(eCliente);
        panelc.add(tCliente = new JTextField(6));
        bCliente = new JButton("Arrancar Cliente");
        bCliente.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
            	        		
            	user= tCliente.getText();
            	if(user.equals("")){
        	    	JOptionPane.showMessageDialog(null, "No puede estar vacio!!");}
            	else{
            	p(user);}
            	
            }
        }); 
        
        panelc.add(bCliente);       
        
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);	  
        
        
        
     }
    
	public static void main(String[] args) throws ClassNotFoundException, NoSuchAlgorithmException, IOException {
		clientegrafico C = new clientegrafico();
	}
	
	public static void p(String user){
		Path defPath1 = FileSystems.getDefault().getPath(System.getProperty("user.home"),"localFolder");
        if(Files.notExists(defPath1)){
            try {
                Files.createDirectories(defPath1);
               
            } catch (IOException t) {
                t.toString();
            }
        }
        String defPath = defPath1.toString();
		client cl;
			
		String path = JOptionPane.showInputDialog("Escriba Ruta de carpeta local, deje en blanco para usar la ruta por defecto");
		
		if (path.equals("")){
			System.out.println("-Ruta del cliente por defecto-" + defPath);
			String pServ = reqServ(user);
			
			
			cl = new client(defPath,pServ);
			
			
		}
		
		else{
			System.out.println("Ruta -> " + path);
			String pServ = reqServ(user);
			
			cl = new client(path,pServ);
			
		}
		
		try {
			cl.main(null);
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
		

	
	private static String reqServ(String user) {
		String path;
		
		
		Path defPath2 = FileSystems.getDefault().getPath(System.getProperty("user.home"),"RemoteFolder"+user);
		if(Files.notExists(defPath2)){
			try {
				Files.createDirectories(defPath2);
			
			} catch (IOException e) {
				e.toString();
			}
		}
			String sdefPath = defPath2.toString();
		
		String sPath = JOptionPane.showInputDialog("Escriba Ruta del servidor, deje en blanco para usar la ruta por defecto");
		
		if (sPath.equals("")){
			path = sdefPath;
			System.out.println("-Ruta del servidor por defecto-" + path);		
		}
		
		else{
			path = sPath +"_"+user;
			System.out.println("Ruta -> " + path);
		}
		return path;
		
	}	
		
	
}
