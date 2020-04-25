package mars.mips.dump;

import mars.Globals;
import mars.mips.hardware.AddressErrorException;
import mars.mips.hardware.Memory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import mars.ProgramStatement;

/**
 * Intel's Hex memory initialization format
 *
 * @author Leo Alterman
 * @version July 2011
 */

public class AMifDumpFormat extends AbstractDumpFormat {

    /**
     * Constructor.  File extention is "hex".
     */
    public AMifDumpFormat() {
        super("MIF format", "MIF", "Memory Initialization Format File", "mif");
    }

    /**
     * Write MIPS memory contents according to the Memory Initialization File
     * (MIF) specification.
     *
     * @param file         File in which to store MIPS memory contents.
     * @param firstAddress first (lowest) memory address to dump.  In bytes but
     *                     must be on word boundary.
     * @param lastAddress  last (highest) memory address to dump.  In bytes but
     *                     must be on word boundary.  Will dump the word that starts at this address.
     * @throws AddressErrorException if firstAddress is invalid or not on a word boundary.
     * @throws IOException           if error occurs during file output.
     */
    public void dumpMemoryRange(File file, int firstAddress, int lastAddress)
            throws AddressErrorException, IOException {
        
        String[] fileNames ={file+"_text.mif", file+"_data.mif"};//, file+"ktext.mif", file+"kdata.mif"};
        int[] widths = {32,64};
        //int[] sizes={16384,32768,2048,1024}; // Pré-defined DE2-70 Size of memory blocks in Words
        int[] sizes={16384,16384,2048,1024}; // Adjusted for AARS, since data width is 64bits, depth is halved
        int[] addrs={
            Memory.textBaseAddress, 
            Memory.dataBaseAddress,
            //Memory.kernelTextBaseAddress,
            //Memory.kernelDataBaseAddress
        };
        
    for (int tipo=0; tipo < fileNames.length; tipo++){
        
        PrintStream out = new PrintStream(new FileOutputStream(fileNames[tipo]));
        String string;
        try {
            string = "DEPTH = " + Integer.toString(sizes[tipo]) + ";";
            out.println(string);
            string = "WIDTH = " + Integer.toString(widths[tipo]) + ";";
            out.println(string);
            out.println("ADDRESS_RADIX = HEX;");
            out.println("DATA_RADIX = HEX;");
            out.println("CONTENT");
            out.println("BEGIN");
            for (int address = addrs[tipo],waddr=0; address <= addrs[tipo]+sizes[tipo]*Memory.WORD_LENGTH_BYTES; address += Memory.WORD_LENGTH_BYTES,waddr++) {
                Integer temp = Globals.memory.getRawWordOrNull(address);
                if (temp == null)
                    break;

                // Gambiarra warning - _data.mif deve extender o bit mais significativo
                if(tipo == 0)
                {
                	String addr = Integer.toHexString(waddr);
                    while (addr.length() < widths[tipo]/4) {
                        addr = '0' + addr;
                    }
                    
                    String data = Integer.toHexString(temp);
                    while (data.length() < widths[tipo]/4) {
                        data = '0' + data;
                    }
                    
                    string = addr + " : " + data + ";";
                    if (tipo==0 || tipo==2) {
                        ProgramStatement ps = Globals.memory.getStatement(address);
                        string += "   % " + ps.getSourceLine() + ": " + ps.getSource()+" %";
                    }
                }
                else if(tipo == 1) {
                	String addr = Integer.toHexString(waddr);
                    while (addr.length() < widths[tipo]/4) {
                        addr = '0' + addr;
                    }
                    
                    String data = Integer.toHexString(temp);
                    if(temp < 0) {
                        while (data.length() < widths[tipo]/4) {
                            data = 'f' + data;
                        }
                    }
                    else {
                        while (data.length() < widths[tipo]/4) {
                            data = '0' + data;
                        }
                    } // como e complemento de 1 os bytes de espacamento devem ser compostos de F 

                    string = addr + " : " + data + ";";
                    if (tipo==0 || tipo==2) {
                        ProgramStatement ps = Globals.memory.getStatement(address);
                        string += "   % " + ps.getSourceLine() + ": " + ps.getSource()+" %";
                    }
                }
                out.println(string);
            }
            out.println("END;");
        } finally {
            out.close();
        }

    }
    }
}
