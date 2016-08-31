package org.laopopo.console.web.controller;

import java.util.Map;

import javax.annotation.Resource;

import org.laopopo.console.info.kaleidoscope.KaleidoscopeInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class MonitorCoreController {

	private static final Logger logger = LoggerFactory.getLogger(MonitorCoreController.class);
	
	@Resource
	private KaleidoscopeInfo kaleidoscopeInfo;

	@ResponseBody
	@RequestMapping(value = "/index.do")
	public Map<String, Object> initTable(@RequestParam Map<String, Object> requestMap) {
		int pageSize = 10;
		int offset = 1;
		try {
			pageSize = Integer.valueOf("" + requestMap.get("limit")); // 页大小
		} catch (NumberFormatException e) {
			logger.info(e.getMessage());
		}
		try {
			offset = Integer.valueOf("" + requestMap.get("offset"));
		} catch (NumberFormatException e) {
			logger.info(e.getMessage());
		}


		Map<String, Object> resultMap = kaleidoscopeInfo.findInfoByPage(pageSize,offset);
		return resultMap;
	}

}
