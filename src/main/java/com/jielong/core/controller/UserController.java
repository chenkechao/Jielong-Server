package com.jielong.core.controller;

import java.util.Map;

import com.jielong.core.dao.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.jielong.core.beans.ResponseBean;
import com.jielong.core.domain.User;
import com.jielong.core.service.UserService;

@RestController
@RequestMapping("/user")
public class UserController {
	
   @Autowired
   UserService userService;
   
   @Autowired
   UserMapper userMapper;
	
  	
   @RequestMapping("/login") 	
   public ResponseBean<Map<String, Object>> login(@RequestParam("code") String code,@RequestParam(value="parentUserId",required = false) Integer parentId){
	  
	  return userService.login(code,parentId);
	     
   }
   
   
   /**
    * 更新用户状态  post
    * @param user 需携带两个参数 id(用户的id)、 state(被拉黑时为0，可用为1) 
    * @return
    */
   @RequestMapping("/updateState")
   public ResponseBean<Integer> updateState(@RequestBody User user){
	
	   ResponseBean<Integer> result=userService.updateState(user);
	   return result;
   }

}
