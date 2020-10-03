import java.sql.*;

class Main {
    public static void main(String[] args) throws Exception {
        Class.forName("org.h2.Driver");
        Connection con = DriverManager.getConnection("jdbc:h2:~/test", "sa", "");
        try (Statement stmt = con.createStatement()) {
            stmt.execute("create table if not exists COFFEE(COF_NAME VARCHAR(25))");
            stmt.execute("insert into COFFEE(COF_NAME)values('cappuccino')");
            ResultSet rs = stmt.executeQuery("select COF_NAME from COFFEE");
            while (rs.next()) {
                String coffeeName = rs.getString("COF_NAME");
                System.out.println(coffeeName);
            }
            stmt.execute("drop table COFFEE");
        } catch (SQLException e) {
            e.printStackTrace();
        }
        con.close();
    }
}