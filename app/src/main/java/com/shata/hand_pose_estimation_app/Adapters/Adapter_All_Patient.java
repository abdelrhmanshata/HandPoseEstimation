package com.shata.hand_pose_estimation_app.Adapters;

import android.content.Context;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.shata.hand_pose_estimation_app.Models.ModelPatient;
import com.shata.hand_pose_estimation_app.R;

import java.util.List;

public class Adapter_All_Patient extends RecyclerView.Adapter<Adapter_All_Patient.ProductViewHolder> {

    private Context mContext;
    private List<ModelPatient> mPatients;
    private OnItemClickListener mListener;

    public Adapter_All_Patient(Context mContext, List<ModelPatient> mPatients) {
        this.mContext = mContext;
        this.mPatients = mPatients;
    }

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(mContext).inflate(R.layout.raw_patient, parent, false);
        return new ProductViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {

        ModelPatient patient = mPatients.get(position);
        holder.Patient_Name.setText(patient.getPatientName());
        holder.Patient_ID.setText("" + (position + 1));

/*
        Picasso.get().load(upload.getmImageUri()).fit().placeholder(R.mipmap.ic_launcher)
                .centerCrop().into(holder.imageView);
*/
    }

    @Override
    public int getItemCount() {
        return mPatients.size();
    }

    public void setOnItemClickListener(OnItemClickListener listener) {

        mListener = listener;
    }

    public interface OnItemClickListener {
        void onItem_Product_Click(int position);

       /* void onEdit_Product_Click(int position);

        void onDelete_Product_Click(int position);*/

    }

    public class ProductViewHolder extends RecyclerView.ViewHolder
            implements View.OnClickListener
            // ,View.OnCreateContextMenuListener
            // ,MenuItem.OnMenuItemClickListener
    {

        public TextView Patient_Name, Patient_ID;

        public ProductViewHolder(@NonNull View itemView) {
            super(itemView);

            Patient_Name = itemView.findViewById(R.id.patientName);
            Patient_ID = itemView.findViewById(R.id.patientID);

            itemView.setOnClickListener(this);

           /* if (!mContext.getClass().equals(Show_Product_Activities_Activity.class)) {
                itemView.setOnCreateContextMenuListener(this);
            }*/

        }

        @Override
        public void onClick(View view) {
            if (mListener != null) {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    mListener.onItem_Product_Click(position);
                }
            }
        }

      /*  @Override
        public void onCreateContextMenu(ContextMenu contextMenu, View view, ContextMenu.ContextMenuInfo contextMenuInfo) {
            contextMenu.setHeaderTitle("Select Action");

            MenuItem Edit = contextMenu.add(Menu.NONE, 1, 1, "Edit");
            Edit.setIcon(R.drawable.ic_edit_black);

            MenuItem delete = contextMenu.add(Menu.NONE, 2, 2, "Delete");
            delete.setIcon(R.drawable.ic_delete_black_24dp);

            Edit.setOnMenuItemClickListener(this);
            delete.setOnMenuItemClickListener(this);

        }

        @Override
        public boolean onMenuItemClick(MenuItem menuItem) {
            if (mListener != null) {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    switch (menuItem.getItemId()) {

                        case 1:
                            mListener.onEdit_Product_Click(position);
                            return true;

                        case 2:
                            mListener.onDelete_Product_Click(position);
                            return true;


                    }
                }
            }
            return false;
        }

       */
    }
}
