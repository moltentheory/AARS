   package mars.venus;
   import mars.*;
   import mars.util.*;
   import mars.simulator.*;
   import mars.mips.hardware.*;
   import javax.swing.*;
   import java.awt.*;
   import java.awt.event.*;
   import java.util.*;
   import javax.swing.table.*;
   import javax.swing.event.*;

/*
Copyright (c) 2003-2009,  Pete Sanderson and Kenneth Vollmar

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
    *  Sets up a window to display registers in the UI.
	 *   @author Sanderson, Bumgarner
	 **/
    
    public class RegistersWindow extends JPanel implements Observer { 
      private static JTable table;
      private static Register [] registers;
      private Object[][] tableData;
      private boolean highlighting;
      private int highlightRow;
      private ExecutePane executePane;
      private static final int NAME_COLUMN = 0;
      private static final int NUMBER_COLUMN = 1;
      private static final int VALUE_COLUMN = 2;
      private static Settings settings;
   /**
     *  Constructor which sets up a fresh window with a table that contains the register values.
     **/
   
       public RegistersWindow(){
         Simulator.getInstance().addObserver(this);
			settings = Globals.getSettings();
         this.highlighting = false;
         table = new MyTippedJTable(new RegTableModel(setupWindow()));
         table.getColumnModel().getColumn(NAME_COLUMN).setPreferredWidth(25);
         table.getColumnModel().getColumn(NUMBER_COLUMN).setPreferredWidth(25);
         table.getColumnModel().getColumn(VALUE_COLUMN).setPreferredWidth(60);
      	// Display register values (String-ified) right-justified in mono font
         table.getColumnModel().getColumn(NAME_COLUMN).setCellRenderer(new RegisterCellRenderer(MonoRightCellRenderer.MONOSPACED_PLAIN_12POINT, SwingConstants.LEFT));
         table.getColumnModel().getColumn(NUMBER_COLUMN).setCellRenderer(new RegisterCellRenderer(MonoRightCellRenderer.MONOSPACED_PLAIN_12POINT, SwingConstants.RIGHT));
         table.getColumnModel().getColumn(VALUE_COLUMN).setCellRenderer(new RegisterCellRenderer(MonoRightCellRenderer.MONOSPACED_PLAIN_12POINT, SwingConstants.RIGHT));
         table.setPreferredScrollableViewportSize(new Dimension(200,700));
         this.setLayout(new BorderLayout()); // table display will occupy entire width if widened
         this.add(new JScrollPane(table, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER));
      }
    
    /**
      *  Sets up the data for the window.
   	*   @return The array object with the data for the window.
   	**/  
   	
       public Object[][] setupWindow(){
         int valueBase = NumberDisplayBaseChooser.getBase(settings.getDisplayValuesInHex());
         tableData = new Object[66][3]; //was hardcoded to 34 -> 66 ; added 32 registers
         registers = RegisterFile.getRegisters();
         for(int i=0; i< registers.length; i++){
            tableData[i][0]= registers[i].getName();
            tableData[i][1]= new Integer(registers[i].getNumber());
            if(i < 32) {
            	tableData[i][2]= 
            			NumberDisplayBaseChooser.formatNumber(registers[i+32].getValue(),valueBase)
            			+ NumberDisplayBaseChooser.formatNumber(registers[i].getValue(),valueBase).substring(2);
            } else {
            	tableData[i][2]= NumberDisplayBaseChooser.formatNumber(registers[i].getValue(),valueBase);
            }
         }
         tableData[64][0]= "pc";
         tableData[64][1]= "";//new Integer(32);
         tableData[64][2]= NumberDisplayBaseChooser.formatUnsignedInteger(RegisterFile.getProgramCounter(),valueBase);
         
         tableData[65][0]= "flags";
         tableData[65][1]= "NZVC";//new Integer(34);
         tableData[65][2]= "0000";
         
         return tableData;
      }
      /**
   	 *  clear and redisplay registers
   	 */
       public void clearWindow() {
         this.clearHighlighting();
         RegisterFile.resetRegisters();
         this.updateRegisters(Globals.getGui().getMainPane().getExecutePane().getValueDisplayBase());
      }
      
   	/**
   	 * Clear highlight background color from any cell currently highlighted.
   	 */
       public void clearHighlighting() {
         highlighting=false;
         if (table != null) {
            table.tableChanged(new TableModelEvent(table.getModel()));
         }
			highlightRow = -1; // assure highlight will not occur upon re-assemble.
      }
   	 
   	 /**
   	  * Refresh the table, triggering re-rendering.
   	  */
       public void refresh() {
         if (table != null) {
            table.tableChanged(new TableModelEvent(table.getModel()));
         }
      }
      
   	/**
   	 * update register display using current number base (10 or 16)
   	 */
       public void updateRegisters() {
         updateRegisters(Globals.getGui().getMainPane().getExecutePane().getValueDisplayBase());
      }
   	
   	/**
   	 * update register display using specified number base (10 or 16)
   	 *
   	 * @param base desired number base
   	 */   	
       public void updateRegisters(int base) {
         registers = RegisterFile.getRegisters();
         for(int i=0; i< registers.length; i++){
        	 if(i < 32) {
        		 updateLongRegisterValue(registers[i].getNumber(), registers[i+32].getValue(), registers[i].getValue(), base);
        	 } else {
        		 updateRegisterValue(registers[i].getNumber(), registers[i].getValue(), base);
        	 }
         }
         //Removed use of custom PC register update function for uniformity
         //updateRegisterUnsignedValue(32, RegisterFile.getProgramCounter(), base);
         // Set PC string. No function since it's unique.
         ((RegTableModel)table.getModel()).setDisplayAndModelValueAt(NumberDisplayBaseChooser.formatUnsignedInteger(RegisterFile.getProgramCounter(),base), 64, 2);
         // Set flag string. No function since it's unique.
         ((RegTableModel)table.getModel()).setDisplayAndModelValueAt(RegisterFile.flagString(), 65, 2);
      }
       
	    //int hi = (int)(val >>> 32);
	    //int lo = (int)(val << 32 >>> 32);
       
     /**
       *  This method handles the updating of the GUI.  
   	 *   @param number The number of the register to update.
   	 *   @param val New value.
   	 **/
   	 
       public void updateRegisterValue(int number,int val, int base){
    	   if(number > 32)
    		   ((RegTableModel)table.getModel()).setDisplayAndModelValueAt(NumberDisplayBaseChooser.formatNumber(val,base), number-1, 2);
    	   else
    		   ((RegTableModel)table.getModel()).setDisplayAndModelValueAt(NumberDisplayBaseChooser.formatNumber(val,base), number, 2);
       }
       public void updateLongRegisterValue(int number,int val1, int val2, int base){
           ((RegTableModel)table.getModel()).setDisplayAndModelValueAt(
        		   (
        				   NumberDisplayBaseChooser.formatNumber(val1,base)+
        				   NumberDisplayBaseChooser.formatNumber(val2,base).substring(2)
        		   ), 
        		   number,
        		   2
        	);
       }
     
       /*
        * removed unecessary method for updating pc since its a single use and theres no need for encapsulation in this case
       private void updateRegisterUnsignedValue(int number,int val, int base){
    	   ((RegTableModel)table.getModel()).setDisplayAndModelValueAt(NumberDisplayBaseChooser.formatUnsignedInteger(val,base), number, 2);   
       }
       */
   	
    	/** Required by Observer interface.  Called when notified by an Observable that we are registered with.
   	 * Observables include:
   	 *   The Simulator object, which lets us know when it starts and stops running
   	 *   A register object, which lets us know of register operations
   	 * The Simulator keeps us informed of when simulated MIPS execution is active.
   	 * This is the only time we care about register operations.
   	 * @param observable The Observable object who is notifying us
   	 * @param obj Auxiliary object with additional information.
   	 */
       public void update(Observable observable, Object obj) {
         if (observable == mars.simulator.Simulator.getInstance()) {
            SimulatorNotice notice = (SimulatorNotice) obj;
            if (notice.getAction()==SimulatorNotice.SIMULATOR_START) {
               // Simulated MIPS execution starts.  Respond to memory changes if running in timed
            	// or stepped mode.
               if (notice.getRunSpeed() != RunSpeedPanel.UNLIMITED_SPEED || notice.getMaxSteps()==1) {
                  RegisterFile.addRegistersObserver(this);
                  this.highlighting = true;
               }
            } 
            else {
               // Simulated MIPS execution stops.  Stop responding.
               RegisterFile.deleteRegistersObserver(this);            
            }
         } 
         else if (obj instanceof RegisterAccessNotice) { 
         	// NOTE: each register is a separate Observable
            RegisterAccessNotice access = (RegisterAccessNotice) obj;
            if (access.getAccessType()==AccessNotice.WRITE) {
            	// Uses the same highlighting technique as for Text Segment -- see
            	// AddressCellRenderer class in DataSegmentWindow.java.
               this.highlighting = true;
               this.highlightCellForRegister((Register)observable);
               Globals.getGui().getRegistersPane().setSelectedComponent(this);
            }
         }
      }
   	
     /**
      *  Highlight the row corresponding to the given register.  
   	*  @param register Register object corresponding to row to be selected.
   	*/
       void highlightCellForRegister(Register register) {
         this.highlightRow = register.getNumber();
         // Tell the system that table contents have changed.  This will trigger re-rendering 
      	// during which cell renderers are obtained.  The row of interest (identified by 
      	// instance variabls this.registerRow) will get a renderer
      	// with highlight background color and all others get renderer with default background. 
         table.tableChanged(new TableModelEvent(table.getModel()));
      }
   	
   /*
   * Cell renderer for displaying register entries.  This does highlighting, so if you
   * don't want highlighting for a given column, don't use this.  Currently we highlight 
   * all columns.
   */
       private class RegisterCellRenderer extends DefaultTableCellRenderer { 
         private Font font;
         private int alignment;
      	 
          public RegisterCellRenderer(Font font, int alignment) {
            super();
            this.font = font;
            this.alignment = alignment;
         }
      	
          public Component getTableCellRendererComponent(JTable table, Object value, 
                            boolean isSelected, boolean hasFocus, int row, int column) {									 
            JLabel cell = (JLabel) super.getTableCellRendererComponent(table, value, 
                                    isSelected, hasFocus, row, column);
            cell.setFont(font);
            cell.setHorizontalAlignment(alignment);
            if (settings.getRegistersHighlighting() && highlighting && row==highlightRow) {
               cell.setBackground( settings.getColorSettingByPosition(Settings.REGISTER_HIGHLIGHT_BACKGROUND) );
               cell.setForeground( settings.getColorSettingByPosition(Settings.REGISTER_HIGHLIGHT_FOREGROUND) );
					cell.setFont( settings.getFontByPosition(Settings.REGISTER_HIGHLIGHT_FONT) );
            } 
            else if (row%2==0) {
               cell.setBackground( settings.getColorSettingByPosition(Settings.EVEN_ROW_BACKGROUND) );
               cell.setForeground( settings.getColorSettingByPosition(Settings.EVEN_ROW_FOREGROUND) );
					cell.setFont( settings.getFontByPosition(Settings.EVEN_ROW_FONT) );
            } 
            else {
               cell.setBackground( settings.getColorSettingByPosition(Settings.ODD_ROW_BACKGROUND) );
               cell.setForeground( settings.getColorSettingByPosition(Settings.ODD_ROW_FOREGROUND) );				
					cell.setFont( settings.getFontByPosition(Settings.ODD_ROW_FONT) );
            }
            return cell;
         }  
      }
   
   
   	////////////////////////////////////////////////////////////////////////////
   	
       class RegTableModel extends AbstractTableModel {
         final String[] columnNames =  {"Name", "Number", "Value"};
         Object[][] data;
      	
          public RegTableModel(Object[][] d){
            data=d;
         }
      
          public int getColumnCount() {
            return columnNames.length;
         }
        
          public int getRowCount() {
            return data.length;
         }
      
          public String getColumnName(int col) {
            return columnNames[col];
         }
      
          public Object getValueAt(int row, int col) {
            return data[row][col];
         }
      
        /*
         * JTable uses this method to determine the default renderer/
         * editor for each cell.  
      	*/
          public Class getColumnClass(int c) {
            return getValueAt(0, c).getClass();
         }
      
              /*
         * Don't need to implement this method unless your table's
         * editable.  
         */
          public boolean isCellEditable(int row, int col) {
            //Note that the data/cell address is constant,
            //no matter where the cell appears onscreen.
         	// these registers are not editable: $zero (0), $pc (32), $ra (31)
            if (col == VALUE_COLUMN && row != 0 && row != 32 && row != 31) { 
               return true;
            } 
            else {
               return false;
            }
         }
      
      
        /*
         * Update cell contents in table model.  This method should be called
      	* only when user edits cell, so input validation has to be done.  If
      	* value is valid, MIPS register is updated.
         */
          public void setValueAt(Object value, int row, int col) {
            int val=0;
            try {
               val = Binary.stringToInt((String) value);
            }
                catch (NumberFormatException nfe) {
                  data[row][col] = "INVALID";
                  fireTableCellUpdated(row, col);
                  return;
               }
         	//  Assures that if changed during MIPS program execution, the update will
         	//  occur only between MIPS instructions.
            synchronized (Globals.memoryAndRegistersLock) {
               RegisterFile.updateRegister(row, val);
            }
            int valueBase = Globals.getGui().getMainPane().getExecutePane().getValueDisplayBase();
            data[row][col] = NumberDisplayBaseChooser.formatNumber(val, valueBase); 
            fireTableCellUpdated(row, col);
            return;
         }
      
      
        /**
         * Update cell contents in table model.  Does not affect MIPS register.
         */
          private void setDisplayAndModelValueAt(Object value, int row, int col) {
            data[row][col] = value;
            fireTableCellUpdated(row, col);
         }
      
      
         // handy for debugging....
          private void printDebugData() {
            int numRows = getRowCount();
            int numCols = getColumnCount();
         
            for (int i=0; i < numRows; i++) {
               System.out.print("    row " + i + ":");
               for (int j=0; j < numCols; j++) {
                  System.out.print("  " + data[i][j]);
               }
               System.out.println();
            }
            System.out.println("--------------------------");
         }
      }  
   	
       ///////////////////////////////////////////////////////////////////
   	 //
   	 // JTable subclass to provide custom tool tips for each of the
   	 // register table column headers and for each register name in 
   	 // the first column. From Sun's JTable tutorial.
   	 // http://java.sun.com/docs/books/tutorial/uiswing/components/table.html
   	 //
       private class MyTippedJTable extends JTable {
          MyTippedJTable(RegTableModel m) {
            super(m);
            this.setRowSelectionAllowed(true); // highlights background color of entire row
            this.setSelectionBackground(Color.GREEN);
         }

          private String[] regToolTips = {
                  /* X0    */  "Arguments/Results",  
                  /* X1    */  "Arguments/Results",
                  /* X2    */  "Arguments/Results",
                  /* X3    */  "Arguments/Results",
                  /* X4    */  "Arguments/Results",
                  /* X5    */  "Arguments/Results",
                  /* X6    */  "Arguments/Results",
                  /* X7    */  "Arguments/Results",
                  /* X8    */  "Indirect location result register",
                  /* X9    */  "temporary (not preserved across call)",
                  /* X10   */  "temporary (not preserved across call)",
                  /* X11   */  "temporary (not preserved across call)",
                  /* X12   */  "temporary (not preserved across call)",
                  /* X13   */  "temporary (not preserved across call)",
                  /* X14   */  "temporary (not preserved across call)",
                  /* X15   */  "temporary (not preserved across call)",
                  /* X16   */  "May be used as a scratch register by the linker; Other times used as a temporary register",
                  /* X17   */  "May be used as a scratch register by the linker; Other times used as a temporary register",
                  /* X18   */  "Platform register for platform dependant code; otherwise a temporary register",
                  /* X19   */  "saved temporary (preserved across call)",
                  /* X20   */  "saved temporary (preserved across call)",
                  /* X21   */  "saved temporary (preserved across call)",
                  /* X22   */  "saved temporary (preserved across call)",
                  /* X23   */  "saved temporary (preserved across call)",
                  /* X24   */  "saved temporary (preserved across call)",
                  /* X25   */  "saved temporary (preserved across call)",
                  /* X26   */  "saved temporary (preserved across call)",
                  /* X27   */  "saved temporary (preserved across call)",
                  /* X28   */  "stack pointer",
                  /* X29   */  "frame pointer",
                  /* X30   */  "return address (used by function call)",
                  /* XZR   */  "The Constant Value Zero", 
                  /* W0    */  "32 most significant bits of register X0",
                  /* W1    */  "32 most significant bits of register X1",
                  /* W2    */  "32 most significant bits of register X2",
                  /* W3    */  "32 most significant bits of register X3",
                  /* W4    */  "32 most significant bits of register X4",
                  /* W5    */  "32 most significant bits of register X5",
                  /* W6    */  "32 most significant bits of register X6",
                  /* W7    */  "32 most significant bits of register X7",
                  /* W8    */  "32 most significant bits of register X8",
                  /* W9    */  "32 most significant bits of register X9",
                  /* W10   */  "32 most significant bits of register X10",
                  /* W11   */  "32 most significant bits of register X11",
                  /* W12   */  "32 most significant bits of register X12",
                  /* W13   */  "32 most significant bits of register X13",
                  /* W14   */  "32 most significant bits of register X14",
                  /* W15   */  "32 most significant bits of register X15",
                  /* W16   */  "32 most significant bits of register X16",
                  /* W17   */  "32 most significant bits of register X17",
                  /* W18   */  "32 most significant bits of register X18",
                  /* W19   */  "32 most significant bits of register X19",
                  /* W20   */  "32 most significant bits of register X20",
                  /* W21   */  "32 most significant bits of register X21",
                  /* W22   */  "32 most significant bits of register X22",
                  /* W23   */  "32 most significant bits of register X23",
                  /* W24   */  "32 most significant bits of register X24",
                  /* W25   */  "32 most significant bits of register X25",
                  /* W26   */  "32 most significant bits of register X26",
                  /* W27   */  "32 most significant bits of register X27",
                  /* W28   */  "32 most significant bits of register X28",
                  /* W29   */  "32 most significant bits of register X29",
                  /* W30   */  "32 most significant bits of register X30",
                  /* W31   */  "32 most significant bits of register XZR",
             /* pc    */  "program counter",
             /* rgf   */  "processor flags",
             /* hi    */  "high-order word of multiply product, or divide remainder",
             /* lo    */  "low-order word of multiply product, or divide quotient"

             };
      
//          Preserved for comparison TODO: delete
//         private String[] regToolTips = {
//            /* $zero */  "constant 0",  
//            /* $at   */  "reserved for assembler",
//            /* $v0   */  "expression evaluation and results of a function",
//            /* $v1   */  "expression evaluation and results of a function",
//            /* $a0   */  "argument 1",
//            /* $a1   */  "argument 2",
//            /* $a2   */  "argument 3",
//            /* $a3   */  "argument 4",
//            /* $t0   */  "temporary (not preserved across call)",
//            /* $t1   */  "temporary (not preserved across call)",
//            /* $t2   */  "temporary (not preserved across call)",
//            /* $t3   */  "temporary (not preserved across call)",
//            /* $t4   */  "temporary (not preserved across call)",
//            /* $t5   */  "temporary (not preserved across call)",
//            /* $t6   */  "temporary (not preserved across call)",
//            /* $t7   */  "temporary (not preserved across call)",
//            /* $s0   */  "saved temporary (preserved across call)",
//            /* $s1   */  "saved temporary (preserved across call)",
//            /* $s2   */  "saved temporary (preserved across call)",
//            /* $s3   */  "saved temporary (preserved across call)",
//            /* $s4   */  "saved temporary (preserved across call)",
//            /* $s5   */  "saved temporary (preserved across call)",
//            /* $s6   */  "saved temporary (preserved across call)",
//            /* $s7   */  "saved temporary (preserved across call)",
//            /* $t8   */  "temporary (not preserved across call)",
//            /* $t9   */  "temporary (not preserved across call)",
//            /* $k0   */  "reserved for OS kernel",
//            /* $k1   */  "reserved for OS kernel",
//            /* $gp   */  "pointer to global area",
//            /* $sp   */  "stack pointer",
//            /* $fp   */  "frame pointer",
//            /* $ra   */  "return address (used by function call)",
//            /* pc    */  "program counter",
//            /* hi    */  "high-order word of multiply product, or divide remainder",
//            /* lo    */  "low-order word of multiply product, or divide quotient"		  
//            };
      	
          //Implement table cell tool tips.
          public String getToolTipText(MouseEvent e) {
            String tip = null;
            java.awt.Point p = e.getPoint();
            int rowIndex = rowAtPoint(p);
            int colIndex = columnAtPoint(p);
            int realColumnIndex = convertColumnIndexToModel(colIndex);
            if (realColumnIndex == NAME_COLUMN) { //Register name column
               tip = regToolTips[rowIndex];
            /* You can customize each tip to encorporiate cell contents if you like:
               TableModel model = getModel();
               String regName = (String)model.getValueAt(rowIndex,0);
            	....... etc .......
            */
            } 
            else { 
                    //You can omit this part if you know you don't have any 
                    //renderers that supply their own tool tips.
               tip = super.getToolTipText(e);
            }
            return tip;
         }
        
         private String[] columnToolTips = {
            /* name */   "Each register has a tool tip describing its usage convention",
            /* number */ "Corresponding register number",
            /* value */  "Current 32 bit value"
            };
      		
          //Implement table header tool tips. 
          protected JTableHeader createDefaultTableHeader() {
            return 
                new JTableHeader(columnModel) {
                   public String getToolTipText(MouseEvent e) {
                     String tip = null;
                     java.awt.Point p = e.getPoint();
                     int index = columnModel.getColumnIndexAtX(p.x);
                     int realIndex = columnModel.getColumn(index).getModelIndex();
                     return columnToolTips[realIndex];
                  }
               };
         }
      }
   
   }