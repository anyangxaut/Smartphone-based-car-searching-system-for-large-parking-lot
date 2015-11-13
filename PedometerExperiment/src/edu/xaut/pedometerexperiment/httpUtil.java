package edu.xaut.pedometerexperiment;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;


/**
 * 该类作用：http工具类，通过url创建httpget或者httppost对象，利用httpget或者httppost
 *对象做参数，获取httpresponse对象
 *
 *主要方法：queryStringForGet和queryStringForPost
 *queryStringForGet方法通过get方式提交httprequest，其参数以明文形式显示于浏览器网址部分。
 *queryStringForPost方法通过post方式提交httprequest，其参数以键值对形式封装在请求实体中。
 *
 *注意：编码格式需要统一，否则会出现乱码。
 * @author anyang
 *
 */
public class httpUtil {

	// 服务器端IP地址
	public static final String BASE_URL="http://10.50.62.65:9999/PedometerExperiment";
	
	public static HttpGet getHttpGet(String url){
		HttpGet request = new HttpGet(url);
		 return request;
	}
	
	public static HttpPost getHttpPost(String url){
		 HttpPost request = new HttpPost(url);
		 return request;
	}
	
	public static HttpResponse getHttpResponse(HttpGet request) throws ClientProtocolException, IOException{
		HttpResponse response = new DefaultHttpClient().execute(request);
		return response;
	}
	
	public static HttpResponse getHttpResponse(HttpPost request) throws ClientProtocolException, IOException{
		HttpResponse response = new DefaultHttpClient().execute(request);
		return response;
	}
	
	public static  String queryStringForGet(String url){

		HttpGet request = httpUtil.getHttpGet(url);
		String result = null;
		
		try {
			
			HttpResponse response = httpUtil.getHttpResponse(request);
			
			if(response.getStatusLine().getStatusCode()==200){
			
				HttpEntity entity = response.getEntity();
				
				 try
			        {
			            InputStream inputStream = entity.getContent();
			            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"), 1);
			            StringBuilder tmp = new StringBuilder();

			           
			            String line = null;
			            while (null != (line = reader.readLine()))
			            {
			            	tmp.append(line);

			            }

			            result = tmp.toString();

			        	return result;
			        }
			        catch (Exception e)
			        {
			            e.printStackTrace();
			        }
				
				return result;
			}
		} catch (ClientProtocolException e) {
			e.printStackTrace();
			result = "网络异常ClientProtocolException"+e;
			return result;
		} 
		catch (IOException e) {
			e.printStackTrace();
			result = "网络异常IOException"+ e;
			return result;
		}
        return null;
    }
	public static  String queryStringForPost(String url, List<NameValuePair> pairList){

		String result = null;
		try {
			HttpEntity requestHttpEntity = new UrlEncodedFormEntity(pairList, "UTF-8");
			
			 // URL使用基本URL即可，其中不需要加参数
			HttpPost request = httpUtil.getHttpPost(url);
			
			 request.setEntity(requestHttpEntity);
          
			 try {
				HttpResponse response = httpUtil.getHttpResponse(request);
				
				if(response.getStatusLine().getStatusCode()==200){
					
					HttpEntity entity = response.getEntity();
					
					 try
				        {
				            InputStream inputStream = entity.getContent();
				            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"), 1);
				            StringBuilder tmp = new StringBuilder();

				           
				            String line = null;
				            while (null != (line = reader.readLine()))
				            {
				            	tmp.append(line);

				            }

				            result = tmp.toString();

				        	return result;
				        }
				        catch (Exception e)
				        {
				            e.printStackTrace();
				        }
					
					return result;
				}
				
			} catch (ClientProtocolException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				result = "网络异常ClientProtocolException"+e;
				return result;
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				result = "网络异常IOException"+ e;
				return result;
			}
			 
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
        return null;
    }
}
