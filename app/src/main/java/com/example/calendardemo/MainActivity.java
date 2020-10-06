package com.example.calendardemo;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.example.calendardemo.adapter.ContentAdapter;
import com.example.calendardemo.calendar.CalendarConstantData;
import com.example.calendardemo.calendar.SystemCalendarHandler;

import com.example.calendardemo.view.DividerListItemDecoration;
import com.hb.dialog.myDialog.MyAlertInputDialog;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import butterknife.BindView;


public class MainActivity extends BaseActivity {

    @BindView(R.id.recycler_view)
    RecyclerView recyclerView;

    ContentAdapter mContentAdapter;
    List<String> mTestList = new ArrayList<>();

    private MyAlertInputDialog myAlertInputDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        checkNeedPermissions();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initData();
    }

    private void initData() {
        mTestList.add("添加行程");
        mTestList.add("删除行程");
        mTestList.add("查询行程");

        mContentAdapter = new ContentAdapter(mTestList);
        recyclerView.setAdapter(mContentAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.addItemDecoration(new DividerListItemDecoration(5, DividerListItemDecoration.VERTICAL_LIST));
        mContentAdapter.setOnItemChildClickListener(new BaseQuickAdapter.OnItemChildClickListener() {
            @Override
            public void onItemChildClick(BaseQuickAdapter adapter, View view, int position) {
//                Toast.makeText(MainActivity.this, "" + position, Toast.LENGTH_SHORT).show();
            }
        });
        mContentAdapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(BaseQuickAdapter adapter, View view, int position) {
                String actionString = mTestList.get(position);
                Log.d(TAG, "mContentAdapter onItemClick action = " + actionString);
                Toast.makeText(MainActivity.this, "" + actionString, Toast.LENGTH_SHORT).show();
//                SystemCalendarHandler.getCalendarInfo(view.getContext());
                long calId = SystemCalendarHandler.checkAndAddCalendarAccounts(view.getContext()); //获取日历账户的id
                if (calId < 0) { //获取账户id失败直接返回，添加日历事件失败
                    return;
                }
                if ("添加行程".equals(actionString)) {
                    InsertDialog();
                } else if ("删除行程".equals(actionString)) {
                    showDeleteCalendarInputDialog();
                } else if ("查询行程".equals(actionString)) {
                    startCOActivity(CalendarInfoActivity.class);
                }
            }
        });
    }

    //新增对话框
    public void InsertDialog() {
        final ConstraintLayout linearLayout = (ConstraintLayout) getLayoutInflater().inflate(R.layout.insert_alarm, null);

        new AlertDialog.Builder(this)
                .setTitle("增加课程提醒")//标题
                .setView(linearLayout)//设置视图
                //确定按钮及其动作
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        String strYear = ((EditText) linearLayout.findViewById(R.id.year)).getText().toString();
                        String strMonth = ((EditText) linearLayout.findViewById(R.id.month)).getText().toString();
                        String strDay = ((EditText) linearLayout.findViewById(R.id.day)).getText().toString();
                        String strHour = ((EditText) linearLayout.findViewById(R.id.hour)).getText().toString();
                        String strMinute = ((EditText) linearLayout.findViewById(R.id.minute)).getText().toString();
                        String strLecture = ((EditText) linearLayout.findViewById(R.id.lecture)).getText().toString();
                        String strLocation = ((EditText) linearLayout.findViewById(R.id.location)).getText().toString();


                        String time = strYear+"-"+strMonth+"-"+strDay+" "+strHour+":"+strMinute+":00";

                        new MyThread(time,strLecture,strLocation).start();
                    }
                })
                //取消按钮及其动作
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {           }
                })
                .create()//创建对话框
                .show();//显示对话框

    }
    private void showDeleteCalendarInputDialog() {
        if (myAlertInputDialog == null) {
            myAlertInputDialog = new MyAlertInputDialog(MainActivity.this).builder()
                    .setTitle("请输入")
                    .setEditText("请输入要删除的行程id");
            myAlertInputDialog.setPositiveButton("确认", new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String inputString = myAlertInputDialog.getContentEditText().getText().toString();
                    Log.d(TAG, "showDeleteCalendarInputDialog  onClick inputString = " + inputString);
                    SystemCalendarHandler.deleteCalendarEvent(MainActivity.this, inputString);
                    myAlertInputDialog.dismiss();
                }
            }).setNegativeButton("取消", new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    myAlertInputDialog.dismiss();
                }
            });
        }
        myAlertInputDialog.getContentEditText().setText("");
        myAlertInputDialog.show();
    }

    private void checkNeedPermissions(){
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CALENDAR)
                != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_CALENDAR)
                != PackageManager.PERMISSION_GRANTED) {
            //多个权限一起申请
            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.READ_CALENDAR,
                    Manifest.permission.WRITE_CALENDAR
            }, 1);
        }
    }
    public class MyThread extends Thread {
        String time;
        String lecture;
        String location;
        public MyThread(String time,String lecture, String location){
            this.time = time;
            this.lecture = lecture;
            this.location = location;
            run(time, lecture, location);
        }

        //继承Thread类，并改写其run方法

        public void run(String dateTime, String lecture, String location) {
            Log.d(TAG, "run");
//            String dateTime = "2020-10-6 00:00:00";
            Calendar calendar = Calendar.getInstance();
            try {
                calendar.setTime(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(dateTime));
            } catch (ParseException e) {
                e.printStackTrace();
            }
            System.out.println("日期"+dateTime+" 对应毫秒：" + calendar.getTimeInMillis());

            long startTime = calendar.getTimeInMillis()/1000;
            long endTime = startTime;
            Log.v("calendar_init_test",startTime+"  "+endTime);
            long calendarId = SystemCalendarHandler.checkCalendarAccounts(MainActivity.this);
            SystemCalendarHandler.insertCalendarEvent(MainActivity.this, calendarId, lecture,
                    location, startTime, endTime, 0, CalendarConstantData.CALENDAR_EVENT_REPEAT_NEVER,
                    "", null, 0);
//            CalendarTest.testInsertCalendarEvent();
        }
    }

}
