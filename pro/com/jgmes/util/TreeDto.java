package com.jgmes.util;

import java.util.Date;
import java.util.List;


/**
 * @author liuc
 */
public class TreeDto<T> {
    private String id;
    private String flbm;
    private String flmc;
    private String flsm;
    private String syParent;
    private String syTreeOrderIndex;

    /**
     * 检验项目集合
     */
    private T data;

    private List<TreeDto> childTreeDto;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getFlbm() {
        return flbm;
    }

    public void setFlbm(String flbm) {
        this.flbm = flbm;
    }

    public String getFlmc() {
        return flmc;
    }

    public void setFlmc(String flmc) {
        this.flmc = flmc;
    }

    public String getSyParent() {
        return syParent;
    }

    public void setSyParent(String syParent) {
        this.syParent = syParent;
    }

    public List<TreeDto> getChildTreeDto() {
        return childTreeDto;
    }

    public void setChildTreeDto(List<TreeDto> childTreeDto) {
        this.childTreeDto = childTreeDto;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

	public String getSyTreeOrderIndex() {
		return syTreeOrderIndex;
	}

	public void setSyTreeOrderIndex(String syTreeOrderIndex) {
		this.syTreeOrderIndex = syTreeOrderIndex;
	}

	@Override
    public boolean equals(Object obj) {
        TreeDto s=(TreeDto)obj;
        return id.equals(s.id) && flbm.equals(s.flbm) && syParent.equals(s.syParent);
    }
}