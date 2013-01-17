package database;

import java.sql.ResultSet;

public class Detail {
	ResultSet rs;

	public Detail(Connect database, String id) throws Exception {
		rs = database.getOne(id);
		rs.next();
	}

	public String get(String name) throws Exception {
		return rs.getString(name);
	}

	public void close() {
		try {
			rs.close();
		} catch (Exception e) {
		}
	}
}
