import javax.annotation.Resource;
import javax.json.*;
import javax.json.stream.JsonParsingException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

@WebServlet(urlPatterns = "/orders")
public class OrderServlet extends HttpServlet {
    @Resource(name = "java:comp/env/jdbc/pool")
    private DataSource ds;

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {

            JsonReader reader = Json.createReader(req.getReader());

            JsonObject order = reader.readObject();

            Integer oid = order.getInt("oid");
            String date = order.getString("date");
            String total = order.getString("total");
            String id = order.getString("id");
            System.out.println(oid);

//            Class.forName("com.mysql.jdbc.Driver");
            Connection connection = ds.getConnection();
            PreparedStatement pstm = connection.prepareStatement("INSERT INTO Orders VALUES (?,?,?,?)");

            pstm.setObject(1, oid);
            pstm.setObject(2, date);
            pstm.setObject(3, total);
            pstm.setObject(4, id);
            int affectedRows = pstm.executeUpdate();

            if (affectedRows > 0) {
                resp.setStatus(HttpServletResponse.SC_CREATED);
            } else {
                resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            }

        } catch (JsonParsingException | NullPointerException ex) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
        } catch (Exception ex) {
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {

            Connection connection = ds.getConnection();
            Statement stm = connection.createStatement();
            ResultSet rst = stm.executeQuery("SELECT  id FROM Customer");

            JsonArrayBuilder ab = Json.createArrayBuilder();
            while (rst.next()) {
                JsonObjectBuilder ob = Json.createObjectBuilder();
                ob.add("id", rst.getString("id"));
//                ob.add("name", rst.getString("name"));
//                ob.add("address", rst.getString("address"));
                ab.add(ob.build());
            }
            JsonArray customers = ab.build();
            resp.setContentType("application/json");
            resp.getWriter().println(customers);
            System.out.println(customers);


            connection.close();
        } catch (Exception e) {
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            e.printStackTrace();
        }
    }
}
