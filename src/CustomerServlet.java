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
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

@WebServlet(urlPatterns = "/customers")
public class CustomerServlet extends HttpServlet {

    @Resource(name ="java:comp/env/jdbc/pool")
    private DataSource ds;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try (PrintWriter out = resp.getWriter()) {

            if (req.getParameter("id") != null) {

                String id = req.getParameter("id");

                try {
                    Connection connection = ds.getConnection();
                    Statement stm = connection.createStatement();
                    PreparedStatement pstm = connection.prepareStatement("SELECT * FROM Customer WHERE id=?");
                    pstm.setObject(1, id);
                    ResultSet rst = pstm.executeQuery();

                    if (rst.next()) {
                        JsonObjectBuilder ob = Json.createObjectBuilder();
                        ob.add("id", rst.getString(1));
                        ob.add("name", rst.getString(2));
                        ob.add("address", rst.getString(3));
                        resp.setContentType("application/json");
                        out.println(ob.build());
                    } else {
                        resp.sendError(HttpServletResponse.SC_NOT_FOUND);
                    }

                } catch (Exception ex) {
                    ex.printStackTrace();
                }

            } else {
                try {

                    Connection connection = ds.getConnection();
                    Statement stm = connection.createStatement();
                    ResultSet rst = stm.executeQuery("SELECT * FROM Customer");

                    JsonArrayBuilder ab = Json.createArrayBuilder();
                    while (rst.next()) {
                        JsonObjectBuilder ob = Json.createObjectBuilder();
                        ob.add("id", rst.getString("id"));
                        ob.add("name", rst.getString("name"));
                        ob.add("address", rst.getString("address"));
                        ab.add(ob.build());
                    }
                    JsonArray customers = ab.build();
                    resp.setContentType("application/json");
                    resp.getWriter().println(customers);

                    connection.close();
                } catch (Exception e) {
                    resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                    e.printStackTrace();
                }
            }
        }
    }


    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {

            JsonReader reader = Json.createReader(req.getReader());

            JsonObject customer = reader.readObject();

            String id = customer.getString("id");
            String name = customer.getString("name");
            String address = customer.getString("address");
            System.out.println(id);
            System.out.println(name);
//            Class.forName("com.mysql.jdbc.Driver");
            Connection connection = ds.getConnection();
            PreparedStatement pstm = connection.prepareStatement("INSERT INTO Customer VALUES (?,?,?)");
            pstm.setObject(1,id);
            pstm.setObject(2,name);
            pstm.setObject(3,address);
            int affectedRows = pstm.executeUpdate();

            if (affectedRows > 0){
                resp.setStatus(HttpServletResponse.SC_CREATED);
            }else{
                resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            }

        }catch (JsonParsingException | NullPointerException  ex){
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
        }catch (Exception ex){
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String id = req.getParameter("id");

        if (id != null){

            try {
//                Class.forName("com.mysql.jdbc.Driver");
                Connection connection = ds.getConnection();
                PreparedStatement pstm = connection.prepareStatement("DELETE FROM Customer WHERE id=?");
                System.out.println(id);
                pstm.setObject(1, id);
                int affectedRows = pstm.executeUpdate();
                if (affectedRows >  0){
                    resp.setStatus(HttpServletResponse.SC_NO_CONTENT);
                }else{
                    resp.sendError(HttpServletResponse.SC_NOT_FOUND);
                }
            }catch (Exception ex){
                resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                ex.printStackTrace();
            }

        }else{
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
        }
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (req.getParameter("id") != null){

            try {
                JsonReader reader = Json.createReader(req.getReader());
                JsonObject customer = reader.readObject();

                String id = customer.getString("id");
                String name = customer.getString("name");
                String address = customer.getString("address");

                if (!id.equals(req.getParameter("id"))){
                    resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
                    return;
                }
//
//                Class.forName("com.mysql.jdbc.Driver");
                Connection connection = ds.getConnection();
                PreparedStatement pstm = connection.prepareStatement("UPDATE Customer SET name=?, address=? WHERE id=?");
                pstm.setObject(3,id);
                pstm.setObject(1,name);
                pstm.setObject(2,address);
                int affectedRows = pstm.executeUpdate();

                if (affectedRows > 0){
                    resp.setStatus(HttpServletResponse.SC_NO_CONTENT);
                }else{
                    resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                }

            }catch (JsonParsingException | NullPointerException  ex){
                resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
            }catch (Exception ex){
                resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            }


        }else{
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
        }
    }
}
