package com.example.yao.myapplication;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

public class MainActivity extends AppCompatActivity {
    private final static int TIMEOUT = 50000;
    private static String url = "http://hanyu.baidu.com/s?wd=${word}&ptype=zici";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //得到按钮实例
        Button queryButton = (Button)findViewById(R.id.query);
        //设置监听按钮点击事件
        queryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MainActivity.this,"开始查询......", Toast.LENGTH_SHORT).show();
                new Thread(runnable).start();
                Toast.makeText(MainActivity.this,"查询完成！", Toast.LENGTH_SHORT).show();
            }
        });

        final EditText editText = (EditText) findViewById(R.id.simplified);
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                ((TextView) findViewById(R.id.simplifiedCount)).setText("?");
                ((TextView) findViewById(R.id.traditional)).setText("??");
                ((TextView) findViewById(R.id.traditionalCount)).setText("?");
            }

            @Override
            public void afterTextChanged(Editable s) { }
        });
    }

    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            Bundle data = msg.getData();
            String traditional = data.getString("traditional");
            String simplifiedStrokeCount = data.getString("simplifiedStrokeCount");
            String traditionalStrokeCount = data.getString("traditionalStrokeCount");

            //得到textview实例
            ((TextView) findViewById(R.id.simplifiedCount)).setText(simplifiedStrokeCount);
            ((TextView) findViewById(R.id.traditional)).setText(traditional);
            ((TextView) findViewById(R.id.traditionalCount)).setText(traditionalStrokeCount);
        }
    };

    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            String simplified = ((EditText) findViewById(R.id.simplified)).getText().toString();
            String traditional = null;
            String simplifiedStrokeCount = null;
            String traditionalStrokeCount = null;
            try {
                //获取指定网址的页面内容
                Document document = Jsoup.connect(getRealUrl(simplified)).timeout(TIMEOUT).get();
                if (document != null) {
                    Element strokeCount = document.getElementById("stroke_count");
                    simplifiedStrokeCount = strokeCount.getElementsByTag("span").get(0).text();
                    Element traditionalElement = document.getElementById("traditional");
                    traditional = traditionalElement.getElementsByTag("span").get(0).text();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            if (null != traditional) {
                try {
                    Document document = Jsoup.connect(getRealUrl(traditional)).timeout(TIMEOUT).get();
                    if (document != null) {
                        Element strokeCount = document.getElementById("stroke_count");
                        traditionalStrokeCount = strokeCount.getElementsByTag("span").get(0).text();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            Message msg = new Message();
            Bundle data = new Bundle();
            data.putString("traditional", traditional);
            data.putString("simplifiedStrokeCount", simplifiedStrokeCount);
            data.putString("traditionalStrokeCount", traditionalStrokeCount);
            msg.setData(data);
            handler.sendMessage(msg);
        }
    };

    private String getRealUrl(String word) {
        return url.replace("${word}", word);
    }
}
