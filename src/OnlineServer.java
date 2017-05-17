import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Queue;

public class OnlineServer implements Runnable {
	
	private Connection sqlConnection;
	
	public OnlineServer(){
        String sql;

        String url = "jdbc:mysql://localhost:3306/chatchat?"
                + "user=root&password=zukiminda&useUnicode=true&characterEncoding=UTF8&useSSL=false";
		
		try {
			Class.forName("com.mysql.jdbc.Driver");
			System.out.println("成功加载MySQL驱动程序");
            sqlConnection = DriverManager.getConnection(url);
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static HashMap<String, OnlineClient> clientMap = new HashMap<String, OnlineClient>();
	
	public void run() {
		try {
			ServerSocket server = new ServerSocket(5223);
			System.out.println("OnlineServer STARTED");
			while (true){
				Socket socket = server.accept();
				System.out.println("OnlineServer Receive");
				OnlineClient onlineClient = new OnlineClient(socket, sqlConnection);
				
				new Thread(onlineClient).start();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}  
}

class OnlineClient implements Runnable{

    public static final String _ID = "id";
    public static final String ACCOUNT = "account";
    public static final String NAME = "name";
    public static final String DATE = "date";
    public static final String AVATAR = "avatar";
    public static final String PHOTO = "photo";
    public static final String CONTENT = "content";
	
	private Socket socket;
	private BufferedReader reader;
	private PrintStream printer;
	private String sql;
	private Connection sqlConnection;
	private Statement statement;
	
	private HashMap<String, String> map = new HashMap<String, String>();
	
	public OnlineClient(Socket socket, Connection sqlConnection){
		this.socket = socket;
		this.sqlConnection = sqlConnection;
	}
	
	public void run() {
		try {
			printer = new PrintStream(socket.getOutputStream(),true);
			reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		try {
			while (true){
				String st = null;
				st = reader.readLine();
				if (st.startsWith("SMS")){
					map = StringUtils.analyse(st.substring("SMS".length()));
					String toAccount = map.get("account");
					
					sql = "select count(*) from sms where toAccount = '" + toAccount + "'";
					statement = sqlConnection.createStatement();
					ResultSet rs = statement.executeQuery(sql);
					int num = 0;
					if (rs.next()){
						num = rs.getInt(1);
					}
					printer.println(num);
					printer.flush();
					
					if (num > 0){
						System.out.println(num);
					
						sql = "select * from sms where toAccount = '" + toAccount + "'";
						statement = sqlConnection.createStatement();
						rs = statement.executeQuery(sql);
						for (int i = 0; i < num; i++){
							rs.next();
							String type = rs.getString("type");
							int id = Integer.valueOf(rs.getString("_id"));
							String fromAccount = rs.getString("fromAccount");
							String content = rs.getString("content");
							String photo = rs.getString("photo");
							String date = rs.getString("time");
							if (type.equals("UPDATE")){							//更新数据
								System.out.println("UPDATE" + content);	
								printer.println("UPDATE" + content);
								printer.flush();
							} else if (type.equals("SMS")){						//发信息
								System.out.println("SMS&fromAccount=" + fromAccount
												+ "&toAccount=" + toAccount
												+ "&content=" + content
												+ "&photo=" + photo
												+ "&date=" + date);
								printer.println("SMS&fromAccount=" + fromAccount
												+ "&toAccount=" + toAccount
												+ "&content=" + content
												+ "&photo=" + photo
												+ "&date=" + date);
								printer.flush();
							} else if (type.equals("PHOTO")){
								System.out.println("PHOTO&fromAccount=" + fromAccount
												+ "&toAccount=" + toAccount
												+ "&content=" + content
												+ "&photo=" + photo
												+ "&date=" + date);
								printer.println("PHOTO&fromAccount=" + fromAccount
												+ "&toAccount=" + toAccount
												+ "&content=" + content
												+ "&photo=" + photo
												+ "&date=" + date);
								printer.flush();
							} else if (type.equals("SHARE")) {					//朋友圈
								System.out.println("SHARE&account=" + fromAccount
										+ "&content=" + content
										+ "&photo=" + photo
										+ "&date=" + date);
								printer.println("SHARE&account=" + fromAccount
										+ "&content=" + content
										+ "&photo=" + photo
										+ "&date=" + date);
								printer.flush();
							} else if (type.equals("NEWFRIEND")){				//加好友
								System.out.println("NEWFRIEND" + content);
								printer.println("NEWFRIEND" + content);
								printer.flush();
							} else if (type.equals("ACFRIEND")){
								sql = "select * from users where account = '" + fromAccount + "'";
								System.out.println(sql);
								Statement statement1 = sqlConnection.createStatement();
								ResultSet rs1 = statement1.executeQuery(sql);
								if (rs1.next()) {
									printer.println("ACFRIEND&id=" + rs1.getString(1) + "&account=" + rs1.getString(2) + "&name="
											+ rs1.getString(4) + "&avatar=" + rs1.getString(5));
									printer.flush();
									System.out.println("ACFRIEND&id=" + rs1.getString(1) + "&account=" + rs1.getString(2) + "&name="
											+ rs1.getString(4) + "&avatar=" + rs1.getString(5));
								}
							}
							
							String tempSQL = "delete from sms where _id = " + id;
							Statement statement1 = sqlConnection.createStatement(); 
							statement1.executeUpdate(tempSQL);
						}
					}
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	class SMS {
		String content, date;
	}
}