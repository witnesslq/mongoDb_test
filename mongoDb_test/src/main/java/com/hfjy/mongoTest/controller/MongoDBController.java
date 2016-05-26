package com.hfjy.mongoTest.controller;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.hfjy.mongoTest.entity.RoomEventDetail;
import com.hfjy.mongoTest.entity.RoomEventEntity;
import com.hfjy.mongoTest.entity.RtcEventDetail;
import com.hfjy.mongoTest.entity.RtcEventEntity;
import com.hfjy.mongoTest.service.MongoDBService;
import com.hfjy.mongoTest.utils.DateUtils;
import com.hfjy.mongoTest.utils.StringUtils;
/**
 * mongoDB 查询的controller 
 * @author leo-zeng
 *
 */
@Controller
@RequestMapping("mongo")
public class MongoDBController {
	@Autowired
	private MongoDBService mongoDBService;
	private static final Logger log=Logger.getLogger(MongoDBController.class);
	/**
	 * 查询维度，根据roomId查询房间具体日志信息
	 * @return
	 */
	@RequestMapping("queryDetailInfo")
	public String queryDetailInfo(@RequestParam(required =false,value="roomId")String roomId,Model model){
		Map<String,Object> coMap = new HashMap<String, Object>();
		coMap.put("roomId", roomId);
		try {
			List<RtcEventDetail> rctEvents =mongoDBService.queryRtcEventDetail(coMap, "RtcEvent");
			for (RtcEventDetail rtcEventDetail : rctEvents) {
				rtcEventDetail.setInsertTime(DateUtils.formatDate(new Date(Long.parseLong(rtcEventDetail.getInsertTime())), "MM/dd HH:mm:ss"));
			}
			//获取roomEvent详细信息
			List<RoomEventDetail> roomEvents = mongoDBService.queryRoomEventDetail(coMap, "RoomEvent");
			for (RoomEventDetail roomEventDetail : roomEvents) {
				roomEventDetail.setInsertTime(DateUtils.formatDate(new Date(Long.parseLong(roomEventDetail.getInsertTime())), "MM/dd HH:mm:ss"));
			}
			model.addAttribute("rctEvents", rctEvents);
			model.addAttribute("roomEvents", roomEvents);
		} catch (Exception e) {
			e.printStackTrace();
			log.debug(e.getMessage());
		}
		return "modules/bi/detailInfoLogs";
	}
	
	@RequestMapping("group")
	@ResponseBody
	public Object group(){
		Map<String,Object> coMap = new HashMap<String, Object>();
		Map<String,Object> initial = new HashMap<String, Object>();
		coMap.put("key", "roomId");
		initial.put("teachers", new HashMap<String, Object>());
		initial.put("students", new HashMap<String, Object>());
		coMap.put("initial", initial);
		coMap.put("cond", new HashMap<String,Object>());
		
		try {
			List<Object> data =mongoDBService.groupByLessonCount(coMap, "lessonCountLog");
			return data;
		} catch (Exception e) {
			return e.getMessage();
		}
	}
	/**
	 * 查询
	 * @param roomId
	 * @return
	 */
	@RequestMapping("groupRoomEvent")
	public String groupRoomEvent(@RequestParam(required =false,value="roomId")String roomId,Model model,
			@RequestParam(required =false,value="weekStatus")String weekStatus,
			@RequestParam(required=false,value="userId") String userId){
		Map<String,Object> coMap = new HashMap<String, Object>();
		if(StringUtils.isNotEmpty(roomId)){
			coMap.put("roomId", roomId);
		}
		if (StringUtils.isNotEmpty(weekStatus)) {
			coMap.put("weekStatus", weekStatus);
		}else{
			coMap.put("weekStatus", "-1");
		}
		if (StringUtils.isNotEmpty(userId)) {
			coMap.put("userId", userId);
		}
		coMap.put("conditions", true);
		try {
			List<RoomEventEntity> data =mongoDBService.groupRoomEvent(coMap, "RoomEvent");
			model.addAttribute("roomEvents", data);
		} catch (Exception e) {
			e.printStackTrace();
			log.debug(e.getMessage());
		}
		return "modules/bi/bi";
	}
	
	@RequestMapping("groupRtcEvent")
	@ResponseBody
	public Object groupRtcEvent(@RequestParam(required =false,value="roomId")String roomId){
		Map<String,Object> coMap = new HashMap<String, Object>();
		Map<String, Object> dataMap=new HashMap<>();
		if(StringUtils.isNotEmpty(roomId)){
			coMap.put("roomId", roomId);
		}
		coMap.put("conditions", true);
		List<RtcEventEntity> data =new ArrayList<RtcEventEntity>();
		try {
			 data =mongoDBService.queryRtcEvent(coMap, "RtcEvent");
			if (data!=null&&data.size()>0) {
				dataMap.put("code", 1);
				dataMap.put("data", data);
			}else {
				dataMap.put("code", 0);
			}
		} catch (Exception e) {
			log.debug(e.getMessage());
			e.printStackTrace();
		}
		return dataMap;
	}
}
