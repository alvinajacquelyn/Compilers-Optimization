package comp0012.main;
import org.apache.bcel.classfile.*;
import org.apache.bcel.generic.*;

public class ValueGetter {
    
	/**
	 * GET PREVIOUS VALUES 
	**/
	/** ****************************************************************************************************************** **/
	//This will also recognize BIPUSH and SIPUSH int values as well as ICONST values
	public static Object getPrevInt(InstructionHandle handle, InstructionList instList, ConstantPoolGen cpgen)
	{
		if (handle.getInstruction() instanceof LDC)
		{
			return (int) ((LDC)(handle.getInstruction())).getValue(cpgen);
		} 
		else if (handle.getInstruction() instanceof BIPUSH) 
		{
			return (int) ((BIPUSH)(handle.getInstruction())).getValue();
		} 
		else if (handle.getInstruction() instanceof SIPUSH) 
		{
			return (int) ((SIPUSH)(handle.getInstruction())).getValue();
		}
		else if (handle.getInstruction() instanceof ICONST)
		{
			return (int) ((ICONST) handle.getInstruction()).getValue();
		}
		else
		{
			return null;
		}
	}

	public static Object getPrevLong(InstructionHandle handle, InstructionList instList, ConstantPoolGen cpgen)
	{
		if (handle.getInstruction() instanceof LDC2_W)
		{
			return (long) ((LDC2_W)(handle.getInstruction())).getValue(cpgen);

		}	else {
			return null;
		}
	}

	public static Object getPrevDouble(InstructionHandle handle, InstructionList instList, ConstantPoolGen cpgen)
	{
		if (handle.getInstruction() instanceof LDC2_W)
		{
			return (double) ((LDC2_W)(handle.getInstruction())).getValue(cpgen);
		
		} else {
			return null;
		}
	}

	public static Object getPrevFloat(InstructionHandle handle, InstructionList instList, ConstantPoolGen cpgen)
	{
		if (handle.getInstruction() instanceof LDC)
		{
			return (float) ((LDC)(handle.getInstruction())).getValue(cpgen);

		}else {
			return null;
		}
	}
}
