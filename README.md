# Laporan Keuangan Majelis Dikdasmen dan PNF Kota Probolinggo

Proyek Android native Java, offline, minSdk 19 (Android 4.4 KitKat).

## Fitur versi awal
- Dashboard total pemasukan, pengeluaran, dan saldo.
- Input transaksi pemasukan/pengeluaran.
- Bidang kebutuhan kantor atau pembangunan bangunan.
- Edit dan hapus transaksi.
- Lampiran foto nota/kuitansi dan foto barang dari galeri.
- Laporan total dan rekap per bidang.
- Database SQLite lokal.

## Cara menghasilkan APK melalui GitHub (bisa dari HP)
1. Buat repository GitHub baru.
2. Unggah seluruh isi folder proyek ini ke repository (bukan folder ZIP sebagai satu file).
3. Buka tab **Actions** lalu pilih workflow **Build Android APK**.
4. Tekan **Run workflow**. Tunggu proses selesai.
5. Buka hasil workflow dan unduh artifact **Keuangan-DikdasmenPNF-APK**.
6. Ekstrak artifact ZIP, lalu pasang `app-debug.apk` di HP. Izinkan instalasi aplikasi dari sumber yang digunakan jika Android meminta.

## Catatan penting
Ini adalah source project dan workflow pembangun APK, bukan APK yang sudah dikompilasi atau diuji. Lingkungan pembuatan saat ini tidak memiliki Android SDK/Gradle sehingga APK belum dapat dinyatakan siap instal. Uji pada perangkat Android 4.4 nyata tetap diperlukan.
