package io.github.dkocian.hipchatmessage;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import butterknife.Bind;
import butterknife.ButterKnife;
import io.github.dkocian.hipchatmessage.service.ParseMessageIntentService;
import io.github.dkocian.hipchatmessage.util.Constants;
import io.github.dkocian.hipchatmessage.util.Extras;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{
    private static final String TAG = MainActivity.class.getName();
    public static final int INDENT_SPACES = 4;
    public static final String INPUT_LABEL = "Input: ";
    public static final String RETURN_LABEL = "Return (string):";
    @Bind(R.id.tvInput)
    protected EditText tvInput;
    @Bind(R.id.tvOutput)
    protected TextView tvOutput;
    @Bind(R.id.btnSend)
    protected Button btnSend;
    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String parsedMessage = intent.getStringExtra(Extras.PARSED_MESSAGE);
            String message = intent.getStringExtra(Extras.MESSAGE);
            tvOutput.setText(getFormattedString(message, parsedMessage));
        }
    };

    private String getFormattedString(String message, String parsedMessage) {
        StringBuilder stringBuilder = new StringBuilder()
                .append(INPUT_LABEL).append(message).append(Constants.NEW_LINE)
                .append(RETURN_LABEL).append(Constants.NEW_LINE);
        String prettyJson;
        try {
            prettyJson = new JSONObject(parsedMessage).toString(INDENT_SPACES);
        } catch (JSONException e) {
            prettyJson = parsedMessage;
        }
        stringBuilder.append(prettyJson);
        return stringBuilder.toString();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        btnSend.setOnClickListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        LocalBroadcastManager.getInstance(this).registerReceiver(mReceiver,  new IntentFilter(Constants.PARSE_ACTION));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void startService(String message) {
        Intent intent = new Intent(this, ParseMessageIntentService.class);
        intent.putExtra(Extras.MESSAGE, message);
        this.startService(intent);
    }

    @Override
    protected void onPause() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mReceiver);
        super.onPause();
    }

    @Override
    public void onClick(View v) {
        startService(tvInput.getText().toString());
        tvInput.setText("");
    }
}
