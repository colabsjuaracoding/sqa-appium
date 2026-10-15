# GEMINI.md — Konfigurasi AI untuk Project Appium

## Skill yang Digunakan

Seluruh instruksi perilaku AI untuk project ini ada di:

- [`.agents/skills/ai-instructions-appium.md`](.agents/skills/ai-instructions-appium.md)

Skill tersebut mencakup:
- Scope yang boleh disentuh (project, folder, target uji, konsep Appium)
- Daftar larangan teknis (15 poin)
- Konvensi penamaan wajib
- Aturan teknis yang wajib dijaga
- Format jawaban yang diharapkan

---

## Perintah Khusus (Keyword Trigger)

### Keyword: `COMMIT dan PUSH`
Ketika user mengirimkan pesan atau prompt yang berisi keyword **"COMMIT dan PUSH"** (case-insensitive), AI **WAJIB** secara otomatis menjalankan alur berikut:
1. **Verifikasi Keamanan Kredensial**:
   - Pastikan file [.gitignore](.gitignore) tetap mengabaikan file kredensial/sensitif (`.env`, `*.env`, token, `target/`, dll.).
   - Pastikan file `.env` tidak ikut ter-stage atau ter-commit.
2. **Ambil Kredensial Git**:
   - Baca [.env](.env) untuk mendapatkan nilai `GIT_PAT` dan `GIT_REPO`.
3. **Stage & Commit**:
   - Cek `git status`.
   - Stage file perubahan kode (`git add .`).
   - Lakukan commit dengan pesan ringkas dan deskriptif mengenai perubahan yang dilakukan.
4. **Push ke GitHub**:
   - Push branch aktif (`main`) ke remote repository `GIT_REPO` menggunakan autentikasi `GIT_PAT`.
5. **Konfirmasi**:
   - Laporkan daftar file yang di-commit, commit hash, status push ke GitHub, serta konfirmasi bahwa kredensial `.env` aman dan tidak ikut terdeploy.