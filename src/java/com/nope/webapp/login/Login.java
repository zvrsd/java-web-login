package com.nope.webapp.login;

import com.nope.webapp.util.CookieUtil;
import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author zvr
 */
@WebServlet(urlPatterns = {"/Login"})
public class Login extends HttpServlet {

    private final String USERNAME = "admin";
    private final String PASSWORD = "root";
    
    private final String URL = "Login";
    private final String COOKIE_LOGIN_NAME = "USER_ID";
    private final String COOKIE_LOGIN_VALUE = "1aze54a98ze4a6ze8sd4qsf9sd5ze";
    private final int COOKIE_LOGIN_EXPIRATION = (10*60);
    
    private final String COOKIE_ATTEMPTS_COUNT = "LOGIN_COUNT";
    private final int LOGIN_MAX_ATTEMPTS = 3;
    
    private final String COOKIE_COOLDOWN = "LOGIN_COOLDOWN";
    private final int LOGIN_COOLDOWN = (10*60);
    
    private final String MSG_ATTEMPTS_LEFT = "%d attempts left";
    
    
    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code>
     * methods.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        response.setContentType("text/html;charset=UTF-8");
        try (PrintWriter out = response.getWriter()) {
            
            boolean isOnCooldown = false;
            boolean isLogged = false;
            boolean isNotSupposedToBeHere = false;
            
            CookieUtil cookieUtil = CookieUtil.getInstance();
            Cookie[] cookies = request.getCookies();
            String errorMessage = "";
            String lastUsername = "";
            
            // Cookie storage
            Cookie cookieLogin = cookieUtil.getCookieByName(cookies, COOKIE_LOGIN_NAME);
            Cookie cookieLoginAttempts = cookieUtil.getCookieByName(cookies, COOKIE_ATTEMPTS_COUNT);
            Cookie cookieCooldown = cookieUtil.getCookieByName(cookies, COOKIE_COOLDOWN);
            
            
            // If the user has used all his attempts and the cooldown is still up
            if( cookieCooldown != null && Integer.parseInt(cookieLoginAttempts.getValue()) == 0){
                isOnCooldown = true;
            }
            // If the user chooses to disconnect
            else if(request.getParameter("logout_button") != null){
                
                if(cookieLogin != null){
                    cookieLogin.setMaxAge(0);
                    response.addCookie(cookieLogin);
                    isLogged = false;
                }
            }
            // If the login is ok
            else if (( request.getParameter("login") != null 
                    && request.getParameter("login").equals(USERNAME)
                    && request.getParameter("password").equals(PASSWORD))
                    || cookieLogin != null) {
                // If the cookie doesnt exist already
                if (cookieLogin == null) {
                    cookieLogin = new Cookie(COOKIE_LOGIN_NAME, request.getParameter("login"));
                }
                // Updates the cookie expiration
                cookieLogin.setMaxAge(COOKIE_LOGIN_EXPIRATION);
                response.addCookie(cookieLogin);

                // Removes attempts cookie
                if (cookieLoginAttempts != null) {
                    cookieLoginAttempts.setMaxAge(0);
                    response.addCookie(cookieLoginAttempts);
                }
                // Removes cooldown cookie
                if (cookieCooldown != null) {
                    cookieCooldown.setMaxAge(0);
                    response.addCookie(cookieCooldown);
                }
                isLogged = true;
            }
            // If the credentials are invalid
            else if (request.getParameter("login") != null){
                
                // Creates a cookie if null
                if(cookieLoginAttempts == null){
                    // Creates the count cookie
                    cookieLoginAttempts = new Cookie(COOKIE_ATTEMPTS_COUNT, ""+(LOGIN_MAX_ATTEMPTS));
                    
                    // Creates / reset cookie cooldown
                    cookieCooldown = new Cookie(COOKIE_COOLDOWN, "");
                    cookieCooldown.setMaxAge(LOGIN_COOLDOWN);
                    response.addCookie(cookieCooldown);
                }
                // Decreases attempts
                int attempts = Integer.parseInt(cookieLoginAttempts.getValue());
                cookieLoginAttempts.setValue(""+(--attempts));
                errorMessage = String.format(MSG_ATTEMPTS_LEFT, attempts);
                response.addCookie(cookieLoginAttempts);
                
                if(attempts == 0){
                    isOnCooldown = true;
                }
                
                lastUsername = request.getParameter("login");
            }
            
            // If user is on cooldown
            if(isOnCooldown){
                // Display homepage
                createTooManyAttemptsPage(out, 0);
            }
            // If user is logged
            else if(isLogged){
                // Display homepage
                createSuccessPage(out, cookieLogin.getValue());
            }
            else{
                if(cookieLoginAttempts != null){
                    createLoginErrorPage(out, lastUsername, errorMessage, isLogged);
                }
                else{
                    createLoginPage(out);
                }
            }
        }
    }

    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /**
     * Handles the HTTP <code>GET</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Handles the HTTP <code>POST</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Returns a short description of the servlet.
     *
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>
    private void createLoginErrorPage(PrintWriter out, String username, String errorMessage, boolean isLoginValid){
        
        out.println("<!DOCTYPE html>");
        out.println("<html>");
        out.println("<head>");
        out.println("<title>Login</title>");
        out.println("<meta charset=\"UTF-8\">");
        out.println("<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">");
        out.println("</head>");
        out.println("<body>");
        out.println("<h2>LOGIN</h2>");
        out.println("<form action=\""+URL+"\" method=\"POST\">");
        out.println("Login    : <input type=\"text\" name=\"login\" value=\""+username+"\" size=\"32\"/>");
        out.println("<br/><br/>");
        out.println("Password : <input type=\"password\" name=\"password\" size=\"32\"/>");
        out.println("<br/><br/>");
        out.println("<br/>");
        out.println("<input type=\"submit\" value=\"Login\" name=\"login_button\" />");
        out.println("</form>");
        
        if(!isLoginValid){
            out.println("<p style=\"color:red\" >"+errorMessage+"</p>");
        }
        
        out.println("</body>");
        out.println("</html>");
    }
    private void createLoginPage(PrintWriter out){
        createLoginErrorPage(out, "", "", true);
    }
    private void createSuccessPage(PrintWriter out, String username){
        
        out.println("<!DOCTYPE html>");
        out.println("<html>");
        out.println("<head>");
        out.println("<title>Home</title>");
        out.println("<meta charset=\"UTF-8\">");
        out.println("<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">");
        out.println("</head>");
        out.println("<body>");
        out.println("<h2>WELCOME "+username+"</h2>");
        
        out.println("<form action=\""+URL+"\" method=\"POST\">");
        out.println("<input type=\"submit\" value=\"Logout\" name=\"logout_button\" />");
        out.println("</form>");
        
        out.println("</body>");
        out.println("</html>");
    }
    private void createGotoMenuPage(PrintWriter out){
        
        out.println("<!DOCTYPE html>");
        out.println("<html>");
        out.println("<head>");
        out.println("<title>Home</title>");
        out.println("<meta charset=\"UTF-8\">");
        out.println("<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">");
        out.println("</head>");
        out.println("<body>");
        out.println("<p>Back to login</p>");
        out.println("<a href=\""+URL+"\">Menu</a>");
        out.println("</body>");
        out.println("</html>");
    }
    private void createTooManyAttemptsPage(PrintWriter out, int secsLeft){
        
        out.println("<!DOCTYPE html>");
        out.println("<html>");
        out.println("<head>");
        out.println("<title>Home</title>");
        out.println("<meta charset=\"UTF-8\">");
        out.println("<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">");
        out.println("</head>");
        out.println("<body>");
        out.println("<p>Too many login attempts</p>");
        out.println("<p>Please wait ? second(s)</p>");
        out.println("</body>");
        out.println("</html>");
    }
}
