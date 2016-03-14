
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.util.*"%>
<%@ page import="java.util.Map"%>
<%@ page import="com.jlsoft.o2o.interfacepackage.alipayTimely.util.*"%>
<%@ page import="com.jlsoft.o2o.interfacepackage.alipayTimely.config.*"%>

<html>
  <head>
		<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
		<title>微信支付页面跳转同步通知页面</title>
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
		$("#header").load("Head.html", function() {
			  $("#menu-lest").remove();
			  $("#header").find("a[id='qbsp']").attr("class","bian");
		      $("#header").find("a[id='sy']").attr("class","");
			});
		$("#footer").load("/customer/qfy/foot-08.html");
	});

	function ckdd(){
		window.location.href="/customer/qfy/back/shop-main.html";
	}
	</script>
  </head>
  <body>
	<div id="header">
	</div>
	<div id="second">
	<img src="/customer/qfy/images/gou.png" class="gou" />
	<span class="ddfh"><b class="cg">您已成功支付，等待发货...</b></span>
	<div style="clear:both;"></div>
	</div>
	<a class="qd" onclick="ckdd();">确定</a>
	<!-- footer -->
	<div id="footer" class="clearfix mt20"> </div>
  </body>
</html>