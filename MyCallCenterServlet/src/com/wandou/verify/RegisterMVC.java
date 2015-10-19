package com.wandou.verify;


import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.multiaction.MultiActionController;
 

/**
 * Servlet implementation class Register
 */
@WebServlet("/RegisterMVC")
public class RegisterMVC extends MultiActionController {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public RegisterMVC() {
        super();
        // TODO Auto-generated constructor stub
    }

    public ModelAndView onSub(HttpServletRequest request,HttpServletResponse response)
	{	
		String uname = request.getParameter("username");
		String upass = request.getParameter("password");
		System.out.println("RegisterMVC:user_name:"+uname+"upwd:"+upass);
		User user = new User(uname,upass);
		return new ModelAndView("index","user",user);   
	}

}
