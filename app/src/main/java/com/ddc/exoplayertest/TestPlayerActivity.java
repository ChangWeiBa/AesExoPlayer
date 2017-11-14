package com.ddc.exoplayertest;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.media.session.PlaybackState;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.ddc.exoplayertest.permission.OnPermissionCallback;
import com.ddc.exoplayertest.permission.PermissionManager;
import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.PlaybackParameters;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
import com.google.android.exoplayer2.extractor.ExtractorsFactory;
import com.google.android.exoplayer2.source.ConcatenatingMediaSource;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelection;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;
import com.google.android.exoplayer2.trackselection.TrackSelector;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.FileDataSource;
import com.google.android.exoplayer2.util.Util;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import butterknife.BindView;
import butterknife.ButterKnife;
import timber.log.Timber;

/**
 * 音频播放测试demo,主要用于直接解密播放AES加密的音频流，利用官方类自定义了DataSource
 */
public class TestPlayerActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "TestPlayerActivity";
    @BindView(R.id.start_play)
    ImageView startPlay;
    @BindView(R.id.already_player)
    TextView alreadyPlayer;
    @BindView(R.id.seek_bar)
    SeekBar seekBar;
    @BindView(R.id.all_player_time)
    TextView allPlayerTime;
    @BindView(R.id.play_linear)
    LinearLayout playLinear;
    @BindView(R.id.bt_encrypt)
    Button btEncrypt;
    @BindView(R.id.bt_decrypt)
    Button btDecrypt;
    @BindView(R.id.bt_encryplay)
    Button btEncryplay;
    private ExtractorMediaSource videoSource;
    private String url = "https://storage.googleapis.com/exoplayer-test-media-0/play.mp3";

    private static final int UP_TIME = 0;//更新播放时间
    private boolean screenFlag = false;//全屏标记
    private SimpleExoPlayer player;
    private boolean playFlag = false;//播放状态
    private ExecutorService executors = Executors.newSingleThreadExecutor();
    private final MyHandler myHandler = new MyHandler(this);
    Uri playerUri = Uri.parse("https://storage.googleapis.com/exoplayer-test-media-0/play.mp3");
    private ExtractorsFactory extractorsFactory;
    private Timeline.Window window;
    private int rewindMs = 2;//快退
    private int fastForwardMs = 2;//快进
    private AitripDataSourceFactory aitripFactory;
    private String Aikey = "1231231241241243";
    private static final String deng  = "/sdcard/Music/终于等到你.mp3";

    private static String FildDir = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator +
            "ExoPlayerTest";


    public static void start(Context context) {
        Intent starter = new Intent(context, TestPlayerActivity.class);
        context.startActivity(starter);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_player);
        ButterKnife.bind(this);
        btDecrypt.setOnClickListener(this);
        btEncrypt.setOnClickListener(this);
        allPlayerTime.setOnClickListener(this);
        startPlay.setOnClickListener(this);
        btEncryplay.setOnClickListener(this);

        if (BuildConfig.DEBUG) {
            Timber.plant(new Timber.DebugTree());
        }


        initData();
        initPlayer(deng);


    }

    protected void initData() {
        PermissionManager.instance()
                .with(this)
                .request(new OnPermissionCallback() {
                             @Override
                             public void onRequestAllow(String permissionName) {
                             }

                             @Override
                             public void onRequestRefuse(String permissionName) {
                             }

                             @Override
                             public void onRequestNoAsk(String permissionName) {
                                 Timber.e("不在询问了");
                             }
                         },
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE);
    }

    private void initPlayer(String url) {
        // 1.创建一个默认TrackSelector,测量播放过程中的带宽。 如果不需要，可以为null。
        DefaultBandwidthMeter bandwidthMeter = new DefaultBandwidthMeter();
        //从MediaSource中选出media提供给可用的Render S来渲染,在创建播放器时被注入
        TrackSelection.Factory videoTrackSelectionFactory =
                new AdaptiveTrackSelection.Factory(bandwidthMeter);
        TrackSelector trackSelector =
                new DefaultTrackSelector(videoTrackSelectionFactory);
        // 2.创建一个默认的LoadControl
        //Create a default LoadControl 控制MediaSource缓存media
        DefaultLoadControl loadControl = new DefaultLoadControl();
//        BufferingLoadControl bufferingLoadControl = new BufferingLoadControl();
        //生成加载媒体数据的DataSource实例。
//        dataSourceFactory = new DefaultDataSourceFactory(this,
//                Util.getUserAgent(this, "aitrip"), bandwidthMeter);
        //自定义解密工厂
        aitripFactory = new AitripDataSourceFactory(this,
                Util.getUserAgent(this, "aitrip"), bandwidthMeter);
        extractorsFactory = new DefaultExtractorsFactory();
//        factory = new DiyExtractorsFactory();
        FileDataSource fileDataSource = new FileDataSource();
        //test mp3
//        url = "https://storage.googleapis.com/exoplayer-test-media-0/play.mp3";
        // MediaSource代表要播放的媒体。
        videoSource = new ExtractorMediaSource(Uri.parse(url),
                aitripFactory, extractorsFactory, null, null);
        // 3.创建播放器
        player = ExoPlayerFactory.newSimpleInstance(this, trackSelector, loadControl);

        //SimpleExoPlayerView simpleExoPlayerView = (SimpleExoPlayerView) findViewById(R.id.exo_player);
        //设置监听器
        player.addListener(eventListener);
        //装载多个资源
        MediaSource[] mediaSources = new MediaSource[1];
//        mediaSources[0] = buildMediaSource(Uri.parse(huai), "");
//        mediaSources[1] = buildMediaSource(Uri.parse(deng), "");
        mediaSources[0] = buildMediaSource(Uri.parse(deng), "");
//        mediaSources[2] = buildMediaSource(playerUri, "");
//        mediaSources[3] = buildMediaSource(Uri.parse(deng), "");
        ConcatenatingMediaSource mediaSource = new ConcatenatingMediaSource(mediaSources);

        //设置资源
        player.prepare(mediaSource);
        window = new Timeline.Window();
    }

    //播放事件监听
    private ExoPlayer.EventListener eventListener = new ExoPlayer.EventListener() {
        @Override
        public void onTimelineChanged(Timeline timeline, Object manifest) {
            Timber.e("播放: onTimelineChanged 周期总数 " + timeline);
        }

        @Override
        public void onTracksChanged(TrackGroupArray trackGroups, TrackSelectionArray trackSelections) {
            Timber.e("播放: TrackGroupArray  ");
        }

        @Override
        public void onLoadingChanged(boolean isLoading) {
            Timber.e("播放: onLoadingChanged  " + player.getBufferedPosition());
            //设置二级进度条
            seekBar.setSecondaryProgress((int) (player.getBufferedPosition() / 1000));
        }

        @Override
        public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
            Timber.e("onPlayerStateChanged: playWhenReady = " + String.valueOf(playWhenReady)
                    + " playbackState = " + playbackState);
            switch (playbackState) {
                case PlaybackState.STATE_PLAYING:
                    //初始化播放点击事件并设置总时长
                    initPlayVideo();
                    Timber.e("播放状态: 准备 playing");
                    break;

                case PlaybackState.STATE_BUFFERING:
                    Timber.e("播放状态: 缓存完成 playing");
                    break;

                case PlaybackState.STATE_CONNECTING:
                    Timber.e("播放状态: 连接 CONNECTING");
                    break;

                case PlaybackState.STATE_ERROR://错误
                    Timber.e("播放状态: 错误 STATE_ERROR");
                    startPlay.setBackgroundColor(Color.WHITE);
                    ToastUtil.showShort("播放错误");
                    break;

                case PlaybackState.STATE_FAST_FORWARDING:
                    Timber.e("播放状态: 快速传递,播放完毕");

                    pausePlay();//暂停播放
                    break;

                case PlaybackState.STATE_NONE:
                    Timber.e("播放状态: 无 STATE_NONE");
                    break;

                case PlaybackState.STATE_PAUSED:
                    Timber.e("播放状态: 暂停 PAUSED");
                    break;

                case PlaybackState.STATE_REWINDING:
                    Timber.e("播放状态: 倒回 REWINDING");
                    break;

                case PlaybackState.STATE_SKIPPING_TO_NEXT:
                    Timber.e("播放状态: 跳到下一个");
                    break;

                case PlaybackState.STATE_SKIPPING_TO_PREVIOUS:
                    Timber.e("播放状态: 跳到上一个");
                    break;

                case PlaybackState.STATE_SKIPPING_TO_QUEUE_ITEM:
                    Timber.e("播放状态: 跳到指定的Item");
                    break;

                case PlaybackState.STATE_STOPPED:
                    Timber.e("播放状态: 停止的 STATE_STOPPED");
                    break;


            }
        }

        @Override
        public void onRepeatModeChanged(int repeatMode) {
            Timber.e("播放: onRepeatModeChanged  ");
        }

        @Override
        public void onPlayerError(ExoPlaybackException error) {
            Timber.e("播放: onPlayerError  ");
        }

        @Override
        public void onPositionDiscontinuity() {
            Timber.e("播放: onPositionDiscontinuity  ");
        }

        @Override
        public void onPlaybackParametersChanged(PlaybackParameters playbackParameters) {
            Timber.e("播放: onPlaybackParametersChanged  ");
        }
    };

    //初始化播放事件
    private void initPlayVideo() {
        allPlayerTime.setText(TimeUtils.secToTime((int) (player.getDuration() / 1000)));
        //设置总时长
        seekBar.setMax((int) (player.getDuration() / 100));
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                //是暂停的开始播放
                if (!player.getPlayWhenReady()) {
                    continuePlay();//继续播放
                }
                player.seekTo(seekBar.getProgress() * 1000);
            }
        });

        //播放按钮
        startPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (player.getPlayWhenReady()) {
                    pausePlay();//暂停播放
                } else {
                    continuePlay();//继续播放
                }
            }
        });

    }


    //暂停播放
    private void pausePlay() {
        player.setPlayWhenReady(false);
        startPlay.setBackgroundColor(Color.WHITE);
        playFlag = false;
    }

    //继续播放
    private void continuePlay() {
        player.setPlayWhenReady(true);
        startPlay.setBackgroundColor(Color.RED);
        //开始读取进度
        playFlag = true;
        executors.execute(runnable);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.bt_encrypt://加密
                AESHelper.encryptFile(Aikey, deng, FildDir + "/" + "终于等到你.mp3.aitrip");
                break;

            case R.id.bt_encryplay://解密播放,自定义路径
                reLoadSourcePlay(FildDir + "/" + "终于等到你.mp3.aitrip");
                break;

            case R.id.already_player://播放进度时间，点击上一首
                previous();

                break;
            case R.id.all_player_time://播放总时间，点击进入下一首
                next();
                break;

        }
    }

    private static final long MAX_POSITION_FOR_SEEK_TO_PREVIOUS = 3000;

    /**
     * 上一首
     */
    private void previous() {
        Timeline timeline = player.getCurrentTimeline();
        if (timeline.isEmpty()) {
            return;
        }
        int windowIndex = player.getCurrentWindowIndex();
        timeline.getWindow(windowIndex, window);
        int previousWindowIndex = timeline.getPreviousWindowIndex(windowIndex, player.getRepeatMode());
        Timber.e("previousWindowIndex:" + previousWindowIndex);
        Timber.e("getCurrentPosition:" + player.getCurrentPosition());
        Timber.e("isDynamic:" + window.isDynamic);
        Timber.e("isSeekable:" + window.isSeekable);
        Timber.e("TIME_UNSET:" + C.TIME_UNSET);
        Timber.e("TIME_UNSET:" + C.TIME_UNSET);
        if (previousWindowIndex != C.INDEX_UNSET) {
            player.seekTo(previousWindowIndex, C.TIME_UNSET);
        } else {
            Timber.e("seekTo（0）:");
            Timber.e("已经是第一首");
//            player.seekTo(0);
        }
    }


    /**
     * 下一首
     */
    private void next() {
        Timeline timeline = player.getCurrentTimeline();
        if (timeline.isEmpty()) {
            return;
        }

        int windowIndex = player.getCurrentWindowIndex();
        Timber.e("windowIndex:" + windowIndex);
        int nextWindowIndex = timeline.getNextWindowIndex(windowIndex, player.getRepeatMode());
        Timber.e("nextWindowIndex:" + nextWindowIndex);
        Timber.e("isDynamic:" + window.isDynamic);
        Timber.e("TIME_UNSET:" + C.TIME_UNSET);
        if (nextWindowIndex != C.INDEX_UNSET) {
            player.seekTo(nextWindowIndex, C.TIME_UNSET);
        } else if (timeline.getWindow(windowIndex, window, false).isDynamic) {
            player.seekTo(windowIndex, C.TIME_UNSET);
            Timber.e("已经最后一首");

        }
    }

    private MediaSource buildMediaSource(Uri uri, String overrideExtension) {
        return new ExtractorMediaSource(uri, aitripFactory, extractorsFactory,
                null, null);
    }

    /**
     * 快退
     */
    private void rewind() {
        if (rewindMs <= 0) {
            return;
        }
        player.seekTo(Math.max(player.getCurrentPosition() - rewindMs, 0));
    }

    /**
     * 快进
     */
    private void fastForward() {
        if (fastForwardMs <= 0) {
            return;
        }
        long durationMs = player.getDuration();
        long seekPositionMs = player.getCurrentPosition() + fastForwardMs;
        if (durationMs != C.TIME_UNSET) {
            seekPositionMs = Math.min(seekPositionMs, durationMs);
        }
        player.seekTo(seekPositionMs);
    }

    /**
     * 重载资源
     *
     * @param url
     */
    private void reLoadSourcePlay(String url) {
        Timber.e("重载资源file1---:" + url);
        //重载资源
        videoSource.releaseSource();
        seekBar.setMax(0);
        seekBar.setProgress(0);
        allPlayerTime.setText("00:00");
        ExtractorMediaSource extractorMediaSource =
                new ExtractorMediaSource(Uri.parse(url), aitripFactory,
                        extractorsFactory, null, null);
        player.prepare(extractorMediaSource);
        //设置文字
//        allPlayerTime.setText(TimeUtils.secToTime((int) (player.getDuration() / 1000)));
        //设置总时长
//        seekBar.setMax((int) (player.getDuration() / 100));
        continuePlay();//开始播放
    }


    private static class MyHandler extends Handler {
        private final WeakReference<TestPlayerActivity> mActivity;

        private MyHandler(TestPlayerActivity mActivity) {
            this.mActivity = new WeakReference<>(mActivity);
        }

        @Override
        public void handleMessage(Message msg) {
            if (mActivity.get() == null) {
                return;
            }
            if (msg.what == UP_TIME) {
                //设置播放进度
                TestPlayerActivity videoActivity = mActivity.get();
                videoActivity.seekBar.setProgress(msg.arg2);

            }
        }
    }

    //开启线程读取进度
    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            while (playFlag) {
                Message msg = new Message();
                msg.what = UP_TIME;
                //获取播放时间
                msg.arg1 = (int) (player.getCurrentPosition() / 1000);
                msg.arg2 = (int) (player.getCurrentPosition() / 100);
//                Timber.e("player---:"+player.getCurrentPosition());
//                Timber.e("arg1-----:"+msg.arg1);
//                Timber.e("arg222---:"+msg.arg2);
                myHandler.sendMessage(msg);
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    };


    @Override
    protected void onDestroy() {
        super.onDestroy();
        //释放资源
        if (player != null) {
            playFlag = false;//停止线程
            executors.shutdown();
            player.stop();
            player.release();
            player = null;
            videoSource.releaseSource();
            aitripFactory = null;
            extractorsFactory = null;
        }
        myHandler.removeCallbacksAndMessages(null);
    }


}
