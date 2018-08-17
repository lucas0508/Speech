package com.example.administrator.speech;

import android.Manifest;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.*;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import com.example.administrator.speech.gen.DBuserinfo;
import com.example.administrator.speech.gen.DBuserinfoDao;
import com.example.administrator.speech.java.AudioMaker;

import java.io.File;
import java.util.List;

public class MainActivity extends AppCompatActivity {


    Button button, button2, button3, button4;
    TextView textView;
    RecyclerView rcy;
    private String TAG = MainActivity.class.getSimpleName();
    private MediaRecorder recorder;
    private File audioFile;
    private Uri fileUri;
    private MediaPlayer player;
    NormalRecyclerViewAdapter recyclerViewAdapter;
    private DBuserinfoDao dBuserinfoDao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        rcy = findViewById(R.id.rcy);


        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, PERMISSIONS_STORAGE, REQUEST_PERMISSION_CODE);
            }
        }

        String path = Environment.getExternalStorageDirectory().toString() + "/data/files/Airport20180706-I.txt";

        AudioMaker maker = new AudioMaker(path, false, 1, true);


        dBuserinfoDao = MyApplication.getInstances().getDaoSession().getDBuserinfoDao();




        dBuserinfoDao.deleteAll();

        maker.makeAudios();
        List<DBuserinfo> users = maker.getUsers();

        Log.d(TAG, "原数据-----:" + users.size());

        try {

        }catch (Exception e){
            e.printStackTrace();
        }


        for (DBuserinfo alls : users) {
            DBuserinfo dBuserinfo = new DBuserinfo();

            dBuserinfo.setIsRecoder(alls.getIsRecoder());

            dBuserinfo.setAudio(alls.getAudio());

            dBuserinfo.setAudioText(alls.getAudioText());

            dBuserinfoDao.insert(dBuserinfo);
        }

        List<DBuserinfo> dBusers = dBuserinfoDao.loadAll();

        Log.d(TAG, "数据库数据-----:" + dBusers.size());




        //   String theme = maker.getTheme();


//        Log.e(TAG,"名字:"+theme);
//        Log.e(TAG,"数据:"+new Gson().toJson(users));


//      rcy.setLayoutManager(new LinearLayoutManager(this));
//      rcy.setLayoutManager(new StaggeredGridLayoutManager(2, OrientationHelper.VERTICAL));
        rcy.setLayoutManager(new GridLayoutManager(this, 2));
        recyclerViewAdapter = new NormalRecyclerViewAdapter(this, dBusers);
        rcy.setAdapter(recyclerViewAdapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        List<DBuserinfo> dBusers = dBuserinfoDao.loadAll();
         recyclerViewAdapter.setData(dBusers);

//        recyclerViewAdapter = new NormalRecyclerViewAdapter(this, all);
//        rcy.setAdapter(recyclerViewAdapter);
        Log.e(TAG, "onResume");
        //  Log.e(TAG,"数据 IsRecorder----:"+ all.get(214).getIsRecoder());
    }

    private static int REQUEST_PERMISSION_CODE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE};

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_PERMISSION_CODE) {
            for (int i = 0; i < permissions.length; i++) {
                Log.i("MainActivity", "申请的权限为：" + permissions[i] + ",申请结果：" + grantResults[i]);
            }
        }
    }

 /*   @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.button:
                start();

                break;
            case R.id.button2:
                textView.setText("录制停止...");
                if (recorder!=null){


                recorder.stop();
                recorder.release();
                //然后我们可以将我们的录制文件存储到MediaStore中
                ContentValues values = new ContentValues();
                values.put(MediaStore.Audio.Media.TITLE, "this is my first record-audio");
                values.put(MediaStore.Audio.Media.DATE_ADDED, System.currentTimeMillis());
                values.put(MediaStore.Audio.Media.DATA, audioFile.getAbsolutePath());
               // fileUri = this.getContentResolver().insert(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, values);
                }else {
                    textView.setText("没有可停止的音频...");
                }
                break;
            case R.id.button3:
                player = new MediaPlayer();
//                player.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
//                    @Override
//                    public void onCompletion(MediaPlayer mediaPlayer) {
//                        //更新状态
//                        textView.setText("准备录制");
////                btnPlay.setEnabled(true);
////                btnStart.setEnabled(true);
////                btnStop.setEnabled(false);
//                    }
//                });
                //准备播放
                if (audioFile ==null || !audioFile.exists()) {
                    textView.setText("请先录制...");
                }else {


                Log.e("路径:", audioFile.getAbsolutePath() + "");
                textView.setText("正在播放...");
                Uri uri = Uri.parse(audioFile.getAbsolutePath());
                try {
                    player.reset();
                    player.setDataSource(MainActivity.this, uri);
                    player.prepare();
                    player.start();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                }
                break;
            case R.id.button4:
                if (audioFile ==null || !audioFile.exists()) {
                    textView.setText("没有可删除的音频...");
                }else {
                    audioFile.delete();
                    Log.e(TAG, "点击了4---->删除" + audioFile);
                    textView.setText("删除刚录制的音频...");
                }
                *//*SdCard中获取资源*//*
//                String path = Environment.getExternalStorageDirectory().toString() + "/data/files/" + "recording-1583867412.mp3";
//                Uri uri1 = Uri.parse(path);
//               MediaPlayer mediaPlayer;
//                mediaPlayer = new MediaPlayer();
//                try {
//                    mediaPlayer.reset();
//                    mediaPlayer.setDataSource(MainActivity.this,uri1);
//                    mediaPlayer.prepare();
//                    mediaPlayer.start();
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
                break;
        }
    }

    private void start() {
        textView.setText("开始录制...");
        recorder = new MediaRecorder();
        //指定AudioSource 为MIC(Microphone audio source ),这是最长用的
        recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
//指定OutputFormat,我们选择3gp格式
//其他格式，MPEG-4:这将指定录制的文件为mpeg-4格式，可以保护Audio和Video
//RAW_AMR:录制原始文件，这只支持音频录制，同时要求音频编码为AMR_NB
//THREE_GPP:录制后文件是一个3gp文件，支持音频和视频录制
        recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
//指定Audio编码方式，目前只有AMR_NB格式
        recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
//接下来我们需要指定录制后文件的存储路径
        File fpath = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/data/files/");
        Log.e("fath", "" + fpath);
        fpath.mkdirs();//创建文件夹
        try {
//创建临时文件
            audioFile = File.createTempFile("recording", ".mp3", fpath);
        } catch (IOException e) {
// TODO Auto-generated catch block
            e.printStackTrace();
        }
        recorder.setOutputFile(audioFile.getAbsolutePath());

//下面就开始录制了
        try {
            recorder.prepare();
        } catch (IllegalStateException e) {
// TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
// TODO Auto-generated catch block
            e.printStackTrace();
        }

        recorder.start();

        textView.setText("正在录制");
    }
*/

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
