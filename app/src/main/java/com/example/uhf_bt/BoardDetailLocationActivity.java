package com.example.uhf_bt;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.example.uhf_bt.component.ListItemView;
import com.example.uhf_bt.dto.ButtonItem;
import com.example.uhf_bt.dto.DetailLocation;
import com.example.uhf_bt.dto.PostCategory;
import com.example.uhf_bt.dto.PostDetailLocation;
import com.example.uhf_bt.dto.StatusVM;
import com.example.uhf_bt.json.JsonTaskGetDetailLocationList;
import com.example.uhf_bt.json.JsonTaskPostItem;
import com.example.uhf_bt.json.JsonTaskUpdateItem;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class BoardDetailLocationActivity extends BaseActivity {

    public int detailLocationId = 0;
    public int floorId = 0;
    private ListView listView;
    private List<ButtonItem> itemList = new ArrayList<>();
    private TextView tvDetailLocationName;
    private Button btnUpdateDetailLocation;
    private Button btnAddDetailLocation;
    public int updateDetailLocationId = 0;

    private TextView path;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_board_detail_location);

        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("detailLocationId")) {

            detailLocationId = intent.getIntExtra("detailLocationId", 0); // 0 is the default value if the key is not found

            Log.d("", String.valueOf(detailLocationId));

            Globals.detailLocationId = detailLocationId;

        }

        if (intent != null && intent.hasExtra("floorId")) {

            floorId = intent.getIntExtra("floorId", 0); // 0 is the default value if the key is not found

            Globals.floorId = floorId;
        }
    }

    public void reCallAPI()
    {
        String req = Globals.apiUrl + "detaillocation/read?id=" + String.valueOf(detailLocationId);
    }
    public void btnLogOut(View v)
    {
        Globals g = (Globals) getApplication();
        g.isLogin = false;

        startActivityForResult(new Intent(getApplicationContext(), LoginActivity.class), 0);
    }
}
