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


@WebServlet(urlPatterns = "/items")
public class ItemServlet extends HttpServlet {

    @Resource(name = "java:comp/env/jdbc/pool")
    private DataSource ds;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try (PrintWriter out = resp.getWriter()) {

            if (req.getParameter("code") != null) {

                String code = req.getParameter("code");

                try {
//                    Class.forName("com.mysql.jdbc.Driver");
                    Connection connection = ds.getConnection();
                    PreparedStatement pstm = connection.prepareStatement("SELECT * FROM Item WHERE code=?");
                    pstm.setObject(1, code);
                    ResultSet rst = pstm.executeQuery();

                    if (rst.next()) {
                        JsonObjectBuilder ob = Json.createObjectBuilder();
                        ob.add("code", rst.getString(1));
                        ob.add("description", rst.getString(2));
                        ob.add("qtyOnHand", rst.getString(3));
                        ob.add("unitPrice", rst.getString(4));
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
                    ResultSet rst = stm.executeQuery("SELECT * FROM Item");

                    JsonArrayBuilder ab = Json.createArrayBuilder();
                    while (rst.next()) {
                        JsonObjectBuilder ob = Json.createObjectBuilder();
                        ob.add("code", rst.getString("code"));
                        ob.add("description", rst.getString("description"));
                        ob.add("qtyOnHand", rst.getString("qtyOnHand"));
                        ob.add("unitPrice", rst.getString("unitPrice"));
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

            JsonObject item = reader.readObject();

            String code = item.getString("code");
            String description = item.getString("description");
            String qtyOnHand = item.getString("qtyOnHand");
            String unitPrice=item.getString("unitPrice");


//            Class.forName("com.mysql.jdbc.Driver");
            Connection connection = ds.getConnection();
            PreparedStatement pstm = connection.prepareStatement("INSERT INTO Item VALUES (?,?,?,?)");
            pstm.setObject(1,code);
            pstm.setObject(2,description);
            pstm.setObject(3,qtyOnHand);
            pstm.setObject(4,unitPrice);
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
        String code = req.getParameter("code");

        if (code!= null){

            try {
//                Class.forName("com.mysql.jdbc.Driver");
                Connection connection = ds.getConnection();
                PreparedStatement pstm = connection.prepareStatement("DELETE FROM Item WHERE code=?");
                pstm.setObject(1, code);
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
        if (req.getParameter("code") != null){

            try {
                JsonReader reader = Json.createReader(req.getReader());
                JsonObject item = reader.readObject();

                String code = item.getString("code");
                String description = item.getString("description");
                String qtyOnHand = item.getString("qtyOnHand");
                String unitPrice=item.getString("unitPrice");

                if (!code.equals(req.getParameter("code"))){
                    resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
                    return;
                }

//                Class.forName("com.mysql.jdbc.Driver");
                Connection connection = ds.getConnection();
                PreparedStatement pstm = connection.prepareStatement("UPDATE Item SET description=?, qtyOnHand=?,unitPrice=? WHERE code=?");
                pstm.setObject(4,code);
                pstm.setObject(1,description);
                pstm.setObject(2,qtyOnHand);
                pstm.setObject(3,unitPrice);
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
