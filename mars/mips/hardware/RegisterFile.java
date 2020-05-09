   package mars.mips.hardware;

   import java.util.Observer;

   import mars.Globals;
   import mars.assembler.SymbolTable;
   import mars.mips.instructions.Instruction;
   import mars.util.Binary;

/*
Copyright (c) 2003-2008,  Pete Sanderson and Kenneth Vollmar

Developed by Pete Sanderson (psanderson@otterbein.edu)
and Kenneth Vollmar (kenvollmar@missouristate.edu)

Permission is hereby granted, free of charge, to any person obtaining 
a copy of this software and associated documentation files (the 
"Software"), to deal in the Software without restriction, including 
without limitation the rights to use, copy, modify, merge, publish, 
distribute, sublicense, and/or sell copies of the Software, and to 
permit persons to whom the Software is furnished to do so, subject 
to the following conditions:

The above copyright notice and this permission notice shall be 
included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, 
EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF 
MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. 
IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR 
ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF 
CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION 
WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.

(MIT license, http://www.opensource.org/licenses/mit-license.html)
 */

/**
  *  Represents the collection of MIPS registers.
  *   @author Jason Bumgarner, Jason Shrewsbury
  *   @version June 2003
  **/

    public  class RegisterFile {
   
      //TODO: figure out what the deal is with these
      public static final int GLOBAL_POINTER_REGISTER = 27;
      public static final int STACK_POINTER_REGISTER = 28;

      private static boolean flagN = false;
      private static boolean flagZ = false;
      private static boolean flagV = false;
      private static boolean flagC = false;
   
      private static Register [] regFile = 
          { new Register("X0", 0, 0),
        	new Register("X1", 1, 0),
         	new Register("X2", 2, 0),
         	new Register("X3", 3, 0),
         	new Register("X4", 4, 0),
         	new Register("X5", 5, 0),
         	new Register("X6", 6, 0),
         	new Register("X7", 7, 0),
         	new Register("X8", 8, 0),
         	new Register("X9", 9, 0),
         	new Register("X10", 10, 0),
         	new Register("X11", 11, 0), 
         	new Register("X12", 12, 0),
         	new Register("X13", 13, 0),
         	new Register("X14", 14, 0),
         	new Register("X15", 15, 0),
         	new Register("X16", 16, 0),
         	new Register("X17", 17, 0),
         	new Register("X18", 18, 0),
         	new Register("X19", 19, 0),
         	new Register("X20", 20, 0),
         	new Register("X21", 21, 0),
         	new Register("X22", 22, 0),
         	new Register("X23", 23, 0),
         	new Register("X24", 24, 0),
         	new Register("X25", 25, 0),
         	new Register("X26", 26, 0),
         	new Register("X27", 27, 0),
         	new Register("X28", 28, Memory.stackPointer),
         	new Register("X29", 29, 0),
         	new Register("X30", 30, 0),
         	new Register("XZR", 31, 0),
         	// 32-high-order-bit registers for 64bit operations
         	// reg + 33 retrieves the corresponding register for high order bits of the specific register
         	new Register("W0", 33, 0),
        	new Register("W1", 34, 0),
         	new Register("W2", 35, 0),
         	new Register("W3", 36, 0),
         	new Register("W4", 37, 0),
         	new Register("W5", 38, 0),
         	new Register("W6", 39, 0),
         	new Register("W7", 40, 0),
         	new Register("W8", 41, 0),
         	new Register("W9", 42, 0),
         	new Register("W10", 43, 0),
         	new Register("W11", 44, 0), 
         	new Register("W12", 45, 0),
         	new Register("W13", 46, 0),
         	new Register("W14", 47, 0),
         	new Register("W15", 48, 0),
         	new Register("W16", 49, 0),
         	new Register("W17", 50, 0),
         	new Register("W18", 51, 0),
         	new Register("W19", 52, 0),
         	new Register("W20", 53, 0),
         	new Register("W21", 54, 0),
         	new Register("W22", 55, 0),
         	new Register("W23", 56, 0),
         	new Register("W24", 57, 0),
         	new Register("W25", 58, 0),
         	new Register("W26", 59, 0),
         	new Register("W27", 60, 0),
         	new Register("W28", 61, 0),
         	new Register("W29", 62, 0),
         	new Register("W30", 63, 0),
         	new Register("WZR", 64, 0)
           };

      private static Register programCounter= new Register("pc", 32, Memory.textBaseAddress);
  
      

  	/**
  	 * Gets the value of the Negative flag
  	 * @return the boolean value of the Negative flag
  	 **/

  	public static boolean flagN() {
  		return flagN;
  	}

  	/**
  	 * Gets the value of the Zero flag
  	 * @return the boolean value of the Zero flag
  	 **/

  	public static boolean flagZ() {
  		return flagZ;
  	}

  	/**
  	 * Gets the value of the oVerflow flag
  	 * @return the boolean value of the oVerflow flag
  	 **/

  	public static boolean flagV() {
  		return flagV;
  	}

  	/**
  	 * Gets the value of the Carry flag
  	 * @return the boolean value of the Carry flag
  	 **/

  	public static boolean flagC() {
  		return flagC;
  	}

  	/**
  	 * Gets a string that represents the flag values (NZVC) in order, as either a one or zero.
  	 * @return a string with 4 characters
  	 **/

  	public static String flagString() {
  		String result = "" + (flagN ? 1 : 0) + (flagZ ? 1 : 0) + (flagV ? 1 : 0) + (flagC ? 1 : 0);
  		return result;
  	}

  	/**
  	 * Sets the processor flags based on input.
  	 * @param value
  	 * 				int that determines the Negative and Zero flag values
  	 * @param overflow
  	 * 				determines the oVerflow flag value
  	 * @param carry
  	 * 				determines the Carry flag value
  	 **/

  	public static void setFlags(int value, boolean overflow, boolean carry) {
  		flagN = Math.abs(value) != value;
  		flagZ = 0 == value;
  		flagV = overflow;
  		flagC = carry;
  	}
  	
  	
  	
  	
	/**
	 * Method for displaying the register values for debugging.
	 **/

	public static void showRegisters() {
		for (int i = 0; i < regFile.length; i++) {
			System.out.println("Name: " + regFile[i].getName());
			System.out.println("Number: " + regFile[i].getNumber());
			System.out.println("Value: " + regFile[i].getValue());
			System.out.println("");
		}
	}

	/**
	 * This method updates the register value who's number is num. Also handles
	 * the lo and hi registers
	 * 
	 * @param num
	 *            Register to set the value of.
	 * @param val
	 *            The desired value for the register.
	 **/

	public static int updateRegister(int num, int val) {
		int old = 0;
		if (num == 31) {
			// System.out.println("You can not change the value of the zero  register.");
		} else {
			for (int i = 0; i < regFile.length; i++) {
				if (regFile[i].getNumber() == num) {
					old = (Globals.getSettings().getBackSteppingEnabled())
							? Globals.program.getBackStepper().addRegisterFileRestore(num, regFile[i].setValue(val))
							: regFile[i].setValue(val);
					break;
				}
			}
		}
		return old;
	}
	
	public static long updateLongRegister(int num, long val) {
		int old1 = 0;
		int old2 = 0;
		int changedFlag = 0;
	    int hi = (int)(val >>> 32);
	    int lo = (int)(val << 32 >>> 32);
	    System.out.println("Updating r["+num+"] = "+String.format("0x%08X", hi) + " | " + String.format("0x%08X", lo));
		if (num == 31) {
			// System.out.println("You can not change the value of the zero  register.");
		} else {
			for (int i = 0; i < regFile.length; i++) {
				if (regFile[i].getNumber() == num && i < 31) {
					if(Globals.getSettings().getBackSteppingEnabled()){
						System.out.println("Changing register "+regFile[i].getName());
						old1 = Globals.program.getBackStepper().addRegisterFileRestore(num, regFile[i].setValue(lo));
						System.out.println("Changing register "+regFile[i+32].getName());
						old2 = Globals.program.getBackStepper().addRegisterFileRestore(num, regFile[i+32].setValue(hi));
					} else {
						regFile[i].setValue(lo);
						regFile[i+32].setValue(hi);
					}
					break;
				}
			}
		}
		return ((long)(hi) << 32 | (long)(lo) & 0xFFFFFFFFL);
	}
	
	/**
	 * TODO IMPLEMENTATION OF 64BIT REGISTERS
	 * 
	 * @param num
	 *            Register to set the value of.
	 * @param val
	 *            The desired value for the register.
	 *            
	 * @author GABRIEL RIBEIRO
	 **/

	/*
	public static long updateRegister(int num, long val) {
	    int hi = (int)(val >>> 32);
	    int lo = (int)(val << 32 >>> 32);
	    
		long old = 0;
		if (num == 31) {
			// System.out.println("You can not change the value of the zero  register.");
		} else {
			for (int i = 0; i < regFile.length; i++) {
				if (regFile[i].getNumber() == num) {
					old = (Globals.getSettings().getBackSteppingEnabled())
							? Globals.program.getBackStepper().addRegisterFileRestore(num, regFile[i].setValue(val))
							: regFile[i].setValue(val);
					break;
				}
			}
		}
		return old;
	}*/

	/**
	 * Sets the value of the register given to the value given.
	 * 
	 * @param reg
	 *            Name of register to set the value of.
	 * @param val
	 *            The desired value for the register.
	 **/

	public static void updateRegister(String reg, int val) {
		if (reg.equals("zero")) {
			// System.out.println("You can not change the value of the zero
			// register.");
		} else {
			for (int i = 0; i < regFile.length; i++) {
				if (regFile[i].getName().equals(reg)) {
					updateRegister(i, val);
					break;
				}
			}
		}
	}

	/**
	 * Returns the value of the register who's number is num.
	 * 
	 * @param num
	 *            The register number.
	 * @return The value of the given register.
	 **/

	public static int getValue(int num) {
		return regFile[num].getValue();

	}
	public static long getLongValue(int num) {
		if(num<32) {
			long hi = (long)(regFile[num+32].getValue()) << 32;
			long lo = (long)(regFile[num].getValue()) & 0xFFFFFFFFL;
			return (hi|lo);
		}
		return regFile[num].getValue();

	}

	/**
	 * For getting the number representation of the register.
	 * 
	 * @param n
	 *            The string formatted register name to look for.
	 * @return The number of the register represented by the string or -1 if no
	 *         match.
	 **/

	public static int getNumber(String n) {
		int j = -1;
		for (int i = 0; i < regFile.length; i++) {
			if (regFile[i].getName().equals(n)) {
				j = regFile[i].getNumber();
				break;
			}
		}
		return j;
	}

	/**
	 * For returning the set of registers.
	 * 
	 * @return The set of registers.
	 **/

	public static Register[] getRegisters() {
		return regFile;
	}

	/**
	 * Get register object corresponding to given name. If no match, return
	 * null.
	 * 
	 * @param Rname
	 *            The register name, either in $0 or $zero format.
	 * @return The register object,or null if not found.
	 **/

	public static Register getUserRegister(String Rname) {
		Register reg = null;
		if (Rname.charAt(0) == 'X') {
			try {
				// check for register number 0-31.
				reg = regFile[Binary.stringToInt(Rname.substring(1))]; // KENV
																		// 1/6/05
			} catch (Exception e) {
				// handles both NumberFormat and ArrayIndexOutOfBounds
				// check for register mnemonic $zero thru $ra
				reg = null; // just to be sure
				// just do linear search; there aren't that many registers
				for (int i = 0; i < regFile.length; i++) {
					if (Rname.equals(regFile[i].getName())) {
						reg = regFile[i];
						break;
					}
				}
			}
		}
		return reg;
	}

	/**
	 * For initializing the Program Counter. Do not use this to implement jumps
	 * and branches, as it will NOT record a backstep entry with the restore
	 * value. If you need backstepping capability, use setProgramCounter
	 * instead.
	 * 
	 * @param value
	 *            The value to set the Program Counter to.
	 **/

	public static void initializeProgramCounter(int value) {
		programCounter.setValue(value);
	}

	/**
	 * Will initialize the Program Counter to either the default reset value, or
	 * the address associated with source program global label "main", if it
	 * exists as a text segment label and the global setting is set.
	 * 
	 * @param startAtMain
	 *            If true, will set program counter to address of statement
	 *            labeled 'main' (or other defined start label) if defined. If
	 *            not defined, or if parameter false, will set program counter
	 *            to default reset value.
	 **/

	public static void initializeProgramCounter(boolean startAtMain) {
		int mainAddr = Globals.symbolTable.getAddress(SymbolTable.getStartLabel());
		if (startAtMain && mainAddr != SymbolTable.NOT_FOUND
				&& (Memory.inTextSegment(mainAddr) || Memory.inKernelTextSegment(mainAddr))) {
			initializeProgramCounter(mainAddr);
		} else {
			initializeProgramCounter(programCounter.getResetValue());
		}
	}

	/**
	 * For setting the Program Counter. Note that ordinary PC update should be
	 * done using incrementPC() method. Use this only when processing jumps and
	 * branches.
	 * 
	 * @param value
	 *            The value to set the Program Counter to.
	 * @return previous PC value
	 **/

	public static int setProgramCounter(int value) {
		int old = programCounter.getValue();
		programCounter.setValue(value);
		if (Globals.getSettings().getBackSteppingEnabled()) {
			Globals.program.getBackStepper().addPCRestore(old);
		}
		return old;
	}

	/**
	 * For returning the program counters value.
	 * 
	 * @return The program counters value as an int.
	 **/

	public static int getProgramCounter() {
		return programCounter.getValue();
	}

	/**
	 * Returns Register object for program counter. Use with caution.
	 * 
	 * @return program counter's Register object.
	 */
	public static Register getProgramCounterRegister() {
		return programCounter;
	}

	/**
	 * For returning the program counter's initial (reset) value.
	 * 
	 * @return The program counter's initial value
	 **/

	public static int getInitialProgramCounter() {
		return programCounter.getResetValue();
	}

	/**
	 * Method to reinitialize the values of the registers. <b>NOTE:</b> Should
	 * <i>not</i> be called from command-mode MARS because this this method uses
	 * global settings from the registry. Command-mode must operate using only
	 * the command switches, not registry settings. It can be called from tools
	 * running stand-alone, and this is done in
	 * <code>AbstractMarsToolAndApplication</code>.
	 **/

	public static void resetRegisters() {
		for (int i = 0; i < regFile.length; i++) {
			regFile[i].resetValue();
		}
                flagN = false;
                flagZ = false;
                flagV = false;
                flagC = false;
		initializeProgramCounter(Globals.getSettings().getStartAtMain());// replaces
																			// "programCounter.resetValue()",
																			// DPS
																			// 3/3/09
	}

	/**
	 * Method to increment the Program counter in the general case (not a jump
	 * or branch).
	 **/

	public static void incrementPC() {
		programCounter.setValue(programCounter.getValue() + Instruction.INSTRUCTION_LENGTH);
	}

	/**
	 * Each individual register is a separate object and Observable. This handy
	 * method will add the given Observer to each one. Currently does not apply
	 * to Program Counter.
	 */
	public static void addRegistersObserver(Observer observer) {
		for (int i = 0; i < regFile.length; i++) {
			regFile[i].addObserver(observer);
		}
	}

	/**
	 * Each individual register is a separate object and Observable. This handy
	 * method will delete the given Observer from each one. Currently does not
	 * apply to Program Counter.
	 */
	public static void deleteRegistersObserver(Observer observer) {
		for (int i = 0; i < regFile.length; i++) {
			regFile[i].deleteObserver(observer);
		}
	}
}
