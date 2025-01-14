package com.example.uhf_bt;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.uhf_bt.component.ListItemView;
import com.example.uhf_bt.dto.Area;
import com.example.uhf_bt.dto.ButtonItem;
import com.example.uhf_bt.dto.Floor;
import com.example.uhf_bt.dto.PostArea;
import com.example.uhf_bt.dto.PostCategory;
import com.example.uhf_bt.dto.PostFloor;
import com.example.uhf_bt.dto.StatusVM;
import com.example.uhf_bt.json.JsonTaskGetAreaList;
import com.example.uhf_bt.json.JsonTaskGetFloorList;
import com.example.uhf_bt.json.JsonTaskPostItem;
import com.example.uhf_bt.json.JsonTaskUpdateItem;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class BoardAreaActivity extends BaseActivity {

    public  int buildingId = 0;
    public  int areaId = 0;
    private ListView floorListView;
    private List<ButtonItem> floorViewList = new ArrayList<>();
    private EditText etFloorName;
    private Button btnUpdateFloor;
    private Button btnAddFloor;
    public int updateFloorId = 0;
    private TextView path;
    private ListItemView adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_board_area);

        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("buildingId")) {
            buildingId = intent.getIntExtra("buildingId", 0);
            Globals.buildingId = buildingId;
        }

        if (intent != null && intent.hasExtra("areaId")) {
            areaId = intent.getIntExtra("areaId", 0);
            Globals.areaId = areaId;
        }

        btnUpdateFloor = findViewById(R.id.btnUpdateFloor);
        btnAddFloor = findViewById(R.id.btnAddFloor);
        btnUpdateFloor.setVisibility(View.GONE);
        etFloorName = findViewById(R.id.etFloorName);
        path = findViewById(R.id.tvLocationArea);
        path.setText(Globals.buildingName + "/" + Globals.areaName);

        floorListView = findViewById(R.id.listFloors);
        adapter = new ListItemView(this, floorViewList, null, null, null, this, null, null, null);
        floorListView.setAdapter(adapter);

        Globals g = (Globals) getApplication();
        if (!Globals.isLogin) {
            startActivityForResult(new Intent(getApplicationContext(), LoginActivity.class), 0);
        }

        reCallAPI();
    }


    public void OnAddFloor(View v) {

        if(btnAddFloor.getText() == "Add")
        {
            if(etFloorName.length() > 0 )
            {
                try {

                    PostFloor model = new PostFloor(etFloorName.getText().toString(), Globals.areaId);
                    StatusVM result = new StatusVM();

                    String req = Globals.apiUrl + "floor/create";

                    result = new JsonTaskPostItem().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, req, model.toJsonString()).get();

                    if (result != null)
                    {

                        Toast.makeText(getApplicationContext(), "Added floor successfully", Toast.LENGTH_SHORT).show();
                        reCallAPI();

                    } else {

                        Toast.makeText(getApplicationContext(), "Not added floor successfully", Toast.LENGTH_SHORT).show();
                    }

                } catch (ExecutionException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        } else {

            btnUpdateFloor.setVisibility(View.GONE);
            btnUpdateFloor.setText("Add");
        }
    }

    public void UpdateFloor(String text, int id)
    {
        btnUpdateFloor.setVisibility(View.VISIBLE);
        btnAddFloor.setText("Cancel");
        etFloorName.setText(text);

        updateFloorId = id;
    }

    public void OnUpdateFloor(View v)
    {
        if (updateFloorId > 0 && etFloorName.length() > 0)
        {
            String req = Globals.apiUrl +  "floor/update?id=" + updateFloorId;

            PostCategory model = new PostCategory();

            model.name = etFloorName.getText().toString();

            new JsonTaskUpdateItem().execute(req, model.toJsonString());

            updateFloorId = 0;

            btnUpdateFloor.setVisibility(View.GONE);
            etFloorName.setText("");

            reCallAPI();
        }
    }

    public void reCallAPI() {
        Globals g = (Globals) getApplication();
        String req = g.apiUrl + "floor/read?id=" + Globals.areaId;

        try {
            floorViewList.clear(); // Clear the existing list

            List<Floor> floors = new JsonTaskGetFloorList()
                    .executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, req)
                    .get();

            if (floors != null) {
                Collections.sort(floors); // Sort if necessary
                for (Floor p : floors) {
                    ButtonItem newVM = new ButtonItem(p.getName(), 5, p.id);
                    floorViewList.add(newVM); // Add new data
                }
            }

            adapter.notifyDataSetChanged(); // Notify the adapter of the data changes

        } catch (ExecutionException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }


    public void btnLogOut(View v)
    {
        Globals g = (Globals) getApplication();
        g.isLogin = false;

        startActivityForResult(new Intent(getApplicationContext(), LoginActivity.class), 0);
    }
}
