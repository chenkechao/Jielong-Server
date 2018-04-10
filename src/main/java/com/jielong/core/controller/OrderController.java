package com.jielong.core.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.jielong.core.beans.ResponseBean;
import com.jielong.core.domain.Order;
import com.jielong.core.service.OrderService;
import com.jielong.core.service.OrderServiceTest;

@RestController
@RequestMapping("/order")
public class OrderController {
	
	@Autowired
	OrderService orderService;
	
	@RequestMapping("/insert")
	public ResponseBean<Integer> insert(Order order){
	    return orderService.insert(order);	
	}

}
