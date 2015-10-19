服务器部署与测试程序使用说明：

1、服务器分核心服务器程序（anychatcoreserver）和业务服务器程序（serversdkdemo）；

2、开启两个终端，分别运行服务器程序与业务服务器程序；

3、核心服务器的配置文件（AnyChatCoreServer.ini）与Windows平台相同，可参考Windows平台用户手册；

4、业务服务器程序由src\server\serversdkdemo目录下的源代码编译生成，用户可在此基础上增加自己的业务逻辑；

5、在运行时应注意动态库搜寻路径并不包括当前文件夹，所以当即使可执行文件和其所需的so文件在同一文件夹，也会出现找不到so的问题。可以执行如下指令，将当前路径加入到系统库的搜索路径：
	#export LD_LIBRARY_PATH=$LD_LIBRARY_PATH:./

6、或是将该目录下的所有.so库文件拷贝到系统的相关目录下。

7、使用如下指令可使服务器在后台以daemon方式运行：
	#./anychatcoreserver -d

8、anychatdeamon.sh是一个守护进程监控脚本，当核心服务器进程工作异常，或是没有启动时，会自动启动核心服务器进程；

9、如需linux系统启动时运行服务器程序，请将核心服务器程序和业务服务器程序配置到相关的启动脚本中；


其它参考资料：
	AnyChat使用攻略之独立部署Linux视频服务器：http://bbs.anychat.cn/forum.php?mod=viewthread&tid=90&extra=page%3D1
	AnyChat服务器在Linux平台上开机自启动配置流程：http://bbs.anychat.cn/forum.php?mod=viewthread&tid=118&extra=page%3D1

