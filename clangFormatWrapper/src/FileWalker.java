import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;

class FileWalker
{
	static void walk(String path, FileVisitor fileVisitor) throws IOException, URISyntaxException
	{
		File file = new File(path);

		if (file.getParentFile().getName().equals(".git")) {
			return;
		}

		if (!file.isDirectory()) {
			String name = file.getName();
			if (name.endsWith(".java") || name.endsWith(".c") || name.endsWith(".h") || name.endsWith(".cpp") ||
				name.endsWith(".hpp")) {
				fileVisitor.visit(file);
				return;
			}
		}

		File[] list = file.listFiles();
		if (list == null)
			return;

		for (File f : list) {
			walk(f.getAbsolutePath(), fileVisitor);
		}
	}

	@FunctionalInterface
	public interface FileVisitor {
		void visit(File file) throws IOException, URISyntaxException;
	}
}
