var currPageIndex = 0;
var currLimit = 10;

$(function() {
	$("#monitorTable")
			.bootstrapTable(
					{

						url : "/laopopo-console/index.do",
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
						detailView : true,
						detailFormatter : detailFormatter,
						columns : [
								{
									field : 'serviceName',
									title : '服务名',
									align : 'center',
									width : '30',
									valign : 'middle',
								},
								{
									field : 'totalCallCount',
									title : '调用次数',
									align : 'center',
									width : '40',
									valign : 'middle',
								},
								{
									field : 'totalFailCount',
									title : '失败次数',
									align : 'center',
									width : '50',
									valign : 'bottom',
								},
								{
									field : 'loadBalanceStrategy',
									title : '负载策略',
									align : 'center',
									width : '50',
									valign : 'bottom',
									formatter : function(value, row, index) {
										switch (row.loadBalanceStrategy) {
										case "WEIGHTINGRANDOM":
											return "加权随机";
										case "RANDOM":
											return "随机";
										case "POLLING":
											return "轮询";

										}

									},
								},
								{
									field : 'totalHandlerRequestBodySize',
									title : '累计请求大小(M)',
									align : 'center',
									width : '150',
									valign : 'bottom',
									formatter : function(value, row, index) {
										return "1,000";
									},
								},
								{
									field : 'id',
									title : '用户操作',
									align : 'center',
									width : '290px',
									formatter : function(value, row, index) {
										var btnStr = "&nbsp;&nbsp;&nbsp;&nbsp;<button onclick=suitBookingCitys() class='btn btn-success btn-xs'><i class='fa fa-edit'></i><span >审核</span></button>";
										btnStr += "&nbsp;&nbsp;&nbsp;&nbsp;<button onclick=editRegional()  class='btn btn-warning btn-xs'><i class='fa fa-eye'></i><span >负载策略</span></button>";
										return btnStr;
									}
								} ],
						onPageChange : function(number, size) {
							currPageIndex = number;
							currLimit = size
						},
						onLoadSuccess : function() {
							$("#searchBtn").button('reset');
						}
					});
});

function detailFormatter(index, row) {
	var html = [];
	$.each(row, function(key, value) {
		html.push('<table class="table"><caption>服务提供者</caption><thead><tr>'
				+'<th style="text-align: center; vertical-align: middle;">服务提供地址</th>'
				+'<th style="text-align: center; vertical-align: middle;">权重</th>'
				+'<th style="text-align: center; vertical-align: middle;">是否可降级</th>'
				+'<th style="text-align: center; vertical-align: middle;">是否可降级</th>'
				+'</tr></thead><tbody>'
				 + '<tr><td>Tanmay</td><td>Bangalore</td></tr><tr><td>Sachin</td><td>Mumbai</td></tr></tbody></table>');
	});
	return html;
}

// 默认加载时携带参数
function getParams(params) {
	var searchKey = $("#searchKey").val();
	return {
		bysex : 1,
		limit : params.limit,
		offset : params.offset,
		search : searchKey
	};
}