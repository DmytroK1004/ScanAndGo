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
import com.example.uhf_bt.dto.Building;
import com.example.uhf_bt.dto.ButtonItem;
import com.example.uhf_bt.dto.PostBuilding;
import com.example.uhf_bt.dto.PostCategory;
import com.example.uhf_bt.dto.PostQRCode;
import com.example.uhf_bt.dto.QrReturn;
import com.example.uhf_bt.dto.StatusVM;
import com.example.uhf_bt.json.JsonTaskGetBuildingList;
import com.example.uhf_bt.json.JsonTaskLogin;
import com.example.uhf_bt.json.JsonTaskPostItem;
import com.example.uhf_bt.json.JsonTaskQrCode;
import com.example.uhf_bt.json.JsonTaskUpdateItem;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class ShortCutActivity extends BaseActivity{
    EditText shortcut;
    Button btnGo;

    private ListItemView adapter;

    private ListView buildingListView;
    private EditText etBuildingName;
    private List<ButtonItem> buildingViewList = new ArrayList<>();
    private Button btnAddBuilding;
    private Button btnUpdateBuilding;
    public int updateBuildingId = 0;

    public void UpdateBuilding(String text, int id)
    {
        btnAddBuilding.setText(text);
        btnUpdateBuilding.setVisibility(View.VISIBLE);
        btnAddBuilding.setText("Cancel");

        updateBuildingId = id;
    }

    public void OnUpdateBuilding(View v)
    {
        if ( updateBuildingId > 0 && btnAddBuilding.length() > 0)
        {
            String req = Globals.apiUrl +  "building/update?id=" + updateBuildingId;

            PostCategory model = new PostCategory();

            model.name = etBuildingName.getText().toString();

            new JsonTaskUpdateItem().execute(req, model.toJsonString());

            updateBuildingId = 0;

            btnUpdateBuilding.setVisibility(View.GONE);
            etBuildingName.setText("");

            Toast.makeText(getApplicationContext(), "Updated building successfully", Toast.LENGTH_SHORT).show();

            reCallAPI();
        }
    }

    public void OnLogOut(View v)
    {
        Globals g = (Globals) getApplication();

        g.isLogin = false;

        startActivityForResult(new Intent(getApplicationContext(), LoginActivity.class), 0);
    }

    public void reCallAPI() {
        Globals g = (Globals) getApplication();
        String req = g.apiUrl + "building/read";

        try {
            List<Building> buildingList = new JsonTaskGetBuildingList()
                    .executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, req)
                    .get();

            if (buildingList != null) {
                buildingViewList.clear(); // Clear the existing list
                for (Building p : buildingList) {
                    ButtonItem newVM = new ButtonItem(p.name, 3, p.id);
                    buildingViewList.add(newVM); // Add new items
                }
            }

            // Notify the adapter of the changes
            adapter.notifyDataSetChanged();

        } catch (ExecutionException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }


    public void OnAddBuilding(View v)
    {
        if (btnAddBuilding.getText() == "Add")
        {
            if(etBuildingName.length() > 0 )
            {
                try {
                    PostBuilding model = new PostBuilding(etBuildingName.getText().toString());
                    StatusVM result = new StatusVM();

                    String req = Globals.apiUrl + "building/create";

                    result = new JsonTaskPostItem().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, req, model.toJsonString()).get();

                    if (result != null) {

                        reCallAPI();
                        Toast.makeText(getApplicationContext(), "Location added successfully", Toast.LENGTH_SHORT).show();

                    } else {
                        Toast.makeText(getApplicationContext(), "Can't save successfully", Toast.LENGTH_SHORT).show();
                    }
                } catch (ExecutionException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        } else {

            btnAddBuilding.setText("Add");
            btnUpdateBuilding.setVisibility(View.GONE);
            updateBuildingId = 0;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shortcut);

        shortcut = findViewById(R.id.etShortcut);
        etBuildingName = findViewById(R.id.etBuildingName);

        btnGo = findViewById(R.id.goShortCut);
        shortcut.requestFocus();

        btnAddBuilding = findViewById(R.id.btnAddBuilding);
        btnUpdateBuilding = findViewById(R.id.btnUpdateBuilding);
        btnUpdateBuilding.setVisibility(View.GONE);

        buildingListView = findViewById(R.id.listBuildings);
        adapter = new ListItemView(this, buildingViewList, null, null, null, null, null, null, this);
        buildingListView.setAdapter(adapter);

        if (!Globals.isLogin) {
            startActivityForResult(new Intent(getApplicationContext(), LoginActivity.class), 0);
        }

        reCallAPI();
    }


    public void onGoShortCut(View view) {

        PostQRCode model = new PostQRCode();
        model.name = String.valueOf(shortcut.getText());

        String req = Globals.apiUrl + "building/detect-qrcode";

        try {

            QrReturn result = new JsonTaskQrCode().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,
                    req, model.toJsonString()).get();

            if (result.block_id != -1)
            {
                Globals.shortCutName = String.valueOf(shortcut.getText());

                Globals.buildingId = result.building_id;
                Globals.areaId = result.area_id;
                Globals.floorId = result.floor_id;
                Globals.detailLocationId = result.block_id;

                startActivityForResult(new Intent(getApplicationContext(), MainActivity.class), 0);
                Globals.tagsList = new ArrayList<>();

            } else if(result.building_id == -1)
            {
                startActivityForResult(new Intent(getApplicationContext(), BoardBuildingActivity.class), 0);

                showToast("Building doesn't exists");
            } else{

                startActivityForResult(new Intent(getApplicationContext(), BoardBuildingActivity.class), 0);
                showToast("Please input detail location shortcut");
            }

        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
