package org.client.menu.settings;


import java.util.ArrayList;
import java.util.List;

public class SettingsCategory {
    private final String nameUa, nameEn;
    public final List<SettingsPage> pages = new ArrayList<>();

    public SettingsCategory(String nameUa, String nameEn) {
        this.nameUa = nameUa;
        this.nameEn = nameEn;
    }

    public String getName(boolean isUa) { return isUa ? nameUa : nameEn; }
    public void addPage(SettingsPage page) { pages.add(page); }
}
