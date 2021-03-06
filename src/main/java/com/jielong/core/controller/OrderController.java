package com.jielong.core.controller;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.jielong.core.beans.PickBean;
import com.jielong.core.beans.PickCountBean;
import com.jielong.core.beans.ResponseBean;
import com.jielong.core.beans.SignPickBean;
import com.jielong.core.domain.Order;
import com.jielong.core.service.OrderGroupService;
import com.jielong.core.service.OrderService;
import com.jielong.core.service.UserAddressService;



@RestController
@RequestMapping("/order")
public class OrderController {

	@Autowired
	OrderService orderService;
	@Autowired
	OrderGroupService orderGroupService;
	@Autowired
	UserAddressService userAddressService;

	@RequestMapping("/insert")
	public ResponseBean<Integer> insert(@RequestBody Order order) {
		Integer isSetGroup = order.getIsSetGroup();
		if (isSetGroup == 1) {
			return orderGroupService.insert(order);
		} else {
			return orderService.insert(order);
		}

	}
	
	
	@RequestMapping("/insertWithPay")
	public ResponseBean<Map<String, String>> insertWithPay(@RequestBody Order order) {
		Integer isSetGroup = order.getIsSetGroup();
		if (isSetGroup == 1) {
			return orderGroupService.insertWithPay(order);
		} else {
			return orderService.insertWithPay(order);
		}

	}

	/**
	 * 根据顾客id查询参与的接龙
	 * 
	 * @param customerId
	 * @return
	 */
	@RequestMapping("/selectByCustomerId")
	public ResponseBean<List<Order>> selectByCustomerId(@RequestParam("customerId") Integer customerId) {
		List<Order> orderList = new ArrayList<Order>();
		List<Order> orderList1 = orderService.selectByCustomerId(customerId).getData();
		List<Order> orderList2 = orderGroupService.selectByCustomerId(customerId).getData();
		if (orderList1 != null) {
			orderList.addAll(orderList1);
		}
		if (orderList2 != null) {
			orderList.addAll(orderList2);
		}
		orderList=orderList.stream().sorted(Comparator.comparing(Order::getCreatedAt).reversed()).collect(Collectors.toList());
		return new ResponseBean<List<Order>>(orderList);
	}

	/**
	 * 自提标记
	 * 
	 * @param orderNumList
	 *            订单列表
	 * @return 受影响记录数
	 */
	@RequestMapping("/signPick")
	public ResponseBean<Integer> signPick(@RequestBody SignPickBean signPickBean) {
		int result = 0;
		Integer result1 = orderService.signPick(signPickBean).getData();
		Integer result2 = orderGroupService.signPick(signPickBean).getData();
		if (result1 != null) {
			result += result1;
		}
		if (result2 != null) {
			result += result2;
		}
		return new ResponseBean<Integer>(result);
	}

	/**
	 * 按提货地点和时间查询所有待提货和已提货的订单
	 * 偷懒了
	 * @param jielongId
	 * @return
	 */
	@RequestMapping("/selectPickOrder")
	public ResponseBean<List> selectOrder(@RequestParam("jielongId") Integer jielongId) {
		List<Order> orderList1 = orderService.selectByJielongId(jielongId).getData();
		List<Order> orderList2 = orderGroupService.selectPickByJielongId(jielongId).getData();
		List<Order> orderList = new ArrayList<Order>();
		if (orderList1 != null) {
			orderList.addAll(orderList1.stream().filter(order -> order.getState() == 2 || order.getState()==3).collect(Collectors.toList()));
		}
		if (orderList2 != null) {
			orderList.addAll(orderList2);
		}		
		//List<Map<String, List<Order>>> responseList=new ArrayList<Map<String, List<Order>>>();
		List<List> responseList=new ArrayList<List>();
		
		//按addressId分组
		Map<Integer, List<Order>> orderByStateMap=orderList.stream().collect(Collectors.groupingBy(Order::getAddressId));
	    if (orderByStateMap!=null) {
			for(Map.Entry<Integer, List<Order>> entry : orderByStateMap.entrySet()) {
				Map<Integer, List<Order>> map=new HashMap<Integer, List<Order>>();
				String addressInfo=userAddressService.selectById(entry.getKey()).getData().getDetail();
				String address=addressInfo.replace("***", "  ");
				
				List order=new ArrayList<>();
			    order.add(address);
			    order.add(entry.getValue());
			    
				responseList.add(order);
				
			}
		}
                
		return new ResponseBean<List>(responseList);
	}

	// 接龙统计
	@RequestMapping("/pickCount")
	public ResponseBean<List<PickCountBean>> pickCount(@RequestParam("jielongId") Integer jielongId,
			@RequestParam(value = "startTime", required = false) String startTime,
			@RequestParam(value = "endTime", required = false) String endTime) {
		
		List<PickCountBean> groupList = orderGroupService.countPick(jielongId).getData();
		List<PickCountBean> directList = orderService.countPick(jielongId).getData();

		List<PickCountBean> countList = new ArrayList<PickCountBean>();
		
		if (directList != null && directList.size() > 0) {
			for(int i=0;i<directList.size();i++) {
			   PickCountBean pickCountBean=directList.get(i);
			   
			   if (pickCountBean.getPickBeans() != null) {
					List<PickBean> pickBeans = pickCountBean.getPickBeans();
					Integer sellSum=0;
					Integer joinPeopleSum=0;
					BigDecimal moneySum=new BigDecimal(0);
					if (startTime != null && endTime != null) {
						pickBeans = pickCountBean.getPickBeans().stream()
								.filter(pickBean -> startTime.compareTo(pickBean.getCreatedAt().split(" ")[0]) <= 0)
								.filter(pickBean -> endTime.compareTo(pickBean.getCreatedAt().split(" ")[0]) >= 0)
								.collect(Collectors.toList());

					}
					for(PickBean pickBean: pickBeans) { 
						joinPeopleSum+=1;
						sellSum+=pickBean.getGoodsSum();
						moneySum=moneySum.add(pickBean.getPrice().multiply(new BigDecimal(pickBean.getGoodsSum())));
						
					}
					pickCountBean.setMoneySum(moneySum);
					pickCountBean.setSellSum(sellSum);
					pickCountBean.setJoinPeopleSum(joinPeopleSum);
					pickCountBean.setPickBeans(pickBeans);

				}
			   PickCountBean pickCountBean2=groupList.get(i);
			   if (pickCountBean2.getPickBeans() != null) {
					List<PickBean> pickBeans = pickCountBean2.getPickBeans();
					Integer sellSum=0;
					Integer joinPeopleSum=0;
					BigDecimal moneySum=new BigDecimal(0);
					if (startTime != null && endTime != null) {
						pickBeans = pickCountBean2.getPickBeans().stream()
								.filter(pickBean -> startTime.compareTo(pickBean.getCreatedAt().split(" ")[0]) <= 0)
								.filter(pickBean -> endTime.compareTo(pickBean.getCreatedAt().split(" ")[0]) >= 0)
								.collect(Collectors.toList());

					}
					for(PickBean pickBean: pickBeans) { 
						joinPeopleSum+=1;
						sellSum+=pickBean.getGoodsSum();
						moneySum=moneySum.add(pickBean.getPrice().multiply(new BigDecimal(pickBean.getGoodsSum())));
						
					}
					pickCountBean.setMoneySum(moneySum);
					pickCountBean.setSellSum(sellSum);
					pickCountBean.setJoinPeopleSum(joinPeopleSum);
					pickCountBean.setPickBeans(pickBeans);

				}
			  
				countList.add(pickCountBean);
			   
			}

		
		}
		return new ResponseBean<List<PickCountBean>>(countList);

	}
	
	//取消参团
	@RequestMapping("/cancelJoinGroup")
    public ResponseBean<Integer> cancelJoinGroup(@RequestBody Order order){
		
		return orderGroupService.cancelJoinGroup(order);
	}	
	
	 /**
	   * 直接下单的方式：取消订单
	  */
	 @RequestMapping("/cancelOrder")
	 public ResponseBean<Integer> cancelOrder(@RequestParam("orderId") Integer orderId){
			
			return orderService.cancelOrder(orderId);
	}	
		

}
