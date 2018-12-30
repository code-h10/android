package com.example.hwang_il_yeong.a4project;


import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.messaging.FirebaseMessaging;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

import static com.example.hwang_il_yeong.a4project.R.id.textView;


public class MainActivity extends ActionBarActivity {

    private String path = "http://weather.naver.com/rgn/townWetr.nhn?naverRgnCd=09260520";
    private Document document;
    private Elements elements;
    private Element targetElement;
    private Animation alphaAnim;
    private ImageView IV, Image01;
    private ProgressBar PB1, PB2, PB3;
    private LinearLayout L1, L2, L3; // 토양, 습도 , 온도 레이아웃
    private static String SERVER;
    private static int PORT;
    private TextView TV1, TV2, TV3,TV4;
    private Button button;
    private Socket client;
    private PrintWriter printwriter; // 서버에게 문자열을 보내기 위한것(LED 제어)
    private BufferedReader bufferedReader = null;
    private BufferedWriter bufferedWriter = null;
    boolean pump = true;
    private String str;
    private String text;

    private double soil;
    private int value2 = 0;
    private String SensorStr;
    private String[] SensorDate;

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_home:
                    L1.setVisibility(View.VISIBLE);
                    L2.setVisibility(View.INVISIBLE);
                    L3.setVisibility(View.INVISIBLE);
                    return true;
                case R.id.navigation_dashboard:
                    L1.setVisibility(View.INVISIBLE);
                    L2.setVisibility(View.VISIBLE);
                    L3.setVisibility(View.INVISIBLE);
                    return true;
                case R.id.navigation_notifications:
                    L1.setVisibility(View.INVISIBLE);
                    L2.setVisibility(View.INVISIBLE);
                    L3.setVisibility(View.VISIBLE);
                    return true;
            }
            return false;
        }

    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation); // 토양 온도 습도 메뉴 버튼
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        Intent intent = getIntent();  // LogActivity 에서 IP,PORT 번호를 받아온다.
        SERVER = intent.getStringExtra("IPStr");
        PORT = intent.getExtras().getInt("PONum",80);

        TV1 = (TextView)findViewById(textView); // 토양습도센서값출력 뷰
        TV2 = (TextView)findViewById(R.id.textView3); // 습도 센서
        TV3 = (TextView)findViewById(R.id.textView5); // 온도 센서
        button = (Button)findViewById(R.id.button4); // 물주기 버튼

        TV4 = (TextView)findViewById(R.id.WaText);
        Toolbar TB = (Toolbar)findViewById(R.id.TooBar);
        setSupportActionBar(TB);

        Image01 = (ImageView)findViewById(R.id.imageView3);
        IV = (ImageView)findViewById(R.id.imageView);
        alphaAnim = AnimationUtils.loadAnimation(this, R.anim.alpha);
        IV.setAnimation(alphaAnim);
        Image01.setAnimation(alphaAnim);
        TV4.setAnimation(alphaAnim);


        PB1 = (ProgressBar)findViewById(R.id.progressbar1);
        PB2 = (ProgressBar)findViewById(R.id.progressbar2);
        PB3 = (ProgressBar)findViewById(R.id.progressbar3);

        L1 = (LinearLayout)findViewById(R.id.linear1);
        L2 = (LinearLayout)findViewById(R.id.linear2);
        L3 = (LinearLayout)findViewById(R.id.linear3);

        FirebaseMessaging.getInstance().subscribeToTopic("news");

        ChatOperator chatOperator = new ChatOperator(); // 비동기 통신 작업 클래스 생성(AsyncTask)
        chatOperator.execute(); //execute 시작한다는 뜻

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.action_cate:
                final CharSequence[] items2 = { "강남구", "강동구", "강북구", "강서구","관악구", "광진구", "구로구", "금천구","노원구", "도봉구", "동대문구", "동작구","마포구","서대문구",
                "성동","성북구","송파구","양천구","영등포구","용산구","은평구","종로구","중구","중랑구","강원도"};
                AlertDialog.Builder alertDialogBuilder2 = new AlertDialog.Builder(this);
                // 제목셋팅
                alertDialogBuilder2.setTitle("지역 날씨");
                alertDialogBuilder2.setNegativeButton("취소", null);
                alertDialogBuilder2.setSingleChoiceItems(items2, -1,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {

                                WaChange(id);
                                Toast.makeText(getApplicationContext(), items2[id] + "가 선택되었습니다", Toast.LENGTH_SHORT).show();
                                dialog.dismiss();
                            }
                        });

                // 다이얼로그 생성
                AlertDialog alertDialog2 = alertDialogBuilder2.create();

                // 다이얼로그 보여주기
                alertDialog2.show();
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void WaChange(int id){
        switch (id){
            case 0:
                path = "http://weather.naver.com/rgn/townWetr.nhn?naverRgnCd=09620585";
                break;
            case 1:
                path = "http://weather.naver.com/rgn/townWetr.nhn?naverRgnCd=09740110";
                break;
            case 2:
                path = "http://weather.naver.com/rgn/townWetr.nhn?naverRgnCd=09305101";
                break;
            case 3:
                path = "http://weather.naver.com/rgn/townWetr.nhn?naverRgnCd=09500603";
                break;
            case 4:
                path = "http://weather.naver.com/rgn/townWetr.nhn?naverRgnCd=09620585";
                break;
            case 5:
                path = "http://weather.naver.com/rgn/townWetr.nhn?naverRgnCd=09215104";
                break;
            case 6:
                path = "http://weather.naver.com/rgn/townWetr.nhn?naverRgnCd=09530520";
                break;
            case 7:
                path = "http://weather.naver.com/rgn/townWetr.nhn?naverRgnCd=09545101";
                break;
            case 8:
                path = "http://weather.naver.com/rgn/townWetr.nhn?naverRgnCd=09350595";
                break;
            case 9:
                path = "http://weather.naver.com/rgn/townWetr.nhn?naverRgnCd=09320521";
                break;
            case 10:
                path = "http://weather.naver.com/rgn/townWetr.nhn?naverRgnCd=09230600";
                break;
            case 11:
                path = "http://weather.naver.com/rgn/townWetr.nhn?naverRgnCd=09590510";
                break;
            case 12:
                path = "http://weather.naver.com/rgn/townWetr.nhn?naverRgnCd=09620585";
                break;
            case 13:
                path = "http://weather.naver.com/rgn/townWetr.nhn?naverRgnCd=09410690";
                break;
            case 14:
                path = "http://weather.naver.com/rgn/townWetr.nhn?naverRgnCd=09200590";
                break;
            case 15:
                path = "http://weather.naver.com/rgn/townWetr.nhn?naverRgnCd=09290660";
                break;
            case 16:
                path = "http://weather.naver.com/rgn/townWetr.nhn?naverRgnCd=09710101";
                break;
            case 17:
                path = "http://weather.naver.com/rgn/townWetr.nhn?naverRgnCd=09470510";
                break;
            case 18:
                path = "http://weather.naver.com/rgn/townWetr.nhn?naverRgnCd=09560550";
                break;
            case 19:
                path = "http://weather.naver.com/rgn/townWetr.nhn?naverRgnCd=09170104";
                break;
            case 20:
                path = "http://weather.naver.com/rgn/townWetr.nhn?naverRgnCd=09380551";
                break;
            case 21:
                path = "http://weather.naver.com/rgn/townWetr.nhn?naverRgnCd=09110146";
                break;
            case 22:
                path = "http://weather.naver.com/rgn/townWetr.nhn?naverRgnCd=09140590";
                break;
            case 23:
                path = "http://weather.naver.com/rgn/townWetr.nhn?naverRgnCd=09260520";
                break;
            case 24:
                path = "http://weather.naver.com/rgn/townWetr.nhn?naverRgnCd=01830350";
                break;
            default:
        }
    }

    private class ChatOperator extends AsyncTask<Void, String, Void> { // AsyncTask 클래스는 주로 3가지 메소드를 사용함



        @Override
        protected Void doInBackground(Void... arg0) { // 1번째 메소드


            try {
                client = new Socket(SERVER, PORT); // 서버 소켓 생성

                if (client != null) {


                    //자동 flushing 기능이 있는 PrintWriter 객체를 생성한다.
                    //client.getOutputStream() 서버에 출력하기 위한 스트림을 얻는다.

                    printwriter = new PrintWriter(client.getOutputStream(), true);
                    InputStreamReader inputStreamReader = new InputStreamReader(client.getInputStream());
                    //입력 스트림 inputStreamReader에 대해 기본 크기의 버퍼를 갖는 객체를 생성한다.
                    bufferedReader = new BufferedReader(inputStreamReader);

                } else {
                    System.out.println("9990 서버가 포트에서 시작된 Bean이 아닙니다.");
                }
            } catch (UnknownHostException e) {
                System.out.println("9990 서버가 포트에서 시작된 Bean이 아닙니다.");
                e.printStackTrace();
            } catch (IOException e) {
                System.out.println("9990 서버가 포트에서 시작된 Bean이 아닙니다.");
                e.printStackTrace();
            }
            if ( client != null) {
                Receiver receiver = new Receiver(); // 센서값 받아오는 AsyncTask 를 생성.
                receiver.execute();
            }
            return null;
        }
        protected void onProgressUpdate(String...value){


        }


        @Override
        protected void onPostExecute(Void result) { // 2번 메소드

            button.setOnClickListener(new View.OnClickListener() { // 물주기 버튼 클릭 이벤트
                public void onClick(View v) {

                    if ( pump == true ) {
                        if(value2 < 60){
                            button.setText("PUMP OFF");
                            str="1";
                            pump = false;
                        }else {
                            AlertDialog.Builder alert_confirm = new AlertDialog.Builder(MainActivity.this);
                            alert_confirm.setMessage("현재 수분이 많습니다.");
                            alert_confirm.setPositiveButton("확인", null);
                            AlertDialog alert = alert_confirm.create();
                            alert.setIcon(R.drawable.cloud01);
                            alert.setTitle("알림!");
                            alert.show();

                            TextView messageText = (TextView)alert.findViewById(android.R.id.message);
                            messageText.setGravity(Gravity.CENTER);
                            alert.show();
                        }
                    }
                    else if(pump == false){
                        button.setText("PUMP ON");
                        str="2";
                        pump = true;
                    }


                    final Sender messageSender = new Sender(); // 펌프 제어 AsyncTask 를 생성.
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                        messageSender.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, str);
                    } else {
                        messageSender.execute(str); // 숫자 1번이 전송되면 Off,숫자 2번이 전송되면 On
                    }


                }
            });

        }
    }

    private class Receiver extends AsyncTask<String, String, String> { // 서버로부터 얻은 값 화면 출력 클래스

        @Override
        protected void onPreExecute(){
            PB1.setProgress(0);
            PB2.setProgress(0);
            PB3.setProgress(0);
        }
        @Override
        protected String doInBackground(String...value) {

            while (true) {
                try {
                    //스트림으로부터 읽어올수 있으면 true 를 반환한다.
                    if (bufferedReader.ready()) {
                        //SensorData = bufferedReader.readLine();
                        SensorStr = bufferedReader.readLine(); // 서버로부터 전송된 값을 '\n'을 만날 때까지 읽어온다.
                        SensorDate =  SensorStr.split("-");

                        document = Jsoup.connect(path).get();
                        elements = document.select("strong");
                        targetElement = elements.get(3);
                        text = targetElement.text();
                        publishProgress(text);
                    }

                }
                catch(IOException e)
                {
                    e.printStackTrace();
                }
                try {
                    Thread.sleep(500);
                } catch (InterruptedException ie) {}
            }
        }

        //publishProgress(null)에 의해서 호출된다. '
        //서버에서 전달받은 문자열을 읽어서 화면에 출력해준다.
        @Override
        protected void onProgressUpdate(String...value) { // 수신받은 데이터 화면 출력


            soil = Integer.parseInt(SensorDate[0]) / 10.23;
            value2 = (int) (100.0 - soil);

            TV4.setText(value[0].toString());
            PB1.setProgress(value2);
            PB2.setProgress(Integer.parseInt(SensorDate[1]));
            PB3.setProgress(Integer.parseInt(SensorDate[2]));
            TV1.setText("토양 습도 : " + value2 + " %");
            TV2.setText("실내 온도 : " + Integer.parseInt(SensorDate[1]) + " C");
            TV3.setText("실내 습도 : " + Integer.parseInt(SensorDate[2]) + " %");


            if(value2 < 10){
                IV.setAnimation(alphaAnim);
                IV.setImageResource(R.drawable.cloud01);

            }else if(value2 >= 20 && value2 < 40){
                IV.setAnimation(alphaAnim);
                IV.setImageResource(R.drawable.cloud02);

            }else if(value2 < 50){
                IV.setImageResource(R.drawable.cloud03);

            }else if(value2 < 60){
                IV.setImageResource(R.drawable.cloud04);

            }


            switch (value[0].toString()){
                case "맑음":
                    Image01.setImageResource(R.drawable.ic_wb_sunny_black_24dp);
                    break;
                case "비":
                    Image01.setImageResource(R.drawable.ic_beach_access_black_24dp);
                    break;
                case "흐림":
                    Image01.setImageResource(R.drawable.ic_wb_cloudy_black_24dp);
                    break;
                default:
                    Image01.setImageResource(R.drawable.ic_texture_black_24dp);

            }
        }
        @Override
        protected void onPostExecute(String result) {

        }
    }

    private class Sender extends AsyncTask<String, String, Void> { // LED 제어 클래스

        private String control;

        @Override
        protected Void doInBackground(String... params) {

            control = params[0];
            //문자 1,2 스트림에 기록한다.
            printwriter.write(control + "\n"); //
            printwriter.flush();
            return null;
        }

        //클라이언트에서 입력한 문자열을 화면에 출력한다.
        @Override
        protected void onPostExecute(Void result) {

        }
    }
}
