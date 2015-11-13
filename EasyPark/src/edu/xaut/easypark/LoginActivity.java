package edu.xaut.easypark;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.Toast;
import edu.xaut.util.HttpUtil;

/**
 * App登陆界面：用户名密码验证
 * @author anyang
 *
 */
public class LoginActivity extends Activity {
    private EditText usernameEdt = null;
    private EditText passwordEdt = null;
    private Button loginBtn = null;
    private Button registerBtn = null;
    private String username, password;
    private Handler handler = null;
    private CheckBox rempasswd = null;
    private SharedPreferences sp = null;

    @SuppressLint("HandlerLeak")
	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
     // 去掉标题栏
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_login);

        //实例化视图组件
        registerBtn = (Button)findViewById(R.id.register_button);
        loginBtn = (Button)findViewById(R.id.login_button);
        usernameEdt = (EditText)findViewById(R.id.username);
        passwordEdt = (EditText)findViewById(R.id.password);
        rempasswd = (CheckBox)findViewById(R.id.rempasswd);
        
        sp = this.getSharedPreferences("userInfo", Context.MODE_PRIVATE);
        
        //判断记住密码多选框的状态  
        if(sp.getBoolean("isCheck", false))  
          {  
            //设置默认是记录密码状态  
        	rempasswd.setChecked(true);  
        	usernameEdt.setText(sp.getString("username", ""));  
        	passwordEdt.setText(sp.getString("password", ""));
          }

        // handler类应该定义成静态类，否则可能导致内存泄露
        handler = new Handler() {
            public void handleMessage(Message msg) {
                // 判断msg类型，进行相应操作
                switch (msg.what){
                    case 1:{Toast.makeText(getApplicationContext(), "用户名或密码错误",
                            Toast.LENGTH_SHORT).show(); break;}
                }
            }
        };
        
       // 监听记住密码多选框按钮事件  
        rempasswd.setOnCheckedChangeListener(new OnCheckedChangeListener() {  
			@Override
			public void onCheckedChanged(CompoundButton arg0, boolean arg1) {
				// TODO Auto-generated method stub
                if (rempasswd.isChecked()) {  
                    sp.edit().putBoolean("isCheck", true).commit();        
                }else {  
                    sp.edit().putBoolean("isCheck", false).commit();        
                }  
			}  
        });  

        // 设置登录按钮登陆监听器
        loginBtn.setOnClickListener(new View.OnClickListener( ) {
            @Override
            public void onClick(View arg0) {
                // TODO Auto-generated method stub
                username = usernameEdt.getText().toString() ;
                password = passwordEdt.getText().toString() ;

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        // 登陆认证httpclient
                        Login(username, password ) ;
                    }
                }).start();
            }
        }) ;

        // 设置注册按钮监听器
        registerBtn.setOnClickListener(new View.OnClickListener( ) {
            @Override
            public void onClick(View arg0) {
                // TODO Auto-generated method stub
            }
        });
    }


    private void Login( String username , String password ){
        //URL地址，访问指定网站的servlet
        String urlStr = HttpUtil.BASE_URL+"/LoginServlet" ;

        // 如果传递参数个数比较多，可以对传递的参数进行封装
        List<NameValuePair> params = new ArrayList<NameValuePair>() ;
        params.add(new BasicNameValuePair("username", username)) ;
        params.add(new BasicNameValuePair("password", password) ) ;

        String msg = HttpUtil.queryStringForPost(urlStr, params);

        if(msg.equals("null") ){
        	// 从消息池中拿来一个msg，不需要另外开辟空间
            Message message = handler.obtainMessage();
            message.what = 1;
            handler.sendMessage(message);
        }else{
        	if(rempasswd.isChecked())  
            {  
             //记住用户名、密码  
              Editor editor = sp.edit();  
              editor.putString("username", username);  
              editor.putString("password", password);  
              editor.commit();  
            }  
            Intent intent = new Intent( ) ;
            intent.putExtra("username", msg.toString());
            intent.setClass(LoginActivity.this, HomePageActivity.class);
            startActivityForResult(intent, RESULT_OK ) ;
            finish();
        }
    }
}