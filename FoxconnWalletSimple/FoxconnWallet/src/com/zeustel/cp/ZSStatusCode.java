package com.zeustel.cp;

public class ZSStatusCode {
	public final static int SUCCESS = 0x0;
	public final static String SUCCESS_MSG = "成功！";
	
	public final static int CP_INFO_ERROR = 0x1;
	public final static String CP_INFO_ERROR_MSG = "SDK初始化失败，请检查ZSSDK.getDefault().initSDK方法的cpInfo参数！";
	
	public final static int NETWORK_ERROR = 0x2;
	public final static String NETWORK_ERROR_MSG = "网络连接失败，请检查您的网络设置！";
	
	public final static int REQUEST_ERROR = 0x3;
	public final static String REQUEST_ERROR_MSG = "请求失败！";
	
	public final static int AUTH_CODE_NULL = 0x4;
	public final static String AUTH_CODE_NULL_MSG = "授权码为空，请检查ZSSDK.getDefault().exchange方法的参数！";
	
	public final static int TOTAL_ERROR = 0x5;
	public final static String TOTAL_ERROR_MSG  = "total不能小于0！请检查ZSSDK.getDefault().exchange方法的total参数！";

	public final static int PHONENO_ERROR = 0x6;
	public final static String PHONENO_ERROR_MSG  = "手机号不正确！";
	
	public final static int URL_ERROR = 0x7;
	public final static String URL_ERROR_MSG = "url不正确！";
	
	public final static int ERROR = 0x8;
	
	public final static int DATA_ERROR = 0x9;
	public final static String DATA_ERROR_MSG = "没有返回数据！";
	
	public final static int EX_ERROR = 0x9;
	public final static String EX_ERROR_MSG = "EX！";
}
