package com.digital.ayaz.stockhawk.Util;

import java.text.SimpleDateFormat;

/**
 * Created by Shakeeb on 12/19/16.
 */

public class Utils {

    public static String getFormatedDate(long time, String format){
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        return sdf.format(time);
    }
}
