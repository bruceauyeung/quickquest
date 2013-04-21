package net.ubuntudaily.quickquest;

import java.beans.PropertyVetoException;
import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.CopyOnWriteArraySet;

import net.ubuntudaily.quickquest.commons.collections.Lists;
import net.ubuntudaily.quickquest.commons.dbutils.DbHelper;
import net.ubuntudaily.quickquest.commons.io.FileUtils;
import net.ubuntudaily.quickquest.commons.io.FilenameUtils;
import net.ubuntudaily.quickquest.commons.io.IOUtils;
import net.ubuntudaily.quickquest.fsobject.FSObject;
import net.ubuntudaily.quickquest.utils.FSObjectUtils;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.ArrayHandler;
import org.apache.commons.dbutils.handlers.ArrayListHandler;
import org.apache.commons.dbutils.handlers.BeanListHandler;
import org.apache.commons.lang3.Range;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.helper.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mchange.v2.c3p0.ComboPooledDataSource;

/**
 * 
 * @author bruce
 * @see <a
 *      href="http://en.wikipedia.org/wiki/Comparison_of_file_systems">http://en.wikipedia.org/wiki/Comparison_of_file_systems</a>
 * @see <a href="http://hsqldb.org/doc/guide/">http://hsqldb.org/doc/guide/</a>
 */
public class HyperSQLManager {

	private static final Logger LOGGER = LoggerFactory
			.getLogger(HyperSQLManager.class);
	private static ComboPooledDataSource dataSource = null;
	private static CopyOnWriteArraySet<String> allCreatedTables = new CopyOnWriteArraySet<String>();

	public static void main(String[] args) {
		LOGGER.trace("trace");
		startupDB();
		createTables(0, 1);
		deleteAll();
		selectAll();
		shutdownDB();

	}

	public static FSObject insert(FSObject parent, File file) throws SQLException{
		FSObject fsobj = FSObjectUtils.convertFrom(file);
		if(parent == null){
			fsobj.setPoid(-1);
		}else{
			fsobj.setPoid(parent.getId());
		}
		
		insertFSObjectInfo(fsobj);
		fsobj = revalidate(fsobj);
		return fsobj;
	}
	public static FSObject revalidate (FSObject fsobj){

		QueryRunner qr = new QueryRunner();
		String sql = "select * from " + getTableName(fsobj.getDepth()) + " where name=? and depth=? and poid=?";
		Object[] params = new Object[]{fsobj.getName(), fsobj.getDepth(), fsobj.getPoid()};
		BeanListHandler<FSObject> blh = new BeanListHandler(
				FSObject.class);
		List<FSObject> newObjs = null;
		Connection conn = null;
		try {
			conn = getConnection();
			newObjs = qr.query(conn, sql, params, blh);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally{
			DbUtils.closeQuietly(conn);
		}
		FSObject newObj = null;
		if(newObjs != null && newObjs.size()>0){
			newObj = newObjs.get(0);
		}
		if(newObjs.size()>1){
			LOGGER.warn("two records are not expected.");
		}
		
		return newObj;
	}
	public static void insertFSObjectInfo(FSObject obj) {

		String tblName = "fs_obj_info" + obj.getDepth();
		PreparedStatement ps = null;
		Connection conn = null;
		try {
			conn = getConnection();
			ps = conn
					.prepareStatement("INSERT INTO "
							+ tblName
							+ "(name, depth, size, type, lmts, poid ) values (?,?,?,?,?,?);");
			ps.setString(1, obj.getName());
			ps.setInt(2, obj.getDepth());
			ps.setLong(3, obj.getSize());
			ps.setByte(4, obj.getType());
			ps.setTimestamp(5, obj.getLmts());
			ps.setLong(6, obj.getPoid());
			ps.executeUpdate();
			conn.commit();
		} catch (SQLException e) {
			DbHelper.rollbackQuietly(conn);
			LOGGER.error(e.getMessage(), e.getCause());
		} finally {
			DbUtils.closeQuietly(ps);
			DbUtils.closeQuietly(conn);

		}

	}

	public static void deleteAll() {
		Statement stmt = null;
		Connection conn = null;
		try {
			conn = getConnection();
			stmt = conn.createStatement();
			stmt.executeUpdate("delete from fs_obj_info;");
			conn.commit();
		} catch (SQLException e) {
			DbHelper.rollbackQuietly(conn);
			e.printStackTrace();
		} finally {
			DbUtils.closeQuietly(stmt);
			DbUtils.closeQuietly(conn);
		}

	}

	public static void startupDB() {
		try {
			dataSource = new ComboPooledDataSource();
			dataSource.setDriverClass("org.hsqldb.jdbcDriver");
			dataSource.setInitialPoolSize(2);
			dataSource
					.setJdbcUrl("jdbc:hsqldb:file:~/.quickquest/quickquest;shutdown=true;");
			dataSource.setMinPoolSize(2);
			dataSource.setMaxPoolSize(8);
			dataSource.setUser("quickquest");
			dataSource.setPassword("quickquest");
			dataSource.setAcquireIncrement(2);

			LOGGER.info("finished to startup hsqldb.");
		} catch (PropertyVetoException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			LOGGER.error("failed to startup hsqldb.", e.getCause());
		}

	}
	public static void dropAllTables(){
		Statement stmt = null;
		Connection conn = null;
		List<String> tbls = listAllTableNames();
		try {
			conn = getConnection();
			stmt = conn.createStatement();
			for (String tbl : tbls) {
				stmt.execute("drop table if exists " + tbl
						+ " CASCADE;");
			}
			conn.commit();
			allCreatedTables.clear();
		} catch (SQLException e) {
			DbHelper.rollbackQuietly(conn);
			e.printStackTrace();
		} finally {
			DbUtils.closeQuietly(stmt);
			DbUtils.closeQuietly(conn);
		}
	}
	public static void dropTables(int... suffixes) {
		Statement stmt = null;
		Connection conn = null;
		try {
			conn = getConnection();
			stmt = conn.createStatement();
			for (int suffix : suffixes) {
				stmt.execute("drop table if exists " + getTableName(suffix)
						+ " CASCADE;");
			}
			conn.commit();
			allCreatedTables.clear();
			allCreatedTables.addAll(listAllTableNames());
		} catch (SQLException e) {
			DbHelper.rollbackQuietly(conn);
			e.printStackTrace();
		} finally {
			DbUtils.closeQuietly(stmt);
			DbUtils.closeQuietly(conn);
		}
	}

	public static void createTables(int... suffixes) {
		Statement stmt = null;
		Connection conn = null;
		try {
			conn = getConnection();
			stmt = conn.createStatement();
			// http://orangeslate.com/2007/10/01/hsqldb-case-insensitive-like-query-three-implementation-methods/
			stmt.execute("SET IGNORECASE TRUE;");
			for (int suffix : suffixes) {
				// poids is an array with default maximum size of 1024. The
				// poids column has a default clause with an empty array.
				final String tableName = getTableName(suffix);
				stmt.execute("CREATE TABLE IF NOT EXISTS "
						+ tableName
						+ "(ID BIGINT IDENTITY, NAME VARCHAR(255), DEPTH INTEGER, SIZE BIGINT, TYPE TINYINT, LMTS TIMESTAMP,  POID BIGINT, UNIQUE (NAME,POID));");

			}
			stmt.execute("SET DATABASE SQL SYNTAX ORA TRUE;");
			conn.commit();
			allCreatedTables.clear();
			allCreatedTables.addAll(listAllTableNames());
		} catch (SQLException e) {
			DbHelper.rollbackQuietly(conn);
			e.printStackTrace();
		} finally {
			DbUtils.closeQuietly(stmt);
			DbUtils.closeQuietly(conn);
		}
	}

	public static void shutdownDB() {

		dataSource.close();
		// stmt.execute("SHUTDOWN");
		// DatabaseManager.closeDatabases(0);
		LOGGER.info("finished to shutdown hsqldb.");

	}

	public static Connection getConnection() throws SQLException {
		Connection connection = dataSource.getConnection();
		connection.setAutoCommit(false);
		return connection;
	}

	public static List<FSObject> selectAll() {

		return selectLike(null);
	}
	
	
	public static boolean delete(FSObject fso){
		
		int depth = fso.getDepth();
		StringBuilder sb = new StringBuilder("delete from ").append(getTableName(depth)).append(" where id=?");
		QueryRunner qr = new QueryRunner();
		int affected = 0;
		Connection conn = null;
		try {
			conn = getConnection();
			affected = qr.update(conn, sb.toString(), fso.getId());
			conn.commit();
		} catch (SQLException e) {
			DbHelper.rollbackQuietly(conn);
			LOGGER.error(e.getMessage());
		}finally{
			DbUtils.closeQuietly(conn);
		}
		if(affected > 0){
			return true;
		}
		return false;
	}
	public static boolean update(FSObject fso){
		
		int depth = fso.getDepth();
		StringBuilder sb = new StringBuilder("update ").append(getTableName(depth)).append(" set size=?, lmts=? where id=?");
		QueryRunner qr = new QueryRunner();
		int affected = 0;
		Connection conn = null;
		try {
			conn = getConnection();
			affected = qr.update(conn, sb.toString(), fso.getSize(), fso.getLmts(), fso.getId());
			conn.commit();
		} catch (SQLException e) {
			DbHelper.rollbackQuietly(conn);
			LOGGER.error(e.getMessage());
		}finally{
			DbUtils.closeQuietly(conn);
		}
		if(affected > 0){
			return true;
		}
		return false;
	}
	public static FSObject findEquivalent(File file) {

		if(file == null){
			return null;
		}
		FSObject equal = null;
		int depth = FileUtils.calculateFileDepth(file);
		List<String> splittedFilenames = FilenameUtils.split(file
				.getAbsolutePath());

		StringBuilder select = new StringBuilder("select T").append(depth)
				.append(".*");
		select.append(" FROM ");
		for (int i = 0; i <= depth; i++) {
			select.append(getTableName(i)).append(" ").append("T").append(i)
					.append(",");
		}
		//select.deleteCharAt(select.length() -1);
		select.replace(select.lastIndexOf(","), select.length(), "");
		select.append(" where");

		List<Object> params = Lists.newArrayList();
		for (int i = 1; i <= depth; i++) {
			select.append(" T").append(i - 1).append(".NAME=?")
					.append(" and").append(" T")
					.append(i - 1).append(".ID").append("=").append("T")
					.append(i).append(".POID and");
			
			params.add(splittedFilenames.get(i - 1));
			
		}
		select.append(" T").append(depth).append(".NAME=?");
		params.add(FileUtils.getNonEmptyName(file));
		
		QueryRunner qr = new QueryRunner();
		BeanListHandler<FSObject> blh = new BeanListHandler(
				FSObject.class);
		Connection conn = null;
		try {
			conn = getConnection();
			List<FSObject> fsObjInfos = qr.query(conn,
					select.toString(), blh, params.toArray());
			if (fsObjInfos.size() > 0) {
				equal = fsObjInfos.get(0);
			}
			if (fsObjInfos.size() > 1) {
				LOGGER.warn("it is not expected that one file has two records in database.");
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally{
			DbUtils.closeQuietly(conn);
		}
		return equal;

	}

	/**
	 * TODO:
	 * 
	 * @param wrapper
	 * @return
	 */
	public static String findParentPath(FSObject obj) {

		Object[] splittedNames = null;
		int depth = obj.getDepth();

		StringBuilder select = new StringBuilder("select ");
		for (int i = 0; i <= depth; i++) {
			select.append("T").append(i).append(".NAME,");
		}
		select.replace(select.lastIndexOf(","), select.length(), "");
		select.append(" FROM ");
		for (int i = 0; i <= depth; i++) {
			select.append(getTableName(i)).append(" ").append("T").append(i)
					.append(",");
		}
		select.replace(select.lastIndexOf(","), select.length(), "");
		select.append(" where");
		for (int i = 1; i <= depth; i++) {
			select.append(" T").append(i - 1).append(".ID").append("=")
					.append("T").append(i).append(".POID and");
		}
		select.append(" T").append(depth).append(".POID=? and");
		select.append(" T").append(depth).append(".NAME=?");
		

		QueryRunner qr = new QueryRunner();
		ArrayListHandler blh = new ArrayListHandler();
		Connection conn = null;
		try {
			conn = getConnection();
			List<Object[]> fsObjInfos = qr.query(conn,
					select.toString(), blh, obj.getPoid(), obj.getName());
			if (fsObjInfos.size() > 0) {
				splittedNames = fsObjInfos.get(0);
			}
			if (fsObjInfos.size() > 1) {
				LOGGER.warn("it is not expected that one file has two records in database.");
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally{
			DbUtils.closeQuietly(conn);
		}
		
		StringBuilder absolutePath = new StringBuilder();
		if(splittedNames != null){
			
			for(int i= 0;i < splittedNames.length - 1;i++){
				Object name = splittedNames[i];
				absolutePath.append(name);
				final String fileName = name.toString();
				if(!StringUtils.equals(fileName, "/") && !FileUtils.isFileSystemRoot(new File(fileName))){
					absolutePath.append(File.separator);
				}
			}
		}
		return absolutePath.toString();

	}

	private static String getTableName(int suffix) {
		String tblName = "FS_OBJ_INFO" + suffix;
		return tblName;
	}

	public static void ensureTableExistence(int suffix) {
		for(int i = 0;i <= suffix;i++){
			
			if (!allCreatedTables.contains(getTableName(i))) {
				createTables(i);
			}
		}
	}

	public static List<String> listAllTableNames() {
		List<String> tableNames = Lists.newArrayList();
		Statement stmt = null;
		Connection conn = null;

		try {
			conn = getConnection();
			stmt = conn.createStatement();
			ResultSet resultSet = stmt
					.executeQuery("SELECT TABLE_NAME FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME LIKE 'FS_OBJ_INFO%' AND TABLE_TYPE='BASE TABLE';");
			while (resultSet.next()) {
				String tableName = resultSet.getString(1);
				Validate.notEmpty(tableName);
				tableNames.add(tableName);
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally{
			DbUtils.closeQuietly(conn);
		}
		return tableNames;

	}

	public static List<FSObject> selectEquals(Map<String, String> conditions) {

		List<String> allTableNames = listAllTableNames();
		List<FSObject> fsObjs = new ArrayList<>(0);
		String whereClause = "";

		if (conditions == null) {
			conditions = new HashMap<>();
		}
		long start = System.nanoTime();
		// http://www.hsqldb.org/doc/guide/ch09.html
		for (Entry<String, String> entry : conditions.entrySet()) {
			if (StringUtils.isNotBlank(entry.getValue())) {
				whereClause += " where " + entry.getKey() + " = '"
						+ entry.getValue() + "'";
			}
		}
		StringBuilder selectStmt = new StringBuilder();
		for(String tbl : allTableNames){
			selectStmt.append("select * from ").append(tbl).append(whereClause).append(" union all ");
		}
		selectStmt.replace(selectStmt.lastIndexOf(" union all "), selectStmt.length(), "");
		String selStr = selectStmt.toString();
		LOGGER.debug("construct selectEquals statement time:{}", System.nanoTime() - start);
		Statement stmt = null;
		Connection conn = null;

		try {
			conn = getConnection();
			// QueryRunner queryRunner = new QueryRunner();
			stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery(selStr);
			conn.commit();
			BeanListHandler<FSObject> blh = new BeanListHandler(
					FSObject.class);
			fsObjs = blh.handle(rs);

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			IOUtils.closeQuietly(stmt);
			IOUtils.closeQuietly(conn);

		}

		return fsObjs;
	}
	
	public static long countLike(Map<String, String> conditions) {
		List<String> allTableNames = listAllTableNames();
		List<Object[]> fsObjs = null;
		String whereClause = "";

		if (conditions == null) {
			conditions = new HashMap<>();
		}
		long start = System.nanoTime();
		// http://www.hsqldb.org/doc/guide/ch09.html
		for (Entry<String, String> entry : conditions.entrySet()) {
			final String value = entry.getValue();
			if (StringUtils.isNotBlank(value)) {
				String sb = replaceAsteriskWithPercent(value);
				whereClause += " where " + entry.getKey() + " like '"
						+ sb + "'";
			}
		}
		StringBuilder selectStmt = new StringBuilder();
		for(String tbl : allTableNames){
			selectStmt.append("select count(id) from ").append(tbl).append(whereClause).append(" union all ");
		}
		
		selectStmt.replace(selectStmt.lastIndexOf(" union all "), selectStmt.length(), "");
		String selStr = selectStmt.toString();
		LOGGER.debug("construct countLike statement time:{}", System.nanoTime() - start);
		Statement stmt = null;
		Connection conn = null;

		try {
			conn = getConnection();
			// QueryRunner queryRunner = new QueryRunner();
			stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery(selStr);
			conn.commit();
			ArrayListHandler blh = new ArrayListHandler();
			fsObjs = blh.handle(rs);

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			IOUtils.closeQuietly(stmt);
			IOUtils.closeQuietly(conn);

		}

		long total = 0;
		if(fsObjs!=null){
			
			for(Object o :fsObjs){
				if(o.getClass().isArray()){
					total +=Long.valueOf(((Object[])o)[0].toString());
				}
				
			}
		}
		return total;
	}
	public static boolean match(String name, String criterion){
		
		if(StringUtils.isEmpty(criterion)){
			return true;
		}
		
		boolean match = false;
		String sql = "select count(1) from dual where ? like ?";


		QueryRunner qr = new QueryRunner();
		ArrayHandler blh = new ArrayHandler();
		Connection conn = null;
		try {
			conn = getConnection();
			Object[] fsObjInfos = qr.query(conn,
					sql, blh, name, replaceAsteriskWithPercent(criterion));
			if (fsObjInfos.length > 0) {
				match = true;
			}
			if (fsObjInfos.length > 1) {
				LOGGER.warn("it is not expected that one file has two records in database.");
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally{
			DbUtils.closeQuietly(conn);
		}
		return match;
	}
	public static List<FSObject> selectLike(Map<String, String> conditions) {
		List<String> allTableNames = listAllTableNames();
		List<FSObject> fsObjs = new ArrayList<>(0);
		String whereClause = "";

		if (conditions == null) {
			conditions = new HashMap<>();
		}
		long start = System.nanoTime();
		// http://www.hsqldb.org/doc/guide/ch09.html
		for (Entry<String, String> entry : conditions.entrySet()) {
			String value = entry.getValue();
			if (StringUtils.isNotBlank(value)) {
				String sb = replaceAsteriskWithPercent(value);
				whereClause += " where " + entry.getKey() + " like '"
						+ sb + "'";
			}
		}
		StringBuilder selectStmt = new StringBuilder();
		for(String tbl : allTableNames){
			selectStmt.append("select * from ").append(tbl).append(whereClause).append(" union all ");
		}
		selectStmt.replace(selectStmt.lastIndexOf(" union all "), selectStmt.length(), "");
		String selStr = selectStmt.toString();
		LOGGER.debug("construct selectLike statement time:{}", System.nanoTime() - start);
		Statement stmt = null;
		Connection conn = null;

		try {
			conn = getConnection();
			// QueryRunner queryRunner = new QueryRunner();
			stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery(selStr);
			conn.commit();
			BeanListHandler<FSObject> blh = new BeanListHandler(
					FSObject.class);
			fsObjs = blh.handle(rs);

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			IOUtils.closeQuietly(stmt);
			IOUtils.closeQuietly(conn);

		}

		return fsObjs;
	}

	private static String replaceAsteriskWithPercent(String value) {
		String[] parts = StringUtils.split(value, "*");
		
		StringBuilder sb = new StringBuilder();

		sb.append("%");
		for(int i = 0;i <parts.length;i++){
			if(!StringUtils.isEmpty(parts[i])){
				sb.append(parts[i]).append("%");
			}
		}
		sb.append("%");
		return sb.toString();
	}
	public static List<FSObject> selectLike(Map<String, String> conditions, Range<Integer> rownum) {
		List<String> allTableNames = listAllTableNames();
		List<FSObject> fsObjs = new ArrayList<>(0);
		String whereClause = "";

		if (conditions == null) {
			conditions = new HashMap<>();
		}

		// http://www.hsqldb.org/doc/guide/ch09.html
		for (Entry<String, String> entry : conditions.entrySet()) {
			final String value = entry.getValue();
			if (StringUtils.isNotBlank(value)) {
				String sb = replaceAsteriskWithPercent(value);
				whereClause += " where " + entry.getKey() + " like '"
						+ sb + "'";
			}
		}

		StringBuilder selectStmt = new StringBuilder();
		for(String tbl : allTableNames){
			selectStmt.append("select * from ").append(tbl).append(whereClause).append(" union all ");
		}
		selectStmt.replace(selectStmt.lastIndexOf(" union all "), selectStmt.length(), "");
		if(rownum != null){
			
			// select limit startindex rowcount * from 可以正常工作，且rowcount不能大于50
			// rownnum() >= x and rownum()<=y的形式工作不正常
			/*LIMIT n m: creates the result set for the SELECT statement first 
			 * and then discards the first n rows and returns the first m rows of the remaining result set. 
			 * Special cases: LIMIT 0 m is equivalent to TOP m or FIRST m in other RDBMS's; 
			 * LIMIT n 0 discards the first n rows and returns the rest of the result set.
			 * TOP m is equivalent to LIMIT 0 m
			 * n starts with 1
			 * rownum() starts with 1,  If you enable Oracle syntax compatibility mode, you can also use ROWNUM
			 * This statement enables it:
			 * SET DATABASE SQL SYNTAX ORA TRUE
			 * Or use the connection property sql.syntax_ora=true
			 * */
			selectStmt = new StringBuilder("select limit ").append(rownum.getMinimum()).append(" ").append(rownum.getMaximum() - rownum.getMinimum() + 1).append(" rownum(),id, name, depth, size, type, lmts,poid from (").append(selectStmt.toString()).append(")");

			
		}
		LOGGER.debug(selectStmt.toString());
		Statement stmt = null;
		Connection conn = null;

		try {
			conn = getConnection();
			// QueryRunner queryRunner = new QueryRunner();
			stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery(selectStmt.toString());
			conn.commit();
			BeanListHandler<FSObject> blh = new BeanListHandler(
					FSObject.class);
			fsObjs = blh.handle(rs);

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			IOUtils.closeQuietly(stmt);
			IOUtils.closeQuietly(conn);

		}

		return fsObjs;
	}
}
