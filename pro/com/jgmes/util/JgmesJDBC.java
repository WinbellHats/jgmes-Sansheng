package com.jgmes.util;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

import com.je.core.util.JdbcUtil;
import com.je.core.util.bean.DynaBean;


import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;

import com.je.core.util.ExecCommand;
import com.je.core.util.JdbcUtil;

public class JgmesJDBC {
	public int doupdateList(String sql,List<String[]> list) throws SQLException {
		JdbcUtil jdbcUtil = new JdbcUtil("com.mysql.jdbc.Driver", "jdbc:mysql://127.0.0.1:3306/jeplus1?useOldAliasMetadataBehavior=true", "root","ront");
		Connection connection = jdbcUtil.getConnection();
		PreparedStatement prepareStatement = connection.prepareStatement(sql);
		for (int i = 0; i < list.size(); i++) {
			for (int j = 0; j < list.get(i).length; j++) {
				prepareStatement.setString(j+1, list.get(i)[j]);
			}
			prepareStatement.addBatch();
		}
		int[]  counts = prepareStatement.executeBatch();
		jdbcUtil.close();
		return 0;
	}

}
