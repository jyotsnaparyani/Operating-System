//package SYSTEM;

import java.io.IOException;

//import SYSTEM.ProcessControlBlock;

public class ERROR_HANDLER {
		// ERROR_HANDLER subsystem is required to handle warnings and errors of the
		// system.
		// When ever an error occurs in the program,
		// the respective Errorcode is set and then ERROR_HANDLER()
		// subsystem is called from LOADER or CPU.
		void ERROR_HANDLER1(SYSTEM system,ProcessControlBlock pcb) throws IOException {
			// invalid opcode error
			if (system.ERRORCODE == 1) {
				pcb.errormsg = "Nature of Termination : abnormal. Invalid Opcode found in the user job in Loader format.";
			} else if (system.ERRORCODE == 3) {
				pcb.errormsg = "Nature of Termination : abnormal. Invalid Loader/data format. The loader/data format should be in Hexadecimal.";
			}
			// attempt to divide by zero error
			else if (system.ERRORCODE == 4) {
				pcb.errormsg = "Nature of Termination : abnormal. Divide by zero error. A number should not be divided by zero.";
			}
			// memory address fault error
			else if (system.ERRORCODE == 6) {
				pcb.errormsg = "Nature of Termination : abnormal. Memory address fault. An address should be within the lenth of the job.";
			}
			// Suspected Infinite Job
			else if (system.ERRORCODE == 9) {
				pcb.errormsg = "Nature of Termination : abnormal. Suspected Infinite job. A user job should have termination point.";
			}
			// Null Reference Exception
			else if (system.ERRORCODE == 10) {
				pcb.errormsg = "Nature of Termination : abnormal. Null Reference Exception occured as input given is null";
			}
			// Index out of bound Exception
			else if (system.ERRORCODE == 11) {
				pcb.errormsg = "Nature of Termination : abnormal. Index out of bound Exception occured as index has exceeded the length of array";
			}
			// Missing Data
			else if (system.ERRORCODE == 12) {
				pcb.errormsg = "Nature of Termination : abnormal. Missing data";
			}
			// OverFlow
			else if (system.ERRORCODE == 13) {
				pcb.errormsg = "Nature of Termination : abnormal. Program too long";
			}
			// Null Job
			else if (system.ERRORCODE == 14) {
				pcb.errormsg = "Nature of Termination : abnormal. Null Job. (Loader format is null)";
			}
			// Missing Program
			else if (system.ERRORCODE == 15) {
				pcb.errormsg = "Nature of Termination : abnormal. Missing Program";
			}
			// Missing **Job
			else if (system.ERRORCODE == 16) {
				pcb.errormsg = "Nature of Termination : abnormal. Missing **Job";
			}
			// invalid trace flag warning
			if (system.WARNINGCODE == 1) {
				pcb.errormsg = "Warning Message : Invalid or Missing Trace Flag. The Trace flag value should be 0 or 1.";
			} else if (system.WARNINGCODE == 2) {
				pcb.errormsg = "Warning Message : Missing **Data";
			} else if (system.WARNINGCODE == 3) {
				pcb.errormsg = "Warning Message : Missing **FIN";
			} else if (system.WARNINGCODE == 4) {
				pcb.errormsg = "Warning Message : Double **DATA";
			}
		}

}
