<%
/* *
 ���ܣ�֧�����������첽֪ͨҳ��
 �汾��3.3
 ���ڣ�2012-08-17
 ˵����
 ���´���ֻ��Ϊ�˷����̻����Զ��ṩ���������룬�̻����Ը����Լ���վ����Ҫ�����ռ����ĵ���д,����һ��Ҫʹ�øô��롣
 �ô������ѧϰ���о�֧�����ӿ�ʹ�ã�ֻ���ṩһ���ο���

 //***********ҳ�湦��˵��***********
 ������ҳ���ļ�ʱ�������ĸ�ҳ���ļ������κ�HTML���뼰�ո�
 ��ҳ�治���ڱ������Բ��ԣ��뵽�������������ԡ���ȷ���ⲿ���Է��ʸ�ҳ�档
 ��ҳ����Թ�����ʹ��д�ı�����logResult���ú�����com.alipay.util�ļ��е�AlipayNotify.java���ļ���
 ���û���յ���ҳ�淵�ص� success ��Ϣ��֧��������24Сʱ�ڰ�һ����ʱ������ط�֪ͨ
 //********************************
 * */
%>
<%@ page language="java" contentType="text/html; charset=gbk" pageEncoding="gbk"%>
<%@ page import="java.util.*"%>
<%@ page import="com.jlsoft.o2o.interfacepackage.alipayTimely.util.*"%>
<%@ page import="com.jlsoft.o2o.interfacepackage.alipayTimely.config.*"%>
<%@ page import="java.io.BufferedReader"%>
<%@ page import="java.io.InputStreamReader"%>
<%@ page import="java.net.HttpURLConnection"%>
<%@ page import="java.net.URL"%>
<%@ page import="com.jlsoft.utils.JlAppResources"%>
<%
//��ȡ֧����POST����������Ϣ
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
		//����������δ����ڳ�������ʱʹ�á����mysign��sign�����Ҳ����ʹ����δ���ת��
		//valueStr = new String(valueStr.getBytes("ISO-8859-1"), "gbk");
		params.put(name, valueStr);
	}
	
	//��ȡ֧������֪ͨ���ز������ɲο������ĵ���ҳ����תͬ��֪ͨ�����б�(���½����ο�)//
	//�˿����κ�
	String batch_no = new String(request.getParameter("batch_no").getBytes("ISO-8859-1"),"UTF-8");
	//����


	//�˿�ɹ�����
	String success_num = new String(request.getParameter("success_num").getBytes("ISO-8859-1"),"UTF-8");
	//���0<= success_num<= batch_num


	//�����������
	String result_details = new String(request.getParameter("result_details").getBytes("ISO-8859-1"),"UTF-8");
	//��������6.3 �����������˵����


	//�ⶳ�����ϸ
	//String unfreezed_deta = new String(request.getParameter("unfreezed_deta").getBytes("ISO-8859-1"),"UTF-8");
	//��ʽ���ⶳ�ᶩ����^���ᶩ����^�ⶳ����^���׺�^����ʱ��^״̬^������

	//��ȡ֧������֪ͨ���ز������ɲο������ĵ���ҳ����תͬ��֪ͨ�����б�(���Ͻ����ο�)/

	if(AlipayNotify.verify(params)){//��֤�ɹ�
		//////////////////////////////////////////////////////////////////////////////////////////
		//������������̻���ҵ���߼��������
		  String urlvalue=JlAppResources.getProperty("timely_header_url")+"/AlipayOnline/update_thd_notify_url.action?batch_no="+batch_no+"&status=1";
		  URL url = new URL(urlvalue);
          HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
          BufferedReader in = new BufferedReader(new InputStreamReader(urlConnection
              .getInputStream()));
          String inputLine = in.readLine().toString();
		  if("{\"dataType\":\"xml\",\"data\":{\"is_update_success\":\"1\"}}".equals(inputLine)){
		  }else{
		  }   
		//�������������ҵ���߼�����д�������´�������ο�������
		
		//�ж��Ƿ����̻���վ���Ѿ����������֪ͨ���صĴ���
			//���û��������������ôִ���̻���ҵ�����
			//�����������������ô��ִ���̻���ҵ�����
			
		out.println("success");	//�벻Ҫ�޸Ļ�ɾ��

		//�������������ҵ���߼�����д�������ϴ�������ο�������

		//////////////////////////////////////////////////////////////////////////////////////////
	}else{//��֤ʧ��
		  String urlvalue=JlAppResources.getProperty("timely_header_url")+"/AlipayOnline/update_thd_notify_url.action?batch_no="+batch_no+"&status=0&result_details="+result_details;
		  URL url = new URL(urlvalue);
        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
        BufferedReader in = new BufferedReader(new InputStreamReader(urlConnection
            .getInputStream()));
        String inputLine = in.readLine().toString();
		  if("{\"dataType\":\"xml\",\"data\":{\"is_update_success\":\"1\"}}".equals(inputLine)){
		  }else{
		  }   
		out.println("fail");
	}
%>