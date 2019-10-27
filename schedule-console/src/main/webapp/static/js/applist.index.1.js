$(function () {

    //init date tables
    var appListTable = $("#appListTable").dataTable({
        "deferRender": true,
        "processing": true,
        "serverSide": true,
        "ajax": {
            url: timestamp(base_url + "/applist/pageList"),
            type: "post",
            data: function (d) {
                var obj = {};
                obj.appName = $('#appName').val();
                obj.appId = $('#appId').val();
                obj.start = d.start;
                obj.length = d.length;
                return obj;
            }
        },
        "searching": false,
        "ordering": false,
        //"scrollX": true,	// scroll x，close self-adaption
        "columns": [
            {
                "data": 'appId',
                "bSortable": false,
                "visible": true,
                "width": '10%'
            },
            {
                "data": 'appName',
                "visible": true,
                "width": '20%',
                "render": function (data, type, row) {
                    return data;
                }
            },
            {
                "data": 'remark',
                "visible": true,
                "width": '20%'
            },
            {
                "data": 'createTime',
                "visible": true,
                "render": function (data, type, row) {
                    return data ? moment(new Date(data)).format("YYYY-MM-DD HH:mm:ss") : "";
                }
            },
            {
                "data": 'updateTime',
                "visible": true,
                "render": function (data, type, row) {
                    return data ? moment(new Date(data)).format("YYYY-MM-DD HH:mm:ss") : "";
                }
            },
            {
                "data": 'operate',
                "visible": true,
                "render": function (data, type, row) {
                    var btn = '<button class="btn btn-warning btn-xs" type="button" onclick="toAddAppPage()">添加</button> ';
                    var msgBtn = '<button class="btn btn-success btn-xs" type="button" onclick="alertMsg()">消息</button> ';
                    var tipBtn = '<button class="btn btn-primary btn-xs" type="button" onclick="alertTip()">tip</button> ';
                    return btn + msgBtn + tipBtn;
                }
            }
        ],
        "language": {
            "sProcessing": '\u5904\u7406\u4E2D...',
            "sLengthMenu": '\u6BCF\u9875 _MENU_ \u6761\u8BB0\u5F55',
            "sZeroRecords": '\u6CA1\u6709\u5339\u914D\u7ED3\u679C',
            "sInfo": '\u7B2C _PAGE_ \u9875 ( \u603B\u5171 _PAGES_ \u9875\uFF0C_TOTAL_ \u6761\u8BB0\u5F55 )',
            "sInfoEmpty": '\u65E0\u8BB0\u5F55',
            "sInfoFiltered": '',
            "sInfoPostFix": "",
            "sSearch": "\u641C\u7D22",
            "sUrl": "",
            "sEmptyTable": "",
            "sLoadingRecords": '\u8868\u4E2D\u6570\u636E\u4E3A\u7A7A',
            "sInfoThousands": ",",
            "oPaginate": {
                "sFirst": '\u9996\u9875',
                "sPrevious": '\u4E0A\u9875',
                "sNext": '\u4E0B\u9875',
                "sLast": '\u672B\u9875'
            },
            "oAria": {
                "sSortAscending": ': \u4EE5\u5347\u5E8F\u6392\u5217\u6B64\u5217',
                "sSortDescending": ': \u4EE5\u964D\u5E8F\u6392\u5217\u6B64\u5217'
            }
        }
    });

    //点查询按钮触发查询事件
    $('#searchBtn').on('click', function () {
        appListTable.fnDraw();
    });

    //编辑应用页面
    toAddAppPage = function () {
        layer.open({
            type: 2,
            title: '编辑应用',
            maxmin: true,
            shadeClose: false, //点击遮罩关闭层
            area: ['790px', '550px'],
            content: base_url + '/applist/addapp'
        });
    };

    alertMsg = function () {
        layer.msg('hello');
    };

    alertTip = function () {
        layer.tips('Hello tips!', '#appId');
    };

});