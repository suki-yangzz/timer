package net.susss.timer.example;

import net.susss.timer.common.Constants;

/**
 * Created by Suki Yang on 11/11/2018.
 */
public class Test {

    public static void main(String[] args) {
        String key = "10000%1541151599919_key1";
        System.out.println(key.substring(0, key.indexOf(Constants.ESCAPE_BUCKET)));
        System.out.println(key.substring(key.indexOf(Constants.ESCAPE_BUCKET) + 1, key.indexOf(Constants.ESCAPE_STARTTIME)));
        System.out.println(key.substring(key.indexOf(Constants.ESCAPE_STARTTIME) + 1, key.length()));
    }
}
