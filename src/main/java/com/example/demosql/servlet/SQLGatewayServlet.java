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

    private static final String DB_URL  = "jdbc:mysql://localhost:3306/testdb?useSSL=false&serverTimezone=UTC";
    private static final String DB_USER = "root";
    private static final String DB_PASS = "290505An@@";

    @Override
    protected void doPost(HttpServletRequest request,
                          HttpServletResponse response)
            throws ServletException, IOException {

        String sqlStatement = request.getParameter("sqlStatement");
        String sqlResult = "";

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);

            sqlStatement = sqlStatement.trim();
            if (sqlStatement.length() >= 6) {
                String sqlType = sqlStatement.substring(0, 6);
                if (sqlType.equalsIgnoreCase("select")) {
                    PreparedStatement ps = connection.prepareStatement(sqlStatement);
                    ResultSet resultSet = ps.executeQuery();
                    sqlResult = SQLUtil.getHtmlTable(resultSet);
                    resultSet.close();
                    ps.close();
                } else {
                    PreparedStatement ps = connection.prepareStatement(sqlStatement);
                    int i = ps.executeUpdate();
                    sqlResult = "<p>Statement executed successfully.<br>" + i + " row(s) affected.</p>";
                    ps.close();
                }
            }
            connection.close();
        } catch (ClassNotFoundException e) {
            sqlResult = "<p>Error loading the database driver:<br>" + e.getMessage() + "</p>";
        } catch (SQLException e) {
            sqlResult = "<p>Error executing the SQL statement:<br>" + e.getMessage() + "</p>";
            e.printStackTrace();
        }

        HttpSession session = request.getSession();
        session.setAttribute("sqlResult", sqlResult);
        session.setAttribute("sqlStatement", sqlStatement);

        String url = "/index.jsp";
        getServletContext().getRequestDispatcher(url).forward(request, response);
    }
}
