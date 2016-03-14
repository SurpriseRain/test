package com.jlsoft.o2o.product.service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import com.jlsoft.framework.JLBill;
import com.jlsoft.framework.dataset.DataSet;
import com.jlsoft.utils.JLTools;
import com.jlsoft.utils.JlAppResources;
/**
 * 楼层
 * @author asus
 *
 */
@Controller
@RequestMapping("/oper_floor")
public class Oper_floor extends JLBill{
	
	//上传图片 
	@RequestMapping("/newlcppupload.action")
	public Map newlcppupload(HttpServletRequest request , String XmlData) throws Exception{
		String XmlDatas =JLTools.unescape(XmlData);	
		cds =new DataSet(XmlDatas);			
		MultipartHttpServletRequest mrRequest=(MultipartHttpServletRequest)request;	
		List<MultipartFile> listFile= mrRequest.getFiles("files");				
		Map map=uploadFile(mrRequest, XmlDatas, listFile);	
		return map;
	}
	
	public Map<String, Object> uploadFile(HttpServletRequest request,String XmlData,List<MultipartFile> listFile) throws Exception{
		cds =new DataSet(XmlData);	
		Map<String, Object> hm=new HashMap<String, Object>();		
		DiskFileItemFactory factory=new DiskFileItemFactory();		
		ServletFileUpload upload=new ServletFileUpload(factory);		
		if(!upload.isMultipartContent(request)){
			hm.put("Flag", "error");
			return hm;
		}		
		for(int i=0;i<listFile.size();i++){
			MultipartFile file=listFile.get(i);	
			try {		
				InputStream in=file.getInputStream();						
				//
				String[] pathFull= request.getClass().getClassLoader().getResource("").getPath().split("/WEB-INF/");
				//
				String path=pathFull[0]+JlAppResources.getProperty("LCTP_ROOT_PATH");
				System.out.println("path---"+path);

				String oldFileName=file.getOriginalFilename();								
				if(!"".equals(oldFileName) && oldFileName != null){
					
					System.out.println("oldFileName---"+oldFileName);					
					File filePath=new File(path);					
					if(!filePath.exists()){						
						System.out.println("aaa---");						
						filePath.mkdirs();
						System.out.println("创建目录为："+filePath);
					}												
					FileOutputStream out=new FileOutputStream(filePath+oldFileName);										
					byte buffer[] = new byte[1024];
					int len = 0;
					while((len=in.read(buffer))>0){
						out.write(buffer,0,len);
					}				
					in.close();
					out.close();
					//String url="/b2b/tpgl/lctp/"+oldFileName;
					String url=JlAppResources.getProperty("LCTP_ROOT_PATH")+oldFileName;
					//修改时上传 
					if(cds.getField("TYPE", i).equals("updatalcpp")){   
						hm= updatelcpp(XmlData,url);											
					}else{						
						hm = newlcppsql(XmlData,oldFileName,url,i);
					}
				}				
			} catch (IOException e) {
				e.printStackTrace();
				hm.put("Flag", "error");
			} catch (Exception e) {
				e.printStackTrace();
				hm.put("Flag", "error");

			}		
		}	
		return hm;
	}
	
	    //新增楼层品牌 
	public Map newlcppsql(String XmlData,String oldFileName,String url,int m) throws Exception{
		cds=new DataSet(XmlData);
		System.out.println("XmlData-"+cds.getField("LCPP05", m));	
		Map map=new HashMap();	
		String sql="DELETE FROM W_LCPP W WHERE W.LCFL01="+cds.getField("LCFL01", m)+" AND W.LCPP05="+cds.getField("LCPP05", m);
		Map row = getRow(sql, null, 0);
		execSQL(o2o, sql, row); 
		int num=JLTools.getJLBH(o2o, "W_LCPP", 1);					
		String sql2="INSERT INTO W_LCPP(LCFL01,LCPP01,LCPP02,lcpp03,LCPP04,LCPP05,OPERTIME)" +
		" VALUES("+cds.getField("LCFL01", m)+","+num+",'"+cds.getField("LCPP02", m)+"','"+url+"','"+cds.getField("LCPP04", m).replace("&", "'||chr(38)||'").replace("?", "'||chr(63)||'")+"',"+cds.getField("LCPP05", m)+",SYSDATE)";		
		Map row2 = getRow(sql2, null, 0);		
		int i= execSQL(o2o, sql2, row2); 
		if(i>0){
			map.put("Flag", "success");
		}else {
			map.put("Flag", "error");
		}	
		return map;
	}
	
	//删除楼层品牌 
	@RequestMapping("/deletelcpp.action")
	public Map deletelcpp(String XmlData) throws Exception{
		System.out.println(XmlData);		
		cds=new DataSet(XmlData);
		Map map=new HashMap();
		String sql="DELETE FROM W_LCPP W WHERE W.LCFL01="+cds.getField("LCFL01", 0)+" AND W.LCPP05="+cds.getField("LCPP05", 0);
		Map row = getRow(sql, null, 0);
		int i=execSQL(o2o, sql, row); 
		if(i>0){
			map.put("STATE","1");
		}else {
			map.put("STATE","0");
		}
		return map;
	}
		
	//修改楼层品牌
	@RequestMapping("/updatelcpp.action")
	public Map updatelcpp(String XmlData,String url) throws Exception{			
		String XmlDatas =JLTools.unescape(XmlData);			
		System.out.println("xmldata--"+XmlDatas);			
		cds=new DataSet(XmlDatas);	
		Map map=new HashMap();	
		
		String sql="UPDATE W_LCPP SET " +
				"LCPP02='"+cds.getField("LCPP02", 0)+"' ," +
				"LCPP04='"+cds.getField("LCPP04", 0).replace("&", "'||chr(38)||'").replace("?", "'||chr(63)||'")+"'  ," +
				"LCPP03='"+url+"'  "+
				"WHERE LCFL01='"+cds.getField("LCFL01", 0)+"'  " +
				"AND LCPP05='"+cds.getField("LCPP05", 0)+"'";		
		Map row = getRow(sql, null, 0);
		System.out.println(sql);
		int i=execSQL(o2o, sql, row); 
		if(i>0){
			map.put("Flag", "success");
		}else {
			map.put("Flag", "error");
		}
		return map;
	}
	
	/**
	 * 
	 * @param XmlData
	 * @return
	 * @throws Exception
	 */
	@RequestMapping("/selectFloorGoods.action")
	public List<Map<String, Object>> selectFloorGoods(String XmlData) throws Exception{
		cds =new DataSet(XmlData);
		String hbid = "";
		if(JlAppResources.getProperty("ROADMAP").equals("4")) {
			hbid = cds.getField("HBID", 0);
		}
		List<Map<String, Object>> lcList=selectFloor(XmlData);
		cds=new DataSet(XmlData);
		String LCFL01=cds.getField("LCFL01", 0);
		int limitNum=-1;
		if(null!=LCFL01&&!"".endsWith(LCFL01)){
			limitNum=Integer.parseInt(LCFL01.split(",")[1]);
		}
		for (Map<String, Object> map : lcList) {
			String lcfl01=map.get("LCFL01").toString();
			map.put("fllx", selectFloorClassification(lcfl01,limitNum));
			map.put("lcspfl", selectFloorGoodsClassification(lcfl01, hbid,limitNum));
			map.put("lcpp", selectFloorBrand(lcfl01));
		}
		System.out.println("lcList---"+lcList);
		return lcList;
	}
	
	/**
	 * 查询楼层分类
	 * @param ZTFB01
	 * @throws Exception 
	 */
	@SuppressWarnings("unchecked")
	public List<Map<String, Object>> selectFloor(String XmlData) throws Exception{
		cds=new DataSet(XmlData);
		String LCFL01=cds.getField("LCFL01", 0);
		String sql="";

		if(null==LCFL01||"".equals(LCFL01)){
			sql="SELECT LCFL01, LCFL02 FROM W_LCFL WHERE ZTFB01 = "+cds.getField("ZTFB01", 0)+" order by LCFL01 limit 5";
		}else{
			sql="SELECT LCFL01, LCFL02 FROM W_LCFL WHERE ZTFB01 = "+cds.getField("ZTFB01", 0)+" and LCFL01='"+LCFL01.split(",")[0]+"'";
		}
		List<Map<String, Object>> lcList=queryForList(o2o, sql);
		if(lcList==null){
			lcList=new ArrayList<Map<String,Object>>();
			Map map=new HashMap<String, Object>();
			map.put("message", "查询失败");
			lcList.add(map);
		}
		return lcList;
	}
	
	/**
	 * 
	 * @param XmlData
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	public List<Map<String, Object>> selectFloorClassification(String lcfl,int limitnum) throws Exception{
		String sql="";
		if(-1!=limitnum){
			 sql=
					"SELECT A.SPFL01, B.SPFL02\n" +
					"  FROM W_LCFLITEM A, SPFL B\n" + 
					" WHERE A.SPFL01 = B.SPFL01\n" + 
					"   AND LCFL01 = "+lcfl+" LIMIT "+limitnum;
		}else{
			 sql=
					"SELECT A.SPFL01, B.SPFL02\n" +
					"  FROM W_LCFLITEM A, SPFL B\n" + 
					" WHERE A.SPFL01 = B.SPFL01\n" + 
					"   AND LCFL01 = "+lcfl+" LIMIT 11";
		}
		List<Map<String, Object>> spflNameList=queryForList(o2o, sql);
		return spflNameList;
	}
	
	public List<Map<String, Object>> selectFloorGoodsClassification(String lcfl01,String hbid,int limitnum){
		String sql=
			"SELECT LCSPLX01, LCSPLX02 FROM W_LCSPLX WHERE LCFL01 = "+lcfl01+" LIMIT 5";
		String spSql=
				"SELECT DISTINCT A.SPXX01  SPXX01,\n" +
				"       B.SPXX02  SPXX02,\n" + 
				"       B.SPXX04  SPMC,\n" + 
				"       E.SPJGB01 SPJG,\n" + 
				"       E.SPJGB05 SPJGB01,\n" + 
				"       E.SPJGB02 SPJGB02,\n" + 
				"       E.SPJGB05 SPJGB05,\n" + 
				"       F.SPGL04  SPGL04,\n" +
				"       F.CKSP12  SPSX, \n" +
				"       E.SPJGB02 SCJG,\n" + 
				"       A.ZCXX01  ZCXX01,\n" +
				"       C.ZCXX02  ZCXX02,\n" +
				"		(SELECT SPTP02 FROM W_SPTP WHERE ZCXX01=F.ZCXX01 AND SPXX01=F.SPXX01 AND SPTP01=1) SPTP02 \n"+
				"  FROM W_LCSP A, W_SPXX B, W_ZCXX C, W_LCSPLX D, W_SPJGB E, W_SPGL F\n" + 
				" WHERE A.SPXX01 = B.SPXX01\n" + 
				"   AND A.ZCXX01 = C.ZCXX01\n" + 
				"   AND A.LCSPLX01 = D.LCSPLX01\n" + 
				"   AND A.SPXX01 = E.SPXX01\n" + 
				"   AND A.ZCXX01 = E.ZCXX01\n" + 
				"   AND A.SPXX01 = F.SPXX01\n" + 
				"   AND F.SPGL12 IN ('1', '3')\n" + 
				"   AND A.LCSPLX01 = ";
		List<Map<String, Object>> list=queryForList(o2o, sql);
		for (Map<String, Object> map : list) {
			String queryfinalSql="";
			if(limitnum==-1){
				queryfinalSql=spSql+map.get("LCSPLX01")+" ORDER BY A.OPERTIME DESC  LIMIT 9";
			}else{
				queryfinalSql=spSql+map.get("LCSPLX01")+" ORDER BY A.OPERTIME DESC  LIMIT "+limitnum;
			}
			List<Map<String, Object>> lcsp = queryForList(o2o, queryfinalSql);
			map.put("lcsp", lcsp);
		}
		if(JlAppResources.getProperty("ROADMAP").equals("4")){
			list = getAllSpxx(list,hbid);
		}
		return list;
	}

	public List getAllSpxx(List<Map<String, Object>> list,String hbid){
		String sql1 = "SELECT ZCXX01, SPXX01, KHSPJGI01 FROM W_KHGX A, W_KHSPJGB B WHERE A.ZTID = B.ZCXX01 AND A.KHLX01 = B.KHLX01 AND A.HBID = '"+hbid+"'";
		List<Map<String, Object>> hbidlist = queryForList(o2o, sql1);
		for (Map<String, Object> map : list) {
			List<Map<String, Object>> lcsp = (List) map.get("lcsp");
			for (Map<String, Object> map1 : hbidlist) {
				String ZCXX01 = map1.get("ZCXX01").toString();
				String SPXX01 = map1.get("SPXX01").toString();
				String KHSPJGI01 = map1.get("KHSPJGI01").toString();
				for (Map<String, Object> map2 : lcsp) {
					String zcxx01 = map2.get("ZCXX01").toString();
					String spxx01 = map2.get("SPXX01").toString();
					if (ZCXX01.equals(zcxx01) && SPXX01.equals(spxx01)) {
						map2.put("KHSPJGI01", KHSPJGI01);
					}
				}
			}
		}
		return list;
	}

	//发布楼层商品
	@SuppressWarnings("unchecked")
	@RequestMapping("/insertLcsp.action")
	public Map<String, Object> insertLcsp(String XmlData) throws Exception{
		cds=new DataSet(XmlData);
		Map map=new HashMap();
		JSONArray jsonList=JSONArray.fromObject(XmlData);
//		JSONArray ja=JSONArray.fromObject(cds.getField("lcfl02", 0));
//		JSONObject jo;
		for (int i = 0; i <jsonList.size() ; i++) {
			//楼层商品类型LCSPLX没有，全部默认成LCFL01
		String sql="INSERT INTO W_LCSP(LCFL01,LCSPLX01,SPXX01,ZCXX01,OPERTIME) VALUES(lcfl01?,lcfl01?,SPXX01?,ZTID?,NOW())";
		Map row=getRow(sql, null, i);
		int j =execSQL(o2o, sql, row);
		if (j == 0) {
			map.put("STATE", "0");
		} else {
			map.put("STATE", "1");
		}
		}
		return map;
	}
	
	//修改楼层商品
	@SuppressWarnings("unchecked")
	@RequestMapping("/updateLcsp.action")
	public Map<String, Object> updateLcsp(String XmlData) throws Exception{
		cds=new DataSet(XmlData);
		Map map=new HashMap(); 
		String sql="UPDATE W_LCSP W SET W.SPXX01=spxx01? , W.ZCXX01=zcxx01? , W.OPERTIME=SYSDATE WHERE W.LCSPLX01=lcsplx01?";
		Map row=getRow(sql, null, 0);
		int j=execSQL(o2o, sql, row);
		if (j == 0) {
			map.put("STATE", "0");
		} else {
			map.put("STATE", "1");
		}
		return map;
	}
	
	//删除楼层商品
	@RequestMapping("/deleteLcsp.action")
	public Map deleteLcsp(String XmlData) throws Exception{
		System.out.println(XmlData);		
		cds=new DataSet(XmlData);
		Map map=new HashMap();
		String sql="DELETE FROM W_LCSP  WHERE SPXX01=SPXX01?";
		Map row = getRow(sql, null, 0);
		int i=execSQL(o2o, sql, row); 
		if(i>0){
			map.put("STATE","1");
		}else {
			map.put("STATE","0");
		}
		return map;
	}
	/***
	 * 
	 * @param XmlData
	 * @param ZTFB01
	 * @param LCFL02
	 * @param 
	 * @return
	 * @throws Exception
	 */
	@RequestMapping("/insertFloorName.action")
	public Map<String, Object> insertFloorName(String XmlData) throws Exception{
			cds=new DataSet(XmlData);
			int lcfl01=JLTools.getJLBH(o2o, "W_LCFL", 1);
			Map<String, Object> map=new HashMap<String, Object>();
			String sql=
				"INSERT INTO W_LCFL\n" +
				"  (ZTFB01, LCFL01, LCFL02, OPERTIME)\n" + 
				"VALUES\n" + 
				"  (1, "+lcfl01+", LCFL02?, NOW())";
			Map row=getRow(sql, null, 0);
			int n=execSQL(o2o, sql, row);
			//目前暂时没有用到W_LCSPLX,但首页查询需要，所以全部默认W_LCFL
			String sqlFl=
				"INSERT INTO W_LCSPLX\n" +
				"  (LCFL01,LCSPLX01, LCSPLX02, OPERTIME)\n" + 
				"VALUES\n" + 
				"  ("+lcfl01+", "+lcfl01+", LCFL02?, NOW())";
			Map row1=getRow(sqlFl, null, 0);
			int m=execSQL(o2o, sqlFl, row1);
			map.put("STATE", n*m);
		return map;
	}
	/**
	 * @param XmlData
	 * @LCFL01
	 * @LCFL02
	 * @return
	 * @throws Exception
	 */
	@RequestMapping("/updateFloorName.action")
	public Map<String, Object> updateFloorName(String XmlData) throws Exception{
		cds=new DataSet(XmlData);
		Map<String, Object> map=new HashMap<String, Object>();
		String sql=
			"UPDATE W_LCFL A SET A.LCFL02 = "+cds.getField("LCFL02", 0)+" WHERE A.LCFL01 = "+cds.getField("LCFL01", 0)+"";
		Map row=getRow(sql, null, 0);
		int i=execSQL(o2o, sql, row);
		map.put("status", i);
		return map;
	}
	/**
	 * 删除楼层名称
	 * @param XmlData
	 * @LCFL01
	 * @LCFL02
	 * @return
	 * @throws Exception
	 */
	@RequestMapping("/deleteteFloorName.action")
	public Map<String, Object> deleteteFloorName(String XmlData) throws Exception{
		cds=new DataSet(XmlData);
		Map<String, Object> map=new HashMap<String, Object>();
		String sql=
			"DELETE FROM W_LCFL  WHERE LCFL01 = LCFL01?";
		Map row=getRow(sql, null, 0);
		int i=execSQL(o2o, sql, row);
		String sqlLX=
			"DELETE FROM W_LCSPLX  WHERE LCFL01 = LCFL01?";
		Map rowLX=getRow(sqlLX, null, 0);
		int j=execSQL(o2o, sqlLX, rowLX);
		map.put("STATE", i*j);
		return map;
	}
	
	/**
	 * 查询楼层品牌
	 * @throws Exception 
	 */
	@RequestMapping("/selectFloorBrand.action")
	public List<Map<String, Object>> selectFloorBrand(String lcfl01) throws Exception{
		Map<String, Object> map=new HashMap<String, Object>();
		String sql=
			"SELECT LCPP01, LCPP02, LCPP03, LCPP04,LCPP05\n" +
			"  FROM W_LCPP\n" + 
			" WHERE LCPP05 < 10\n" + 
			"   AND LCFL01 = "+lcfl01+" order by LCPP01";
		Map row=getRow(sql, null, 0);
		List<Map<String, Object>> lcppList=queryForList(o2o, sql);
		return lcppList;
	}

	/**
	 * 增加楼层分类明细
	 * @param ZTFB01
	 * @throws Exception 
	 */
	@SuppressWarnings("unchecked")
	@RequestMapping("/insertLcflItem.action")
	public Map insertLcflItem(String XmlData) throws Exception{
		System.out.println(XmlData);
		cds=new DataSet(XmlData);
		Map map=new HashMap();
		String[] SPFL01 = cds.getField("SPFL01", 0).split(",");
		int i=0;
		if(SPFL01.length>0){
			String deleteSql="DELETE FROM W_LCFLITEM WHERE LCFL01=LCFL01?";
			Map rows = getRow(deleteSql, null, 0);
			execSQL(o2o, deleteSql, rows); 
			for(int j=0 ; j<SPFL01.length ; j++){
				String sql="INSERT INTO W_LCFLITEM(LCFL01,SPFL01,OPERTIME) \n"+
							"VALUES (LCFL01?,'"+SPFL01[j]+"',SYSDATE)";
				Map row = getRow(sql, null, 0);
				i+=execSQL(o2o, sql, row); 
			}
		}
		if(i>0){
			map.put("STATE","1");
		}else {
			map.put("STATE","0");
		}
		return map;
	}
	
	/**
	 * 删除楼层分类明细
	 * @param ZTFB01
	 * @throws Exception 
	 */
	@SuppressWarnings("unchecked")
	@RequestMapping("/deleteSPFL.action")
	public Map deleteSPFL(String XmlData) throws Exception{
		System.out.println(XmlData);
		cds=new DataSet(XmlData);
		Map map=new HashMap();
		String sql="DELETE FROM W_LCFLITEM WHERE LCFL01=LCFL01? AND SPFL01=SPFL01?";
		Map row = getRow(sql, null, 0);
		int i=execSQL(o2o, sql, row); 
		if(i>0){
			map.put("STATE","1");
		}else {
			map.put("STATE","0");
		}
		return map;
	}
	
	
	
	
	/**
	 * 新增楼层商品类型
	 * @param ZTFB01
	 * @throws Exception 
	 */
	@SuppressWarnings("unchecked")
	@RequestMapping("/insertSPLX.action")
	public Map insertSPLX(String XmlData) throws Exception{
		Map map=new HashMap();
		cds=new DataSet(XmlData);
			String LCSPLXITEM = cds.getField("LCSPLXITEM", 0);
			JSONArray ja = JSONArray.fromObject(LCSPLXITEM);
			JSONObject jo;
			int j=0;
			for(int i = 0; i<ja.size(); i++){
				jo = ja.getJSONObject(i);
				String LCSPLX02=jo.get("LCSPLX02").toString();
				String isExitSQL="SELECT COUNT(0) FROM W_LCSPLX WHERE LCFL01='"+cds.getField("LCFL01", 0)+"' AND LCSPLX02='"+LCSPLX02+"'";
				int count = queryForInt(o2o, isExitSQL);
				if(count==0){
				LinkedList inParameter = new LinkedList();
				inParameter.add("W_LCSPLX");
				inParameter.add(1);
				LinkedList outParameter = new LinkedList();
				outParameter.add(java.sql.Types.INTEGER);
				String sqlq="{call Update_WBHZT(?,?,?)}";
				List LCSPLX01 = callProc(o2o, sqlq, inParameter, outParameter);
				
				String sql="INSERT INTO W_LCSPLX(LCFL01,LCSPLX01,LCSPLX02,OPERTIME) VALUES (LCFL01?,"+LCSPLX01.get(0)+",'"+LCSPLX02+"',SYSDATE)";
				Map row = getRow(sql, null, 0);
				j+=execSQL(o2o, sql, row); 
				}else if(count>0){
				j=-1;
				break;
				}
			}
			if(j>0){
				map.put("STATE","1");
				}else if(j==0){
				map.put("STATE","0");
				}else if(j==-1){
				map.put("STATE","-1");
				}
		return map;
	}
	
	/**
	 * 修改楼层商品类型
	 * @param ZTFB01
	 * @throws Exception 
	 */
	@SuppressWarnings("unchecked")
	@RequestMapping("/updateLCSPLX.action")
	public Map updateLCSPLX(String XmlData) throws Exception{
		cds=new DataSet(XmlData);
		Map map=new HashMap();
		String sql="UPDATE W_LCSPLX SET LCSPLX02=LCSPLX02? WHERE LCFL01=LCFL01? AND LCSPLX01=LCSPLX01?";
		Map row = getRow(sql, null, 0);
		int i=execSQL(o2o, sql, row); 
		if(i>0){
			map.put("STATE","1");
		}else {
			map.put("STATE","0");
		}
		return map;
	}
	
	
	/**
	 * 删除楼层商品类型
	 * @param ZTFB01
	 * @throws Exception 
	 */
	@SuppressWarnings("unchecked")
	@RequestMapping("/deleteSplx.action")
	public Map deleteSplx(String XmlData) throws Exception{
		cds=new DataSet(XmlData);
		Map map=new HashMap();
		String sql="DELETE FROM W_LCSPLX WHERE LCFL01=LCFL01? AND LCSPLX01=LCSPLX01?";
		Map row = getRow(sql, null, 0);
		int i=execSQL(o2o, sql, row); 
		if(i>0){
			map.put("STATE","1");
		}else {
			map.put("STATE","0");
		}
		return map;
	}

	
	/**
	 * 新增热门搜索
	 * @param ZTFB01
	 * @throws Exception 
	 */
	@SuppressWarnings("unchecked")
	@RequestMapping("/insertRmss.action")
	public Map insertRmss(String XmlData) throws Exception{
		cds=new DataSet(XmlData);
		Map map=new HashMap();
		LinkedList inParameter = new LinkedList();
		inParameter.add("W_RMSS");
		inParameter.add(1);
		LinkedList outParameter = new LinkedList();
		outParameter.add(java.sql.Types.INTEGER);
		String sqlq="{call Update_WBHZT(?,?,?)}";
		List RMSSC01 = callProc(o2o, sqlq, inParameter, outParameter);
		
		String sql="INSERT INTO W_RMSS(RMSSC01,GSXX01,RMSSC02) VALUES ("+RMSSC01.get(0)+",GSXX01?,RMSSC02?)";
		Map row = getRow(sql, null, 0);
		int i=execSQL(o2o, sql, row); 
		if(i>0){
			map.put("STATE","1");
		}else {
			map.put("STATE","0");
		}
		return map;
	}
		
	/**
	 * 删除热门搜索
	 * @param ZTFB01
	 * @throws Exception 
	 */
	@SuppressWarnings("unchecked")
	@RequestMapping("/deleteRmss.action")
	public Map deleteRmss(String XmlData) throws Exception{
		cds=new DataSet(XmlData);
		Map map=new HashMap();
		String sql="DELETE FROM W_RMSS WHERE RMSSC01=RMSSC01? ";
		Map row = getRow(sql, null, 0);
		int i=execSQL(o2o, sql, row); 
		if(i>0){
			map.put("STATE","1");
		}else {
			map.put("STATE","0");
		}
		return map;
	}
	
	/**
	 * 修改热门搜索
	 * @param ZTFB01
	 * @throws Exception 
	 */
	@SuppressWarnings("unchecked")
	@RequestMapping("/updateRmss.action")
	public Map updateRmss(String XmlData) throws Exception{
		cds=new DataSet(XmlData);
		Map map=new HashMap();
		String sql="UPDATE W_RMSS SET RMSSC02=RMSSC02? WHERE RMSSC01=RMSSC01?";
		Map row = getRow(sql, null, 0);
		int i=execSQL(o2o, sql, row); 
		if(i>0){
			map.put("STATE","1");
		}else {
			map.put("STATE","0");
		}
		return map;
	}
	
	/**
	 * 修改楼层名称
	 * @throws Exception 
	 */
	@RequestMapping("/updateFloor.action")
	public Map<String, Object> updateFloor(String XmlData) {
		Map<String, Object> hm=new HashMap<String, Object>();
		try {
			cds =new DataSet(XmlData);
			String sql=
				"UPDATE W_LCFL A\n" +
				"   SET A.LCFL02 = '"+cds.getField("LCFL02", 0)+"'\n" + 
				" WHERE A.LCFL01 = '"+cds.getField("LCFL01", 0)+"'\n" + 
				"   AND A.ZTFB01 = "+cds.getField("ZTFB01", 0)+"";
			int i=execSQL(o2o, sql, 0);
			if(i>0){
				hm.put("STATUS", 1);
			}else{
				hm.put("STATUS", 0);
			}
		} catch (Exception e) {
			e.printStackTrace();
			hm.put("STATUS", 0);
		}
		return hm;
	}
	
	/**
	 * 查询所有楼层
	 * @param ZTFB01
	 * @throws Exception 
	 * @author ywl-SYXL
	 */
	@SuppressWarnings("unchecked")
	@RequestMapping("/selectAllFloor.action")
	public List<Map<String, Object>> selectAllFloor() throws Exception{
		String sql="";
			sql="SELECT LCFL01, LCFL02 FROM W_LCFL order by LCFL01";
		List<Map<String, Object>> lcList=queryForList(o2o, sql);
		if(lcList==null){
			lcList=new ArrayList<Map<String,Object>>();
			Map map=new HashMap<String, Object>();
			map.put("message", "查询失败");
			lcList.add(map);
		}
		return lcList;
	}
	
}