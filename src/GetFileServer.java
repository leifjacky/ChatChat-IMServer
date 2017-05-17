import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.activation.MimetypesFileTypeMap;

public class GetFileServer implements Runnable {

	private ExecutorService pool = Executors.newFixedThreadPool(5);
	
	public void run() {
		try {
			ServerSocket server = new ServerSocket(8080);
			System.out.println("GetFile Server STARTED");
			while (true){
				Socket socket = server.accept();
				System.out.println("GetFile Server Receive");
				pool.submit(new Client(socket));
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}  
	
}

class Client implements Runnable{
	
	private Socket socket;
	
	public Client(){}
	public Client(Socket socket){
		this.socket = socket;
	}
	
	public void run(){
		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			String st = reader.readLine().trim();
			System.out.println(st);
			if (!st.startsWith("GET") && !st.startsWith("UPLOAD")){
				st = st.substring(1);
			}
			 
			if (st.startsWith("GET")){
				String path = st.substring("GET".length() + 1);
				if (path.startsWith("/"))
					path = path.substring(1);
				Download(socket, path);
			} else if (st.startsWith("UPLOAD")) {
				String path = st.substring("UPLOAD".length() + 1);
				if (path.startsWith("/"))
					path = path.substring(1);
				Upload(socket, path);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
	}
	
	static void Download(Socket connection, String path){
        try{
			PrintStream out = new PrintStream(connection.getOutputStream(),true);
			
			File file = new File(path);
            
            System.out.println(connection.getInetAddress().toString().substring(1)+":"+connection.getPort()+" Ask for GETTING "+path);
            FileInputStream in = new FileInputStream(file);
            int len = 0;
            byte b[] = new byte[1024];
            while ((len = in.read(b, 0, 1024)) > 0){
            	out.write(b, 0, len);            	
            	out.flush();
            }
            out.close();
            System.out.println(connection.getInetAddress().toString().substring(1)+":"+connection.getPort()+" Finish GETTING "+path);
            in.close();
		} catch (Exception e){
			e.printStackTrace();
			try {
				connection.close();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
	}
	
	static void Upload(Socket connection, String path){
		try {
			System.out.println("UPLOAD");
			InputStream in = connection.getInputStream();
			FileOutputStream out;
			File file = new File(path);
			File parentFile = file.getParentFile();
			if (!parentFile.exists()){
				parentFile.mkdirs();
			}
			if (file.exists()){
				file.delete();
			}
			file.createNewFile();
			
			out = new FileOutputStream(file);
			int len = 0;
			byte b[] = new byte[1024];
			while ((len=in.read(b, 0, 1024)) > 0){
				out.write(b, 0, len);
				out.flush();
			}
			out.close();
			in.close();		
			connection.close();
			System.out.println("FINISH UPLOAD");
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	
	}
}