package com.zut.lpf.util;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;

public class GlobalLock {

    public static Semaphore nettyLock=new Semaphore(1);
    public static Semaphore HttpLock=new Semaphore(0);
    public static String remoteId;

    //name转化channelID
    public static ConcurrentHashMap<String,String>  humanToChannelId=new ConcurrentHashMap<>();

    //标记用户登录状态
    public static ConcurrentHashMap<String,Integer> flag=new ConcurrentHashMap<>();


}
