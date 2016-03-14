<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@ page import="com.jlsoft.o2o.interfacepackage.weixin.util.MD5Util"%>
<%@ page import="java.io.BufferedReader"%>
<%@ page import="java.io.InputStreamReader"%>
<%@ page import="java.net.HttpURLConnection"%>
<%@ page import="java.net.URL"%>
<%@ page import="com.jlsoft.utils.JlAppResources"%>
<%@ page import="java.util.*"%>
<%@ page import="com.jlsoft.o2o.interfacepackage.weixin.util.XMLUtil"%>
<%@ page import="com.jlsoft.o2o.interfacepackage.weixin.util.WeixinUtil"%>
<%@ page import="com.jlsoft.o2o.interfacepackage.weixin.RequestHandler"%>
<%@ page import="com.jlsoft.o2o.interfacepackage.weixin.ResponseHandler"%>
<%@ page import="com.jlsoft.o2o.interfacepackage.weixin.client.TenpayHttpClient"%>
<%@ page import="com.jlsoft.o2o.interfacepackage.weixin.client.ClientResponseHandler"%>
<%@ page import="java.io.File" %>
<%
    //商户号 
    String partner = JlAppResources.getProperty("WEIXIN_PARTNER");

    //密钥 
    String key = JlAppResources.getProperty("WEIXIN_PARTNER_KEY");
    
    String cacertPath=JlAppResources.getProperty("WEIXIN_CACERT_PATH");
    
    String passwd=JlAppResources.getProperty("WEIXIN_PARTNER_PASSWD");
    
    String pfxPath=JlAppResources.getProperty("WEIXIN_PFX_PATH");
    
    String out_trade_no=request.getParameter("out_trade_no");
    
    String transaction_id=request.getParameter("transaction_id");
    
    String out_refund_no=request.getParameter("out_refund_no");
    
    String total_fee=request.getParameter("total_fee");
    
    String refund_fee=request.getParameter("refund_fee");
    

    //创建查询请求对象
    RequestHandler reqHandler = new RequestHandler(null, null);
    //通信对象
    TenpayHttpClient httpClient = new TenpayHttpClient();
    //应答对象
    ClientResponseHandler resHandler = new ClientResponseHandler();
    
    //-----------------------------
    //设置请求参数
    //-----------------------------
    reqHandler.init();
    reqHandler.setKey(key);
    reqHandler.setGateUrl("https://mch.tenpay.com/refundapi/gateway/refund.xml");
    
    //-----------------------------
    //设置接口参数
    //-----------------------------
    reqHandler.setParameter("service_version", "1.1");
    reqHandler.setParameter("partner", partner);	
    reqHandler.setParameter("out_trade_no", out_trade_no);	
    reqHandler.setParameter("transaction_id", transaction_id);
    reqHandler.setParameter("out_refund_no", out_refund_no);	
    reqHandler.setParameter("total_fee", total_fee);	
    reqHandler.setParameter("refund_fee", refund_fee);
    reqHandler.setParameter("op_user_id", partner);	
    //操作员密码,MD5处理
    reqHandler.setParameter("op_user_passwd", MD5Util.MD5Encode(passwd,"GBK"));	
    	
    reqHandler.setParameter("recv_user_id", "");	
    reqHandler.setParameter("reccv_user_name", "");
    //-----------------------------
    //设置通信参数
    //-----------------------------
    //设置请求返回的等待时间
    httpClient.setTimeOut(5);	
    //设置ca证书
    httpClient.setCaInfo(new File(cacertPath));
		
    //设置个人(商户)证书
    httpClient.setCertInfo(new File(pfxPath), partner);
    
    //设置发送类型POST
    httpClient.setMethod("POST");     
    
    //设置请求内容
    String requestUrl = reqHandler.getRequestURL();
    httpClient.setReqContent(requestUrl);
    String rescontent = "null";

    //后台调用
    if(httpClient.call()) {
    	//设置结果参数
    	rescontent = httpClient.getResContent();
    	resHandler.setContent(rescontent);
    	resHandler.setKey(key);
    	   	
    	//获取返回参数
    	String retcode = resHandler.getParameter("retcode");
    	
    	//判断签名及结果
    	if(resHandler.isTenpaySign()&& "0".equals(retcode)) {
    	/*退款状态	refund_status	
			4，10：退款成功。
			3，5，6：退款失败。
			8，9，11:退款处理中。
			1，2: 未确定，需要商户原退款单号重新发起。
			7：转入代发，退款到银行发现用户的卡作废或者冻结了，导致原路退款银行卡失败，资金回流到商户的现金帐号，需要商户人工干预，通过线下或者财付通转账的方式进行退款。
			*/
    	String refund_status=resHandler.getParameter("refund_status");
    	
    	out.println("商户退款单号"+out_refund_no+"的退款状态是："+refund_status);
    		

    	} else {
    		//错误时，返回结果未签名，记录retcode、retmsg看失败详情。
    		System.out.println("验证签名失败或业务错误");
    		System.out.println("retcode:" + resHandler.getParameter("retcode")+
    	    	                    " retmsg:" + resHandler.getParameter("retmsg"));
    	    	out.println("retcode:" + resHandler.getParameter("retcode")+
    	    	                    " retmsg:" + resHandler.getParameter("retmsg"));
    	}	
    } else {
    	System.out.println("后台调用通信失败");   	
    	System.out.println(httpClient.getResponseCode());
    	System.out.println(httpClient.getErrInfo());
    	//有可能因为网络原因，请求已经处理，但未收到应答。
    }
    
    //获取debug信息,建议把请求、应答内容、debug信息，通信返回码写入日志，方便定位问题
    System.out.println("http res:" + httpClient.getResponseCode() + "," + httpClient.getErrInfo());
    System.out.println("req url:" + requestUrl);
    System.out.println("req debug:" + reqHandler.getDebugInfo());
    System.out.println("res content:" + rescontent);
    System.out.println("res debug:" + resHandler.getDebugInfo());
    
%>

<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=gbk">
	<title>退款后台调用示例</title>
</head>
<body>
</body>
</html>
