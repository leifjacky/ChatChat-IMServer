import java.util.*;

public class StringUtils {
	
	public static final int KEY_LENGTH = 64;

	public static HashMap<String, String> analyse(String st){
		String key, value;
		HashMap<String, String> map = new HashMap<String, String>();
		while (!st.equals("")){
			value = st.substring(st.lastIndexOf('=') + 1, st.length());
			key = st.substring(st.lastIndexOf('&') + 1, st.lastIndexOf('='));
			map.put(key, value);
			//System.out.println(key + ' ' + value);
			st = st.substring(0, st.lastIndexOf('&'));
		}
		
		return map;
	}
	
	public static String getRandomString(int length) { //length表示生成字符串的长度  
	    String base = "abcdefghijklmnopqrstuvwxyz0123456789";     
	    Random random = new Random();     
	    StringBuffer sb = new StringBuffer();     
	    for (int i = 0; i < length; i++) {     
	        int number = random.nextInt(base.length());     
	        sb.append(base.charAt(number));     
	    }     
	    return sb.toString();     
	 }
	

    public static String getSessionID(String strA, String strB){
        if (strA.compareTo(strB) <= 0){
            return strA + "_" + strB;
        } else {
            return strB + "_" + strA;
        }
    }
}
