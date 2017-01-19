package com.zeustel.top9.fragments.html;


import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.gson.Gson;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.zeustel.top9.LoginActivity;
import com.zeustel.top9.R;
import com.zeustel.top9.adapters.HolderGG;
import com.zeustel.top9.adapters.HolderReply;
import com.zeustel.top9.adapters.OverallRecyclerAdapter;
import com.zeustel.top9.base.WrapNotAndRefreshFragment;
import com.zeustel.top9.bean.GameEvaluating;
import com.zeustel.top9.bean.SubUserInfo;
import com.zeustel.top9.bean.community.Comment;
import com.zeustel.top9.bean.community.Content;
import com.zeustel.top9.bean.community.Reply;
import com.zeustel.top9.result.ReplyListResult;
import com.zeustel.top9.result.Result;
import com.zeustel.top9.utils.Constants;
import com.zeustel.top9.utils.NetHelper;
import com.zeustel.top9.utils.Tools;
import com.zeustel.top9.utils.operate.DBReplyImp;
import com.zeustel.top9.utils.operate.DataReply;
import com.zeustel.top9.utils.operate.DataUser;
import com.zeustel.top9.widgets.CustomRecyclerView;
import com.zeustel.top9.widgets.InputView;

import org.apache.http.Header;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

/**
 * 评论详细
 */
public class CommentDetailFragment extends WrapNotAndRefreshFragment<Reply> implements InputView.OnInputView, OverallRecyclerAdapter.OnitemClickListener, View.OnClickListener {
    private ImageView commentDetailIcon;
    private TextView commentDetailName;
    private TextView commentDetailTime;
    private TextView commentDetailContent;
    private TextView commentDetailGood;
    private View animGood;
    private TextView commentDetailReply;
    private CustomRecyclerView commentDetailgoodGroup;
    private ImageView commentDetailmoreGood;
    private LinearLayout commentDetailEv;
    private ImageView commentDetailBanner;
    private TextView commentDetailTitle;
    private OverallRecyclerAdapter<Reply> adapter;
    private Comment comment;
    private GameEvaluating gameEvaluating;
    private static final String EXTRA_NAME_EV = "gameEvaluating";
    private static final String EXTRA_NAME_COM = "comment";
    private static final String EXTRA_NAME_DATA = "dataComment";
    private DataReply dataReply;
    private ImageLoader imageLoader = ImageLoader.getInstance();
    private InputView commentDetailInput;
    private List<SubUserInfo> goodGroups = new ArrayList();
    private OverallRecyclerAdapter<SubUserInfo> goodAdapter;

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (goodGroups != null) {
            goodGroups.clear();
        }
    }

    public static CommentDetailFragment newInstance(GameEvaluating gameEvaluating, Comment comment) {
        Bundle args = new Bundle();
        args.putSerializable(EXTRA_NAME_EV, gameEvaluating);
        args.putSerializable(EXTRA_NAME_COM, comment);
        CommentDetailFragment fragment = new CommentDetailFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            gameEvaluating = (GameEvaluating) getArguments().getSerializable(EXTRA_NAME_EV);
            comment = (Comment) getArguments().getSerializable(EXTRA_NAME_COM);
        }
        if (gameEvaluating == null || comment == null) {
            getFragmentManager().popBackStack();
        }
        dataReply = new DataReply(getHandler(), null);
    }

    public CommentDetailFragment() {

    }

    @Override
    public View onBeforeCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View contentView = inflater.inflate(R.layout.fragment_comment_detail, container, false);
        commentDetailInput = (InputView) contentView.findViewById(R.id.commentDetailInput);
        View comment_detail_head = contentView.findViewById(R.id.comment_detail_head);
        commentDetailIcon = (ImageView) comment_detail_head.findViewById(R.id.commentDetailIcon);
        commentDetailName = (TextView) comment_detail_head.findViewById(R.id.commentDetailName);
        commentDetailTime = (TextView) comment_detail_head.findViewById(R.id.commentDetailTime);
        commentDetailContent = (TextView) comment_detail_head.findViewById(R.id.commentDetailContent);
        ///////////////////////
        commentDetailGood = (TextView) comment_detail_head.findViewById(R.id.commentDetailGood);
        animGood = comment_detail_head.findViewById(R.id.animGood);
        commentDetailReply = (TextView) comment_detail_head.findViewById(R.id.commentDetailReply);
        commentDetailgoodGroup = (CustomRecyclerView) comment_detail_head.findViewById(R.id.commentDetailgoodGroup);
        commentDetailmoreGood = (ImageView) comment_detail_head.findViewById(R.id.commentDetailmoreGood);
        commentDetailEv = (LinearLayout) comment_detail_head.findViewById(R.id.commentDetailEv);
        commentDetailBanner = (ImageView) comment_detail_head.findViewById(R.id.commentDetailBanner);
        commentDetailTitle = (TextView) comment_detail_head.findViewById(R.id.commentDetailTitle);
        commentDetailgoodGroup.setItemAnimator(new DefaultItemAnimator());
        commentDetailgoodGroup.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        linearLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        commentDetailgoodGroup.setLayoutManager(linearLayoutManager);
        try {
            goodAdapter = new OverallRecyclerAdapter(goodGroups, R.layout.list_item_gg, HolderGG.class);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
            goodAdapter = null;
        }
        commentDetailgoodGroup.setAdapter(goodAdapter);
        commentDetailInput.setOnInputView(this);
        commentDetailGood.setOnClickListener(this);
        return contentView;
    }

    /*更新评论相关信息*/
    private void updateCommentUI() {
        if (comment != null) {
            SubUserInfo publishUser = comment.getPublishUser();
            if (publishUser != null) {
                imageLoader.displayImage(Constants.TEST_IMG + publishUser.getIcon(), commentDetailIcon, Tools.options);
                String nickName = publishUser.getNickName();
                commentDetailName.setText(Tools.isEmpty(nickName) ? "" : nickName);
            }
            int replyCount;
            int goodCount;
            try {
                replyCount = comment.getReplyCount();
            } catch (Exception e) {
                e.printStackTrace();
                replyCount = 0;
            }
            try {
                goodCount = comment.getGoodCount();
            } catch (Exception e) {
                e.printStackTrace();
                goodCount = 0;
            }
            Content content = comment.getContent();
            if (content != null) {
                Content.ContentType contentType = content.getContentType();
                if (Content.ContentType.AUDIO == contentType) {

                } else {
                    commentDetailContent.setText(content.getMsg());
                }
            }
            if (goodCount > 0) {
                commentDetailGood.setText(String.valueOf(goodCount));
            }
            if (replyCount > 0) {
                commentDetailReply.setText(String.valueOf(replyCount));
            }
        }
        if (gameEvaluating != null) {
            imageLoader.displayImage(Constants.TEST_IMG + gameEvaluating.getBanner(), commentDetailBanner, Tools.options);
            commentDetailTitle.setText(gameEvaluating.getTitle());
        }
    }

    @Override
    public void onClick(View v) {
        if (commentDetailGood.equals(v)) {
            if (Constants.USER.isOnline()) {
                DataUser.doGood(comment.getId(), new AsyncHttpResponseHandler() {
                    @Override
                    public void onSuccess(int i, Header[] headers, byte[] bytes) {
                        if (bytes != null) {
                            String json = new String(bytes);
                            Gson gson = new Gson();
                            Result result = gson.fromJson(json, Result.class);
                            if (result.getSuccess() == Result.SUCCESS) {
                                success(result);
                                return;
                            } else if (result.getError() == 10017/*已经点赞*/) {
                                Tools.showToast(getActivity(), result.getMsg());
                                success(null);
                                return;
                            }
                        }
                        failueToast();
                    }

                    @Override
                    public void onFailure(int i, Header[] headers, byte[] bytes, Throwable throwable) {
                        failueToast();
                    }

                    private void failueToast() {
                        Tools.showToast(getActivity(), "点赞失败");
                    }

                    private void success(Result result) {
                        if (result != null) {
                            //动画
                            commentDetailGood.setText(String.valueOf(result.getCount()));
                            Tools.startGoodOne(getActivity(), animGood);
                            Tools.log_i(CommentDetailFragment.class, "success", "start good anim ");
                        }
                        commentDetailGood.setSelected(true);

                    }
                });
            } else {
                Intent intent = new Intent(getContext(), LoginActivity.class);
                startActivity(intent);
            }
        }
    }

    @Override
    protected void onAfterCreateView(View contentView) {
        setDbOperate(new DBReplyImp(getActivity()));
        setDataOperate(new DataReply(getHandler(), getDbOperate()));
        try {
            adapter = new OverallRecyclerAdapter<Reply>(getData(), R.layout.reply_item, HolderReply.class);
            adapter.setOnitemClickListener(this);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
        setAdapter(adapter);
        updateCommentUI();
    }

    @Override
    public void onInputView(String content) {
        Reply reply = new Reply();
        reply.setTagId(comment.getId());
        Content content1 = new Content();
        content1.setContentType(Content.ContentType.TEXT);
        content1.setMsg(content);
        reply.setContent(content1);
        dataReply.publishData(reply);
    }

    @Override
    public void onHandlePublishSuccess(Result result) {
        super.onHandlePublishSuccess(result);
        commentDetailInput.done();
    }

    @Override
    public void onHandleListUpdate(Result result, Object obj) {
        if (result != null && obj != null && Result.SUCCESS == result.getSuccess()) {
            List<SubUserInfo> goodGroup = null;
            List<Reply> data = null;
            if (result instanceof ReplyListResult) {
                goodGroup = ((ReplyListResult) result).getGoodGroup();
                if (!Tools.isEmpty(goodGroup) && goodAdapter != null) {
                    goodGroups.clear();
                    goodGroups.addAll(goodGroup);
                    goodAdapter.notifyDataSetChanged();
                }
            }
            data = (List<Reply>) obj;
            cancelRefresh();
            getLoading().loadSuccess();
            if (!Tools.isEmpty(data)) {
                getData().addAll(0, data);
                adapter.notifyItemRangeInserted(0, data.size());
            }
        }
    }

    @Override
    public void onHandleListNo(Result result) {
        cancelRefresh();
        getLoading().loadSuccess();
        if (result != null && result instanceof ReplyListResult) {
            List<SubUserInfo> goodGroup = ((ReplyListResult) result).getGoodGroup();
            if (!Tools.isEmpty(goodGroup) && goodAdapter != null) {
                goodGroups.clear();
                goodGroups.addAll(goodGroup);
                goodAdapter.notifyDataSetChanged();
            }
        }
    }

    @Override
    public void onHandleUpdate(Object obj) {
        Tools.log_i(CommentDetailFragment.class, "onHandleUpdate", "");
    }

    @Override
    public void onHandlePublishFailed(Result result) {
        commentDetailInput.done();
    }

    @Override
    public String getFloatTitle() {
        return null;
    }

    @Override
    public int getBackgroundColor() {
        return 0;
    }

    @Override
    public void onPullDownRefresh() {
        Reply reply = Tools.getFirstData(getData());
        long time = 0;
        if (reply != null) {
            time = reply.getTime();
        }
        try {
            dataReply.postListData(dataReply.createReplyBundle(Constants.URL_COMMENT_REPLY_LIST, time, NetHelper.Flag.UPDATE, comment.getId()), ReplyListResult.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onPullUpRefresh() {
        Reply reply = Tools.getLastData(getData());
        long time = 0;
        if (reply != null) {
            time = reply.getTime();
        }
        try {
            dataReply.postListData(dataReply.createReplyBundle(Constants.URL_COMMENT_REPLY_LIST, time, NetHelper.Flag.HISTORY, comment.getId()), ReplyListResult.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onitemClick(RecyclerView.Adapter adapter, int position, String tag) {
        if (tag.equals("good")) {
            if (Constants.USER.isOnline()) {
                DataUser.doGood(getData().get(position).getId(), new GoodRequestResponse(position, getContext(), getRecyler(), getData()));
            } else {
                Intent intent = new Intent(getContext(), LoginActivity.class);
                startActivity(intent);
            }
        } else {
            //at ...
        }
    }

    private static final class GoodRequestResponse extends AsyncHttpResponseHandler {
        private final WeakReference<Context> contextRef;
        private final WeakReference<RecyclerView> mRecyclerViewRef;
        private final WeakReference<List<Reply>> mListRef;

        private int index;

        public GoodRequestResponse(int index, Context context, RecyclerView mRecyclerView, List<Reply> data) {
            this.index = index;
            contextRef = new WeakReference<Context>(context);
            mRecyclerViewRef = new WeakReference<RecyclerView>(mRecyclerView);
            mListRef = new WeakReference<List<Reply>>(data);
        }

        @Override
        public void onSuccess(int i, Header[] headers, byte[] bytes) {
            if (bytes != null) {
                String json = new String(bytes);
                Gson gson = new Gson();
                Tools.log_i(GoodRequestResponse.class, "onSuccess", "json :" + json);
                Result result = gson.fromJson(json, Result.class);
                if (result.getSuccess() == Result.SUCCESS) {
                    success(result);
                    return;
                } else if (result.getError() == 10017/*已经点赞*/) {
                    final Context context = contextRef.get();
                    if (context != null) {
                        Tools.showToast(context, result.getMsg());
                        success(null);
                    }
                    return;
                }
            }
            failueToast();
        }

        @Override
        public void onFailure(int i, Header[] headers, byte[] bytes, Throwable throwable) {
            failueToast();
        }

        private void failueToast() {
            final Context context = contextRef.get();
            if (context != null) {
                Tools.showToast(context, "点赞失败");
            }
        }

        private void success(Result result) {
            final RecyclerView recyclerView = mRecyclerViewRef.get();
            final List<Reply> replies = mListRef.get();
            if (recyclerView != null && replies != null) {
                View childAt = recyclerView.getChildAt(index);
                if (childAt != null) {
                    RecyclerView.ViewHolder childViewHolder = recyclerView.getChildViewHolder(childAt);
                    if (childViewHolder != null && childViewHolder instanceof HolderReply) {
                        boolean need = (result != null);
                        //..更新数据库
                        Reply reply = replies.get(index);
                        reply.setIsGooded(true);
                        if (need) {
                            reply.setGoodCount(result.getCount());
                            ((HolderReply) childViewHolder).goodChange(index);
                        }
                        replies.set(index, reply);
                        recyclerView.getAdapter().notifyItemChanged(index);
                    }
                }
            }
        }
    }
}
