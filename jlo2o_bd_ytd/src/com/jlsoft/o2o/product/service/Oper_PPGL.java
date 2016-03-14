package com.jlsoft.o2o.product.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import com.jlsoft.framework.JLBill;
import com.jlsoft.framework.dataset.DataSet;
import com.jlsoft.o2o.interfacepackage.V9.V9BasicData;
import com.jlsoft.o2o.pub.service.PubService;
import com.jlsoft.utils.JLTools;
import com.jlsoft.utils.JlAppResources;
import com.jlsoft.utils.PubFun;

@Controller
@RequestMapping("/Oper_PPGL")
public class Oper_PPGL extends JLBill {
	
	@Autowired
	private PubService pubService;
	@Autowired
	private V9BasicData v9BasicData;
	
	public Oper_PPGL(JdbcTemplate o2o) {
		this.o2o = o2o;
	}

	public Oper_PPGL() {

	}

	/**
	 * @todo 配件品牌查询
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	@RequestMapping("/getJson/searchPPB.action")
	public Map searchPPB(String XmlData) throws Exception {
		Map map = new HashMap();
		cds = new DataSet(XmlData);
		List arrList = new ArrayList();
		// String pptp01 = cds.getField("pptp01", 0);
		String zcxx01 = cds.getField("ZCXX01", 0);
		Map resultMap = new HashMap();
		String querySql = "";
		// NineDragon 2015/11/17 修改以W_SPXX 为主表查询 店铺的品牌图片
		// 20151201 齐俊宇 修改关联信息 删除W_PPTP表的关联内容
		if (!"null".equals(zcxx01) && !"".equals(zcxx01)) {
			// 20160106 齐俊宇 update 修改查询品牌logo的方式 BEGIN
			/*querySql = "SELECT P.PPB01 ppb01, P.PPB02 ppb02, P.PPB06 ppb06, P.PPTP PPTP02 "
					+ "FROM W_SPXX S "
					+ "LEFT JOIN W_SPGL G ON S.SPXX01 = G.SPXX01 "
					+ "LEFT JOIN PPB P ON S.PPB01 = P.PPB01 "
					+ "LEFT JOIN W_PPQX Q ON P.PPB01 = Q.PPB01 "
					+ "WHERE G.ZCXX01 = '"
					+ zcxx01
					+ "' "
					+ "AND P.PPB04 = 1 "
					+ "AND P.YXBJ = 1 "
					+ "AND Q.STATE = 1 "
					+ "GROUP BY P.PPB01 "
					+ "ORDER BY P.PPB06";*/
			querySql = "SELECT P.PPB01 ppb01, P.PPB02 ppb02, P.PPB06 ppb06, P.PPTP PPTP02 "
					+ "FROM W_PPQX Q "
					+ "LEFT JOIN PPB P ON Q.PPB01 = P.PPB01 WHERE Q.ZCXX01 = '" + zcxx01 + "' "
					+ "AND P.PPB04 = 1 AND P.YXBJ = 1 AND Q.STATE = 1 "
					+ "GROUP BY P.PPB01 "
					+ "UNION "
					+ "SELECT P.PPB01, P.PPB02, P.PPB06 ppb06, P.PPTP PPTP02 "
					+ "FROM PPB P "
					+ "LEFT JOIN W_GSGXPP GP ON P.PPB01 = GP.PPB01 "
					+ "LEFT JOIN W_GSGX G ON G.HBID = GP.HBID WHERE G.HBID = '" + zcxx01 + "' "
					+ "AND P.PPB04 = 1 AND P.YXBJ = 1 AND G.STATE = 1 "
					+ "GROUP BY P.PPB01 "
					+ "ORDER BY ppb06";
			// 20160106 齐俊宇 update 修改查询品牌logo的方式 END
		} else {
			querySql = "SELECT t1.ppb01,t1.ppb02,t1.ppb06, t1.PPTP PPTP02 FROM W_PPQX T "
					+ "LEFT JOIN PPB t1 ON T.PPB01 = t1.PPB01 "
					+ " where t1.ppb04 = 1 and YXBJ = 1 AND T.STATE = 1 order by t1.ppb06";
			// " where t2.pptp01="+pptp01+" or t2.pptp01 is null ";
		}

		List ppbList = queryForList(o2o, querySql);
		for (int i = 0; i < ppbList.size(); i++) {
			HashMap<String, Object> rowMap = (HashMap<String, Object>) ppbList
					.get(i);
			String ppb06 = (String) rowMap.get("ppb06");

			boolean isExists = false;
			int isExistIndex = 0;
			for (int j = 0; j < arrList.size(); j++) {
				HashMap<String, Object> rowMapNew = (HashMap<String, Object>) arrList
						.get(j);
				String ppb06New = (String) rowMapNew.get("code");
				if (ppb06.equals(ppb06New)) {
					isExists = true;
					isExistIndex = j;
					break;
				}
			}
			if (isExists) {
				Map isExistsMap = (Map) arrList.get(isExistIndex);
				List existlist = (List) isExistsMap.get("list");
				existlist.add(rowMap);
			} else {
				Map<String, Object> sortMap = new HashMap<String, Object>();
				sortMap.put("code", ppb06);
				List addList = new ArrayList();
				addList.add(rowMap);
				sortMap.put("list", addList);
				arrList.add(sortMap);
			}
		}

		resultMap.put("data", arrList);
		return resultMap;
	}

	/**
	 * 查询品牌名称
	 * 
	 * @param jsonData
	 * @return
	 * @throws Exception
	 */
	@RequestMapping("/selectPPXX.action")
	public Map<String, Object> selectPPXX(String query) throws Exception {
		Map<String, Object> resultMap = new HashMap<String, Object>();
		try {
			String sql = "SELECT P.PPB01, P.PPB02 label, P.PPB07, P.PPTP, P.PPB04, P.ZCXX01, G.LX "
					   + "FROM PPB P LEFT JOIN W_GSLX G ON P.ZCXX01 = G.GSID "
					   + "WHERE P.PPB02 LIKE CONCAT('%','"
					   + query + "','%') AND P.PPB04 = 1 ORDER BY PPB02 ASC LIMIT 10";
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
	 * 新增品牌--包括已有品牌授权
	 * @param XmlData
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	@RequestMapping("/insertPPB.action")
	public Map<String, Object> insertPPB(String XmlData,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		Map<String, Object> hm = new HashMap<String, Object>();
		try {
			XmlData = JLTools.unescape(XmlData);
			cds = new DataSet(XmlData);
			String HBID = cds.getField("HBID", 0);
			String PPMC = cds.getField("ppmc", 0);
			// 20151201 齐俊宇 品牌申请用户上传资质状态: 1经销商上传品牌授权书 2生产商上传商标注册证
			String PPTP01 = cds.getField("PPTP01", 0);
			String PPB07 = cds.getField("gsmc", 0);
			String sPPB01 = cds.getField("PPB01", 0);
			if("".equals(sPPB01)){
				// 20151225 齐俊宇 update 修改PPB01生成规则
				/*String s = "SELECT DISTINCT MAX(CAST(PPB01 AS SIGNED INTEGER)) as ppb01 FROM PPB";
				int PPB01 = queryForInt(o2o, s);
				PPB01 = PPB01 + 1;
				sPPB01 = "" + PPB01;
				int j = sPPB01.length();
				for (int i = j; i < 6; i++) {
					sPPB01 = "0" + sPPB01;
				}*/
				sPPB01 = "SQ" + JLTools.getID_X(PubFun.updateWBHZT(o2o,"PPB_SQ",1),6);
			}
			
			// 20151201 齐俊宇 品牌申请插入W_PPQX表 PPB表插入图片放到循环中进行插入
			String w_ppqxSql = "INSERT INTO W_PPQX(ZCXX01, PPB01, STATE, ENDTIME) VALUES('"
					+ HBID
					+ "','"
					+ sPPB01
					+ "','0','"
					+ cds.getField("ENDTIME", 0) + "')";
			Map row4 = getRow(w_ppqxSql, null, 0);
			execSQL(o2o, w_ppqxSql, row4);

			String[] ppflList = cds.getField("ppfl", 0).split(",");
			for (int i = 0; i < ppflList.length; i++) {
				String sql2 = "INSERT INTO W_PPFL(ZCXX01,PPB01,SPFL01) VALUES('"
						+ HBID + "','" + sPPB01 + "','" + ppflList[i] + "')";
				Map row2 = getRow(sql2, null, 0);
				execSQL(o2o, sql2, row2);
			}

			DiskFileItemFactory factory = new DiskFileItemFactory();
			ServletFileUpload upload = new ServletFileUpload(factory);
			MultipartHttpServletRequest mrRequest = (MultipartHttpServletRequest) request;
			List<MultipartFile> listFile = mrRequest.getFiles("file");
			for (int i = 0; i < listFile.size(); i++) {
				MultipartFile file = listFile.get(i);
				String fileName = file.getOriginalFilename();
				String fileSuffix = fileName.substring(fileName
						.lastIndexOf("."));
				String PPTP02 = HBID + "_" + PPMC + "_" + PPTP01 + fileSuffix;// 图片名称，店铺编码+图片类型+图片格式
				String path = JlAppResources.getProperty("path_pptp");
				String PPTP03 = path + PPTP02;
				// 20151201 齐俊宇 品牌申请 i = 0 并且 listFile.size() > 1 时证明有品牌logo的上传,插入PPB表,并将PPTP字段进行插入
				if (i == 0 && listFile.size() > 1) {
					PPTP02 = HBID + "_" + PPMC + "_" + i + fileSuffix;// 图片名称，店铺编码+图片类型+图片格式
					String sql = "INSERT INTO PPB(ZCXX01,PPB01,PPB02,time01,PPTP, PPB04,PPB06,PPB07) VALUES('"
							+ HBID
							+ "','"
							+ sPPB01
							+ "',\""
							+ PPMC
							+ "\",NOW(),\""
							+ PPTP02
							+ "\", '0',UPPER(SUBSTRING(fn_getpy(\""
							+ PPMC
							+ "\"),1,1)), \"" + PPB07 + "\")";
					Map row = getRow(sql, null, 0);
					execSQL(o2o, sql, row);
				} else {
					String sql3 = "INSERT INTO W_PPTP(ZCXX01,PPB01,PPTP01,PPTP02,PPTP03,PPTP04) VALUES('"
							+ HBID
							+ "','"
							+ sPPB01
							+ "','"
							+ PPTP01
							+ "','"
							+ PPTP02 + "','" + PPTP03 + "',NOW())";
					Map row3 = getRow(sql3, null, 0);
					execSQL(o2o, sql3, row3);
				}

				if (!upload.isMultipartContent(request)) {

				} else {
					hm.put("imgPath", path);
					hm.put("imgName", PPTP02);
					JSONArray jso = JSONArray.fromObject(XmlData);
					hm.put("sqlParam", (List<Map<String, Object>>) jso);
					// 调用图片上传公用方法
					hm = JLTools.fileUploadNew(file, hm);
				}
			}
			hm.put("STATE", "1");
		} catch (Exception e) {
			e.printStackTrace();
			hm.put("STATE", "0");
			throw e;
		}
		return hm;
	}

	/**
	 * 修改品牌信息
	 * @param XmlData
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	@RequestMapping("/updatePPB.action")
	public Map<String, Object> updatePPB(String XmlData,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		Map<String, Object> hm = new HashMap<String, Object>();
		try {
			XmlData = JLTools.unescape(XmlData);
			cds = new DataSet(XmlData);
			String HBID = cds.getField("HBID", 0); // 公司ID ZCXX01 字段
			String PPMC = cds.getField("ppmc", 0); // 品牌名称
			String PPTP01 = cds.getField("PPTP01", 0);
			String sPPB01 = cds.getField("PPB01", 0);
			
			String w_ppqxSql = "UPDATE W_PPQX SET STATE = '0', ENDTIME = '" + cds.getField("ENDTIME", 0) + "' "
					+ "WHERE PPB01 = '" + sPPB01 + "' AND ZCXX01 = '" + HBID + "'";
			Map row = getRow(w_ppqxSql, null, 0);
			execSQL(o2o, w_ppqxSql, row);

			// 删除W_PPFL表相关内容
			String w_ppflSql = "DELETE FROM W_PPFL WHERE ZCXX01 = '" + HBID + "' AND PPB01 = '" + sPPB01 + "'";
			Map ppflRow = getRow(w_ppflSql, null, 0);
			execSQL(o2o, w_ppflSql, ppflRow);
			String[] ppflList = cds.getField("ppfl", 0).split(",");
			for (int i = 0; i < ppflList.length; i++) {
				String sql2 = "INSERT INTO W_PPFL(ZCXX01,PPB01,SPFL01) VALUES('"
						+ HBID + "','" + sPPB01 + "','" + ppflList[i] + "')";
				Map row2 = getRow(sql2, null, 0);
				execSQL(o2o, sql2, row2);
			}

			MultipartHttpServletRequest mrRequest = (MultipartHttpServletRequest) request;
			// 品牌LOGO 上传
			MultipartFile logoFile = mrRequest.getFile("fileLogo");
			String path = JlAppResources.getProperty("path_pptp");
			String PPTP = "";
			if(logoFile != null){
				String logoFileName = logoFile.getOriginalFilename();
				String logoFileSuffix = logoFileName.substring(logoFileName.lastIndexOf("."));
				PPTP = HBID + "_" + PPMC + "_0" + logoFileSuffix; // 图片名称，店铺编码+图片类型+图片格式
				updateTP(path, PPTP, XmlData, request, logoFile, "fileLogo");
			}
			
			String selectPPB = "SELECT PPB02 FROM PPB "
			 		 + "WHERE PPB01 = '" + sPPB01 + "'";
			Map ppbMap = queryForMap(o2o, selectPPB);
			// PPB表更新
			String ppb02 = ppbMap.get("PPB02").toString();
			
			if(!ppb02.equals(PPMC) || !"".equals(PPTP)){
				String ppbSql = "UPDATE PPB SET PPB02 = \"" + PPMC + "\", ";
				if(!"".equals(PPTP)){
					ppbSql = ppbSql + "PPTP = \"" + PPTP + "\", ";
				}
				ppbSql = ppbSql + "PPB06 = UPPER(SUBSTRING(fn_getpy(\"" + PPMC + "\"),1,1)) "
						+ "WHERE ZCXX01 = '" + HBID + "' "
						+ "AND PPB01 = '" + sPPB01 + "'";
				Map rowPPB = getRow(ppbSql, null, 0);
				execSQL(o2o, ppbSql, rowPPB);
			}
			
			// 商标注册授权证图片上传
			MultipartFile msgFile = mrRequest.getFile("fileMsg");
			if(msgFile != null){
				String msgFileName = msgFile.getOriginalFilename();
				String msgFileSuffix = msgFileName.substring(msgFileName.lastIndexOf("."));
				String PPTP02 = HBID + "_" + PPMC + "_" + PPTP01 + msgFileSuffix; // 图片名称，店铺编码+图片类型+图片格式
				String PPTP03 = path + PPTP02;
				String w_pptpSql = "UPDATE W_PPTP SET PPTP02 = '" + PPTP02 + "', "
							  + "PPTP03 = '" + PPTP03 + "', "
							  + "PPTP04 = NOW() "
							  + "WHERE ZCXX01 = '" + HBID + "' "
						  	  + "AND PPB01 = '" + sPPB01 + "' "
			  	  			  + "AND PPTP01 = '" + PPTP01 + "'";
				Map rowW_PPTP = getRow(w_pptpSql, null, 0);
				execSQL(o2o, w_pptpSql, rowW_PPTP);
				updateTP(path, PPTP02, XmlData, request, msgFile, "fileMsg");
			}
			hm.put("STATE", "1");
		} catch (Exception e) {
			e.printStackTrace();
			hm.put("STATE", "0");
			throw e;
		}
		return hm;
	}

	// 调用品牌更新图片的公用方法
	@SuppressWarnings("unchecked")
	public Map<String, Object> updateTP(String path, String PPTP02,
			String XmlData, HttpServletRequest request, MultipartFile file, String fileName)
			throws DataAccessException, Exception {
		DiskFileItemFactory factory = new DiskFileItemFactory();
		ServletFileUpload upload = new ServletFileUpload(factory);
		Map hm = new HashMap();
		if (!upload.isMultipartContent(request)) {

		} else {
			hm.put("imgPath", path);
			hm.put("imgName", PPTP02);
			JSONArray jso = JSONArray.fromObject(XmlData);
			hm.put("sqlParam", (List<Map<String, Object>>) jso);
			// 调用图片上传公用方法
			hm = JLTools.fileUploadNew(file, hm);
		}
		return hm;
	}
	
	/**
	 * 查询品牌名称
	 * @param jsonData
	 * @return
	 * @throws Exception
	 */
	@RequestMapping("/selectPPMC.action")
	public Map<String, Object> selectPPMC(String jsonData) throws Exception{
		Map<String, Object> resultMap = new HashMap<String, Object>();
		try {
			jsonData = JLTools.unescape(jsonData);
			cds = new DataSet(jsonData);
			String ppName = cds.getField("PPMC", 0);
			String sql = "SELECT COUNT(1) COUNT FROM PPB WHERE PPB02 = '" + ppName + "' AND PPB04 = 1";
			int count = queryForInt(o2o, sql);
			resultMap.put("COUNT", count);
			resultMap.put("STATE", "1");
		} catch (Exception e) {
			resultMap.put("STATE", "0");
			throw e;
		}
		return resultMap;
	}
	
	/**
	 * 修改品牌状态 
	 * @param jsonData
	 * @return
	 * @throws Exception
	 */
	@RequestMapping("/updateW_PPQXZT.action")
	public Map<String, Object> updateW_PPQXZT(String jsonData) throws Exception{
		Map<String, Object> resultMap = new HashMap<String, Object>();
		try {
			cds = new DataSet(jsonData);
			String zt = cds.getField("STATE", 0);
			String ppid = cds.getField("PPID", 0);
			String gsid = cds.getField("GSID", 0);
			String sql = "UPDATE W_PPQX SET STATE = " + zt + " WHERE PPB01 = " + ppid + " AND ZCXX01 = " + gsid;
			Map row = new HashMap();
			execSQL(o2o, sql, row);
			resultMap.put("STATE", "1");
		} catch (Exception e) {
			resultMap.put("STATE", "0");
			throw e;
		}
		return resultMap;
	}
	//管理员修改品牌信息
	@RequestMapping("/updatePP.action")
	public Map<String, Object> updatePP(String XmlData,
					HttpServletRequest request, HttpServletResponse response)
					throws Exception {
				Map<String, Object> hm = new HashMap<String, Object>();
				try {
					XmlData = JLTools.unescape(XmlData);
					cds = new DataSet(XmlData);
					String HBID = cds.getField("HBID", 0); // 公司ID ZCXX01 字段
					String PPMC = cds.getField("ppmc", 0); // 品牌名称
					String PPTP01 = cds.getField("PPTP01", 0);
					String sPPB01 = cds.getField("PPB01", 0);
					
					String w_ppqxSql = "UPDATE W_PPQX SET ENDTIME = '" + cds.getField("endTime", 0) + "', BZ = '" + cds.getField("spyj", 0) + "' "
							+ " WHERE PPB01 = '" + sPPB01 + "' AND ZCXX01 = '" + HBID + "'";
					Map row = getRow(w_ppqxSql, null, 0);
					execSQL(o2o, w_ppqxSql, row);
		
					// 删除W_PPFL表相关内容
					String w_ppflSql = "DELETE FROM W_PPFL WHERE ZCXX01 = '" + HBID + "' AND PPB01 = '" + sPPB01 + "'";
					Map ppflRow = getRow(w_ppflSql, null, 0);
					execSQL(o2o, w_ppflSql, ppflRow);
					String[] ppflList = cds.getField("ppfl", 0).split(",");
					for (int i = 0; i < ppflList.length; i++) {
						String sql2 = "INSERT INTO W_PPFL(ZCXX01,PPB01,SPFL01) VALUES('"
								+ HBID + "','" + sPPB01 + "','" + ppflList[i] + "')";
						Map row2 = getRow(sql2, null, 0);
						execSQL(o2o, sql2, row2);
					}
		
					MultipartHttpServletRequest mrRequest = (MultipartHttpServletRequest) request;
					// 品牌LOGO 上传
					MultipartFile logoFile = mrRequest.getFile("fileLogo");
					String path = JlAppResources.getProperty("path_pptp");
					String PPTP = "";
					if(logoFile != null){
						String logoFileName = logoFile.getOriginalFilename();
						String logoFileSuffix = logoFileName.substring(logoFileName.lastIndexOf("."));
						PPTP = HBID + "_" + PPMC + "_0" + logoFileSuffix; // 图片名称，店铺编码+图片类型+图片格式
						updateTP(path, PPTP, XmlData, request, logoFile, "fileLogo");
					}
					// PPB表更新
					String ppbSql = "UPDATE PPB SET PPB02 = '" + PPMC + "', ";
					if(!"".equals(PPTP)){
						ppbSql = ppbSql + "PPTP = '" + PPTP + "', ";
					}
					ppbSql = ppbSql + "PPB06 = UPPER(SUBSTRING(fn_getpy('" + PPMC + "'),1,1)), PPB08 = '" + cds.getField("jzrq", 0) + "', "
						  + " PPB07 =  '" + cds.getField("gsmc", 0) + "' "
						  + "WHERE ZCXX01 = '" + HBID + "' "
					  	  + "AND PPB01 = '" + sPPB01 + "'";
					Map rowPPB = getRow(ppbSql, null, 0);
					execSQL(o2o, ppbSql, rowPPB);
					
					// 商标注册授权证图片上传
					MultipartFile msgFile = mrRequest.getFile("fileMsg");
					if(msgFile != null){
						String msgFileName = msgFile.getOriginalFilename();
						String msgFileSuffix = msgFileName.substring(msgFileName.lastIndexOf("."));
						String PPTP02 = HBID + "_" + PPMC + "_" + PPTP01 + msgFileSuffix; // 图片名称，店铺编码+图片类型+图片格式
						String PPTP03 = path + PPTP02;
						String w_pptpSql = "UPDATE W_PPTP SET PPTP02 = '" + PPTP02 + "', "
									  + "PPTP03 = '" + PPTP03 + "', "
									  + "PPTP04 = NOW() "
									  + "WHERE ZCXX01 = '" + HBID + "' "
								  	  + "AND PPB01 = '" + sPPB01 + "' "
					  	  			  + "AND PPTP01 = '" + PPTP01 + "'";
						Map rowW_PPTP = getRow(w_pptpSql, null, 0);
						execSQL(o2o, w_pptpSql, rowW_PPTP);
						updateTP(path, PPTP02, XmlData, request, msgFile, "fileMsg");
					}
					hm.put("STATE", "1");
				} catch (Exception e) {
					e.printStackTrace();
					hm.put("STATE", "0");
					throw e;
				}
				return hm;
			}
	
	
				//审核品牌信息
				@SuppressWarnings("unchecked")
				@RequestMapping("/auditPPXX.action")
				public Map auditPPXX(String XmlData) throws Exception {
					Map hm = new HashMap();
					cds=new DataSet(XmlData);
					int bj = 0;
					if (!cds.getField("STATE", 0).equals("")) {
						if (cds.getField("STATE", 0).equals("1")) {
							bj = 1;
						}else if(cds.getField("STATE", 0).equals("2")){
							bj = 0;
						}
					}
					try {
						//先检查ppb品牌状态
						String sql = "select ppb04 from ppb where ppb01 = '"+cds.getField("PPB01", 0)+"'";
						Map count = queryForMap(o2o, sql); 
						Integer ppb04 = (Integer)count.get("ppb04");
						       sql=" UPDATE W_PPQX SET State ='"+cds.getField("STATE", 0)+"',BZ ='"+cds.getField("SPYJ", 0)+"' "+
						       	   " WHERE PPB01='"+cds.getField("PPB01", 0)+"' AND ZCXX01 = '"+ cds.getField("GSID", 0) +"'";
						Map row=getRow(sql, null, 0);
						execSQL(o2o, sql, row);
						       sql=" UPDATE PPB SET PPB04 = '"+cds.getField("STATE", 0)+"',PPB05='"+cds.getField("SPYJ", 0)+ "'," +
						       	   " YXBJ = '"+bj+"',PPB08='"+cds.getField("ETIME", 0)+"' WHERE PPB01='"+cds.getField("PPB01", 0)+"' AND ZCXX01 = '"+ cds.getField("GSID", 0) +"' ";
						execSQL(o2o, sql, row);
						//品牌申请为生产企业，更新当前品牌经销企业品牌状态为停用
						if (cds.getField("GSLX", 0).equals("43") && cds.getField("STATE", 0).equals("1")){
							String sql2="UPDATE w_ppqx SET State = 3 WHERE PPB01 = '"+cds.getField("PPB01", 0)+"'  AND (SELECT zcgs03 FROM w_zcgs WHERE zcxx01 = w_ppqx.zcxx01) = 42";
							execSQL(o2o, sql2, row); 
						}
						hm.put("STATE", 1);
						//审核通过时，对接ERP系统
						//未和ERP对接品牌ID为SQ开头,判断是否对接
						String ppb01 = cds.getField("PPB01", 0).substring(0, 2);
					if(ppb04 != 1 && cds.getField("STATE", 0).equals("1") && ppb01.equals("SQ")){
						Map map = pubService.getECSURL("JL");
						if(map.get("DJLX") != null){
							String ppSQL = "SELECT PPB02 PPMC,fn_getpy(PPB02) PPYW FROM PPB WHERE PPB01='"+cds.getField("PPB01", 0)+"'";
							Map ppMap = queryForMap(o2o,ppSQL);
							String ppid = JLTools.getID_X(PubFun.updateWBHZT(o2o,"PPB",1),6);
							String  upSql = " UPDATE PPB SET PPB01 = '"+ppid+"' WHERE PPB01 = '"+cds.getField("PPB01", 0)+"' ";
							execSQL(o2o, upSql, row);
									upSql = " UPDATE W_PPQX SET PPB01 = '"+ppid+"' WHERE PPB01 = '"+cds.getField("PPB01", 0)+"' ";
							execSQL(o2o, upSql, row);
									upSql = " UPDATE W_PPTP SET PPB01 = '"+ppid+"' WHERE PPB01 = '"+cds.getField("PPB01", 0)+"' ";
							execSQL(o2o, upSql, row);
									upSql = " UPDATE W_PPFL SET PPB01 = '"+ppid+"' WHERE PPB01 = '"+cds.getField("PPB01", 0)+"' ";
							execSQL(o2o, upSql, row);
							if(map.get("DJLX").equals("V9")){
								String returnStr = v9BasicData.createSPPP(ppMap, map);
								JSONObject jsonObject = JSONObject.fromObject(returnStr);
								JSONObject returnData =(JSONObject) jsonObject.get("data");
								if(!returnData.getString("JL_State").equals("1")){
									throw new Exception("品牌对接V9失败，"+returnData.get("JL_ERR").toString());
								}
							}
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
					hm.put("STATE", 0);
				} 
				return hm;
			}
				
		/**
		 * 管理员审核品牌时判断其品牌是否存在,生产和经销判断规则不同		
		 * @param jsonData
		 * @return
		 * @throws Exception
		 */
				@RequestMapping("/selectPP.action")
				public Map<String, Object> selectPP(String jsonData) throws Exception{
					Map<String, Object> resultMap = new HashMap<String, Object>();
					try {
						jsonData = JLTools.unescape(jsonData);
						cds = new DataSet(jsonData);
						String ppName = cds.getField("PPMC", 0);
						String SQGSLX = cds.getField("SQGSLX", 0);
						String SQGSID = cds.getField("SQGSID", 0);
						//申请企业为生产企业,检查是否已存在有生产企业拥有该品牌
						if (SQGSLX.equals("43")){
							String sql = "SELECT COUNT(1) COUNT FROM PPB WHERE PPB02 = '" + ppName + "' AND PPB04 = 1" +
									" AND (SELECT zcgs03 FROM w_zcgs WHERE zcxx01=ppb.zcxx01) = 43 ";
							int count = queryForInt(o2o, sql);
							resultMap.put("COUNT", count);
							//若该品牌存在于生产企业下,判断其申请企业和所属企业是否是一家
							if (count == 1){
									sql = "SELECT A.zcxx01 FROM w_ppqx A LEFT JOIN PPB B ON A.PPB01 = B.PPB01  "+
										  " WHERE PPB02 = '" + ppName + "' AND (SELECT zcgs03 FROM w_zcgs WHERE zcxx01=B.zcxx01) = 43 ";
									Map map = queryForMap(o2o, sql);
									String Sid = (String) map.get("zcxx01");
									if (Sid.equals(SQGSID)) {
										resultMap.put("COUNT", "0");
									}
							}
							resultMap.put("STATE", "1");
							//申请企业为经销企业.检查是否存在该品牌,若存在,经销企业无法领取
						}else if(SQGSLX.equals("42")){
							String sql = "SELECT COUNT(1) COUNT FROM PPB WHERE PPB02 = '" + ppName + "' AND PPB04 = 1 " +
									     "AND (SELECT zcgs03 FROM w_zcgs WHERE zcxx01=ppb.zcxx01) = 43 ";
							int count = queryForInt(o2o, sql);
							resultMap.put("COUNT", count);
							resultMap.put("STATE", "1");
						}
					} catch (Exception e) {
						resultMap.put("STATE", "0");
						throw e;
					}
					return resultMap;
				}
			
	/**
	 * 查询品牌权限表数据是否存在,如果存在不允许添加数据
	 * @param jsonData
	 * @return
	 * @throws Exception
	 */
	@RequestMapping("/selectPPQX_Count.action")
	public Map<String, Object> selectPPQX_Count(String jsonData) throws Exception{
		Map<String, Object> resultMap = new HashMap<String, Object>();
		try {
			cds = new DataSet(jsonData);
			String PPB01 = cds.getField("PPB01", 0);
			String ZCXX01 = cds.getField("ZCXX01", 0);
			String sql = "SELECT COUNT(1) COUNT FROM W_PPQX WHERE PPB01 = '" + PPB01 + "' AND ZCXX01 = '" + ZCXX01 + "'";
			int count = queryForInt(o2o, sql);
			resultMap.put("COUNT", count);
			resultMap.put("STATE", "1");
		} catch (Exception e) {
			resultMap.put("STATE", "0");
			throw e;
		}
		return resultMap;
	}
	
	/**
	 * 查询当前卖家可发布的品牌
	 * @param jsonData
	 * @return
	 * @throws Exception
	 */
	@RequestMapping("/selectFBSPPP.action")
	public Map<String, Object> selectFBSPPP(String jsonData) throws Exception{
		Map<String, Object> resultMap = new HashMap<String, Object>();
		try {
			cds = new DataSet(jsonData);
			String SPFL01 = cds.getField("SPFL01_CODE", 0);
			String ZCXX01 = cds.getField("ZCXX01", 0);
			String sql = "SELECT A.PPB01, B.PPB02 FROM W_PPQX A "
					+ "LEFT JOIN PPB B ON A.PPB01 = B.PPB01 "
					+ "LEFT JOIN W_PPFL C ON A.PPB01 = C.PPB01 AND A.ZCXX01 = C.ZCXX01 "
					+ "WHERE C.SPFL01 = '" + SPFL01 + "' "
					+ "AND A.ZCXX01 = '" + ZCXX01 + "' "
					+ "AND A.STATE = 1 "
					+ "AND B.PPB04 = 1 "
					+ "AND B.YXBJ = 1";
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
