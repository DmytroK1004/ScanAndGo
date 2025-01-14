package com.example.uhf_bt;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.example.uhf_bt.adapter.CommonAdapter;
import com.example.uhf_bt.adapter.ViewHolder;
import com.example.uhf_bt.tool.ExcelUtils;
import com.rscja.deviceapi.RFIDWithUHFBLE;
import com.rscja.deviceapi.entity.UHFTAGInfo;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ExtractDataActivity extends AppCompatActivity implements View.OnClickListener {

    private Button btn_getCount;
    private Button btn_delete;
    private Button btn_upload;
    private Button btn_clearList,btn_R2_export;
    private ListView mListView;;
    private BluetoothAdapter mBtAdapter;
    private MainActivity mContext;
    public ArrayList<HashMap<String, String>> tagList;
    public RFIDWithUHFBLE uhf = RFIDWithUHFBLE.getInstance();

    private static final int REQUEST_ENABLE_BT = 2;
    private static final int REQUEST_SELECT_DEVICE = 1;
    private static final int REQUEST_SELECT_DEVICE2 = 3;

    private static final int UPDATE_PROGRESS = 101;
    private static final int GET_TAG_START = 102;
    private static final int GET_TAG_FINISH = 103;

    private int totalTagCount;

    private Handler mHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {

                case UPDATE_PROGRESS:
                    int currCount = msg.arg1;
                    int totalCount = msg.arg2;
                    if(totalCount > 0 && currCount <= totalCount) {
                        setProgress(currCount, totalCount);
                        updateDataList(mTagList);
                    }
                    break;
                case GET_TAG_START:
                    mDialog.show();
                    break;
                case GET_TAG_FINISH:
                    mDialog.cancel();
                    updateDataList(mTagList);
                    break;
            }
        }
    };

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            default:
                break;
        }

        return super.onOptionsItemSelected(item);
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_extract_data);
        initView();

        //设置顶部菜单栏
        String menuTitle = "";
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
            actionBar.setCustomView(R.layout.title_layout);//设置标题样式
            TextView textView = (TextView) actionBar.getCustomView().findViewById(R.id.display_title);//获取标题布局的textview
            textView.setText(menuTitle + "");//设置标题名称，menuTitle为String字符串
            actionBar.setHomeButtonEnabled(true);//设置左上角的图标是否可以点击
            actionBar.setDisplayHomeAsUpEnabled(true);//给左上角图标的左边加上一个返回的图标
            actionBar.setDisplayShowCustomEnabled(true);// 使自定义的普通View能在title栏显示，即actionBar.setCustomView能起作用
        }
    }

    private void initView() {

        btn_getCount =(Button) findViewById(R.id.btn_getCount);
        btn_getCount.setOnClickListener(this);
        btn_delete = (Button)findViewById(R.id.btn_delete);
        btn_delete.setOnClickListener(this);
        btn_upload =(Button) findViewById(R.id.btn_upload);
        btn_upload.setOnClickListener(this);
        btn_clearList = (Button)findViewById(R.id.btn_clearList);
        btn_clearList.setOnClickListener(this);
        btn_R2_export = findViewById(R.id.btn_R2_export);
        btn_R2_export.setOnClickListener(this);

        mListView = (ListView)findViewById(R.id.listView);

        mBtAdapter = BluetoothAdapter.getDefaultAdapter();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_getCount:
                getCount(true);
                break;
            case R.id.btn_delete:
                if(getCount(false) > 0) {
                    deleteTag();
                } else {
                    showToast(R.string.no_tag_for_delete);
                }
                break;
            case R.id.btn_upload:
                uploadTag();
                break;
            case R.id.btn_clearList:
                clearList();
                break;
            case R.id.btn_R2_export:
                if (mTagList != null && mTagList.size() > 0) {
                    new ExcelTask(this).execute();
                } else {
                    Toast.makeText(this, "fail", Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

    }

    private Toast mToast;
    private void showToast(String text) {
        if(mToast != null) {
            mToast.cancel();
        }
        mToast = Toast.makeText(this, text, Toast.LENGTH_SHORT);
        mToast.show();
    }

    private void showToast(int resId) {
        showToast(getString(resId));
    }

    /**
     * 获取数量
     * @param showToast 是否显示Toast
     * @return
     */
    private int getCount(boolean showToast) {
        int count = uhf.getAllTagTotalFromFlash();
        if(showToast) {
            showToast(String.format(getString(R.string.totalTagCount), count));
        }
        return count;
    }

    /**
     * 删除标签
     */
    private void deleteTag() {
        boolean res = uhf.deleteAllTagToFlash();
        if(res) {
            showToast(R.string.delete_succ);
        } else {
            showToast(R.string.delete_fail);
        }
    }

    /**
     * 设置进度
     * @param progress
     * @param max
     */
    private void setProgress(int progress, int max) {
        tv_progress.setText(progress + "/" + max);
        mProgressBar.setMax(max);
        mProgressBar.setProgress(progress);
    }
    private CommonAdapter<UHFTAGInfo> mAdapter;
    private void updateDataList(List<UHFTAGInfo> infoList) {
        if(mAdapter == null) {
            mAdapter = new CommonAdapter<UHFTAGInfo>(this, infoList, R.layout.item_tag) {
                @Override
                public void convert(ViewHolder helper, UHFTAGInfo item, int position) {
                    helper.setText(R.id.tvIndex, String.valueOf(position + 1));
                    helper.setText(R.id.tvEPC, item.getEPC());

                    List<UHFTAGInfo> epcList = tagInfoMap.get(item.getEPC());
                    helper.setText(R.id.tvCount, String.valueOf(epcList.size()));
                }
            };
            mListView.setAdapter(mAdapter);
        } else {
            mAdapter.updateData(infoList);
        }
    }

    private Map<String, List<UHFTAGInfo>> tagInfoMap = new HashMap<>();
    private List<UHFTAGInfo> mTagList = new ArrayList<>(); // 存每一条数据的EPC都不一样
    private Dialog mDialog;
    private ProgressBar mProgressBar;
    private TextView tv_progress;

    /**
     * 上传标签
     */
    private void uploadTag() {
        initProgressDialog();
        setProgress(0, totalTagCount);
        clearList();
        new Thread() {
            @Override
            public void run() {
                mHandler.sendEmptyMessage(GET_TAG_START);
                totalTagCount = getCount(false); // 总共的数量
                int currCount = 0; // 当前获取数量
                while (true) {
                    List<UHFTAGInfo> tagInfoList = uhf.getTagDataFromFlash();
                    if(tagInfoList == null || tagInfoList.isEmpty()) {
                        break;
                    }
                    currCount += tagInfoList.size();

                    for (UHFTAGInfo tagInfo : tagInfoList) {
                        String epc = tagInfo.getEPC();
                        if(!tagInfoMap.containsKey(epc)) {
                            tagInfoMap.put(epc, new ArrayList<UHFTAGInfo>());
                            mTagList.add(tagInfo);
                        }
                        List<UHFTAGInfo> infoList = tagInfoMap.get(epc);
                        infoList.add(tagInfo);
                    }


                    Message msg = mHandler.obtainMessage();
                    msg.what = UPDATE_PROGRESS;
                    msg.arg1 = currCount;
                    msg.arg2 = totalTagCount;
                    mHandler.sendMessage(msg);
                }
                mHandler.sendEmptyMessage(GET_TAG_FINISH);
            }
        }.start();
    }

    /**
     * 初始化进度条弹窗
     */
    private void initProgressDialog() {
        if(mDialog == null) {
            View contentView = LayoutInflater.from(this).inflate(R.layout.dialog_progress, null);
            mProgressBar = (ProgressBar)contentView.findViewById(R.id.progressBar);
            tv_progress = (TextView)contentView.findViewById(R.id.tv_progress);
            mDialog = new Dialog(ExtractDataActivity.this);
            mDialog.setCancelable(false);
            mDialog.setContentView(contentView);
        }
    }
    /**
     * 清除列表
     */
    private void clearList() {
        tagInfoMap.clear();
        mTagList.clear();
        updateDataList(mTagList);
    }

        public class ExcelTask extends AsyncTask<String, Integer, Boolean> {
        protected ProgressDialog mypDialog;
        protected Activity mContxt;
        boolean isSotp = false;
        String path = "sdcard/uhf" + File.separator + GetTimesyyyymmddhhmmss() + ".xls";
        String txtPath = path.replace("xls","txt");
        public ExcelTask(Activity act) {
            mContxt = act;
        }

        @Override
        protected Boolean doInBackground(String... params) {
            // TODO Auto-generated method stub
            boolean result = false;
            File f=new File("sdcard/uhf");
            if(!f.exists()){
                if(!f.mkdirs()){
                    return false;
                }
            }


            File file = new File(path);
            String[] h = new String[]{"EPC"};//{"EPC", "TID", "COUNT", "RSSI"};
            ExcelUtils excelUtils = new ExcelUtils();
            excelUtils.createExcel(file, h);
            int size = mTagList.size();
            List<String[]> list = new ArrayList<>();
            try {
                BufferedWriter bufferedWriter=new BufferedWriter(new FileWriter(txtPath,true));

                for (int k = 0; !isSotp && k < size; k++) {
                    String epc=  mTagList.get(k).getEPC();
//                    String tid=  tagList.get(k).get(MainActivity.TAG_TID);
//                    String count=  tagList.get(k).get(MainActivity.TAG_COUNT);
//                    String rssi=  tagList.get(k).get(MainActivity.TAG_RSSI);
                    int pro = (int) (div(k + 1, size, 2) * 100);
                    publishProgress(pro);
                    Log.d("TAG", "size:" + mTagList.size() + " k=" + k);
                    String[] data = new String[]{
                            epc,
//                            tid,
//                            count,
//                            rssi,
                    };
                    list.add(data);

                    bufferedWriter.write(epc);
                    bufferedWriter.newLine();
                }


                bufferedWriter.flush();
                bufferedWriter.close();
            }catch (Exception ex){
                Log.e("TAG","ex="+ex.toString());
            }

            publishProgress(101);
            excelUtils.writeToExcel(list);
            notifySystemToScan(file);
            notifySystemToScan(new File(txtPath));
            sleepTime(2000);
            return true;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);
            mypDialog.cancel();
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
            if (values[0] == 101) {
                mypDialog.setMessage("path:" + path);
            } else {
                mypDialog.setProgress(values[0]);
            }
        }

        @Override
        protected void onPreExecute() {
            // TODO Auto-generated method stub
            super.onPreExecute();
            mypDialog = new ProgressDialog(mContxt);
            mypDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            mypDialog.setMessage("...");
            mypDialog.setCanceledOnTouchOutside(false);
            mypDialog.setMax(100);
            mypDialog.setProgress(0);

            mypDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialog) {
                    isSotp = true;
                }
            });

            if (mContxt != null) {
                mypDialog.show();
            }
        }
        public String GetTimesyyyymmddhhmmss() {
            SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
            Date curDate = new Date(System.currentTimeMillis());// 获取当前时间
            String dt = formatter.format(curDate);
            return dt;
        }
        private float div(float v1, float v2, int scale) {
            BigDecimal b1 = new BigDecimal(Float.toString(v1));
            BigDecimal b2 = new BigDecimal(Float.toString(v2));
            return b1.divide(b2, scale, BigDecimal.ROUND_HALF_UP).floatValue();
        }
        public void notifySystemToScan(File file) {
            // mLogUtils.info
            Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
            if (file.exists()) {
                Uri uri = Uri.fromFile(file);
                intent.setData(uri);
                sendBroadcast(intent);
            }
        }
        private void sleepTime(long time) {
            try {
                Thread.sleep(time);
            } catch (Exception ex) {
            }
        }

    }

}