//package SYSTEM;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.Arrays;
import java.util.Iterator;

//import SYSTEM.ProcessControlBlock;

public class DiskManage {

	void DiskManager(SYSTEM system, String arg) throws Exception {
		if (system.opcode == 0x0F) // Read instruction semantics are executed
		{
			int bjob = system.outerList.get(2).get(0);
			int newEA = system.getNewEAForIO();
			String errmsg = "";
			for (ProcessControlBlock pcb : system.listPCB) {
				if (pcb.jobId == bjob) {
					if (pcb.diskReadRecord.size() == 0) {
						system.ERRORCODE = 12;
						system.errorHandler.ERROR_HANDLER1(system,pcb);
						errmsg = pcb.errormsg;
					} else {
						for (int i = 0; i < 4; i++) {
	
							if (newEA == system.EA) {
								system.VirtualMem[system.EA] = system.Disk[pcb.diskReadRecord.get(0)];
								pcb.diskReadRecord.remove(0);
								system.EA++;
								newEA++;
							} else {
								system.VirtualMem[newEA] = system.Disk[pcb.diskReadRecord.get(0)];
								pcb.diskReadRecord.remove(0);
								newEA++;
							}
							while (i < 0) {
								// suspected Infinite loop if i is less than
								// zero
								system.ERRORCODE = 9;
								system.errorHandler.ERROR_HANDLER1(system,pcb);
							}
						}
					}
				}
			}
	
			int job = system.outerList.get(2).remove(0);
			if (system.ERRORCODE == 12) {
				for (Iterator<ProcessControlBlock> iterator = system.listPCB.iterator(); iterator.hasNext();) {
					ProcessControlBlock pcb = iterator.next();
					if (pcb.jobId == job) {
						for (int i = pcb.initialPC; i <= pcb.endaddress; i++) {
							system.VirtualMem[i] = 0;
							system.Disk[i] = 0;
						}
						iterator.remove();
						File file = new File("PROGRESS_FILE.txt");
						FileWriter fileWritter = new FileWriter(file.getName(), true);
						BufferedWriter bufferWritter = new BufferedWriter(fileWritter);
						bufferWritter.newLine();
						bufferWritter.write("Job " + job + " exited with error");
						system.abnormalJobs++;
						bufferWritter.newLine();
						bufferWritter.write("Job Identification Number : " + job);
						bufferWritter.newLine();
						bufferWritter.write("Clock at Load Time(in Hex) : 0 ");
						bufferWritter.newLine();
						bufferWritter.write(errmsg);
						bufferWritter.newLine();
						system.appendProgress(bufferWritter);
						bufferWritter.close();
						system.ERRORCODE = 0;
					}
	
				}
			} else {
				for (ProcessControlBlock pcb : system.listPCB) {
					if (pcb.jobId == job) {
						int turn = system.getQueueTurns(pcb.previoussubqueue);
						int qsize = system.getQueueQuantumSize(pcb.previoussubqueue);
						if(pcb.turns>=turn || pcb.cumulativeTime>=qsize)
						{
							if(pcb.previoussubqueue < 3)
							{
								
								system.cntMigrations++;
							system.mainqueue.get(pcb.previoussubqueue+1).add(job);
							pcb.previoussubqueue=pcb.previoussubqueue+1;
							}
							else 
							{
								system.cntMigrations++;
								system.mainqueue.get(0).add(job);
								pcb.previoussubqueue =0;
								
							}
						}
						else
						{
							
							system.mainqueue.get(pcb.previoussubqueue).add(job);
							pcb.turns=0;
						}
					}
					
				}
				File file = new File("PROGRESS_FILE.txt");
				FileWriter fileWritter = new FileWriter(file.getName(), true);
				BufferedWriter bufferWritter = new BufferedWriter(fileWritter);
				bufferWritter.newLine();
				system.appendProgress(bufferWritter);
				bufferWritter.close();
			}
		} else if (system.opcode == 0x10) // Write instruction semantics are executed
		{
			int newEA = system.getNewEAForIO();
			int job = system.outerList.get(2).remove(0);
			File file = new File("PROGRESS_FILE.txt");
			FileWriter fileWritter = new FileWriter(file.getName(), true);
			BufferedWriter bufferWritter = new BufferedWriter(fileWritter);
			bufferWritter.newLine();
			system.appendProgress(bufferWritter);
			bufferWritter.newLine();
			bufferWritter.close();
			if (newEA == system.EA) {
				String output = "";
				for (int j = 0; j < 4; j++) {
					system.Disk[system.EA] = system.VirtualMem[system.EA];
					output = output + String.format("%08x", system.Disk[system.EA]).toUpperCase() + " ";
					system.EA++;
				}
				for (ProcessControlBlock pcb : system.listPCB) {
					if (pcb.jobId == job) {
						pcb.output.add(output);
					}
				}
			} else {
				String output = "";
				for (int j = 0; j < 4; j++) {
					system.Disk[newEA] = system.VirtualMem[newEA];
					output = output + String.format("%08x", system.Disk[system.EA]).toUpperCase() + " ";
					newEA++;
				}
				for (ProcessControlBlock pcb : system.listPCB) {
					if (pcb.jobId == job) {
						pcb.output.add(output);
					}
				}
			}
			for (ProcessControlBlock pcb : system.listPCB) {
				if (pcb.jobId == job) {
					int turn = system.getQueueTurns(pcb.previoussubqueue);
					int qsize = system.getQueueQuantumSize(pcb.previoussubqueue);
					if(pcb.turns>=turn || pcb.cumulativeTime>=qsize)
					{
						if(pcb.previoussubqueue < 3)
						{
							
							
							system.cntMigrations=system.cntMigrations+10;
						system.mainqueue.get(pcb.previoussubqueue+1).add(job);
						pcb.previoussubqueue=pcb.previoussubqueue+1;
						}
						else 
						{
							
							system.cntMigrations=system.cntMigrations+10;
							system.mainqueue.get(0).add(job);
							pcb.previoussubqueue =0;
						}
					}
					else
					{
						
						system.mainqueue.get(pcb.previoussubqueue).add(job);
						pcb.turns=0;
					}
				}
			}
			
		} else {
			if(system.executingtimes == 1)
				system.userjob = arg;
			system.Disk = new Integer[4096];
			Arrays.fill(system.Disk, 0);
			BufferedReader in = new BufferedReader(new FileReader(system.userjob));
			system.line = system.readFile();
			system.isFirstaccess = true;
			system.getJob();
		}
	}

}
