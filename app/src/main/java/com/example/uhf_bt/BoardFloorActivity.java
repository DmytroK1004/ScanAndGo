package com.example.uhf_bt;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.example.uhf_bt.component.ListItemView;
import com.example.uhf_bt.dto.Area;
import com.example.uhf_bt.dto.ButtonItem;
import com.example.uhf_bt.dto.DetailLocation;
import com.example.uhf_bt.dto.Floor;
import com.example.uhf_bt.dto.PostCategory;
import com.example.uhf_bt.dto.PostDetailLocation;
import com.example.uhf_bt.dto.PostFloor;
import com.example.uhf_bt.dto.StatusVM;
import com.example.uhf_bt.json.JsonTaskGetDetailLocationList;
import com.example.uhf_bt.json.JsonTaskGetFloorList;
import com.example.uhf_bt.json.JsonTaskPostItem;
import com.example.uhf_bt.json.JsonTaskUpdateItem;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class BoardFloorActivity extends BaseActivity {

    public int floorId = 0;
    private ListView listView;
    private List<ButtonItem> itemList = new ArrayList<>();
    private EditText etDetailLocation;
    private Button btnUpdateDetailLocation;
    private Button btnAddDetailLocation;
    public int updateDetailLocationId = 0;
    private ListItemView adapter;
    private TextView path;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_board_floor);

        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("floorId")) {
            floorId = intent.getIntExtra("floorId", 0);
            Globals.floorId = floorId;
        }

        btnUpdateDetailLocation = findViewById(R.id.btnUpdateDetailLocation);
        btnAddDetailLocation = findViewById(R.id.btnAddDetailLocation);
        btnUpdateDetailLocation.setVisibility(View.GONE);
        etDetailLocation = findViewById(R.id.etDetailLocation);
        path = findViewById(R.id.tvLocationFloor);

        path.setText(Globals.buildingName + "/" + Globals.areaName + "/" + Globals.floorName);

        Globals g = (Globals) getApplication();
        if (!Globals.isLogin) {
            startActivityForResult(new Intent(getApplicationContext(), LoginActivity.class), 0);
        }

        listView = findViewById(R.id.listDetailLocations);
        adapter = new ListItemView(this, itemList, null, null, null, null, this, null, null);
        listView.setAdapter(adapter);

        reCallAPI();
    }


    public void OnAddDetailLocation(View v) {

        if(btnAddDetailLocation.getText() == "Add")
        {
            if(etDetailLocation.length() > 0 )
            {
                try {

                    PostDetailLocation model = new PostDetailLocation(etDetailLocation.getText().toString(), Globals.floorId,"");
                    StatusVM result = new StatusVM();

                    String req = Globals.apiUrl + "detaillocation/create";

                    result = new JsonTaskPostItem().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, req, model.toJsonString()).get();

                    reCallAPI();

                } catch (ExecutionException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        } else {

            btnUpdateDetailLocation.setVisibility(View.GONE);
            btnAddDetailLocation.setText("Add");
        }
    }

    public void UpdateDetailLocation(String text, int id)
    {
        btnUpdateDetailLocation.setVisibility(View.VISIBLE);
        btnAddDetailLocation.setText("Cancel");
        etDetailLocation.setText(text);

        updateDetailLocationId = id;
    }

    public void OnUpdateDetailLocation(View v)
    {
        if (updateDetailLocationId > 0 && etDetailLocation.length() > 0)
        {
            String req = Globals.apiUrl +  "detaillocation/update?id=" + updateDetailLocationId;

            PostCategory model = new PostCategory();

            model.name = etDetailLocation.getText().toString();

            new JsonTaskUpdateItem().execute(req, model.toJsonString());

            updateDetailLocationId = 0;

            etDetailLocation.setVisibility(View.GONE);
            etDetailLocation.setText("");

            reCallAPI();
        }
    }

    public void reCallAPI() {
        String req = Globals.apiUrl + "detaillocation/readall?id=" + Globals.floorId;

        try {
            itemList.clear(); // Clear existing items

            List<DetailLocation> detailLocationLists = new JsonTaskGetDetailLocationList()
                    .executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, req)
                    .get();

            if (detailLocationLists != null) {
                for (DetailLocation p : detailLocationLists) {
                    Globals.detailLocationId = p.id;
                    ButtonItem newVM = new ButtonItem(p.name, 6, p.id);
                    itemList.add(newVM); // Add new items
                }
            }

            adapter.notifyDataSetChanged(); // Notify the adapter

        } catch (ExecutionException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public void btnLogOut(View v)
    {
        Globals.isLogin = false;
        startActivityForResult(new Intent(getApplicationContext(), LoginActivity.class), 0);
    }
}
