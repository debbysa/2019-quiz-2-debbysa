package id.ac.polinema.todoretrofit.adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.support.annotation.NonNull;
import android.support.v7.widget.AlertDialogLayout;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import java.util.List;

import id.ac.polinema.todoretrofit.R;
import id.ac.polinema.todoretrofit.models.Todo;

public class TodoAdapter extends RecyclerView.Adapter<TodoAdapter.ViewHolder> {

    private Context context;
    private List<Todo> items;
    private AdapterCallback mAdapterCallback;

    public TodoAdapter(Context context) {
        this.context = context;
        this.mAdapterCallback = (AdapterCallback) context;
    }

    public void setItems(List<Todo> items) {
        this.items = items;
        this.notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_todo, viewGroup, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int i) {
        Todo todo = items.get(i);
        viewHolder.bind(todo);

        if(todo.getDone() == true){
            viewHolder.image.setImageResource(R.drawable.check);
        } else {
            viewHolder.image.setImageResource(R.drawable.check_empty);
        }
    }

    @Override
    public int getItemCount() {
        return (items != null) ? items.size() : 0;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView todoText;
        ImageView image;
        public TextView buttonViewOption;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            todoText = itemView.findViewById(R.id.text_todo);
            image = itemView.findViewById ( R.id.imageView );
            buttonViewOption = itemView.findViewById((R.id.textViewOptions));
        }

        public void bind(final Todo todo) {
            todoText.setText(todo.getTodo());

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mAdapterCallback.onClickItem(todo);
                }
            });

            buttonViewOption.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    //creating a pop up menu
                    PopupMenu popup = new PopupMenu(context, buttonViewOption);
                    popup.inflate(R.menu.options_menu);
                    //adding clicklistener
                    popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem item) {
                            switch (item.getItemId()){
                                case R.id.update :
                                    mAdapterCallback.onUpdateMenuClick(todo);
                                    break;
                                case R.id.delete :
                                    final AlertDialog.Builder dialogHapus = new AlertDialog.Builder(context);

                                    dialogHapus.setTitle("Warning!");
                                    dialogHapus.setMessage("Yakin Hapus? ");
                                    dialogHapus.setPositiveButton("Ya", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            mAdapterCallback.onDeleteMenuClick(todo);
                                        }
                                    });

                                    dialogHapus.setNegativeButton("Tidak", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            dialog.dismiss();
                                        }
                                    });

                                    dialogHapus.show();

                                    break;
                            }
                            return false;
                        }
                    });
                    //displaying popup
                    popup.show();
                }
            });
        }
    }

    public interface AdapterCallback {
        void onDeleteMenuClick(Todo todo);
        void onUpdateMenuClick(Todo todo);
        void onClickItem(Todo todo);
    }
}
