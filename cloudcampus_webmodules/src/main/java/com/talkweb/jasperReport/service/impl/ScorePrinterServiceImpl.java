package com.talkweb.jasperReport.service.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.talkweb.jasperReport.bean.ClassScoreHead;
import com.talkweb.jasperReport.bean.ClassScoreMiddle;
import com.talkweb.jasperReport.bean.ClassScoreTail;
import com.talkweb.jasperReport.bean.CompareTable;
import com.talkweb.jasperReport.bean.ExamOverview;
import com.talkweb.jasperReport.bean.GradeFirstHead;
import com.talkweb.jasperReport.bean.GradeMiddle;
import com.talkweb.jasperReport.bean.GradeSecondHead;
import com.talkweb.jasperReport.bean.GradeThirdHead;
import com.talkweb.jasperReport.bean.HistogramChart;
import com.talkweb.jasperReport.bean.HistogramTable;
import com.talkweb.jasperReport.bean.ScorePlacesSet;
import com.talkweb.jasperReport.bean.ScoreSectionSet;
import com.talkweb.jasperReport.bean.ScoreTrendBean;
import com.talkweb.jasperReport.bean.StudentRankVary;
import com.talkweb.jasperReport.bean.TrendLine;
import com.talkweb.jasperReport.bean.TrendTableBean;
import com.talkweb.jasperReport.service.ScorePrinterService;
import com.talkweb.jasperReport.util.Constant;

/**
 * 
 * 成绩报表打印-数据封装
 *
*/
@Service
public class ScorePrinterServiceImpl implements ScorePrinterService{
	
	private List<HistogramTable> compareList = new ArrayList<HistogramTable>();
	
	private static boolean JSONObjectIsNotEmpty(JSONObject object, String key) {
		return (object.containsKey(key) && !"".equals(object.get(key).toString()));
	}

	public List<ScoreTrendBean> getResultsTrendList(List<JSONObject> datas,String head,String type) {
		Pattern pattern = Pattern.compile("(?<=\\().*(?=\\))");
		List<ScoreTrendBean> stList = new ArrayList<ScoreTrendBean>();
		// 总标题
		String[] titles = head.trim().split(",");
		String title = "";
		if (ArrayUtils.isNotEmpty(titles) && titles.length >= 3) {
			String semester = "";
			if (titles[0].length() > 10){
				semester = titles[0].substring(1, 10);
			}
			String grade = "";
			if (titles[1].length() > 6){
				grade = titles[1].substring(6);
			}	
			
			title = semester + grade + Constant.PREVIOUSEXAM
					+ titles[2].replace("\"", "") + Constant.TREND;
		}	
		// ---打印的图形类型---
		String label = "";
		double minRangeAxis = 0;
		double maxRangeAxis = 0;
		if(Constant.AVERAGEFIGURE.equals(type)){
			label = Constant.RANKING;
			minRangeAxis = 0;
			maxRangeAxis = 22;
		}else if(Constant.FULLEXCELLENT.equals(type)){
			label = Constant.NUMBER;
			minRangeAxis = -1;
			maxRangeAxis = 1;
		}else if(Constant.SUBOPTIMALFIGURE.equals(type)){
			label = Constant.NUMBER;
			minRangeAxis = -1;
			maxRangeAxis = 1;
		}else if(Constant.PASSRATEDIAGRAM.equals(type)){
			label = Constant.PASSRATE;
			minRangeAxis = 0;
			maxRangeAxis = 100;
		}else if(Constant.EXECLLENTRATEDIAGRAM.equals(type)){
			label = Constant.EXECLLENTRATE;
			minRangeAxis = 0;
			maxRangeAxis = 100;
		}
		// 一张A4纸打印六个图表,size:打印长度
		int length = datas.size();		
		int size = length/6 + ((length%6) > 0 ? 1 : 0);		
		for(int i = 1;i <= size; i++){
			ScoreTrendBean stBean = new ScoreTrendBean();
			int startNum = (i-1) * 6;   	    	   
	    	int endNum = startNum + 6;
	    	if (endNum > length)endNum = length;
	    	List<JSONObject> tempList = datas.subList(startNum, endNum);
	    	// 遍历数据集合，填充六个图表数据
	    	for(int k = 0;k < tempList.size();k++){
	    		JSONArray temp = tempList.get(k).getJSONArray("Series");
	    		String classTitle = tempList.get(k).getString("title");	    		
	    		Matcher matcher = pattern.matcher(classTitle);
	    		String className = "";
	    		while (matcher.find()) {
	    			className = matcher.group();
	    		}		
 				// Series节点下图表内容
 				List<TrendTableBean> tbList = new ArrayList<TrendTableBean>();
 				TrendTableBean table = new TrendTableBean();
 				table.setExamRowName(label);
 				tbList.add(table);
 			    // Series节点下折线及趋势线坐标
	    		Map<String,TrendLine> broken = new LinkedHashMap<String,TrendLine>();
				int firstx = 0;float firsty = 0;
				int endx = 0;float endy = 0;
	    		Map<String,Float> bee = new HashMap<String,Float>();
	    		for(int j=0;j < temp.size();j++){
	    			JSONObject line = temp.getJSONObject(j);
	    			String name = line.getString("name").trim();
	    			if((Constant.TRACE).equals(name)){
	    				JSONArray trend = line.getJSONArray("data");
                        for(int n = 0;n < trend.size();n++){
                        	String x = trend.getJSONObject(n).getString("x");
	    					float y = Float.parseFloat(trend.getJSONObject(n).getString("y"));
                        	if (n == 0){
                        		firstx = Integer.parseInt(x);
                        		firsty = y;
                        	}
                            if (n == trend.size()-1){
                            	endx = Integer.parseInt(x);
                        		endy = y;
                            }                           
	    					bee.put(x,y);
	    				}
	    			}else{
	    				JSONArray rank = line.getJSONArray("data");
	    				for(int m = 0;m < rank.size();m++){
	    					String x = rank.getJSONObject(m).getString("x");
	    					String y = rank.getJSONObject(m).getString("y");
	    					String cname = rank.getJSONObject(m).getString("name");
	    					String key = cname.substring(0,2);
	    					String headline = cname.substring(3);
	    					TrendLine point = new TrendLine();
	    					point.setX1(x);
	    					point.setY1(Float.parseFloat(y));
	    					point.setKey(key);
	    					point.setName(headline);
	    					broken.put(x, point);
	    					switch(m){
	    						case 0:table.setExamFirstName(headline);
	    						table.setExamFirstRanking(y);
	    						break;
	    						case 1:table.setExamSecondName(headline);
	    						table.setExamSecondRanking(y);
	    						break;
	    						case 2:table.setExamThirdName(headline);
	    						table.setExamThirdRanking(y);
	    						break;
	    						case 3:table.setExamFourthName(headline);
	    						table.setExamFourthRanking(y);
	    						break;
	    						case 4:table.setExamFifthName(headline);
	    						table.setExamFifthRanking(y);
	    						break;
	    						case 5:table.setExamSixthName(headline);
	    						table.setExamSixthRanking(y);
	    						break;
	    						case 6:table.setExamSeventhName(headline);
	    						table.setExamSeventhRanking(y);
	    						break;
	    						case 7:table.setExamEighthName(headline);
	    						table.setExamEighthRanking(y);
	    						break;
	    						case 8:table.setExamNinthName(headline);
	    						table.setExamNinthRanking(y);
	    						break;
	    						case 9:table.setExamTenthName(headline);
	    						table.setExamTenthRanking(y);
	    						break;
	    						case 10:table.setExamEleventhName(headline);
	    						table.setExamEleventhRanking(y);
	    						break;
	    						case 11:table.setExamTwelfthName(headline);
	    						table.setExamTwelfthRanking(y);
	    						break;
    					    }
	    				}			
	    			}	
	    		}
	    		List<TrendLine> lines = null;
	    		if (!broken.isEmpty()){
		    		// Series下折线图及直线图数据封装
	 				lines = new ArrayList<TrendLine>();		
		    		Iterator<Entry<String, TrendLine>> ite = broken.entrySet().iterator();
		    		float slope = Math.abs((endy-firsty)/(endx-firstx));
			        while (ite.hasNext()) {
			                Map.Entry<String,TrendLine> entry = (Map.Entry<String,TrendLine>) ite.next();
			                String key = entry.getKey();
		                	TrendLine template = entry.getValue();
			                if (bee.containsKey(key)){
			                	template.setX2(key);
			                	template.setY2(bee.get(key));
			                }else{
			                	int xkey = Integer.parseInt(key);
			                	template.setX2(key);
			                	if (endy > firsty){
			                		template.setY2(firsty + (xkey-firstx)*slope);
			                	}else{
			                		template.setY2(firsty - (xkey-firstx)*slope);
			                	}	
			                }
			                lines.add(template);
			        }		
	    		}	
		        // 六个图表的详细数据
	    		switch(k){
	    		    case 0:stBean.setTrendDataOne(tbList);
	    		    stBean.setClassNameOne(className);
	    		    stBean.setMinRangeAxis(minRangeAxis);
	    		    stBean.setMaxRangeAxis(maxRangeAxis);
	    		    stBean.setLineListOne(lines);
	    		    break;
	    		    case 1:stBean.setTrendDataTwo(tbList);
	    		    stBean.setClassNameTwo(className);
	    		    stBean.setMinRangeAxis(minRangeAxis);
	    		    stBean.setMaxRangeAxis(maxRangeAxis);
	    		    stBean.setLineListTwo(lines);
	    		    break;
	    		    case 2:stBean.setTrendDataThree(tbList);
	    		    stBean.setClassNameThree(className);
	    		    stBean.setMinRangeAxis(minRangeAxis);
	    		    stBean.setMaxRangeAxis(maxRangeAxis);
	    		    stBean.setLineListThree(lines);
	    		    break;
	    		    case 3:stBean.setTrendDataFour(tbList);
	    		    stBean.setClassNameFour(className);
	    		    stBean.setMinRangeAxis(minRangeAxis);
	    		    stBean.setMaxRangeAxis(maxRangeAxis);
	    		    stBean.setLineListFour(lines);
	    		    break;
	    		    case 4:stBean.setTrendDataFive(tbList);
	    		    stBean.setClassNameFive(className);
	    		    stBean.setMinRangeAxis(minRangeAxis);
	    		    stBean.setMaxRangeAxis(maxRangeAxis);
	    		    stBean.setLineListFive(lines);
	    		    break;
	    		    case 5:stBean.setTrendDataSix(tbList);
	    		    stBean.setClassNameSix(className);
	    		    stBean.setMinRangeAxis(minRangeAxis);
	    		    stBean.setMaxRangeAxis(maxRangeAxis);
	    		    stBean.setLineListSix(lines);
	    		    break;
		         }	    			
	    	}
	    	stBean.setTitle(title);
	    	stBean.setClassTypeName(label);
	    	stList.add(stBean);
		}	
		return stList;
	}
	
	/**
	 * 
	 * 班级报告报表-首页
	 *
	*/
	public List<ClassScoreHead> getClassScoreHeadList(JSONObject data) {		
		List<ClassScoreHead> headList = new ArrayList<ClassScoreHead>();
		ClassScoreHead head = new ClassScoreHead();		
		if (JSONObjectIsNotEmpty(data,"examName")&& JSONObjectIsNotEmpty(data,"className")){			
			head.setReportTitle(data.getString("examName") + " "
					+ data.getString("className") + " " + Constant.SUFFIX);
		}
		if (JSONObjectIsNotEmpty(data,"examSituation")){
			JSONObject situation = data.getJSONObject("examSituation");
			if (JSONObjectIsNotEmpty(situation,"rows")){				
				JSONArray array = situation.getJSONArray("rows");
				int situationLen = array.size();
				if (situationLen > 0){
					List<ExamOverview> examview = new ArrayList<ExamOverview>();
					for(int i = 0;i < array.size(); i++){
						JSONObject object = array.getJSONObject(i);
						ExamOverview view = new ExamOverview();
						if (object.containsKey("item")){
							view.setItem(object.get("item").toString());
						}
						if (object.containsKey("pjf")){
							view.setAverageRate(object.get("pjf").toString());
						}
						if (object.containsKey("pjfpm")){
							String rank = object.get("pjfpm").toString();
							if (object.containsKey("pjfpmgz")){
								int gz = object.getIntValue("pjfpmgz");								
								if (gz > 0){
									rank = rank + Constant.RISE + gz;
								}else if(gz < 0){
									rank = rank + Constant.DROP + Math.abs(gz);
								}
							}
							view.setAverageRank(rank);
						}
						if (object.containsKey("pjffc")){
							view.setAverageRateHigh(object.get("pjffc").toString());
						}
						if (object.containsKey("yxl")){
							view.setExcellentRate(object.get("yxl").toString());
						}
						if (object.containsKey("yxlpm")){
							String rank = object.get("yxlpm").toString();
							if (object.containsKey("yxlpmgz")){
								int gz = object.getIntValue("yxlpmgz");
								if (gz > 0){
									rank = rank + Constant.RISE + gz;
								}else if(gz < 0){
									rank = rank + Constant.DROP + Math.abs(gz);
								}		
							}
							view.setExcellentRank(rank);
						}
						if (object.containsKey("yxlfc")){
							view.setExcellentRateHigh(object.get("yxlfc").toString());
						}
						if (object.containsKey("hgl")){
							view.setPassRate(object.get("pjf").toString());
						}
						if (object.containsKey("hglpm")){
							String rank = object.get("hglpm").toString();
							if (object.containsKey("hglpmgz")){
								int gz = object.getIntValue("hglpmgz");
								if (gz > 0){
									rank = rank + Constant.RISE + gz;
								}else if(gz < 0){
									rank = rank + Constant.DROP + Math.abs(gz);
								}
							}
							view.setPassRank(rank);
						}
						if (object.containsKey("hglfc")){
							view.setPassRateHigh(object.get("hglfc").toString());
						}
						if (object.containsKey("jzsrs")){
							String rank = object.get("jzsrs").toString();
							if (object.containsKey("jzsrsgz")){
								int gz = object.getIntValue("jzsrsgz");
								if (gz > 0){
									rank = rank + Constant.RISE + gz;
								}else if(gz < 0){
									rank = rank + Constant.DROP + Math.abs(gz);
								}			
							}
							view.setTopStudentNum(rank);
						}
						if (object.containsKey("qnsrs")){
							String rank = object.get("qnsrs").toString();
							if (object.containsKey("qnsrsgz")){
								int gz = object.getIntValue("qnsrsgz");
								if (gz > 0){
									rank = rank + Constant.RISE + gz;
								}else if(gz < 0){
									rank = rank + Constant.DROP + Math.abs(gz);
								}			
							}
							view.setPotentialStuNum(rank);
						}
						examview.add(view);
					}	
					head.setExamOverview(examview);
				}					
			}
			if (situation.containsKey("sm"))
				head.setExamExplanation(situation.getString("sm"));		
		}
		if (JSONObjectIsNotEmpty(data,"studentAbnormalSituation")){
			JSONArray normal = data.getJSONArray("studentAbnormalSituation");
			int lenght = normal.size();
			if (lenght > 0){
				JSONObject object = normal.getJSONObject(0);			
				if (object.containsKey("kmmc")){
					head.setTotalScoreItem(object.getString("kmmc"));
					head.setTotalUpName(Constant.ADVANCESTUDENT);
					head.setTotalDownName(Constant.REGRESSTATE);
				}
				if (JSONObjectIsNotEmpty(object,"jb")){
					JSONArray jb = object.getJSONArray("jb");					
					int jbLenght = jb.size();
					if (jbLenght > 0){
						List<StudentRankVary> totalUp = new ArrayList<StudentRankVary>() ;
						String title = Constant.HEADONE + jbLenght + Constant.HEADTWO;
						setVaryStudentList(jb, jbLenght, totalUp, Constant.LABELONE, title);
						head.setTotalScoreUp(totalUp);
					}				
				}
				if (JSONObjectIsNotEmpty(object,"yc")){
					JSONArray yc = object.getJSONArray("yc");
					int ycLenght = yc.size();
					if (ycLenght > 0){
						List<StudentRankVary> totalDown = new ArrayList<StudentRankVary>() ;
						String title = Constant.HEADONE + ycLenght + Constant.HEADTHR;
						setVaryStudentList(yc, ycLenght, totalDown, Constant.LABELTWO, title);
						head.setTotalScoreDown(totalDown);
					}
				}
			}
		}
		headList.add(head);
		return headList;
	}

	private static void setVaryStudentList(JSONArray bh, int bhLenght,
			List<StudentRankVary> varyList,String label,String title) {
		int size = bhLenght/4 + ((bhLenght%4) > 0 ? 1 : 0);	
		for(int k = 0;k < size; k++){
			StudentRankVary vary = new StudentRankVary();
			vary.setTableHead(title);
			varyList.add(vary);
		    int j = k * 4 + 0;
		    if (j < bhLenght){
			    JSONObject bhOne = bh.getJSONObject(j);
			    vary.setStudentNameOne(bhOne.getString("xm") + label);
			    vary.setStudentRankOne(String.valueOf(Math.abs(bhOne.getIntValue("pmsj"))));
		    }else{
		    	break;
		    }
			j = k * 4 + 1;
			if (j < bhLenght){
				JSONObject bhTwo = bh.getJSONObject(j);
				vary.setStudentNameTwo(bhTwo.getString("xm") + label);
				vary.setStudentRankTwo(String.valueOf(Math.abs(bhTwo.getIntValue("pmsj"))));	
			}else{
		    	break;
		    }
			j = k * 4 + 2;
			if (j < bhLenght){
				JSONObject bhThree = bh.getJSONObject(j);
				vary.setStudentNameThree(bhThree.getString("xm") + label);
				vary.setStudentRankThree(String.valueOf(Math.abs(bhThree.getIntValue("pmsj"))));
			}else{
		    	break;
		    }
			j = k * 4 + 3;
			if (j < bhLenght){
				JSONObject bhFour = bh.getJSONObject(j);
				vary.setStudentNameFour(bhFour.getString("xm") + label);
				vary.setStudentRankFour(String.valueOf(Math.abs(bhFour.getIntValue("pmsj"))));   
			}else{
		    	break;
		    }	
		}
	}

	/**
	 * 
	 * 班级报告报表-学生变化情况
	 *
	*/
	public List<ClassScoreMiddle> getClassScoreMiddleList(JSONObject data) {
		List<ClassScoreMiddle> middleList = null;
		if (JSONObjectIsNotEmpty(data,"studentAbnormalSituation")){
			JSONArray normal = data.getJSONArray("studentAbnormalSituation");
			int length = normal.size();
			if (length > 1){
				middleList = new ArrayList<ClassScoreMiddle>();
				int msize = length/3 + ((length%3) > 0 ? 1 : 0);
				for(int m = 0; m < msize; m++){
					ClassScoreMiddle middle = new ClassScoreMiddle();
					middleList.add(middle);	
					// ---第一个科目的进步与异常学生---
					int n = m * 3 + 1;		
					if (n < length){
						JSONObject firstObj = normal.getJSONObject(n);
						if (firstObj.containsKey("kmmc")){
							middle.setLessonFirstItem(firstObj.getString("kmmc"));
							middle.setLessonUpFirstName(Constant.ADVANCESTUDENT);
							middle.setLessonDownFirstName(Constant.REGRESSTATE);
						}
						if (JSONObjectIsNotEmpty(firstObj,"jb")){	
							JSONArray jb = firstObj.getJSONArray("jb");					
							int jbLenght = jb.size();
							if (jbLenght > 0){
								List<StudentRankVary> firstUp = new ArrayList<StudentRankVary>() ;
								String title = Constant.HEADONE + jbLenght + Constant.HEADTWO;
								setVaryStudentList(jb, jbLenght, firstUp, Constant.LABELONE, title);
								middle.setLessonUpFirst(firstUp);
							}				
						}
						if (JSONObjectIsNotEmpty(firstObj,"yc")){
							JSONArray yc = firstObj.getJSONArray("yc");
							int ycLenght = yc.size();
							if (ycLenght > 0){
								List<StudentRankVary> firstDown = new ArrayList<StudentRankVary>() ;
								String title = Constant.HEADONE + ycLenght + Constant.HEADTHR;
								setVaryStudentList(yc, ycLenght, firstDown, Constant.LABELTWO, title);
								middle.setLessonDownFirst(firstDown);
							}
						}
					}else{
						break;
					}	
					// ---第二个科目的进步与异常学生---
					n = m * 3 + 2;
					if (n < length){
						JSONObject secondObj = normal.getJSONObject(n);
						if (secondObj.containsKey("kmmc")){
							middle.setLessonSecondItem(secondObj.getString("kmmc"));
							middle.setLessonUpSecondName(Constant.ADVANCESTUDENT);
							middle.setLessonDownSecondName(Constant.REGRESSTATE);
						}
						if (JSONObjectIsNotEmpty(secondObj,"jb")){		
							JSONArray jb = secondObj.getJSONArray("jb");					
							int jbLenght = jb.size();
							if (jbLenght > 0){
								List<StudentRankVary> secondUp = new ArrayList<StudentRankVary>() ;
								String title = Constant.HEADONE + jbLenght + Constant.HEADTWO;
								setVaryStudentList(jb, jbLenght, secondUp, Constant.LABELONE, title);
								middle.setLessonUpSecond(secondUp);
							}				
						}
						if (JSONObjectIsNotEmpty(secondObj,"yc")){	
							JSONArray yc = secondObj.getJSONArray("yc");
							int ycLenght = yc.size();
							if (ycLenght > 0){
								List<StudentRankVary> secondDown = new ArrayList<StudentRankVary>() ;
								String title = Constant.HEADONE + ycLenght + Constant.HEADTHR;
								setVaryStudentList(yc, ycLenght, secondDown, Constant.LABELTWO, title);
								middle.setLessonDownSecond(secondDown);
							}
						}
					}else{
						break;
					}	
					// ---第三个科目的进步与异常学生---
					n = m * 3 + 3;	
                    if (n < length){
                    	JSONObject thirdObj = normal.getJSONObject(n);
    					if (thirdObj.containsKey("kmmc")){
    						middle.setLessonThirdItem(thirdObj.getString("kmmc"));
    						middle.setLessonUpThirdName(Constant.ADVANCESTUDENT);
    						middle.setLessonDownThirdName(Constant.REGRESSTATE);
    					}
    					if (JSONObjectIsNotEmpty(thirdObj,"jb")){	
    						JSONArray jb = thirdObj.getJSONArray("jb");					
    						int jbLenght = jb.size();
    						if (jbLenght > 0){
    							List<StudentRankVary> thirdUp = new ArrayList<StudentRankVary>() ;
    							String title = Constant.HEADONE + jbLenght + Constant.HEADTWO;
    							setVaryStudentList(jb, jbLenght, thirdUp, Constant.LABELONE, title);
    							middle.setLessonUpThird(thirdUp);
    						}				
    					}
    					if (JSONObjectIsNotEmpty(thirdObj,"yc")){
    						JSONArray yc = thirdObj.getJSONArray("yc");
    						int ycLenght = yc.size();
    						if (ycLenght > 0){
    							List<StudentRankVary> thirdDown = new ArrayList<StudentRankVary>() ;
    							String title = Constant.HEADONE + ycLenght + Constant.HEADTHR;
    							setVaryStudentList(yc, ycLenght, thirdDown, Constant.LABELTWO, title);
    							middle.setLessonDownThird(thirdDown);
    						}
    					}
					}else{
						break;
					}					
				}		
			}	
		}	
		return middleList;
	}

	/**
	 * 
	 * 班级报告报表-尾页
	 *
	*/
	public List<ClassScoreTail> getClassScoreTailList(JSONObject data) {
		List<ClassScoreTail> tailList = new ArrayList<ClassScoreTail>();	
		ClassScoreTail tail = new ClassScoreTail();	
		// ---各科名次段人数情况---
		if (JSONObjectIsNotEmpty(data,"everySubjectRankNum")){
			JSONObject placeArray = data.getJSONObject("everySubjectRankNum");
			if (JSONObjectIsNotEmpty(placeArray,"rows")){				
				JSONArray array = placeArray.getJSONArray("rows");
				int placeLen = array.size();
				if (placeLen > 0){
					List<ScorePlacesSet> placeSet = new ArrayList<ScorePlacesSet>();
					for(int i = 0;i < placeLen; i++){
						JSONObject object = array.getJSONObject(i);
						ScorePlacesSet place = new ScorePlacesSet();
						if (object.containsKey("scoreDisName")){
							place.setScoreSection(object.get("scoreDisName").toString());
						}
						if (object.containsKey("dis00000000")){
							place.setTotalNumber(object.get("dis00000000").toString());
						}
						if (object.containsKey("dis1")){
							place.setChineseNumber(object.get("dis1").toString());
						}
						if (object.containsKey("dis2")){
							place.setMathNumber(object.get("dis2").toString());
						}
						if (object.containsKey("dis3")){
							place.setEnglishNumber(object.get("dis3").toString());
						}
						if (object.containsKey("dis4")){
							place.setPoliticsNumber(object.get("dis4").toString());
						}
						if (object.containsKey("dis5")){
							place.setHistoryNumber(object.get("dis5").toString());
						}
						if (object.containsKey("dis6")){
							place.setGeographyNumber(object.get("dis6").toString());
						}
						if (object.containsKey("dis7")){
							place.setPhysicsNumber(object.get("dis7").toString());
						}
						if (object.containsKey("dis8")){
							place.setChemistryNumber(object.get("dis8").toString());
						}
						if (object.containsKey("dis9")){
							place.setBiologyNumber(object.get("dis9").toString());
						}
						placeSet.add(place);
					}	
					tail.setScoreStudentCount(placeSet);
				}			
			}
		}
		// ---各科名次段人数情况---
		if (JSONObjectIsNotEmpty(data,"everyClassTotalScoreNum")){
			JSONObject totalSecArray = data.getJSONObject("everyClassTotalScoreNum");
			Map<String,String> everyClassMap = new LinkedHashMap<String,String>();
			if (JSONObjectIsNotEmpty(totalSecArray,"columns")){				
				JSONArray column = totalSecArray.getJSONArray("columns").getJSONArray(0);
				for(int j = 0; j < column.size(); j++){
					JSONObject columnObj = column.getJSONObject(j);
					String field = columnObj.get("field").toString();
					String title = columnObj.get("title").toString();
					if(!field.equals("bjmc")){
						String value = title.replace(Constant.SCORE, "");
						everyClassMap.put(field, value);
					}	   
				}
			}
			if (JSONObjectIsNotEmpty(totalSecArray,"rows")){				
				JSONArray secRows = totalSecArray.getJSONArray("rows");
				int rowLenght = secRows.size();
				if (rowLenght > 0){
					List<ScoreSectionSet> sectionSet = new ArrayList<ScoreSectionSet>();
					for(int k = 0;k < rowLenght; k++){
						JSONObject sectionObj = secRows.getJSONObject(k);
						ScoreSectionSet section = new ScoreSectionSet();
						section.setClassName(sectionObj.getString("bjmc"));
						Iterator<Entry<String, String>> it = everyClassMap.entrySet().iterator();
						int l = 1;
				        while (it.hasNext()) {
				                Map.Entry<String,String> entry = (Map.Entry<String,String>) it.next();
				                String field = entry.getKey();
			                	String title = entry.getValue();			                	
			                	switch(l){
	    						case 1:section.setSectionOne(title);
	    						section.setSectionOneNum(sectionObj.get(field).toString());
	    						break;
	    						case 2:section.setSectionTwo(title);
	    						section.setSectionTwoNum(sectionObj.get(field).toString());
	    						break;
	    						case 3:section.setSectionThree(title);
	    						section.setSectionThreeNum(sectionObj.get(field).toString());
	    						break;
	    						case 4:section.setSectionFour(title);
	    						section.setSectionFourNum(sectionObj.get(field).toString());
	    						break;
	    						case 5:section.setSectionFive(title);
	    						section.setSectionFiveNum(sectionObj.get(field).toString());
	    						break;
	    						case 6:section.setSectionSix(title);
	    						section.setSectionSixNum(sectionObj.get(field).toString());
	    						break;
	    						case 7:section.setSectionSeven(title);
	    						section.setSectionSevenNum(sectionObj.get(field).toString());
	    						break;
	    						case 8:section.setSectionEight(title);
	    						section.setSectionEightNum(sectionObj.get(field).toString());
	    						break;
	    						case 9:section.setSectionNine(title);
	    						section.setSectionNineNum(sectionObj.get(field).toString());
	    						break;
	    						case 10:section.setSectionTen(title);
	    						section.setSectionTenNum(sectionObj.get(field).toString());
	    						break;    						
				                }	
			                	l++;
					    }	
				sectionSet.add(section);
				}		
				tail.setSectionCount(sectionSet);	
			  }
		   }		
		}
		// ---成绩跟踪轨迹---
		if (JSONObjectIsNotEmpty(data,"scoreTraceData")){
			JSONObject trace = data.getJSONObject("scoreTraceData");
			List<Float> yz = new ArrayList<Float>();
			if (JSONObjectIsNotEmpty(trace,"series")){
				JSONArray yArray = trace.getJSONArray("series");
				if (yArray.size() > 0){
					JSONObject obj = yArray.getJSONObject(0);
					JSONArray ys = obj.getJSONArray("data");
					for(int t = 0; t < ys.size(); t++){
						JSONObject yb = ys.getJSONObject(t);
						if (yb.containsKey("y"))
					    yz.add(yb.getFloatValue("y"));
					}
				}
			}
			if (JSONObjectIsNotEmpty(trace,"xAxis")){
				String xs = trace.get("xAxis").toString();			
				List<String> xz= JSON.parseArray(xs, String.class);
				if ((xz.size() != 0) && (xz.size() == yz.size())){
					List<TrendLine> scoreTrack = new ArrayList<TrendLine>();
					for(int r = 0; r < xz.size(); r++){
						TrendLine point = new TrendLine();
						point.setX1(xz.get(r));
						point.setY1(yz.get(r));
						scoreTrack.add(point);
					}
					tail.setScoreTracking(scoreTrack);
				}
			}		
		}
		tailList.add(tail);
		return tailList;
	}

	/**
	 * 
	 * 年级报告报表
	 *
	*/
	public Map<String,List<?>> getGradeReportList(JSONObject data) {	
		Map<String,List<?>> returnMap = new HashMap<String,List<?>>();
		List<Integer> classList = new ArrayList<Integer>();
		// ----班级角度-I
		List<GradeFirstHead> firstHead = new ArrayList<GradeFirstHead>();
		GradeFirstHead firsthead = new GradeFirstHead();
		firstHead.add(firsthead);
		if (JSONObjectIsNotEmpty(data,"examName")){
			firsthead.setReportTitle(data.get("examName").toString());
		}
		if (JSONObjectIsNotEmpty(data,"classViewTip")){
		    JSONObject object = data.getJSONObject("classViewTip");
		    if (object.containsKey("pjf")){
		    	firsthead.setAverageViewTip(object.get("pjf").toString());
			}
			if (object.containsKey("hgl")){
				firsthead.setPassrateViewTip(object.get("hgl").toString());
			}
			if (object.containsKey("yxl")){
				firsthead.setFinerateViewTip(object.get("yxl").toString());
			}
			if (object.containsKey("rsddqk")){
				firsthead.setLadderViewTip(object.get("rsddqk").toString());
			}
			if (object.containsKey("fsdqk")){
				firsthead.setSectionViewTip(object.get("fsdqk").toString());
			}
		}
		rangeCompareTable(data);
		if (compareList.size() > 0){
			HistogramTable result = compareList.get(0);
			classList.add(result.getHistogram().size());
			firsthead.setTableZero(result.getTable());
			firsthead.setHistogramZero(result.getHistogram());
			firsthead.setCompareTableZero(result.getCompareTable());
		}
		returnMap.put("firstHead", firstHead);
		// ----班级角度-II
		List<GradeMiddle> firstMiddle = null;
		int length = compareList.size();
		if (length > 1){
			int end = length;
			if(length > 3)end = 3;
			List<HistogramTable> middle = compareList.subList(1, end);
			firstMiddle = getHistogramTable(middle,classList);
		}
		returnMap.put("firstMiddle", firstMiddle);
		// ----班级角度-III
		List<GradeMiddle> firstTail = null;
		int lenght = compareList.size();
		if (lenght > 3){
			List<HistogramTable> tail = compareList.subList(3, lenght);
			firstTail = getHistogramTable(tail,classList);
		}
		returnMap.put("firstTail", firstTail);
		// ----学科角度-I
		List<GradeSecondHead> secondHead = new ArrayList<GradeSecondHead>();
		GradeSecondHead secondhead = new GradeSecondHead();
		if (JSONObjectIsNotEmpty(data,"subjectViewTip")){
			JSONArray array = data.getJSONArray("subjectViewTip");
			StringBuffer tips = new StringBuffer();
			for(int i = 0;i < array.size(); i++){
				JSONObject tip = array.getJSONObject(i);
				int k = 0;
				if (tip.containsKey("kmmc")){
					String kmmc = tip.get("kmmc").toString();
					tips.append(kmmc + Constant.SUBJECT + ": ");		
					String prefix = "          ";
					String suffix = "\n";
					if (tip.containsKey("pjf")){
						String content = tip.get("pjf").toString();
						if(k > 0)tips.append(prefix);
						if (StringUtils.isNotEmpty(content)){
							tips.append(content + suffix);
							k++;
						}
					}
					if (tip.containsKey("hgl")){
						String content = tip.get("hgl").toString();
						if(k > 0)tips.append(prefix);
						if (StringUtils.isNotEmpty(content)){
							tips.append(content + suffix);
							k++;
						}
					}
					if (tip.containsKey("yxl")){
						String content = tip.get("yxl").toString();
						if(k > 0)tips.append(prefix);
						if (StringUtils.isNotEmpty(content)){
							tips.append(content + suffix);
							k++;
						}
					}
					if (tip.containsKey("Ars")){
						String content = tip.get("Ars").toString();
						if(k > 0)tips.append(prefix);
						if (StringUtils.isNotEmpty(content)){
							tips.append(content + suffix);
							k++;
						}
					}															
				    tips.append(suffix);
				}		
			}
			secondhead.setSubjectViewTip(tips.toString());
		}	
		secondHead.add(secondhead);
		returnMap.put("secondHead", secondHead);
		// ----学科角度-II
		List<HistogramTable> secondmiddle = new ArrayList<HistogramTable>();
		List<GradeMiddle> secondMiddle = null;
		if (JSONObjectIsNotEmpty(data,"subjectCompareTab")){
			JSONArray subList = data.getJSONArray("subjectCompareTab");
			if (subList.size() > 0){				
				for (int i = 0; i < subList.size(); i++) {
					JSONObject subject = subList.getJSONObject(i);
					if (subject.containsKey("title")) {
					HistogramTable result = getCompareTab(subject,Constant.CLASSHEAD);
					sortHistogramTable(result);
					result.setCompareTable(subject.get("title").toString());
					if (null != result)secondmiddle.add(result);
					}
				}
				secondMiddle = getHistogramTable(secondmiddle,classList);
			}
		}
		returnMap.put("secondMiddle", secondMiddle);
		// ----教师角度-I
		List<GradeThirdHead> thirdHead = new ArrayList<GradeThirdHead>();
		GradeThirdHead thirdhead = new GradeThirdHead();
		if (JSONObjectIsNotEmpty(data,"teacherViewTip")){
			JSONArray array = data.getJSONArray("teacherViewTip");
			StringBuffer tips = new StringBuffer();
			for(int i = 0;i < array.size(); i++){
				JSONObject tip = array.getJSONObject(i);
				int k = 0;
				if (tip.containsKey("kmmc")){
					String kmmc = tip.get("kmmc").toString();
					tips.append(kmmc + Constant.SUBJECT + ": ");		
					String prefix = "          ";
					String suffix = "\n";
					if (tip.containsKey("pjf")){
						String content = tip.get("pjf").toString();
						if(k > 0)tips.append(prefix);
						if (StringUtils.isNotEmpty(content)){
							tips.append(content + suffix);
							k++;
						}
					}
					if (tip.containsKey("yxl")){
						String content = tip.get("yxl").toString();
						if(k > 0)tips.append(prefix);
						if (StringUtils.isNotEmpty(content)){
							tips.append(content + suffix);
							k++;
						}
					}
					tips.append(suffix);
				}		
			}
			thirdhead.setClassViewTip(tips.toString());
		}
		if (JSONObjectIsNotEmpty(data,"teacherSubjectCompareTab")){
			JSONArray subject = data.getJSONArray("teacherSubjectCompareTab");
			if (subject.size() > 0){
				JSONObject tab = subject.getJSONObject(0);
				if (tab.containsKey("title")) {					
					HistogramTable result = getCompareTab(tab,Constant.NAMEHEAD);
					if (null != result){	
						thirdhead.setCompareTableZero(tab.get("title").toString());
						thirdhead.setTableZero(result.getTable());
						thirdhead.setHistogramZero(result.getHistogram());
					}
				}
			}	
		}
		thirdHead.add(thirdhead);
		returnMap.put("thirdHead", thirdHead);
		// ----教师角度-II
		List<HistogramTable> thirdmiddle = new ArrayList<HistogramTable>();
		List<GradeMiddle> thirdMiddle = null;
		if (JSONObjectIsNotEmpty(data,"teacherSubjectCompareTab")){
			JSONArray subList = data.getJSONArray("teacherSubjectCompareTab");
			if (subList.size() > 1){				
				for (int i = 1; i < subList.size(); i++) {
					JSONObject subject = subList.getJSONObject(i);
					if (subject.containsKey("title")) {
					HistogramTable result = getCompareTab(subject,Constant.NAMEHEAD);
					result.setCompareTable(subject.get("title").toString());
					if (null != result)thirdmiddle.add(result);
					}
				}
				thirdMiddle = getHistogramTable(thirdmiddle,classList);
			}
		}
		returnMap.put("thirdMiddle", thirdMiddle);
		returnMap.put("classList", classList);
		return returnMap;
	}	
	
	// ---------封装对比表的数据---------
	private void rangeCompareTable(JSONObject data) {
		compareList.clear();
		// 平均分对比表
		if (JSONObjectIsNotEmpty(data,"averageScoreCompareTab")){
			JSONObject avgrate = data.getJSONObject("averageScoreCompareTab");
			HistogramTable result = getCompareTab(avgrate,Constant.CLASSHEAD);
			if (null != result){
				result.setCompareTable(Constant.AVERAGECOMPARETABLE);
				compareList.add(result);
			}
		}
		// 合格率对比表
		if (JSONObjectIsNotEmpty(data,"passRateCompareTab")){
			JSONObject passrate = data.getJSONObject("passRateCompareTab");
			HistogramTable result = getCompareTab(passrate,Constant.CLASSHEAD);
			if (null != result){
				result.setCompareTable(Constant.PASSRATECOMPARETABLE);
				compareList.add(result);
			}
		}
		// 优秀率对比表
		if (JSONObjectIsNotEmpty(data,"excellentRateCompareTab")){
			JSONObject excellent = data.getJSONObject("excellentRateCompareTab");
			HistogramTable result = getCompareTab(excellent,Constant.CLASSHEAD);
			if (null != result){
				result.setCompareTable(Constant.EXECLLENTCOMPARETABLE);
				compareList.add(result);
			}
		}
		// 等第值对比表
		if (JSONObjectIsNotEmpty(data,"levelValueCompareTab")){
			JSONObject levelvalue = data.getJSONObject("levelValueCompareTab");
			HistogramTable result = getCompareTab(levelvalue,Constant.CLASSHEAD);
			if (null != result){
				result.setCompareTable(Constant.ATCGCOMPARETABLE);
				compareList.add(result);
			}
		}
		// 前200对比表
		if (JSONObjectIsNotEmpty(data,"rankCompareTab")){
			JSONObject rank = data.getJSONObject("rankCompareTab");
			HistogramTable result = getCompareTab(rank,Constant.CLASSHEAD);
			if (null != result){
				result.setCompareTable(Constant.TWOHUNDREDCOMPARETABLE);
				compareList.add(result);
			}
		}	
	}
	
	private void sortHistogramTable(HistogramTable result) {
		// 柱状图排序
		List<HistogramChart> histogram = result.getHistogram();
		Collections.sort(histogram, new Comparator<HistogramChart>() {
			@Override
			public int compare(HistogramChart o1, HistogramChart o2) {
		    	  return (o1.getX()).compareTo(o2.getX());
			}	
		});	
		// 表格数据排序
		List<CompareTable> tableList = result.getTable();
		Map<String,String[]> map = result.getSortMap();
		if (map.size() > 0){
			for(int i = 0;i < tableList.size();i++){			
				Iterator<String> it = map.keySet().iterator();
				int k = 0;
				CompareTable table = tableList.get(i);
				while(it.hasNext()){
				       String key = it.next();
				       String[] value = map.get(key);
				       String val = value[i];
				       setTableColumn(k, table, key, val);
				       k++;
				}
			}	
		}	
	}

	private void setTableColumn(int k, CompareTable table, String key,
			String val) {
		switch(k){
			case 0:
				table.setHeaderOne(key);
				table.setCellOne(val);
			break;
			case 1:
				table.setHeaderTwo(key);
		        table.setCellTwo(val);
			break;
			case 2:
				table.setHeaderThree(key);
		        table.setCellThree(val);
			break;
			case 3:
				table.setHeaderFour(key);
		        table.setCellFour(val);
		    break;    
			case 4:
				table.setHeaderFive(key);
		        table.setCellFive(val);
			break;
			case 5:
				table.setHeaderSix(key);
		        table.setCellSix(val);
			break;
			case 6:
				table.setHeaderSeven(key);
		        table.setCellSeven(val);
			break;
			case 7:
				table.setHeaderEight(key);
		        table.setCellEight(val);
			break;
			case 8:
				table.setHeaderNine(key);
		        table.setCellNine(val);
			break;
			case 9:
				table.setHeaderTen(key);
		        table.setCellTen(val);
			break; 
			case 10:
				table.setHeaderEleven(key); 
		        table.setCellEleven(val);
			break;
			case 11:
				table.setHeaderTwelve(key);
		        table.setCellTwelve(val);
			break;
			case 12:
				table.setHeaderThirteen(key);
		        table.setCellThirteen(val);
			break;
			case 13:
				table.setHeaderFourteen(key);
		        table.setCellFourteen(val);
			case 14:
				table.setHeaderFifteen(key);
		        table.setCellFifteen(val);
			break;
			case 15:
				table.setHeaderSixteen(key);
		        table.setCellSixteen(val);
			break;
			case 16:
				table.setHeaderSeventeen(key);
		        table.setCellSeventeen(val);
			break;
			case 17:
				table.setHeaderEighteen(key);
		        table.setCellEighteen(val);
			break;
			case 18:
				table.setHeaderNineteen(key);
		        table.setCellNineteen(val);
			break;
			case 19:
				table.setHeaderTwenty(key);
		        table.setCellTwenty(val);
			break;
			case 20:
				table.setHeaderTwentyOne(key);
		        table.setCellTwentyOne(val);
			break;
			case 21:
				table.setHeaderTwentyTwo(key);
		        table.setCellTwentyTwo(val);
			break;
			case 22:
				table.setHeaderTwentyThree(key);
		        table.setCellTwentyThree(val);
			break;
			case 23:
				table.setHeaderTwentyFour(key);
		        table.setCellTwentyFour(val);
			case 24:
				table.setHeaderTwentyFive(key);
		        table.setCellTwentyFive(val);
			break;
			case 25:
				table.setHeaderTwentySix(key);
		        table.setCellTwentySix(val);
			break;
			case 26:
				table.setHeaderTwentySeven(key);
		        table.setCellTwentySeven(val);
			break;
			case 27:
				table.setHeaderTwentyEight(key);
		        table.setCellTwentyEight(val);
			break;
			case 28:
				table.setHeaderTwentyNine(key);
		        table.setCellTwentyNine(val);
			break;
			case 29:
				table.setHeaderThirty(key);
		        table.setCellThirty(val);
			break; 
			case 30:
				table.setHeaderThirtyOne(key); 
		        table.setCellThirtyOne(val);
			break;
			case 31:
				table.setHeaderThirtyTwo(key);
		        table.setCellThirtyTwo(val);
			break;
			case 32:
				table.setHeaderThirtyThree(key);
		        table.setCellThirtyThree(val);
			break;
			case 33:
				table.setHeaderThirtyFour(key);
		        table.setCellThirtyFour(val);
			case 34:
				table.setHeaderThirtyFive(key);
		        table.setCellThirtyFive(val);
			break;
		    }
	}
	
	// ---------填充柱状图及表格数据---------
	private List<GradeMiddle> getHistogramTable(List<HistogramTable> middle,List<Integer> list) {
		List<GradeMiddle> middleList = null;
		int lenght = middle.size();
		if (lenght > 0){
			middleList = new ArrayList<GradeMiddle>();
			int size = lenght/2 + ((lenght%2) > 0 ? 1 : 0);	
			for(int i = 0;i < size; i++){
				int k = i * 2 + 0;
				GradeMiddle m = new GradeMiddle();
				middleList.add(m);
				if (k >= lenght)break;
				m.setTableOne(middle.get(k).getTable());
				m.setCompareTableOne(middle.get(k).getCompareTable());
				m.setHistogramOne(middle.get(k).getHistogram());
				list.add(middle.get(k).getHistogram().size());
				k = i * 2 + 1;
				if (k >= lenght)break;
			    m.setTableTwo(middle.get(k).getTable());
			    m.setCompareTableTwo(middle.get(k).getCompareTable());
				m.setHistogramTwo(middle.get(k).getHistogram());
				list.add(middle.get(k).getHistogram().size());
			}
		}
		return middleList;
	}
	
	public static String doubleTrans(double d){
		  if(Math.round(d)-d==0){
		   return String.valueOf((int)d);
		  }
		  return String.valueOf(d);
	}
	
	// ---------获取柱状图及表格数据--------
	private HistogramTable getCompareTab(JSONObject trace,String headName) {
		List<List<Double>> yzList = new ArrayList<List<Double>>();
		List<String> headerList = new ArrayList<String>();
		HistogramTable result = null;
		// y轴坐标值		
		if (JSONObjectIsNotEmpty(trace,"series")){
			JSONArray array = trace.getJSONArray("series");
			for(int i = 0;i < array.size();i++){
				JSONObject obj = array.getJSONObject(i);
				String cell = obj.get("data").toString();	
				if (StringUtils.isNotEmpty(cell)){
					List<Double> yz = JSON.parseArray(cell, Double.class);
					if (CollectionUtils.isNotEmpty(yz)){
					yzList.add(yz);
					String headerName = "";
					if(obj.containsKey("name")){
					   headerName = obj.get("name").toString();
					}
					headerList.add(headerName);
					}
				}
			}
		}
		// x轴坐标值
		TreeMap<String,String[]> map = new TreeMap<String,String[]>();
		if (JSONObjectIsNotEmpty(trace,"xAxis")){
			String bjmc = trace.get("xAxis").toString();	
			if (StringUtils.isNotEmpty(bjmc)){
				List<String> xz = JSON.parseArray(bjmc, String.class);
				if (xz.size() > 0 && yzList.size() > 0) {
					result = new HistogramTable();
					List<HistogramChart> histogram = new ArrayList<HistogramChart>();
					List<CompareTable> tableList = new ArrayList<CompareTable>();
					for(int r = 0; r < xz.size(); r++){
						HistogramChart column = new HistogramChart();
						column.setX(xz.get(r));
						String[] value = new String[]{"","","",""};
						for(int i = 0; i < yzList.size();i++){	
							List<Double> yz = yzList.get(i);
							if (i == 0){
								column.setY1(yz.get(r));
								value[0] = doubleTrans(yz.get(r));
							}else if(i == 1){
								column.setY2(yz.get(r));
								value[1] = doubleTrans(yz.get(r));
							}else if(i == 2){
								column.setY3(yz.get(r));
								value[2] = doubleTrans(yz.get(r));
							}else if(i == 3){
								column.setY4(yz.get(r));
								value[3] = doubleTrans(yz.get(r));
							}							
						}
						histogram.add(column);
						map.put(xz.get(r), value);
					}					
					for(int j = 0; j < yzList.size();j++){	
						CompareTable table = new CompareTable();
						table.setRowItem(headerList.get(j));
						table.setRowHeader(headName);
						List<Double> yz = yzList.get(j);
						if (xz.size() == yz.size()){
							for(int k = 0; k < xz.size(); k++){	
								String key = xz.get(k);
								String val = doubleTrans(yz.get(k));
								setTableColumn(k,table,key,val);
						}
					}
			        tableList.add(table);			
				}
				result.setSortMap(map);
				result.setHistogram(histogram);
				result.setTable(tableList);	
			  }
			}
		}
		return result;
	}	
}