package com.example.demoregister.admin;

import android.widget.Filter;

import com.example.demoregister.model.CreateMenuModel;

import java.util.ArrayList;

public class Filterproduct extends Filter {

    private AdapterMenuStaff adapter;
    private ArrayList<CreateMenuModel> filterList;

    public Filterproduct(AdapterMenuStaff adapter, ArrayList<CreateMenuModel> filterList) {
        this.adapter = adapter;
        this.filterList = filterList;
    }


    //untuk search field customer search dengan key in ...
    @Override
    protected FilterResults performFiltering(CharSequence constraint) {
        FilterResults results = new FilterResults();
        //validate data for search query
        if (constraint != null && constraint.length()>0){
            //search filed not empty, searching something,perform search

            //change to upper case, to make case sensitive
            constraint = constraint.toString().toUpperCase();
            //store our filtered list
            ArrayList<CreateMenuModel> filteredModels = new ArrayList<>();

            for (int i=0; i<filterList.size(); i++){
                //check, search by menu name and category
                if (filterList.get(i).getMenuName().toUpperCase().contains(constraint) ||
                        filterList.get(i).getCategory().toUpperCase().contains(constraint)){

                    //add filtered data to list
                    filteredModels.add(filterList.get(i));

                }
            }
            results.count = filteredModels.size();
            results.values = filteredModels;
        }
        else {
            //search filed empty, not searching, return original/all/complete list
            results.count = filterList.size();
            results.values = filterList;
        }
        return results;
    }



    //lepas perform validate data n dh dapat value search akan hantar value ke model class dekat notifydata set change.
    @Override
    protected void publishResults(CharSequence constraint, FilterResults results) {
        adapter.createMenuModelList = (ArrayList<CreateMenuModel>) results.values;
        //refresh adapter
        adapter.notifyDataSetChanged();
    }
}
