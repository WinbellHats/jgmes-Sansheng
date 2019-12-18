package com.jgmes.service;

/**
 * ERP�ӿ�
 * @author admin
 * @version 2019-07-29 16:01:38
 */
public interface ERPService {
	
	public void load();

	/**
	 * 传入多条数据的Json字符串值以更新或新增物料资料信息
	 * @param jsonList
	 * @return
	 */
	public String UpdateOrAddMaterialData(String jsonList);

	/**
	 * 通过物料编号查询数据库中对应的物料资料
	 * @param MaterialCode 物料编号
	 * @param MaterialName 物料名称
	 * @return
	 */
	public String GetMaterialData(String MaterialCode, String MaterialName);

	/**
	 * 根据工单号或者产品编码查找工单信息
	 * @param WorkOrderCode 工单编号
	 * @param MaterialCode	产品编号
	 * @return
	 */
	public String GetWorkOrderData(String WorkOrderCode,String MaterialCode);



	/**
	 * 传入多条数据的Json字符串值以更新或新增工单信息
	 * @param jsonList
	 * @return
	 */
	public String UpdateOrAddOrderData(String jsonList);


	/**
	 * 通过客户编号更新或新增客户信息
	 * @param ClientCode 客户编码
	 * @param ClientName 客户名称
	 * @return
	 */
	public String GetClientData(String ClientCode,String ClientName);


	/**
	 * 传入多条数据的Json字符串值以更新或新增客户信息
	 * @param jsonList
	 * @return
	 */
	public String UpdateOrAddClientData(String jsonList);

	/**
	 * 根据任务单号或者订单查找工单信息
	 * @param SCRW_RWDH
	 * @param SCRW_DDHM
	 * @return
	 */
	public String GetProductiveTaskData(String SCRW_RWDH,String SCRW_DDHM,String SCRW_GDHM);


	/**
	 * 传入多条数据的Json字符串值以更新或新增生产任务单信息
	 * @param jsonList
	 * @return
	 */
	public String UpdateOrAddProductiveTaskData(String jsonList);

	/**
	 * 根据产线编码或者产线名称查找产线信息
	 * @param ProductionLineCode
	 * @param ProductionLineName
	 * @return
	 */
	public String GetProductionLineData(String ProductionLineCode,String ProductionLineName);

	/**
	 * 传入多条数据的Json字符串值以更新或新增产线信息
	 * @param jsonList
	 * @return
	 */
	public String UpdateOrAddProductionLineData(String jsonList);

	/**
	 * 根据部门编码，部门名称，级别名称，负责人查找部门信息
	 * @param DeptCode
	 * @param deptName
	 * @param RankName
	 * @param ChargeUserName
	 * @return
	 */
	public String GetDepartmentData(String DeptCode,String deptName,String RankName,String ChargeUserName);

	/**
	 * 传入多条数据的Json字符串值以更新或新增部门信息
	 * @param jsonList
	 * @return
	 */
	public String UpdateOrAddDepartmentData(String jsonList);


	/**
	 * 根据用户账号，用户名，性别查找人员表信息
	 * @param USERCODE
	 * @param USERNAME
	 * @param GENDER
	 * @return
	 */
	public String GetUserData(String USERCODE,String USERNAME,String GENDER);


	/**
	 * 传入多条数据的Json字符串值以更新或新增人员信息
	 * @param jsonList
	 * @return
	 */
	public String UpdateOrAddUserData(String jsonList);


	/**
	 * 查询所有能选择的角色
	 * @return
	 */
	public String GetRoleData();


}