package com.zeustel.top9.adapters;

import android.content.Context;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.gson.Gson;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.zeustel.top9.R;
import com.zeustel.top9.bean.SubUserInfo;
import com.zeustel.top9.bean.community.Comment;
import com.zeustel.top9.bean.community.Content;
import com.zeustel.top9.bean.community.Level;
import com.zeustel.top9.result.Result;
import com.zeustel.top9.utils.Constants;
import com.zeustel.top9.utils.Tools;
import com.zeustel.top9.utils.operate.DataUser;

import org.apache.http.Header;

/**
 * @author NiuLei
 * @email niulei@zeustel.com
 * @date 2015/11/10 13:51
 */
public class HolderComment extends OverallRecyclerHolder<Comment> {

    private ImageView comment_icon;
    private TextView comment_level;
    private TextView comment_name;
    private TextView comment_time;
    private TextView comment_content;
    private TextView comment_goodCount;
    private TextView comment_replyCount;
    private View animGood;
    private Gson gson = new Gson();
    private Comment item;
    private SubUserInfo commentUser;
    private String icon;
    private Level level;
    private String nickName;
    private int goodCount;
    private boolean gooded;
    private int replyCount;
    private Result result;
    private String msg;
    private Content content;

    public HolderComment(Context context, View itemView) {
        super(context, itemView);
    }

    @Override
    protected void initItemView(View itemView) {
        comment_icon = (ImageView) itemView.findViewById(R.id.comment_icon);
        comment_level = (TextView) itemView.findViewById(R.id.comment_level);
        comment_name = (TextView) itemView.findViewById(R.id.comment_name);
        comment_time = (TextView) itemView.findViewById(R.id.comment_time);
        comment_content = (TextView) itemView.findViewById(R.id.comment_content);
        comment_goodCount = (TextView) itemView.findViewById(R.id.comment_goodCount);
        comment_replyCount = (TextView) itemView.findViewById(R.id.comment_replyCount);
        animGood = itemView.findViewById(R.id.animGood);
        comment_icon.setOnClickListener(this);
        comment_goodCount.setOnClickListener(this);
        comment_replyCount.setOnClickListener(this);

    }

    @Override
    public void set(final OverallRecyclerAdapter<Comment> adapter, int position) {
        animGood.setVisibility(View.INVISIBLE);
        item = adapter.getItem(position);
        if (item != null) {
            commentUser = item.getPublishUser();
            if (commentUser != null) {
                icon = commentUser.getIcon();
                level = item.getLevel();
                nickName = commentUser.getNickName();
                content = item.getContent();

                goodCount = item.getGoodCount();
                gooded = item.isGooded();
                replyCount = item.getReplyCount();
                getImageLoader().displayImage(Constants.TEST_IMG + icon, comment_icon, Tools.options);
                if (Level.BEST.equals(level)) {
                    comment_level.setVisibility(View.VISIBLE);
                } else {
                    comment_level.setVisibility(View.INVISIBLE);
                }
                comment_name.setText(nickName);
                //                if (Mutual.ContentType.AUDIO != item.getContentType()) {
                msg = content.getMsg();
                comment_content.setText(msg);
                //                } else {
                //
                //                }
                try {
                    comment_goodCount.setText(String.valueOf(goodCount));
                } catch (Exception e) {
                    e.printStackTrace();
                    comment_goodCount.setText("0");
                }
                comment_goodCount.setSelected(gooded);
                try {
                    comment_replyCount.setText(String.valueOf(replyCount));
                } catch (Exception e) {
                    e.printStackTrace();
                    comment_replyCount.setText("0");
                }
            }
            if (!item.isGooded()) {
                DataUser.checkGoodState(item.getId(), new AsyncHttpResponseHandler() {
                    @Override
                    public void onSuccess(int i, Header[] headers, byte[] bytes) {
                        if (bytes != null) {
                            result = gson.fromJson(new String(bytes), Result.class);
                            if (result != null) {
                                if (result.getError() == 10017/*已经点赞*/) {
                                    comment_goodCount.setSelected(true);
                                }
                            }
                        }
                    }

                    @Override
                    public void onFailure(int i, Header[] headers, byte[] bytes, Throwable throwable) {

                    }
                });
            }
        }
    }

    @Override
    public void onClick(View v) {
        super.onClick(v);
        if (getOnElementClickListener() != null) {
            switch (v.getId()) {
                case R.id.comment_icon:
                    getOnElementClickListener().onElementClick(getAdapterPosition(), "icon");
                    break;
                case R.id.comment_goodCount:
                    getOnElementClickListener().onElementClick(getAdapterPosition(), "good");
                    break;
                case R.id.comment_replyCount:
                    getOnElementClickListener().onElementClick(getAdapterPosition(), "reply");
                    break;
            }
        }
    }

    public void goodChange(int position) {
        if (getAdapterPosition() == position) {
            Tools.startGoodOne(getContext(), animGood);
        }
    }
}
