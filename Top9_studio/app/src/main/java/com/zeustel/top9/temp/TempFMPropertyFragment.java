package com.zeustel.top9.temp;


import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Scroller;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.zeustel.top9.FMDetailActivity;
import com.zeustel.top9.R;
import com.zeustel.top9.base.WrapOneFragment;
import com.zeustel.top9.bean.Compere;
import com.zeustel.top9.bean.FM;
import com.zeustel.top9.bean.Program;
import com.zeustel.top9.bean.SubUserInfo;
import com.zeustel.top9.fm.FMHelper;
import com.zeustel.top9.fmcontrol.MediaControl;
import com.zeustel.top9.utils.Constants;
import com.zeustel.top9.utils.NativeUtils;
import com.zeustel.top9.utils.Tools;
import com.zeustel.top9.widgets.CircleImage;
import com.zeustel.top9.widgets.InputView;
import com.zeustel.top9.widgets.VerticalDanmaku;

import java.lang.ref.WeakReference;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * A simple {@link Fragment} subclass.
 */
public class TempFMPropertyFragment extends WrapOneFragment implements View.OnClickListener, ServiceConnection {

    private VerticalDanmaku danmaku;
    //    private Intent service;
    private Scroller scroller;
    private Future<?> submit;
    private boolean flag;

    public TempFMPropertyFragment() {

    }
    private ImageView temp_fm_theme;
    private VerticalDanmaku temp_fm_danmaku;
    private InputView temp_fm_input;
    private TextView temp_fm_flag;
    private ImageButton temp_fm_game;
    private LinearLayout temp_fm_compere;
    private LinearLayout temp_fm_program;
    private LinearLayout temp_fm_attention;
    private LinearLayout temp_fm_edit;
    private MediaControl temp_fm_control;

    private ImageLoader imageLoader = ImageLoader.getInstance();
    private FM fm;
    //    private TempSkipFragment.OnSkipListener mOnSkipListener;
    //主播
    private Compere compere;
    private List<SubUserInfo> subUserInfos;
    private SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm");
    private String[] sss = new String[]{"每日新游资讯、鉴赏、吐槽", "送鲜花抢手机充值卡", " 蛋蛋陪你玩游戏之《生存大冒险》", "新歌音乐直播间", "弹幕达人有奖大比拼", "奇葩话题八卦哦",};
    private List<Program> mPrograms = new ArrayList();
    private ArrayList<Program> mPrograms1 = new ArrayList();

    private LayoutInflater inflater;
    private Random mRandom = new Random();
    private int index;
    private int strIndex;
    private SubUserInfo mSubUserInfo;
    //    private ILocalFM mILocalFM;

    private String[] texts = {"这屁股能玩一年", "车模屁股我能玩一年", "我以为楼上说的是狗屁股", "咦 这期好短!", "最后姿势不是一个人玩的吧/", "小编你瞎吗,那是手套好不", "十八个姿势里面有个不太河蟹", "隐身果实", "这胸够我摸十年", "右边是白女侠吗", "地铁上福利多", "那是没成熟的恶魔果实", "恶魔果实", "恶魔果实", "恶魔果实", "这是恶魔果实", "那明明是手套", "不是看不到未来，只是未来，还未来！", "生无可恋，死何足惜", "说明的，都是理由；明说的，都是结果。我不听你的理由，我只要我的结果。说白了，你白说了。", "当初真心的泪，都是从后来瞎了的眼流出来的", "那些泼过我冷水的人，总有一天我会烧开了还给你们的！", "只要你按时到达目的地，很少有人在乎你开的是奔驰还是手动拖拉机。", "没死就别把自己当废物！", "鱼哭了水知道，我哭了谁知道？??", "大哥！我错了！你就把我当做是个屁给放了吧?", "不要祈求所有人都理解你，因为发生在你身上99%的事与别人无关。", "思念别人是一种温馨，被别人思念是一种幸福。", "A:我长的就这熊样！ B:切！不许侮辱熊。",};

    @Override
    public void onDestroyView() {
        Tools.recycleImg(temp_fm_theme);
        super.onDestroyView();
        //取消正在进行的工作
        if (submit != null) {
            if (!submit.isCancelled())
                submit.cancel(true);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        flag = true;
        //关闭线程池
        if (danMakupool != null) {
            danMakupool.shutdown();
        }
        //        mOnSkipListener = null;
        compere = null;
        if (subUserInfos != null) {
            subUserInfos.clear();
            subUserInfos = null;
        }
        simpleDateFormat = null;
        sss = null;
        if (mPrograms != null) {
            mPrograms.clear();
            mPrograms = null;
        }
        if (mPrograms1 != null) {
            mPrograms1.clear();
            mPrograms1 = null;
        }
        mRandom = null;
        mSubUserInfo = null;
        fm = null;
        compere = null;
        super.onDestroy();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        //view生成后 添加新的任务弹幕测试任务
        submit = danMakupool.submit(new TestTask(this, temp_fm_danmaku, handler));
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Tools.log_i(TempFMPropertyFragment.class, "onCreate", "");
        inflater = getActivity().getLayoutInflater();
        Tools.pool.submit(new Runnable() {
            @Override
            public void run() {
                subUserInfos = NativeUtils.importSubs(getContext());
                Tools.log_i(TempFMPropertyFragment.class, "run", "");
                if (subUserInfos != null && !subUserInfos.isEmpty()) {
                    Tools.log_i(TempFMPropertyFragment.class, "run", " size : " + subUserInfos.size());
                }
            }
        });

        fm = new FM();
        compere = new Compere();
        compere.setNickName("艾利");
        compere.setNameEN("AILEY");
        compere.setBirthday("12月1日");
        compere.setBloodType("O");
        compere.setConstellation("射手");
        compere.setHobby("吃饭睡觉打豆豆");
        compere.setManifesto("很高兴认识大家,我在TOP9平台哦~ 喜欢我的就点我吧,千万别错过每天的抽奖活动哦 !");
        compere.setVideo("android.resource://" + getActivity().getPackageName() + "/" + R.raw.fm09);
        compere.setVideoCover(String.valueOf(R.mipmap.fm08));
        compere.setPhotos(R.mipmap.fm04 + "," + R.mipmap.fm05 + "," + R.mipmap.fm06 + "," + R.mipmap.fm07);
        fm.setCompere(compere);

        for (int i = 0; i < sss.length; i++) {
            final Program mProgram = new Program();
            try {
                mProgram.setTime(simpleDateFormat.parse((10 + i + ":" + "00")).getTime());
            } catch (ParseException e) {
                e.printStackTrace();
            }
            mPrograms.add(mProgram);
        }

        for (int i = 0; i < mPrograms.size(); i++) {
            final Program program = mPrograms.get(i);
            program.setTitle(sss[i]);
            if (i == 2) {
                fm.setPlayingProgram(program);
            }
            mPrograms1.add(program);
        }
        fm.setPrograms(mPrograms1);

        //        service = new Intent(getActivity(), LocalFmService.class);
        //        getActivity().startService(service);
        //        getActivity().bindService(service, this, Context.BIND_AUTO_CREATE);
    }

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            strIndex = mRandom.nextInt(texts.length);
            if (subUserInfos != null && !subUserInfos.isEmpty()) {
                index = mRandom.nextInt(subUserInfos.size());
                mSubUserInfo = subUserInfos.get(index);
            } else {
                mSubUserInfo = new SubUserInfo();
                mSubUserInfo.setIcon(null);
                mSubUserInfo.setNickName("asdsaonlkd");
            }
            final View view = makeAmmo(mSubUserInfo, texts[strIndex]);
            if (view != null) {
                temp_fm_danmaku.send(view);
            }
        }
    };

    @Override
    public String getFloatTitle() {
        return getString(R.string.title_fm_detail);
    }

    @Override
    public int getBackgroundColor() {
        return 0;
    }

    /**
     * 输入框发送弹幕
     */
    private static final class SendTask implements Runnable {
        private final WeakReference<VerticalDanmaku> mVerticalDanmakuRef;
        private final WeakReference<InputView> mInputViewRef;
        private final WeakReference<TempFMPropertyFragment> mTempFMFragmentRef;
        private String content;

        public SendTask(VerticalDanmaku temp_fm_danmaku, InputView temp_fm_input, TempFMPropertyFragment mTempFMFragment, String content) {
            mVerticalDanmakuRef = new WeakReference<VerticalDanmaku>(temp_fm_danmaku);
            mInputViewRef = new WeakReference<InputView>(temp_fm_input);
            mTempFMFragmentRef = new WeakReference<TempFMPropertyFragment>(mTempFMFragment);
            this.content = content;
        }

        @Override
        public void run() {
            final VerticalDanmaku danmaku = mVerticalDanmakuRef.get();
            final InputView inputView = mInputViewRef.get();
            final TempFMPropertyFragment mTempFMFragment = mTempFMFragmentRef.get();
            if (mTempFMFragment != null && danmaku != null && inputView != null) {
                danmaku.send(mTempFMFragment.makeAmmo(Constants.USER, content));
                inputView.done();
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View contentView = inflater.inflate(R.layout.fragment_temp_fm, container, false);
        temp_fm_theme = (ImageView) contentView.findViewById(R.id.temp_fm_theme);
        temp_fm_danmaku = (VerticalDanmaku) contentView.findViewById(R.id.temp_fm_danmaku);
        temp_fm_danmaku.setEnabled(false);
        temp_fm_input = (InputView) contentView.findViewById(R.id.temp_fm_input);
        temp_fm_flag = (TextView) contentView.findViewById(R.id.temp_fm_flag);
        temp_fm_game = (ImageButton) contentView.findViewById(R.id.temp_fm_game);
        temp_fm_compere = (LinearLayout) contentView.findViewById(R.id.temp_fm_compere);
        temp_fm_program = (LinearLayout) contentView.findViewById(R.id.temp_fm_program);
        temp_fm_attention = (LinearLayout) contentView.findViewById(R.id.temp_fm_attention);
        temp_fm_edit = (LinearLayout) contentView.findViewById(R.id.temp_fm_edit);
        temp_fm_control = (MediaControl) contentView.findViewById(R.id.temp_fm_control);
        temp_fm_input.setOnInputView(new InputView.OnInputView() {
            @Override
            public void onInputView(final String content) {

                handler.postDelayed(new SendTask(temp_fm_danmaku, temp_fm_input, TempFMPropertyFragment.this, content), 500);
            }
        });
        temp_fm_input.setBg(Color.TRANSPARENT);
        temp_fm_game.setOnClickListener(this);
        temp_fm_danmaku.setOnClickListener(this);
        temp_fm_input.setOnClickListener(this);
        temp_fm_edit.setOnClickListener(this);
        temp_fm_compere.setOnClickListener(this);
        temp_fm_program.setOnClickListener(this);
        temp_fm_control.setOnMediaControlListener(new MediaControl.OnMediaControlListener() {
            @Override
            public void onPlay() {
                Tools.log_i(TempFMPropertyFragment.class, "onPlay", "");
                try {
                    FMHelper.netSDK.mic(true);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                //                if (mILocalFM != null) {
                //                    mILocalFM.start();
                //                }
            }

            @Override
            public void onPause() {
                Tools.log_i(TempFMPropertyFragment.class, "onPause", "");
                try {
                    FMHelper.netSDK.mic(false);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                //                if (mILocalFM != null) {
                //                    mILocalFM.pause();
                //                }

            }
        });
        updateUI();
        return contentView;
    }

    @Override
    public void onStop() {
        super.onStop();
        if (temp_fm_danmaku != null) {
            temp_fm_danmaku.setEnabled(false);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (temp_fm_danmaku != null) {
            temp_fm_danmaku.setEnabled(true);
        }
    }

    private ExecutorService danMakupool = Executors.newFixedThreadPool(1);

    private void updateUI() {
        temp_fm_flag.setText("小妹陪你玩游戏,速来围观啊啊啊啊啊阿萨斯的");
        temp_fm_flag.setSelected(true);
        imageLoader.displayImage("drawable://" + R.mipmap.fm02, temp_fm_theme, Tools.options);
    }

    /**
     * 测试弹幕任务
     */
    private static final class TestTask implements Runnable {
        private final WeakReference<VerticalDanmaku> mVerticalDanmakuRef;
        private final WeakReference<TempFMPropertyFragment> mFragmentRef;
        private final WeakReference<Handler> handlerRef;

        public TestTask(TempFMPropertyFragment fragment, VerticalDanmaku temp_fm_danmaku, Handler mHandler) {
            mFragmentRef = new WeakReference<TempFMPropertyFragment>(fragment);
            mVerticalDanmakuRef = new WeakReference<VerticalDanmaku>(temp_fm_danmaku);
            handlerRef = new WeakReference<Handler>(mHandler);
        }

        @Override
        public void run() {
            final VerticalDanmaku mVerticalDanmaku = mVerticalDanmakuRef.get();
            final TempFMPropertyFragment fragment = mFragmentRef.get();
            final Handler mHandler = handlerRef.get();
            while (fragment != null && !fragment.isDetached() && !fragment.flag && mVerticalDanmaku != null && mHandler != null) {
                Tools.log_i(TestTask.class, "run", "send ...");
                if (mVerticalDanmaku.isEnabled()) {
                    mHandler.sendEmptyMessage(1);
                } else {
                    int childCount = mVerticalDanmaku.getChildCount();
                    if (childCount > 0) {
                        mHandler.post(new ClearTask(mVerticalDanmaku));
                    }
                }
                try {
                    Thread.sleep(1500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 清理工
     */
    private static final class ClearTask implements Runnable {
        private final WeakReference<VerticalDanmaku> mVerticalDanmakuRef;

        public ClearTask(VerticalDanmaku mVerticalDanmaku) {
            mVerticalDanmakuRef = new WeakReference<VerticalDanmaku>(mVerticalDanmaku);
        }

        @Override
        public void run() {
            final VerticalDanmaku danmaku = mVerticalDanmakuRef.get();
            if (danmaku != null) {
                danmaku.resetView();
            }
        }
    }

    /**
     * 生产弹幕
     * @param subUserInfo 用户
     * @param text 文本
     * @return
     */
    private View makeAmmo(SubUserInfo subUserInfo, String text) {
        if (subUserInfo == null) {
            return null;
        }
        final View ammoView = inflater.inflate(R.layout.list_item_ammo, null);
        CircleImage ammo_by = (CircleImage) ammoView.findViewById(R.id.ammo_by);
        TextView ammo_text = (TextView) ammoView.findViewById(R.id.ammo_text);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            ammo_by.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        }
        String icon = subUserInfo.getIcon();
        if (!Tools.isEmpty(icon)) {
            imageLoader.displayImage(Constants.TEST_IMG + icon, ammo_by, Tools.options);
        }

        ammo_text.setText((Html.fromHtml(getContext().getString(R.string.html_font, "#FD4C3C"/*color*/, subUserInfo.getNickName()) +
                getContext().getString(R.string.space) +
                ":" +
                getContext().getString(R.string.space) +
                getContext().getString(R.string.html_font, "#FFFFFF"/*color*/, text))));
        return ammoView;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.temp_fm_game:
                //                if (mOnSkipListener == null) {
                //                    mOnSkipListener = getOnSkipListener();
                //                }
                //                if (mOnSkipListener != null) {
                //                    mOnSkipListener.onSkip(TempFMFragment.this);
                //                }
                break;
            case R.id.temp_fm_program:
            case R.id.temp_fm_compere:

                Intent intent = new Intent(getContext(), FMDetailActivity.class);
                intent.putExtra(FMDetailActivity.EXTRA_NAME_DATA, fm);
                if (R.id.temp_fm_program == v.getId()) {
                    intent.putExtra(FMDetailActivity.EXTRA_NAME_POSITION, 1/*position*/);
                } else {
                    intent.putExtra(FMDetailActivity.EXTRA_NAME_POSITION, 0/*position*/);
                }
                startActivity(intent);
                break;
            case R.id.temp_fm_edit:
                if (temp_fm_input.getVisibility() == View.VISIBLE) {
                    temp_fm_input.setVisibility(View.INVISIBLE);
                } else {
                    temp_fm_input.setVisibility(View.VISIBLE);
                }
                break;
            case R.id.temp_fm_attention:
                break;
        }
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        //        mILocalFM = (ILocalFM) service;
        Tools.log_i(TempFMPropertyFragment.class, "onServiceConnected", "");
    }


    @Override
    public void onServiceDisconnected(ComponentName name) {
        Tools.log_i(TempFMPropertyFragment.class, "onServiceDisconnected", "");

    }
}
