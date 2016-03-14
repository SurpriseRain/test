/**********************************************************************
 * @file	CartManage.java
 * @breif	未经授权不得使用或修改该文档
 * @author	Design:	曾海峰/20140312
 * @author	Code:	曾海峰/20140312
 * @author	Modify:	曾海峰/20140312
 * @copy	Tag:	金力软件
 **********************************************************************/

package com.jlsoft.o2o.user.service;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.ibatis.session.SqlSession;
import org.apache.log4j.Logger;
import org.springframework.context.annotation.Scope;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import com.jlsoft.framework.JLBill;
import com.jlsoft.framework.dataset.DataSet;
import com.jlsoft.utils.ClientDataSet;
import com.jlsoft.utils.JLTools;
import com.jlsoft.utils.JlAppResources;
/**
 * 
 * @breif 用户公共信息查询接口方法
 *
 */
@Controller
@RequestMapping("/UserInfo")
public class UserInfo extends JLBill{
	private static final Logger logger = Logger.getLogger(UserInfo.class); 
	private SqlSession session=null;
	public UserInfo() {

	}
	
	public UserInfo(JdbcTemplate o2o,JdbcTemplate scm) {
		this.o2o = o2o;
		this.scm = scm;
	}
	/**
	 * 查询用户在ERP系统中的GSXX
	 * @param	String ZCXX01
	 * 	- String	ZCXX01	B2B公司编码
	 * 
	 * @return	String
	 *  - GSXX01	所对ERP中GSXX
	 *  
	 * @note 根据O2O编码获得公司类型下的ERP编码
	 */
	@SuppressWarnings("unchecked")
	public String selectUserErpGsxx(String ZCXX01) throws Exception {
		String gsxx01 = "";
		String querySQL = "SELECT ifnull(ZCXX25,1) GSXX01 FROM W_ZCXX WHERE ZCXX01 ='"+ZCXX01+"'";
		Map map = queryForMap(o2o, querySQL);
		if(map != null && map.size() > 0){
			gsxx01 = map.get("GSXX01").toString();
		}
		return gsxx01;
	}
	
	/**
	 * 查询用户在ERP系统中的WLDW
	 * @param
	 * 	- String	ZTID	O2O主体公司编码
	 *  - String	HBID	O2O伙伴公司编码
	 * 
	 * @return	Map
	 *  - WLDW01	所对ERP中WLDW
	 *  
	 * @note 根据公司编码和伙伴编码找到ERP中对应的WLDW编码
	 */
	@SuppressWarnings("unchecked")
	public String selectUserErpWldw(String ZTID,String HBID) throws Exception {
		String WLDW01 = "";
		String querySQL = "SELECT WLDW01 FROM W_YWDJXX WHERE ZTID = '"+ZTID+"' AND HBID = '"+HBID+"'";
		Map map = (Map) queryForMap(o2o, querySQL);
		if(map != null && map.size() > 0){
			WLDW01 = map.get("WLDW01").toString();
		}
		return WLDW01;
	}
	
	/**
	 * 查询用户在ERP系统中的地区信息
	 * @param
	 * 	- String	WLDW01	ERP往来单位编码
	 * 
	 * @return	Map
	 *  - DQXX01	地区编码
	 *  
	 */
	@SuppressWarnings("unchecked")
	public String selectUserErpDqxx(String WLDW01) throws Exception {
		String DQXX01 = "";
		String querySQL = "	SELECT NVL(DQXX01, '"+JlAppResources.getProperty("DEFAULT_DQCODE")+"') DQXX01 FROM WLDW WHERE WLDW01 = '"+WLDW01+"'";
		Map map = (Map) queryForMap(scm, querySQL);
		if(map != null && map.size() > 0){
			DQXX01 = map.get("DQXX01").toString();
		}
		return DQXX01;
	}
	
	/**
	 * 查询用户在ERP系统中的所对公司的人员信息
	 * @param
	 * 	- String	WLDW01	ERP往来单位编码
	 *  - String	GSXX01	公司编码
	 *  - String	DQXX01	地区编码
	 * @return	Map
	 *  - RYXX01	人员编码
	 *  - RYXX02	人员名称
	 */
	@SuppressWarnings("unchecked")
	public Map selectUserErpRyxx(String WLDW01,String GSXX01,String DQXX01) throws Exception {
		Map map = new HashMap();
		//首先取客商业务员
		String querySQL = "SELECT RYXX01, RYXX02 FROM DWYWY WHERE WLDW01 = '"+WLDW01+"' AND GSXX01 = '"+GSXX01+"' AND RYXX01 IS NOT NULL";
		Map ksywyMap = queryForMap(scm, querySQL);
		if(ksywyMap != null && ksywyMap.size() > 0){
			map = ksywyMap;
		}else{
			//取单位业务员
			querySQL = "SELECT RYXX01, RYXX02 FROM DQXX  WHERE DQXX01 LIKE '%'||"+DQXX01+"||'%' AND RYXX01 IS NOT NULL  ORDER BY DQXX01 DESC";
			Map dqywyMap =   queryForMap(scm, querySQL);
			if(dqywyMap != null && dqywyMap.size() > 0){
				map = dqywyMap;
    		}else{
    			map.put("ryxx01", GSXX01+"9999");
    			map.put("ryxx02", "system");
    		}
		}
		return map;
	}
	
	
	/**
	 * 根据往来单位、公司信息编码获得用户在ERP中的单位余额账
	 * @param	String XmlData
	 * 	- String	GSXX01	ERP公司编码
	 *  - String	WLDW01	ERP往来单位
	 * 
	 * @return	Map
	 *  - DWYEZ04	可用余额
	 *  - DWYEZ05	授信额度
	 *  
	 * @note 根据公司编码和伙伴编码找到ERP中对应的WLDW编码
	 */
	@SuppressWarnings({ "unchecked", "null" })
	@RequestMapping("/selectUserDwyez.action")
	public Map selectUserDwyez(String XmlData) throws Exception {
		cds = new DataSet(XmlData);
		//首先获得ERP中的公司编码
		String GSXX01 = selectUserErpGsxx(cds.getField("ZTID", 0).toString());
		//获得往来单位编码
		String WLDW01 = selectUserErpWldw(cds.getField("ZTID", 0).toString(),cds.getField("HBID", 0).toString());
		String querySQL = "SELECT (IFNULL(SUM(DWYE04 * -1), 0) - IFNULL((SELECT E.DJJE FROM W_DJJE E WHERE E.WLDW01 = '"+WLDW01+"' AND E.GSXX01 = '"+GSXX01+"'), 0)) DWYEZ04," +
				"				   IFNULL(SUM(DWYE05), 0) DWYEZ05 \n" +
				"			 FROM  DWYEZ \n" +
				"			WHERE  WLDW01 = '"+WLDW01+"' \n" +
				"			  AND  GSXX01 = '"+GSXX01+"' \n" +
				"		 GROUP BY  GSXX01, WLDW01";
		Map map = null;
		if (JlAppResources.getProperty("ROADMAP").equals("2")){
		   map = (Map) queryForMap(scm, querySQL);
		} else {
		   map = (Map) queryForMap(o2o, querySQL);
		}
		if(map == null){
			map =new HashMap<String, Object>();
			map.put("DWYEZ04", 0);
			map.put("DWYEZ05", 0);
		}
		return map;
	}
	
	/**
	 * 根据客户编码查询会员总积分
	 * @param	String XmlData
	 * 	- String	ZCXX01	公司编码
	 * 
	 * @return	Map
	 *  - JFNUM		积分数量
	 *  
	 * @note 根据客户编码查询会员积分总积分数量
	 */
	@SuppressWarnings({ "unchecked"})
	@RequestMapping("/selectUserHyjf.action")
	public Map selectUserHyjf(String XmlData) throws Exception {
		cds = new DataSet(XmlData);
		String querySQL = "SELECT IFNULL(SUM(JFNUM),0) JFNUM FROM W_HYJF WHERE WLDW01 = '"+cds.getField("ZCXX01", 0)+"'";
		Map map = (Map) queryForMap(o2o, querySQL);
		return map;
	}
	
	/**
	 * 插入公司关系
	 */
	public void insertGsgx(String XmlData)  throws Exception{
		cds = new DataSet(XmlData);
		try{
			String querySQL = "SELECT COUNT(0) FROM W_GSGX WHERE ZTID = '"+cds.getField("ZTID", 0)+"' AND HBID = '"+cds.getField("HBID", 0)+"'";
			int count = queryForInt(o2o, querySQL);
			if(count == 0){
				String insertSQL = "INSERT INTO W_GSGX(ZTID,HBID,LX,TYBJ,SYBJ,HYBJ) VALUES(ZTID?,HBID?,21,0,1,HYBJ?)";
				Map row = getRow(insertSQL, null, 0);
				execSQL(o2o, insertSQL, row);
			}
		}catch(Exception e){
			e.printStackTrace();
			throw new Exception("插入注册信息表出现异常！");
		}
	}
	
	/**
	 * 更新会员标记
	 * @param	String 
	 * 	- String	ZCXX01	公司编码
	 * 
	 * @note 根据主体编码更新会员标记公用方法
	 */
	@SuppressWarnings({ "unchecked"})
	public void updateHybj(String ZCXX01) throws Exception {
		try{
			//查询附件是否已经上传
			String fileQuerySQL ="SELECT COUNT(0) FROM W_FILE F  WHERE (F.FILE04 = 11 OR F.FILE04 = 13) AND F.FILE01 = '"+ZCXX01+"'";
			int isUploadFile = queryForInt(o2o, fileQuerySQL);
			String updateHybjSQL = "";
			if(isUploadFile >= 2){
				//判断是否签约
				String isQySQL = 
					"SELECT COUNT(0)\n" +
					"  FROM (SELECT A.HBID HBID\n" + 
					"          FROM W_QYXX A\n" + 
					"         WHERE A.ZTID = '1122'\n" + 
					"           AND A.HBID = '"+ZCXX01+"'\n" + 
					"        UNION\n" + 
					"        SELECT Z.ZCXX01 HBID\n" + 
					"          FROM W_ZCXX Z\n" + 
					"         WHERE Z.ZCXX12 IS NOT NULL\n" + 
					"           AND Z.ZCXX01 = '"+ZCXX01+"')";
				int isQY = queryForInt(o2o, isQySQL);
				if(isQY > 0){
					//判断是否已经赠送过会员积分
					String isZsjfSQL = "SELECT COUNT(0) FROM W_HYJFITEM A WHERE A.WLDW01 = '"+ZCXX01+"' AND A.BGBJ = 1";
					int iszsjf = queryForInt(o2o, isZsjfSQL);
					if(iszsjf == 0){
						//赠送积分
						Map jfmap = new HashMap();
						jfmap.put("ZCXX01", ZCXX01);
						jfmap.put("JFNUM", 200);
						jfmap.put("BGBJ", 1);
						jfmap.put("WBDJBH", "");
						JSONArray jfJson = JSONArray.fromObject(jfmap);
						updateHyjf(jfJson.toString());
					}
					updateHybjSQL = "UPDATE W_ZCXX SET ZCXX16 = 1 ,ZCXX17 = IFNULL((SELECT W_ZCXX.ZCXX17 FROM W_ZCXX WHERE W_ZCXX.ZCXX01 = '"+ZCXX01+"'),SYSDATE) WHERE ZCXX01 = '"+ZCXX01+"'";
				}else{
					updateHybjSQL = "UPDATE W_ZCXX SET ZCXX16 = 0 ,ZCXX17 = IFNULL((SELECT W_ZCXX.ZCXX17 FROM W_ZCXX WHERE W_ZCXX.ZCXX01 = '"+ZCXX01+"'),SYSDATE) WHERE ZCXX01 = '"+ZCXX01+"'";
				}
			}else{
				updateHybjSQL = "UPDATE W_ZCXX SET ZCXX16 = 0 ,ZCXX17 = IFNULL((SELECT W_ZCXX.ZCXX17 FROM W_ZCXX WHERE W_ZCXX.ZCXX01 = '"+ZCXX01+"'),SYSDATE) WHERE ZCXX01 ='"+ZCXX01+"'";
			}
			//获取行值
			Map row = getRow(updateHybjSQL, null, 0);
			execSQL(o2o, updateHybjSQL, row);
		}catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}
	
	
	/**
	 * 插入会员积分
	 * @param	String XmlData
	 * 	- String	ZCXX01	公司编码
	 *  - INT		JFNUM   积分数量
	 *  - INT		BGBJ	变更原因标记
	 *  - String	WBDJBH	关联外部单据编号
	 * 
	 * @return	
	 * @throws Exception 
	 *  
	 * @note 根据客户编码查询会员积分总积分数量
	 */
	@SuppressWarnings({ "unchecked"})
	@RequestMapping("/updateHyjf.action")
	public void updateHyjf(String XmlData) throws Exception {
		try {
			cds = new DataSet(XmlData);
			//首先判断是否已经存在会员积分主表数据
			String isExitSQL = "SELECT COUNT(0) JFNUM FROM W_HYJF WHERE WLDW01 = '"+cds.getField("ZCXX01", 0)+"'";
			int isExit = queryForInt(o2o, isExitSQL);
			String hyjfSQL = "";
			if(isExit == 0){
				hyjfSQL = "INSERT INTO W_HYJF (WLDW01, JFNUM) VALUES ('"+cds.getField("ZCXX01", 0)+"', '"+Double.valueOf(cds.getField("JFNUM", 0))+"')";
			}else{
				hyjfSQL = "UPDATE W_HYJF A SET A.JFNUM = A.JFNUM + '"+Double.valueOf(cds.getField("JFNUM", 0))+"' WHERE A.WLDW01 = '"+cds.getField("ZCXX01", 0)+"'";
			}
			//获取行值
			Map row = getRow(hyjfSQL, null, 0);
			execSQL(o2o, hyjfSQL, row);
			String hyjfItemSql="";
			if("".equals(cds.getField("WBDJBH", 0))){
				hyjfItemSql = "INSERT INTO W_HYJFITEM\n" +
                "  (WLDW01, BGBJ, BGNUM, BGDATE)" + 
             "VALUES" + 
                "  ('"+cds.getField("ZCXX01", 0)+"','"+cds.getField("BGBJ", 0)+"', '"+Double.valueOf(cds.getField("JFNUM", 0))+"', SYSDATE);";
			}else{
				hyjfItemSql = "INSERT INTO W_HYJFITEM\n" +
				"  (WLDW01, BGBJ, XSDD01, BGNUM, BGDATE)" + 
				"VALUES" + 
				"  ('"+cds.getField("ZCXX01", 0)+"','"+cds.getField("BGBJ", 0)+"','"+cds.getField("WBDJBH", 0)+"', '"+Double.valueOf(cds.getField("JFNUM", 0))+"', SYSDATE);";
			}
			Map rowItem = getRow(hyjfItemSql, null, 0);
			execSQL(o2o, hyjfItemSql, rowItem);
		} catch (Exception e) {
			e.printStackTrace();
			throw new Exception("更新会员积分数量失败！");
		}
	}
public  Map Shr(String XmlData){
	String querySQL = "SELECT DISTINCT A.WLDW01 FROM W_YWDJXX A WHERE A.HBID = '"+ XmlData +"'";
	Map WLDW01=queryForMap(o2o, querySQL);
	return WLDW01;
}
	/**
	 * 根据客户编码查询账户余额及总授信额度
	 * @param	String XmlData
	 * 	- String	GSID	公司编码
	 * 
	 * @return	Map
	 *  - YESUM		总账户余额
	 *  - EDSUM		总授信额度
	 *  - YELIST    商品列表
	 * 		- GSXX01	公司编码 
	 * 		- GSXX02	公司名称		
	 * 		- DWYE04	余额	
	 * 		- DJJE		冻结金额
	 * 		- DQKYYE	可用余额
	 *  
	 * @return	
	 * @throws Exception 
	 *  
	 * @note 根据客户编码查询会员积分总积分数量
	 */
	@SuppressWarnings({ "unchecked"})
	@RequestMapping("/selectZhye.action")
	public Map selectZhye(String XmlData) throws Exception {
		cds = new DataSet(XmlData);
		String querySQL="";
		String WLDW01="";
		Map mapstr=Shr(cds.getField("GSID", 0));
		if(mapstr!=null&&mapstr.get("WLDW01")!=null&&!mapstr.get("WLDW01").toString().equals("")){
		//String querySQL = "SELECT DISTINCT A.WLDW01 FROM W_YWDJXX A WHERE A.HBID = '"+ cds.getField("GSID", 0) +"'";
		
		//String WLDW01=queryForMap(o2o, querySQL).get("WLDW01").toString();
				
		querySQL = "SELECT Y.GSXX01 GSXX01," +
							"(SELECT G.GSXX02 FROM GSXX G WHERE G.GSXX01 = Y.GSXX01) GSXX02," +
							"(NVL(SUM(DWYE04 * -1), 0)) DWYE04," +
							"NVL(D.DJJE, 0) DJJE," +
							"(SUM(DWYE04 * -1) - NVL(D.DJJE, 0)) DQKYYE," +
							"NVL(SUM(DWYE05), 0) SXED " +
							"FROM DWYEZ Y, W_DJJE D " +
							"WHERE Y.WLDW01 = '"+ WLDW01 +"' "+
							"AND Y.WLDW01 = D.WLDW01(+) " +
							"AND Y.GSXX01 = D.GSXX01(+)" ;
		
		if (JlAppResources.getProperty("ROADMAP").equals("2")){
			querySQL=querySQL+	"AND Y.Gsxx01 in ('0602','0618') ";
		}
		
		querySQL=querySQL+					"GROUP BY Y.GSXX01, Y.WLDW01, D.DJJE";
		
		List<Map> YELIST = queryForList(scm, querySQL);
		
		double YESUM = 0;
		double EDSUM = 0;
		
		if(YELIST!=null&&YELIST.size()!=0){
			for(int i=0;i<YELIST.size();i++){
				double DQKYYE=Double.parseDouble(YELIST.get(i).get("DQKYYE").toString());
				double SXED=Double.parseDouble(YELIST.get(i).get("SXED").toString());
				
				YESUM = YESUM + DQKYYE;
				EDSUM = EDSUM + SXED;
			}
		}
		
		Map map = new HashMap();
		map.put("YESUM", YESUM);
		map.put("EDSUM", EDSUM);
		
		//YELIST.add(0, map);
		
		map.put("YELIST", YELIST);
		
		return map;
		}
		else{
		return mapstr;
		}
	}

	/**
	 * 根据客户编码获得客户签约列表
	 * @param	String XmlData
	 * 	- String	HBID	客户编码
	 * 
	 * @return	Map
	 *  - QYXXLIST	签约信息集合
	 *  - ZTID		主体编码
	 *  - ZCXX02	主体名称
	 *  - QYXX01	持卡人姓名
	 *  - QYXX03	银行账号
	 *  - QYXX05	签约手机号
	 *  
	 * @note 根据客户编码查询会员积分总积分数量
	 */
	@SuppressWarnings({ "unchecked"})
	@RequestMapping("/selectUserQyxx.action")
	public Map selectUserQyxx(String XmlData) throws Exception {
		cds = new DataSet(XmlData);
		String querySQL = "SELECT A.ZTID,B.ZCXX02,A.QYXX01,A.QYXX03,A.QYXX05 FROM W_QYXX A ,W_ZCXX B WHERE A.ZTID = B.ZCXX01 AND A.HBID = '"+cds.getField("HBID", 0)+"'";
		List qyxxlist = (List) queryForList(o2o, querySQL);
		Map map = new HashMap();
		map.put("QYXXLIST", qyxxlist);
		return map;
	}
	
	/**
	 * 查询用户密码
	 * @param	String ZCXX01
	 * 	- String	W_XTCZY01	用户名
	 * 
	 * @return	String
	 *  - W_XTCZY02		密码
	 *  
	 * @note 
	 */
	@SuppressWarnings({ "unchecked"})
	@RequestMapping("/selectUserPassWord.action")
	public Map selectUserPassWord(String XmlData) throws Exception {
		cds = new DataSet(XmlData);
		String querySQL = "SELECT PASSWD XTCZY02 FROM W_XTCZY WHERE PERSON_ID='"+cds.getField("XTCZY01", 0)+"'";
		Map map = queryForMap(o2o, querySQL);
		return map;
	}
	
	
	/** 修改用户信息
	 * 
	 * @param XmlData
	 * @return
	 * @throws DataAccessException
	 * @throws Exception
	 */
	@RequestMapping("/updateUserInfo.action")
	public Map updateUserInfo(String XmlData,HttpServletRequest request) throws DataAccessException, Exception{
		Map resultMap = new HashMap();
		try{
			cds = new DataSet(XmlData);
			String sql = "UPDATE W_ZCXX A SET A.ZCXX06 = mobile?,A.ZCXX03 = frdb?,A.ZCXX04 = sex?,A.ZCXX09 = email?,A.ZCXX07 = county?, A.ZCXX10 = ZCXX10?,A.ZCXX32 = ZCXX32?,A.ZCXX33 = ZCXX33? WHERE A.ZCXX01 = ZCXX01?";
			Map row = getRow(sql, null, 0);
			execSQL(o2o, sql, row);
			// 上传附件
			Map zcxxMap = new HashMap();
			zcxxMap = (Map) JSONArray.fromObject(XmlData).get(0);
			zcxxMap.put("gsid", cds.getField("ZCXX01", 0));
			MultipartHttpServletRequest mrRequest=(MultipartHttpServletRequest)request;
			List<MultipartFile> listFile= mrRequest.getFiles("files");
			//暂时屏蔽zly
			//Regist regist = new Regist(o2o, scm);
			//zcxxMap=(Map<String, Object>) regist.uploadFile(request, zcxxMap,listFile);
			resultMap.put("STATE", 1);
		}catch(Exception e ){
			e.printStackTrace();
			resultMap.put("STATE", 0);
		}
		return resultMap;
	}
	
	
	/**
	 * 查询用户菜单
	 * @param	String ZCXX01
	 * 	- String	W_XTCZY01	用户名
	 * 
	 * @return	String
	 *  - W_XTCZY02		密码
	 *  
	 * @note 
	 */
	@SuppressWarnings({ "unchecked"})
	@RequestMapping("/selectUserMenu.action")
	public Map selectUserMenu(String XmlData) throws Exception {
		cds = new DataSet(XmlData);
		String querySQL = "SELECT RTRIM(M.CODE) CODE,\n" +
				"M.NAME,\n" +
				"M.URL,\n" +
				"LENGTH(RTRIM(CODE)) / 2 LEVELS,\n" +
				"MJBJ\n" +
				"FROM W_XTCZYMENU M\n" +
				"WHERE (select count(A.CODE) from W_XTCZYMENU A, W_XTCZYCZ B \n" +
				"where B.PERSON_ID = '"+cds.getField("PERSON_ID", 0)+"' and A.TYPE = 0 and A.CODE \n" +
				"like CONCAT(rtrim(B.CD),'%') and A.CODE like CONCAT(M.CODE,'%'))>0 \n" +
				"ORDER BY M.CODE ";
		Map map = new HashMap();
		List menu = queryForList(o2o, querySQL);
		map.put("menu", menu);
		return map;
	}
	
	/**
	 * 增值发票修改，也可以修改发票抬头(明珠用)
	 * @param	String ZCXX01
	 * 	- String	W_XTCZY01	用户名
	 * 
	 * @return	String
	 *  - W_XTCZY02		密码
	 *  
	 * @note 
	 */
	@SuppressWarnings({ "unchecked"})
	@RequestMapping("/modifyFpxx_mz.action")
	public Map modifyFpxx_mz(String XmlData) throws Exception {
		cds = new DataSet(XmlData);
		Map map=new HashMap();
		String sql = "UPDATE W_ZCXX SET ZCXX37=DWMC?,ZCXX28=NSRSBH?,ZCXX22=KHH?,ZCXX13=KHH?,ZCXX23=CWZH?,ZCXX29=FPLXDH?,ZCXX24=FPYJDZ?,ZCXX20=1 WHERE ZCXX01=ZCXX01?";
		
		Map row=getRow(sql, null, 0);
		int i=execSQL(o2o, sql, row);
		if(i>0){
			map.put("STATE", "1");
		}else{
			map.put("STATE", "0");
		}
		
		return map;
	}
	
	/**
	 * 查询用户菜单
	 * @param	String ZCXX01
	 * 	- String	W_XTCZY01	用户名
	 * 
	 * @return	String
	 *  - W_XTCZY02		密码
	 *  
	 * @note 
	 */
	@SuppressWarnings({ "unchecked"})
	@RequestMapping("/modifyFpxx.action")
	public Map modifyFpxx(String XmlData) throws Exception {
		cds = new DataSet(XmlData);
		Map map=new HashMap();
		String sql = "UPDATE W_ZCXX SET ZCXX28=NSRSBH?,ZCXX22=KHH?,ZCXX13=KHH?,ZCXX23=CWZH?,ZCXX29=FPLXDH?,ZCXX24=FPYJDZ?,ZCXX20=1 WHERE ZCXX01=ZCXX01?";
		
		Map row=getRow(sql, null, 0);
		int i=execSQL(o2o, sql, row);
		if(i>0){
			map.put("STATE", "1");
		}else{
			map.put("STATE", "0");
		}
		
		return map;
	}
	/**
	 * 修改发票抬头
	 * @param XmlData
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings({ "unchecked"})
	@RequestMapping("/modifyFpttxx.action")
	public Map modifyFpttxx(String XmlData) throws Exception {
		cds = new DataSet(XmlData);
		Map map=new HashMap();
		String sql = "UPDATE W_ZCXX SET ZCXX37=FPTT?,ZCXX24=YJDZ? WHERE ZCXX01=ZCXX01?";
		Map row=getRow(sql, null, 0);
		int i=execSQL(o2o, sql, row);
		if(i>0){
			map.put("STATE", "1");
		}else{
			map.put("STATE", "0");
		}
		return map;
	}
	/**
	 * 查询发票信息
	 * @param XmlData
	 * @param ZCXX01 --公司编码
	 * @return
	 * 		KHH --开户行
	 * 		ZWZH --账务账号
	 * 		FPDZ --发票地址
	 * 		FPDZXQ --发票地址详情
	 * 		FPDZBM --发票地址编码
	 * 		NSRSBH --纳税人识别号
	 * 		FPTT   --发票抬头
	 * 		FPBZ   --发票备注	
	 * @throws Exception	
	 */
	@RequestMapping("/selectFpxx.action")
	public Map selectFpxx(String XmlData) throws Exception{
		try {
				cds =new DataSet(XmlData);
			String sql=
				"SELECT A.FPXX01 KHH,\n" +
				"       A.FPXX02 ZWZH,\n" + 
				"       A.FPXX03 FPDZ,\n" + 
				"       A.FPXX04 FPDZXQ,\n" + 
				"       A.FPXX05 FPDZBM,\n" + 
				"       SUBSTR(A.FPXX05, 1, 4) CITY,\n" + 
				"       SUBSTR(A.FPXX05, 1, 2) PROVINCE,\n" + 
				"       A.FPXX06 FPLXDH,\n" + 
				"       A.FPXX07 NSRSBH,\n" + 
				"       A.FPXX08 FPTT,\n" + 
				"       A.FPXX09 FPBZ\n" + 
				"  FROM W_FPXX A\n" + 
				" WHERE A.ZCXX01 = '"+cds.getField("ZCXX01", 0)+"'";
			Map hm=queryForMap(o2o, sql);
			return hm;
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}
	@RequestMapping("/updateFpxx.action")
	public Map<String, Object> updateFpxx(String XmlData) throws Exception{
		Map<String, Object> returnMap=new HashMap<String, Object>();
		int i;
		try {
			cds=new DataSet(XmlData);
			Map hm=selectFpxx(XmlData);
			if(hm!=null){
			  i=updateFpxxInfo(XmlData);
			}else{
			  i=insertFpxx(XmlData);	
			}
		} catch (Exception e) {
			e.printStackTrace();
			returnMap.put("STATE", false);
			throw e;
		}
		if(i>0){
			returnMap.put("STATE", true);
		}else{
			returnMap.put("STATE", false);
		}
		return returnMap;
	}
	/**
	 * 
	 * @return
	 * @throws Exception 
	 */
	public int updateFpxxInfo(String XmlData) throws Exception{
		try {
			cds=new DataSet(XmlData); 
			String sql=
				"UPDATE W_FPXX A\n" +
				"   SET A.FPXX01 = khh?,\n" + 
				"       A.FPXX02 = cwzh?,\n" + 
				"       A.FPXX03 = fpdz?,\n" + 
				"       A.FPXX04 = fpdzxq?,\n" + 
				"       A.FPXX05 = fpdzbm?,\n" + 
				"       A.FPXX06 = fplxdh?,\n" + 
				"       A.FPXX07 = nsrsbh?,\n" + 
				"       A.FPXX08 = fptt?,\n" + 
				"       A.FPXX09 = fpbz?\n" + 
				" WHERE A.ZCXX01 = ZCXX01?";
			Map row =getRow(sql, null, 0);
			int i=execSQL(o2o, sql, row);
			return i;
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}
	public int insertFpxx(String XmlData) throws Exception{
		int i =0;
		try {
			cds =new DataSet(XmlData);
			String sql=
				"INSERT INTO W_FPXX\n" +
				"  (ZCXX01,\n" + 
				"   FPXX01,\n" + 
				"   FPXX02,\n" + 
				"   FPXX03,\n" + 
				"   FPXX04,\n" + 
				"   FPXX05,\n" + 
				"   FPXX06,\n" + 
				"   FPXX07,\n" + 
				"   FPXX08,\n" + 
				"   FPXX09)\n" + 
				"VALUES\n" + 
				"  (ZCXX01?,\n" + 
				"   khh?,\n" + 
				"   cwzh?,\n" + 
				"   fpdz?,\n" + 
				"   fpdzxq?,\n" + 
				"   fpdzbm?,\n" + 
				"   fplxdh?,\n" + 
				"   nsrsbh?,\n" + 
				"   fptt?,\n" + 
				"   fpbz?)";
			Map row =getRow(sql, null, 0);
				i=execSQL(o2o, sql, row);
		} catch (Exception e) {
			logger.error(e);
			throw e;
		}
		return i;
	}
	
	/**
	 * 找回密码提交申请
	 * @param XmlData
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings({ "unchecked"})
	@RequestMapping("/UpdateW_MMGL.action")
	public Map UpdateW_MMGL(String XmlData) throws Exception {
		cds = new DataSet(XmlData);
		String ZCXX01=cds.getField("ZCXX01", 0);
		Map map=new HashMap();
		String sqlgsid="SELECT ZCXX01 FROM W_ZCXX  WHERE ZCXX02='"+cds.getField("ZCXX02", 0)+"' AND ZCXX06='"+cds.getField("MMZH01", 0)+"'";
		Map map0=queryForMap(o2o, sqlgsid);
		if(map0==null){
			 map.put("STATE", "-1");//不存在该公司
			 return map;
		}
		ZCXX01=map0.get("ZCXX01").toString();
		
		String sql0="SELECT count(1) count FROM W_MMZH where ZCXX01='"+ZCXX01+"'";
		int i=queryForInt(o2o, sql0);
		String sql;
		String MMZH04="";
		if(i==0){
			sql="INSERT INTO W_MMZH(ZCXX01,ZCXX02,MMZH01,MMZH02,MMZH03,MMZH04) " +
					"VALUES ('"+ZCXX01+"',ZCXX02?,MMZH01?,0,SYSDATE,'"+MMZH04+"')";
		}else{
			
			sql = "UPDATE W_MMZH SET MMZH01=MMZH01?,ZCXX02=ZCXX02?,MMZH02=0,MMZH03=SYSDATE WHERE ZCXX02=ZCXX02?";
		}
		
		Map row=getRow(sql, null, 0);
		int j=execSQL(o2o, sql, row);
		if(j>0){
			map.put("STATE", "1");
		}else{
			map.put("STATE", "0");
		}
		
		return map;
	}
	
	/*
	 * 找回密码
	 * */
	//找回密码，修改状态
	@SuppressWarnings("unchecked")
	@RequestMapping("/updatePW.action")
	public Map updatePW(String XmlData) throws Exception{
		Map map = new HashMap();
		cds=new DataSet(XmlData);
		try {
			String GSID=cds.getField("GSID", 0);
			String sql="UPDATE W_XTCZY SET PASSWD='"+cds.getField("PASSWD", 0)+"' WHERE GSID='"+GSID+"'";
			Map row=getRow(sql, null, 0);
			execSQL(o2o, sql, row);
			String sSQL="UPDATE W_MMZH SET MMZH02=1 WHERE ZCXX01='"+GSID+"'";
			Map row2=getRow(sSQL, null, 0);
			execSQL(o2o, sSQL, row2);
			map.put("STATE", "1");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			map.put("STATE", "0");
			e.printStackTrace();
			throw e;
		}
		return map;
	}
	
	//驳回，修改状态
	@SuppressWarnings("unchecked")
	@RequestMapping("/updateBackPW.action")
	public Map updateBackPW(String XmlData) throws Exception{
		Map map = new HashMap();
		cds=new DataSet(XmlData);
		try {
			String sql="UPDATE W_MMZH SET MMZH02=2 WHERE ZCXX01='"+cds.getField("GSID", 0)+"'";
			Map row=getRow(sql, null, 0);
			execSQL(o2o, sql, row);
			map.put("STATE", "1");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			map.put("STATE", "0");
			e.printStackTrace();
			throw e;
		}
		return map;
	}
	
	/**
	 * 查询店铺的基本信息
	 */
/*	@SuppressWarnings("unchecked")
	@RequestMapping("/selectDPInfo.action")
	public Map selectDPInfo(String XmlData) throws Exception{
		Map map = new HashMap();
		cds=new DataSet(XmlData);
		try {
			String GSID=cds.getField("gsid",0);
			String sql="SELECT ZCXX02,ZCXX06,ZCXX08,ZCXX09,ZCXX26 FROM W_ZCGS WHERE ZCXX01='"+GSID+"'";
			Map list=queryForMap(o2o, sql);
			map.put("list", list);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw e;
		}
		return map;
	}*/
	
	/**
	 * 查询企业账户的基本信息
	 */
	@SuppressWarnings("unchecked")
	@RequestMapping("/selectCompanyInfo.action")
	public Map selectCompanyInfo(String XmlData) throws Exception{
		Map map=new HashMap();
		cds=new DataSet(XmlData);
		try {
			String ZCXX01=cds.getField("ZCXX01",0);
			String sql="SELECT C.ZCXX02,B.XTCZY01,B.HYGLM,A.SBM,B.WZM,C.ZCXX07,C.ZCXX08,C.ZCXX03,C.ZCXX33,C.ZCXX06,C.ZCXX55,C.ZCXX09,C.ZCXX64,C.ZCXX70,D.DPTP01,D.DPTP02," +
							 "D.DPTP05,C.ZCXX57,C.ZCXX66,C.ZCXX67,C.ZCXX68,C.ZCXX27,C.ZCXX70,G.GSYHK01,G.GSYHK09,G.GSYHK03,G.GSYHK02,(SELECT LX FROM w_gslx WHERE w_gslx.gsid = C.zcxx01)LX,zcgs01 "+
							 "FROM  W_ZCGS B LEFT JOIN W_ZCXX C ON B.ZCXX01=C.ZCXX01 LEFT JOIN W_ZCGSSBM A ON A.ZCXX01=B.ZCXX01 LEFT JOIN W_DPTP D ON B.ZCXX01=D.ZCXX01 " +
							 "LEFT JOIN W_GSYHK G ON B.ZCXX01=G.ZCXX01 AND G.GSYHK11=1 WHERE C.ZCXX01='"+ZCXX01+"'";
			List list=queryForList(o2o, sql);
			map.put("lists",list);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw e;
		}
		return map;

	}
	
	/**
	 * 更新企业账户的基本信息
	 */
	@SuppressWarnings("unchecked")
	@RequestMapping("/updateCompanyInfo.action")
	public Map updateCompanyInfo(String XmlData,HttpServletRequest request,HttpServletResponse response) throws Exception{	
		Map<String, Object> hm =new HashMap<String, Object>();
		XmlData= JLTools.unescape(XmlData);
		cds=new DataSet(XmlData);
		try {
			String GSLX=cds.getField("GSLX",0); //企业类型
			String ZCXX01=cds.getField("ZCXX01",0);
			String ZCXX02=cds.getField("dpName", 0);//企业名称
			String SBM=cds.getField("csm",0);//识别码
			String WZM=cds.getField("wzm",0);//位置码
			String ZCXX07=cds.getField("dqdm",0);//区域编码
			String ZCXX08=cds.getField("address",0);//公司中文地址
			String ZCXX03=cds.getField("lxr",0);//联系人
			String ZCXX06=cds.getField("phone",0);//手机号码
			String ZCXX55=cds.getField("zj",0);//座机号码
			String ZCXX64=cds.getField("fplx",0);//发票类型
			String ZCXX09=cds.getField("eml",0);//邮箱地址
			String ZCXX70=cds.getField("ZCXX70", 0);//店铺名称
			String YYYY=cds.getField("YYYY",0);
			String MM=cds.getField("MM",0);
			String DD=cds.getField("DD",0);
			String birthday=YYYY+"-"+MM+"-"+DD;//出身日期
			String weixin=cds.getField("weixin",0);//微信
			String qq=cds.getField("qq",0);//QQ
			String xl=cds.getField("xl",0);//会员卡号
			String ZCGS01=cds.getField("state",0);//状态
			String Six=cds.getField("Six", 0);
			String Longitude=cds.getField("Longitude", 0);//经度
			String Latitude=cds.getField("Latitude", 0);//纬度
			
			String sql1="UPDATE W_ZCXX " +
			"SET ZCXX02='"+ZCXX02+"', ZCXX07='"+ZCXX07+"',ZCXX08='"+ZCXX08+"',ZCXX03='"+ZCXX03+"'," +
			"ZCXX06='"+ZCXX06+"',ZCXX55='"+ZCXX55+"',ZCXX09='"+ZCXX09+"',ZCXX70='"+ZCXX70+"',";
			if(!qq.equals("") && qq!=null){
				sql1 += "ZCXX57='"+qq+"',";
			}
			if(!weixin.equals("") && weixin!=null){
				sql1 += "ZCXX67='"+weixin+"',";
			}
			if(!xl.equals("") && xl!=null){
				sql1 += "ZCXX68='"+xl+"',";
			}
			if(!YYYY.equals("") && YYYY!=null){
				sql1 += "ZCXX66='"+birthday+"',";				
			}
			if(!Six.equals("") && YYYY!=null){
				sql1 += "ZCXX04='"+Six+"',";				
			}
			/**if(!ZCXX02.equals("") && qq!=null){
				sql1 += "ZCXX02='"+ZCXX02+"',";
			}*/
			if(!Longitude.equals("") && Longitude!=null){
				sql1 += "Longitude="+Longitude+",";				
			}
			if(!Latitude.equals("") && Latitude!=null){
				sql1 += "Latitude="+Latitude+",";				
			}
			
			sql1 +="ZCXX64='"+ZCXX64+"' WHERE ZCXX01='"+ZCXX01+"'";
			Map row1=getRow(sql1, null, 0);
			execSQL(o2o, sql1, row1);
			
			//跟新厂家识别码
			if(SBM != null && !SBM.equals("")){
				String SQL2 = "DELETE FROM W_ZCGSSBM WHERE ZCXX01='"+ZCXX01+"'";
				execSQL(o2o, SQL2, new HashMap());
				SQL2 = "INSERT INTO W_ZCGSSBM(ZCXX01,SBM) VALUES('"+ZCXX01+"','"+SBM+"')";
				execSQL(o2o, SQL2, new HashMap());
			}
			//更新W_ZCGS
			String SQL3="UPDATE W_ZCGS " +
					"SET ZCXX02='"+ZCXX02+"', ZCXX07='"+ZCXX07+"',ZCXX08='"+ZCXX08+"'," +
					"ZCXX03='"+ZCXX03+"',ZCXX06='"+ZCXX06+"'," +
					"ZCXX55='"+ZCXX55+"',ZCXX09='"+ZCXX09+"'," +
					"WZM='"+WZM+"',ZCGS01="+ZCGS01+" WHERE ZCXX01='"+ZCXX01+"'";
			Map row3=getRow(SQL3, null, 0);
			execSQL(o2o, SQL3, row3);
			String GSYHK01=cds.getField("GSYHK01",0);//银行卡编号
			String jskhhmc=cds.getField("jskhhmc",0);//开户行名称GSYHK09
			String jshkhh=cds.getField("jshkhh", 0);//开户行GSYHK03
			String jshkhhzh=cds.getField("jshkhhzh", 0);//开户行账号GSYHK02
			if (GSYHK01.equals("") || GSYHK01 == null){
				//公司银行卡处理.生产和经销企业更新银行卡
				if(("42".equals(GSLX) || "43".equals(GSLX)) && GSLX != null){
					int GSYHK01Num=JLTools.getJLBH(o2o,"W_GSYHK",1);
					String sql = "INSERT INTO W_GSYHK(GSYHK01,ZCXX01,GSYHK02,GSYHK03,GSYHK09,GSYHK10,GSYHK11) " +
						  "VALUES("+GSYHK01Num+",'"+ZCXX01+"','"+jshkhhzh+"','"+jshkhh+"','"+jskhhmc+"',now(),1)";
					Map yhkRow = getRow(sql, null, 0);
					execSQL(o2o, sql, yhkRow);
				}
			}
			
			//更新银行卡信息
			if (GSYHK01 != null && !GSYHK01.equals("")) {
				String yhkSQL = "UPDATE W_GSYHK SET GSYHK02='"+jshkhhzh+"',GSYHK03='"+jshkhh+"',GSYHK09='"+jskhhmc+"' WHERE GSYHK01="+GSYHK01+"";
				execSQL(o2o, yhkSQL, new HashMap());				
			}
			
			
			
			//更新图片编码
			String YYZZBH=cds.getField("yyzzfb_code",0); //营业执照
			String ZZJGBH=cds.getField("zzjg_code",0);//组织机构代码证
			String SWDJBH=cds.getField("swdj_code",0);//税务登记证
			String YHKHZBH=cds.getField("khyh_code",0);//银行开户许可证
			String YBNSRBH=cds.getField("ybnsr_code",0);//一般纳税人证明
			String DLXKZBH=cds.getField("dlxzk_code",0);//道路许可证
			if(YYZZBH!="" && YYZZBH!=null){
				updateZJHM(ZCXX01,3,YYZZBH);
			}
			if(ZZJGBH!="" && ZZJGBH!=null){
				updateZJHM(ZCXX01,4,ZZJGBH);
			}
			if(SWDJBH!="" && SWDJBH!=null){
				updateZJHM(ZCXX01,5,SWDJBH);
			}
			if(YHKHZBH!="" && YHKHZBH!=null){
				updateZJHM(ZCXX01,6,YHKHZBH);
			}
			if(YBNSRBH!="" && YBNSRBH!=null){
				updateZJHM(ZCXX01,7,YBNSRBH);
			}
			if(DLXKZBH!="" && DLXKZBH!=null){
				updateZJHM(ZCXX01,9,DLXKZBH);
			}
			//对图片进行处理
			DiskFileItemFactory factory=new DiskFileItemFactory();
			ServletFileUpload upload=new ServletFileUpload(factory);
			MultipartHttpServletRequest mrRequest=(MultipartHttpServletRequest)request;
			Iterator<String> iterator = mrRequest.getFileNames();
			while (iterator.hasNext()) {
				// 获取文件名file类型的name属性
				String fileType = (String)iterator.next();
				MultipartFile file = mrRequest.getFile(fileType);
				
				if(fileType.equals("dp_logo")){
					//logo图片单独上传
					updateDPLogo(file,XmlData);
				}else if(file.getOriginalFilename() != null){
					String fileName =fileType + "." + file.getOriginalFilename().split("\\.")[1];
					String path=JlAppResources.getProperty("path_dptp");
					String tplx = fileType.split("_")[1];
					String sql="SELECT COUNT(0) FROM W_DPTP WHERE ZCXX01='"+ZCXX01+"' AND DPTP01='"+tplx+"'";
					int num = queryForInt(o2o,sql);
					if(num==0){
						sql="INSERT INTO W_DPTP(ZCXX01,DPTP01,DPTP02,DPTP04) VALUES('"+ZCXX01+"','"+tplx+"','"+fileName+"',now())";
					}
					else{
						sql="UPDATE W_DPTP SET DPTP04=NOW(),DPTP02='"+fileName+"' WHERE ZCXX01='"+ZCXX01+"' AND DPTP01='"+tplx+"'";
					}
					Map rows=getRow(sql, null, 0);
					execSQL(o2o, sql, rows);
					//上传图片
					Map map = new HashMap();
					map.put("imgPath", JlAppResources.getProperty("path_dptp")+ZCXX01);
					map.put("imgName", fileName);
					JLTools.fileUploadNew(file,map);
				}
			}
			hm.put("STATE", "1");
		} catch (Exception e) {
			hm.put("STATE", "0");
			e.printStackTrace();
			throw e;
		}
		return hm;
	}
	
	/**
	 * @todo 跟新证件号码
	 * @param zcxx01
	 * @param zjhm
	 * @throws Exception
	 */
	public void updateZJHM(String zcxx01,int tplx,String zjhm) throws Exception{
		try{
			String sql = "SELECT COUNT(0) FROM W_DPTP WHERE ZCXX01='"+zcxx01+"' AND DPTP01="+tplx+"";
			int num = queryForInt(o2o,sql);
			if(num == 0){
				sql="INSERT INTO W_DPTP(ZCXX01,DPTP01,DPTP02,DPTP04,DPTP05) VALUES('"+zcxx01+"',"+tplx+",'',now(),'"+zjhm+"')";
			}else{
				sql="UPDATE W_DPTP SET DPTP04=NOW(),DPTP05='"+zjhm+"' WHERE ZCXX01='"+zcxx01+"' AND DPTP01="+tplx+"";
			}
			execSQL(o2o, sql, new HashMap());
		}catch(Exception ex){
			throw ex;
		}
	}
	
	/**
	 * 更改银行卡启用标记
	 */
	@SuppressWarnings("unchecked")
	@RequestMapping("/updateYHKBJ.action")
	public Map updateYHKBJ(String XmlData) throws Exception{
		Map map = new HashMap();
		cds=new DataSet(XmlData);
		try {
			String sql1="UPDATE W_GSYHK SET GSYHK11=1 WHERE  GSYHK01 = GSYHK01? AND ZCXX01 = '"+cds.getField("ZCXX01", 0)+"' ";
			Map row1=getRow(sql1, null, 0);
			execSQL(o2o, sql1, row1);
			String sql2="UPDATE W_GSYHK SET GSYHK11=0 WHERE  GSYHK01 != GSYHK01? AND ZCXX01 = '"+cds.getField("ZCXX01", 0)+"' ";
			Map row2=getRow(sql2, null, 0);
			execSQL(o2o, sql2, row2);
			map.put("STATE", "1");
		} catch (Exception e) {
			map.put("STATE", "0");
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw e;
		}
		return map;

	}
	/**
	 * 删除银行卡信息
	 */
	@SuppressWarnings("unchecked")
	@RequestMapping("/deleteW_GSYHK.action")
	public Map deleteW_GSYHK(String XmlData) throws Exception{
		Map map = new HashMap();
		cds=new DataSet(XmlData);
		try {
			String sql1="DELETE FROM W_GSYHK  WHERE  GSYHK01 = GSYHK01?";
			Map row1=getRow(sql1, null, 0);
			execSQL(o2o, sql1, row1);
			map.put("STATE", "1");
		} catch (Exception e) {
			map.put("STATE", "0");
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw e;
		}
		return map;

	}
	//新增银行卡信息
	@RequestMapping("/insertW_GSYHK.action")
	public Map<String, Object> insertW_GSYHK(String XmlData,HttpServletRequest request,HttpServletResponse response) throws Exception{
		Map<String, Object> hm =new HashMap<String, Object>();
		try {
			XmlData= JLTools.unescape(XmlData);
			cds=new DataSet(XmlData);
			DiskFileItemFactory factory=new DiskFileItemFactory();
			ServletFileUpload upload=new ServletFileUpload(factory);
			MultipartHttpServletRequest mrRequest=(MultipartHttpServletRequest)request;
			List<MultipartFile> listFile= mrRequest.getFiles("file");
			String GSYHK09="";
			String GSYHK10="";
			for (int i = 0; i < listFile.size(); i++) {
				MultipartFile file1 = listFile.get(0);
				GSYHK09 = file1.getOriginalFilename();
				MultipartFile file2 = listFile.get(1);
				GSYHK10 = file2.getOriginalFilename();
				MultipartFile file = listFile.get(i);
				String fileName = file.getOriginalFilename();
				String fileSuffix=fileName.substring(fileName.lastIndexOf("."));
			String ZCXX01=cds.getField("ZCXX01", 0);
			String path=JlAppResources.getProperty("path_yhktp");
			String savepath=path+ZCXX01;
			Object obj=request.getParameter("file");
			if(!upload.isMultipartContent(request)&&obj==null){
			}else{
				hm.put("imgPath",savepath);
				hm.put("imgName", fileName);
				JSONArray jso =JSONArray.fromObject(XmlData);
				hm.put("sqlParam", (List<Map<String, Object>>)jso);
				//TODO 案例分享 
				//调用图片上传公用方法
				hm=JLTools.fileUploadNew(file, hm);
				}
			}
			if(true==Boolean.parseBoolean(hm.get("STATE").toString())){
				int GSYHK01=JLTools.getJLBH(o2o,"W_GSYHK",1);
				String sql="INSERT INTO W_GSYHK (GSYHK01,ZCXX01,GSYHK02,GSYHK03,GSYHK04,GSYHK05,GSYHK06,GSYHK07,GSYHK08,GSYHK09,GSYHK10,GSYHK11)" +
				"VALUES("+GSYHK01+",ZCXX01?,GSYHK02?,GSYHK03?,GSYHK04?,GSYHK05?,GSYHK06?,GSYHK07?,GSYHK08?,GSYHK09?,NOW(),1)";
				Map row=getRow(sql, null, 0);
				execSQL(o2o, sql, row);
				//更新其他银行卡绑定为禁用
				String updateSql="UPDATE W_GSYHK SET  GSYHK11='0' WHERE GSYHK01!= '"+GSYHK01+"' AND ZCXX01 = '"+cds.getField("ZCXX01", 0)+"'";
				row=getRow(updateSql, null, 0);
				execSQL(o2o, updateSql, row);
				
				hm.clear();
				hm.put("STATE", "1");
			}
		} catch (Exception e) {
			e.printStackTrace();
			hm.put("STATE", "0");
			throw e;
		}
		return hm;
	} 
	//修改银行卡信息
	@RequestMapping("/updateW_GSYHK.action")
	public Map<String, Object> updateW_GSYHK(String XmlData,HttpServletRequest request,HttpServletResponse response) throws Exception{
		Map<String, Object> hm =new HashMap<String, Object>();
		try {
			XmlData= JLTools.unescape(XmlData);
			cds=new DataSet(XmlData);
			DiskFileItemFactory factory=new DiskFileItemFactory();
			ServletFileUpload upload=new ServletFileUpload(factory);
			if(cds.getField("TPBJ", 0).equals("0")){
				String sql="UPDATE W_GSYHK SET GSYHK02=GSYHK02?,GSYHK03=GSYHK03?,GSYHK04=GSYHK04?," +
				"GSYHK05=GSYHK05?,GSYHK06=GSYHK06?,GSYHK07=GSYHK07?,GSYHK08=GSYHK08?,GSYHK09=GSYHK09?,GSYHK10=NOW(),GSYHK11=GSYHK11? WHERE  GSYHK01=GSYHK01? AND ZCXX01 = '"+cds.getField("ZCXX01", 0)+"' ";
				Map row=getRow(sql, null, 0);
				execSQL(o2o, sql, row);
				hm.clear();
				hm.put("STATE", "1");
			}else{
			MultipartHttpServletRequest mrRequest=(MultipartHttpServletRequest)request;
			//listFile.size!=null&&listFile.size()>0 这样判断是否有图片
			List<MultipartFile> listFile= mrRequest.getFiles("file");
			for (int i = 0; i < listFile.size(); i++) {
				MultipartFile file = listFile.get(i);
				String fileName = file.getOriginalFilename();
			String ZCXX01=cds.getField("ZCXX01", 0);
			String path=JlAppResources.getProperty("path_yhktp");
			String savepath=path+ZCXX01;
			Object obj=request.getParameter("file");
			if(!upload.isMultipartContent(request)&&obj==null){
			}else{
				hm.put("imgPath",savepath);
				hm.put("imgName", fileName);
				JSONArray jso =JSONArray.fromObject(XmlData);
				hm.put("sqlParam", (List<Map<String, Object>>)jso);
				//TODO 案例分享 
				//调用图片上传公用方法
				hm=JLTools.fileUploadNew(file, hm);
				}
			}
			if(true==Boolean.parseBoolean(hm.get("STATE").toString())){
				//如果修改当前状态为启用，则将其它银行卡片禁用
				String sql="UPDATE W_GSYHK SET GSYHK02=GSYHK02?,GSYHK03=GSYHK03?,GSYHK04=GSYHK04?," +
						"GSYHK05=GSYHK05?,GSYHK06=GSYHK06?,GSYHK07=GSYHK07?,GSYHK08=GSYHK08?,GSYHK09=GSYHK09?,GSYHK10=NOW(),GSYHK11=GSYHK11? WHERE GSYHK01=GSYHK01? AND ZCXX01 = '"+cds.getField("ZCXX01", 0)+"' ";
				Map row=getRow(sql, null, 0);
				execSQL(o2o, sql, row);
				hm.clear();
				hm.put("STATE", "1");
			}
		}
			//如果修改当前状态为启用，则将其它银行卡片禁用
			if(cds.getField("GSYHK11", 0).equals("1")){
				String sql="UPDATE W_GSYHK SET GSYHK11=0 WHERE GSYHK01!=GSYHK01? AND ZCXX01 = '"+cds.getField("ZCXX01", 0)+"'";
				Map row=getRow(sql, null, 0);
				execSQL(o2o, sql, row);
			}
		} catch (Exception e) {
			e.printStackTrace();
			hm.put("STATE", "0");
			throw e;
		}
		return hm;
	}
	
	
	//查询公司银行卡及注册信息 add 2015.10.15.
	/**
	 * 查询公司名称和营业执照
	 * @param ZCXX01
	 * @return
	 * @throws Exception
	 */
	@RequestMapping("/select_InvoiceForUserInfo.action")
	public Map select_InvoiceForUserInfo(String ZCXX01) throws Exception {
		Map resultMap = new HashMap();
		try {
			String sql = "";
			sql = "SELECT "
				+ "A.ZCXX02 ZCXX02, "
				+ "A.ZCXX28 ZCXX28, "
				+ "A.ZCXX71 ZCXX71, "
				+ "A.ZCXX72 ZCXX72, "
				+ "B.DPTP02 DPTP02, "
				+ "C.GSYHK02 GSYHK02, "
				+ "C.GSYHK03 GSYHK03 "
				+ "FROM "
				+ "W_ZCXX A "
				+ "LEFT JOIN W_DPTP B "
				+ "ON A.ZCXX01 = B.ZCXX01 "
				+ "LEFT JOIN W_GSYHK C "
				+ "ON A.ZCXX01 = C.ZCXX01 "
				+ "AND C.GSYHK11 = 1 "
				+ "WHERE "
				+ "A.ZCXX01 = '" + ZCXX01 + "' "
				+ "AND B.DPTP01 IN (3, 4, 5, 6)";
			List resultList = queryForList(o2o, sql);
			
			resultMap.put("resultList", resultList);
			resultMap.put("STATE", "1");
		} catch (Exception e) {
			resultMap.put("STATE", "0");
			throw e;
		}
		
		return resultMap;
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
	 * 查询生产企业辐射区域范围
	 * @author NineDragon
	 * @version 2015/12/14
	 * @return returnMap
	 * @throws Exception
	 */
	@RequestMapping("/select_QYFGFW.action")
	public Map<String, Object> select_QYFGFW(String XmlData) throws Exception{
		Map resultMap = new HashMap();
		cds=new DataSet(XmlData);
		String zcxx01 = cds.getField("zcxx01", 0);
		String type = cds.getField("type", 0);
		String dqckType = cds.getField("dqckType", 0);
		try {
			// 20151231 齐俊宇 update 修改增加查询大区仓库状态的连接 BEGIN
			//String sql = " SELECT c.dqbzm01 ,d.dqbzm02 SF,c.dqbzm02 SJ FROM  w_gsck e LEFT JOIN ck a ON e.ck01 = a.ck01 LEFT JOIN w_dqck b ON a.ck01=b.ck01 LEFT JOIN dqbzm c ON b.dqxx01=c.dqbzm01 LEFT JOIN dqbzm d ON c.dqbzm_dqbzm01= d.dqbzm01 WHERE e.zcxx01='"+zcxx01+"' AND a.ck09=0 AND a.type='"+type+"'";
			String sql = " SELECT c.dqbzm01 ,d.dqbzm02 SF,c.dqbzm02 SJ FROM  w_gsck e LEFT JOIN ck a ON e.ck01 = a.ck01 LEFT JOIN w_dqck b ON a.ck01=b.ck01 AND b.type = '" + dqckType + "' LEFT JOIN dqbzm c ON b.dqxx01=c.dqbzm01 LEFT JOIN dqbzm d ON c.dqbzm_dqbzm01= d.dqbzm01 WHERE e.zcxx01='"+zcxx01+"' AND a.ck09=0 AND a.type='"+type+"'";
			// 20151231 齐俊宇 update 修改增加查询大区仓库状态的连接 END
			List resultList = queryForList(o2o, sql);
			resultMap.put("resultList", resultList);
			resultMap.put("STATE", "1");
		} catch (Exception e) {
			resultMap.put("STATE", "0");
			throw e;
		}
		return resultMap;
}
}
