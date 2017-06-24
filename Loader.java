//package SYSTEM;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.Arrays;

//import SYSTEM.ProcessControlBlock;

public class Loader {

	// The LOADER subsystem is called from SYSTEM to load the user job
		// which is required in CPU subsystem for execution
		// of LOAD and STORE instruction. Moreover, to also retrieve the next
		// instruction from memory.
		void LOADER1(SYSTEM system,int startingaddress, int traceswitch) throws Exception {
			// to access memory, a call is made from CPU to LOADER as only LOADER
			// can access the MEM[] array directly.
			if (system.fromCPU && !system.fromCPUStoreMemory) {
				system.tempValue = system.accessMemory(startingaddress);
			}
			// to write into memory, a call is made from CPU to LOADER as only
			// LOADER can access the MEM[] array directly.
			else if (system.fromCPU && system.fromCPUStoreMemory) {
				int registerValue = system.getRegisterValue(system.A);
				system.writeIntoMemory(startingaddress, registerValue);
			}
			// to read the loader format job file and to load the job in
			// memory(MEM[] array).
			else {
				system.MEM = new Integer[256];
				for (int i = 0; i < system.MEM.length; i++) {
					system.MEM[i] = 0;
				}

				system.VirtualMem = new Integer[256];
				for (int i = 0; i < system.VirtualMem.length; i++) {
					system.VirtualMem[i] = 0;
				}

				system.PageTable = new Integer[16];
				for (int i = 0; i < system.PageTable.length; i++) {
					system.PageTable[i] = i + 1;
				}
				
				int currentJob = system.mainqueue.get(0).remove(0);
				system.innerListRunning.add(currentJob);
				File file = new File("PROGRESS_FILE.txt");
				FileWriter fileWritter = new FileWriter(file.getName(), true);
				BufferedWriter bufferWritter = new BufferedWriter(fileWritter);
				bufferWritter.newLine();
				bufferWritter.write("Job " + currentJob + " running");
				bufferWritter.newLine();
				bufferWritter.close();
				for (ProcessControlBlock pcb : system.listPCB) {
					pcb.pageTable = new Integer[16];
					pcb.pageTable = system.PageTable;
					pcb.previoussubqueue =0;
					pcb.timeQuantum = 40;
					pcb.isTimeQuantSet = false;
					pcb.REG = new Integer[16];
					Arrays.fill(pcb.REG, 0);
				}
				
				
				for (ProcessControlBlock pcb : system.listPCB) {
					system.loadJobs(pcb);
				}

			}
		}

}
