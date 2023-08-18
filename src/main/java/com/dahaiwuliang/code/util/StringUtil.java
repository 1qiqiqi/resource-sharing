package com.dahaiwuliang.code.util;

import java.util.Random;

/**
 * 字符串操作工具类
 */
public class StringUtil {
    /**
     * 判断字符串是否为空
     */
    public static boolean isEmpty(String str){
        if(str==null||str.trim().equals("")){
            return true;
        }
        return false;
    }

    /**
     * 判断字符串是否不为空
     */
    public static boolean isNotEmpty(String str){
        if(str!=null&&!str.trim().equals("")){
            return true;
        }
        return false;
    }

    /**
     * 生成六位随机数
     */
    public static String genSixRandom(){
        Random random = new Random();
        String result = "";
        for(int i = 0;i<6;i++){
            result+=random.nextInt(10);
        }
        return result;
    }

    /**
     * 去除html标签
     */
    public static String stripHtml(String content){
        //<p>段落替换成换行
        content = content.replaceAll("<p .*?>","\r\n");
        //<br><br/>替换成换行
        content.replaceAll("<br\\s*/?>","\r\n");
        //去掉其它<>之间的东西
        content = content.replaceAll("\\<.*?>","");
        //去掉空格
        content = content.replaceAll(" ","");
        return content;
    }

    /**
     * 转义大于小于
     */
    public static String esc(String content){
        return content.replace("<","&lt;").replace(">","&gt;");
    }
}
