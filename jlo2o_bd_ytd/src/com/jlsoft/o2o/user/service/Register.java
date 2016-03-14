package com.jlsoft.o2o.user.service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import com.jlsoft.framework.JLBill;
import com.jlsoft.framework.dataset.DataSet;
import com.jlsoft.framework.dataset.IDataSet;
import com.jlsoft.o2o.interfacepackage.V9.V9BasicData;
import com.jlsoft.o2o.interfacepackage.jlinterface.JlInterfaces;
import com.jlsoft.o2o.interfacepackage.jlinterface.XlInterfaces;
import com.jlsoft.o2o.message.Oper_YHZX;
import com.jlsoft.o2o.pub.service.PubService;
import com.jlsoft.o2o.pub.service.SmsService;
import com.jlsoft.scm.service.OperSCM;
import com.jlsoft.utils.JLTools;
import com.jlsoft.utils.JlAppResources;
import com.jlsoft.utils.PubFun;

@Controller
@RequestMapping("/Register")
public class Register extends JLBill {
	private OperSCM operSCM;
	@Autowired
	private SmsService smsService;
	@Autowired
	private PubService pubService;
	@Autowired
	private V9BasicData v9BasicData;
	@Autowired
	private JlInterfaces jlInterFace;
	@Autowired
	private XlInterfaces xlInterFace;
	@Autowired
	private Oper_YHZX peryhzx;
	
	
	@Autowired
	private void setOperSCM(OperSCM operSCM) {
		this.operSCM = operSCM;
	}
	
	/**
	 * @todo 判断用户是否存在
	 * @param json
	 * @return
	 * @throws Exception 
	 */
	@RequestMapping("/checkUserExist")
	public Map<String, Object> checkUserExist(String json) throws Exception{
		Map<String, Object> ResultMap = new HashMap<String, Object>();
		try{
			cds = new DataSet(json);
			String sql = "SELECT COUNT(0) FROM W_ZCGS WHERE 1=1 ";
			//判断用户名是否已存在
			String RYDM = cds.getField("RYDM", 0);
			if(RYDM != null && !RYDM.equals("")){
				sql = sql + " AND XTCZY01='"+RYDM+"'";
			}
			ResultMap.put("num", queryForInt(o2o,sql));
		}catch(Exception ex){
			throw ex;
		}
		return ResultMap;
	} 
	
	/**
	 * @todo 快捷注册(生产企业43 销售企业42 维修企业44 最终用户24)  用于表单和手机APP注册
	 * @param XmlData
	 * @return
	 */
	@RequestMapping("/insertQuickRegister")
	public Map<String, String> insertQuickRegister(String XmlData)
			throws Exception {
		Map<String, String> ResultMap = new HashMap<String, String>();
		try {
			cds = new DataSet(XmlData);
			String zcxx01 = "", gssx = "";
			String gslx = cds.getField("GSLX", 0);
			String xtczy01 = cds.getField("XTCZY01", 0);
			String password = cds.getField("XTCZY02", 0);
			String sjly = cds.getField("SJLY", 0); // 数据来源 默认0:pc 1:app
			//默认审核状态,为待完善,字段 ZCGS01为2
			String ZCGS01 = cds.getField("ZCGS01", 0);
			String phone = cds.getField("ZCXX06", 0);
			String email = "";
			if (gslx.equals("42") || gslx.equals("43")) {
				zcxx01 = JLTools.getID_X(PubFun.updateWBHZT(o2o, "W_ZCGS", 1),
						4);
				gssx = "0";
			} else if (gslx.equals("44") || gslx.equals("24")) {
				zcxx01 = '9' + JLTools.getID_X(PubFun.updateWBHZT(o2o,
						"WLDW_W_ZCXX", 1), 5);
				gssx = "1";
			}

			// 插入相应表数据
			insertZCGS(gslx, zcxx01, phone, email, xtczy01, password, sjly,ZCGS01);
			insertZCXX(zcxx01, xtczy01);
			insertXtczy(zcxx01, xtczy01, password);
			insertGslx(zcxx01, gslx);

			ResultMap.put("ZCXX01", zcxx01);
			ResultMap.put("state", "success");
		} catch (Exception e) {
			ResultMap.put("state", "0");
			throw e;
		}
		return ResultMap;
	}
	
	
	/**
	 * @todo 更新注册信息 用于表单和手机APP注册
	 * @param XmlData
	 * @return
	 * @throws Exception
	 */
	@RequestMapping("/updateRegister")
	public Map<String, String> updateRegister(String XmlData,HttpServletRequest request) throws Exception {
		Map<String, String> ResultMap = new HashMap<String, String>();
		try {
			//加密标记
			/**if(request.getParameter("jmbj").equals("1")){
				XmlData= JLTools.unescape(XmlData);
			}*/
			XmlData= JLTools.unescape(XmlData);
			cds = new DataSet(XmlData);
			String zcxx01 = cds.getField("ZCXX01", 0);
			String sjly = cds.getField("SJLY", 0);
			String gslx = cds.getField("GSLX", 0);
			String  sbm=cds.getField("sbm", 0);
			String GSYHK02 = cds.getField("GSYHK02", 0);
			String GSYHK03 = cds.getField("GSYHK03", 0);
			String GSYHK09 = cds.getField("GSYHK09", 0);
			//更新厂家识别码
			if(zcxx01!=null && !"44".equals(gslx)){
				String queryExists="select 1 from w_zcgssbm where ZCXX01='"+zcxx01+"'";
				String insertsql = "insert into  w_zcgssbm values('"+zcxx01+"','"+sbm+"',"+"null)";
				String deletesql = "delete from  w_zcgssbm where ZCXX01='"+zcxx01+"'";
				
				List ls=queryForList(o2o, queryExists, new HashMap());
				if(ls.size()>0){

					Map row = getRow(deletesql, null, 0);
					execSQL(o2o, deletesql, row);
				}
				Map row = getRow(insertsql, null, 0);
				execSQL(o2o, insertsql, row);
			}
			// 更新W_ZCGS表
			String sql = "UPDATE W_ZCGS SET ZCXX02=ZCXX02?,ZCXX03=ZCXX03?,ZCXX06=ZCXX06?,ZCXX07=ZCXX07?,ZCXX08=ZCXX08?,"
					+ "ZCXX09=ZCXX09?,ZCXX55=ZCXX55?,WZM=WZM?,ZCXX57=ZCXX57?,ZCGS01=3 WHERE XTCZY01=XTCZY01?";
			Map row = getRow(sql, null, 0);
			execSQL(o2o, sql, row);
			//更新W_ZCXX表
			String Longitude = cds.getField("Longitude", 0);
			String Latitude = cds.getField("Latitude", 0);
			System.out.println(Longitude+"  +++++++++++  "+Latitude);
			//2015.12.21 一拖多业务修改,zcxx里zcxx65字段(仓库类型默认为0)
			sql = "UPDATE W_ZCXX SET ZCXX02=ZCXX02?,ZCXX03=ZCXX03?,ZCXX06=ZCXX06?,ZCXX07=ZCXX07?,ZCXX08=ZCXX08?,"+ 
					 "ZCXX09=ZCXX09?,ZCXX55=ZCXX55?,ZCXX57=ZCXX57?,ZCXX64=ZCXX64?,ZCXX65=0,ZCXX67=ZCXX67?," +
					 "ZCXX70=ZCXX70?,Longitude="+Longitude+",Latitude="+Latitude+" WHERE XTCZY01=XTCZY01?";
			execSQL(o2o, sql, row);
			//公司银行卡处理.生产和经销企业更新银行卡
			if(("42".equals(gslx) || "43".equals(gslx)) && gslx != null){
				int GSYHK01=JLTools.getJLBH(o2o,"W_GSYHK",1);
				sql = "INSERT INTO W_GSYHK(GSYHK01,ZCXX01,GSYHK02,GSYHK03,GSYHK09,GSYHK10,GSYHK11) " +
					  "VALUES("+GSYHK01+",'"+zcxx01+"','"+GSYHK02+"','"+GSYHK03+"','"+GSYHK09+"',now(),1)";
				Map yhkRow = getRow(sql, null, 0);
				execSQL(o2o, sql, yhkRow);
			}
			//当是PC注册时图片处理
			if(sjly.equals("0")){
				MultipartHttpServletRequest mrRequest=(MultipartHttpServletRequest)request;
				Iterator iterator = mrRequest.getFileNames();
				while(iterator.hasNext()){
					String fileType = (String)iterator.next();
					MultipartFile file = mrRequest.getFile(fileType);
					if(fileType.equals("dp_logo")){
						//logo图片单独上传
						updateDPLogo(file,XmlData);
					}else{
						saveZCTP(file,fileType,XmlData);
					}
				}
			}
			// 当是APP注册时图片处理
			if (sjly.equals("1")) {
				// 上传身份证正面
				saveDPTP(XmlData, "fssfzzm", "fssfzzmFileType");
				// 上传身份证反面
				saveDPTP(XmlData, "fssfzfm", "fssfzfmFileType");
				// 上传营业执照副本
				saveDPTP(XmlData, "yyzzfb", "yyzzfbFileType");
			}
			ResultMap.put("state", "success");
		} catch (Exception ex) {
			ResultMap.put("state", "error");
			throw ex;
		}
		return ResultMap;
	}
	
	
	/**
	 * @todo 更新注册信息 沈阳兴隆注册
	 * @param XmlData
	 * @return
	 * @throws Exception
	 */
	@RequestMapping("/updateSytjtRegister")
	public Map<String, String> updateSytjtRegister(String XmlData,HttpServletRequest request) throws Exception {
		Map<String, String> ResultMap = new HashMap<String, String>();
		try {
			//加密标记
			/**if(request.getParameter("jmbj").equals("1")){
				XmlData= JLTools.unescape(XmlData);
			}*/
			XmlData= JLTools.unescape(XmlData);
			cds = new DataSet(XmlData);
			String zcxx01 = cds.getField("ZCXX01", 0);
			String sjly = cds.getField("SJLY", 0);
			String xtczy01 = cds.getField("XTCZY01", 0);
			// 更新W_ZCGS表
			String sql = "UPDATE W_ZCGS SET ZCXX02=ZCXX02?,ZCXX03=ZCXX03?,ZCXX06=ZCXX06?,ZCXX07=ZCXX07?,ZCXX08=ZCXX08?,"
					+ "ZCXX09=ZCXX09?,ZCXX55=ZCXX55?,WZM=WZM?,ZCXX57=ZCXX57?,ZCGS01=4 WHERE XTCZY01=XTCZY01?";
			Map row = getRow(sql, null, 0);
			execSQL(o2o, sql, row);
			// 更新W_ZCXX表
			sql = "UPDATE W_ZCXX SET ZCXX02=ZCXX02?,ZCXX03=ZCXX03?,ZCXX06=ZCXX06?,ZCXX07=ZCXX07?,ZCXX08=ZCXX08?,"
					+ "ZCXX09=ZCXX09?,ZCXX55=ZCXX55?,ZCXX57=ZCXX57? WHERE XTCZY01=XTCZY01?";
			execSQL(o2o, sql, row);
			sql = "SELECT A.ZCXX01,A.ZCGS01,B.LX GSLX,A.ZCXX06 SJHM,A.ZCXX02, " +
					 "A.ZCXX02,'' KHPYM,A.ZCXX07,A.ZCXX08,A.ZCXX14,A.XTCZY01 " +
					 "FROM W_ZCGS A,W_GSLX B WHERE A.ZCXX01=B.GSID AND A.XTCZY01='"
					 + xtczy01 + "'";
			Map zcxxMap = (Map) queryForMap(o2o, sql);
			String gslx = zcxxMap.get("GSLX").toString();
			//插入操作员菜单表
			insertCzMenu(xtczy01, gslx);
			//发送短信
			/*if(JLTools.getCurConf(2) == 1){
				String context = "尊敬的"+zcxxMap.get("ZCXX02").toString()+"您好，您的企业审核已经通过，欢迎您加入汽服云平台！";
				smsService.sendSms(5, zcxxMap.get("ZCXX01").toString(), zcxxMap.get("SJHM").toString(), context, zcxxMap.get("ZCXX01").toString());
			}*/
			ResultMap.put("gszt", "4");
			ResultMap.put("state", "success");
		} catch (Exception ex) {
			ResultMap.put("state", "error");
			throw ex;
		}
		return ResultMap;
	}
	
	/**
	 * @todo 更新买家和卖家LOGO
	 * @param file
	 * @param fileType
	 * @param XmlData
	 * @throws Exception 
	 */
	public void updateDPLogo(MultipartFile file,String XmlData) throws Exception{
		try{
			System.out.println(file.getOriginalFilename()+"  +++++++");
			//if(file.getOriginalFilename() != null){
				cds = new DataSet(XmlData);
				String zcxx01 = cds.getField("ZCXX01", 0);
				//获取图片
				//String fileName = JLTools.getDateTime()+(int)(Math.random()*1000);
				String fileName = zcxx01+"_logo";
				fileName = fileName + "." + file.getOriginalFilename().split("\\.")[1];
				//上传图片到服务器
				Map map = new HashMap();
				map.put("imgPath", JlAppResources.getProperty("path_dptp")+cds.getField("ZCXX01", 0));
				map.put("imgName", fileName);
				JLTools.fileUploadNew(file,map);
				//更新数据库
				String sql = "UPDATE W_ZCXX SET ZCXX27='"+fileName+"' WHERE ZCXX01='"+zcxx01+"'";
				execSQL(o2o, sql, map);
			//}
		}catch(Exception ex){
			throw ex;
		}
	}
	
	/**
	 * @todo PC端注册时保存图片
	 * @param file
	 * @param json
	 * @throws Exception 
	 */
	public void saveZCTP(MultipartFile file,String fileType,String XmlData) throws Exception{
		try{
			cds = new DataSet(XmlData);
			//获取图片名称和图片类型
			String fileName = fileType;
			String tplx = fileType.split("_")[1];
			String fileCode = fileType.split("_")[0]+"_code";
			fileName = fileName + "." + file.getOriginalFilename().split("\\.")[1];
			//上传图片到服务器
			Map map = new HashMap();
			map.put("imgPath", JlAppResources.getProperty("path_dptp")+cds.getField("ZCXX01", 0));
			map.put("imgName", fileName);
			JLTools.fileUploadNew(file,map);
			//写入W_DPTP
			String zjhm = (cds.getField(fileCode, 0)==null)?"":cds.getField(fileCode, 0);
			Map dptpMap = new HashMap();
			dptpMap.put("ZCXX01", cds.getField("ZCXX01", 0));
			dptpMap.put("TPLX", tplx);
			dptpMap.put("TPNAME", fileName);
			dptpMap.put("ZJHM", zjhm);
			insertW_DPTP(dptpMap);
		}catch(Exception ex){
			throw ex;
		}
	}
	
	/**
	 * @todo 保存图片 用户APP更新注册图片
	 * @param zcxx01
	 * @param fileNameKey  临时图片KEY
	 * @param tpStreanName  图片类型名称，如：yyzzfb
	 * @param Tplx  图片类型
	 * @throws Exception
	 */
	public void saveDPTP(String XmlData, String fileNameKey, String fileType)
			throws Exception {
		cds = new DataSet(XmlData);
		String fileName = cds.getField(fileNameKey, 0);
		if (fileName != null && !fileName.equals("")) {
			String Tplx = cds.getField(fileType, 0);
			String zcxx01 = cds.getField("ZCXX01", 0);
			String fileUrl = JlAppResources.getProperty("tempFile") + fileName;
			String inFileUrl = JlAppResources.getProperty("path_dptp") + zcxx01;
			String inFileName = fileNameKey + "_" + Tplx + "."
					+ fileName.split("\\.")[1]; // yyzzfb_3.jpg
			Map returnVal = JLTools.uploadFormUrlToOtherUrl(fileUrl, inFileUrl,
					inFileName);
			if (returnVal.get("state").equals("sucess")) {
				// 删除临时图片
				File file = new File(fileUrl);
				if (file.isFile() && file.exists()) {
					file.delete();
				}
				//写入W_DPTP
				Map map = new HashMap();
				map.put("ZCXX01", zcxx01);
				map.put("TPLX", Tplx);
				map.put("TPNAME", inFileName);
				map.put("ZJHM", "");
				insertW_DPTP(map);
			}
		}
	}

	/**
	 * @todo 获取流生成店铺注册时候的图片
	 * @param ins
	 *            后台获取图片流
	 * @param zcxx01
	 *            注册公司
	 * @param Tplx
	 *            图片类型（1身份证正面 2生分证反面 3营业执照副本）
	 * @param fileName
	 *            （图片名称）
	 * @return
	 * @throws Exception
	 */
	@RequestMapping("/insertDPTP")
	public Map<String, Object> insertDPTP(HttpServletRequest request,
			HttpServletResponse response, String filename) throws Exception {
		Map<String, Object> resultMap = new HashMap<String, Object>();
		try {
			request.setCharacterEncoding("utf-8");
			// ServletInputStream inStream = request.getInputStream(); //
			// 取HTTP请求流
			/*
			 * int size = request.getContentLength(); // 取HTTP请求流长度 byte[]
			 * buffer = new byte[size]; // 用于缓存每次读取的数据 byte[] result = new
			 * byte[size]; // 用于存放结果的数组 int count = 0; int rbyte = 0; // 循环读取
			 * while (count < size) { rbyte = inStream.read(buffer); //
			 * 每次实际读取长度存于rbyte中 sflj for (int i = 0; i < rbyte; i++) {
			 * result[count + i] = buffer[i]; } count += rbyte; } InputStream
			 * ins = new ByteArrayInputStream(result);
			 */
			MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) request;
			MultipartFile file = multipartRequest.getFiles("file").get(0);
			long size = file.getSize();
			byte[] data = new byte[(int) size];
			InputStream inStream = file.getInputStream();

			String fileUrl = JlAppResources.getProperty("tempFile").substring(
					0, JlAppResources.getProperty("tempFile").length() - 1);
			String inFileName = System.currentTimeMillis() + "."
					+ filename.split("\\.")[1];
			// 组成图片名称，上传图片
			JLTools.fileUploadIoStream(inStream, fileUrl, inFileName);

			resultMap.put("imgName", inFileName);
			resultMap.put("STATE", "success");
		} catch (Exception ex) {
			resultMap.put("STATE", "success");
			throw ex;
		}
		return resultMap;
	}

	/**
	 * @todo 插入店铺图片
	 * @param hm
	 * @throws Exception
	 */
	public void insertW_DPTP(Map<String, Object> hm) throws Exception {
		try {
			String ZCXX01 = hm.get("ZCXX01").toString();
			String TPLX = hm.get("TPLX").toString();
			String TPNAME = hm.get("TPNAME").toString();
			String ZJHM = hm.get("ZJHM").toString();
			// 删除原有图片
			String sql = "DELETE FROM W_DPTP WHERE ZCXX01='" + ZCXX01
					+ "' AND DPTP01=" + TPLX + "";
			execSQL(o2o, sql, hm);
			// 插入新图片
			sql = "INSERT INTO W_DPTP(ZCXX01,DPTP01,DPTP02,DPTP04,DPTP05) "
					+ "VALUES('" + ZCXX01 + "'," + TPLX + ",'" + TPNAME
					+ "',now(),'"+ZJHM+"')";
			execSQL(o2o, sql, hm);
		} catch (Exception ex) {
			throw ex;
		}
	}

	/**
	 * @todo 查询注册信息
	 * @param json
	 * @return
	 * @throws Exception
	 */
	@RequestMapping("/getZCXX")
	public Map<String, Object> getZCXX(String json) throws Exception {
		String sql = "";
		Map<String, Object> resultMap = new HashMap<String, Object>();
		try {
			cds = new DataSet(json);
			String xtczy01 = cds.getField("XTCZY01", 0);
			String zcxx01 = cds.getField("ZCXX01", 0);
			sql = "SELECT A.ZCXX01,A.XTCZY01,A.ZCXX02,A.ZCXX07,A.ZCXX08,A.ZCXX06,A.ZCXX55,A.ZCXX56,A.ZCGS01,B.LX "+ 
				  "FROM W_ZCGS A,W_GSLX B WHERE A.ZCXX01=B.GSID AND XTCZY01='"+xtczy01+"'";
			resultMap = queryForMap(o2o, sql, resultMap);
			// 查询店铺图片
			sql = "SELECT ZCXX01,DPTP01 TPLX,DPTP02 TPMC FROM W_DPTP WHERE ZCXX01='"
					+ zcxx01 + "'";
			List list = queryForList(o2o, sql, resultMap);
			resultMap.put("dptpList", list);
		} catch (Exception ex) {
			throw ex;
		}
		return resultMap;
	}
	/**
	 * @todo 流程-审核注册信息
	 * @param XmlData
	 * @return
	 * @throws Exception
	 */
	@Transactional(propagation=Propagation.SUPPORTS)
	@RequestMapping("/auditRegister")
	public Map<String, String> auditRegister(String json) throws Exception {
		Map<String, String> ResultMap = new HashMap<String, String>();
		try {
			cds = new DataSet(json);
			String xtczy01 = cds.getField("XTCZY01", 0);
			String updateStatus=cds.getField("updateStatus", 0);
			String zcxx01 = cds.getField("ZCXX", 0);
			//状态：4 最终审核状态
			if(updateStatus.equals("4")){
				// 获取行业管理码
				int hyglm = PubFun.updateWBHZT(o2o, "TEMP_HYGLM", 1);
				// 更新W_ZCGS表
				String sql = "UPDATE W_ZCGS  SET ZCGS01='"+updateStatus+"',HYGLM='" + hyglm
						+ "' WHERE XTCZY01='" + xtczy01 + "'";
				execSQL(o2o, sql, ResultMap);
				// 查ZCXX01值
				sql = "SELECT A.ZCXX01,A.ZCGS01,B.LX GSLX,A.ZCXX06 SJHM,A.ZCXX02, " +
						 "A.ZCXX02,'' KHPYM,A.ZCXX07,A.ZCXX08,A.ZCXX14,A.XTCZY01, " + 
						 "(SELECT ZCXX65 FROM W_ZCXX C WHERE C.XTCZY01 = A.XTCZY01) ZCXX65  " +
						 "FROM W_ZCGS A,W_GSLX B WHERE A.ZCXX01=B.GSID AND A.XTCZY01='"
						 + xtczy01 + "'";
				Map zcxxMap = (Map) queryForMap(o2o, sql);
				String gslx = zcxxMap.get("GSLX").toString();
				//插入ck和gsck表格,type 默认为 0(虚拟残酷),ck09仓库属性默认为0 正常,
				if (gslx.equals("42") || gslx.equals("43")) {
					String ERPDZ = JlAppResources.getProperty("ERPDZ");
					  String ck01 = JLTools.getID_X(PubFun.updateWBHZT(o2o, "CK", 1),
							8);
					  String ck02 = zcxxMap.get("ZCXX02").toString();
					  String gsxx01 = zcxxMap.get("ZCXX01").toString();
					  ck02 = ck02 +"仓库";
					  String insertSQL = "INSERT INTO CK(ck01,ck02,ck09,gsxx01,ERPDZ,type) " +
							  			 " VALUES('" + ck01 + "','" + ck02 + "',0,'"+gsxx01+"','"+ERPDZ+"','0')";
					  Map row = getRow(insertSQL, null, 0);
					  execSQL(o2o, insertSQL, row);
					  insertSQL = "INSERT INTO W_GSCK(ZCXX01,CK01) "+
							      " VALUES('"+gsxx01+"','"+ck01+"')";
					  execSQL(o2o, insertSQL, row);
				} 
				String zcxx65 = "";
				if(zcxxMap.get("ZCXX65") != null){
					zcxx65 = zcxxMap.get("ZCXX65").toString();
				}
				//更新W_ZCXX表
				String ZCXX69 = cds.getField("ZCXX69", 0);
				String ZCXX01=zcxxMap.get("ZCXX01").toString();
				// TODO 待确定 ZCXX01
				sql="UPDATE W_ZCXX SET ZCXX69="+ZCXX69+" WHERE ZCXX01='"+ZCXX01+"'";
				execSQL(o2o, sql, ResultMap);
				//插入操作员菜单表
				insertCzMenu(xtczy01, gslx, zcxx65);
				//发送短信
				if(JLTools.getCurConf(2) == 1){
					String context = "尊敬的"+zcxxMap.get("ZCXX02").toString()+"您好，您的企业审核已经通过，欢迎您加入汽服云平台！";
					smsService.sendSms(5, zcxxMap.get("ZCXX01").toString(), zcxxMap.get("SJHM").toString(), context, zcxxMap.get("ZCXX01").toString());
				}
				//跟新ERP对接地址
				if(JLTools.getCurConf(3) == 1){
					sql = "SELECT ZCXX58,ZCXX59,ZCXX60,ZCXX61 FROM W_ZCXX WHERE ZCXX01='JL'";
					Map erpMap = queryForMap(o2o,sql);
					sql = "UPDATE W_ZCXX SET ZCXX58='"+erpMap.get("ZCXX58").toString()+"',ZCXX59='"+erpMap.get("ZCXX59").toString()+"',ZCXX60='"+erpMap.get("ZCXX60").toString()+"',ZCXX61='"+erpMap.get("ZCXX61").toString()+"' WHERE ZCXX01='"+zcxxMap.get("ZCXX01").toString()+"'";
					execSQL(o2o, sql, ResultMap);
				}
				//对接ERP系统
				Map map = pubService.getECSURL("JL");
				if(map.get("DJLX") != null){
					//写入问题状态表
					pubService.insertW_WTDJ(5,zcxxMap.get("ZCXX01").toString(),0,0);
					if(map.get("DJLX").equals("V9")){
						v9BasicData.registerDock(zcxxMap,map);
					}
				}
			}else{
				String sql = "UPDATE W_ZCGS  SET ZCGS01='"+updateStatus+"' WHERE XTCZY01='" + xtczy01 + "'";
				execSQL(o2o, sql, ResultMap);
			}
			//跟新写入W_ZCGS_SHJL
			String Content = cds.getField("Content", 0);
			if(Content!=null && Content.length()>0){
				String CZZH = cds.getField("ZCXX01", 0);
				String sqlYJ = "INSERT INTO W_ZCGS_SHJL(ZCXX01,CZZH,CZSJ,CONTENT) " +
									  "VALUES('" + zcxx01 + "','" + CZZH + "',now(),'"+Content+"')";
				execSQL(o2o, sqlYJ, new HashMap());
			}
			ResultMap.put("gszt", "4");
			ResultMap.put("state", "success");
		} catch (Exception ex) {
			ResultMap.put("state", "failure");
			throw ex;
		}
		return ResultMap;
	}
	
	
	/**
	 * @todo 注册用户修改显示最新一条驳回意见
	 * @param json
	 * @return
	 * @throws Exception
	 */
	@RequestMapping("/selectW_ZCGSHF")
	public Map selectW_ZCGSHF(String json) throws Exception{
		Map<String, String> resultMap = new HashMap<String, String>();
		cds = new DataSet(json);
		String sql = "SELECT ZCXX01,CZZH,CZSJ,CONTENT FROM W_ZCGS_SHJL WHERE ZCXX01='"+cds.getField("XTCZY01", 0)+"' ORDER BY ID DESC LIMIT 1";
		resultMap = queryForMap(o2o,sql);
		return resultMap;
	}
	
	/**
	 * @todo 通过注册用户获取对应公司类型【GSLX】
	 * @param json
	 * @return
	 * @throws Exception
	 */
	@RequestMapping("/getGSLX")
	public Map<String, String> getGSLX(String json) throws Exception {
		Map<String, String> ResultMap = new HashMap<String, String>();
		try{
			cds = new DataSet(json);
			String person_id=cds.getField("person_id", 0);
			String sql = "SELECT "+
							   "CASE WHEN B.LX='43' THEN '生产厂家'  "+
							   "WHEN B.LX='44' THEN '维修厂' "+
							   "WHEN B.LX='42' THEN '分销商' "+
							   "END GSLX "+
							   "FROM W_XTCZY A,W_GSLX B WHERE A.GSID=B.GSID AND A.PERSON_ID='"+person_id+"'";
			ResultMap = queryForMap(o2o,sql);
		}catch(Exception ex){
			throw ex;
		}
		return ResultMap;
	}
	
	/**
	 * 插入操作员菜单
	 * @param XTCZY01
	 * @param GSLX
	 * @param ZCXX65
	 * @throws Exception
	 */
	public void insertCzMenu(String XTCZY01, String GSLX, String ZCXX65) throws Exception {
		try {
			String mrczy = "mz123456";
			if (GSLX.equals("43") && ZCXX65.equals("0")) {
				// 库存托管生产企业
				mrczy = "JL_MRSCCJ43";
			} else if (GSLX.equals("43") && ZCXX65.equals("1")){
				// 库存自管生产企业
				mrczy = "JL_MRSCCJ43_KCZG";
			} else if (GSLX.equals("44")) {
				mrczy = "JL_MRWXC44";
			} else if (GSLX.equals("42") && ZCXX65.equals("0")){
				// 库存托管经销商
				mrczy = "JL_MRJXS42";
			} else if (GSLX.equals("42") && ZCXX65.equals("1")){
				// 库存自管经销商
				mrczy = "JL_MRJXS42_KCZG";
			} else if(GSLX.equals("24")){
				mrczy = "JL_MRJXS24";
			}
			String insertXtczycz = "INSERT INTO W_XTCZYCZ\n"
					+ "  (PERSON_ID, CD)\n" + "  SELECT '" + XTCZY01
					+ "', W.CD FROM W_XTCZYCZ W WHERE PERSON_ID = '" + mrczy
					+ "'";
			Map row1 = getRow(insertXtczycz, null, 0);
			execSQL(o2o, insertXtczycz, row1);
		} catch (Exception e) {
			e.printStackTrace();
			throw new Exception("插入操作菜单异常！");
		}
	}
	
	/**
	 * @todo 插入操作员菜单
	 * @param XTCZY01
	 * @param GSLX
	 * @throws Exception
	 */
	public void insertCzMenu(String XTCZY01, String GSLX) throws Exception {
		try {
			String mrczy = "mz123456";
			if (GSLX.equals("43")) {
				mrczy = "JL_MRSCCJ43";
			} else if (GSLX.equals("44")) {
				mrczy = "JL_MRWXC44";
			} else if (GSLX.equals("42")){
				mrczy = "JL_MRJXS42";
			} else if(GSLX.equals("24")){
				mrczy = "JL_MRJXS24";
			}
			String insertXtczycz = "INSERT INTO W_XTCZYCZ\n"
					+ "  (PERSON_ID, CD)\n" + "  SELECT '" + XTCZY01
					+ "', W.CD FROM W_XTCZYCZ W WHERE PERSON_ID = '" + mrczy
					+ "'";
			Map row1 = getRow(insertXtczycz, null, 0);
			execSQL(o2o, insertXtczycz, row1);
		} catch (Exception e) {
			e.printStackTrace();
			throw new Exception("插入操作菜单异常！");
		}
	}

	/**
	 * 新增--普通注册(生产企业43 销售企业42 维修企业44 最终用户24)
	 * 
	 * @param XmlData
	 * @return
	 * @throws Exception
	 */
	@RequestMapping("/insert.action")
	public Map<String, String> insert(String XmlData) throws Exception {
		Map<String, String> ResultMap = new HashMap<String, String>();
		try {
			cds = new DataSet(XmlData);
			String zcxx01 = "", gssx = "";
			String gslx = cds.getField("ZCGS03", 0);
			String xtczy01 = cds.getField("XTCZY01", 0);
			String password = cds.getField("XTCZY02", 0);
			String email = cds.getField("ZCXX09", 0);
			String phone = cds.getField("ZCXX26", 0);

			if (gslx.equals("43")) {
				// 生产企业
				zcxx01 = JLTools.getID_X(PubFun.updateWBHZT(o2o, "W_ZCGS", 1),
						4);
				gssx = "0";
			} else if (gslx.equals("42")) {
				// 销售企业
				zcxx01 = JLTools.getID_X(PubFun.updateWBHZT(o2o, "W_ZCGS", 1),
						4);
				gssx = "0";
			} else if (gslx.equals("44")) {
				// 维修企业
				zcxx01 = '9' + JLTools.getID_X(PubFun.updateWBHZT(o2o,
						"WLDW_W_ZCXX", 1), 5);
				gssx = "1";
			} else if (gslx.equals("24")) {
				// 用户
				zcxx01 = '9' + JLTools.getID_X(PubFun.updateWBHZT(o2o,
						"WLDW_W_ZCXX", 1), 5);
				gssx = "1";
			}

			System.out.println("zcxx01:" + zcxx01 + "email=" + email);
			insertZCGS(gslx, zcxx01, phone, email, xtczy01, password, "0","2");
			if (gslx.equals("24")) {// 最终用户
				insertZCXX(zcxx01, xtczy01);
				insertXtczy(zcxx01, xtczy01, password);
				insertGslx(zcxx01, gslx);
				// 插入业务单据信息表
				insertYwdjxx_QFY(zcxx01, zcxx01);
			}
			ResultMap.put("state", "1");// 成功
		} catch (Exception e) {
			ResultMap.put("state", "0");//
			throw e;
		}
		return ResultMap;
	}

	/**
	 * @todo 修改--资料完善
	 * @param XmlData
	 * @return
	 * @throws Exception
	 */
	@RequestMapping("/insertzcgs.action")
	public Map<String, Object> insertzcgs(HttpServletRequest request,
			HttpServletResponse response, String XmlData) throws Exception {
		XmlData = request.getParameter("XmlData");
		System.out.println(XmlData);
		Map<String, Object> hm = new HashMap<String, Object>();
		DiskFileItemFactory factory = new DiskFileItemFactory();
		ServletFileUpload upload = new ServletFileUpload(factory);
		if (!upload.isMultipartContent(request)) {
			hm.put("Flag", "error");
			return hm;
		}
		cds = new DataSet(XmlData);
		JSONArray jsonList = JSONArray.fromObject(XmlData);
		List<HashMap<String, Object>> listMap = (List<HashMap<String, Object>>) jsonList;
		hm = (Map<String, Object>) listMap.get(0);
		System.out.println("hm:" + hm);
		String registStatus = "regeditOK";
		ghszc(request, cds, hm);
		if ("success".equals(hm.get("Flag")) && "OK".equals(hm.get("regist"))) {
			hm.put("registStatus", registStatus);
		} else {
		}
		hm.put("ZCXX01", hm.get("gsid").toString());
		return hm;
	}

	// 经销商注册
	@SuppressWarnings("unchecked")
	public Map<String, Object> ghszc(HttpServletRequest request, IDataSet ids,
			Map<String, Object> hm) throws Exception {
		// 上传附件
		MultipartHttpServletRequest mrRequest = (MultipartHttpServletRequest) request;
		List<MultipartFile> listFile = mrRequest.getFiles("files");
		hm = (Map<String, Object>) uploadFile(request, hm, listFile);
		// 修改注册公司基本信息
		int i = updateZCGS(hm);
		if (hm.get("zclx").equals("24")) {// 最终用户
			int j = updateZCXX(hm);
		}
		// 插入送货地址
		int k = insertShdz(hm);
		if (i != 0 && k != 0) {
			hm.put("regist", "OK");
		}
		return hm;
	}

	public Map<String, Object> uploadFile(HttpServletRequest request,
			Map<String, Object> hm, List<MultipartFile> listFile)
			throws Exception {
		DiskFileItemFactory factory = new DiskFileItemFactory();
		ServletFileUpload upload = new ServletFileUpload(factory);
		if (!upload.isMultipartContent(request)) {
			hm.put("Flag", "error");
			return hm;
		}
		for (int i = 0; i < listFile.size(); i++) {
			MultipartFile file = listFile.get(i);
			try {
				InputStream in = file.getInputStream();
				String[] pathFull = request.getClass().getClassLoader()
						.getResource("").getPath().split("/WEB-INF/");
				String path1 = pathFull[0] + "/"
						+ JlAppResources.getProperty("UploadImagesFilePath");
				String path = path1 + hm.get("gsid");
				String oldFileName = file.getOriginalFilename();
				if (!"".equals(oldFileName) && oldFileName != null) {
					String fileSuffix = oldFileName.substring(oldFileName
							.lastIndexOf("."));// 截取文件后缀
					System.out.println(fileSuffix);
					String newFileName = System.currentTimeMillis()
							+ (int) (Math.random() * 10) + fileSuffix;
					System.out.println(newFileName + "+++++++" + path);
					File filePath = new File(path);
					if (!filePath.exists()) {
						filePath.mkdirs();
						System.out.println("创建目录为：" + filePath);
					}
					// file.transferTo(filePath);
					FileOutputStream out = new FileOutputStream(filePath + "\\"
							+ oldFileName);
					System.out.println(filePath + "\\" + newFileName);
					byte buffer[] = new byte[1024];
					int len = 0;
					while ((len = in.read(buffer)) > 0) {
						out.write(buffer, 0, len);
					}
					in.close();
					out.close();
					String filepath = filePath + "\\" + oldFileName;
					hm.put("filepath", filepath);
					// int n=saveFile(hm, oldFileName, filepath,i);
					int n = 1;
					if (n <= 0) {
						hm.put("Flag", "error");
					} else {
						hm.put("Flag", "success");
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
				hm.put("Flag", "error");
				throw e;
			} catch (Exception e) {
				e.printStackTrace();
				hm.put("Flag", "error");
				throw e;
			}
		}
		return hm;
	}

	/**
	 * 添加附件信息
	 * 
	 * @return
	 * @throws Exception
	 */
	public int saveFile(Map<String, Object> hm, String fileName, String path,
			int i) throws Exception {
		System.out.println("file04=" + hm.get("file0" + (i + 1)));
		// 首先删除原来的数据
		cds = new DataSet("[{}]");
		String deleteSQL = "DELETE FROM W_FILE WHERE FILE01 = '"
				+ hm.get("gsid") + "' AND FILE04 = "
				+ hm.get("file0" + (i + 1)) + "";
		Map row0 = (Map) getRow(deleteSQL, null, 0);
		int n0 = execSQL(o2o, deleteSQL, row0);
		String sql = "insert into w_file(file01,file02,file03,file04) values('"
				+ hm.get("gsid") + "','" + fileName + "','" + path + "',"
				+ hm.get("file0" + (i + 1)) + ")";
		Map row = (Map) getRow(sql, null, 0);
		int n = execSQL(o2o, sql, row);
		return n;
	}

	// 修改注册公司基本信息
	public int updateZCGS(Map<String, Object> hm) throws Exception {
		String sql = "UPDATE W_ZCGS SET ZCXX03='" + hm.get("ZCXX03") + "',"
				+ "ZCXX06='" + hm.get("ZCXX06") + "'," + "ZCXX02='"
				+ hm.get("ZCXX02") + "'," + "ZCXX07='" + hm.get("ZCXX07")
				+ "'," + "HMD='" + hm.get("HMD") + "'," + "ZCXX08='"
				+ hm.get("ZCXX08") + "'," + "ZCXX04='" + hm.get("ZCXX04")
				+ "'," + "ZCXX05='" + hm.get("ZCXX05") + "'," + "ZCGS01="
				+ hm.get("ZCGS01") + "," + "ZCXX14=STR_TO_DATE('"
				+ JLTools.getCurrentDate1() + "','%Y-%m-%d %k:%i:%s')"
				+ " WHERE XTCZY01='" + hm.get("XTCZY01") + "'";
		System.out.println("sql=" + sql);
		Map row = (Map) getRow(sql, null, 0);
		int i = execSQL(o2o, sql, row);
		return i;
	}

	public void insertZCGS(String gslx, String zcxx01, String phone,
			String email, String xtczy01, String password, String sjly, String ZCGS01)
			throws Exception {
		try {
			String insertSQL = "INSERT INTO W_ZCGS (ZCGS03,ZCXX01,ZCXX26,ZCXX06,ZCXX09,XTCZY01,XTCZY02,ZCXX56,ZCGS01,ZCXX14) "
					+ "VALUES("
					+ gslx
					+ ",'"
					+ zcxx01
					+ "','"
					+ phone
					+ "','"
					+ phone
					+ "','"
					+ email
					+ "','"
					+ xtczy01
					+ "','"
					+ password
					+ "','"
					+ sjly
					+ "','"
					+ ZCGS01
					+ "', now())";
			Map row = getRow(insertSQL, null, 0);
			execSQL(o2o, insertSQL, row);
		} catch (Exception e) {
			e.printStackTrace();
			throw new Exception("插入注册公司表出现异常！");
		}
	}

	public void insertZCXX(String ZCXX01, String XTCZY01) throws Exception {
		try {
			String insertSQL = "INSERT INTO W_ZCXX(ZCXX01, ZCXX02, ZCXX03, ZCXX04, ZCXX05, ZCXX06, ZCXX07, ZCXX08, ZCXX09, ZCXX10, ZCXX14,XTCZY01,ZCXX56)\n"
					+ "SELECT ZCXX01,ZCXX02,ZCXX03,ZCXX04,ZCXX05,ZCXX06,ZCXX07,ZCXX08,ZCXX09,ZCXX10,ZCXX14,'"
					+ XTCZY01
					+ "',ZCXX56 FROM W_ZCGS B  WHERE B.ZCXX01 = '"
					+ ZCXX01 + "';";
			Map row = getRow(insertSQL, null, 0);
			execSQL(o2o, insertSQL, row);
		} catch (Exception e) {
			e.printStackTrace();
			throw new Exception("插入注册信息表出现异常！");
		}
	}

	public int updateZCXX(Map<String, Object> hm) throws Exception {
		String sql = "UPDATE W_ZCXX SET ZCXX04='" + hm.get("ZCXX04") + "',"
				+ "ZCXX05='" + hm.get("ZCXX05") + "'," + "ZCXX02='"
				+ hm.get("ZCXX02") + "'," + "ZCXX06='" + hm.get("ZCXX06")
				+ "'," + "HMD='" + hm.get("HMD") + "'," + "ZCXX08='"
				+ hm.get("ZCXX08") + "'," + "ZCXX09='" + hm.get("ZCXX09")
				+ "'," + "ZCXX14=STR_TO_DATE('" + JLTools.getCurrentDate1()
				+ "','%Y-%m-%d %k:%i:%s')" + " WHERE XTCZY01='"
				+ hm.get("XTCZY01") + "'";
		System.out.println("sql=" + sql);
		Map row = (Map) getRow(sql, null, 0);
		int i = execSQL(o2o, sql, row);
		return i;
	}

	public void insertXtczy(String ZCXX01, String XTCZY01, String PASSWORD)
			throws Exception {
		try {
			String insertSQL = "INSERT INTO W_XTCZY(PERSON_ID, PASSWD, TYBJ,GSID) VALUES('"
					+ XTCZY01 + "','" + PASSWORD + "',0,'" + ZCXX01 + "')";
			Map row = getRow(insertSQL, null, 0);
			execSQL(o2o, insertSQL, row);
		} catch (Exception e) {
			e.printStackTrace();
			throw new Exception("插入系统操作员出现异常！");
		}
	}

	public void insertGslx(String ZCXX01, String GSLX) throws Exception {
		try {
			String querySQL = "SELECT COUNT(0) FROM W_GSLX WHERE GSID = '"
					+ ZCXX01 + "' AND LX = '" + GSLX + "'";
			int count = queryForInt(o2o, querySQL);
			if (count == 0) {
				String insertSQL = "INSERT INTO W_GSLX(GSID,LX,TYBJ) VALUES('"
						+ ZCXX01 + "','" + GSLX + "',0)";
				Map row = getRow(insertSQL, null, 0);
				execSQL(o2o, insertSQL, row);
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw new Exception("插入公司类型出现异常，检查系统中是否存在该公司类型或其他问题！");
		}
	}

	public void insertCzMenu(String XTCZY01) throws Exception {
		try {
			String insertXtczycz = "INSERT INTO W_XTCZYCZ\n"
					+ "  (PERSON_ID, CD)\n" + "  SELECT '" + XTCZY01
					+ "', W.CD FROM W_XTCZYCZ W WHERE PERSON_ID = 'mz123456'";
			Map row1 = getRow(insertXtczycz, null, 0);
			execSQL(o2o, insertXtczycz, row1);
		} catch (Exception e) {
			e.printStackTrace();
			throw new Exception("插入操作菜单异常！");
		}
	}

	/**
	 *@todo 插入默认收获地址
	 * @param REGZT
	 * @param ZCXX01
	 * @throws Exception
	 */
	public int insertShdz(Map<String, Object> hm) throws Exception {
		int i;
		try {
			// 获取地区编码
			int DZBH = JLTools.getJLBH(o2o, "W_SHDZWH", 1);
			String sql = "INSERT INTO W_SHDZWH\n"
					+ "  (DZBH, WLDW01, SHRMC, PROVINCE, CITY, COUNTY, OTHERDZ, SJHM,YZBM, EMAIL)\n"
					+ "VALUES\n" + "  (" + DZBH + ", '" + hm.get("WLDW01")
					+ "','" + hm.get("ZCXX03") + "', '" + hm.get("province")
					+ "', '" + hm.get("city") + "', '"  + hm.get("country")
					+ "', '" + hm.get("otherdz") + "', '" + hm.get("ZCXX06")
					+ "', '" + hm.get("ZCXX07") + "','" + hm.get("email")
					+ "')";
			i = execSQL(o2o, sql, 0);
		} catch (Exception e) {
			e.printStackTrace();
			throw new Exception("插入收货地址出现异常！");
		}
		return i;
	}

	/**
	 * 汽服云插入业务单据信息
	 */
	public void insertYwdjxx_QFY(String WLDW01, String HBID) throws Exception {
		try {
			String querySQL = "SELECT COUNT(1) FROM W_YWDJXX WHERE HBID = '"
					+ HBID + "'";
			int count = queryForInt(o2o, querySQL);
			// 如果数据中不存在则进行插入
			if (count == 0) {
				String insertSQL = "INSERT INTO W_YWDJXX(ZTID, HBID, WLDW01) SELECT A.ZCXX01, '"
						+ HBID
						+ "', '"
						+ WLDW01
						+ "' FROM W_ZCXX A, W_GSLX B  WHERE A.ZCXX01 = B.GSID AND B.LX = '42'";
				Map row = getRow(insertSQL, null, 0);
				execSQL(o2o, insertSQL, row);
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw new Exception("插入业务单据信息出现异常！");
		}
	}

	/**
	 * @todo 兴隆注册发送短信
	 * @param XmlData
	 * @return
	 * @throws Exception
	 */
	@RequestMapping("/insertSms.action")
	public Map<String, Object> insertSms(HttpServletRequest request,
			HttpServletResponse response, String XmlData) throws Exception {
		XmlData = request.getParameter("XmlData");
		Map map=JSONObject.fromObject(XmlData);
		Map hm=new HashMap();
		System.out.println(XmlData);
		boolean bool = xlInterFace.sendSmsInvoke(map); 
		hm.put("flag", bool);
		return hm;
	}
	
	/**
	 *  手机APP获取验证码
	 * @param response
	 * @param XmlData
	 * @return
	 * @throws Exception
	 */
	@RequestMapping("/insertAppRegisterCode.action")
	public Map<String, Object> insertAppRegSms(HttpServletRequest request,
			HttpServletResponse response, String XmlData) throws Exception {
		XmlData= JLTools.unescape(XmlData);
		StringBuilder smsYzm = new StringBuilder();
		for (int i = 0; i < 4; i++) {
			smsYzm.append(new Double(Math.random()*10).intValue());
		}
		String message = "您好，验证码为"+smsYzm.toString()+"（客服绝不会索要此验证码，切勿告知他人），请在页面中输入以完成验证,有效时间2分钟。";
		cds = new DataSet(XmlData);
		int DJLX = Integer.parseInt(cds.getField("DJLX", 0));
		String SJHM = cds.getField("SJHM", 0);
		Map map=JSONObject.fromObject(XmlData);
		Map hm=new HashMap();
		System.out.println(XmlData);
		System.out.println("YZM:" + smsYzm.toString());
		boolean bool = smsService.sendSms(DJLX, "", SJHM, message, "");
		hm.put("flag", bool);
		hm.put("YZM", smsYzm.toString());
		return hm;
	}
	
	/**
	 * 
	 * @param response
	 * @param XmlData
	 * @return
	 * @throws Exception
	 */
	@RequestMapping("/insertRegisterCode.action")
	public Map<String, Object> insertRegSms(HttpServletRequest request,
			HttpServletResponse response, String XmlData) throws Exception {
		XmlData= JLTools.unescape(XmlData);
		cds = new DataSet(XmlData);
		int DJLX = Integer.parseInt(cds.getField("DJLX", 0));
		String SJHM = cds.getField("SJHM", 0);
		String FSNR = cds.getField("FSNR", 0);
		Map map=JSONObject.fromObject(XmlData);
		Map hm=new HashMap();
		System.out.println(XmlData);
		boolean bool = smsService.sendSms(DJLX, "", SJHM, FSNR, "");
		hm.put("flag", bool);
		return hm;
	}
	
	/**
	 * @todo 验证手机号是否注册
	 * @param XmlData
	 * @return
	 * @throws Exception
	 */
	@RequestMapping("/selectSms.action")
	public Map<String, Object> selectSms(HttpServletRequest request,
			HttpServletResponse response, String XmlData) throws Exception {
		XmlData = request.getParameter("XmlData");
		Map map=JSONObject.fromObject(XmlData);
		Map hm=new HashMap();
		String sql="select count(1) from W_ZCXX where zcxx06='"+map.get("SJHM")+"'";
		Integer count = queryForInt(o2o, sql);
		System.out.println(XmlData);
		if(count>0){
			hm.put("flag", "1003");
			return hm;
		}else{
			hm.put("flag", "1000");
		}
		return hm;
	}
	
	
	
	/**
	 * @todo 更新发票信息 add 2015.10.15.
	 * @param XmlData
	 * @return
	 * @throws Exception
	 */
	@RequestMapping("/updateUserInfo.action")
	public Map<String, String> updateUserInfo(String XmlData,HttpServletRequest request) throws Exception {
		Map<String, String> ResultMap = new HashMap<String, String>();
		try {

			XmlData= JLTools.unescape(XmlData);
			cds = new DataSet(XmlData);
			String zcxx01 = cds.getField("ZCXX01", 0);
			String sjly = cds.getField("SJLY", 0);
			
			if(zcxx01 == null){
				return null;
			}
			
			Map map=JSONObject.fromObject(XmlData);
			//更新w_zcxx表中的的发票注册地址和发票注册电话
			String updateW_ZCXXSql = "update w_zcxx set ZCXX71=ZCXX71?, ZCXX72=ZCXX72?, ZCXX28=ZCXX28? where zcxx01=ZCXX01?";
			Map updateW_ZCXXMap = getRow(updateW_ZCXXSql, null, 0);
			execSQL(o2o, updateW_ZCXXSql, updateW_ZCXXMap);
			
			//根据参数中的公司编码及账号查询是否公司银行卡中存在记录
			String querySql = "select GSYHK11 from w_gsyhk where ZCXX01='"+ map.get("ZCXX01") +"' and GSYHK02='"+ map.get("GSYHK02") +"'";
			Map queryMap = queryForMap(o2o, querySql);
			
			//如果存在匹配的记录，进行状态判断
			if(queryMap != null && queryMap.size() > 0){
				Object state = queryMap.get("GSYHK11");
				if(state != null && Integer.parseInt(state.toString()) == 0){
					//将匹配的记录设置为启用
					String updateGsyhkSql = "update w_gsyhk set GSYHK11=1  where ZCXX01=ZCXX01? and GSYHK02=GSYHK02?";
					Map updateMap = getRow(updateGsyhkSql, null, 0);
					execSQL(o2o, updateGsyhkSql, updateMap);
				}
			}
			else{
				//将之前的记录设置为未启用
				String updateGsyhkSql = "update w_gsyhk set GSYHK11=0  where ZCXX01=ZCXX01?";
				Map updateMap = getRow(updateGsyhkSql, null, 0);
				execSQL(o2o, updateGsyhkSql, updateMap);
				
				//获取最大记录数
				int number = PubFun.updateWBHZT(o2o,"w_gsyhk",1);
				//如果不存在，添加相应的记录
				String insertGsyhkSql = "insert into w_gsyhk (GSYHK01, ZCXX01, GSYHK02, GSYHK03, GSYHK07, GSYHK11) values ("+ number +",ZCXX01?, GSYHK02?, GSYHK03?, GSYHK07?, 1)";
				Map insertMap = getRow(insertGsyhkSql, null, 0);
				execSQL(o2o, insertGsyhkSql, insertMap);
			}
			
			//当是PC注册时图片处理
			if(sjly.equals("0")){
				MultipartHttpServletRequest mrRequest=(MultipartHttpServletRequest)request;
				Iterator iterator = mrRequest.getFileNames();
				while(iterator.hasNext()){
					String fileType = (String)iterator.next();
					MultipartFile file = mrRequest.getFile(fileType);
					saveInvoicemages(file,fileType,XmlData);
				}
			}

			ResultMap.put("state", "success");
		} catch (Exception ex) {
			ResultMap.put("state", "error");
			throw ex;
		}
		return ResultMap;
	}
	
	/**
	 * 保存申请开发票时上传的图片 add 2015.10.15.
	 * @param file
	 * @param fileType
	 * @param XmlData
	 * @throws Exception
	 */
	public void saveInvoicemages(MultipartFile file,String fileType,String XmlData) throws Exception{
		try{
			cds = new DataSet(XmlData);
			//获取图片名称和图片类型
			String fileName = fileType;
			String tplx = fileType.split("_")[1];
			String fileCode = fileType.split("_")[0]+"_code";
			fileName = fileName + "." + file.getOriginalFilename().split("\\.")[1];
			//上传图片到服务器
			Map map = new HashMap();
			map.put("imgPath", JlAppResources.getProperty("path_dptp")+cds.getField("ZCXX01", 0));
			map.put("imgName", fileName);
			JLTools.fileUploadNew(file,map);
			//写入W_DPTP
			String zjhm = (cds.getField(fileCode, 0)==null)?"":cds.getField(fileCode, 0);
			Map dptpMap = new HashMap();
			dptpMap.put("ZCXX01", cds.getField("ZCXX01", 0));
			dptpMap.put("TPLX", tplx);
			dptpMap.put("TPNAME", fileName);
			dptpMap.put("ZJHM", zjhm);
			insertW_DPTP(dptpMap);
		}catch(Exception ex){
			throw ex;
		}
	}
	
	
	/**
	 * 查询企业名称是否存在
	 */
	@RequestMapping("/selectAddress.action")
	public Map<String, Object> selectAddress(HttpServletRequest request,
			HttpServletResponse response, String XmlData) throws Exception {
		XmlData = request.getParameter("address");
		Map hm=new HashMap();
		String sql="select count(1) from W_ZCXX where zcxx02='"+XmlData+"'";
		Integer count = queryForInt(o2o, sql);
		System.out.println(XmlData);
		if(count>0){
			hm.put("state", "failed");
			return hm;
		}else{
			hm.put("state", "success");
		}
		return hm;
	}
	
	
	/**
	 * @todo 验证邮箱是否注册
	 * @param XmlData
	 * @return
	 * @throws Exception
	 */
	@RequestMapping("/selectEmail.action")
	public Map<String, Object> selectEmail(HttpServletRequest request,
			HttpServletResponse response, String XmlData) throws Exception {
		XmlData = request.getParameter("email");
		Map hm=new HashMap();
		String sql="select count(1) from W_ZCXX where zcxx09='"+XmlData+"'";
		Integer count = queryForInt(o2o, sql);
		System.out.println(XmlData);
		if(count>0){
			hm.put("state", "failed");
			return hm;
		}else{
			hm.put("state", "success");
		}
		return hm;
	}
	
	/**
	 * @todo 验证厂商识别码
	 * @param XmlData
	 * @return
	 * @throws Exception
	 */
	@RequestMapping("/selectSMB.action")
	public Map<String, Object> selectSMB(HttpServletRequest request,
			HttpServletResponse response, String XmlData) throws Exception {
		XmlData = request.getParameter("sbm");
		Map hm=new HashMap();
		String sql="SELECT COUNT(1) FROM w_zcgssbm WHERE SBM ='"+XmlData+"'";
		Integer count = queryForInt(o2o, sql);
		System.out.println(XmlData);
		if(count>0){
			hm.put("state", "failed");
			return hm;
		}else{
			hm.put("state", "success");
		}
		return hm;
	}
	
}
