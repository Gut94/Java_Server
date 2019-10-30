package cloud1;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.io.RandomAccessFile;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;

import javax.swing.JOptionPane;

public class HServer implements Runnable {
	
	static Socket sc;
	static BufferedReader in;
	static ObjectOutputStream out;
	static String serverFolder;
	static File dirServer;
	
	public HServer(Socket so, String path){
		sc = so;
		serverFolder = path;
	}
	
	@Override
	public void run() {
		try {
			comenzarcliente();
		} catch (NoSuchAlgorithmException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	

	public static void comenzarcliente() throws IOException, NoSuchAlgorithmException{
		
		serverFolder = File.separator;
		dirServer = new File(serverFolder);
		
		in = new BufferedReader(new InputStreamReader(sc.getInputStream()));
		out = new ObjectOutputStream(sc.getOutputStream());
		
		while(true){
			String msg=null;
			while(msg==null){
				msg= in.readLine();
			}
			EscucharEntrada(msg);
		}
		
		
	}
	
	public static void EscucharEntrada(String msg) throws NoSuchAlgorithmException, IOException{
		
		
		String str = msg.substring(0, msg.indexOf(':'));
		switch(str){
		case "Packet":			
			out.writeObject(Packet());
			break;
		case "Directorio":		
			Directorio((msg.substring(msg.indexOf(':')+1)));
			break;
		case "HoraServ":			
			long HoraServ = System.currentTimeMillis();
			out.writeObject(HoraServ);
			break;
		case "ArchivoMod":			
			out.writeObject(ArchivoMod(msg.substring(msg.indexOf(':')+1)));
			break;
		case "ArchivoUp":			
			String filePath = (msg.substring(msg.indexOf(':')+1));			
			upload(filePath);
			break;
		case "ArchivoDw":			
			String fileName = (msg.substring(msg.indexOf(':')+1));			
			download(fileName);
			break;
			
		}
		
	}
	
	private static void Directorio(String newPath){
		serverFolder = newPath;
		dirServer = new File (serverFolder);
		if (!dirServer.exists()){
			dirServer.mkdirs();
		}
		
	}
	
	private static void download(String fileName) {
		String fileDir = serverFolder + File.separator + fileName;
		File archivo = new File(fileDir);
		
		int port = 10000;
		
		try {
			Thread.sleep(500);
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		}
		
		try {
	    	
		    SocketAddress DireccionSocket = new InetSocketAddress("127.0.0.1", port);
		    SocketChannel sc = SocketChannel.open();
		    sc.connect(DireccionSocket);
		    sc.configureBlocking(true);

		    long fsize = archivo.length();
		    
		    FileInputStream ArchivoIS = new FileInputStream(archivo);
			FileChannel fc = ArchivoIS.getChannel();
		    long curnset = 0;
		    curnset =  fc.transferTo(0, fsize, sc);
		
		    fc.close();
			sc.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		
	}

	public static void upload(String filePath) throws IOException{
		
		
		if (!dirServer.exists()){	//comprobar si existe el directorio
			dirServer.mkdirs();
		}
		
		String pathDir = dirServer.getAbsolutePath();
		String fileDir = pathDir +File.separator+ filePath;
			
		
		ByteBuffer dst = ByteBuffer.allocate(4096);
		
		ServerSocketChannel listener = ServerSocketChannel.open();
		ServerSocket ss = listener.socket();
		ss.setReuseAddress(true);
		ss.bind(new InetSocketAddress(10000));
		
		try {
				
				SocketChannel conn = listener.accept();
			
				conn.configureBlocking(true);
				RandomAccessFile ArchivoIS = new RandomAccessFile(fileDir, "rw");
				FileChannel fc = ArchivoIS.getChannel();
				int nread = 0;
				while (nread != -1)  {
					try {
						nread = conn.read(dst);
						dst.flip();
						fc.write(dst);
						dst.clear();

					} catch (IOException e) {
						e.printStackTrace();
						nread = -1;
					}
					dst.rewind();
					ss.close(); //Si no, no aceptaria las siguientes
				}
				
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		
	}
	
	public static long ArchivoMod(String file){
		String pathDir = dirServer.getAbsolutePath();
		String fileDir = pathDir +File.separator+ file;
		File archivo = new File(fileDir);
		
		if (archivo.exists()){
		return archivo.lastModified();
		}
		else{
			return -1;	
		}
		
}
	
	public static HashMap Packet() throws NoSuchAlgorithmException, IOException{ 	//Creamos el paquete que enviamos al servidor, este paquete consiste en un Map (FileName:FileHash)
		HashMap<String,byte[]> packet = new HashMap<String,byte[]>();
		
		if (dirServer.exists()){
			File[] server_files = dirServer.listFiles();
			
			for (int i=0; i<server_files.length; i++){
				String fName = server_files[i].getName();
				byte[] fHash = calcularHASH(server_files[i]);
				packet.put(fName, fHash);
			}
			
		} else {
			JOptionPane.showMessageDialog(null, "verifique carpeta");
		
		}
		return packet;
		
	}
	
	public static byte[] calcularHASH(File f) throws NoSuchAlgorithmException, IOException{
			
			String datafile = f.getAbsolutePath();
			
		    MessageDigest md = MessageDigest.getInstance("SHA1");
		    FileInputStream ArchivoIS = new FileInputStream(datafile);
		    byte[] dataBytes = new byte[1024];
		    
		    int nread = 0; 
		    
		    while ((nread = ArchivoIS.read(dataBytes)) != -1) {
		      md.update(dataBytes, 0, nread);
		    };
	
		    byte[] mdbytes = md.digest();
			
			return mdbytes;		
		}

}
