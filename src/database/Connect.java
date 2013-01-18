package database;

import java.sql.*;

import server.Interface;

public class Connect {
	private Statement statement;
	private Connection conn;
	public int totalNum;
	public String[] name, id, idNum, faculty, pic;

	public Connect() throws Exception {
		String url = "jdbc:mysql://" + Configure.databaseAddress + "/"
				+ Configure.database + "?characterEncoding=utf8"
				+ "&jdbcCompliantTruncation=false"
				+ "&zeroDateTimeBehavior=convertToNull";
		Class.forName("com.mysql.jdbc.Driver");
		conn = DriverManager.getConnection(url, Configure.user,
				Configure.password);
		statement = conn.createStatement();
	}

	public void close() {
		try {
			statement.close();
			conn.close();
		} catch (Exception e) {
		}
	}

	ResultSet getBasic(String query) throws Exception {
		ResultSet rs;
		statement.setFetchSize(1001);
		if ((query == null) || query.equals(""))
			rs = statement
					.executeQuery("select name,id,idnum,faculty,pic from "
							+ Configure.table);
		else
			rs = statement
					.executeQuery("select name,id,idnum,faculty,pic from "
							+ Configure.table + " where " + query);
		return rs;
	}

	ResultSet getOne(String id) throws Exception {
		return statement.executeQuery("select * from " + Configure.table
				+ " where id='" + id + "'");
	}

	public int getCount(String query) throws Exception {
		ResultSet rs;
		if ((query == null) || query.equals(""))
			rs = statement.executeQuery("select count(*) from "
					+ Configure.table);
		else
			rs = statement.executeQuery("select count(*) from "
					+ Configure.table + " where " + query);
		rs.next();
		int countNum = rs.getInt(1);
		rs.close();
		return countNum;
	}

	public Count[] getStatistic(String col, String query) throws Exception {
		String sql;
		if (col.equals("age"))
			sql = "select count(*),(YEAR(CURDATE())-YEAR(birthday))-"
					+ "(RIGHT(CURDATE(),5)<RIGHT(birthday,5)) as age from "
					+ Configure.table;
		else
			sql = "select count(*) as c," + col + " from " + Configure.table;
		if ((query != null) && !query.equals(""))
			sql += " where " + query;
		if (col.equals("age"))
			sql += " group by age order by age";
		else
			sql += " group by " + col + " order by c desc";
		ResultSet rs = statement.executeQuery(sql);
		int count = 0;
		while (rs.next())
			count++;
		rs.first();
		Count[] data = new Count[count];
		for (int i = 0; i < count; i++) {
			data[i] = new Count(rs.getString(2), rs.getInt(1));
			rs.next();
		}
		rs.close();
		return data;
	}

	public void delete(String id) throws Exception {
		statement.executeUpdate("delete from " + Configure.table
				+ " where id='" + id + "'");
	}

	public void deletePic(Interface webServer, String id) throws Exception {
		ResultSet rs = statement.executeQuery("select pic from "
				+ Configure.table + " where id='" + id + "'");
		rs.next();
		String picAddress = rs.getString(1);
		if ((picAddress != null) && (picAddress.length() == 32))
			webServer.deletePic(picAddress);
	}

	public String[] getEnumList(int x, int y) {
		String[] list;
		try {
			ResultSet rs = statement.executeQuery("show columns from "
					+ Configure.table + " like '" + List.COLUMN_NAME[x][y]
					+ "'");
			rs.next();
			String enums = rs.getString("Type");
			int position = 0, count = 0;
			while ((position = enums.indexOf('\'', position)) > 0) {
				position = enums.indexOf('\'', position + 1) + 1;
				count++;
			}
			position = 0;
			list = new String[count + 1];
			list[0] = "";
			for (int i = 1; i <= count; i++) {
				position = enums.indexOf('\'', position);
				int secondPosition = enums.indexOf('\'', position + 1);
				list[i] = enums.substring(position + 1, secondPosition);
				position = secondPosition + 1;
			}
		} catch (Exception e) {
			list = new String[0];
		}
		return list;
	}

	public void getData(String query) throws Exception {
		totalNum = getCount(query);
		ResultSet rs = getBasic(query);
		int num = (totalNum > 1000) ? 1000 : totalNum;
		name = new String[num];
		id = new String[num];
		idNum = new String[num];
		faculty = new String[num];
		pic = new String[num];
		for (int i = 0; i < num; i++) {
			rs.next();
			name[i] = rs.getString("name");
			id[i] = rs.getString("id");
			idNum[i] = rs.getString("idnum");
			faculty[i] = rs.getString("faculty");
			pic[i] = rs.getString("pic");
		}
		rs.close();
	}

	public String[] getID(String query) throws Exception {
		String[] id = new String[totalNum];
		ResultSet rs = getBasic(query);
		for (int i = 0; i < totalNum; i++) {
			rs.next();
			id[i] = rs.getString("id");
		}
		rs.close();
		return id;
	}

	public boolean exist(String id) throws Exception {
		ResultSet rs = getOne(id);
		boolean flag = false;
		if (rs.next())
			flag = true;
		return flag;
	}

	public void insert(String data) throws Exception {
		statement.executeUpdate("insert into " + Configure.table + " ("
				+ List.COLUMN_STRING + ") values (" + data + ")");
	}

	public void update(String id, String col, String data) throws Exception {
		statement.executeUpdate("update " + Configure.table + " set " + col
				+ "=" + data + " where id='" + id + "'");
	}

	public void merge(String id, String data) throws Exception {
		if (exist("temp"))
			delete("temp");
		insert("'temp" + data.substring(data.indexOf('\'', 1)));
		Detail info = new Detail(this, "temp");
		String[][] newData = new String[7][7];
		for (int x = 0; x < 7; x++)
			for (int y = 0; y < 7; y++)
				newData[x][y] = info.get(List.COLUMN_NAME[x][y]);
		info.close();
		for (int x = 0; x < 7; x++)
			for (int y = 0; y < 7; y++)
				if ((newData[x][y] != null) && (!newData[x][y].equals(""))) {
					if (List.COLUMN_TYPE[x][y] != 2)
						newData[x][y] = "'" + newData[x][y] + "'";
					update(id, List.COLUMN_NAME[x][y], newData[x][y]);
				}
		delete("temp");
	}
}
