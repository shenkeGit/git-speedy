package com.ershi.tool.gitspeedy.main;

import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Description: TODO <br/>
 * Copyright: (c) 2023 SunTime Co'Ltd Inc. All rights reserved.<br/>
 *
 * @author 沈科
 * @version 1.0
 * @date 2023/1/16 16:50
 * @since JDK11
 */
@Slf4j
public class RunThread extends Thread{

    boolean isSucceed = true;
    InputStream is;
    public RunThread(InputStream is) {
        this.is = is;
    }

    public void run() {
        BufferedReader br1 = new BufferedReader(new InputStreamReader(is), 4096);
        try {
            String line1 = null;
            while ((line1 = br1.readLine()) != null) {
                if (line1 != null){
                    if (line1.contains("error")) {
                        this.isSucceed = false;
                        log.error(line1);
                    } else {
                        log.info(line1);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        finally{
            try {
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public boolean isSucceed() {
        return isSucceed;
    }

    public void setSucceed(boolean succeed) {
        isSucceed = succeed;
    }
}
