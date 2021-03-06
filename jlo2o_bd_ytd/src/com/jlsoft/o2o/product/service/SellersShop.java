package com.jlsoft.o2o.product.service; 

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import com.jlsoft.framework.JLBill;
import com.jlsoft.framework.dataset.DataSet;

/**
 * 
 * @author 齐俊宇
 * @version 1.0
 * @date 2015年10月30日
 *
 */

@Controller
@RequestMapping("/SellersShop")
public class SellersShop extends JLBill{

	/**
	 * 查询店铺的基本信息
	 * 	ZCXX01 查询条件店铺ID
	 * 	ZCXX70 店铺名称
	 * 	ZCXX14 注册时间
	 *  LX 经营模式
	 *  ZCXX08 经营地址
	 *  ZCXX03 联系人
	 *  ZCXX06 电    话
	 *  ZCXX09 邮    箱
	 *  PROVINCE 省
	 *  CITY 市
	 *  DPTP02 店铺图片 3营业执照 4组织机构 5税务登记证 7一般纳税人证明
	 * @param jsonData
	 * @return
	 * @throws Exception
	 */
	@RequestMapping("/selectSellersInfo.action")
	public Map selectSellersMesg(String jsonData) throws Exception {
		Map resultMap = new HashMap();
		try {
			cds = new DataSet(jsonData);
			// 公司的注册信息ZCXX01
			String zcxx01 = cds.getField("ZCXX01", 0);
			// 查询公司注册的基本信息和查询店铺图片信息
			String zcxxSql = "SELECT A.ZCXX01 ZCXX01, A.ZCXX02 ZCXX02, A.ZCXX07 ZCXX07, A.ZCXX70 ZCXX70, DATE(A.ZCXX14) ZCXX14, "
					   + "(SELECT LX FROM W_GSLX WHERE GSID = A.ZCXX01) LX, "
					   + "A.ZCXX08 ZCXX08, A.ZCXX03 ZCXX03, "
					   + "A.ZCXX06 ZCXX06, A.ZCXX09 ZCXX09, B.DPTP02 DPTP02 "
					   + "FROM W_ZCXX A "
					   + "LEFT JOIN W_DPTP B ON A.ZCXX01 = B.ZCXX01 AND B.DPTP01 IN (3, 4, 5, 7)"
					   + "WHERE A.ZCXX01 = '" + zcxx01 + "'";
			List dpxxList = queryForList(o2o, zcxxSql);
			resultMap.put("dpxxList", dpxxList);
			Map dqxxMap = (Map) dpxxList.get(0);
			
			// 查询大区相关信息, 省(PROVINCE) 和市(CITY)
//			String citySql = "SELECT DQBZM_DQBZM01, DQBZM02 CITY "
//						   + "FROM DQBZM WHERE DQBZM01 = ("
//						   + "SELECT DQBZM_DQBZM01 FROM DQBZM WHERE DQBZM01 = '" + dqxxMap.get("ZCXX07") + "') ";
//			resultMap.putAll(queryForMap(o2o, citySql));
//			String provinceSql = "SELECT DQBZM02 PROVINCE FROM DQBZM WHERE DQBZM_DQBZM01 = '" + resultMap.get("DQBZM_DQBZM01") + "'";
//			resultMap.putAll(queryForMap(o2o, provinceSql));
			
			// 查询品牌图片信息
			//NineDragon 2015/11/17 修改以W_SPXX 为主表查询 店铺的品牌图片
			/*String pptpSql = "SELECT P.PPB01,P.PPB02,T.PPTP02,T.PPB01  "
						   + "FROM W_SPXX S "
						   + "LEFT JOIN W_SPGL G ON S.SPXX01 = G.SPXX01 "
						   + "LEFT JOIN PPB P ON S.PPB01 = P.PPB01  "
						   + "LEFT JOIN W_PPTP T ON P.PPB01 = T.PPB01 "
						   + "WHERE G.ZCXX01 = '" + zcxx01 + "' "
						   + "AND T.PPTP01 = 0 "
						   + "AND P.PPB04 = 1 "
						   + "AND P.YXBJ = 1 "
						   + "GROUP BY P.PPB01 "
						   + "LIMIT 4";*/
			// 20160106 齐俊宇 update 修改查询品牌logo的方式 BEGIN
			String pptpSql = "SELECT P.PPB01, P.PPB02, P.PPTP PPTP02 "
					+ "FROM W_PPQX Q "
					+ "LEFT JOIN PPB P ON Q.PPB01 = P.PPB01 WHERE Q.ZCXX01 = '" + zcxx01 + "' "
					+ "AND P.PPB04 = 1 AND P.YXBJ = 1 AND Q.STATE = 1 "
					+ "GROUP BY P.PPB01 LIMIT 4 "
					+ "UNION "
					+ "SELECT P.PPB01, P.PPB02, P.PPTP PPTP02 "
					+ "FROM PPB P "
					+ "LEFT JOIN W_GSGXPP GP ON P.PPB01 = GP.PPB01 "
					+ "LEFT JOIN W_GSGX G ON G.HBID = GP.HBID WHERE G.HBID = '" + zcxx01 + "' "
					+ "AND P.PPB04 = 1 AND P.YXBJ = 1 AND G.STATE = 1 "
					+ "GROUP BY P.PPB01 LIMIT 4";
			// 20160106 齐俊宇 update 修改查询品牌logo的方式 END
			List pptpList = queryForList(o2o, pptpSql);
			resultMap.put("pptpList", pptpList);
			resultMap.put("STATE", "1");
		} catch (Exception e) {
			resultMap.put("STATE", "0");
			throw e;
		}
		return resultMap;
	}
	
	/**
	 * 查询企业概述
	 * 	ZCXX01 查询条件店铺ID
	 *  ZCXX02 企业名称
	 *  ZCXX08 经营地址
	 *  ZCXX03 联系人
	 *  ZCXX06 电    话
	 *  ZCXX09 邮    箱
	 *  ZCXX28 纳税人识别号
	 *  SBM   厂商识别码 外联 W_ZCGSSBM
	 *  HYGLM 行业管理码 外联 W_ZCGS
	 *  DPTP02 店铺图片 3营业执照 7一般纳税人证明 外联W_DPTP
	 * @param jsonData
	 * @return
	 * @throws Exception
	 */
	@RequestMapping("/selectQYGS.action")
	public Map selectQYGS(String jsonData) throws Exception {
		Map resultMap = new HashMap();
		try {
			cds = new DataSet(jsonData);
			// 公司的注册信息ZCXX01
			String zcxx01 = cds.getField("ZCXX01", 0);
			// 查询公司注册的基本信息
			String zcxxSql = "SELECT A.ZCXX02 ZCXX02, A.ZCXX08 ZCXX08, A.ZCXX03 ZCXX03, "
					   + "A.ZCXX06 ZCXX06, A.ZCXX09 ZCXX09,A.ZCXX28 ZCXX28, "
					   + "B.SBM SBM, C.HYGLM HYGLM, D.DPTP02 DPTP02, D.DPTP05 "
					   + "FROM W_ZCXX A "
					   + "LEFT JOIN W_ZCGSSBM B ON B.ZCXX01 = A.ZCXX01 "
					   + "LEFT JOIN W_ZCGS C ON C.ZCXX01 = A.ZCXX01 "
					   + "LEFT JOIN W_DPTP D ON D.ZCXX01 = A.ZCXX01 AND D.DPTP01 IN (3, 7) "
					   + "WHERE A.ZCXX01 = '" + zcxx01 + "'";
			List dpxxList = queryForList(o2o, zcxxSql);
			resultMap.put("dpxxList", dpxxList);
			resultMap.put("STATE", "1");
		} catch (Exception e) {
			resultMap.put("STATE", "0");
			throw e;
		}
		return resultMap;
	}
	
	/**
	 * 查询店铺的公司名称, 联系地址, 联系人, 电话, 邮箱 在联系我们页面显示
	 * 这是之前使用的方法
	 * @param XmlData
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	@RequestMapping("/selectDPInfo.action")
	public Map selectDPInfo(String XmlData) throws Exception{
		Map map = new HashMap();
		cds=new DataSet(XmlData);
		try {
			String GSID=cds.getField("gsid",0);
			String sql="SELECT ZCXX02,ZCXX06,ZCXX08,ZCXX09,ZCXX26,Longitude,Latitude FROM W_ZCXX WHERE ZCXX01='" + GSID + "'";
			Map list=queryForMap(o2o, sql);
			map.put("list", list);
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
		return map;
	}
	
	/**
	 * 企业概述页面显示的8个品牌信息
	 *  PPB01 品牌编号
	 *  PPB02 品牌名称
	 *  PPTP02 品牌图片
	 * @param jsonData
	 * @return
	 * @throws Exception
	 */
	@RequestMapping("/selectDPPPTP.action")
	public Map<String, Object> selectDPPPTP(String jsonData) throws Exception {
		Map<String, Object> resultMap = new HashMap<String, Object>();
		try {
			cds = new DataSet(jsonData);
			String zcxx01 = cds.getField("ZCXX01", 0);
			String sql = "";
			// 20160106 齐俊宇 update 修改查询品牌logo的方式 BEGIN
			/*sql = "SELECT P.PPB01, P.PPB02, P.PPTP PPTP02 "
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
				+ "ORDER BY P.PPB01 "
				+ "LIMIT 12";*/
			sql = "SELECT P.PPB01, P.PPB02, P.PPTP PPTP02 "
				+ "FROM W_PPQX Q "
				+ "LEFT JOIN PPB P ON Q.PPB01 = P.PPB01 WHERE Q.ZCXX01 = '" + zcxx01 + "' "
				+ "AND P.PPB04 = 1 AND P.YXBJ = 1 AND Q.STATE = 1 "
				+ "GROUP BY P.PPB01 LIMIT 12 "
				+ "UNION "
				+ "SELECT P.PPB01, P.PPB02, P.PPTP PPTP02 "
				+ "FROM PPB P "
				+ "LEFT JOIN W_GSGXPP GP ON P.PPB01 = GP.PPB01 "
				+ "LEFT JOIN W_GSGX G ON G.HBID = GP.HBID WHERE G.HBID = '" + zcxx01 + "' "
				+ "AND P.PPB04 = 1 AND P.YXBJ = 1 AND G.STATE = 1 "
				+ "GROUP BY P.PPB01 LIMIT 12";
			// 20160106 齐俊宇 update 修改查询品牌logo的方式 END
			List ppxxList = queryForList(o2o, sql);
			resultMap.put("ppxxList", ppxxList);
			resultMap.put("STATE", "1");
		} catch (Exception e) {
			resultMap.put("STATE", "0");
			throw e;
		}
		
		return resultMap;
	}
	
	/**
	 * 店铺轮播图
	 * FILE01 公司编码
	 * FILE02 附件名称
	 * FILE03 附件路径
	 * FILE04 附件类型
	 * FILE05 图片对应的URL路径
	 * FILE06 顺序号
	 * @param jsonData
	 * @return
	 * @throws Exception
	 */
	@RequestMapping("/selectDPLBT.action")
	public Map<String, Object> selectDPLBT(String jsonData) throws Exception {
		Map<String, Object> resultMap = new HashMap<String, Object>();
		try {
			cds = new DataSet(jsonData);
			String file01 = cds.getField("ZCXX01", 0);
			String sql = "";
			sql = "SELECT FILE01, FILE02, FILE03, FILE05, FILE06 FROM W_FILE WHERE FILE01 = '" + file01 + "'";
			List dplbtList = queryForList(o2o, sql);
			resultMap.put("dplbtList", dplbtList);
			resultMap.put("STATE", "1");
		} catch (Exception e) {
			resultMap.put("STATE", "0");
			throw e;
		}
		
		return resultMap;
	}
	
	/**
	 * 收藏店铺
	 * @param jsonData
	 * @return
	 * @throws Exception
	 */
	@RequestMapping("/insertSCDP.action")
	public Map<String, Object> insertSCDP(String jsonData) throws Exception {
		Map<String, Object> resultMap = new HashMap<String, Object>();
		try {
			cds = new DataSet(jsonData);
			String sql = "";
			sql = "INSERT INTO OWNCOLLECT(ZCXX01, COLLECTTYPE, COLLECTINFO) VALUES('" + cds.getField("ZCXX01", 0) + "', 1, '" + cds.getField("COLLECTINFO", 0) + "')";
			execSQL(o2o, sql, resultMap);
			resultMap.put("STATE", "1");
		} catch (Exception e) {
			resultMap.put("STATE", "0");
			throw e;
		}
		
		return resultMap;
	}
}
 