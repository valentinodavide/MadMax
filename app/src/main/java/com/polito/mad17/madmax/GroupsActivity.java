package com.polito.mad17.madmax;

// JAVA IMPORTS
import java.util.ArrayList;

// ANDROID IMPORTS
import android.database.DataSetObserver;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

class GroupItem {
    private String name;

    public GroupItem(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public String toString() {  return name; }
}

public class GroupsActivity extends AppCompatActivity {

    private ListView listView;
    private ArrayList<GroupItem> arrayGroup = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_groups);

        GroupItem item1 = new GroupItem("Gruppo1");
        GroupItem item2 = new GroupItem("Gruppo2");

        arrayGroup.add(item1);
        arrayGroup.add(item2);

        listView = (ListView) findViewById(R.id.listGroups);

        ListAdapter listAdapter = new ListAdapter() {
            @Override
            public boolean areAllItemsEnabled() {
                return false;
            }

            @Override
            public boolean isEnabled(int position) {
                return false;
            }

            @Override
            public void registerDataSetObserver(DataSetObserver observer) {

            }

            @Override
            public void unregisterDataSetObserver(DataSetObserver observer) {

            }

            @Override
            public int getCount() {
                return arrayGroup.size();
            }

            @Override
            public Object getItem(int position) {
                return arrayGroup.get(position);
            }

            @Override
            public long getItemId(int position) {
                return 0;
            }

            @Override
            public boolean hasStableIds() {
                return false;
            }

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {

                if(convertView == null) {
                    convertView = getLayoutInflater().inflate(R.layout.activity_group_row, parent, false);
                }
                else {
                    ImageView image = (ImageView) convertView.findViewById(R.id.groupImage);
                    TextView name = (TextView) convertView.findViewById(R.id.groupName);

                    GroupItem groupItem = arrayGroup.get(position);
                    name.setText(groupItem.getName());
                }

                return convertView;
            }

            @Override
            public int getItemViewType(int position) {
                return 0;
            }

            @Override
            public int getViewTypeCount() {
                return 0;
            }

            @Override
            public boolean isEmpty() {
                if(arrayGroup.isEmpty()) {
                    return true;
                }
                else {
                    return true;
                }
            }
        };

        listView.setAdapter(listAdapter);
    }
}
