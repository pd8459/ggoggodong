package com.example.ggoggodong;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class BookAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final List<Book> books;
    private final Context context;
    private final OnBookClickListener listener;

    public interface OnBookClickListener {
        void onClick(Book book);
    }

    public BookAdapter(List<Book> books, Context context, OnBookClickListener listener) {
        this.books = books;
        this.context = context;
        this.listener = listener;
    }

    @Override
    public int getItemViewType(int position) {
        return books.get(position).isCreateButton() ? 0 : 1;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == 0) {
            View view = LayoutInflater.from(context).inflate(R.layout.item_createbook, parent, false);
            return new CreateViewHolder(view);
        } else {
            View view = LayoutInflater.from(context).inflate(R.layout.item_book, parent, false);
            return new BookViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Book book = books.get(position);
        if (getItemViewType(position) == 0) {
            ((CreateViewHolder) holder).bind(book);
        } else {
            ((BookViewHolder) holder).bind(book);
        }
    }

    @Override
    public int getItemCount() {
        return books.size();
    }

    class BookViewHolder extends RecyclerView.ViewHolder {
        private final TextView title;

        BookViewHolder(View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.book_title);
            itemView.setOnClickListener(v -> listener.onClick(books.get(getAdapterPosition())));
        }

        void bind(Book book) {
            title.setText(book.getTitle());
        }
    }

    class CreateViewHolder extends RecyclerView.ViewHolder {
        private final ImageView image;

        CreateViewHolder(View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.image_createbook);
            itemView.setOnClickListener(v -> listener.onClick(books.get(getAdapterPosition())));
        }

        void bind(Book book) {
            // 추가로 꾸미고 싶다면 여기에서
        }
    }
}
