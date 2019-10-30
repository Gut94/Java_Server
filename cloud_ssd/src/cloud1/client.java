package cloud1;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.*;
import java.nio.file.FileSystem;
import java.nio.file.Path;
import java.nio.file.WatchEvent;
import java.nio.file.WatchEvent.Kind;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import javax.swing.JOptionPane;

import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_DELETE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;
import static java.nio.file.StandardWatchEventKinds.OVERFLOW;

public class client {
	static int puerto=0;
	static Socket client_socket;
	
	static PrintWriter out;
	static ObjectInputStream in;
	
	static ArrayList<String> listClientFiles;
	static ArrayList<byte[]> listClientHash;
	static HashMap<String,byte[]> mapClient;
	
	static ArrayList<String> listServerFiles;
	static ArrayList<byte[]> listServerHash;
	static HashMap<String,byte[]> mapServer;
	static File dirClient ;
	static String clientFolder;
	
	static String serverPath;
	public client (){
		
		
	}
	
	public client(String cPath, String sPath){ //Para establecer la ruta en el servidor
		this.clientFolder = cPath;
		this.serverPath = sPath;
	}
	
	public static void main(String[] args) throws ClassNotFoundException, NoSuchAlgorithmException, IOException {
		
		
		dirClient = new File(clientFolder);
		
		if (!dirClient.exists()){		//Si no existe la carpeta la crea
			dirClient.mkdirs();
		}
		
		listClientFiles = new ArrayList<String>();
		listClientHash = new ArrayList<byte[]>();	
		mapClient = new HashMap <String,byte[]>();
		
		//Creamos el Map en el cliente (FileName:FileHash)
		File[] files = dirClient.listFiles();
		for (int i=0; i<files.length; i++){
			String fName = files[i].getName();
			byte[] fHash = calcularHASH(files[i]);
			mapClient.put(fName, fHash);
		}
		
		try {
			if(puerto==0){
				String stringInput; 
				stringInput = JOptionPane.showInputDialog("Introduzca Puerto");
				while(stringInput.equals("")){
					JOptionPane.showMessageDialog(null, "No puede estar vacio!!");
					stringInput = JOptionPane.showInputDialog("Introduzca Puerto");
				}
					puerto = Integer.parseInt(stringInput);
			}
			client_socket = new Socket("127.0.0.1", puerto);
			comenzarcliente(); 
			System.out.println("mal");
			
		} catch (IOException e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(null, "Error, verifique que el servidor esta en ejecucion"); 
		}
		
		
	}

	
	public static void comenzarcliente() throws IOException, ClassNotFoundException{
		
		out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(client_socket.getOutputStream())),true);
		in = new ObjectInputStream(client_socket.getInputStream());
		out.println("Directorio:" + serverPath);
		start();
	}	
	
	public static void start(){
		out.println("Packet:"); //Pedimos el Packet al servidor
		HashMap<String, byte[]> packet = null;
		try {
			packet = (HashMap<String, byte[]>) in.readObject();
		} catch (ClassNotFoundException | IOException e1) {
			e1.printStackTrace();
		}
		mapServer = new HashMap<String, byte[]>();
		mapServer = packet;
		
		listServerFiles = new ArrayList<String>();
		listServerHash = new ArrayList<byte[]>();
		
		
		//Pasamos los maps a dos listas cada uno (fileNames y fileHashs)
		Iterator it = mapServer.keySet().iterator();
		while(it.hasNext()){
			String nFile = (String) it.next();
			listServerFiles.add(nFile);
			listServerHash.add(packet.get(nFile));
		}
		
		Iterator itc = mapClient.keySet().iterator();
		while(itc.hasNext()){
			String nFile = (String) itc.next();
			listClientFiles.add(nFile);
			listClientHash.add(packet.get(nFile));
		}
		
		ArrayList <String> listcl = new ArrayList <>();
		File[] dirCli = dirClient.listFiles();
		for (int i=0; i<dirCli.length; i++){
			listcl.add(dirCli[i].getName());
		}
				
		try {
			Comprobar();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	
	public static void Comprobar() throws IOException{ //compara archivos
		
		ArrayList<String> iguales = new ArrayList<> (listClientFiles); 
		iguales.retainAll(listServerFiles);
		
		ArrayList<String> distintos = new ArrayList<>();
		distintos.addAll(listClientFiles);
		distintos.addAll(listServerFiles);
		distintos.removeAll(iguales); //elimina los archivos iguales
		
		ArrayList<String> upload = new ArrayList<> (distintos);
		ArrayList<String> download = new ArrayList<> (distintos);//trabajamos con los  distintos
		upload.removeAll(listServerFiles);
		download.removeAll(listClientFiles);
		
		ArrayList<String> modificados= new ArrayList<>();

		for (String s:iguales){ //comprobar Hash
			byte[] hashServer = mapServer.get(s);
			byte[] hashClient = mapClient.get(s);
			
			StringBuilder sbS = new StringBuilder();			
			for (byte bS : hashServer) {
				sbS.append(String.format("%02X ", bS));
			}
			
			StringBuilder sbC = new StringBuilder();			
			for (byte bC : hashClient) {
				sbC.append(String.format("%02X ", bC));
			}
			
			String stringClient = sbC.toString();
			String stringServer = sbS.toString();
		
			
			if (!stringClient.equals(stringServer)){
				 modificados.add(s);
			}
		}
		
		//Algoritmo de cristian
		long current = System.currentTimeMillis();
		long server_t = Hserver();
		long minDelay = retardo();
		long tDiference =current-server_t-minDelay/2;
		
		for (String file: modificados){//comprobacion de momento de cambios en los archivos
			long tFile = tCliente(file); 
			long tServer = ModARchivo(file); 
			
			if(tFile > tServer + tDiference){
				upload.add(file);
			}
			else{
				download.add(file);;
			}
			}
		
		uploadFiles(upload);
		downloadFiles(download);
		JOptionPane.showMessageDialog(null, "Finalizo Subida y Descarga"); 

		
		try {
			try {
				comprobarCarpetas();
			} catch (NoSuchAlgorithmException e) {
				e.printStackTrace();
			}
		} catch (ClassNotFoundException | InterruptedException e) {

			e.printStackTrace();
		}
		}
		
		
	private static boolean downloadFiles(ArrayList<String> download) throws IOException {
		//metodo para la descarga		
		ArrayList<Hdown> threads = new ArrayList<Hdown>();			
		for (String f:download){
			Hdown hilodw = new Hdown(f, client_socket, dirClient.getAbsolutePath());
			threads.add(hilodw);
		}
		for (Hdown dw:threads){
			dw.run();
		}
		
		return true;
		
	}


	
	public static boolean uploadFiles(ArrayList<String> upload) throws IOException{
		//MEtodo para la subida
		
		ArrayList<HUp> threads = new ArrayList<HUp>();
		for (String f:upload){
			HUp hiloup = new HUp(f, client_socket, dirClient.getAbsolutePath());
			threads.add(hiloup);
		}
		for (HUp up:threads){
			up.run();
		}
		
		return true;
	}
//Metodos necesarios para el Algoritmo de Cristian	
	public static long retardo(){
		long delay = Long.MAX_VALUE;
		InetAddress ip;
		ip = client_socket.getInetAddress();
		int i = 0;
		while (i<15){ 	
			long tSend = System.currentTimeMillis(); 
			long tReceive;
			long req = Hserver();	
			tReceive = System.currentTimeMillis();
			long iDelay = tReceive - tSend;
			if (iDelay < delay){
				delay = iDelay;
			}
			i++;
		}
		
		return delay;
	}
	
	public static long tCliente(String file){
			String pathDir = dirClient.getAbsolutePath();
			String fileDir = pathDir +File.separator+ file;
			File archivo = new File(fileDir);
			
			if (archivo.exists()){
			return archivo.lastModified();
			}
			else{
				return -1;	
			}
			
	}
	
	public static long Hserver(){

		
		out.println("HoraServ:");
		long HoraServ = -1;
		try {
			HoraServ = (long) in.readObject();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		return HoraServ;
	}

	public static long ModARchivo(String file){
		out.println("ArchivoMod:"+file);
		long ArchivoMod = -1;
		try {
			ArchivoMod = (long) in.readObject();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		return ArchivoMod;
	}
 	public static byte[] calcularHASH(File f) throws NoSuchAlgorithmException, IOException{
		
		String datafile = f.getAbsolutePath();
		
	    MessageDigest md = MessageDigest.getInstance("SHA1");
	    FileInputStream fis = new FileInputStream(datafile);
	    byte[] dataBytes = new byte[1024];
	    
	    int nread = 0; 
	    
	    while ((nread = fis.read(dataBytes)) != -1) {
	      md.update(dataBytes, 0, nread);
	    };

	    byte[] mdbytes = md.digest();
		
		return mdbytes;		
	}
 	
 	
 	public static void comprobarCarpetas() throws ClassNotFoundException, IOException, InterruptedException, NoSuchAlgorithmException{
 		
 		Path path = dirClient.toPath();
 		FileSystem fs = path.getFileSystem();

 		try (WatchService comprobacion = fs.newWatchService()) {
 			
 			path.register(comprobacion,ENTRY_CREATE, ENTRY_MODIFY,ENTRY_DELETE);
 			
 			WatchKey key = null;
 			
			while (true) {
				key = comprobacion.take();
				Kind<?> kind = null;
				
				for (WatchEvent<?> watchEvent : key.pollEvents()) {
					kind = watchEvent.kind();
					if (OVERFLOW == kind) {
						continue; 
					} else {
						main(null);
					}
				}
				if (!key.reset()) {
					break; 
					}
 		}
 			
 		}
 		
 	}
 	
 	
}
