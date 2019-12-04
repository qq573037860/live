package com.sjq.live.utils.ffmepg;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class OutHandler extends Thread {
	// 控制线程状态
    volatile boolean status = true;
 
    BufferedReader br = null;
 
    String type = null;
 
    public OutHandler(InputStream is, String type)
    {
        br = new BufferedReader(new InputStreamReader(is));
        this.type = type;
    }
 
    /**
     * 重写线程销毁方法，安全的关闭线程
     */
    @Override
    public void destroy()
    {
        status = false;
    }
 
    /**
     * 执行输出线程
     */
    @Override
    public void run()
    {
        String msg = null;
        try
        {
            while (status)
            {
 
                if ((msg = br.readLine()) != null)
                {
                    System.out.println(type + "消息：" + msg);
                }
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

}
