package edu.xaut.pedometerexperiment;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import edu.xaut.dao.SampleDao;
import edu.xaut.daoImpl.SampleDaoImpl;

public class SampleServlet extends HttpServlet {

	/**
	 * Servlet序列号
	 */
	private static final long serialVersionUID = 1L;

	public void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		response.setContentType("application/json;charset=UTF-8");
		request.setCharacterEncoding("UTF-8");

		PrintWriter out = response.getWriter();

		SampleDao dao = new SampleDaoImpl();

		List<Double> gvList = new ArrayList<Double>();
		String temp1 = request.getParameter("gv").toString().replace("[", "");
		String temp2 = temp1.replace("]", "");
		String[] temp3 = temp2.split(",");
		for (String temp : temp3) {
			gvList.add(Double.parseDouble(temp.trim()));
		}

		boolean result = dao.Sample(gvList);

		out.print(result);

		out.flush();// 清理servlet容器的缓冲区
		out.close();// 关闭输出流对象，释放输出流资源
	}

	public void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		doGet(request, response);

	}
}
