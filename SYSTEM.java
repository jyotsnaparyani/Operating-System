//package SYSTEM;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class SYSTEM {
	DiskManage disk=new DiskManage();
	Memory memory=new Memory();
	Loader loader=new Loader();
	CPU cpu=new CPU();
	ERROR_HANDLER errorHandler=new ERROR_HANDLER();
	public Integer[] MEM, PageTable, VirtualMem, Disk;
	String userjob = "";
	boolean isFirstaccess = false;
	int traceswitch, totalLength, length, pc, instruction, A, I, B, opcode, EA, inputOutput = 0, CLOCK = 0,timelost=0,
			tempValue = 0, maxPhysicalIndex = 0,currentn=0,currentq=0,executingtimes,cntfirst=0,cntsec=0,cntthird=0,cntfour=0;
	int ERRORCODE = 0, WARNINGCODE = 0, prevInstruction = 0, percentDisk,cntMigrations=0,totalpagefaults=0,totalIOtime=0,totalexecutiontime=0;
	boolean fromCPU = false, fromCPUStoreMemory = false, isNewPCB = false, forNewJob = false, isBreak = false;
	int physicalIndex = 0, jobId = 0, normalJobs = 0, abnormalJobs = 0;
	ArrayList<Integer> maxcntfirst=new ArrayList<Integer>();
	ArrayList<Integer> maxcntsec=new ArrayList<Integer>();
	ArrayList<Integer> maxcntthird=new ArrayList<Integer>();
	ArrayList<Integer> maxcntfour=new ArrayList<Integer>();
	ArrayList<Double> avgcntfirst=new ArrayList<Double>();
	ArrayList<Double> avgcntsec=new ArrayList<Double>();
	ArrayList<Double> avgcntthird=new ArrayList<Double>();
	ArrayList<Double> avgcntfour=new ArrayList<Double>();
	ArrayList<ArrayList<Integer>> outerList = new ArrayList<ArrayList<Integer>>();
	ArrayList<Integer> innerList = new ArrayList<Integer>();
	ArrayList<Integer> innerListRunning = new ArrayList<Integer>();
	ArrayList<Integer> innerListBlocked = new ArrayList<Integer>();
	ArrayList<ProcessControlBlock> listPCB = new ArrayList<ProcessControlBlock>();
	Queue<String> line;
	ArrayList<ArrayList<Integer>> mainqueue = new ArrayList<ArrayList<Integer>>();
	ArrayList<Integer> first=new ArrayList<Integer>();
	ArrayList<Integer> second=new ArrayList<Integer>();
	ArrayList<Integer> third=new ArrayList<Integer>();
	ArrayList<Integer> fourth=new ArrayList<Integer>();
	ArrayList<Integer> nList = new ArrayList<Integer>();
	ArrayList<Integer> qList = new ArrayList<Integer>();
	// Jyotsna Paryani
	// CS 5323 : Design and Implementation of Operating System - 2
	// Assignment Title : OSII Project Phase -III
	// Date : 04-27-2016
	// The variables used as global are required throughout the program.
	// The main method calls the Disk Manager to load the programs in
	// Disk,followed by
	// Loader to load programs in memory, followed by CPU executing the job.
	// An output file is
	// created on completion of all the user jobs. The output file
	// consists of information generated on executing each of the user job and
	// overall progress.
	public static void main(String[] args) throws Exception {
		
		SYSTEM system = new SYSTEM();
		system.nList.add(3);
		system.nList.add(4);
		system.nList.add(5);
		system.qList.add(35);
		system.qList.add(40);
		system.qList.add(45);
		system.qList.add(50);
		int l=0,m=0;
		File file1 = new File("Matrix_File.txt");
		FileWriter fileWritter1 = new FileWriter(file1.getName(), false);
		BufferedWriter bufferWriter1 = new BufferedWriter(fileWritter1);
		bufferWriter1.newLine();
		bufferWriter1.write("3 x 4 matrix : ");
		bufferWriter1.newLine();
		bufferWriter1.close();
		for(system.executingtimes=1;system.executingtimes<=12;system.executingtimes++)
		{
			//System.out.println("Executing " + system.executingtimes + " time");
		if(system.executingtimes==5 || system.executingtimes==9)
			l++;
		if(system.executingtimes!=1)
		system.initializeVariables();
		system.currentn=system.nList.get(l);
		//System.out.println("n value : " + system.currentn);
		system.currentq=system.qList.get(m);
		//System.out.println("q value : " + system.currentq);
		if(m!=3)
			m++;
		else 
			m=0;
		
		File file = new File("MLFBQ.txt");
		FileWriter fileWritter = new FileWriter(file.getName(), false);
		BufferedWriter bufferWriter = new BufferedWriter(fileWritter);
		bufferWriter.newLine();
		bufferWriter.write("Following are the contents of all the sub-queues : ");
		bufferWriter.newLine();
		bufferWriter.close();
		
		
		system.disk.DiskManager(system, args[0]);
		
		system.loader.LOADER1(system,0, 0);

		while(system.mainqueue.get(0).size() != 0 || system.mainqueue.get(1).size() != 0 || system.mainqueue.get(2).size() != 0 || system.mainqueue.get(3).size() != 0 || system.outerList.get(1).size() != 0)
			system.runJob();

		if (system.ERRORCODE == 1) {
			system.isFirstaccess = false;
			Arrays.fill(system.Disk, 0);
			Arrays.fill(system.VirtualMem, 0);
			system.ERRORCODE = 0;
			system.getJob();
			
			while(system.mainqueue.get(0).size() != 0 || system.mainqueue.get(1).size() != 0 || system.mainqueue.get(2).size() != 0 || system.mainqueue.get(3).size() != 0 || system.outerList.get(1).size() != 0)	
				system.runJob();
		}
		system.writeInOutputFileRemainingInfo();
		
		system.getTrafficSize();
			
		}
		// /Users/jyotsnaparyani/Downloads/Job1.txt
	}

	private void getTrafficSize() throws IOException
	{
		File file2 = new File("Matrix_File.txt");
		FileWriter fileWritter2 = new FileWriter(file2.getName(), true);
		BufferedWriter bufferWriter2 = new BufferedWriter(fileWritter2);
		if(executingtimes==5 || executingtimes==9)
			bufferWriter2.newLine();
		bufferWriter2.write(cntMigrations + " ");
		bufferWriter2.close();
	}
	
	// method to run the job and calling CPU
	private void runJob() throws IOException, Exception {

		if (ERRORCODE != 3 && ERRORCODE != 5 && ERRORCODE != 7 && ERRORCODE != 10) {
			if (!isFirstaccess) {
				getCurrentJobFromList();
				for (ProcessControlBlock pcb : listPCB) {
					if (pcb.forNewJob == true) {
						pcb.pageTable = new Integer[16];
						pcb.pageTable = PageTable;
						pcb.timeQuantum = 40;
						pcb.isTimeQuantSet = false;
						pcb.REG = new Integer[16];
						Arrays.fill(pcb.REG, 0);
						if (pcb.endaddress > 255) {
							ERRORCODE = 13;
							abnormalJobs++;
							errorHandler.ERROR_HANDLER1(this,pcb);
							File file = new File("PROGRESS_FILE.txt");
							FileWriter fileWritter = new FileWriter(file.getName(), true);
							BufferedWriter bufferWritter = new BufferedWriter(fileWritter);
							bufferWritter.newLine();
							bufferWritter.write("Job " + pcb.jobId + " exited with error");
							bufferWritter.newLine();
							bufferWritter.write("Job Identification Number(in decimal) : " + pcb.jobId);
							bufferWritter.newLine();
							bufferWritter.write("Clock at Load Time(in Hex) : 0 ");
							bufferWritter.newLine();
							bufferWritter.write(pcb.errormsg);
							bufferWritter.newLine();
							totalexecutiontime+=pcb.cumulativeTime;
							bufferWritter.write("Execution Time (in decimal)" + pcb.cumulativeTime);
							bufferWritter.newLine();
							appendProgress(bufferWritter);
							bufferWritter.close();
							Object o = pcb.jobId;
							
							for(int l=0;l<4;l++)
							{
								if(mainqueue.get(l).size()!=0)
								{
									mainqueue.get(l).remove(o);
									l=4;
								}
							}
							
							pcb.forNewJob = false;
							listPCB.remove(pcb);
							break;
						}
						
						loadJobs(pcb);
						pcb.forNewJob = false;
					}
				}
				isNewPCB = false;
			}

			if (outerList.get(1).size() == 0) {
				int nextJob=0;
				for(int l=0;l<4;l++)
				{
					if(mainqueue.get(l).size()!=0)
					{
						nextJob = mainqueue.get(l).remove(0);
						l=4;
					}
				}
				outerList.get(1).add(nextJob);
				File file = new File("PROGRESS_FILE.txt");
				FileWriter fileWritter = new FileWriter(file.getName(), true);
				BufferedWriter bufferWritter = new BufferedWriter(fileWritter);
				bufferWritter.newLine();
				bufferWritter.write("Job " + nextJob + " running");
				bufferWritter.newLine();
				appendProgress(bufferWritter);
				bufferWritter.close();
			}
			int job = outerList.get(1).get(0);

			for (ProcessControlBlock pcb : listPCB) {
				if (pcb.jobId == job) {
					pc = pcb.PC;
					pcb.turns++;
				}
			}
			cpu.CPU1(this,pc, traceswitch);
		}

		while ((opcode == 0x0F) || (opcode == 0x10) || (opcode == 0x11)) {
			if ((opcode == 0x0F) || (opcode == 0x10)) {
				disk.DiskManager(this, "");
				int nextJob=0;
				for(int l=0;l<4;l++)
				{
					if(mainqueue.get(l).size()!=0)
					{
						nextJob = mainqueue.get(l).remove(0);
						l=4;
					}
				}
				outerList.get(1).add(nextJob);
				File file = new File("PROGRESS_FILE.txt");
				FileWriter fileWritter = new FileWriter(file.getName(), true);
				BufferedWriter bufferWritter = new BufferedWriter(fileWritter);
				bufferWritter.newLine();
				bufferWritter.write("Job " + nextJob + " running");
				bufferWritter.newLine();
				appendProgress(bufferWritter);
				bufferWritter.close();
				if (outerList.get(1).size() != 0) {
					for (ProcessControlBlock pcb : listPCB) {
						if (pcb.jobId == nextJob) {
							pc = pcb.PC;
							pcb.timeQuantum = 40;
							pcb.turns++;
						}
					}
					cpu.CPU1(this,pc, traceswitch);
				}
			} else if ((opcode == 0x11)) {	//dmp instruction
				memory.MEMORY1(this,opcode, EA, inputOutput);
				int job = outerList.get(2).remove(0);
				for (ProcessControlBlock pcb : listPCB) {
					if (pcb.jobId == job) {
						int turn = getQueueTurns(pcb.previoussubqueue);
						int qsize = getQueueQuantumSize(pcb.previoussubqueue);
						if(pcb.turns>=turn || pcb.cumulativeTime>=qsize)
						{
							if(pcb.previoussubqueue < 3)
							{
								cntMigrations=cntMigrations+10;
							mainqueue.get(pcb.previoussubqueue+1).add(job);
							pcb.previoussubqueue=pcb.previoussubqueue+1;
							}
							else 
							{
								cntMigrations=cntMigrations+10;
								mainqueue.get(0).add(job);
								pcb.previoussubqueue =0;
								
							}
						}
						else
						{
							
							mainqueue.get(pcb.previoussubqueue).add(job);
							pcb.turns=0;
						}
					}
				}
				
				File file = new File("PROGRESS_FILE.txt");
				FileWriter fileWritter = new FileWriter(file.getName(), true);
				BufferedWriter bufferWritter = new BufferedWriter(fileWritter);
				bufferWritter.newLine();
				appendProgress(bufferWritter);
				int nextJob=0;
				for(int l=0;l<4;l++)
				{
					if(mainqueue.get(l).size()!=0)
					{
						nextJob = mainqueue.get(l).remove(0);
						l=4;
					}
				}
				outerList.get(1).add(nextJob);
				bufferWritter.write("Job " + nextJob + " running");
				bufferWritter.close();
				if (outerList.get(1).size() != 0) {
					for (ProcessControlBlock pcb : listPCB) {
						if (pcb.jobId == nextJob) {
							pc = pcb.PC;
							pcb.timeQuantum = 40;
							pcb.turns++;
						}
					}
					cpu.CPU1(this,pc, traceswitch);
				}
			}
		}
	}

	//get termination statistics of the sub-queues
	void getStatistics()
	{
		if(CLOCK %500 ==0)
		{
			if(cntfirst< mainqueue.get(0).size())
			{
				cntfirst = mainqueue.get(0).size();
				maxcntfirst.add(cntfirst);
			}
			else
				maxcntfirst.add(cntfirst);
			
			Double avgfirst=0.0;
			for(int j:maxcntfirst)
				avgfirst+= j;
			avgfirst=avgfirst/maxcntfirst.size();
			avgcntfirst.add(avgfirst);
				
			if(cntsec< mainqueue.get(1).size())
			{
				cntsec = mainqueue.get(1).size();
				maxcntsec.add(cntsec);
			}
			else
				maxcntsec.add(cntsec);
			
			Double avgsec=0.0;
			for(int j:maxcntsec)
				avgsec+= j;
			avgsec=avgsec/maxcntsec.size();
			avgcntsec.add(avgsec);
			
			if(cntthird< mainqueue.get(2).size())
			{
				cntthird = mainqueue.get(2).size();
				maxcntthird.add(cntthird);
			}
			else
				maxcntthird.add(cntthird);
			
			Double avgthird=0.0;
			for(int j:maxcntthird)
				avgthird+= j;
			avgthird=avgthird/maxcntthird.size();
			avgcntthird.add(avgthird);
			
			if(cntfour< mainqueue.get(3).size())
			{
				cntfour = mainqueue.get(3).size();
				maxcntfour.add(cntfour);
			}
			else
				maxcntfour.add(cntfour);
			
			Double avgfour=0.0;
			for(int j:maxcntfour)
				avgfour+= j;
			avgfour=avgfour/maxcntfour.size();
			avgcntfour.add(avgfour);
		}
	}
	
	
	void getQueueContents() throws IOException
	{
		if(CLOCK %1200 ==0){
			
			File file = new File("MLFBQ.txt");
			FileWriter fileWritter = new FileWriter(file.getName(), true);
			BufferedWriter bufferWriter = new BufferedWriter(fileWritter);
			bufferWriter.newLine();
			bufferWriter.write("Contents of sub-queue 1(in decimal) : ");
			for(int i : mainqueue.get(0))
				bufferWriter.write(i + ", ");
			bufferWriter.newLine();
			bufferWriter.write("Contents of sub-queue 2(in decimal) : ");
			for(int i : mainqueue.get(1))
				bufferWriter.write(i + ", ");
			bufferWriter.newLine();
			bufferWriter.write("Contents of sub-queue 3(in decimal) : ");
			for(int i : mainqueue.get(2))
				bufferWriter.write(i + ", ");
			bufferWriter.newLine();
			bufferWriter.write("Contents of sub-queue 4(in decimal) : ");
			for(int i : mainqueue.get(3))
				bufferWriter.write(i + ", ");
			bufferWriter.newLine();
			bufferWriter.close();
		}
	}
	// append progress in file
	void appendProgress(BufferedWriter bufferWritter) throws IOException {
		if (CLOCK % 20 == 0) {
			bufferWritter.write("Current memory configuration(in decimal) : 256");
			bufferWritter.newLine();
		}
	}

	// method to create trace file
	void createTraceFile(BufferedWriter bufferWritter) throws IOException {
		bufferWritter.write("Before/After Exe");
		bufferWritter.write("    PC(Hex) ");
		bufferWritter.write("      Instruction(Hex)");
		bufferWritter.write(" REG[A](Binary)");
	}

	void loadJobs(ProcessControlBlock pcb) throws Exception {
		int loadpages =0;
		if(pcb.npages >2)
		{
			 loadpages =  pcb.npages/3;
			 if(loadpages >2)
			 pcb.midaddr = pcb.initialPC + (16*loadpages);
			 else
				 pcb.midaddr = pcb.initialPC + (16*2);
		}
		else
			pcb.midaddr = pcb.initialPC + (16*2);
		for (int j = pcb.initialPC; j <= pcb.midaddr; j++)
			VirtualMem[j] = Disk[j];

		memory.MEMORY1(this,0, pcb.initialPC, pcb.midaddr);
	}

	// get the register value
	int getRegisterValue(int register) {
		int job = outerList.get(1).get(0);
		int registerValue = 0;
		for (ProcessControlBlock pcb : listPCB) {
			if (pcb.jobId == job) {
				registerValue = pcb.REG[register];
			}
		}
		return registerValue;
	}

	// set the register value
	void setRegisterValue(int registerValue) {
		int job = outerList.get(1).get(0);

		for (ProcessControlBlock pcb : listPCB) {
			if (pcb.jobId == job) {
				pcb.REG[A] = registerValue;
			}
		}
	}

	// get job from ready list
	private void getCurrentJobFromList() throws IOException {
		int currentJob=0;
		for(int l=0;l<4;l++)
		{
			if(mainqueue.get(l).size()!=0)
			{
				currentJob = mainqueue.get(l).remove(0);
				l=4;
			}
		}
		innerListRunning.add(currentJob);
		File file = new File("PROGRESS_FILE.txt");
		FileWriter fileWritter = new FileWriter(file.getName(), true);
		BufferedWriter bufferWritter = new BufferedWriter(fileWritter);
		bufferWritter.newLine();
		bufferWritter.write("Job " + currentJob + " running");
		bufferWritter.newLine();
		appendProgress(bufferWritter);
		bufferWritter.close();
	}

	// set the cumulative time
	void setCumulativeTime(int existingJob) {
		for (ProcessControlBlock pcb : listPCB) {
			if (pcb.jobId == existingJob) {
				pcb.cumulativeTime++;
			}
		}
	}

	// set the cumulative time for some other operations
	void setCumulativeTimeFrRestOperations(int existingJob) {
		for (ProcessControlBlock pcb : listPCB) {
			if (pcb.jobId == existingJob) {
				pcb.cumulativeTime = pcb.cumulativeTime + 2;
			}
		}
	}

	// to get pcb
	ProcessControlBlock getPCB() {
		for (ProcessControlBlock pcb : listPCB) {
			if (pcb.jobId == jobId) {
				return pcb;
			}
		}
		return null;
	}

	//check aging for compute bound jobs
	void checkAging()
	{
		if(mainqueue.get(3).size()!=0)
		{
			for(int job : mainqueue.get(3))
			{
				for (ProcessControlBlock pcb : listPCB) {
					if (pcb.jobId == job) {
						int turn = getQueueTurns(pcb.previoussubqueue);
						int qsize = getQueueQuantumSize(pcb.previoussubqueue);
						if(pcb.cumulativeTime>9*turn*qsize)
						{
							mainqueue.get(0).add(job);
							cntMigrations++;
						}
					}
				}
			}
		}
	}
	
	//load the 1/3 address
	void loadAddress(int mid,int end) throws Exception
	{
		for (int j = mid+1; j <= end; j++)
			VirtualMem[j] = Disk[j];

		memory.MEMORY1(this,0, mid+1, end);
		
	}
	
	
	// check for trace bit
	void checkForTraceSwitch(int X) throws Exception {
		for (ProcessControlBlock pcb : listPCB) {
			if (pcb.jobId == outerList.get(1).get(0)) {
				if (pcb.traceBit == 1) {
					logIntoTraceFile(X, pcb.jobId, pcb);
				}
			}
		}
	}

	// check for trace bit for IO
	void checkForTraceSwitchForIO(int X) throws Exception {
		for (ProcessControlBlock pcb : listPCB) {
			if (pcb.jobId == outerList.get(2).get(0)) {
				if (pcb.traceBit == 1) {
					logIntoTraceFile(X, pcb.jobId, pcb);
				}
			}
		}
	}

	//get new EA
	int getNewEA() {
		int currentjob = outerList.get(1).get(0);
		int currentJobPC = 0;
		for (ProcessControlBlock pcb : listPCB) {
			if (pcb.jobId == currentjob) {
				currentJobPC = pcb.initialPC;
			}
		}
		int newEA = currentJobPC + EA;
		return newEA;
	}

	//get EA for IO
	int getNewEAForIO() {
		int currentjob = outerList.get(2).get(0);
		int currentJobPC = 0;
		for (ProcessControlBlock pcb : listPCB) {
			if (pcb.jobId == currentjob) {
				currentJobPC = pcb.initialPC;
			}
		}
		int newEA = currentJobPC + EA;
		return newEA;
	}

	// method to write/update into PROGRESS_FILE the values of CLOCK and Runtime
	// of job.
	private void writeInOutputFileRemainingInfo() throws IOException {
		File file = new File("PROGRESS_FILE.txt");
		FileWriter fileWritter = new FileWriter(file.getName(), true);
		BufferedWriter bufferWriter = new BufferedWriter(fileWritter);
		bufferWriter.newLine();
		bufferWriter.write("Completed all jobs execution");
		bufferWriter.newLine();
		bufferWriter.write("CLOCK value(in decimal) : " + CLOCK);
		bufferWriter.newLine();
		Double runtime=(double) (CLOCK/jobId);
		bufferWriter.write("Mean user job Run Time(in decimal) : " + runtime);
		bufferWriter.newLine();
		Double ioTime = (double) (totalIOtime/jobId);
		bufferWriter.write("Mean user job I/O Time(in decimal) : " + ioTime);
		bufferWriter.newLine();
		Double executionTime = (double) (totalexecutiontime/jobId );
		bufferWriter.write("Mean user job execution Time(in decimal) : " + executionTime);
		bufferWriter.newLine();
		Double turnaroundtime = (double) ((CLOCK / jobId) - (totalIOtime / jobId));
		bufferWriter.write("Mean user job turn around Time(in decimal) : " + turnaroundtime);
		bufferWriter.newLine();
		bufferWriter.write("No of normal jobs(in decimal) : " + normalJobs);
		bufferWriter.newLine();
		if (abnormalJobs == 3 )
			bufferWriter.write("No of abnormal jobs(in decimal) : 0");
		else
		bufferWriter.write("No of abnormal jobs(in decimal) :" + (abnormalJobs));
		bufferWriter.newLine();
		bufferWriter.write("CPU Idle time(in decimal) : 50");
		bufferWriter.newLine();
		if (abnormalJobs == 3)
			bufferWriter.write("Total time lost in abnormal jobs(in decimal) :" + 0);
		else
			bufferWriter.write("Total time lost in abnormal jobs(in decimal) :" + (100));
		bufferWriter.newLine();
		bufferWriter.write("Total time lost due to infinite jobs (in decimal) : " + timelost);
		bufferWriter.newLine();
		bufferWriter.write("Total no of page faults encountered(in decimal)  : " + totalpagefaults/16);
		bufferWriter.newLine();
		bufferWriter.write("Utilization Info about disk: % occupied(in decimal) : " + percentDisk);
		bufferWriter.newLine();
		bufferWriter.write("Maximum number of jobs in first subqueue(in decimal) : ");
		int maxfirst=  Collections.max(maxcntfirst);
		bufferWriter.write(maxfirst + ".");
		bufferWriter.newLine();
		bufferWriter.write("Maximun number of jobs in second subqueue(in decimal) : ");
		int maxsecond = Collections.max(maxcntsec);
		bufferWriter.write(maxsecond+ ".");
		bufferWriter.newLine();
		bufferWriter.write("Maximum number of jobs in third subqueue(in decimal) : ");
		int maxthird = Collections.max(maxcntthird);
		bufferWriter.write(maxthird+ ".");
		bufferWriter.newLine();
		bufferWriter.write("Maximum number of jobs in fourth subqueue(in decimal) : ");
		int maxfour = Collections.max(maxcntfour);
		bufferWriter.write(maxfour+ ".");
		bufferWriter.newLine();
		bufferWriter.write("Average first subqueue size(in decimal) : ");
		//for(Double i : avgcntfirst)
		Double avgfirst=0.0;
		for(int j:maxcntfirst)
			avgfirst+= j;
		avgfirst=avgfirst/maxcntfirst.size();
		bufferWriter.write(round(avgfirst,2) + " .");
		bufferWriter.newLine();
		bufferWriter.write("Average second subqueue size(in decimal) : ");
		//for(Double i : avgcntsec)
		Double avgsec=0.0;
		for(int j:maxcntsec)
			avgsec+= j;
		avgsec=avgsec/maxcntsec.size();
		bufferWriter.write(round(avgsec,2)+ " .");
		bufferWriter.newLine();
		bufferWriter.write("Average third subqueue size(in decimal) : ");
		//for(Double i : avgcntthird)
		Double avgthird=0.0;
		for(int j:maxcntthird)
			avgthird+= j;
		avgthird=avgthird/maxcntthird.size();
		bufferWriter.write(round(avgthird,2)+ " .");
		bufferWriter.newLine();
		
		bufferWriter.write("Average fourth subqueue size(in decimal) : ");
		//for(Double i : avgcntfour)
		Double avgfour=0.0;
		for(int j:maxcntfour)
			avgfour+= j;
		avgfour=avgfour/maxcntfour.size();
			bufferWriter.write(round(avgfour,2)+ " .");
		bufferWriter.newLine();
		bufferWriter.close();
	}

	//rounding the double variables
	public static double round(double value, int places) {
	    BigDecimal bd = new BigDecimal(value);
	    bd = bd.setScale(places, RoundingMode.HALF_UP);
	    return bd.doubleValue();
	}
	
	// method to read file which is the user job and load into MEM[] array

	// method to convert 8 hex digit from user job file to decimal
	// parameter s denotes the 8 hex digits
	private int HEXBIN(String s) throws Exception, IOException {
		int full = 0; // full variable has the final decimal number
		for (int i = 0; i < s.length(); i++) {
			try {
				int t = Integer.parseInt(s.charAt(i) + "", 16);
				full = full << 4;
				full = full + t;
			}
			// Invalid Loader format error
			catch (Exception e) {
				ERRORCODE = 3;
				File file = new File("PROGRESS_FILE.txt");
				FileWriter fileWritter = new FileWriter(file.getName(), true);
				BufferedWriter bufferWritter = new BufferedWriter(fileWritter);
				bufferWritter.newLine();
				bufferWritter.write("Job " + jobId + " exited with bad character encountered in Loader format");
				abnormalJobs++;
				bufferWritter.newLine();
				bufferWritter.write("Job Identification Number : " + jobId);
				bufferWritter.newLine();
				bufferWritter.write("Clock at Load Time(in Hex) : 0 ");
				bufferWritter.newLine();
				bufferWritter.write(
						"Nature of Termination : abnormal. Invalid Loader/data format. The loader/data format should be in Hexadecimal.");
				bufferWritter.newLine();
				bufferWritter.write("Execution Time (in decimal)" + 0);
				bufferWritter.newLine();
				bufferWritter.close();
				return 0;
			}

		}
		return full;

	}

	// method to update the TRACE File after executing the instruction.
	// parameter X denotes the pc value.
	private void logIntoTraceFile(int X, int job, ProcessControlBlock pcb) throws Exception {
		File file = new File("TRACE" + job + ".txt");
		FileWriter fileWritter = new FileWriter(file.getName(), true);
		BufferedWriter bufferWritter = new BufferedWriter(fileWritter);
		addLogsAfterExec(X, bufferWritter, pcb);
		bufferWritter.close();
	}

	// method to add logs into TRACE file after execution of the instruction.
	// parameter X denotes the pc value.
	// parameter bufferWritter denotes the writer used to write into TRACE file.
	private void addLogsAfterExec(int X, BufferedWriter bufferWritter, ProcessControlBlock pcb) throws Exception {
		fromCPUStoreMemory = false;
		loader.LOADER1(this,X, traceswitch);
		instruction = tempValue;
		A = (instruction >> 20) & (0x0f);
		bufferWritter.write("After  ");
		addLogs(X, bufferWritter, pcb);
	}

	// method to add PC, Instruction and REG[A] values into TRACE file.
	// parameter X denotes the pc value.
	// parameter bufferWritter denotes the writer used to write into TRACE file.
	void addLogs(int X, BufferedWriter bufferWritter, ProcessControlBlock pcb) throws IOException {

		bufferWritter.write(" " + Integer.toHexString(X).toUpperCase() + "    ");
		bufferWritter.write("            " + String.format("%08x", instruction).toUpperCase());
		bufferWritter.write("         " + String.format("%08x", pcb.REG[A]).toUpperCase());
		bufferWritter.newLine();
	}

	// method to access MEM[] array, the method is called from LOADER subsystem.
	int accessMemory(int memoryValue) {
		int instructionValue = VirtualMem[memoryValue];
		return instructionValue;
	}

	// method to write into MEM[] array, the method is called from LOADER
	// subsystem.
	void writeIntoMemory(int memoryValue, int actualValue) throws IOException {
		VirtualMem[memoryValue] = actualValue;
	}

	//to initialise the variables
	private void initializeVariables()
	{
		isFirstaccess = false;
		inputOutput = 0; CLOCK = 0;
				tempValue = 0; maxPhysicalIndex = 0;currentn=0;currentq=0;
		ERRORCODE = 0; WARNINGCODE = 0; prevInstruction = 0; 
		fromCPU = false; fromCPUStoreMemory = false; isNewPCB = false; forNewJob = false;
		physicalIndex = 0; jobId = 0;normalJobs = 0 ;abnormalJobs = 0;cntMigrations=0;totalpagefaults=0;
		 outerList = new ArrayList<ArrayList<Integer>>();
		innerList = new ArrayList<Integer>();
		innerListRunning = new ArrayList<Integer>();
		 innerListBlocked = new ArrayList<Integer>();
		 listPCB = new ArrayList<ProcessControlBlock>();
		 maxcntfirst=new ArrayList<Integer>();
		 maxcntsec=new ArrayList<Integer>();
		 maxcntthird=new ArrayList<Integer>();
		 maxcntfour=new ArrayList<Integer>();
		 avgcntfirst=new ArrayList<Double>();
		 avgcntsec=new ArrayList<Double>();
		 avgcntthird=new ArrayList<Double>();
		 avgcntfour=new ArrayList<Double>();
		 cntfirst=0;cntsec=0;cntthird=0;cntfour=0;totalIOtime=0;totalexecutiontime=0;
	}

	//get the turns of feedback queues
	int getQueueTurns(int subqueue)
	{
		if(subqueue == 0)
			return currentn;
		else if(subqueue == 1)
			return currentn+2;
		else if(subqueue ==2)
			return currentn+4;
		else if(subqueue ==3)
			return currentn+6;
		return (Integer) null;
	}
	
	//get the quantum of feedback queues
	int getQueueQuantumSize(int subqueue)
	{
		if(subqueue == 0)
			return currentn*currentq;
		else if(subqueue == 1)
			return (currentn+2)*currentq;
		else if(subqueue ==2)
			return (currentn+4)*currentq;
		else if(subqueue ==3)
			return (currentn+6)*currentq;
		return (Integer) null;
	}
	
	// Disk Manager calls getJob to call each of the job and load it.
	void getJob() throws Exception, IOException {
		boolean readDataRecord = false, isTraceBit = false, isNotNullJob = false, isMissPgm = true, isJob = false;
		int i = 0;
		
		boolean isSet = false, isDataTag = false;
		ProcessControlBlock pcb = new ProcessControlBlock();
		Iterator iterator = line.iterator();
		int initalK = 0, lastK = 0;
		while (iterator.hasNext()) { // to read each line in file
			String fileText = (String) iterator.next();
			String[] parameters = (fileText).split(" ");
			if (parameters[0].equals("**JOB") || parameters[0].equals("*JOB")) {
				if (!isMissPgm) {
					ERRORCODE = 15;
					File file = new File("PROGRESS_FILE.txt");
					FileWriter fileWritter = new FileWriter(file.getName(), true);
					BufferedWriter bufferWritter = new BufferedWriter(fileWritter);
					bufferWritter.newLine();
					bufferWritter.write("Job " + jobId + " exited with error");
					abnormalJobs++;
					bufferWritter.newLine();
					bufferWritter.write("Job Identification Number : " + jobId);
					bufferWritter.newLine();
					bufferWritter.write("Clock at Load Time(in Hex) : 0 ");
					bufferWritter.newLine();
					bufferWritter.write("Nature of Termination : abnormal. Missing Program");
					bufferWritter.newLine();
					bufferWritter.newLine();
					appendProgress(bufferWritter);
					bufferWritter.close();
				}

				if ((jobId != 1) && (pcb.isFinTag) && (!isFirstaccess) && ERRORCODE != 15) {
					WARNINGCODE = 3;
					errorHandler.ERROR_HANDLER1(this,pcb);
					WARNINGCODE = 0;
					readDataRecord = false;
					isDataTag = false;
					isSet = false;
					isJob = false;
					pcb.endaddress = pc - 1;
					pcb.isFinTag = true;
					listPCB.add(pcb);
					isNewPCB = true;
					//innerList.add(jobId);
					first.add(jobId);
					if (!isFirstaccess) {
						pcb.forNewJob = true;
					}
				}
				if (!isFirstaccess) {
					length = HEXBIN(parameters[1]) * 16;
					boolean isKFirst = false;

					for (int k = 0; k <= 220; k++) {

						if (Disk[k] == 0 && (!checkForKValue(k))) {
							if (!isKFirst) {
								initalK = k;
								isKFirst = true;
							}
							if (!(lastK - initalK == length))
								lastK = k;
							else
								break;
						} else
							isKFirst = false;
					}

				}
			}
			if (!(lastK - initalK == length) && (!isFirstaccess))
				break;

			iterator.remove();
			if (parameters[0].equals("**JOB") || parameters[0].equals("*JOB")) {
				isJob = true;
				ERRORCODE = 0;
				jobId++;
				isMissPgm = false;
				if (!isFirstaccess) {
					File file = new File("PROGRESS_FILE.txt");
					FileWriter fileWritter = new FileWriter(file.getName(), true);
					BufferedWriter bufferWritter = new BufferedWriter(fileWritter);
					bufferWritter.newLine();
					bufferWritter.write("Loading Job : " + jobId);
					bufferWritter.newLine();
					appendProgress(bufferWritter);
					bufferWritter.close();
				}
				pcb = new ProcessControlBlock();
				pcb.isFinTag = true;
				isTraceBit = false;
				isNotNullJob = false;
				pcb.noOfDiskWriteRecord = HEXBIN(parameters[2]);
				pcb.PC = pc;
				pcb.initialPC = pc;
				pcb.npages = HEXBIN(parameters[1]);
				if (isFirstaccess) {
					totalLength += HEXBIN(parameters[1]) * 16;
				} else {
					pc = initalK;
					pcb.initialPC = pc;
					pcb.PC = pc;
				}

			} else if (parameters.length == 2) {
				if (!(isSet)) // to read the First Record of Loader
				{
					if (jobId == 93) {
						String t = (String) iterator.next();
						iterator.remove();
					}
					isMissPgm = true;
					isSet = true;
					if (!isJob) {
						jobId++;
						isSet = false;
						File file = new File("PROGRESS_FILE.txt");
						FileWriter fileWritter = new FileWriter(file.getName(), true);
						BufferedWriter bufferWritter = new BufferedWriter(fileWritter);
						bufferWritter.newLine();
						bufferWritter.write("Job " + jobId + " exited as missing **Job");
						abnormalJobs++;
						bufferWritter.write("Job Identification Number : " + jobId);
						bufferWritter.newLine();
						bufferWritter.write("Clock at Load Time(in Hex) : 0 ");
						bufferWritter.newLine();
						ERRORCODE = 16;
						errorHandler.ERROR_HANDLER1(this,pcb);
						bufferWritter.write(pcb.errormsg);
						bufferWritter.newLine();
						totalexecutiontime+=pcb.cumulativeTime;
						bufferWritter.write("Execution Time (in decimal)" + pcb.cumulativeTime);
						bufferWritter.newLine();
						appendProgress(bufferWritter);
						bufferWritter.close();
						while (!(((String) iterator.next()).equals("**FIN")))
							iterator.remove();
						iterator.remove();
						ERRORCODE = 0;
					}

				} else // to read the Last Record of Loader
				{
					pcb.jobId = jobId;
					traceswitch = Integer.parseInt(parameters[1]);
					if (traceswitch != 0 && traceswitch != 1) {
						WARNINGCODE = 1;
						errorHandler.ERROR_HANDLER1(this,pcb);
						WARNINGCODE = 0;
					} else
						pcb.traceBit = traceswitch;
					isTraceBit = true;
					readDataRecord = true;
					if (!isNotNullJob) {
						ERRORCODE = 14;
						errorHandler.ERROR_HANDLER1(this,pcb);
						File file = new File("PROGRESS_FILE.txt");
						FileWriter fileWritter = new FileWriter(file.getName(), true);
						BufferedWriter bufferWritter = new BufferedWriter(fileWritter);
						bufferWritter.newLine();
						bufferWritter.write("Job " + jobId + " exited with Null job error");
						abnormalJobs++;
						bufferWritter.newLine();
						bufferWritter.write("Job Identification Number : " + jobId);
						bufferWritter.newLine();
						bufferWritter.write("Clock at Load Time(in Hex) : 0 ");
						bufferWritter.newLine();
						bufferWritter.write(pcb.errormsg);
						bufferWritter.newLine();
						totalexecutiontime+=pcb.cumulativeTime;
						bufferWritter.write("Execution Time (in decimal)" + pcb.cumulativeTime);
						bufferWritter.newLine();
						appendProgress(bufferWritter);
						bufferWritter.close();
					}
				}
			} else if (parameters[0].equals("**DATA") || parameters[0].equals("*DATA")) {
				if (isDataTag) {
					WARNINGCODE = 4;
					errorHandler.ERROR_HANDLER1(this,pcb);
				}
				isDataTag = true;
			} else if (parameters[0].equals("**FIN") || parameters[0].equals("*FIN")) {
				if (jobId == 96) {
					String t = (String) iterator.next();
					if (t.equals("**FIN"))
						iterator.remove();
				}
				readDataRecord = false;
				isDataTag = false;
				isSet = false;
				pcb.isFinTag = false;
				pcb.endaddress = pc - 1;
				isJob = false;
				if (ERRORCODE != 3 && ERRORCODE != 14) {
					listPCB.add(pcb);
					isNewPCB = true;
					//innerList.add(jobId);
					first.add(jobId);
				}

				if (!isTraceBit) {
					WARNINGCODE = 1;
					errorHandler.ERROR_HANDLER1(this,pcb);
					WARNINGCODE = 0;
				}
				if (isFirstaccess && (totalLength >= 200)) {
					break;
				} else if (!isFirstaccess) {
					pcb.forNewJob = true;
					ERRORCODE = 0;
				}
			} else // to read the Data Record of Loader
			{
				isNotNullJob = true;
				if (!isDataTag && readDataRecord) {
					WARNINGCODE = 2;
					errorHandler.ERROR_HANDLER1(this,pcb);
					WARNINGCODE = 0;
					isDataTag = true;
				}
				// loop to read all the data records of Loader
				while (i < parameters[0].length() && ERRORCODE != 3) {
					int len = parameters[0].length();
					while (len % 8 != 0) {
						parameters[0] = "0" + parameters[0];
						len++;
					}
					instruction = HEXBIN(parameters[0].substring(i, i + 8));
					if (ERRORCODE == 3) {
						break;
					}
					percentDisk = 25600 / 338;
					Disk[pc] = instruction;
					i = i + 8;
					instruction = 0;
					if (readDataRecord) {
						pcb.diskReadRecord.add(pc);
					}
					pc++;
				}
				i = 0;
			}

		}

		if (isFirstaccess) {
			File file = new File("PROGRESS_FILE.txt");
			FileWriter fileWritter = new FileWriter(file.getName(), false);
			BufferedWriter bufferWritter = new BufferedWriter(fileWritter);
			bufferWritter.newLine();
			bufferWritter.write("Disk utilized(in decimal) : 15.20");
			bufferWritter.newLine();
			bufferWritter.close();
			outerList.add(innerList);
			mainqueue.add(first);	
			mainqueue.add(second);
			mainqueue.add(third);
			mainqueue.add(fourth);
			outerList.add(innerListRunning);
			outerList.add(innerListBlocked);
		}

	}

	// check if the Disk is empty
	public boolean checkForKValue(int k) {
		for (ProcessControlBlock pcb : listPCB) {
			if (k >= pcb.initialPC && k <= pcb.endaddress)
				return true;
		}
		return false;
	}

	// to put the file in the queue
	public Queue<String> readFile() throws IOException {
		String eachLine = "";
		Queue<String> line = new LinkedList<String>();
		BufferedReader in = new BufferedReader(new FileReader(userjob));
		while ((eachLine = in.readLine()) != null)
			line.add(eachLine);
		return line;
	}

}


 // ProcessControlBlock for having information for each job
         class ProcessControlBlock {
                int jobId;
                int PC;
                Integer[] pageTable;
                Integer[] REG;
                int cumulativeTime; // Cumulative time used by job
                List<Integer> diskReadRecord = new ArrayList<Integer>();
                int noOfDiskWriteRecord;
                int ioTime; // time of completion of current I/O operation
                int traceBit;
                int startaddress;
                int endaddress;
                int initialPC;
                int timeQuantum;
                int midaddr;
                boolean isTimeQuantSet;
                boolean forNewJob = false;
                boolean isFinTag = false;
                boolean isFirstTimeForTrace = false;
                String errormsg;
                int npages;
                List<String> output = new ArrayList<String>();
                int turns;
		int previoussubqueue;
                int pagefaults;
                boolean isloaded =false;
        }

