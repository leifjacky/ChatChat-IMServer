import java.io.*;
import java.net.*;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ClientThread implements Runnable {

	public static final String _ID = "id";
	public static final String ACCOUNT = "account";
	public static final String NAME = "name";
	public static final String SORT = "sort";
	public static final String SECTION = "section";
	public static final String AVATAR = "avatar";
	public static final String FRIENDS = "friends";
	public static final String PASSWORD = "password";

	Socket socket;
	Statement statement;
	BufferedReader reader;
	PrintStream printer;
	String sql;
	HashMap<String, String> map;

	ClientThread() {
	}

	ClientThread(Socket socket, Statement statement) {
		this.socket = socket;
		this.statement = statement;
		try {
			reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			printer = new PrintStream(socket.getOutputStream(), true);
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	@Override
	public void run() {
		try {
			sql = "alter table users convert to charset utf8mb4";
			statement.executeUpdate(sql);
			System.out.println("users(table)使用uft8mb4编码");
			sql = "alter table sms convert to charset utf8mb4";
			statement.executeUpdate(sql);
			System.out.println("sms(table)使用uft8mb4编码");

			String st = reader.readLine();
			System.out.println(st);

			if (st.startsWith("LOGIN")) { // 登录
				actLOGIN(st.substring("LOGIN".length()));
			} else if (st.startsWith("REGISTER")) { // 注册
				actREGISTER(st.substring("REGISTER".length()));
			} else if (st.startsWith("UPDATE")) { // 更新资料
				actUPDATE(st.substring("UPDATE".length()));
			} else if (st.startsWith("SEARCH")) { // 搜索用户
				actSEARCH(st.substring("SEARCH".length()));
			} else if (st.startsWith("CONTACT")) {
				actCONTACT(st.substring("CONTACT".length()));
			} else if (st.startsWith("ACCEPTFRIEND")) {
				actACCEPTFRIEND(st.substring("ACCEPTFRIEND".length()));
			} else if (st.startsWith("SMS")) {
				actSMS(st.substring("SMS".length()));
			} else if (st.startsWith("SHARE")) {
				actSHARE(st.substring("SHARE".length()));
			} else if (st.startsWith("PHOTO")){
				actPHOTO(st.substring("PHOTO".length()));
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public void actLOGIN(String st) {
		try {
			map = StringUtils.analyse(st);
			sql = "select * from users where account = '" + map.get("account") + "'";
			System.out.println(sql);
			ResultSet rs = statement.executeQuery(sql);

			if (rs.next()) {
				if (map.get("password").equals(rs.getString(3))) {
					String keycode = StringUtils.getRandomString(StringUtils.KEY_LENGTH);
					printer.println("ACCEPT&id=" + rs.getString(1) + "&account=" + rs.getString(2) + "&nickname="
							+ rs.getString(4) + "&avatar=" + rs.getString(5) + "&key=" + keycode);
					System.out.println("ACCEPT&id=" + rs.getString(1) + "&account=" + rs.getString(2) + "&nickname="
							+ rs.getString(4) + "&avatar=" + rs.getString(5) + "&key=" + keycode);

					String str = rs.getString(6);
					System.out.println(str);
					Pattern p = Pattern.compile("[0-9]+");
					Matcher m = p.matcher(str);
					while (m.find()) {
						sql = "select * from users where _id = " + m.group();
						System.out.println(sql);
						rs = statement.executeQuery(sql);
						if (rs.next()) {
							printer.println("&id=" + rs.getString(1) + "&account=" + rs.getString(2) + "&name="
									+ rs.getString(4) + "&avatar=" + rs.getString(5));
							printer.flush();
							System.out.println("&id=" + rs.getString(1) + "&account=" + rs.getString(2) + "&name="
									+ rs.getString(4) + "&avatar=" + rs.getString(5));
						}
					}
					printer.println("END");
					System.out.println("END");
				} else {
					printer.println("FAILED");
					System.out.println("FAILED");
				}
			} else {
				printer.println("FAILED");
				System.out.println("FAILED");
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public void actREGISTER(String st) {
		try {
			map = StringUtils.analyse(st);
			sql = "select * from users where account = '" + map.get(ACCOUNT) + "'";
			System.out.println(sql);
			ResultSet rs = statement.executeQuery(sql);

			if (rs.next()) {
				printer.println("FAILED");
			} else {
				sql = "select * from users order by _id desc";
				rs = statement.executeQuery(sql);
				int num = 1;
				if (rs.next()) {
					num = rs.getInt(1);
					System.out.println(num);
				}
				sql = "insert into users(_id, account, password, name, friends) values " + "(" + (num + 1) + ", '"
						+ map.get("account") + "', '" + map.get("password") + "', '" + map.get("account") + "', "
						+ (num + 1) + ")";
				System.out.println(sql);
				int ret = statement.executeUpdate(sql);
				if (ret == 1)
					printer.println("ACCEPT");
				else
					printer.println("FAILED");
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public void actUPDATE(String st) {
		try {
			map = StringUtils.analyse(st);
			String account = map.get(ACCOUNT);
			String key = ACCOUNT + " = '" + account + "'";
			String keys[] = { NAME, PASSWORD, AVATAR, FRIENDS };
			ResultSet rs;

			for (String iter : keys) {
				String value = map.get(iter);
				if (value != null) {
					key = key + "," + iter + " = '" + value + "'";
				}
			}
			sql = "UPDATE users SET " + key + " WHERE " + ACCOUNT + " = '" + account + "'";
			System.out.println(sql);
			int ret = statement.executeUpdate(sql);

			if (ret == 1) {
				sql = "select * from users where account = '" + account + "'";
				System.out.println(sql);
				rs = statement.executeQuery(sql);
				if (rs.next()) {
					String friends = rs.getString(rs.findColumn("friends"));
					Pattern p = Pattern.compile("[0-9]+");
					Matcher m = p.matcher(friends);
					while (m.find()) {
						sql = "select * from users where _id = " + m.group();
						System.out.println(sql);
						rs = statement.executeQuery(sql);
						if (rs.next()) {
							sql = "insert into sms(type, fromAccount, toAccount, content) values (" + "'" + "UPDATE"
									+ "', " + "'" + account + "', " + "'" + rs.getString(rs.findColumn(ACCOUNT)) + "', "
									+ "'" + st + "')";
							System.out.println(sql);
							statement.executeUpdate(sql);
						}
					}
				}

				System.out.println("UPDATED");
				printer.println("UPDATED");
				printer.flush();
			} else {
				System.out.println("FAILED");
				printer.println("FAILED");
				printer.flush();
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public void actSEARCH(String st) {
		try {
			map = StringUtils.analyse(st);
			String account = map.get(ACCOUNT);
			sql = "select * from users where account = '" + map.get(ACCOUNT) + "'";
			System.out.println(sql);
			ResultSet rs = statement.executeQuery(sql);

			if (rs.next()) {
				System.out.print("FOUND");
				System.out.println("&id=" + rs.getString(1) + "&account=" + rs.getString(2) + "&name=" + rs.getString(4)
						+ "&avatar=" + rs.getString(5));
				printer.print("FOUND");
				printer.println("&id=" + rs.getString(1) + "&account=" + rs.getString(2) + "&name=" + rs.getString(4)
						+ "&avatar=" + rs.getString(5));
			} else {
				printer.println("FAILED");
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public void actCONTACT(String st) {
		String fromAccount, toAccount, fromAvatar, fromId, fromName;
		try {
			map = StringUtils.analyse(st);
			
			fromAccount = map.get("fromAccount");
			sql = "select * from users where account = '" + fromAccount + "'";
			System.out.println(sql);
			ResultSet rs = statement.executeQuery(sql);

			if (rs.next()) {
				System.out.print("FOUND");
				System.out.println("&id=" + rs.getString(1) + "&account=" + rs.getString(2) + "&name=" + rs.getString(4)
						+ "&avatar=" + rs.getString(5));
				
				sql = "insert into sms(type, fromAccount, toAccount, content) values ("
						+ "'" + "NEWFRIEND" + "', "
						+ "'" + map.get("fromAccount") + "', "
						+ "'" + map.get("toAccount") + "', "
						+ "'" + "&id=" + rs.getString(1) + "&account=" + rs.getString(2) + "&name=" + rs.getString(4) + "&avatar=" + rs.getString(5) + "')";
				System.out.println(sql);
				int ret = statement.executeUpdate(sql);
				printer.println("OK");
			} else {
				printer.println("FAILED");
			}
		} catch (SQLException e) {
			e.printStackTrace();
			printer.print("FAILED");
		}
	}

	public void actACCEPTFRIEND(String st) {
		String fromAccount, toAccount, fromid = null, fromfriends = null, toid = null, tofriends = null;
		int ret = 0;
		
		try {
			map = StringUtils.analyse(st);
			fromAccount = map.get("fromAccount");
			sql = "select * from users where account = '" + fromAccount + "'";
			System.out.println(sql);
			ResultSet rs = statement.executeQuery(sql);
			if (rs.next()) {
				System.out.print("FOUND");
				System.out.println("&id=" + rs.getString(1) + "&account=" + rs.getString(2) + "&name=" + rs.getString(4)
						+ "&avatar=" + rs.getString(5) + "&friends=" + rs.getString(6));

				fromid = rs.getString(rs.findColumn("_id"));
				fromfriends = rs.getString(rs.findColumn("friends"));
			}
			
			toAccount = map.get("toAccount");
			sql = "select * from users where account = '" + toAccount + "'";
			System.out.println(sql);
			rs = statement.executeQuery(sql);
			if (rs.next()) {
				System.out.print("FOUND");
				System.out.println("&id=" + rs.getString(1) + "&account=" + rs.getString(2) + "&name=" + rs.getString(4)
						+ "&avatar=" + rs.getString(5) + "&friends=" + rs.getString(6));

				toid = rs.getString(rs.findColumn("_id"));
				tofriends = rs.getString(rs.findColumn("friends"));
			}

			sql = "UPDATE users SET friends = '" + fromfriends + "_" + toid  + "' WHERE account = '" + fromAccount + "'";
			System.out.println(sql);
			ret = statement.executeUpdate(sql);
			
			sql = "UPDATE users SET friends = '" + tofriends + "_" + fromid + "' WHERE account = '" + toAccount + "'";
			System.out.println(sql);
			ret = statement.executeUpdate(sql);
			
			sql = "insert into sms(type, fromAccount, toAccount) values ("
					+ "'" + "ACFRIEND" + "', "
					+ "'" + fromAccount + "', "
					+ "'" + toAccount + "')";
			System.out.println(sql);
			ret = statement.executeUpdate(sql);
			
			sql = "insert into sms(type, fromAccount, toAccount) values ("
					+ "'" + "ACFRIEND" + "', "
					+ "'" + toAccount + "', "
					+ "'" + fromAccount + "')";
			System.out.println(sql);
			ret = statement.executeUpdate(sql);
			
			System.out.println("OK");
			printer.println("OK");
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	public void actSHARE(String st) {
		try {
			map = StringUtils.analyse(st);
			sql = "select * from users where account = '" + map.get("fromAccount") + "'";
			System.out.println(sql);
			ResultSet rs = statement.executeQuery(sql);

			if (rs.next()) {
				System.out.print("FOUND");
				System.out.println("&id=" + rs.getString(1) + "&account=" + rs.getString(2) + "&name=" + rs.getString(4)
						+ "&avatar=" + rs.getString(5) + "&friends=" + rs.getString(6));

				String friends = rs.getString(rs.findColumn("friends"));
				Pattern p = Pattern.compile("[0-9]+");
				Matcher m = p.matcher(friends);
				while (m.find()) {
					sql = "select * from users where _id = " + m.group();
					System.out.println(sql);
					rs = statement.executeQuery(sql);
					if (rs.next()) {
						sql = "insert into sms(type, fromAccount, toAccount, content, photo) values ("
								+ "'" + "SHARE" + "', "
								+ "'" + map.get("fromAccount") + "', "
								+ "'" + rs.getString(rs.findColumn(ACCOUNT)) + "', "
								+ "'" + map.get("content") + "', "
								+ "'"+ map.get("photo") + "')";
						System.out.println(sql);
						int ret = statement.executeUpdate(sql);
					}
				}

				System.out.println("OK");
				printer.println("OK");
			} else {
				System.out.println("FAILED");
				printer.println("FAILED");
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	public void actSMS(String st){
		try {
			map = StringUtils.analyse(st);
			String session_id = StringUtils.getSessionID(map.get("fromAccount"), map.get("toAccount"));
			sql = "insert into sms(type, fromAccount, toAccount, content, photo) values ("
					+ "'" + "SMS" + "', "
					+ "'" + map.get("fromAccount") + "', "
					+ "'" + map.get("toAccount") + "', "
					+ "'" + map.get("content") + "&session_id=" + session_id + "', "
					+ "'"+ map.get("photo") + "')";
			System.out.println(sql);
			int ret = statement.executeUpdate(sql);

			sql = "insert into sms(type, fromAccount, toAccount, content, photo) values ("
					+ "'" + "SMS" + "', "
					+ "'" + map.get("fromAccount") + "', "
					+ "'" + map.get("fromAccount") + "', "
					+ "'" + map.get("content") + "&session_id=" + session_id + "', "
					+ "'"+ map.get("photo") + "')";
			System.out.println(sql);
			ret = statement.executeUpdate(sql);
			
			System.out.println("OK");
			printer.println("OK");
		} catch (SQLException e) {
			e.printStackTrace();
			System.out.println("FAILED");
			printer.println("FAILED");
		}
	}
	
	public void actPHOTO(String st){
		try {
			map = StringUtils.analyse(st);
			String session_id = StringUtils.getSessionID(map.get("fromAccount"), map.get("toAccount"));
			sql = "insert into sms(type, fromAccount, toAccount, content, photo) values ("
					+ "'" + "PHOTO" + "', "
					+ "'" + map.get("fromAccount") + "', "
					+ "'" + map.get("toAccount") + "', "
					+ "'" + map.get("content") + "&session_id=" + session_id + "', "
					+ "'"+ map.get("photo") + "')";
			System.out.println(sql);
			int ret = statement.executeUpdate(sql);

			sql = "insert into sms(type, fromAccount, toAccount, content, photo) values ("
					+ "'" + "PHOTO" + "', "
					+ "'" + map.get("fromAccount") + "', "
					+ "'" + map.get("fromAccount") + "', "
					+ "'" + map.get("content") + "&session_id=" + session_id + "', "
					+ "'"+ map.get("photo") + "')";
			System.out.println(sql);
			ret = statement.executeUpdate(sql);
			
			System.out.println("OK");
			printer.println("OK");
		} catch (SQLException e) {
			e.printStackTrace();
			System.out.println("FAILED");
			printer.println("FAILED");
		}
	}
}