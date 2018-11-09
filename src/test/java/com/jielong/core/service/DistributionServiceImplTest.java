package com.jielong.core.service;


import com.jielong.core.domain.Distribution;
import com.jielong.core.domain.Order;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.math.BigDecimal;
import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest
public class DistributionServiceImplTest {

  @Autowired
  DistributionService distributionService;

  @Test
  public void  insertTest(){
      Order order=new Order();
      order.setId(223);
      order.setOrderNum("201807041640151568");
      order.setUserId(61);
      order.setSumMoney(new BigDecimal(100));
      distributionService.insert(order);


  }
    @Test
    public void  selectTest(){
        List<Distribution> list=distributionService.selectByUserId(19);
        list.forEach(distribution -> {
            System.out.println("金额:"+distribution.getDistMoneyYuan());
        });
        Assert.assertEquals(1L,list.size());

    }


}
