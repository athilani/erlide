package org.erlide.jinterface;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;

/**
 * Takes a fully qualified class name and generates an erlang stub module that
 * uses rpc to call the class and its instances.
 */
public class StubGenerator {

	public static String generate(Class clazz) {
		return generate(clazz.getName(), clazz.getClassLoader());
	}

	public static String generate(Class clazz, ClassLoader cl) {
		return generate(clazz.getName(), cl);
	}

	public static String generate(String className, ClassLoader cl) {
		try {
			Class clazz = Class.forName(className, true, cl);

			String moduleName = module(clazz);

			return generate(clazz, moduleName);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			return e.getMessage();
		}
	}

	private static String generate(Class clazz, String moduleName) {
		StringBuffer buf = new StringBuffer();
		buf.append(String.format("-module(%s).%n-compile(export_all).%n%n",
				moduleName));

		// TODO add constructors
		// TODO support overloaded methods with same # of parameters

		Method[] methods = clazz.getMethods();
		for (Method method : methods) {
			int mod = method.getModifiers();
			boolean statik = Modifier.isStatic(mod);
			Class<?>[] params = method.getParameterTypes();

			if (!method.getReturnType().getName().equals("void")) {
				buf.append("  %% ");
				buf.append(" returns " + method.getReturnType().getName()
						+ "\n");
			}

			StringBuffer suffix = new StringBuffer();
			// for (int i = 0; i < at.length; i++) {
			// args.append("_");
			// args.append(at[i].getName());
			// }
			buf.append("'" + method.getName() + suffix + "'(");
			if (statik) {
			} else {
				buf.append("Obj");
			}

			printParams(statik, buf, params);
			buf.append(") ->\n");

			if (method.getReturnType() == Void.TYPE) {
				buf.append("  jrpc:cast(");
			} else {
				buf.append("  jrpc:call(");
			}
			if (statik) {
				buf.append("<<\"" + clazz.getName() + "\">>, ");
			} else {
				buf.append("Obj, ");
			}
			StringBuffer args = new StringBuffer();
			Class<?>[] at = method.getParameterTypes();
			for (int i = 0; i < at.length; i++) {
				args.append("\"" + at[i].getName() + "\"");
				if (i < at.length - 1) {
					args.append(", ");
				}
			}
			buf.append("{<<\"" + method.getName() + "\">>, [").append(args)
					.append("]}, [");
			printParams(true, buf, params);
			buf.append("]).\n\n");
		}

		return buf.toString();

	}

	private static void printParams(boolean nohdr, StringBuffer buf,
			Class<?>[] params) {
		if (!nohdr && params.length > 0) {
			buf.append(", ");
		}
		if (params.length > 0) {
			buf.append("P0");
		}
		for (int i = 1; i < params.length; i++) {
			buf.append(", " + "P" + i);
		}
	}

	public static String module(Class clazz) {
		return clazz.getName().replaceAll("\\.", "_");
	}

	public static void tofile(Class clazz, IFile out) {

		String s = generate(clazz);
		String fn = module(clazz);

		InputStream source = new ByteArrayInputStream(s.getBytes());
		try {
			out.create(source, false, null);
		} catch (CoreException e) {
			e.printStackTrace();
		}

	}

}
