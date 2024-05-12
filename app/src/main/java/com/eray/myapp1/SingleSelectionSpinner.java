package com.eray.myapp1;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import java.util.List;

public class SingleSelectionSpinner extends androidx.appcompat.widget.AppCompatSpinner implements AdapterView.OnItemSelectedListener {

    private List<String> items;
    private String selectedStudent;
    private SingleSelectionSpinnerListener listener;

    public SingleSelectionSpinner(Context context) {
        super(context);
        setOnItemSelectedListener(this);
    }

    public SingleSelectionSpinner(Context context, AttributeSet attrs) {
        super(context, attrs);
        setOnItemSelectedListener(this);
    }

    public SingleSelectionSpinner(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setOnItemSelectedListener(this);
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        selectedStudent = items.get(position);
        if (listener != null) {
            listener.onItemSelected(selectedStudent);
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
    }

    public void setItems(List<String> items) {
        this.items = items;
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, items);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        setAdapter(adapter);
    }

    public void setListener(SingleSelectionSpinnerListener listener) {
        this.listener = listener;
    }

    public interface SingleSelectionSpinnerListener {
        void onItemSelected(String selectedItem);
    }

    public String getSelectedStudent() {
        return selectedStudent;
    }
}

