package com.jielong.core.service.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.util.StringUtil;
import com.jielong.base.util.ErrorCode;
import com.jielong.core.beans.PageBean;
import com.jielong.core.beans.ResponseBean;
import com.jielong.core.dao.CommonDao;
import com.jielong.core.dao.GoodsMapper;
import com.jielong.core.dao.JielongMapper;
import com.jielong.core.dao.UserAddressMapper;
import com.jielong.core.dao.UserInfoMapper;
import com.jielong.core.domain.Goods;
import com.jielong.core.domain.Jielong;
import com.jielong.core.domain.UserAddress;
import com.jielong.core.domain.UserInfo;
import com.jielong.core.service.JielongService;

@Service
public class JielongServiceImpl implements JielongService {

	@Autowired
	JielongMapper jielongMapper;

	@Autowired
	GoodsMapper goodsMapper;

	@Autowired
	CommonDao commonDao;

	@Autowired
	UserAddressMapper addressMapper;

	@Autowired
	UserInfoMapper userInfoMapper;

	@Transactional
	@Override
	public ResponseBean<Integer> insert(Jielong jielong) {
		ResponseBean<Integer> responseBean = new ResponseBean<>();

		try {
			jielong.setStatus(1);     //状态：进行中
			Integer result = jielongMapper.insertSelective(jielong);
			
			// 商品列表
			List<Goods> goodsList = jielong.getGoodsList();	

			Integer jieLongId = commonDao.getLastId(); // 最新插入的id

			// 插入该接龙对应的所有商品
			for (Goods goods : goodsList) {
				goods.setJielongId(jieLongId);
				goodsMapper.insertSelective(goods);

			}
			responseBean.setData(result);

		} catch (Exception e) {
			e.printStackTrace();
			responseBean.setErrorCode(ErrorCode.INSERT_EXCEPTION);
			responseBean.setErrorMessage("插入数据错误");

		}
		return responseBean;

	}

	@Transactional
	@Override
	public ResponseBean<List<Jielong>> selectByPage(PageBean pageBean) {
		PageHelper.startPage(pageBean.getPageNum(), pageBean.getPageSize());
		//查询状态为1的接龙
		List<Jielong> jielongs = jielongMapper.selectAll().stream().filter(j->j.getStatus()==1).collect(Collectors.toList());

		for (Jielong jielong : jielongs) {
			// 发布用户信息
			UserInfo userInfo = userInfoMapper.selectByUserId(jielong.getUserId()).get(0);
			jielong.setUserInfo(userInfo);
			// 商品列表
			List<Goods> goodsList = goodsMapper.selectByJielongId(jielong.getId());
			jielong.setGoodsList(goodsList);

			// 图片列表
			String images = jielong.getIntroImages();
			if (StringUtil.isNotEmpty(images)) {
				String[] introImages = images.split(",");
				List<String> imagList = Arrays.asList(introImages);
				jielong.setImageList(imagList);
			}

			// 自提地址列表
			String address = jielong.getGoodsAddresses();
			if (StringUtil.isNotEmpty(address)) {
				List<UserAddress> addressList = new ArrayList<UserAddress>();
				String[] addresses = address.split(",");
				for (int i = 0; i < addresses.length; i++) {
					Integer addressId = Integer.parseInt(addresses[i]);
					UserAddress ads = addressMapper.selectByPrimaryKey(addressId);
					addressList.add(ads);
				}
				jielong.setTakeGoodsAddressList(addressList);

			} //

		}

		ResponseBean<List<Jielong>> responseBean = new ResponseBean<List<Jielong>>(jielongs);
		return responseBean;
	}

	@Transactional
	@Override
	public ResponseBean<List<Jielong>> selectByUserId(Integer userId) {

		List<Jielong> jielongs = jielongMapper.selectByUserId(userId);
		for (Jielong jielong : jielongs) {
			// 发布用户信息
			UserInfo userInfo = userInfoMapper.selectByUserId(jielong.getUserId()).get(0);
			jielong.setUserInfo(userInfo);

			// 商品列表
			List<Goods> goodsList = goodsMapper.selectByJielongId(jielong.getId());
			jielong.setGoodsList(goodsList);

			// 图片列表
			String images = jielong.getIntroImages();
			if (StringUtil.isNotEmpty(images)) {
				String[] introImages = images.split(",");
				List<String> imagList = Arrays.asList(introImages);
				jielong.setImageList(imagList);
			}

			// 自提地址列表
			String address = jielong.getGoodsAddresses();
			if (StringUtil.isNotEmpty(address)) {
				List<UserAddress> addressList = new ArrayList<UserAddress>();
				String[] addresses = address.split(",");
				for (int i = 0; i < addresses.length; i++) {
					Integer addressId = Integer.parseInt(addresses[i]);
					UserAddress ads = addressMapper.selectByPrimaryKey(addressId);
					addressList.add(ads);
				}
				jielong.setTakeGoodsAddressList(addressList);

			} //

		}
		return new ResponseBean<List<Jielong>>(jielongs);
	}

}
