package com.zeustel.top9.adapters;

import android.content.Context;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.zeustel.top9.R;
import com.zeustel.top9.bean.SpeakUser;
import com.zeustel.top9.utils.Constants;
import com.zeustel.top9.utils.Tools;

/**
 * @author NiuLei
 * @email niulei@zeustel.com
 * @date 2015/9/8 16:26
 */
public class HolderSpeakUser extends OverallRecyclerHolder<SpeakUser> {
    private ImageView voice_child_icon;
    private ImageView voice_child_speak;
    private TextView voice_child_nickName;
    private SpeakUser speakUser;
    private String icon = null;
    private String nickName = null;

    public HolderSpeakUser(Context context, View itemView) {
        super(context, itemView);
    }

    @Override
    protected void initItemView(View itemView) {
        voice_child_icon = (ImageView) itemView.findViewById(R.id.voice_child_icon);
        voice_child_speak = (ImageView) itemView.findViewById(R.id.voice_child_speak);
        voice_child_nickName = (TextView) itemView.findViewById(R.id.voice_child_nickName);
    }

    @Override
    public void set(OverallRecyclerAdapter<SpeakUser> adapter, int position) {
        speakUser = adapter.getItem(position);

        if (speakUser != null) {
            icon = speakUser.getIcon();
            nickName = speakUser.getNickName();
        }
        getImageLoader().displayImage(Constants.TEST_IMG + icon, voice_child_icon, Tools.newOptions(voice_child_icon.getWidth() / 2));
        voice_child_nickName.setText(Tools.isEmpty(nickName) ? "" : nickName);
        voice_child_speak.setVisibility(speakUser.isSpeak() ? View.VISIBLE : View.INVISIBLE);
    }

    @Override
    protected boolean isItemClickEnabled() {
        return false;
    }
}
