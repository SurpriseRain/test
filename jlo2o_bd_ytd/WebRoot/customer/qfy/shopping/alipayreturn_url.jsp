<%
/* *
 功能：支付宝页面跳转同步通知页面
 版本：3.2
 日期：2011-03-17
 说明：
 以下代码只是为了方便商户测试而提供的样例代码，商户可以根据自己网站的需要，按照技术文档编写,并非一定要使用该代码。
 该代码仅供学习和研究支付宝接口使用，只是提供一个参考。

 //***********页面功能说明***********
 该页面可在本机电脑测试
 可放入HTML等美化页面的代码、商户业务逻辑程序代码
 TRADE_FINISHED(表示交易已经成功结束，并不能再对该交易做后续操作);
 TRADE_SUCCESS(表示交易已经成功结束，可以对该交易做后续操作，如：分润、退款等);
 //********************************
 * */
%>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.util.*"%>
<%@ page import="java.util.Map"%>
<%@ page import="com.jlsoft.o2o.interfacepackage.alipayTimely.util.*"%>
<%@ page import="com.jlsoft.o2o.interfacepackage.alipayTimely.config.*"%>

<html>
  <head>
		<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
		<title>支付宝页面跳转同步通知页面</title>
		<link rel="stylesheet" type="text/css" href="/customer/qfy/css/shop-Public.css" />
		<script type="text/javascript" src="/js/jquery-1.9.1.min.js"></script>
		<script type="text/javascript" src="/js/commons.js"></script>
		<script type="text/javascript" src="/js/json2.js"></script>
		<script type="text/javascript" src="/js/jquery.cookie.js"></script>
		<script type="text/javascript" src="/customer/qfy/js/pubJson.js"></script>
		<script type="text/javascript">

	//获取公用默认图片路径

	$(document).ready(function(){
		//加载头和尾 
		$("#header").load("/customer/qfy/head-08.jsp?ss="+Math.random(),function(reponse,status){
		if(status=="success"){
		$(".sptms").css({"color":"#ff8800"});
		}
	});
		$("#footer").load("/customer/qfy/foot-08.html");
	});

	function ckdd(){
		window.location.href="/customer/qfy/back/shop-main.html";
	}
	</script>
	<style>
	.zfContent{margin:0 auto;width:1200px;height:auto;}
	.zfContent{display:block;width:1200px;height:400px;}
	.zfbg{width:100%;height:500px;margin:0 auto;background:url(/customer/qfy/images/zfbg_1.jpg);margin:10px 0;}
	.zfbg h4{font-size:35px;color:#fff;width:100%;text-align:center;line-height:200px;font-weight:400;padding-left:20px;}
	a.qd{width:200px;height:50px;line-height:50px;background:#fff;color:#515151;font-size:25px;border:1px solid #c2c2c2;}
	a.qd:hover{background:none;}

</style>
  </head>
  <body>
<%
		Map<String,String> params = new HashMap<String,String>();
		Map requestParams = request.getParameterMap();
		for (Iterator iter = requestParams.keySet().iterator(); iter.hasNext();) {
			String name = (String) iter.next();
			String[] values = (String[]) requestParams.get(name);
			String valueStr = "";
			for (int i = 0; i < values.length; i++) {
				valueStr = (i == values.length - 1) ? valueStr + values[i]
						: valueStr + values[i] + ",";
			}
			valueStr = new String(valueStr.getBytes("ISO-8859-1"), "utf-8");
			//乱码解决，这段代码在出现乱码时使用。如果mysign和sign不相等也可以使用这段代码转化
			params.put(name, valueStr);
		}
		
		//获取支付宝的通知返回参数，可参考技术文档中页面跳转同步通知参数列表(以下仅供参考)//
		//商户订单号
		
		String out_trade_no = new String(request.getParameter("out_trade_no").getBytes("ISO-8859-1"),"UTF-8");
		
		//支付宝交易号
		
		String trade_no = new String(request.getParameter("trade_no").getBytes("ISO-8859-1"),"UTF-8");
		
		//交易状态
		String trade_status = new String(request.getParameter("trade_status").getBytes("ISO-8859-1"),"UTF-8");
		
		//获取支付宝的通知返回参数，可参考技术文档中页面跳转同步通知参数列表(以上仅供参考)//
		
		//计算得出通知验证结果
		boolean verify_result = AlipayNotify.verify(params);
		
		if(verify_result){//验证成功
			//////////////////////////////////////////////////////////////////////////////////////////
			//请在这里加上商户的业务逻辑程序代码
		
			//——请根据您的业务逻辑来编写程序（以下代码仅作参考）——
			if(trade_status.equals("TRADE_FINISHED") || trade_status.equals("TRADE_SUCCESS")){
				//判断该笔订单是否在商户网站中已经做过处理
					//如果没有做过处理，根据订单号（out_trade_no）在商户网站的订单系统中查到该笔订单的详细，并执行商户的业务程序
					//如果有做过处理，不执行商户的业务程序
			}
			
			//该页面可做页面美工编辑
			//response.getWriter().println("验证成功<br />");
			//——请根据您的业务逻辑来编写程序（以上代码仅作参考）——
			//response.sendRedirect("/customer/qfy/shopping/paysuccess.html");
			//////////////////////////////////////////////////////////////////////////////////////////
		}else{
			//该页面可做页面美工编辑
			//response.getWriter().println("验证失败<br />");
			//response.sendRedirect("/customer/qfy/shopping/payerror.html");
		}
%>
<!-- head开始 -->
	<div id="header"></div>
	<!-- head结束 -->
	<!-- 中间开始 -->
	<div class="zfContent"><!-- id="second" -->
		<div class="zfbg">
			<h4>您已成功支付，等待发货&nbsp;.&nbsp;.&nbsp;.</h4>
		</div>
	</div>
	<!-- 中间结束 -->
	
	<a class="qd" onclick="ckdd();">确定</a>
	<!-- footer开始 -->
	<div id="footer" class="clearfix mt20"> </div>
</body>
</html>