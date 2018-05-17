import java.io.*;

import java.util.*;

class address{
	
 static int base_address;
}



class Registersfile {
    public boolean notvalid;
    public String regname;
    public int value;
	boolean forwardvalue;
    
    public Registersfile(String rn,int v, boolean vl, boolean v2) {
        value = v;
        regname=rn;
        notvalid = vl;
		forwardvalue= v2;
        
    }
    
}

class PhysicalRegistersfile {
    public boolean notvalid;
    public String regname;
    public int value;
	boolean forwardvalue;
	public boolean allocated;
    
    public PhysicalRegistersfile(String rn,int v, boolean vl,boolean f,boolean a) {
        value = v;
        regname=rn;
        notvalid = vl;
		forwardvalue = f;
        allocated = a;
    }
    
}


class Memoryfile {
    
    public String memname;
    public long value;
    
    public Memoryfile(String mn,long va) {
        memname=mn;
        value = va;
    }

    
}

class RenameTable {
	
	public String arcregister;
	public String phyregist;
	public boolean commit;
	
	
	public void display() {
		System.out.println(arcregister+" : "+phyregist);
	}
	
	
	
}

class Instructioninfo {
    public String operation;
    public String source1;
    public String source2;
    public String dest;
    public String current_inst;
    public int instruction_number;
    public int literal;
	boolean isforward;
	public String acurrent_inst;
	public int index;
    

    public int sourcevalue1;
    public int sourcevalue2;
    public String stage;
    public boolean stalled;
    public boolean takenvalue;
    public int result;
	public String destPhy;
	public String source1Phy;
	public String source2Phy;
	public int destindexiniq;
    public boolean zeroflag;
    public boolean carryflag;
    public boolean negitiveflag;
	public IssueQueue iq;
	public boolean entryValid;
	
	boolean s1flag;
	boolean s2flag;
	boolean dflag;
    
	public String acurrent() {
		int i4=(this.instruction_number/4000)-1;
		if(operation.equals("MUL")||operation.equals("DIV")||operation.equals("ADD")||operation.equals("SUB")||operation.equals("OR")||operation.equals("AND")||operation.equals("EXOR")) {
		this.acurrent_inst = "(I"+Integer.toString(index) +")" + operation+" " + destPhy +" "+source1Phy+" " +source2Phy ;
		return acurrent_inst;
		}
		else if(operation.equals("MOVC")){
			this.acurrent_inst = "(I"+Integer.toString(index) +")" + operation+"," + destPhy +",#"+Integer.toString(literal);
			return acurrent_inst;
		}else{
		return current_inst;
		}
	}

}

class reorderbuffer {
	public Instructioninfo instruction;
	public String phyreg;
	public boolean status;
	public int branchaddress;
}


class IssueQueue {

	public boolean entryValid;
	public int IQEntryIndex;
	public int src1Value;
	public boolean src1Valid;
	public int src2Value;
	public boolean src2Valid;
	public String dest;
	public Instructioninfo instruction;
	public String currentInstruction;
}



class Displayinfo {
	public int cycles;
	public String fetch_stage;
	public String decode_stage;
	public ArrayList<String> rntable = new ArrayList();
	public ArrayList<String> isqtable = new ArrayList();
	public ArrayList<String> robtable = new ArrayList();
	public String execution1_stage;
	public String executionmul1_stage;
	public String executionmul2_stage;
	public String executiondiv1_stage;
	public String executiondiv2_stage;
	public String executiondiv3_stage;
	public String executiondiv4_stage;
	public String memory_stage;
	public String writeback_stage;
	
}








public class inorder {
	    private Map<String, Instructioninfo> current_stageinstruction= new HashMap<String, Instructioninfo>();
		public Map<Integer,IssueQueue> current_issues= new HashMap<Integer,IssueQueue>();
	    public int pc;
		public int cycle_counter;
		public boolean hbreak;
		public int zero_flag;
		public boolean ZF;
		public int pc_value;
		String ln;
		int instnumber;
		ArrayList<Instructioninfo> instruction;
		ArrayList<Registersfile> reg = new ArrayList<>();
		ArrayList<PhysicalRegistersfile> phyreg = new ArrayList<>();
	    ArrayList<Memoryfile> mem = new ArrayList<>();
		address baddress = new address();
		ArrayList<Displayinfo> displaylist = new ArrayList<>();
		ArrayList<RenameTable> renametable = new ArrayList<>();
		
	    ArrayList<reorderbuffer> rob = new ArrayList<>(32);
		
		
		public void init () {
			
			current_issues.put(1,null);
			current_issues.put(2,null);
			current_issues.put(3,null);
			current_issues.put(4,null);
			current_issues.put(5,null);
			current_issues.put(6,null);
			current_issues.put(7,null);
			current_issues.put(8,null);
			current_issues.put(9,null);
			current_issues.put(10,null);
			current_issues.put(11,null);
			current_issues.put(12,null);
			current_issues.put(13,null);
			current_issues.put(14,null);
			current_issues.put(15,null);
			current_issues.put(16,null);
			
			current_stageinstruction.put("fetchstage", null);
			current_stageinstruction.put("decodestage", null);
			current_stageinstruction.put("exe1stage", null);
			current_stageinstruction.put("exemulstage1", null);
			current_stageinstruction.put("exemulstage2", null);
			current_stageinstruction.put("exedivstage1", null);
			current_stageinstruction.put("exedivstage2", null);
			current_stageinstruction.put("exedivstage3", null);
			current_stageinstruction.put("exedivstage4", null);
			current_stageinstruction.put("writebackstage", null);
			address.base_address = 4000;
			zero_flag= -1;
			ZF=false;
			pc=address.base_address;
			cycle_counter=1;
			for(int i=0;i<=15;i++) {
				reg.add(new Registersfile("R"+i , 0 , false, false));
			}
			
			for(int i=0;i<=31;i++) {
				phyreg.add(new PhysicalRegistersfile("P"+i , 0 , false, false, false));
			}
			
			for(int j=0;j<=999;j++) {
				mem.add(new Memoryfile("M"+j , 0 ));
			}
			
		}
		
		
		public void dispalystages()
		{
			Displayinfo display = new Displayinfo();
			Instructioninfo instfetchdisplay = current_stageinstruction.get("fetchstage");
			Instructioninfo instdecodedisplay = current_stageinstruction.get("decodestage");
			Instructioninfo instexe1display = current_stageinstruction.get("exe1stage");
			Instructioninfo instexemul1display = current_stageinstruction.get("exemulstage1");
			Instructioninfo instexemul2display = current_stageinstruction.get("exemulstage2");
			Instructioninfo instexediv1display = current_stageinstruction.get("exedivstage1");
			Instructioninfo instexediv2display = current_stageinstruction.get("exedivstage2");
			Instructioninfo instexediv3display = current_stageinstruction.get("exedivstage3");
			Instructioninfo instexediv4display = current_stageinstruction.get("exedivstage4");
			Instructioninfo instmemorydisplay = current_stageinstruction.get("memorystage");
			Instructioninfo instwritebackdisplay = current_stageinstruction.get("writebackstage");
			
			if(instfetchdisplay != null) {
			String stall = (instfetchdisplay.stalled== true)?" (stalled)":"";
			display.fetch_stage = instfetchdisplay.current_inst+stall;
		} 	else if(instfetchdisplay == null) {
			display.fetch_stage = "Empty";
		}

			if(instdecodedisplay != null) {
			String stall = (instdecodedisplay.stalled== true)?" (stalled)":"";
			display.decode_stage = instdecodedisplay.current_inst+stall;
		} 	else if(instdecodedisplay == null) {
			display.decode_stage = "Empty";
		}
			if(instexe1display != null) {
			String stall = (instexe1display.stalled== true)?" (stalled)":"";
			display.execution1_stage = instexe1display.acurrent();
		} 	else if(instexe1display == null) {
			display.execution1_stage = "Empty";
		}
		
			if(instexemul1display != null) {
			String stall = (instexemul1display.stalled== true)?" (stalled)":"";
			display.executionmul1_stage = instexemul1display.acurrent();
		} 	else if(instexemul1display == null) {
			display.executionmul1_stage = "Empty";
		}
		
			if(instexemul2display != null) {
			String stall = (instexemul2display.stalled== true)?" (stalled)":"";
			display.executionmul2_stage = instexemul2display.acurrent();
		} 	else if(instexemul2display == null) {
			display.executionmul2_stage = "Empty";
		}
		
			if(instexediv1display != null) {
			String stall = (instexediv1display.stalled== true)?" (stalled)":"";
			display.executiondiv1_stage = instexediv1display.acurrent();
		} 	else if(instexediv1display == null) {
			display.executiondiv1_stage = "Empty";
		}
		
			if(instexediv2display != null) {
			String stall = (instexediv2display.stalled== true)?" (stalled)":"";
			display.executiondiv2_stage = instexediv2display.acurrent();
		} 	else if(instexediv2display == null) {
			display.executiondiv2_stage = "Empty";
		}
		
			if(instexediv3display != null) {
			String stall = (instexediv3display.stalled== true)?" (stalled)":"";
			display.executiondiv3_stage = instexediv3display.acurrent();
		} 	else if(instexediv3display == null) {
			display.executiondiv3_stage = "Empty";
		}
		
			if(instexediv4display != null) {
			String stall = (instexediv4display.stalled== true)?" (stalled)":"";
			display.executiondiv4_stage = instexediv4display.acurrent();
		} 	else if(instexediv4display == null) {
			display.executiondiv4_stage = "Empty";
		}
		
		if(instmemorydisplay != null) {
			String stall = (instmemorydisplay.stalled== true)?" (stalled)":"";
			display.memory_stage = instmemorydisplay.acurrent();
		} 	else if(instmemorydisplay == null) {
			display.memory_stage = "Empty";
		}
		
		if(instwritebackdisplay != null) {
			String stall = (instwritebackdisplay.stalled== true)?" (stalled)":"";
			display.writeback_stage = instwritebackdisplay.acurrent();
		} 	else if(instwritebackdisplay == null) {
			display.writeback_stage = "Empty";
		}

			for(int i=1;i<17;i++) {
				if(current_issues.get(i)!=null){
					IssueQueue isq = current_issues.get(i);
					display.isqtable.add(isq.instruction.acurrent());
				}
			}
	    
		
		
			for(int i=0;i<rob.size();i++) {
				
					display.robtable.add(rob.get(i).instruction.acurrent());
			}	
				

			for(int i=0;i<renametable.size();i++) {
				display.rntable.add(renametable.get(i).arcregister+" : "+renametable.get(i).phyregist);
			}
		
		
		
		display.cycles = cycle_counter;
			displaylist.add(display);
			
		}
		
		
		
		
		public void loadwritebackstage() {
			Instructioninfo instwriteback;
			if(current_stageinstruction.get("writebackstage") != null) {
				instwriteback = current_stageinstruction.get("memorystage");
				current_stageinstruction.put("writebackstage", null);
				//instwriteback.stage = "completed";
			}

			if(current_stageinstruction.get("writebackstage") == null && current_stageinstruction.get("memorystage") != null) {
				instwriteback = current_stageinstruction.get("memorystage");
				instwriteback.stage = "writebackstage";
				current_stageinstruction.put("writebackstage", instwriteback);
				current_stageinstruction.put("memorystage", null);
				
				
			}
		}
		
		public void loadmemorystage() {
			if(current_stageinstruction.get("memorystage") == null) {
				Instructioninfo instmemory;
				
				if(current_stageinstruction.get("exemulstage2") != null && current_stageinstruction.get("exe1stage") != null && current_stageinstruction.get("exedivstage4") !=null ){
					rob.remove(0);
					instmemory = current_stageinstruction.get("exedivstage4");
					instmemory.stage = "memory";
					current_stageinstruction.put("memorystage", instmemory);
					current_stageinstruction.put("exedivstage4", null);
				} else if(current_stageinstruction.get("exemulstage2") == null && current_stageinstruction.get("exe1stage") != null && current_stageinstruction.get("exedivstage4") !=null ){
					rob.remove(0);
					instmemory = current_stageinstruction.get("exedivstage4");
					instmemory.stage = "memory";
					current_stageinstruction.put("memorystage", instmemory);
					current_stageinstruction.put("exedivstage4", null);
				} else if(current_stageinstruction.get("exemulstage2") == null && current_stageinstruction.get("exe1stage") == null && current_stageinstruction.get("exedivstage4") !=null ){
					rob.remove(0);
					instmemory = current_stageinstruction.get("exedivstage4");
					instmemory.stage = "memory";
					current_stageinstruction.put("memorystage", instmemory);
					current_stageinstruction.put("exedivstage4", null);
				} else if(current_stageinstruction.get("exemulstage2") != null && current_stageinstruction.get("exe1stage") == null && current_stageinstruction.get("exedivstage4") !=null ){
					rob.remove(0);
					instmemory = current_stageinstruction.get("exedivstage4");
					instmemory.stage = "memory";
					current_stageinstruction.put("memorystage", instmemory);
					current_stageinstruction.put("exedivstage4", null);
				} else if(current_stageinstruction.get("exemulstage2") != null && current_stageinstruction.get("exe1stage") != null && current_stageinstruction.get("exedivstage4") ==null ){
					rob.remove(0);
					instmemory = current_stageinstruction.get("exemulstage2");
					instmemory.stage = "memory";
					current_stageinstruction.put("memorystage", instmemory);
					current_stageinstruction.put("exemulstage2", null);
				} else if(current_stageinstruction.get("exemulstage2") == null && current_stageinstruction.get("exe1stage") != null && current_stageinstruction.get("exedivstage4") ==null ){
					rob.remove(0);
					instmemory = current_stageinstruction.get("exe1stage");
					instmemory.stage = "memory";
					current_stageinstruction.put("memorystage", instmemory);
					current_stageinstruction.put("exe1stage", null);
				} else if(current_stageinstruction.get("exemulstage2") != null && current_stageinstruction.get("exe1stage") == null && current_stageinstruction.get("exedivstage4") ==null ){
					rob.remove(0);
					instmemory = current_stageinstruction.get("exemulstage2");
					instmemory.stage = "memory";
					current_stageinstruction.put("memorystage", instmemory);
					current_stageinstruction.put("exemulstage2", null);
				}
			}
		}
		
		public void loadexedivstage4() {
			if(current_stageinstruction.get("exedivstage4")== null && current_stageinstruction.get("exedivstage3")!=null) {
				Instructioninfo instdiv4= current_stageinstruction.get("exedivstage3");
					instdiv4.stage= "exedivstage4";
					current_stageinstruction.put("exedivstage4", instdiv4);
					current_stageinstruction.put("exedivstage3", null);
			}
		}
		
		public void loadexedivstage3() {
			if(current_stageinstruction.get("exedivstage3")== null && current_stageinstruction.get("exedivstage2")!=null) {
				Instructioninfo instdiv3= current_stageinstruction.get("exedivstage2");
					instdiv3.stage= "exedivstage3";
					current_stageinstruction.put("exedivstage3", instdiv3);
					current_stageinstruction.put("exedivstage2", null);
			}
		}
		
		
		
		public void loadexedivstage2() {
			if(current_stageinstruction.get("exedivstage2")== null && current_stageinstruction.get("exedivstage1")!=null) {
				Instructioninfo instdiv2= current_stageinstruction.get("exedivstage1");
					instdiv2.stage= "exedivstage2";
					current_stageinstruction.put("exedivstage2", instdiv2);
					current_stageinstruction.put("exedivstage1", null);
			}
		}
		
		
		

		
		public void loadexedivstage1() {
			   for(int i =1; i<17;i++){
				IssueQueue isq=current_issues.get(i);
				if (isq!= null) {
					Instructioninfo instexe1 = isq.instruction;
					String s = instexe1.operation;
				if(s.equals("DIV") ) {
					if((instexe1.entryValid)){
					instexe1.stage= "exedivstage1";
					current_stageinstruction.put("exedivstage1", instexe1);
					current_issues.put(i, null);
					break;
					}
				}
				}
			}
		}
		
		public void loadexemulstage2() {
			if(current_stageinstruction.get("exemulstage2")== null && current_stageinstruction.get("exemulstage1")!=null) {
				Instructioninfo instmul2= current_stageinstruction.get("exemulstage1");
					instmul2.stage= "exemulstage2";
					current_stageinstruction.put("exemulstage2", instmul2);
					current_stageinstruction.put("exemulstage1", null);
			}
		}
		
		

		
		
		public void loadexemulstage1() {
			   for(int i =1; i<17;i++){
				IssueQueue isq=current_issues.get(i);
				if (isq!= null) {
					Instructioninfo instexe1 = isq.instruction;
					String s = instexe1.operation;
				if(s.equals("MUL") ) {
					if((instexe1.entryValid)){
					instexe1.stage= "exemulstage1";
					current_stageinstruction.put("exemulstage1", instexe1);
					current_issues.put(i, null);
					break;
					}
				}
				}
			}
		}
		
		
		
		public void loadexe1stage() {
			for(int i =1; i<17;i++){
				IssueQueue isq=current_issues.get(i);
				if (isq!= null) {
					Instructioninfo instexe1 = isq.instruction;
					String s = instexe1.operation;
				if(s.equals("ADD") ||s.equals("SUB")||s.equals("OR") ||s.equals("EXOR") || s.equals("LOAD") || s.equals("STORE")||s.equals("MOVC") ||s.equals("AND")|| s.equals("JUMP") || s.equals("HALT") || s.equals("BZ") || s.equals("BNZ") || s.equals("JUMP") || s.equals("JAL") ) {
					if((instexe1.entryValid)){
					instexe1.stage= "exe1stage";
					current_stageinstruction.put("exe1stage", instexe1);
					current_issues.put(i, null);
					break;
					}
				}
				}
			}
		}
		
		public void loadissuequeuestage() {
			//System.out.println("I am here");
			if(current_stageinstruction.get("decodestage")!= null) {
				Instructioninfo instissue = current_stageinstruction.get("decodestage");
				IssueQueue isq=new IssueQueue();
				isq.dest = instissue.destPhy;
				isq.instruction = instissue;
				isq.currentInstruction = instissue.current_inst;
				for(int i =1; i<17;i++)
				{
					if(current_issues.get(i)==null) {
						current_issues.put(i,isq);
						break;
					}
				}
				current_stageinstruction.put("decodestage", null);
				reorderbuffer rb1= new reorderbuffer();
				rb1.instruction = instissue;
				rb1.phyreg = instissue.destPhy;
				rb1.status = false;
				rob.add(rb1);
				
				
				
			}
			
		}
		
		public void loaddecodestage() {
			if(current_stageinstruction.get("decodestage")==null && current_stageinstruction.get("fetchstage") != null ) {
				Instructioninfo instdecode = current_stageinstruction.get("fetchstage");
				instdecode.stage = "decodestage";
				current_stageinstruction.put("decodestage", instdecode);
				current_stageinstruction.put("fetchstage", null);
			}
		}
		
		
		
		
		public void loadfetchstage () {
			if(current_stageinstruction.get("fetchstage")==null) {
				int lastinst;
				lastinst=instruction.size();
				if(this.pc < (4000+(4*lastinst)))
				{
					Instructioninfo instfetch = instruction.get((this.pc-4000)/4);
					instfetch.stage="fetchstage";
					current_stageinstruction.put("fetchstage",instfetch);
					this.pc = this.pc + 4;
				}
			}
			
		}
		
		public Registersfile getregister(String s) {
			int i;
			for(i=0; i< reg.size(); i++) {
				if(reg.get(i).regname.equals(s)) {
					return reg.get(i);
				}
			}
			return null;
		}
		
		public PhysicalRegistersfile getphyregister(String s) {
			int i;
			for(i=0; i< phyreg.size(); i++) {
				if(phyreg.get(i).regname.equals(s)) {
					return phyreg.get(i);
				}
			}
			return null;
		}

		public Memoryfile getmemory(String s) {
			int i;
			for(i=0; i< mem.size(); i++) {
				if(mem.get(i).memname.equals(s)) {
					return mem.get(i);
				}
			}
			return null;
		}
		
		public String allocate() {
		 
		 for(int i = 0; i < 32; i++)
		 {
			 if(!(phyreg.get(i).allocated))
			 {
				 phyreg.get(i).allocated = true;
				return phyreg.get(i).regname;
				
			 }
		 }
		 return null;
		}
		
		
		
		
		
		public void pipeline(int n) {
			do {
				
				loadwritebackstage();
				loadmemorystage();
				loadexedivstage4();
				loadexedivstage3();
				loadexedivstage2();
				loadexedivstage1();
				loadexemulstage2();
				loadexemulstage1();
				loadexe1stage();
				loadissuequeuestage();
				loaddecodestage();
				loadfetchstage();

				dispalystages();
				
				
				if (current_stageinstruction.get("writebackstage") != null) {
					
					Instructioninfo instwb = current_stageinstruction.get("writebackstage");
					String destination = instwb.dest;
					Registersfile regist = getregister(destination);
					
					if(regist != null) {
						regist.notvalid = false;
						regist.forwardvalue = false;
					}
					
					if(instwb.takenvalue) {
						regist.value = instwb.result;
					}
					
					if(instwb.operation.equals("HALT")) {
						hbreak = true;
						current_stageinstruction.put("fetchstage", null);
						current_stageinstruction.put("decodestage", null);
						current_stageinstruction.put("exe1stage", null);
						current_stageinstruction.put("exemulstage1", null);
						current_stageinstruction.put("exemulstage2", null);
						current_stageinstruction.put("exedivstage1", null);
						current_stageinstruction.put("exedivstage2", null);
						current_stageinstruction.put("exedivstage3", null);
						current_stageinstruction.put("exedivstage4", null);
						current_stageinstruction.put("memorystage", null);
					}
					if(instwb.operation.equals("ADD") || instwb.operation.equals("SUB") ||instwb.operation.equals("DIV")||instwb.operation.equals("MUL")) {
						ZF = instwb.zeroflag;
					}
				}
				
				if (current_stageinstruction.get("memorystage") != null) {
					Instructioninfo instmem = current_stageinstruction.get("memorystage");
					Instructioninfo instruct3= current_stageinstruction.get("writebackstage");
					
					String destination = instmem.dest;
					Registersfile regist = getregister(destination);
					
					if(regist != null) {
						regist.notvalid = false;
						regist.forwardvalue = false;
					}
					
					if(instmem.takenvalue) {
						regist.value = instmem.result;
					}
					
					if(instmem.operation.equals("ADD") || instmem.operation.equals("SUB") ||instmem.operation.equals("DIV")||instmem.operation.equals("MUL")) {
						ZF = instmem.zeroflag;
					}
					
					
					switch(instmem.operation) {
						case "HALT":
						current_stageinstruction.put("fetchstage", null);
						current_stageinstruction.put("decodestage", null);
						break;
						case "STORE":
							Memoryfile stmem = getmemory("M"+(instmem.sourcevalue2+ instmem.literal)/4);
							stmem.value = instmem.sourcevalue1;
							break;
							
						case "LOAD":
							Registersfile regmemldest = getregister(instmem.dest);
							Memoryfile  regmemval = getmemory("M"+(instmem.sourcevalue1+ instmem.literal)/4);
							instmem.result= ((int)regmemval.value);
							instmem.takenvalue = true;
							regmemldest.forwardvalue = false;
							instmem.isforward = false;
							break;
					}
					
					
					 
				}
				
				
				if(current_stageinstruction.get("exedivstage4") != null) {
					Instructioninfo insted4= current_stageinstruction.get("exedivstage4");
					Registersfile destind4 = getregister(insted4.dest);
					destind4.forwardvalue = true;
					insted4.isforward = true;
					}
				
				if(current_stageinstruction.get("exedivstage3") != null) {
					Instructioninfo insted3= current_stageinstruction.get("exedivstage3");
					}
				
				if(current_stageinstruction.get("exedivstage2") != null) {
					Instructioninfo insted2= current_stageinstruction.get("exedivstage2");
					}
				
				
				
				
				
				if(current_stageinstruction.get("exedivstage1") != null) {
					Instructioninfo insted1= current_stageinstruction.get("exedivstage1");
					Registersfile destind1 = null;
					if (insted1.dest != null) {
						destind1 = getregister(insted1.dest);
							destind1.notvalid = true; 
					}
					
					
					switch(insted1.operation) {
						case "DIV":
						insted1.result = insted1.sourcevalue1 / insted1.sourcevalue2;
							insted1.takenvalue = true;
							
							if (insted1.result == 0) {
								insted1.zeroflag= false;
							}else {
								insted1.zeroflag = true;
							}
						
					}
				
				
				}
				
				
				
				if(current_stageinstruction.get("exemulstage2") != null) {
					Instructioninfo instem2= current_stageinstruction.get("exemulstage2");
					Registersfile destinm2 = getregister(instem2.dest);
					destinm2.forwardvalue = true;
					instem2.isforward = true;
					}
				
				
				
				
				if(current_stageinstruction.get("exemulstage1") != null) {
					Instructioninfo instem1= current_stageinstruction.get("exemulstage1");
					Registersfile destinm1 = null;
					if (instem1.dest != null) {
						destinm1 = getregister(instem1.dest);
						destinm1.notvalid = true;
					}
					
					
					switch(instem1.operation) {
						case "MUL":
						instem1.result = instem1.sourcevalue1 * instem1.sourcevalue2;
							instem1.takenvalue = true;
							if(instem1.result == 0) {
								instem1.zeroflag= false;
							}else {
								instem1.zeroflag = true;
							}
						
					}
				
				
				}
				
				
				if(current_stageinstruction.get("exe1stage") != null) {
					Instructioninfo inste1= current_stageinstruction.get("exe1stage");
					Registersfile destin = null;
					if (inste1.dest != null) {
						destin = getregister(inste1.dest);
						
							destin.notvalid = true; 
						
					}
					
					
					switch(inste1.operation) {
						
						case "MOVC":
							inste1.result = inste1.literal;
							inste1.takenvalue = true;
							destin.forwardvalue = true;
							inste1.isforward = true;
						break;
						case "ADD":
							inste1.result = inste1.sourcevalue1 + inste1.sourcevalue2;
							inste1.takenvalue = true;
							destin.forwardvalue = true;
							inste1.isforward = true;
							if (inste1.result == 0) {
								inste1.zeroflag= false;
							}else {
								inste1.zeroflag = true;
							}
						break;
						case "SUB":
							inste1.result = inste1.sourcevalue1 - inste1.sourcevalue2;
							inste1.takenvalue = true;
							destin.forwardvalue = true;
							inste1.isforward = true;
							if (inste1.result == 0) {
								inste1.zeroflag= false;
							}else {
								inste1.zeroflag = true;
							}
						break;
						case "AND":
							inste1.result = inste1.sourcevalue1 & inste1.sourcevalue2;
							inste1.takenvalue = true;
							destin.forwardvalue = true;
							inste1.isforward = true;
							
						break;
						case "OR":
							inste1.result = inste1.sourcevalue1 | inste1.sourcevalue2;
							inste1.takenvalue = true;
							destin.forwardvalue = true;
							inste1.isforward = true;
							
						break;
						case "EXOR":
							inste1.result = inste1.sourcevalue1 ^ inste1.sourcevalue2;
							inste1.takenvalue = true;
							destin.forwardvalue = true;
							inste1.isforward = true;
		
						break;
						case "LOAD":
							inste1.result = inste1.sourcevalue1 + inste1.sourcevalue2;
							inste1.takenvalue = true;
						break;
						case "STORE":
							inste1.result = inste1.literal + inste1.sourcevalue2;
						break;
						case "JUMP":							
							this.pc = ((inste1.sourcevalue1 + inste1.literal)/4)*4;
							current_stageinstruction.put("fetchstage", null);
							current_stageinstruction.put("decodestage", null);
						break;
						
						case "JAL":
						
							inste1.result=pc_value;
							inste1.takenvalue = true;
							this.pc = ((inste1.sourcevalue1 + inste1.literal)/4)*4;
							current_stageinstruction.put("fetchstage", null);
							current_stageinstruction.put("decodestage", null);
						break;
						
						case "BNZ":
							boolean zfvalue = true;

							Instructioninfo instumem = current_stageinstruction.get("memorystage");
							Instructioninfo instuwb = current_stageinstruction.get("writebackstage");
							switch(zero_flag){
								case -1:
									zfvalue=ZF;
								break;
								
								case 1:
									zfvalue= instumem.zeroflag;
								break;
								
								case 2:
									zfvalue = instuwb.zeroflag;
								break;
								
							}
							
							if(zfvalue == true) {
								this.pc = inste1.instruction_number + ((inste1.literal/4)*4);
								current_stageinstruction.put("fetchstage", null);
								current_stageinstruction.put("decodestage", null);
							}
						break;
						
						case "BZ":
						
							boolean zfvalue1 = true;

							Instructioninfo instumem1 = current_stageinstruction.get("memorystage");
							Instructioninfo instuwb1 = current_stageinstruction.get("writebackstage");
							switch(zero_flag){
								case -1:
									zfvalue1 = ZF;
								break;
								
								case 1:
									zfvalue1 = instumem1.zeroflag;
								break;
								
								case 2:
									zfvalue1 = instuwb1.zeroflag;
								break;
								
							}
							
							if(zfvalue1 == false) {
								this.pc = inste1.instruction_number +((inste1.literal/4)*4);
								current_stageinstruction.put("fetchstage", null);
								current_stageinstruction.put("decodestage", null);
							}
						break;
						case "HALT":
						{
							current_stageinstruction.put("fetchstage", null);
							current_stageinstruction.put("decodestage", null);
						}
						break;
						
					}
				}
				
				for (int i=0;i<17;i++){
					if (current_issues.get(i) != null){
						IssueQueue isq1=current_issues.get(i);
						
					}
				}
				
				
		
				
				if(current_stageinstruction.get("decodestage") != null) {
					Instructioninfo instds = current_stageinstruction.get("decodestage");
					Instructioninfo instdexe1 = current_stageinstruction.get("exe1stage");
					Instructioninfo instdmul1 = current_stageinstruction.get("exemulstage1");
					Instructioninfo instdmul2 = current_stageinstruction.get("exemulstage2");
					Instructioninfo instddiv1 = current_stageinstruction.get("exedivstage1");
					Instructioninfo instddiv2 = current_stageinstruction.get("exedivstage2");
					Instructioninfo instddiv3 = current_stageinstruction.get("exedivstage3");
					Instructioninfo instddiv4 = current_stageinstruction.get("exedivstage4");
					Instructioninfo instdmem = current_stageinstruction.get("memorystage");
					//Instructioninfo instdwriteback = current_stageinstruction.get("writebackstage");
					switch(instds.operation) {
						case "BZ":
						case "BNZ":
							if((instdexe1 != null) && (instdexe1.operation.equals("ADD") || instdexe1.operation.equals("SUB") ) ) {
								instds.stalled = false;
								zero_flag = 1;
							} else if((instdmul1 != null) && (instdmul1.operation.equals("MUL"))) {
								instds.stalled = true;
								zero_flag = 0;
							} else if ((instdmul2 != null) && (instdmul2.operation.equals("MUL"))) {
								instds.stalled = false;
								zero_flag = 1;
							} else if((instddiv1 != null) && (instddiv1.operation.equals("DIV"))) {
								instds.stalled = true;
								zero_flag = 0;
							} else if((instddiv2 != null) && (instddiv2.operation.equals("DIV"))) {
								instds.stalled = true;
								zero_flag = 0;
							} else if((instddiv3 != null) && (instddiv3.operation.equals("DIV"))) {
								instds.stalled = true;
								zero_flag = 0;
							} else if((instddiv4 != null) && (instddiv4.operation.equals("DIV"))) {
								instds.stalled = false;
								zero_flag = 1;
							}else if ((instdmem != null) && (instdmem.operation.equals("ADD") || instdmem.operation.equals("SUB") || instdmem.operation.equals("DIV") || instdmem.operation.equals("MUL") )) {
								instds.stalled = false;
								zero_flag = 2;
							} else {
								instds.stalled = false;
								zero_flag=-1;
							}
						break;
						
						case "HALT":
						{
							if(instddiv1 != null || instddiv2 != null || instddiv3 != null) {
								instds.stalled = true;
							}else {instds.stalled = false;}
							current_stageinstruction.put("fetchstage",null);
						}
						break;
						
						case "JUMP":
						{
							String source13 = instds.source1;
							if(source13 != null && instds.s1flag==false) {
								if(!getregister(source13).notvalid || (getregister(source13).notvalid && getregister(source13).forwardvalue )) {
									
									if((getregister(source13).notvalid && getregister(source13).forwardvalue ) == true) {
									 //System.out.println("i am ok here "+ source11 +" exe1 value"+instdexe1.dest+"  "+instdexe1.result);
											if(instdmul2!= null && instdmul2.dest!=null && instdmul2.dest.equals(source13) && instdmul2.isforward == true && (instdmul2.operation.equals("MUL"))) {
												instds.sourcevalue1 = instdmul2.result;
												instds.s1flag=true;
											}else if(instddiv4!= null && instddiv4.dest!=null && instddiv4.dest.equals(source13) && instddiv4.isforward == true && (instddiv4.operation.equals("DIV"))) {
												instds.sourcevalue1 = instddiv4.result;
												instds.s1flag=true;
											}else if(instdexe1!= null && instdexe1.dest !=null && instdexe1.dest.equals(source13) && instdexe1.isforward == true && ( instdexe1.operation.equals("ADD")|| instdexe1.operation.equals("MOVC") || instdexe1.operation.equals("SUB")|| instdexe1.operation.equals("AND")|| instdexe1.operation.equals("OR")|| instdexe1.operation.equals("EXOR")|| instdexe1.operation.equals("JAL"))) {
											instds.sourcevalue1 = instdexe1.result;
											instds.s1flag=true;
											}else {instds.s1flag=false;}
									}else {instds.sourcevalue1 = getregister(source13).value;
											instds.s1flag=true;}
								}else{instds.s1flag=false;}
							}else{instds.s1flag=true;}
							
							
							if (instds.s1flag==true) {
								instds.stalled=false;
							}else {instds.stalled=true;}
						}
						
						break;
						case "JAL":
						case "LOAD":
						{ 
							String source12 = instds.source1;
							String dest2 = instds.dest;
							pc_value = this.pc-4;
							if(source12 != null && instds.s1flag==false) {
								if(!getregister(source12).notvalid || (getregister(source12).notvalid && getregister(source12).forwardvalue )) {
									
									if((getregister(source12).notvalid && getregister(source12).forwardvalue ) == true) {
									 //System.out.println("i am ok here "+ source11 +" exe1 value"+instdexe1.dest+"  "+instdexe1.result);
											if(instdmul2!= null && instdmul2.dest!=null && instdmul2.dest.equals(source12) && instdmul2.isforward == true && (instdmul2.operation.equals("MUL"))) {
												instds.sourcevalue1 = instdmul2.result;
												instds.s1flag=true;
											}else if(instddiv4!= null && instddiv4.dest!=null && instddiv4.dest.equals(source12) && instddiv4.isforward == true && (instddiv4.operation.equals("DIV"))) {
												instds.sourcevalue1 = instddiv4.result;
												instds.s1flag=true;
											}else if(instdexe1!= null && instdexe1.dest !=null && instdexe1.dest.equals(source12) && instdexe1.isforward == true && ( instdexe1.operation.equals("ADD")|| instdexe1.operation.equals("MOVC")|| instdexe1.operation.equals("SUB")|| instdexe1.operation.equals("AND")|| instdexe1.operation.equals("OR")|| instdexe1.operation.equals("EXOR")|| instdexe1.operation.equals("JAL"))) {
											instds.sourcevalue1 = instdexe1.result;
											instds.s1flag=true;
											}else {instds.s1flag=false;}
									}else {instds.sourcevalue1 = getregister(source12).value;
											instds.s1flag=true;}
								}else{instds.s1flag=false;}
							}else{instds.s1flag=true;}
							
							if(dest2!=null) {
								if(!getregister(dest2).notvalid ){
									instds.dflag = true;
								}else {instds.dflag = false;}
							}else {instds.dflag = true;}
							
							if (instds.dflag==true && instds.s1flag==true) {
								instds.stalled=false;
							}else {instds.stalled=true;}
						
						}
						break;
						
						case "STORE":
						{
							String source14 = instds.source1;
							String source24 = instds.source2;
							
							if(source14 != null && instds.s1flag==false) {
								if(!getregister(source14).notvalid || (getregister(source14).notvalid && getregister(source14).forwardvalue )) {
									
									if((getregister(source14).notvalid && getregister(source14).forwardvalue ) == true) {
									 //System.out.println("i am ok here "+ source11 +" exe1 value"+instdexe1.dest+"  "+instdexe1.result);
											if(instdmul2!= null && instdmul2.dest!=null && instdmul2.dest.equals(source14) && instdmul2.isforward == true && (instdmul2.operation.equals("MUL"))) {
												instds.sourcevalue1 = instdmul2.result;
												instds.s1flag=true;
											}else if(instddiv4!= null && instddiv4.dest!=null && instddiv4.dest.equals(source14) && instddiv4.isforward == true && (instddiv4.operation.equals("DIV"))) {
												instds.sourcevalue1 = instddiv4.result;
												instds.s1flag=true;
											}else if(instdexe1!= null && instdexe1.dest !=null && instdexe1.dest.equals(source14) && instdexe1.isforward == true && ( instdexe1.operation.equals("ADD")|| instdexe1.operation.equals("MOVC")|| instdexe1.operation.equals("SUB")|| instdexe1.operation.equals("AND")|| instdexe1.operation.equals("OR")|| instdexe1.operation.equals("EXOR")|| instdexe1.operation.equals("JAL"))) {
											instds.sourcevalue1 = instdexe1.result;
											instds.s1flag=true;
											}else {instds.s1flag=false;}
									}else {instds.sourcevalue1 = getregister(source14).value;
											instds.s1flag=true;}
								}else{instds.s1flag=false;}
							}else{instds.s1flag=true;}
							
							if(source24 != null && instds.s2flag==false) {
								if(!getregister(source24).notvalid || (getregister(source24).notvalid && getregister(source24).forwardvalue )) {
									
									if((getregister(source24).notvalid && getregister(source24).forwardvalue ) == true) {
									 //System.out.println("i am ok here "+ source11 +" exe1 value"+instdexe1.dest+"  "+instdexe1.result);
											if(instdmul2!= null && instdmul2.dest!=null && instdmul2.dest.equals(source24) && instdmul2.isforward == true && (instdmul2.operation.equals("MUL"))) {
												instds.sourcevalue2 = instdmul2.result;
												instds.s2flag=true;
											}else if(instddiv4!= null && instddiv4.dest!=null && instddiv4.dest.equals(source24) && instddiv4.isforward == true && (instddiv4.operation.equals("DIV"))) {
												instds.sourcevalue2 = instddiv4.result;
												instds.s2flag=true;
											}else if(instdexe1!= null && instdexe1.dest !=null && instdexe1.dest.equals(source24) && instdexe1.isforward == true && ( instdexe1.operation.equals("ADD")|| instdexe1.operation.equals("MOVC")|| instdexe1.operation.equals("SUB")|| instdexe1.operation.equals("AND")|| instdexe1.operation.equals("OR")|| instdexe1.operation.equals("EXOR")|| instdexe1.operation.equals("JAL"))) {
											instds.sourcevalue2 = instdexe1.result;
											instds.s2flag=true;
											}else {instds.s2flag=false;}
									}else {instds.sourcevalue2 = getregister(source24).value;
											instds.s2flag=true;}
								}else{instds.s2flag=false;}
							}else{instds.s2flag=true;}
							
							if (instds.s1flag==true&& instds.s2flag==true) {
								instds.stalled=false;
							}else {instds.stalled=true;}
						}
						break;
						
						case "ADD":
						case "SUB":
						case "OR":
						case "AND":
						case "DIV":
						case "EXOR":
						case "MUL":
							String source11 = instds.source1;
							String source22 = instds.source2;
							String dest1=instds.dest;
							instds.source1Phy="empty";
							instds.source2Phy= "empty";
							instds.destPhy = allocate();
							RenameTable rnd = new RenameTable();
						    rnd.arcregister = dest1;
						    rnd.phyregist = instds.destPhy;
						    rnd.commit=false;
						    renametable.add(rnd);
							for(int i=0;i<renametable.size();i++) {
								//System.out.println((renametable.get(i).commit==false));
							if((renametable.get(i).arcregister).equals(source11) && (renametable.get(i).commit==false)){
								//System.out.println("i am here");
								instds.source1Phy=renametable.get(i).phyregist;
								break;
							}
						}
						if((instds.source1Phy).equals("empty")){
						instds.source1Phy = allocate();
						RenameTable rn1 = new RenameTable();
						rn1.arcregister = source11;
						rn1.phyregist = instds.source1Phy;
						rn1.commit=false;
						renametable.add(rn1);
							}
						
						
							
						for(int i=0;i<renametable.size();i++) {
						if((renametable.get(i).arcregister).equals(source22) && (renametable.get(i).commit==false)){
							instds.source2Phy=renametable.get(i).phyregist;
							break;
							}
						}
						if((instds.source2Phy).equals("empty")){
						instds.source2Phy = allocate();
						RenameTable rn2 = new RenameTable();
						rn2.arcregister = source22;
						rn2.phyregist = instds.source1Phy;
						rn2.commit=false;
						renametable.add(rn2);
						}
							
						break;
						
						case "MOVC":
						String dest11 = instds.dest;
						instds.destPhy = allocate();
						instds.entryValid = true;
						RenameTable rn = new RenameTable();
						rn.arcregister = dest11;
						rn.phyregist = instds.destPhy;
						rn.commit=false;
						renametable.add(rn);
						//System.out.println("wwwwwwwwwwwww  "+instds.destPhy);
					}
				}
				cycle_counter++;
			}while((cycle_counter != n+1)&& !hbreak);
			
		}
		

		
		public void dispaly_content() {
			
			int count;
		for( count = 0; count < displaylist.size(); count++) {

			System.out.println();
			Displayinfo displayStage = displaylist.get(count);
			System.out.println("Cycles: " + displayStage.cycles+"\n");
			System.out.println("FetchStage\t\t" + displayStage.fetch_stage);
			System.out.println("DecodeStage\t\t" + displayStage.decode_stage);
			System.out.println("Rename Table");
			for(int i=0; i<displayStage.rntable.size();i++) {
				System.out.println(displayStage.rntable.get(i));
			}
			System.out.println("Issue Queue");
			for(int i=0; i<displayStage.isqtable.size();i++) {
				System.out.println(displayStage.isqtable.get(i));
			}
			System.out.println("Reorder Buffer");
			for(int i=0; i<displayStage.robtable.size();i++) {
				System.out.println(displayStage.robtable.get(i));
			}
			System.out.println("LSQ");
			System.out.println("commite \t\t" + displayStage.memory_stage);
			System.out.println("ALUStage\t\t" + displayStage.execution1_stage);
			System.out.println("MUL1Stage\t\t" + displayStage.executionmul1_stage);
			System.out.println("MUL2Stage\t\t" + displayStage.executionmul2_stage);
			System.out.println("DIV1Stage\t\t" + displayStage.executiondiv1_stage);
			System.out.println("DIV2Stage\t\t" + displayStage.executiondiv2_stage);
			System.out.println("DIV3Stage\t\t" + displayStage.executiondiv3_stage);
			System.out.println("DIV4Stage\t\t" + displayStage.executiondiv4_stage);
			System.out.println("MemoryStage\t\t" );
			

			System.out.println("*********************************************************" + "\n");
			
		}
		
			
			
			address dbaddress=new address();
			
			System.out.println("Register values are:");
			for(int i=0; i<=15 ;i++)
			{
				System.out.println(reg.get(i).regname+"  "+reg.get(i).value);
			}
			
			for(int j=0;j<=999;j++) {
				System.out.println(mem.get(j).memname+"  "+mem.get(j).value);
			}
			
			
		}
		
		void loadinstructions(String fn)
		{
			instruction = new ArrayList<>();
			instruction.clear();
			try{	
			BufferedReader filereader = new BufferedReader(new FileReader(fn));
			String Line;
			int inum;
			inum=4000;
			int i1=0;
			
			while((Line = filereader.readLine()) != null)
			{
				String curr=Line;
				Line = Line.replace(","," ");
				Line = Line.replace("#","");
				String[] instset = Line.split(" ");
				Instructioninfo instruct = new Instructioninfo();
				instruct.instruction_number=inum;
				instruct.zeroflag=false; 
				inum = inum + 4;
				
				instruct.operation=instset[0]; 
				instruct.stalled=false;
				instruct.takenvalue = false;
				instruct.s1flag=false;
				instruct.s2flag=false;
				instruct.dflag=false;
				instruct.entryValid = false;
				instruct.stage=("Read");
				instruct.index = i1;
				instruct.current_inst="(I"+Integer.toString(i1)+")  "+curr;
				switch(instset[0]){
				
				case "ADD":
				case "SUB":
				case "AND":
				case "OR":
				case "EXOR":
				case "MUL":
				case "DIV":
				instruct.dest=instset[1];
				instruct.source1=instset[2];
				instruct.source2=instset[3];
				break;
				case "BZ":
				case "BNZ":
				instruct.literal = Integer.parseInt(instset[1]);
				instruct.dest=null;
				instruct.source1=null;
				instruct.source2=null;
				break;
				case "JUMP":
				instruct.source1 = instset[1];
				instruct.literal = Integer.parseInt(instset[2]);
				instruct.dest=null;
				instruct.source2=null;
				break;
				case "LOAD":
				case "JAL":
				instruct.dest=instset[1];
				instruct.source1=instset[2];
				instruct.literal = Integer.parseInt(instset[3]);
				instruct.source2=null;
				break;
				case "STORE":
				instruct.literal = Integer.parseInt(instset[3]);
				instruct.source1=instset[1];
				instruct.source2=instset[2];
				instruct.dest=null;
				break;
				case "MOVC":
				instruct.dest=instset[1];
				instruct.source1=null;
				instruct.source2= null;
				instruct.literal = Integer.parseInt(instset[2]);
				break;
				case "HALT":
				instruct.dest= null;
				instruct.source1=null;
				instruct.source2= null;
				break;
				default :
				System.out.println("exception in the file loading"+ i1);
				}
				instruction.add(instruct);
				i1++;
			}
		}catch(Exception r){
			
		}
		}	
		
  public static void main(String args[]) {
		
		String file = "";
		int select;
		int ncycles;
		inorder in_order= new inorder();
		System.out.println("Enter the file name");
		Scanner scan= new Scanner(System.in);
		file=scan.nextLine();
		in_order.loadinstructions(file);
		
		
		for(;;){
		 System.out.println("1. intialization " );
		 System.out.println("2. simulation");
		 System.out.println("3. display content");
		 System.out.println("Enter the command number :");
		select = scan.nextInt();
        switch(select) {
			case 1: 
			    in_order.init();
				System.out.println("Initialization Done \n");
			break;
			case 2:
			     System.out.println("Enter the number of cycles");
		         ncycles=scan.nextInt();
				 in_order.pipeline(ncycles);
			break;
			case 3:
			     in_order.dispaly_content();
			break;
			default:
			System.out.println("wrong command");
		}
		
    }
}
}