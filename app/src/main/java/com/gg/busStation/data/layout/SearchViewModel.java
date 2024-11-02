package com.gg.busStation.data.layout;

import androidx.lifecycle.ViewModel;

import java.util.ArrayList;
import java.util.List;

public class SearchViewModel extends ViewModel {
    public List<ListItemData> listItemData = new ArrayList<>();
    public String outputText = "";
}
