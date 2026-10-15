package id.co.juaracoding.appium;

import org.testng.Assert;
import org.testng.annotations.Test;

import io.appium.java_client.AppiumBy;

public class LoginTest extends BaseAppiumTest {

    @Test
    public void loginBerhasil_denganKredensialAdminValid() {
        hubungkanKeBackend();
        login("admin1", "Admin1@123");

        boolean masihDiLayarLogin = !driver.findElements(AppiumBy.className("android.widget.EditText")).isEmpty();
        Assert.assertFalse(masihDiLayarLogin,
                "Setelah login berhasil, layar Login (dengan EditText Username/Password) wajib sudah berpindah");
    }
}