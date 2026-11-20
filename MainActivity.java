package id.org.muhammadiyah.keuangan;

import android.app.*;
import android.os.*;
import android.content.*;
import android.database.*;
import android.database.sqlite.*;
import android.net.Uri;
import android.view.*;
import android.widget.*;
import android.graphics.Color;
import android.text.InputType;
import java.io.*;
import java.text.*;
import java.util.*;

public class MainActivity extends Activity {
    DB db; LinearLayout root, body; String photoNota="", photoBarang="";
    EditText eDate,eDesc,eAmount,eSource; Spinner sType,sArea,sCategory; int pickTarget=0;
    final String[] types={"Pemasukan","Pengeluaran"};
    final String[] areas={"Kebutuhan Kantor","Pembangunan Bangunan"};
    final String[] cats={"Operasional kantor","ATK dan perlengkapan","Pemeliharaan","Material bangunan","Upah tenaga kerja","Transportasi","Konsumsi","Lain-lain"};
    int blue=Color.rgb(20,63,105), pale=Color.rgb(239,244,249);

    public void onCreate(Bundle b){super.onCreate(b); db=new DB(this); showHome();}
    TextView text(String s,int size){TextView t=new TextView(this);t.setText(s);t.setTextSize(size);t.setTextColor(Color.rgb(35,45,55));t.setPadding(4,8,4,8);return t;}
    Button btn(String s){Button b=new Button(this);b.setText(s);return b;}
    void base(String title){
        root=new LinearLayout(this);root.setOrientation(1);root.setBackgroundColor(pale);
        TextView h=text(title,20);h.setTextColor(Color.WHITE);h.setBackgroundColor(blue);h.setPadding(16,18,12,18);root.addView(h);
        HorizontalScrollView nav=new HorizontalScrollView(this);LinearLayout row=new LinearLayout(this);
        String[] tabs={"Dashboard","Input Transaksi","Transaksi","Laporan"};
        for(int i=0;i<tabs.length;i++){final int n=i;Button b=btn(tabs[i]);b.setTextSize(11);b.setOnClickListener(v->{if(n==0)showHome();if(n==1)showForm(-1);if(n==2)showTransactions();if(n==3)showReport();});row.addView(b);}
        nav.addView(row);root.addView(nav);
        ScrollView sc=new ScrollView(this);body=new LinearLayout(this);body.setOrientation(1);body.setPadding(14,10,14,20);sc.addView(body);root.addView(sc,new LinearLayout.LayoutParams(-1,0,1));
        setContentView(root);
    }
    void card(String label,String value){LinearLayout c=new LinearLayout(this);c.setOrientation(1);c.setPadding(14,10,14,10);c.setBackgroundColor(Color.WHITE);LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,-2);p.setMargins(0,5,0,5);body.addView(c,p);c.addView(text(label,13));TextView v=text(value,22);v.setTextColor(blue);v.setTypeface(null,1);c.addView(v);}
    String rupiah(double n){return "Rp "+String.format(new Locale("in","ID"),"%,.0f",n);}
    void showHome(){
        base("Laporan Keuangan Majelis Dikdasmen dan PNF");
        double in=db.sum("Pemasukan"),out=db.sum("Pengeluaran");
        card("TOTAL PEMASUKAN",rupiah(in));card("TOTAL PENGELUARAN",rupiah(out));card("SALDO SAAT INI",rupiah(in-out));
        body.addView(text("Transaksi Terbaru",17));
        Cursor c=db.getAll();int count=0;
        while(c.moveToNext()&&count<5){body.addView(text(c.getString(1)+" • "+c.getString(2)+" • "+rupiah(c.getDouble(4)),14));count++;}
        c.close();
        body.addView(text("Data tersimpan di perangkat ini. Aplikasi dapat digunakan tanpa koneksi internet.",12));
    }
    void addLabel(String s){body.addView(text(s,14));}
    EditText edit(String hint,int type){EditText e=new EditText(this);e.setSingleLine(true);e.setHint(hint);e.setInputType(type);body.addView(e);return e;}
    Spinner spinner(String[] a){Spinner s=new Spinner(this);s.setAdapter(new ArrayAdapter<String>(this,android.R.layout.simple_spinner_dropdown_item,a));body.addView(s);return s;}
    void showForm(int id){
        base(id<0?"Input Transaksi":"Edit Transaksi");
        String date=new SimpleDateFormat("yyyy-MM-dd",Locale.US).format(new Date());
        addLabel("Tanggal (YYYY-MM-DD)");eDate=edit("2026-11-20",InputType.TYPE_CLASS_DATETIME);eDate.setText(date);
        addLabel("Jenis Transaksi");sType=spinner(types);
        addLabel("Bidang");sArea=spinner(areas);
        addLabel("Kategori");sCategory=spinner(cats);
        addLabel("Uraian transaksi");eDesc=edit("Contoh: pembelian ATK",InputType.TYPE_CLASS_TEXT);
        addLabel("Nominal (angka)");eAmount=edit("Contoh: 150000",InputType.TYPE_CLASS_NUMBER|InputType.TYPE_NUMBER_FLAG_DECIMAL);
        addLabel("Sumber dana / penerima");eSource=edit("Nama sumber dana atau penerima",InputType.TYPE_CLASS_TEXT);
        Button n=btn("Pilih Foto Nota / Kuitansi");body.addView(n);n.setOnClickListener(v->{pickTarget=1;pickPhoto();});
        Button b=btn("Pilih Foto Barang / Pekerjaan");body.addView(b);b.setOnClickListener(v->{pickTarget=2;pickPhoto();});
        TextView status=text("Foto nota: "+(photoNota.isEmpty()?"belum dipilih":"tersimpan")+"\nFoto barang: "+(photoBarang.isEmpty()?"belum dipilih":"tersimpan"),12);body.addView(status);
        Button save=btn("SIMPAN TRANSAKSI");body.addView(save);
        if(id>=0){Cursor c=db.one(id);if(c.moveToFirst()){
            eDate.setText(c.getString(1));sType.setSelection(Arrays.asList(types).indexOf(c.getString(2)));
            sArea.setSelection(Arrays.asList(areas).indexOf(c.getString(3)));sCategory.setSelection(Arrays.asList(cats).indexOf(c.getString(5)));
            eDesc.setText(c.getString(6));eAmount.setText(c.getString(4));eSource.setText(c.getString(7));
            photoNota=c.getString(8);photoBarang=c.getString(9);status.setText("Foto nota: "+(photoNota.isEmpty()?"belum ada":"tersimpan")+"\nFoto barang: "+(photoBarang.isEmpty()?"belum ada":"tersimpan"));
        }c.close();}
        save.setOnClickListener(v->{
            try{
                String d=eDate.getText().toString().trim(),desc=eDesc.getText().toString().trim(),amt=eAmount.getText().toString().trim();
                if(d.length()==0||desc.length()==0||amt.length()==0)throw new Exception("Tanggal, uraian, dan nominal wajib diisi.");
                double val=Double.parseDouble(amt.replace(",","."));if(val<=0)throw new Exception("Nominal harus lebih dari nol.");
                db.save(id,d,types[sType.getSelectedItemPosition()],areas[sArea.getSelectedItemPosition()],val,cats[sCategory.getSelectedItemPosition()],desc,eSource.getText().toString(),photoNota,photoBarang);
                photoNota="";photoBarang="";new AlertDialog.Builder(this).setMessage("Transaksi berhasil disimpan.").setPositiveButton("OK",(a,z)->showHome()).show();
            }catch(Exception ex){new AlertDialog.Builder(this).setMessage(ex.getMessage()).setPositiveButton("OK",null).show();}
        });
    }
    void pickPhoto(){Intent i=new Intent(Intent.ACTION_GET_CONTENT);i.setType("image/*");startActivityForResult(Intent.createChooser(i,"Pilih foto"),pickTarget);}
    protected void onActivityResult(int req,int res,Intent data){super.onActivityResult(req,res,data);if(res==RESULT_OK&&data!=null){try{
        InputStream in=getContentResolver().openInputStream(data.getData());File dir=new File(getFilesDir(),"bukti");dir.mkdirs();
        File f=new File(dir,"foto_"+System.currentTimeMillis()+".jpg");FileOutputStream out=new FileOutputStream(f);byte[] buf=new byte[8192];int n;while((n=in.read(buf))>0)out.write(buf,0,n);in.close();out.close();
        if(req==1)photoNota=f.getAbsolutePath();else if(req==2)photoBarang=f.getAbsolutePath();
        Toast.makeText(this,"Foto bukti disimpan",Toast.LENGTH_SHORT).show();
    }catch(Exception e){Toast.makeText(this,"Gagal menyimpan foto",Toast.LENGTH_LONG).show();}}}
    void showTransactions(){
        base("Data Transaksi");Cursor c=db.getAll();if(c.getCount()==0)body.addView(text("Belum ada transaksi.",15));
        while(c.moveToNext()){final int id=c.getInt(0);LinearLayout box=new LinearLayout(this);box.setOrientation(1);box.setPadding(10,8,10,8);box.setBackgroundColor(Color.WHITE);LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,-2);p.setMargins(0,5,0,5);body.addView(box,p);
            box.addView(text(c.getString(1)+" | "+c.getString(2)+" | "+rupiah(c.getDouble(4)),15));
            box.addView(text(c.getString(3)+" • "+c.getString(5)+"\n"+c.getString(6),12));
            LinearLayout actions=new LinearLayout(this);Button edit=btn("Edit"),del=btn("Hapus");actions.addView(edit);actions.addView(del);box.addView(actions);
            edit.setOnClickListener(v->{photoNota="";photoBarang="";showForm(id);});
            del.setOnClickListener(v->new AlertDialog.Builder(this).setMessage("Hapus transaksi ini?").setNegativeButton("Batal",null).setPositiveButton("Hapus",(a,z)->{db.delete(id);showTransactions();}).show());
        }c.close();
    }
    void showReport(){
        base("Laporan Keuangan");double in=db.sum("Pemasukan"),out=db.sum("Pengeluaran");
        card("Total Pemasukan",rupiah(in));card("Total Pengeluaran",rupiah(out));card("Saldo",rupiah(in-out));
        body.addView(text("Rekap per bidang",17));for(String area:areas){double a=db.sumArea(area,"Pemasukan"),b=db.sumArea(area,"Pengeluaran");body.addView(text(area+"\nPemasukan: "+rupiah(a)+"\nPengeluaran: "+rupiah(b)+"\nSelisih: "+rupiah(a-b),14));}
        body.addView(text("Rincian transaksi dapat dilihat pada menu Transaksi.",12));
    }
    static class DB extends SQLiteOpenHelper{
        DB(Context c){super(c,"keuangan_dikdasmen.db",null,1);}
        public void onCreate(SQLiteDatabase d){d.execSQL("CREATE TABLE trx(id INTEGER PRIMARY KEY AUTOINCREMENT,tanggal TEXT,jenis TEXT,bidang TEXT,nominal REAL,kategori TEXT,uraian TEXT,sumber TEXT,nota TEXT,barang TEXT)");}
        public void onUpgrade(SQLiteDatabase d,int o,int n){}
        void save(int id,String date,String type,String area,double amount,String cat,String desc,String source,String nota,String barang){
            ContentValues v=new ContentValues();v.put("tanggal",date);v.put("jenis",type);v.put("bidang",area);v.put("nominal",amount);v.put("kategori",cat);v.put("uraian",desc);v.put("sumber",source);v.put("nota",nota);v.put("barang",barang);
            SQLiteDatabase d=getWritableDatabase();if(id<0)d.insert("trx",null,v);else d.update("trx",v,"id=?",new String[]{""+id});
        }
        Cursor getAll(){return getReadableDatabase().rawQuery("SELECT * FROM trx ORDER BY tanggal DESC,id DESC",null);}
        Cursor one(int id){return getReadableDatabase().rawQuery("SELECT * FROM trx WHERE id="+id,null);}
        void delete(int id){getWritableDatabase().delete("trx","id=?",new String[]{""+id});}
        double sum(String type){Cursor c=getReadableDatabase().rawQuery("SELECT COALESCE(SUM(nominal),0) FROM trx WHERE jenis=?",new String[]{type});c.moveToFirst();double x=c.getDouble(0);c.close();return x;}
        double sumArea(String area,String type){Cursor c=getReadableDatabase().rawQuery("SELECT COALESCE(SUM(nominal),0) FROM trx WHERE bidang=? AND jenis=?",new String[]{area,type});c.moveToFirst();double x=c.getDouble(0);c.close();return x;}
    }
}
