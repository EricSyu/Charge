package homework5.person.charge;

import android.content.ContentResolver;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Layout;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;

public class MainActivity extends AppCompatActivity {

    private ListView listView;

    private BillListAdapter billListAdapter;

    private File file;
    private final String filePath = "/sdcard/ChargeStorage/";
    private final String typeFileName = "TypeFile.txt";
    private final String[] defaultType = {"運輸交通", "餐飲食品", "生活用品"};

    private ArrayList<String> typeArray;

    private ImageButton dialog_imgBtn;
    private EditText dialog_editText_date, dialog_editText_item, dialog_editText_cost;
    private Spinner dialog_spinner;
    private ArrayAdapter dialog_arrayAdapter;
    private String picPath = null;

    private final static int PHOTO = 99 ;

    private ChargeDB chargeDB;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        typeArray = new ArrayList<String>();
        listView = (ListView) findViewById(R.id.listView);

        chargeDB = new ChargeDB(getApplicationContext());
        billListAdapter = new BillListAdapter(this, chargeDB.getAll());
        listView.setAdapter(billListAdapter);

        initType();
        calCost();
    }

    private void calCost(){
        ArrayList<Bill> allBills = chargeDB.getAll();
        HashSet<String> date = new HashSet<>();
        int sum = 0;
        for (int i=0; i<allBills.size(); i++){
            sum += allBills.get(i).getCost();

            date.add(allBills.get(i).getDate());
        }
        int dayAverage = (date.size() < 1? sum : sum / date.size());
        TextView textView_totalCost = (TextView) findViewById(R.id.textView4);
        textView_totalCost.setText(sum+"");
        TextView textView_dayCost = (TextView) findViewById(R.id.textView6);
        textView_dayCost.setText(dayAverage+"");
    }

    private void initType(){
        file = new File(filePath+typeFileName);
        if(!file.exists()){
            try {
                new File(filePath).mkdir();
                FileWriter writer = new FileWriter(file, true);
                for(int i=0; i<defaultType.length; i++){
                    writer.write(defaultType[i]+"\n");
                    typeArray.add(defaultType[i]);
                }
                writer.close();
            }catch (Exception e){
                Toast.makeText(this, "創建檔案錯誤", Toast.LENGTH_SHORT).show();
            }
        }
        else {
            try {
                BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
                String s = "";
                while ((s = reader.readLine()) != null){
                    typeArray.add(s);
                }
                reader.close();
            }catch (Exception e){
                Toast.makeText(this, "讀取檔案錯誤", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        chargeDB.close();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_add_bill) {
            AlertDialog dialog = new AlertDialog.Builder(this)
                    .setTitle("新增帳單")
                    .setView(R.layout.add_bill_dialogview)
                    .setCancelable(false)
                    .setPositiveButton("新增", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            boolean b = dialog_editText_date.getText().toString().matches("((19|20)\\d\\d)-(0?[1-9]|1[012])-(0?[1-9]|[12][0-9]|3[01])");
                            if(!b){
                                Toast.makeText(MainActivity.this, "日期格式錯誤", Toast.LENGTH_SHORT).show();
                                return;
                            }
                            if(dialog_editText_date.getText().toString().equals("") | dialog_editText_item.getText().toString().equals("") | dialog_editText_cost.getText().toString().equals("")){
                                Toast.makeText(MainActivity.this, "資料輸入不完全", Toast.LENGTH_SHORT).show();
                                return;
                            }

                            Bill bill = new Bill(dialog_editText_date.getText().toString(), dialog_spinner.getSelectedItem().toString(), dialog_editText_item.getText().toString(), Integer.parseInt(dialog_editText_cost.getText().toString()), picPath);
                            chargeDB.insert(bill);

                            picPath = null;

                            billListAdapter.setBills(chargeDB.getAll());
                            billListAdapter.notifyDataSetChanged();

                            calCost();
                        }
                    })
                    .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    })
                    .show();

            dialog_editText_date = (EditText) dialog.findViewById(R.id.editText);
            Date date = new Date(System.currentTimeMillis());
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
            String currentDate = format.format(date);
            dialog_editText_date.setText(currentDate);

            dialog_spinner = (Spinner) dialog.findViewById(R.id.spinner);
            dialog_arrayAdapter = new ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, typeArray);
            dialog_spinner.setAdapter(dialog_arrayAdapter);

            dialog_imgBtn = (ImageButton) dialog.findViewById(R.id.imageButton);
            dialog_imgBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent();
                    intent.setType("image/*");
                    intent.setAction(Intent.ACTION_GET_CONTENT);
                    startActivityForResult(intent, PHOTO);
                }
            });

            dialog_editText_item = (EditText) dialog.findViewById(R.id.editText2);
            dialog_editText_cost = (EditText) dialog.findViewById(R.id.editText3);

            return true;
        }
        else if(id == R.id.action_add_type){
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
            LinearLayout linearLayout = new LinearLayout(this);
            linearLayout.setLayoutParams(params);
            linearLayout.setPadding(80,50,80,0);

            final EditText editText = new EditText(this);
            editText.setLayoutParams(params);
            linearLayout.addView(editText);

            AlertDialog dialog = new AlertDialog.Builder(this)
                    .setTitle("新增帳單類型")
                    .setView(linearLayout)
                    .setPositiveButton("新增", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            try {
                                FileWriter writer = new FileWriter(file, true);
                                writer.write(editText.getText().toString() + "\n");
                                typeArray.add(editText.getText().toString());
                                writer.close();
                                Toast.makeText(MainActivity.this, "新增成功", Toast.LENGTH_SHORT).show();
                            } catch (Exception e) {
                                Toast.makeText(MainActivity.this, "寫入檔案錯誤", Toast.LENGTH_SHORT).show();
                            }
                        }
                    })
                    .setNegativeButton("取消", null)
                    .show();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PHOTO && data != null){
            Uri uri = data.getData();
            ContentResolver cr = getContentResolver();

            try{
                Bitmap bitmap = BitmapFactory.decodeStream(cr.openInputStream(uri));

                if(bitmap.getWidth()>bitmap.getHeight()) scalePic(bitmap, dialog_imgBtn.getHeight());
                else scalePic(bitmap, dialog_imgBtn.getWidth());

                picPath = filePath+billListAdapter.getCount()+".jpeg";
                FileOutputStream out = new FileOutputStream(picPath);
                bitmap.compress(Bitmap.CompressFormat.JPEG, 20, out);

                dialog_imgBtn.setBackgroundColor(0x00FFFFFF);
            }catch (Exception e){

            }
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    private void scalePic(Bitmap bitmap,int box) {
        float mScale = 1 ;

        if(bitmap.getWidth() > box ){
            mScale = (float)box/(float)bitmap.getWidth();

            Matrix mMat = new Matrix() ;
            mMat.setScale(mScale, mScale);

            Bitmap mScaleBitmap = Bitmap.createBitmap(bitmap,0,0,bitmap.getWidth(),bitmap.getHeight(),mMat,false);
            dialog_imgBtn.setImageBitmap(mScaleBitmap);
        }
        else dialog_imgBtn.setImageBitmap(bitmap);
    }

    class BillListAdapter extends BaseAdapter {
        private LayoutInflater myInflater;
        private ArrayList<Bill> bills;

        public BillListAdapter(Context c, ArrayList<Bill> bills) {
            myInflater = LayoutInflater.from(c);
            this.bills = bills;
        }

        @Override
        public int getCount() {
            return bills.size();
        }

        @Override
        public Object getItem(int position) {
            return bills.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        public void setBills(ArrayList<Bill> bills) {
            this.bills = bills;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            convertView = myInflater.inflate(R.layout.bill_list_view, null);
            ImageView imageView = (ImageView) convertView.findViewById(R.id.imageView);
            TextView textView_type_item = (TextView) convertView.findViewById(R.id.textView7);
            TextView textView_date = (TextView) convertView.findViewById(R.id.textView8);
            TextView textView_cost = (TextView) convertView.findViewById(R.id.textView9);

            if(bills.get(position).getPicturePath() != null){
                File imageFile = new File(bills.get(position).getPicturePath());
                if(imageFile.exists()){
                    Bitmap bitmap = getSmallBitmap(imageFile.getAbsolutePath());
                    imageView.setImageBitmap(bitmap);
                }
                else imageView.setImageResource(android.R.drawable.ic_menu_crop);
            }
            else imageView.setImageResource(android.R.drawable.ic_menu_crop);

            textView_type_item.setText("["+bills.get(position).getType()+"] "+bills.get(position).getItem());
            textView_date.setText(bills.get(position).getDate()+"");
            textView_cost.setText("NT$ "+bills.get(position).getCost());

            return convertView;
        }

        public Bitmap getSmallBitmap(String filePath) {
            final BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(filePath, options);

            options.inSampleSize = calculateInSampleSize(options, 80, 80);

            options.inJustDecodeBounds = false;

            return BitmapFactory.decodeFile(filePath, options);
        }

        public int calculateInSampleSize(BitmapFactory.Options options,int reqWidth, int reqHeight) {
            final int height = options.outHeight;
            final int width = options.outWidth;
            int inSampleSize = 1;

            if (height > reqHeight || width > reqWidth) {
                final int heightRatio = Math.round((float) height/ (float) reqHeight);
                final int widthRatio = Math.round((float) width / (float) reqWidth);
                inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
            }
            return inSampleSize;
        }
    }
}
