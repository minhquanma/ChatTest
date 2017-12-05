package com.mmq.chattest;

import android.content.Context;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.URLUtil;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.List;

/**
 * Created by mmq on 05/08/2017.
 */

public class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{

    private static final int ITEM_TYPE_LEFT = 0;
    private static final int ITEM_TYPE_RIGHT = 1;
    private List<Messages> messagesList;

    public ChatAdapter(List<Messages> messagesList) {
        this.messagesList = messagesList;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        if (viewType == ITEM_TYPE_LEFT) {
            View left = LayoutInflater.from(parent.getContext()).inflate(R.layout.chat_item_left, parent, false);
            return new ChatLeftViewHolder(left); // view holder for normal items
        } else if (viewType == ITEM_TYPE_RIGHT) {
            View right = LayoutInflater.from(parent.getContext()).inflate(R.layout.chat_item_right, parent, false);
            return new ChatRightViewHolder(right); // view holder for header items
        }
        return null;
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, final int position) {
        if (holder.getItemViewType() == ITEM_TYPE_LEFT) {

            // Set view holder type
            final ChatLeftViewHolder myholder = ((ChatLeftViewHolder)holder);

            // Get current msg item
            final Messages msg = messagesList.get(position);

            myholder.itemMessage.setText(msg.getMessage());
            myholder.itemSender.setText(msg.getSender());
            if (URLUtil.isValidUrl(msg.getAvatar())) {
                Picasso.with(myholder.imageAvatar.getContext())
                        .load(msg.getAvatar())
                        .into(myholder.imageAvatar);
            }

            // Click vao avatar thi hien dialog thong tin cua nguoi do
            myholder.imageAvatar.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Context context = myholder.imageAvatar.getContext();
                    API.showProfileDialog(context, msg.getUid());
                }
            });
        }
        if (holder.getItemViewType() == ITEM_TYPE_RIGHT) {

            // Set view holder type
            final ChatRightViewHolder myholder = ((ChatRightViewHolder)holder);

            // Get current msg item
            Messages msg = messagesList.get(position);
            myholder.itemMessage.setText(msg.getMessage());
            myholder.itemSender.setText(msg.getSender());
            if (URLUtil.isValidUrl(msg.getAvatar())) {
                Picasso.with(myholder.imageAvatar.getContext())
                        .load(msg.getAvatar())
                        .into(myholder.imageAvatar);
            }

            myholder.itemMessage.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    final PopupMenu popup = new PopupMenu(myholder.itemMessage.getContext(), myholder.itemMessage);
                    popup.getMenuInflater().inflate(R.menu.context_menu, popup.getMenu());
                    popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem item) {
                            switch (item.getItemId()) {
                                case R.id.m_delete:
                                    API.removeMessage(messagesList.get(position).getKey());
                                    break;
                            }
                            return false;
                        }
                    });
                    popup.show();
                    return false;
                }
            });
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (messagesList.get(position).isMe()) {
            return ITEM_TYPE_RIGHT;
        } else {
            return ITEM_TYPE_LEFT;
        }
    }

    @Override
    public int getItemCount() {
        return messagesList.size();
    }

    public static class ChatLeftViewHolder extends RecyclerView.ViewHolder {

        public TextView itemSender;
        public TextView itemMessage;
        public ImageView imageAvatar;

        public ChatLeftViewHolder(final View itemView) {
            super(itemView);
            itemSender = itemView.findViewById(R.id.itemSender);
            itemMessage = itemView.findViewById(R.id.itemMessage);
            imageAvatar = itemView.findViewById(R.id.imageViewAvatar);
        }
    }

    public static class ChatRightViewHolder extends RecyclerView.ViewHolder {

        public TextView itemSender;
        public TextView itemMessage;
        public ImageView imageAvatar;

        public ChatRightViewHolder(final View itemView) {
            super(itemView);
            itemSender = itemView.findViewById(R.id.itemSender);
            itemMessage = itemView.findViewById(R.id.itemMessage);
            imageAvatar = itemView.findViewById(R.id.imageViewAvatar);
        }
    }
}



