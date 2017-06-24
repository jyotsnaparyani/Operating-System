//package SYSTEM;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;

public class Memory {

	// The MEMORY subsystem is called from SYSTEM so as to read from or write
		// into memory while executing the user job in CPU subsystem.
		// parameter X denotes the READ, WRITE or DUMP instruction.
		// parameter Y denotes the memory address register.
		// parameter Z denotes the memory buffer register.
		void MEMORY1(SYSTEM system,int X, int Y, int Z) throws Exception {
			if (X == 0x11) // DUMP instruction semantics are executed
			{
				int j = 0, l = 0, m = 0;
				int actualIteration = 0;
				File file = new File("PROGRESS_FILE.txt");
				FileWriter fileWritter = new FileWriter(file.getName(), true);
				BufferedWriter bufferWritter = new BufferedWriter(fileWritter);
				bufferWritter.write("Output of the Dump instruction of job : " + system.outerList.get(2).get(0));
				bufferWritter.newLine();
				actualIteration = system.maxPhysicalIndex / 8 + 1;
				for (int k = 0; k < actualIteration; k++) {
					bufferWritter.write((String.format("%04x", m)).toUpperCase());
					j = 0;
					m = m + 8;
					while (j < 8 && l <= system.maxPhysicalIndex) {
						bufferWritter.write(" " + String.format("%08x", system.MEM[l]).toUpperCase() + " ");
						l++;
						j++;
					}
					bufferWritter.newLine();
				}
				bufferWritter.close();
			} else {
				for (int k = Y; k <= Z; k++) {
					int index = (k >> 4) & 0x7;
					int pageTableValue = system.PageTable[index];
					int address = (k) & 0xf;
					system.physicalIndex = (pageTableValue << 4) | address;
					system.MEM[system.physicalIndex] = system.VirtualMem[k];
					if (system.maxPhysicalIndex < system.physicalIndex)
						system.maxPhysicalIndex = system.physicalIndex;
				}
			}
		}

}
