package com.example.vinayak.whatchat;

import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.okhttp.internal.Util;
import com.squareup.picasso.Picasso;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {

    private List<Messages> mMessageList;
    private FirebaseAuth mAuth;

    public MessageAdapter(List<Messages> mMessageList){
        this.mMessageList=mMessageList;
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.message_single_layout, parent, false);
        return new MessageViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull final MessageViewHolder holder, int position) {
        mAuth=FirebaseAuth.getInstance();
        final String current_user_id = mAuth.getUid();

        Messages c = mMessageList.get(position);
        final String from_user_id=c.getFrom();
        String message_type = c.getType();

        holder.messageText.setBackgroundColor(Color.WHITE);
        holder.messageText.setTextColor(Color.BLACK);
        if(message_type.equals("text")) {

            if(from_user_id.equals(current_user_id)){
                FirebaseDatabase.getInstance().getReference().child("Users").child(current_user_id)
                        .addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                String userThumbImg = dataSnapshot.child("thumb_image").getValue().toString();
                                holder.nameText.setText( "You"/*user_name*/);
                                Picasso.with(holder.profileImage.getContext()).load(userThumbImg)
                                       .placeholder(R.drawable.default_avatar).into(holder.profileImage);
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });
            }else{
                FirebaseDatabase.getInstance().getReference().child("Users").child(from_user_id)
                        .addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                String user_name = dataSnapshot.child("name").getValue().toString();
                                String userThumbImg = dataSnapshot.child("thumb_image").getValue().toString();
                                holder.nameText.setText(user_name);
                                Picasso.with(holder.profileImage.getContext()).load(userThumbImg)
                                       .placeholder(R.drawable.default_avatar).into(holder.profileImage);

                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });
            }

            holder.messageText.setText(c.getMessage());


            DateFormat dateFormat = new SimpleDateFormat("HH:mm");
            //Date date = new Date();
            String date_string = dateFormat.format(c.getTime());
            holder.timeText.setText(date_string);
            holder.messageImage.setVisibility(View.INVISIBLE);
        } else {
            holder.messageText.setVisibility(View.INVISIBLE);

            DateFormat dateFormat = new SimpleDateFormat("HH:mm");
            //Date date = new Date();
            String date_string = dateFormat.format(c.getTime());
            holder.timeText.setText(date_string);
            Picasso.with(holder.profileImage.getContext()).load(c.getMessage())
                    .placeholder(R.drawable.default_avatar).into(holder.messageImage);
        }
        long previousTs = 0;
        if(position>1){
            Messages pm = mMessageList.get(position-1);
            previousTs = pm.getTime();
        }
        setTimeTextVisibility(c.getTime(), previousTs, holder.timeSeparator);

    }

    private void setTimeTextVisibility(long ts1, long ts2, TextView timeSeparator){

        if(ts2==0){
            timeSeparator.setVisibility(View.VISIBLE);
            DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
            String date_string = dateFormat.format(ts1);
            timeSeparator.setText(date_string);
        }else {
            Calendar cal1 = Calendar.getInstance();
            Calendar cal2 = Calendar.getInstance();
            cal1.setTimeInMillis(ts1);
            cal2.setTimeInMillis(ts2);

            boolean sameDay = cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                    cal1.get(Calendar.MONTH) == cal2.get(Calendar.MONTH) &&
                    cal1.get(Calendar.DAY_OF_MONTH) == cal2.get(Calendar.DAY_OF_MONTH);

            if(sameDay){
                timeSeparator.setVisibility(View.INVISIBLE);
                timeSeparator.setText("");
            }else {
                timeSeparator.setVisibility(View.VISIBLE);
                DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
                String date_string = dateFormat.format(ts2);
                timeSeparator.setText(date_string);
            }

        }
    }

    @Override
    public int getItemCount() {
        return mMessageList.size();
    }

    public class MessageViewHolder extends RecyclerView.ViewHolder {

        public TextView messageText, nameText, timeText, timeSeparator;
        public CircleImageView profileImage;
        public ImageView messageImage;

        public MessageViewHolder(View itemView) {
            super(itemView);

            messageText=(TextView)itemView.findViewById(R.id.message_text_layout);
            nameText=(TextView)itemView.findViewById(R.id.name_text_layout);
            timeText=(TextView)itemView.findViewById(R.id.time_text_layout);
            timeSeparator =(TextView)itemView.findViewById(R.id.timeText);
            profileImage=(CircleImageView) itemView.findViewById(R.id.message_profile_layout);
            messageImage=(ImageView) itemView.findViewById(R.id.message_image_layout);
        }
    }

}
