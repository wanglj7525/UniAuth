package com.dianrong.common.uniauth.server.service;

import com.dianrong.common.uniauth.common.bean.dto.ConfigDto;
import com.dianrong.common.uniauth.common.bean.dto.PageDto;
import com.dianrong.common.uniauth.server.data.entity.Cfg;
import com.dianrong.common.uniauth.server.data.entity.CfgExample;
import com.dianrong.common.uniauth.server.data.entity.CfgType;
import com.dianrong.common.uniauth.server.data.entity.CfgTypeExample;
import com.dianrong.common.uniauth.server.data.mapper.CfgMapper;
import com.dianrong.common.uniauth.server.data.mapper.CfgTypeMapper;
import com.dianrong.common.uniauth.server.util.BeanConverter;
import com.dianrong.common.uniauth.server.util.CheckEmpty;
import org.apache.cxf.common.util.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Arc on 25/3/2016.
 */
@Service
public class ConfigService {

    @Autowired
    private CfgMapper cfgMapper;

    @Autowired
    private CfgTypeMapper cfgTypeMapper;

    public ConfigDto addOrUpdateConfig(Integer id, String cfgKey, Integer cfgTypeId, String value, byte[] file) {
        Cfg cfg = new Cfg();
        cfg.setId(id);
        cfg.setCfgKey(cfgKey);
        cfg.setFile(file);
        cfg.setValue(value);
        cfg.setCfgTypeId(cfgTypeId);
        // update process.
        if(id != null) {
            if(file != null) {
                cfgMapper.updateByPrimaryKeyWithBLOBs(cfg);
            } else {
                cfgMapper.updateByPrimaryKey(cfg);
            }
        } else {
            // add process.
            cfgMapper.insert(cfg);
        }
        // do not need to return file after add or update.
        cfg.setFile(null);
        return BeanConverter.convert(cfg);
    }

    public PageDto<ConfigDto> queryConfig(Integer id, String cfgKey, Integer cfgTypeId, String value,
                                          Boolean needBLOBs,Integer pageSize, Integer pageNumber) {

        CheckEmpty.checkEmpty(pageNumber, "pageNumber");
        CheckEmpty.checkEmpty(pageSize, "pageSize");

        CfgExample cfgExample = new CfgExample();
        cfgExample.setPageOffSet(pageNumber * pageSize);
        cfgExample.setPageSize(pageSize);
        cfgExample.setOrderByClause("cfg_type_id asc");
        CfgExample.Criteria criteria = cfgExample.createCriteria();

        if(id != null) {
            criteria.andIdEqualTo(id);
        }
        if(!StringUtils.isEmpty(cfgKey)) {
            criteria.andCfgKeyEqualTo(cfgKey);
        }
        if(cfgTypeId != null) {
            criteria.andCfgTypeIdEqualTo(cfgTypeId);
        }
        if(!StringUtils.isEmpty(value)) {
            criteria.andValueLike("%" + value + "%");
        }

        List<Cfg> cfgs;
        if(needBLOBs != null && needBLOBs) {
            cfgs = cfgMapper.selectByExampleWithBLOBs(cfgExample);
        } else {
            cfgs = cfgMapper.selectByExample(cfgExample);
        }

        if(CollectionUtils.isEmpty(cfgs)) {
            return null;
        } else {
            List<ConfigDto> configDtos = new ArrayList<>();
            Map<Integer, String> cfgTypeIndex = this.getAllCfgTypesMap();
            for(Cfg cfg:cfgs) {
                ConfigDto configDto = BeanConverter.convert(cfg);
                configDto.setCfgType(cfgTypeIndex.get(cfg.getCfgTypeId()));
                configDtos.add(configDto);
            }
            int count = cfgMapper.countByExample(cfgExample);
            return new PageDto<>(pageNumber,pageSize,count,configDtos);
        }
    }

    public void delConfig(Integer cfgId) {
        cfgMapper.deleteByPrimaryKey(cfgId);
    }

    public Map<Integer, String> getAllCfgTypesMap() {
        List<CfgType> cfgTypes = cfgTypeMapper.selectByExample(new CfgTypeExample());
        Map<Integer, String> cfgTypeMap = new HashMap<>();
        for(CfgType cfgType : cfgTypes) {
            cfgTypeMap.put(cfgType.getId(), cfgType.getCode());
        }
        return cfgTypeMap;
    }

}
