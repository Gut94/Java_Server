package cloud1;

import java.io.BufferedInputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.SocketChannel;

public class HUp implements Runnable {
	
	
	
	FileInputStream ArchivoIS;
	BufferedInputStream BufferIS;
	String fileName;
	String ruta;
	Socket socket;
	static PrintWriter out;
	
	
	public HUp(String f, Socket socket_cliente, String ruta_cliente) throws IOException {
		this.fileName = f;
		this.socket = socket_cliente;
		this.ruta =ruta_cliente;
		out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())),true); 
		
		
	}
	@Override
	public void run() {
		out.println("ArchivoUp:" + fileName);
		String Dir = ruta + File.separator + fileName;
		File archivo = new File(Dir);
		
		int port = 10000;
		out.println("upload:"+ port);
		try {
			Thread.sleep(500);
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		}

		
	    try {
		    SocketAddress DireccionSocket = new InetSocketAddress("127.0.0.1", port);
		    SocketChannel SocketC = SocketChannel.open();
		    SocketC.connect(DireccionSocket); //
		    SocketC.configureBlocking(true);

		    long fsize = archivo.length();
		    
		    ArchivoIS = new FileInputStream(archivo);
			FileChannel fc = ArchivoIS.getChannel();
		    long curnset = 0;
		    curnset =  fc.transferTo(0, fsize, SocketC);
		    		    
		    fc.close();//cerramos los Sockets
			SocketC.close();
		} catch (IOException e) {
			e.printStackTrace();//Error
		}
 
	       
	}
	

}
