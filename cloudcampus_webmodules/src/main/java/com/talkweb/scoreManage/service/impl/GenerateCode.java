package com.talkweb.scoreManage.service.impl;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author talkweb 根据表名生成批量插入语句
 */
public class GenerateCode {

	public static void main(String[] args) {
		try {
			// 加载MySql的驱动类
			Class.forName("com.mysql.jdbc.Driver");
		} catch (ClassNotFoundException e) {
			System.out.println("找不到驱动程序类 ，加载驱动失败！");
			e.printStackTrace();
		}

		// 连接MySql数据库，用户名和密码都是root
		String url = "jdbc:mysql://192.168.140.126:3306/cc_schedule";
		String username = "user";
		String password = "user";
		Connection con = null;
		try {
			con = DriverManager.getConnection(url, username, password);
		} catch (SQLException se) {
			System.out.println("数据库连接失败！");
			se.printStackTrace();
		}
		// String[] tbls = " T_GM_ScoreStuStatisticsRank,
		// T_GM_ScoreStuStatisticsRank_mk, T_GM_ScoreClassStatistics,
		// T_GM_ScoreClassStatistics_Mk, t_gm_scoreclassdistribute,
		// T_GM_ScoreClassDistribute_mk, T_GM_ScoreRankStatistics,
		// T_GM_ScoreRankStatistics_mk, T_GM_ScoreGroupStatistics_mk,
		// T_GM_ScoreGroupStatistics, T_GM_ScoreStuBZF, T_GM_ScoreStuBZF_mk,
		// T_GM_ScoreStuJZSQNS, T_GM_ScoreStuJZSQNS_mk,
		// T_GM_ScoreClassStatistics_range, T_GM_ScoreClassStatistics_range,
		// T_GM_ScoreGroupStatistics_mk_range, T_GM_ScoreGroupStatistics_range,
		// T_GM_ClassScoreLevelMK, T_GM_GroupScoreLevelMK,
		// T_GM_ClassScoreLevelSequnce".split(",");
		String[] tbls = "t_sch_dezy_classGroup,t_sch_dezy_tclassfrom,t_sch_tclass_tclassGoClassTime".split(",");

		for (int i = 0; i < tbls.length; i++) {

			String sql = genSql(tbls[i].trim(), con);

			System.out.println(sql);
		}
	}

	public static String genSql(String tblName, Connection con) {

		List<String> keys = new ArrayList<String>();
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		try {
			pstmt = con.prepareStatement("SELECT * FROM  " + tblName);
			rs = pstmt.executeQuery("SELECT * FROM  " + tblName);
			ResultSetMetaData data = rs.getMetaData();
			for (int i = 1; i <= data.getColumnCount(); i++) {
				// 获得指定列的列名
				String columnName = data.getColumnName(i);
				keys.add(columnName);
				// System.out.println("键名："+columnName);
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			if (rs != null) { // 关闭记录集
				try {
					rs.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
			if (pstmt != null) { // 关闭声明
				try {
					pstmt.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}
		String enName = "";
		String[] sub = tblName.split("_");
		for (int i = 0; i < sub.length; i++) {
			String tp = sub[i];
			String t1 = tp.substring(0, 1);
			String t2 = tp.substring(1, tp.length());
			enName = enName + t1.toUpperCase() + t2.toLowerCase();

		}
		String sql = "";
		sql += "<update id='batchInsert" + enName + "List' parameterType='java.util.List' >"
				+ " insert into ${zhx_conn}." + tblName + "(";
		for (int i = 0; i < keys.size(); i++) {
			if (i != keys.size() - 1) {
				sql += keys.get(i) + ",";
			} else {
				sql += keys.get(i);
			}
		}
		sql += ") values " + " <foreach collection=\"list\" item=\"item\" index=\"index\" separator=\",\">(";
		for (int i = 0; i < keys.size(); i++) {
			if (i != keys.size() - 1) {
				sql += "#{item." + keys.get(i) + "},";
			} else {
				sql += "#{item." + keys.get(i) + "}";
			}
		}

		sql += ") </foreach>" + " on duplicate key update ";
		for (int i = 0; i < keys.size(); i++) {
			String kn = keys.get(i);
			if (i != keys.size() - 1) {
				sql += kn + "=values(" + kn + "),";
			} else {
				sql += kn + "=values(" + kn + ")";
			}
		}
		sql += "</update> ";

		return sql;
	}
}
