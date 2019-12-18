package com.jgmes.util;

import com.je.core.util.bean.DynaBean;
import org.python.google.common.collect.Lists;
import org.python.google.common.collect.Maps;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class TreeToolUtils {
    /**
     * 根节点对象存放到这里
     */
    private List<TreeDto> rootList;
    /**
     * 其他节点存放到这里，可以包含根节点
     */
    private List<TreeDto> bodyList;
 
    public TreeToolUtils(List<TreeDto> rootList, List<TreeDto> bodyList) {
        this.rootList = rootList;
        this.bodyList = bodyList;
    }
 
    public List<TreeDto> getTree(){   //调用的方法入口
        if(bodyList != null && !bodyList.isEmpty()){
            //声明一个map，用来过滤已操作过的数据
            Map<String,String> map = Maps.newHashMapWithExpectedSize(bodyList.size());
            rootList.forEach(beanTree -> getChild(beanTree,map));
            return rootList;
        }
        return rootList;
    }
 
    public void getChild(TreeDto treeDto,Map<String,String> map){
        List<TreeDto> childList = Lists.newArrayList();
        bodyList.stream()
                .filter(c -> !map.containsKey(c.getId()))
                .filter(c ->c.getSyParent().equals(treeDto.getId()))
                .forEach(c ->{
                    map.put(c.getId(),c.getSyParent());
                    getChild(c,map);
                    childList.add(c);
                });
        treeDto.setChildTreeDto(childList);
 
    }
}