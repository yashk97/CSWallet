package hk.ust.cse.comp107x.chatclient;

import android.content.Context;
import android.support.v7.widget.RecyclerView.Adapter;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import java.util.ArrayList;

public class MyAdapter extends Adapter<ViewHolder> {
    private static final int VIEW_HOLDER_TYPE_1 = 1;
    private static final int VIEW_HOLDER_TYPE_2 = 2;
    private final Context context;
    private final ArrayList<Message> messages;

    public static class ViewHolder_Type1 extends ViewHolder {
        public TextView mymessageTextView;
        public TextView mytimeTextView;

        public ViewHolder_Type1(View v) {
            super(v);
            this.mymessageTextView = (TextView) v.findViewById(R.id.mymessageTextView);
            this.mytimeTextView = (TextView) v.findViewById(R.id.mytimeTextView);
        }
    }

    public static class ViewHolder_Type2 extends ViewHolder {
        public TextView messageTextView;
        public TextView timeTextView;

        public ViewHolder_Type2(View v) {
            super(v);
            this.messageTextView = (TextView) v.findViewById(R.id.messageTextView);
            this.timeTextView = (TextView) v.findViewById(R.id.timeTextView);
        }
    }

    public MyAdapter(Context context, ArrayList<Message> messages) {
        this.context = context;
        this.messages = messages;
    }

    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        switch (viewType) {
            case VIEW_HOLDER_TYPE_1 /*1*/:
                return new ViewHolder_Type1(LayoutInflater.from(parent.getContext()).inflate(R.layout.mymessage, parent, false));
            case VIEW_HOLDER_TYPE_2 /*2*/:
                return new ViewHolder_Type2(LayoutInflater.from(parent.getContext()).inflate(R.layout.message, parent, false));
            default:
                return null;
        }
    }

    public void onBindViewHolder(ViewHolder holder, int position) {
        switch (getItemViewType(position)) {
            case VIEW_HOLDER_TYPE_1 /*1*/:
                ViewHolder_Type1 viewholder1 = (ViewHolder_Type1) holder;
                viewholder1.mytimeTextView.setText(((Message) this.messages.get(position)).getTime());
                viewholder1.mymessageTextView.setText(((Message) this.messages.get(position)).getMessage());
                return;
            case VIEW_HOLDER_TYPE_2 /*2*/:
                ViewHolder_Type2 viewholder2 = (ViewHolder_Type2) holder;
                viewholder2.timeTextView.setText(((Message) this.messages.get(position)).getTime());
                viewholder2.messageTextView.setText(((Message) this.messages.get(position)).getMessage());
                return;
            default:
                return;
        }
    }

    public int getItemViewType(int position) {
        if (((Message) this.messages.get(position)).fromMe()) {
            return VIEW_HOLDER_TYPE_1;
        }
        return VIEW_HOLDER_TYPE_2;
    }

    public int getItemCount() {
        return this.messages.size();
    }
}
