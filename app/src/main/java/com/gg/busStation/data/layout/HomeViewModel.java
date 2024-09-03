package com.gg.busStation.data.layout;

import androidx.databinding.ObservableField;
import androidx.lifecycle.ViewModel;

import java.util.List;

public class HomeViewModel extends ViewModel {
    public ObservableField<List<ListItemData>> data = new ObservableField<>();
    public int scrollOffset = 0;
}