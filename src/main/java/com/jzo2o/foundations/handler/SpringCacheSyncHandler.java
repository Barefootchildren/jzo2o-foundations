package com.jzo2o.foundations.handler;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.jzo2o.api.foundations.dto.response.RegionSimpleResDTO;
import com.jzo2o.foundations.constants.RedisConstants;
import com.jzo2o.foundations.enums.FoundationStatusEnum;
import com.jzo2o.foundations.mapper.ServeItemMapper;
import com.jzo2o.foundations.model.domain.Serve;
import com.jzo2o.foundations.model.domain.ServeItem;
import com.jzo2o.foundations.service.HomeService;
import com.jzo2o.foundations.service.IRegionService;
import com.jzo2o.foundations.service.IServeItemService;
import com.jzo2o.foundations.service.IServeService;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

@Component
@Slf4j
public class SpringCacheSyncHandler {
    @Resource
    private IRegionService regionService;
    @Resource
    private RedisTemplate redisTemplate;
    @Resource
    private HomeService homeService;

    /**
     * 同步活跃区域缓存任务
     * <p>
     * 该方法通过XXL-JOB定时执行，用于同步系统中的活跃区域缓存数据。
     * 主要功能包括：删除旧的活跃区域缓存、重新查询并缓存最新的活跃区域列表、
     * 清除各区域对应的服务类型缓存并重新加载。
     * </p>
     */
    @XxlJob(value = "activeRegionSync")
    public void activeRegionSync(){
        log.info(">>>>>>>>开始进行缓存同步，更新已启用区域");
        // 删除Redis中存储的活跃区域缓存数据
        redisTemplate.delete(RedisConstants.CacheName.JZ_CACHE+"::ACTIVE_REGIONS");
        // 重新查询并缓存活跃区域列表
        List<RegionSimpleResDTO> regionSimpleResDTOS = regionService.queryActiveRegionListCache();
        regionSimpleResDTOS.forEach(item->{
            Long regionId = item.getId();
            String serve_icon_key = RedisConstants.CacheName.SERVE_ICON + "::" + regionId;
            String serve_type_key = RedisConstants.CacheName.SERVE_TYPE + "::" + regionId;
            String serve_hot_key = RedisConstants.CacheName.HOT_SERVE + "::" + regionId;
            redisTemplate.delete(serve_icon_key);
            redisTemplate.delete(serve_type_key);
            redisTemplate.delete(serve_hot_key);
            homeService.queryServeIconCategoryByRegionIdCache(regionId);
            homeService.queryServeTypeListByRegionIdCache(regionId);
            homeService.queryHotServeListByRegionIdCache(regionId);
            //todo 删除该区域下的服务类型列表缓存
        });
        log.info(">>>>>>>>更新已启用区域完成");
    }
    @Resource
    private IServeService serveService;
    @Resource
    private IServeItemService serveItemService;
    @XxlJob(value = "hotServeAndServeItemSync")
    public void hotServeAndServeItemSync(){
        log.info(">>>>>>>>开始进行缓存同步，更新热门服务及服务项");
        List<Serve> serveList = serveService.list(
                Wrappers.<Serve>lambdaQuery()
                        .eq(Serve::getIsHot, 1)
                        .eq(Serve::getSaleStatus, FoundationStatusEnum.ENABLE.getStatus())
        );
        serveList.forEach(serve->{
            Long serveId = serve.getId();
            String serve_key = RedisConstants.CacheName.SERVE + "::" + serveId;
            String serve_item_key = RedisConstants.CacheName.SERVE_ITEM + "::" + serveId;
            redisTemplate.delete(serve_key);
            redisTemplate.delete(serve_item_key);
            serveService.queryServeByIdCache(serveId);
            serveItemService.queryServeItemByServeId(serveId);
        });
        log.info(">>>>>>>>更新热门服务及服务项缓存同步结束");

    }
}
