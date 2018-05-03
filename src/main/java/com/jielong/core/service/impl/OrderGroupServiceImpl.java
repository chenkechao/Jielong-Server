package com.jielong.core.service.impl;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.jielong.base.util.Utils;
import com.jielong.core.beans.PickBean;
import com.jielong.core.beans.PickCountBean;
import com.jielong.core.beans.ResponseBean;
import com.jielong.core.beans.SignBean;
import com.jielong.core.beans.SignPickBean;
import com.jielong.core.dao.CommonDao;
import com.jielong.core.dao.GoodsMapper;
import com.jielong.core.dao.JielongMapper;
import com.jielong.core.dao.OrderGroupConsoleMapper;
import com.jielong.core.dao.OrderGroupMapper;
import com.jielong.core.dao.OrderMapper;
import com.jielong.core.domain.Goods;
import com.jielong.core.domain.Jielong;
import com.jielong.core.domain.Order;
import com.jielong.core.domain.OrderGoods;
import com.jielong.core.domain.OrderGroup;
import com.jielong.core.domain.OrderGroupConsole;
import com.jielong.core.domain.UserAddress;
import com.jielong.core.domain.UserInfo;
import com.jielong.core.domain.UserMessage;
import com.jielong.core.service.JielongService;
import com.jielong.core.service.OrderGroupService;
import com.jielong.core.service.UserAddressService;
import com.jielong.core.service.UserInfoService;
import com.jielong.core.service.UserMessageService;

@Service
public class OrderGroupServiceImpl implements OrderGroupService {

	@Autowired
	CommonDao commonDao;
	@Autowired
	OrderMapper orderMapper;
	@Autowired
	OrderGroupConsoleMapper orderGroupConsoleMapper;
	@Autowired
	OrderGroupMapper orderGroupMapper;
	@Autowired
	GoodsMapper goodsMapper;
	@Autowired
	JielongService jielongService;
	@Autowired
	UserMessageService userMessageService;
	@Autowired
	UserAddressService userAddressService;
	@Autowired
	UserInfoService userInfoService;
	@Autowired
	JielongMapper jielongMapper;

	@Transactional
	@Override
	public ResponseBean<Integer> insert(Order order) {
		ResponseBean<Integer> responseBean = new ResponseBean<Integer>();
		String orderNum = Utils.createFileName();
		List<OrderGoods> orderGoodsList = order.getOrderGoods();
		if (orderGoodsList != null && orderGoodsList.size() > 0) {
			
			StringBuilder sp = new StringBuilder();  //商品名称
			String addressInfo="";
			
			for (int i = 0; i < orderGoodsList.size(); i++) {
				
				UserAddress address=userAddressService.selectById(order.getAddressId()).getData();
				addressInfo=address.getDetail();
				
				OrderGroup orderGroupGoods = new OrderGroup();
				// 订单编号
				orderGroupGoods.setOrderId(orderNum);
				// 接龙ID
				orderGroupGoods.setJielongId(order.getJielongId());
				// 购买者ID
				orderGroupGoods.setCustId(order.getUserId());
				orderGroupGoods.setCustName(order.getUserName());
				orderGroupGoods.setCustPhone(order.getUserPhone());
				orderGroupGoods.setCustNote(order.getRemark());
				orderGroupGoods.setAddressId(order.getAddressId());

				OrderGoods orderGoods = orderGoodsList.get(i);
				orderGroupGoods.setGoodsId(orderGoods.getGoodsId());
				orderGroupGoods.setCustBuyNum(orderGoods.getSum());
				orderGroupGoods.setCustBuyPrice(orderGoods.getMoney());
				BigDecimal buyAllMoney = new BigDecimal(0);
				buyAllMoney = orderGoods.getMoney().multiply(new BigDecimal(orderGoods.getSum()));
				orderGroupGoods.setCustBuyAllMoney(buyAllMoney);
				// 交易状态
				orderGroupGoods.setTradeFlg(0);
				// 订单状态
				orderGroupGoods.setOrderFlg(0);
				
			
				

				orderGroupMapper.insertSelective(orderGroupGoods);

				// 取得接龙商品的成团数
				Goods goods = new Goods();
				goods = goodsMapper.selectByPrimaryKey(orderGoods.getGoodsId());
				sp.append(goods.getName());
				int setGroupNum = Integer.valueOf(goods.getGroupSum());

				int newGroupNum = Optional
						.ofNullable(orderGroupMapper.selectByCustBuyNum(order.getJielongId(), orderGoods.getGoodsId()))
						.orElse(0);
				if (newGroupNum >= setGroupNum) {
					// 成团状态
					// 查看ordergroupconsole表的GroupOkFlg状态如果已经是1了
					Integer oldGroupOkFlg = orderGroupConsoleMapper.selectGroupOkState(order.getJielongId(),
							orderGoods.getGoodsId());
					if (oldGroupOkFlg == 1) {
						// 发送单人通知 已经是成功的团了。
						UserMessage userMessage = new UserMessage();
						userMessage.setUserId(order.getUserId());
					//	userMessage.setTitle("拼团成功通知！");
					//	userMessage.setMessage("您已成功参团，拼团成功，如在接龙结束后拼团依然成功，即可上门提货！订单详情请前往我的->我参与的接龙查看.");
						userMessage.setTitle("下单成功通知！");
					//	userMessage.setMessage("恭喜您，下单成功，您购买的订单已成团，订单详情请前往我的->我参与的团购查看.");
						userMessage.setMessage("恭喜您，下单成功，你购买了"+sp.toString()+",请于"+addressInfo+"提货，如需修改订单，您可以在我的-我参与的团购中找到下单记录，取消订单后重新下单。");
						userMessageService.insert(userMessage);
					} else {
						// 恭喜终于成团了。
						// 更新ordergroupconsole表
						int updateRet = orderGroupConsoleMapper.updateGroupOkFlg(1, order.getJielongId(),
								orderGoods.getGoodsId());

						// 下单之后给用户发送消息
						userMessageService.groupStateModify(order.getJielongId(), orderGoods.getGoodsId(), 1, sp.toString(),addressInfo);
					}

				} else {
					// 成团状态
					// 查看ordergroupconsole表的GroupOkFlg状态如果已经是1了
					Integer oldGroupOkFlg = orderGroupConsoleMapper.selectGroupOkState(order.getJielongId(),
							orderGoods.getGoodsId());
					if (oldGroupOkFlg != null) {
						if (oldGroupOkFlg == 1) {
							// 有撤单的情况!从成团变成了 未成团。群发通知
							int updateret = orderGroupConsoleMapper.updateGroupOkFlg(0, order.getJielongId(),
									orderGoods.getGoodsId());

							// 下单之后给用户发送消息
							userMessageService.groupStateModify(order.getJielongId(), orderGoods.getGoodsId(), 0,goods.getName(),addressInfo);

						} else {
							// 发送单人通知
							// 下单之后给用户发送消息
							UserMessage userMessage = new UserMessage();
							userMessage.setUserId(order.getUserId());
						//	userMessage.setTitle("参团成功通知！");
						//	userMessage.setMessage("您已成功参团，拼团人数暂不足，请等候拼团成功！订单详情请前往我的->我参与的接龙查看。");
							userMessage.setTitle("下单成功通知");
							userMessage.setMessage("恭喜您下单成功，您购买了"+goods.getName()+",本次团购的最小成团数量是"+setGroupNum+"，订单详情可前往我的->我参与的团购查看。转发到微信群，可以帮助团长一起促成团购哦！");
							userMessageService.insert(userMessage);
						}
					}

				}
				// 减少对应商品的库存
				goodsMapper.updateRepertory(orderGoods.getGoodsId(), orderGoods.getSum());
				// 下单之后，更新接龙参与人数、参与金额等信息
				jielongService.updateJoin(order.getJielongId(), buyAllMoney);

			}
			responseBean.setData(1);
			return responseBean;
		}

		return null;
	}

	// 根据顾客id查询订单(参与的接龙)
	@Transactional
	@Override
	public ResponseBean<List<Order>> selectByCustomerId(Integer userId) {
		ResponseBean<List<Order>> responseBean = new ResponseBean<List<Order>>();
		List<OrderGroup> orderGroupList = orderGroupMapper.selectByCustId(userId);
		// 转换输出格式
		List<Order> orderList = new ArrayList<Order>();
		if (orderGroupList != null && orderGroupList.size() > 0) {
			for (OrderGroup ordergroup : orderGroupList) {
				// 转换输出格式
				Order order = new Order();
				order.setId(ordergroup.getId());
				order.setIsSetGroup(1);
				order.setJielongId(ordergroup.getJielongId());
				order.setOrderNum(ordergroup.getOrderId());
				order.setRemark(ordergroup.getCustNote());
				order.setState(ordergroup.getTradeFlg());

				order.setSumMoney(ordergroup.getCustBuyAllMoney());

				order.setUserId(ordergroup.getCustId());

				order.setUserName(ordergroup.getCustName());
				order.setUserPhone(ordergroup.getCustPhone());
				order.setAddressId(ordergroup.getAddressId());
				order.setCreatedAt(ordergroup.getCreatedAt());
				order.setUpdatedAt(ordergroup.getUpdatedAt());

				// Jielong主题
				String topic = jielongMapper.selectTopic(ordergroup.getJielongId());
				order.setJielongTopic(topic);
				// 提货地址信息
				Integer addressId = ordergroup.getAddressId();
				UserAddress address = userAddressService.selectById(addressId).getData();
				order.setUserAddress(address);
				// 用户信息
				Integer clientId = ordergroup.getCustId();
				UserInfo userInfo = userInfoService.selectByUserId(clientId).getData();
				order.setUserInfo(userInfo);

				// 订单商品信息
				List<OrderGoods> orderGoodsList = new ArrayList<OrderGoods>();

				// 对应统一接口 ORDER
				Goods goods = goodsMapper.selectByPrimaryKey(ordergroup.getGoodsId());
				OrderGoods orderGoods = new OrderGoods();
				orderGoods.setGoods(goods);
				orderGoods.setGoodsId(ordergroup.getGoodsId());
				// orderGoods.setId();
				orderGoods.setMoney(ordergroup.getCustBuyPrice());
				// orderGoods.setOrderId(orderGroup.getOrderId());
				orderGoods.setSum(ordergroup.getCustBuyNum());

				OrderGroupConsole orderGroupConsole = orderGroupConsoleMapper
						.selectByJielongAndGoods(ordergroup.getJielongId(), ordergroup.getGoodsId());

				Integer orderFlg = ordergroup.getOrderFlg();

				if (orderFlg == 1) {// (Jielong结束,参团失败) 或者 (订单取消) 订单取消state=4
					Integer jielongState = orderGroupConsole.getConsoleFlg();
					if (jielongState == 1) { // 1表示Jielong结束
						orderGoods.setGroupFlg(2);   //groupFlg==2 表示参团失败
					}else {  //取消订单情况
						order.setState(4);
					}

				} else {
					Integer groupOkFlg = orderGroupConsole.getGroupOkFlg();
					if (groupOkFlg == 1) {
						// 参团成功
						orderGoods.setGroupFlg(groupOkFlg);
						orderGoods.setJoinGroupNum(0);
					} else {

						orderGoods.setGroupFlg(0);
						// 待拼团成功，差几人计算
						Integer setGroupNum = Integer.valueOf(goods.getGroupSum());

						Integer newGroupNum = orderGroupMapper.selectByCustBuyNum(ordergroup.getJielongId(),
								ordergroup.getGoodsId());

						if (setGroupNum != null && newGroupNum != null) {
							int numtmp = setGroupNum - newGroupNum;
							orderGoods.setJoinGroupNum(numtmp);
						}
					}

				}

				orderGoodsList.add(orderGoods);
				order.setOrderGoods(orderGoodsList);
				orderList.add(order);
			}

		}

		responseBean.setData(orderList);
		return responseBean;
	}

	/**
	 * 根据发布者id查询订单
	 */
	@Transactional
	@Override
	public ResponseBean<List<Order>> selectByPublisherId(Integer userId) {
		ResponseBean<List<Order>> responseBean = new ResponseBean<List<Order>>();
		List<OrderGroup> orderGroupList = orderGroupMapper.selectByPublisherId(userId);
		// 转换输出格式
		List<Order> orderList = new ArrayList<Order>();

		if (orderGroupList != null && orderGroupList.size() > 0) {
			for (OrderGroup orderGroup : orderGroupList) {
				// 转换输出格式
				Order order = new Order();
				order.setId(orderGroup.getId());
				order.setIsSetGroup(1);
				order.setJielongId(orderGroup.getJielongId());
				// order.setJielongTopic(ordergroup);
				// order.setOrderGoods(ordergroup);
				order.setOrderNum(orderGroup.getOrderId());
				order.setRemark(orderGroup.getCustNote());
				order.setState(orderGroup.getTradeFlg());
				order.setSumMoney(orderGroup.getCustBuyAllMoney());
				// order.setUserAddress(userAddress);
				order.setUserId(orderGroup.getCustId());
				// order.setUserInfo(ordergroup.get);
				order.setUserName(orderGroup.getCustName());
				order.setUserPhone(orderGroup.getCustPhone());
				order.setCreatedAt(orderGroup.getCreatedAt());
				order.setUpdatedAt(orderGroup.getUpdatedAt());

				order.setAddressId(orderGroup.getAddressId());

				// Jielong主题
				String topic = jielongMapper.selectTopic(orderGroup.getJielongId());
				order.setJielongTopic(topic);
				// 提货地址信息
				Integer addressId = orderGroup.getAddressId();
				UserAddress address = userAddressService.selectById(addressId).getData();
				order.setUserAddress(address);
				// 用户信息
				Integer clientId = orderGroup.getCustId();
				UserInfo userInfo = userInfoService.selectByUserId(clientId).getData();
				order.setUserInfo(userInfo);

				// 订单商品信息
				List<OrderGroup> orderGroupList2 = orderGroupMapper.selectByOrderId(orderGroup.getOrderId());

				if (orderGroupList2 != null && orderGroupList2.size() > 0) {
					List<OrderGoods> orderGoodsList = new ArrayList<OrderGoods>();
					for (OrderGroup orderGroup2 : orderGroupList2) {

						// 对应统一接口 ORDER
						Goods goods = goodsMapper.selectByPrimaryKey(orderGroup2.getGoodsId());
						OrderGoods orderGoods = new OrderGoods();
						orderGoods.setGoods(goods);
						orderGoods.setGoodsId(orderGroup2.getGoodsId());
						// orderGoods.setId();
						orderGoods.setMoney(orderGroup2.getCustBuyPrice());
						// orderGoods.setOrderId(orderGroup.getOrderId());
						orderGoods.setSum(orderGroup2.getCustBuyNum());

						// 商品成功成团与否FLG
						Integer groupOkFlg = orderGroupConsoleMapper.selectGroupOkState(orderGroup2.getJielongId(),
								orderGroup2.getGoodsId());

						if (groupOkFlg == 1) {
							// 参团成功
							orderGoods.setGroupFlg(groupOkFlg);
							orderGoods.setJoinGroupNum(0);
						} else {
							orderGoods.setGroupFlg(groupOkFlg);
							// 参团不成功，差几人计算
							int setGroupNum = Integer.valueOf(goods.getGroupSum());

							int newGroupNum = orderGroupMapper.selectByCustBuyNum(orderGroup2.getJielongId(),
									orderGroup2.getGoodsId());

							int numtmp = setGroupNum - newGroupNum;
							orderGoods.setJoinGroupNum(numtmp);
						}

						orderGoodsList.add(orderGoods);
					}
					order.setOrderGoods(orderGoodsList);
				}
				orderList.add(order);
			}
		}

		responseBean.setData(orderList);
		return responseBean;
	}

	@Override
	// 计算已参团数量
	public int getGroupPeople(Integer jielongId, Integer goodsId) {
		int peopleSum = 0;
		// 商品成功成团与否FLG
		Integer groupOkFlg = orderGroupConsoleMapper.selectGroupOkState(jielongId, goodsId);
		if (groupOkFlg != null) {

			// 参团不成功，计算已参团数量

			Integer num = orderGroupMapper.selectByCustBuyNum(jielongId, goodsId);
			if (num != null) {
				peopleSum = num;
			}

		}

		return peopleSum;

	}

	@Transactional
	@Override
	public int closeJieLong(Integer jielongId) {

		// 传入为结束接龙ID
		// 1.判断结束的接龙是否是已经成团的接龙
		// 取得接龙ID的商品清单
		List<OrderGroupConsole> listOrderGroupConsole = orderGroupConsoleMapper.selectByJieLongId(jielongId);

		// 接龙名取得
		Jielong JielongName = jielongMapper.selectByPrimaryKey(jielongId);
		for (OrderGroupConsole orderGroupConsole : listOrderGroupConsole) {
			// 商品成功成团与否FLG
			if (orderGroupConsole.getGroupOkFlg() == 1) {
				// 拼团成功 最终结果更新
				// 1.关闭order_group_console表，状态关闭
				orderGroupConsoleMapper.updateLastStateFlg(1, orderGroupConsole.getJielongId(),
						orderGroupConsole.getGoodsId());

				// 2.关闭order_group表，状态关闭,trade_flg 0 -> 2,order_flg 0 -> 0 where trade_flg = 0
				// and order_flg = 0
				orderGroupMapper.updateLastStateFlg(2, 0, orderGroupConsole.getJielongId(),
						orderGroupConsole.getGoodsId());

				List<OrderGroup> orderGroupList = orderGroupMapper
						.selectByJieLongGoodsId(orderGroupConsole.getJielongId(), orderGroupConsole.getGoodsId());
				for (OrderGroup orderGroup : orderGroupList) {
					Goods goods = goodsMapper.selectByPrimaryKey(orderGroup.getGoodsId());
					// 拼团成功每个下单的客户消息发送，状态更新
					UserMessage userMessage = new UserMessage();
					userMessage.setUserId(orderGroup.getCustId());
					userMessage.setTitle("群发拼团成功通知！");
					String address=userAddressService.selectById(orderGroup.getAddressId()).getData().getDetail();
					//userMessage.setMessage(
					//		"恭喜您，截止接龙结束，" + JielongName.getTopic() + "的" + goods.getName() + "已拼团成功，请尽快上门提货！");
					userMessage.setMessage("亲爱的团员们，本次团购已结束。恭喜您购买到心仪的商品。团长会尽快备货，在"+address+"来为大家派发！");
					userMessageService.insert(userMessage);
				}

			} else {
				// 拼团失败 最终结果更新
				// 1.关闭order_group_console表，状态关闭
				orderGroupConsoleMapper.updateLastStateFlg(1, orderGroupConsole.getJielongId(),
						orderGroupConsole.getGoodsId());

				// 2.关闭order_group表，状态关闭,trade_flg 0 -> 0,order_flg 0 -> 1 where trade_flg = 0
				// and order_flg = 0
				orderGroupMapper.updateLastStateFlg(0, 1, orderGroupConsole.getJielongId(),
						orderGroupConsole.getGoodsId());

				// 拼团失败每个下单的客户消息发送，状态更新
				List<OrderGroup> orderGroupList = orderGroupMapper
						.selectByJieLongGoodsId(orderGroupConsole.getJielongId(), orderGroupConsole.getGoodsId());
				for (OrderGroup orderGroup : orderGroupList) {
					Goods goods = goodsMapper.selectByPrimaryKey(orderGroup.getGoodsId());
					// 拼团成功每个下单的客户消息发送，状态更新
					UserMessage userMessage = new UserMessage();
					userMessage.setUserId(orderGroup.getCustId());
					userMessage.setTitle("群发拼团失败通知！");
					userMessage.setMessage(
							"非常遗憾地告诉您，" + JielongName.getTopic() + "的" + goods.getName() + "未达到最小成团数量，本次团购未成功。您还可以去首页看看其他团购哦！");
					userMessageService.insert(userMessage);
				}

			}

		}

		// return 0 为接龙结束异常 1为正常
		return 1;
	}

	@Transactional
	@Override
	public ResponseBean<List<Order>> selectByJielongId(Integer jielongId) {
		// 根据接龙ID查询所有商品订单
		ResponseBean<List<Order>> responseBean = new ResponseBean<List<Order>>();
		List<OrderGroup> orderGroupList = orderGroupMapper.selectByJielongId(jielongId);
		// 转换输出格式
		List<Order> orderList = new ArrayList<Order>();
		if (orderGroupList != null && orderGroupList.size() > 0) {
			for (OrderGroup orderGroup : orderGroupList) {

				// 转换输出格式
				Order order = new Order();
				order.setId(orderGroup.getId());
				order.setIsSetGroup(1);
				order.setJielongId(orderGroup.getJielongId());
				// order.setJielongTopic(ordergroup);
				// order.setOrderGoods(ordergroup);
				order.setOrderNum(orderGroup.getOrderId());
				order.setRemark(orderGroup.getCustNote());
				order.setState(orderGroup.getTradeFlg());
				order.setSumMoney(orderGroup.getCustBuyAllMoney());
				// order.setUserAddress(userAddress);
				order.setUserId(orderGroup.getCustId());
				// order.setUserInfo(ordergroup.get);
				order.setUserName(orderGroup.getCustName());
				order.setUserPhone(orderGroup.getCustPhone());
				order.setCreatedAt(orderGroup.getCreatedAt());
				order.setUpdatedAt(orderGroup.getUpdatedAt());

				order.setAddressId(orderGroup.getAddressId());

				// Jielong主题
				String topic = jielongMapper.selectTopic(orderGroup.getJielongId());
				order.setJielongTopic(topic);
				// 提货地址信息
				Integer addressId = orderGroup.getAddressId();
				UserAddress address = userAddressService.selectById(addressId).getData();
				order.setUserAddress(address);
				// 用户信息
				Integer clientId = orderGroup.getCustId();
				UserInfo userInfo = userInfoService.selectByUserId(clientId).getData();
				order.setUserInfo(userInfo);

				// 订单商品信息
				List<OrderGroup> orderGroupList2 = orderGroupMapper.selectByOrderId(orderGroup.getOrderId());

				if (orderGroupList2 != null && orderGroupList2.size() > 0) {
					List<OrderGoods> orderGoodsList = new ArrayList<OrderGoods>();
					for (OrderGroup orderGroup2 : orderGroupList2) {

						// 对应统一接口 ORDER
						Goods goods = goodsMapper.selectByPrimaryKey(orderGroup2.getGoodsId());
						OrderGoods orderGoods = new OrderGoods();
						orderGoods.setGoods(goods);
						orderGoods.setGoodsId(orderGroup2.getGoodsId());
						// orderGoods.setId();
						orderGoods.setMoney(orderGroup2.getCustBuyPrice());
						// orderGoods.setOrderId(orderGroup.getOrderId());
						orderGoods.setSum(orderGroup2.getCustBuyNum());

						// 商品成功成团与否FLG
						Integer groupOkFlg = orderGroupConsoleMapper.selectGroupOkState(orderGroup2.getJielongId(),
								orderGroup2.getGoodsId());

						if (groupOkFlg == 1) {
							// 参团成功
							orderGoods.setGroupFlg(groupOkFlg);
							orderGoods.setJoinGroupNum(0);
						} else {
							orderGoods.setGroupFlg(groupOkFlg);
							// 参团不成功，差几人计算
							int setGroupNum = Integer.valueOf(goods.getGroupSum());

							int newGroupNum = orderGroupMapper.selectByCustBuyNum(orderGroup2.getJielongId(),
									orderGroup2.getGoodsId());

							int numtmp = setGroupNum - newGroupNum;
							orderGoods.setJoinGroupNum(numtmp);
						}

						orderGoodsList.add(orderGoods);
					}
					order.setOrderGoods(orderGoodsList);

				}
				orderList.add(order);

			}

		} // end if

		responseBean.setData(orderList);
		return responseBean;
	}

	@Override
	public ResponseBean<Integer> signPick(SignPickBean signPickBean) {
		ResponseBean<Integer> responseBean = new ResponseBean<Integer>();
		Integer result = 0;
		List<SignBean> signBeanList = signPickBean.getOrderNumList();
		if (signBeanList != null && signBeanList.size() > 0) {
			for (SignBean signBean : signBeanList) {
				result += orderGroupMapper.signPick(signBean);

			}

		}
		// ResponseBean<Integer> responseBean=new ResponseBean<Integer>();
		// Integer result=orderGroupMapper.signPick(signPickBean);
		responseBean.setData(result);
		return responseBean;
	}

	@Transactional
	@Override
	public ResponseBean<List<Order>> selectPickByJielongId(Integer jielongId) {
		// 根据接龙ID查询所有商品订单
		ResponseBean<List<Order>> responseBean = new ResponseBean<List<Order>>();
		List<OrderGroup> orderGroupList = orderGroupMapper.selectPickByJielongId(jielongId);
		// 转换输出格式
		List<Order> orderList = new ArrayList<Order>();
		if (orderGroupList != null && orderGroupList.size() > 0) {
			for (OrderGroup orderGroup : orderGroupList) {

				// 转换输出格式
				Order order = new Order();
				order.setId(orderGroup.getId());
				order.setIsSetGroup(1);
				order.setJielongId(orderGroup.getJielongId());
				// order.setJielongTopic(ordergroup);
				// order.setOrderGoods(ordergroup);
				order.setOrderNum(orderGroup.getOrderId());
				order.setRemark(orderGroup.getCustNote());
				order.setState(orderGroup.getTradeFlg());
				order.setSumMoney(orderGroup.getCustBuyAllMoney());
				// order.setUserAddress(userAddress);
				order.setUserId(orderGroup.getCustId());
				// order.setUserInfo(ordergroup.get);
				order.setUserName(orderGroup.getCustName());
				order.setUserPhone(orderGroup.getCustPhone());
				order.setCreatedAt(orderGroup.getCreatedAt());
				order.setUpdatedAt(orderGroup.getUpdatedAt());

				order.setAddressId(orderGroup.getAddressId());

				// Jielong主题
				String topic = jielongMapper.selectTopic(orderGroup.getJielongId());
				order.setJielongTopic(topic);
				// 提货地址信息
				Integer addressId = orderGroup.getAddressId();
				UserAddress address = userAddressService.selectById(addressId).getData();
				order.setUserAddress(address);
				// 用户信息
				Integer clientId = orderGroup.getCustId();
				UserInfo userInfo = userInfoService.selectByUserId(clientId).getData();
				order.setUserInfo(userInfo);

				// 订单商品信息
				List<OrderGroup> orderGroupList2 = orderGroupMapper.selectByOrderId(orderGroup.getOrderId());

				if (orderGroupList2 != null && orderGroupList2.size() > 0) {
					List<OrderGoods> orderGoodsList = new ArrayList<OrderGoods>();
					for (OrderGroup orderGroup2 : orderGroupList2) {

						// 对应统一接口 ORDER
						Goods goods = goodsMapper.selectByPrimaryKey(orderGroup2.getGoodsId());
						OrderGoods orderGoods = new OrderGoods();
						orderGoods.setGoods(goods);
						orderGoods.setGoodsId(orderGroup2.getGoodsId());
						orderGoods.setMoney(orderGroup2.getCustBuyPrice());
						orderGoods.setSum(orderGroup2.getCustBuyNum());

						orderGoodsList.add(orderGoods);
					}
					order.setOrderGoods(orderGoodsList);

				}
				orderList.add(order);

			}

		} // end if

		responseBean.setData(orderList);
		return responseBean;
	}

	@Override
	public ResponseBean<List<PickCountBean>> countPick(Integer jielongId) {
		// 自提统计
		List<PickCountBean> pickCountBeanList = new ArrayList<PickCountBean>();
		// 1、首先根据jielongId查询所有商品
		List<Integer> goodsIdList = goodsMapper.selectIdsByJielongId(jielongId);
		for (Integer goodsid : goodsIdList) {

			PickCountBean pickCountBean = new PickCountBean();
			Goods goods = goodsMapper.selectByPrimaryKey(goodsid);
			pickCountBean.setGoods(goods);

			// 参与人数
			Integer joinPeopleSum = 0;
			// 已售数量
			Integer sellSum = 0;
			// 入账总额
			BigDecimal moneySum = new BigDecimal(0);
			// 2、用商品id去订单商品列表查询所有订单
			List<OrderGroup> orderGroupList = orderGroupMapper.selectByGoodsId(goodsid);

			if (orderGroupList != null && orderGroupList.size() > 0) {
				List<PickBean> pickBeans = new ArrayList<PickBean>();
				for (OrderGroup orderGroup : orderGroupList) {

					joinPeopleSum += 1;
					sellSum += orderGroup.getCustBuyNum();
					// BigDecimal totalMoney=orderGoods.getMoney().multiply(new
					// BigDecimal(orderGoods.getSum()));
					moneySum = moneySum.add(orderGroup.getCustBuyAllMoney());

					PickBean pickBean = new PickBean();
					pickBean.setCreatedAt(orderGroup.getCreatedAtStr());
					pickBean.setGoodsSum(orderGroup.getCustBuyNum());
					pickBean.setPrice(orderGroup.getCustBuyPrice());
					pickBean.setPhoneNumber(orderGroup.getCustPhone());
					pickBean.setUserName(orderGroup.getCustName());
					pickBean.setRemark(orderGroup.getCustNote());
					UserInfo userInfo = userInfoService.selectByUserId(orderGroup.getCustId()).getData();
					pickBean.setUserInfo(userInfo);

					Integer addressId = orderGroup.getAddressId();
					UserAddress address = userAddressService.selectById(addressId).getData();
					pickBean.setUserAddress(address);
					pickBeans.add(pickBean);
				}
				pickCountBean.setPickBeans(pickBeans);

			}

			pickCountBean.setJoinPeopleSum(joinPeopleSum);
			pickCountBean.setMoneySum(moneySum);
			pickCountBean.setSellSum(sellSum);
			pickCountBeanList.add(pickCountBean);
		}
		return new ResponseBean<>(pickCountBeanList);
	}

	@Override
	public ResponseBean<List<Order>> selectJoinByJielongId(Integer jielongId) {
		// 根据接龙ID查询所有商品订单
		ResponseBean<List<Order>> responseBean = new ResponseBean<List<Order>>();
		List<OrderGroup> orderGroupList = orderGroupMapper.selectFinishByJielongId(jielongId);
		// 转换输出格式
		List<Order> orderList = new ArrayList<Order>();
		if (orderGroupList != null && orderGroupList.size() > 0) {
			for (OrderGroup orderGroup : orderGroupList) {

				// 转换输出格式
				Order order = new Order();
				order.setId(orderGroup.getId());
				order.setIsSetGroup(1);
				order.setJielongId(orderGroup.getJielongId());
				order.setOrderNum(orderGroup.getOrderId());
				order.setRemark(orderGroup.getCustNote());
				order.setState(orderGroup.getTradeFlg());
				order.setSumMoney(orderGroup.getCustBuyAllMoney());
				order.setUserId(orderGroup.getCustId());
				order.setUserName(orderGroup.getCustName());
				order.setUserPhone(orderGroup.getCustPhone());
				order.setCreatedAt(orderGroup.getCreatedAt());
				order.setUpdatedAt(orderGroup.getUpdatedAt());

				order.setAddressId(orderGroup.getAddressId());

				// Jielong主题
				String topic = jielongMapper.selectTopic(orderGroup.getJielongId());
				order.setJielongTopic(topic);
				// 提货地址信息
				Integer addressId = orderGroup.getAddressId();
				UserAddress address = userAddressService.selectById(addressId).getData();
				order.setUserAddress(address);
				// 用户信息
				Integer clientId = orderGroup.getCustId();
				UserInfo userInfo = userInfoService.selectByUserId(clientId).getData();
				order.setUserInfo(userInfo);

				// 订单商品信息
				List<OrderGroup> orderGroupList2 = orderGroupMapper.selectByOrderId(orderGroup.getOrderId());

				if (orderGroupList2 != null && orderGroupList2.size() > 0) {
					List<OrderGoods> orderGoodsList = new ArrayList<OrderGoods>();
					for (OrderGroup orderGroup2 : orderGroupList2) {

						// 对应统一接口 ORDER
						Goods goods = goodsMapper.selectByPrimaryKey(orderGroup2.getGoodsId());
						OrderGoods orderGoods = new OrderGoods();
						orderGoods.setGoods(goods);
						orderGoods.setGoodsId(orderGroup2.getGoodsId());
						orderGoods.setMoney(orderGroup2.getCustBuyPrice());
						orderGoods.setSum(orderGroup2.getCustBuyNum());
						orderGoodsList.add(orderGoods);
					}
					order.setOrderGoods(orderGoodsList);

				}
				orderList.add(order);

			}

		} // end if

		responseBean.setData(orderList);
		return responseBean;
	}

	/**
	 * 取消参团
	 */
	@Transactional
	@Override
	public ResponseBean<Integer> cancelJoinGroup(Order order) {

		ResponseBean<Integer> responseBean = new ResponseBean<Integer>();

		Integer result = orderGroupMapper.updateStateById(0, 1, order.getId());
		// 减少参与人数和接龙金额
		jielongMapper.reduceJoin(order.getJielongId(), order.getSumMoney());

		List<OrderGoods> orderGoodsList = order.getOrderGoods();
		if (orderGoodsList != null && orderGoodsList.size() > 0) {
			for (int i = 0; i < orderGoodsList.size(); i++) {
				OrderGoods orderGoods = orderGoodsList.get(i);
				// 增加库存
				goodsMapper.addRepertory(orderGoods.getGoodsId(), orderGoods.getSum());

				Goods goods = new Goods();
				goods = goodsMapper.selectByPrimaryKey(orderGoods.getGoodsId());
				int setGroupNum = Integer.valueOf(goods.getGroupSum());
				int newGroupNum = Optional
						.ofNullable(orderGroupMapper.selectByCustBuyNum(order.getJielongId(), orderGoods.getGoodsId()))
						.orElse(0);
				// 取消订单后成团
				if (newGroupNum >= setGroupNum) {

					Integer oldGroupOkFlg = orderGroupConsoleMapper.selectGroupOkState(order.getJielongId(),
							orderGoods.getGoodsId());
					// 之前就是成团的
					if (oldGroupOkFlg == 1) {
						// 发送单人通知 撤单后还是成功的团
						UserMessage userMessage = new UserMessage();
						userMessage.setUserId(order.getUserId());
						userMessage.setTitle("取消参团通知！");
						userMessage.setMessage("您已成功取消参团，敬请下次惠顾，谢谢！");
						userMessageService.insert(userMessage);
					} else {
						// 之前不成团，状态应该没有这种情况
						UserMessage userMessage = new UserMessage();
						userMessage.setUserId(order.getUserId());
						userMessage.setTitle("取消参团通知！");
						userMessage.setMessage("您已成功取消参团，敬请下次惠顾，谢谢！");
						userMessageService.insert(userMessage);
					}

				} else { // 取消订单后不成团

					// 查看ordergroupconsole表的GroupOkFlg状态如果已经是1了
					Integer oldGroupOkFlg = orderGroupConsoleMapper.selectGroupOkState(order.getJielongId(),
							orderGoods.getGoodsId());
					if (oldGroupOkFlg != null) {
						// 本来是成团的，取消订单后不成团
						if (oldGroupOkFlg == 1) {
							// 有撤单的情况!从成团变成了 未成团。群发通知
							int updateRet = orderGroupConsoleMapper.updateGroupOkFlg(0, order.getJielongId(),
									orderGoods.getGoodsId());

							// 给本人发送消息
							UserMessage userMessage = new UserMessage();
							userMessage.setUserId(order.getUserId());
							userMessage.setTitle("取消参团通知！！");
							userMessage.setMessage("您已成功取消参团，敬请下次惠顾，谢谢！");
							userMessageService.insert(userMessage);

							// 取消订单之后给团里其他用户发送消息
							userMessageService.groupStateModify(order.getJielongId(), orderGoods.getGoodsId(), 0,goods.getName(),"");

						} else { // 本来就不成团
							// 发送单人通知
							// 下单之后给用户发送消息
							UserMessage userMessage = new UserMessage();
							userMessage.setUserId(order.getUserId());
							userMessage.setTitle("取消参团通知！");
							userMessage.setMessage("您已成功取消参团，敬请下次惠顾，谢谢！");
							userMessageService.insert(userMessage);
						}
					}

				}

			}
		}

		responseBean.setData(result);
		return responseBean;
	}

}
