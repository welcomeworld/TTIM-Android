/*
 * Copyright (c) 2018. welcomeworld All rights reserved
 */

package cn.dmandp.utils;

import cn.dmandp.tt.R;

public class ThemeUtil {
    public static int theme = R.style.AppTheme;

    public static void setTheme(int theme) {
        ThemeUtil.theme = theme;
    }

    public static int getTheme() {
        return ThemeUtil.theme;
    }
}
