package com.example.hwang_il_yeong.a4project;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

/**
 * Created by hwang-il-yeong on 2017. 5. 14..
 */

public class LogActivity extends AppCompatActivity {
    EditText IpAdd,PoNum;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log);

        IpAdd = (EditText)findViewById(R.id.editText);
        PoNum = (EditText)findViewById(R.id.editText2);

    }

    public void LogButton(View v){
        try{
            String IPStr = IpAdd.getText().toString();
            String POStr = PoNum.getText().toString();
            int PONum = Integer.parseInt(POStr);
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            intent.putExtra("IPStr",IPStr);
            intent.putExtra("PONum",PONum);
            startActivity(intent);
        }catch(Exception e){
            Toast.makeText(getApplicationContext(),"IP와 PORT 번호를 입력하세요!",Toast.LENGTH_LONG).show();
        }

    }
}
