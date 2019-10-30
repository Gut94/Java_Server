package cloud1;


import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.RandomAccessFile;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

public class Hdown implements Runnable {
	
	FileInputStream ArchivoIS;
	String fileName;
	String ruta;
	Socket socket;
	static PrintWriter out;
	
	public Hdown(String f, Socket client_socket, String client_path) throws IOException {
		this.fileName = f;
		this.socket = client_socket;
		this.ruta = client_path;
		out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())),true); 
	}

	@Override
	public void run() {
		out.println("ArchivoDw:" + fileName);
		
		String fileDir = ruta + File.separator + fileName;
		ByteBuffer Buffer1 = ByteBuffer.allocate(4096);
		ServerSocketChannel listener;
		
		try {
			
			listener = ServerSocketChannel.open();
			ServerSocket ss = listener.socket();
			ss.setReuseAddress(true);
			ss.bind(new InetSocketAddress(10000));
			
			SocketChannel conn = listener.accept();
			conn.configureBlocking(true); 
			RandomAccessFile ArchivoIS = new RandomAccessFile(fileDir, "rw"); 
			FileChannel fc = ArchivoIS.getChannel();
			int nread = 0;
			while (nread != -1)  {
				try {
					nread = conn.read(Buffer1);
					Buffer1.flip();
					fc.write(Buffer1);
					Buffer1.clear();

				} catch (IOException e) {
					e.printStackTrace();
					nread = -1;
				}
				Buffer1.rewind();
				ss.close();
			}
		
			
		} catch (IOException e) {
			e.printStackTrace(); //error
		}
		
	}
	
	

}
