package com.example.administrator.speech;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.example.administrator.speech.gen.DBuserinfo;

import java.util.List;

public class NormalRecyclerViewAdapter extends RecyclerView.Adapter<NormalRecyclerViewAdapter.NormalTextViewHolder> {
    private final LayoutInflater mLayoutInflater;
    private final Context mContext;
    //private String[] mTitles;
    List<DBuserinfo> all;
    public NormalRecyclerViewAdapter(Context context,List<DBuserinfo> all) {
     //   mTitles = context.getResources().getStringArray(R.array.items);
        this.all=all;
        mContext = context;
        mLayoutInflater = LayoutInflater.from(context);
    }

    public void setData(List<DBuserinfo> alls){
        all.clear();
        all.addAll(alls);
        this.notifyDataSetChanged();
    }

    @Override
    public NormalTextViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new NormalTextViewHolder(mLayoutInflater.inflate(R.layout.item_text, parent, false));
    }

    @Override
    public void onBindViewHolder(NormalTextViewHolder holder, int position) {
       // holder.mTextView.setText(mTitles[position]);

        String audioText = all.get(position).getAudioText();

       // Log.d("ssssssss","audioText:"+audioText);

        String audio = all.get(position).getAudio();
        int isRecoder = all.get(position).getIsRecoder();


        Log.d("ssssssss","isRecoder:"+isRecoder);
        if (!" ".equals(audioText)){
            holder.mTextView.setTextColor(Color.DKGRAY);
            holder.mTextView.setText(audioText);
        }else{
            holder.mTextView.setTextColor(Color.GRAY);
            holder.mTextView.setText(audio);
        }
        if (isRecoder==1){
            holder.mTextView.setBackgroundColor(Color.RED);
        }else {
            holder.mTextView.setBackgroundColor(Color.WHITE);
        }
    }

    @Override
    public int getItemCount() {
        return all == null ? 0 : all.size();
    }

    public  class NormalTextViewHolder extends RecyclerView.ViewHolder {
        TextView mTextView;

        NormalTextViewHolder(View view) {
            super(view);
            mTextView = view.findViewById(R.id.text_view);
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.d("NormalTextViewHolder", "onClick--> position = " + getPosition()+all.get(getPosition()).getIsRecoder());
                    Intent intent = new Intent(mContext,AudioRecorderActivity.class);
                    intent.putExtra("pos",getPosition());
                    intent.putExtra("id",all.get(getPosition()).getId());
                    intent.putExtra("isRecoder",all.get(getPosition()).getIsRecoder());
                    intent.putExtra("audioText",all.get(getPosition()).getAudioText());
                    intent.putExtra("audio",all.get(getPosition()).getAudio());
                    mContext.startActivity(intent);

                }
            });
        }
    }
}