package us.suselinks.quickquest.commons.io;

import java.sql.Connection;
import java.sql.Statement;


public class IOUtils extends org.apache.commons.io.IOUtils{

	public static final void closeQuietly(Statement stmt){
		if(stmt != null){
			try{
				stmt.close();
			}catch(Exception e){}
		}

	}
	public static final void closeQuietly(Connection conn){
		if(conn != null){
			try{
				conn.close();
			}catch(Exception e){}
		}
	}
}
