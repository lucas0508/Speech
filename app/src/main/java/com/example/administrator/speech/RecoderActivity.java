package com.example.administrator.speech;

import android.content.ContentValues;
import android.content.Intent;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import com.example.administrator.speech.gen.DBuserinfo;
import com.example.administrator.speech.gen.DBuserinfoDao;
import org.greenrobot.greendao.annotation.Id;

import java.io.File;
import java.io.IOException;

public class RecoderActivity extends AppCompatActivity implements View.OnClickListener {


    Button button, button2, button3, button4;
    TextView textView, textView2;
    private String TAG = "MainActivity";
    private MediaRecorder recorder;
    private File audioFile;
    private Uri fileUri;
    private MediaPlayer player;
    private String audioText;
    private String audio;
    private int isRecoder;
    private Long id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recoder);
        button = findViewById(R.id.button);
        button2 = findViewById(R.id.button2);
        button3 = findViewById(R.id.button3);
        button4 = findViewById(R.id.button4);
        textView = findViewById(R.id.textView);
        textView2 = findViewById(R.id.textView2);

        Intent intent = getIntent();
        audioText = intent.getStringExtra("audioText");
        audio = intent.getStringExtra("audio");
        int pos = intent.getIntExtra("pos", -1);
        isRecoder = intent.getIntExtra("isRecoder", -1);
        id = intent.getLongExtra("id",-1);

        Log.e(TAG, "pos:" + pos);
        if (!" ".equals(audioText)) {
            textView2.setText(audioText);
        } else {
            textView2.setText("空音频");
        }
        Log.e(TAG, "onCreate: audio" + audio);


       // updateRecoder(isRecoder, id);


        button.setOnClickListener(this);
        button2.setOnClickListener(this);
        button3.setOnClickListener(this);
        button4.setOnClickListener(this);

    }

    /**
     *
     * @param isAddOrDelte 1 增加  0 删除
     * @param id
     */
    private void updateRecoder(int isAddOrDelte, Long id) {
        DBuserinfoDao dBuserinfoDao = MyApplication.getInstances().getDaoSession().getDBuserinfoDao();


        DBuserinfo unique = dBuserinfoDao.queryBuilder().where(DBuserinfoDao.Properties.Id.eq(id)).build().unique();
        Log.e(TAG, "onCreate: 修改之前" + isRecoder);
        if (unique != null){
            unique.setIsRecoder(isAddOrDelte);
            dBuserinfoDao.update(unique);
            Log.e(TAG, "onCreate: 修改" + "success");
        }else {
            Log.e(TAG, "onCreate: 修改" + "error");
        }
        Log.e(TAG, "onCreate: 修改后" + unique.getIsRecoder());
    }


    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.button:
                start();

                break;
            case R.id.button2:
                textView.setText("录制停止...");
                if (recorder != null) {


                    recorder.stop();
                    recorder.release();
                    //然后我们可以将我们的录制文件存储到MediaStore中
                    ContentValues values = new ContentValues();
                    values.put(MediaStore.Audio.Media.TITLE, "this is my first record-audio");
                    values.put(MediaStore.Audio.Media.DATE_ADDED, System.currentTimeMillis());
                    values.put(MediaStore.Audio.Media.DATA, audioFile.getAbsolutePath());

                    updateRecoder(1, id);
                    // fileUri = this.getContentResolver().insert(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, values);
                } else {
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
//                if (audioFile == null || !audioFile.exists()) {
//                    textView.setText("请先录制...");
//                } else {
                File fpath = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/data/files/");

                if (fpath.exists()) {
                    File pathfile = new File(fpath + "/" + audio + ".mp3");
                    Log.e(TAG, "重播: pathfile" + pathfile.getAbsolutePath());
                    try {
                        Log.e(TAG, "重播: pathfile" + pathfile.exists());
                        if (pathfile.exists()) {

                            Log.e("路径:", pathfile.getAbsolutePath() + "");
                            textView.setText("正在播放...");
                            Uri uri = Uri.parse(pathfile.getAbsolutePath());
                            try {
                                player.reset();
                                player.setDataSource(RecoderActivity.this, uri);
                                player.prepare();
                                player.start();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        } else {
                            textView.setText("音频不存在，请录制...");
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

//                    }
                }
                break;
            case R.id.button4:
                if (audioFile == null || !audioFile.exists()) {
                    textView.setText("没有可删除的音频...");
                } else {
                    audioFile.delete();
                    updateRecoder(0, id);
                    Log.e(TAG, "点击了4---->删除" + audioFile);
                    textView.setText("删除刚录制的音频...");
                }
                /*SdCard中获取资源*/
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
        if (!fpath.exists())
            fpath.mkdirs();//创建文件夹
        try {
//创建临时文件
            Log.e(TAG, "  audio  :" + audio);
            audioFile = new File(fpath + "/" + audio + ".mp3");
            if (!audioFile.exists()) {
                audioFile.createNewFile();
            } else {
                new Exception("The new file already exists!");
                audioFile.delete();
                audioFile.createNewFile();
            }
            recorder.setOutputFile(audioFile.getAbsolutePath());
        } catch (IOException e) {
// TODO Auto-generated catch block
            e.printStackTrace();
        }


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


    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
