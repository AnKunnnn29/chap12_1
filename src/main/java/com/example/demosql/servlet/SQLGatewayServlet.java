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

    // 🔹 Đọc thông tin kết nối từ Environment Variables trên Render
    private static final String DB_URL  = System.getenv("DB_URL");
    private static final String DB_USER = System.getenv("DB_USER");
    private static final String DB_PASS = System.getenv("DB_PASS");

    @Override
    protected void doPost(HttpServletRequest request,
                          HttpServletResponse response)
            throws ServletException, IOException {

        String sqlStatement = request.getParameter("sqlStatement");
        String sqlResult = "";

        try {
            // ✅ Tải driver MySQL
            Class.forName("com.mysql.cj.jdbc.Driver");

            // ✅ Kết nối tới MySQL Cloud (Render Database hoặc Aiven)
            Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);

            sqlStatement = sqlStatement.trim();
            if (sqlStatement.length() >= 6) {
                String sqlType = sqlStatement.substring(0, 6);

                // ✅ SELECT → hiển thị bảng kết quả
                if (sqlType.equalsIgnoreCase("select")) {
                    PreparedStatement ps = connection.prepareStatement(sqlStatement);
                    ResultSet resultSet = ps.executeQuery();
                    sqlResult = SQLUtil.getHtmlTable(resultSet);
                    resultSet.close();
                    ps.close();
                } else {
                    // ✅ INSERT / UPDATE / DELETE / CREATE / DROP
                    PreparedStatement ps = connection.prepareStatement(sqlStatement);
                    int rows = ps.executeUpdate();
                    sqlResult = "<p>Statement executed successfully.<br>" 
                              + rows + " row(s) affected.</p>";
                    ps.close();
                }
            }

            connection.close();

        } catch (ClassNotFoundException e) {
            sqlResult = "<p>Error loading the database driver:<br>" + e.getMessage() + "</p>";
            e.printStackTrace();

        } catch (SQLException e) {
            sqlResult = "<p>Error executing the SQL statement:<br>" + e.getMessage() + "</p>";
            e.printStackTrace();

            // ✅ In log ra để kiểm tra trong Render Logs
            System.err.println("❌ SQL Error: " + e.getMessage());
            System.err.println("🔗 DB_URL: " + DB_URL);
            System.err.println("👤 DB_USER: " + DB_USER);
        }

        // ✅ Gửi kết quả về JSP
        HttpSession session = request.getSession();
        session.setAttribute("sqlResult", sqlResult);
        session.setAttribute("sqlStatement", sqlStatement);

        String url = "/index.jsp";
        getServletContext().getRequestDispatcher(url).forward(request, response);
    }
}
