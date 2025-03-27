package com.gg.busStation.ui.layout.preference;

import android.content.Context;
import android.util.AttributeSet;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.PopupMenu;
import androidx.preference.ListPreference;
import androidx.preference.PreferenceViewHolder;

public class MenuPreference extends ListPreference {
    private View anchor;

    public MenuPreference(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public MenuPreference(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public MenuPreference(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public MenuPreference(@NonNull Context context) {
        super(context);
    }

    @Override
    public void onBindViewHolder(@NonNull PreferenceViewHolder holder) {
        super.onBindViewHolder(holder);
        anchor = holder.itemView;
    }

    @Override
    protected void onClick() {
        showDialog();
    }

    protected void showDialog() {
        final PopupMenu popup = new PopupMenu(getContext(), anchor);
        final Menu menu = popup.getMenu();

        for (int i = 0; i < getEntries().length; i++) {
            MenuItem item = menu.add(1, i, Menu.NONE, getEntries()[i]);
            item.setChecked(item.getTitle().equals(getEntry()));
        }
        menu.setGroupCheckable(1, true, true);

        popup.setOnMenuItemClickListener(item -> {
            popup.dismiss();
            String value = getEntryValues()[item.getItemId()].toString();
            if (callChangeListener(value))
                setValue(value);
            return true;
        });
        popup.show();
    }
}
