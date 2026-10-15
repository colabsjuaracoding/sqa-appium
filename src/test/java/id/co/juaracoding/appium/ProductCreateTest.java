package id.co.juaracoding.appium;

import java.util.List;

import org.openqa.selenium.WebElement;
import org.testng.Assert;
import org.testng.annotations.Test;

import io.appium.java_client.AppiumBy;

public class ProductCreateTest extends BaseAppiumTest {
    @Test
    public void tambahProduk_denganKelimaFotoTerisi_berhasilTersimpan() throws InterruptedException {
        hubungkanKeBackend();
        login("admin1", "Admin1@123");

        driver.findElement(AppiumBy.accessibilityId("Open navigation menu")).click();
        tunggu(800);
        driver.findElement(AppiumBy.accessibilityId("Produk")).click();
        tunggu(1200);
        driver.findElement(AppiumBy.accessibilityId("Tambah")).click();
        tunggu(1200);

        List<WebElement> fieldAtas = driver.findElements(AppiumBy.className("android.widget.EditText"));
        String penanda = String.valueOf(System.currentTimeMillis() % 1_000_000L);
        isi(fieldAtas.get(0), "Produk Appium Demo " + penanda);
        isi(fieldAtas.get(1), "INI DESKRIPSI dari Produk Appium Demo " + penanda);
        isi(fieldAtas.get(2), "150000");
        isi(fieldAtas.get(3), "25");

        List<WebElement> fieldAtribut = scrollSampaiBlokAtributTerlihat(8);
        isi(fieldAtribut.get(0), "Ungu");
        isi(fieldAtribut.get(1), "All Size");
        isi(fieldAtribut.get(2), "Demo Appium " + penanda);
        isi(fieldAtribut.get(3), "Test Field Terakhir");

        for (int slot = 1; slot <= 5; slot++) {
            unggahFotoBerikutnya();
        }

        scrollTurunSampaiTombol("Tambah Produk", 6);
        tombolBerlabel("Tambah Produk").click();
        tunggu(2000);

        boolean modalMasihTerbuka = !driver.findElements(AppiumBy.accessibilityId("Dismiss")).isEmpty();
        Assert.assertFalse(modalMasihTerbuka,
                "Modal wajib tertutup setelah submit berhasil -- kalau masih terbuka, submit ditolak backend");
    }
}