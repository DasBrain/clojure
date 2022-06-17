package clojure.lang;

import static java.lang.invoke.MethodType.methodType;

import java.lang.invoke.CallSite;
import java.lang.invoke.ConstantCallSite;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;

public final class IndySupport {
	private IndySupport() {}
	
	private static final Keyword redefKey = Keyword.intern(null, "redef");
	private static final MethodHandle IFN;
	static {
		try {
			IFN = MethodHandles.lookup().findVirtual(Var.class, "fn", methodType(IFn.class));
		} catch (ReflectiveOperationException roe) {
			throw new ExceptionInInitializerError(roe);
		}
	}
	
	public static CallSite varInvoke(MethodHandles.Lookup l, String invoke, MethodType mt, String ns, String name) throws Throwable {
		Var v = RT.var(ns, name);
		MethodHandle t = l.findVirtual(IFn.class, "invoke", mt);
		String type = "indirect";
		if (!v.isDynamic() && !RT.booleanCast(RT.get(v.meta(), redefKey, false))) {
			t = t.bindTo(v.fn());
			type = "direct";
		} else {
			t = MethodHandles.filterArguments(t, 0, IFN).bindTo(v);
		}
		System.out.println("Binding " + ns + "/" + name + mt.toMethodDescriptorString() + " " + type);
		return new ConstantCallSite(t);
		
	}
}
