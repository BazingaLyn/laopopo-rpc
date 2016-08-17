var currPageIndex = 0;
var currLimit = 10;

$(function() {
	$("#monitorTable").bootstrapTable({
		
						url : "./demo.json",
						sortName : "rkey",// 排序列
						striped : true,// 條紋行
						sidePagination : "server",// 服务器分页
						clickToSelect : true,// 选择行即选择checkbox
						singleSelect : true,// 仅允许单选
						pagination : true,// 启用分页
						escape : true,// 过滤危险字符
						queryParams : getParams,// 携带参数
						pageCount : 10,// 每页行数
						pageIndex : 0,// 其实页
						method : "get",// 请求格式
						columns : [
								{
									field : 'regionalId',
									title : '编号',
									align : 'center',
									width : '30',
									valign : 'middle',
								},
								{
									field : 'regionalTypeName',
									title : '区域类型',
									align : 'center',
									width : '40',
									valign : 'middle',
								},
								{
									field : 'regionalName',
									title : '区域名称',
									align : 'center',
									width : '50',
									valign : 'bottom',
								},
								{
									field : 'departureCityNames',
									title : '包含城市（出发城市）',
									align : 'center',
									width : '150',
									valign : 'bottom',
									formatter : function(value, row, index) {
										var showVal = value.substring(0, 20);
										if (showVal.substr(-1) == ",") {
											showVal = showVal.substring(0,
													showVal.length - 1)
										}
										return "<span class='btn-purple popover-purple' onmouseover='region.showPopover($(this))' data-toggle='popover' data-trigger='hover' data-placement='top' data-content='"
												+ value
												+ "' data-original-title='出发城市'>"
												+ showVal + "</span>";
									},
								},
								{
									field : 'bookingCityNames',
									title : '适用预订城市',
									align : 'center',
									width : '100',
									valign : 'bottom',
									formatter : function(value, row, index) {
										var showVal = value.substring(0, 15);
										if (showVal.substr(-1) == ",") {
											showVal = showVal.substring(0,
													showVal.length - 1)
										}
										return "<span class='btn-purple popover-purple' data-toggle='popover' onmouseover='region.showPopover($(this))' data-trigger='hover' data-placement='top' data-content='"
												+ value
												+ "' data-original-title='预订城市'>"
												+ showVal + "</span>";
									}
								},
								{
									field : 'description',
									title : '备注',
									align : 'center',
									width : '100',
									valign : 'bottom',
								},
								{
									field : 'id',
									title : '用户操作',
									align : 'center',
									width : '290px',
									formatter : function(value, row, index) {
										var btnStr = "&nbsp;&nbsp;&nbsp;&nbsp;<button onclick=region.suitBookingCitys("
												+ row.regionalId
												+ ",'"
												+ row.regionalTypeName
												+ "','"
												+ row.regionalName
												+ "','"
												+ row.bookingCityIds
												+ "') class='btn btn-success btn-xs'><i class='fa fa-edit'></i><span >适用预订城市</span></button>";
										btnStr += "&nbsp;&nbsp;&nbsp;&nbsp;<button onclick=region.editRegional("
												+ row.regionalId
												+ ",'"
												+ row.regionalTypeId
												+ "','"
												+ row.regionalName
												+ "','"
												+ row.departureCityIds
												+ "','"
												+ row.description
												+ "','"
												+ row.regionalTypeId
												+ "')  class='btn btn-warning btn-xs'><i class='fa fa-eye'></i><span >编辑</span></button>";
										if (!row.flag) {
											btnStr += "&nbsp;&nbsp;&nbsp;&nbsp;<button onclick=region.changeFlag("
													+ row.regionalId
													+ ",'"
													+ row.flag
													+ "') class='btn btn-red btn-xs'><i class='fa fa-close'></i><span >停用</span></button>";
										} else {
											btnStr += "&nbsp;&nbsp;&nbsp;&nbsp;<button onclick=region.changeFlag("
													+ row.regionalId
													+ ",'"
													+ row.flag
													+ "') class='btn btn-info btn-xs'><i class='fa fa-save'></i><span >启用</span></button>";
										}
										return btnStr;
									}
								} ],
						// toolbar: "#toolBar",
						onPageChange : function(number, size) {
							currPageIndex = number;
							currLimit = size
						},
						onLoadSuccess : function() {
							$("#searchBtn").button('reset');
						}
					});

//	//搜索  
//	$("#searchBtn").click(function() {
//		$(this).button('loading');
//		var nullparamss = {};
//		$("#dataShow").bootstrapTable("refresh", nullparamss);
//
//	});
//	//enter键搜索  
//	$("#searchKey").keydown(function(event) {
//		if (event.keyCode == 13) {
//			$("#searchBtn").click();
//		}
//	});
//	//阻止enter键提交表单  
//	$("#mainForm").submit(function() {
//		return false;
//	});

});
//默认加载时携带参数  
function getParams(params) {
	var searchKey = $("#searchKey").val();
	return {
		bysex : 1,
		limit : params.limit,
		offset : params.offset,
		search : searchKey
	};
}