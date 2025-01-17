package com.example.uhf_bt;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.telecom.Call;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import com.example.uhf_bt.component.ListItemView;
import com.example.uhf_bt.dto.ButtonItem;
import com.example.uhf_bt.dto.LoginVM;
import com.example.uhf_bt.dto.ResponseCheckTag;
import com.example.uhf_bt.dto.PostCheckTags;
import com.example.uhf_bt.dto.UpdateLocation;
import com.example.uhf_bt.json.JsonTaskCheckTag;
import com.example.uhf_bt.json.JsonTaskUpdateLocation;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class CheckActivity extends BaseActivity{

    private List<ButtonItem> rightItemList = new ArrayList<>();
    private List<ButtonItem> wrongItemList = new ArrayList<>();
    private List<ButtonItem> unknownItemList = new ArrayList<>();
    private List<ButtonItem> missingItemList = new ArrayList<>();

    private String[] rightListValues;
    private String[] wrongListValues;
    private String[] unknownListValues;
    private String[] missingListValues;

    private ListView rightListView;
    private ListView wrongListView;
    private ListView unknownListView;
    private ListView missingListView;

    private TextView txtRight;
    private TextView txtWrong;
    private TextView txtMissing;

    private TextView txtUnknown;

    private TextView shortCut;

    public int type = 0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_check);

        rightListView = (ListView)findViewById(R.id.rightItems);
        wrongListView = (ListView)findViewById(R.id.wrongItems);
        unknownListView = (ListView)findViewById(R.id.unknownItems);
        missingListView = (ListView)findViewById(R.id.missingItems);

        txtMissing = (TextView)findViewById(R.id.txtMissingCount);
        txtRight = (TextView)findViewById(R.id.txtRightCount);
        txtUnknown = (TextView)findViewById(R.id.txtUnknownCount);
        txtWrong = (TextView)findViewById(R.id.txtWrongCount);

        shortCut = (TextView)findViewById(R.id.shortCutName);

        shortCut.setText(Globals.shortCutName);
        CallAPI();

        Globals.unknownItems.clear();
    }

    public void onCheckRightLocation(View v)
    {
        if(rightItemList.size() > 0){
            type = 1;

            Intent intent = new Intent(this, CheckItemActivity.class);

            intent.putExtra("type", 7);
            intent.putExtra("barcode", rightListValues);
            this.startActivity(intent);
        }

    }

    public void onCheckWrongLocation(View v)
    {
        if(wrongItemList.size() > 0){
            type = 2;

            Intent intent = new Intent(this, CheckItemActivity.class);

            intent.putExtra("type", 8);
            intent.putExtra("barcode", wrongListValues);
            this.startActivity(intent);
        }
    }

    public void onCheckUnknownLocation(View v)
    {
        if(unknownItemList.size() > 0)
        {
            type = 3;

            Intent intent = new Intent(this, CheckItemActivity.class);

            intent.putExtra("type", 9);
            intent.putExtra("barcode", unknownListValues);
            this.startActivity(intent);
        }
    }

    public void onFixWrongLocation(View v)
    {
        Globals g = (Globals) getApplication();

        String req = g.apiUrl + "location/barcode";

        try {
            UpdateLocation model = new UpdateLocation();

            model.barcode_list = Arrays.asList(wrongListValues);

            model.building_id = Globals.buildingId;
            model.area_id = Globals.areaId;
            model.floor_id = Globals.floorId;
            model.block_id = Globals.detailLocationId;

            LoginVM response = new LoginVM();

            Gson gson = new Gson();
            String jsonString = gson.toJson(model);

            response = new JsonTaskUpdateLocation().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, req, jsonString).get();

            if (response != null) {

                CallAPI();
            }
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public void onCheckMissingLocation(View v)
    {
        if (missingItemList.size() > 0)
        {
            type = 4;

            Intent intent = new Intent(this, CheckItemActivity.class);

            intent.putExtra("type", 10);
            intent.putExtra("barcode", missingListValues);
            this.startActivity(intent);
        }
    }

    public void onAddMissingItems(View v)
    {
        Intent intent = new Intent(this, CheckItemActivity.class);

        String[] missingItems = new String[Globals.unknownItems.size()];

        missingItems = Globals.unknownItems.toArray(missingItems);

        intent.putExtra("type", 11);
        intent.putExtra("barcode", missingItems);
        this.startActivity(intent);
    }
    
    public void CallAPI()
    {
        Globals g = (Globals) getApplication();

        String req = g.apiUrl + "inventory/detect/barcode";

        try {
            PostCheckTags model = new PostCheckTags();

            model.floor_id = Globals.floorId;
            model.barcodes = Globals.tagsList;

            ResponseCheckTag response = new ResponseCheckTag();

            response = new JsonTaskCheckTag().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, req, model.toJsonString()).get();

            if (response != null) {

                wrongItemList.clear();
                rightItemList.clear();
                missingItemList.clear();
                unknownItemList.clear();

                for (String p : response.rightLists) {
                    rightListValues = response.rightLists.toArray(new String[0]);
                    ButtonItem newVM = new ButtonItem(p, 7, 0);
                    rightItemList.add(newVM);
                }

                for (String p : response.wrongLists) {
                    wrongListValues = response.wrongLists.toArray(new String[0]);
                    ButtonItem newVM = new ButtonItem(p, 8, 0);
                    wrongItemList.add(newVM);
                }

                for (String p : response.missingLists) {
                    missingListValues = response.missingLists.toArray(new String[0]);
                    ButtonItem newVM = new ButtonItem(p, 9, 0);
                    missingItemList.add(newVM);
                }

                for (String p : response.unknownLists) {
                    unknownListValues = response.unknownLists.toArray(new String[0]);
                    ButtonItem newVM = new ButtonItem(p, 10, 0);
                    unknownItemList.add(newVM);
                }
            }
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        ListItemView rightAdapter = new ListItemView(this, rightItemList, null, null, null, null, null, null, null);
        // Set the adapter for the ListView
        rightListView.setAdapter(rightAdapter);

        ListItemView wrongAdapter = new ListItemView(this, wrongItemList, null, null, null, null, null, null, null);
        // Set the adapter for the ListView
        wrongListView.setAdapter(wrongAdapter);

        ListItemView missingAdapter = new ListItemView(this, missingItemList, null, null, null, null, null, null, null);
        // Set the adapter for the ListView
        missingListView.setAdapter(missingAdapter);

        ListItemView unknownAdapter = new ListItemView(this, unknownItemList, null, null, null, null, null, null, null);
        // Set the adapter for the ListView
        unknownListView.setAdapter(unknownAdapter);

        txtWrong.setText(wrongItemList.size() + " tags");
        txtRight.setText(rightItemList.size() + " tags");
        txtUnknown.setText(unknownItemList.size() + " tags");
        txtMissing.setText(missingItemList.size() + " tags");
    }
}
