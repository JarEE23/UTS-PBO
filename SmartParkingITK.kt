import kotlin.random.Random

// ============================================================
//   SMART PARKING ITK - Institut Teknologi Kalimantan
//   Sistem Manajemen Parkir Cerdas
// ============================================================

// ─── DATA MODEL ─────────────────────────────────────────────

data class Mahasiswa(
    val nim: String,
    var saldo: Int,
    var sedangParkir: Boolean = false
)

// ─── INISIALISASI DATA ───────────────────────────────────────

fun buatDatabaseMahasiswa(): MutableMap<String, Mahasiswa> {
    val db = mutableMapOf<String, Mahasiswa>()

    repeat(50) { i ->
        val nim   = "0423${1000 + i + 1}"   // 04231001 – 04231050
        val saldo = Random.nextInt(1, 21) * 3000             // 3.000 – 60.000 (kelipatan 3.000)
        db[nim] = Mahasiswa(nim, saldo)
    }
    return db
}

// ─── UTILITAS TAMPILAN ───────────────────────────────────────

fun printHeader() {
    println("""
╔══════════════════════════════════════════════════════════════╗
║         🚗  SMART PARKING ITK  🚗                           ║
║      Institut Teknologi Kalimantan — Sistem Parkir           ║
╚══════════════════════════════════════════════════════════════╝
""".trimIndent())
}

fun printSeparator() = println("─".repeat(64))

fun printStatusParkir(kuotaTersedia: Int, kuotaMaksimal: Int) {
    val terpakai  = kuotaMaksimal - kuotaTersedia
    val persen    = if (kuotaMaksimal > 0) (terpakai * 100) / kuotaMaksimal else 0
    val bar       = buildString {
        val filled = (persen / 5)
        repeat(filled)          { append("█") }
        repeat(20 - filled)     { append("░") }
    }
    val status = when {
        kuotaTersedia == 0            -> "🔴 PENUH"
        persen >= 80                  -> "🟡 HAMPIR PENUH"
        else                          -> "🟢 TERSEDIA"
    }
    println("""
┌──────────────────────────────────────────────────────────────┐
│  STATUS PARKIR                                               │
│  Slot Tersedia : $kuotaTersedia / $kuotaMaksimal             |                 
│  Terpakai      : [$bar] $persen%                             |
│  Status        : $status                                     |
└──────────────────────────────────────────────────────────────┘""".trimMargin("|"))
    println()
}

fun formatRupiah(nominal: Int): String = "Rp ${"%,d".format(nominal).replace(',', '.')}"

// ─── LOGIKA MASUK ────────────────────────────────────────────

fun prosesKendaraanMasuk(
    db: MutableMap<String, Mahasiswa>,
    kuotaTersedia: Int,
    kuotaMaksimal: Int
): Int {
    printSeparator()
    println("📥  KENDARAAN MASUK")
    printSeparator()

    if (kuotaTersedia <= 0) {
        println("""
⛔  MAAF — PARKIRAN PENUH!
    Tidak ada slot tersedia saat ini.
    Silakan coba beberapa saat lagi.
""")
        return kuotaTersedia
    }

    print("   Masukkan NIM Anda : ")
    val nim = readLine()?.trim() ?: ""

    val mahasiswa = db[nim]
    if (mahasiswa == null) {
        println("\n❌  NIM tidak ditemukan dalam sistem!\n")
        return kuotaTersedia
    }

    if (mahasiswa.sedangParkir) {
        println("\n⚠️  Kendaraan dengan NIM ini sudah berada di dalam parkiran!\n")
        return kuotaTersedia
    }

    println("""
   ✅  Selamat datang, Mahasiswa $nim!
   💰  Saldo saat ini : ${formatRupiah(mahasiswa.saldo)}
   🎫  Tarif parkir   : ${formatRupiah(3000)} (flat)
   📌  Saldo akan dipotong saat Anda keluar.
""")

    mahasiswa.sedangParkir = true
    val kuotaBaru = kuotaTersedia - 1
    println("   🔽  Slot berkurang. Sisa slot: $kuotaBaru / $kuotaMaksimal\n")
    return kuotaBaru
}

// ─── LOGIKA KELUAR ───────────────────────────────────────────

fun prosesKendaraanKeluar(
    db: MutableMap<String, Mahasiswa>,
    kuotaTersedia: Int,
    kuotaMaksimal: Int
): Int {
    val tarif = 3000
    printSeparator()
    println("📤  KENDARAAN KELUAR")
    printSeparator()

    print("   Masukkan NIM Anda : ")
    val nim = readLine()?.trim() ?: ""

    val mahasiswa = db[nim]
    if (mahasiswa == null) {
        println("\n❌  NIM tidak ditemukan dalam sistem!\n")
        return kuotaTersedia
    }

    if (!mahasiswa.sedangParkir) {
        println("\n⚠️  Kendaraan dengan NIM ini tidak tercatat sedang parkir!\n")
        return kuotaTersedia
    }

    if (mahasiswa.saldo < tarif) {
        println("""
⛔  SALDO TIDAK CUKUP!
   Saldo Anda    : ${formatRupiah(mahasiswa.saldo)}
   Tarif parkir  : ${formatRupiah(tarif)}
   Kekurangan    : ${formatRupiah(tarif - mahasiswa.saldo)}
   
   Silakan isi saldo terlebih dahulu.
""")
        // Tetap bisa keluar — paksakan keluar, tandai minus (kebijakan kampus)
        // Namun di sini kita blokir dulu sebagai implementasi aman.
        return kuotaTersedia
    }

    val saldoLama  = mahasiswa.saldo
    mahasiswa.saldo -= tarif
    mahasiswa.sedangParkir = false
    val kuotaBaru = minOf(kuotaTersedia + 1, kuotaMaksimal)

    println("""
   ✅  Terima kasih, Mahasiswa $nim!
   💸  Tarif parkir  : ${formatRupiah(tarif)}
   💰  Saldo sebelum : ${formatRupiah(saldoLama)}
   💳  Saldo sekarang: ${formatRupiah(mahasiswa.saldo)}
   🔼  Slot bertambah. Sisa slot: $kuotaBaru / $kuotaMaksimal

   🙏  Selamat jalan & berkendara dengan aman!
""")
    return kuotaBaru
}

// ─── CEK SALDO ───────────────────────────────────────────────

fun cekSaldo(db: Map<String, Mahasiswa>) {
    printSeparator()
    println("💳  CEK SALDO")
    printSeparator()
    print("   Masukkan NIM Anda : ")
    val nim = readLine()?.trim() ?: ""
    val mhs = db[nim]
    if (mhs == null) {
        println("\n❌  NIM tidak ditemukan!\n")
        return
    }
    println("""
   NIM           : ${mhs.nim}
   Saldo         : ${formatRupiah(mhs.saldo)}
   Status        : ${if (mhs.sedangParkir) "🚗 Sedang parkir" else "🏠 Tidak parkir"}
""")
}

// ─── DAFTAR KENDARAAN YANG SEDANG PARKIR ─────────────────────

fun lihatKendaraanParkir(db: Map<String, Mahasiswa>) {
    printSeparator()
    println("🔍  DAFTAR KENDARAAN DI DALAM PARKIRAN")
    printSeparator()
    val parkir = db.values.filter { it.sedangParkir }
    if (parkir.isEmpty()) {
        println("   (Tidak ada kendaraan yang sedang parkir)\n")
        return
    }
    parkir.forEachIndexed { idx, mhs ->
        println("   ${idx + 1}. NIM: ${mhs.nim}  |  Saldo: ${formatRupiah(mhs.saldo)}")
    }
    println()
}

// ─── SIMULASI OTOMATIS (DEMO) ─────────────────────────────────

fun jalankanSimulasi(
    db: MutableMap<String, Mahasiswa>,
    kuotaMaksimal: Int
): Int {
    println("""
╔══════════════════════════════════════════════════════════════╗
║               🎮  MODE SIMULASI DEMO                         ║
╚══════════════════════════════════════════════════════════════╝

Simulasi akan menjalankan:
  • 5 kendaraan masuk berturut-turut
  • Lalu 2 kendaraan keluar
  • Kemudian 1 kendaraan masuk kembali
""")

    var kuota = kuotaMaksimal
    val nimList = db.keys.toList().shuffled().take(7)
    val tarif   = 3000

    println("━━━ FASE 1: 5 Kendaraan Masuk ━━━")
    nimList.take(5).forEach { nim ->
        val mhs = db[nim]!!
        println("\n  ➤ Kendaraan masuk: NIM $nim | Saldo: ${formatRupiah(mhs.saldo)}")
        if (kuota > 0) {
            mhs.sedangParkir = true
            kuota--
            println("    ✅ Masuk berhasil. Sisa slot: $kuota / $kuotaMaksimal")
        } else {
            println("    ⛔ Parkiran penuh!")
        }
    }

    println("\n━━━ FASE 2: 2 Kendaraan Keluar ━━━")
    nimList.take(2).forEach { nim ->
        val mhs = db[nim]!!
        println("\n  ➤ Kendaraan keluar: NIM $nim | Saldo: ${formatRupiah(mhs.saldo)}")
        if (mhs.sedangParkir && mhs.saldo >= tarif) {
            mhs.saldo -= tarif
            mhs.sedangParkir = false
            kuota = minOf(kuota + 1, kuotaMaksimal)
            println("    ✅ Keluar berhasil. Dipotong ${formatRupiah(tarif)}. Saldo kini: ${formatRupiah(mhs.saldo)}. Sisa slot: $kuota / $kuotaMaksimal")
        } else if (mhs.saldo < tarif) {
            println("    ⛔ Saldo tidak cukup! Tidak dapat keluar.")
        }
    }

    println("\n━━━ FASE 3: 1 Kendaraan Masuk Lagi ━━━")
    val nimBaru = nimList[6]
    val mhsBaru = db[nimBaru]!!
    println("\n  ➤ Kendaraan masuk: NIM $nimBaru | Saldo: ${formatRupiah(mhsBaru.saldo)}")
    if (kuota > 0) {
        mhsBaru.sedangParkir = true
        kuota--
        println("    ✅ Masuk berhasil. Sisa slot: $kuota / $kuotaMaksimal")
    } else {
        println("    ⛔ Parkiran penuh!")
    }

    println("\n✅ Simulasi selesai.\n")
    return kuota
}

// ─── MAIN ────────────────────────────────────────────────────

fun main() {
    printHeader()

    // Set kuota maksimal parkiran
    print("⚙️  Masukkan kapasitas maksimal lahan parkir: ")
    val kuotaMaksimal = readLine()?.trim()?.toIntOrNull()?.coerceAtLeast(1) ?: 20
    var kuotaTersedia = kuotaMaksimal

    println("\n✅  Kapasitas parkir ditetapkan: $kuotaMaksimal slot\n")

    val db = buatDatabaseMahasiswa()
    println("📦  Database mahasiswa berhasil dimuat (${db.size} akun)\n")

    // Menu utama
    while (true) {
        printStatusParkir(kuotaTersedia, kuotaMaksimal)

        println("""
┌─ MENU UTAMA ────────────────────────────────────────────────┐
│  [1] Kendaraan Masuk                                        │
│  [2] Kendaraan Keluar                                       │
│  [3] Cek Saldo                                              │
│  [4] Lihat Kendaraan di Parkiran                            │
│  [5] Jalankan Simulasi Demo                                 │
│  [0] Keluar Program                                         │
└─────────────────────────────────────────────────────────────┘""")
        print("  Pilih menu: ")

        when (readLine()?.trim()) {
            "1" -> kuotaTersedia = prosesKendaraanMasuk(db, kuotaTersedia, kuotaMaksimal)
            "2" -> kuotaTersedia = prosesKendaraanKeluar(db, kuotaTersedia, kuotaMaksimal)
            "3" -> cekSaldo(db)
            "4" -> lihatKendaraanParkir(db)
            "5" -> kuotaTersedia = jalankanSimulasi(db, kuotaMaksimal)
            "0" -> {
                println("""
╔══════════════════════════════════════════════════════════════╗
║   Terima kasih telah menggunakan Smart Parking ITK 🚗        ║
║   Institut Teknologi Kalimantan — Balikpapan                 ║
╚══════════════════════════════════════════════════════════════╝
""")
                break
            }
            else -> println("\n⚠️  Pilihan tidak valid. Silakan coba lagi.\n")
        }
    }
}