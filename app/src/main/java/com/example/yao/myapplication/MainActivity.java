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
import org.jsoup.select.Elements;

import java.io.IOException;

public class MainActivity extends AppCompatActivity {
    private final static int TIMEOUT = 50000;
    private static final String SEARCH_URL = "https://www.zdic.net/hans/{word}";

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
                Document document = getDocument(simplified);
                if (document != null) {
                    Elements strokeCount = document.getElementsByClass("z_ts3");
                    simplifiedStrokeCount = strokeCount.get(0).parent().text().split(" ")[1];
                    Elements elements = document.getElementsByClass("diczx3");
                    if (null != elements && elements.size() > 0) {
                        if ("strong".equals(elements.get(0).parent().tag().toString())) {
                            traditional = elements.get(0).text();
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            if (null != traditional) {
                try {
                    Document document = getDocument(traditional);
                    if (document != null) {
                        Elements strokeCount = document.getElementsByClass("z_ts3");
                        traditionalStrokeCount = strokeCount.get(0).parent().text().split(" ")[1];
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

    private Document getDocument(String simplified) throws IOException {
        return Jsoup.connect(getRealUrl(simplified)).timeout(TIMEOUT).followRedirects(true).execute().parse();
    }

    private String getRealUrl(String word) {
        return SEARCH_URL.replace("{word}", word);
    }
}
