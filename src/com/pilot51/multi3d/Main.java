package com.pilot51.multi3d;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

public class Main extends Activity implements OnClickListener {
	protected static String TAG;
	private Button btn1, btn2;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		TAG = getString(R.string.app_name);
		setContentView(R.layout.main);
		btn1 = (Button)findViewById(R.id.Button1);
		btn1.setOnClickListener(this);
		btn2 = (Button)findViewById(R.id.Button2);
		btn2.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.Button1:
				startActivity(new Intent(getBaseContext(), TestActivity.class));
				break;
			case R.id.Button2:
				final EditText input = new EditText(this);
				AlertDialog.Builder dlgEnterIP = new AlertDialog.Builder(this);
				dlgEnterIP
					.setTitle(R.string.connect)
					.setMessage(R.string.connect_msg)
					.setView(input)
					.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							startActivity(new Intent(getBaseContext(), TestMultiActivity.class).putExtra("address", input.getText().toString()));
						}
					})
					.setNegativeButton(android.R.string.cancel,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int which) {}
						});
				dlgEnterIP.show();
				break;
		}
	}
}