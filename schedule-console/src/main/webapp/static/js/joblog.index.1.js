$(function () {

//init date tables
    var joblogTable = $("#joblogTable").dataTable({
        "deferRender": true,
        "processing": true,
        "serverSide": true,
        "ajax": {
            url: timestamp(base_url + "/joblog/pageList"),
            type: "post",
            data: function (d) {
                var obj = {};
                obj.appName = $('#appName').val();
                obj.jobName = $('#jobName').val();
                obj.jobHandler = $('#jobHandler').val();
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
                "data": 'id',
                "bSortable": false,
                "visible": true,
                "width": '6%'
            },
            {
                "data": 'jobName',
                "visible": true,
                "width": '20%',
                "render": function (data, type, row) {
                    return data;
                }
            },
            {
                "data": 'appName',
                "visible": true,
                "width": '11%',
                "render": function (data, type, row) {
                    return data;
                }
            },
            {
                "data": 'jobType',
                "visible": true,
                "width": '7%',
                "render": function (data, type, row) {
                    if (0 == data) {
                        return 'bean模式';
                    }
                    if (1 == data) {
                        return 'shell模式';
                    }
                    return '';
                }
            },
            {
                "data": 'jobHandler',
                "visible": true,
                "width": '11%',
                "render": function (data, type, row) {
                    return data;
                }
            },
            {
                "data": 'jobCron',
                "visible": true,
                "width": '11%',
                "render": function (data, type, row) {
                    return data;
                }
            },
            {
                "data": 'status',
                "visible": true,
                "width": '5%',
                "render": function (data, type, row) {
                    // status
                    if (0 == data) {
                        return '<small class="label label-success" >RUNNING</small>';
                    } else {
                        return '<small class="label label-default" >STOP</small>';
                    }
                }
            },
            {
                "data": 'id',
                "visible": true,
                "width": '13%',
                "render": function (data, type, row) {
                    var btn = '<button class="btn btn-warning btn-xs" type="button" onclick="toUpdateJobPage(' + data + ')">编辑</button> ';
                    var executeOncebtn = '<button class="btn bg-purple btn-xs" type="button" onclick="toexecuteAtonce(' + data + ')">立即执行</button> ';
                    var msgBtn;
                    if (row.status == 1) {
                        msgBtn = '<button class="btn btn-success btn-xs" type="button" onclick="validJob(' + data + ')">启动</button> ';
                    } else {
                        msgBtn = '<button class="btn bg-gray btn-xs" type="button" onclick="validJob(' + data + ')">关闭</button> ';
                    }
                    //  var tipBtn = '<button class="btn btn-danger btn-xs" type="button" onclick="deleteJob(' + data + ')">删除</button> ';
                    //  return btn + executeOncebtn + msgBtn + tipBtn;
                    return btn + executeOncebtn + msgBtn;
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
        joblogTable.fnDraw();
    });

});