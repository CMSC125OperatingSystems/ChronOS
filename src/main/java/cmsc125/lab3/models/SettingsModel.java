package cmsc125.lab3.models;

import java.util.Properties;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.FileOutputStream;

public class SettingsModel {
    private boolean bgmEnabled = true, sfxEnabled = true, darkModeEnabled = true;
    private int bgmVolume = 100, sfxVolume = 100;
    private static final String FILE_PATH = "settings.properties";

    public SettingsModel() {
        loadSettings();
    }

    public void loadSettings() {
        Properties properties = new Properties();
        try (FileInputStream in = new FileInputStream(FILE_PATH)) {
            properties.load(in);

            bgmEnabled = Boolean.parseBoolean(properties.getProperty("bgmEnabled", "true"));
            sfxEnabled = Boolean.parseBoolean(properties.getProperty("sfxEnabled", "true"));
            darkModeEnabled = Boolean.parseBoolean(properties.getProperty("darkModeEnabled", "true"));
            bgmVolume = Integer.parseInt(properties.getProperty("bgmVolume", "100"));
            sfxVolume = Integer.parseInt(properties.getProperty("sfxVolume", "100"));
        } catch (IOException e) {
            System.out.println("No previous settings found. Using default settings.");
        }
    }

    public void saveSettings() {
        Properties properties = new Properties();
        properties.setProperty("bgmEnabled", String.valueOf(bgmEnabled));
        properties.setProperty("sfxEnabled", String.valueOf(sfxEnabled));
        properties.setProperty("darkModeEnabled", String.valueOf(darkModeEnabled));
        properties.setProperty("bgmVolume", String.valueOf(bgmVolume));
        properties.setProperty("sfxVolume", String.valueOf(sfxVolume));

        try (FileOutputStream out = new FileOutputStream(FILE_PATH)) {
            properties.store(out, "ChronOS Game Settings");
        } catch (IOException e) {
            System.err.println("Error saving settings: " + e.getMessage());
        }
    }

    public boolean isBgmEnabled() { return bgmEnabled; }
    public void setBgmEnabled(boolean bgmEnabled) { this.bgmEnabled = bgmEnabled; }

    public boolean isSfxEnabled() { return sfxEnabled; }
    public void setSfxEnabled(boolean sfxEnabled) { this.sfxEnabled = sfxEnabled; }

    public boolean isDarkModeEnabled() { return darkModeEnabled; }
    public void setDarkModeEnabled(boolean darkModeEnabled) { this.darkModeEnabled = darkModeEnabled; }

    public int getBgmVolume() { return bgmVolume; }
    public void setBgmVolume(int bgmVolume) { this.bgmVolume = bgmVolume; }

    public int getSfxVolume() { return sfxVolume; }
    public void setSfxVolume(int sfxVolume) { this.sfxVolume = sfxVolume; }
}