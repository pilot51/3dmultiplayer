package com.pilot51.multi3d;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class Main extends Activity implements OnClickListener {
    Button btn1;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        btn1 = (Button)findViewById(R.id.Button1);
		btn1.setOnClickListener(this);
    }

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.Button1:
				startActivity(new Intent(getBaseContext(), TestActivity.class));
				break;
		}
	}
}