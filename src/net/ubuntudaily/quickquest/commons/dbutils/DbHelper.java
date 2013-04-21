package net.ubuntudaily.quickquest.commons.dbutils;

import java.sql.Connection;
import java.sql.SQLException;

public class DbHelper{

	public static void rollbackQuietly(Connection conn){
		if(conn != null){
			try {
				conn.rollback();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}
