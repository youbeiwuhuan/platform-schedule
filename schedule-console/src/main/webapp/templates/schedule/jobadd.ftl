<!DOCTYPE html>
<html>
<head>
  	<#import "/common/common.macro.ftl" as netCommon>
	<@netCommon.commonStyle />
    <!-- DataTables -->
    <link rel="stylesheet" href="${request.contextPath}/static/adminlte/plugins/datatables/dataTables.bootstrap.css">
    <title>添加任务</title>
</head>

<body>
<div>
    <div class="modal-body">
        <form class="form-horizontal form" role="form" id="jobaddForm">
            <div class="form-group">
                <label for="firstname" class="col-sm-2 control-label">app应用<font color="red">*</font></label>
                <div class="col-sm-4">
                    <select class="form-control" name="jobGroup">
          		            	<#list JobGroupList as group>
                                    <option value="${group.id}"
                                            <#if jobGroup==group.id>selected</#if> >${group.title}</option>
                                </#list>
                    </select>
                </div>
                <label for="lastname" class="col-sm-2 control-label">任务名<font color="red">*</font></label>
                <div class="col-sm-4"><input type="text" class="form-control" name="jobDesc"
                                             placeholder="请输入任务名"
                                             maxlength="50"></div>
            </div>
            <div class="form-group">
                <label for="firstname" class="col-sm-2 control-label">路由模式<font
                        color="red">*</font></label>
                <div class="col-sm-4">
                    <select class="form-control glueType" name="routeMode">
                        <option value="0">随机</option>
                        <option value="1">广播</option>
                    </select>
                </div>
                <label for="lastname" class="col-sm-2 control-label">Cron<font color="red">*</font></label>
                <div class="col-sm-4"><input type="text" class="form-control" name="jobCron"
                                             placeholder="cron表达式..." maxlength="128"></div>
            </div>
            <div class="form-group">
                <label for="firstname" class="col-sm-2 control-label">运行模式<font
                        color="red">*</font></label>
                <div class="col-sm-4">
                    <select class="form-control glueType" name="jobType">
                         <option value="0">Bean模式</option>
                    </select>
                </div>
                <label for="firstname" class="col-sm-2 control-label">JobHandler<font color="red">*</font></label>
                <div class="col-sm-4"><input type="text" class="form-control" name="executorHandler"
                                             placeholder="请输入jobHandler" maxlength="100"></div>
            </div>
            <div class="form-group">
                <label for="firstname" class="col-sm-2 control-label">任务参数<font color="black">*</font></label>
                <div class="col-sm-10">
                    <textarea class="textarea form-control" name="jobParam" placeholder="请输入任务参数" maxlength="512" style="height: 63px; line-height: 1.2;"></textarea>
                </div>
            </div>
            <div class="form-group">
                <div class="col-sm-offset-3 col-sm-6" style="text-align: center">
                    <button type="submit" class="btn btn-primary">提交</button>
                    <button type="button" class="btn btn-default" data-dismiss="modal" onclick="closeLayer()">取消
                    </button>
                </div>
            </div>
        </form>
    </div>
</div>

<@netCommon.commonScript />
<!-- DataTables -->
<script src="${request.contextPath}/static/adminlte/plugins/datatables/jquery.dataTables.min.js"></script>
<script src="${request.contextPath}/static/adminlte/plugins/datatables/dataTables.bootstrap.min.js"></script>
<script src="${request.contextPath}/static/plugins/jquery/jquery.validate.min.js"></script>
<!-- moment -->
<script src="${request.contextPath}/static/adminlte/plugins/daterangepicker/moment.min.js"></script>

<!-- 弹窗 -->
<script src="${request.contextPath}/static/plugins/layer/layer.js?t=20181226"></script>

<script type="text/javascript">

    function closeLayer() {
        window.parent.layer.closeAll();
    }

    jQuery.validator.addMethod("myValid01", function (value, element) {
        var length = value.length;
        var valid = /^[a-z][a-zA-Z0-9-]*$/;
        return this.optional(element) || valid.test(value);
    }, 'AppName必须是字母数字组合');

    var addModalValidate = $("#jobaddForm").validate({
        errorElement: 'span',
        errorClass: 'help-block',
        focusInvalid: true,
        rules: {
            appName: {
                required: true,
                myValid01: true
            },
            appId: {
                required: true,
                rangelength: [4, 20]
            }
        },
        messages: {
            appName: {
                required: '请输入' + "AppName",
                myValid01: 'AppName含有特殊字符'
            },
            appId: {
                required: '请输入' + "应用编号",
                rangelength: 'AppId必须大于4'
            }
        },
        highlight: function (element) {
            $(element).closest('.form-group').addClass('has-error');
        },
        success: function (label) {
            label.closest('.form-group').removeClass('has-error');
            label.remove();
        },
        errorPlacement: function (error, element) {
            element.parent('div').append(error);
        },
        submitHandler: function (form) {
            //提交请求
            $.post(base_url + "/addJob", $("#jobaddForm").serialize(), function (data, status) {
                if (data.code == "200") {
                    layer.open({
                        title: '系统提示',
                        btn: ['确定'],
                        content: '新增成功',
                        icon: '1',
                        end: function (layero, index) {
                            window.parent.location.reload();
                        }
                    });
                } else {
                    layer.open({
                        title: '系统提示',
                        btn: ['确定'],
                        content: '新增失败',
                        icon: '2'
                    });
                }
            });
        }
    });

</script>
</body>
</html>
