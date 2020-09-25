package com.suek.ex38externalstorage;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

public class MainActivity extends AppCompatActivity {

    EditText et;
    TextView tv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        et= findViewById(R.id.et);
        tv= findViewById(R.id.tv);
    }


//1)
    //Save
    public void clickSave(View view) {
        //외장메모리(SD card)가 있는지 부터 확인..
        String state= Environment.getExternalStorageState();   //외장메모리가 있는지 확인해서 문자열로 알려줌

        //외장메모리(state)가 연결(mounted)되어있지 않은지 확인..
        if(!state.equals(Environment.MEDIA_MOUNTED)){    // ! 부정
            Toast.makeText(this, "SD card is not mounted", Toast.LENGTH_SHORT).show();
            return;     //return 을 {}안에 쓰면 이 함수가 끝남
        }

        //파일에 저장할 데이터를 EditText 에서 얻어오기
        String data= et.getText().toString();
        et.setText("");

        //데이터를 저장할 파일의 경로부터 얻어오기
        File path;

        //액티비티에게 외부메모리에 앱한테 할당한 고유한 폴더경로 얻어오기
        File[] dirs= getExternalFilesDirs("MyDir");
        path= dirs[0];   //dir 한테 첫번째 경로 선택

        tv.setText(path.getPath());    //테스트- 위에서 선택한 경로를 출력해보기

        //위의 경로(path)와 저장할 파일명을 결합한 File 객체 생성
        File file= new File(path, "Data.txt");

        try {
            FileWriter fw= new FileWriter(file, true);   //append 덧붙이기, 이어쓰기
            PrintWriter writer= new PrintWriter(fw);  //보조 writer
            writer.println(data);
            writer.flush();
            writer.close();

            Toast.makeText(this, "Saved", Toast.LENGTH_SHORT).show();

        } catch (IOException e) {
            e.printStackTrace();
        }


    }


//2)
    //Load
    public void clickLoad(View view) {

        String state= Environment.getExternalStorageState();

        if(state.equals(Environment.MEDIA_MOUNTED)||state.equals((Environment.MEDIA_MOUNTED_READ_ONLY))){
            //위의 조건에 다 일치하면
            //읽을 수 있는 상태...

            File path;
            File[] dirs= getExternalFilesDirs("MyDir");   //MyDir 저장할때와 마찬가지로 로드할때도 같은 경로 써주기
            path= dirs[0];
            File file= new File(path, "Data.tct");    //파일명이 잘못되었을때를 대비해서 catch 문에 토스트 띄우기


            try {
                FileReader fr= new FileReader(file);
                BufferedReader reader= new BufferedReader(fr);

                StringBuffer buffer= new StringBuffer();

                while(true){
                    String line= reader.readLine();
                    if(line==null) break;
                    buffer.append(line + "\n");
                }

                tv.setText(buffer.toString());

            } catch (FileNotFoundException e) {
                //e.printStackTrace();    //토스트를 띄울때는 이거 //e.printStackTrace(); 지우고
                Toast.makeText(this, "로드를 실패했습니다.(저장됐던 파일이 안보여짐)", Toast.LENGTH_SHORT).show();

            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }








    //4)
    //requestPermissions()를 실행해서 보여진 다이얼로그의
    //허가/거부(allow/ deny)를 선택했을때 자동으로 실행되는 콜백메소드
    //마치, startActivityForResult()했을때.. onActivityResult()자동호출
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode){
            case 10:

                if(grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    Toast.makeText(this, "저장 가능합니다.", Toast.LENGTH_SHORT).show();

                }else{
                    Toast.makeText(this, "외부 저장소 사용불가", Toast.LENGTH_SHORT).show();
                }

                break;
        }
    }




    //3)
    public void clickBtn(View view) {
        //외부메모리에서 본인앱에게 할당된 고유한 경로가 아닌곳을 사용할때
        //무조건 퍼미션 필요함

        String state= Environment.getExternalStorageState();
        if(!state.equals(Environment.MEDIA_MOUNTED)){
            Toast.makeText(this, "외부저장소 없음", Toast.LENGTH_SHORT).show();
        }

        //동적퍼미션 작업 (앱 실행중에 다이얼로그가 보이면서 퍼미션 체크)
        //이 앱에서 저장소를 사용하는 퍼미션이 허가되어 있는지 체크
        //이 동적퍼미션은 api 23버전(마쉬멜로 버전)'이상'에서만 체크
        if(Build.VERSION.SDK_INT>= Build.VERSION_CODES.M){    //M버젼. 마쉬멜로

            int checkResult= checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE);

            //외부저장소 사용허가가 거부(denied)되었는지 확인
            if(checkResult == PackageManager.PERMISSION_DENIED){
                //허가가 안되어있으면 퍼미션을 허용해 달라고 요청하는 다이얼로그 보이는 메소드 실행
                String[] permissions= new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};   //ex)  String[]{2,3,4....} =배열에 값을 넣은것임
                requestPermissions(permissions, 10);

                return;
            }
        }

        //퍼미션이 허가된 이후에 실행되는 영역
        //SD card 의 '특정위치' 저장하기 (-> download 파일에 저장하기)
        File path= Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        File file= new File(path, "aaa.txt");     //File을 쓰면 굳이 Outstream...등등 을 안하고 바로 FileWriter 쓰는게 가능

        try {
            FileWriter fr= new FileWriter(file, true);
            PrintWriter writer= new PrintWriter(fr);
            writer.println(et.getText().toString());
            writer.flush();
            writer.close();

            tv.setText("저장이 되었습니다.");
        } catch (IOException e) {
            e.printStackTrace();
        }


    }
}//MainActivity
