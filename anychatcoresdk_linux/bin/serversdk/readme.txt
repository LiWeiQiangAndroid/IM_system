该目录下的可执行程序均为业务服务器程序（调用AnyChat Server SDK），需与核心服务器配合才能正常工作。

1、helloAnyChatServer_c++	调用AnyChat Server SDK的演示程序，可以通过脚本：./runhelloAnyChatServer.sh启动，源代码在src\server\c++\helloAnyChatServer目录中

2、anychatbusinessserver.jar	调用AnyChat Server SDK的演示程序，可以通过脚本：./runbusinessserver.sh启动，有GUI界面，源代码在src\server\java目录中

3、anychatsampleserver.jar	调用AnyChat Server SDK的演示程序，可以通过脚本：./runsampleserver.sh启动，没有GUI界面，适合在控制台运行，源代码在src\server\java目录中

4、anychatcallcenterserver.jar	视频呼叫中心业务服务器，实现了视频呼叫和大厅好友等功能，可以通过脚本：./runcallcenterserver.sh启动，没有GUI界面，源代码在src\server\java目录中


参考资料：
	有关AnyChat平台用户身份验证与第三方平台集成的问题
	http://bbs.anychat.cn/forum.php?mod=viewthread&tid=12&extra=page%3D1

	AnyChat使用攻略之独立部署Linux视频服务器
	http://bbs.anychat.cn/forum.php?mod=viewthread&tid=10&extra=page%3D1
	
	AnyChat业务服务器部署到Java Web容器（Tomcat）详细流程
	http://bbs.anychat.cn/forum.php?mod=viewthread&tid=335&extra=page%3D1


