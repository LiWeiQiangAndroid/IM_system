package com.wandou.sittingdetect;

import java.io.*;
import org.joone.engine.*;
import org.joone.engine.learning.TeachingSynapse;
import org.joone.io.*;
import org.joone.net.*;

/**BPNN类，实现了神经网络初始化，训练，测试等方法
 * 输入为0~1之间的double数（组）
 * 输出为0~1之间的double数（组）
 * 使用的接口方法可自定义执行内容
 * */
public class BPNN  implements NeuralNetListener,Serializable {
	//序列号用于检查兼容性
	private static final long serialVersionUID = -3472219226214066504L;
	//神经网络成员
	private NeuralNet nnet = null;
	//神经网络保存路径
	private String NNet_Path = null;
	
	/**初始化神经网络 
	 * String NNet_Path	神经网络存放路径
	 * int InputNum		输入层神经元个数
	 * int HiddenNum 	隐藏层神经元个数
	 * int OutputNum	输出层神经元个数
	 * */
	public void Init_BPNN(String NNet_Path,int InputNum,int HiddenNum,int OutputNum) {
		//设置新网络的保存路径
		this.NNet_Path = NNet_Path;
		//新建三个Layer，分别作为输入层，隐藏层，输出层
		LinearLayer input = new LinearLayer();
		SigmoidLayer hidden = new SigmoidLayer();
		SigmoidLayer output = new SigmoidLayer();
		//设置每个Layer包含的神经元个数
		input.setRows(InputNum);
		hidden.setRows(HiddenNum);
		output.setRows(OutputNum);
		//新建两条突触，用于连接各层
		FullSynapse synapse_IH = new FullSynapse();
		FullSynapse synapse_HO = new FullSynapse();
		//连接输入-隐藏，隐藏-输出各层
		input.addOutputSynapse(synapse_IH);
		hidden.addInputSynapse(synapse_IH);
		hidden.addOutputSynapse(synapse_HO);
		output.addInputSynapse(synapse_HO);
		//新建一个神经网络，并添加输入层，隐藏层，输出层		
		this.nnet = new NeuralNet();
		this.nnet.addLayer(input, NeuralNet.INPUT_LAYER);
		this.nnet.addLayer(hidden, NeuralNet.HIDDEN_LAYER);
		this.nnet.addLayer(output, NeuralNet.OUTPUT_LAYER);
	}
	/**以本地存储的参数文件初始化神经网络 
	 * String NNet_Path	神经网络存放路径
	 */
	public void Init_BPNN(String NNet_Path){
		this.nnet = Get_BPNN(NNet_Path);
	}
			
	/**使用磁盘文件训练新建的神经网络，需与Init_BPNN搭配使用
	 * String TrainFile	训练文件存放路径
	 * int TrainLength	训练文件的行数
	 * double Rate		神经网络训练速度
	 * double Momentum	神经网络训练动量
	 * int TrainCicles	神经网络训练次数
	 * */
	public void Train_BPNN(String TrainFile,int TrainLength,double Rate,double Momentum,int TrainCicles){	
		//获取输入层
		Layer input = nnet.getInputLayer();		
		//新建输入突触
		FileInputSynapse trains = new FileInputSynapse();
		//设置输入文件
		trains.setInputFile(new File(TrainFile));
		//设置使用的列数，文件第1，2，3，4列作为训练的输入
		trains.setAdvancedColumnSelector("1,2,3,4");
		
		//获取输出层
		Layer output = nnet.getOutputLayer();
		//新建输入突触
		FileInputSynapse target = new FileInputSynapse();
		//设置输入文件
		target.setInputFile(new File(TrainFile));
		//设置使用的列数，文件第5，6列作为训练的目标
		target.setAdvancedColumnSelector("5,6");
		
		//新建训练突触
		TeachingSynapse trainer = new TeachingSynapse();
		//设置训练目标
		trainer.setDesired(target);
		
		//添加输入层的输入突触
		input.addInputSynapse(trains);
		//添加输出层的输出突触
		output.addOutputSynapse(trainer);		
		//设置神经网络的训练突触
		nnet.setTeacher(trainer);

		//获取神经网络的监视器
		Monitor monitor = nnet.getMonitor();
		//设置训练速率
		monitor.setLearningRate(Rate);
		//设置训练动量
		monitor.setMomentum(Momentum);
		//新增监听者
		monitor.addNeuralNetListener(this);// 输入流
		//设置训练数据个数（行数）
		monitor.setTrainingPatterns(TrainLength);
		//设置训练次数
		monitor.setTotCicles(TrainCicles); 
		//打开训练模式
		monitor.setLearning(true);
		//开始训练
		nnet.go();
	}
	/**使用内存数据训练新建的神经网络，需与Init_BPNN搭配使用
	 * double[][] TrainData	训练文件存放路径
	 * double Rate		神经网络训练速度
	 * double Momentum	神经网络训练动量
	 * int TrainCicles	神经网络训练次数
	 * */
	public void Train_BPNN(double[][] TrainData,double Rate,double Momentum,int TrainCicles){	
		
		Layer input = nnet.getInputLayer();		
		//设置输入数组
		MemoryInputSynapse trains = new MemoryInputSynapse();
		trains.setInputArray(TrainData);	
		trains.setAdvancedColumnSelector("1,2,3,4");

		Layer output = nnet.getOutputLayer();
		//设置输入数组
		MemoryInputSynapse target = new MemoryInputSynapse();
		target.setInputArray(TrainData);
		target.setAdvancedColumnSelector("5,6");
		
		TeachingSynapse trainer = new TeachingSynapse();
		trainer.setDesired(target);		
		input.addInputSynapse(trains);
		output.addOutputSynapse(trainer);		
		nnet.setTeacher(trainer);

		Monitor monitor = nnet.getMonitor();
		monitor.setLearningRate(Rate);
		monitor.setMomentum(Momentum);
		monitor.addNeuralNetListener(this);
		monitor.setTrainingPatterns(TrainData.length);
		monitor.setTotCicles(TrainCicles); 
		monitor.setLearning(true);
		nnet.go();
	}
	/**使用磁盘文件再次训练已有的神经网络
	 * String NNet_Path	神经网络存放路径
	 * String TrainFile	训练文件存放路径
	 * int TrainLength	训练文件的行数
	 * double Rate		神经网络训练速度
	 * double Momentum	神经网络训练动量
	 * int TrainCicles	神经网络训练次数
	 * */
	public void Train_BPNN(String NNet_Path,String TrainFile,int TrainLength,double Rate,double Momentum,int TrainCicles){
		//初始化神经网络的保存路径
		this.NNet_Path = NNet_Path;
		//获取保存的神经网络
		this.nnet = this.Get_BPNN(NNet_Path);
		
		Monitor monitor = this.nnet.getMonitor();
		monitor.setLearningRate(Rate);
		monitor.setMomentum(Momentum);
		monitor.addNeuralNetListener(this);
		monitor.setTrainingPatterns(TrainLength);
		monitor.setTotCicles(TrainCicles); 
		monitor.setLearning(true);
		this.nnet.go();
	}
	/**使用磁盘文件测试训练过的神经网络
	 * String NNet_Path	神经网络存放路径
	 * String OutFile	测试结果存放路径	
	 * String TestFile	训练文件存放路径
	 * int TestLength	训练文件的行数
	 * */
	public void Test_BPNN(String NNet_Path,String OutFile,String TestFile,int TestLength){
		NeuralNet testBPNN = this.Get_BPNN(NNet_Path);
		if (testBPNN != null) {	
			
			Layer input = testBPNN.getInputLayer();
			/**采用文件输入测试 */
			FileInputSynapse inputStream = new FileInputSynapse();
			inputStream.setInputFile(new File(TestFile));
			inputStream.setAdvancedColumnSelector("1,2,3,4");
			input.removeAllInputs();
			input.addInputSynapse(inputStream);
			
	    	Layer output = testBPNN.getOutputLayer();
	    	//设置输出突触
	    	FileOutputSynapse fileOutput = new FileOutputSynapse();
	    	//设置输出文件保存路径
	    	fileOutput.setFileName(OutFile);
	    	output.addOutputSynapse(fileOutput);
	    	
	    	Monitor monitor = testBPNN.getMonitor();
			monitor.setTrainingPatterns(TestLength);
	    	monitor.setTotCicles(1);
	    	//关闭训练模式
	    	monitor.setLearning(false);
	    	
	    	//开始测试
	    	testBPNN.go();   	
	    	System.out.println("test");
		}
	}
	/**使用内存矩阵测试训练过的神经网络
	 * String NNet_Path	神经网络存放路径
	 * String OutFile	测试结果存放路径	
	 * double[][] TestData	测试矩阵
	 * */
	public void Test_BPNN(String NNet_Path,String OutFile,double[][] TestData){
		NeuralNet testBPNN = this.Get_BPNN(NNet_Path);
		if (testBPNN != null) {	
			
			Layer input = testBPNN.getInputLayer();			
			/**采用矩阵输入测试*/
			MemoryInputSynapse inputStream = new MemoryInputSynapse();
			input.removeAllInputs();
			input.addInputSynapse(inputStream);
			inputStream.setInputArray(TestData);
			inputStream.setAdvancedColumnSelector("1,2,3,4");
			
	    	Layer output = testBPNN.getOutputLayer();
	    	FileOutputSynapse fileOutput = new FileOutputSynapse();
	    	fileOutput.setFileName(OutFile);
	    	output.addOutputSynapse(fileOutput);
	    	
	    	Monitor monitor = testBPNN.getMonitor();
			monitor.setTrainingPatterns(TestData.length);
	    	monitor.setTotCicles(1);
	    	monitor.setLearning(false);
	    	
	    	testBPNN.go();   	
	    	System.out.println("test");
		}		
	}
	/**使用内存矩阵测试训练过的神经网络
	 * String NNet_Path	神经网络存放路径
	 * double[][] TestData	测试矩阵
	 * int[][] result	返回的测试结果
	 * */
	public int[][] Test_BPNN(String NNet_Path,double[][] TestData){
		//NeuralNet testBPNN = this.Get_BPNN(NNet_Path);
		NeuralNet testBPNN = this.nnet;
		int[][] result = new int[TestData.length][2];
		if (testBPNN != null) {
	    	double[] temp = new double[2];		
	    	
			Layer input = testBPNN.getInputLayer();			
			/**采用矩阵输入测试*/
			MemoryInputSynapse inputStream = new MemoryInputSynapse();
			input.removeAllInputs();
			input.addInputSynapse(inputStream);
			inputStream.setInputArray(TestData);
			inputStream.setAdvancedColumnSelector("1,2,3,4");
			
	    	Layer output = testBPNN.getOutputLayer();
	    	MemoryOutputSynapse fileOutput = new MemoryOutputSynapse();
	    	output.addOutputSynapse(fileOutput);
	    	
	    	Monitor monitor = testBPNN.getMonitor();
			monitor.setTrainingPatterns(TestData.length);
	    	monitor.setTotCicles(1);
	    	monitor.setLearning(false);	    	
	    	testBPNN.go();
	    	
	    	for(int i = 0;i<result.length;i++){
	    		temp = fileOutput.getNextPattern();
	    		result[i][0] = temp[0] < 0.5 ? 0 : 1;
	    		result[i][1] = temp[1] < 0.5 ? 0 : 1;
	    	}

	    	System.out.println("test");
	    	return result;
		}
		return result;
	}
	/**读入已有的神经网络
	 * String NNet_Path	神经网络存放路径
	 * */
	NeuralNet Get_BPNN(String NNet_Path) {
		NeuralNetLoader loader = new NeuralNetLoader(NNet_Path);
		NeuralNet nnet = loader.getNeuralNet();
		return nnet;
	}
	
	/**具体实现所用接口的方法
	 * */
	@Override
	public void cicleTerminated(NeuralNetEvent arg0) {
		// TODO 自动生成的方法存根
		//获取监视器
		Monitor mon = (Monitor)arg0.getSource();
		//获取总的训练次数
		long totalcicles = mon.getTotCicles();
		//获取当前要做的训练次数
		long currentcicle = mon.getCurrentCicle();
		
		//按照一定规律减小训练速率
		int t1 = (int) ((totalcicles*0.3)/100);
		int t2 = (int) ((totalcicles*0.5)/100);
		int t3 = (int) ((totalcicles*0.8)/100);
		if (currentcicle == t1*100){
			double rate = mon.getLearningRate();
			mon.setLearningRate(rate*0.5);			
			System.out.println(1);
		}
		else if (currentcicle == t2*100){
			double rate = mon.getLearningRate();
			mon.setLearningRate(rate*0.5);
			System.out.println(2);
		}
		else if (currentcicle == t3*100){
			double rate = mon.getLearningRate();
			mon.setLearningRate(rate*0.5);
			System.out.println(3);
		}
		
		//获取误差并输出
		double err = mon.getGlobalError();
		if (currentcicle % 100 == 0)
			System.out.println(currentcicle + " epochs remaining - RMSE = " +
		err);		
	}

	@Override
	public void netStarted(NeuralNetEvent arg0) {
		// TODO 自动生成的方法存根
		System.out.println("start");		
	}

	@Override
	public void netStopped(NeuralNetEvent arg0) {
		// TODO 自动生成的方法存根
		System.out.println("Training Stopped...");
		//保存训练完的网络
		try {
			FileOutputStream stream = new FileOutputStream(NNet_Path);
			ObjectOutputStream out = new ObjectOutputStream(stream);
			out.writeObject(nnet);// 写入nnet对象
			out.close();
			System.out.println("Save in "+NNet_Path);
		} catch (Exception excp) {
			excp.printStackTrace();
		}
	}

	@Override
	public void errorChanged(NeuralNetEvent arg0) {
		// TODO 自动生成的方法存根
		
	}

	
	@Override
	public void netStoppedError(NeuralNetEvent arg0, String arg1) {
		// TODO 自动生成的方法存根
		
	}
	
}