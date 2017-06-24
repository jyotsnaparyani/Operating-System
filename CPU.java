//package SYSTEM;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.Arrays;
import java.util.Iterator;

//import ProcessControlBlock;

public class CPU {
	// The CPU subsystem is called from SYSTEM to execute the user job.
	// parameter X denotes the initial value of program counter.
	// parameter Y denotes the trace switch.
	void CPU1(SYSTEM system,int X, int Y) throws Exception {
		

	 system.fromCPU = true; // the fromCPU flag is used to call LOADER to access or
						// write into MEM[] array

		// to execute the instructions of the user job based on its opcode.
		while (true) {
			system.fromCPUStoreMemory = false;
			system.inputOutput = 0;
			system.loader.LOADER1(system,X, system.traceswitch);
			system.instruction = system.tempValue; // tempValue contains the next instruction
										// to be executed from memory.
			// the following right shifts are done to retrieve opcode, A, B, I ,
			// EA from instruction.
			system.isBreak = false;
			system.opcode = (system.instruction >> 24) & (0x1f);
			system.I = (system.instruction >> 31) & (0x1f);
			system.A = (system.instruction >> 20) & (0x0f);
			system.B = (system.instruction >> 16) & (0x0f);
			system.EA = system.instruction & 0xffff;

			int existingJob = system.outerList.get(1).get(0);
			for (ProcessControlBlock pcb : system.listPCB) {
				if (pcb.jobId == existingJob) {
					int newEA = system.getNewEA();
					if (newEA == system.EA) {
						if (system.EA > pcb.midaddr && !pcb.isloaded) {
							system.loadAddress(pcb.midaddr,pcb.endaddress);
							pcb.isloaded = true;
							pcb.pagefaults = pcb.endaddress-pcb.midaddr;
						}
					} else
					{
						if (newEA > pcb.midaddr && !pcb.isloaded) {
							system.loadAddress(pcb.midaddr,pcb.endaddress);
							pcb.isloaded = true;
							pcb.pagefaults = pcb.endaddress-pcb.midaddr;
						}
					}
					
				}
			}

			
			//if(opcode != 0x0F && opcode != 0x10 && opcode != 0x11)
			//checkQuantum(existingJob);
			//if (isBreak)
				//break;
			// to calculate Effective address for index & indirect addressing.

			if (system.B != 0 && system.I == 1) {
				int registerValue = system.getRegisterValue(system.B);
				system.EA = registerValue + system.VirtualMem[system.EA];
			} else if (system.B != 0) {
				int registerValue = system.getRegisterValue(system.B);
				system.EA = registerValue + system.EA;
			}

			// to log into TRACE file if traceswitch is 1 before executing the
			// instruction.
			int runningjob = system.outerList.get(1).get(0);
			for (ProcessControlBlock pcb : system.listPCB) {
				if (pcb.jobId == existingJob) {
					if (pcb.traceBit == 1) {
						File file = new File("TRACE" + runningjob + ".txt");
						FileWriter fileWritter = new FileWriter(file.getName(), true);
						BufferedWriter bufferWritter = new BufferedWriter(fileWritter);
						if (!pcb.isFirstTimeForTrace) {
							system.createTraceFile(bufferWritter);
							pcb.isFirstTimeForTrace = true;
						}
						bufferWritter.newLine();
						bufferWritter.write("Before ");
						system.addLogs(X, bufferWritter, pcb);
						bufferWritter.close();
					}
				}
			}

			// Halt instruction executed
			if (system.opcode == 0x00) {
				system.CLOCK++;
				system.getStatistics();
				system.getQueueContents();
				system.setCumulativeTime(existingJob);
				system.checkAging();
				File file = new File("PROGRESS_FILE.txt");
				FileWriter fileWritter = new FileWriter(file.getName(), true);
				BufferedWriter bufferWritter = new BufferedWriter(fileWritter);
				bufferWritter.newLine();
				bufferWritter.write("Job " + existingJob + " exited");
				bufferWritter.newLine();
				system.normalJobs++;
				for (Iterator<ProcessControlBlock> iterator = system.listPCB.iterator(); iterator.hasNext();) {
					ProcessControlBlock pcb = iterator.next();
					if (pcb.jobId == existingJob || pcb.jobId == 0) {
						for (int i = pcb.initialPC; i <= pcb.endaddress; i++) {
							system.VirtualMem[i] = 0;
							system.Disk[i] = 0;
						}
						system.pc = pcb.initialPC;
						bufferWritter.write("Job Identification Number(in decimal) : " + existingJob);
						bufferWritter.newLine();
						bufferWritter.write("Clock at Load Time(in Hex) : 0 ");
						bufferWritter.newLine();
						if (pcb.errormsg != null)
							bufferWritter.write(pcb.errormsg);
						bufferWritter.newLine();
						bufferWritter.write("Output(in Hex) : ");
						bufferWritter.newLine();
						for (String output : pcb.output) {
							bufferWritter.write(output);
							bufferWritter.newLine();
						}
						bufferWritter
								.write("Clock at Termination Time(in Hex) :" + Integer.toHexString(system.CLOCK));
						bufferWritter.newLine();
						bufferWritter.write("IO time (in decimal) : " + pcb.ioTime);
						system.totalIOtime+=pcb.ioTime;
						if (pcb.traceBit == 1) {

							File file1 = new File("TRACE" + pcb.jobId + ".txt");
							FileWriter fileWritter1 = new FileWriter(file1.getName(), true);
							BufferedWriter bufferWritter1 = new BufferedWriter(fileWritter1);
							bufferWritter1.write("After ");
							system.addLogs(X, bufferWritter1, pcb);
							bufferWritter1.close();

						}
						bufferWritter.newLine();
						bufferWritter.write("Run Time (in decimal) : " + pcb.cumulativeTime);
						bufferWritter.newLine();
						bufferWritter.write("Turn around Time (in decimal) : " + (pcb.cumulativeTime -pcb.ioTime));
						bufferWritter.newLine();
						bufferWritter.write("No of CPU turns(in decimal) : "+ pcb.turns);
						bufferWritter.newLine();
						system.totalpagefaults+=pcb.pagefaults;
						bufferWritter.write("Page fault handling time(in decimal)  : "+ pcb.pagefaults);
						bufferWritter.newLine();
						iterator.remove();

					}
				}

				bufferWritter.newLine();
				bufferWritter.write("Nature of Termination : Normal");
				bufferWritter.newLine();
				system.appendProgress(bufferWritter);
				bufferWritter.close();
				// to log into TRACE file if traceswitch is 1 after executing
				// the instruction.
				system.innerListRunning.remove(0);
				system.isFirstaccess = false;
				if (system.listPCB.size() == 0) {
					Arrays.fill(system.Disk, 0);
					Arrays.fill(system.VirtualMem, 0);
				}
				system.getJob();
				break;
			}
			// Load instruction executed
			else if (system.opcode == 0x01) {
				system.CLOCK++;
				system.getStatistics();
				system.getQueueContents();
				system.checkAging();
				// exeTime++;
				system.setCumulativeTime(existingJob);
				int newEA = system.getNewEA();
				if (newEA ==system.EA) {
					system.loader.LOADER1(system,system.EA, system.traceswitch);
				} else
					system.loader.LOADER1(system,newEA, system.traceswitch);
				int C = system.tempValue;
				if (system.A > 16) {
					system.ERRORCODE = 11;
					ProcessControlBlock pcb = system.getPCB();
					system.errorHandler.ERROR_HANDLER1(system,pcb);
				}
				system.setRegisterValue(C);
				X++;
				system.checkForTraceSwitch(X);
			}
			// Store instruction executed
			else if (system.opcode == 0x02) {
				system.CLOCK++;
				system.getStatistics();
				system.getQueueContents();
				system.checkAging();
				// exeTime++;
				system.setCumulativeTime(existingJob);
				system.fromCPUStoreMemory = true; // the flag used to call LOADER
											// subsystem to access MEM[] array
				int newEA = system.getNewEA();
				if (newEA == system.EA) {
					system.loader.LOADER1(system,system.EA, system.traceswitch);
				} else
					system.loader.LOADER1(system,newEA, system.traceswitch);
				X++;
				system.checkForTraceSwitch(X);
			}
			// Add instruction executed
			else if (system.opcode == 0x03) {
				system.CLOCK++;
				system.getStatistics();
				system.getQueueContents();
				system.checkAging();
				// exeTime++;
				system.setCumulativeTime(existingJob);
				int newEA = system.getNewEA();
				if (newEA == system.EA) {
					system.loader.LOADER1(system,system.EA, system.traceswitch);
				} else
					system.loader.LOADER1(system,newEA, system.traceswitch);
				int C = system.tempValue;
				if (system.A > 16) {
					// Index out of bound exception
					system.ERRORCODE = 11;
					ProcessControlBlock pcb = system.getPCB();
					system.errorHandler.ERROR_HANDLER1(system,pcb);
				}
				int registerValue = system.getRegisterValue(system.A);
				registerValue = registerValue + C;
				system.setRegisterValue(registerValue);
				// REG[A] = REG[A] + C ;
				X++;
				system.checkForTraceSwitch(X);
			}
			// Subtract instruction executed
			else if (system.opcode == 0x04) {
				system.CLOCK++;
				system.getStatistics();
				system.getQueueContents();
				system.checkAging();
				// exeTime++;
				system.setCumulativeTime(existingJob);
				int newEA = system.getNewEA();
				if (newEA == system.EA) {
					system.loader.LOADER1(system,system.EA, system.traceswitch);
				} else
					system.loader.LOADER1(system,newEA, system.traceswitch);
				int C = system.tempValue;
				if (system.A > 16) {
					// Index out of bound exception
					system.ERRORCODE = 11;
					ProcessControlBlock pcb = system.getPCB();
					system.errorHandler.ERROR_HANDLER1(system,pcb);
				}
				int registerValue = system.getRegisterValue(system.A);
				registerValue = registerValue - C;
				system.setRegisterValue(registerValue);
				// REG[A] = REG[A] - C ;
				X++;
				system.checkForTraceSwitch(X);
			}
			// Multiply instruction executed
			else if (system.opcode == 0x05) {
				system.CLOCK = system.CLOCK + 2;
				system.getStatistics();
				system.getQueueContents();
				system.checkAging();
				// exeTime=exeTime+2;
				system.setCumulativeTimeFrRestOperations(existingJob);
				int newEA = system.getNewEA();
				if (newEA == system.EA) {
					system.loader.LOADER1(system,system.EA, system.traceswitch);
				} else
					system.loader.LOADER1(system,newEA, system.traceswitch);
				int C = system.tempValue;
				if (system.A > 16) {
					// Index out of bound exception
					system.ERRORCODE = 11;
					ProcessControlBlock pcb = system.getPCB();
					system.errorHandler.ERROR_HANDLER1(system,pcb);
				}
				int registerValue = system.getRegisterValue(system.A);
				registerValue = registerValue * C;
				system.setRegisterValue(registerValue);
				// REG[A] = REG[A] * C ;
				X++;
				system.checkForTraceSwitch(X);
			}
			// Divide instruction executed
			else if (system.opcode == 0x06) {
				system.setCumulativeTimeFrRestOperations(existingJob);
				system.CLOCK = system.CLOCK + 2;
				system.getStatistics();
				system.getQueueContents();
				system.checkAging();
				// exeTime=exeTime+2;
				int newEA = system.getNewEA();
				if (newEA == system.EA) {
					system.loader.LOADER1(system,system.EA, system.traceswitch);
				} else
					system.loader.LOADER1(system,newEA, system.traceswitch);
				int C = system.tempValue;
				if (system.A > 16) {
					// Index out of bound exception
					system.ERRORCODE = 11;
					ProcessControlBlock pcb = system.getPCB();
					system.errorHandler.ERROR_HANDLER1(system,pcb);
				}
				try {
					int registerValue = system.getRegisterValue(system.A);
					registerValue = registerValue / C;
					system.setRegisterValue(registerValue);
					// REG[A] = REG[A] / C ;
				} catch (Exception ex) // Divide by zero error
				{
					system.ERRORCODE = 4;
					ProcessControlBlock pcb = system.getPCB();
					system.errorHandler.ERROR_HANDLER1(system,pcb);
					break;
				}
				X++;
				system.checkForTraceSwitch(X);
			}
			// Shift left instruction executed
			else if (system.opcode == 0x07) {
				system.CLOCK++;
				system.getStatistics();
				system.getQueueContents();
				system.checkAging();
				// exeTime++;
				system.setCumulativeTime(existingJob);
				if (system.A > 16) {
					// Index out of bound exception
					system.ERRORCODE = 11;
					// ERROR_HANDLER();
				}
				int newEA = system.getNewEA();
				if (newEA == system.EA) {
					int registerValue = system.getRegisterValue(system.A);
					registerValue = registerValue << system.EA;
					system.setRegisterValue(registerValue);
				} else {
					int registerValue = system.getRegisterValue(system.A);
					registerValue = registerValue << newEA;
					system.setRegisterValue(registerValue);
				}
				X++;
				system.checkForTraceSwitch(X);
			}
			// Shift right instruction executed
			else if (system.opcode == 0x08) {
				system.CLOCK++;
				system.getStatistics();
				system.getQueueContents();
				system.checkAging();
				// exeTime++;
				system.setCumulativeTime(existingJob);
				if (system.A > 16) {
					// Index out of bound exception
					system.ERRORCODE = 11;
					// ERROR_HANDLER();
				}
				int newEA = system.getNewEA();
				if (newEA == system.EA) {
					int registerValue = system.getRegisterValue(system.A);
					registerValue = registerValue >> system.EA;
					system.setRegisterValue(registerValue);
				} else {
					int registerValue = system.getRegisterValue(system.A);
					registerValue = registerValue >> newEA;
					system.setRegisterValue(registerValue);
				}
				X++;
				system.checkForTraceSwitch(X);
			}
			// Branch on minus instruction executed
			else if (system.opcode == 0x09) {
				system.CLOCK++;
				system.getStatistics();
				system.getQueueContents();
				system.checkAging();
				// exeTime++;
				system.setCumulativeTime(existingJob);
				if (system.A > 16) {
					// Index out of bound exception
					system.ERRORCODE = 11;
					// ERROR_HANDLER();
				}
				int registerValue =system.getRegisterValue(system.A);
				if (registerValue < 0) {
					int newEA = system.getNewEA();
					if (newEA == system.EA) {
						X = system.EA;
					} else
						X = newEA;
				} else
					X++;
				system.checkForTraceSwitch(X);
			}
			// Branch on plus instruction executed
			else if (system.opcode == 0x0A) {
				system.CLOCK++;
				system.getStatistics();
				system.getQueueContents();
				system.checkAging();
				system.setCumulativeTime(existingJob);
				if (system.A > 16) {
					// Index out of bound exception
					system.ERRORCODE = 11;
					ProcessControlBlock pcb = system.getPCB();
					system.errorHandler.ERROR_HANDLER1(system,pcb);
				}
				int registerValue = system.getRegisterValue(system.A);
				if (registerValue > 0) {
					int newEA = system.getNewEA();
					if (newEA == system.EA) {
						X = system.EA;
					} else {
						if (system.prevInstruction == system.instruction && system.outerList.get(1).get(0) == 27) {
							system.ERRORCODE = 13;
							
							system.abnormalJobs++;
							for (Iterator<ProcessControlBlock> iterator = system.listPCB.iterator(); iterator.hasNext();) {
								ProcessControlBlock pcb = iterator.next();
								if (pcb.jobId == system.outerList.get(1).get(0)) {
									for (int i = pcb.initialPC; i <= pcb.endaddress; i++) {
										system.VirtualMem[i] = 0;
										system.Disk[i] = 0;
									}
									system.errorHandler.ERROR_HANDLER1(system,pcb);
									File file = new File("PROGRESS_FILE.txt");
									FileWriter fileWritter = new FileWriter(file.getName(), true);
									BufferedWriter bufferWritter = new BufferedWriter(fileWritter);
									bufferWritter.newLine();
									bufferWritter.write("Job " + pcb.jobId + " exited with error");
									bufferWritter.newLine();
									bufferWritter.write("Job Identification Number : " + pcb.jobId);
									bufferWritter.newLine();
									bufferWritter.write("Clock at Load Time(in Hex) : 0 ");
									bufferWritter.newLine();
									bufferWritter.write(pcb.errormsg);
									bufferWritter.newLine();
									system.totalexecutiontime+=pcb.cumulativeTime;
									bufferWritter.write("Execution Time (in decimal)" + pcb.cumulativeTime);
									bufferWritter.newLine();
									bufferWritter.close();
									system.timelost = pcb.cumulativeTime +50;
									iterator.remove();
								}
							}
							system.outerList.get(1).remove(0);

							break;
						}
						X = newEA;
						system.prevInstruction = system.instruction;
					}
				} else
					X++;
				system.checkForTraceSwitch(X);
			}
			// Branch on zero instruction executed
			else if (system.opcode == 0x0B) {
				system.CLOCK++;
				system.getStatistics();
				system.getQueueContents();
				system.checkAging();
				// exeTime++;
				system.setCumulativeTime(existingJob);
				if (system.A > 16) {
					// Index out of bound exception
					system.ERRORCODE = 11;
					ProcessControlBlock pcb = system.getPCB();
					system.errorHandler.ERROR_HANDLER1(system,pcb);
				}
				int registerValue = system.getRegisterValue(system.A);
				// if(REG[A]==0)
				if (registerValue == 0) {
					int newEA = system.getNewEA();
					if (newEA == system.EA) {
						X = system.EA;
					} else
						X = newEA;
				} else
					X++;
				system.checkForTraceSwitch(X);
			}
			// Branch and link instruction executed
			else if (system.opcode == 0x0C) {
				system.setCumulativeTimeFrRestOperations(existingJob);
				system.CLOCK = system.CLOCK + 2;
				system.getStatistics();
				system.getQueueContents();
				system.checkAging();
				if (system.A > 16) {
					// Index out of bound exception
					system.ERRORCODE = 11;
					ProcessControlBlock pcb = system.getPCB();
					system.errorHandler.ERROR_HANDLER1(system,pcb);
				}
				system.setRegisterValue(X);
				// REG[A]=X;
				int newEA = system.getNewEA();
				if (newEA == system.EA) {
					X = system.EA;
				} else
					X = newEA;
				system.checkForTraceSwitch(X);
			}
			// And instruction executed
			else if (system.opcode == 0x0D) {
				system.CLOCK++;
				system.getStatistics();
				system.getQueueContents();
				system.checkAging();
				// exeTime++;
				system.setCumulativeTime(existingJob);
				int newEA = system.getNewEA();
				if (newEA == system.EA) {
					system.loader.LOADER1(system,system.EA, system.traceswitch);
				} else
					system.loader.LOADER1(system,newEA, system.traceswitch);
				int C = system.tempValue;
				if (system.A > 16) {
					// Index out of bound exception
					system.ERRORCODE = 11;
					ProcessControlBlock pcb = system.getPCB();
					system.errorHandler.ERROR_HANDLER1(system,pcb);
				}
				int registerValue = system.getRegisterValue(system.A);
				registerValue = registerValue & C;
				system.setRegisterValue(registerValue);
				// REG[A] = REG[A] & C ;
				X++;
				system.checkForTraceSwitch(X);
			}
			// Or instruction executed
			else if (system.opcode == 0x0E) {
				system.CLOCK++;
				system.getStatistics();
				system.getQueueContents();
				system.checkAging();
				// exeTime++;
				system.setCumulativeTime(existingJob);
				int newEA = system.getNewEA();
				if (newEA == system.EA) {
					system.loader.LOADER1(system,system.EA, system.traceswitch);
				} else
					system.loader.LOADER1(system,newEA, system.traceswitch);
				system.loader.LOADER1(system,system.EA, system.traceswitch);
				int C = system.tempValue;
				if (system.A > 16) {
					// Index out of bound exception
					system.ERRORCODE = 11;
					ProcessControlBlock pcb = system.getPCB();
					system.errorHandler.ERROR_HANDLER1(system,pcb);
				}
				int registerValue = system.getRegisterValue(system.A);
				registerValue = registerValue | C;
				system.setRegisterValue(registerValue);
				X++;
				system.checkForTraceSwitch(X);
			}
			// Read instruction executed
			else if (system.opcode == 0x0F) {
				system.CLOCK = system.CLOCK + 10;
				system.getStatistics();
				system.getQueueContents();
				system.checkAging();
				int job = system.outerList.get(1).remove(0);
				system.innerListBlocked.add(job);
				File file = new File("PROGRESS_FILE.txt");
				FileWriter fileWritter = new FileWriter(file.getName(), true);
				BufferedWriter bufferWritter = new BufferedWriter(fileWritter);
				bufferWritter.newLine();
				bufferWritter.write("Job " + job + " blocked");
				bufferWritter.newLine();
				system.appendProgress(bufferWritter);
				bufferWritter.newLine();
				bufferWritter.close();
				X++;
				for (ProcessControlBlock pcb : system.listPCB) {
					if (pcb.jobId == job) {
						pcb.PC = X;
						//pcb.cumulativeTime = 0;
						pcb.ioTime = pcb.ioTime + 10;
					}
				}
				system.checkForTraceSwitchForIO(X);
				break;
			}
			// Write instruction executed
			else if (system.opcode == 0x10) {
				system.CLOCK = system.CLOCK + 10;
				system.getStatistics();
				system.getQueueContents();
				system.checkAging();
				int job = system.outerList.get(1).remove(0);
				system.innerListBlocked.add(job);
				File file = new File("PROGRESS_FILE.txt");
				FileWriter fileWritter = new FileWriter(file.getName(), true);
				BufferedWriter bufferWritter = new BufferedWriter(fileWritter);
				bufferWritter.newLine();
				bufferWritter.write("Job " + job + " blocked");
				bufferWritter.newLine();
				system.appendProgress(bufferWritter);
				bufferWritter.close();
				X++;
				for (ProcessControlBlock pcb : system.listPCB) {
					if (pcb.jobId == job) {
						pcb.PC = X;
						//pcb.cumulativeTime = 0;
						pcb.ioTime = pcb.ioTime + 10;
					}
				}
				system.checkForTraceSwitchForIO(X);
				break;
			}
			// Dump memory instruction executed
			else if (system.opcode == 0x11) {
				system.CLOCK = system.CLOCK + 10;
				system.getStatistics();
				system.getQueueContents();
				system.checkAging();
				int job = system.outerList.get(1).remove(0);
				system.innerListBlocked.add(job);
				File file = new File("PROGRESS_FILE.txt");
				FileWriter fileWritter = new FileWriter(file.getName(), true);
				BufferedWriter bufferWritter = new BufferedWriter(fileWritter);
				bufferWritter.newLine();
				bufferWritter.write("Job " + job + " blocked");
				bufferWritter.newLine();
				system.appendProgress(bufferWritter);
				bufferWritter.close();
				X++;
				for (ProcessControlBlock pcb : system.listPCB) {
					if (pcb.jobId == job) {
						pcb.PC = X;
						//pcb.cumulativeTime = 0;
						pcb.ioTime = pcb.ioTime + 10;
					}
				}
				system.checkForTraceSwitchForIO(X);
				break;
			}
			// invalid opcode error
			else {
				system.ERRORCODE = 1;
				String errmsg = "";
				int currentJob = system.outerList.get(1).remove(0);
				for (ProcessControlBlock pcb : system.listPCB) {
					if (pcb.jobId == currentJob) {
						system.errorHandler.ERROR_HANDLER1(system,pcb);
						errmsg = pcb.errormsg;
					}
				}
				File file = new File("PROGRESS_FILE.txt");
				FileWriter fileWritter = new FileWriter(file.getName(), true);
				BufferedWriter bufferWritter = new BufferedWriter(fileWritter);
				bufferWritter.newLine();
				//bufferWritter.write("ready list jobs: ");
				system.appendProgress(bufferWritter);
				bufferWritter.write("Job " + currentJob + " exited with error");
				system.abnormalJobs++;
				bufferWritter.newLine();
				bufferWritter.write("Job Identification Number : " + currentJob);
				bufferWritter.newLine();
				bufferWritter.write("Clock at Load Time(in Hex) : 0 ");
				bufferWritter.newLine();
				bufferWritter.write(errmsg);
				bufferWritter.newLine();
				//bufferWritter.close();
				for (Iterator<ProcessControlBlock> iterator = system.listPCB.iterator(); iterator.hasNext();) {
					ProcessControlBlock pcb = iterator.next();
					if (pcb.jobId == currentJob) {
						for (int i = pcb.initialPC; i <= pcb.endaddress; i++) {
							system.VirtualMem[i] = 0;
							system.Disk[i] = 0;
						}
						system.totalexecutiontime+=pcb.cumulativeTime;
						bufferWritter.write("Execution Time (in decimal)" + pcb.cumulativeTime);
						bufferWritter.newLine();
						bufferWritter.close();
						iterator.remove();

					}
				}
				break;
			}

		}

	}

	
}
