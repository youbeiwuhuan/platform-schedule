# 简单的任务调度系统

![Image text](doc/images/jobinfo.png)

![Image text](doc/images/joblog.png)


任务调度系统
java -jar -Dspring.profiles.active=test demo-0.0.1-SNAPSHOT.jar

统一返回格式:
{
   code : 0 ,
   data :
   [
   {
       type: 0 ,
       servers: []
   }
   ]
}