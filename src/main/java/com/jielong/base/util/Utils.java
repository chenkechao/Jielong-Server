package com.jielong.base.util;

import org.json.JSONException;
import org.json.JSONObject;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Random;

public class Utils {
	
	
	/**
	 * 文件名称：由当前时间戳+4位随机数组成
	 * @return
	 */
	public static String createFileName() {
		LocalDateTime dateTime=LocalDateTime.now();
		DateTimeFormatter formatter=DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
		String fileName=dateTime.format(formatter)+createRandomNum(4);
		
		return fileName;
		
	}
	
	//根据指定长度生成字母和数字的随机数
    //0~9的ASCII为48~57
    //A~Z的ASCII为65~90
    //a~z的ASCII为97~122
    public static String createRandomChar(int length)
    {
        StringBuilder sb=new StringBuilder();
        Random rand=new Random();//随机用以下三个随机生成器
        Random randdata=new Random();
        int data=0;
        for(int i=0;i<length;i++)
        {
            int index=rand.nextInt(3);
            //目的是随机选择生成数字，大小写字母
            switch(index)
            {
            case 0:
                 data=randdata.nextInt(10);//仅仅会生成0~9
                 sb.append(data);
                break;
            case 1:
                data=randdata.nextInt(26)+65;//保证只会产生65~90之间的整数
                sb.append((char)data);
                break;
            case 2:
                data=randdata.nextInt(26)+97;//保证只会产生97~122之间的整数
                sb.append((char)data);
                break;
            }
        }
        String result=sb.toString();
        return result;
    }
                       
    //根据指定长度生成纯数字的随机数
    public static String createRandomNum(int length) {
        StringBuilder sb=new StringBuilder();
        Random rand=new Random();
        for(int i=0;i<length;i++)
        {
            sb.append(rand.nextInt(10));
        }
        String data=sb.toString();
        return data;
    }
    public static  String  getTestToken() throws JSONException {
        StringBuilder url=new StringBuilder("https://api.weixin.qq.com/cgi-bin/token?grant_type=client_credential");
        url.append("&appid=").append(Constants.APPID).append("&secret=").append(Constants.SECRET);
        String result=NetworkConnection.get(url.toString());
        JSONObject jsonObject=new JSONObject(result);
        String token=jsonObject.getString("access_token");
        return token;
    }
    

}
