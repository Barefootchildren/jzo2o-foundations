package com.jzo2o.foundations.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jzo2o.common.expcetions.ForbiddenOperationException;
import com.jzo2o.common.model.PageResult;
import com.jzo2o.foundations.enums.FoundationStatusEnum;
import com.jzo2o.foundations.mapper.RegionMapper;
import com.jzo2o.foundations.mapper.ServeItemMapper;
import com.jzo2o.foundations.mapper.ServeMapper;
import com.jzo2o.foundations.model.domain.Region;
import com.jzo2o.foundations.model.domain.Serve;
import com.jzo2o.foundations.model.domain.ServeItem;
import com.jzo2o.foundations.model.dto.request.ServePageQueryReqDTO;
import com.jzo2o.foundations.model.dto.request.ServeUpsertReqDTO;
import com.jzo2o.foundations.model.dto.response.ServeResDTO;
import com.jzo2o.foundations.service.IServeService;
import com.jzo2o.mysql.utils.PageHelperUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.List;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author itcast
 * @since 2023-07-03
 */
@Service
public class ServeServiceImpl extends ServiceImpl<ServeMapper, Serve> implements IServeService {

    /**
     * 分页查询
     *
     * @param servePageQueryReqDTO 查询条件
     * @return 分页结果
     */
    @Override
    @Transactional
    public PageResult<ServeResDTO> page(ServePageQueryReqDTO servePageQueryReqDTO) {
        //通过baseMapper调用queryServeListByRegionId方法
        PageResult<ServeResDTO> serveResDTOPageResult = PageHelperUtils.selectPage(servePageQueryReqDTO, () -> baseMapper.queryServeListByRegionId(servePageQueryReqDTO.getRegionId()));
        return serveResDTOPageResult;
    }
    @Resource
    private ServeItemMapper serveItemMapper;
    @Resource
    private RegionMapper regionMapper;
        /**
     * 批量添加服务到区域
     *
     * @param serveUpsertReqDTOList 服务添加请求DTO列表，包含要添加的服务项ID和区域ID等信息
     */
    @Override
    public void batchAdd(List<ServeUpsertReqDTO> serveUpsertReqDTOList) {
        // 遍历服务添加请求列表，逐个处理服务添加逻辑
        for (ServeUpsertReqDTO serveUpsertReqDTO : serveUpsertReqDTOList) {
            // 检查服务项是否启用，未启用则抛出异常
            ServeItem serveItem = serveItemMapper.selectById(serveUpsertReqDTO.getServeItemId());
            if(!(serveItem.getActiveStatus()==FoundationStatusEnum.ENABLE.getStatus())){
                throw new ForbiddenOperationException("该服务未启用，无法添加到区域下使用");
            }

            // 检查服务是否已存在于指定区域，如果已存在则抛出异常
            LambdaQueryWrapper<Serve> queryWrapper = Wrappers.<Serve>lambdaQuery()
                    .eq(Serve::getRegionId,serveUpsertReqDTO.getRegionId())
                    .eq(Serve::getServeItemId,serveUpsertReqDTO.getServeItemId());
            Integer count = baseMapper.selectCount(queryWrapper);
            if(count>0){
                throw new ForbiddenOperationException("该服务已添加到该区域下");
            }

            // 转换DTO为实体对象，设置城市编码并插入数据库
            Serve serve = BeanUtil.toBean(serveUpsertReqDTO, Serve.class);
            Region region = regionMapper.selectById(serveUpsertReqDTO.getRegionId());
            serve.setCityCode(region.getCityCode());
            baseMapper.insert(serve);
        }
    }

    /**
     * 更新服务价格
     *
     * @param id 服务ID
     * @param price 新的价格
     * @return 更新后的服务对象
     * @throws ForbiddenOperationException 当更新失败时抛出此异常
     */
    @Override
    @Transactional
    public Serve update(Long id, BigDecimal price) {
        // 执行更新操作，根据ID更新价格字段
        boolean update = lambdaUpdate().eq(Serve::getId, id).set(Serve::getPrice, price).update();
        if (! update){
            throw new ForbiddenOperationException("更新失败");
        }
        // 返回更新后的完整服务对象
        return baseMapper.selectById(id);
    }


    /**
     * 启用指定ID的服务
     *
     * @param id 服务ID
     * @return 启用后的服务对象
     * @throws ForbiddenOperationException 当服务不存在、已处于启用状态、服务项不存在、服务项未启用或更新失败时抛出异常
     */
    @Override
    @Transactional
    public Serve onSale(Long id) {
        // 查询要启用的服务
        Serve serve = baseMapper.selectById(id);
        if(ObjectUtil.isNull(serve)){
            throw new ForbiddenOperationException("区域服务不存在");
        }
        Integer saleStatus = serve.getSaleStatus();
        // 检查服务状态是否为初始状态或禁用状态
        if (!(saleStatus==FoundationStatusEnum.INIT.getStatus()||saleStatus==FoundationStatusEnum.DISABLE.getStatus())){
            throw new ForbiddenOperationException("该服务已处于启用状态");
        }
        // 查询关联的服务项
        Long serveItemId = serve.getServeItemId();
        ServeItem serveItem = serveItemMapper.selectById(serveItemId);
        if(ObjectUtil.isNull(serveItem)){
            throw new ForbiddenOperationException("服务项不存在");
        }
        Integer activeStatus = serveItem.getActiveStatus();
        // 检查服务项是否处于启用状态
        if (!(activeStatus==FoundationStatusEnum.ENABLE.getStatus())){
            throw new ForbiddenOperationException("服务项未启用");
        }
        // 更新服务状态为启用
        boolean update = lambdaUpdate()
                .eq(Serve::getId, id)
                .set(Serve::getSaleStatus, FoundationStatusEnum.ENABLE.getStatus())
                .update();
        if(!update){
            throw new ForbiddenOperationException("更新失败");
        }
        // 返回更新后的服务对象
        return baseMapper.selectById(id);
    }

    /**
     * 根据ID删除服务信息
     *
     * @param id 服务ID
     */
    @Override
    @Transactional
    public void deleteById(Long id) {
        // 查询要删除的服务信息
        Serve serve = baseMapper.selectById(id);
        if(ObjectUtil.isNull(serve)){
            throw new ForbiddenOperationException("服务不存在");
        }

        // 检查服务状态，只有草稿状态才能删除
        Integer saleStatus = serve.getSaleStatus();
        if (!(saleStatus==FoundationStatusEnum.INIT.getStatus())){
            throw new ForbiddenOperationException("该区域服务不是草稿状态，只有草稿状态才能删除");
        }

        // 执行删除操作
        boolean delete = baseMapper.deleteById(id) > 0;
        if(!delete){
            throw new ForbiddenOperationException("删除失败");
        }
    }

    /**
     * 下架服务
     *
     * @param id 服务ID
     */
    @Override
    @Transactional
    public void offSale(Long id) {
        // 查询要下架的服务
        Serve serve = baseMapper.selectById(id);
        if(ObjectUtil.isNull(serve)){
            throw new ForbiddenOperationException("服务不存在");
        }

        // 检查服务状态，只有启用状态的服务才能下架
        Integer saleStatus = serve.getSaleStatus();
        if (!(saleStatus==FoundationStatusEnum.ENABLE.getStatus())){
            throw new ForbiddenOperationException("该区域服务不是启用状态，只有启用状态才能删除");
        }

        // 更新服务状态为禁用（下架）
        boolean update = lambdaUpdate().eq(Serve::getId, id).set(Serve::getSaleStatus, FoundationStatusEnum.DISABLE.getStatus()).update();
        if(!update){
            throw new ForbiddenOperationException("下架失败");
        }
    }

    /**
     * 将指定服务设置为热门服务
     *
     * @param id 服务ID
     */
    @Override
    public void onHot(Long id) {
        // 查询服务信息
        Serve serve = baseMapper.selectById(id);
        if(ObjectUtil.isNull(serve)){
            throw new ForbiddenOperationException("服务不存在");
        }
        //查询是否启用
        Integer saleStatus = serve.getSaleStatus();
        if (!(saleStatus==FoundationStatusEnum.ENABLE.getStatus())){
            throw new ForbiddenOperationException("该服务未启用，不能添加到热门服务");
        }
        // 检查服务是否已添加到热门服务
        Integer isHot = serve.getIsHot();
        if(isHot!=0){
            throw new ForbiddenOperationException("该服务已添加到热门服务");
        }

        // 更新服务热门状态
        boolean update = lambdaUpdate().eq(Serve::getId, id).set(Serve::getIsHot, 1).update();
        if(!update){
            throw new ForbiddenOperationException("添加热门服务失败");
        }
    }

    @Override
    public void offHot(Long id) {
        // 查询服务信息
        Serve serve = baseMapper.selectById(id);
        if(ObjectUtil.isNull(serve)){
            throw new ForbiddenOperationException("服务不存在");
        }
        // 检查服务是否已添加到热门服务
        Integer isHot = serve.getIsHot();
        if(isHot!=1){
            throw new ForbiddenOperationException("该服务不是热门服务");
        }

        // 更新服务热门状态
        boolean update = lambdaUpdate().eq(Serve::getId, id).set(Serve::getIsHot, 0).update();
        if(!update){
            throw new ForbiddenOperationException("添加热门服务失败");
        }
    }


}


