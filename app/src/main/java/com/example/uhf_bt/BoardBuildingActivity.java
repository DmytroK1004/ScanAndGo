package com.example.uhf_bt;

import android.annotation.SuppressLint;
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
import com.example.uhf_bt.dto.Building;
import com.example.uhf_bt.dto.PostArea;
import com.example.uhf_bt.dto.PostBuilding;
import com.example.uhf_bt.dto.PostCategory;
import com.example.uhf_bt.dto.StatusVM;
import com.example.uhf_bt.json.JsonTaskGetAreaList;
import com.example.uhf_bt.json.JsonTaskGetBuildingList;
import com.example.uhf_bt.json.JsonTaskPostItem;
import com.example.uhf_bt.json.JsonTaskUpdateItem;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class BoardBuildingActivity extends BaseActivity{

    public  int buildingId = 0;
    private ListView areaListView;
    private EditText etAreaName;
    private List<ButtonItem> areaViewList = new ArrayList<>();
    private Button btnAddArea;
    private Button btnUpdateArea;
    public int updateAreaId = 0;
    private ListItemView adapter;

    public  TextView tvLocationBuilding;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_board_building);

        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("buildingId")) {
            buildingId = intent.getIntExtra("buildingId", 0);
            Globals.buildingId = buildingId;
        }

        Globals g = (Globals) getApplication();

        etAreaName = findViewById(R.id.etAreaName);
        btnAddArea = findViewById(R.id.btnAddArea);
        btnUpdateArea = findViewById(R.id.btnUpdateArea);
        tvLocationBuilding = findViewById(R.id.tvLocationBuilding);
        tvLocationBuilding.setText(Globals.buildingName);

        btnUpdateArea.setVisibility(View.GONE);

        if (!Globals.isLogin) {
            startActivityForResult(new Intent(getApplicationContext(), LoginActivity.class), 0);
        }

        areaListView = findViewById(R.id.listBuildings);
        adapter = new ListItemView(this, areaViewList, null, null, this, null, null, null, null);
        areaListView.setAdapter(adapter);

        reCallAPI();
    }

    public void UpdateArea(String text, int id)
    {
        btnAddArea.setText(text);
        btnUpdateArea.setVisibility(View.VISIBLE);
        btnAddArea.setText("Cancel");

        updateAreaId = id;
    }

    public void OnUpdateArea(View v)
    {
        if ( updateAreaId > 0 && btnUpdateArea.length() > 0)
        {
            String req = Globals.apiUrl +  "area/update?id=" + updateAreaId;

            PostCategory model = new PostCategory();

            model.name = etAreaName.getText().toString();

            new JsonTaskUpdateItem().execute(req, model.toJsonString());

            updateAreaId = 0;

            btnUpdateArea.setVisibility(View.GONE);
            etAreaName.setText("");

            reCallAPI();
            Toast.makeText(getApplicationContext(), "Updated area successfully", Toast.LENGTH_SHORT).show();
        }
    }

    public void btnLogOut(View v)
    {
        Globals g = (Globals) getApplication();

        g.isLogin = false;

        startActivityForResult(new Intent(getApplicationContext(), LoginActivity.class), 0);
    }

    public void reCallAPI() {
        Globals g = (Globals) getApplication();
        String req = g.apiUrl + "area/read?id=" + Globals.buildingId;

        try {
            List<Area> areaList = new JsonTaskGetAreaList()
                    .executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, req)
                    .get();

            if (areaList != null) {
                areaViewList.clear(); // Clear the existing list
                for (Area p : areaList) {
                    ButtonItem newVM = new ButtonItem(p.name, 4, p.id);
                    areaViewList.add(newVM); // Add new items
                }
            }

            // Notify the adapter about data changes
            adapter.notifyDataSetChanged();

        } catch (ExecutionException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public void OnAddArea(View v)
    {
        if (btnAddArea.getText() == "Add")
        {
            if(etAreaName.length() > 0 )
            {
                try {
                    PostArea model = new PostArea(etAreaName.getText().toString(), Globals.buildingId);
                    StatusVM result = new StatusVM();

                    String req = Globals.apiUrl + "area/create";

                    result = new JsonTaskPostItem().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, req, model.toJsonString()).get();

                    if (result != null) {

                        Toast.makeText(getApplicationContext(), "Added area successfully", Toast.LENGTH_SHORT).show();
                        reCallAPI();
                    } else
                    {
                        Toast.makeText(getApplicationContext(), "Can't save successfully", Toast.LENGTH_SHORT).show();
                    }
                } catch (ExecutionException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        } else {

            btnAddArea.setText("Add");
            btnUpdateArea.setVisibility(View.GONE);
            updateAreaId = 0;
        }
    }
}
