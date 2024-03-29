import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebFilter(urlPatterns = "/*")
public class CorsFilter extends HttpFilter {
    @Override
    protected void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws IOException, ServletException {
        response.setHeader("Access-Control-Allow-Origin","http://localhost:4300");
        response.setHeader("Access-Control-Allow-Methods","OPTIONS, GET,POST");
        response.setHeader("Access-Control-Allow-Headers","Content-Type");

//        response.setHeader("Content-Type", "application/json");
//        response.setHeader("Accept", "application/json");
        super.doFilter(request, response, chain);
    }
}

