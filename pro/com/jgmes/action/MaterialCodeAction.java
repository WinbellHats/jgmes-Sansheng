package com.jgmes.action;

		import javax.annotation.Resource;

		import com.je.core.action.BaseAction;
		import com.je.core.entity.QueryInfo;
		import com.je.core.security.SecurityUserHolder;
		import com.je.core.service.PCDynaServiceTemplate;
		import com.je.core.service.PCServiceTemplate;
		import com.je.core.util.*;
		import com.je.core.util.bean.BeanUtils;
		import com.je.core.util.bean.DynaBean;
		import com.je.dd.service.DictionaryManager;
		import com.je.dd.vo.DictionaryItemVo;
		import com.je.develop.service.DataImplManager;
		import com.je.rbac.model.EndUser;
		import net.sf.json.JSONObject;
		import org.apache.poi.hssf.usermodel.*;
		import org.apache.poi.poifs.filesystem.POIFSFileSystem;
		import org.springframework.context.annotation.Scope;
		import org.springframework.stereotype.Component;

		import com.je.core.action.DynaAction;

		import java.io.FileInputStream;
		import java.io.FileNotFoundException;
		import java.io.IOException;
		import java.io.Serializable;
		import java.math.BigDecimal;
		import java.util.*;

/**
 *
 * @author admin
 * @version 2019-04-10 13:48:39
 * @see /jgmes/materialCodeAction!load.action
 */
@Component("materialCodeAction")
@Scope("prototype")
public class MaterialCodeAction extends DynaAction  {

	private static final long serialVersionUID = 1L;


}