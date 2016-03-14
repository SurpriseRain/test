package com.jlsoft.o2o.pub.service;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import com.jlsoft.framework.JLBill;
import com.jlsoft.utils.JLTools;

@Controller
@RequestMapping("/PubService")
public class PubService  extends JLBill{
	/**
	 * @todo 返回问题订单数量
	 * @param DJLX
	 * @param YWDH
	 * @return
	 */
	public int getWTDJSL(int DJLX,String YWDH){
		String sql = "SELECT COUNT(0) FROM W_WTDJ WHERE DJLX="+DJLX+" AND YWDH='"+YWDH+"'";
		return queryForInt(o2o,sql);
	}
	
	/**
	 * @todo 查询问题订单状态
	 * @param YWDH
	 * @return
	 */
	public Map querWTDJ(int DJLX,String YWDH){
		String sql = "SELECT CLZT,DJZT FROM W_WTDJ WHERE DJLX="+DJLX+" AND YWDH='"+YWDH+"'";
		return (Map)queryForMap(o2o,sql);
	}
	
	/**
	 * @todo 写入问题订单表
	 * @param DJLX 单据类型
	 * @param YWDH 业务单号
	 * @param CLZT 处理状态
	 * @param DJZT 单据状态
	 * @throws Exception 
	 */
	public void insertW_WTDJ(int DJLX,String YWDH,int CLZT,int DJZT) throws Exception{
		Map map = new HashMap();
		String sql = "INSERT INTO W_WTDJ(DJLX,YWDH,CLZT,DJZT) VALUES("+DJLX+",'"+YWDH+"',"+CLZT+","+DJZT+")";
		execSQL(o2o,sql,map);
	}
	
	/**
	 * @todo 更新问题订单状态
	 * @param YWDH 因为单号
	 * @param CLZT 处理状态
	 * @param DJZT 单据状态
	 * @throws Exception 
	 */
	public void updateW_WTDJ(int DJLX,String YWDH,int CLZT,int DJZT) throws Exception{
		Map map = new HashMap();
		String sql = "UPDATE W_WTDJ SET CLZT="+CLZT+",DJZT="+DJZT+" WHERE DJLX="+DJLX+" AND YWDH='"+YWDH+"'";
		execSQL(o2o,sql,map);
	}
	
	/**
	 * @todo 根据不同公司获取不同ECS对应URL
	 * @param ZCXX01
	 * @return
	 */
	public Map getECSURL(String ZCXX01){
		Map map = new HashMap();
		String sql = "SELECT ifnull(ZCXX58,'') DJLX,ifnull(ZCXX59,'') URL,ZCXX60 UserName,ZCXX61 PassWord FROM W_ZCXX WHERE ZCXX01='"+ZCXX01+"'";
		map = (Map)queryForMap(o2o,sql);
		return map;
	}
	
	/**
	 * @todo 获取注册企业物流相关信息
	 * @param ZCXX01
	 * @return
	 */
	@RequestMapping("/getZCGSWL")
	public Map getZCGSWL(String ZCXX01){
		Map map = new HashMap();
		if(JLTools.getCurConf(1) == 1){
			String sql = "SELECT APP_KEY,APP_SECRET FROM W_ZCGSWL WHERE ZCXX01='"+ZCXX01+"'";
			map = (Map)queryForMap(o2o,sql);
			if(map == null){
				map = new HashMap();
			}
			map.put("CurConf", "1");
		}else{
			map.put("CurConf", "0");
		}
		return map;
	}
	
	/**
	 * @todo 获取销售订单信息
	 * @param XSDD01
	 * @return
	 */
	public Map getXSDD(String XSDD01){
		Map map = new HashMap();
		String sql = "SELECT ZTID FROM W_XSDD WHERE XSDD01='"+XSDD01+"'";
		map = (Map)queryForMap(o2o,sql);
		return map;
	}
	
	/**
	 * @todo 获取出库单信息
	 * @param CKDH
	 * @return
	 */
	public Map getCKD(String CKDH){
		Map map = new HashMap();
		String sql = "SELECT ZCXX01 FROM W_CKD WHERE CKDH='"+CKDH+"'";
		map = (Map)queryForMap(o2o,sql);
		return map;
	}
	
	/**
	 * @todo 获取退货单信息
	 * @param THDH
	 * @return
	 */
	public Map getTHD(String THDH){
		Map map = new HashMap();
		String sql = "SELECT ZTID FROM W_THD WHERE THDH='"+THDH+"'";
		map = (Map)queryForMap(o2o,sql);
		return map;
	}
	
	/**
	 * @todo 获取商品信息
	 * @param barcode
	 * @return
	 */
	public Map getSPXX(String barcode){
		Map map = new HashMap();
		String sql = "SELECT ZCXX01 FROM W_GOODS WHERE BARCODE='"+barcode+"'";
		map = (Map)queryForMap(o2o,sql);
		return map;
	}
	
	/**
	 * 插入入库单
	 * @param RKDH
	 * @param CKDH
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings("rawtypes")
	public Map selectCKDForRKD(String CKDH, String ZCXX01) throws Exception{
		Map resultMap = new HashMap();
		try {
			String gsidSql = "SELECT DISTINCT A.ZCXX01, A.SHGS, B.TYPE, A.SHCK "
					+ "FROM W_CKD A LEFT JOIN CK B "
					+ "ON A.SHCK = B.CK01 "
					+ "LEFT JOIN W_GSCK C "
					+ "ON B.CK01 = C.CK01 "
					+ "WHERE A.CKDH = '" + CKDH + "' "
					+ "AND C.ZCXX01 = '" + ZCXX01 + "'";// 20160108 齐俊宇 update 增加ZCXX01 条件查询
			Map gsidMap = queryForMap(o2o, gsidSql);
			if("1".equals(gsidMap.get("TYPE").toString())){
				resultMap.put("ZCXX01", gsidMap.get("ZCXX01").toString());
			} else {
				resultMap.put("ZCXX01", gsidMap.get("SHGS").toString());
			}
			resultMap.put("SHCK", gsidMap.get("SHCK").toString());
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
		return resultMap;
	}
	
}
