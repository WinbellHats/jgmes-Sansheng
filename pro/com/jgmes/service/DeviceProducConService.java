package com.jgmes.service;

import com.jgmes.util.DataCollection;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * �豸�Խ�webservice������
 * @author liuc
 * @version 2019-04-23 11:44:16
 */
public interface DeviceProducConService {
	
	public void load();

	/**
	 * 设置设备计数
	 * @param DeviceID
	 * @param Quantity
	 * @return
	 */
	public DeviceProducConServiceImpl.ResultBoolean SetDeviceQuantity(String DeviceID, Long Quantity);


	/**
	 * 设置红外线计数
	 * @param DeviceID
	 * @param Quantity
	 * @param comk
	 * @return
	 */
	public DeviceProducConServiceImpl.ResultBoolean SetInfraredQuantity(String DeviceID, Long Quantity,String comk);




	/**
	 * 设备计数与安灯异常服务接口
	 * @param DeviceID			设备唯一ID,不能为空(字符串)
	 * @param DeviceTime		格式:年-月-日 时:分:秒（2019-04-26 20:20:00）(字符串)
	 * @param Admin				超级现场管理功能：1表示启用，0表示正常（整形）
	 * @param TurnOffLight		服务器灭灯功能：1表示启用，0表示正常（整形）
	 * @param Lighting			服务器控灯功能：1表示启用，0表示正常（整形）
	 * @param AllTotalQuantity	所有累计数量（长整形）
	 * @param NormalQuantity	正常数量（长整形）
	 * @param AbnormalState		异常状态:1表示有超过5分钟没有动作报警需要处理，0表示没有异常（整形）
	 * @param MoldException		模具异常:1表示有按下，0表示没有按下，2表示超过时间需要处理（整形）
	 * @param DeviceException	设备异常:1表示有按下，0表示没有按下，2表示超过时间需要处理（整形）
	 * @param QualityException	品质异常:1表示有按下，0表示没有按下，2表示超过时间需要处理（整形）
	 * @param MaterialsException 物料异常:1表示有按下，0表示没有按下，2表示超过时间需要处理（整形）
	 * @param CallSquadLeader	呼叫主管：1表示有按下，0表示没有按下，2表示超过时间需要处理（整形）
	 * @param MoldCumulativeTime		模具按下累计时间:秒（整形）
	 * @param DeviceCumulativeTime		设备按下累计时间:秒（整形）
	 * @param QualityCumulativeTime		品质按下累计时间:秒（整形）
	 * @param MaterialsCumulativeTime	物料按下累计时间：秒（整形）
	 * @param CallCumulativeTime		呼叫班长按下累计时间：秒（整形）
	 * @return
	 */
	public DeviceProducConServiceImpl.ResultBoolean SetDeviceCountAnlamp(String DeviceID, String DeviceTime,int Admin,int TurnOffLight,int Lighting,Long AllTotalQuantity,Long NormalQuantity,int AbnormalState,int MoldException,int DeviceException,int QualityException,int MaterialsException,int CallSquadLeader,int MoldCumulativeTime,int DeviceCumulativeTime,int QualityCumulativeTime,int MaterialsCumulativeTime,int CallCumulativeTime);




	public DeviceProducConServiceImpl.ResultBoolean setZKSJCJB(List<DataCollection> dataCollectionList);

	/**
	 * 保养
	 * @return
	 */
	public DeviceProducConServiceImpl.ResultBoolean upKeep(String id, HttpServletRequest request);


}