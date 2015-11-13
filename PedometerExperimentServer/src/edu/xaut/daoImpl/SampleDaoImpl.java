package edu.xaut.daoImpl;

import java.util.ArrayList;
import java.util.List;

import edu.xaut.dao.SampleDao;
import edu.xaut.util.DBOperation;

public class SampleDaoImpl implements SampleDao {

	public boolean Sample(List<Double> gvList) {
		// TODO Auto-generated method stub

		String[] sql = new String[gvList.size()];

		for (int i = 0; i < gvList.size(); i++) {

			sql[i] = "insert into gvdata (gvdata) values (" + gvList.get(i)
					+ ");";
		}

		DBOperation dboperation = new DBOperation();

		boolean rs = dboperation.excutesql(sql);

		dboperation.closeConn();

		return rs;

	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		List<Double> value = new ArrayList<Double>();
		value.add(1.2);
		SampleDao test = new SampleDaoImpl();
		boolean result = test.Sample(value);
		System.out.println(result);
	}

}
