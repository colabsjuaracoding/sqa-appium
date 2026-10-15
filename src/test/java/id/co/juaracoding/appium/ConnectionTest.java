package id.co.juaracoding.appium;

import io.appium.java_client.AppiumBy;
import org.testng.Assert;
import org.testng.annotations.Test;

public class ConnectionTest extends BaseAppiumTest {

    @Test
    public void koneksiBerhasil_mendaratDiLayarHome() {
        hubungkanKeBackend();

        boolean tombolLoginAda = !driver.findElements(AppiumBy.accessibilityId("Login")).isEmpty();
        Assert.assertTrue(tombolLoginAda,
                "Setelah koneksi berhasil, layar Home (Selamat Datang) wajib tampil dengan tombol Login");
    }
}
