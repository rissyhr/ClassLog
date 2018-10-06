package android.lifeistech.com.classlog;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class RegisterActivity extends AppCompatActivity implements View.OnClickListener {

    private Button button;

    // 講義名の登録は必須。担当教員名、教室名は任意。


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        button = findViewById(R.id.register);
    }

    @Override
    public void onClick(View v) {

        final EditText subjectText = findViewById(R.id.subject);
        String subject = subjectText.getText().toString();

        if(subject.isEmpty()){

            subjectText.setError("講義名を入力してください。");

        } else {

            // レルムに保存
            // intentでも値を渡す？

            // 画面遷移（DetailActivityへ）



        }

    }
}

