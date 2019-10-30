package cloud1;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.*;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.swing.JOptionPane;

public class server {

	static int port;
	static Socket serverSocket;
	
	static BufferedReader in;
	static ObjectOutputStream out;
	static String serverFolder;

	
	public server (int port){
		this.port = port;
		
		String sPath1 = JOptionPane.showInputDialog("Escriba Ruta del servidor, deje en blanco para usar la ruta por defecto");
		String user;
		
		user = JOptionPane.showInputDialog("Escriba usuario");
		
		while (user.equals("")){
			user = JOptionPane.showInputDialog("No puede estar vacio! Escriba usuario");
		}		
		Path defPath3 = FileSystems.getDefault().getPath(System.getProperty("user.home"),"RemoteFolder"+user);
        if(Files.notExists(defPath3)){
            try {
                Files.createDirectories(defPath3);
               
            } catch (IOException e) {
                e.toString();
            }
        }
        String sdefPath1 = defPath3.toString();
        if(sPath1.equals("")){
        	serverFolder = sdefPath1;
					
		}
		
		else{
			serverFolder = sPath1;       		
        	
        }
        
		server.main(null);
	}
	
	public static void main(String[] args) {

			
		
			ServerSocket sc;
			ExecutorService es = Executors.newCachedThreadPool();
			try {
				
				sc = new ServerSocket(port);
				JOptionPane.showMessageDialog(null, "Servidor corriendo en el puerto: " + port); 
				
				while (true){ 
					serverSocket = sc.accept();
					HServer hiloServer = new HServer(serverSocket, serverFolder);
					es.execute(hiloServer);
				}
				
			} catch (IOException e) {
				e.printStackTrace();
			}
			
		
		
	}
	
	
	
}
