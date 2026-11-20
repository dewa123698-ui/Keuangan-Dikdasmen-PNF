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
import android.graphics.pdf.PdfDocument;
import android.graphics.Paint;
import android.graphics.Canvas;
import android.provider.OpenableColumns;
import android.text.InputType;
import java.io.*;
import java.text.*;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class MainActivity extends Activity {
    DB db; LinearLayout root, body; String photoNota="", photoBarang="";
    EditText eDate,eDesc,eAmount,eSource; Spinner sType,sArea,sCategory; int pickTarget=0; TextView photoStatus;
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
        photoStatus=text(photoLabel(),12);body.addView(photoStatus);
        Button save=btn("SIMPAN TRANSAKSI");body.addView(save);
        if(id>=0){Cursor c=db.one(id);if(c.moveToFirst()){
            eDate.setText(c.getString(1));sType.setSelection(Arrays.asList(types).indexOf(c.getString(2)));
            sArea.setSelection(Arrays.asList(areas).indexOf(c.getString(3)));sCategory.setSelection(Arrays.asList(cats).indexOf(c.getString(5)));
            eDesc.setText(c.getString(6));eAmount.setText(c.getString(4));eSource.setText(c.getString(7));
            photoNota=c.getString(8);photoBarang=c.getString(9);photoStatus.setText(photoLabel());
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
    String photoLabel(){return "Foto nota: "+(photoNota.isEmpty()?"belum dipilih":"tersimpan")+"\\nFoto barang: "+(photoBarang.isEmpty()?"belum dipilih":"tersimpan");}
    void showReport(){
        base("Laporan Keuangan");double in=db.sum("Pemasukan"),out=db.sum("Pengeluaran");
        card("Total Pemasukan",rupiah(in));card("Total Pengeluaran",rupiah(out));card("Saldo",rupiah(in-out));
        body.addView(text("Rekap per bidang",17));
        for(String area:areas){double a=db.sumArea(area,"Pemasukan"),b=db.sumArea(area,"Pengeluaran");body.addView(text(area+"\\nPemasukan: "+rupiah(a)+"\\nPengeluaran: "+rupiah(b)+"\\nSelisih: "+rupiah(a-b),14));}
        body.addView(text("Ekspor dan keamanan data",17));
        Button excel=btn("EKSPOR LAPORAN EXCEL (CSV)");body.addView(excel);
        excel.setOnClickListener(v->createExport(11,"laporan_keuangan.csv","text/csv"));
        Button pdf=btn("EKSPOR LAPORAN PDF");body.addView(pdf);
        pdf.setOnClickListener(v->createExport(12,"laporan_keuangan.pdf","application/pdf"));
        Button backup=btn("BACKUP DATABASE + FOTO");body.addView(backup);
        backup.setOnClickListener(v->createExport(13,"backup_keuangan_dikdasmen.zip","application/zip"));
        Button restore=btn("RESTORE BACKUP");body.addView(restore);
        restore.setOnClickListener(v->{
            new AlertDialog.Builder(this).setTitle("Pulihkan data")
              .setMessage("Restore akan mengganti database dan folder foto aplikasi saat ini dengan isi backup. Data terbaru yang belum dibackup dapat hilang. Lanjutkan?")
              .setNegativeButton("Batal",null).setPositiveButton("Pilih file backup",(a,b)->{
                  Intent i=new Intent(Intent.ACTION_OPEN_DOCUMENT);i.addCategory(Intent.CATEGORY_OPENABLE);i.setType("application/zip");startActivityForResult(i,14);
              }).show();
        });
        body.addView(text("File Excel berupa CSV yang dapat dibuka di Microsoft Excel atau aplikasi spreadsheet. Backup ZIP berisi database SQLite dan foto bukti transaksi.",12));
    }
    void createExport(int req,String name,String mime){
        Intent i=new Intent(Intent.ACTION_CREATE_DOCUMENT);i.addCategory(Intent.CATEGORY_OPENABLE);i.setType(mime);i.putExtra(Intent.EXTRA_TITLE,name);startActivityForResult(i,req);
    }
    protected void onActivityResult(int req,int res,Intent data){
        super.onActivityResult(req,res,data);
        if(res!=RESULT_OK||data==null||data.getData()==null)return;
        if(req==1||req==2){savePickedPhoto(req,data.getData());return;}
        try{
            if(req==11)writeCsv(data.getData());
            else if(req==12)writePdf(data.getData());
            else if(req==13)writeBackup(data.getData());
            else if(req==14)restoreBackup(data.getData());
        }catch(Exception e){new AlertDialog.Builder(this).setMessage("Gagal: "+e.getMessage()).setPositiveButton("OK",null).show();}
    }
    void savePickedPhoto(int req,Uri uri){
        try{InputStream in=getContentResolver().openInputStream(uri);File dir=new File(getFilesDir(),"bukti");dir.mkdirs();
            File f=new File(dir,"foto_"+System.currentTimeMillis()+".jpg");FileOutputStream out=new FileOutputStream(f);
            byte[] buf=new byte[8192];int n;while((n=in.read(buf))>0)out.write(buf,0,n);in.close();out.close();
            if(req==1)photoNota=f.getAbsolutePath();else photoBarang=f.getAbsolutePath();
            if(photoStatus!=null)photoStatus.setText(photoLabel());
            Toast.makeText(this,"Foto bukti disimpan",Toast.LENGTH_SHORT).show();
        }catch(Exception e){Toast.makeText(this,"Gagal menyimpan foto: "+e.getMessage(),Toast.LENGTH_LONG).show();}
    }
    String csv(String v){if(v==null)v="";return "\\""+v.replace("\\"","\\"\\\"")+"\\"";}
    void writeCsv(Uri uri)throws Exception{
        OutputStream out=getContentResolver().openOutputStream(uri);OutputStreamWriter w=new OutputStreamWriter(out,"UTF-8");
        w.write("\\uFEFFTanggal,Jenis,Bidang,Nominal,Kategori,Uraian,Sumber Dana/Penerima,Foto Nota,Foto Barang\\r\\n");
        Cursor c=db.getAll();while(c.moveToNext()){
            String[] vals={c.getString(1),c.getString(2),c.getString(3),String.valueOf(c.getDouble(4)),c.getString(5),c.getString(6),c.getString(7),c.getString(8),c.getString(9)};
            for(int j=0;j<vals.length;j++){if(j>0)w.write(",");w.write(csv(vals[j]));}w.write("\\r\\n");
        }c.close();w.flush();w.close();Toast.makeText(this,"Laporan Excel/CSV berhasil diekspor",Toast.LENGTH_LONG).show();
    }
    void writePdf(Uri uri)throws Exception{
        PdfDocument doc=new PdfDocument();Paint paint=new Paint();paint.setTextSize(12);
        PdfDocument.PageInfo info=new PdfDocument.PageInfo.Builder(595,842,1).create();PdfDocument.Page page=doc.startPage(info);Canvas canvas=page.getCanvas();
        int y=45;paint.setTextSize(17);paint.setFakeBoldText(true);canvas.drawText("LAPORAN KEUANGAN DIKDASMEN DAN PNF",35,y,paint);
        paint.setTextSize(11);paint.setFakeBoldText(false);y+=28;
        canvas.drawText("Total Pemasukan: "+rupiah(db.sum("Pemasukan")),35,y,paint);y+=20;
        canvas.drawText("Total Pengeluaran: "+rupiah(db.sum("Pengeluaran")),35,y,paint);y+=20;
        canvas.drawText("Saldo: "+rupiah(db.sum("Pemasukan")-db.sum("Pengeluaran")),35,y,paint);y+=30;
        canvas.drawText("RINCIAN TRANSAKSI",35,y,paint);y+=22;
        Cursor c=db.getAll();int pageNo=1;
        while(c.moveToNext()){
            if(y>800){doc.finishPage(page);pageNo++;info=new PdfDocument.PageInfo.Builder(595,842,pageNo).create();page=doc.startPage(info);y=40;}
            String line=c.getString(1)+" | "+c.getString(2)+" | "+c.getString(3)+" | "+rupiah(c.getDouble(4));
            canvas.drawText(line,35,y,paint);y+=17;
            String desc=c.getString(5)+" - "+c.getString(6);
            canvas.drawText(desc.length()>85?desc.substring(0,85):desc,45,y,paint);y+=19;
        }c.close();doc.finishPage(page);OutputStream out=getContentResolver().openOutputStream(uri);doc.writeTo(out);out.close();doc.close();
        Toast.makeText(this,"Laporan PDF berhasil diekspor",Toast.LENGTH_LONG).show();
    }
    void addZipFile(ZipOutputStream zos,File file,String entry)throws Exception{
        if(!file.exists()||!file.isFile())return;zos.putNextEntry(new ZipEntry(entry));FileInputStream in=new FileInputStream(file);
        byte[] b=new byte[8192];int n;while((n=in.read(b))>0)zos.write(b,0,n);in.close();zos.closeEntry();
    }
    void addZipDir(ZipOutputStream zos,File dir,String prefix)throws Exception{
        File[] files=dir.listFiles();if(files==null)return;for(File f:files){if(f.isDirectory())addZipDir(zos,f,prefix+f.getName()+"/");else addZipFile(zos,f,prefix+f.getName());}
    }
    void writeBackup(Uri uri)throws Exception{
        db.close();File dbFile=getDatabasePath("keuangan_dikdasmen.db");
        OutputStream raw=getContentResolver().openOutputStream(uri);ZipOutputStream zos=new ZipOutputStream(raw);
        addZipFile(zos,dbFile,"keuangan_dikdasmen.db");addZipDir(zos,new File(getFilesDir(),"bukti"),"bukti/");
        zos.putNextEntry(new ZipEntry("backup_info.txt"));zos.write("Backup Keuangan Dikdasmen PNF\\nFormat 1\\n".getBytes("UTF-8"));zos.closeEntry();zos.close();db=new DB(this);
        Toast.makeText(this,"Backup database dan foto berhasil dibuat",Toast.LENGTH_LONG).show();
    }
    void restoreBackup(Uri uri)throws Exception{
        File temp=new File(getCacheDir(),"restore_"+System.currentTimeMillis());temp.mkdirs();
        ZipInputStream zis=new ZipInputStream(getContentResolver().openInputStream(uri));ZipEntry entry;boolean hasDb=false;
        while((entry=zis.getNextEntry())!=null){
            String name=entry.getName();File dest=new File(temp,name);
            if(!dest.getCanonicalPath().startsWith(temp.getCanonicalPath()+File.separator))throw new Exception("Isi ZIP tidak valid.");
            if(entry.isDirectory()){dest.mkdirs();continue;}dest.getParentFile().mkdirs();
            FileOutputStream out=new FileOutputStream(dest);byte[] b=new byte[8192];int n;while((n=zis.read(b))>0)out.write(b,0,n);out.close();zis.closeEntry();
            if("keuangan_dikdasmen.db".equals(name))hasDb=true;
        }zis.close();if(!hasDb)throw new Exception("Database tidak ditemukan di file backup.");
        db.close();File live=getDatabasePath("keuangan_dikdasmen.db");live.getParentFile().mkdirs();
        new File(live.getAbsolutePath()+"-wal").delete();new File(live.getAbsolutePath()+"-shm").delete();
        copyFile(new File(temp,"keuangan_dikdasmen.db"),live);
        File livePhotos=new File(getFilesDir(),"bukti");deleteTree(livePhotos);
        File tempPhotos=new File(temp,"bukti");if(tempPhotos.exists())copyTree(tempPhotos,livePhotos);
        db=new DB(this);db.getReadableDatabase();deleteTree(temp);
        new AlertDialog.Builder(this).setMessage("Restore berhasil. Data aplikasi telah dipulihkan.").setPositiveButton("OK",(a,b)->showHome()).show();
    }
    void copyFile(File from,File to)throws Exception{FileInputStream in=new FileInputStream(from);FileOutputStream out=new FileOutputStream(to);byte[] b=new byte[8192];int n;while((n=in.read(b))>0)out.write(b,0,n);in.close();out.close();}
    void copyTree(File from,File to)throws Exception{to.mkdirs();File[] fs=from.listFiles();if(fs==null)return;for(File f:fs){File d=new File(to,f.getName());if(f.isDirectory())copyTree(f,d);else copyFile(f,d);}}
    void deleteTree(File f){if(!f.exists())return;if(f.isDirectory()){File[] fs=f.listFiles();if(fs!=null)for(File x:fs)deleteTree(x);}f.delete();}
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
