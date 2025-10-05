package com.example.demosql.servlet;

import com.example.demosql.SQL.SQLUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.sql.*;

@WebServlet("/SQLGatewayServlet")
public class SQLGatewayServlet extends HttpServlet {

    private static final String DB_URL  = "jdbc:mysql://mysql-15cb0e9b-tnumber696-ebdf.e.aivencloud.com:16607/defaultdb?useSSL=true&requireSSL=true&serverTimezone=UTC";
    private static final String DB_USER = "avnadmin";
    private static final String DB_PASS = "AVNS_Fwodpkd9L5BNvjfqHMH";

    @Override
    protected void doPost(HttpServletRequest request,
                          HttpServletResponse response)
            throws ServletException, IOException {

        String sqlStatement = request.getParameter("sqlStatement");
        String sqlResult = "";

        try {
            // Load MySQL driver
            Class.forName("com.mysql.cj.jdbc.Driver");

            // Connect to Aiven MySQL
            Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);

            sqlStatement = sqlStatement.trim();
            if (sqlStatement.toLowerCase().startsWith("select")) {
                PreparedStatement ps = connection.prepareStatement(sqlStatement);
                ResultSet rs = ps.executeQuery();
                sqlResult = SQLUtil.getHtmlTable(rs);
                rs.close();
                ps.close();
            } else {
                PreparedStatement ps = connection.prepareStatement(sqlStatement);
                int rows = ps.executeUpdate();
                sqlResult = "<p>Statement executed successfully.<br>" + rows + " row(s) affected.</p>";
                ps.close();
            }
            connection.close();

        } catch (ClassNotFoundException e) {
            sqlResult = "<p>Error loading MySQL driver:<br>" + e.getMessage() + "</p>";
        } catch (SQLException e) {
            sqlResult = "<p>Error executing SQL statement:<br>" + e.getMessage() + "</p>";
        }

        HttpSession session = request.getSession();
        session.setAttribute("sqlResult", sqlResult);
        session.setAttribute("sqlStatement", sqlStatement);

        getServletContext().getRequestDispatcher("/index.jsp").forward(request, response);
    }
}
