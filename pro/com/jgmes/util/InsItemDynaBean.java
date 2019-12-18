package com.jgmes.util;

import com.je.core.util.bean.DynaBean;

import java.util.List;

/**
 * @author liuc
 * 检验项目分类工具类
 */
public class InsItemDynaBean {
    /**
     * 检验项目
     */
    private DynaBean insItem;
    /**
     * 产品检验标准集合
     */
    private List<DynaBean> productInsStandardChildList;


    public DynaBean getInsItem() {
        return insItem;
    }

    public void setInsItem(DynaBean insItem) {
        this.insItem = insItem;
    }

    public List<DynaBean> getProductInsStandardChildList() {
        return productInsStandardChildList;
    }

    public void setProductInsStandardChildList(List<DynaBean> productInsStandardChildList) {
        this.productInsStandardChildList = productInsStandardChildList;
    }
}
