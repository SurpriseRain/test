<%@ page language="java" pageEncoding="UTF-8"%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=utf-8"/>
	<link rel="stylesheet" type="text/css" href="/customer/qfy/css/head-08.min.css?v=015915231009" />
	<link rel="stylesheet" type="text/css" href="/customer/qfy/css/indexSear.css" />
	<link rel="stylesheet" type="text/css" href="/css/edlg.css" />
</head>
<body>
<!--页头导航-->
<div id="site-nav">
    <div id="sn-bd">
        <div class="sn-container">
            <!--城市选择-->
            <div id="sn-citybar">
                <!--默认城市-->
                <div class="city-locate">
                    <span>送至&nbsp;:</span>&nbsp;
                    <a id="city_name" class="current-city">天津</a>
                    <b class="arrow"></b>
                    <div id="city-wrapper">
                        <ul id="city-list" class="city_list" data-read="0">

                        </ul>
                    </div>
                </div>
            </div>

            <!--快捷菜单-->
            <ul class="sn-quick-menu">
                <li class="login-info">
                    <!--登录信息-->
                    <a class="l-login" href="/customer/qfy/register/login.html?type=1">您好，请登录</a>
                    <a class="l-login login-reg" href="/customer/qfy/register/loginzc.html">免费注册</a>
                </li>
                <li class="login-ext">
                    <a class="l-ext" id="login-name" href=""></a>
                    <a class="l-ext" id="login-ext" href="javascript:void(0);">&nbsp;&nbsp;[退出]</a>
                </li>
                <li class="spacer">|</li>
                <li>
                    <a href="/customer/qfy/index.html">商城首页</a></li>
                <li class="spacer">|</li>
                <li>
                    <a href="/customer/qfy/register/login.html" id="u-home">个人中心</a></li>
                <!-- <li class="spacer">|</li>
                <li class="sn-mobile"><a href="javascript:void(0);">手机版</a></li> -->
                <li class="spacer">|</li>
				<li class="sn-serv">
					<a href="javascript:void(0);">服务中心：18519109250</a></li>
            </ul>
        </div>
    </div>
    <div style="display: none;">
        <input type="hidden" id="city_value"/>
        <input type="hidden" id="search-t" value="cp">
    </div>
</div>
<!--页头-->
<div id="p-header">
    <div class="headerLayout">
        <div class="headerCon">
            <!--logo-->
            <h1 id="qfyLogo">
                    <span class="qlogo">
                        <a href="/customer/qfy/index.html"><img src="/customer/qfy/images/logo.png" alt="" height="85"/></a>
                    </span>

            </h1>
            <div class="header-extra">
                <!--我的购物车-->
                <div class="header-mycart">
                    <div class="cw-icon">
                        <i class="icon-cart"></i>
                        <i class="ci-right">&gt;</i><i class="ci-count" id="shopping-amount">0</i>
                        <a href="/customer/qfy/shopping/cart.html">我的购物车</a>
                    </div>

                </div>
                <!--搜索-->
                <div id="qfySearch" class="headerSearch">
                    <div class="search-type">
                        <ul id="seach_tj" class="search-tj">
                            <li class="bg-ff3c3c">产品</li>
                            <li>OEM</li>
                            <li>商品条码</li>
                        </ul>
                    </div>
                    <form class="qSearch-form clearfix" name="searchTop" action=""  style="height:auto;position:relative;">
                        <div class="qSearch-input clearfix">
                            <div class="s-combobox-input-wrap">
                                <input type="text" name="q" autocomplete="off" id="mq" class="s-combobox-inputz" placeholder="输入产品名称可以直接搜索">
                                <!-- <label for="mq" class="s-combobox-placeholder"></label> -->
                            </div>
                            <button type="submit" id="search">搜索</button>
                        </div>
                    </form>
                    <!--热门搜索
                    <ul class="hot-query">
                        <li>
                            热门搜索:</li>
                        <li><a href="###">机油</a>
                        </li>
                        <li><a href="###">机滤</a>
                        </li>
                        <li><a href="###">刹车片</a>
                        </li>
                    </ul>-->
                </div>
            </div>
        </div>
    </div>
</div>
<!--菜单-->
<!--内容导航-->
<div class="moudle">
    <div class="fp-main-nav">
        <div class="main-container">
            <ul class="main-nav-ctn">
                <li><a href="/customer/qfy/index.html">首页</a></li>
                <!-- <li><a href="http://news.motorsc.com/">政策法规</a></li> -->
                <li><a href="/customer/qfy/showMessage/index-tzpj.html">同质配件</a></li>
                <li><a href="/customer/qfy/showMessage/index-fxpp.html">放心品牌</a></li>     
                <li><a href="/customer/qfy/showMessage/bm-index.html">统一编码</a></li>
                <!-- <li><a href="/customer/qfy/showMessage/pbd-index.html">公益品牌</a></li> -->
                <li><a href="/customer/qfy/showMessage/sptmzs.html">商品追朔</a></li>
                <!-- <li><a href="/customer/qfy/showProduct/member_shop_n.html">配件商城</a></li> -->
                <li><a id="pjsc" href="javascript:void(0);">配件商城</a></li>
                <li><a href="/customer/qfy/showMessage/index-zzfw.html">增值服务</a></li>
                <!-- <li><a href="javascript:void(0);" id="reqPub">发布需求</a></li> -->
            </ul>
        </div>
    </div>
</div>
<script type="text/javascript" src="/js/underscore-min.js"></script>
<script type="text/javascript" src="/js/edlg.min.js"></script>
<script type="text/javascript" src="/customer/qfy/js/pubJson.js"></script>
<script type="text/javascript" src="/customer/qfy/js/jquery.autocompleter1.js"></script>
<!--配送至-->
<script type="text/template" id="m-tpl-2">
    {{_.each(mapList, function(item) { }}
    {{ if(item.dqbzm02 !== "黑龙江省") { }}
    <li id="{{= item.dqbzm01 }}">
        <a href="javascript:void(0);">{{= item.dqbzm02.substring(0,2) }}</a>
    </li>
    {{ } else {  }}
    <li id="{{= item.dqbzm01 }}">
        <a href="javascript:void(0);">{{= item.dqbzm02.replace("省","") }}</a>
    </li>
    {{ }  }}
    {{}); }}
</script>
<script type="text/javascript">
    _.templateSettings = {
        evaluate:    /\{\{(.+?)\}\}/g,
        interpolate: /\{\{=(.+?)\}\}/g,
        escape:      /\{\{-(.+?)\}\}/g
    };
    var uinfo = $.cookie('userInfo');
    var usercookie = JSON.parse(uinfo);
    var city_list = $.cookie("city_list");
    var bycode=$.getUrlParam("barcode");
    var oe=$.getUrlParam("spglljh01");
    var SPNAMELIST=$.getUrlParam("SPXX04");
 	// 商品详情页面的搜索栏显示搜索关键词 增加 !="" 判断 20151123 齐俊宇 BEGIN
    if(SPNAMELIST!=null && SPNAMELIST != ""){
    	$("#mq").val(SPNAMELIST);	
    }
    if(oe!=null && oe != ""){
    	$("#seach_tj li").eq(0).removeClass("bg-ff3c3c");
    	$("#seach_tj li").eq(1).addClass("bg-ff3c3c");
    	$("#mq").val(oe);	
    }
    if(bycode!=null && bycode != ""){
    	$("#seach_tj li").eq(0).removeClass("bg-ff3c3c");
    	$("#seach_tj li").eq(2).addClass("bg-ff3c3c");
    	$("#mq").val(bycode);	
    }
	// 商品详情页面的搜索栏显示搜索关键词 增加 !="" 判断 20151123 齐俊宇 END
  
    //cookie误删时，存放默认值
    if(city_list==null){
        city_list = [];
        var o = {};
        o.city_value = "120000";
        o.city_name = "天津";
        o.city_value_2 = "120100";
        o.city_name_2 = "天津市";
        city_list.push(o);
        $.cookie("city_list", JSON.stringify(city_list), {
            expires : 1, path : '/'
        });
        city_list = $.cookie("city_list");
    }
    city_list = JSON.parse(city_list);
    $("#city_value").val(city_list[0].city_value);
    $("#city_name").html(city_list[0].city_name);
    var template1 = "{{ label }} <span>约{{ NUM }}件商品</span>";
    var type1 = "SPMC_STR";
    $(function(){
        /* 大区选择 --start-- */
        
        $(".city-locate").on("mouseenter",function(e){
            var read = $("#city-list").data("read");
            if(read === 0){
                var XmlData=[{'sqlid':'com.jlsoft.o2o.user.sql.select_dqmc','dataType':'Json'}];
                var url="/jlquery/selecto2o.action?XmlData="+JSON.stringify(XmlData);
                ajaxQ({
                    "url" : url,//原始url
                    "callback" : function(rData){
                        var tpl = $.trim($("#m-tpl-2").html()),
                                data = {"mapList":rData};
                        dToHtml({"tpl":tpl,"data":data,"target":"#city-list"});
                        $("#city-list").data("read","1");
                        $("#city-wrapper").css({"display":"block"});

                        $("#city-list").on("click","li",function(e){
                            var $this = $(e.currentTarget),
                                    select_text = $.trim($this.children("a").text()),
                                    b = $this.attr("id"),
                                    $city_wrapper = $("#city-wrapper");

                            //将已选的省份值存放于cookie
                            var XmlData=[{'sqlid':'com.jlsoft.o2o.user.sql.select_dqmc_2','dataType':'Json','dqbzm01':b}];
                            var url="/jlquery/selecto2o.action?XmlData="+JSON.stringify(XmlData);
                            ajaxQ({
                                "url" : url,
                                "callback" : function(rData){
                                    var city_list = [];
                                    var o = {};
                                    o.city_value = b;
                                    o.city_name = select_text;
                                    o.city_value_2 = rData[0].dqbzm01;
                                    o.city_name_2 = rData[0].dqbzm02;
                                    city_list.push(o);
                                    $.cookie("city_list", JSON.stringify(city_list), {
                                        expires : 1, path : '/'
                                    });

                                    $("#city_name").html(select_text);
                                    $("#city_value").val(b);
                                    $city_wrapper.css({"display":"none"});
                                    window.location.reload();
                                },
                                "error":function(){}
                            });

                        });
                    },
                    "error":function(){}
                });
            }else{
                $("#city-wrapper").css({"display":"block"});
            }
        }).on("mouseleave",function(e){
            $("#city-wrapper").css({"display":"none"});
        });
        /* 大区选择 --end-- */

		/* 是否启用搜索引擎 */
        var solr = pubJson.Solr;
        var searchUrl = "";
    	if(solr == "1"){
    		searchUrl = pubJson.O2OUrl + "/customer/qfy/showProduct/member_shop_s.html";
   			$('#mq').autocompleter({
   		        highlightMatches: true,
   		        source: "/SearchHandler/solrFacets.action",
   		        hint: true,
   		        empty: false,
   		        limit : 5,
   		        combine : function(){
   		    		var val = $.trim($('#mq').val());
   		        	var jsonData = [{"q" : val, "type" : type1}];
   		        	return {
   		        		jsonData : JSON.stringify(jsonData),
				        template : template1
   		        	};
   		        },
   		        callback: function (value, index, selected) {
   		         	/* 搜索 --start-- */
	   		 	    //产品搜索
	   		 	    var $searcht = $("#search-t");
   		         	var $mq = $("#mq");
   		         	searchFun($searcht, $mq);
   		         	return false;
   				}
   			});
    		
    	} else {
    		searchUrl = pubJson.O2OUrl + "/customer/qfy/showProduct/member_shop_n.html";
    	}
    	$("#pjsc").attr("href", searchUrl);
        /* 搜索 --start-- */
	    $("#search").on("click",function(e){
	    	
	        var $searcht = $("#search-t"),
		        $mq = $("#mq");
	        if($mq.val()==null || $mq.val()=='') {
				//alert("请输入查询内容");
				//dlgShow({content:"请输入查询内容"});
				if(location.href.indexOf("member_shop_n") < 0 && location.href.indexOf("member_shop_s") < 0){
					location.href = searchUrl;
				}
				return false;
			}
	        searchFun($searcht, $mq);
	        return false;
	      /* //产品搜索
		 	if($searcht.val()=="cp") {
				var cpmc = ($mq.val()).trim();
				location.href= searchUrl + "?SPXX04=" + escape(cpmc);
			}else if($searcht.val()=="oe"){
				var oe = ($mq.val()).trim();
				var url="/QFY/selectSPXXbyOE.action?oe="+oe;
				var data=visitService(url);
				if(data.STATE == 1){
					data=data.data;
					location.href="/customer/qfy/showProduct/product.html?spxx01="+data.SPXX01+"&zcxx01="+data.ZCXX01+"&gsid="+data.ZCXX01+"&gsmc="+escape(data.ZCXX02);
				}else if(data.STATE == 2){
					var oe = ($mq.val()).trim();
					location.href=searchUrl + "?spglljh01="+oe;
				}else{
					//alert("该OEM商品暂无！");
					dlgShow({content:"该OEM商品暂无"});
				}
			}else if($searcht.val()=="tm") {
					//搜索条码
				var sptm = ($mq.val()).trim();
				var url="/QFY/selectSPXXbySPTM.action?sptm="+sptm;
				var data=visitService(url);
				if(data.STATE == 1){
					data=data.data;
					location.href="/customer/qfy/showProduct/product.html?spxx01="+data.SPXX01+"&zcxx01="+data.ZCXX01+"&gsid="+data.ZCXX01+"&gsmc="+escape(data.ZCXX02);
				}else if(data.STATE == 2){
					location.href=searchUrl + "?barcode="+sptm;
				}else{
					//alert("该条码商品暂无！");
					dlgShow({content:"该条码商品暂无"});
				}
			}
		 	return false; */
		 	
	    });
        // 查询条件
        $("#seach_tj").on("click","li",function(e){
            var $this = $(e.currentTarget),
	            $searcht = $("#search-t"),
	            $mq = $("#mq");
	        $this.addClass("bg-ff3c3c").siblings("li").removeClass("bg-ff3c3c");
	        $('#mq').autocompleter('destory');
	        $.autocompleter('clearCache');
	
	        var tjval = $this.text();
	        switch(tjval){
	            case "产品":
	            	$mq.attr("placeholder","输入产品名称可以直接搜索");
	                $searcht.val("cp");
	                type1 = "SPMC_STR";
	                template1 = '{{ label }} <span>约{{ NUM }}件商品</span>';
	                break;
	            case "OEM":
	            	$mq.attr("placeholder","输入产品OEM码");
	                $searcht.val("oe");
	                template1 = '{{ label }}';
	                type1 = "ZJCLJH";
	                break;
	            case "商品条码":
	            	$mq.attr("placeholder","请输入商品条码");
	                $searcht.val("tm");
	                template1 = '{{ label }}';
	                type1 = "BARCODE";
	                break;
	        }
        });
        
     	// 搜索页面跳转
        function searchFun($searcht, $mq){
        	//产品搜索
		 	if($searcht.val()=="cp") {
				var cpmc = ($mq.val()).trim();
				location.href= searchUrl + "?SPXX04=" + escape(cpmc);
			}else if($searcht.val()=="oe"){
				var oe = ($mq.val()).trim();
				var url="/QFY/selectSPXXbyOE.action?oe="+oe;
				var data=visitService(url);
				if(data.STATE == 1){
					data=data.data;
					location.href="/customer/qfy/showProduct/product.html?spxx01="+data.SPXX01+"&zcxx01="+data.ZCXX01+"&gsid="+data.ZCXX01+"&gsmc="+escape(data.ZCXX02);
				}else if(data.STATE == 2){
					var oe = ($mq.val()).trim();
					location.href=searchUrl + "?spglljh01="+oe;
				}else{
					//alert("该OEM商品暂无！");
					dlgShow({content:"该OEM商品暂无"});
				}
			}else if($searcht.val()=="tm") {
					//搜索条码
				var sptm = ($mq.val()).trim();
				var url="/QFY/selectSPXXbySPTM.action?sptm="+sptm;
				var data=visitService(url);
				if(data.STATE == 1){
					data=data.data;
					location.href="/customer/qfy/showProduct/product.html?spxx01="+data.SPXX01+"&zcxx01="+data.ZCXX01+"&gsid="+data.ZCXX01+"&gsmc="+escape(data.ZCXX02);
				}else if(data.STATE == 2){
					location.href=searchUrl + "?barcode="+sptm;
				}else{
					//alert("该条码商品暂无！");
					dlgShow({content:"该条码商品暂无"});
				}
			}
		 	return false;
        }
        
     	// 手机版
        $(".sn-mobile").on("mouseenter",function(e){
        	var $this = $(e.currentTarget),
        		snQrCode = $(".sn-qrcode"),
        		isSQC = snQrCode.length >= 1 ? true : false,
    			tpl = "";
        	if(isSQC){
        		snQrCode.show();
        	}else{
        		tpl = '<div class="sn-qrcode" style="display: none;">' +
            			'<div class="sn-qrcode-content"></div>' +
            				'<p>扫一扫，关注下载APP</p>' +
            				'<b></b>' +
            		  '</div>';
        		$this.append(tpl);
        		$(".sn-qrcode").show();
        	}
        }).on("mouseleave",function(e){
        	$(".sn-qrcode").hide();
        });
        /*
         var oe=$.getUrlParam("spglljh01");
         if(oe!=""&&oe!=null){
         $("#seach_tj2").click();
         $("#sousuo").val(oe);
         }*/
        /* 搜索 --end-- */
    });

    /* 大区选择 --start--*/
    function select_dqmc(){
        var XmlData=[{"sqlid":"com.jlsoft.o2o.user.sql.select_dqmc","dataType":"Json"}];
        var url="/jlquery/selecto2o.action?XmlData="+JSON.stringify(XmlData);
        ajaxQ({
            "url" : url,
            "callback" : function(rData){
                if($("#city-list").data("read") === "1"){
                    return;
                }
                dToHtml({"tpl":$("#m-tpl-2").html(),"data":rData,"target":"#city-list"});
                $("#city-list").data("read","1");

                $("#city-list").on("click","li",function(e){
                    var $this = $(e.currentTarget);
                    var selectSea_text = $this.text();
                    var b = $this.attr("id");
                    $("#city_name").html(selectSea_text);
                    $("#city_value").val(b);
                    $("#city-list").css({"display":"none"});
                    //将已选的省份值存放于cookie
                    var XmlData=[{"sqlid":"com.jlsoft.o2o.user.sql.select_dqmc_2","dataType":"Json","dqbzm01":b}];
                    var url="/jlquery/selecto2o.action?XmlData="+JSON.stringify(XmlData);
                    var rData=visitService(url);
                    var city_list = [];
                    var o = {};
                    o.city_value = b;
                    o.city_name = selectSea_text;
                    o.city_value_2 = rData[0].dqbzm01;
                    o.city_name_2 = rData[0].dqbzm02;
                    city_list.push(o);
                    $.cookie("city_list", JSON.stringify(city_list), {
                        expires : 1, path : '/'
                    });
                });
            },
            "error":function(){}
        });

    }
    /* 大区选择 --end-- */

    /* 购物车 --start-- */
    function selectMyCart(){
        var xmlData=[];
        var json={};
        json.HBID=usercookie.ZCXX01;
        json.DQXX01=usercookie.ZCXX07;
        xmlData.push(json);
        var url="/CartManage/selectGwcByHbid.action?XmlData="+JSON.stringify(xmlData);

        ajaxQ({
            "url" : url,
            "callback" : function(rData){
                if(!rData || rData.GWCLIST == "" || rData.GWCLIST == null) {
                    $("#shopping-amount").html(0);
                }else {
                    $("#shopping-amount").html(rData.GWCLIST[0].TOTALNUM);
                }
            },
            "error":function(){}
        });
    }
    /* 购物车 --end-- */

    /* 检查是否登录 --start--  */
    checkLogin();
    function checkLogin(){
    	if(usercookie!=null){
    		selectMyCart(usercookie);
    		// 
    		$("#login-ext").one("click",function(e){
    			var qm = $(".sn-quick-menu");
    			qm.find("li.login-info").css("display","block");
    			$("#login-name").text("");
    			$("#login-ext").text("");
    			qm.find("li.login-ext").css("display","none");
    			$.cookie('userInfo',null,{path:"/"});
    			$("#shopping-amount").html(0);
    			location.href="/customer/qfy/register/login.html?type=1";
    			return false;
    		});
    		var qm = $(".sn-quick-menu");
    		var ZCXX02="";
    		if(usercookie.ZCXX02==null){
    			ZCXX02 = "游客";
    		}else{
    			ZCXX02 = usercookie.ZCXX02;
    		}
    		$("#login-name").text("您好，"+ZCXX02);
        	//$("#login-name").attr('href','/customer/qfy/showDpxx/shopAllGoods.html?gsid='+usercookie.ZCXX01+'&gsmc='+escape(usercookie.ZCXX02));
    		if(usercookie.ZCGS01==2 && usercookie.LX == 44){
    			$("#login-name").attr('href',"/customer/qfy/register/registerPerfectDBWX.html");
    			$("#u-home").hide();
    			$("#reqPub").hide();
			}else if(usercookie.ZCGS01==2 && (usercookie.LX == 44 || usercookie.LX ==43)){
				$("#login-name").attr('href',"/customer/qfy/register/registerPerfectDB.html");
    			$("#u-home").hide();
    			$("#reqPub").hide();
			}
    		else if(usercookie.ZCGS01==3 && usercookie.LX == 44){
				$("#login-name").attr('href',"/customer/qfy/back/shop-main.html");
    			$("#u-home").attr('href',"/customer/qfy/back/shop-main.html");
    			$("#reqPub").hide();
			}else if((usercookie.ZCGS01== 5 || usercookie.ZCGS01== 7) && usercookie.LX == 44){
				$("#reqPub").hide();
				$("#u-home").attr('href',"/customer/qfy/back/shop-main.html");
				$("#login-name").attr('href',"/customer/qfy/back/shop-main.html");
			}else if((usercookie.LX == 43 && usercookie.ZCGS01 == 3) || (usercookie.LX == 42 && usercookie.ZCGS01 == 3)){
				$("#login-name").attr('href',"/customer/qfy/back/shop-main.html");
    			$("#u-home").attr('href',"/customer/qfy/back/shop-main.html");
			}else if(usercookie.ZCGS01 == 3 || usercookie.ZCGS01 == 5 || usercookie.ZCGS01 == 6 || usercookie.ZCGS01 == 7){
				$("#reqPub").hide();
				$("#u-home").hide();
				$("#login-name").attr('href',"/customer/qfy/back/shop-main.html");
			}else{
    			$("#login-name").attr('href',"/customer/qfy/back/shop-main.html");
    		}
    		qm.find("li.login-info").css("display","none");
    		qm.find("li.login-ext").css("display","block");
    		if(usercookie.ZCGS01== 4){
    			$("#u-home").attr('href','/customer/qfy/back/shop-main.html');
    			$("#login-name").attr('href',"/customer/qfy/back/shop-main.html");
    			//$("#fbsp").attr('href','/customer/qfy/back/shop-main.html?menuId=A001');
    		}else{
    			$("#u-home").hide();
    		}
    		// 车主
    		if(usercookie["LX"] === "24"){
    			/* $("#reqPub").attr("href","/customer/qfy/c2b/CarService.html"); */
    			 $("#login-name").attr('href',"#");
    			 $("#reqPub").hide();
    		}else if(usercookie["LX"] === "44"){
    			// 维修厂
    			$("#reqPub").attr("href","/customer/qfy/c2b/repair/Repari-main.html");
    		}else{
    			$("#reqPub").parent("li").remove();
    		}
    	}else{
    		var qm = $(".sn-quick-menu");
    		qm.find("li.login-info").css("display","block");
    		qm.find("li.login-ext").css("display","none");
    		
    		$("#reqPub").attr("href","/customer/qfy/register/login.html?type=1");
    	}
    }
    /* 检查是否登录 --end--*/

    /* 通过数据构建HTML */
    function dToHtml(obj){
        var tpl = $.trim(obj["tpl"]),
                datas = obj["data"],
                fc =  _.template(tpl),
                rh = fc(datas);
        var target = typeof obj["target"] === "string"?$(obj["target"]):obj["target"];
        target.html(rh);
    }
    

</script>

</body>
</html>