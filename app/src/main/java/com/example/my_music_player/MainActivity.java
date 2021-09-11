package com.example.my_music_player;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
  ListView listView;
  String[] items; /** to strore song items **/
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        listView=findViewById(R.id.listViewSong);
        runtimePermition();
    }
    /** for RUNTIME PERMITION **/
     public  void runtimePermition(){
        Dexter.withContext(this).withPermissions(Manifest.permission.READ_EXTERNAL_STORAGE,Manifest.permission.RECORD_AUDIO)
                .withListener(new MultiplePermissionsListener() {
                    @Override
                    public void onPermissionsChecked(MultiplePermissionsReport multiplePermissionsReport) {
                        displaySongs();
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(List<PermissionRequest> list, PermissionToken permissionToken) {
                         permissionToken.continuePermissionRequest();
                    }
                }).check();
    }


    /** to find songs form extermal storage **/
    public ArrayList<File> findSong (File file){
        ArrayList<File> arrayList =new ArrayList<>();
        File[] files=file.listFiles();

        /** now check its mp3 file or not **/
            for(File singlefile:files){
                /** CHECK FOR DIRECTORY AND NOT HIDDEN **/
                if(singlefile.isDirectory() && !singlefile.isHidden()){
                   arrayList.addAll(findSong(singlefile)); /** call again to find songs in directory **/
                }else{
                    /** check mp3 file or not **/
     if(singlefile.getName().endsWith(".mp3") || singlefile.getName().endsWith(".wav")){
                        arrayList.add(singlefile);
                    }
                }
            }
            return arrayList;
    }


    /** TO DISPLAY THE SONGS **/
     void displaySongs(){
        final  ArrayList<File> mySongs=findSong(Environment.getExternalStorageDirectory());
        items=new String[mySongs.size()];
        for(int i=0;i<mySongs.size();i++){
            items[i]=mySongs.get(i).getName().toString().replace(".mp3" ,"").replace(".wav","");
        }
        ArrayAdapter<String> myAdapter =new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1,items);
        listView.setAdapter(myAdapter);
        customAdapter customAdapter=new customAdapter();
        listView.setAdapter(customAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String songName = (String) listView.getItemAtPosition(position);

              startActivity(new Intent(getApplicationContext(),PlayerActivity.class)
                      .putExtra("songs",mySongs)
                      .putExtra("songname",songName)
                      .putExtra("pos",position));
            }
        });
    }
    /** custom adapter **/


 class  customAdapter extends BaseAdapter{

        @Override
        public int getCount() {
            return items.length;
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View myview = getLayoutInflater().inflate(R.layout.list_item,null);
            TextView textsong= myview.findViewById(R.id.txtsongname);
            textsong.setSelected(true);
            textsong.setText(items[position]);
            return myview;
        }
    }
}