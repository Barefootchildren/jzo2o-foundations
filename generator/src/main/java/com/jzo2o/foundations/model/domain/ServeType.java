package com.jzo2o.foundations.model.domain;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * 服务类型表
 * </p>
 *
 * @author itcast
 * @since 2025-10-17
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("serve_type")
public class ServeType implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 服务类型id
     */
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 服务类型编码
     */
    @TableField("code")
    private String code;

    /**
     * 服务类型名称
     */
    @TableField("name")
    private String name;

    /**
     * 服务类型图标
     */
    @TableField("serve_type_icon")
    private String serveTypeIcon;

    /**
     * 服务类型图片
     */
    @TableField("img")
    private String img;

    /**
     * 排序字段
     */
    @TableField("sort_num")
    private Integer sortNum;

    /**
     * 是否启用，0草稿,1禁用，2启用
     */
    @TableField("active_status")
    private Integer activeStatus;

    /**
     * 创建时间
     */
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    /**
     * 创建者
     */
    @TableField("create_by")
    private Long createBy;

    /**
     * 更新者
     */
    @TableField("update_by")
    private Long updateBy;


}
