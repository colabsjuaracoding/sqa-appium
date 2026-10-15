package id.co.juaracoding.appium;

import io.appium.java_client.AppiumBy;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.android.options.UiAutomator2Options;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.PointerInput;
import org.openqa.selenium.interactions.Sequence;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;

import java.net.URI;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class BaseAppiumTest {

    protected static final String APPIUM_SERVER_URL = "http://127.0.0.1:4723";
    protected static final String DEVICE_UDID = "emulator-5554";
    protected static final String APP_PACKAGE = "id.co.juaracoding.mobile";
    protected static final String APP_ACTIVITY = ".MainActivity";

    // Host = alias NAT LDPlayer menuju mesin host Windows (default LDPlayer:
    // 172.16.1.2)
    // ⛔ BUKAN localhost karena emulator berjalan di lingkungan OS Android terpisah
    protected static final String BACKEND_HOST = "172.16.1.2";
    protected static final String BACKEND_PORT = "8080";
    protected static final String LICENSE_KEY = "d461265dd7323fef9755bb3257275d67";

    // Nilai captcha dibaca LANGSUNG dari elemen tersembunyi captcha_value_hint pada
    // aplikasi Mobile
    // ⛔ JANGAN pakai AppiumBy.id() karena UiAutomator2 otomatis menambahkan prefix
    // package
    protected static final String SELEKTOR_NILAI_CAPTCHA = "new UiSelector().resourceId(\"captcha_value_hint\")";

    private static final int MAKS_POLL_NILAI_CAPTCHA = 20;
    private static final long JEDA_POLL_MILIDETIK = 500;

    protected AndroidDriver driver;

    @BeforeMethod
    public void setUp() throws Exception {
        // 🔴 setNoReset(false) WAJIB tetap ada: data aplikasi di-reset sebelum tiap
        // test method
        // supaya setiap test independen dan selalu mulai dari form Koneksi kosong.
        UiAutomator2Options options = new UiAutomator2Options()
                .setUdid(DEVICE_UDID)
                .setAppPackage(APP_PACKAGE)
                .setAppActivity(APP_ACTIVITY)
                .setNoReset(false)
                .autoGrantPermissions()
                .setNewCommandTimeout(Duration.ofSeconds(120));

        URL serverUrl = URI.create(APPIUM_SERVER_URL).toURL();
        driver = new AndroidDriver(serverUrl, options);
    }

    @AfterMethod
    public void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }

    /** Mengisi form Koneksi (Host/Port/Lisensi) dan tap "Hubungkan". */
    protected void hubungkanKeBackend() {
        List<WebElement> editTexts = driver.findElements(AppiumBy.className("android.widget.EditText"));
        if (editTexts.size() != 3) {
            return;
        }
        isi(editTexts.get(0), BACKEND_HOST);
        isi(editTexts.get(1), BACKEND_PORT);
        isi(editTexts.get(2), LICENSE_KEY);
        tombolBerlabel("Hubungkan").click();
        tunggu(1500);
    }

    /** Login dari layar Home: tap "Login", isi form, baca & isi captcha, submit. */
    protected void login(String username, String password) {
        tombolBerlabel("Login").click();
        tunggu(1200);

        List<WebElement> editTexts = driver.findElements(AppiumBy.className("android.widget.EditText"));
        Assert.assertEquals(editTexts.size(), 3,
                "Layar Login wajib punya 3 EditText: Username, Password, Simple Captcha");
        isi(editTexts.get(0), username);
        isi(editTexts.get(1), password);
        isi(editTexts.get(2), bacaNilaiCaptcha());

        tombolBerlabel("Login").click();
        tunggu(2000);
    }

    /**
     * Membaca nilai captcha dari elemen captcha_value_hint.
     * ⛔ Flutter merender teks via Skia, wajib memakai
     * .getAttribute("content-desc"), bukan .getText().
     */
    protected String bacaNilaiCaptcha() {
        WebElement elemen = null;
        for (int percobaan = 0; percobaan < MAKS_POLL_NILAI_CAPTCHA && elemen == null; percobaan++) {
            List<WebElement> ditemukan = driver.findElements(AppiumBy.androidUIAutomator(SELEKTOR_NILAI_CAPTCHA));
            if (!ditemukan.isEmpty()) {
                elemen = ditemukan.get(0);
            } else {
                tunggu(JEDA_POLL_MILIDETIK);
            }
        }
        Assert.assertNotNull(elemen,
                "Elemen captcha_value_hint tidak muncul dalam "
                        + (MAKS_POLL_NILAI_CAPTCHA * JEDA_POLL_MILIDETIK / 1000) + " detik. "
                        + "Pastikan aplikasi berjalan dalam mode TESTING dan backend menjawab GET /api/v1/captcha.");

        String nilai = elemen.getAttribute("content-desc");
        Assert.assertNotNull(nilai, "content-desc elemen captcha_value_hint tidak boleh null");
        Assert.assertFalse(nilai.isEmpty(), "content-desc elemen captcha_value_hint tidak boleh kosong");
        return nilai;
    }

    /**
     * Mencari tombol berdasarkan className Button dan content-desc label.
     * Mencegah salah klik heading non-klik yang memiliki teks/content-desc sama.
     */
    protected WebElement tombolBerlabel(String label) {
        return driver.findElement(AppiumBy.androidUIAutomator(selektorTombol(label)));
    }

    private String selektorTombol(String label) {
        return "new UiSelector().className(\"android.widget.Button\").description(\"" + label + "\")";
    }

    /** Scroll turun di modal sampai tombol target ber-class Button terlihat. */
    protected void scrollTurunSampaiTombol(String label, int maksPercobaan) {
        for (int percobaan = 0; percobaan < maksPercobaan; percobaan++) {
            if (!driver.findElements(AppiumBy.androidUIAutomator(selektorTombol(label))).isEmpty()) {
                return;
            }
            swipeKecil();
        }
    }

    protected void isi(WebElement element, String value) {
        element.click();
        try {
            element.clear();
        } catch (Exception ignored) {
            // Widget Flutter kadang tidak mendukung aksi clear
        }
        element.sendKeys(value);
    }

    /**
     * Scroll turun modal bersyarat sampai elemen dengan content-desc tertentu
     * ketemu.
     */
    protected void scrollTurunSampaiKetemu(String contentDesc, int maksPercobaan) {
        for (int percobaan = 0; percobaan < maksPercobaan; percobaan++) {
            if (!driver.findElements(AppiumBy.accessibilityId(contentDesc)).isEmpty()) {
                return;
            }
            swipeKecil();
        }
    }

    /** Scroll turun bersyarat dengan batas area tertentu. */
    protected void scrollTurunSampaiKetemuDalam(String contentDesc, int maksPercobaan,
            int left, int top, int width, int height) {
        for (int percobaan = 0; percobaan < maksPercobaan; percobaan++) {
            if (!driver.findElements(AppiumBy.accessibilityId(contentDesc)).isEmpty()) {
                return;
            }
            swipeKecilDalam(left, top, width, height);
        }
    }

    protected void unggahFotoBerikutnya() {
        scrollTurunSampaiKetemu("Unggah", 6);
        driver.findElement(AppiumBy.accessibilityId("Unggah")).click();
        tunggu(2000);

        driver.findElement(AppiumBy.accessibilityId("Tampilkan root")).click();
        tunggu(1000);
        driver.findElement(AppiumBy.androidUIAutomator("new UiSelector().text(\"Galeri\")")).click();
        tunggu(1200);

        tapKoordinat(800, 550); // album "Kamera"
        tunggu(1500);
        // tapKoordinat(800, 470); // album "Kamera"
        // tunggu(1500);
        tapKoordinat(387, 290); // foto kiri-atas di grid
        tunggu(2000);
    }

    protected List<WebElement> scrollSampaiBlokAtributTerlihat(int maksPercobaan) {
        for (int percobaan = 0; percobaan < maksPercobaan; percobaan++) {
            List<WebElement> field = driver.findElements(AppiumBy.className("android.widget.EditText"));
            boolean semuaKosong = field.stream().allMatch(e -> e.getText() == null || e.getText().isEmpty());
            if (field.size() == 4 && semuaKosong) {
                return field;
            }
            swipeKecil();
        }
        throw new AssertionError("Blok atribut tidak pernah terlihat utuh setelah " + maksPercobaan + " swipe.");
    }

    /** Swipe kecil terkendali pada modal (misal dialog Produk). */
    protected void swipeKecil() {
        swipeKecilDalam(500, 306, 600, 500);
    }

    protected void swipeKecilDalam(int left, int top, int width, int height) {
        Map<String, Object> params = new HashMap<>();
        params.put("left", left);
        params.put("top", top);
        params.put("width", width);
        params.put("height", height);
        params.put("direction", "up");
        params.put("percent", 0.30);
        params.put("speed", 800);
        driver.executeScript("mobile: swipeGesture", params);
        tunggu(700);
    }

    /**
     * Drag manual W3C Pointer Actions.
     * Wajib dipakai untuk dialog Registrasi karena mobile: swipeGesture gagal total
     * di dialog tersebut.
     */
    protected void geserDenganPointer(int xMulai, int yMulai, int xAkhir, int yAkhir) {
        PointerInput jari = new PointerInput(PointerInput.Kind.TOUCH, "jari");
        Sequence urutan = new Sequence(jari, 0);
        urutan.addAction(jari.createPointerMove(Duration.ZERO,
                PointerInput.Origin.viewport(), xMulai, yMulai));
        urutan.addAction(jari.createPointerDown(0));
        urutan.addAction(jari.createPointerMove(Duration.ofMillis(400),
                PointerInput.Origin.viewport(), xAkhir, yAkhir));
        urutan.addAction(jari.createPointerUp(0));
        driver.perform(Collections.singletonList(urutan));
        tunggu(700);
    }

    /**
     * Scroll turun bersyarat dengan XPath hint (misal untuk input No. KTP / NPWP
     * pada registrasi).
     */
    protected void scrollTurunSampaiXpathKetemu(String xpath, int maksPercobaan) {
        for (int percobaan = 0; percobaan < maksPercobaan; percobaan++) {
            if (!driver.findElements(AppiumBy.xpath(xpath)).isEmpty()) {
                return;
            }
            geserDenganPointer(800, 583, 800, 459);
        }
    }

    /**
     * Menutup keyboard sebelum scroll form panjang.
     * ⛔ JANGAN dipanggil di dalam modal/dialog karena akan menekan tombol BACK dan
     * menutup dialog.
     */
    protected void sembunyikanKeyboard() {
        try {
            driver.hideKeyboard();
            tunggu(500);
        } catch (Exception ignored) {
        }
    }

    /**
     * Menyimpan page source XML dan screenshot ke target/explore untuk keperluan
     * debugging/inspeksi.
     */
    protected void simpanBukti(String namaTahap) {
        try {
            Path dir = Path.of("target", "explore");
            Files.createDirectories(dir);
            Files.writeString(dir.resolve(namaTahap + ".xml"), driver.getPageSource());
            byte[] png = driver.getScreenshotAs(OutputType.BYTES);
            Files.write(dir.resolve(namaTahap + ".png"), png);
        } catch (Exception e) {
            System.err.println("[simpanBukti] Gagal menyimpan bukti " + namaTahap + ": " + e.getMessage());
        }
    }

    /**
     * Tap koordinat khusus elemen Canvas/GL yang tidak memiliki node accessibility
     * (misal photo picker).
     */
    protected void tapKoordinat(int x, int y) {
        Map<String, Object> params = new HashMap<>();
        params.put("x", x);
        params.put("y", y);
        driver.executeScript("mobile: clickGesture", params);
    }

    protected void tunggu(long milidetik) {
        try {
            Thread.sleep(milidetik);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
