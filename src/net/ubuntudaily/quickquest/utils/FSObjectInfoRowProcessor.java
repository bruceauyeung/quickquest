package net.ubuntudaily.quickquest.utils;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import org.apache.commons.dbutils.RowProcessor;

public class FSObjectInfoRowProcessor implements RowProcessor {

	@Override
	public Object[] toArray(ResultSet rs) throws SQLException {
		if (!rs.next()) {
            return null;
        }
    
        ResultSetMetaData meta = rs.getMetaData();
        int cols = meta.getColumnCount();
        Object[] result = new Object[cols];

        for (int i = 0; i < cols; i++) {
            result[i] = rs.getObject(i + 1);
        }

        return result;
	}

	@Override
	public <T> T toBean(ResultSet arg0, Class<T> arg1) throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <T> List<T> toBeanList(ResultSet arg0, Class<T> arg1)
			throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Map<String, Object> toMap(ResultSet arg0) throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}

}
