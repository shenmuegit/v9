package com;

import com.mchange.v2.c3p0.ComboPooledDataSource;

import java.beans.PropertyVetoException;
import java.sql.*;

public class JDBCUtils {

    private static final String USER = "";
    private static final String PASSWD = "";
    private static final String URL = "jdbc:mysql://?useUnicode=true&characterEncoding=utf8&serverTimezone=GMT%2B8&useSSL=false";
    private static final ComboPooledDataSource cpds = new ComboPooledDataSource();

    static {
        try {
            cpds.setDriverClass( "com.mysql.cj.jdbc.Driver" );
            cpds.setJdbcUrl(URL);
            cpds.setUser(USER);
            cpds.setPassword(PASSWD);
            cpds.setMinPoolSize(1);
            cpds.setMaxPoolSize(1);
            cpds.setAcquireIncrement(1);
        } catch (PropertyVetoException e) {
            e.printStackTrace();
        }
    }

    public static void insert(String title,String url,String img){
        Connection conn = null;
        try {
            conn = cpds.getConnection();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        PreparedStatement ps = null;
        ResultSet res = null;
        try {
            String unique = "select id from v9 where title = ?";
            ps = conn.prepareStatement(unique);
            ps.setString(1,title);
            res = ps.executeQuery();
            System.out.println("title:" + title + ",url=" + url + ",img=" + img);
            if(res.next()){
                System.out.println("重复");
                return;
            }
            //编写SQL语句
            String sql = "insert into v9(title,url,img) values(?,?,?)";
            ps = conn.prepareStatement(sql);
            ps.setString(1, title);
            ps.setString(2, url);
            ps.setString(3, img);
            ps.executeUpdate();
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            try {
                conn.rollback();
            } catch (Exception e1) {
                e1.printStackTrace();
            }
        }finally {
            try {
                res.close();
                ps.close();
                conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

}
