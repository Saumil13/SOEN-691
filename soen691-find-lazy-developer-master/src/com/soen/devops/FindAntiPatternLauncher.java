package com.soen.devops;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ForStatement;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.LineComment;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.WhileStatement;
import org.eclipse.jdt.internal.compiler.ast.Block;


public class FindAntiPatternLauncher {


	private static final String[] extensions = new String[] { "java" };

	public static void main(String[] args) throws IOException, CoreException {

		File dir = new File(args[0]);

		List<File> javaFiles = (List<File>) FileUtils.listFiles(dir, extensions, true);

		for (File src : javaFiles) {
			astParse(src);
		}

	}

	private static String readFileToString(File file) throws IOException {
		StringBuilder fileData = new StringBuilder(1000);
		BufferedReader reader = new BufferedReader(new FileReader(file));

		char[] buf = new char[10];
		int numRead = 0;
		while ((numRead = reader.read(buf)) != -1) {
			String readData = String.valueOf(buf, 0, numRead);
			fileData.append(readData);
			buf = new char[1024];
		}

		reader.close();

		return fileData.toString();
	}

	private static void astParse(File file) throws IOException {

		String fileData = readFileToString(file);

		ASTParser parser = ASTParser.newParser(AST.JLS3);
		parser.setSource(fileData.toCharArray());
		parser.setKind(ASTParser.K_COMPILATION_UNIT);

		final CompilationUnit cu = (CompilationUnit) parser.createAST(null);

		cu.accept(new ASTVisitor() {

			Set names = new HashSet();

			public boolean visit(VariableDeclarationFragment node) {
				SimpleName name = node.getName();
				this.names.add(name.getIdentifier());
				//System.out.println("Declaration of '" + name + "' at line" + cu.getLineNumber(name.getStartPosition()));
				return false; // do not continue
			}

			public boolean visit(SimpleName node) {
				if (this.names.contains(node.getIdentifier())) {
					
					if (node.toString().contains("namedParameterJdbcTemplate")) {

						System.out.println(
								"Usage of '" + node + "' at line " + cu.getLineNumber(node.getStartPosition())
								 +" end line: "+cu.getLineNumber(node.getLength()));
						
					}
				}
				return true;
			}

			public boolean visit(ForStatement node) {

				// System.out.println("ForStatement -- content:" +
				// node.toString());

				ArrayList<Integer> al = new ArrayList<Integer>();
				al.add(node.getStartPosition());
				al.add(node.getLength());

				//System.out.println(node.toString());
				
				if(node.toString().contains("namedParameterJdbcTemplate")){
					System.out.println("Warning! There may be EAGER fatch logic.");
				}
				
				// System.out.println(al);
				// System.out.println(al);

				System.out.println("ForStatement -- content Line number:" + node.getStartPosition());

				return false;
			}

			public boolean visit(WhileStatement node) {

				//System.out.println("WhileStatement -- content:" + node.toString());

				ArrayList<Integer> al = new ArrayList<Integer>();
				al.add(node.getStartPosition());
				al.add(node.getLength());

				if(node.toString().contains("namedParameterJdbcTemplate")){
					System.out.println("Warning! There may be EAGER fatch logic.");
				}
				
				// System.out.println(al);
				System.out.println("WhileStatement -- content Line number:" + node.getStartPosition());

				return false;
			}

		});


	}

}