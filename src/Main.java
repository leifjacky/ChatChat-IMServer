import java.io.IOException;
import java.net.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class Main {
	
	private static final int PORT = 5222;
	
	public static void main(String args[]){
		Connection sqlConnection = null;
        String sql;

        String url = "jdbc:mysql://localhost:3306/chatchat?"
                + "user=root&password=&useUnicode=true&characterEncoding=UTF8&useSSL=false";
		
		try {
			Class.forName("com.mysql.jdbc.Driver");
			System.out.println("成功加载MySQL驱动程序");
            sqlConnection = DriverManager.getConnection(url);
            Statement statement = sqlConnection.createStatement();
			
			ServerSocket server = new ServerSocket(PORT);
			System.out.println("Client Server Started!");
			
			Thread getFileServer = new Thread(new GetFileServer());
			getFileServer.start();
			
			Thread onlineServer = new Thread(new OnlineServer());
			onlineServer.start();
			
			while (true){
				Socket connection = server.accept();
				System.out.println("Client Server receive from " + connection.getInetAddress() + ":" + connection.getPort());
				new Thread(new ClientThread(connection, statement)).start();
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
}
