package codegen.assem;

import static util.List.list;
import util.IndentingWriter;
import util.List;
import ir.temp.Label;
import ir.temp.Temp;

public class A_MOVE extends Instr {
	public Temp dst;   
	public Temp src;

	public A_MOVE(String a, Temp d, Temp s) {
		super(a); dst=d; src=s;
	}
	public List<Temp> use()    {return list(src);}
	public List<Temp> def()    {return list(dst);}
	public List<Label> jumps() {return null;}

	@Override
	public void dump(IndentingWriter out) {
		if (dst.getColor()!=null && dst.getColor().equals(src.getColor())) {
			out.print("# "); // comment out this redundant move
		}
		super.dump(out);
	}
}
