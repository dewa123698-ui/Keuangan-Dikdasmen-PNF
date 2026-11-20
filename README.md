# Keuangan Dikdasmen dan PNF Kota Probolinggo

Aplikasi Android native Java, penyimpanan lokal SQLite, tanpa perlu koneksi internet. `minSdk 19` (Android 4.4).

## Fitur
- Dashboard total pemasukan, pengeluaran, dan saldo.
- Input, edit, dan hapus transaksi.
- Bidang Kebutuhan Kantor dan Pembangunan Bangunan.
- Kategori transaksi dan sumber dana/penerima.
- Lampiran foto nota/kuitansi dan foto barang/pekerjaan.
- Rekap total dan rekap per bidang.
- Ekspor rincian transaksi ke CSV yang dapat dibuka dengan Microsoft Excel atau aplikasi spreadsheet.
- Ekspor laporan PDF.
- Backup database SQLite dan seluruh foto bukti ke satu file ZIP.
- Restore dari file backup ZIP. Restore mengganti data lokal yang sedang ada; lakukan backup terlebih dahulu.

## Struktur backup
File ZIP berisi `keuangan_dikdasmen.db`, folder `bukti/`, dan `backup_info.txt`. Simpan file backup di tempat aman. Foto bukti disalin bersama database sehingga path foto di transaksi tetap cocok setelah restore pada aplikasi ini.

## Build APK dari GitHub
1. Buat repository GitHub baru.
2. Unggah **isi** folder `KeuanganDikdasmenPNF` ke repository (termasuk folder `.github`).
3. Buka tab **Actions** dan pilih workflow **Build Android APK**.
4. Jalankan workflow dengan **Run workflow** atau push ke branch `main`/`master`.
5. Setelah berhasil, unduh artifact `Keuangan-DikdasmenPNF-APK`.
6. Ekstrak artifact dan pasang `app-debug.apk` di HP.

## Catatan pengujian
Source ini telah diperbarui, tetapi APK belum dikompilasi atau diuji pada perangkat Android dalam lingkungan ini. Jalankan workflow GitHub Actions terlebih dahulu. Sebelum memakai data penting, uji input/edit transaksi, ekspor CSV/PDF, backup, lalu restore pada data percobaan. Jangan menghapus instalasi lama sebelum data lama diamankan.
