package com.hmdp.service.impl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hmdp.dto.Result;
import com.hmdp.entity.ShopType;
import com.hmdp.mapper.ShopTypeMapper;
import com.hmdp.service.IShopTypeService;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

import static com.hmdp.utils.RedisConstants.CACHE_SHOPTYPE_KEY;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author 虎哥
 * @since 2021-12-22
 */
@Service
public class ShopTypeServiceImpl extends ServiceImpl<ShopTypeMapper, ShopType> implements IShopTypeService {
    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public Result queryByType() {
        //查询Redis缓存
        String shopTypeList = stringRedisTemplate.opsForValue().get(CACHE_SHOPTYPE_KEY);
        //若命中,则Redis直接返回数据
        if (StrUtil.isNotBlank(shopTypeList)) {
            List<ShopType> list = JSONUtil.toList(shopTypeList, ShopType.class);
            return Result.ok(list);
        }
        //若未命中,则查询数据库
        List<ShopType> typeList = query().orderByAsc("sort").list();
        if (typeList.size() == 0) {
            return Result.fail("系统异常!");
        }
        //查询数据库数据写入Redis进行缓存
        stringRedisTemplate.opsForValue().set(CACHE_SHOPTYPE_KEY, JSONUtil.toJsonStr(typeList));
        //返回数据库数据
        return Result.ok(typeList);
    }
}
